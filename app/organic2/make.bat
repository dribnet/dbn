@echo off

rem -- this is dbn for the organic course, with both python and scheme
rem -- requires full jdk11, no ifs ands or butts
rem -- will require applet signing if used in courseware (not yet in use)

rem -- also include DbnFancy, which is for courseware teacher to do local
rem -- viewing of applets. this is a temporary fix for applet signing
rem -- issues that have been a problem.

rem cleanup the stuff that changes the most
del classes\*.class 

set CLASSPATH2=%CLASSPATH%
set CLASSPATH=organic2\lib\python.jar;%CLASSPATH%

cd ..
buzz.pl "jikes +D -nowarn -d organic2\classes" -dJDK11 -dPYTHON -dSCHEME -dFANCY *.java
cd organic2

set CLASSPATH=%CLASSPATH2%





