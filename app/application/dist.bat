@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.

rm readme.txt
rm dbn.zip

cp ..\readme.txt .\readme.txt
zip -rq dbn.zip run.bat readme.txt bin\*.dll bin\*.exe classes\*.class classes\*.dbn lib\*.properties lib\rt.jar lib\security\java.security 
rm readme.txt
