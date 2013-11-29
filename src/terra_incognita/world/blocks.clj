(ns terra-incognita.world.blocks)

(defmacro blocks [& body]
  (let [defs (map (fn [s n] `(def ~s ~n))
                  body (range (count body)))]
    `(do ~@defs)))

(blocks air water grass dirt stone sand)
