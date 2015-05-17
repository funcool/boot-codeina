(ns codeina.core)

(defn- resolve-sym
  "Given a namespace qualified symbol, try resolve
  it and return the underlying value."
  [s]
  (let [ns-part (symbol (namespace s))]
    (try
      (require ns-part)
      (catch Exception e
        (throw (Exception. (str "Could not load codeina writer " s) e))))
    (if-let [value (resolve s)]
      value
      (throw (Exception. (str "Could not resolve codeina writer " s))))))

(defmulti get-writer
  "Get writer function."
  :writer)
(defmulti get-reader
  "Get reader function."
  :reader)

(defmethod get-writer :html5
  [options]
  (resolve-sym 'codeina.writer.html/write-docs))

(defmethod get-reader :clojure
  [options]
  (resolve-sym 'codeina.reader.clojure/read-namespaces))
