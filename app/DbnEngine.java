import java.util.*;


public class DbnEngine {
    DbnToken root;
    DbnRunner parent;
    DbnGraphics graphics;
    boolean stopFlag;

    int stackSize = 100;
    Hashtable stack[];
    int stackIndex = 0;


    DbnEngine() { }  // so it can be subclassed for scheme and python

    DbnEngine(DbnToken root, DbnGraphics graphics, DbnRunner parent) {
	this.root = root;
	this.graphics = graphics;
	this.parent = parent;
    }


    public void start() throws DbnException {
	stopFlag = false;
	stack = new Hashtable[stackSize];
	execRoot();	
    }

    
    public void stop() {
	stopFlag = true;
    }


    void execRoot() throws DbnException {
	pushLocalVariables(root);
	execStatements(root.children[0]);
	popVariables();
    }


    void execStatements(DbnToken current) throws DbnException {
	if (stopFlag) return;

	for (int i = 0; i < current.childCount; i++) {
	    execStatement(current.children[i]);
	    parent.idle();
	}
    }


    void execStatement(DbnToken statement) throws DbnException {
	DbnToken current = statement.children[0];
	switch (current.kind) {

	case DbnToken.BLOCK: execBlock(current); break;
	case DbnToken.STATEMENT: execStatement(current); break;

	case DbnToken.COMMAND_DEF: break;
	case DbnToken.FUNCTION_DEF: break;
	case DbnToken.RETURN_VALUE: execReturnValue(current); break;

	case DbnToken.COMMAND: execCommand(current); break;

	case DbnToken.REPEAT: execRepeat(current); break;
	case DbnToken.FOREVER: execForever(current); break;

	case DbnToken.SET: execSet(current); break;
	case DbnToken.PAPER: execPaper(current); break;
	case DbnToken.PEN: execPen(current); break;
	case DbnToken.LINE: execLine(current); break;
	case DbnToken.FIELD: execField(current); break;
	case DbnToken.PAUSE: execPause(current); break;
	case DbnToken.ANTIALIAS: execAntiAlias(current); break;
	case DbnToken.REFRESH: execRefresh(current); break;

	case DbnToken.SMALLER: 
	case DbnToken.NOT_SMALLER: 
	case DbnToken.SAME: 
	case DbnToken.NOT_SAME: 
	    execComparison(current);
	    break;

	default: die("internal error, not handled: " + 
		     current.kind, statement);	    
	}
    }


    void execBlock(DbnToken current) throws DbnException {
	execStatements(current);
    }


    void execReturnValue(DbnToken current) throws DbnException {
	int amount = getValue(current.children[0]);
	setStackVariable("_result", amount);
    }


    void execCommand(DbnToken current) throws DbnException {
	DbnToken command = current.findFunction(current.name);
	int paramCount = command.childCount - 1;
	// put all params for function and local vars onto the stack
	pushFunctionVariables(current, paramCount);

	// last child is the block of code for the fxn
	execBlock(command.children[paramCount]);

	// clean up the stack
	popVariables();
    }


    void execRepeat(DbnToken current) throws DbnException {
	//System.out.println("            REPEAT");

	DbnToken iterator = current.children[0];
	int start = getValue(current.children[1]);
	int finish = getValue(current.children[2]);
	DbnToken block = current.children[3];

	//boolean backwards = (finish < start);
	int step = (finish > start) ? 1 : -1;
	/*
	Hashtable table = new Hashtable();
	DbnToken iteratorVariable = findStackVariable(iterator.name);
	if (iteratorVariable == null) {
	    table.put(iterator.name, 
		      new DbnToken(DbnToken.NUMBER, start));
	}
	pushVariables(table);
	*/
	//pushLocalVariables(current);

	// warning for ai double buffer super duper refresh

	// loop is inclusive (0 10 goes up to equals 10)
	setStackVariable(iterator.name, start);

	int amount;
	while (((step > 0) && 
		(amount = getStackVariable(iterator.name)) <= finish) || 
	       ((step < 0) &&
		(amount = getStackVariable(iterator.name)) >= finish)) {
	    graphics.beginRepeat();
	    if (stopFlag) break;
	    // actually do the stuff
	    execBlock(block);
	    // has to 'get' the value again, because user might
	    // have modified it inside the block
	    setStackVariable(iterator.name, 
			     getStackVariable(iterator.name)+step);
	    parent.idle();
	    graphics.endRepeat();
	}
	//popVariables();
    }


