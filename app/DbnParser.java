import java.io.*;
import java.util.*;


public class DbnParser {
  // used while the parser is actually running
  char p[];
  int index;
  int line;

    // root of the parsed tree
  DbnToken root;
  // used for command def to keep track of local vars
  DbnToken currentDef;
  // use for repeat to keep track of local vars
  //DbnToken currentBlock;

  // used by consumeWord as temp storage
  int tempIndex;
  char temp[] = new char[256];

  /*
    static public void main(String args[]) {
	if (args.length == 0) {
	    System.err.println("usage: java DbnParser <filename>");
	    System.exit(0);
	}
	try {
	    File file = new File(args[0]);
	    int length = (int) file.length();
	    FileInputStream input = new FileInputStream(file);
	    byte bytes[] = readBytes(input, 0, length);
	    char data[] = new char[length];
	    for (int i = 0; i < length; i++) {
		data[i] = (char)bytes[i];
	    }
	    DbnParser p = new DbnParser(data);
	    //p.root.print();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
  */
    
  DbnParser(char data[]) throws DbnException {
    line = 0;  // NOTE changed to zero, b/c that's what gui wants
    p = data;
    index = 0;
    root = new DbnToken(DbnToken.ROOT, line);

    root.functions = new Hashtable();
    root.functions.put("command", new Object());
    root.functions.put("number", new Object());
    root.functions.put("value", new Object());
    root.functions.put("notsame?", new Object());
    root.functions.put("notsmaller?", new Object());
    root.functions.put("forever", new Object());
    root.functions.put("line", new Object());
    root.functions.put("paper", new Object());
    root.functions.put("pen", new Object());
    root.functions.put("repeat", new Object());
    root.functions.put("same?", new Object());
    root.functions.put("set", new Object());
    root.functions.put("smaller?", new Object());
	
    parseStatements(root);

    // see if there are leftovers, if so, then it's an error
    while (index != p.length) {
#ifdef KVM
      if (!KvmLacunae.isSpace(p[index])) {
	die("not a valid statement");
      }
#else
      if (!Character.isSpace(p[index])) {
	die("not a valid statement");
      }
#endif
    }
    //root.convert();
    //root.print();
    //System.out.println("over here");
  }
	
    
  public DbnToken getRoot() {
    return root;
  }
    
    
  boolean parseStatements(DbnToken parent) throws DbnException {
    DbnToken current = parent.addChild(DbnToken.STATEMENTS, line);
    while (parseStatement(current)) { }
    return true;
  }


  boolean parseStatement(DbnToken parent) throws DbnException {
    //DbnToken current = parent.addChild(STATEMENT);
	
    //System.out.println("parsing statement");
    consumeSpaces();
    //if (parseEOL(current)) return true;

    if (parseEOL()) return true;
    if (parseBlock(parent)) return true;

    String word = consumeWord();
    if ((word == null) || (word.length() == 0)) return false;
	
    DbnToken current = parent.addChild(DbnToken.STATEMENT, line);
	
    switch (word.charAt(0)) {
    case 'a':
      if (word.equals("antialias"))
	return parseAntiAlias(current);
      break;
    case 'c':
      if (word.equals("command"))
	return parseFunctionDef(current, false);
      break;
    case 'n':
      if (word.equals("notsame?")) 
	return parseComparison(current, DbnToken.NOT_SAME);
      if (word.equals("notsmaller?")) 
	return parseComparison(current, DbnToken.NOT_SMALLER);
      if (word.equals("number")) 
	return parseFunctionDef(current, true);
      if (word.equals("norefresh")) 
	return parseNoRefresh(current);
      break;
    case 'f':
      if (word.equals("field")) return parseField(current);
      if (word.equals("forever")) return parseForever(current); 
      break;
    case 'l':
      if (word.equals("line")) return parseLine(current); 
      break;
    case 'p':
      if (word.equals("paper")) return parsePaper(current);
      else if (word.equals("pause")) return parsePause(current);
      else if (word.equals("pen")) return parsePen(current);
      break;
    case 'r':
      if (word.equals("repeat")) return parseRepeat(current); 
      else if (word.equals("refresh")) return parseRefresh(current);
      break;
    case 's':
      if (word.equals("set")) 
	return parseSet(current);
      if (word.equals("same?")) 
	return parseComparison(current, DbnToken.SAME);
      if (word.equals("smaller?")) 
	return parseComparison(current, DbnToken.SMALLER);
      break;
    case 'v':
      if (word.equals("value")) return parseReturnValue(current);
      break;
    }
    if (parseCommand(current, word)) {
      // usage of some user-defined command
      return true;
    }
    die("expecting a statement");
    return false;
  }


