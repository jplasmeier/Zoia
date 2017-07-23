(defproject zoia "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler zoia.core/handler}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [liberator "0.14.1"]
                 [compojure "1.3.4"]
                 [ring/ring-core "1.2.1"]
                 [google-apps-clj "0.6.1"]]
  
  :main ^:skip-aot zoia.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
