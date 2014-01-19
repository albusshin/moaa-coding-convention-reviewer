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
    (is (unfinished-todo-in? "TODO check bug"))
    (is (unfinished-todo-in? "see, todo"))
    (is (unfinished-todo-in? "Check TODO"))
    (is (not (unfinished-todo-in? "TODO later")))
    (is (not (unfinished-todo-in? "TODO postpone")))
    (is (not (unfinished-todo-in? "TODO defer")))))

(deftest test-unfinished-todo-seq
  (testing "test if unfinished-todo-seq work properly"
    (is (= ["todo check bug"] (unfinished-todo-seq ["TODO check bug"])))
    (is (= ["see, todo"] (unfinished-todo-seq ["see, todo"])))
    (is (= ["check todo"] (unfinished-todo-seq ["Check TODO"])))
    (is (= [] (unfinished-todo-seq ["TODO later"])))
    (is (= [] (unfinished-todo-seq ["TODO postpone"])))
    (is (= [] (unfinished-todo-seq ["TODO defer"])))
    (is (= ["todo" "todo" "tododo"] (unfinished-todo-seq ["TODO" "todo" "tododo"])))
    (is (= ["todo"] (unfinished-todo-seq ["abc" "todo" "def"])))))

