@echo off

rem -- this is dbn for the organic course, with both python and scheme
rem -- requires full jdk11, no ifs ands or butts
rem -- will require applet signing if used in courseware (not yet in use)

rem -- also includes DbnFancy, which is for courseware teacher to do local
rem -- viewing of applets. this is a temporary fix for applet signing
rem -- issues that have been a problem with jpython

rem -- cleanup the stuff that changes the most
rm -f classes\*.class 

set CLASSPATH2=%CLASSPATH%
set CLASSPATH=organic2\lib\jpython.jar;%CLASSPATH%

cd ..
buzz.pl "jikes +D -nowarn -d organic2\classes" -dJDK11 -dPYTHON -dSCHEME -dFANCY *.java scheme\*.java python\*.java
cd organic2

rm -f lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.dbn *.class jscheme\*.class 
cd ..

set CLASSPATH=%CLASSPATH2%

rem cab.bat


