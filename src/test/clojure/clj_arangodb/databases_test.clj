(ns clj-arangodb.databases-test
  (:require [clj-arangodb.databases :as databases]
            [clj-arangodb.entity :as entity]
            [clj-arangodb.test.helper :as h]
            [clojure.test :refer :all]))


(deftest get-info-test
  (h/with-test-db [db]
    (is (= (-> db
               databases/get-info
               class)
           (-> db
               databases/get-info
               entity/->map
               :class)))))
