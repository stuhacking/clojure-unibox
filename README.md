Clojure-unibox
==============

Small Clojure library for mapping line descriptions to unicode box drawing characters.

To use this library, add the following dependency to your project.clj:

    [unibox "0.1.0-SNAPSHOT"]

Unibox exposes a single function which will return a unicode char matching the line description given as parameters.  If a matching char cannot be found, nil will be returned.

E.g.
    (box-char :north :single, :south :single, :west :single) => ┤

Parameters should be given as a mapping of the cardinal edges of the line section:

            :north
               |
       :west  -+-  :east
               |
            :south

and the style of the line leaving that edge: One of [:single, :wide,:double].

Line styles can be mixed between :single and :wide, or :single and :double, but there are no combinations containing :wide and :double.
