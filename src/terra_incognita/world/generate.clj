(ns terra-incognita.world.generate
  (:import [toxi.math.noise SimplexNoise])
  (:use [terra-incognita.world blocks core]))

(defn simplex [& {:keys [scale average amplitude]
                  :or {scale 1.0 average 0.0 amplitude 1.0}}]
  (let [x-offset (rand (bit-shift-left 1 24))
        y-offset (rand (bit-shift-left 1 24))]
    (fn [x y]
      (->> (SimplexNoise/noise
            (+ x-offset (/ x scale))
            (+ y-offset (/ y scale)))
           (* amplitude)
           (+ average)))))

(defn ocean-world [size]
  {:pre [(= 0 (mod size chunk-width))
         (= 0 (mod size chunk-depth))]}
  (reduce (fn [world chunk-key]
            (put-chunk world chunk-key (filled-chunk water)))
          empty-world
          (for [x (range 0 size chunk-width)
                z (range 0 size chunk-depth)]
            (coords-to-chunk-key x 0 z))))

(defn generate-world [size]
  (let [altitude (simplex :size size :scale 128 :amplitude 12 :average 4)]
    (loop [x 0 z 0 world empty-world]
      (if (= z size) world
          (let [new-x (if (= size x) 0 (inc x))
                new-z (if (= 0 new-x) (inc z) z)
                y (int (altitude x z))
                block (cond
                       (< y 0) water
                       (< y 1) sand
                       (< y 12) grass
                       (< y 14) dirt
                       :else stone)
                y (if (< y 0) 0 y)]
            (recur new-x new-z
                   (put-block world x y z block)))))))
