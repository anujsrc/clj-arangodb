(ns clj-arangodb.test.helper
  (:require [clj-arangodb.arangodb :as arangodb]
            [clj-arangodb.databases :as databases]))

(def ^:dynamic *connection-spec* {:user "test"})

(defmacro with-conn
  [[conn] & body]
  `(let [~conn (arangodb/connect *connection-spec*)]
     ~@body
     (arangodb/shutdown ~conn)))

(defmacro with-test-db
  [[db] & body]
  `(let [label# (str (gensym))
         conn# (arangodb/connect *connection-spec*)
         ~db (arangodb/ensure-and-get-database conn# label#)]
     ~@body
     (databases/drop ~db)
     (arangodb/shutdown conn#)))
