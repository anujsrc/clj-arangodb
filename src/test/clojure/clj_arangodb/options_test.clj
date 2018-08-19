(ns clj-arangodb.options-test
  (:require [clojure.test :refer :all]
            [clj-arangodb.options :as options])
  (:import [com.arangodb.model
            CollectionCreateOptions]))

(deftest build-test
  (testing "Can pass args to variadic function using a String Array"
    (is (= CollectionCreateOptions
           (->> {:doCompact true :shardKeys (into-array ["a" "b" "c" "d"])}
                (options/build CollectionCreateOptions)
                class))))
  (testing "Can call multiple times - arguments evaluated from left to right"
    (is (= true
           (->> [[:doCompact true]]
                ^CollectionCreateOptions (options/build CollectionCreateOptions)
                .getDoCompact)))
    (is (= false
           (->> [[:doCompact true] [:doCompact false]]
                ^CollectionCreateOptions (options/build CollectionCreateOptions)
                .getDoCompact)))
    (is (= true
           (->> [[:doCompact true] [:doCompact false] [:doCompact true]]
                ^CollectionCreateOptions (options/build CollectionCreateOptions)
                .getDoCompact))))
  (testing "passing a collection object returns the object"
    (let [o (CollectionCreateOptions.)]
      (is (= o (options/build CollectionCreateOptions o))))))
