import java.awt.*;
import java.applet.Applet;


public class DbnRunner implements Runnable {
    DbnApplet app;
    DbnGui gui;
    Panel parent;
    DbnGraphics dbg;
    Graphics cachedg;

    String program;

    DbnPreprocessor preprocessor;
    DbnEngine engine;
    long heartbeatTime;

    int dispx, dispy;
    int dispw, disph;
	
    static final int RUNNER_STARTED = 0;
    static final int RUNNER_FINISHED = 1;
    static final int RUNNER_ERROR = -1;
    static final int RUNNER_STOPPED = 2;
    int state = RUNNER_FINISHED;
	    
    Thread thread;


    public DbnRunner(DbnApplet app, DbnGui gui, Panel parent, 
		     int x, int y, int w, int h, String program) {
	super();
	this.app = app;
	this.gui = gui;
	this.parent = parent;
	
	dispx = x; dispy = y;
	dispw = w; disph = h;
	dbg = new DbnGraphics(parent, w, h, this, app.getHost());
	setProgram(program);

	preprocessor = new DbnPreprocessor(gui, app);
    }
	
    public void setDisplayXY(int x, int y) {
	dispx = x; dispy = y;
    }
	
    public boolean insidep(int x, int y) {
	return (dispx<x&&x<(dispx+dispw)&&dispy<y&&y<(dispy+disph));
    }

    public void setProgram(String program) {
	this.program = program;
    }
    
    public boolean runningp() {
	return (state==RUNNER_STARTED);
    }

    public void start() {
	// threadless version didn't work
	try { 
	    if (thread != null) {
		thread.stop(); 
		thread = null;
	    } 
	} catch (Exception e) { 
	    thread = null;
	}
	if (thread == null) {
	    thread = new Thread(this);
	    thread.start();
	}
    }
	
    public void msg(String s) {
	gui.msg(s);
    }


    public void run() {
	boolean donep = false;
	
	state = RUNNER_STARTED;
	dbg.reset();
	cachedg = null;
	
	try {
	    if (program.charAt(0) == ';') {
		engine = new SchemeEngine(dbg, program);
		engine.start();
	    } else {
		String processed = preprocessor.process(program);
		DbnParser parser = new DbnParser(processed.toCharArray());
		engine = new DbnEngine(parser.getRoot(), dbg, this);
		engine.start();
	    }
	    state = RUNNER_FINISHED;
	    gui.success();

	} catch (DbnException e) { 
	    state = RUNNER_ERROR;
	    this.stop();
	    // must go below so that error msg shows
	    gui.reporterror(e);

	} catch (Exception e) {
	    e.printStackTrace();
	    this.stop();
	}	
	render();
	gui.terminated();
    }


    public void stop() {
	if (engine != null) {
	    engine.stop();
	    engine = null;
	}
	msg(""); 
    }


    public void render() {
	if (cachedg == null) cachedg = parent.getGraphics();
	render(cachedg);
    }


    public void render(Graphics g) {
	if (dbg.image == null) {
	    dbg.buildBuffers();
	}
	// i think this was throwing an exception on close
	if (g != null) {
	    g.drawImage(dbg.image, dispx, dispy, parent);
	}
    }


    // from DbnProcessor

    public void idle() {
	long currentTime = System.currentTimeMillis();
	gui.idle(currentTime);

	if ((currentTime % 1000) > 800) {
	    // beat the heart if a new beat
	    long hba = currentTime / 1000;
	    if (hba != heartbeatTime)
		gui.heartbeat();
	    heartbeatTime = hba;
	}
    }

}
