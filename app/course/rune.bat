@echo off

jre -nojit -Dpython.cachedir=potato -Dpython.home=. -mx48m -cp classes;lib\dbn.jar;lib\jpython.jar FcmdExhibitionApp
