(ns nota.application
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [nota.application.data :as data]
            [nota.application.env :as env]
            [nota.application.remote :as remote]
            [nota.resolvers :as resolvers]
            [nota.routing :as routing]
            [nota.ui :as ui]
            [nota.ui.pages :as ui.pages]))

(defonce nota-app
  (app/fulcro-app {:remotes {:remote
                             (remote/local-pathom resolvers/resolvers
                                                  {:config env/config
                                                   :database-fn data/database-fn
                                                   :fetch-text-fn data/fetch-text-fn})}}))


(defn ^:export init []
  (app/set-root! nota-app ui/Root {:initialize-state? true})
  (dr/initialize! nota-app)
  (routing/start! nota-app)
  (app/mount! nota-app ui/Root "app" {:initialize-state? false})
  (df/load! nota-app :list-pages ui.pages/ListPage)
  (js/console.log "Loaded!"))

(defn ^:export refresh []
  ;; re-mounting will cause forced UI refresh
  (app/mount! nota-app ui/Root "app")
  ;; 3.3.0+ Make sure dynamic queries are refreshed
  (comp/refresh-dynamic-queries! nota-app)
  (js/console.log "Hot reload!"))
