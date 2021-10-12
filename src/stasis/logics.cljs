(ns stasis.logics)

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
