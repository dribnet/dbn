@echo off

jview /d:python.home=. /cp:p .\lib;lib\dbn.jar;lib\jpython.jar;lib\qtjava.zip PockyVision

rem .\bin\jre -nojit -Dpython.home=. -mx48m -cp .\lib;lib\dbn.jar;lib\jpython.jar;lib\qtjava.zip PockyVision
