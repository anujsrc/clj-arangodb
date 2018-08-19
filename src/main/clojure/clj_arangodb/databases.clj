(ns clj-arangodb.databases
  (:require [clj-arangodb.options :as options])
  (:import [com.arangodb
            ArangoDatabase]
           [com.arangodb.entity
            CollectionEntity
            DatabaseEntity])
  (:refer-clojure :exclude [drop]))

(defn create
  [^ArangoDatabase db]
  (.create db))

(defn drop
  [^ArangoDatabase db]
  (.drop db))

(defn exists?
  [^ArangoDatabase db]
  (.exists db))

(defn get-info ^DatabaseEntity
  [^ArangoDatabase db]
  (.getInfo db))
