(ns nota.application.remote
  (:require [cljs.core.async :as async]
            [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
            [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [edn-query-language.core :as eql]))

(defn pseudo-remote-server
  "Create a remote that mocks a Fulcro remote server.

  :parser - A function `(fn [eql-query] async-channel)` that returns a core async channel with the result for the
  given eql-query."
  [{:keys [parser] :as options}]
  (merge options
    {:transmit! (fn transmit! [{:keys [_active-requests]} {:keys [::txn/ast ::txn/result-handler ::txn/update-handler] :as _send-node}]
                  (let [edn           (eql/ast->query ast)
                        ok-handler    (fn [result]
                                        (try
                                          (result-handler (select-keys result #{:transaction :status-code :body :status-text}))
                                          (catch :default e
                                            (js/console.error e "Result handler failed with an exception."))))
                        error-handler (fn [error-result]
                                        (try
                                          (result-handler (merge {:status-code 500} (select-keys error-result #{:transaction :status-code :body :status-text})))
                                          (catch :default e
                                            (js/console.error e "Error handler failed with an exception."))))]
                    (try
                      (async/go
                        (let [result (async/<! (parser edn))]
                          (ok-handler {:transaction edn :status-code 200 :body result})))
                      (catch :default _e
                        (error-handler {:transaction edn :status-code 500})))))
     :abort!    (fn abort! [_this _id])}))

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
         transmit! (:transmit! (pseudo-remote-server {:parser (fn [req] (parser env req))}))]
     {:transmit! (fn [this send-node]
                   (transmit! this send-node))}))
  ([resolvers]
   (local-pathom resolvers {})))
