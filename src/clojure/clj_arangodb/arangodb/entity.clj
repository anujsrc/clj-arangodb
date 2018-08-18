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

(defprotocol ErrorEntity
  (get-error-message [entity])
  (get-exception [entity])
  (get-code [entity])
  (get-error-num [entity]))

(defprotocol EntityHasKey
  (get-key [entity] "get the associated key"))

(defprotocol EntityContainsErrors
  (has-error? [entity] "does the entity contain an error?"))

(defprotocol EntityHasId
  (get-id [entity] "get the associated id"))

(defprotocol EntityHasRevision
  (get-rev [entity] "get the associated revision"))

(defprotocol EntityHasNew
  (get-new [entity] "get the associated new value"))

(defprotocol EntityHasOld
  (get-old [entity] "get the associated old value"))

(defprotocol EntityHasName
  (get-name [entity] "get the associated name"))

(defprotocol EntityHasAttribute
  (get-attribute [entity k] "get the associated attribute"))

(defprotocol EntityHasProperties
  (get-properties [entity] "get the associated property map"))

(defprotocol EntityHasSerializeName
  (get-serialize-name [entity] "get the associated serialize name"))

(defprotocol EntityHasDocuments
  (get-documents [entity] "get the associated documents"))

(defprotocol EntityHasErrors
  (get-errors [entity] "get the associated errors"))

(defprotocol EntityHasDocumentsAndErrors
  (get-documents-and-errors [entity] "get the associated documents and errors"))

(extend-protocol EntityContainsErrors
  com.arangodb.entity.MultiDocumentEntity
  (has-error? [entity]
    (not= 0 (.size ^java.util.ArrayList (get-errors entity)))))

(extend-protocol ErrorEntity
  com.arangodb.entity.ErrorEntity
  (get-error-message [o] ^String (.getErrorMessage o))
  (get-exception [o] ^String (.getException o))
  (get-code [o] ^Integer (.getCode o))
  (get-error-num [o] ^Integer (.getErrorNum o)))

(extend-protocol EntityHasId
  com.arangodb.entity.DatabaseEntity
  (get-id [o] (.getId o))
  com.arangodb.entity.DocumentEntity
  (get-id [o] (.getId o))
  com.arangodb.entity.BaseDocument
  (get-id [o] (.getId o)))

(extend-protocol EntityHasName
  com.arangodb.entity.DatabaseEntity
  (get-name [o] (.getName o)))

(extend-protocol EntityHasKey
  com.arangodb.entity.DocumentEntity
  (get-key [o] (.getKey o))
  com.arangodb.entity.BaseDocument
  (get-key [o] (.getKey o)))

(extend-protocol EntityHasRevision
  com.arangodb.entity.DocumentEntity
  (get-rev [o] (.getRev o))
  com.arangodb.entity.BaseDocument
  (get-rev [o] (.getRevision o)))

(extend-protocol EntityHasProperties
  com.arangodb.entity.BaseDocument
  (get-properties [o] (.getProperties o)))

(extend-protocol EntityHasAttribute
  com.arangodb.entity.BaseDocument
  (get-attribute [o ^String k] (.getAttribute o k)))

(extend-protocol EntityHasNew
  com.arangodb.entity.DocumentCreateEntity
  (get-new [o] (.getNew o)))

(extend-protocol EntityHasOld
  com.arangodb.entity.DocumentCreateEntity
  (get-old [o] (.getOld o))
  com.arangodb.entity.DocumentDeleteEntity
  (get-old [o] (.getOld o)))

(extend-protocol EntityHasDocuments
  com.arangodb.entity.MultiDocumentEntity
  (get-documents [o] (.getDocuments o)))

(extend-protocol EntityHasErrors
  com.arangodb.entity.MultiDocumentEntity
  (get-errors [o] (.getErrors o)))

(extend-protocol EntityHasDocumentsAndErrors
  com.arangodb.entity.MultiDocumentEntity
  (get-documents-and-errors [o] (.getDocumentsAndErrors o)))




(defn getter [entity keyword]
  (let [arg (with-meta (gensym) {:tag (class entity)})
        method (symbol (name keyword))]
    ((eval `(fn [~arg] (. ~arg ~method))) entity)))

(defprotocol EntityHasErrors
  (get-errors [entity] "get the associated errors"))
