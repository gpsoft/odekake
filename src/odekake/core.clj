(ns odekake.core
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as str]
   [odekake.scrape :as scrape]
   [odekake.render :as render]
   [odekake.util :as u])
  (:gen-class))

(def ^:private ^:dynamic *debug* true)

(def ^:private cli-options
  [["-l" "--area-list" "Show list of areas"]
   ["-h" "--help" "Show usage"]])

(def ^:private db-file "weather.edn")

(def ^:private all-areas
  {:akiku ["安芸区" "hiroshima" 7 37 6710 34107 34]
   :saka ["坂町" "hiroshima" 7 37 6710 34309 34]
   :etajima ["江田島市" "hiroshima" 7 37 6710 34215 34]
   :akiota ["安芸太田町" "hiroshima" 7 37 6720 34368 34]
   :kure ["呉市" "hiroshima" 7 37 6710 34202 34]
   })

(def ^:private all-sites
  {:wn
   {:site-name "ウェザーニュース"
    :site-name-ab "WN"
    :url-fn (fn [area-id]
              (let [[_ u1 _ _ _ u5] (get all-areas area-id)]
                (str "https://weathernews.jp/onebox/tenki/" u1 "/" u5 "/")))}
   :tenkijp
   {:site-name "tenki.jp"
    :site-name-ab "jp"
    :url-fn (fn [area-id]
              (let [[_ _ u2 u3 u4 u5] (get all-areas area-id)]
                (str "https://tenki.jp/forecast/" u2 "/" u3 "/" u4 "/" u5 "/10days.html")))}
   :yahoo
   {:site-name "Yahoo!"
    :site-name-ab "Y!"
    :url-fn (fn [area-id]
              (let [[_ _ _ _ u4 u5 u6] (get all-areas area-id)]
                (str "https://weather.yahoo.co.jp/weather/jp/" u6 "/" u4 "/" u5 ".html")))}
   })

(defn- show-usage!
  [options-summary]
  (println "Inspects weather for the area specified by argument.")
  (println "Shows list of areas when no arguments specified.")
  (println "Usage: odekake [OPTIONS] [AREA-ID]")
  (println "Ex:    odekake :akiku")
  (println "       odekake :saka")
  (println)
  (println "Options:")
  (println options-summary))

(defn- show-areas!
  []
  (println "AREA ID     AREA")
  (println "-----------+--------------------")
  (dorun (for [area-id (keys all-areas)]
           (printf "%-12s%s%n" area-id (get-in all-areas [area-id 0])))))

(defn- coerce-area-id
  [area-id-str]
  (when area-id-str
    (keyword (last (re-find #"^:*(.+)" area-id-str)) )))

(defn- valid-area?
  [area-id]
  (and (some? area-id)
       (contains? all-areas area-id)))

(defn- fetch-and-scrape-site!
  [area-id site-id page-file scrape-fn]
  (let [{:keys [site-name site-name-ab url-fn]} (site-id all-sites)
        url (url-fn area-id)
        snippet (if *debug* (slurp page-file)
                  (let [snippet (slurp url)]
                    (spit page-file snippet)
                    snippet))
        [version forecasts] (scrape-fn snippet)]
    (array-map :site-name site-name
               :site-name-ab site-name-ab
               :site-url url
               :version version
               :forecasts forecasts)))

(defn- gen-index!
  [db]
  (let [area-ids (keys db)
        html-str (u/read-resource! "weathers.html")]
    (spit "weathers.html"
          (str
           html-str
           (render/index db area-ids)
           "</div></body></html>")))
  )

(defn- gen-details!
  [db area-id]
  (let [{:keys [area-name sites]} (area-id db)
        html-str (u/read-resource! "weather.html")]
    (spit (str (name area-id) ".html")
          (str
           html-str
           (render/area-name area-name)
           (render/site-list sites)
           (render/weather sites)
           "</div></body></html>"))))

(defn- go! [area-id]
  (let [db (u/read-edn! db-file {})
        sites (get-in db [area-id :sites])
        area-name (get-in all-areas [area-id 0])
        wn (fetch-and-scrape-site! area-id :wn "wn.html" scrape/wn) 
        tenkijp  (fetch-and-scrape-site! area-id :tenkijp "tenkijp.html" scrape/tenkijp)
        yahoo  (fetch-and-scrape-site! area-id :yahoo "yahoo.html" scrape/yahoo)
        db (assoc db area-id {:area-name area-name
                              :last-updated (u/datetime2str (u/now))
                              :sites {:wn (or wn (:wn sites))
                                      :tenkijp (or tenkijp (:tenkijp sites))
                                      :yahoo (or yahoo (:yahoo sites))}})
        _ (u/write-edn! db-file db)]
    (gen-index! db)
    (gen-details! db area-id)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        {:keys [area-list help]} options
        area-id (coerce-area-id (first arguments))
        err-options? (when errors
                       (println (str/join \newline errors))
                       true)]
    (cond
     (or help err-options?) (show-usage! summary)
     (or area-list (nil? area-id)) (show-areas!)
     (valid-area? area-id) (go! area-id)
     :else (println "Invalid area-id")
     )))

(comment

 (spit "wn.html" (slurp "https://weathernews.jp/onebox/tenki/hiroshima/34107/"))
 (spit "tenkijp.html" (slurp "https://tenki.jp/forecast/7/37/6710/34107/10days.html"))
 (spit "yahoo.html" (slurp "https://weather.yahoo.co.jp/weather/jp/34/6710/34107.html"))

 )

