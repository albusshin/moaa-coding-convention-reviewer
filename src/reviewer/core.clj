(ns reviewer.core
  (:gen-class))
(use 'clojure.string)

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn reload
  []
  (use 'reviewer.core :reload))

(defn getCodeCommentByStartAndEnd
  "detect the code comment by starting and ending characters"
  [start end startPattern endPattern code]
  (if (substring? start code)
    (loop [sections (rest (split code startPattern))
           section (first sections)
           comments []]
      (if (= section nil)
        comments
        (recur (rest sections)
               (first (rest sections))
               (conj comments (first (split section endPattern))))))))

(defn getCodeCommentWithString 
  "return the comment of the code passed in"
  [code fileExtension]
  (case fileExtension 
    ("cs" "js") 
    (into (getCodeCommentByStartAndEnd "//" "\n" #"//" #"\n" code)
          (getCodeCommentByStartAndEnd "/*" "*/" #"/\*" #"\*/" code))
    "cshtml"
    (into (getCodeCommentByStartAndEnd "@*" "*@" #"@\*" #"\*@" code)
          (getCodeCommentByStartAndEnd "<!--" "-->" #"<!--" #"-->" code))
    "xml"
    (getCodeCommentByStartAndEnd "<!--" "-->" #"<!--" #"-->" code)
    "html"
    (getCodeCommentByStartAndEnd "<!--" "-->" #"<!--" #"-->" code)
    "css"
    (getCodeCommentByStartAndEnd "/*" "*/" #"/\*" #"\*/" code)))

(defn codeCommentWithFile
  "return the comment part of the file passed in"
  [filename]
  (let [fileExtension (last (split filename #"\."))
      code (slurp filename)]
    (getCodeCommentWithString code fileExtension)
     ;return the comments identified by "//"
      ));TODO not .cs file extension

(defn hasUnfinishedTodosByFile?
  "return if the current file has unfinished TODOs inside"
  [filename]
  (loop [comments (codeCommentWithFile filename)
         hasUnfinishedTodos false]
    (if (= (first comments) nil)
      false
        (if (substring? "todo" (lower-case (first comments)))
          true
          (recur (rest comments) false)))))

(defn hasUnfinishedTodosInComments?
  "return if the code passed in has unfinished TODOs inside"
  [comments]
  (some  #(substring? "todo" %) (map lower-case comments)))

(defn unfinishedTodos
  "Find all unfinished TODOs in current file and then add a comment emphasizing the task"
  [filename]
  (do (def lines [])
    (with-open [rdr (clojure.java.io/reader filename)]
      (doseq [line (line-seq rdr)]
        (if (substring? "todo" (first (getCodeCommentWithString (lower-case line)
                   (last (split filename #"\.")))))
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
