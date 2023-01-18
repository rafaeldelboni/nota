(ns nota.ui.root-test
  (:require [clojure.test :refer [async deftest]]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [fulcro-spec.core :refer [assertions]]
            [nota.application.remote :as remote]
            [nota.resolvers :as resolvers]
            [nota.routing :as routing]
            [nota.ui :as ui]
            [nota.ui.pages :as ui.pages]))

(declare =>)

(def mock-config
  {:app-prefix "#"
   :default-route "home"})

(def mock-database
  {:pages {"home1"  {:page/name "Home"
                     :page/path "pages/index.md"}
           "about1" {:page/name "About"
                     :page/path "pages/about.md"}}})

(defn mock-database-fn
  ([table]
   (table mock-database))
  ([]
   mock-database))

(defn mock-fetch-text-fn
  [_ _]
  "#fetchbody")

(defn create-app []
  (let [app (app/headless-synchronous-app
             ui/Root
             {:remotes {:remote
                        (remote/local-pathom resolvers/resolvers
                                             {:config mock-config
                                              :database-fn mock-database-fn
                                              :fetch-text-fn mock-fetch-text-fn})}})]
    (app/set-root! app ui/Root {:initialize-state? true})
    (dr/initialize! app)
    (routing/start! app)
    (app/mount! app ui/Root "app" {:initialize-state? false})
    app))

(deftest integration->root
  (let [app (create-app)
        get-app-state (fn [state] (-> app app/current-state state))]
    (df/load! app :list-pages ui.pages/ListPage)
    (async done
           (js/setTimeout
            (fn []
              (assertions
               "Should root app state"
               (get-app-state :list-pages) => [[:page/id "home1"] [:page/id "about1"]])
              (done)
              500)))))
