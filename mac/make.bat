@echo off
jikes -classpath %CLASSPATH%;shared.jar +D *.java
