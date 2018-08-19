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

The structure of the library can roughly be separated into `core`, `databases` and `collections`
The `core` namespace exposes functions that expect a **connection** object as their first parameter,
The `databases` namespace exposes those that expect a **database** object and so on.

Lets assume that you are testing locally and have created a "test" user -
we can create a new database and collection (assuming that they do not exist) using the following commands

```clojure
(def conn (arango/connect {:user "test"}))
(def db (arango/create-and-get-database conn "someDB"))
(def coll (databases/create-and-get-collection db "someColl"))
```
If we only wanted the collection then we could use the following

```clojure
(def coll
  (-> {:user "test"}
      arrango/connect
      (arango/create-and-get-database "someDB")
      (databases/create-and-get-collection "someColl"))
```
However as these are statefull java objects it makes sense to keep them around for re-use


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

This is a consious decision as it allows both for use of the `->>` (thread last) macro and writing `partial` functions

### What is a document

A document is a mapping from keys to vals - by default the java driver allows the user to pass
the following

- JSON
- VPackSlice
- POJO (plain old Java object)

By default if you pass a clojure map (strictly speaking anything that implements the `IPersistentMap` interface) - then
it will be converted into a `VPackSlice` behind the scenes by `adapter/serialize`.
The java driver allows for serialization/deserialization *modules* to be attached when creating a connection.
Currently there is not one for Clojure datatypes. If/when such a module is written (so far my attempts have failed) then
the `Serialize` protocol will be removed as it will be handled internally by the driver.

```clojure
(defprotocol Serialize
  (serialize [data] "enusre that the data is in a suitable format"))

(extend-protocol Serialize
  clojure.lang.IPersistentMap
  (serialize [data] (vpack/pack data))
  Object
  (serialize [data] data))
```

Thus the following two calls are identical

```clojure
(->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
     (collections/insert-document coll)
```

```clojure
(->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
     vpack/pack
     (collections/insert-document coll)
```

You can pass JSON encoded strings
```clojure
(->> "{\"name\":\"Leonhard\",\"surname\":\"Euler\",\"likes\":\"graphs\",\"age\":28}"
     (c/insert-document coll))
```
Or java Maps (note that the keys are strings)
```clojure
(->> {"name" "Leonhard" "surname" "Euler" "likes" "graphs" "age" 28}
     (java.util.HashMap.)
     (collections/insert-document coll))
```

## Entity Objects

When we inserted our document the java driver returned an `DocumentCreateEntity` object.
The namespace `clj-arangodb.arangodb.entity` provides utility functions for accessing fields and for converting them
into clojure data structures.




Until recently this library had chosen to **implicitly** convert these objects into clojure maps.
While in many cases this was desirable - it is not always the case.
For example consider the call

```clojure
(->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
     vpack/pack
     (collections/insert-document coll {:silent true})
```
In which the resulting `DocumentCreateEntity` will not have any values (`silent` option). Converting this into a clojure map is just a waste of time.

Another example is when you simply want to keep the returned `key` or `id` for later processing.

This is where the `entity` namespace comes in. It provides a nummber of protocols (to avoid reflection warnings) that can be extended.

``` clojure
user> (->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
           (c/insert-document coll)
           entity/get-key)
"2859436"
```

```clojure
user> (->> {:name "Leonhard" :surname "Euler" :likes "graphs" :age 28}
           (c/insert-document coll)
           entity/from-entity)
{:class com.arangodb.entity.DocumentCreateEntity, :id "someColl/2860840", :key "2860840", :new nil, :old nil, :rev "_XR8bHYK--_"}
```

## Retreiving Documents

Documents can be retreived using a `database` object or a `collection` object.
If the `database` is being used then an `id` must be provided, with a `collection` a `key` is required.

```clojure
(defn get-document
  ([^ArangoCollection coll ^String key]
   (adapter/deserialize-doc (.getDocument coll key VPackSlice)))
  ([^ArangoCollection coll ^Class as ^String key]
   (.getDocument coll key as))
  ([^ArangoCollection coll ^DocumentReadOptions options ^Class as ^String key]
   (.getDocument coll key as (options/build DocumentReadOptions options))))
```

If no class is passed then it defaults to deserialization using the `deserialize-doc` protocol for `VPackSlice`

The interesting thing here is that the `class` of the deserialized document must be passed.
If `String` is passed then the resulting document will be *JSON*, `VPackSlice` returns a *VPackSlice*

To make life a bit easier there are functions `get-base-document`, `get-json-document` etc that call `get-document` and deserialize under the hood.


Lets assume that we have a `res` document from using the function `collections/insert-document`

### VPack

By **not** passing a *Class* we get automatic deserialization. The returned object is a clojure map. By default the `unpack` function will keywordize the keys.
```clojure
user> (->> (entity/get-key res)
           (c/get-document coll))
{:_id "someColl/2859734", :_key "2859734", :_rev "_XR8Os5e--_", :age 28, :likes "graphs", :name "Leonhard", :surname "Euler"}
```

