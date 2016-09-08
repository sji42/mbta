(ns mbta.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]
              [ajax.edn :refer [edn-request-format edn-response-format]]))

;; -------------------------
;; Core

(def server "https://mbta-86422.herokuapp.com")
(defonce doc (r/atom {:departures nil}))

(defn load-departures []
  (GET (str server "/load-departures")
       {:format (edn-request-format)
        :response-format (edn-response-format)
        :handler #(swap! doc assoc :departures %)
        :error-handler #(println "ERROR: " %)}))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h4 "Welcome to the MBTA"]
   [:div [:a {:href "/departures"} "View Departures"]]])

(defn table-body [departures]
  (into [:tbody]
        (cons (into [:tr]
                    (for [header (first departures)]
                      [:th header]))
              (for [departure (rest departures)]
                (into [:tr]
                      (for [value departure]
                        [:td value]))))))

(defn departures-page []
  [:div [:h4 "MBTA Departures"]
   [:table {:class :table} (table-body (:departures @doc))]
   [:div [:a {:href "/"} "Home"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/departures" []
  (session/put! :current-page #'departures-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (load-departures)
  (mount-root))
