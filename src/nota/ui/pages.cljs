(ns nota.ui.pages
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [nota.routing :as routing]
            [nota.ui.markdown :as markdown]))

(defsc Page [_this {:page/keys [body]}]
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
     (markdown/render {:body body}))
    (dom/div "loading")))

(defsc ListPage [_this {:page/keys [id name] :as props}]
  {:query [:page/id
           :page/name]
   :ident (fn [] [:page/id (:page/id props)])}
  (dom/button {:onClick #(routing/route-to! (dr/path-to Page id))} name))

(def ui-list-page (comp/factory ListPage {:keyfn :page/id}))
