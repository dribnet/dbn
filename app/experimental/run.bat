@echo off
jre -Dpython.home=. -mx48m -cp .\lib;lib\dbn.jar;lib\jpython.jar DbnApplication
