(ns clj-arangodb.arangodb.accessors)

(defprotocol EntityHasKey
  (get-key [entity] "get the associated key"))

(defprotocol EntityHasId
  (get-id [entity] "get the associated id"))

(defprotocol EntityHasRev
  (get-rev [entity] "get the associated revision"))

(extend-protocol EntityHasKey
  com.arangodb.entity.DocumentEntity
  (get-key [o] (.getKey o)))

(extend-protocol EntityHasId
  com.arangodb.entity.DocumentEntity
  (get-key [o] (.getId o)))

(extend-protocol EntityHasRev
  com.arangodb.entity.DocumentEntity
  (get-rev [o] (.getRev o)))

;; (defprotocol DocumentEntity
;;   (get-id  [document-entity] "get the document id")
;;   (get-key [document-entity] "get the document key")
;;   (get-rev [document-entity] "get the rev field")
;;   (get-new [document-create-entity])
;;   (get-old [document-entity]))

;; (defprotocol FieldEntity
;;   (get-serialized-name [o]))

;; (extend-protocol DocumentEntity
;;   com.arangodb.entity.DocumentEntity
;;   (get-id  [o] (.getId o))
;;   (get-key [o] (.getKey o))
;;   (get-rev [o] (.getRev o)))

;; (extend-protocol DocumentEntity
;;   com.arangodb.entity.DocumentCreateEntity
;;   ;; (get-id  [o] (.getId o))
;;   ;; (get-key [o] (.getKey o))
;;   ;; (get-rev [o] (.getRev o))
;;   (get-new [o] (.getNew o))
;;   (get-old [o] (.getOld o)))

;; (extend-protocol DocumentEntity
;;   com.arangodb.entity.DocumentDeleteEntity
;;   (get-old [o] (.getOld o)))
