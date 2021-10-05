(ns stasis.application.data 
  (:require [cljs.reader :as reader]
            [shadow.resource :as rc]))

(def data (reader/read-string (rc/inline "../../data.edn")))
(def config (reader/read-string (rc/inline "../../config.edn")))
