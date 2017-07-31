(ns zoia.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.file :refer [wrap-file]]
            [compojure.core :refer [defroutes ANY routes]]
            [clojure.java.io :refer [file]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn get-file [file_id]
  (let [file_path "resources/test.mp3"]
    (file file_path)))

; TODO: Implement this 
(defn retrieve-all-json []
  ())

(defresource index
  :available-media-types ["text/html"]
  :handle-ok (file "resources/static/index.html"))

(defresource all
  :available-media-types ["application/json"]
  :handle-ok (retrieve-all-json))

(defresource static [filename]
  :available-media-types ["text/css" "text/javascript"]
  :handle-ok (file (str "resources/static/" filename)))

(defresource parameter [id]
  :available-media-types ["audio/mpeg"]
  :handle-ok (get-file "hardcodedpath")) 

(defn assemble-routes []
  (->
   (routes
    (ANY "/" [] index)
    (ANY "/all" [] all)
    (ANY "/file/:id" [id] (parameter id))
    (ANY "/:filename" [filename] (static filename)))))

(def handler 
  (-> (assemble-routes)
      (wrap-file "resources")
      wrap-params))

