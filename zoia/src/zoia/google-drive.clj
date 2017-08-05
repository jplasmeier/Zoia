(ns zoia.google-drive
  (require [clojure.edn :as edn]
           [google-apps-clj.credentials :as gauth]))

(def creds-file "resources/secrets/google-creds.edn")

(defn read-creds
  [filepath]
  (edn/read-string (slurp filepath)))

(def creds (read-creds creds-file))

