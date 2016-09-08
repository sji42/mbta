(ns mbta.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [mbta.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [clojure.data.csv :refer [read-csv]]
            [clj-time.coerce :refer [from-long]]
            [clj-time.format :refer [formatters unparse]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn convert-time [t]
  (unparse (formatters :hour-minute-second) (from-long (Long/parseLong t))))

(defn convert-times [rows]
  (cons (first rows)
        (for [row (rest rows)]
          (assoc row
            0 (convert-time (nth row 0))
            4 (convert-time (nth row 4))))))

(defn load-departures [] (-> "data/Departures.csv" slurp read-csv convert-times vec str))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/load-departures" [] (load-departures))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
