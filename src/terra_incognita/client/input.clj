(ns terra-incognita.client.input
  (:use [terra-incognita.client camera math])
  (:import [com.jme3.input MouseInput RawInputListener controls.MouseAxisTrigger]
           [com.jme3.math Vector2f]))

(def mouse-state (atom nil))

(defn handle-right-button-drag []
  )

(defn setup-input-handlers [app rotate-cam]
  (let [cam (.getCamera app)
        center (center cam)]
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
             (let [b (-> (vec2 (.getX event) (.getY event))
                         (.subtract center))
                   a (.subtract b (vec2 (.getDX event) (.getDY event)))]
               (rotate-cam (- (.getAngle a) (.getAngle b))))))
        (onTouchEvent       [event] nil))))))
