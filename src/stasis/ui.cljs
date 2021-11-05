(ns stasis.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
            [com.fulcrologic.fulcro.react.hooks :as hooks]
            [stasis.ui.pages :as ui.pages]
            [stasis.ui.posts :as ui.posts]
            [stasis.ui.posts.pagination :as ui.posts.pagination]
            [stasis.routing :as routing]))

(defrouter TopRouter [_this {:keys [current-state]}]
  {:router-targets [ui.pages/Page
                    ui.posts/Post
                    ui.posts/ListPostByTag
                    ui.posts.pagination/PaginatedPosts]}
  (case current-state
    (nil :pending) (dom/div "Loading...")
    :failed (dom/div "Loading seems to have failed. Try another route.")
    (dom/div "Unknown route")))

(def ui-top-router (comp/factory TopRouter))

(defn toggleTheme [theme]
  (set! (.. js/document -documentElement -className) theme))

(defsc Header [_this {:keys [list-pages]}]
  {:use-hooks? true}
  (let [[theme change-theme] (hooks/use-state "dark")]
    (dom/header
     (dom/nav {:class "nota-nav"}
      (map ui.pages/ui-list-page list-pages)
      (dom/button {:onClick #(routing/route-to! (dr/path-to ui.posts.pagination/PaginatedPosts "list"))
                   :class "nota-btn"}
                  "Blog")
      (dom/button {:onClick #(js/window.open "https://github.com/rafaeldelboni/nota" "_blank")
                   :class "nota-btn nota-btn--source"}
                  "Source")
      (dom/button {:class "nota-btn nota-btn--theme"
                   :onClick #(do
                              (change-theme (if (= theme "dark") "light" "dark")) 
                              (toggleTheme (if (= theme "dark") "light" "dark")))} 
                   (if (= theme "dark") "🌞" "🌚"))))))

(def header (comp/factory Header))

(defsc Footer [_this _]
  {}
  (dom/footer
    (dom/div
      (dom/hr)
      (dom/span "© 2021 built using ")
      (dom/a {:href "https://github.com/rafaeldelboni/nota"} "nota")
      (dom/span " with ❤ by ")
      (dom/a {:href "https://github.com/rafaeldelboni"} "@rafaeldelboni"))))

(def footer (comp/factory Footer))

(defsc Root [_this {:keys [list-pages]
                    :root/keys [router]}]
  {:query         [{:list-pages (comp/get-query ui.pages/ListPage)}
                   {:root/router (comp/get-query TopRouter)}]
   :initial-state {:root/router {}}}
  (dom/div :.container
           (header {:list-pages list-pages})
           (ui-top-router router)
           (footer)))
