import java.awt.*;
import java.applet.Applet;


public class DbnRunner implements Runnable {
    DbnApplet app;
    DbnGui gui;
    DbnRunPanel dbrp;
    DbnGraphics dbg;
    Graphics cachedg;

    String program;
    String proglid;
    String progrem;

    DbnPreprocessor preprocessor;
    DbnEngine engine;
    long heartbeatTime;

    static final int RUNNER_STARTED = 0;
    static final int RUNNER_FINISHED = 1;
    static final int RUNNER_ERROR = -1;
    static final int RUNNER_STOPPED = 2;
    int state = RUNNER_FINISHED;
	    
    Thread thread;
    boolean forceStop;
    

    public DbnRunner(DbnApplet app, DbnGui gui, DbnRunPanel dbrp, 
		     int width, int height, String program, 
		     String _proglid, String _progrem) {
	super();
	this.app = app;
	this.gui = gui;
	this.dbrp = dbrp;

	int displayMode = 0;
	String displayModeStr = app.getParameter("display_mode");
	if ((displayModeStr == null) || (displayModeStr.equals("plain")))
	    displayMode = DbnGraphics.DISPLAY_PLAIN;
	else if (displayModeStr.equals("flush"))
	    displayMode = DbnGraphics.DISPLAY_FLUSH;
	else if (displayModeStr.equals("flush_more"))
	    displayMode = DbnGraphics.DISPLAY_FLUSH_MORE;
	else if (displayModeStr.equals("auto"))
	    displayMode = DbnGraphics.DISPLAY_AUTO;

	Image image = app.createImage(width, height);
	dbg = new DbnGraphics(image, width, height, this, 
			      app.getHost(), displayMode);
	//render();
	setProgram(program);
	proglid = (_proglid==null)?"unnamed":_proglid;
	progrem = (_progrem==null)?"unremed":_progrem;

	preprocessor = new DbnPreprocessor(gui, app);
    }

    public boolean emptyp()
    {
	return (program == null) || program.equals("") || program.equals("\n");
    }
    /*
    public void setDisplayXY(int x, int y) {
	this.x = x;
	this.y = y;
    }
	
    public boolean insidep(int mx, int my) {
	return ((mx > x) && (mx < (x + width)) &&
		(my > y) && (my < (y + height)));
    }
    */

    public void setProgram(String program) {
	this.program = program;
    }
    
    public boolean isRunning() {
	//System.out.println("state of runner is " + state);
	return (state == RUNNER_STARTED);
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
#ifdef SCHEME
		engine = new SchemeEngine(dbg, program);
		engine.start();
#endif
	    } else if (program.charAt(0) == '#') {
#ifdef PYTHON
		forceStop = true;
		engine = new PythonEngine(dbg, program);
		engine.start();
		forceStop = false;
#endif
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
	    forceStop = false;
	    this.stop();
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
	    if (forceStop) {
		thread.stop();
		thread = null;
	    }
	    engine = null;
	}
	msg(""); 
    }


    public void render() {
	//System.out.println(dbg);
	//System.out.println(dbg.image);
	//System.out.println(dbrp);
	dbrp.update(dbg.image);
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
