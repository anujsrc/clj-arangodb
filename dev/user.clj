(ns user
  (:require [clojure.reflect :as r]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.core :as ar]
            [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.velocypack.core :as v]
            [clj-arangodb.arangodb.adapter :as adapter])
  (:import [com.arangodb
            ArangoCollection
            ArangoDB$Builder
            ArangoDB]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            DocumentCreateEntity
            BaseDocument]))

;; unalias all aliases in ns
;; (map (partial ns-unalias *ns*) (keys (ns-aliases *ns*)))

(def conn (ar/connect {:user "test"}))
(def db (ar/get-database conn "webapp"))


(def q1 (aql/parse-aql
         [:FOR ["v"] {:start "\"Characters/536203\""
                      :type :any ;; :inbound :any
                      :depth [2 2]
                      :unique {:vertices :global}
                      :collections ["ChildOf"]}
          [:RETURN "v.name"]]))

(def q2 (aql/parse-aql
         [:FOR ["c" "Characters"]
          [:FILTER ['== "c.name" "\"Ned\""]]
          [:FOR ["v" "e" {:start "c"
                          :type :inbound
                          :depth [1 1]
                          :collections ["ChildOf"]}]
           [:RETURN "v.name"]]]))

(def q3 (aql/parse-aql
         (aql/FOR ["c" "Characters"]
                  (aql/FILTER ['== "c.name" "\"Ned\""])
                  (aql/FOR ["v" "e" {:start "c"
                                     :type :inbound
                                     :depth [1 1]
                                     :collections ["ChildOf"]}]
                           (aql/RETURN "v.name")))))


;; (def res (c/insert-document coll {:name "clj-arango" :version "0.0.1"}))

;; (defmethod ad/serialize-doc clojure.lang.PersistentArrayMap [o]
;;   (v/pack o))

;; (defmethod ad/deserialize-doc BaseDocument [o]
;;   (-> o bean (dissoc :class)))

;; (defonce conn (atom (arango/connect {:user "test"})))

;; (def db-name (str (gensym)))
;; (defonce db (atom (arango/create-and-get-database @conn db-name)))

;; (def coll-name (str (gensym)))
;; (defonce coll (atom (d/create-and-get-collection @db coll-name)))


;; (defn lisp-ify [cammelCase]
;;   (-> cammelCase
;;       str
;;       (clojure.string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
;;       (clojure.string/replace #"([a-z0-9])([A-Z])" "$1-$2")
;;       (clojure.string/lower-case)
;;       symbol))

;; (defn class-as-param-name [class-name]
;;   (-> class-name
;;       str
;;       (clojure.string/split #"\.")
;;       last
;;       lisp-ify))

;; (defn inspect [o]
;;   (map :name (:members (r/reflect o))))


;; (defn wrap-methods [{:keys [name
;;                             return-type
;;                             declaring-class
;;                             parameter-types
;;                             exception-types
;;                             flags] :as member}]
;;   (let [dot-name (symbol (str "." name))
;;         o# (class-as-param-name declaring-class)
;;         arg-types-and-names (into [o#] (map class-as-param-name parameter-types))]
;;     `(defn ^{:tag ~return-type} ~(lisp-ify name)
;;        ~(into [o#] (map class-as-param-name parameter-types))
;;        ~(cons (vary-meta dot-name assoc :tag declaring-class)
;;               arg-types-and-names))))

;; (defn wrap-class [o]
;;   (map wrap-methods (:members (r/reflect o))))


;; (defn wrap-method-member [{:keys [name
;;                                   return-type
;;                                   declaring-class
;;                                   parameter-types
;;                                   exception-types
;;                                   flags] :as member}]
;;   (let [dot-name (symbol (str "." name))
;;         o# (class-as-param-name declaring-class)
;;         arg-types-and-names (into [o#] (map class-as-param-name parameter-types))]
;;     `(~(into [o#] (map class-as-param-name parameter-types))
;;       ~(cons (vary-meta dot-name assoc :tag declaring-class)
;;              arg-types-and-names))))

;; (defn generate-multi-arity-decls [[name implementations]]
;;   `(defn ~(lisp-ify name)
;;      ~@(map wrap-method-member implementations)))

;; (defn wrap-object-publics [o]
;;   (as-> (r/reflect o) $
;;     (:members $)
;;     (filter (fn [x] (some #{:public} (:flags x))) $)
;;     (group-by :name $)
;;     (map generate-multi-arity-decls $)))
