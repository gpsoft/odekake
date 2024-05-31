(ns odekake.render
  (:require
   [clojure.string :refer [join] :as str]
   [hiccup2.core :refer [html]]
   [odekake.util :as u]))

(defn- maybe-transition
  [v_or_vs]
  (if (vector? v_or_vs)
    (join "⇒" v_or_vs)
    v_or_vs))

(defn- iconic-weather
  [weather-str]
  (when weather-str
    (-> weather-str
        (str/replace #"(晴れ|晴)" "☀")
        (str/replace #"(曇り|曇)" "☁")
        (str/replace #"雨" "☔")
        (str/replace #"雪" "❄")
        (str/replace #"のち" "→")
        (str/replace #"(ときどき|時々)" "/"))))

(defn index
  [db area-ids]
  (html [:div.area-list
         (map
          (fn [area-id]
            (let [area (area-id db)
                  {:keys [area-name area-file-path last-updated]} area]
              [:a.area-link {:href (u/fname-from-path area-file-path)}
               ;;           ↑remove parent path from area-file-path,
               ;;             assuming it's placed in the same dir as index file.
               [:div.area-name area-name]
               [:div.area-timestamp last-updated]]))
          area-ids)]))

(defn area-name
  [area-name]
  (html [:h1 (str area-name "の天気予報まとめ")]))

(defn site-list
  [sites]
  (let [site-ids (keys sites)]
    (html [:div.site-list-section
           [:h2.site-list-header "サイト一覧"]
           [:div.site-list-body
            (map (fn [site-id]
                   (let [site (site-id sites)
                         {:keys [site-name site-name-ab version site-url]} site]
                     [:div.site-list-item
                      [:div.site-name (str site-name "(" site-name-ab ")")]
                      [:div.site-version version]
                      [:a.site-link {:href site-url
                                     :target "_blank"}
                       "サイトへ"]]))
                 site-ids)]])))

(defn weather
  [sites]
  (let [site-ids (keys sites)]
    (html [:div.weather-section
           [:h2.weather-header "天気予報"]
           [:table.weather-table
           [:thead
            [:tr.weather-date-row
             [:th {:colspan 2} "日付"]
             (map (fn [n]
                    (let [today (u/today)
                          date (-> today
                                   (u/plus-days n))]
                      [:td {:data-dow (u/day-of-week date)}
                       (u/date2str date)]))
                  (range 1 11))]]
           [:tbody
            (map-indexed (fn [ix site-id]
                           (let [site (site-id sites)]
                             [:tr.weather-row
                              (when (zero? ix) [:th.span-header {:rowspan (count site-ids)} "天気"])
                              [:th (:site-name-ab site)]
                              (map (fn [n]
                                     (let [today (u/today)
                                           dk (-> today
                                                  (u/plus-days n)
                                                  u/date-key)]
                                       [:td (iconic-weather (maybe-transition (get-in site [:forecasts dk :weather])))])) (range 1 11)) ]))
                         site-ids)
            (map-indexed (fn [ix site-id]
                           (let [site (site-id sites)]
                             [:tr.temp-high-row
                              (when (zero? ix) [:th.span-header {:rowspan (count site-ids)} "最高気温"])
                              [:th (:site-name-ab site)]
                              (map (fn [n]
                                     (let [today (u/today)
                                           dk (-> today
                                                  (u/plus-days n)
                                                  u/date-key)]
                                       [:td (maybe-transition (get-in site [:forecasts dk :temp-high]))])) (range 1 11)) ]))
                         site-ids)
            (map-indexed (fn [ix site-id]
                           (let [site (site-id sites)]
                             [:tr.temp-low-row
                              (when (zero? ix) [:th.span-header {:rowspan (count site-ids)} "最低気温"])
                              [:th (:site-name-ab site)]
                              (map (fn [n]
                                     (let [today (u/today)
                                           dk (-> today
                                                  (u/plus-days n)
                                                  u/date-key)]
                                       [:td (get-in site [:forecasts dk :temp-low])])) (range 1 11)) ]))
                         site-ids)
            (map-indexed (fn [ix site-id]
                           (let [site (site-id sites)]
                             [:tr.rain-row
                              (when (zero? ix) [:th.span-header {:rowspan (count site-ids)} "降水確率/降水量"])
                              [:th (:site-name-ab site)]
                              (map (fn [n]
                                     (let [today (u/today)
                                           dk (-> today
                                                  (u/plus-days n)
                                                  u/date-key)]
                                       [:td (maybe-transition (get-in site [:forecasts dk :rain]))])) (range 1 11)) ]))
                         site-ids)
            ]]]))
  )

(comment
 
 (html [:td "a" [:span "b"]])
 
 )
