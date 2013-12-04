(ns terra-incognita
  (:use [terra-incognita.client camera chunk-mesh input math color]
        [terra-incognita.world blocks core generate])
  (:import [com.jme3.app SimpleApplication]
           [com.jme3.material Material]
           [com.jme3.scene SceneGraphVisitor Geometry Node shape.Box]
           [com.jme3.asset BlenderKey]
           [com.jme3.animation AnimControl LoopMode]))

(def app
  (proxy [SimpleApplication] []
    (simpleUpdate [seconds] nil)
    (simpleInitApp []
      (let [assets (.getAssetManager this)
            block-material (doto (new Material assets "Common/MatDefs/Misc/Unshaded.j3md")
                             (.setTexture "ColorMap" (.loadTexture assets "textures/blocks.png"))
                             (.setBoolean "VertexColor" true))
            world-size 32
            nodes (->>
                   (for [x (range 0 (inc (/ world-size chunk-width)))
                         z (range 0 (inc (/ world-size chunk-depth)))]
                     (let [n (doto (new Node)
                               (.setLocalTranslation (* chunk-width x) 0 (* chunk-depth z)))]
                       (.attachChild (.getRootNode this) n)
                       [[x z] n]))
                   (into {}))]

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

        (let [world (time (generate-world world-size))
              chunk-geos (time (build-chunk-geometries (world :chunks)))]
          (doseq [[chunk-key geo] chunk-geos]
            (let [[x y z] (chunk-key-to-world-coords chunk-key)]
              (.setLocalTranslation geo x y z))
            (.setMaterial geo block-material)
            (.attachChild (.getRootNode this) geo)))

        (let [rotate-cam (setup-camera this (/ world-size 2) (/ world-size 2))]
          (setup-input-handlers this rotate-cam))))))

(defn update-chunk-meshes [app world]
  (let [assets (.getAssetManager app)
        block-material (doto (new Material assets "Common/MatDefs/Misc/Unshaded.j3md")
                         (.setTexture "ColorMap" (.loadTexture assets "textures/blocks.png"))
                         (.setBoolean "VertexColor" true))
        chunk-geos (time (build-chunk-geometries (world :chunks)))]
    (doseq [[chunk-key geo] chunk-geos]
      (let [[x y z] (chunk-key-to-world-coords chunk-key)]
        (.setLocalTranslation geo x y z))
      (.setMaterial geo block-material)
      (.attachChild (.getRootNode app) geo))))

(defn start [] (.start app))

(defn -main []
  (start))
