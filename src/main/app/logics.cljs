(ns app.logics)

(defn assoc-if-exists
  [value k v]
  (when value
    (assoc value k v)))
