@echo off

rem -- this is dbn for the organic course
rem -- includes scheme but not python
rem -- this is not downloadable, but applet/browser only
rem -- requires jdk 1.1 compliant browser

rem del classes\*.class 
rem del classes\dbn.jar

cd ..
buzz.pl "jikes +D -nowarn -d organic1\dbn" -dJDK11 -dSCHEME *.java scheme\*.java
cd organic1

rem cd dbn
rem zip -0q dbn.jar *.class *.dbn jscheme\*.class
rem cd ..






