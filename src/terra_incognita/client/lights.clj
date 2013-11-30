(ns terra-incognita.client.lights
  (:use [terra-incognita.client color math])
  (:import
   [com.jme3.light DirectionalLight]
   [com.jme3.scene Node]))

(defn directional-light [direction]
  (doto (new DirectionalLight)
    (.setDirection direction) (.setColor (color 0.5 0.5 0.5))))

(defn setup-lights [app]
  (let [sun (directional-light (vec3 -0.2 0.4 -6.0))
        moon (directional-light (vec3 0.2 -0.4 -6.0))]
    (doto (.getRootNode app) (.addLight sun) (.addLight moon))))
