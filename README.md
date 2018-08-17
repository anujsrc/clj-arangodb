# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.
The maintainers of arangodb provide a [java driver](https://docs.arangodb.com/devel/Drivers/Java/Reference/) for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.

## Philosophy

- Simple
- Unopinionated (But with sane defaults)
- Idiomatic (whatever that means)

## Lets connect

So... you've installed arangodb... fired it up... added `clj-arangodb` to your project... now what?

Creating a connection is as simple as passing a map.

Connecting using the Java client might look something like this:

```java
ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).host("192.168.182.50", 8888).build();
```

Nasty! This library abstracts away inner classes and calls to `build` - just pass a map.
In this example the keys of the map correspond to methods on the ArangoDB.Builder object.
If a method has arity > 1 then the arguments are placed in a vector.

```clojure
(require '[clj-arangodb.arangodb.core :as arango])
(import 'com.arangodb.Protocol)
(def conn (arango/connect {:useProtocol Protocol/VST :host ["192.168.182.50" 8888]}))
```

This approach will be explained slightly more when we look at passing options.

## Creating Databases and Collections

We have a connection object - it's now time to create a database.

The structure of the library can roughly be separated into `core`, `databases` and `collections`
The `core` namespace exposes functions that expect a **connection** object as their first parameter,
The `databases` namespace exposes those that expect a **database** object and so on.

Lets assume that you are testing locally and have given a user "test" access -
we create a new database and collection (assuming that they do not exist).
We will not bother with any options here

```clojure
(def conn (arango/connect {:user "test"}))
(def db (arango/create-and-get-database conn "someDB"))
(def coll (databases/create-and-get-collection db "someColl"))
```
Now that we have a collection, lets add some *stuff*.

## Adding documents

## Document positions

As a rule funtions that accept documents/keys/ids take them as their last argument - this is **not** inkeeping with the underlying methods.
Connection (database/collection) objects appear in position 1 (standard) and then arguments are in reverse order.

```clojure
(defn insert-document ^DocumentCreateEntity
  ([^ArangoCollection coll ^Object doc]
   (.insertDocument coll (adapter/serialize doc)))
  ([^ArangoCollection coll ^DocumentCreateOptions options ^Object doc]
   (.insertDocument coll (adapter/serialize doc) (options/build DocumentCreateOptions options))))
```

```clojure
(defn get-document
  ([^ArangoCollection coll ^Class as ^String key]
   (.getDocument coll key as))
  ([^ArangoCollection coll ^DocumentReadOptions options ^Class as ^String key]
   (.getDocument coll key as (options/build DocumentReadOptions options))))
```


This is a consious decision as it allows for use of the `->>` (thread last) macro.

# What is a document

A document is a mapping from keys to vals - by default the java driver allows the user to pass
the following

- JSON
- VPackSlice
- POJO (plain old Java object)

By default if you pass a clojure map (strictly speaking anything that implements the `IPersistentMap` interface) - then
it will be converted into a `VPackSlice` behind the scenes by `adapter/serialize`.

```
(defprotocol Serialize
  (serialize [data] "enusre that the data is in a suitable format"))

(extend-protocol Serialize
  clojure.lang.IPersistentMap
  (serialize [data] (vpack/pack data))
  Object
  (serialize [data] data))
```


The `VpackSlice` Object is of interest to us as it used internally by the database. This library provides functionality for converting both to and from.

Most return values are `Entity` objects - for example inserting a document returns a `DocumentCreateEntity`.
Initially I thought it was a good idea to implicity convert these objects into clojure maps - however, if you are after performance (while not much of an overhead) such an implicit conversion is undesirable.
Consider for example the case when you want to call

```clojure
(->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
     vpack/pack
     (collections/insert-document coll {:silent true})
```

The silent option means that no information will be returned - making such an implict convertsion superfluous.



Lets assume that we have a document will use the function `collections/insert-document`

## Vpack

```clojure
user> (defrecord Person [name surname age])
user.Person
user> (->Person "a" "b" 23)
#user.Person{:name "a", :surname "b", :age 23}
user> (vpack/pack (->Person "a" "b" 23))
#object[com.arangodb.velocypack.VPackSlice 0x4b0b8826 "{\"name\":\"a\",\"surname\":\"b\",\"age\":23}"]
user> (-> (->Person "a" "b" 23)
      	  vpack/pack
          vpack/unpack
          map->Person)
#user.Person{:name "a", :surname "b", :age 23}
```

## An introduction to Options

The majority of methods allow for an optional **Options** object to be passed.
The constructors are found under `com.arangodb.model.*` in the java driver.

Options are used in a very similar way to how we created a connection.

Lets assume that we want to insert a document - looking at the [documentation](https://docs.arangodb.com/devel/Drivers/Java/Reference/Collection/DocumentManipulation.html) we see that the method `insertDocument` takes an optional `DocumentCreateOptions`.

The `options` namespace provides a single function `build`

`(build class options)`

If `options` is a map then the **keys** are treated as methods and the `vals` treated as arguments.
If `options` is **not** a map then then it returns `options`.

```clojure
user> (options/build com.arangodb.model.DocumentCreateOptions {:returnNew true :waitForSync true})
#object[com.arangodb.model.DocumentCreateOptions 0x709af356 "com.arangodb.model.DocumentCreateOptions@709af356"]
```
This has the same effect as:
```clojure
(-> (new com.arangodb.model.DocumentCreateOptions)
    (.returnNew true)
    (.waitForSync true))
```

As this is a pain to work with, every function that allows for options to be passed performs this behind the scenes -
If you are after performance and want to re-use options, then just create them explicity and pass them to the functions.
