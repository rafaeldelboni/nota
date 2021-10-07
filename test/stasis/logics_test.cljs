(ns stasis.logics-test
  (:require
   [clojure.test :refer [deftest]]
   [stasis.logics :as logics]
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
