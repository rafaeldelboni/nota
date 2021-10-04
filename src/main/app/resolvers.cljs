(ns app.resolvers
  (:require [app.adapters :as adapters]
            [app.logics :as logics]
            [app.routing :as routing]
            [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [com.wsscode.pathom.connect :as pc]
            [shadow.resource :as rc]))

(defn download-file-path!
  [id-key path]
  (async/go
    (let [result (<p! (-> (if (str/blank? routing/base-route)
                            (str "/" path)
                            (str "/" routing/base-route "/" path))
                          js/fetch
                          (.then #(.text %))))]
      {id-key result})))

(def database (reader/read-string (rc/inline "./db.edn")))

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
  (-> database
      :pages
      (get id)
      (logics/assoc-if-exists :page/id id)))

(pc/defresolver author-resolver [_env {:author/keys [id]}]
  {::pc/input  #{:author/id}
   ::pc/output [:author/id
                :author/name
                :author/email]}
  (-> database
      :authors
      (get id)
      (logics/assoc-if-exists :author/id id)))

(pc/defresolver tag-resolver [_env {:tag/keys [id]}]
  {::pc/input  #{:tag/id}
   ::pc/output [:tag/id
                :tag/name]}
  (-> database
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
  (-> database
      :posts
      (get id)
      (logics/assoc-if-exists :post/id id)
      (adapters/assoc-vec-as-map-list-id :post/tags :tag/id)))

(pc/defresolver list-pages-resolver [_ _]
  {::pc/output [{:list-pages [:page/id
                              :page/name
                              :page/path]}]}
  {:list-pages (-> database
                   :pages
                   (adapters/hashmap->map-list-id :page/id))})

(def resolvers [page-body-resolver
                post-body-resolver
                page-resolver
                author-resolver
                tag-resolver
                post-resolver
                list-pages-resolver])
