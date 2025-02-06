(ns odekake.scrape
  (:require
   [net.cgrand.enlive-html :as en]
   [odekake.util :as u]))

(def ^:private wn-icons
  (u/read-edn! "wn_icons.edn"
               {100 "晴"
                101 "晴時々曇"
                102 "晴時々雨"
                103 "晴時々雪"
                110 "晴のち曇"
                111 "晴のち曇"
                112 "晴のち雨"
                114 "晴のち雨"
                200 "曇"
                201 "曇時々晴"
                202 "曇時々雨"
                203 "曇時々雨"
                ;;??? "曇時々雪"
                210 "曇のち晴"
                211 "曇のち晴"
                212 "曇のち雨"
                214 "曇のち雨"
                300 "雨"
                301 "雨時々晴"
                302 "雨時々曇"
                303 "雨時々雪"
                311 "雨のち晴"
                313 "雨のち曇"
                552 "晴時々曇" ;猛暑
                572 "曇時々晴" ;猛暑
                582 "曇のち晴" ;猛暑
                852 "雨時々曇" ;嵐
                862 "雨のち曇"
                }))

(defn- sel1
  [nodes selector]
  (first (en/select nodes selector)))

(defn- attr1
  [node attr]
  (first (en/attr-values node attr)))

(defn- wn-date
  [content]
  (let [day (-> content
                (sel1 [:.date :.day])
                en/text
                u/to-int)]
    (-> day
        u/infer-date
        u/date-key)))

(defn- lookup-wn-weather
  [icon-no]
  (get wn-icons icon-no (str icon-no)))

(defn- wn-weather
  [img]
  (-> img
      (attr1 :src)
      (->> (re-find #"\d+\.png$"))
      u/to-int
      lookup-wn-weather))

(defn- wn-content
  [content]
  (let [weather (-> content
                    (sel1 [:.weather :img]))
        high (-> content
                 (sel1 [:.high :p en/text-node])
                 en/text
                 u/to-int)
        low (-> content
                (sel1 [:.low :p en/text-node])
                en/text
                u/to-int)
        rain (-> content
                 (sel1 [:.rain :p en/text-node])
                 en/text
                 u/to-int)
        ]
    {:weather (wn-weather weather)
     :temp-high high
     :temp-low low
     :rain rain}))

(defn wn
  [snippet]
  (let [page (en/html-snippet snippet)
        version nil
        contents (-> page
                     (en/select [:.wxweek_content]))]
    [version
     (reduce (fn [forecasts content]
               (assoc forecasts (wn-date content) (wn-content content)))
             (sorted-map)
             contents)]))

(defn tenki-version
  [page]
  (-> page
      (sel1 [:h2 :time.date-time])
      en/text))

(defn tenki-date
  [content]
  (let [mmdd (-> content
                 (sel1 [:.days])
                 en/text)
        day (->> mmdd
                 (re-find #"(\d+)日")
                 last
                 u/to-int)]
    (-> day
        u/infer-date
        u/date-key)))

(defn tenki-content
  [content]
  (let [weather(-> content
                   (sel1 [:.forecast :.forecast-telop])
                   en/text)
       high (-> content
                (sel1 [:.temp :.high-temp])
                en/text
                u/to-int)
       low (-> content
               (sel1 [:.temp :.low-temp])
               en/text
               u/to-int)
       rain (-> content
                (sel1 [:.prob-precip])
                en/text
                u/to-int)
        ]
    {:weather weather
     :temp-high high
     :temp-low low
     :rain rain}))

(defn tenkijp
  [snippet]
  (let [page (en/html-snippet snippet)
        version (tenki-version page)
        contents (-> page
                     (en/select [:.forecast10days-actab]))]
    [version
     (reduce (fn [forecasts content]
               (assoc forecasts (tenki-date content) (tenki-content content)))
             (sorted-map)
             contents)]))

(defn yahoo-version
  [page]
  (-> page
      (sel1 [:#addpsnl :.yjSt.yjw_note_h2])
      en/text))

(defn yahoo-date-tomorrow
  [date]
  (let [mmdd (-> date
                 en/text)
        day (->> mmdd
                 (re-find #"(\d+)日")
                 last
                 u/to-int)]
    (-> day
        u/infer-date
        u/date-key)))

(defn yahoo-content-tomorrow
  [content-tomorrow]
  (let [weather (mapv (fn [ix] (-> content-tomorrow
                                   (sel1 [[:tr (en/nth-of-type 2)]])
                                   (sel1 [[:td (en/nth-of-type (+ 4 ix))] :small])
                                   en/text
                                   )) (range 0 4))
        high (mapv (fn [ix] (-> content-tomorrow
                                (sel1 [[:tr (en/nth-of-type 3)]])
                                (sel1 [[:td (en/nth-of-type (+ 4 ix))] :small])
                                en/text
                                u/to-int
                                )) (range 0 4))
        rain (mapv (fn [ix] (-> content-tomorrow
                                (sel1 [[:tr (en/nth-of-type 5)]])
                                (sel1 [[:td (en/nth-of-type (+ 4 ix))] :small])
                                en/text
                                u/to-int
                                )) (range 0 4))
        ]
    {:weather weather
     :temp-high high
     :temp-low nil
     :rain rain
     })
  )

(defn yahoo-date
  [contents ix]
  (let [
        mmddw (-> contents
                 (sel1 [[:tr (en/nth-of-type 1)]])
                 (sel1 [[:td (en/nth-of-type (+ 2 ix))] :small])
                 en/text)
        day (->> mmddw
                 (re-find #"(\d+)日")
                 last
                 u/to-int)]
    (-> day
        u/infer-date
        u/date-key)))

(defn yahoo-content
  [contents ix]
  (let [
        weather (-> contents
                   (sel1 [[:tr (en/nth-of-type 2)]])
                   (sel1 [[:td (en/nth-of-type (+ 2 ix))] :small])
                   en/text)
        high (-> contents
                 (sel1 [[:tr (en/nth-of-type 3)]])
                 (sel1 [[:td (en/nth-of-type (+ 2 ix))] :small [:font (en/nth-of-type 1)]])
                 en/text
                 u/to-int)
        low (-> contents
                (sel1 [[:tr (en/nth-of-type 3)]])
                (sel1 [[:td (en/nth-of-type (+ 2 ix))] :small [:font (en/nth-of-type 2)]])
                en/text
                u/to-int)
        rain (-> contents
                 (sel1 [[:tr (en/nth-of-type 4)]])
                 (sel1 [[:td (en/nth-of-type (+ 2 ix))] :small])
                 en/text
                 u/to-int)
        ]
    {:weather weather
     :temp-high high
     :temp-low low
     :rain rain}))

(defn yahoo
  [snippet]
  (let [page (en/html-snippet snippet)
        version (yahoo-version page)
        date-tomorrow (-> page
                  (sel1 [:#yjw_pinpoint_tomorrow :.yjSt])
                  )
        content-tomorrow (-> page
                     (sel1 [:#yjw_pinpoint_tomorrow :.yjw_table2])
                     )
        contents (-> page
                     (en/select [:#yjw_week :.yjw_table :tbody])
                     ;; :#yjw_pinpoint_tomorrow :.yjw_table2
                     )
        ]
    [version
     (reduce (fn [forecasts ix]
               (assoc forecasts (yahoo-date contents ix) (yahoo-content contents ix)))
             (sorted-map (yahoo-date-tomorrow date-tomorrow) (yahoo-content-tomorrow content-tomorrow))
             (range 0 6))]))
