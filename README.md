match
====

An optimizing pattern match and predicate dispatch library for Clojure.

Note there's nothing to see here yet. There's a lot of work and research to do before anything useful comes of this. If you've signed a Clojure CA I'm more than willing to accept help.

The basic idea is to maintain a DAG and compile it into a series of nested case statements. The following illustrates the basic idea and compares it against multimethod performance in the latest Clojure 1.3.0 alphas.

More "crazy ideas here":https://github.com/swannodette/match/wiki/Crazy-Ideas.

```clj
(defprotocol I
  (x [this])
  (y [this]))

(defprotocol IClass
  (id [this]))

(deftype A [x y] I (x [_] x) (y [_] y))
(deftype B [x y] I (x [_] x) (y [_] y))
(deftype C [x y] I (x [_] x) (y [_] y))
(deftype D [x y] I (x [_] x) (y [_] y))

(extend-type A IClass (id [_] 0))
(extend-type B IClass (id [_] 1))
(extend-type C IClass (id [_] 2))
(extend-type D IClass (id [_] 3))

(defn dag [f1 f2]
  (case (id f1)
        0 (case (id (x f1))
                0 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood)
                1 :m-not-understood
                2 :m-not-understood
                3 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood))
        1 (case (id (x f1)) 
                0 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood)
                1 :m2
                2 :m-not-understood
                3 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood))
        2 (case (id f2)
                0 (case (id (x f1))
                        0 :m4
                        1 :m2
                        2 :m4
                        3 :m4)
                1 (case (id (x f1))
                        0 :m4
                        1 :m2
                        2 :m4
                        3 :m4)
                2 :m3
                3 :m-ambiguous)))

;; ~340-50ms
(let [s1 (B. nil nil)
      o1 (A. (A. nil nil) s1)
      o2 (A. (A. nil nil) s1)]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (dag o1 o2)))))



(defmulti gf (fn [f1 f2]
               [(class f1)
                (class f2)
                (class (x f1))
                (= (y f1) (y f2))]))

(defmethod gf [A Object A true] [f1 f2] :m1)

;; ~1900ms-2000ms
(let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (gf o1 o2)))))
```

Resources
----

* [Efficient Predicate Dispatch](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.47.4553)
* [Optimizing Pattern Matching"](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.6.5507)
* [Pattern Guards And Transformational Patterns"](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.35.8851)
* [Extensible Pattern Matching for Extensible Languages](http://www.ccs.neu.edu/home/samth/ifl2010-slides.pdf)
* [Compiling Pattern Matching to Good Decision Trees"](http://portal.acm.org/citation.cfm?id=1411311)
* [Art of the Metaobject Protocol](http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=3925)

Copyright (C) 2011 David Nolen

Distributed under the Eclipse Public License, the same as Clojure.