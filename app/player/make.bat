@echo off

rem -- this builds the player applet to be included
rem -- with compiled, standalone, dbn apps

rm -f classes\*.class 

cd ..
buzz.pl "jikes +D -nowarn -d player\classes" -dJDK11 -dPLAYER -dPLAYER_CLASS DbnApplet.java DbnGraphics.java DbnException.java player\*.java 
cd player

rem rm -f lib\dbn.jar
cd classes
zip -q player.jar *.class
cd ..


