(ns clj-arangodb.arangodb.databases
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb
            ArangoDB
            ArangoCursor
            ArangoDatabase
            ArangoGraph
            ArangoCollection]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            GraphEntity
            CollectionEntity
            DatabaseEntity]
           [com.arangodb.model
            DocumentReadOptions
            CollectionCreateOptions
            CollectionsReadOptions
            GraphCreateOptions
            AqlQueryOptions])
  (:refer-clojure :exclude [drop]))

(defn exists? ^Boolean
  [^ArangoDatabase db] (.exists db))

(defn drop ^Boolean
  [^ArangoDatabase db] (.drop db))

(defn get-info ^DatabaseEntity
  [^ArangoDatabase db]
  (.getInfo db))

(defn get-document
  ([^ArangoDatabase db ^String id]
   (vpack/unpack (.getDocument db id VPackSlice)))
  ([^ArangoDatabase db ^Class as ^String id]
   (.getDocument db id as))
  ([^ArangoDatabase db ^DocumentReadOptions options ^Class as ^String id]
   (.getDocument db id as (options/build DocumentReadOptions options))))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String label]
   (.createCollection db label))
  ([^ArangoDatabase db ^CollectionCreateOptions options ^String label]
   (.createCollection db label (options/build CollectionCreateOptions options))))

(defn collection ^ArangoCollection
  ([^ArangoDatabase db ^String label]
   (.collection db label)))

(def get-collection collection)

(defn create-and-get-collection ^ArangoCollection
  ([^ArangoDatabase db ^String label]
   (do (.createCollection db label)
       (.collection db label)))
  ([^ArangoDatabase db ^CollectionCreateOptions options ^String label]
   (do (.createCollection db label (options/build CollectionCreateOptions options))
       (.collection db label))))

(defn get-collections
  ;; returns a collection of CollectionEntity
  ([^ArangoDatabase db]
   (.getCollections db))
  ([^ArangoDatabase db ^CollectionsReadOptions options]
   (.getCollections db (options/build CollectionsReadOptions options))))

(defn get-collection-names
  ([^ArangoDatabase db]
   (map #(.getName ^CollectionEntity %) (.getCollections db))))

(defn get-graphs
  ;; returns a collection of GraphEntity
  [^ArangoDatabase db]
  (.getGraphs db))

(defn collection-exists?
  [^ArangoDatabase db label]
  (some #(= label (.getName ^CollectionEntity %)) (.getCollections db)))

(defn graph-exists? [^ArangoDatabase db label]
  (some #(= label (.getName ^GraphEntity %)) (.getGraphs db)))

(defn create-graph
  "Create a new graph `label` edge-definitions must be a non empty
  sequence of maps `{:name 'relationName' :from ['collA'...] :to [collB...]}`
  if the names in sources and targets do not exist on the database,
  then new collections will be created."
  ^GraphEntity
  [^ArangoDatabase db
   ^GraphCreateOptions options
   ^String label
   ^java.util.Collection edge-definitions]
  (.createGraph db label
                (map #(if (map? %) (graph/edge-definition %) %)
                     edge-definitions)
                (options/build GraphCreateOptions options)))

(defn graph ^ArangoGraph
  ([^ArangoDatabase db ^String graph-name]
   (.graph db graph-name)))

(def get-graph graph)

(defn query ^ArangoCursor
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db aql-query]
   (query db nil nil Object aql-query))
  ([^ArangoDatabase db ^Class as aql-query]
   (query db nil nil as aql-query))
  ([^ArangoDatabase db ^AqlQueryOptions options ^Class as aql-query]
   (query db nil options as aql-query))
  ([^ArangoDatabase db bindvars ^AqlQueryOptions options ^Class as aql-query]
   (.query db ^String (aql/serialize aql-query) bindvars (options/build AqlQueryOptions options) as)))
