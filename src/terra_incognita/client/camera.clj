(ns terra-incognita.client.camera
  (:use [terra-incognita.client math])
  (:import [com.jme3.app FlyCamAppState]
           [com.jme3.scene Node]))

(defn cam [app] (.getCamera app))

(defn resolution [cam]
  (vec2 (.getWidth cam) (.getHeight cam)))

(defn center [cam]
  (.divide (resolution cam) 2))

(defn setup-camera [app x z]
  (let [sm (.getStateManager app)]
    (.detach sm (.getState sm FlyCamAppState)))
  (let [cam-node (doto (new Node "Camera Node")
                   (.setLocalTranslation (vec3 -128 512 -128)))
        cam-focus-node (doto (new Node "Camera Focus")
                         (.setLocalTranslation (vec3 x 0 z))
                         (.attachChild cam-node))
        zoom 32]
    (prn (str "Frust: "
              (.getFrustumTop (cam app)) " "
              (.getFrustumRight (cam app)) " "
              (.getFrustumBottom (cam app)) " "
              (.getFrustumLeft (cam app)) " "
              ))
        (doto (cam app)
          (.setParallelProjection true)
          (.setFrustumBottom (* (.getFrustumBottom (cam app)) zoom))
          (.setFrustumLeft (* (.getFrustumLeft (cam app)) zoom))
          (.setFrustumRight (* (.getFrustumRight (cam app)) zoom))
          (.setFrustumTop (* (.getFrustumTop (cam app)) zoom))
          (.setLocation (vec3 4096 512 0))
          (.lookAt (vec3 0 0 0) (vec3 0 1 0)))

        (letfn [(rotate-cam [a]
                  (.rotate cam-focus-node 0 a 0)
                  (doto (cam app)
                    (.setLocation (.getTranslation (.getWorldTransform cam-node)))
                    (.lookAt (.getTranslation (.getWorldTransform cam-focus-node))
                             (vec3 0 1 0))))]
          (rotate-cam 0)
          rotate-cam)))
