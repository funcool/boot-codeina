(ns codeina.format
  "Documentation writer that outputs HTML."
  (:import org.pegdown.PegDownProcessor
           org.pegdown.Extensions
           org.pegdown.LinkRenderer
           org.pegdown.LinkRenderer$Rendering
           org.pegdown.ast.WikiLinkNode)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [codeina.utils :as util]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.element :refer :all]))

(def ^:private url-regex
  #"((https?|ftp|file)://[-A-Za-z0-9+()&@#/%?=~_|!:,.;]+[-A-Za-z0-9+()&@#/%=~_|])")

(defn- add-anchors
  [text]
  (when text
    (str/replace text url-regex "<a href=\"$1\">$1</a>")))

(def ^:private
  pegdown
  (PegDownProcessor.
   (bit-or Extensions/AUTOLINKS
           Extensions/QUOTES
           Extensions/SMARTS
           Extensions/STRIKETHROUGH
           Extensions/TABLES
           Extensions/FENCED_CODE_BLOCKS
           Extensions/WIKILINKS
           Extensions/DEFINITIONS
           Extensions/ABBREVIATIONS)
   2000))

(defn- find-wikilink
  [options ns text]
  (let [ns-strs (map (comp str :name) (:namespaces options))]
    (if (contains? (set ns-strs) text)
      (str text ".html")
      (when-let [var (util/search-vars (:namespaces options) text (:name ns))]
        (str (namespace var) ".html#" (util/var-id var))))))

(defn- link-renderer
  [options ns]
  (proxy [LinkRenderer] []
    (render
      ([node]
       (if (instance? WikiLinkNode node)
         (let [text (.getText node)]
           (LinkRenderer$Rendering. (find-wikilink options ns text) text))
         (proxy-super render node)))
      ([node text]
       (proxy-super render node text))
      ([node url title text]
       (proxy-super render node url title text)))))

(defmulti format-docstring
  "Format the docstring of a var or namespace into HTML."
  (fn [options ns var]
    (or (:doc/format var)
        (:format options)))
  :default :markdown)

(defmethod format-docstring :plaintext
  [_ _ metadata]
  (html
   [:pre.plaintext (add-anchors (h (:doc metadata)))]))

(defmethod format-docstring :markdown
  [options ns metadata]
  (html
   [:div.markdown
    (when-let [doc (:doc metadata)]
      (.markdownToHtml pegdown doc (link-renderer options ns)))]))
