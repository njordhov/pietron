(ns app.events
  (:require
   [clojure.spec.alpha :as s]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [taoensso.timbre :as timbre]
   [app.model.slice :as slice
    :refer [new-slice]]
   [app.store :as store]
   [app.config :as config]
   [app.db]))

(defn log-event [& [label]]
  (rf/->interceptor
    :id      ::log-args
    :before  (fn [context]
               (let [event (get-in context [:coeffects :event])]
                 (timbre/debug (or label "Event:") event)
                 context))))

(defn persist-data []
  (rf/->interceptor
   :id ::persist-data
   :after (fn [context]
            (let [{:keys [user-session] :as db}
                  (get-in context [:effects :db])
                  content (get-in db [:pie :data])]
              (assoc-in context
                        [:effects :blockstack/store-file]
                        (assoc store/data-storage
                               :user-session user-session
                               :content content))))))

(rf/reg-event-db
 :initialize
 [(log-event)]
 (fn [db [_ initial override]]
   (if (or override (empty? db)) initial db)))

(rf/reg-event-db
 :pie/update-slice
 [(log-event)
  (persist-data)]
 (fn [db [_ id {:as slice}]]
   {:pre [(string? id)]}
   (update-in db [:pie :data id] merge slice)))

(rf/reg-event-db
 :pie/add-slice
 [(log-event)
  (persist-data)]
 (fn [{:as db}
      [_ {:as slice}]]
   "Add a new slice to the pie, filling in field as needed"
   (let [slice (slice/as-slice slice)
         path [:pie :data (:id slice)]]
     (assert (not (get-in db path)))
     (assoc-in db path slice))))

