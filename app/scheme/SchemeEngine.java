import java.awt.*;
import java.io.*;
import java.util.*;
import jscheme.*;


// this extends DbnEngine so that it's easier on 
// type-checking inside DbnRunner. this way, both are
// of type DbnEngine which has start() stop() and die() methods

// this also incorporates the contents of "Interpreter"
// because that used to have a thread and it doesn't need to.
// and half its functions were get/set methods between this class.

public class SchemeEngine extends DbnEngine implements MemWin {
    static public boolean debugging = false;

    Evaluator evaluator;
    //Interpreter interpreter;
    //Interface interfase;

    //DbnGraphics dbg;
    String program;
    
    EnvList env;
    Parser parser;
    //Evaluator e;


    public SchemeEngine(/*DbnGraphics dbg,*/ String program) {
	//this.dbg = dbg;
	this.program = program;

	Evaluator.kill = false;
	jscheme.EnvList.appletwin = this;
	//interfase = new Interface(true);
    }
    

    public void start() throws DbnException {
        Evaluator.kill = false; // again?
	if (program == null) return;

	stop();
	
	// from interface
	//env = new EnvList();
	//env = env.extendEnv(null,null); // this isolates user/sys envs
	// from here
	//env = (EnvList)interfase.env.clone();
	env = new EnvList();
	env = env.extendEnv(null, null);
	
	parser = new Parser(program);
	evaluator = new Evaluator();
 
	Object v = null, r = null;
	//System.out.println("*** Reading and Evaluating your program");
	Boolean truthful = new Boolean(true);
	while ((v = parser.parse()) != null) {
	    // aaaaak! slow! bad! dirty! take out yo *own* garbage!
	    debugging = env.lookup("debugging").equals(truthful);
	    if (debugging) System.out.println(v);
	    //	    System.out.println("parse" + System.currentTimeMillis());
	    try {
		r = evaluator.eval(v, env);
		if (debugging) System.out.println("=> " + r);

	    } catch (RuntimeException e) {
		if (!e.getMessage().equals("not really")) {
		    // on its way out..
		    ThrowError.error("ERROR: \n" + e + "\n");
		    e.printStackTrace();
		    throw new DbnException(e.toString());
		}
	    }
	}    
	//System.out.println("\n*** Finished Reading and Evaluating");

	Object last = (r == null) ? "( )" : r;    
	//System.out.println("result is: "+last);
	
	//interfase.tresult.appendText(last + "\n");
	//interfase.env = env;
    }



    // is this ever used?
    /*
    private Object interpretString(String program) {
	Object v = null, r = null;
	parser = new Parser(program);

	while ((v = parser.parse()) != null) {
	    try {
		r = evaluator.eval(v, env);
	    } catch (RuntimeException err) {
		System.out.println("ERROR: " + err);
	    }
	}
	Object last = (r == null) ? "( )" : r;
	return last;
    }    
    

    private static String readLine(InputStream inStream) {
	StringBuffer buf = new StringBuffer();
	int c = -1;
	boolean done = false ;

	try {
	    System.out.print(">> "); 
	    System.out.flush();
	    while (!done) {
		c = inStream.read();
		done = ((c=='\n') || (c==-1));
		if (!done) buf.append((char) c);
	    }
	    return (buf.toString());
        } catch (Exception err) {                        // catch exceptions
	    System.out.println("jscheme.Interpreter.readLine: \n" +
			       "    Found an I/O exception " + err);
	    System.exit(0);
        } finally {                                    // close out  
        }
	return(null);
    }
    */

    public void stop() {
	//System.out.println("STOPPING");
	if (evaluator != null) {
	    Evaluator.kill = true;
	    //evaluator.stop();
	    //evaluator = null;
	}

	// needs to set a flag to tell everyone to stop
	// will have to learn more about scheme innards to do this

	//if (interpreter != null) { 
	//  interpreter.stop(); 
	//  interpreter = null; 
	//}
    }


    public Component add(Component x) { return x; }
    public void addCallback(Component x, Closure y) { }
    public void refresh() { }
    public void clear() { }
    public void drawLine(int a1, int a2, int a3, int a4) { }
    public void drawOval(int a1, int a2, int a3, int a4) { }
    public void drawRect(int a1, int a2, int a3, int a4) { }
}
