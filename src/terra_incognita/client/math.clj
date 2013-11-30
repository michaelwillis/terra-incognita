(ns terra-incognita.client.math
  (:import [com.jme3.math Vector2f Vector3f]))

(defn vec2 [x y] (new Vector2f x y))
(defn vec3
  ([v] (apply vec3 v))
  ([x y z] (new Vector3f x y z)))

(defn vec3-array [& args]
  (into-array (->> args (partition 3) (map #(apply vec3 %)))))

(defn vec2-array [& args]
  (into-array (->> args (partition 2) (map #(apply vec2 %)))))
