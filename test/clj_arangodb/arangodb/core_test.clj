(ns clj-arangodb.arangodb.core-test
  (:require [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.helper :as h]
            [clojure.test :refer :all]))

(deftest create-database-test
  (h/with-conn [conn {:user "test"}]
    (let [label (loop [label (str (gensym))]
                  (if-not (a/database? conn label)
                    label
                    (recur (str (gensym)))))
          res (a/create-database conn label)]
      (is res)
      (is (contains? (set (a/get-databases conn)) label)))))
