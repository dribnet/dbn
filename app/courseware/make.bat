@echo off

rem -- this is for applet/courseware only, not downloadable
rem -- originally for the organic course, includes scheme but not python
rem -- this version may go away if we can figure out security issues

rem -- ** requires jdk 1.1 compliant browser **
rem -- windows nav 4.x -- mostly works
rem -- windows ie 4.x -- fully works
rem -- mac nav 4.x -- does not work
rem -- mac ie 4.x -- works with mrj

rm -f classes\*.class 
rm -f classes\dbn.jar

set ME=courseware

cd ..
buzz.pl "jikes +D -nowarn -d courseware\classes" -dEDITOR -dJDK11 -dSCHEME *.java scheme\*.java
cd %ME%

cd classes
zip -0q dbn.jar *.class *.dbn jscheme\*.class
cd ..






