(ns reviewer.core
  (:gen-class))
(use 'clojure.string)

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn reload
  []
  (use 'reviewer.core :reload))

(defn code-comments 
  "return the comment parts of the source code as a string or a file"
  ([code file-extension]
  (case file-extension 
    ("cs" "js") 
    (re-seq #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)|(?://.*)" code)
    "cshtml"
    (re-seq #"(?:@\*(?:[^*]|(?:\*+[^*@]))*\*+@)|<!--.*?-->" code)
    ("xml" "html")
    (re-seq #"<!--.*?-->" code)
    ("css" "less")
    (re-seq #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)" code)))
  ([filename]
  (let [file-extension (last (split filename #"\."))
      code (slurp filename)]
    (code-comments (code file-extension)))))

(defn unfinished-todo-in?
  "return if the code or file passed in has unfinished TODOs inside"
  ([comments]
  (let [comments (map lower-case comments)]
    (every? true? 
            [(some  #(substring? "todo" %) comments)
             (not-any? #(substring? "later" %) comments)
             (not-any? #(substring? "postpone" %) comments)
             (not-any? #(substring? "defer" %) comments)]))))


(defn -main
  [& args]
  ())
