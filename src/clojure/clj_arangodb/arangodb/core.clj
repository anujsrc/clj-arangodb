(ns clj-arangodb.arangodb.core
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb
            ArangoDB$Builder
            ArangoDB
            ArangoDatabase]))

(defn ^ArangoDB connect
  "
  Takes an optional map that may contain the following:
  keys have the same names as the java methods.
  :host a pair default is ['127.0.0.1' 8529]
  :user a String default is 'root'
  :password String by default no password is used
  :use-protocol vst | http-json | http-vpack (:vst by default)
  :ssl-context SSlContext not used
  :timeout Integer | Long
  :chunksize Integer | Long
  :max-connections Integer | Long
  "
  ([] (connect {}))
  ([options] (.build ^ArangoDB$Builder (options/build ArangoDB$Builder options))))

(defn shutdown [^ArangoDB conn]
  (.shutdown conn))

(defn ^Boolean create-database
  "Create a new database with name `label`.
  Returns `true` on success else `ArangoDBException`"
  [^ArangoDB conn ^String label]
  (.createDatabase conn label))

(defn ^ArangoDatabase db
  "Always returns a new `ArrangoDatabase` even if no such database exists
  the returned object can be used if a databse is created at a later time"
  [^ArangoDB conn ^String label]
  (.db conn label))

(def get-database db)

(defn ^Boolean create-and-get-database
  "create a new databse with name `label` and return the `object`"
  [^ArangoDB conn ^String label]
  (do (.createDatabase conn label)
      (.db conn label)))

(defn get-databases
  "Returns a `seq` of database labels"
  [^ArangoDB conn] (seq (.getDatabases conn)))

(defn database?
  "Returns true if `label` is an existsing db"
  [^ArangoDB conn ^String label]
  (boolean (some #{label} (get-databases conn))))
