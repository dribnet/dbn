@echo off

rem -- this is downloadable dbn for courses

rem -- cleanup the stuff that changes the most
rm -f classes\*.class 

set ME=course
set CLASSPATH2=%CLASSPATH%
set CLASSPATH=%ME%\lib\jpython.jar;%CLASSPATH%
set OPTIONS=-dJDK11 -dPYTHON -dSCHEME -dFANCY -dEDITOR

cd ..

REM buzz.pl "sj -nowarn -d %ME%\classes" %OPTIONS% *.java scheme\*.java python\*.java

buzz.pl "jikes +D -nowarn -d %ME%\classes" %OPTIONS% *.java scheme\*.java python\*.java

cd %ME%

rem -- only zips up non-jpython.jar stuff
rem -- the rest will get sucked up during the dist process
rm -f lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.dbn *.class jscheme\*.class 
cd ..

set CLASSPATH=%CLASSPATH2%


