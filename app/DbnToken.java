import java.io.*;
import java.util.*;


public class DbnToken {
    DbnToken parent;

    int kind;
    String name;
    int number;

    int childCount;
    DbnToken children[];

    // root has globals, functions have locals, repeat has var
    Hashtable variables;

    // only root has functions, null everywhere else
    Hashtable functions;

    
    static final int EOL            = 0;
    static final int NUMBER         = 1;
    static final int NAME           = 2;
    static final int PIXEL          = 3;
    static final int VARIABLE       = 4;
    static final int BLOCK          = 5;
    static final int VALUE          = 6;
    static final int MATH           = 7;
    static final int STACK_VARIABLE = 8;

    static final int ADD      = 10;
    static final int SUBTRACT = 11;
    static final int MULTIPLY = 12;
    static final int DIVIDE   = 13;
    static final int OPERATOR = 14;

    static final int INPUT_CONNECTOR  = 20;
    static final int OUTPUT_CONNECTOR = 21;

    static final int COMMAND_DEF  = 30;
    static final int FUNCTION_DEF = 31;
    static final int RETURN_VALUE = 32;
    static final int COMMAND      = 33;
    static final int FUNCTION     = 34;

    static final int REPEAT  = 40;
    static final int FOREVER = 41;

    static final int SET     = 50;
    static final int PAPER   = 51;
    static final int PEN     = 52;
    static final int LINE    = 53;
    static final int FIELD   = 54;

    static final int SMALLER     = 60;
    static final int NOT_SMALLER = 61;
    static final int SAME        = 62;
    static final int NOT_SAME    = 63;

    static final int ROOT        = 70;
    static final int STATEMENT   = 71;
    static final int STATEMENTS  = 72;


    //DbnToken() { }

    DbnToken(int kind) {
	this.kind = kind;
    }

    DbnToken(int kind, String name) {
	this.kind = kind;
	this.name = name;
    }

    DbnToken(int kind, int number) {
	this.kind = kind;
	this.number = number;
    }
    

    void addChild(DbnToken newbie) {
	if (children == null) {
	    children = new DbnToken[10];
	} else if (childCount == children.length) {
	    DbnToken temp[] = new DbnToken[childCount*2];
	    System.arraycopy(children, 0, temp, 0, childCount);
	    children = temp;
	}
	newbie.parent = this;
	children[childCount++] = newbie;
    }

    DbnToken addChild(int kind) {
	DbnToken newbie = new DbnToken(kind);
	addChild(newbie);
	return newbie;
    }

    DbnToken addChild(int kind, String name) {
	DbnToken newbie = new DbnToken(kind, name);
	addChild(newbie);
	return newbie;
    }

    DbnToken addChild(int kind, int number) {
	DbnToken newbie = new DbnToken(kind, number);
	addChild(newbie);
	return newbie;
    }


    DbnToken addVariable(String title) {
	if (variables == null) variables = new Hashtable();

	// this will store the value for the execution engine
	DbnToken newbie = new DbnToken(VARIABLE, title);
	variables.put(title, newbie);
	return newbie;
    }


    DbnToken findVariable(String name) {
	if (variables != null) {
	    DbnToken guess = (DbnToken) variables.get(name);
	    if (guess != null) return guess;
	}
	return (parent != null) ? parent.findVariable(name) : null;
    }


    DbnToken findFunction(String name) {
	if (functions != null) {
	    DbnToken guess = (DbnToken) functions.get(name);
	    if (guess != null) return guess;
	}
	return (parent != null) ? parent.findFunction(name) : null;
    }

    
    public void print() {
	System.out.println(toString());
    }

    public String toString() {
	return toString(0);
    }
    
    public String toString(int indentCount) {
	StringBuffer buffer = new StringBuffer();

	for (int i = 0; i < indentCount; i++) 
	    buffer.append(' ');

	switch(kind) {
	case EOL: buffer.append("EOL"); break;
	case NUMBER: buffer.append("NUMBER " + number); break;
	case NAME: buffer.append("NAME " + name); break;
	case PIXEL: buffer.append("PIXEL"); break;
	case VARIABLE: buffer.append("VARIABLE " + name); break;
	case BLOCK: buffer.append("BLOCK"); break;
	case VALUE: buffer.append("VALUE"); break;
	case MATH: buffer.append("MATH"); break;
	case STACK_VARIABLE: 
	    buffer.append("STACK_VARIABLE " + number); break;

	case ADD: buffer.append("ADD"); break;
	case SUBTRACT: buffer.append("SUBTRACT"); break;
	case MULTIPLY: buffer.append("MULTIPLY"); break;
	case DIVIDE: buffer.append("DIVIDE"); break;
	case OPERATOR: buffer.append("OPERATOR"); break;

	case INPUT_CONNECTOR: 
	    buffer.append("INPUT_CONNECTOR " + name); break;
	case OUTPUT_CONNECTOR: 
	    buffer.append("OUTPUT_CONNECTOR " + name); break;

	case COMMAND_DEF: buffer.append("COMMAND_DEF " + name); break;
	case FUNCTION_DEF: buffer.append("FUNCTION_DEF " + name); break;
	case RETURN_VALUE: buffer.append("RETURN_VALUE"); break;
	case COMMAND: buffer.append("COMMAND " + name); break;
	case FUNCTION: buffer.append("FUNCTION " + name); break;

	case REPEAT: buffer.append("REPEAT"); break;
	case FOREVER: buffer.append("FOREVER"); break;

	case SET: buffer.append("SET"); break;
	case PAPER: buffer.append("PAPER"); break;
	case PEN: buffer.append("PEN"); break;
	case LINE: buffer.append("LINE"); break;
	case FIELD: buffer.append("FIELD"); break;

	case SMALLER: buffer.append("SMALLER"); break;
	case NOT_SMALLER: buffer.append("NOT_SMALLER"); break;
	case SAME: buffer.append("SAME"); break;
	case NOT_SAME: buffer.append("NOT_SAME"); break;

	case STATEMENT: buffer.append("STATEMENT"); break;
	case STATEMENTS: buffer.append("STATEMENTS"); break;
	case ROOT: buffer.append("ROOT"); break;

	default: 
	    System.err.println("not handled: " + kind);
	    System.exit(1);
	}
	
	if (variables != null) {
	    buffer.append(" variables: ");
	    Enumeration e = variables.keys();
	    while (e.hasMoreElements()) {
		buffer.append((String) e.nextElement());
		buffer.append(' ');
	    }
	}
	buffer.append(System.getProperty("line.separator"));

	for (int i = 0; i < childCount; i++) {
	    buffer.append(children[i].toString(indentCount + 2));
	}
	return buffer.toString();
    }
}
