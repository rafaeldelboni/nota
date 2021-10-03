(ns app.logics-test
  (:require
   [clojure.test :refer [deftest]]
   [app.logics :as logics]
   [fulcro-spec.core :refer [behavior assertions]]))

(declare =>)

(deftest assoc-if-exists
  (behavior "assoc only if value isn't nil"
            (assertions
             "Should return nil"
             (logics/assoc-if-exists nil :a 1) => nil
             "Should return assoc'ed maps"
             (logics/assoc-if-exists {:a 1} :b 2) => {:a 1 :b 2})))
