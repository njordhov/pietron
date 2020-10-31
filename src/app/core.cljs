(ns app.core
  (:require
   [app.lib.blockstack :as blockstack
    :refer [init-blockstack]]
   [mount.core :as mount]
   [reagent.core :as r]
   [app.config :as config]
   [app.events :as events]
   [app.view.core :as view]
   [app.view.entryform :as entryform]
   [app.routing :as routing]
   [app.store :as store]
   [app.grpc]))

(defn mount-root []
  (->> (.getElementById js/document "app")
       (r/render-component [view/app]))
  (->> (.getElementById js/document "entryform")
       (r/render-component [entryform/view])))

(defn ^:dev/before-load stop []
  (mount/stop))

(defn ^:dev/after-load start []
  (events/initialize-state config/initial-db true)
  (mount/start)
  (mount-root)
  (init-blockstack))

(defn ^:export main []
  (start))
