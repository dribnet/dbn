@echo off

rem -- this is similar to downloadable/courseware dbn
rem -- except that it also includes cricket support
rem -- java comm files are stored in the lib and bin dirs

rem -- mac: ie and mrj 2.1.2 or navigator w/ mrj plugin
rem -- win: jdk 1.1.8, navigator (sort of), and IE

rm -f classes\*.class

cd ..
buzz.pl "jikes +D -nowarn -d application\classes" -dJDK11 -dEDITOR -dCRICKET *.java cricket\*.java
cd cricket

cd classes
rm -f ..\lib\dbn.jar
zip -0q ..\lib\dbn.jar *.class *.dbn *.gif
cd ..






