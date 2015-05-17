# boot-codeina

```clojure
[funcool/boot-codeina "0.1.0-SNAPSHOT"]
```

Tasks for generate beautiful api reference documentation for the [boot Clojure build tool][1]


## Usage

Add `boot-codeina` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[funcool/boot-codeina "X.Y.Z" :scope "test"]])
(require '[funcool.boot-codeina :refer :all])

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


You can get the detailed information passing `-h` parameter to the `apidoc` task:

```bash
$ boot apidoc -h
Generate beautiful api documentation.

Options:
  -h, --help              Print this help info.
  -t, --title TITLE       Set the project title to TITLE.
  -d, --description DESC  Set the project description to DESC.
  -v, --version VERSION   Set the project version to VERSION.
  -i, --include INCLUDE   Conj INCLUDE onto include concrete namespaces.
  -x, --exclude EXCLUDE   Conj EXCLUDE onto exclude concrete namespaces.
  -f, --format FORMAT     Set docstring format to FORMAT.
  -o, --target OUTDIR     Set the output directory to OUTDIR.
  -s, --src-uri SRCURI    Set source code uri to SRCURI.
  -w, --writer WRITER     Set documentation writer to WRITER.
  -r, --reader READER     Set source reader to READER.
```

[1]: https://github.com/boot-clj/boot


## Examples ##

- https://funcool.github.io/cats/latest/api/
- https://funcool.github.io/buddy-auth/latest/api/
- https://funcool.github.io/catacumba/latest/api/
