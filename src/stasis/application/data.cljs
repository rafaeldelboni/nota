(ns stasis.application.data
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [shadow.resource :as rc]))

(def data (reader/read-string (rc/inline "../../data.edn")))

(defn database-fn
  ([table]
   (table data))
  ([]
   data))

(defn fetch-text-fn
  [id-key path config]
  (async/go
    (let [path-prefix (:path-prefix config)
          result (<p! (-> (if-not (str/blank? path-prefix)
                            (str "/" path-prefix "/" path)
                            (str "/" path))
                          js/fetch
                          (.then #(.text %))))]
      {id-key result})))
