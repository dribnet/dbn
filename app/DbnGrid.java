#ifndef KVM
#ifndef PLAYER


// doesn't actually need app passed in to the constructor

// need to find a good generic solution to the multiple runner issue


import java.awt.*;
import java.util.*;

import java.awt.event.*;

// this panel holds 1 or more DbnPanels in a gridded
// layout or the editor-style layout with the numbers

// mouse downs that are captured outside the DbnPanels
// kill the currently 'running' dbn program

// DbnGrid is jdk 1.1 only.. this is only used by the 
// courseware and by downloadable, both of which require it

public class DbnGrid extends Panel implements DbnEnvironment, MouseListener {
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


  public DbnGrid(DbnApplet app, String progs[]) {
    this.app = app;
    setup(progs);
  }

  public void setup(String programs[]) {
    this.programs = programs;

    gwidth = DbnApplet.getInteger("graphics_width", 101);
    gheight = DbnApplet.getInteger("graphics_height", 101);

    runners = new Vector();

    gcount = programs.length;
    graphics = new DbnGraphics[gcount];
    for (int i = 0; i < gcount; i++) {
      graphics[i] = new DbnGraphics(gwidth, gheight);

      DbnGridBoobyPrize booby = new DbnGridBoobyPrize(graphics[i]);
      graphics[i].addMouseListener(booby);
      graphics[i].addMouseMotionListener(booby);
      graphics[i].addKeyListener(booby);

      //graphics[i].disable();
      graphics[i].addMouseListener(this);
      add(graphics[i]);
    }
    gx = new int[gcount];
    gy = new int[gcount];
	
    gcurrent = -1;
    //setBackground(Color.orange);
    //setBackground(new Color(230, 230, 230));
    setBackground(Color.white);

    //System.out.println("creating new dbngrid");
    addMouseListener(this);
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
    for (int i = 0; i < gcount; i++) {
      if (programs[i] == null) {
	screen.setColor(Color.lightGray);
      } else {
	screen.setColor(Color.black);
      }
      screen.drawRect(gx[i]-1, gy[i]-1, gwidth+1, gheight+1);
    }
  }

  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }
  
  public void mousePressed(MouseEvent e) {
    //mouseClicked(e);
  }

  public void mouseClicked(MouseEvent e) {
    //int x = e.getX();
    //int y = e.getY();
    //System.out.println("mouse is down at " + x + ", " + y);
    Object source = e.getSource();
    //System.out.println("mouse is down at " + x + ", " + y);
    //System.out.flush();
    //public boolean mouseDown(Event ev, int x, int y) {

    if (gcurrent != -1) {
      //Rectangle r = graphics[gcurrent].getBounds();
      //if (graphics[gcurrent].inside(x, y)) {  // grrr.. jdk10
      if (source == graphics[gcurrent]) {
	//return true;
	System.out.println("(ignoring.. clicked the same)");
	return;
      } else {
	System.out.println("(new selection.. killing what's currently running)");
	// kill the currently running applet
	//graphics[gcurrent].terminate();
	terminate();
	//graphics[gcurrent].disable();
	//gcurrent = -1;
      }
    }
    gcurrent = -1;
    for (int i = 0; i < gcount; i++) {
      if (source == graphics[i]) {
	System.out.println("setting gcurrent to " + i);
	//x += gx[i];
	//y += gy[i];
	gcurrent = i;
	break;
      }
    }
    System.out.println("  gcurrent = " + gcurrent);
    if (gcurrent == -1) {
      System.out.println("  source is " + source);
    }
    if (gcurrent != -1) {
    //System.out.flush();
    // figure out what was selected
    //for (int i = 0; i < gcount; i++) {
      //#ifdef JDK11
    //Rectangle r = graphics[i].getBounds();
      //System.out.println("checking " + i + " " + r);
      //System.out.flush();
      //#else
      //Rectangle r = graphics[i].bounds();
      //#endif
      //if (r.contains(x, y)) {
      //if (r.inside(x, y)) {  // grr.. jdk10
	//System.out.println("starting " + i);
      //System.out.flush();
	//graphics[gcurrent].reset();
      //gcurrent = i;
      runner = new DbnRunner(programs[gcurrent], graphics[gcurrent], this);
      runner.start();
      graphics[gcurrent].enable();
      graphics[gcurrent].setCurrentDbnGraphics();
      runners.addElement(runner);
      System.out.println("  starting " + gcurrent);
      /*
      } else {
	System.out.println("nope. " + " " + gx[i] + " " + gy[i]);
	if ((x > r.x) && (x < r.x + r.width) &&
	    (y > r.y) && (y < r.y + r.height)) {
	  System.out.println("shoulda been");
	}
      }
      */
    }
    //return true;
    return;
  }


  // DbnEnvironment duties

  public void terminate() {
    //System.err.println("stopping " + runner);
    if (runner != null) {
      runner.stop();
      //graphics[gcurrent].disable();
      gcurrent = -1;

      runners.removeElement(runner);
      if (runners.size() > 0) {
	// some bizarre state that can show up on an
	// inconsistency of some kind. yuck.
	Enumeration e = runners.elements();
	while (e.hasMoreElements()) {
	  DbnRunner deadguy = (DbnRunner)e.nextElement();
	  deadguy.stop();
	}
	//for (int i = 0; i < gcount; i++) {
	  //graphics[gcurrent].disable();
	//}
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

  public boolean keyDown(Event ev, int n) {
    System.out.println((char) n);
    return false;
  }
}


// worthless piece of shit code

class DbnGridBoobyPrize implements KeyListener, 
  MouseListener, MouseMotionListener
{
  DbnGraphics graphics;

  public DbnGridBoobyPrize(DbnGraphics graphics) {
    this.graphics = graphics;
  }

  public void mouseEntered(MouseEvent e) { 
    graphics.mouseEnter(null, e.getX(), e.getY());
  }

  public void mouseExited(MouseEvent e) { 
    graphics.mouseExit(null, e.getX(), e.getY());
  }
  
  public void mousePressed(MouseEvent e) {
    graphics.mouseDown(null, e.getX(), e.getY());
  }

  public void mouseReleased(MouseEvent e) { 
    graphics.mouseUp(null, e.getX(), e.getY());
  }

  public void mouseClicked(MouseEvent e) {
    graphics.mouseDown(null, e.getX(), e.getY());
    graphics.mouseUp(null, e.getX(), e.getY());
  }

  public void mouseDragged(MouseEvent e) {
    graphics.mouseDrag(null, e.getX(), e.getY());
  }

  public void mouseMoved(MouseEvent e) {
    graphics.mouseMove(null, e.getX(), e.getY());
  }

  public void keyPressed(KeyEvent e) {
    graphics.keyDown(null, (int)e.getKeyChar());
  }

  public void keyReleased(KeyEvent e) {
    graphics.keyUp(null, (int)e.getKeyChar());
  }

  public void keyTyped(KeyEvent e) {
    graphics.keyDown(null, (int)e.getKeyChar());
    graphics.keyUp(null, (int)e.getKeyChar());
  }
}


#endif
#endif
