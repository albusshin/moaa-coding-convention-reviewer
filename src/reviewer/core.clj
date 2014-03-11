(ns reviewer.core
  (:gen-class)
  (:use  [clojure.string :only (split lower-case)]))

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
     ("xml" "html" "config" "resx"),
     (re-seq-pos #"<!--.*?-->" code)
     ("css" "less"),
     (re-seq-pos #"(?:/\*(?:[^*]|(?:\*+[^*/]))*\*+/)" code)
     [])))

(defn unfinished-todo-in?
  "return if the code comment has unfinished TODO inside"
  [codecomment-seq]
  (let [codecomment (lower-case (:group codecomment-seq))]
    (every? true?
            [(true?  (substring? "todo" codecomment))
             (false? (substring? "later" codecomment))])))

(defn todo-in?
  "return if the code comment has keyword `TODO` inside"
  [codecomment-seq]
  (let [codecomment (lower-case (:group codecomment-seq))]
    (substring? "todo" codecomment)))

(defn unfinished-todo-seq
  "return the seq-pos of the comments-seq with unfinished todo"
  [comments-seq]
  (filter unfinished-todo-in? comments-seq))

(defn todo-seq
  "return the seq-pos of the comments-seq with each comment with the keyword 'todo'"
  [comments-seq]
  (filter todo-in? comments-seq))

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

(defn apply-unfinished-todos
  "Apply coding conventions on unfinished todos"
  [filename]
  (if-not (.exists (clojure.java.io/as-file filename)) nil
    (let [code (slurp filename)
          file-extension (last (split filename #"\."))]
      (spit filename (unfinished-todo-message code file-extension)))))


(use '[clojure.java.shell :only [sh]])
(defn compare-branches-apply-fn
  "Apply apply-fn on the files changed between HEAD and target-branch, on current working dir"
  [dir target-branch apply-fn]
  (let [files (filter
               (fn [filename] (not (empty? (some #{"cs" "js" "cshtml" "xml" "html" "config" "resx" "css" "less"}
                                                 [(last (split filename #"\."))]))))
               (map #(str dir "/" %)
                    (split (:out
                            (sh "git"
                                "diff"
                                "--name-only"
                                target-branch
                                :dir dir ))
                           #"\n")))]
    (doseq [file files] (apply-fn  file))))

(defn print-help-message
  []
  (println "
           usage: review [repo-dir] [commit|branch]

           repo-dir      : the directory of repository to be checked.
           Checking current directory, just trivially type `.`
           {default value: current working directory}

           commit|branch : the commit hash or the branch name
           as the base of git diff
           {default value: \"origin/master\"}


           version 0.1.1
           https://github.com/albusshin/Reviewer
           Shin
           "))

(defn create-todo-count-map
  "Return a map counted with files"
  [dir target-branch]
  (let [files (filter
               (fn [filename] (and (not (empty? (some #{"cs" "js" "cshtml" "xml" "html" "config" "resx" "css" "less"}
                                                      [(last (split filename #"\."))])))
                                   (.exists (clojure.java.io/as-file filename))))
               (map #(str dir "/" %)
                    (split (:out
                            (sh "git"
                                "diff"
                                "--name-only"
                                target-branch
                                :dir dir ))
                           #"\n")))]
    ((fn [m] (into {} (for [[k v] m] [k (count v)])))
     (group-by identity (reduce concat (map (fn [filename] (let [code (slurp filename)
                                                                 file-extension (last (split filename #"\."))]
                                                             (map :group (todo-seq (code-comments code file-extension))))
                                              ) files))))))

(defn format-review-result
  "Return a review result with the todo count map passed in"
  [todo-count-map]
  (str
   (str
    "Result:\r\n"
    "    |-Total:" (reduce + (vals todo-count-map)) "\r\n"
    "    |" "\r\n"
    "    |-Details" "\r\n"
    "        |-Unfinished TODOs emphasized:" ((fn count-unfinished-todos
                                                [todo-count-map]
                                                (reduce + 0 (vals (filter #(every? true?
                                                                                   [(true?  (substring? "unfinished" (key %)))
                                                                                    (false? (substring? "later" (key %)))]) todo-count-map ))))
                                              todo-count-map) "\r\n"
    "        |-Deferable tasks:" ((fn count-deferable-todos
                                    [todo-count-map]
                                    (reduce + 0 (vals (filter #(every? true?
                                                                       [(true?  (substring? "later" (key %)))]) todo-count-map ))))
                                  todo-count-map) "\r\n"
    "        |-Not defereable tasks:" (- (reduce + (vals todo-count-map)) ((fn count-deferable-todos
                                                                             [todo-count-map]
                                                                             (reduce + 0 (vals (filter #(every? true?
                                                                                                                [(true?  (substring? "later" (key %)))]) todo-count-map ))))
                                                                           todo-count-map)) "\r\n"
    "    |-Mannually refactored tasks:" "\r\n"
    "\r\n\r\n\r\n\r\n"
    "Details of each type of TODOs:" "\r\n"
    )
   (apply str (flatten (for [[k v] (reverse (sort-by val todo-count-map))] ["    |-Count: "(str v) " " k "\r\n"])))
   ))

(defn -main
  [ & args]
  (do
    (if (empty? args)
      (compare-branches-apply-fn (System/getProperty "user.dir") "origin/master" apply-unfinished-todos)
      (if (empty? (rest args))
        (cond (or (= "-h" (first args)) (= "--help" (first args)))
              (print-help-message)
              (= "result" (first args))
              (println (format-review-result (create-todo-count-map "." "origin/master")))
              :else
              (compare-branches-apply-fn (first args) "origin/master" apply-unfinished-todos))
        (if (empty? (rest (rest args)))
          (compare-branches-apply-fn (first args) (first (rest args)) apply-unfinished-todos))))
    (println "Done")))



