@echo off

cd classes
rm dbn.cab

cabarc -r -p N dbn.cab *.dbn *.class jscheme\*.class org\python\compiler\*.class org\python\core\*.class org\python\modules\*.class org\python\parser\*.class org\python\rmi\*.class org\python\util\*.class

cd ..

signcode -n "Design By Numbers" -j javasign.dll -jp LOW -spc ..\..\cert\acgcert.spc -v ..\..\cert\acgkey.pvk classes\dbn.cab

