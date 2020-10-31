(ns app.store
  (:require
   [cljs.reader :as edn]
   [mount.core :as mount
     :refer [defstate]]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre]
   [app.lib.blockstack :as blockstack]
   [app.model.slice :as slice]
   [app.subs]))

(defn parse-pie [s]
  (timbre/info "Loaded file:\n" s)
  (edn/register-tag-parser! ;; likely misplaced...
    'app.model.slice.Slice slice/new-slice)
  (edn/read-string s))

(def data-storage ;; should be in settings
  {:path "v1/pie.edn"
   :options {:decrypt true}
   ; :spec ::index
   :reader parse-pie
   :writer prn-str})

(def pie-data (rf/subscribe [:pie/data]))
(def signed-in-status (rf/subscribe [:signed-in-status]))

(defn restore-data []
  (rf/dispatch [:state/load]))

(defn entering-hook [signed-in]
  (timbre/debug "Entering:" signed-in)
  (case signed-in
    nil :pending
    false :ignore
    true (restore-data)))

(defn on-entering []
  (let [status @signed-in-status]
    (timbre/debug "Entering status:" status)
    (entering-hook status)))

(defstate observing-signin
  :start (reagent/track! on-entering)
  :end (reagent/dispose! observing-signin))
