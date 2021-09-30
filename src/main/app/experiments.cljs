(ns app.experiments 
  (:require [cljs.reader :as reader]
            [shadow.resource :as rc]))

(defn make-xhrio [] (XhrIo.))
(defn xhrio-dispose [^js xhrio] (.dispose xhrio))
(defn xhrio-enable-progress-events [^js xhrio] (.setProgressEventsEnabled xhrio true))
(defn xhrio-abort [^js xhrio] (.abort xhrio))
(defn xhrio-send [^js xhrio url verb body headers] (.send xhrio url verb body (some-> headers clj->js)))

(def teste (reader/read-string (rc/inline "./db.edn")))

(-> (js/fetch "gfm-syntax.md")
    (.then #(.text %))
    (.then println))
