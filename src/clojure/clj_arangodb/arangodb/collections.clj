(ns clj-arangodb.arangodb.collections
  (:require
   [clj-arangodb.arangodb.options :as options]
   [clj-arangodb.velocypack.core  :as vpack]
   [clj-arangodb.arangodb.adapter :as adapter])
  (:import
   [java.util
    ArrayList
    Collection]
   [com.arangodb
    ArangoCollection]
   [com.arangodb.velocypack
    VPackSlice]
   [com.arangodb.entity
    BaseDocument
    BaseEdgeDocument
    CollectionEntity
    IndexEntity
    MultiDocumentEntity
    DocumentCreateEntity
    DocumentUpdateEntity
    DocumentDeleteEntity
    DocumentImportEntity
    CollectionPropertiesEntity
    CollectionRevisionEntity]
   [com.arangodb.model
    CollectionPropertiesOptions
    SkiplistIndexOptions
    GeoIndexOptions
    FulltextIndexOptions
    PersistentIndexOptions
    HashIndexOptions
    DocumentCreateOptions
    DocumentReadOptions
    DocumentUpdateOptions
    DocumentDeleteOptions
    DocumentReplaceOptions
    DocumentImportOptions])
  (:refer-clojure :exclude [drop load]))

(defn get-info ^CollectionEntity
  [^ArangoCollection coll]
  (.getInfo coll))

(defn get-properties ^CollectionPropertiesEntity
  [^ArangoCollection coll]
  (.getProperties coll))

(defn get-revision ^CollectionRevisionEntity
  [^ArangoCollection coll]
  (.getRevision coll))

(defn exists? ^Boolean
  [^ArangoCollection coll]
  (.exists coll))

(defn rename ^CollectionEntity
  [^ArangoCollection coll ^String label]
  (.rename coll label))

(defn load ^CollectionEntity
  [^ArangoCollection coll]
  (.load coll))

(defn unload ^CollectionEntity
  [^ArangoCollection coll]
  (.unload coll))

(defn change-properties
  ^CollectionEntity
  [^ArangoCollection coll ^CollectionPropertiesOptions options]
  (.changeProperties coll (options/build CollectionPropertiesOptions options)))

(defn drop ;;void
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))

(defn ensure-hash-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^HashIndexOptions options]
  (.ensureHashIndex coll fields (options/build HashIndexOptions options)))

(defn ensure-skip-list-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^SkiplistIndexOptions options]
  (.ensureSkiplistIndex coll fields (options/build SkiplistIndexOptions options)))

(defn ensure-geo-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^GeoIndexOptions options]
  (.ensureGeoIndex coll fields (options/build GeoIndexOptions options)))

(defn ensure-full-text-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^FulltextIndexOptions options]
  (.ensureFulltextIndex coll fields (options/build FulltextIndexOptions options)))

(defn ensure-persistent-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^PersistentIndexOptions options]
  (.ensurePersistentIndex coll fields (options/build PersistentIndexOptions options)))

(defn get-index ^IndexEntity
  [^ArangoCollection coll ^String index]
  (.getIndex coll index))

(defn get-indexes ^Collection
  ;; collection of IndexEntity
  [^ArangoCollection coll]
  (.getIndexes coll))

(defn delete-index ^String
  [^ArangoCollection coll ^String index]
  (.deleteIndex coll index))

(defn get-document
  ([^ArangoCollection coll ^String key]
   (adapter/deserialize-doc (.getDocument coll key VPackSlice)))
  ([^ArangoCollection coll ^Class as ^String key]
   (.getDocument coll key as))
  ([^ArangoCollection coll ^DocumentReadOptions options ^Class as ^String key]
   (.getDocument coll key as (options/build DocumentReadOptions options))))

(defn get-json-document
  ([coll key]
   (adapter/deserialize-doc (get-document coll String key)))
  ([coll options key]
   (adapter/deserialize-doc (get-document coll options String key))))

(defn get-object-document
  ([coll key]
   (adapter/deserialize-doc (get-document coll Object key)))
  ([coll options key]
   (adapter/deserialize-doc (get-document coll options Object key))))

