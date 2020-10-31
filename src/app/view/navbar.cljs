(ns app.view.navbar
  (:require
   [re-frame.core :as rf]
   ["@material-ui/icons/FlashOn" :default LightningIcon]
   ["@material-ui/icons/AccountCircle" :default AccountCircle]
   ["@material-ui/icons/ExitToApp" :default LogoutIcon]
   [app.view.donation :as donation]))

(def product-name (rf/subscribe [:product/name]))
(def signed-in-status (rf/subscribe [:signed-in-status]))

(defn css-slice [{:keys [start end] :or {start 0 end 90}}]
  [:div
   {:style
      {:width "2em"
       :height "2em"
       :border-radius "50%"
       :background-color "none"
       :backgroundImage
       (str "linear-gradient(" (- end 90) "deg, transparent 50%, bisque 50%),"
            "linear-gradient(" (- start 90) "deg, bisque 50%, transparent 50%")}}])

(defn svg-slice [{:keys [x y r origin offset]}]
  [:svg {:view-box "0 0 230 230"}
    #_[:circle {:cx "115" :cy "115" :r (str r)}]
    [:path
     {:d (clojure.string/join " "
           [(str "M" x "," y) ;; move to center
            "L115,5" ;; draw line to edge
            (str "A" r "," r)
            "1 0,1 190,35 z"])}]])

(defn product-logo []
  [:div {:width "2em" :height "2em"}
    [css-slice {:start 180 :end 90}]]
  #_
  [svg-slice {:r 110}]
  #_
  [:svg {:view-box "0 0 100 100"}#_{:width "100" :height "100"}
   [:circle {:cx "50" :cy "50" :r "40"
             :stroke "white" :stroke-width "12"
             :stroke-dasharray "85 15" :stroke-dashoffset "25"
             :fill "none"}]
   #_
   [:circle {:r "40" :cx "50" :cy "50" :fill "bisque"
             :stroke "tomato" :stroke-width "5"
             :stroke-dasharray "1 3"}]
   "Logo"])

(defn product-area []
  [:a.brand-name {:href "#"}
   [:span {:style {:margin-right "0.5em"}}
    [:div.d-inline-block  #_{:height "2em" :width "2em"}
     [product-logo]]]
   [:span {:style {:margin-left "-1.2em"}}
    @product-name]])

(defn user-status-area [{:keys [signed-in-status]}]
  (let [signout #(rf/dispatch [:app/exit])
        signin #(rf/dispatch [:app/signin])]
    [:form.form-inline
     (if (not signed-in-status)
       [:button.btn.btn-default
        {:type "button"
         :on-click signin}
        [:> AccountCircle]
        "Sign In"]
       [:button.btn.btn-default
        {:type "button"
         :on-click signout}
        [:> LogoutIcon]])]))

(defn view []
 [:div {:style {:height "100"}}
  [:nav.nav.navbar.navbar-dark.bg-dark.sticky-top.shadow {}
   [product-area]
   [donation/view
    {:class "form-inline"
     :hidden (if-not @signed-in-status true)}]
   [user-status-area {:signed-in-status @signed-in-status}]]])
