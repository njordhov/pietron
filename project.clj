(defproject pietron "0.1.0-SNAPSHOT"
  :description "A leiningen project to use proto repl with shadow-cljs."
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 #_[binaryage/devtools "0.9.10"]
                 [metosin/reitit "0.3.9"]
                 [mount "0.1.16"]
                 [thheller/shadow-cljs "2.8.37"]
                 [proto-repl-charts "0.3.1"]
                 [proto-repl "0.3.1"]
                 [re-frame "0.10.6"]
                 [reagent "0.8.1" #_"0.9.0-SNAPSHOT"]
                 [com.taoensso/timbre "4.10.0"]
                 ; from bitbucket!
                 #_ ;; was the one I used, but suddenly got problem missing user session
                 [norderhaug/person8 "dbcafa511bdadbc68e0af9186663f7bcd9de0f03"]
                 [TerjeNorderhaug/person8 "c7e597f3b375a3217307e1130413afed5d7ab61c"]
                 ;; works for github!
                 #_
                 [TerjeNorderhaug/person8 "37c7a9957a42f397819134981df1ed245531d8e0"]]

  :min-lein-version "2.8.1"

  :plugins [[reifyhealth/lein-git-down "0.3.5"]]

  :repositories [["public-github" {:url "git://github.com"}]
                 ["public-bitbucket" {:url "git://bitbucket.org"}]]


 ; repl works better without (per june 2019) or does it?
  ; if included results in errors on loading.
  :repl-options {:nrepl-middleware
                 [#_shadow.cljs.devtools.server.nrepl/cljs-load-file
                  #_shadow.cljs.devtools.server.nrepl/cljs-eval
                  #_shadow.cljs.devtools.server.nrepl/cljs-select]}
  :profiles
  {:dev {:source-paths ["src"]}})
