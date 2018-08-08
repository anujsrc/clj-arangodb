(ns clj-arangodb.arangodb.collections-test
  (:require [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.helper :as h]
            [clojure.test :refer :all]))

(deftest insert-document-test
  (h/with-coll
    [coll "colly"]
    (let [data {:name "name" :age 100}
          x (c/insert-document coll data)]
      (is (= (:class x)
             com.arangodb.entity.DocumentCreateEntity))
      (let [res (c/get-document coll (:key x))]
        (is (= data (select-keys res [:age :name])))))))
