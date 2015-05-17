(set-env!
 :resource-paths #{"src" "resources"}
 :dependencies '[[boot/core "2.0.0-rc14" :scope "provided"]
                 [org.clojure/clojure "1.7.0-beta3" :scope "provided"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/tools.namespace "0.2.10"]
                 [org.pegdown/pegdown "1.4.2"]
                 [leinjacker "0.4.2"]
                 [hiccup "1.0.5"]
                 [funcool/bootutils "0.1.0-SNAPSHOT" :scope "test"]])

(require '[funcool.boot-codeina :refer :all]
         '[funcool.bootutils :refer :all])

(def +version+
  "0.1.0-SNAPSHOT")

(def +description+
  "A tool for generating API documentation from Clojure")

(task-options!
 pom  {:project     'funcool/boot-codeina
       :version     +version+
       :description +description+
       :url         "https://github.com/funcool/codeina"
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
       :scm         {:url "https://github.com/funcool/boot-codeina"}}
 apidoc {:version +version+
         :title "Boot-Codeina"
         :description +description+})
