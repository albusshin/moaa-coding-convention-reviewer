(ns reviewer.core
  (:gen-class))
(use 'clojure.string)

(defn substring? [sub st]
  (not= (.indexOf st sub) -1))

(defn reload
  []
  (use 'reviewer.core :reload))

(defn re-seq-pos [pattern string] 
  (let [m (re-matcher pattern string)] 
    ((fn step [] 
      (when (. m find) 
        (cons {:start (. m start) :end (. m end) :group (. m group)} 
          (lazy-seq (step))))))))

(defn code-comments 
  "return the comment parts of the source code as a string or a file"
  ([code file-extension]
  (case file-extension 
    ("cs" "js"),
    (re-seq-pos #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)|(?://.*)" code)
    "cshtml",
    (re-seq-pos #"(?:@\*(?:[^*]|(?:\*+[^*@]))*\*+@)|<!--.*?-->" code)
    ("xml" "html"),
    (re-seq-pos #"<!--.*?-->" code)
    ("css" "less"),
    (re-seq-pos #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)" code)))
  ([filename]
  (let [file-extension (last (split filename #"\."))
      code (slurp filename)]
    (code-comments (code file-extension)))))

(defn unfinished-todo-in?
  "return if the code comment has unfinished TODO inside"
  [codecomment]
  (let [codecomment (lower-case codecomment)]
    (every? true? 
            [(true?  (substring? "todo" codecomment))
             (false? (substring? "later" codecomment))
             (false? (substring? "postpone" codecomment))
             (false? (substring? "defer" codecomment))])))

(defn unfinished-todo-seq
  "return the seq-pos of the comments with unfinished todo"
  [comments]
  (let [comments (map lower-case comments)]
    (filter unfinished-todo-in? comments)))

(defn unfinished-todo-message
  "add the unfinished todo message to the whole code"
  [comments-seq-pos code]
  ())

(defn -main
  [& args]
  ())
