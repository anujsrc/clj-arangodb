(ns clj-arangodb.arangodb.aql-test
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.query :as q]
            [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.cursor :as cursor]
            [clj-arangodb.arangodb.helper :as h]
            [clj-arangodb.arangodb.test-data :as td]
            [clj-arangodb.arangodb.aql :as aql]
            [clojure.test :refer :all]))

(deftest let-test-1
  (h/with-temp-db [db "testDB"]
    (let [qry [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                     "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                     "z" 100]
               [:RETURN [:SUM ["x" "y" "z"]]]]]
      (is (= 158 (cursor/first (d/query db qry Integer)))))))

(deftest let-test-2
  (h/with-temp-db [db "testDB"]
    (let [qry [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                     "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                     "z" [1 2 3 4 5]]
               [:RETURN [:SUM ["x" "y" [:SUM "z"]]]]]]
      (is (= 73 (cursor/first (d/query db qry Integer)))))))

(deftest can-encode-nested-funs
  (is (= "sum([sum([1,2,3]),sum([2,3,4])])"
         (aql/serialize [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]])))
  (h/with-temp-db [db "testDB"]
    (let [query [:return [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]]]]
      (is (= 15 (cursor/first (d/query db query Integer)))))))

(deftest obj-test
  (h/with-temp-db [db "testDB"]
    (let [query-1 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {"a" [:SUM [1 2 3]] "b" "b"}]]
          query-2 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {:a [:SUM [1 2 3]] :b "b"}]]]
      (is (= (ad/deserialize-doc (cursor/first (d/query db query-1)))
             (ad/deserialize-doc (cursor/first (d/query db query-2)))
             {:a 6.0 :b 6.0})))))

(deftest neds-children-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [children #{"Robb" "Jon" "Bran" "Arya" "Sansa"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Ned\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (ad/deserialize-doc (d/query db query)))
             children)))))

(deftest group-test-1
  (h/with-db [db td/game-of-thrones-db-label]
    (is (= (->> [:FOR ["c" "Characters"]
                 [:COLLECT ["surname" "c.surname"]]
                 [:RETURN "surname"]]
                (d/query db)
                ((comp set ad/deserialize-doc)))
           (->> [:FOR ["c" "Characters"]
                 [:RETURN-DISTINCT "c.surname"]]
                (d/query db)
                ((comp set ad/deserialize-doc)))))))

(deftest group-test-2
  (h/with-db [db td/game-of-thrones-db-label]
    (let [result (->> [:FOR ["c" "Characters"]
                       [:COLLECT
                        ["surname" "c.surname"] {:into ["members" "c.name"]}]
                       [:FILTER [:NE "surname" nil]]
                       [:RETURN ["surname" "members"]]]
                      (d/query db)
                      ad/deserialize-doc
                      (into {}))]
      (is (= (count (get result "Lannister")) 4))
      (is (= (count (get result "Stark")) 6)))))

(deftest brans-parents-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [parents #{"Ned" "Catelyn"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Bran\""]]
                 [:LIMIT 1] ;; does not change anything
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (ad/deserialize-doc (d/query db query)))
             parents)))))

(deftest tywins-grandchildren-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [grandchildren #{"Joffrey"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Tywin\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [2 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (ad/deserialize-doc (d/query db query)))
             grandchildren)))))

(deftest joffrey-parents-and-grandparents-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [grandparents #{"Tywin"}
          parents #{"Cersei" "Jaime"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Joffrey\""]]
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (ad/deserialize-doc (d/query db query)))
             (set/union parents grandparents))))))


(deftest collection-size-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [query [:RETURN [:LENGTH "Characters"]]]
      (is (= (first (ad/deserialize-doc (d/query db query)))
             43)))))


(deftest cursor-stats-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:RETURN true])
          stats (cursor/get-stats res)]
      (is (= (:class stats)
             com.arangodb.entity.CursorEntity$Stats))
      (is (= (set (keys stats))
             #{:class :executionTime :filtered :fullCount
               :scannedFull :scannedIndex :writesExecuted
               :writesIgnored})))))

(deftest cursor-count-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil {:count true} Integer)]
      (is (= 5 (cursor/get-count res))))
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])]
      (is (= 5 (cursor/count res))))))

(deftest cursor-cache-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])]
      (is (= false (cursor/is-cached res))))))

(deftest cursor-collect-into-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs  (cursor/collect-into res (java.util.ArrayList.))]
      (is (= [1 2 3 4 5] xs))
      (testing "collect-into consumes the contents of the cursor"
        (is (= false (cursor/has-next res)))))
    (let [res (d/query db [:FOR ["x" [1 1 2 2 3]] [:RETURN "x"]] nil nil Integer)
          xs  (cursor/collect-into res (java.util.HashSet.))]
      (is (= #{1 2 3} xs)))))

(deftest cursor-seq-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs  (seq res)]
      (is (= (type xs) clojure.lang.LazySeq))
      (is (= [1 2 3 4 5] xs)))
    (testing "can map deserialize-doc"
      (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])
            xs  (ad/deserialize-doc res)]
        (is (= (type xs) clojure.lang.LazySeq))
        (is (= [1 2 3 4 5] xs))))))

(deftest cursor-predicate-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)]
      (is (= true (cursor/all-match res (pred (partial >= 5)))))
      (testing "can only test once"
        (is (= false (cursor/all-match res (pred nat-int?))))))))

(deftest cursor-map-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs (cursor/map res inc)]
      (is (= [1 2 3 4 5] (seq res)))
      (is (= nil (seq xs))))
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs (cursor/map res inc)]
      (is (= [2 3 4 5 6] (seq xs)))
      (is (= (seq res) nil)))    ))

(deftest cursor-first-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..3"] [:RETURN "x"]] nil nil Integer)]
      (is (= 1 (cursor/first res)))
      (is (= 2 (cursor/first res)))
      (is (= 3 (cursor/first res)))
      (is (nil? (cursor/first res))))))

(deftest cursor-foreach-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..3"] [:RETURN "x"]] nil nil Integer)
          state (atom 0)]
      (cursor/foreach res (fn [x] (swap! state + x)))
      (is (nil? (cursor/first res)))
      (is (= @state 6)))))

(deftest multi-batch-tests
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db
                       [:FOR ["x" "0..99"] [:RETURN "x"]]
                       nil
                       {:batchSize (int 5)}
                       Integer)]
      (is (= (range 100) (seq res))))
    (let [res (d/query db
                       [:FOR ["x" "1..3"] [:RETURN "x"]]
                       nil
                       {:batchSize (int 1)}
                       Integer)]
      (while (cursor/has-next? res)
        (is (nat-int? (cursor/next res)))))))
