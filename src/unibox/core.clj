;;;; core.clj --- Unicode Box Drawing.

;;; Small helper library for converting descriptions
;;; of line-sections into corresponding unicode characters
;;; for box drawing.
;;;
;;; Each box drawing char has a possible connector at the
;;; [:north, :west, :south, :east] edge. The connector may be
;;; of [:plain, :wide, :double].
;;;
;;; Plain lines may be mixed with either :wide or :double lines,
;;; but :wide and :double never appear in the same character.

;;; Code:
(ns unibox.core
  (:use [clojure.pprint :refer [print-table]]))

(def ^:private character-map "A mapping of line properties to characters."
  {;; Single Lines
   {:north :single}                                               \u2575,
   {:south :single}                                               \u2577,
   {:west :single}                                                \u2574,
   {:east :single}                                                \u2576,
   {:west  :single, :east :single}                                \u2500,
   {:north :single, :south :single}                               \u2502,
   {:north :single, :west :single}                                \u2518,
   {:north :single, :east :single}                                \u2514,
   {:south :single, :west :single}                                \u2510,
   {:south :single, :east :single}                                \u250c,
   {:north :single, :south :single, :east :single}                \u251c,
   {:north :single, :south :single, :west :single}                \u2524,
   {:north :single, :west :single, :east :single}                 \u2534,
   {:south :single, :west :single, :east :single}                 \u252c,
   {:north :single, :south :single, :west :single, :east :single} \u253c,

   ;; Wide Lines
   {:north :wide}                                                 \u2579,
   {:south :wide}                                                 \u257b,
   {:west :wide}                                                  \u2578,
   {:east :wide}                                                  \u257a,
   {:west :wide, :east :wide}                                     \u2501,
   {:north :wide, :south :wide}                                   \u2503,
   {:north :wide, :west :wide}                                    \u251b,
   {:north :wide, :east :wide}                                    \u2517,
   {:south :wide, :west :wide}                                    \u2513,
   {:south :wide, :east :wide}                                    \u250f,
   {:north :wide, :south :wide, :east :wide}                      \u2523,
   {:north :wide, :south :wide, :west :wide}                      \u252b,
   {:north :wide, :west :wide, :east :wide}                       \u253b,
   {:south :wide, :west :wide, :east :wide}                       \u2533,
   {:north :wide, :south :wide, :west :wide, :east :wide}         \u254b,
   
   ;; Double Lines
   {:west :double, :east :double}                                 \u2550,
   {:north :double, :south :double}                               \u2551,
   {:north :double, :west :double}                                \u255d,
   {:north :double, :east :double}                                \u255a,
   {:south :double, :west :double}                                \u2557,
   {:south :double, :east :double}                                \u2554,
   {:north :double, :south :double, :east :double}                \u2560,
   {:north :double, :south :double, :west :double}                \u2563,
   {:north :double, :west :double, :east :double}                 \u2569,
   {:south :double, :west :double, :east :double}                 \u2566,
   {:north :double, :south :double, :west :double, :east :double} \u256c,
   
   ;; Mixed Lines - single / wide
   {:west :single, :east :wide}                                   \u257c,
   {:west :wide, :east :single}                                   \u257e,
   {:north :single, :south :wide}                                 \u257d,
   {:north :wide, :south :single}                                 \u257f,
   {:north :single, :west :wide}                                  \u2519,
   {:north :wide, :west :single}                                  \u251a,
   {:north :single, :east :wide}                                  \u2515,
   {:north :wide, :east :single}                                  \u2516,
   {:south :single, :west :wide}                                  \u2511,
   {:south :wide, :west :single}                                  \u2512,
   {:south :single, :east :wide}                                  \u250d,
   {:south :wide, :east :single}                                  \u250e,
   {:north :single, :south :single, :east :wide}                  \u251d,
   {:north :single, :south :wide, :east :single}                  \u251f,
   {:north :single, :south :wide, :east :wide}                    \u2522,
   {:north :wide, :south :single, :east :single}                  \u251e,
   {:north :wide, :south :single, :east :wide}                    \u2521,
   {:north :wide, :south :wide, :east :single}                    \u2520,
   {:north :single, :south :single, :west :wide}                  \u2525,
   {:north :single, :south :wide, :west :single}                  \u2527,
   {:north :single, :south :wide, :west :wide}                    \u252a,
   {:north :wide, :south :single, :west :single}                  \u2526,
   {:north :wide, :south :single, :west :wide}                    \u2529,
   {:north :wide, :south :wide, :west :single}                    \u2528,
   {:north :single, :west :single, :east :wide}                   \u2536,
   {:north :single, :west :wide, :east :single}                   \u2535,
   {:north :single, :west :wide, :east :wide}                     \u2537,
   {:north :wide, :west :single, :east :single}                   \u2538,
   {:north :wide, :west :single, :east :wide}                     \u253a,
   {:north :wide, :west :wide, :east :single}                     \u2539,
   {:south :single, :west :single, :east :wide}                   \u252e,
   {:south :single, :west :wide, :east :single}                   \u252d,
   {:south :single, :west :wide, :east :wide}                     \u252f,
   {:south :wide, :west :single, :east :single}                   \u2530,
   {:south :wide, :west :single, :east :wide}                     \u2532,
   {:south :wide, :west :wide, :east :single}                     \u2531,
   {:north :single, :south :single, :west :single, :east :wide}   \u253e,
   {:north :single, :south :single, :west :wide, :east :single}   \u253d,
   {:north :single, :south :single, :west :wide, :east :wide}     \u253f,
   {:north :single, :south :wide, :west :single, :east :single}   \u2541,
   {:north :single, :south :wide, :west :single, :east :wide}     \u2546,
   {:north :single, :south :wide, :west :wide, :east :single}     \u2545,
   {:north :single, :south :wide, :west :wide, :east :wide}       \u2548,
   {:north :wide, :south :single, :west :single, :east :single}   \u2540,
   {:north :wide, :south :single, :west :single, :east :wide}     \u2544,
   {:north :wide, :south :single, :west :wide, :east :single}     \u2543,
   {:north :wide, :south :single, :west :wide, :east :wide}       \u2547,
   {:north :wide, :south :wide, :west :single, :east :single}     \u2542,
   {:north :wide, :south :wide, :west :single, :east :wide}       \u254a,
   {:north :wide, :south :wide, :west :wide, :east :single}       \u2549,
   
   ;; Mixed Lines - single / double
   {:north :single :east :double}                                 \u2558,
   {:north :single :west :double}                                 \u255b,
   {:south :single :east :double}                                 \u2552,
   {:south :single :west :double}                                 \u2555,
   {:north :double :east :single}                                 \u2559,
   {:north :double :west :single}                                 \u255c,
   {:south :double :east :single}                                 \u2553,
   {:south :double :west :single}                                 \u2556,
   {:north :single :south :single :east :double}                  \u255e,
   {:north :single :south :single :west :double}                  \u2561,
   {:north :double :south :double :east :single}                  \u255f,
   {:north :double :south :double :west :single}                  \u2562,
   {:north :single :west :double :east :double}                   \u2567,
   {:south :single :west :double :east :double}                   \u2564,
   {:north :double :west :single :east :single}                   \u2568,
   {:south :double :west :single :east :single}                   \u2565,
   {:north :double :south :double :west :single :east :single}    \u256b,
   {:north :single :south :single :west :double :east :double}    \u256a
  })

(defn box-char
  "Return a unicode char matching the line description given by opts.
  If a matching char cannot be found, nil will be returned.

  Opts should be given as a mapping of the cardinal edges of the line
  section:

            :north
               |
       :west  -+-  :east
               |
            :south

  and the style of the line leaving that edge: [:single, :wide, :double].
  E.g: (box-char :north :single, :south :single, :west :single) => ┤

  Line styles can be mixed between :single and :wide, or :single and :double,
  but there are no combinations containing :wide and :double."
  [& opts]
  (character-map (apply hash-map opts)))

(defn print-char-map
  "Print a table of each line description and the unicode char it produces."
  []
  (let [table (map #(assoc (% 0) :char (% 1)) (sort #(compare (count (%1 0)) (count (%2 0))) (seq character-map)))]
    (print-table [:north :south :west :east :char] table)))

;;;; unibox.clj ends here.
