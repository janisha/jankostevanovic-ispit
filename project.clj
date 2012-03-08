(defproject hang4 "1.0.0-SNAPSHOT"
  :description "Hangman version 4" 
  :dependencies [[org.clojure/clojure "1.2.1"]
				 [postgresql/postgresql "8.4-702.jdbc4"]
				 [org.clojure/java.jdbc "0.1.1"]         
				 [compojure "0.6.4"]
				 [hiccup "0.3.6"]
				 [ring "1.0.2"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]
                     [lein-ring "0.4.5"]
                     [swank-clojure "1.2.1"]]
  :ring {:handler hang4.core/app})