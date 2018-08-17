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
