(ns stasis.ui.pages 
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            ["react-markdown" :default ReactMarkdown]
            ["remark-gfm" :default remarkGfm]
            [stasis.routing :as routing]))

(def ui-markdown (interop/react-factory ReactMarkdown))

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
                   :remarkPlugins [remarkGfm]}))
    (dom/div "loading")))

(defsc ListPage [_this {:page/keys [id name] :as props}]
  {:query [:page/id
           :page/name]
   :ident (fn [] [:page/id (:page/id props)])}
  (dom/button {:onClick #(routing/route-to! (dr/path-to Page id))} name))

(def ui-list-page (comp/factory ListPage {:keyfn :page/id}))
