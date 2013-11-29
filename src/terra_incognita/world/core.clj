(ns terra-incognita.world.core
  (:use [terra-incognita.world blocks]))

(def empty-world {:chunks {}})

;; All chunk dimensions are in powers of 2
;; width:  x axis, east/west
;; height: y axis, up/down
;; dpeth:  z axis, north/south
(def chunk-width-power 4)
(def chunk-height-power 4)
(def chunk-depth-power 4)

(def chunk-width  (bit-shift-left 1 chunk-width-power))  
(def chunk-height (bit-shift-left 1 chunk-height-power))
(def chunk-depth  (bit-shift-left 1 chunk-depth-power))

(def chunk-size (* chunk-width chunk-height chunk-depth))

(def filled-chunk
  (memoize #(apply (partial vector-of :byte)
                   (repeat chunk-size %))))

(defn coords-to-chunk-index
  "Returns the index of a given block within a chunk
   Coords may be local or global"
  [x y z]
  (+ (mod x chunk-width)
     (* chunk-width (mod y chunk-height))
     (* chunk-width chunk-height (mod z chunk-depth))))

(defn chunk-index-to-local-coords
  "Returns chunk-local coordinates given an index of a block within a chunk"
  [^Integer index]
  [(mod index chunk-width)
   (-> index (bit-shift-right chunk-width-power) (mod chunk-height))
   (-> (bit-shift-right index (+ chunk-width-power chunk-height-power)))])

(defn coords-to-chunk-key [^Integer x ^Integer y ^Integer z]
  "Takes global coordinates
   Returns a value that can be used as a key to look up the corresponding chunk"
  (map bit-shift-right [x y z] [chunk-width-power chunk-height-power chunk-depth-power]))

(defn get-chunk [world chunk-key]
  (if-let [chunk (get-in world [:chunks chunk-key])]
    chunk (filled-chunk air)))

(defn put-chunk [world chunk-key chunk]
  (assoc-in world [:chunks chunk-key] chunk))

(defn get-block [world x y z]
  (let [chunk-key (coords-to-chunk-key x y z)
        chunk (get-chunk world chunk-key)
        index (coords-to-chunk-index x y z)]
    (nth chunk index)))

(defn put-block [world x y z block]
  (let [chunk-key (coords-to-chunk-key x y z)
        index (coords-to-chunk-index x y z)
        chunk (-> (get-chunk world chunk-key)
                  (assoc index block))]
    (put-chunk world chunk-key chunk)))
