@echo off

rem --- you must first call msmake.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.

rm -f readme.txt
rm -f dbn.zip

rm -rf dbn
mkdir dbn

rem -- make a copy in a folder called 'dbn'
cp DBN.exe dbn/
cp ../readme.txt dbn/readme.txt
cp -r examples dbn/

rem -- put buttons and props into a lib directory
mkdir dbn\lib
cp lib/dbn.properties dbn/lib/dbn.properties
cp lib/buttons.gif dbn/lib/buttons.gif

rem -- remove all the CVS crap from that dir
rm -rf dbn/examples/CVS

rem -- make the zip file and destroy the evidence
zip -rq dbn.zip dbn
rm -rf dbn