    void execForever(DbnToken current) throws DbnException {
	//System.out.println("               FOREVER");
	DbnToken block = current.children[0];
	// rilly important to be checking stopFlag
	while (!stopFlag) {
	    graphics.beginForever();
	    execBlock(block);
	    parent.idle();
	    graphics.endForever();
	}
    }


    void execSet(DbnToken current) throws DbnException {
	DbnToken variable = current.children[0];
	DbnToken value = current.children[1];
	setValue(variable, getValue(value));
    }


    void execPaper(DbnToken current) throws DbnException {
	graphics.paper(getValue(current.children[0]));
    }


    void execPen(DbnToken current) throws DbnException {
	graphics.pen(getValue(current.children[0]));
    }

   
    void execLine(DbnToken current) throws DbnException {
	int x1 = getValue(current.children[0]);
	int y1 = getValue(current.children[1]);
	int x2 = getValue(current.children[2]);
	int y2 = getValue(current.children[3]);
	graphics.line(x1, y1, x2, y2);
    }


    void execField(DbnToken current) throws DbnException {
	int x1 = getValue(current.children[0]);
	int y1 = getValue(current.children[1]);
	int x2 = getValue(current.children[2]);
	int y2 = getValue(current.children[3]);
	int color = getValue(current.children[4]);
	graphics.field(x1, y1, x2, y2, color);
    }


    void execPause(DbnToken current) throws DbnException {
	int amount = getValue(current.children[0]);
	graphics.pause(amount);
    }


    void execAntiAlias(DbnToken current) throws DbnException {
	graphics.setAntiAlias(getValue(current.children[0]));
    }


    void execRefresh(DbnToken current) throws DbnException {
	graphics.refresh();
    }


    void execComparison(DbnToken current) throws DbnException {
	boolean result = false;
	int value1 = getValue(current.children[0]);
	int value2 = getValue(current.children[1]);

	switch (current.kind) {
	case DbnToken.SMALLER: 
	    result = (value1 < value2); break;
	case DbnToken.NOT_SMALLER: 
	    result = (value1 >= value2); break;
	case DbnToken.SAME: 
	    result = (value1 == value2); break;
	case DbnToken.NOT_SAME: 
	    result = (value1 != value2); break;
	}
	if (result) execBlock(current.children[2]);
    }


    int getValue(DbnToken value) throws DbnException {
	DbnToken current = null;
	if (value.kind != DbnToken.VALUE) {
	    current = value;
	} else {
	    current = value.children[0];
	}
	switch (current.kind) {
	case DbnToken.PIXEL:           
	    return getPixel(current);
	case DbnToken.NUMBER:          
	    return getNumber(current);
	case DbnToken.MATH:            
	    return getMath(current);
	case DbnToken.INPUT_CONNECTOR: 
	    return getConnector(current);
	case DbnToken.VARIABLE:        
	    return getVariable(current);
	case DbnToken.FUNCTION:        
	    return getFunction(current);
	default: 
	    die("value not understood", current); 
	}
	return -1;  // not reached
    }


    int getPixel(DbnToken current) throws DbnException {
	return graphics.getPixel(getValue(current.children[0]),
				 getValue(current.children[1]));
    }


    int getNumber(DbnToken current) throws DbnException {
	return current.number;
    }


