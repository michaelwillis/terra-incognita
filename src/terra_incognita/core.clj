(ns terra-incognita.core)

(import 'com.jme3.app.SimpleApplication)
(import 'com.jme3.material.Material)
(import 'com.jme3.math.Vector3f)
(import 'com.jme3.scene.Geometry)
(import 'com.jme3.scene.shape.Box)
(import 'com.jme3.math.ColorRGBA)

(defn main- []
  (.start (proxy [SimpleApplication] [] (simpleInitApp [] nil ))))
