@echo off

rem -- this is an attempt at using the kvm for dbn

rm -f classes\*.class
rem rm -f classes\dbn.jar

set ME=kvm

set SAVED_CLASSPATH=%CLASSPATH%
set CLASSPATH=.\%ME%\kvm.zip

cd ..
rem buzz.pl "jikes +1.0 +D -nowarn -d %ME%\classes" -dKVM *.java kvm\*.java
buzz.pl "sj -nowarn -d %ME%\classes" -dKVM *.java kvm\*.java

cd %ME%\classes
rem zip -0q dbn.jar *.class *.dbn
cd ..

set CLASSPATH=%SAVED_CLASSPATH%
