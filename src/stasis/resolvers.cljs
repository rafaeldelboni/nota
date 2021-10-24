(ns stasis.resolvers
  (:require [com.wsscode.pathom.connect :as pc]
            [stasis.adapters :as adapters]
            [stasis.logics :as logics]))

(pc/defresolver page-body-resolver
  [{:keys [config fetch-text-fn]} {:page/keys [path]}]
  {::pc/input  #{:page/path}
   ::pc/output [:page/body]}
  (fetch-text-fn :page/body path config))

(pc/defresolver post-body-resolver
  [{:keys [config fetch-text-fn]} {:post/keys [path]}]
  {::pc/input  #{:post/path}
   ::pc/output [:post/body]}
  (fetch-text-fn :post/body path config))

(pc/defresolver page-resolver [{:keys [database-fn]} {:page/keys [id]}]
  {::pc/input  #{:page/id}
   ::pc/output [:page/id
                :page/name
                :page/path]}
  (-> :pages
      database-fn
      (get id)
      (logics/assoc-if-exists :page/id id)))

(pc/defresolver author-resolver [{:keys [database-fn]} {:author/keys [id]}]
  {::pc/input  #{:author/id}
   ::pc/output [:author/id
                :author/name
                :author/email]}
  (-> :authors
      database-fn
      (get id)
      (logics/assoc-if-exists :author/id id)))

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
                             (adapters/hashmap->map-list-id :post/id))})


(def alias-list-posts-tag-id (pc/alias-resolver :list-posts-tag/id :tag/id))

(pc/defresolver post-resolver [{:keys [database-fn]} {:post/keys [id]}]
  {::pc/input  #{:post/id}
   ::pc/output [:post/id
                :post/name
                :post/path
                :post/description
                :post/tags
                :author/id]}
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
                   (adapters/hashmap->map-list-id :page/id))})

(pc/defresolver list-posts-resolver [{:keys [database-fn]} _]
  {::pc/output [{:list-posts [:post/timestamp
                              :post/path
                              :post/name
                              :post/description]}]}
  {:list-posts (-> :posts
                   database-fn
                   (adapters/hashmap->map-list-id :post/id))})

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
                         (as-> posts (sort-by :post/timestamp posts))
                         (logics/pagination page page-size))}))

(def resolvers [page-body-resolver
                post-body-resolver
                page-resolver
                author-resolver
                tag-resolver
                list-posts-tag-resolver
                alias-list-posts-tag-id
                post-resolver
                list-pages-resolver
                list-posts-resolver
                paginate-posts])
