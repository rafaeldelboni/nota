(ns app.ui
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            ["react-markdown" :as react-markdown]
            ["remark-gfm" :as remark-gfm]))

(def ui-markdown (interop/react-factory react-markdown/default))

(defsc Author [_this {:author/keys [name email] :as props}]
  {:query [:author/name
           :author/email]
   :ident (fn [] [:author/email (:author/email props)])}
  (dom/h2 (str name " (email: " email ")")))

(def ui-author (comp/factory Author {:keyfn :author/id}))

(defsc Page [_this {:page/keys [id slug path body] :as props}]
  {:query [:page/id
           :page/slug
           :page/path
           :page/body]
   :ident (fn [] [:page/id (:page/id props)])}
  (dom/div
   (dom/h2 (str id " - " slug " - " path))
   (when body
     (ui-markdown {:children body
                   :remarkPlugins [remark-gfm/default]}))))

(def ui-page (comp/factory Page {:keyfn :page/id}))

(defsc Root [_this {:keys [author list-pages]}]
  {:query  [{:author (comp/get-query Author)}
            {:list-pages (comp/get-query Page)}]}
  (dom/div
   (dom/h1 "--> AUTHOR")
   (when author
     (ui-author author))
   (dom/h1 "--> PAGES")
   (when list-pages
     (map ui-page list-pages))))
