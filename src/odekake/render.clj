(ns odekake.render
  (:require
   [clojure.string :refer [join]]
   [hiccup2.core :refer [html]]
   [odekake.util :as u]))

(defn- maybe-transition
  [v_or_vs]
  (if (vector? v_or_vs)
    (join "→" v_or_vs)
    v_or_vs))

(defn index
  [db area-ids]
  (html (map
         (fn [area-id]
           (let [area (area-id db)
                 {:keys [area-name last-updated]} area]
             [:a {:href (str (name area-id) ".html")}
              [:div area-name]
              [:div last-updated]]))
         area-ids)))

(defn area-name
  [area-name]
  (html [:div area-name]))

(defn site-list
  [sites]
  (let [site-ids (keys sites)]
    (html (map (fn [site-id]
                 (let [site (site-id sites)
                       {:keys [site-name site-name-ab version site-url]} site]
                   [:div
                    [:div (str site-name "(" site-name-ab ")")]
                    [:div version]
                    [:a {:href site-url} "サイトへ"]]))
               site-ids))))

(defn weather
  [sites]
  (let [site-ids (keys sites)] (html [:table
         [:thead
          [:tr
           [:td "日付"]
           [:td ""]
           (map (fn [n]
                  (let [today (u/today)]
                    [:td (-> today
                             (u/plus-days n)
                             u/date2str)])) (range 1 11))]]
         [:tbody
          (map-indexed (fn [ix site-id]
                         (let [site (site-id sites)]
                           [:tr
                            (when (zero? ix) [:td {:rowspan (count site-ids)} "天気"])
                            [:td (:site-name-ab site)]
                            (map (fn [n]
                                   (let [today (u/today)
                                         dk (-> today
                                                (u/plus-days n)
                                                u/date-key)]
                                     [:td (maybe-transition (get-in site [:forecasts dk :weather]))])) (range 1 11)) ]))
                       site-ids)
          (map-indexed (fn [ix site-id]
                         (let [site (site-id sites)]
                           [:tr
                            (when (zero? ix) [:td {:rowspan (count site-ids)} "最高気温"])
                            [:td (:site-name-ab site)]
                            (map (fn [n]
                                   (let [today (u/today)
                                         dk (-> today
                                                (u/plus-days n)
                                                u/date-key)]
                                     [:td (maybe-transition (get-in site [:forecasts dk :temp-high]))])) (range 1 11)) ]))
                       site-ids)
          (map-indexed (fn [ix site-id]
                         (let [site (site-id sites)]
                           [:tr
                            (when (zero? ix) [:td {:rowspan (count site-ids)} "最低気温"])
                            [:td (:site-name-ab site)]
                            (map (fn [n]
                                   (let [today (u/today)
                                         dk (-> today
                                                (u/plus-days n)
                                                u/date-key)]
                                     [:td (get-in site [:forecasts dk :temp-low])])) (range 1 11)) ]))
                       site-ids)
          (map-indexed (fn [ix site-id]
                         (let [site (site-id sites)]
                           [:tr
                            (when (zero? ix) [:td {:rowspan (count site-ids)} "降水確率"])
                            [:td (:site-name-ab site)]
                            (map (fn [n]
                                   (let [today (u/today)
                                         dk (-> today
                                                (u/plus-days n)
                                                u/date-key)]
                                     [:td (maybe-transition (get-in site [:forecasts dk :rain]))])) (range 1 11)) ]))
                       site-ids)
          ]]))
  )
