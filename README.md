# boot-codeina

```clojure
[funcool/boot-codeina "0.1.0-SNAPSHOT"]
```

Tasks for generate beautiful api reference documentation for the [boot Clojure build tool][1]


## Usage

Add `boot-codeina` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[funcool/boot-codeina "X.Y.Z" :scope "test"]])
(require '[adzerk.bootlaces :refer :all])

(task-options!
 apidoc {:version "0.1.0"
         :title "MyPackage name"
         :description "MyPackage description"})
```


And now, execute the `apidoc` task:

```bash
$ boot apidoc
Generated HTML docs in /home/user/yourproject/doc/api
```

[1]: https://github.com/boot-clj/boot
