(ns stasis.application.build
  (:require [aero.core :as aero]
            [stasis.application.env :as env]
            [clojure.java.io :as io]))

(defn read-env [release-flag]
  (-> (io/resource "config.edn")
      (aero/read-config {:profile release-flag})
      (assoc :release-flag release-flag)))

(defn load-env
  {:shadow.build/stages #{:compile-prepare}}
  [{:shadow.build/keys [mode] :as build-state}]
  (let [app-env (read-env mode)]
    (alter-var-root #'env/config (constantly app-env))
    (-> build-state
        (assoc-in [:compiler-options :external-config ::env] app-env))))
