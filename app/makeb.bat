@echo off
rem buzz.pl "jikes +1.0 +D -nowarn -d classes2" *.java python\*.java scheme\*.java
buzz.pl "jikes +D -dJDK11 -nowarn -d classes2" *.java python\*.java scheme\*.java
