@echo off

rem --- you must first call make.bat
rem --- creates the downloadable dbn that's ready to go
rem --- onto the website. copies readme from parent dir.

rm -f readme.txt
rm -f dbn.zip

rm -rf dbn
mkdir dbn

rem -- make a copy in a folder called 'dbn'
cp ../readme.txt dbn/readme.txt
cp -r bin dbn/
cp -r lib dbn/
cp run.bat dbn/
cp -r ../application/examples dbn/

rem -- remove all the CVS crap from that dir
rm -rf dbn/bin/CVS
rm -rf dbn/lib/CVS
rm -rf dbn/lib/security/CVS
rm -rf dbn/examples/CVS

rem -- make the zip file and destroy the evidence
zip -rq dbn.zip dbn
rm -rf dbn


rem --- now to make the mac version
rem --- in the main folder: DBN (app), lib (folder), README (mac lfs)
rem --- in the lib folder: buttons.gif, dbn.properties (mac lfs), dbn.jar

rem --- re-make things sort of

unzip -q -d classes comm.mrj.jar
cd classes
rm -f ..\lib\dbn.jar
zip -r0q ..\lib\dbn-mac.jar *.class *.dbn *.gif javax
cd ..

rm -rf classes/javax


mkdir dbn
cp ../readme.txt dbn/README
java LineFeedConverter mac dbn/README
cp ..\application\appl.bin dbn/dbn.bin

mkdir dbn\lib
cp lib/buttons.gif dbn/lib/buttons.gif
mv lib/dbn-mac.jar dbn/lib/dbn.jar

cp lib/dbn.properties dbn/lib/dbn.properties
java LineFeedConverter mac dbn/lib/dbn.properties

cp -r ../application/examples dbn/
rm -rf dbn/examples/CVS
java LineFeedConverter mac dbn/examples

zip -rq dbn-mac.zip dbn
rm -rf dbn
