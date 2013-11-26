(ns terra-incognita.client.color
  (:import [com.jme3.math ColorRGBA]))

(defn color [r g b]
  (new ColorRGBA r g b 1.0))
