(ns user
  (:require [clojure.reflect :as r]
            [clj-arangodb.arangodb.options :as options]
            [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.helper :as h]
            [clj-arangodb.arangodb.entity :as entity]
            [clj-arangodb.arangodb.adapter :as adapter])
  (:import [com.arangodb
            ArangoCollection
            ArangoDB$Builder
            ArangoDB]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            DocumentCreateEntity
            BaseDocument]
           clojure.lang.ILookup
           ))


(deftype Slice [^VPackSlice slice]
  clojure.lang.ILookup
  (valAt [this k]
    (if-not (.isObject slice)
      nil
      (let [elem (.get slice ^String (name k))]
        (if (.isNone elem)
          nil
          elem))))
  (valAt [this k default]
    (if-not (.isObject slice)
      default
      (let [elem (.get slice ^String (name k))]
        (if (.isNone elem)
          default
          elem)))))


;; (def conn (arango/connect {:user "test"}))
(def db (arango/create-and-get-database conn "testDB"))
(def coll (d/create-and-get-collection db "testColl"))

;; (def data {:name "fred" :age 33 :nested {:a 1 :b 2 :c 3}})

;; (def res (c/insert-document coll data))


;; (def base-doc
;;   (binding [adapter/*default-doc-class* BaseDocument]
;;     (c/get-document coll (entity/get-key res))))

;; (def base-no-conversion
;;   (c/get-document coll BaseDocument (entity/get-key res)))

;; (def vpack-no-conversion
;;   (c/get-document coll VPackSlice (entity/get-key res)))
