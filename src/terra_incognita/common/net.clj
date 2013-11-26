(ns terra-incognita.common.net
  (:use [gloss.core]))

(defn aleph-params [& args]
  (merge {:port 13884 :frame (string :utf-8 :delimiters ["\n"])}
         (apply hash-map args)))
