(ns app.client
  (:require [app.application :refer [app]]
            [app.routing :as routing]
            [app.ui :as ui]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

(defn ^:export init []
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app)
  (routing/start! app)
  (app/mount! app ui/Root "app" {:initialize-state? false})
  (df/load! app :list-pages ui/ListPage)
  (js/console.log "Loaded!"))

(defn ^:export refresh []
  ;; re-mounting will cause forced UI refresh
  (app/mount! app ui/Root "app")
  ;; 3.3.0+ Make sure dynamic queries are refreshed
  (comp/refresh-dynamic-queries! app)
  (js/console.log "Hot reload!"))
