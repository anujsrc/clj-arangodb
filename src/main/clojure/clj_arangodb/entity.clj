(ns clj-arangodb.entity
  (:import [com.arangodb.entity
            Entity]))

(def ^:const ^:private entity-package-name "com.arangodb.entity")

(defn- entity-like?
  ""
  [o]
  (when o
    (= entity-package-name
       (-> o class .getPackage .getName))))

(defn is-entity? [o]
  (or (instance? Entity o)
      (entity-like? o)))

(defprotocol EntityAsMap
  (->map [o] "convert an Entity instance into a persistent map"))

(extend-protocol EntityAsMap
  Entity
  (->map [o] (bean o)))
