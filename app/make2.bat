@echo off

REM yes scheme, yes python, no cricket, yes keylisteners
jikes +D -classpath %classpath%;classes2 -nowarn -d classes2 *.java scheme\*.java python\*.java

REM yes scheme, no python, yes cricket, yes keylisteners
REM jikes +D -classpath %classpath%;classes2 -nowarn -d classes2 *.java scheme\*.java cricket\*.java

cd classes2
rm dbn.jar
zip -r0q dbn.jar *.class *.dbn jscheme org com
REM zip -r0q dbn.jar *.class *.dbn jscheme
cd ..
