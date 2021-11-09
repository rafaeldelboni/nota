(ns nota.hooks.index
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [hiccup.core :refer [html]]))

(defn read-edn! [path]
  (edn/read-string (slurp path)))

(defn output-dir [build-state]
  (get-in build-state [:shadow.build/config :output-dir]))

(defn asset-path [build-state]
  (get-in build-state [:shadow.build/config :asset-path]))

(defn get-manifest-path [build-state]
  (str (output-dir build-state) "/manifest.edn"))

(defn get-manifest! [build-state]
  (-> build-state
      get-manifest-path
      read-edn!))

(def entry-point-js-error-message
  "console.debug('shadow-cljs-hooks.index: ', 'no `:entry-point` provided.')")

(defn entry-point-js [{:keys [entry-point]}]
  (if entry-point
    (-> entry-point
        str
        (string/replace #"-" "_")
        (string/replace #"/" ".")
        (str "();"))
    entry-point-js-error-message))

(defn template [main-src
                {:keys [title links scripts lang app-mount]
                 :as options}]
  (html
   [:html {:lang lang}
    [:head
     [:title title]
     [:meta {:charset "utf-8"}]
     (for [{:keys [href rel htype]} links]
       [:link {:rel rel
               :type htype
               :href href}])]
    [:body
     [app-mount "Loading..."]
     (for [src scripts]
       [:script {:src src}])
     [:script {:src main-src}]
     [:script (entry-point-js options)]]]))

(defn conform-options [build-state options]
  (merge {:path      (string/replace (output-dir build-state)
                                     (re-pattern (asset-path build-state))
                                     "")
          :title     "Nota"
          :links     [{:href "./imp/favicon.png"
                       :rel "icon"}
                      {:href "https://fonts.googleapis.com"
                       :rel "preconnect"}
                      {:href "https://fonts.gstatic.com"
                       :rel "preconnect"}
                      {:href "https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@100;400;500&family=Poppins:wght@500;600&display=swap"
                       :rel "stylesheet"}
                      {:href "https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css"
                       :rel "stylesheet"}
                      {:href "./css/nota.min.css"
                       :rel "stylesheet"
                       :htype "text/css"}]
          :scripts   []
          :lang      "en"
          :app-mount :div#app}
         options))

(defn write-html! [path index-html]
  (spit (str path "/" "index.html")
        index-html))

(defn hook* [build-state options {:keys [get-manifest
                                         write-html]}]
  (let [{:keys [path] :as options} (conform-options build-state options)
        main-src (str (asset-path build-state)
                      "/"
                      (-> (get-manifest build-state)
                          first
                          :output-name))
        index-html (template main-src options)]
    (write-html path index-html)
    build-state))

(defn hook
  {:shadow.build/stage :flush}
  ([build-state]
   (hook build-state {}))
  ([build-state options]
   (hook* build-state options
          {:write-html   write-html!
           :get-manifest get-manifest!})))