@echo off

rem -- this makes the applet to be put on the website
rem -- it has no I18N support and is generally really wimpy
rem -- because of netscape's bad java support
rem -- it is fully and forcefully jdk 1.0 compliant (yuck)

rm classes\*.class
rm classes\dbn.jar

rem --- to be really strict, but that means that 
rem --- printing goes away, which would be really bad
set CLASSPATH2=%CLASSPATH%
set CLASSPATH=.\applet\jdk-102.zip

cd ..
buzz.pl "jikes +1.0 +D -nowarn -d applet\classes" *.java
cd applet\classes
zip -0q dbn.jar *.class *.dbn
cd ..

set CLASSPATH=%CLASSPATH2%
