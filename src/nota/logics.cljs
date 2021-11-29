(ns nota.logics
  (:require [clojure.string :as string]))

(defn assoc-if-exists
  [value k v]
  (when value
    (assoc value k v)))

(defn pagination
  [coll current size]
  (let [itens-to-skip (* current size)]
    (->> coll
         (drop itens-to-skip)
         (take size))))

(defn filter-by-tag
  [list-to-filter tag-key tag-id]
  (->> list-to-filter
       (filter
        (fn [[_key val]]
          (contains? (tag-key val) tag-id)))
       (into {})))

(defn filter-by
  [list-to-filter by-fn]
  (->> list-to-filter
       (filter (fn [[_key val]]
                 (by-fn val)))
       (into {})))

(defn get-tag-override
  [tags id]
  (or (get tags id)
      {:tag/id id
       :tag/name (string/capitalize id)}))

(defn order-by-desc
  [list-to-order by-key]
  (-> (comp - by-key)
      (sort-by list-to-order)))
