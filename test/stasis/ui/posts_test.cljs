(ns stasis.ui.posts-test
  (:require
   [clojure.test :refer [deftest]]
   [stasis.ui.posts :as ui.posts]
   [fulcro-spec.core :refer [behavior assertions]]))

(declare =>)

(deftest page-exists
  (behavior "should check if page exists in app state"
            (assertions
             (ui.posts/page-exists? {} 1) => false
             (ui.posts/page-exists? {:pagination/by-number {:page-number {:pagination/posts []}}} 1) => false
             (ui.posts/page-exists? {:pagination/by-number {0 {:pagination/posts [:a :b :c]}}} 1) => false
             (ui.posts/page-exists? {:pagination/by-number {1 {:pagination/posts [:a :b :c]}}} 1) => true)))

(deftest gc-distant-pages
  (behavior "should garbage collect pages from more than 5 positions of given number"
            (assertions
             (ui.posts/gc-distant-pages {:pagination/by-number {1 {:pagination/posts [{:a 1} {:a 2} {:a 3}]}}} 1)
             => {:pagination/by-number {1 {:pagination/posts [{:a 1} {:a 2} {:a 3}]}}}
             (ui.posts/gc-distant-pages {:pagination/by-number {6 {:pagination/posts [{:a 1} {:a 2} {:a 3}]}}} 1)
             => {:pagination/by-number {}, :posts/by-id nil})))
