(defproject reviewer "0.1.0-SNAPSHOT"
  :description "Code reviewer for MOAA coding conventions"
  :url "https://github.com/albusshin/Reviewerx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :main ^:skip-aot reviewer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
