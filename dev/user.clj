(ns user
  (:require [clojure.reflect :as r]
            [clj-arangodb.arangodb.options :as options]
            [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.databases :as databases]
            [clj-arangodb.arangodb.collections :as collections]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.helper :as h]
            [clj-arangodb.arangodb.test-data :as td]
            [clj-arangodb.arangodb.entity :as entity]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.cursor :as cursor])
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

(let [example-collection
      (-> {:user "test"} ; connection spec
          arango/connect
          (arango/ensure-and-get-database "exampleDB")
          (databases/ensure-and-get-collection "exampleCollection"))])

(defn example [conn who]
  (let [db (arango/get-database conn "gameOfThronesDB")]
    (->> [:FOR ["c" "Characters"]
          [:FILTER [:EQ "c.name" (adapter/double-quote who)]]
          [:FOR ["v" {:start "c"
                      :type :inbound
                      :depth [1 1]
                      :collections ["ChildOf"]}]
           [:RETURN "v.name"]]]
         (databases/query db)
         (cursor/foreach #(printf "%s is a child of %s\n" % who)))))



;; (deftype Slice [^VPackSlice slice]
;;   clojure.lang.ILookup
;;   (valAt [this k]
;;     (if-not (.isObject slice)
;;       nil
;;       (let [elem (.get slice ^String (name k))]
;;         (if (.isNone elem)
;;           nil
;;           elem))))
;;   (valAt [this k default]
;;     (if-not (.isObject slice)
;;       default
;;       (let [elem (.get slice ^String (name k))]
;;         (if (.isNone elem)
;;           default
;;           elem)))))

;; (defn slice [x]
;;   (->Slice (vpack/pack x)))


;; (def conn (arango/connect {:user "test"}))
;; (def db (arango/create-and-get-database conn "testDB"))
;; (def coll (d/create-and-get-collection db "testColl"))

;; (def data {:name "fred" :age 33 :nested {:a 1 :b 2 :c 3}})

;; (def res (c/insert-document coll data))


;; (def base-doc
;;   (binding [adapter/*default-doc-class* BaseDocument]
;;     (c/get-document coll (entity/get-key res))))

;; (def base-no-conversion
;;   (c/get-document coll BaseDocument (entity/get-key res)))

;; (def vpack-no-conversion
;;   (c/get-document coll VPackSlice (entity/get-key res)))
