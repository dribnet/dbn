@echo off

rem --- you must first call make.bat
rem --- wraps up the fiendish experimental dbn

rm -f dbn.zip
zip -rq dbn.zip run.bat run95.bat bin\*.dll bin\*.exe lib\*.gif lib\*.zip lib\player\*.class lib\*.properties lib\*.jar lib\security\java.security lib\*.py lib\pawt\*.py

rm -rf experimental
mkdir experimental
unzip -d experimental dbn.zip
rm dbn.zip
zip -rq dbn.zip experimental
rm -rf experimental

echo Remove dbn.zip when finished.
