(ns nota.application.data
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [nota.routing :as routing]
            [shadow.resource :as rc]))

(def data (reader/read-string (rc/inline "../../data.edn")))

(defn database-fn
  ([table]
   (table data))
  ([]
   data))

(defn fetch-text-fn
  [id-key path]
  (async/go
    (let [path-prefix (routing/get-path-prefix)
          result (<p! (-> (if-not (str/blank? path-prefix)
                            (str "/" path-prefix "/" path)
                            (str "/" path))
                          js/fetch
                          (.then #(.text %))))]
      {id-key result})))
