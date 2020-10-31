(ns app.view.palette
  "custom colors"
  (:require
   [taoensso.timbre :as timbre]
   ["color" :as Color]))

(def css-color-bisque "#FFE4C4") ; https://www.99colors.net/name/bisque
(def css-color-bisque-complement-bright "#C4DFFF")
(def css-color-bisque-complement        "#86ACD9")
(def css-color-bisque-complement-dark   "#547FB2")
(def css-color-bisque-complement-light  "#EAF4FF")

(defn random-hex-color []
  ; tmp to get a color for the slice, move to color lib?
  (->
   (reduce #(+ (* 10 %) (rand-int 16)) 0 (range 6))
   (+ (js/Math.pow 2 24))
   (.toString 16)
   (subs 1)))

#_
(random-hex-color)


(def pie-default-color (new Color css-color-bisque))

(defn pie-colors []
  (let [base pie-default-color]
    (->> (reductions #(.darken %1 0.1) base (range))
         (map #(.hex %))
         #_(map #(.string %)))))
