@echo off

rem --- you must first call make.bat
rem --- creates full downloadable dbn for the organic course
rem --- there is no applet version for this stuff (yet)

rem --- unzip jpython.jar into classes folder
unzip -qd classes lib/jpython.jar

rem --- build dbn.exe from the classes folder
cd classes
rem jexegen /r /w /main:DbnApplication /out:..\dbn.exe *.class *.dbn com\* org\*
jexegen /r /main:DbnApplication /out:..\dbn.exe *.class *.dbn com\* org\*
jexegen /r /main:DbnFancy /out:..\fancy.exe *.class *.dbn com\* org\*
cd ..

rem --- build dbn.cab from the classes folder
rem --- ms tools have idiotic syntax for recursion, 
rem --- in case you're wondering what \* is. (whack star?)
cd classes
cabarc -r -p N dbn.cab *.dbn *.class jscheme\*.class org\* com\*
cd ..
rem --- sign dbn.cab with acg certificate
signcode -n "Design By Numbers" -j javasign.dll -jp LOW -spc ..\..\cert\acgcert.spc -v ..\..\cert\acgkey.pvk classes\dbn.cab

rem --- remove any previous build
rm -f dbn.zip
rm -rf dbn
mkdir dbn

rem -- make a copy in a folder called 'dbn'
cp *.exe dbn/

rem -- put buttons and props into a lib directory
mkdir dbn\lib
cp lib/dbn.properties dbn/lib/dbn.properties
cp lib/buttons.gif dbn/lib/buttons.gif
cp lib/*.py dbn/lib/

cp -r ../application/examples dbn/
rm -rf dbn/examples/CVS

rem -- make the zip file and destroy the evidence
zip -rq dbn.zip dbn
rm -rf dbn


rem --- now to make the mac version
rem --- in the main folder: DBN (app), lib (folder), README (mac lfs)
rem --- in the lib folder: buttons.gif, dbn.properties (mac lfs), dbn.jar

mkdir dbn
cp ../application/appl.bin dbn/dbn.bin

rem --- make super-duper dbn.jar for the mac guys
rm -f lib/dbn.jar
cd classes
zip -r0q ../lib/dbn.jar *.dbn *.class jscheme\*.class org com
cd ..

mkdir dbn\lib
cp lib/buttons.gif dbn/lib/buttons.gif
cp lib/dbn.jar dbn/lib/dbn.jar
cp lib/dbn.properties dbn/lib/dbn.properties
java LineFeedConverter mac dbn/lib/dbn.properties

cp -r ../application/examples dbn/
rm -rf dbn/examples/CVS
java LineFeedConverter mac dbn/examples

zip -rq dbn-mac.zip dbn
rm -rf dbn


rem --- clean everything up

rem --- remove the boogers left behind from jpython.jar
rm -rf classes/org
rm -rf classes/com


echo Remove dbn.zip and dbn-mac.zip when finished.
