(ns app.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]))

(defsc Person [this {:person/keys [id name age] :as props}]
  {:query [:person/id
           :person/name
           :person/age]
   :ident (fn [] [:person/id (:person/id props)])}
  (dom/li
   (dom/h5 (str name " (age: " age ")"))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:list/keys [id label people] :as props}]
  {:query [:list/id
           :list/label
           {:list/people (comp/get-query Person)}]
   :ident (fn [] [:list/id (:list/id props)])}
  (dom/div
   (dom/ul
    (map ui-person people))))

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:keys [friends enemies]}]
  {:query         [{:friends (comp/get-query PersonList)}
                   {:enemies (comp/get-query PersonList)}]
   :initial-state {}}
  (dom/div
   (dom/h3 "Friends")
   (when friends
     (ui-person-list friends))
   (dom/h3 "Enemies")
   (when enemies
     (ui-person-list enemies))))
