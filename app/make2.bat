@echo off
rem jikes -nowarn -d classes2 *.java scheme\*.java
jikes -classpath %classpath%;classes2 -nowarn -d classes2 *.java scheme\*.java
cd classes2
rm dbn.jar
zip -r0q dbn.jar *.class *.dbn jscheme
cd ..
