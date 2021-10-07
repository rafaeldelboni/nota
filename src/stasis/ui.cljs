(ns stasis.ui
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
            ["react-markdown" :as react-markdown]
            ["remark-gfm" :as remark-gfm]
            [stasis.routing :as routing]))

(def ui-markdown (interop/react-factory react-markdown/default))

(defsc Page [_this {:page/keys [id path body]}]
  {:query           [:ui/modified?
                     :page/id
                     :page/name
                     :page/path
                     :page/body]
   :ident           :page/id
   :route-segment   [:page/id]
   :will-enter      (fn [app {:page/keys [id]}]
                      (dr/route-deferred [:page/id id]
                                         #(df/load! app [:page/id id] Page
                                                    {:post-mutation `dr/target-ready
                                                     :post-mutation-params
                                                     {:target [:page/id id]}})))}
  (if body
    (dom/div
     (dom/h2 (str "Id " id))
     (dom/h2 (str "Path " path))
     (ui-markdown {:children body
                   :remarkPlugins [remark-gfm/default]}))
    (dom/div "loading")))

(defsc ListPage [_this {:page/keys [id name] :as props}]
  {:query [:page/id
           :page/name]
   :ident (fn [] [:page/id (:page/id props)])}
  (dom/button {:onClick #(routing/route-to! (dr/path-to Page id))} name))

(def ui-list-page (comp/factory ListPage {:keyfn :page/id}))

(defsc ListPost [_this _props]
  {:ident         (fn [] [:component/id :list-posts])
   :query         [{'(:list-posts {:page 0 :page-size 11})
                    [:post/id
                     :post/time
                     :post/title
                     :post/description]}]
   :route-segment ["post" "list"]
   :will-enter    (fn [app _route-params]
                    (dr/route-deferred [:component/id :list-posts]
                                       #(df/load! app [:component/id :list-posts] ListPost
                                                  {:post-mutation `dr/target-ready
                                                   :post-mutation-params
                                                   {:target [:component/id :list-posts]}})))}
  ;TODO render post list with links
  (js/console.log "list/post/props" _props)
  (dom/div "Post List"))

(defrouter TopRouter [_this {:keys [current-state]}]
  {:router-targets [Page ListPost]}
  (js/console.log current-state)
  (case current-state
    (nil :pending) (dom/div "Loading...")
    :failed (dom/div "Loading seems to have failed. Try another route.")
    (dom/div "Unknown route")))

(def ui-top-router (comp/factory TopRouter))

(defsc Root [_this {:keys [list-pages]
                    :root/keys [router]}]
  {:query         [{:list-pages (comp/get-query ListPage)}
                   {:root/router (comp/get-query TopRouter)}]
   :initial-state {:root/router {}}}
  (dom/div
   (map ui-list-page list-pages)
   (dom/button {:onClick #(routing/route-to! (dr/path-to ListPost "list"))} "Go to Blog")
   (dom/hr)
   (ui-top-router router)
   (dom/hr)
   (dom/div "footer")))
