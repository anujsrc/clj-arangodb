(ns clj-arangodb.arangodb.adapter
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            Entity
            MultiDocumentEntity
            BaseDocument]))

(def ^:const entity-package-name "com.arangodb.entity")

;; *default-doc-class* is used to indicate
;; the class of the objects that we want RETURNED
(def ^:dynamic *default-doc-class* VPackSlice)

(defmulti serialize-doc class)
(defmulti deserialize-doc class)

(defmethod serialize-doc :default [o] (vpack/pack o))

(defmethod deserialize-doc VPackSlice [o] (vpack/unpack o))
(defmethod deserialize-doc BaseDocument [o] (bean o))
(defmethod deserialize-doc :default [o] o)

(defmulti from-entity class)

(defn is-entity? [o]
  (when o
    (or (instance? Entity o)
        (= entity-package-name (-> o class .getPackage .getName)))))

(defmethod from-entity Enum [o] (str o))

(defn as-entity-vec-or-deserialized [o]
  (if (= 0 (.size o))
    []
    (let [f (if (is-entity? (.get o 0)) from-entity deserialize-doc)
          n (.size o)]
      (loop [i 0
             xs (transient [])]
        (if (= i n)
          (persistent! xs)
          (recur
           (inc i)
           (conj! xs (f (.get o i)))))))))

(defmethod from-entity MultiDocumentEntity [o]
  (-> o
      bean
      (dissoc :documentsAndErrors)
      (update :documents as-entity-vec-or-deserialized)
      (update :errors as-entity-vec-or-deserialized)))

(defmethod from-entity java.util.ArrayList [o]
  (if (= 0 (.size o))
    []
    (let [x (.get o 0)]
      (if (is-entity? x)
        (let [n (.size o)]
          (loop [i 0
                 xs (transient [])]
            (if (= i n)
              (persistent! xs)
              (recur
               (inc i)
               (conj! xs (from-entity (.get o i)))))))
        (vec o)))))

(defmethod from-entity :default [o]
  (if-not (is-entity? o)
    o
    (persistent!
     (reduce (fn [m [k v]]
               (assoc! m k (from-entity v)))
             (transient {}) (bean o)))))
