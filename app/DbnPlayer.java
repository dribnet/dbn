#ifdef CONVERTER


import java.awt.*;
import java.applet.Applet;


// this class is extended by others to be used for 
// making an individual player for a dbn program
abstract public class DbnPlayer extends Panel
implements Runnable, DbnEnvironment {
    DbnApplet applet;
    DbnGraphics graphics;

    static final int RUNNER_STARTED = 0;
    static final int RUNNER_FINISHED = 1;
    static final int RUNNER_ERROR = -1;
    static final int RUNNER_STOPPED = 2;
    int state = RUNNER_FINISHED;
    
    Thread thread;


    public DbnPlayer(DbnApplet applet) {
	this.applet = applet;
	graphics = new DbnGraphics(101, 101);
	this.add(graphics);
	//this.start();
    }


    public void start() {
	if (thread != null) {
	    try { 
		thread.stop(); 
	    } catch (Exception e) { }
	    thread = null;
	}
	thread = new Thread(this);
	thread.start();
	//thread.setPriority(6);
    }


    public void run() {
	state = RUNNER_STARTED;
	graphics.reset();
	
	try {
	    execute();
	    state = RUNNER_FINISHED;
	    this.finished();

	} catch (DbnException e) { 
	    state = RUNNER_ERROR;
	    this.stop();
	    this.error(e);

	} catch (Exception e) {
	    e.printStackTrace();
	    this.stop();
	}	
    }

    public void execute() throws DbnException { 
	// this is what PlayerProgram subclasses
    }
    
    public void stop() {
	//running = false;
	if (thread != null) {
	    thread.stop();
	    thread = null;
	}
    }


    // DbnEnvironment methods

    public void terminate() {
    }

    // error being reported to gui (called by dbn)
    public void error(DbnException e) {
	e.printStackTrace();
    }

    // successful finish reported to gui (called by dbn)
    public void finished() { }

    // message to write to gui (called by dbn)
    public void message(String msg) {
	System.out.println(msg);
    }
}


#endif
