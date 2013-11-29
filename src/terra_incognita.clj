(ns terra-incognita
  (:use [terra-incognita.client lights camera input math color]
        [terra-incognita.world blocks core generate])
  (:import [com.jme3.app SimpleApplication]
           [com.jme3.material Material]
           [com.jme3.scene SceneGraphVisitor Geometry Node shape.Box]
           [com.jme3.asset BlenderKey]
           [com.jme3.animation AnimControl LoopMode]))

(def box (new Box 0.5 0.5 0.5))

(defn render-chunk [chunk place-block]
  (doseq [[index block] (map list (range (count chunk)) chunk)]
    (if (not= block air)
      (let [[x y z] (chunk-index-to-local-coords index)]
        (place-block x y z block)))))

(def app
  (proxy [SimpleApplication] []
    (simpleUpdate [seconds] nil)
    (simpleInitApp []
      (let [assets (.getAssetManager this)
            block-material (fn [r g b]
                             (doto (new Material assets "Common/MatDefs/Light/Lighting.j3md")
                               (.setBoolean "UseMaterialColors" true)
                               (.setTexture "DiffuseMap" (.loadTexture assets "texture.png"))
                               (.setColor "Diffuse" (color r g b))
                               (.setColor "Ambient" (color r g b))))
            block-materials {dirt (block-material 0.8 0.55 0.3)
                             stone (block-material 0.6 0.6 0.6)
                             water (block-material 0.1 0.2 1.0)
                             grass (block-material 0.1 0.9 0.1)
                             sand (block-material 0.8 0.8 0.4)}
            world-size 16
            nodes (->>
                   (for [x (range 0 (inc (/ world-size chunk-width)))
                         z (range 0 (inc (/ world-size chunk-depth)))]
                     (let [n (doto (new Node)
                               (.setLocalTranslation (* chunk-width x) 0 (* chunk-depth z)))]
                       (.attachChild (.getRootNode this) n)
                       [[x z] n]))
                   (into {}))
            
            place-block (fn [x y z block]
                          (let [geo (doto (new Geometry (str "Block (" x "," y "," z ")") box)
                                      (.setLocalTranslation (mod x chunk-width)
                                                            y
                                                            (mod z chunk-depth))
                                      (.setMaterial (block-materials block)))
                                node (nodes [(int (/ x chunk-width))
                                             (int (/ z chunk-depth))])]
                            (.attachChild node geo)))]

        (comment
          (let [guy (.loadAsset assets (new BlenderKey "guy.blend"))]
            (.depthFirstTraversal
             guy (proxy [SceneGraphVisitor] []
                   (visit [spatial]
                     (prn (str (type spatial) " " (.getName spatial))))))
            
            (.setLocalTranslation guy 256 16 256)
            (.setLocalScale guy (vec3 0.1 0.1 0.1))
            
            (let [model (.getChild guy "Cube")
                  control (.getControl model AnimControl)
                  channel (.createChannel control)]
              (.setAnim channel "Walking")
              (.setLoopMode channel LoopMode/Loop)
              (.setSpeed channel 1.0))
            (.attachChild (.getRootNode this) guy)
            (let [place-guy
                  (fn [x y z]
                    (let [clone (.clone guy)
                          model (.getChild clone "Cube")
                          control (.getControl model AnimControl)
                          channel (.createChannel control)]
                      (.setLocalTranslation clone x y z)
                      (.setAnim channel "Walking")
                      (.setLoopMode channel LoopMode/Loop)
                      (.setSpeed channel (+ 1.0 (rand)))
                      (.attachChild (.getRootNode this) clone)))])))

        (let [world (time (generate-world world-size))]
          (render-chunk ((world :chunks) [0 0 0]) place-block))

        (setup-lights this)
        (let [rotate-cam (setup-camera this (/ world-size 2) (/ world-size 2))]
          (setup-input-handlers this rotate-cam))))))

(defn start [] (.start app))

(defn -main []
  (start))
