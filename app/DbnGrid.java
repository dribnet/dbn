import java.awt.*;
import java.util.*;


// this panel holds 1 or more DbnPanels in a gridded
// layout or the editor-style layout with the numbers

// mouse downs that are captured outside the DbnPanels
// kill the currently 'running' dbn program

public class DbnGrid extends Panel implements DbnEnvironment {
    static final int MARGARINE = 20;

    DbnApplet app;
    String programs[];

    int gx[], gy[];
    int gwidth, gheight;
    DbnGraphics graphics[];
    int gcount;
    int gcurrent;
    DbnRunner runner;
    boolean inited;

    Vector runners;


    public DbnGrid(DbnApplet app, String programs[]) {
	this.app = app;
	this.programs = programs;

	gwidth = 101;
	gheight = 101;

	runners = new Vector();

	gcount = programs.length;
	graphics = new DbnGraphics[gcount];
	for (int i = 0; i < gcount; i++) {
	    graphics[i] = new DbnGraphics(gwidth, gheight);
	    graphics[i].disable();
	    add(graphics[i]);
	}
	gx = new int[gcount];
	gy = new int[gcount];
	
	gcurrent = -1;
	setBackground(Color.orange);    
    }


    public Dimension preferredSize() {
	//return new Dimension(gwidth + 150, 350);
	int count = programs.length;
	int wide = Math.min(count, 4);
	int high = (int)Math.ceil((float)count / (float)wide);
	return new Dimension(50 + wide*gwidth + MARGARINE*(wide-1),
		     50 + high*gheight + MARGARINE*(high-1));
	//return new Dimension(1024, 768);
    }

    //public void update(Graphics g) {
    //paint(g);
    //}

    public void paint(Graphics screen) {
	if (!inited) {
	    setLayout(null);
	    Dimension dim = size();

	    //int margin = 20;
	    int ncols = (dim.width - MARGARINE) / (gwidth+MARGARINE);
	    int marginX = (dim.width - (ncols*gwidth + (ncols-1)*MARGARINE)) / 2;
	    //int marginY = marginX;
	    int nrows = (gcount + ncols-1) / ncols;
	    int marginY = (dim.height - (nrows*gheight + (nrows-1)*MARGARINE)) / 2;
	    
	    if (gcount == 1) {
		gx[0] = (dim.width - gwidth) / 2;
		gy[0] = (dim.height - gheight) / 2;
	    } else {
		int x = 0, y = 0;
		for (int i = 0; i < gcount; i++) {
		    gx[i] = x + marginX;
		    gy[i] = y + marginY;
		    
		    if (((i+1) % ncols) == 0) {
			y += gheight + MARGARINE;
			x = 0;
		    } else {
			x += gwidth + MARGARINE;
		    }
		}
	    }
	    for (int i = 0; i < gcount; i++) {
		graphics[i].reshape(gx[i], gy[i], gwidth, gheight);
	    }
	    inited = true;
	}
	//paintAll(screen);

	// draw rectangles around each of the little guys
    }


    public boolean mouseDown(Event ev, int x, int y) {
	if (gcurrent != -1) {
	    //Rectangle r = graphics[gcurrent].getBounds();
	    if (graphics[gcurrent].contains(x, y)) {
		return true;
	    } else {
		// kill the currently running applet
		//graphics[gcurrent].terminate();
		terminate();
		//graphics[gcurrent].disable();
		//gcurrent = -1;
	    }
	}
	// figure out what was selected
	for (int i = 0; i < gcount; i++) {
	    Rectangle r = graphics[i].getBounds();
	    //if (graphics[i].contains(x, y)) {
	    if (r.contains(x, y)) {
		//System.err.println("starting " + i);
		//graphics[gcurrent].reset();
		gcurrent = i;
		runner = new DbnRunner(programs[i], graphics[gcurrent], this);
		runner.start();
		graphics[gcurrent].enable();
		runners.addElement(runner);
	    }
	}
	return true;
    }


    // DbnEnvironment duties

    public void terminate() {
	//System.err.println("stopping " + runner);
	if (runner != null) {
	    runner.stop();
	    graphics[gcurrent].disable();
	    gcurrent = -1;

	    runners.removeElement(runner);
	    if (runners.size() > 0) {
		// some bizarre state that can show up on an
		// inconsistency of some kind. yuck.
		//System.err.println("golan GOLAN GOLLANNN!");
		Enumeration e = runners.elements();
		while (e.hasMoreElements()) {
		    DbnRunner deadguy = (DbnRunner)e.nextElement();
		    deadguy.stop();
		}
		for (int i = 0; i < gcount; i++) {
		    graphics[gcurrent].disable();
		}
	    }
	}
	//System.err.println("forced termination not implemented");
    }
    
    public void error(DbnException e) {
	System.err.println(e.getMessage() + " (line " + e.line + ")");
	e.printStackTrace();
    }

    public void finished() { }

    public void message(String msg) {
	System.out.println(msg);
    }
}
