@echo off
sj -nowarn -d classes2 *.java scheme\*.java
cd classes2
rm dbn.jar
zip -r0q dbn.jar *.class *.dbn jscheme
cd ..
