@echo off

.\bin\jre -nojit -Dpython.home=. -mx48m -cp .\lib;lib\dbn.jar;lib\jpython.jar;lib\qtjava.zip Experimental

rem d:\jdk-1.1.8\bin\java -nojit -Dpython.home=. -mx48m -classpath %CLASSPATH%;.\lib;lib\dbn.jar;lib\jpython.jar;lib\qtjava.zip DbnApplication
