@echo off
sj -nowarn -d classes *.java
cd classes
rm dbn.jar
zip -r0q dbn.jar *.class *.dbn
rem zip -rq dbn.jar *.class *.dbn
cd ..
