(ns funcool.boot-codeina
  {:boot/export-tasks true}
  (:require [codeina.utils :as utils]
            [codeina.core :as core]
            [boot.core :refer :all]
            [boot.task.built-in :refer :all]))

(def ^:private
  +defaults+ {:target "doc/api"
              :format :markdown
              :root (System/getProperty "user.dir")
              :src-uri nil
              :src-uri-prefix nil
              :reader :clojure
              :writer :html5})

(deftask apidoc
  "Generate beautiful api documentation."
  [t title          TITLE        str "The project title."
   s sources        SOURCES      #{str} "Sources to read."
   d description    DESC         str "The project description."
   v version        VERSION      str "The project version."
   i include        INCLUDE      [sym] "Include concrete namespaces."
   x exclude        EXCLUDE      [sym] "Exclude concrete namespaces."
   f format         FORMAT       kw  "Docstring format."
   o target         OUTDIR       str "The output directory."
   n root           ROOTDIR      src "The project root directory."
   u src-uri        SRCURI       str "Source code uri"
   p src-uri-prefix SRCURIPREFIX str "Source code uri prefix (for line anchors)"
   w writer         WRITER       kw "Documentation writer."
   r reader         READER       kw "Source reader."]
  (fn [next-handler]
    (fn [fileset]
      (let [options (merge +defaults+ *opts*)
            reader-fn (core/get-reader options)
            writter-fn (core/get-writer options)
            root (:root options)
            namespaces (->> (apply reader-fn sources)
                            (utils/ns-filter include exclude)
                            (utils/add-source-paths root sources))]
        (writter-fn (assoc options :namespaces namespaces)))
      (next-handler fileset))))
