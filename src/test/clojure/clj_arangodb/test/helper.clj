(ns clj-arangodb.test.helper
  (:require [clj-arangodb.arangodb :as arangodb]))

(def ^:dynamic *connection-spec* {:user "test"})

(defmacro with-conn
  [[conn] & body]
  `(let [~conn (arangodb/connect *connection-spec*)]
     ~@body
     (arangodb/shutdown ~conn)))
