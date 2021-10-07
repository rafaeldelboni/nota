(ns stasis.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
            [stasis.ui.pages :as ui.pages]
            [stasis.ui.posts :as ui.posts]
            [stasis.routing :as routing]))

(defrouter TopRouter [_this {:keys [current-state]}]
  {:router-targets [ui.pages/Page ui.posts/ListPost]}
  (js/console.log current-state)
  (case current-state
    (nil :pending) (dom/div "Loading...")
    :failed (dom/div "Loading seems to have failed. Try another route.")
    (dom/div "Unknown route")))

(def ui-top-router (comp/factory TopRouter))

(defsc Root [_this {:keys [list-pages]
                    :root/keys [router]}]
  {:query         [{:list-pages (comp/get-query ui.pages/ListPage)}
                   {:root/router (comp/get-query TopRouter)}]
   :initial-state {:root/router {}}}
  (dom/div
   (map ui.pages/ui-list-page list-pages)
   (dom/button {:onClick #(routing/route-to! (dr/path-to ui.posts/ListPost "list"))} "Go to Blog")
   (dom/hr)
   (ui-top-router router)
   (dom/hr)
   (dom/div "footer")))