(rf/reg-event-db
 :pie/active-slice
 [(log-event)]
 (fn [db [_ id]]
   {:pre [(or (nil? id)(string? id))]}
   (assoc-in db [:pie :active] id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONATION

(def test-donation-uri
  "lightning:lntb15u1pw3072qpp5mvaj5jxs8w9ad9c5js9ayfkujfhyyaswgvltymmv3cw8mq8wm40sdq4xysyymr0vd4kzcmrd9hx7cqp5s4u6jw9tgmycuxemf6l83xedncz4s9jtvu3ncdns9ppwls0qlf086lj2dacgakwfxq692l5xv736msmvmtvnp6s33t2qn2gc4jrsjgsqasmhey")

(def alt0-donation-uri ;; 0btc (!)
  "lightning:lntb1pw3s83xpp55hqa08fcy26m0z2z6jhw8l5n47dczrftgtlvfef6az8kwh77qtjqdqqcqzpgxqy9gcqxuaxteancd80n5yrnsdswrg7qusen5edq7wumhl6hpnuq9r9rtypjq95hpc2v6xaxuvemc0zx46gfw8szzsxfn4j4x0lelf8s2tpuhgpwtg8w3")

(def alt1-donation-uri
  "lightning:lnbc1500n1pw3spcxpp5scss32yxefnckr8zy39rwj587m78vy3t39nka7ssn6xy3gcw9ensdpa2fjkzep6ypxxjemgw3hxjmn8ypph2um5dajxjctvyptkzmrvv468xgpdyprk7cqzpgxqr23sg2yk8f7ejq08w7ectvhptmh0t722tzkgv90vyal05h6y3urrd93hkgjj4tghqc3jgt202yn2l9zstcplvghrmhtcuxpdknxjy5hxwgcpjjnwpx")

(def alt2-donation-uri ; 0.43
  "lightning:lntb36u1pw3s8qkpp5ulxthqwk6fsm8l975mvyve9xsutwyjsgxulx2mm0u662yhda36sqdzvxys9xcmpd3sjqsmgd9czq3njv9c8qatrvd5kumevyqcjq3tnwpex2umndusyxmmfdcs9qctwdesscqp5w280h36hq2e0n83jm6lveppzu74dds0pk0xw659xuh46pf05rzqrzgy8cgxjfxl9lsydygwy7vn3qmsha3w48uqrwg570mxey2z60rgpl4uxq9")

(def alt3-donation-uri ; to self in ligthning act, may not work
  "lightning:lntb331420n1pw3s8wrpp5fslq3rmkqy9hphvvr27gsfn2z3tkud9e3u64p2aywm6qr6ffdlrsdqqcqzpgxqy9gcqh9hz2786hxxcyrjkl28lh3pecpmzt05eencr35qp4gt3ku8ryh5n34y0yzdrc8yyq4wrx56ke47rgnvxeyc2tf763jmuh3eaqjdclxspg63av9")

(rf/reg-event-db
 :donation/donate
 [(log-event)]
 (fn [{:keys [donation] :as db} [_]]
   (timbre/info "Donating " (:amount donation))
   (case :multi
     ;; will open new tab
     :open (.open js/window test-donation-uri)
     ;; still testing with multiple invoices
     :multi (do (.open js/window alt0-donation-uri "w1")
              (.open js/window alt1-donation-uri "w2")
              #_(.replace (.-location js/window) alt2-donation-uri))
     ; doesn't open tab
     :location (set! (.-location js/window) test-donation-uri)
     ; perhaps even better?
     :replace (.replace (.-location js/window) test-donation-uri))
   (assoc-in db [:donation :amount] nil)))

(rf/reg-event-db
 :donation/amount
 [(log-event)]
 (fn [db [_ amount]]
   (assoc-in db [:donation :amount] amount)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STATE/

(rf/reg-event-fx
   :state/loaded
   [(log-event)]
   (fn [{{:keys [user-session] :as db} :db :as fx}
        [_ pie-data]]
     ; more resilient to merge than replace...
     {:db (update-in db [:pie :data] merge pie-data)}))

(rf/reg-event-fx
 :state/load ;; load all content from files at startup
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as fx} [_]]
   {:pre [user-session]}
   {:blockstack/list-files {:user-session user-session}
    :blockstack/load-file
    (assoc store/data-storage
           :user-session user-session
           :dispatch #(rf/dispatch [:state/loaded %]))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; APP/

(rf/reg-event-db
 :app/home
 [(log-event)]
 (fn [{:as db} [_]]
   (timbre/info "Welcome!")
   db))

(rf/reg-event-fx
 :app/enter
 [(log-event)]
 (fn [{:as db} [_ {:as item}]]
   {}))

(rf/reg-event-fx
 :app/exit
 [(log-event)]
 (fn [{:as fx} [_ {:as item}]]
   ;; FIX: qualify blockstack keyword
   {:dispatch [:sign-user-out]}))

(rf/reg-event-fx
 :app/signin
 [(log-event)]
 (fn [{db :db :as fx} [_ {:as item}]]
   ;; FIX: qualify blockstack keyword
   (let [redirect-uri (if-let [payload (get-in db [:blockstack :payload])]
                        (str (.. js/window -location -origin)
                             payload))]
     (when redirect-uri
       (timbre/debug "Redirect to:" redirect-uri))
     {:dispatch [:sign-user-in {:redirect-uri redirect-uri}]})))


(rf/reg-event-fx
  :app/new
  [(log-event)]
  (fn [{db :db :as fx} [_ {:keys [params query] :as args}]]
    (let [entry (new-slice {:title (:name query)
                            :address (:address query)
                            :href (:ref query)})]
      ;; ## FIX: should be fx...
       {:db (assoc-in db [:pie :entry] entry)})))

(rf/reg-event-fx
  :app/reset!
  [(log-event)]
  (fn [{db :db :as fx} [_]]
    ;; tmp here, should be covered in lib!
    (.deleteFile (:user-session db) "v1/pie.edn")
    {:db (merge db config/initial-db)}))

(defn initialize-state [initial & [override]]
  (rf/dispatch-sync [:initialize initial override]))
