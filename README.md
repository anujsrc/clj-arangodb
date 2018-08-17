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

Lets see what happens if we pass it an empty map...

```clojure
user> (require '[clj-arangodb.arangodb.options :as options])
nil
user> (def opts (options/build com.arangodb.model.DocumentCreateOptions {}))
#'user/opts
user> opts
#object[com.arangodb.model.DocumentCreateOptions 0x709af356 "com.arangodb.model.DocumentCreateOptions@709af356"]
;; the function `bean` lets us see the fields.
user> (bean opts)
{:class com.arangodb.model.DocumentCreateOptions, :overwrite nil, :returnNew nil, :returnOld nil, :silent nil, :waitForSync nil}
```
According to the documentation, these are all boolean flags - so lets try and set `returnNew`

```clojure
user> (bean (options/build com.arangodb.model.DocumentCreateOptions {:returnNew true}))
{:class com.arangodb.model.DocumentCreateOptions, :overwrite nil, :returnNew true, :returnOld nil, :silent nil, :waitForSync nil}
```
So thats options!
