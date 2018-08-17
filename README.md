# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.

## Philosophy

## Examples

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
(def arango-db (arango/connect {:useProtocol Protocol/VST :host ["192.168.182.50" 8888]}))
```

This approach will be explained slightly more when we look at passing options.


## An introduction to Options

The majority of methods allow for an optional **Options** object to be passed.
The constructors are found under `com.arangodb.model.*` in the java driver.

Options are used in a very similar way to how we created a connection.

Information can be found [here](https://docs.arangodb.com/devel/Drivers/Java/Reference/)

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
