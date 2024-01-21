(ns dev
  (:refer-clojure :exclude [test])
  (:require
   [clojure.java.io :as io]
   [clojure.repl :refer :all]
   [clojure.tools.namespace.repl :refer [refresh]]
   [duct.core :as duct]
   [duct.core.repl :as duct-repl :refer [auto-reset]]
   [eftest.runner :as eftest]
   [fipp.edn :refer [pprint]]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt init prep]]
   [integrant.repl.state :refer [config system]]
   [orchestra.spec.test :as stest]
   [ragtime.jdbc]
   [ragtime.repl]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "duct_todo_api/config.edn")))

(defn reset []
  (let [result (integrant.repl/reset)]
    (with-out-str (stest/instrument))
    result))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(def ^:private env-profiles
  {"dev" [:duct.profile/dev :duct.profile/local]})

(defn- validate-env [env]
  (when-not (some #{env} (keys env-profiles))
    (throw (IllegalArgumentException. (format "env `%s` is undefined" env)))))

(defn- load-migration-config [env]
  (when-let [profiles (get env-profiles env)]
    (let [prepped (duct/prep-config (read-config) profiles)
          {{:keys [connection-uri]} :duct.database.sql/hikaricp} prepped
          resources-key :duct.migrator.ragtime/resources]
      {:datastore (ragtime.jdbc/sql-database connection-uri)
       :migrations (-> prepped
                       (ig/init [resources-key])
                       (get resources-key))})))

(defn db-migrate
  "Migrate DB to the latest migration."
  [env]
  (validate-env env)
  (ragtime.repl/migrate (load-migration-config env)))

(defn db-rollback
  "Rollback DB one migration."
  [env]
  (validate-env env)
  (ragtime.repl/rollback (load-migration-config env)))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

