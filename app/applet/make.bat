@echo off

rem -- applet-only version of dbn, supports jdk 1.1 because
rem -- netscape java isn't even worth supporting

rem -- (the stuff below is no longer the case...)
rem -- this makes the applet to be put on the website
rem -- it has no I18N support and is generally really wimpy
rem -- because of netscape's bad java support
rem -- it is fully and forcefully jdk 1.0 compliant (yuck)

rm -f classes\*.class
rm -f classes\dbn.jar

rem --- to be really strict, but that means that 
rem --- printing goes away, which would be really bad
REM set CLASSPATH2=%CLASSPATH%
REM set CLASSPATH=.\applet\jdk-102.zip

cd ..
buzz.pl "jikes +1.0 +D -nowarn -d applet\classes" -dEDITOR -dJDK11 *.java
cd applet\classes
zip -0q dbn.jar *.class *.dbn
cd ..

REM set CLASSPATH=%CLASSPATH2%
