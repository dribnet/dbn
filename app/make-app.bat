@echo off

rem -- this is downloadable and courseware dbn
rem -- no scheme, python or crickets
rem -- but full jdk11 and i18n support 
rem -- mac: ie and mrj 2.1.2 or navigator w/ mrj plugin
rem -- win: jdk 1.1.8, navigator (sort of), and IE

del classes\*.class
del classes\dbn.jar

buzz.pl "jikes +D -nowarn -d applet\classes" -dJDK11 *.java
cd classes
zip -0q dbn.jar *.class *.dbn
cd ..
