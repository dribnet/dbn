@echo off

rem -- this is the same as downloadable/courseware dbn
rem -- except that it also includes cricket support
rem -- java comm files are stored in the lib and bin dirs

rem -- mac: ie and mrj 2.1.2 or navigator w/ mrj plugin
rem -- win: jdk 1.1.8, navigator (sort of), and IE

del classes\*.class

cd ..
buzz.pl "jikes +D -nowarn -d cricket\classes" -dJDK11 -dCRICKET *.java cricket\*.java
cd cricket






