@echo off
REM .\bin\jre -nojit -mx48m -cp lib\dbn.jar DbnApplication
REM jview /vst /cp:p classes DbnApplication
jre -v -verbosegc -nojit -cp lib\dbn.jar DbnApplication
