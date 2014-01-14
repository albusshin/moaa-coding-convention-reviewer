(ns reviewer.core
  (:require clojure.contrib.string)
  (:gen-class))

(defn unfinishedtodos [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (doseq [line (line-seq rdr)]
      (if (clojure.contrib.string/substring? "todo" (clojure.string/lower-case line))
       (println "unfinished todo") (println "nothing")))))

(defn -main
  [& args]
  (unfinishedtodos "/home/albus/Desktop/ChartererController.cs"))
