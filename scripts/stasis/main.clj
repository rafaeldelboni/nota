(ns stasis.main
  (:require [babashka.fs :as fs]
            [borkdude.rewrite-edn :as r]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [clojure.string :as string]
            [docopt.core :as docopt]))

; logic
(def ^:private +slug-tr-map+
  (zipmap "ąàáäâãåæăćčĉęèéëêĝĥìíïîĵłľńňòóöőôõðøśșšŝťțŭùúüűûñÿýçżźž"
          "aaaaaaaaaccceeeeeghiiiijllnnoooooooossssttuuuuuunyyczzz"))

(defn create-slug
  "Transform text into a URL slug."
  [s]
  (some-> (string/lower-case s)
          (string/escape +slug-tr-map+)
          (string/replace #"[\P{ASCII}]+" "")
          (string/replace #"[^\w\s]+" "")
          (string/replace #"\s+" "-")))

; adapter
(defn args->new-post
  [{:keys [new-name desc slug tags]} resource-path now]
  {:post/slug (if string/blank? (create-slug new-name) slug)
   :post/name new-name
   :post/description desc
   :post/timestamp now
   :post/path (string/join "/" (drop 2 resource-path))
   :post/tags (-> tags (string/split #" ") (->> (map keyword)) set)})

(defn ->dialog-text
  [title data question]
  (if data
    (format "%s:\n%s\n%s: " title (with-out-str (pp/pprint data)) question)
    (format "%s:\n%s: " title question)))

(defn ->md-file [new-name desc]
  (format "# %s\n\n%s \n" new-name desc))

; file
(defn get-file [filepath]
  (->> filepath
       (apply fs/path)
       fs/file))

(defn create-or-file [file]
  (when-not (.exists file)
    (fs/create-file file))
  file)

(def write-file-lock (Object.))

(defn write-file! [content filepath append]
  (locking write-file-lock
    (-> filepath
        get-file
        create-or-file
        (spit content :append append))))

(defn delete-file! [filepath]
  (locking write-file-lock
    (-> filepath
        get-file
        fs/delete-if-exists)))

; db
(defn read-db
  ([]
   (read-db r/parse-string))
  ([parser]
   (-> ["src" "data.edn"]
       get-file
       slurp
       parser)))

(defn save-db! [db]
  (-> db
      str
      (write-file! ["src" "data.edn"] false)))

(defn db->upsert!
  [content type index db]
  (-> db
      (r/assoc-in [type index] content)
      save-db!))

(defn db->delete!
  [type index db]
  (-> db
      (r/update type #(r/dissoc % index))
      save-db!))

; actions
(defn new-post-action [{:keys [type new-name desc slug] :as post-args}]
  (let [id-slug (if string/blank? (create-slug new-name) slug)
        now (System/currentTimeMillis)
        resource-path ["resources" "public" (name type) (str id-slug ".md")]
        new-post (args->new-post post-args resource-path now)
        dialog-result (-> (System/console)
                          (.readLine (->dialog-text "New post"
                                                    new-post
                                                    "Create? (Y/n)")
                                     nil)
                          String.)]
    (if (= (string/lower-case dialog-result) "n")
      (println "Not saved!")
      (do (write-file! (->md-file new-name desc) resource-path true)
          (db->upsert! (dissoc new-post :post/slug) type id-slug (read-db))
          (println "Saved!")))))

(defn delete-post-action [id-slug]
  (if-let [delete-post (get-in (read-db edn/read-string) [:posts id-slug])]
    (let [resource-path ["resources" "public" "posts" (str id-slug ".md")]
          dialog-result (-> (System/console)
                            (.readLine (->dialog-text "Delete post"
                                                      delete-post
                                                      "Are you sure? (y/N)")
                                       nil)
                            String.)]
      (if-not (= (string/lower-case dialog-result) "y")
        (println "Not deleted!")
        (do (delete-file! resource-path)
            (db->delete! :posts id-slug (read-db))
            (println "Deleted!"))))
    (println "Post not found.")))

; inputs
(def new-post-doc "Stasis: New post
Usage:
  new:post <name> [Options]
  new:post -h | --help

Options:
  -h --help          Show help.
  -n --name <name>   Post name.
  -d --desc <desc>   Post short description. [Optional]
  -s --slug <slug>   Post slug override. [Optional]
  -t --tags <tags>   Post tags ids on double-quotes split by space (eg: \"gamedev raylib\"). [Optional]
")

(defn new-post
  [& _]
  (docopt/docopt
   new-post-doc
   *command-line-args*
   (fn [arg-map]
     (when (arg-map "--help")
       (println new-post-doc)
       (System/exit 0))
     (let [new-name (or (arg-map "<name>") (arg-map "--name"))
           desc     (arg-map "--desc")
           slug     (arg-map "--slug")
           tags     (arg-map "--tags")]
       (new-post-action {:new-name new-name
                         :desc desc
                         :slug slug
                         :tags tags
                         :type :posts})))))

(def del-post-doc "Stasis: New post
Usage:
  new:post <slug> [Options]
  new:post -h | --help

Options:
  -h --help          Show help.
  -s --slug <slug>   Post slug/id.
")

(defn del-post
  [& _]
  (docopt/docopt
   del-post-doc
   *command-line-args*
   (fn [arg-map]
     (when (arg-map "--help")
       (println del-post-doc)
       (System/exit 0))
     (let [id-slug (or (arg-map "<slug>") (arg-map "--slug"))]
       (delete-post-action id-slug)))))