```clojure
user> (->> (entity/get-key res)
           (c/get-document coll VPackSlice))
#object[com.arangodb.velocypack.VPackSlice 0x3f287f55 "{\"_id\":\"someColl\\/2859734\",\"_key\":\"2859734\",\"_rev\":\"_XR8Os5e--_\",\"age\":28,\"likes\":\"graphs\",\"name\":\"Leonhard\",\"surname\":\"Euler\"}"]
```
You can pass any function that you want in this example we use `identity` so that the keys are keps as strings
```clojure
user> (->> (entity/get-key res)
           (c/get-document coll VPackSlice)
           (vpack/unpack identity))
{"_id" "someColl/2859734", "_key" "2859734", "_rev" "_XR8Os5e--_", "age" 28, "likes" "graphs", "name" "Leonhard", "surname" "Euler"}
```

### JSON (String)

```clojure
user> (->> (entity/get-key res)
           (c/get-document coll String))
"{\"_id\":\"someColl\\/2859734\",\"_key\":\"2859734\",\"_rev\":\"_XR8Os5e--_\",\"age\":28,\"likes\":\"graphs\",\"name\":\"Leonhard\",\"surname\":\"Euler\"}"
```

### java.util.Map and Object

Note that the returned object is mutable!

```clojure
user> (->> (entity/get-key res)
      	   (c/get-document coll java.util.Map))
{"surname" "Euler", "_rev" "_XR8Os5e--_", "name" "Leonhard", "_id" "someColl/2859734", "_key" "2859734", "age" 28, "likes" "graphs"}
```
passing the `Object` class returns a `java.util.HashMap`

```clojure
user> (->> res
           entity/get-key
           (c/get-document coll Object))
{"surname" "Euler", "_rev" "_XR8Os5e--_", "name" "Leonhard", "_id" "someColl/2859734", "_key" "2859734", "age" 28, "likes" "graphs"}
```

### com.arangodb.entity.BaseDocument

The java driver provides a `BaseDocument` class that implements the `Entity` interface.

```clojure
user> (->> (entity/get-key res)
      	   (c/get-document coll com.arangodb.entity.BaseDocument))
#object[com.arangodb.entity.BaseDocument 0xa5124bc "BaseDocument [documentRevision=_XR8Os5e--_, documentHandle=someColl/2859734, documentKey=2859734, properties={surname=Euler, name=Leonhard, age=28, likes=graphs}]"]
```

```clojure
user> (->> (entity/get-key res)
       	   (c/get-document coll com.arangodb.entity.BaseDocument)
	   entity/from-entity)
{:class com.arangodb.entity.BaseDocument, :id "someColl/2859734", :key "2859734", :properties {"surname" "Euler", "name" "Leonhard", "age" 28, "likes" "graphs"}, :revision "_XR8Os5e--_"}
```

### Clojure Class (clojure.lang.PersistentArrayMap etc)

Unfortunately if you try to pass a clojure Class then you get a nasty exception.

```clojure
user> (->> (entity/get-key res)
      	   (c/get-document coll clojure.lang.PersistentArrayMap))
IllegalAccessException Class com.arangodb.velocypack.VPack can not access a member of class clojure.lang.PersistentArrayMap with modifiers "protected"  sun.reflect.Reflection.ensureMemberAccess (Reflection.java:102)
```

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

The namespace `clj-arangodb.velocypack.core` provides you with a host of utility functions for working with VPackSlice objects.

```clojure
user> (def xs (vpack/pack {:name "Leonhard" :nested {:likes ["graphs" "bridges"] :dislikes []}}))
#'user/xs
```

```clojure
user> xs
#object[com.arangodb.velocypack.VPackSlice 0x7f59241e "{\"name\":\"Leonhard\",\"nested\":{\"likes\":[\"graphs\",\"bridges\"],\"dislikes\":[]}}"]
```

```clojure
user> (vpack/unpack xs)
{:name "Leonhard", :nested {:dislikes [], :likes ["graphs" "bridges"]}}
user> (vpack/unpack identity xs)
{"name" "Leonhard", "nested" {"dislikes" [], "likes" ["graphs" "bridges"]}}
```

It is possible to get from a VPackSlice - this takes an optional `not-found`
```clojure
user> (vpack/slice-get xs :name)
#object[com.arangodb.velocypack.VPackSlice 0x45f670d3 "\"Leonhard\""]
```

`unpack-get` will unpack only under the key provided - returns `nil` if key not found.
```clojure
user> (vpack/unpack-get xs :name)
"Leonhard"
```
`unpack-get-in` allows for unpacking nested structures
`unpack-get` will unpack only under the key provided - returns `nil` if key not found.
```clojure
user> (vpack/unpack-get-in xs [:nested :likes])
["graphs" "bridges"]
```

VPackSlice has been extended to support both reduce and kv-reduce
```clojure
user> (vpack/slice-get-in xs [:nested :likes])
#object[com.arangodb.velocypack.VPackSlice 0x1e6b854b "[\"graphs\",\"bridges\"]"]
user> (reduce (fn [acc x] (conj acc x)) [] (vpack/slice-get-in xs [:nested :likes]))
[#object[com.arangodb.velocypack.VPackSlice 0x224346b2 "\"graphs\""] #object[com.arangodb.velocypack.VPackSlice 0x72e852c9 "\"bridges\""]]
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
