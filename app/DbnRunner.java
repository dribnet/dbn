import java.awt.*;
import java.applet.Applet;


/*
 * dbnrunner is not threaded, dbn in general is not multi-threaded.
 * you can imagine situations where there might be 2 dbnrunners running
 * at the same time, in which case you would want to encapsulate each
 * runner in its own thread.
 */ 
public class DbnRunner implements Runnable {
    DbnApplet app;
    DbnGui gui;
    Panel parent;
	
    public DbnGraphics dbg;
    String prog;
    DbnProcessor dp;
	
    public int dispx = 0, dispy = 0;
    public int dispw, disph;
	
    public int state = DBNRUN_FINISHED;
	
    static final int DBNRUN_STARTED = 0;
    static final int DBNRUN_FINISHED = 1;
    static final int DBNRUN_ERROR = -1;
    static final int DBNRUN_STOPPED = 2;
    
    Thread runner = null;
    long sleepc = 0; // how slow to slow it down


    public DbnRunner(DbnApplet app, DbnGui gui, Panel parent, 
		     int x, int y, int w, int h, String program)
    {
	super();
	this.app = app;
	this.gui = gui;
	this.parent = parent;
	
	dispx = x; dispy = y;
	dispw = w; disph = h;
	dbg = new DbnGraphics(parent, this, w, h);	
	setProgram(program);
    }
	
    public void setDisplayXY(int x, int y)
    {
	dispx = x; dispy = y;
    }
	
    public void setProgram(String program)
    {
	prog = program;
    }
    
    public boolean runningp()
    {
	return (state==DBNRUN_STARTED);
    }

    public void start()
    {
	// threadless version didn't work
	try { 
	    if (runner!=null) {
		runner.stop(); 
		runner=null;
	    } 
	} catch (Exception e) { 
	    runner = null;
	}
	
	if (runner == null) {
	    runner = new Thread(this);
	    runner.start();
	}
    }
	
    // called just before starts
    public void allsettogo()
    {
	state=DBNRUN_STARTED;
	dbg.reset();
    }
    
    // called just when done
    public void alldone()
    {
	// called as the last thing to do
	//	db.im.flush();
	render();
	gui.terminated();
    }
	
    public void stop() {
	if (dp != null) dp.pleaseQuit();
	//msg("Stopped.");
	msg(""); // modified as per jm's wishes
	/*
	  if (runner != null)
	  {
	  // only sandmen kill runners
	  runner.stop();
	  runner = null;
	  state=DBNRUN_STOPPED;
	  }
	  alldone();
	*/
    }
    
    public void msg(String s)
    {
	gui.msg(s);
	sp(s);
    }
    
    public void sp(String s) 
    {
	System.out.println(s);
    }
    
	
    public void run()
    {
	boolean donep = false;
	
	allsettogo();
	// should process program here
	// run must call render while it is running ...
	// must tell processor to slowdown when going to fast ... ? 
	// speed constantcy in dbn ...
	
	try {
	    // THIS IS WHERE THE 'dbnprocessor' should be doing it's thing
	    dp = new DbnProcessorG3(gui, dbg, app);
	    dp.process(prog);
	    dp = null;
	    //Thread.sleep(1000);
	    state=DBNRUN_FINISHED;
	    gui.success();
	    //donep = true;
	} catch (DbnException e) { 
	    //sp("Caught dbn exception");
	    state = DBNRUN_ERROR;
	    gui.reporterror(e);
	    this.stop();
	} catch (Exception e) {
	    e.printStackTrace();
	    this.stop();
	}	
	alldone();
    }
	
    public void render(Graphics g)
    {
	if (dbg.im == null) {
	    dbg.buildbuffers();
	}
	// i think this was throwing an exception on close
	if (g != null) {
	    g.drawImage(dbg.im,dispx,dispy,parent);
	}
    }
    
    public void render()
    {
	render(parent.getGraphics());
    }
}
