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
                y (range 0 size chunk-depth)]
            (coords-to-chunk-key x y 0))))

(defn generate-world [size]
  (let [altitude (simplex :size size :scale 512 :amplitude 64 :average 32)]
    (loop [x 0 y 0 world empty-world]
      (if (= y size) world
          (let [new-x (if (= size x) 0 (inc x))
                new-y (if (= 0 new-x) (inc y) y)
                z (int (altitude x y))
                block (cond
                       (< z 0) water
                       (< z 4) sand
                       (< z 64) grass
                       (< z 96) dirt
                       :else stone)
                z (if (< z 0) 0 z)]
            (recur new-x new-y (put-block world x y z block)))))))
