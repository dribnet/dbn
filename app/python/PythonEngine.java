import org.python.util.PythonInterpreter; 
import org.python.core.*; 


public class PythonEngine extends DbnEngine {
    DbnGraphics graphics;
    String program;
    PythonInterpreter interpreter;

    public PythonEngine(DbnGraphics graphics, String program) {
	this.graphics = graphics;
	this.program = program;
        interpreter = new PythonInterpreter();
    }

    public void start() throws DbnException {
	stopFlag = false;
	try {
	    interpreter.exec("import DbnGraphics");
	    interpreter.exec("g = DbnGraphics.getCurrentGraphics()");
	    interpreter.exec(program);
	} catch (Exception e) {
	    //e.printStackTrace();
	    //if (!stopFlag) throw new DbnException(e.toString());
	    if (!stopFlag) {
		e.printStackTrace();
		String s = e.toString();
		int num = s.indexOf("line");
		if (num != -1) {
		    s = s.substring(num + 5);
		    num = s.indexOf(',');
		    int linenum = 0;
		    try {
			linenum = Integer.parseInt(s.substring(0, num));
		    } catch (NumberFormatException e2) {
			throw new DbnException("Python error, check the console.");
		    }
		    throw new DbnException("Python error on line " + linenum, linenum-1);
		} else {
		    //throw new DbnException(e.toString());
		    throw new DbnException("Python error, check the console.");
		}
	    }
	}
    }
    
    public void stop() {
	// killing the thread (setting it to null) throws an
	// exception, so when the stopFlag is set, the exception won't
	// be passed to the calling application. wow, i hope this 
	// works in exploder.
	stopFlag = true;
    }
}
