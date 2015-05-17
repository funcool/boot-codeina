(ns funcool.boot-codeina
  {:boot/export-tasks true}
  (:require [codeina.utils :as utils]
            [codeina.core :as core]
            [boot.core :refer :all]
            [boot.task.built-in :refer :all]))

(def ^:private
  +defaults+ {:target "doc/api"
              :format :markdown
              :src-uri "http://github.com/clojure/clojure/blob/master/"
              :src-uri-prefix "#L"
              :root   (System/getProperty "user.dir")
              :reader 'codeina.reader.clojure/read-namespaces
              :writer 'codeina.writer.html/write-docs})

(deftask apidoc
  "Generate beautiful api documentation."
  [t title   TITLE       str "The project title."
   d description DESC    str "The project description."
   v version VERSION     str "The project version."
   i include INCLUDE     [sym] "Include concrete namespaces."
   x exclude EXCLUDE     [sym] "Exclude concrete namespaces."
   f format  FORMAT      kw  "Docstring format (default :markdown)."
   o target  OUTDIR      str "The output directory."
   w writer  WRITER      sym "Documentation writer."
   s src-uri SRCURI      str "Source code uri"
   r reader  READER      sym "Source reader."]
  (fn [next-handler]
    (fn [fileset]
      (let [options (merge +defaults+ *opts*)
            root   (System/getProperty "user.dir")
            reader-fn (core/get-reader options)
            writter-fn (core/get-writer options)
            dirs (input-dirs fileset)
            namespaces (->> dirs
                            (map reader-fn)
                            (mapcat identity)
                            (utils/ns-filter include exclude))]
        (writter-fn (assoc options :namespaces namespaces)))
      (next-handler fileset))))
