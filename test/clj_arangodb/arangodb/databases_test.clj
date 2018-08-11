(ns clj-arangodb.arangodb.databases-test
  (:require [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.helper :as h]
            [clojure.test :refer :all]))

(deftest exists-test
  (h/with-temp-db
    [db "mahdb"]
    (is (d/exists? db))))

(deftest drop-test
  (h/with-temp-db
    [db "mahdb"]
    (is (d/drop db))))

(deftest get-info-test
  (h/with-temp-db
    [db "mahdb"]
    (let [info (d/get-info db)]
      (is (= com.arangodb.entity.DatabaseEntity
             (:class info)))
      (is (= #{:class :id :isSystem :name :path}
             (set (keys info)))))))

(deftest collection-test
  (h/with-temp-db
    [db "mahdb"]
    (let [coll (d/create-collection db "mahcoll")]
      (is (= com.arangodb.entity.CollectionEntity
             (:class coll)))
      (is (= #{:class :id :isSystem :isVolatile :name :status :type :waitForSync}
             (set (keys coll)))))
    (is (instance? com.arangodb.ArangoCollection
                   (d/collection db "mahcoll")))
    (is (contains? (set (d/get-collection-names db))
                   "mahcoll"))))

(deftest collection-exists-test
  (h/with-temp-db [db "some_database"]
    (let [label "some_collection"]
      (d/create-collection db label)
      (is (contains? (set (d/get-collection-names db)) label))
      (is (d/collection-exists? db label)))))
