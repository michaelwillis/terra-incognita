(ns terra-incognita.world.core-test
  (:use [clojure.test]
        [terra-incognita.world blocks core]))

(deftest chunk-test
  (testing "filled-chunk should return a byte vector filled with the passed in byte value"
    (let [chunk (filled-chunk water)]
      (is (= (count chunk)
             (* chunk-width chunk-depth chunk-height)))
      (is (= (first chunk) water))
      (is (= (nth chunk 17) water))
      (is (= (last chunk) water)))))

(deftest chunk-index-test
  (testing "chunk local coords should be mappable to chunk index, and vice versa"
    (is (= (coords-to-chunk-index 0 0 0) 0))
    (is (= (coords-to-chunk-index 5 0 0) 5))
    (is (= (coords-to-chunk-index 7 2 0) (+ 7 (* 2 chunk-width))))
    (is (= (coords-to-chunk-index 5 3 2) (+ 5
                                            (* 3 chunk-width)
                                            (* 2 chunk-width chunk-depth))))
    (is (= (chunk-index-to-local-coords 0) [0 0 0]))
    (is (= (chunk-index-to-local-coords 1) [1 0 0]))
        (is (= (chunk-index-to-local-coords
            (coords-to-chunk-index 7 2 0)) [7 2 0]))
    (is (= (chunk-index-to-local-coords
            (coords-to-chunk-index 5 1 1)) [5 1 1]))))

(deftest set-and-get-block
  (testing "Set block get block"
    (let [world (-> empty-world (put-block 2 5 6 dirt))]
      (is (= (get-block world 2 5 6) dirt))
      (is (= (get-block world 2 5 7) air)))))
