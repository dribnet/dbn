import java.awt.*;
import java.io.*;
import java.util.*;


// _ mouse/scrolling spring
// _ clean up boogery text stuff

// _ save new image after changes while running
// X make image update based on what's left behind

// hiding might be the fix for the courseware as well

// use axel's parameter space stuff

// viewing the code as the same kind of map
// better solution for text in general

// X have a single graphics
// X hide the graphics when not in use
// X move it when mouse drags
// X double click will start the applet
// X click outside stops applet
// X paint on startup


public class PockyVision extends Window implements DbnEnvironment {
  static final int HCOUNT = 46;
  static final int VCOUNT = 46;
  static final int COUNT = HCOUNT * VCOUNT;

  static final int WIDE = 101;
  static final int HIGH = 101;

  static final int IMAGE_WIDTH = HCOUNT * WIDE;
  static final int IMAGE_HEIGHT = VCOUNT * HIGH;

  int width, height;
  int offsetX, offsetY;
  Dimension screen;

  Image image;
  Graphics g;
  int lastMouseX, lastMouseY;

  String filenames[];
  String programs[];
  int runningIndex;
  DbnGraphics graphics;
  int gx, gy;
  DbnRunner runner;
  Vector runners = new Vector();
  DbnApplication app;
  boolean ignoreDrag; // set if mouseDown inside the Graphics


  static public void main(String args[]) {
    new PockyVision();
  }

  public PockyVision() {
    super(new Frame());
    app = new DbnApplication();

    graphics = new DbnGraphics(WIDE, HIGH);
    add(graphics);
    graphics.setVisible(false);

    pack();
    Toolkit tk = getToolkit();
    screen = tk.getScreenSize();
    setBounds(0, 0, screen.width, screen.height);

    try {
      FileInputStream fis = new FileInputStream("pocky.list");
      InputStreamReader isr = new InputStreamReader(fis);
      BufferedReader reader = new BufferedReader(isr);
      filenames = new String[COUNT];
      programs = new String[COUNT];
      for (int i = 0; i < COUNT; i++) {
	filenames[i] = reader.readLine();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    Image original = tk.getImage("pocky.gif");
    image = createImage(IMAGE_WIDTH, IMAGE_HEIGHT);

    //image = tk.getImage("pocky.gif");
    MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(image, 0);
    tracker.addImage(original, 1);
    try {
      tracker.waitForAll();
    } catch (InterruptedException e) { } 

    Graphics ig = image.getGraphics();
    ig.drawImage(original, 0, 0, this);

    runningIndex = -1;
    //repaint();
    show();
    toFront();
  }

  public boolean keyDown(Event e, int k) {
    if (k == 27) System.exit(0);
    return false;
  }

  public void paint() {
    if (g == null) {
      g = this.getGraphics();
      g.setColor(Color.orange);
      g.setFont(new Font("SansSerif", Font.BOLD, 14));
    }
    paint(g);
  }

  public void update(Graphics screen) {
    paint(screen);
  }

  public void paint(Graphics screen) {
    //System.out.println("painting " + image);
    if (image != null) {
      screen.drawImage(image, offsetX, offsetY, null);
    } else {
      repaint();
    }
    //paintComponents();
  }


  synchronized public boolean mouseDown(Event e, int x, int y) {
    int selX = (-offsetX + x) / WIDE;
    int selY = (-offsetY + y) / HIGH;
    int sel = selY * HCOUNT + selX;

    if (e.shiftDown()) {
      if (g != null) {
	if (sel == runningIndex) {
	  y = (selY * HIGH) + offsetY + HIGH + 20;
	}
	paint();
	g.drawString(filenames[sel], x, y);
      }
      ignoreDrag = true;
      return false;
    }

    if ((sel == runningIndex) && (!e.controlDown())) {
      ignoreDrag = true;
      return false;
    } else {
      ignoreDrag = false;
    }
    exterminate();

    //if (e.clickCount == 2) {
    if (e.controlDown()) {
      //System.out.println("gonna start " + filenames[sel]);

      if (programs[sel] == null) {
	programs[sel] = app.readFile(filenames[sel]);
      }
      runner = new DbnRunner(programs[sel], graphics, this);
      runner.start();
      graphics.setVisible(true);
      runners.addElement(runner);

      gx = (selX * WIDE) + offsetX;
      gy = (selY * HIGH) + offsetY;
      graphics.setBounds(gx, gy, WIDE, HIGH);
      graphics.reset();
      //if (g != null) {
      //g.setColor(Color.orange);
      //g.drawString(filenames[sel], x, y);
      //}
      runningIndex = sel;
    }

    lastMouseX = x;
    lastMouseY = y;
    return false;
  }

  synchronized public void exterminate() {
    if (runningIndex != -1) {
      //System.out.println("replacing");
      int selX = runningIndex % HCOUNT;
      int selY = runningIndex / HCOUNT;
      Graphics ig = image.getGraphics();
      ig.drawImage(graphics.image, selX*WIDE, selY*HIGH,
		   WIDE, HIGH, null);
    }
    Enumeration e = runners.elements();
    while (e.hasMoreElements()) {
      DbnRunner r = (DbnRunner) e.nextElement();
      r.stop();
    }
    runners.removeAllElements();
    graphics.setVisible(false);
    runningIndex = -1;
  }


  public boolean mouseDrag(Event e, int x, int y) {
    if (ignoreDrag) return false;

    int deltaX = x - lastMouseX;
    int deltaY = y - lastMouseY;
    offsetX += deltaX;
    offsetY += deltaY;
    gx += deltaX;
    gy += deltaY;
    graphics.setLocation(gx, gy);

    lastMouseX = x;
    lastMouseY = y;
    paint();
    return false;
  }

  public boolean mouseUp(Event e, int x, int y) {
    return false;
  }

  public void terminate() { } 
  public void error(DbnException e) { }
  public void finished() { }
  public void message(String msg) { }
}
