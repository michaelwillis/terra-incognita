(ns terra-incognita.client
  (:use [lamina.core] [aleph.tcp] [gloss.core] [terra-incognita.common.net]))

(def ch (-> (aleph-params :host "localhost") tcp-client wait-for-result))
(enqueue ch (str {:message "Hello, Server!"}))
(prn (wait-for-message ch))
