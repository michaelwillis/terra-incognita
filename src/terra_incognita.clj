(ns terra-incognita
  (:use [terra-incognita.client camera chunk-mesh jme input]
        [terra-incognita.world blocks core generate]))

(def chunks-to-update (atom {}))

(defn update [app t]
  (swap! chunks-to-update
         (fn [chunk-geos]
           (doseq [[chunk-key geo] chunk-geos]
             (.attachChild (.getRootNode app) geo))
           {})))

(defn setup-world [app size]
  (let [world (time (generate-world size))
        block-material (doto (material app "Common/MatDefs/Misc/Unshaded.j3md")
                         (.setTexture "ColorMap" (texture app "textures/blocks.png"))
                         (.setBoolean "VertexColor" true))]
    (doseq [[chunk-key geo] (build-chunk-meshes (world :chunks))]
      (let [[x y z] (chunk-key-to-world-coords chunk-key)]
        (.setLocalTranslation geo x y z))
      (.setMaterial geo block-material)
      (swap! chunks-to-update conj [chunk-key geo]))))

(defn -main []
  (let [app (create-app update)
        world (setup-world app 128)]
    (setup-camera app)
    (setup-input app)
    (move-cam-global app 64 64)))
