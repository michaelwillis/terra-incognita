(ns terra-incognita
  (:use [terra-incognita.client lights camera input math color])
  (:import [com.jme3.app SimpleApplication FlyCamAppState]
           [com.jme3.font BitmapText]
           [com.jme3.light DirectionalLight]
           [com.jme3.material Material]
           [com.jme3.scene SceneGraphVisitor control.CameraControl Geometry Node shape.Box shape.Quad]
           [com.jme3.asset BlenderKey]
           [com.jme3.animation AnimChannel AnimControl LoopMode]
           [toxi.math.noise SimplexNoise]))

(defn simplex [& {:keys [scale average amplitude]
                  :or {scale 1.0 average 0.0 amplitude 1.0}}]
  (let [x-offset (rand (bit-shift-left 1 24))
        y-offset (rand (bit-shift-left 1 24))]
    (fn [x y]
      (->> (SimplexNoise/noise
            (+ x-offset (/ x scale))
            (+ y-offset (/ y scale)))
           (* amplitude)
           (+ average)))))

(defn generate-world [size place-block blocks place-guy]
  (let [altitude (simplex :size size :scale 128 :amplitude 32 :average 8)]
    (loop [x 0 z 0]
      (let [new-x (if (= size x) 0 (inc x))
            new-z (if (= 0 new-x) (inc z) z)
            y (int (altitude x z))
            block (cond
                   (< y 0) :water
                   (< y 1) :sand
                   (< y 12) :grass
                   (< y 14) :dirt
                   :else :stone)
            y (if (< y 0) 0 y)]
        (when (< z size)
          (place-block x y z (blocks block))
          (place-guy x y z)
          (recur new-x new-z))))))

(def block (new Box 0.5 0.5 0.5))
;; (def block (new Quad 1 1))

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

            dirt (block-material 0.8 0.55 0.3)
            stone (block-material 0.6 0.6 0.6)
            water (block-material 0.1 0.2 1.0)
            grass (block-material 0.1 0.9 0.1)
            sand (block-material 0.8 0.8 0.4)
            world-size 64
            nodes (->>
                   (for [x (range 0 (inc (/ world-size 16)))
                         z (range 0 (inc (/ world-size 16)))]
                     (let [n (doto (new Node)
                               (.setLocalTranslation (* 16 x) 0 (* 16 z)))]
                       (.attachChild (.getRootNode this) n)
                       [[x z] n]))
                   (into {}))
            
            place-block (fn [x y z mat]
                          (let [geo (doto (new Geometry "Block" block)
                                      (.setLocalTranslation (mod x 16) y (mod z 16))
                                      (.setMaterial mat))
                                node (nodes [(int (/ x 16)) (int (/ z 16))])]
                            (.attachChild node geo)))]
        
        (prn nodes)
        (let [guy (.loadAsset assets (new BlenderKey "guy.blend"))]
          
          (.depthFirstTraversal
           guy (proxy [SceneGraphVisitor] []
                 (visit [spatial]
                   (prn (str "BOBOBOBOBOBOBO" (type spatial) " " (.getName spatial))))))
          
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
                    (.attachChild (.getRootNode this) clone)))]
            (time
             (generate-world world-size
                             place-block
                             {:dirt dirt :sand sand :stone stone :water water :grass grass}
                             place-guy))))
        


        (setup-lights this)
        (let [rotate-cam (setup-camera this (/ world-size 2) (/ world-size 2))]
          (setup-input-handlers this rotate-cam))))))

(defn start [] (.start app))

(defn -main []
  (start))