    int getMath(DbnToken current) throws DbnException {
	int valueCount = (current.childCount+1) / 2;
	int values[] = new int[valueCount];
	int operationCount = current.childCount / 2;
	int operations[] = new int[operationCount];

	for (int i = 0; i < valueCount; i++) {
	    values[i] = getValue(current.children[i*2]);
	}
	for (int i = 0; i < operationCount; i++) {
	    operations[i] = current.children[i*2+1].kind;
	}
	/*
	for (int i = 0; i < operationCount; i++) {
	    System.out.print(values[i]);
	    if (operations[i] == DbnToken.MULTIPLY) System.out.print("*");
	    else if (operations[i] == DbnToken.DIVIDE) System.out.print("/");
	    else if (operations[i] == DbnToken.ADD) System.out.print("+");
	    else if (operations[i] == DbnToken.SUBTRACT) System.out.print("-");
	}
	System.out.print(values[valueCount-1] + " = ");
	*/

	// do multiply and divide
	for (int i = 0; i < operationCount; i++) {
	    if ((operations[i] == DbnToken.MULTIPLY) ||
		(operations[i] == DbnToken.DIVIDE)) {

		// multiply val i and i+1 against each other
		if (operations[i] == DbnToken.MULTIPLY) {
		    values[i] *= values[i+1];
		} else {
		    //values[i] /= values[i+1];
		    // division by zero evaluates to zero
		    values[i] = (values[i+1] == 0) ? 0 :
			values[i] / values[i+1];
		}
		// put result in i, scoot everything down
		for (int j = i+1; j < valueCount-1; j++) {
		    values[j] = values[j+1];
		}
		for (int j = i; j < operationCount-1; j++) {
		    operations[j] = operations[j+1];
		}
		i--; // gotta check this one
		operationCount--;
		valueCount--;
	    }
	}
	// do add and subtract
	for (int i = 0; i < operationCount; i++) {
	    //while (i < operationCount) {
	    if ((operations[i] == DbnToken.ADD) ||
		(operations[i] == DbnToken.SUBTRACT)) {

		// multiply val i and i+1 against each other
		if (operations[i] == DbnToken.ADD) {
		    values[i] += values[i+1];
		} else {
		    values[i] -= values[i+1];
		}
		// put result in i, scoot everything down
		for (int j = i+1; j < valueCount-1; j++) {
		    values[j] = values[j+1];
		}
		for (int j = i; j < operationCount-1; j++) {
		    operations[j] = operations[j+1];
		}
		i--;
		operationCount--;
		valueCount--;
	    }
	}
	//System.out.println(values[0]);
	return values[0];

	/*
	  // simple, but does no order-of-operations stuff
	int sum = getValue(current.children[0]);
	int operations = (current.childCount / 2);  // count = 5, ops = 2
	for (int i = 0; i < operations; i++) {  // i for 0..1, 0..4
	    int amount = getValue(current.children[i*2+2]);
	    switch (current.children[i*2+1].kind) {
	    case DbnToken.ADD: 
		sum += amount; break;
	    case DbnToken.SUBTRACT: 
		sum -= amount; break;
	    case DbnToken.MULTIPLY: 
		sum *= amount; break;
	    case DbnToken.DIVIDE: 
		sum = (amount != 0) ? (sum / amount) : 0; break;
	    }
	}
	return sum;
	*/
    }


    int getConnector(DbnToken current) throws DbnException {
	if (graphics.isConnector(current.name))
	    return graphics.connectorGet(current.name,
					 getValue(current.children[0]));
	die("connector not found", current);
	return -1;
    }


    int getVariable(DbnToken current) throws DbnException {
	if (current.childCount == 0) {
	    if (current.name == null) {
		die("confused, trying to set no-name var", current);
	    }
	    return getStackVariable(current.name);
	}
	// getValue looks at the children
	return getValue(current);
    }


    int getFunction(DbnToken current) throws DbnException {
	DbnToken function = current.findFunction(current.name);
	int paramCount = function.childCount - 1;
	pushFunctionVariables(current, paramCount);

	// push a spot for the result onto the stack
	DbnToken token = new DbnToken(DbnToken.NUMBER, 0);
	Hashtable table = new Hashtable();
	table.put("_result", token);
	pushVariables(table);

	// last child is the block of code for the fxn
	execBlock(function.children[paramCount]);

	// get the result and pop its container off the stack
	int result = getStackVariable("_result");
	popVariables();

	// clean up the stack
	popVariables();

	// send back the good stuff
	return result;
    }


