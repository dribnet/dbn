@echo off

rem -- this is ben's experimental dbn

rem -- cleanup the stuff that changes the most
rm -f classes\*.class 

set ME=organic3
set CLASSPATH2=%CLASSPATH%
set CLASSPATH=%ME%\lib\jpython.jar;%CLASSPATH%
set OPTIONS=-dJDK11 -dPYTHON -dJAVAC -dPLAYER_CLASS -dFANCY -dEDITOR -dRECORDER

cd ..

buzz.pl "jikes +D -nowarn -d %ME%\classes" %OPTIONS% *.java python\*.java javac\*.java

cd %ME%

rm -f lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.dbn *.class jscheme\*.class 
cd ..

set CLASSPATH=%CLASSPATH2%

rem cab.bat


