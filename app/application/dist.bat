@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.

rm -f readme.txt
rm -f dbn.zip

rm -rf dbn
mkdir dbn
cp ../readme.txt dbn/readme.txt
cp -r bin dbn/
cp -r lib dbn/
cp run.bat dbn/
rm -rf dbn/bin/CVS
rm -rf dbn/lib/CVS
rm -rf dbn/lib/security/CVS
zip -rq dbn.zip dbn
rm -rf dbn

rem zip -rq dbn.zip run.bat readme.txt bin\*.dll bin\*.exe lib\*.properties lib\*.jar lib\security\java.security 
rem rm -f readme.txt
