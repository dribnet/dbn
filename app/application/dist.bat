@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.


rm -f readme.txt
rm -f dbn.zip

rm -rf dbn
mkdir dbn

rem -- make a copy in a folder called 'dbn'
REM cp DBN.exe dbn/
cp run.bat dbn/dbn.bat
cp run95.bat dbn/dbn95.bat
cp ../readme.txt dbn/readme.txt

rem -- get the examples dir
cp -r examples dbn/
rm -rf dbn/examples/CVS

rem -- put buttons and props into a lib directory
REM mkdir dbn\lib
rem cp lib/dbn.properties dbn/lib/dbn.properties
rem cp lib/buttons.gif dbn/lib/buttons.gif

rem -- copy lib dir
cp -r lib dbn/
rm -rf dbn/lib/CVS
rm -rf dbn/lib/security/CVS

rem -- copy bin dir
cp -r bin dbn/
rm -rf dbn/bin/CVS

rem -- make the zip file and destroy the evidence
zip -rq dbn.zip dbn
rm -rf dbn


rem --- now to make the mac version
rem --- in the main folder: DBN (app), lib (folder), README (mac lfs)
rem --- in the lib folder: buttons.gif, dbn.properties (mac lfs), dbn.jar

mkdir dbn
cp ../readme.txt dbn/README
maclf dbn/README
cp appl.bin dbn/dbn.bin

mkdir dbn\lib
cp lib/buttons.gif dbn/lib/buttons.gif
cp lib/dbn.jar dbn/lib/dbn.jar
cp lib/dbn.properties dbn/lib/dbn.properties
maclf dbn/lib/dbn.properties

cp -r examples dbn/
rm -rf dbn/examples/CVS
maclf dbn/examples

zip -rq dbn-mac.zip dbn
rm -rf dbn
