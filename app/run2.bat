@echo off
rem jre -nojit -cp classes2 DbnApplication
java -Dpython.home=.\python -classpath %CLASSPATH%;classes2 DbnApplication

