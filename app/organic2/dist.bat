@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn for the organic course

rm dbn.zip

zip -rq dbn.zip run.bat bin\*.dll bin\*.exe classes\*.class classes\*.dbn lib\*.properties lib\rt.jar lib\security\java.security 
