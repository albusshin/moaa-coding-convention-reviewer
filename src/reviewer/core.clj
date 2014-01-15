(ns reviewer.core
  (:require clojure.contrib.string)
  (:gen-class))

(defn codeCommentWithString 
  ""
  [code fileExtension]
  (if (= fileExtension "cs")
      (if (clojure.contrib.string/substring? "//" code)
        (loop [sections (rest (clojure.contrib.string/split #"//" code))
               section (first sections)
               comments []]
          (if (= section nil)
            comments
            (recur (rest sections) 
                   (first (rest sections))
                   (conj comments (first (clojure.contrib.string/split #"\n" section))))))
        [""] )))

(defn codeCommentWithFile
  "return the comment part of the file passed in"
  [filename]
  (let [fileExtension (last (clojure.contrib.string/split #"\." filename))
      code (slurp filename)]
    (codeCommentWithString code fileExtension)
     ;return the comments identified by "//"
      ));TODO not .cs file extension

(defn unfinishedTodos
  "Find all unfinished TODOs in current file and then add a comment emphasizing the task"
  [filename]
  (do (def lines [])
    (with-open [rdr (clojure.java.io/reader filename)]
      (doseq [line (line-seq rdr)]
        (if (clojure.contrib.string/substring? "todo" (first (codeCommentWithString (clojure.string/lower-case line)
                                                                                    (last (clojure.contrib.string/split #"\." filename)))))
          (def lines (conj lines (str line " !unfinished TODO" )))
          (def lines (conj lines line)))))
    (with-open [wrtr (clojure.java.io/writer "/home/albus/Desktop/dummy")
                #_(clojure.java.io/writer "C:\\Users\\xinti\\Desktop\\dummy")
               #_(clojure.java.io/writer filename) ]
      (doseq [line lines]
        (.write wrtr (str line "\n"))))))

(defn -main
  [& args]
  (unfinishedTodos "/home/albus/Desktop/ChartererController.cs")
  #_(unfinishedTodos "C:\\Users\\xinti\\Desktop\\ChartererController.cs")
  #_(unfinishedTodos "C:\\Users\\xinti\\Desktop\\List.cshtml"))