    void setValue(DbnToken value, int amount) throws DbnException {
	switch (value.kind) {

	case DbnToken.VALUE: 
	    setValue(value.children[0], amount); 
	    break;

	case DbnToken.VARIABLE:
	    if (value.name == null) {
		// if it's a pixel or some other elementary
		// var, needs to recurse here
		if (value.childCount != 0) {
		    setValue(value.children[0], amount);
		} else {
		    die("no name, no good var", value);
		}
	    } else {
		setStackVariable(value.name, amount);
	    }
	    break;

	case DbnToken.NUMBER:
	    value.number = amount;
	    break;

	case DbnToken.PIXEL:
	    graphics.setPixel(getValue(value.children[0]),
			      getValue(value.children[1]), amount);
	    break;

	case DbnToken.OUTPUT_CONNECTOR:
	    //value.print();
	    if (graphics.isConnector(value.name)) {
		graphics.connectorSet(value.name,
				      getValue(value.children[0]), 
				      amount);
	    } else {
		die("output connector not found", value);
	    }
	    break;

	default:
	    die("cannot set var to " + amount, value);
	}
    }


    int getStackVariable(String name) throws DbnException {
	//System.out.println("getting " + name);
	DbnToken var = findStackVariable(name);
	return var.number;
    }


    void setStackVariable(String name, int amount) throws DbnException {
	//System.out.println("trying to set " + name + " to " + amount);
	DbnToken var = findStackVariable(name);
	var.number = amount;
    }


    DbnToken findStackVariable(String name) throws DbnException {
	DbnToken var = null;
	//System.err.println("searching for " + name);
	// go backwards searching for this var
	for (int i = stackIndex-1; i >= 0; --i) {
	    var = (DbnToken) stack[i].get(name);
	    if (var != null) return var;

	    Enumeration e = stack[i].keys();
	    while (e.hasMoreElements()) {
		String n = (String) e.nextElement();
		DbnToken blah = (DbnToken) stack[i].get(n);
		//System.out.println("  " + n + " = " + blah.number);
	    }
	    //System.out.println();
	}
	//System.out.println();
	return null;
    }


    // used at the root and for repeat
    void pushLocalVariables(DbnToken current) throws DbnException {
	Hashtable table = new Hashtable();
	if (current.variables != null) {
	    Enumeration e = current.variables.keys();
	    while (e.hasMoreElements()) {
		String name = (String) e.nextElement();
		table.put(name, new DbnToken(DbnToken.NUMBER, 0));
	    }
	}
	pushVariables(table);
    }


    // for function vars, must get the actual values from the
    // children here, but the names must come from the function def

    // this also pushes the local vars (from .variables) for this
    // function. this avoids the overlap that would occur if
    // pushFunction and pushLocal were called together (duh)

    void pushFunctionVariables(DbnToken current, int paramCount) 
	throws DbnException {
	DbnToken function = root.findFunction(current.name);

	// evaluate each param to function and put it in the stack
	Hashtable table = new Hashtable();
	for (int i = 0; i < paramCount; i++) {
	    String name = function.children[i].name;
	    int amount = getValue(current.children[i]);
	    table.put(name, new DbnToken(DbnToken.NUMBER, amount));
	    //System.out.println("  param " + name + " = " + amount);
	}

	// now add each of the function's local vars 
	// not covered by the incoming params
	if (function.variables != null) {
	    Enumeration e = function.variables.keys();
	    while (e.hasMoreElements()) {
		String name = (String) e.nextElement();
		if (!table.containsKey(name)) {
		    //System.err.println("adding local " + name);
		    table.put(name, new DbnToken(DbnToken.NUMBER, 0));
		    //} else {
		    //System.err.println("has local " + name);
		    //System.out.println("  " + name + " = empty");
		}
	    }
	}
	pushVariables(table);
    }


    void pushVariables(Hashtable newbie) throws DbnException {
	if (stackIndex == stackSize) {
	    Hashtable temp[] = new Hashtable[stackSize*2];
	    System.arraycopy(stack, 0, temp, 0, stackSize);
	    stack = temp;
	    stackSize *= 2;
	    //die("stack overflow error.", null);
	}
	stack[stackIndex++] = newbie;
    }


    Hashtable popVariables() throws DbnException {
	if (stackIndex == 0) {
	    die("stack underflow error.", null);
	}
	stackIndex--;
	Hashtable deadMan = stack[stackIndex];
	stack[stackIndex] = null;
	return deadMan;
    }


    private void die(String message, DbnToken where) throws DbnException {
	//DbnException e = new DbnException(message, where);
	//System.err.println(e);
	//throw e;
	if (where != null) {
	    throw new DbnException(message, where);
	} 
	throw new DbnException(message);
    }
}
