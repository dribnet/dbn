@echo off
sj -classpath classes;%classpath% -d classes *.java engine\DbnProcessorG3.java engine\codeBlock.java parser\Yylex.java engine\ParserHandler.java parser\sym.java parser\parser.java parser\java_cup\runtime\virtual_parse_stack.java parser\java_cup\symbol.java parser\java_cup\runtime\lr_parser.java parser\java_cup\runtime\Symbol.java

rem -- use this to compile to a jar file
rem -- only use the 'zip' or 'jar' line, not both
rem cd classes
rem zip -r0q ..\classes.zip *
rem jar cvf ..\dbn.jar *
rem cd ..



