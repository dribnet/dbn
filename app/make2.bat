@echo off

jikes +D -classpath %classpath%;classes2 -nowarn -d classes2 *.java scheme\*.java cricket\*.java

cd classes2
rm dbn.jar
rem zip -r0q dbn.jar *.class *.dbn jscheme org com
zip -r0q dbn.jar *.class *.dbn jscheme
cd ..
