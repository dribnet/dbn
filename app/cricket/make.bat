@echo off

rem -- this is the same as downloadable/courseware dbn
rem -- except that it also includes cricket support
rem -- java comm files are stored in the lib and bin dirs

rem -- mac: ie and mrj 2.1.2 or navigator w/ mrj plugin
rem -- win: jdk 1.1.8, navigator (sort of), and IE

del classes\*.class
del classes\dbn.jar

cd ..
buzz.pl "jikes +D -nowarn -d cricket\classes" -dJDK11 -dCRICKET *.java cricket\*.java

rem -- cricket dbn requires downloadable, so jar file not used
rem cd cricket\classes
rem zip -0q dbn.jar *.class *.dbn
rem cd ..






