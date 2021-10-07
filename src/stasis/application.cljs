(ns stasis.application
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [stasis.application.remote :as remote]
            [stasis.resolvers :as resolvers]
            [stasis.routing :as routing]
            [stasis.ui :as ui]
            [stasis.ui.pages :as ui.pages]))

(defonce stasis-app (app/fulcro-app
              {:remotes {:remote (remote/local-pathom resolvers/resolvers)}}))

(defn ^:export init []
  (app/set-root! stasis-app ui/Root {:initialize-state? true})
  (dr/initialize! stasis-app)
  (routing/start! stasis-app)
  (app/mount! stasis-app ui/Root "app" {:initialize-state? false})
  (df/load! stasis-app :list-pages ui.pages/ListPage)
  (js/console.log "Loaded!"))

(defn ^:export refresh []
  ;; re-mounting will cause forced UI refresh
  (app/mount! stasis-app ui/Root "app")
  ;; 3.3.0+ Make sure dynamic queries are refreshed
  (comp/refresh-dynamic-queries! stasis-app)
  (js/console.log "Hot reload!"))
