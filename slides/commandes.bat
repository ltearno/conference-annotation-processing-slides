rem pandoc -s -t revealjs annotation-processing.md -o annotation-processing.html --css reveal.js/css/theme/beige.css --slide-level 3

pandoc -s -t revealjs annotation-processing.md -o annotation-processing.html --css reveal.js/css/theme/beige.css --slide-level 3 --no-highlight --variable hlss=zenburn --template reveal-template.html

pandoc -s -t revealjs annotation-processing-light.md -o annotation-processing-light.html --css reveal.js/css/theme/beige.css --slide-level 3 --no-highlight --variable hlss=zenburn --template reveal-template.html