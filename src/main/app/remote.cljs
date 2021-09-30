(ns app.remote
  (:require [com.fulcrologic.fulcro.networking.mock-server-remote :refer [mock-http-server]]
            [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]))

(defn new-parser [my-resolvers]
  (p/parallel-parser
   {::p/env     {::p/reader [p/map-reader
                             pc/parallel-reader
                             pc/open-ident-reader]}
    ::p/mutate  pc/mutate-async
    ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers})
                 p/error-handler-plugin
                 p/request-cache-plugin
                 (p/post-process-parser-plugin p/elide-not-found)]}))

(defn local-pathom
  ([resolvers env]
   (let [parser    (new-parser resolvers)
         transmit! (:transmit! (mock-http-server {:parser (fn [req] (parser env req))}))]
     {:transmit! (fn [this send-node]
                   (transmit! this send-node))}))
  ([resolvers]
   (local-pathom resolvers {})))
