@echo off
jre -nojit -Dpython.home=. -mx48m -cp .\lib;lib\dbn.jar;lib\jpython.jar DbnApplication
