@echo off

rem -- this is dbn for the organic course, with both python and scheme
rem -- requires jdk11, applet signing if used in courseware

rem del classes\*.class 
rem del classes\dbn.jar

cd ..
buzz.pl "jikes +D -nowarn -d organic\classes" -dJDK11 -dPYTHON -dSCHEME *.java
cd organic

rem cd classes
rem zip -r0q dbn.jar *.class *.dbn com org jscheme
rem cd ..






