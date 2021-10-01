(ns app.resolvers
  (:require [cljs.reader :as reader]
            [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
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
   ::pc/output [:page/id :page/slug :page/path]}
  (get-in database [:pages id]))

(pc/defresolver list-pages-resolver [_ _]
  {::pc/output [{:list-pages [:page/id]}]}
  {:list-pages (map (fn [id] {:page/id id}) (:list-pages database))})

(def resolvers [author-resolver page-body-resolver page-resolver list-pages-resolver ])
