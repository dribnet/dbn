@echo off
sj -nowarn -d classes *.java
rem jikes -nowarn -d classes *.java
rem javac -nowarn -d classes *.java
cd classes
rm dbn.jar
zip -r0q dbn.jar *.class *.dbn
cd ..
