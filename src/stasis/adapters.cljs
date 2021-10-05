(ns stasis.adapters)

(defn vec->map-list-id
  [vector id-key]
  (mapv (fn [value] {id-key value}) vector))

(defn assoc-vec-as-map-list-id
  [value vec-key id-key]
  (assoc value vec-key (vec->map-list-id (vec-key value) id-key)))

(defn hashmap->map-list-id
  [hashset id-key]
  (map (fn [[key value]]
         (assoc value id-key key))
       hashset))
