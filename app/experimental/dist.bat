@echo off

rem --- you must first call make.bat
rem --- creates full downloadable dbn for the organic course
rem --- there is no applet version for this stuff (yet)
rem --- this is where most development is being done

rm -f dbn.zip
zip -rq dbn.zip run.bat bin\*.dll bin\*.exe lib\*.properties lib\*.jar lib\security\java.security lib\*.py lib\pawt\*.py

echo Remove dbn.zip when finished.
