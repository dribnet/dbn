@echo off
.\bin\jre -Dpython.cachedir=potato -Dpython.home=. -mx48m -cp lib\dbn.jar;lib\jpython.jar DbnApplication
