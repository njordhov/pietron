(ns app.config
  (:require
   [app.model.slice
    :refer [utime new-slice]]))

;; vocabulary: pledge, patron, benefactor, receiver, sponsor, donor,
;; protege, recipient....

(def bogus-bitcoin-address ;; eliminate!
  "1PrR3vcYhd9Kmfbq33eVUiPcMyLSgREcUf")

(def person8-bitcoin-address ; on Rune's coinbase
  "3QZd2cvtAod5EJuCA1S726RTjnTTrTTp8U")

(def initial-db
  {:product {:name "Pietron"}
   :pie {:data {"pietron"
                (new-slice
                 {:id "pietron"
                  :title "Pietron"
                  :address person8-bitcoin-address
                  :value 1
                  :created (utime (new js/Date "2019-06-24T00:00:00"))
                  :href "https://pietron.net"
                  :color "#6A2135"})}
         :recommend
               {"pietron"
                {"clipbox"
                  {:id "clipbox"
                   :title "ClipBox"
                   :address person8-bitcoin-address
                   :description "Share your clipboard across devices"
                   :href "https://clipbox.in-progress.com"}
                 "person8"
                  {:id "person8"
                   :title "Person8"
                   :address person8-bitcoin-address
                   :description "A lifeline for the digital nomad"
                   :href "https://person8.in-progress.com"}}}}})
