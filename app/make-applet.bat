@echo off

rem -- this makes the applet to be put on the website
rem -- it has no I18N support and is generally really wimpy
rem -- because of netscape's bad java support
rem -- it is fully and forcefully jdk 1.0 compliant (yuck)

del classes\*.class
del classes\dbn.jar

rem --- to be really strict, but that means that 
rem --- printing goes away, which would be really bad
set CLASSPATH2=%CLASSPATH%
set CLASSPATH=jdk-102.zip

buzz.pl "jikes +1.0 +D -nowarn -d classes" *.java
cd classes
zip -0q dbn.jar *.class *.dbn
cd ..

set CLASSPATH=%CLASSPATH2%
