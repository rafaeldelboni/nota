# Faking the backend in clojurescript applications

Whenever I have to do any frontend work, I always like to decouple it from the backend.
It encourages a clean contract specification, allows running and developing
the UI without a backend, and enables many UI exploration options.  

What happens if the network is slow? If a text is too short or too long? If the data is corrupted?
All these scenarios are much easier to explore (and test) if the backend is faked.

My tool of choice is [msw](https://mswjs.io). Implemented on service workers, it allows
me to fake the backend without any code change or without having to spin up another application.

```clojure
(ns app.fake.browser
  (:require [app.utils.localstorage :as lc]
            [app.fake.foo :as fake.foo]
            ["msw" :refer (setupworker)]))

(def ^:private mock-key "mock-active?")
(def ^:private ^js/object worker (apply setupworker fake.foo/fakes))

(defn- start-worker! []
  (.start worker (clj->js {:onunhandledrequest "bypass"})))

(defn fake-start! []
  (->  (start-worker!)
       (.then #(lc/set-item! mock-key true))))

(defn fake-stop! []
  (.stop worker)
  (lc/remove-item! mock-key))

(defn fake-init! []
  (if (lc/get-item mock-key)
    (start-worker!)
    (js/promise.resolve)))
```
You can now start/stop your fake service from the repl. *fake-init!* will
make sure the fake activation option is kept across page refreshes.

With the help from a little helper namespace
```clojure
(ns app.fake.helper
  (:require ["msw" :rename {rest Rest}]))

(defn get
  [path handler]
  (.get Rest path handler))

(defn post
  [path handler]
  (.post Rest path handler))

(defn reply
  [& fns]
  (fn [_, res, ctx]
    (apply res (map #(% ctx) fns))))

(defn status
  [status-code]
  (fn [ctx] (.status ctx status-code)))

(defn defer
  [ms]
  (fn [ctx] (.delay ctx ms)))

(defn json
  [data]
  (fn [ctx] (.json ctx (clj->js data))))

```

We can now, idiomatically create fakes:

```clojure
(ns app.fake.foo
  (:require [app.fake.helper :as f :refer [reply json status defer]]))

(def fakes
  [(f/get  "/foo" (reply (json {:foo "bar"})))
   (f/post "/foo" (reply (status 201)))])
```

The behaviour of a fake can easily be changed composing the helpers:
```clojure
;; A post to /foo will return the json {"ok": true} after 2 seconds, with the status 201
(f/post "/foo" (reply 
                  (defer 2000)
                  (json {:ok true})
                  (status 201)))
```

That's all! As simple as it can be, you can easily fake your backend.
Check out the [docs](https://mswjs.io/docs/) for msw, it supports rest, graphql, session storage and much more.
You can fake sophiscated scenarios like login and get an experience close to your real backend application.
