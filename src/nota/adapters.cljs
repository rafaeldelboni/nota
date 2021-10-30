(ns nota.adapters
  (:require [clojure.string :as string]))

(defn vec->map-list-id
  [vector id-key]
  (mapv (fn [value] {id-key value}) vector))

(defn assoc-vec-as-map-list-id
  [value vec-key id-key]
  (assoc value vec-key (vec->map-list-id (vec-key value) id-key)))

(defn hashmap->map-list-id
  [hashset id-key]
  (map (fn [[key value]]
         (assoc value id-key key))
       hashset))

(defn- month->string
  [month]
  (case month
    1  {"Jan" "January"}
    2  {"Feb" "February"}
    3  {"Mar" "March"}
    4  {"Apr" "April"}
    5  {"May" "May"}
    6  {"Jun" "June"}
    7  {"Jul" "July"}
    8  {"Aug" "August"}
    9  {"Sep" "September"}
    10 {"Oct" "October"}
    11 {"Nov" "November"}
    12 {"Dec" "December"}
    {"" ""}))

(defn- weekday->string
  [weekday]
  (case weekday
    0 {"Mon" "Monday"}
    1 {"Tue" "Tuesday"}
    2 {"Wed" "Wednesday"}
    3 {"Thu" "Thursday"}
    4 {"Fri" "Friday"}
    5 {"Sat" "Saturday"}
    6 {"Sun" "Sunday"}
    {"" ""}))

(defn- num->zeropad-string
  [num]
  (->> num
       (str "0")
       (take-last 2)
       (apply str)))

(defn timestamp->utc-string
  [timestamp format-string]
  (let [datetime (js/Date. timestamp)]
    (-> format-string
        (string/replace "yyyy" (.getUTCFullYear datetime))
        (string/replace "mmmm" (-> datetime .getUTCMonth inc month->string vals first))
        (string/replace "mmm" (-> datetime .getUTCMonth inc month->string keys first))
        (string/replace "mm" (-> datetime .getUTCMonth inc num->zeropad-string))
        (string/replace "hh" (-> datetime .getUTCHours num->zeropad-string))
        (string/replace "MM" (-> datetime .getUTCMinutes num->zeropad-string))
        (string/replace "ss" (-> datetime .getUTCSeconds num->zeropad-string))
        (string/replace "dddd" (-> datetime .getUTCDay weekday->string vals first))
        (string/replace "ddd" (-> datetime .getUTCDay weekday->string keys first))
        (string/replace "dd" (-> datetime .getUTCDate num->zeropad-string)))))
