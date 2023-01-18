(ns nota.adapters-test
  (:require
   [clojure.test :refer [deftest is]]
   [nota.adapters :as adapters]
   [fulcro-spec.core :refer [behavior assertions]]))

(declare =>)

(deftest vec->map-list-id
  (behavior "should adapt vec into a map list with key id"
            (assertions
             (adapters/vec->map-list-id [:a :b :c] :ns/id) => [{:ns/id :a} {:ns/id :b} {:ns/id :c}]
             (adapters/vec->map-list-id [1 2] :a) => [{:a 1} {:a 2}])))

(deftest assoc-vec-as-map-list-id
  (behavior "should assoc adapted vec into a map list with key id on value"
            (assertions
             (adapters/assoc-vec-as-map-list-id {:meta "data" :pages [:a :b :c]} :pages :page/id) =>
             {:meta "data"
              :pages [{:page/id :a}
                      {:page/id :b}
                      {:page/id :c}]})))

(deftest hashset->map-list-id
  (behavior "should adapt hashset into a map list with key id"
            (assertions
             (adapters/hashmap->map-list-id {"page-1" {:page/number 1}
                                             "page-2" {:page/number 2}
                                             "page-3" {:page/number 3}}
                                            :page/id) =>
             [{:page/id "page-1" :page/number 1}
              {:page/id "page-2" :page/number 2}
              {:page/id "page-3" :page/number 3}])))

(deftest timestamp->utc-string
  (is (= (adapters/timestamp->utc-string 1634428800000 "ddd") "Mon"))
  (is (= (adapters/timestamp->utc-string 1633910400000 "ddd") "Tue"))
  (is (= (adapters/timestamp->utc-string 1633996800000 "ddd") "Wed"))
  (is (= (adapters/timestamp->utc-string 1634083200000 "ddd") "Thu"))
  (is (= (adapters/timestamp->utc-string 1634169600000 "ddd") "Fri"))
  (is (= (adapters/timestamp->utc-string 1634256000000 "ddd") "Sat"))
  (is (= (adapters/timestamp->utc-string 1634342400000 "ddd") "Sun"))

  (is (= (adapters/timestamp->utc-string 1634428800000 "dddd") "Monday"))
  (is (= (adapters/timestamp->utc-string 1633910400000 "dddd") "Tuesday"))
  (is (= (adapters/timestamp->utc-string 1633996800000 "dddd") "Wednesday"))
  (is (= (adapters/timestamp->utc-string 1634083200000 "dddd") "Thursday"))
  (is (= (adapters/timestamp->utc-string 1634169600000 "dddd") "Friday"))
  (is (= (adapters/timestamp->utc-string 1634256000000 "dddd") "Saturday"))
  (is (= (adapters/timestamp->utc-string 1634342400000 "dddd") "Sunday"))

  (is (= (adapters/timestamp->utc-string 1609491661000 "mmmm") "January"))
  (is (= (adapters/timestamp->utc-string 1612170061000 "mmmm") "February"))
  (is (= (adapters/timestamp->utc-string 1614589261000 "mmmm") "March"))
  (is (= (adapters/timestamp->utc-string 1617267661000 "mmmm") "April"))
  (is (= (adapters/timestamp->utc-string 1619859661000 "mmmm") "May"))
  (is (= (adapters/timestamp->utc-string 1622538061000 "mmmm") "June"))
  (is (= (adapters/timestamp->utc-string 1625130061000 "mmmm") "July"))
  (is (= (adapters/timestamp->utc-string 1627808461000 "mmmm") "August"))
  (is (= (adapters/timestamp->utc-string 1630486861000 "mmmm") "September"))
  (is (= (adapters/timestamp->utc-string 1633078861000 "mmmm") "October"))
  (is (= (adapters/timestamp->utc-string 1635757261000 "mmmm") "November"))
  (is (= (adapters/timestamp->utc-string 1638349261000 "mmmm") "December"))

  (is (= (adapters/timestamp->utc-string 1609491661000 "mmm") "Jan"))
  (is (= (adapters/timestamp->utc-string 1612170061000 "mmm") "Feb"))
  (is (= (adapters/timestamp->utc-string 1614589261000 "mmm") "Mar"))
  (is (= (adapters/timestamp->utc-string 1617267661000 "mmm") "Apr"))
  (is (= (adapters/timestamp->utc-string 1619859661000 "mmm") "May"))
  (is (= (adapters/timestamp->utc-string 1622538061000 "mmm") "Jun"))
  (is (= (adapters/timestamp->utc-string 1625130061000 "mmm") "Jul"))
  (is (= (adapters/timestamp->utc-string 1627808461000 "mmm") "Aug"))
  (is (= (adapters/timestamp->utc-string 1630486861000 "mmm") "Sep"))
  (is (= (adapters/timestamp->utc-string 1633078861000 "mmm") "Oct"))
  (is (= (adapters/timestamp->utc-string 1635757261000 "mmm") "Nov"))
  (is (= (adapters/timestamp->utc-string 1638349261000 "mmm") "Dec"))

  (is (= (adapters/timestamp->utc-string 1634050961747 "dd/mm/yyyy hh:MM:ss") "12/10/2021 15:02:41"))
  (is (= (adapters/timestamp->utc-string 1634050961747 "yyyy-mm-ddThh:MM:ss") "2021-10-12T15:02:41"))
  (is (= (adapters/timestamp->utc-string 978336061000 "dd/mm/yyyy hh:MM:ss") "01/01/2001 08:01:01"))
  (is (= (adapters/timestamp->utc-string 978336061000 "yyyy-mm-ddThh:MM:ss") "2001-01-01T08:01:01"))
  (is (= (adapters/timestamp->utc-string 978336061000 "mmmm, mmm, dddd, ddd, yyyy-mm-ddThh:MM:ss") "January, Jan, Tuesday, Tue, 2001-01-01T08:01:01")))

(deftest location-pathname->path-prefix-test
  (is (= (adapters/location-pathname->path-prefix "/") ""))
  (is (= (adapters/location-pathname->path-prefix "/aa") "aa"))
  (is (= (adapters/location-pathname->path-prefix "/aa/") "aa"))
  (is (= (adapters/location-pathname->path-prefix "/aa/bb") "aa/bb"))
  (is (= (adapters/location-pathname->path-prefix "/aa/bb/") "aa/bb")))
