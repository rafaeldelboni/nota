(ns stasis.main
  (:require [clojure.string :as string]
            [docopt.core :as docopt]))

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

(defn new-post-old [& _args]
  (let [post-title (-> (System/console)
                       (.readLine "What is the post tittle? " nil)
                       String.)
        post-description (-> (System/console)
                             (.readLine "Describe it in one sentence: " nil)
                             String.)
        timestamp (System/currentTimeMillis)
        post-slug (slug post-title)
        file-path (str "post/" post-slug ".md")]
    (println "Title: " post-title
             " Description: " post-description
             " Timestamp: " timestamp
             " Slug: " post-slug
             " Markdown:" file-path)))

(def usage "Stasis
Usage:
  bb <action> [options]
  bb <action> <id> [options]
  bb -h | --help

Options:
  -h --help          Show help.
  --action <action>  Action name: [init, new:post, delete:post, new:page, delete:page, new:tag, delete:tag]
  --id <id>          Id, if action is edit/delete.
  -n --name <name>   Post/Page/Tag name.
  -d --desc <desc>   Post short description.
  -s --slug <slug>   Post/Page slug override.
  -t --tags <tags>   Post tags ids on double-quotes [eg: \":gamedev :raylib\"]. Use new:tag/delete:tag to manage tags.
")

(defn new-post
  [& _]
  (docopt/docopt
    usage
    *command-line-args*
    (fn [arg-map]
      (when (arg-map "--help")
        (println usage)
        (System/exit 0))
      (let [action (or (arg-map "<action>") (arg-map "--action"))
            id (or (arg-map "<id>") (arg-map "--id"))
            name (arg-map "--name")
            desc (arg-map "--desc")
            slug (arg-map "--slug")
            tags (arg-map "--tags")]
        (println {:action action
                  :id     id
                  :name   name
                  :desc   desc
                  :slug   slug
                  :tags   tags})))))
