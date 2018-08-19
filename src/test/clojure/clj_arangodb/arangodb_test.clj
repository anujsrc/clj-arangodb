(ns clj-arangodb.arangodb-test
  (:require [clj-arangodb.arangodb :as arangodb]
            [clj-arangodb.databases :as databases]
            [clj-arangodb.test.helper :as h]
            [clojure.test :refer :all])
  (:import [com.arangodb
            ArangoDB
            ArangoDatabase]))

(deftest connect-test
  (let [spec {:user "test" :host ["127.0.0.1" 8529]}
        conn (arangodb/connect spec)]
    (is (instance? ArangoDB conn))
    (is (nil? (arangodb/shutdown conn)))))

(deftest ensure-and-get-database-test
  (h/with-conn [conn]
    (let [label (str (gensym))
          db-conn-1 (arangodb/db conn label)]
      (testing "a db instance can exist without an actual database"
        (is (false? (databases/exists? db-conn-1))))
      (let [db-conn-2 (arangodb/ensure-and-get-database conn label)]
        (testing "creating a db updates all instances"
          (is (true? (databases/exists? db-conn-1)))
          (is (true? (databases/exists? db-conn-2))))
        (testing "can drop a database"
          (is (true? (databases/drop db-conn-1))))
        (testing "error when trying to delete a db that has been deleted"
          (is (->> (try (databases/drop db-conn-2)
                        (catch Exception e e))
                   (instance? com.arangodb.ArangoDBException))))))))
