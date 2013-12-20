(ns terra-incognita.client.jme
  "A set of accessor, helper, and wrapper functions for JMonkeyEngine"
  (:import [com.jme3.app SimpleApplication FlyCamAppState]
           [com.jme3.material Material]
           [com.jme3.math Vector2f Vector3f]
           [com.jme3.scene SceneGraphVisitor Geometry]
           [com.jme3.asset BlenderKey]
           [com.jme3.animation AnimControl LoopMode]
           [com.jme3.system AppSettings]))

(defn create-app [update-fn]
  (let [started (atom false)
        settings (new AppSettings true)
        app (proxy [SimpleApplication] []
              (simpleInitApp []
                (let [sm (.getStateManager this)]
                  (.detach sm (.getState sm FlyCamAppState)))
                (reset! started true))
              (simpleUpdate [t] (update-fn this t)))]

    (doto settings (.setWidth (int 640)) (.setHeight (int 480)) (.setFullscreen false))
    (doto app
      (.setSettings settings)
      (.setShowSettings false)
      (.setDisplayStatView false)
      (.start))

    ;; init happens on a separate thread, have to wait here because the
    ;; SimpleApplication object isn't populated with InputManager,
    ;; AssetManager, etc. until init is done
    (while (not @started) (Thread/sleep 1))

    app))

(defn cam [app] (.getCamera app))
(defn assets [app] (.getAssetManager app))
(defn input [app] (.getInputManager app))
(defn root-node [app] (.getRootNode app))
(defn attach [node child] (.attachChild node child))

(defn screen-width [app] (.getWidth (cam app)))
(defn screen-height [app] (.getHeight (cam app)))

(defn material [app resource] (new Material (assets app) resource))
(defn texture [app resource] (.loadTexture (assets app) resource))

(defn vec2 [x y] (new Vector2f x y))
(defn vec3
  ([v] (apply vec3 v))
  ([x y z] (new Vector3f x y z)))

(defn vec3-array [& args]
  (into-array (->> args (partition 3) (map #(apply vec3 %)))))

(defn vec2-array [& args]
  (into-array (->> args (partition 2) (map #(apply vec2 %)))))

