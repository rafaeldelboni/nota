(ns app.routing
  (:require [clojure.string :as str]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [pushy.core :as pushy]))

(def base-route "stasis")
(def app-prefix "#")
(def default-route "home")

(defonce history (atom nil))

(defn route-to!
  "Change routes to the given route-string"
  [route-string]
  (pushy/set-token! @history (->> route-string
                                  (into [base-route
                                         app-prefix])
                                  (remove str/blank?)
                                  (str/join "/"))))

(defn create-history [app]
  (pushy/pushy (fn [path]
                 (let [real-path (if (= (first path) "/")
                                   path
                                   (str "/" path))
                       path-vec (->> (str/split real-path "/")
                                     (remove str/blank?)
                                     (remove #(= % base-route))
                                     (remove #(= % app-prefix))
                                     vec)
                       route-segments path-vec]
                   (if (or (empty? route-segments)
                           (= [base-route] route-segments))
                     (route-to! [default-route])
                     (dr/change-route app route-segments))))
               identity))

(defn start! [app]
  (pushy/start! (reset! history (create-history app))))
