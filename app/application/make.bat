@echo off

rem -- this is downloadable dbn
rem -- no scheme, python or crickets
rem -- but full jdk11 and i18n support 
rem -- mac: ie and mrj 2.1.x or navigator w/ mrj plugin
rem -- win: jdk 1.1.8, navigator (sort of), and IE

rm -f classes\*.class

cd ..
perl buzz.pl "jikes +D -nowarn -d application\classes" -dJDK11 -dEDITOR *.java
cd application

rem -- make dbn.jar for the mac
cd classes
rm -f ..\lib\dbn.jar
zip -0q ..\lib\dbn.jar *.class *.dbn *.gif
cd ..

rem -- build dbn.exe from the classes folder
REM -- removed this because the behavior across machines
REM -- using jview is so completely erratic.
REM cd classes
REM jexegen /w /main:DbnApplication /out:..\dbn.exe *.class *.dbn
REM cd ..






