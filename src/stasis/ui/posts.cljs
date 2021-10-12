(ns stasis.ui.posts
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [stasis.routing :as routing]
            [stasis.ui.markdown :as markdown]
            [stasis.adapters :as adapters]))

(def pagination-page-max-size 10)

; TODO unit tests
(defn page-exists? [state-map page-number]
  (let [page-items (get-in state-map [:pagination/by-number page-number :pagination/posts])]
    (boolean (seq page-items))))

(defn clear-page
  "Clear the given page (and associated items) from the app database."
  [state-map page-number]
  (let [page        (get-in state-map [:pagination/by-number page-number])
        item-idents (:pagination/posts page)
        item-ids    (map second item-idents)]
    (as-> state-map s
      (update s :pagination/by-number dissoc page-number)
      (reduce (fn [acc id] (update acc :posts/by-id dissoc id)) s item-ids))))

; TODO unit tests
(defn gc-distant-pages
  "Clears loaded items from pages 5 or more steps away from the given page number."
  [state-map page-number]
  (reduce (fn [s n]
            (if (< 4 (Math/abs (- page-number n)))
              (clear-page s n)
              s)) state-map (keys (:pagination/by-number state-map))))

(defn init-page
  "An idempotent init function that just ensures enough of a page exists to make the UI work.
   Doesn't affect the items."
  [state-map page-number]
  (assoc-in state-map [:pagination/by-number page-number :pagination/number] page-number))

(defn set-current-page
  "Point the current list's current page to the correct page entity in the db (via ident)."
  [state-map page-number]
  (assoc-in state-map [:posts/by-id 1 :posts/current-page] [:pagination/by-number page-number]))

(declare ListPost)

(defn load-if-missing [{:keys [app state]} page-number]
  (when-not (page-exists? @state page-number)
    (let [page (dec page-number)]
      (df/load! app :paginate/posts ListPost {:params {:page page :page-size pagination-page-max-size}
                                              :marker :page
                                              :target [:pagination/by-number page-number :pagination/posts]}))))

(m/defmutation goto-page [{:keys [page-number]}]
  (action [{:keys [state] :as env}]
          (load-if-missing env page-number)
          (swap! state (fn [s]
                         (-> s
                             (init-page page-number)
                             (set-current-page page-number)
                             (gc-distant-pages page-number))))))

(defn initialize
  "To be used as started-callback. Load the first page."
  [{:keys [app]}]
  (comp/transact! app [(goto-page {:page-number 1})]))

(defsc Post [_this {:post/keys [id path body]}]
  {:query           [:ui/modified?
                     :post/id
                     :post/name
                     :post/path
                     :post/body]
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
     (markdown/render {:body body}))
    (dom/div "loading")))

(defsc ListPost [_this {:post/keys [id title timestamp description]}]
  {:query [:post/id
           :post/title
           :post/timestamp
           :post/description
           :ui/fetch-state]
   :ident [:posts/by-id :post/id]}
  (dom/div
   (dom/a {:onClick #(routing/route-to! (dr/path-to Post id))}
          (dom/h1 title))
   (dom/p (adapters/timestamp->utc-string timestamp "mmm dd, yyyy"))
   (dom/p description)))

(def ui-list-post (comp/factory ListPost {:keyfn :post/id}))

(defsc ListPaginationPosts [_this {:keys [pagination/number pagination/posts] :as props}]
  {:initial-state {:pagination/number 1 :pagination/posts []}
   :query         [:pagination/number {:pagination/posts (comp/get-query ListPost)}
                   [df/marker-table :pagination]]
   :ident         [:pagination/by-number :pagination/number]}
  (let [status (get props [df/marker-table :pagination])]
    (dom/div
     (dom/p "Page number " number)
     (if (df/loading? status)
       (dom/div "Loading...")
       (dom/div (mapv ui-list-post posts))))))

(def ui-list-pagination-posts (comp/factory ListPaginationPosts {:keyfn :pagination/number}))

(defsc LargeListPosts [this {:keys [posts/current-page]}]
  {:initial-state (fn [_] {:posts/current-page (comp/get-initial-state ListPaginationPosts {})})
   :query         [{:posts/current-page (comp/get-query ListPaginationPosts)}]
   :ident         (fn [] [:posts/by-id 1])}
  (let [{:keys [pagination/number]} current-page]
    (dom/div
     (dom/button {:disabled (= 1 number)
                  :onClick #(comp/transact! this [(goto-page {:page-number (dec number)})])}
                 "Prior Page")
     (dom/button {:disabled (not= pagination-page-max-size (count (:pagination/posts current-page)))
                  :onClick #(comp/transact! this [(goto-page {:page-number (inc number)})])}
                 "Next Page")
     (ui-list-pagination-posts current-page))))

(def ui-large-list-posts (comp/factory LargeListPosts))

(defsc PaginatedPosts [_this {:keys [pagination/list]}]
  {:initial-state (fn [_] {:pagination/list (comp/get-initial-state LargeListPosts {})})
   :query         [{:pagination/list (comp/get-query LargeListPosts)}]
   :ident         (fn [] [:component/id :list-posts])
   :route-segment ["posts" "list"]
   :will-enter    (fn [app _route-params]
                    (dr/route-deferred
                     [:component/id :list-posts]
                     #(do
                        (initialize {:app app})
                        (dr/target-ready! app [:component/id :list-posts]))))}
  (dom/div
   (ui-large-list-posts list)))
