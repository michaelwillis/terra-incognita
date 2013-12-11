(ns terra-incognita.client.camera
  (:use [terra-incognita.client math])
  (:import [com.jme3.app FlyCamAppState]
           [com.jme3.scene Node]
           [com.jme3.math Quaternion]))

(defn cam [app] (.getCamera app))

(defn screen-width [app]
  (.getWidth (cam app)))

(defn screen-height [app]
  (.getHeight (cam app)))

(def focus-x (atom 0))
(def focus-y (atom 0))
(def min-tilt (* Math/PI 0.1))
(def max-tilt (* Math/PI 0.4))
(def tilt (atom min-tilt))
(def spin (atom (/ Math/PI 4)))
(def cam-height 512)
(def zoom 0.02)

(defn update-cam! [app]
  (doto (cam app)
    (.setLocation
     (-> (new Quaternion)
         (.fromAngles @tilt 0 @spin)
         (.mult (vec3 0 0 cam-height))
         (.add @focus-x @focus-y 0)))
    (.lookAt (vec3 @focus-x @focus-y 0) (vec3 0 0 1))))

(defn rotate-cam [app tilt-delta spin-delta]
  (swap! tilt #(-> % (+ tilt-delta) (max min-tilt) (min max-tilt)))
  (swap! spin #(- % spin-delta))
  (update-cam! app))

(defn move-cam-global [app x y]
  (reset! focus-x x)
  (reset! focus-y y)
  (update-cam! app))

(defn move-cam-local [app x y]
  (swap! focus-x (partial + x))
  (swap! focus-y (partial + y))
  (update-cam! app))

(defn setup-camera [app x y]
  (let [sm (.getStateManager app)]
    (.detach sm (.getState sm FlyCamAppState)))
  (doto (cam app)
    (.setParallelProjection true)
    (.setFrustumBottom (/ (.getFrustumBottom (cam app)) zoom))
    (.setFrustumLeft (/ (.getFrustumLeft (cam app)) zoom))
    (.setFrustumRight (/ (.getFrustumRight (cam app)) zoom))
    (.setFrustumTop (/ (.getFrustumTop (cam app)) zoom)))
  (move-cam-global app x y))
