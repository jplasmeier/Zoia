(ns zoia.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.java.io :refer [file]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn get-file [file_id]
  (let [file_path "resources/test.mp3"]
    (file file_path)))


(defroutes app
  (ANY "/foo" [] (resource :available-media-types ["text/html"]
                           :handle-ok "<html>Hello, Internet.</html>")))

(defresource parameter [id]
  :available-media-types ["audio/mpeg"]
  :handle-ok (get-file "hardcodedpath")) 

(defroutes app
  (ANY "/file/:id" [id] (parameter id)))

(def handler 
  (-> app 
      wrap-params))

