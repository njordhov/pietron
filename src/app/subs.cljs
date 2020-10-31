(ns app.subs
  (:require
    [app.model.slice :as slice-model]
    [re-frame.core :as rf]))

(rf/reg-sub
 :debug/db
 (fn [db _]
   db))

(rf/reg-sub
 :debug/enabled
 (fn [db [_ & modes]]
   false))

(rf/reg-sub
 :product/name
 (fn [db _]
   (get-in db [:product :name])))

(rf/reg-sub
 :pie/data
 (fn [db [_]]
   (get-in db [:pie :data])))

(rf/reg-sub
 :pie/slice
 (fn [db [_ id]]
   (get-in db [:pie :data id])))

(rf/reg-sub
 :pie/list
 (fn [db _]
   {:out [coll?]}
   "Pie slices in presentation order (reverse chronological)"
   (->> (get-in db [:pie :data])
        (map (fn [[id value]]
               (assoc value :id id)))
        (sort-by :created)
        (reverse))))

(rf/reg-sub
 :pie/active-slice
 (fn [db _]
   (get-in db [:pie :active])))

(rf/reg-sub
 :pie/total
 (fn [db _]
   {:post [number?]}
   (->> (get-in db [:pie :data])
        (map (comp :value second))
        (reduce + 0))))

(rf/reg-sub
 :pie/entry
 (fn [db _]
   (get-in db [:pie :entry])))

(rf/reg-sub
 :pie/recommend
 (fn [db _]
   (get-in db [:pie :recommend])))

(rf/reg-sub
 :donation/amount
 (fn [db _]
   (get-in db [:donation :amount])))
