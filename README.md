# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.

## Philosophy

## Examples

## An introduction to Options

The majority of methods allow for an optional **Options** object to be passed.
The constructors are found under `com.arangodb.model.*` in the java driver.

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
