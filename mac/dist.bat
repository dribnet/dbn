@echo off

rem --- first copies all the class info from organic2
rem --- makes a zip file, then sticks it in the macbinary stuff

echo Creating dbn.bin from dbn-template.bin and class folder

cp -r ../app/organic2/classes ./$VFS
zip -r0q dbn-classes.zip $VFS
rm -rf $VFS

jre -cp shared.jar -nojit Appendage dbn-template.bin dbn-classes.zip dbn.bin

rm -f dbn-classes.zip

