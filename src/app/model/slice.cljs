(ns app.model.slice
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [taoensso.timbre :as timbre]))

(defrecord Entry [title address href email])

(defrecord Slice [id title address created value color href])

; careful, possibly too strict????? Got from clojure spec dox
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def bech32-regex #"^(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,39}$")

(s/def ::title (s/and string? (complement string/blank?)))
(s/def ::bitcoin-address (s/and string?
                                (complement string/blank?)
                                #(re-matches bech32-regex %)))
(s/def ::address ::bitcoin-address)
(s/def ::email (s/nilable
                (s/and string?
                      (complement string/blank?)
                      #(re-matches email-regex %))))
(s/def ::href (s/nilable string?))
(s/def ::id (s/and string? (complement string/blank?)))
(s/def ::value number?) ;; rename to portion?
(s/def ::created int?)
(s/def ::color (s/nilable string?))

(s/def ::entry (s/keys :req-un [::title ::address]
                       :opt-un [::email ::href]))

(s/def ::slice (s/keys :req-un [::id ::title ::address ::value ::created]
                       :opt-un [::color ::href]))


(defn utime [& [date]]
  "UTC universal time"
  (js/Math.floor (/ (.getTime (or date (new js/Date))) 1000)))

(s/fdef new-slice
        :ret ::slice)

(defn ensure [spec val]
  (if (s/valid? spec val)
    true
    (s/explain spec val)))

(defn title? [value]
  (s/valid? ::title value))

(defn email? [value]
  (s/valid? ::email value))

(defn bitcoin? [value]
  (s/valid? ::bitcoin-address value))

(defn entry? [{:as value}]
  (s/valid? ::entry value))

(defn new-slice [{:keys [id title value created address color href]
                  :as data}]
  {:post [(ensure ::slice %)]}
  ;; # FIX: should defaults instead be required in call?
  ;; Cannot use :or in arg destruction for the defaults...
  (let [defaults {:id (str (random-uuid))
                  :value 0
                  :created (utime)}]
    (timbre/debug "Create slice:" (merge defaults data))
    (map->Slice (merge defaults data))))

(defn as-slice [slice]
  (if (instance? Slice slice) slice (new-slice slice)))

#_
(->>
 (new-slice {:title "ClipBox", :value 5, :created 1561532400,
             :href "https://clipbox.in-progress.com",
             :address "xxx"
             :color "#E38627"})
 (s/explain ::slice)
 (timbre/debug "->>>>"))
