@echo off

rem --- you must first call make.bat
rem --- creates a copy of cricket dbn distro as a zip file

rm -f readme.txt
rm -f dbn.zip

cp ..\readme.txt .\readme.txt
zip -rq dbn.zip run.bat readme.txt bin\*.dll bin\*.exe lib\*.properties lib\*.jar lib\security\java.security 
rm readme.txt
