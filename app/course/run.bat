@echo off

rem .\bin\jre -Dpython.cachedir=potato -Dpython.home=. -mx48m -cp lib\dbn.jar;lib\jpython.jar DbnApplication

jview /d:python.home=. /cp:p lib\dbn.jar;lib\jpython.jar DbnApplication
