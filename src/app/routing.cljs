(ns app.routing
  (:require
   [clojure.spec.alpha :as s]
   [mount.core :refer [defstate]]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion :as rc]
   [reitit.coercion.spec :as rss]
   [re-frame.core :as re-frame]
   [taoensso.timbre :as timbre]
   [app.config :as config]
   [app.events]))


;; see https://github.com/metosin/reitit/blob/master/examples/frontend-re-frame/src/cljs/frontend_re_frame/core.cljs

(s/def ::name string?)

(s/def ::request (s/keys :opt-un [::name]))

(def routes
  [["/"
    {:name :app/home
     :parameters {:query ::request}
     :view "home-page"}]
   ["/signin" {:name :app/signin}]
   ["/exit" :app/exit]
   ["/enter" :app/enter]
   ["/demo" :app/demo]
   ["/donation" :app/donation]
   ["/reset" :app/reset!] ; for debug, nuclear! ## FIX: Disable in production?
   ["/new" {:name :app/new
            :parameters {:query ::request}}]])


(re-frame/reg-fx
 ::navigate!
 (fn [k params query]
   (rfe/push-state k params query)))

(re-frame/reg-event-fx
 ::navigate
 (fn [{:keys [db] :as fx} [_ match]]
   (timbre/debug "Navigate:" match)
   (let [name (get-in match [:data :name])
         params (get-in match [:path-params])
         query (get-in match [:query-params])]
     {:dispatch [name {:params params :query query}]})))

(defn init! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    (fn [new-match]
      (timbre/debug "Route match:" new-match)
      (re-frame/dispatch [::navigate new-match]))
    ;; set to false to enable HistoryAPI
    ;; as if true query args aren't passed on...
    ;; should perhaps be reported as issue to reitit?
    {:use-fragment false}))

(defstate retit-state
  :start (init!))
