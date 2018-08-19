(ns clj-arangodb.arangodb.cursor-test
  (:require
   [clj-arangodb.arangodb.databases :as d]
   [clj-arangodb.arangodb.aql :as aql]
   [clj-arangodb.arangodb.adapter :as adapter]
   [clj-arangodb.arangodb.cursor :as cursor]
   [clj-arangodb.arangodb.helper :as h]
   [clojure.test :refer :all]))

(deftest cursor-stats-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:RETURN true])
          stats (cursor/get-stats res)]
      (is (= (class stats)
             com.arangodb.entity.CursorEntity$Stats))
      (is (= (set (keys (adapter/from-entity stats)))
             #{:class :executionTime :filtered :fullCount
               :scannedFull :scannedIndex :writesExecuted
               :writesIgnored})))))

(deftest cursor-count-test
  (h/with-temp-db [db "testDB"]
    (is (= 5
           (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                (d/query db {:count true} Integer)
                cursor/get-count)
           (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                (d/query db)
                cursor/count)))))

(deftest cursor-cache-test
  (h/with-temp-db [db "testDB"]
    (is (not (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                  (d/query db)
                  cursor/is-cached)))))

(deftest cursor-collect-into-test
  (h/with-temp-db [db "testDB"]
    (is (= [1 2 3 4 5]
           (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                (d/query db Integer)
                (cursor/collect-into (java.util.ArrayList.)))))
    (testing "collect-into consumes the contents of the cursor"
      (let [res (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                     (d/query db))]
        (cursor/collect-into (java.util.ArrayList.) res)
        (is (not (cursor/has-next res)))))
    (testing "can collect-into a hash set and compare to a clojure set"
      (is (= #{1 2 3}
             (->> [:FOR ["x" [1 1 2 2 3]] [:RETURN "x"]]
                  (d/query db)
                  (cursor/collect-into (java.util.HashSet.))))))))

(deftest cursor-seq-test
  (h/with-temp-db [db "testDB"]
    (let [xs (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                  (d/query db Integer)
                  seq)]
      (is (= (type xs) clojure.lang.LazySeq))
      (is (= [1 2 3 4 5] xs)))
    (testing "can map deserialize-doc"
      (let [xs (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                    (d/query db com.arangodb.velocypack.VPackSlice)
                    (map adapter/deserialize-doc))]
        (is (= (type xs) clojure.lang.LazySeq))
        (is (= [1 2 3 4 5] xs))))
    (testing "but getting as a default Object is going to give us numbers!"
      (is (= [1 2 3 4 5]
             (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                  (d/query db)
                  seq))))))

(deftest cursor-predicate-test
  (h/with-temp-db [db "testDB"]
    (let [curs (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                    (d/query db))]
      (is (= true
             (cursor/all-match (partial >= 5) curs)))
      (testing "can only test once"
        (is (= false
               (cursor/all-match nat-int? curs)))))))

(deftest cursor-map-test
  (h/with-temp-db [db "testDB"]
    (is (= [2 3 4 5 6]
           (->> [:FOR ["x" "1..5"] [:RETURN "x"]]
                (d/query db)
                (cursor/map inc)
                seq)))))

(deftest cursor-first-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..3"] [:RETURN "x"]])]
      (is (= 1 (cursor/first res)))
      (is (= 2 (cursor/first res)))
      (is (= 3 (cursor/first res)))
      (is (nil? (cursor/first res))))))

(deftest cursor-foreach-test
  (h/with-temp-db [db "testDB"]
    (let [res (->> [:FOR ["x" "1..3"] [:RETURN "x"]]
                   (d/query db))
          state (atom 0)]
      (->> res
           (cursor/foreach (fn [x] (swap! state + x))))
      (is (nil? (cursor/first res)))
      (is (= @state 6)))))

(deftest multi-batch-tests
  (h/with-temp-db [db "testDB"]
    (is (= (range 100)
           (->> [:FOR ["x" "0..99"] [:RETURN "x"]]
                (d/query db {:batchSize (int 5)} Integer)
                seq)))
    (let [curs (->> [:FOR ["x" "0..10"] [:RETURN "x"]]
                    (d/query db {:batchSize (int 2)} Integer))]
      (while (cursor/has-next curs)
        (is (nat-int? (cursor/next curs)))))))
