@echo off
rem jre -nojit -cp classes2 DbnApplication
java -Dpython.home=. -classpath %CLASSPATH%;classes2 DbnApplication

