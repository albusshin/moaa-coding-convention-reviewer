(ns reviewer.core-test
  (:require [clojure.test :refer :all]
            [reviewer.core :refer :all]))

(deftest test-code-comment-with-cs-file
  (testing "test if code-comments returns correct value with .cs files"
    (is (= ["//foo"] 
           (code-comments "C#Code//foo\nGetAll()" "cs")))
    (is (= ["//bar"] 
           (code-comments "//bar" "cs")))
    (is (= #{"/*foo*/" "//bar"} 
           (into #{} (code-comments "//bar\nabcdefg/*foo*/fdsa" "cs"))))
    (is (nil? (code-comments "abcdefg" "cs")))))


(deftest test-code-comment-with-cshtml-file
  (testing "test if code-comments returns correct value with .cshtml files"
    (is (= ["<!--foo-->"]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "cshtml")))
    (is (= ["@*bar*@"]
           (code-comments "//b<!baaaaaar@*bar*@ar" "cshtml")))
    (is (= #{"@*foo*@" "<!--bar-->"}
           (into #{} (code-comments "<Whateveritis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "cshtml"))))
    (is nil?
        (code-comments "abcdefg" "cshtml"))))

(deftest test-code-comment-with-html-file
  (testing "test if code-comments returns correct value with .html files"
    (is (= ["<!--foo-->"]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "html")))
    (is (= #{"<!--foo-->" "<!--bar-->"}
           (into #{} (code-comments "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "html"))))
    (is nil? (code-comments "abcdefg" "html"))))

(deftest test-code-comment-with-xml-file
  (testing "test if code-comments returns correct value with .xml files"
    (is (= ["<!--foo-->"]
           (code-comments "C#View Code<!--foo-->Display(x => x.Name)" "xml")))
    (is (= #{"<!--foo-->" "<!--bar-->"}
           (into #{} (code-comments "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "xml"))))
    (is nil? (code-comments "abcdefg" "xml"))))

(deftest test-code-comment-with-css-file
  (testing "test if code-comments returns correct value with .css files"
    (is (= #{"/*foo*/" "/*bar*/"}
           (into #{} (code-comments "/*foo*/What the foo is this? /*bar*/ Its a bar" "css"))))
    (is nil? (code-comments "abcdefg" "css"))))

(deftest test-code-comment-with-js-file
  (testing "test if code-comments returns correct value with .js files"
    (is (= ["//foo"]
           (code-comments "C#Code//foo\nGetAll()" "js")))
    (is (= ["//bar"]
           (code-comments "//bar" "js")))
    (is (= #{"/*foo*/" "//bar"}
           (into #{} (code-comments "//bar\nabcdefg/*foo*/fdsa" "js"))))
    (is nil? (code-comments "abcdefg" "js"))))

(deftest test-unfinished-todos-in?
  (testing "test if unfinished-todo-in? work properly"
    (is (unfinished-todo-in? ["TODO check bug"]))
    (is (unfinished-todo-in? ["see, todo"]))
    (is (unfinished-todo-in? ["Check TODO"]))
    (is (not (unfinished-todo-in? ["TODO later"])))
    (is (not (unfinished-todo-in? ["TODO postpone"])))
    (is (not (unfinished-todo-in? ["TODO defer"])))
    (is (every? unfinished-todo-in? [["TODO"] ["todo"] ["tododo"]]))
    (is (some unfinished-todo-in? [["abc"] ["todo"] ["def"]]))))
