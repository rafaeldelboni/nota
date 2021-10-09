(ns stasis.routing.pushy-test
  (:require [clojure.test :refer [async deftest is] :as test]
            [secretary.core :as secretary :refer-macros [defroute]]
            [stasis.routing.pushy :as pushy])
  (:import goog.history.Html5History))

(secretary/set-config! :prefix "/")
(def test-val (atom false))

(def history
  (pushy/pushy secretary/dispatch!
               (fn [x] (when (secretary/locate-route x) x))
               identity))

(defroute foo-route "/foo" []
  (reset! test-val true))

(defroute bar-route "/bar" []
  (reset! test-val true))

(deftest constructing-history
  (is (instance? Html5History (pushy/new-history))))

(deftest constructing-pushy
  (is (satisfies? pushy/IHistory (pushy/pushy (constantly nil) (constantly nil)))))

(deftest supported-browser
  (is (pushy/supported?)))

;; event listeners started = dispatch
(deftest push-state-foo-route
  (async done
         (reset! test-val false)
         (pushy/start! history)
         (pushy/replace-token! history "/foo")
         (js/setTimeout
          (fn []
            (is @test-val)
            (is (nil? (pushy/stop! history)))
            (is (= "/foo" (pushy/get-token history)))
            (pushy/replace-token! history "/")
            (done))
          500)))

;; no event listeners started = no dispatch
(deftest push-state-bar-route
  (async done
         (reset! test-val false)
         (pushy/replace-token! history "/bar")
         (js/setTimeout
          (fn []
            (is (false? @test-val))
            (is (= "/bar" (pushy/get-token history)))
            (pushy/replace-token! history "/")
            (done))
          500)))
