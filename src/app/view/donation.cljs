(ns app.view.donation
  (:require
   ["@material-ui/icons/FlashOn" :default LightningIcon]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [taoensso.timbre :as timbre]))

(def donation-amount (rf/subscribe [:donation/amount]))

(defn on-change-fn [var]
  (fn [e]
    (let [value (.. e -target -value)]
      (timbre/debug "Donation:" value)
      (reset! var value)
      (let [num (if-not (number? value)
                  (js/parseFloat value)
                  value)]
        (if (and (number? num)
                 (not (js/Number.isNaN num)))
          (rf/dispatch [:donation/amount num])
          (rf/dispatch [:donation/amount nil]))))))

;  (if-not (empty? @donation-amount) @donation-amount)

(defn donation-field []
  (let [value (reagent/atom (str @donation-amount))]
    (fn []
      [:input.form-control.donation-field
       {:class (if-not (number? @donation-amount)
                 "form control" #_"is-invalid"
                 "form control is-valid")
        :type "number"
        :aria-label "Amount to donate"
        :placeholder "amount"
        :value @value
        :on-change (on-change-fn value)}])))

(def currency (reagent/atom "STX"))

(defn view [{:as attributes}]
  [:form.needs-validation attributes
   [:div.input-group.donation-group
    #_
    [:div.input-group-prepend
     [:span.input-group-text
      {:style {:color "yellow"}}
      [:> LightningIcon]]]
    [donation-field]
    [:div.input-group-append
     #_
     [:span.input-group-text
      "STX"]
     [:button.btn.btn-secondary.dropdown-toggle
      {:type "button" :data-toggle "dropdown"
       :aria-haspopup true :aria-expanded false}
      @currency
      [:div.dropdown-menu
       (for [{:keys [title] :as item} [{:title "STX"} {:title "BTC"}]]
         ^{:key title}
         [:button ; .dropdown-item
          {:class (if (= title @currency) "active")
           :href "#"
           :on-click #(do (timbre/debug "Change:" title)
                        (reset! currency title))}
          title])]]
     [:button.btn.btn-primary
      {:type "button"
       :on-click #(rf/dispatch [:donation/donate])}
      "Donate"]]]])
