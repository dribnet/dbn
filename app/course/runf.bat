@echo off

jre -nojit -Dpython.cachedir=potato -Dpython.home=. -mx48m -cp lib\dbn.jar;lib\jpython.jar DbnFancy

rem jview /d:python.home=. /cp:p lib\dbn.jar;lib\jpython.jar DbnFancy
