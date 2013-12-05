(ns terra-incognita.client.camera
  (:use [terra-incognita.client math])
  (:import [com.jme3.app FlyCamAppState]
           [com.jme3.scene Node]
           [com.jme3.math Quaternion]))

(defn cam [app] (.getCamera app))

(defn resolution [cam]
  (vec2 (.getWidth cam) (.getHeight cam)))

(defn center [cam]
  (.divide (resolution cam) 2))

(def min-tilt (* Math/PI 0.1))
(def max-tilt (* Math/PI 0.4))
(def tilt (atom min-tilt))
(def spin (atom (/ Math/PI 4)))
(def cam-height 512)
(def zoom 0.02)

(defn setup-camera [app x y]
  (let [sm (.getStateManager app)]
    (.detach sm (.getState sm FlyCamAppState)))
  (doto (cam app)
    (.setParallelProjection true)
    (.setFrustumBottom (/ (.getFrustumBottom (cam app)) zoom))
    (.setFrustumLeft (/ (.getFrustumLeft (cam app)) zoom))
    (.setFrustumRight (/ (.getFrustumRight (cam app)) zoom))
    (.setFrustumTop (/ (.getFrustumTop (cam app)) zoom)))
  (letfn [(rotate-cam [tilt-delta spin-delta]
            (swap! tilt #(-> % (+ tilt-delta) (max min-tilt) (min max-tilt)))
            (swap! spin #(- % spin-delta))
            (doto (cam app)
              (.setLocation
               (-> (new Quaternion)
                   (.fromAngles @tilt 0 @spin)
                   (.mult (vec3 0 0 cam-height))
                   (.add x y 0)))
              (.lookAt (vec3 x y 0) (vec3 0 0 1))))]
    (rotate-cam (/ Math/PI 8) 0)
    rotate-cam))
