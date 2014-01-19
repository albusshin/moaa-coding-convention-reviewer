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
  [codecomment-seq]
  (let [codecomment (lower-case (:group codecomment-seq))]
    (every? true? 
            [(true?  (substring? "todo" codecomment))
             (false? (substring? "later" codecomment))
             (false? (substring? "postpone" codecomment))
             (false? (substring? "defer" codecomment))])))

(defn unfinished-todo-seq
  "return the seq-pos of the comments-seq with unfinished todo"
  [comments-seq]
    (filter unfinished-todo-in? comments-seq))

(defn concat-message
  "concat the message passed in with the code comment which has string 'todo' inside"
  [codecomment message pattern]
  (let [codecomment-pos (first (re-seq-pos pattern (lower-case codecomment)))]
  (str (subs codecomment 0 (:end codecomment-pos))
       message
       (subs codecomment (:end codecomment-pos) (. codecomment length)))))

(defn unfinished-todo-message
  "add the unfinished todo message to the whole code"
  [code file-extension]
  (let [comments (sort-by :end (unfinished-todo-seq (code-comments code file-extension))),
        message "! unfinished TODO "]
    (if (empty? comments)
      code  ;if no comments has unfinished todo just return the code itself
      (loop [start 0,
             next-comment (first comments)
             comments (rest comments)
             return-code (subs code 0 (:start next-comment))]
        (if (empty? comments)
          (str return-code
               (concat-message (:group next-comment) message #"todo")
               (subs code (:end next-comment) (. code length)))
            (recur (:end next-comment)
                   (first comments)
                   (rest comments)
                   (str return-code 
                        (concat-message (:group next-comment) message #"todo")
                        (subs code (:end next-comment) (:start (first comments))))))))))

(defn -main
  [& args]
  ())
