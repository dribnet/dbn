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
  //boolean ignoreDrag; // set if mouseDown inside the Graphics

  FontMetrics metrics;
  int ascent, descent;

  static final Color textColor = new Color(153, 0, 0);
  static final Color textFrameColor = new Color(204, 204, 204);
  static final Color textFillColor = Color.white;

  static final int MOVING = 0;
  static final int STARTING = 1;
  static final int NAMING = 2;
  static final int IGNORING = 3;
  int mouseMode;


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
    show();
    toFront();
  }

  public boolean keyDown(Event e, int k) {
    if (k == 27) System.exit(0);
    return false;
  }

  public void update() {
    if (g == null) {
      g = this.getGraphics();
      //g.setColor(Color.orange);

      g.setFont(new Font("SansSerif", Font.PLAIN, 12));
      metrics = g.getFontMetrics();
      ascent = metrics.getAscent();
      descent = metrics.getDescent();
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
      //} else {
      //reupdate();
    }
    //paintComponents();
  }


  synchronized public boolean mouseDown(Event e, int x, int y) {
    int selX = (-offsetX + x) / WIDE;
    int selY = (-offsetY + y) / HIGH;
    int sel = selY * HCOUNT + selX;

    if (e.shiftDown()) {
      nameSelection(x, y, selX, selY, sel);
      //ignoreDrag = true;
      mouseMode = NAMING;
      return false;
    }

    //if ((sel == runningIndex) && (!e.controlDown())) {
    if ((sel == runningIndex) && (e.clickCount != 2)) {
      mouseMode = IGNORING;
      //ignoreDrag = true;
      return false;
    } else {
      //ignoreDrag = false;
      mouseMode = MOVING;
    }
    exterminate();

    if (e.clickCount == 2) {
      //if (e.controlDown()) {
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
      mouseMode = STARTING;
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
      ig.drawImage(graphics.lastImage, selX*WIDE, selY*HIGH,
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

  protected void nameSelection(int x, int y, int selX, int selY, int sel) {
    if (g == null) return;

    update(); // clear it out
    int y1 = (selY * HIGH) + offsetY + HIGH + 3;
    //int ascent = metrics.getAscent();
    //int descent = metrics.getDescent();
    int textY = y1 + 3 + ascent;
    int y2 = textY + descent + 3;
    int extentY = y2 - y1;

    int x1 = x;
    int textX = x1 + 3;
    int x2 = textX + metrics.stringWidth(filenames[sel]) + 3;
    int extentX = x2 - x1;

    g.setColor(textFillColor);
    g.fillRect(x1, y1, extentX, extentY);
    g.setColor(textFrameColor);
    g.drawRect(x1, y1, extentX, extentY);
    g.setColor(textColor);
    g.drawString(filenames[sel], textX, textY);
  }

  public boolean mouseDrag(Event e, int x, int y) {
    //if (ignoreDrag) return false;
    if (mouseMode == MOVING) {
      int deltaX = x - lastMouseX;
      int deltaY = y - lastMouseY;
      offsetX += deltaX;
      offsetY += deltaY;
      gx += deltaX;
      gy += deltaY;
      //graphics.setLocation(gx, gy);
      update();

    } else if (mouseMode == NAMING) {
      int selX = (-offsetX + x) / WIDE;
      int selY = (-offsetY + y) / HIGH;
      int sel = selY * HCOUNT + selX;
      nameSelection(x, y, selX, selY, sel);
    }
    lastMouseX = x;
    lastMouseY = y;
    return false;
  }

  public boolean mouseUp(Event e, int x, int y) {
    if (mouseMode == NAMING) {
      update();
    }
    return false;
  }

  public void terminate() { } 
  public void error(DbnException e) { }
  public void finished() { }
  public void message(String msg) { }
}
