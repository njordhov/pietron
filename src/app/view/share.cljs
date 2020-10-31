(ns app.view.share
  (:require
   [goog.Uri :as uri]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [reagent.ratom
    :refer [reaction]]
   [taoensso.timbre :as timbre]
   ["react-minimal-pie-chart" :as chart :default Chart]
   [app.view.suggest :as suggest]
   [app.view.pie :as pie]
   [app.view.palette :as palette]))

;; UI for slices

(def pie-list pie/pie-data)

(def pie-total (rf/subscribe [:pie/total]))

(def donation-amount (rf/subscribe [:donation/amount]))

(defn legend [{color :color}]
  [:span.badge.badge-primary
   {:style {:background-color color
            :width "1em"
            :height "1em"}}
   " "])

(defn percentage [nominator denominator & [decimals]]
  (-> (* 100. nominator)
      (/ denominator)
      (.toFixed decimals)))

(defn link-label [{:keys [href] :as item}]
  (let [uri (uri/parse href)]
    [:span (.getDomain uri) (.getPath uri)]))

(defn backlink [{:keys [href] :as item}]
  [:a {:href href :target "_blank"} [link-label item]])

(defn slice-slider [{:keys [id title value color href] :as item}]
  {:pre [id]}
  (let [on-change (fn [ev val]
                    (rf/dispatch [:pie/slice id val]))]
    [:form
     [:div.form-group {:style {:width "100%"}}
      [:input.custom-range
       {:type "range"
        :style {:width "100%"}
        :value value
        :on-change #(rf/dispatch [:pie/update-slice id
                                  {:value (js/parseFloat (.. % -target -value))}])}]]]))

(defn payout-field [{value :value}]
  (if (number? @donation-amount)
    (str (-> (* @donation-amount value)
             (/ @pie-total)
             (.toFixed 2)))
    (str (percentage value @pie-total 2) "%")))

;; Active slide is set by hover (dekstop) or by click (mobile default behavior).
;; Need to be changed on both desktop and mobile on changes as it is essential behavior.
;; Links should not be shown unless slice is active, as it on mobile causes UX issues.

(def active-slice (rf/subscribe [:pie/active-slice]))

(def hover-var (reagent/atom nil))

(defn set-active-slice [id]
  (reset! hover-var id)
  (rf/dispatch [:pie/active-slice id]))

(defn slide-row [{:keys [item hidden]}]
  [:tr>td
   {:style {:border-top "none"
            :display (when hidden "none")
            :transition "0.5s all ease-in-out"}
    :col-span 4}
   [slice-slider item]])

(defn recipient-field [{:keys [id href active? highlight-color item value title color]}]
  (let [hovering (= id @hover-var)]
      [:tbody {:style (if active? {:box-shadow (str "0 0 0 2px " highlight-color)})
               :className (if active? "tbody-light")
               :on-mouse-over #(set-active-slice id)
               :on-mouse-out #(set-active-slice nil)}
       [:tr
        [:td [legend {:color (if active? highlight-color color)}]]
        [:td [:a {:href (if active? href) :target "_blank"} title]]
        #_[:td [backlink item]]
        [:td [payout-field {:value value}]]]
       [slide-row {:item item
                   :hidden (not (and active? hovering))}]]))

(defn recipient-table []
  [:table.table
       [:thead
        [:tr
         [:th #_(str @pie-total)]
         [:th "Recipient"]
         #_[:th "Homepage"]
         [:th (if (number? @donation-amount)
                "Amount"
                "Share")]]]
       (doall
         (for [{:keys [id title value color href] :as item} @pie-list
               :let [highlight-color palette/css-color-bisque-complement]
               :let [active? (= id @active-slice)]]
           ^{:key (hash-string id)}
           [recipient-field {:item item
                             :id id
                             :href href
                             :value value
                             :title title
                             :color color
                             :active? active?
                             :highlight-color highlight-color}]))
         ;; dummy filler for smooth ux
      [:tbody {:style {:visibility "hidden"}}
         [slide-row {:item (first @pie-list)
                     :hidden (boolean @hover-var)}]]])

(defn view []
  [:div.container-fluid
   {:style {:width "100%"}}
   [recipient-table]
   [suggest/view]])
