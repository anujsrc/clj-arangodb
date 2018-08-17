(ns clj-arangodb.arangodb.accessors)


(defprotocol DocumentEntity
  (get-id  [document-entity] "get the document id")
  (get-key [document-entity] "get the document key")
  (get-rev [document-entity] "get the rev field")
  (get-new [document-create-entity])
  (get-old [document-entity]))

(defprotocol FieldEntity
  (get-serialized-name [o]))

(extend-protocol DocumentEntity
  com.arangodb.entity.DocumentEntity
  (get-id  [o] (.getId o))
  (get-key [o] (.getKey o))
  (get-rev [o] (.getRev o)))

(extend-protocol DocumentEntity
  com.arangodb.entity.DocumentCreateEntity
  ;; (get-id  [o] (.getId o))
  ;; (get-key [o] (.getKey o))
  ;; (get-rev [o] (.getRev o))
  (get-new [o] (.getNew o))
  (get-old [o] (.getOld o)))

(extend-protocol DocumentEntity
  com.arangodb.entity.DocumentDeleteEntity
  (get-old [o] (.getOld o)))
