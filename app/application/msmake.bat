@echo off
cd classes
jexegen /w /main:DbnApplication /out:..\dbn.exe *.class *.dbn
cd ..
