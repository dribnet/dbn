@echo off
cd classes2
cabarc -r -p N dbn.cab *.class *.dbn jscheme\*.class org\python\compiler\*.class org\python\core\*.class org\python\modules\*.class org\python\parser\*.class org\python\rmi\*.class org\python\util\*.class
cd ..
signcode -j javasign.dll -jp LOW -spc acgcert.spc -v acgkey.pvk classes2\dbn.cab
