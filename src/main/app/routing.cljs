(ns app.routing
  (:require [clojure.string :as str]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [pushy.core :as pushy]))

(def app-prefix "#")
(def default-route "/#/home")

(declare route-to!)

(defn create-history [app]
  (pushy/pushy (fn [path]
                 (let [route-segments (vec (rest (str/split path "/")))
                       path-prefix (first route-segments)]
                   (if (or (not= path-prefix app-prefix) (= 1 (count route-segments)))
                     (route-to! default-route)
                     (dr/change-route! app (rest route-segments)))))
    identity))

(defonce history (atom nil))

(defn start! [app]
  (pushy/start! (reset! history (create-history app))))

(defn route-to! [route]
  (let [route-string (if (vector? route) (str "/" (str/join "/" route)) route)
        route-string (if (str/starts-with? route-string (str "/" app-prefix))
                       route-string
                       (str "/" app-prefix route-string))]
    (pushy/set-token! @history route-string)))
