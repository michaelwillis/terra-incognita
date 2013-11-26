(ns terra-incognita.server
  (:require [clojure.edn :as edn]
            [aleph.tcp :refer :all]
            [gloss.core :as gloss]
            [lamina.core :as lamina]
            [terra-incognita.common.net :refer :all]))

(defn handler [ch client-info]
  (lamina/receive-all
   ch #(let [message (edn/read-string %)]
         (lamina/enqueue ch (str (merge message {:client client-info}))))))

(defn start-server []
  (start-tcp-server handler (aleph-params)))

(start-server)
