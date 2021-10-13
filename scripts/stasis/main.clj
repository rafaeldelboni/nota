(ns stasis.main
  (:require [clojure.string :as string]))

(def ^:private +slug-tr-map+
  (zipmap "ąàáäâãåæăćčĉęèéëêĝĥìíïîĵłľńňòóöőôõðøśșšŝťțŭùúüűûñÿýçżźž"
          "aaaaaaaaaccceeeeeghiiiijllnnoooooooossssttuuuuuunyyczzz"))

(defn slug
  "Transform text into a URL slug."
  [s]
  (some-> (string/lower-case s)
          (string/escape +slug-tr-map+)
          (string/replace #"[\P{ASCII}]+" "")
          (string/replace #"[^\w\s]+" "")
          (string/replace #"\s+" "-")))

(defn new-post [& _args]
  (let [post-title (-> (System/console)
                       (.readLine "What is the post tittle? " nil)
                       String.)
        post-description (-> (System/console)
                             (.readLine "Describe it in one sentence: " nil)
                             String.)
        timestamp (System/currentTimeMillis)
        post-slug (slug post-title)]
    (println "Title: " post-title
             " Description: " post-description
             " Timestamp: " timestamp
             " Slug: " post-slug)))
