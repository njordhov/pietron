(ns app.view.suggest
  (:require
   ["@material-ui/icons/ExpandLess" :default CollapseIcon]
   ["@material-ui/icons/ExpandMore" :default ExpandIcon]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [reagent.ratom
    :refer [reaction]]))

(def recommend (rf/subscribe [:pie/recommend]))

(defn add-suggestion-button [{entry :entry}]
  [:button.btn.btn-success.float-right
   {:on-click #(rf/dispatch [:pie/add-slice entry])}
   "Add " (:title entry) " to Pie"])

(defn recommend-list-item [{entry :entry}]
  (if entry
    [:li.list-group-item
     (:title entry)[:br]
     (:description entry)[:br]
     [add-suggestion-button
      {:entry entry}]]))

(defn recommendation-list [{suggestions :suggestions}]
  (timbre/debug "Suggestions:" (empty? suggestions) suggestions)
  [:<>
   (doall
     (for [[id candidates] suggestions
           :let [origin @(rf/subscribe [:pie/slice id])]]
       ^{:key id}
       [:div
        [:h6 "Because you sponsor " (:title origin) ":"]
        (into [:ul.list-group]
              (for [[id entry] candidates]
                ^{:key id}
                [recommend-list-item {:entry entry}]))]))])

(def suggestions
  "Only the recommendations to suggest"
  (let [keep-values (fn [f m]
                      (->> m
                          (keep (fn [[k v]]
                                   (if-let [u (f v)]
                                     (if-not (empty? u)[k u]))))
                          (into {})))
        used? (fn [id] @(rf/subscribe [:pie/slice id]))]
    (reaction
     (->> @recommend
         (keep-values (fn [candidates]
                        (->> candidates
                            (keep-values (fn [entry]
                                           (if-not (used? (:id entry))
                                             entry))))))))))

(defn recommend-card []
  (let [collapsed (reagent/atom true)]
    (fn []
      [:div.card {:hidden (empty? @suggestions)}
       [:h5.card-header
         {:style (if @collapsed {:height "2em"
                                 :padding-top "0.4em"})
          :on-click #(swap! collapsed not)}]
       [:h6.card-subheader "Suggestions"
          [:div.float-right
           (if @collapsed [:> ExpandIcon] [:> CollapseIcon])]]
       [:div.card-body
        {:hidden (if @collapsed true)}
        [recommendation-list
         {:suggestions @suggestions}]]])))

(defn view []
  [recommend-card])
