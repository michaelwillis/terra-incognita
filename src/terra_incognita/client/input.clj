(ns terra-incognita.client.input
  (:use [terra-incognita.client camera math])
  (:import [com.jme3.input KeyInput MouseInput RawInputListener]
           [com.jme3.input.controls ActionListener InputListener KeyTrigger MouseAxisTrigger]
           [com.jme3.math Vector2f]))

(def mouse-state (atom nil))

(defn setup-key-bindings [app]
  (let [input (.getInputManager app)
        key-bindings {KeyInput/KEY_UP #(move-cam-local %1 0 (* 10 %2))
                      KeyInput/KEY_DOWN #(move-cam-local %1 0 (- (* 10 %2)))
                      KeyInput/KEY_LEFT #(move-cam-local %1 (- (* 10 %2)) 0)
                      KeyInput/KEY_RIGHT #(move-cam-local %1 (* 10 %2) 0)}]
    (doseq [[k f] key-bindings]
      (let [mapping (str (gensym))
            listener (proxy [ActionListener] []
                       (onAction [_ _ t]
                         (prn [mapping t])
                         (f app t)))]
        (.addMapping input mapping (into-array [(new KeyTrigger k)]))
        (.addListener input listener (into-array [mapping]))))))

(defn handle-rotate-event [app event]
  ;; calc movement relative to screen center
  (let [x2 (- (.getX event) (-> app screen-width (/ 2)))
        y2 (- (.getY event) (-> app screen-height (/ 2)))
        x1 (- x2 (.getDX event))
        y1 (- y2 (.getDY event))
        spin (- (.getAngle (vec2 x2 y1))
                (.getAngle (vec2 x1 y1)))
        tilt (->> (screen-width app)
                  (/ (.getDY event))
                  (* Math/PI))]
    (rotate-cam app tilt spin)))

(defn setup-input-handlers [app]
  (doto (.getInputManager app)
    (.addRawInputListener
     (proxy [RawInputListener] []
       (beginInput [] nil)
       (endInput   [] nil)
       (onJoyAxisEvent     [event] nil)
       (onJoyButtonEvent   [event] nil)
       (onKeyEvent         [event] nil)
       (onMouseButtonEvent [event]
         (if (= (.getButtonIndex event) MouseInput/BUTTON_RIGHT)
           (reset! mouse-state (if (.isPressed event) :rotate))))
       (onMouseMotionEvent [event]
         (if (= @mouse-state :rotate)
           (handle-rotate-event app event)))
       (onTouchEvent [event] nil)))))
