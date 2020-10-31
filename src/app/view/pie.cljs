(ns app.view.pie
  (:require
   [re-frame.core :as rf]
   [reagent.ratom
    :refer [reaction]]
   [taoensso.timbre :as timbre]
   ["react-minimal-pie-chart" :as chart :default Chart]
   [app.view.palette :as palette]))

(def pie-list (rf/subscribe [:pie/list]))

(def pie-total (rf/subscribe [:pie/total]))

(def active-slice (rf/subscribe [:pie/active-slice]))

(def pie-data
  (reaction
     (map (fn [{:keys [id] :as slice} color]
              (assoc slice :color color))
          @pie-list
          (palette/pie-colors))))

(def transparent "#FFFFFF00")

(defn highlight-active-slice [active slices]
  (map
   (fn [slice]
     (let [active? (= (:id slice) active)
           color (if active? transparent (:color slice))]
       (assoc slice :color color)))
   slices))

(defn chart []
  ; https://www.npmjs.com/package/react-minimal-pie-chart
  (timbre/debug "Pie Chart:" @pie-data)
  (cond
    (= 0 @pie-total)
    [:div "No shares!"]
    @pie-data
    [:div
     [:> Chart
      {:data @(reaction (highlight-active-slice @active-slice @pie-data))
       :background palette/css-color-bisque-complement
       :animate (not @active-slice)
       :on-mouse-over (fn [event data ix]
                        (let [slice (aget data ix)]
                          (timbre/debug "Over:" slice)
                          (rf/dispatch [:pie/active-slice (.-id slice)])))
       :on-mouse-out (fn [event data ix]
                       (timbre/debug "Out:" (aget data ix))
                       (rf/dispatch [:pie/active-slice nil]))}]]))

(defn view []
  [chart])
