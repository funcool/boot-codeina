(ns funcool.boot-codeina
  {:boot/export-tasks true}
  (:require [codeina.utils :as utils]
            [codeina.core :as core]
            [boot.core :refer :all]
            [boot.task.built-in :refer :all]))

(def ^:private
  +defaults+ {:target "doc/api"
              :format :markdown
              :src-uri nil
              :src-uri-prefix nil
              :reader :clojure
              :writer :html5})

(deftask apidoc
  "Generate beautiful api documentation."
  [t title   TITLE       str "The project title."
   d description DESC    str "The project description."
   v version VERSION     str "The project version."
   i include INCLUDE     [sym] "Include concrete namespaces."
   x exclude EXCLUDE     [sym] "Exclude concrete namespaces."
   f format  FORMAT      kw  "Docstring format."
   o target  OUTDIR      str "The output directory."
   s src-uri SRCURI      str "Source code uri"
   w writer  WRITER      sym "Documentation writer."
   r reader  READER      sym "Source reader."]
  (fn [next-handler]
    (fn [fileset]
      (let [options (merge +defaults+ *opts*)
            reader-fn (core/get-reader options)
            writter-fn (core/get-writer options)
            dirs (input-dirs fileset)
            namespaces (->> dirs
                            (map reader-fn)
                            (mapcat identity)
                            (utils/ns-filter include exclude))]
        (writter-fn (assoc options :namespaces namespaces)))
      (next-handler fileset))))
