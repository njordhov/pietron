(ns app.view.entryform
  (:require
   [mount.core :as mount]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [reagent.ratom
    :refer [reaction]]
   [taoensso.timbre :as timbre]
   [app.lib.clipboard :as clipboard] ; person8 import
   [app.model.slice :as slice]))

(defn on-change-fn [v]
   (fn [e] (reset! v (.. e -target -value))))

(defn validation [test var]
  (if (test @var) "is-valid" "is-invalid"))

(defn title-field [{:keys [title]}]
  [:div
   [:label {:for "title-field"} "Title"]
   [:input {:type "title"
            :class ["form-control"
                    (validation slice/title? title)]
            :id "title-field"
            :value @title
            :on-change (on-change-fn title)
            :aria-describedby "titlelHelp" :placeholder "Enter title"}]
   [:small {:id "titleHelp" :class "form-text text-muted"}
    "The name you will be listed as for your supporters."]])

(defn crypto-field [{:keys [address]}]
  [:div
   [:label {:for "crypto-field"}
    "Bitcoin Address"]
   [:input {:type "text"
            :class ["form-control"
                    (validation slice/bitcoin? address)]
            :id "crypto-field"
            :value @address
            :on-change (on-change-fn address)
            :aria-describedby "cryptoHelp"
            :placeholder "Enter a bitcoin address to receive funds"}]
   [:small {:id "titleHelp" :class "form-text text-muted"}
     "Bitcoin address to receive donations"]])

(defn webpage-field [{:keys [href]}]
  [:div
   [:div
    [:label {:for "webpage-field"} "Webpage"]
    [:input {:type "url"
             :class "form-control"
             :id "href-field"
             :value @href
             :on-change (on-change-fn href)
             :aria-describedby "hrefHelp" :placeholder "Enter url for your webpage"}]]])

(defn email-field [{:keys [email]}]
  [:div
   [:label {:for "email-field"} "Email"]
   [:input {:type "email"
            :class ["form-control"
                    ;; ## TODO: use input.checkValidity() for the browser validation
                    (validation slice/email? email)]
            :id "email-field"
            :value @email
            :on-change (on-change-fn email)
            :aria-describedby "emailHelp" :placeholder "Enter email"}]
   [:small {:id "emailHelp" :class "form-text text-muted"}
    "Optional email address so we can update you about important changes."]])


(defn result-field [{:keys [entry url code]}]
  [:div
   [:label {:for "entry-field"} "Link to insert in your webpages:"]
   [:div.input-group
    [:input.form-control
     {:type "text"
      :style (if-not (slice/entry? @entry)
               {:color "transparent"})
      :value @url
      :read-only true}]
    [:div.input-group-append
     [:button.btn.btn-secondary
      {:type "button"
       :disabled (if-not (slice/entry? @entry) true)
       :on-click #(clipboard/insert-clip @url {:type "text/plain"})}
      "Copy"]]]
   [:a {:hidden (if-not (slice/entry? @entry) true)
        :href @code} "Try it out!"]])

(defn view [{registered? :registered
             :or {registered? false}}]
  (let [service-hostname "https://pietron.app"
        title (reagent/atom "")
        email (reagent/atom "")
        address (reagent/atom "")
        default-href "https://"
        href (reagent/atom default-href)
        encode #(js/encodeURIComponent %)
        code (reaction
              (->> (str "/new?" "name=" (encode @title) "&"
                                ; "email=" (encode @email) "&"
                                "address=" (encode @address))))
        url (reaction
             (->> (str service-hostname @code)
                  (new goog.Uri)
                  (.toString)))
        entry (reaction
               (merge {:title @title
                       :address @address}
                      (if-not (clojure.string/blank? @email)
                        {:email @email})
                      (if-not (= @href default-href)
                        {:href @href})))]
    (fn []
      [:form
       [title-field {:title title}]
       [crypto-field {:address address}]
       (if registered?
         [:<>
          [webpage-field {:href href}]
          [email-field {:email email}]])
       [result-field {:entry entry :url url :code code}]])))

;; # FIX: Should perhaps be elsewhere?

(def new-entry (rf/subscribe [:pie/entry]))
(def signed-in-status (rf/subscribe [:signed-in-status]))

(defn redirect-entry []
  (str
   (.. js/window -location -origin)
   "/new"
   (.. js/window -location -search)))

(defn on-entry []
  (timbre/debug "ON ENTRY:" @signed-in-status @new-entry)
  (when @new-entry
    (case @signed-in-status
      nil (timbre/info "Authentication is pending")
      false (rf/dispatch [:sign-user-in {:redirect-uri (redirect-entry)}])
      true (rf/dispatch [:pie/add-slice @new-entry]))
    (and)))

(mount/defstate observed-entry
  :start (reagent/track! on-entry)
  :end (reagent/dispose! observed-entry))
