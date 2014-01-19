(ns reviewer.core-test
  (:require [clojure.test :refer :all]
            [reviewer.core :refer :all]))

(deftest test-code-comment-with-file-extensions
  (testing "test if code-comments returns correct value with file and file extensions"
    (is (= [{:start 0, :end 5, :group "//foo"}]
           (code-comments "//foo\nC#CodeGetAll()" "cs")))
    (is (= [{:start 0, :end 5, :group "//bar"}]
           (code-comments "//bar" "cs")))
    (is (= [{:start 0, :end 5, :group "//bar"} {:start 13, :end 20, :group "/*foo*/"}]
           (code-comments "//bar\nabcdefg/*foo*/fdsa" "cs")))
    (is (nil? (code-comments "abcdefg" "cs"))))
  (testing "test if code-comments returns correct value with .cshtml files"
    (is (= [{:start 11, :end 21, :group "<!--foo-->"}]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "cshtml")))
    (is (= [{:start 13, :end 20, :group "@*bar*@"}]           (code-comments "//b<!baaaaaar@*bar*@ar" "cshtml")))
    (is (= [{:start 13, :end 23, :group "<!--bar-->"} {:start 42, :end 49, :group "@*foo*@"}]
           (code-comments "<Whateveritis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "cshtml")))
    (is nil?
        (code-comments "abcdefg" "cshtml")))
  (testing "test if code-comments returns correct value with .html files"
    (is (= [{:start 11, :end 21, :group "<!--foo-->"}]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "html")))
    (is (= [{:start 5, :end 15, :group "<!--foo-->"} {:start 23, :end 33, :group "<!--bar-->"}]
           (code-comments "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "html")))
    (is nil? (code-comments "abcdefg" "html")))
  (testing "test if code-comments returns correct value with .xml files"
    #_(is (= [{:start 11, :end 21, :group "<!--foo-->"}]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "xml")))
    #_(is (= ({:start 5, :end 15, :group "<!--foo-->"} {:start 23, :end 33, :group "<!--bar-->"})
           (code-comments "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "xml")))
    (is nil? (code-comments "abcdefg" "xml")))
  (testing "test if code-comments returns correct value with .css files"
    (is (= [{:start 0, :end 7, :group "/*foo*/"} {:start 29, :end 36, :group "/*bar*/"}]
           (code-comments "/*foo*/What the foo is this? /*bar*/ Its a bar" "css")))
    (is nil? (code-comments "abcdefg" "css")))
  (testing "test if code-comments returns correct value with .js files"
    (is (= [{:start 6, :end 11, :group "//foo"}]
           (code-comments "C#Code//foo\nGetAll()" "js")))
    (is (= [{:start 0, :end 5, :group "//bar"}]
           (code-comments "//bar" "js")))
    (is (= [{:start 0, :end 5, :group "//bar"} {:start 13, :end 20, :group "/*foo*/"}]
           (code-comments "//bar\nabcdefg/*foo*/fdsa" "js")))
    (is nil? (code-comments "abcdefg" "js"))))

(deftest test-unfinished-todo-in?
  (testing "test if unfinished-todo-in? work properly"
    (is (unfinished-todo-in? {:group "TODO check bug"}))
    (is (unfinished-todo-in? {:group "see, todo"}))
    (is (unfinished-todo-in? {:group "Check TODO"}))
    (is (not (unfinished-todo-in? {:group "TODO later"})))
    (is (not (unfinished-todo-in? {:group "TODO postpone"})))
    (is (not (unfinished-todo-in? {:group "TODO defer"})))))

(deftest test-unfinished-todo-seq
  (testing "test if unfinished-todo-seq work properly"
    (is (= [{:start 0, :end 14,:group "TODO check bug"}] (unfinished-todo-seq [{:start 0, :end 14,:group "TODO check bug"}])))
    (is (= [{:group "see, todo"}] (unfinished-todo-seq [{:group "see, todo"}])))
    (is (= [{:group "Check TODO"} ] (unfinished-todo-seq [{:group "Check TODO"} ])))
    (is (= [] (unfinished-todo-seq [{:group "TODO later"}])))
    (is (= [] (unfinished-todo-seq [{:group "TODO postpone"}])))
    (is (= [] (unfinished-todo-seq [{:group "TODO defer"}])))
    (is (= [{:group "TODO"} {:group "todo"} {:group "tododo"}] (unfinished-todo-seq [{:group "TODO"} {:group "todo"} {:group "tododo"}])))
    (is (= [{:group "todo"}] (unfinished-todo-seq [{:group "abc"} {:group "todo"} {:group "def"}])))))

(deftest test-concat-message
  (testing "test concating with todo is correctly working"
   (is (= "abctodounfinishedtododef" (concat-message "abctododef" "unfinishedtodo" #"todo")))))

(deftest test-unfinished-todo-message
  (testing "test concating unfinished todo message is correctly working"
    (is (= "abcabcdefg" (unfinished-todo-message "abcabcdefg" "cs")))
    (is (= "abca//todo! unfinished TODO \nbcdefg" (unfinished-todo-message "abca//todo\nbcdefg" "cs")))
    (is (= "abca/*todo! unfinished TODO */bcdefg" (unfinished-todo-message "abca/*todo*/bcdefg" "js")))
    (is (= "abca<!--todo! unfinished TODO  check properties-->bcdefg" (unfinished-todo-message "abca<!--todo check properties-->bcdefg" "html")))))
