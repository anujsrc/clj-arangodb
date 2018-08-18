(ns clj-arangodb.arangodb.entity)

(def ^:const entity-package-name "com.arangodb.entity")

(defn entity-like? [o]
  (when o
    (= entity-package-name (-> o class .getPackage .getName))))

(defn is-entity? [o]
  (or (instance? com.arangodb.entity.Entity o)
      (entity-like? o)))

(defprotocol FromEntity
  (from-entity [o]))

(extend-protocol FromEntity
  Enum
  (from-entity [o] (str o))
  com.arangodb.entity.Entity
  (from-entity [o]
    (persistent!
     (reduce (fn [acc [k v]]
               (assoc! acc k (from-entity v)))
             (transient {}) (bean o))))
  java.util.ArrayList
  (from-entity [xs]
    (cond (= 0 (.size xs)) nil
          (is-entity? (.get xs 0)) (map from-entity xs)
          :else xs))
  Object
  (from-entity [o]
    (if (entity-like? o)
      (persistent!
       (reduce (fn [acc [k v]]
                 (assoc! acc k (from-entity v)))
               (transient {}) (bean o)))
      o))
  nil
  (from-entity [_] nil))

(defprotocol EntityHasKey
  (get-key [entity] "get the associated key"))

(defprotocol EntityHasId
  (get-id [entity] "get the associated id"))

(defprotocol EntityHasRev
  (get-rev [entity] "get the associated revision"))

(defprotocol EntityHasNew
  (get-new [entity] "get the associated new value"))

(defprotocol EntityHasOld
  (get-old [entity] "get the associated old value"))

(defprotocol EntityHasSerializedName
  (get-serialized-name [entity] "get the associated serialized name"))

(extend-protocol EntityHasKey
  com.arangodb.entity.DocumentEntity
  (get-key [o] (.getKey o)))

(extend-protocol EntityHasId
  com.arangodb.entity.DocumentEntity
  (get-id [o] (.getId o)))

(extend-protocol EntityHasRev
  com.arangodb.entity.DocumentEntity
  (get-rev [o] (.getRev o)))

(extend-protocol EntityHasNew
  com.arangodb.entity.DocumentCreateEntity
  (get-new [o] (.getNew o)))

(extend-protocol EntityHasOld
  com.arangodb.entity.DocumentCreateEntity
  (get-old [o] (.getOld o)))

(extend-protocol EntityHasOld
  com.arangodb.entity.DocumentDeleteEntity
  (get-old [o] (.getOld o)))
