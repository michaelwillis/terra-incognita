(ns terra-incognita.client.math
  (:import [com.jme3.math Vector2f Vector3f]))

(defn vec2 [x y] (new Vector2f x y))
(defn vec3 [x y z] (new Vector3f x y z))