(defn get-base-document
  ([coll key]
   (adapter/deserialize-doc (get-document coll BaseDocument key)))
  ([coll options key]
   (adapter/deserialize-doc (get-document coll options BaseDocument key))))

(defn get-base-edge-document
  ([coll key]
   (adapter/deserialize-doc (get-document coll BaseEdgeDocument key)))
  ([coll options key]
   (adapter/deserialize-doc (get-document coll options BaseEdgeDocument key))))

(defn get-documents ^MultiDocumentEntity
  ([^ArangoCollection coll ^Collection keys]
   (.getDocuments coll keys VPackSlice))
  ([^ArangoCollection coll ^Class as ^Collection keys]
   (.getDocuments coll keys as)))

(defn insert-document
  "insert a single document into the collection.
  Ensures that `doc` is converted into a format suitable for the `java-driver`"
  ^DocumentCreateEntity
  ([^ArangoCollection coll ^Object doc]
   (.insertDocument coll (adapter/serialize-doc doc)))
  ([^ArangoCollection coll ^DocumentCreateOptions options ^Object doc]
   (.insertDocument coll (adapter/serialize-doc doc)
                    (options/build DocumentCreateOptions options))))

(defn insert-documents
  "insert multiple documents into the collection.
  Ensures that each `doc` is converted into a format suitable for the `java-driver`"
  ^MultiDocumentEntity
  ([^ArangoCollection coll docs]
   (.insertDocuments coll ^Collection (map adapter/serialize-doc docs)))
  ([^ArangoCollection coll ^DocumentCreateOptions options ^Collection docs]
   (.insertDocuments coll (map adapter/serialize-doc docs)
                     (options/build DocumentCreateOptions options))))

(defn update-document ^DocumentUpdateEntity
  ([^ArangoCollection coll ^String key ^Object doc]
   (.updateDocument coll key (adapter/serialize-doc doc)))
  ([^ArangoCollection coll ^DocumentUpdateOptions options ^String key ^Object doc]
   (.updateDocument coll key (adapter/serialize-doc doc)
                    (options/build DocumentUpdateOptions options))))

(defn update-documents ^MultiDocumentEntity
  ([^ArangoCollection coll ^Collection docs]
   (.updateDocuments coll (map adapter/serialize-doc docs)))
  ([^ArangoCollection coll ^DocumentUpdateOptions options ^Collection docs]
   (.updateDocuments coll (map adapter/serialize-doc docs)
                     (options/build DocumentUpdateOptions options))))

(defn replace-document ^DocumentUpdateEntity
  ([^ArangoCollection coll ^String key ^Object doc]
   (.replaceDocument coll key (adapter/serialize-doc doc)))
  ([^ArangoCollection coll ^DocumentReplaceOptions options ^String key ^Object doc]
   (.replaceDocument coll key (adapter/serialize-doc doc)
                     (options/build DocumentReplaceOptions options))))

(defn replace-documents ^MultiDocumentEntity
  ([^ArangoCollection coll docs]
   (.replaceDocuments coll ^Collection (map adapter/serialize-doc docs)))
  ([^ArangoCollection coll ^DocumentReplaceOptions options ^Collection docs]
   (.replaceDocuments coll (map adapter/serialize-doc docs)
                      (options/build DocumentReplaceOptions options))))

(defn delete-document ^DocumentDeleteEntity
  ([^ArangoCollection coll ^String key]
   (.deleteDocument coll key))
  ([^ArangoCollection coll ^DocumentDeleteOptions options ^Class as ^String key]
   (.deleteDocument coll key as (options/build DocumentDeleteOptions options))))

(defn delete-documents ^MultiDocumentEntity
  ([^ArangoCollection coll ^Collection keys]
   (.deleteDocuments coll keys))
  ([^ArangoCollection coll ^DocumentDeleteOptions options ^Class as ^Collection keys]
   (.deleteDocuments coll keys as (options/build DocumentDeleteOptions options))))
