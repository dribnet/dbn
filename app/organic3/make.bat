@echo off

rem -- this is dbn for the organic course exhibition

rem -- cleanup the stuff that changes the most
rm -f classes\*.class 

set CLASSPATH2=%CLASSPATH%
set CLASSPATH=organic3\lib\jpython.jar;%CLASSPATH%

cd ..
rem buzz.pl "sj -nowarn -d organic3\classes" -dJDK11 -dPYTHON -dSCHEME *.java scheme\*.java python\*.java
buzz.pl "jikes +D -nowarn -d organic3\classes" -dJDK11 -dPYTHON -dSCHEME -dCONVERTER *.java scheme\*.java python\*.java
cd organic3

rm -f lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.dbn *.class jscheme\*.class 
cd ..

set CLASSPATH=%CLASSPATH2%

rem cab.bat


