#ifdef PLAYER


import java.awt.*;
import java.applet.Applet;


// this class is extended by others to be used for 
// making an individual player for a dbn program

// this would be a DbnEnvironment class under the model, 
// but that just adds an extra, unneeded class to the mix

abstract public class DbnPlayer extends Panel implements Runnable {
    DbnApplet applet;
    DbnGraphics graphics;

    static final int RUNNER_STARTED = 0;
    static final int RUNNER_FINISHED = 1;
    static final int RUNNER_ERROR = -1;
    //static final int RUNNER_STOPPED = 2;
    int state = RUNNER_FINISHED;
    
    Thread thread;


    //public DbnPlayer(DbnApplet applet) {
    public void init(DbnApplet applet) {
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
    }


    public void run() {
	state = RUNNER_STARTED;
	graphics.reset();
	
	try {
	    execute();
	    state = RUNNER_FINISHED;
	    //this.finished();

       //} catch (DbnException e) { 
	    //state = RUNNER_ERROR;
	    //e.printStackTrace();
	    //this.stop();

	} catch (Exception e) {
	    state = RUNNER_ERROR;
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

    public void paint(Graphics g) {
	if (state == RUNNER_ERROR) {
	    g.setColor(Color.red);
	    g.fillRect(0, 0, size().width, size().height);
	}
    }

    public boolean mouseDown(Event e, int x, int y) {
	if (state == RUNNER_FINISHED) {
	    start();
	}
	return true;
    }
    
    public boolean keyDown(Event e, int key) {
	if ((key == 27) && (state == RUNNER_STARTED)) {
	    stop();
	    state = RUNNER_FINISHED;
	}
	return true;
    }
}


#endif
