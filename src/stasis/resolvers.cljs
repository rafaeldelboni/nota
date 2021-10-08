(ns stasis.resolvers
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as str]
            [com.wsscode.pathom.connect :as pc]
            [stasis.adapters :as adapters]
            [stasis.application.data :as app-data]
            [stasis.logics :as logics]))

(defn download-file-path!
  [id-key path]
  (async/go
    (let [path-prefix (:path-prefix app-data/config)
          result (<p! (-> (if-not (str/blank? path-prefix)
                            (str "/" path-prefix "/" path)
                            (str "/" path))
                          js/fetch
                          (.then #(.text %))))]
      {id-key result})))

(pc/defresolver page-body-resolver [_env {:page/keys [path]}]
  {::pc/input  #{:page/path}
   ::pc/output [:page/body]}
  (download-file-path! :page/body path))

(pc/defresolver post-body-resolver [_env {:post/keys [path]}]
  {::pc/input  #{:post/path}
   ::pc/output [:post/body]}
  (download-file-path! :post/body path))

(pc/defresolver page-resolver [_env {:page/keys [id]}]
  {::pc/input  #{:page/id}
   ::pc/output [:page/id
                :page/name
                :page/path]}
  (-> app-data/data
      :pages
      (get id)
      (logics/assoc-if-exists :page/id id)))

(pc/defresolver author-resolver [_env {:author/keys [id]}]
  {::pc/input  #{:author/id}
   ::pc/output [:author/id
                :author/name
                :author/email]}
  (-> app-data/data
      :authors
      (get id)
      (logics/assoc-if-exists :author/id id)))

(pc/defresolver tag-resolver [_env {:tag/keys [id]}]
  {::pc/input  #{:tag/id}
   ::pc/output [:tag/id
                :tag/name]}
  (-> app-data/data
      :tags
      (get id)
      (logics/assoc-if-exists :tag/id id)))

(pc/defresolver post-resolver [_env {:post/keys [id]}]
  {::pc/input  #{:post/id}
   ::pc/output [:post/id
                :post/name
                :post/path
                :post/title
                :post/description
                :post/tags
                :author/id]}
  (-> app-data/data
      :posts
      (get id)
      (logics/assoc-if-exists :post/id id)
      (adapters/assoc-vec-as-map-list-id :post/tags :tag/id)))

(pc/defresolver list-pages-resolver [_ _]
  {::pc/output [{:list-pages [:page/id
                              :page/name
                              :page/path]}]}
  {:list-pages (-> app-data/data
                   :pages
                   (adapters/hashmap->map-list-id :page/id))})

(pc/defresolver list-posts-resolver [_ _]
  {::pc/output [{:list-posts [:post/timestamp
                              :post/path
                              :post/title
                              :post/description]}]}
  {:list-posts (-> app-data/data
                   :posts
                   (adapters/hashmap->map-list-id :post/id))})

(pc/defresolver paginate-posts [env _]
  {::pc/output [{:paginate/posts [:post/id
                                  :post/timestamp
                                  :post/path
                                  :post/title
                                  :post/description]}]}
  (let [{:keys [page page-size]
         :or {page 0 page-size 10}} (-> env :ast :params)]
    {:paginate/posts (-> app-data/data
                         :posts
                         (adapters/hashmap->map-list-id :post/id)
                         (as-> posts (sort-by :post/timestamp posts))
                         (logics/pagination page page-size))}))

(def resolvers [page-body-resolver
                post-body-resolver
                page-resolver
                author-resolver
                tag-resolver
                post-resolver
                list-pages-resolver
                list-posts-resolver
                paginate-posts])
