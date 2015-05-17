(ns codeina.core)

(defn get-writer
  [{:keys [writer] :as options}]
  (let [writer-sym writer
        writer-ns (symbol (namespace writer))]
    (try
      (require writer-ns)
      (catch Exception e
        (throw
         (Exception. (str "Could not load codeina writer " writer-ns) e))))
    (if-let [writer (resolve writer-sym)]
      writer
      (throw
         (Exception. (str "Could not resolve codeina writer " writer-sym))))))

(defn get-reader
  [{:keys [reader] :as options}]
  (let [reader-sym reader
        reader-ns (symbol (namespace reader-sym))]
    (try
      (require reader-ns)
      (catch Exception e
        (throw
         (Exception. (str "Could not load codeina reader " reader-ns) e))))
    (if-let [reader (resolve reader-sym)]
      reader
      (throw
         (Exception. (str "Could not resolve codeina reader " reader-sym))))))
