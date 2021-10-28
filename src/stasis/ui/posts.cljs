(ns stasis.ui.posts
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [stasis.adapters :as adapters]
            [stasis.routing :as routing]
            [stasis.ui.markdown :as ui.markdown]))

(declare ListPostByTag)

(defsc LinkTags [_this {:tag/keys [id] :as tag}]
  {:ident :tag/id
   :query [:tag/id
           :tag/name]}
  (dom/a {:onClick #(routing/route-to! (dr/path-to ListPostByTag id))}
         (dom/h5 (:tag/name tag))))

(def link-tags (comp/factory LinkTags {:keyfn :tag/id}))

(defsc Post [_this {:post/keys [id path body tags]}]
  {:query           [:ui/modified?
                     :post/id
                     :post/name
                     :post/path
                     :post/body
                     {:post/tags (comp/get-query LinkTags)}]
   :ident           :post/id
   :route-segment   ["post" :post/id]
   :will-enter      (fn [app {:post/keys [id]}]
                      (dr/route-deferred [:post/id id]
                                         #(df/load! app [:post/id id] Post
                                                    {:post-mutation `dr/target-ready
                                                     :post-mutation-params
                                                     {:target [:post/id id]}})))}
  (if body
    (dom/div
     (dom/h2 (str "Id " id))
     (dom/h2 (str "Path " path))
     (map link-tags tags)
     (ui.markdown/render {:body body}))
    (dom/div "loading")))

(defsc ListPost [_this {:post/keys [id name timestamp description]}]
  {:query [:post/id
           :post/name
           :post/timestamp
           :post/description
           :ui/fetch-state]
   :ident [:posts/by-id :post/id]}
  (dom/div
   (dom/a {:onClick #(routing/route-to! (dr/path-to Post id))}
          (dom/h1 name))
   (dom/p (adapters/timestamp->utc-string timestamp "mmm dd, yyyy"))
   (dom/p description)))

(def ui-list-post (comp/factory ListPost {:keyfn :post/id}))

(defsc ListPostByTag [_this {:list-posts-tag/keys [posts] :as props}]
  {:ident         :list-posts-tag/id
   :query         [:list-posts-tag/id
                   :tag/name
                   {:list-posts-tag/posts (comp/get-query ListPost)}]
   :route-segment ["tag" :list-posts-tag/id]
   :will-enter    (fn [app {:list-posts-tag/keys [id]}]
                    (dr/route-deferred [:list-posts-tag/id id]
                                       #(df/load! app [:list-posts-tag/id id] ListPostByTag
                                                  {:post-mutation `dr/target-ready
                                                   :post-mutation-params
                                                   {:target [:list-posts-tag/id id]}})))}
  (dom/div
   (dom/h1 (:tag/name props))
   (mapv ui-list-post posts)))
