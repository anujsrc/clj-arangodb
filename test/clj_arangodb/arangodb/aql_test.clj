(ns clj-arangodb.arangodb.aql-test
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.query :as q]
            [clj-arangodb.arangodb.helper :as h]
            [clj-arangodb.arangodb.aql :as aql]
            [clojure.test :refer :all]))

(def query-1
  [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
         "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
         "z" 100]
   [:RETURN [:SUM ["x" "y" "z"]]]])

(deftest let-test-1
  (h/with-temp-db [db "testDB"]
    (let [query [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                       "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                       "z" 100]
                 [:RETURN [:SUM ["x" "y" "z"]]]]
          res (d/query db query)]
      (is (= (first (q/->vec res))
             158.0)))))

(deftest let-test-2
  (h/with-temp-db [db "testDB"]
    (let [query [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                       "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                       "z" [1 2 3 4 5]]
                 [:RETURN [:SUM ["x" "y" [:SUM "z"]]]]]
          res (d/query db query)]
      (is (= (first (q/->vec res))
             73.0)))))

(deftest can-encode-nested-funs
  (is (= "sum([sum([1,2,3]),sum([2,3,4])])"
         (aql/serialize [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]])))
  (h/with-temp-db [db "testDB"]
    (let [query [:return [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]]]]
      (is (= (first (q/->vec (d/query db query)))
             15.0)))))

(deftest obj-test
  (h/with-temp-db [db "testDB"]
    (let [query-1 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {"a" [:SUM [1 2 3]] "b" "b"}]]
          query-2 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {:a [:SUM [1 2 3]] :b "b"}]]]
      (is (= (first (q/->vec (d/query db query-1)))
             {:a 6.0 :b 6.0}))
      (is (= (first (q/->vec (d/query db query-2)))
             {:a 6.0 :b 6.0})))))

(deftest neds-children-test
  (h/with-db [db "webapp"]
    (let [children #{"Robb" "Jon" "Bran" "Arya" "Sansa"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Ned\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (q/->vec (d/query db query)))
             children)))))

(deftest group-test-1
  (h/with-db [db "webapp"]
    (is (= (->> [:FOR ["c" "Characters"]
                 [:COLLECT ["surname" "c.surname"]]
                 [:RETURN "surname"]]
                (d/query db)
                ((comp set q/->vec)))
           (->> [:FOR ["c" "Characters"]
                 [:RETURN-DISTINCT "c.surname"]]
                (d/query db)
                ((comp set q/->vec)))))))

(deftest group-test-2
  (h/with-db [db "webapp"]
    (let [result (->> [:FOR ["c" "Characters"]
                       [:COLLECT
                        ["surname" "c.surname"] {:into ["members" "c.name"]}]
                       [:FILTER [:NE "surname" nil]]
                       [:RETURN ["surname" "members"]]]
                      (d/query db)
                      q/->vec
                      (into {}))]
      (is (= (count (get result "Lannister")) 4))
      (is (= (count (get result "Stark")) 6)))))

(deftest brans-parents-test
  (h/with-db [db "webapp"]
    (let [parents #{"Ned" "Catelyn"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Bran\""]]
                 [:LIMIT 1] ;; does not change anything
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (q/->vec (d/query db query)))
             parents)))))

(deftest tywins-grandchildren-test
  (h/with-db [db "webapp"]
    (let [grandchildren #{"Joffrey"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Tywin\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [2 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (q/->vec (d/query db query)))
             grandchildren)))))

(deftest joffrey-parents-and-grandparents-test
  (h/with-db [db "webapp"]
    (let [grandparents #{"Tywin"}
          parents #{"Cersei" "Jaime"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Joffrey\""]]
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (q/->vec (d/query db query)))
             (set/union parents grandparents))))))


(deftest collection-size-test
  (h/with-db [db "webapp"]
    (let [query [:RETURN [:LENGTH "Characters"]]]
      (is (= (first (q/->vec (d/query db query)))
             43)))))
