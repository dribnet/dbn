@echo off

rem -- this is ben's experimental dbn

rem -- cleanup the stuff that changes the most
rm -f classes/*.class 

set ME=experimental
set CLASSPATH2=%CLASSPATH%
set CLASSPATH=%ME%\lib\jpython.jar;%ME%\lib\QTJava.zip;%CLASSPATH%
set OPTIONS=-dJDK11 -dPLAYER_CLASS -dEDITOR -dPYTHON -dSCHEME -dCONVERTER -dRECORDER 

rem GRAPHICS2 has been removed
rem -dJAVAC -dFANCY

cd ..

buzz.pl "jikes +D -nowarn -d %ME%\classes" %OPTIONS% *.java python\*.java %ME%\*.java course\*.java scheme\*.java

cd %ME%

rm -f lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.dbn *.class jscheme\*.class 
cd ..

set CLASSPATH=%CLASSPATH2%

rem cab.bat


