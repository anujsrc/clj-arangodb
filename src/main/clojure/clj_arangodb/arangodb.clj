(ns clj-arangodb.arangodb
  (:require [clj-arangodb.options :as options])
  (:import [com.arangodb
            ArangoDB$Builder
            ArangoDB
            ArangoDatabase]))

(defn connect
  "
  Takes an optional map or collection of pairs that may contain the following:
  keys have the same names as the java methods.
  :host - [String nat-int?] - default is ['127.0.0.1' 8529] (multiple allowed)
  :user String - default 'root'
  :password String - by default no password is used
  :useProtocol com.arangodb.Protocol - default com.arangodb.Protocol/VST
  :sslContext SSlContext - default nil
  :timeout Integer | Long
  :chunksize Integer | Long
  :maxConnections Integer | Long
  :acquireHostList Boolean
  :loadBalancingStrategy com.arangodb.entity.LoadBalancingStrategy
  :connectionTtl nil | Long - default nil
  and more
  "
  ([] (connect {}))
  (^ArangoDB [options]
   (.build ^ArangoDB$Builder (options/build ArangoDB$Builder options))))

(defn shutdown
  "
  The driver does not explicitly release connections.
  To avoid exhaustion of resources when no connection is needed,
  you can clear the connection pool (close all connections to the server)
  or use connection TTL.
  "
  [^ArangoDB conn] (.shutdown conn))

(defn db
  "Returns a new `ArrangoDatabase` instance"
  ^ArangoDatabase [^ArangoDB conn label]
  (.db conn label))

(defn create-database
  "Create a new database with name `label`.
  Returns `true` on success else `ArangoDBException`"
  [^ArangoDB conn label]
  (.createDatabase conn label))

(defn ensure-and-get-database
  "if database with `label` does not exist, it will create it.
  Returns an `ArangoDatabase` instance"
  ^ArangoDatabase
  [^ArangoDB conn label]
  (let [database (db conn label)]
    (when-not (.exists database)
      (.create database))
    database))
