@echo off
jre -nojit -Dpython.home=. -mx48m -cp lib\dbn.jar;lib\jpython.jar DbnApplication
