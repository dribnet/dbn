@echo off

rem --- you must first call getpy.bat and make.bat
rem --- creates full downloadable dbn for the organic course

rm ..\lib\dbn.jar
cd classes
zip -r0q ..\lib\dbn.jar *.class *.dbn jscheme\*.class 
cd ..

rm dbn.zip

zip -rq dbn.zip run.bat bin\*.dll bin\*.exe classes\*.class classes\*.dbn lib\*.properties lib\*.jar lib\security\java.security 
