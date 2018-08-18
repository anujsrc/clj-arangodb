(ns clj-arangodb.arangodb.graph
  (:require [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.options :as options]
            [clj-arangodb.velocypack.core  :as vpack])
  (:import [com.arangodb.entity
            GraphEntity
            VertexEntity
            EdgeEntity
            EdgeDefinition
            VertexUpdateEntity
            EdgeUpdateEntity]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb
            ArangoGraph
            ArangoVertexCollection
            ArangoEdgeCollection]
           [com.arangodb.model
            VertexCreateOptions
            VertexDeleteOptions
            VertexReplaceOptions
            VertexUpdateOptions
            EdgeCreateOptions
            EdgeDeleteOptions
            EdgeReplaceOptions
            EdgeUpdateOptions
            DocumentCreateOptions
            DocumentReadOptions
            DocumentReplaceOptions
            DocumentUpdateOptions])
  (:refer-clojure :exclude [drop]))

(defn ^Boolean exists? [^ArangoGraph x] (.exists x))

(defn ^GraphEntity get-info [^ArangoGraph x] (.getInfo x))

(defn drop [^ArangoGraph x] (.drop x))

(defn get-vertex-collections [^ArangoGraph graph]
  ;; collection of strings
  (.getVertexCollections graph))

(defn ^ArangoVertexCollection vertex-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (.vertexCollection graph name))

(defn ^GraphEntity add-vertex-collection [^ArangoGraph graph ^String name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (.addVertexCollection graph name))

(defn get-edge-definitions [^ArangoGraph graph]
  ;; collection of strings
  (.getEdgeDefinitions graph))

(defn ^ArangoEdgeCollection edge-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (.edgeCollection graph name))

(defn ^EdgeDefinition edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn ^GraphEntity add-edge-definition
  [^ArangoGraph graph ^EdgeDefinition definition]
  (.addEdgeDefinition graph definition))

(defn ^GraphEntity replace-edge-definition [^ArangoGraph graph ^EdgeDefinition definition]
  (.replaceEdgeDefinition graph definition))

(defn ^GraphEntity remove-edge-definition [^ArangoGraph graph ^String name]
  (.removeEdgeDefinition graph name))

(defn ^VertexEntity insert-vertex
  ([^ArangoVertexCollection coll doc]
   (.insertVertex coll (adapter/serialize-doc doc)))
  ([^ArangoVertexCollection coll doc ^VertexCreateOptions options]
   (.insertVertex coll (adapter/serialize-doc doc)
                  (options/build VertexCreateOptions options))))

(defn get-vertex
  ([^ArangoVertexCollection coll key]
   (vpack/unpack (.getVertex coll key VPackSlice)))
  ([^ArangoVertexCollection coll ^Class as ^String key]
   (.getVertex coll key as))
  ([^ArangoVertexCollection coll ^DocumentReadOptions options ^Class as ^String key]
   (.getVertex coll key as (options/build DocumentReadOptions options))))

(defn ^VertexUpdateEntity replace-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (.replaceVertex coll key (adapter/serialize-doc doc)))
  ([^ArangoVertexCollection coll ^VertexUpdateOptions options ^String key ^Object doc]
   (.replaceVertex coll key (adapter/serialize-doc doc) (options/build VertexUpdateOptions options))))

(defn ^VertexUpdateEntity update-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (.updateVertex coll key (adapter/serialize-doc doc)))
  ([^ArangoVertexCollection coll ^VertexUpdateOptions options ^String key ^Object doc]
   (.updateVertex coll key (adapter/serialize-doc doc) (options/build VertexUpdateOptions options))))

(defn delete-vertex ; void
  ([^ArangoVertexCollection coll ^String key]
   (.deleteVertex coll key))
  ([^ArangoVertexCollection coll ^String key ^VertexDeleteOptions options]
   (.deleteVertex coll key (options/build VertexDeleteOptions options))))

(defn insert-edge ^EdgeEntity
  ([^ArangoEdgeCollection coll doc]
   (.insertEdge coll (adapter/serialize-doc doc)))
  ([^ArangoEdgeCollection coll ^EdgeCreateOptions options doc]
   (.insertEdge coll (adapter/serialize-doc doc) (options/build EdgeCreateOptions options))))

(defn get-edge
  ([^ArangoEdgeCollection coll key]
   (vpack/unpack (get-edge coll key VPackSlice)))
  ([^ArangoEdgeCollection coll ^String key ^Class as]
   (adapter/deserialize-doc (.getEdge coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (adapter/deserialize-doc (.getEdge coll key as (options/build DocumentReadOptions options)))))


(defn replace-edge ^EdgeUpdateEntity
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (adapter/from-entity (.replaceEdge coll key (adapter/serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (adapter/from-entity (.replaceEdge coll key (adapter/serialize-doc doc)
                                      (options/build EdgeUpdateOptions options)))))

(defn update-edge ^EdgeUpdateEntity
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (adapter/from-entity (.updateEdge coll key (adapter/serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (adapter/from-entity (.updateEdge coll key (adapter/serialize-doc doc)
                                     (options/build EdgeUpdateOptions options)))))

(defn delete-edge ; void
  ([^ArangoEdgeCollection coll ^String key]
   (.deleteEdge coll key))
  ([^ArangoEdgeCollection coll ^String key ^EdgeDeleteOptions options]
   (.deleteEdge coll key (options/build EdgeDeleteOptions options))))