  boolean parseFunctionDef(DbnToken parent, 
			   boolean hasReturnValue) throws DbnException {
    String title = consumeWord();
    if (root.functions.contains(title)) {
      die("command " + title + " already defined");
    }
    int kind = hasReturnValue ? 
      DbnToken.FUNCTION_DEF : DbnToken.COMMAND_DEF;
    DbnToken current = parent.addChild(kind, title, line);
    currentDef = current;
    int paramCount = 0;
    while (!parseEOL()) {
      // must be another parameter
      String name = consumeWord();
      if (current.findVariable(name) != null) {
	die(name + " cannot be used twice");
      }
      // add as a local var in vars table (for parser)
      DbnToken newbie = current.addVariable(name, line);
      // also add as parameter to function (for engine)
      current.addChild(newbie, line);
    }

    if (!parseBlock(current)) {
      die("expecting a block: { blah blah blah }");
    }
    // add a pointer to the tree from the functions table
    root.functions.put(title, current);
    // clear out local storage
    currentDef = null;
    return true;
  }


  boolean parseReturnValue(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete value statement");
    }
    DbnToken current = parent.addChild(DbnToken.RETURN_VALUE, line);
    if (!parseValue(current)) {
      die("the value command must be followed by a value");
    }
    //currentDef.setReturnValue(current);
    return true;
  }



  boolean parseComparison(DbnToken parent, int which) throws DbnException {
    if (!consumeSpaces()) return false;

    DbnToken current = parent.addChild(which, line);
    if (!parseValue(current) || !parseValue(current)) {
      die("a comparison must be followed by two things " +
	  "that are being compared");
    }
    if (!parseEOL()) {
      die("need a new line after a comparison");
    }
    if (!parseBlock(current)) {
      die("a comparison must be followed by a block { ... }");
    }
    // block makes sure there's a newline
    //if (!parseEOL(current)) {
    //  die("need a new line after the block ends with a }");
    //}
    return true;
  }


  boolean parseNegatedValue(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;

    int oldIndex = index;
    if (p[index] != '-') return false;
    index++;

    DbnToken value = parent.addChild(DbnToken.VALUE, line);

    DbnToken newbie = value.addChild(DbnToken.MATH, line);

    DbnToken temp = newbie.addChild(DbnToken.VALUE, line);
    temp.addChild(DbnToken.NUMBER, -1, line);
    newbie.addChild(DbnToken.MULTIPLY, line);
    DbnToken current = newbie.addChild(DbnToken.VALUE, line);

    if (parseMath(current)) return true;
    else if (parseVariable(current)) return true;

    index = oldIndex;
    return false;
  }


  boolean parseValue(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;

    DbnToken current = parent.addChild(DbnToken.VALUE, line);
    if (parsePixel(current)) return true;
    else if (parseNumber(current)) return true;
    else if (parseMath(current)) return true;
    else if (parseConnector(current, true)) return true;
    else if (parseVariable(current)) return true;
    return false;
  }


  boolean parsePixel(DbnToken parent) throws DbnException {
    //System.err.println("  at pixel, p[index] == " + p[index]);
    if (!consumeSpaces()) return false;
    DbnToken test = new DbnToken(DbnToken.PIXEL, line);
	
    if (p[index] == '[') {
      index++;
      if (parseValue(test) &&
	  parseValue(test) &&
	  consumeSpaces() &&
	  p[index] == ']') {
	index++;
	parent.addChild(test, line);
	return true;
      }
      die("found a [ but no ] to match it");
    } else {
      //System.err.println("not pixel, got " + p[index]);
    }
    return false;
  }


  boolean parseNumber(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;

    int oldIndex = index;
    Integer number = consumeNumber();
    if (number != null) {
      parent.addChild(DbnToken.NUMBER, number.intValue(), line);
      return true;
    }
    index = oldIndex;
    return false;
  }

  boolean parseMath(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;

    if (p[index] == '(') {
      //System.err.println("  reading math");
      index++;
      DbnToken test = new DbnToken(DbnToken.MATH, line);
      boolean working = false;
      if (parseValue(test)) working = true;
      if (!working) {
	test = new DbnToken(DbnToken.MATH, line);
	if (parseNegatedValue(test)) working = true;
      }
      if (working) {
	while (true) {
	  consumeSpaces();
	  if (p[index] == ')') {
	    index++;
	    parent.addChild(test, line);
	    return true;
	  } else {
	    if (!parseOperator(test)) {
	      die("expecting one of these: / * + -");
	    }
	    if (!parseValue(test)) {
	      die("expecting a value");
	    }
	  }
	}
      } 
      //System.out.println(p[index]);
      die("something wrong inside the parentheses");
    }
    return false;
  }

    
  boolean parseVariable(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;
	
    // is it a named variable?
    int oldIndex = index;
    String name = consumeWord();
    if ((name != null) && (name.length() != 0)) {
      // walk through parents to find if it's really a var
      DbnToken node = parent.findVariable(name);

      // check if it's global, in case this node is
      // a 'temporary' node, disconnected from root
      if (node == null) {
	node = root.findVariable(name);
      }
      // check if it's a local variable
      //if ((node == null) && (currentBlock != null)) {
      //node = currentBlock.findVariable(name);
      //if (node != null) System.out.println("found in block");
      //}
      // check if it's a function variable
      if ((node == null) && (currentDef != null)) {
	node = currentDef.findVariable(name);
	//if (node != null) System.out.println("found in def");
      }
      // it is in fact a named variable, mark it as such
      if (node != null) {
	// parent will always be a VALUE, so it's clear that
	// it's a variable being *used* versus being declared
	parent.addChild(DbnToken.VARIABLE, name, line);
	return true;
	//} else {
	//System.err.println("'" + name + "' is not a var");
      }
      index = oldIndex;
    }

    // try some other things
    DbnToken test = new DbnToken(DbnToken.VARIABLE, line);

    // is it the value of a particular pixel?
    if (parsePixel(test)) {
      parent.addChild(test, line);
      return true;
    }

    // is it an output connector?
    if (parseConnector(test, false)) {
      parent.addChild(test, line);
      return true;
    }
    return false;
  }


  boolean parseOperator(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) return false;

    switch (p[index]) {
    case '*': index++; parent.addChild(DbnToken.MULTIPLY, line); return true;
    case '/': index++; parent.addChild(DbnToken.DIVIDE, line); return true;
    case '+': index++; parent.addChild(DbnToken.ADD, line); return true;
    case '-': index++; parent.addChild(DbnToken.SUBTRACT, line); return true;
    case '%': index++; parent.addChild(DbnToken.MODULO, line); return true;
    }
    return false;
  }


  // external functions have to make sure than this is preceeded
  // with an EOL. this makes sure that an EOL follows, however
  boolean parseBlock(DbnToken parent) throws DbnException {
    while (isEOL()) parseEOL();
    if (!consumeSpaces()) return false;
    if (p[index] != '{') return false;
    index++;
    if (!parseEOL()) return false;
	
    DbnToken current = parent.addChild(DbnToken.BLOCK, line);
    while (parseStatement(current)) { }
    if (!consumeSpaces() || (p[index] != '}')) {
      die("missing a right-hand squiggle: }  ");
    }
    index++;
    if (!parseEOL()) {
      die("need a newline after the squiggle: }");
    }
    return true;
  }


  boolean parseForever(DbnToken parent) throws DbnException {
    DbnToken current = parent.addChild(DbnToken.FOREVER, line);

    if (!consumeSpaces() || !parseEOL()) {
      die("forever must be followed by a new line");
    }
    if (!parseBlock(current)) {
      die("forever must be followed by a block: { blah blah }");
    }
    return true;
  }

    
  boolean parseRepeat(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("repeat must be followed by something");
    }
    DbnToken current = parent.addChild(DbnToken.REPEAT, line);
    //currentBlock = current;
    if (!parseVariable(current)) {
      // add the variable as a local variable to the block
      String name = consumeWord();
      //System.err.println("making local var for repeat: " + name);
      //DbnToken symbol = current.addVariable(name);
      DbnToken symbol = null;
      if (currentDef != null) {
	symbol = currentDef.addVariable(name, line);
      } else {
	symbol = root.addVariable(name, line);
      }
      current.addChild(symbol, line);
      //} else {
      //System.err.println("found local var for repeat");
    }
    if (!parseValue(current) || !parseValue(current)) { 
      die("repeat needs to have a start and end point");
    }
    if (!parseEOL()) {
      die("repeat needs a new line after it");
    }
    if (!parseBlock(current)) {
      die("repeat should be followed by a block");
    }
    //currentBlock = null;
    return true;
  }


  boolean parseSet(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete set statement");
    }
    DbnToken current = parent.addChild(DbnToken.SET, line);
    if (!parseVariable(current)) {
      String name = consumeWord();
      if (name.length() != 0) {
	// this is a new variable, add it
	DbnToken symbol = null;
	if (currentDef != null) {
	  //System.out.println("  creating local variable: " + name);
	  symbol = currentDef.addVariable(name, line);
	} else {
	  //System.out.println("  creating global variable: " + name);
	  symbol = root.addVariable(name, line);
	}
	//symbol = parent.addVariable(name);
	current.addChild(symbol, line);
	//DbnToken var = current.addChild(DbnToken.VARIABLE);
      } else {
	die("set needs to be followed by a variable");
      }
    }
    if (!parseValue(current)) {
      die("variable should be followed by value in a set statement");
    }
    if (!parseEOL()) {
      die("new line required after set");
    }
    return true;
  }


  boolean parsePaper(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete paper statement");
    }
    DbnToken current = parent.addChild(DbnToken.PAPER, line);
    if (!parseValue(current)) {
      die("paper must be followed by a value");
    }
    return true;
  }


  boolean parsePen(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete pen statement");
    }
    DbnToken current = parent.addChild(DbnToken.PEN, line);
    if (!parseValue(current)) {
      die("pen must be followed by a value");
    }
    return true;
  }


  boolean parseLine(DbnToken parent) throws DbnException {
    //System.err.println("parseLine");
    if (!consumeSpaces()) {
      die("incomplete line statement");
    }
    DbnToken current = parent.addChild(DbnToken.LINE, line);
    for (int i = 0; i < 4; i++) {
      if (!parseValue(current)) {
	die("a line needs 4 numbers or variables");
      }
    }
    return true;
  }


  boolean parseField(DbnToken parent) throws DbnException {
    //System.err.println("  in parseField");
    if (!consumeSpaces()) {
      die("incomplete line statement");
    }
    DbnToken current = parent.addChild(DbnToken.FIELD, line);
    for (int i = 0; i < 5; i++) {
      if (!parseValue(current)) {
	die("field must be followed by 5 values");
      }
    }
    return true;
  }


  boolean parsePause(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete pause statement");
    }
    DbnToken current = parent.addChild(DbnToken.PAUSE, line);
    if (!parseValue(current)) {
      die("pause must be followed by a value");
    }
    return true;
  }


  boolean parseAntiAlias(DbnToken parent) throws DbnException {
    if (!consumeSpaces()) {
      die("incomplete antialias statement");
    }
    DbnToken current = parent.addChild(DbnToken.ANTIALIAS, line);
    if (!parseValue(current)) {
      die("antialias must be followed by a value");
    }
    return true;
  }


  boolean parseRefresh(DbnToken parent) throws DbnException {
    DbnToken current = parent.addChild(DbnToken.REFRESH, line);
    return true;
  }


  boolean parseNoRefresh(DbnToken parent) throws DbnException {
    DbnToken current = parent.addChild(DbnToken.NOREFRESH, line);
    return true;
  }


  boolean parseCommand(DbnToken parent, String cmd) throws DbnException {
    if (!consumeSpaces()) return false;

    DbnToken test = new DbnToken(DbnToken.COMMAND, cmd, line);
    DbnToken command = null;
    //	System.err.println("looking for " + cmd + 
    //	   " def is " + currentDef.name);
    if ((currentDef != null) && (currentDef.name.equals(cmd))) {
      command = currentDef;
      //System.err.println("got it");
    } else {
      command = parent.findFunction(cmd);
    }
    if (command == null) return false;

    // command def is followed by an eol and a block
    int paramCount = command.childCount - 1;
    //System.err.println("  " + cmd + "(" + command.kind + 
    //	   ") should have " + paramCount + " params");
    for (int i = 0; i < paramCount; i++) {
      if (!parseValue(test)) {
	die("missing parameter number " + (i+1) + " of " +
	    paramCount + " from " + cmd);
      }
    }
    consumeSpaces();
    if (!parseEOL()) {
      die("expecting end of line after use of " + cmd);
    }
    parent.addChild(test, line);
    return true;
  }


  // DONE make sure that the connector being used
  //      is really an input or an output connector
  boolean parseConnector(DbnToken parent, 
			 boolean input) throws DbnException {
    if (!consumeSpaces()) return false;
    if (p[index] != '<') return false;

    index++;
    //DbnToken current =
    //  parent.addChild(input ? INPUT_CONNECTOR : OUTPUT_CONNECTOR);
    String name = consumeWord();
    if (name == null) {
      die("connector must have a valid name");
    }
    DbnToken current = null;
    // param count is only 1, because if it's an input connector
    // (something that's being 'set') this will be the first
    // child of the set command (not unlike a variable), 
    // and the second child will be the actual value being set
    int paramCount = 1; //input ? 1 : 2;
    if (!input && !name.equals("net") && !name.equals("array")) {
      die(name + " cannot be set");
    }
    if (name.equals("net") || name.equals("key") || 
	name.equals("mouse") || name.equals("time") || 
	name.equals("array") || name.equals("sensor")) {
      current = parent.addChild(input ? DbnToken.INPUT_CONNECTOR : 
				DbnToken.OUTPUT_CONNECTOR, name, line);
    } else {
      current = parent.addChild(DbnToken.FUNCTION, name, line);
      //DbnToken function = parent.findFunction(name);
      DbnToken function = root.findFunction(name);
      if (function == null) {
	die("i don't know anything about <" + name + ">");
      }
      paramCount = function.childCount - 1;
    }
    //current.addChild(NAME, name);
    for (int i = 0; i < paramCount; i++) {
      if (!parseValue(current)) {
	die("missing parameter number " + (i+1) + " of " +
	    paramCount + " from " + name);
      }
    }
    //if (!parseValue(current)) {
    //  die("specify a number for the connector");
    //}
    if (!consumeSpaces() || p[index] != '>') {
      die("connector function should finish with >");
    }
    index++;
    return true;
  }


  boolean isEOL() throws DbnException {
    if (!consumeSpaces()) return false;
    return ((p[index] == '\n') || (p[index] == ';'));
  }

  // preprocessor must remove all but the \n newlines!
  boolean parseEOL() throws DbnException {
    //System.out.println("at parseEOL, next char is " + p[index]);
    //if (index == p.length) return false;
    if (!consumeSpaces()) return false;

    if ((p[index] == '\n') || (p[index] == ';')) {
      if (p[index] != ';') line++;
      index++;
      //parent.addChild(EOL);
      //System.out.println("  got eol");
      return true;
      //} else {
      //System.err.println("not eol, got " + (p[index]));
    }
    return false;
  }


  private void die(String message) throws DbnException {
    throw new DbnException(message, line);
    //System.err.println(line + ": " + message);
    //System.exit(1);
  }


  private boolean isLetter(char what) {
    return ((what >= 'a' && what <= 'z') ||
	    (what >= 'A' && what <= 'Z'));
  }


  private boolean isNumber(char what) {
    return ((what >= '0') && (what <= '9'));
  }


  boolean consumeSpaces() {
    while ((index != p.length) &&
	   (p[index] == '\t' || p[index] == ' ')) {
      index++;
    }
    return (index != p.length);
  }


  private String consumeWord() {
    if (!consumeSpaces()) return null;  // consuming spaces met eof

    tempIndex = 0;
    while (index != p.length) {
      char c = p[index];
      if (isLetter(c) || isNumber(c) || 
	  (c == '?') || (c == '_')) {
	temp[tempIndex++] = c;
	index++;
      } else {
	break;
      }
    }
    return new String(temp, 0, tempIndex);	
  }


  private Integer consumeNumber() {
    if (!consumeSpaces()) return null;
	
    int lastIndex = index;
    tempIndex = 0;
    char c = p[index];
    if ((c == '-') || (c == '+')) {
      temp[tempIndex++] = c;
      index++;
    }
    while (index != p.length) {
      c = p[index];
      if (isNumber(c)) {
	temp[tempIndex++] = c;
	index++;
      } else {
	break;
      }
    }
    try {
#ifdef KVM
      return Integer.valueOf(new String(temp, 0, tempIndex));
#else
      return new Integer(new String(temp, 0, tempIndex));
#endif
    } catch (NumberFormatException e) {
      index = lastIndex;
      return null;
    }
  }


  static public byte[] readBytes(InputStream input, int start, int length)
    throws IOException 
  {
    byte[] returning = new byte[length];
	
    while (true) {
      int byteCount = input.read(returning, start, length);
      if (byteCount <= 0)
	break;
	    
      start += byteCount;
      length -= byteCount;
    }
    return returning;
  }
}
