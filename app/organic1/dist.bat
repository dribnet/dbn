@echo off

rem -- creates the 'dbn' folder to be copied
rem -- to the courseware site

rm -rf dbn
cp -r classes dbn
cd dbn
rm -rf CVS
cd jscheme
rm -rf CVS
cd ..\..

echo Delete the 'dbn' folder when finished.
