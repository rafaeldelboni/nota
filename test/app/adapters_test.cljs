(ns app.adapters-test
  (:require
   [clojure.test :refer [deftest]]
   [app.adapters :as adapters]
   [fulcro-spec.core :refer [behavior assertions]]))

(declare =>)

(deftest vec->map-list-id
  (behavior "should adapt vec into a map list with key id"
            (assertions
             (adapters/vec->map-list-id [:a :b :c] :ns/id) => [{:ns/id :a} {:ns/id :b} {:ns/id :c}]
             (adapters/vec->map-list-id [1 2] :a) => [{:a 1} {:a 2}])))

(deftest assoc-vec-as-map-list-id
  (behavior "should assoc adapted vec into a map list with key id on value"
            (assertions
             (adapters/assoc-vec-as-map-list-id {:meta "data" :pages [:a :b :c]} :pages :page/id) =>
             {:meta "data"
              :pages [{:page/id :a}
                      {:page/id :b}
                      {:page/id :c}]})))

(deftest hashset->map-list-id
  (behavior "should adapt hashset into a map list with key id"
            (assertions
             (adapters/hashmap->map-list-id {"page-1" {:page/number 1}
                                             "page-2" {:page/number 2}
                                             "page-3" {:page/number 3}}
                                            :page/id) =>
             [{:page/id "page-1" :page/number 1}
              {:page/id "page-2" :page/number 2}
              {:page/id "page-3" :page/number 3}])))
