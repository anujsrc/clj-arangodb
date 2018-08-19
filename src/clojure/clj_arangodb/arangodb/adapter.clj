(ns clj-arangodb.arangodb.adapter
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import [java.util
            ArrayList]
           [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            Entity
            BaseDocument
            BaseEdgeDocument]))

(defmulti double-quote-strings class)
(defmethod double-quote-strings String
  [x] (str "\"" x "\""))
(defmethod double-quote-strings clojure.lang.APersistentVector
  [xs] (mapv double-quote-strings xs))
(defmethod double-quote-strings clojure.lang.APersistentMap
  [xs] (into {} (for [[k v] xs] [(double-quote-strings k) (double-quote-strings v)])))
(defmethod double-quote-strings clojure.lang.ASeq
  [xs] (seq (map double-quote-strings xs)))
(defmethod double-quote-strings clojure.lang.APersistentSet
  [xs] (set (map double-quote-strings xs)))
(defmethod double-quote-strings :default
  [x] x)

;; *default-doc-class* is used to indicate
;; the class of the objects that we want RETURNED
(def ^:dynamic *default-doc-class* VPackSlice)

(defprotocol SerializeDoc
  (serialize-doc [data] "enusre that the data is in a suitable format"))

(extend-protocol SerializeDoc
  clojure.lang.IPersistentMap
  (serialize-doc [data] (vpack/pack data))
  Object
  (serialize-doc [data] data)
  nil
  (serialize-doc [_] nil))

(defprotocol DeserializeDoc
  (deserialize-doc [data] ""))

(extend-protocol DeserializeDoc
  VPackSlice
  (deserialize-doc [data] (vpack/unpack data))
  BaseDocument
  (deserialize-doc [data]
    {:key (.getKey data)
     :id (.getId data)
     :revision (.getRevision data)
     :properties (into {} (.getProperties data))})
  BaseEdgeDocument
  (deserialize-doc [data]
    {:key (.getKey data)
     :id (.getId data)
     :revision (.getRevision data)
     :from (.getFrom data)
     :to (.getTo data)
     :properties (into {} (.getProperties data))})
  Object
  (deserialize-doc [data] data)
  nil
  (deserialize-doc [_] nil))
