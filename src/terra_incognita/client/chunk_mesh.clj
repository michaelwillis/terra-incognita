(ns terra-incognita.client.chunk-mesh
  (:use [terra-incognita.world blocks core]
        [terra-incognita.client math])
  (:import [com.jme3.math Vector3f]
          [com.jme3.material Material]
          [com.jme3.scene Geometry Mesh VertexBuffer$Type]
          [com.jme3.util BufferUtils]
          [java.nio FloatBuffer IntBuffer]))

;;   v2---v3
;;   |\    |
;;   |  \  | 
;;   |    \| 
;;   v0---v1 

;; { block-type [top sides] ...}
(def block-textures
  {water [[1 14][1 14]]
   grass [[0 15][0 14]]
   dirt  [[2 15][2 15]]
   stone [[3 15][3 14]]
   sand  [[4 15][4 14]]})

(defn pick-texture [tex-coords [x y]]
  (let [x1 (/ x 16)
        y1 (/ y 16)
        x2 (+ x1 (/ 1 16))
        y2 (+ y1 (/ 1 16))]
    (conj tex-coords x1 y1, x2 y1, x1 y2, x2 y2)))

(defn top-texture [tex-coords block]
  (pick-texture tex-coords (get-in block-textures [block 0])))

(defn side-texture [tex-coords block]
  (pick-texture tex-coords (get-in block-textures [block 1])))

(defn build-triangles [triangles vertices & args]
  (let [vertex-count (/ (count vertices) 3)
        vertex-indices (map (partial + vertex-count) args)]
    (apply (partial conj triangles) vertex-indices)))

(defn build-top [block [x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y z1, x1 y z1, x y1 z1, x1 y1 z1)
     (top-texture tex-coords block)
     (build-triangles triangles vertices 0 1 2, 1 3 2)
     (conj vertex-colors 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0)]))

(defn build-north [block [x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x1 y1 z, x y1 z, x1 y1 z1, x y1 z1)
     (side-texture tex-coords block)
     (build-triangles triangles vertices 0 1 2, 1 3 2)
     (conj vertex-colors 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4)]))

(defn build-east [block [x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x1 y z, x1 y1 z, x1 y z1, x1 y1 z1)
     (side-texture tex-coords block)
     (build-triangles triangles vertices 0 1 2, 1 3 2)
     (conj vertex-colors 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7)]))

(defn build-south [block [x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y z, x1 y z, x y z1, x1 y z1)
     (side-texture tex-coords block)
     (build-triangles triangles vertices 0 1 2, 1 3 2)
     (conj vertex-colors 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85)]))

(defn build-west [block [x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y1 z, x y z, x y1 z1, x y z1)
     (side-texture tex-coords block)
     (build-triangles triangles vertices 0 1 2, 1 3 2)
     (conj vertex-colors 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55)]))

(defn build-block [[vertices tex-coords triangles vertex-colors] [chunk-index block]]
  (if (= block air)
    [vertices tex-coords triangles vertex-colors]
    (let [[x y z] (chunk-index-to-local-coords chunk-index)]
      (->> [vertices tex-coords triangles vertex-colors]
           (apply build-top block [x y z])
           (apply build-north block [x y z])
           (apply build-east block [x y z])
           (apply build-south block [x y z])
           (apply build-west block [x y z])))))

(defn build-chunk-geometry [chunk]
  (let [empty-vertices      (vector-of :float)
        empty-tex-coords    (vector-of :float)
        empty-triangles     (vector-of :int)
        empty-vertex-colors (vector-of :float)
        [vertices tex-coords triangles vertex-colors]
        (reduce build-block [empty-vertices empty-tex-coords empty-triangles empty-vertex-colors]
                (map-indexed vector chunk))]

    (new Geometry "Chunk"
         (doto (new Mesh)
           (.setBuffer VertexBuffer$Type/Position 3 (into-array Float/TYPE vertices))
           (.setBuffer VertexBuffer$Type/TexCoord 2 (into-array Float/TYPE tex-coords))
           (.setBuffer VertexBuffer$Type/Index 3 (into-array Integer/TYPE triangles))
           (.setBuffer VertexBuffer$Type/Color 4 (into-array Float/TYPE vertex-colors))
           (.setStatic)
           (.updateBound)))))

(defn build-chunk-geometries [chunks]
  (zipmap (keys chunks)
          (pmap build-chunk-geometry (vals chunks))))
