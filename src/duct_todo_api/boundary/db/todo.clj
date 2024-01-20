(ns duct-todo-api.boundary.db.todo
  (:require
   [clojure.spec.alpha :as s]
   [duct.database.sql]))

(s/def ::id nat-int?)
(s/def ::title string?)
(s/def ::todo
  (s/keys :req-un [::id
                   ::title]))
(s/def ::row-count nat-int?)

(s/fdef find-todos
  :args (s/cat :db any?)
  :ret (s/coll-of :todo))

(s/fdef find-todo-by-id
  :args (s/cat :db any?
               :id ::id)
  :ret (s/nilable ::todo))

(s/fdef create-todo!
  :args (s/cat :db any?
               :todo (s/keys :req-un [::title]))
  :ret ::id)

(s/fdef update-todo!
  :args (s/cat :db any?
               :id ::id
               :todo (s/keys :req-un [::title])))

(s/fdef delete-todo!
  :args (s/cat :db any?
               :id ::id)
  :ret ::row-count)

