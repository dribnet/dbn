@echo off

unzip -q -d classes lib\jpython.jar

cd classes
rm -f dbn.cab

cabarc -r -p N dbn.cab *.dbn *.class jscheme\*.class org\python\compiler\*.class org\python\core\*.class org\python\modules\*.class org\python\parser\*.class org\python\rmi\*.class org\python\util\*.class

rem -- remove the crap extracted from jpython.jar
rm -rf org
rm -rf com

cd ..

signcode -n "Design By Numbers" -j javasign.dll -jp LOW -spc ..\..\cert\acgcert.spc -v ..\..\cert\acgkey.pvk classes\dbn.cab

