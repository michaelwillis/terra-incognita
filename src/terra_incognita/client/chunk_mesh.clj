(ns terra-incognita.client.chunk-mesh
  (:use [terra-incognita.world blocks core]
        [terra-incognita.client math])
  (:import [com.jme3.math Vector3f]
          [com.jme3.material Material]
          [com.jme3.scene Geometry Mesh VertexBuffer$Type]
          [com.jme3.util BufferUtils]))

;;   Top of cube, z+1      Bottom of cube, z
;;   -----------------     -----------------
;;   vertex2   vertex3     vertex6   vertex7
;;   |x,y+1    x+1,y+1     |x,y+1    x+1,y+1     
;;   |     \         |     |               |
;;   |       \       |     |               |
;;   |         \     |     |               |
;;   vertex0   vertex1     vertex4   vertex5
;;   | x,y      x+1,y|     | x,y      x+1,y|
;;   -----------------     -----------------

(defn build-chunk-geometry [chunk]
  (let [mesh (new Mesh)]
    (doseq [[index block] (map list (range (count chunk)) chunk)]
      (if (not= block air)
        (let [[x y z] (chunk-index-to-local-coords index)
              [x1 y1 z1] (map inc [x y z])
              vertices   (vec3-array
                          x   y   z1
                          x1  y   z1
                          x   y1  z1
                          x1  y1  z1)
              tex-coords (vec2-array
                          0 0
                          1 0
                          0 1
                          1 1)
              triangles  (int-array
                          [0 1 2
                           1 3 2])]
          (doto mesh
            (.setBuffer VertexBuffer$Type/Position 3 (BufferUtils/createFloatBuffer vertices))
            (.setBuffer VertexBuffer$Type/TexCoord 2 (BufferUtils/createFloatBuffer tex-coords))
            (.setBuffer VertexBuffer$Type/Index    3 (BufferUtils/createIntBuffer   triangles))
            (.updateBound)))))
    (new Geometry "Chunk" mesh)))
