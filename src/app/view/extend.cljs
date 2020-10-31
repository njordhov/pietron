(ns app.view.extend
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre]
   ["reactstrap" :as reactstrap
    :refer [Button, Modal, ModalHeader, ModalBody, ModalFooter]]))

;;     "reactstrap": "8.0.0",
;;     "warning": "^4.0.3"

(def invitation (rf/subscribe [:app/new]))

(defn cancel-button [] ; btn-outline-success
  [:> Button {:color "secondary"
              :on-click #(timbre/info "Cancel...")}
    "Cancel"])

(defn ok-button []
  [:> Button {:color "primary"
              :on-click #(timbre/info "OK")}
   "OK"])

(defn view []
  [:div "Hello"]
  #_
  (let [{:keys [name ref]} @invitation]
    [:> Modal {:is-open (some? @invitation)}
     [:> ModalHeader (str "Added " name " to your pie.")]
     #_
     [:> ModalBody "xxxx"]
     #_
     [:> ModalFooter
      [cancel-button]
      [ok-button]]]
    #_
    [:div.modal {:role "dialog" :aria-hidden "false"}
     [:div.modal-dialog {:role "document"}
      [:div.modal-header
       [:h5.modal-title "Add xxx to your pie?"]]]]))
