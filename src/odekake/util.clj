(ns odekake.util
  (:require
   [clojure.pprint :as pp]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [lambdaisland.uri :as uri])
  (:import
   [java.awt Desktop]
   [java.net URI]
   [java.io File]
   [java.util Locale]
   [java.time LocalDateTime LocalDate]
   [java.time.format DateTimeFormatter]))

(defn tap! [v] (pp/pprint v) v)

(defn to-int
  [s]
  (Integer. (re-find #"\d+" s)))

(defn pad00
  [s]
  (->> s
       to-int
       (format "%02d")))

(defn rev-sort
  ([coll]
   (sort #(compare %2 %1) coll))
  ([keyfn coll]
   (sort-by keyfn #(compare %2 %1) coll)))

(defn resolve-path
  [base-str part-str]
  (let [d (io/file base-str)]
    (-> d
        (.toPath)
        (.resolve part-str)
        (.toString))))

(defn fname-from-path
  [path-str]
  (let [f (io/file path-str)]
    (-> f
        (.getName))))

(defn mkdir
  [path-str]
  (-> path-str
      (File.)
      (.mkdirs)))

(defn read-edn!
  [fpath-str not-found]
  (if (.exists (io/file fpath-str))
    (-> fpath-str
        (slurp)
        (edn/read-string))
    not-found))

(defn write-edn!
  [fpath-str data]
  (let [text (with-out-str (pp/pprint data))]
    (spit fpath-str text)))

(defn read-resource!
  [path-str]
  (-> (io/resource path-str)
      slurp))

(defn wget
  [url-str fpath-str]
  (try
   (with-open [in (io/input-stream url-str)
               out (io/output-stream fpath-str)]
     (io/copy in out)
     url-str)
   (catch Exception e nil)))

(defn open-browser!
  [url-str]
  (let [desktop (Desktop/getDesktop)
        uri (new URI url-str)]
    (.browse desktop uri)))

(defn split-url
  [url-str]
  (let [u (uri/uri url-str)
        q-map (uri/query-map u)
        f (:fragment u)]
    [(uri/uri-str (assoc u :query nil :fragment nil)) q-map f]))

(defn join-url
  ([base q-map] (join-url base q-map nil))
  ([base q-map f]
   (let [q (uri/map->query-string q-map)
         u (uri/uri base)]
     (-> u
         (uri/assoc-query* q-map)
         (assoc :fragment f)
         uri/uri-str))))

(defn now
  []
  (LocalDateTime/now))

(defn today
  []
  (LocalDate/now))

(defn plus-days
  [^LocalDate date num-days]
  (.plusDays date num-days))

(defn next-month-top
  [^LocalDate date]
  (-> date
      (.plusMonths 1)
      (.withDayOfMonth 1)))

(defn infer-date
  [day]
  (let [t (today)
        d (.getDayOfMonth t)]
    (if (>= day d)
      (plus-days t (- day d))
      (plus-days (next-month-top t) (- day 1)))))

(defn datetime2str
  [dt]
  (let [fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss" Locale/JAPANESE)]
    (.format dt fmt)))

(defn str2datetime
  [dt-str]
  (LocalDateTime/parse dt-str))

(defn date-key
  [date]
  (let [fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd" Locale/JAPANESE)]
    (-> date
        (.format fmt)
        keyword)))

(defn date2str
  [^LocalDate date]
  (let [fmt (DateTimeFormatter/ofPattern "M/d(E)" Locale/JAPANESE)]
    (.format date fmt)))

(defn day-of-week
  [^LocalDate date]
  (-> date
      (.getDayOfWeek)
      (.getValue)))

(comment
 
 (split-url "https://yamap.com:8080/hoge-fuga?t=yes&s=no#footer")
 (join-url "https://yamap.com:8080/hoge-fuga" {:t "yes" :s "no"})
 (join-url "https://yamap.com:8080/hoge-fuga" {:t "yes" :s "no"} "footer")

 (-> (today)
     (plus-days 2)
     (date2str))

 (-> (today)
     (day-of-week))

 )
