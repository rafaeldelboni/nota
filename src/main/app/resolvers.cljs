(ns app.resolvers
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.reader :as reader]
            [com.wsscode.pathom.connect :as pc]
            [shadow.resource :as rc]))

(def database (reader/read-string (rc/inline "./db.edn")))

(pc/defresolver author-resolver [_ _]
  {::pc/output [{:author [:author/id :author/name :author/email]}]}
  {:author (:author database)})

(pc/defresolver page-body-resolver [_env {:page/keys [path]}]
  {::pc/input  #{:page/path}
   ::pc/output [:page/body]}
  (async/go
    (let [result (<p! (-> (js/fetch path)
                          (.then #(.text %))))]
      {:page/body result})))

(pc/defresolver page-resolver [_env {:page/keys [id]}]
  {::pc/input  #{:page/id}
   ::pc/output [:page/id :page/name :page/path]}
  (when-let [page (get-in database [:pages id])]
    (assoc page :page/id id)))

(pc/defresolver list-pages-resolver [_ _]
  {::pc/output [{:list-pages [:page/id :page/name]}]}
  {:list-pages (->> database
                    :pages
                    (map (fn [[key {:page/keys [name]}]]
                           {:page/id key :page/name name})))})

(def resolvers [author-resolver page-body-resolver page-resolver list-pages-resolver])
