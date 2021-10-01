(ns app.application
  (:require [app.remote :as remote]
            [app.resolvers :as resolvers]
            [com.fulcrologic.fulcro.application :as app]))

(defonce app (app/fulcro-app
              {:remotes {:remote (remote/local-pathom resolvers/resolvers)}}))
