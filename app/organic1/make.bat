@echo off

rem -- this is dbn for the organic course
rem -- includes scheme but not python
rem -- this is for applet/courseware only, not downloadable
rem -- requires jdk 1.1 compliant browser

rem del classes\*.class 
rem del classes\dbn.jar

cd ..
buzz.pl "jikes +D -nowarn -d organic1\classes" -dJDK11 -dSCHEME *.java scheme\*.java
cd organic1

rem cd classes
rem zip -0q dbn.jar *.class *.dbn jscheme\*.class
rem cd ..






