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
	    if (!stopFlag) throw new DbnException(e.toString());
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
