(ns nota.tests
  (:require [nota.main :as main]
            [clojure.test :as t :refer [deftest is]]))

; logic tests
(deftest create-slug-test
  (is (= "rebuildcast-8-ferramentas-windows-mac-linux" (main/create-slug "RebuildCast #8 - Ferramentas [Windows, Mac, Linux]")))
  (is (= "rebuildcast-15-net-para-devs-nao-net" (main/create-slug "RebuildCast #15 - .NET para Devs não .NET")))
  (is (= "a-b-a-1a-" (main/create-slug "á B {} -a 1!~ã "))))

; adapters tests
(deftest args-new-post-test
  (is (= {:post/name "A Link To The Past"
          :post/description "The first and only Zelda for the Super Nintendo Entertainment System."
          :post/timestamp #inst "1991-11-21T00:00:00.000-00:00"
          :post/path "games"
          :post/tags #{"games" "zelda" "snes"}}
         (main/args->new-post {:new-name "A Link To The Past"
                               :desc "The first and only Zelda for the Super Nintendo Entertainment System."
                               :tags "snes zelda games"}
                              ["res" "pub" "games"]
                              #inst "1991-11-21"))))

(deftest args-new-page-test
  (is (= {:page/name "A Link To The Past"
          :page/path "games"}
         (main/args->new-page {:new-name "A Link To The Past"}
                              ["res" "pub" "games"]))))

(deftest args-new-tag-test
  (is (= {:tag/name "A Link To The Past"}
         (main/args->new-tag {:new-name "A Link To The Past"}))))

(deftest dialog-text-test
  (is (= "New Thing:\n{:some \"thing\"}\n\nAre you sure?: "
         (main/->dialog-text "New Thing" {:some "thing"} "Are you sure?"))))

(deftest md-file-test
  (is (= "# Thing Name\n\nThing Description \n"
         (main/->md-file "Thing Name" "Thing Description"))))

; files tests
(deftest get-file
  (is (= "(ns nota.main\n "
         (->> (main/get-file ["scripts" "nota" "main.clj"])
              slurp
              (take 15)
              (apply str)))))
