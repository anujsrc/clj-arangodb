(ns clj-arangodb.databases
  (:require [clj-arangodb.options :as options])
  (:import [com.arangodb
            ArangoDatabase
            ArangoCollection]
           [com.arangodb.model
            CollectionCreateOptions]
           [com.arangodb.entity
            CollectionEntity
            DatabaseEntity])
  (:refer-clojure :exclude [drop]))

(defn- collection-create-options
  ^CollectionCreateOptions [opts]
  (options/build CollectionCreateOptions opts))

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

(defn collection
  "Returns a new `ArrangoCollection` instance"
  ^ArangoCollection [^ArangoDatabase db label]
  (.collection db label))

(defn create-collection
  "Create a new collection with name `label`.
  Returns `true` on success else `ArangoDBException`"
  ([db label] (create-collection db nil label))
  (^ArangoCollection [^ArangoDatabase db options label]
   (.createCollection db (collection-create-options options) label)))

(defn ensure-and-get-collection
  "if collection with `label` does not exist, it will create it.
  Returns an `ArangoCollection` instance"
  ([db label] (ensure-and-get-collection db nil label))
  (^ArangoCollection [^ArangoDatabase db options label]
   (let [c (collection db label)]
     (when-not (.exists c)
       (.create c (collection-create-options options)))
     c)))
