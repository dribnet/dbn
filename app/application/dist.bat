@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.

rm -f readme.txt
rm -f dbn.zip

cp ..\readme.txt .\readme.txt
zip -rq dbn.zip run.bat readme.txt bin\*.dll bin\*.exe lib\*.properties lib\*.jar lib\security\java.security 
rm -f readme.txt
