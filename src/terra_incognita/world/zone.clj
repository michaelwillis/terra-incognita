(ns terra-incognita.world.zone)

(def zone-height 256)
(def zone-width 16)

(defn column [] (vector-of :byte))

(defn zone []
  (reduce (fn [acc [x y]] (assoc acc [x y] (vector-of :byte)))
          {} (for [x (range 0 16) y (range 0 16)] [x y])))

(defn- fill-column [col z]
  (reduce (fn [col z] (assoc col z 0)) col (range (count col) z)))

(defn put-block [zone x y z block]
  (let [col (-> [x y] zone (fill-column z))]
    (assoc zone [x y] (assoc col z block))))

(defn get-block [zone x y z]
  (let [col (zone [x y])]
    (if (< z (count col)) (nth col z) 0)))

(def z (zone))

(put-block z 2 0 3 42)
