(ns reviewer.core
  (:require clojure.contrib.string)
  (:gen-class))

(defn codeComment
  "return the comment part of code passed in"
  [code]
  (if(clojure.contrib.string/substring? "//" code)
    (rest(clojure.contrib.string/split #"//" code))
    [""]))

(defn unfinishedTodos
  "Find all unfinished TODOs in current file and then add a comment emphasizing the task"
  [filename]
  (do (def lines [])
    (with-open [rdr (clojure.java.io/reader filename)]
      (doseq [line (line-seq rdr)]
        (if (clojure.contrib.string/substring? "todo" (first (codeComment (clojure.string/lower-case line))))
          (conj lines (concat line "! unfinished TODO" ))
          (conj lines line))))
    (with-open [wrtr (clojure.java.io/writer "/home/albus/Desktop/dummy")]
      (doseq [line lines]
        (.write wrtr line)))))

(defn -main
  [& args]
  (unfinishedTodos "/home/albus/Desktop/ChartererController.cs"))
