(ns stasis.logics-test
  (:require
   [stasis.logics :as logics]
   [clojure.test :refer [deftest is]]
   [fulcro-spec.core :refer [behavior assertions]]))

(declare =>)

(deftest assoc-if-exists
  (behavior "assoc only if value isn't nil"
            (assertions
             "Should return nil"
             (logics/assoc-if-exists nil :a 1) => nil
             "Should return assoc'ed maps"
             (logics/assoc-if-exists {:a 1} :b 2) => {:a 1 :b 2})))

(deftest pagination
  (behavior "assoc only if value isn't nil"
            (assertions
             "Should return paginated list"
             (logics/pagination (range 1 101) 0 10) => [1 2 3 4 5 6 7 8 9 10]
             "Should return paginated list with 3 itens"
             (logics/pagination (range 1 104) 10 10) => [101 102 103]
             "Should return empty"
             (logics/pagination (range 1 101) 10 10) => [])))

(deftest filter-by-tag
  (let [fixture {"post-1" {:post/title "Post 1" :post/tags #{:a}}
                 "post-2" {:post/title "Post 2" :post/tags #{:a :b}}
                 "post-3" {:post/title "Post 3" :post/tags #{:a :b :c}}}]
    (is (= (logics/filter-by-tag fixture :post/tags :a)
           fixture))
    (is (= (logics/filter-by-tag fixture :post/tags :b)
           {"post-2" {:post/title "Post 2" :post/tags #{:a :b}}
            "post-3" {:post/title "Post 3" :post/tags #{:a :b :c}}}))
    (is (= (logics/filter-by-tag fixture :post/tags :c)
           {"post-3" {:post/title "Post 3" :post/tags #{:a :b :c}}}))))