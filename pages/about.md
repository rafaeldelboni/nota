# About Radical

About radical description

# GFM

## Autolink literals

www.example.com, https://example.com, and contact@example.com.

## Strikethrough

~one~ or ~~two~~ tildes.

## Table

| a | b  |  c |  d  |
| - | :- | -: | :-: |

## Tasklist

* [ ] to do
* [x] done

```clojure
  (defsc Page [_this {:ui/keys      [modified?]
                      :page/keys [id path body]}]
    {:query           [:ui/modified?
                       :page/id
                       :page/path
                       :page/body]
     :ident           :page/id
     :route-segment   [:page/id]
     :route-cancelled (fn [{:page/keys [id]}]
                        (js/console.log "Routing cancelled to user " id))
     :will-leave      (fn [_this {:ui/keys [modified?]}]
                        (when modified?
                          (js/alert "You cannot navigate until the user is not modified!"))
                        (not modified?))
     :will-enter      (fn [app {:page/keys [id] :as route-params}]
                        (js/console.log "Will enter user with route params " route-params)
                        (dr/route-deferred [:page/id id]
                                           #(df/load app [:page/id id] Page
                                                     {:post-mutation `dr/target-ready
                                                      :post-mutation-params
                                                      {:target [:page/id id]}})))}
    (if body
      (dom/div
       (dom/h2 (str "Id " id))
       (dom/h2 (str "Path " path))
       (ui-markdown {:children body
                     :remarkPlugins [remark-gfm/default]}))
      (dom/div "404")))
```
