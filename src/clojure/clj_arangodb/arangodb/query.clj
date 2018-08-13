(ns clj-arangodb.arangodb.query
  (:require [clj-arangodb.arangodb.adapter :as ad])
  (:import [com.arangodb
            ArangoDB
            ArangoCursor]))

(defn ->vec [^ArangoCursor c]
  (vec (map ad/deserialize-doc (.asListRemaining c))))

(defn has-next? [^ArangoCursor c]
  ^Boolean (.hasNext c))
