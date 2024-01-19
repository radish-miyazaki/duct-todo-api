(ns duct-todo-api.handler.todo
  (:require
   [ataraxy.response :as response]
   [integrant.core :as ig]))

(def todos
  (atom {1 {:id 1 :title "Build an API"}
         2 {:id 2 :title "?????"}
         3 {:id 3 :title "Profit!"}}))

(defmethod ig/init-key ::list-todos [_ {:keys [_]}]
  (fn [_]
    ;; TODO: DB Access
    [::response/ok (vals @todos)]))

(defmethod ig/init-key ::create-todo [_ {:keys [_]}]
  (fn [{[_ todo] :ataraxy/result}]
    (let [todo-id (->> @todos
                       keys
                       (apply max)
                       inc)]
      (swap! todos assoc todo-id (merge {:id todo-id}
                                        (select-keys todo [:title])))
      [::response/created (str "/todos/" todo-id) (get @todos todo-id)])))

(defmethod ig/init-key ::fetch-todo [_ {:keys [_]}]
  (fn [{[_ todo-id] :ataraxy/result}]
    (if-let [todo (get @todos todo-id)]
      [::response/ok todo]
      [::response/not-found {:message (str "Todo " todo-id " doesn't exist")}])))

(defmethod ig/init-key ::delete-todo [_ {:keys [_]}]
  (fn [{[_ todo-id] :ataraxy/result}]
    (if (get @todos todo-id)
      (swap! todos dissoc todo-id)
      [::response/not-found {:message (str "Todo " todo-id "doesn't exist")}])))

(defmethod ig/init-key ::update-todo [_ {:keys [_]}]
  (fn [{[_ todo-id todo] :ataraxy/result}]
    (swap! todos assoc todo-id (merge {:id todo-id}
                                      (select-keys todo [:title])))
    [::response/created (str "/todos/" todo-id) (get @todos todo-id)]))

