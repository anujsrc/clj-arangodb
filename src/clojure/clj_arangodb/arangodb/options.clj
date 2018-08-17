(ns clj-arangodb.arangodb.options
  (:require [clojure.string :as str]))

(defn- class->symbol [^Class c]
  (symbol (.substring (.toString c) 6)))

(defn- fn-builder
  ;; given a map entry that represents a method call where the key (keyword)
  ;; is the method name and the `val` are args (modulo the implicit object)
  ;; returns a partial function that takes a single `object` argument.
  ;; to ensure that objects can be passed as args, we need to create a
  ;; partial function so that we do not need to eval any args directly!
  ;; Arguments single multiple arguments should be in a seq
  [class-symbol [method-key args]]
  (let [method-name (symbol (name method-key))
        args (flatten (list args))
        params (vec (repeatedly (inc (count args)) #(gensym)))
        ;; add meta to the object so that we dont get reflection warnings!
        obj-param (with-meta (peek params) {:tag class-symbol})
        arg-params (pop params)
        f (eval `(fn ~params (. ~obj-param ~method-name ~@arg-params)))]
    (apply partial f args)))

(defn build
  "takes a `class` and `map` of options.
  creates and calls a sequence of functions a new `object` of class `class`"
  [class options]
  (if (map? options)
    (reduce (fn [acc f]
              (f acc)) (eval `(new ~class))
            (map (partial fn-builder (class->symbol class)) options))
    options))
