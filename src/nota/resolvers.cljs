(ns nota.resolvers
  (:require [com.wsscode.pathom.connect :as pc]
            [nota.adapters :as adapters]
            [nota.logics :as logics]))

(pc/defresolver page-body-resolver
  [{:keys [fetch-text-fn]} {:page/keys [path]}]
  {::pc/input  #{:page/path}
   ::pc/output [:page/body]}
  (fetch-text-fn :page/body path))

(pc/defresolver post-body-resolver
  [{:keys [fetch-text-fn]} {:post/keys [path]}]
  {::pc/input  #{:post/path}
   ::pc/output [:post/body]}
  (fetch-text-fn :post/body path))

(pc/defresolver page-resolver [{:keys [database-fn]} {:page/keys [id]}]
  {::pc/input  #{:page/id}
   ::pc/output [:page/id
                :page/name
                :page/path]}
  (-> :pages
      database-fn
      (get id)
      (logics/assoc-if-exists :page/id id)))

(pc/defresolver tag-resolver [{:keys [database-fn]} {:tag/keys [id]}]
  {::pc/input  #{:tag/id}
   ::pc/output [:tag/id
                :tag/name]}
  (-> :tags
      database-fn
      (logics/get-tag-override id)
      (logics/assoc-if-exists :tag/id id)))

(pc/defresolver list-posts-tag-resolver
  [{:keys [database-fn]} {:list-posts-tag/keys [id]}]
  {::pc/input  #{:list-posts-tag/id}
   ::pc/output [{:list-posts-tag/posts [:post/timestamp
                                        :post/path
                                        :post/name
                                        :post/description]}]}
  {:list-posts-tag/id id
   :list-posts-tag/posts (-> :posts
                             database-fn
                             (logics/filter-by-tag :post/tags id)
                             (adapters/hashmap->map-list-id :post/id)
                             (logics/order-by-desc :post/timestamp))})


(def alias-list-posts-tag-id (pc/alias-resolver :list-posts-tag/id :tag/id))

(pc/defresolver post-resolver [{:keys [database-fn]} {:post/keys [id]}]
  {::pc/input  #{:post/id}
   ::pc/output [:post/id
                :post/name
                :post/path
                :post/description
                :post/tags
                :post/timestamp]}
  (-> :posts
      database-fn
      (get id)
      (logics/assoc-if-exists :post/id id)
      (adapters/assoc-vec-as-map-list-id :post/tags :tag/id)))

(pc/defresolver list-pages-resolver [{:keys [database-fn]} _]
  {::pc/output [{:list-pages [:page/id
                              :page/name
                              :page/path]}]}
  {:list-pages (-> :pages
                   database-fn
                   (logics/filter-by (comp not :page/hidden))
                   (adapters/hashmap->map-list-id :page/id))})

(pc/defresolver list-posts-resolver [{:keys [database-fn]} _]
  {::pc/output [{:list-posts [:post/timestamp
                              :post/path
                              :post/name
                              :post/description]}]}
  {:list-posts (-> :posts
                   database-fn
                   (adapters/hashmap->map-list-id :post/id)
                   (logics/order-by-desc :post/timestamp))})

(pc/defresolver paginate-posts [{:keys [ast database-fn]} _]
  {::pc/output [{:paginate/posts [:post/id
                                  :post/timestamp
                                  :post/path
                                  :post/name
                                  :post/description]}]}
  (let [{:keys [page page-size]
         :or {page 0 page-size 10}} (:params ast)]
    {:paginate/posts (-> :posts
                         database-fn
                         (adapters/hashmap->map-list-id :post/id)
                         (logics/order-by-desc :post/timestamp)
                         (logics/pagination page page-size))}))

(def resolvers [page-body-resolver
                post-body-resolver
                page-resolver
                tag-resolver
                list-posts-tag-resolver
                alias-list-posts-tag-id
                post-resolver
                list-pages-resolver
                list-posts-resolver
                paginate-posts])
