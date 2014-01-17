(ns reviewer.core
  (:gen-class))
(use 'clojure.string)

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn reload
  []
  (use 'reviewer.core :reload))

(defn getCodeCommentsWithString 
  "return the comment parts of the source code as a string"
  [code fileExtension]
  (case fileExtension 
    ("cs" "js") 
    (re-seq #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)|(?://.*)" code)
    "cshtml"
    (re-seq #"(?:@\*(?:[^*]|(?:\*+[^*@]))*\*+@)|<!--.*?-->" code)
    ("xml" "html")
    (re-seq #"<!--.*?-->" code)
    "css"
    (re-seq #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)" code)))

(defn codeCommentsWithFile
  "return the comment parts of the source code as a file"
  [filename]
  (let [fileExtension (last (split filename #"\."))
      code (slurp filename)]
    (getCodeCommentsWithString code fileExtension)))

(defn hasUnfinishedTodosByFile?
  "return if the current file has unfinished TODOs inside"
  [filename]
  (hasUnfinishedTodosInComments? (codeCommentsWithFile filename)))

(defn hasUnfinishedTodosInComments?
  "return if the code passed in has unfinished TODOs inside"
  [comments]
  (let [comments (map lower-case comments)]
    (every? true? 
            [(some  #(substring? "todo" %) comments)
             (not-any? #(substring? "later" %) comments)
             (not-any? #(substring? "postpone" %) comments)
             (not-any? #(substring? "defer" %) comments)])))


(defn unfinishedTodos
  "Find all unfinished TODOs in current file and then add a comment emphasizing the task"
  [filename]
  (do (def lines [])
    (with-open [rdr (clojure.java.io/reader filename)]
      (doseq [line (line-seq rdr)]
        (if (substring? "todo" (first (getCodeCommentsWithString (lower-case line)
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
