(ns match.core
  (:refer-clojure :exclude [reify == inc])
  (:use [logos minikanren tabled rel]))

;; -----------------------------------------------------------------------------
;; Tables

(def method-table (atom {}))
(def pred-table (atom {}))

;; -----------------------------------------------------------------------------
;; Logic

(defrel is a b)

(def implies
     (tabled [a b]
       (conde
         ((is a b))
         ((exist [z]
            (is a z)
            (implies z b))))))

;; -----------------------------------------------------------------------------
;; DAG Operations

(defn add-node [dag pred edges]
  (let [n (count dag)]
    (assoc dag n {:test pred :edges edges})))

;; -----------------------------------------------------------------------------
;; defm

(defn var->sym [v]
  (symbol (str (.ns v)) (name (.sym v))))

(defn get-implied-pred-fns [predsym]
  (map (comp var-get resolve)
       (run* [q]
         (implies predsym q))))

(defn emit-method [mname]
  (let [m (mname @method-table)]))

(defn dag->case [dag])

(defn subsumes? [pga pgb]
  )

(defn guards-for [arg guards]
  (reduce (fn [s guard]
              (if (contains? (set guard) arg)
                (conj s guard)
                s))
          [] guards))

;; in the end will we want to do this together with everything else
;; TODO: descend into more complex destructuring
(defn index-guards [arglist guards]
  (loop [[arg & rargs] arglist path [0] m {}]
    (if arg
      (recur rargs (update-in path [0] clojure.core/inc)
             (assoc m path (guards-for arg guards)))
      m)))

(defn handle-p [mdata arglist guards body]
  )

(defn defpred* [& xs]
  (let [[predsym predfn] (map (comp var->sym resolve) xs)]
   `(swap! pred-table assoc '~predsym '~predfn)))

(defmacro defpred [& xs]
  (apply defpred* xs))

;; sometimes syntax-rules would be real sweet
(defn defm* [mname & [arglist & r']]
  (let [mdata (mname @method-table)]
   (cond
    (vector? arglist) (let [[guards body] (if (== (first r') :guard)
                                            [(second r') (drop 2 r')]
                                            [nil (rest r')])]
                        (handle-p mdata arglist guards body))
    :else nil)))

(defmacro defm [& xs]
  (apply defm* xs))

(comment
  (defpred even? even?)

  ;; could fix this with a macro
  ;; are there composability expectations?
  (fact is `even? `integer?)
  (fact is `integer? `number?)

  (run* [q]
    (implies `even? q))

  (defm foo [x] :guard [(even? x)]
    :two)
  (defm foo [0] :one)

  ;; we kind of have to build backwards?
  (add-node {} `even? {true 1 false 2})

  ;; mapping from arg -> guard list for that arg
  ;; some kind of ordering
  {:arglists #{[0] [x]}
   :guards {[0] [even?]}}

  ;; we some concept of a path to an argument

  ;; pattern match existence / non-existence of keys

  ;; we need to sort the guards on the order of their appearce in the arglist ?

  ;; keys that are internal to a map don't have order anyway
  ;; vectors/seqs do
  (let [{:keys [d e] a :a b :b}]
    )

  ;; keeping a path would work just fine
  [0 :d]
  [0 :b]
  [1 :b :c 0]
  ;; value should probably be a sorted set?

  ;; guard indexing

  [a b] :guard [(= a b)]
  ;; track used guards
  )