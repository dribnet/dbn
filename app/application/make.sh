#!/bin/sh

# -- this is downloadable dbn
# -- no scheme, python or crickets
# -- but full jdk11 and i18n support 
# -- mac: ie and mrj 2.1.x or navigator w/ mrj plugin
# -- win: jdk 1.1.8, navigator (sort of), and IE

rm -f classes/*.class

cd ..
perl buzz.pl "jikes +D -nowarn -d application/classes" -dJDK11 -dEDITOR *.java
cd application

# -- make dbn.jar for the mac
cd classes
rm -f ../lib/dbn.jar
zip -0q ../lib/dbn.jar *.class *.dbn *.gif
cd ..

# -- build dbn.exe from the classes folder
# -- #oved this because the behavior across machines
# -- using jview is so completely erratic.
# cd classes
# jexegen /w /main:DbnApplication /out:../dbn.exe *.class *.dbn
# cd ..






