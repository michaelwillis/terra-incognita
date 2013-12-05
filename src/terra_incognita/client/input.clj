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
             ;; calc movement relative to screen center
             (let [x2 (- (.getX event) (.getX center))
                   y2 (- (.getY event) (.getY center))
                   x1 (- x2 (.getDX event))
                   y1 (- y2 (.getDY event))
                   spin (- (.getAngle (vec2 x2 y1))
                           (.getAngle (vec2 x1 y1)))
                   tilt (->> (.getY (resolution cam))
                             (/ (.getDY event))
                             (* Math/PI))]
               (rotate-cam tilt spin))))
        (onTouchEvent       [event] nil))))))
