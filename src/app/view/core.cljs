(ns app.view.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [taoensso.timbre :as timbre]
   [mount.core :as mount
    :refer [defstate]]
   [app.db]
   [app.events]
   [app.subs]
   [app.view.navbar :as navbar]
   [app.view.pie :as pie]
   [app.view.share :as share]
   [app.view.donation :as donation]))


;; TODO: Similar in other projects (person8) so separate out
;; as external/shared module for autthentification?
;; Note there need to be a script in the html page setting Reloading
;; class depending on blockstack status...
;; better in app.core?

(def signed-in-status (rf/subscribe [:signed-in-status]))

(defn authenticated-hook [signed-in-status]
  "Affect what is shown after logging in and out"
  (timbre/debug "Authenticated Status Changed:" signed-in-status)
  ; class supposed to be added by script in head of html file
  (case signed-in-status
    true
    (js/document.documentElement.classList.add "authenticated")
    false
    (js/document.documentElement.classList.remove "authenticated")
    nil)
    ; when returning from blockstack:
  (if (some? signed-in-status)
    (js/document.documentElement.classList.remove "reloading")))

(defn on-authenticated-changes []
  (authenticated-hook @signed-in-status))

(defstate authenticated-track
  :start (reagent/track! on-authenticated-changes)
  :end (reagent/dispose! authenticated-track))

;;;;;;

(defn state-inspector []
  [:div.col
   [:code>pre
    (with-out-str
      (cljs.pprint/pprint @(rf/subscribe [:debug/db])))]])

(defn page [{:keys [open]}]
  ;; really the body
  [:div.jumbotron.jumbotron-fluid.mt-1.pt-sm-3.pt-0.mb-0
   {:hidden (if open nil true)}
   [:div.container-fluid
    [:div.row
     [:div.col-12.col-sm-4.col-md-5.p-5.p-sm-0.pl-sm-3.pr-sm-3.p-md-4.p-lg-5
      [pie/view]]
     [:div.col-12.col-sm-8.col-md-7.p-0.px-md-3.px-lg-5.d-flex.justify-content-center
      [:div.d-flex.align-items-center.flex-fill
       [share/view]]]]
    (if @(rf/subscribe [:debug/enabled :inspect-state])
      [:div.row
       [:a.button {:href "/new?name=Juliet&ref=oberdinglaw.com"} "New"]
       [state-inspector]])]])

(defn app []
  [:<>
   [navbar/view]
   [page {:open (boolean @signed-in-status)}]])
