(ns codeina.writer.html
  "Documentation writer that outputs HTML."
  (:import java.io.File)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [codeina.utils :as util]
            [codeina.format :as fmt]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.element :refer :all]))

(defn- ns-filename
  [namespace]
  (str (:name namespace) ".html"))

(defn- ns-filepath
  [output-dir namespace]
  (str output-dir "/" (ns-filename namespace)))

(defn- var-uri
  [namespace var]
  (str (ns-filename namespace) "#" (util/var-id (:name var))))

(defn- get-mapping-fn
  [mappings path]
  (some (fn [[re f]] (if (re-find re path) f)) mappings))

(defn- uri-path
  [path]
  (str/replace (str path) File/separator "/"))


(defn- var-source-uri
  [{:keys [src-uri src-uri-mapping src-uri-prefix]}
   {:keys [path file line]}]
  (let [path (uri-path path)
        file (uri-path file)]
    (str src-uri
         (if-let [mapping-fn (get-mapping-fn src-uri-mapping path)]
           (mapping-fn file)
           path)
         (if src-uri-prefix
           (str src-uri-prefix line)))))

(defn- split-ns
  [namespace]
  (str/split (str namespace) #"\."))

(defn- namespace-parts
  [namespace]
  (->> (split-ns namespace)
       (reductions #(str %1 "." %2))
       (map symbol)))

(defn- add-depths
  [namespaces]
  (->> namespaces
       (map (juxt identity (comp count split-ns)))
       (reductions (fn [[_ ds] [ns d]] [ns (cons d ds)]) [nil nil])
       (rest)))

(defn- add-heights
  [namespaces]
  (for [[ns ds] namespaces]
    (let [d (first ds)
          h (count (take-while #(not (or (= d %) (= (dec d) %))) (rest ds)))]
      [ns d h])))

(defn- add-branches
  [namespaces]
  (->> (partition-all 2 1 namespaces)
       (map (fn [[[ns d0 h] [_ d1 _]]] [ns d0 h (= d0 d1)]))))

(defn- namespace-hierarchy [namespaces]
  (->> (map :name namespaces)
       (sort)
       (mapcat namespace-parts)
       (distinct)
       (add-depths)
       (add-heights)
       (add-branches)))

(defn- index-by [f m]
  (into {} (map (juxt f identity) m)))

;; The values in ns-tree-part are chosen for aesthetic reasons, based
;; on a text size of 15px and a line height of 31px.

(defn- ns-tree-part [height]
  (if (zero? height)
    [:span.tree [:span.top] [:span.bottom]]
    (let [row-height 31
          top        (- 0 21 (* height row-height))
          height     (+ 0 30 (* height row-height))]
      [:span.tree {:style (str "top: " top "px;")}
       [:span.top {:style (str "height: " height "px;")}]
       [:span.bottom]])))

(defn- namespaces-menu [options & [current]]
  (let [namespaces (:namespaces options)
        ns-map     (index-by :name namespaces)]
    [:div#namespaces.sidebar
     [:h3 (link-to "index.html" [:span.inner "Namespaces"])]
     [:ul
      (for [[name depth height branch?] (namespace-hierarchy namespaces)]
        (let [class  (str "depth-" depth (if branch? " branch"))
              short  (last (split-ns name))
              inner  [:div.inner (ns-tree-part height) [:span (h short)]]]
          (if-let [ns (ns-map name)]
            (let [class (str class (if (= ns current) " current"))]
              [:li {:class class} (link-to (ns-filename ns) inner)])
            [:li {:class class} [:div.no-link inner]])))]]))

(defn- sorted-public-vars
  [namespace]
  (sort-by (comp str/lower-case :name) (:publics namespace)))

(def ^{:private true}
  default-includes
  (list
   [:meta {:charset "UTF-8"}]
   (include-css "css/default.css")))

(defn- project-title
  [options]
  (str (:title options) " " (:version options)))

(defn- header
  [options]
  (let [title (format "%s Api Documentation" (:title options))]
    [:header
     [:section.title
      [:h1 (link-to "index.html" (h title))]]
      [:small "Version: " (:version options)]]))

(defn- index-page
  [options]
  (html5
   [:head
    default-includes
    [:title (h (project-title options)) " API documentation"]]
   [:body
    (header options)
    [:section.container
     (namespaces-menu options)
     [:section#content.namespace-index
      [:section.title-container
       [:h2 (h (:title options))]
       [:div.doc [:p (h (:description options))]]]
      (for [namespace (sort-by :name (:namespaces options))]
        [:div.namespace
         [:h3 (link-to (ns-filename namespace) (h (:name namespace)))]
         [:div.doc (fmt/format-docstring options nil (update-in namespace [:doc] util/summary))]
         [:div.index
          [:p "Public variables and functions:"]
          (unordered-list
           (for [var (sorted-public-vars namespace)]
             (list " " (link-to (var-uri namespace var) (h (:name var))) " ")))]])]]]))

(defn- var-usage [var]
  (for [arglist (:arglists var)]
    (list* (:name var) arglist)))

(defn- added-and-deprecated-docs [var]
  (list
   (if-let [added (:added var)]
     [:h4.added "added in " added])
   (if-let [deprecated (:deprecated var)]
     [:h4.deprecated "deprecated" (if (string? deprecated) (str " in " deprecated))])))

(defn- var-docs [options namespace var]
  [:div.public.anchor {:id (h (util/var-id (:name var)))}
   [:h3
    (link-to (str "#" (util/var-id (:name var)))
             (h (:name var)))]
   (if-not (= (:type var) :var)
     [:h4.type (name (:type var))])
   (if (:dynamic var)
     [:h4.dynamic "dynamic"])
   (added-and-deprecated-docs var)
   [:div.usage
    (for [form (var-usage var)]
      [:code (h (pr-str form))])]
   [:div.doc (fmt/format-docstring options namespace var)]
   (if-let [members (seq (:members var))]
     [:div.members
      [:h4 "members"]
      [:div.inner
       (let [options (dissoc options :src-uri)]
         (map (partial var-docs options namespace) members))]])
   (if (:src-uri options)
     (if (:file var)
       [:div.src-link (link-to (var-source-uri options var) "view source")]
       (println "Could not generate source link for" (:name var))))])

(defn- namespace-page [options namespace]
  (html5
   [:head
    default-includes
    [:title (h (:name namespace)) " documentation"]]
   [:body
    (header options)
    [:section.container
     (namespaces-menu options namespace)
     [:section#content.namespace-docs
      [:h2#top.anchor (h (:name namespace))]
      (added-and-deprecated-docs namespace)
      [:div.doc (fmt/format-docstring options nil namespace)]
      (for [var (sorted-public-vars namespace)]
        (var-docs options namespace var))]]]))

(defn- copy-resource!
  [^String output-dir src dest]
  (io/copy (io/input-stream (io/resource src))
           (io/file output-dir dest)))

(defn- mkdirs!
  [^String output-dir & dirs]
  (doseq [dir dirs]
    (.mkdirs (io/file output-dir dir))))

(defn- write-index!
  [^String output-dir options]
  (spit (io/file output-dir "index.html") (index-page options)))

(defn- write-namespaces!
  [^String output-dir {:keys [namespaces] :as options}]
  (doseq [namespace namespaces]
    (spit (ns-filepath output-dir namespace)
          (namespace-page options namespace))))

(defn write-docs
  "Take raw documentation info and turn it into formatted HTML."
  [{:keys [target namespaces] :as options}]
  (mkdirs! target "css")
  (copy-resource! target "codeina/css/default.css" "css/default.css")
  (write-index! target options)
  (write-namespaces! target options)
  (println "Generated HTML docs in"
           (.getAbsolutePath (io/file target))))
