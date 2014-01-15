(ns reviewer.core-test
  (:require [clojure.test :refer :all]
            [reviewer.core :refer :all]))

(deftest testGetCodeCommentWithCsFile
  (testing "test if getCodeCommentWithString returns correct value with .cs files"
    (is 
      (= ["foo"]
         (getCodeCommentWithString "C#Code//foo\nGetAll()" "cs")))
    (is 
      (= ["bar"]
         (getCodeCommentWithString "//bar" "cs")))
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "//bar\nabcdefg/*foo*/fdsa" "cs"))))))


(deftest testGetCodeCommentWithCshtmlFile
  (testing "test if getCodeCommentWithString returns correct value with .cshtml files"
    (is 
      (= ["foo"]
         (getCodeCommentWithString "C#View Code<!--foo-->Display(x => x.Name)" "cshtml")))
    (is 
      (= ["bar"]
         (getCodeCommentWithString "//b<!baaaaaar@*bar*@ar" "cshtml")))
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "<Whateveritis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "cshtml"))))))

(deftest testGetCodeCommentWithHtmlFile
  (testing "test if getCodeCommentWithString returns correct value with .html files"
    (is 
      (= ["foo"]
         (getCodeCommentWithString "C#View Code<!--foo-->Display(x => x.Name)" "html")))
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "html"))))))

(deftest testGetCodeCommentWithxmlFile
  (testing "test if getCodeCommentWithString returns correct value with .xml files"
    (is 
      (= ["foo"]
         (getCodeCommentWithString "C#View Code<!--foo-->Display(x => x.Name)" "xml")))
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "<What<!--foo-->everitis<!--bar-->AndDisplayx=>x.Name@*foo*@fdsa" "xml"))))))

(deftest testGetCodeCommentWithCssFile
  (testing "test if getCodeCommentWithString returns correct value with .css files"
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "/*foo*/What the foo is this? /*bar*/ Its a bar" "css"))))))

(deftest testGetCodeCommentWithJsFile
  (testing "test if getCodeCommentWithString returns correct value with .js files"
    (is 
      (= ["foo"]
         (getCodeCommentWithString "C#Code//foo\nGetAll()" "js")))
    (is 
      (= ["bar"]
         (getCodeCommentWithString "//bar" "js")))
    (is 
      (= #{"foo" "bar"}
         (into #{} (getCodeCommentWithString "//bar\nabcdefg/*foo*/fdsa" "js"))))))
