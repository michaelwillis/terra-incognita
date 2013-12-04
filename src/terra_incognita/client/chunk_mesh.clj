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

(defn build-triangles [vertex-count & args]
  (map #(+ % (/ vertex-count 3)) args))

(defn build-top [[x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y z1, x1 y z1, x y1 z1, x1 y1 z1)
     (conj tex-coords 0 0, 1 0, 0 1, 1 1)
     (apply (partial conj triangles)
            (build-triangles (count vertices) 0 1 2, 1 3 2))
     (conj vertex-colors 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0, 1.0 1.0 1.0 1.0)]))

(defn build-north [[x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x1 y1 z, x y1 z, x1 y1 z1, x y1 z1)
     (conj tex-coords 0 0, 1 0, 0 1, 1 1)
     (apply (partial conj triangles)
            (build-triangles (count vertices) 0 1 2, 1 3 2))
     (conj vertex-colors 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4, 0.4 0.4 0.4 0.4)]))

(defn build-east [[x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x1 y z, x1 y1 z, x1 y z1, x1 y1 z1)
     (conj tex-coords 0 0, 1 0, 0 1, 1 1)
     (apply (partial conj triangles)
            (build-triangles (count vertices) 0 1 2, 1 3 2))
     (conj vertex-colors 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7, 0.7 0.7 0.7 0.7)]))

(defn build-south [[x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y z, x1 y z, x y z1, x1 y z1)
     (conj tex-coords 0 0, 1 0, 0 1, 1 1)
     (apply (partial conj triangles)
            (build-triangles (count vertices) 0 1 2, 1 3 2))
     (conj vertex-colors 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85, 0.85 0.85 0.85 0.85)]))

(defn build-west [[x y z] vertices tex-coords triangles vertex-colors]
  (let [[x1 y1 z1] (map inc [x y z])]
    [(conj vertices x y1 z, x y z, x y1 z1, x y z1)
     (conj tex-coords 0 0, 1 0, 0 1, 1 1)
     (apply (partial conj triangles)
            (build-triangles (count vertices) 0 1 2, 1 3 2))
     (conj vertex-colors 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55, 0.55 0.55 0.55 0.55)]))

(defn build-block [[vertices tex-coords triangles vertex-colors] [chunk-index block]]
  (if (= block air)
    [vertices tex-coords triangles vertex-colors]
    (let [[x y z] (chunk-index-to-local-coords chunk-index)]
      (->> [vertices tex-coords triangles vertex-colors]
           (apply build-top [x y z])
           (apply build-north [x y z])
           (apply build-east [x y z])
           (apply build-south [x y z])
           (apply build-west [x y z])))))

(defn into-buffer [buffer seq]
  (doseq [val seq] (.put buffer val))
  (.flip buffer))

(defn build-chunk-geometry [chunk]
  (let [empty-vertices      (vector-of :float)
        empty-tex-coords    (vector-of :float)
        empty-triangles     (vector-of :int)
        empty-vertex-colors (vector-of :float)
        [vertices tex-coords triangles vertex-colors]
        (reduce build-block [empty-vertices empty-tex-coords empty-triangles empty-vertex-colors]
                (map-indexed vector chunk))]

    (into-buffer (BufferUtils/createIntBuffer (count triangles)) triangles)
    (new Geometry "Chunk"
         (doto (new Mesh)
           (.setBuffer VertexBuffer$Type/Position 3
                       (into-buffer (BufferUtils/createFloatBuffer (count vertices)) vertices))
           (.setBuffer VertexBuffer$Type/TexCoord 2
                       (into-buffer (BufferUtils/createFloatBuffer (count tex-coords)) tex-coords))
           (.setBuffer VertexBuffer$Type/Index 3
                       (into-buffer (BufferUtils/createIntBuffer (count triangles)) triangles))
           (.setBuffer VertexBuffer$Type/Color 4
                       (into-buffer (BufferUtils/createFloatBuffer (count vertex-colors)) vertex-colors))
           (.setStatic)
           (.updateBound)))))

(defn build-chunk-geometries [chunks]
  (zipmap (keys chunks)
          (pmap build-chunk-geometry (vals chunks))))
