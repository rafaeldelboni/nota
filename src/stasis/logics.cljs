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
