@echo off
cd classes2
cabarc -r -p N dbn.cab *.class *.dbn jscheme\*.class org\python\compiler\*.class org\python\core\*.class org\python\modules\*.class org\python\parser\*.class org\python\rmi\*.class org\python\util\*.class
cd ..
rem signcode -j javasign.dll -jp LOW -spc ..\cert\acgcert.spc -v ..\cert\acgkey.pvk classes2\dbn.cab
signcode -n "Design By Numbers" -j javasign.dll -jp LOW -spc ..\cert\acgcert.spc -v ..\cert\acgkey.pvk classes2\dbn.cab
