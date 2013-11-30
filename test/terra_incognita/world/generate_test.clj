(ns terra-incognita.world.generate-test
  (:use [clojure.test]
        [terra-incognita.world blocks core generate]))

(deftest test-ocean-world
  (testing "ocean-world creates a world with a bottom layer of water-filled chunks"
    (let [world (ocean-world 256)]
      (is (= (-> world :chunks count)
             (* (/ 256 chunk-width)
                (/ 256 chunk-depth))))
      (is (= (get-block world 0 0 0) water))
      (is (= (get-block world 128 128 (/ chunk-height 2)) water))      
      (is (= (get-block world 255 255 (dec chunk-height)) water)))))

