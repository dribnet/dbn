import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;


// _ hit a key to go to favorites
// _   or make markings for 'nice' stuff
// _ mouse/scrolling spring
// _ when something doesn't run, make sure lastImage still updated

// make this a publicly accessible adaptive space, where people
// can vote on which pieces they like the most, composition 
// rearranges based on preferences

// _ launching into editor to make fixes and updates
// _ indicator for error conditions
// _ hiding might be the fix for the courseware as well
// _ use axel's parameter space stuff
// _ viewing the code as the same kind of map
// _ make the pop-up text less ugly

// X have a single graphics
// X hide the graphics when not in use
// X move it when mouse drags
// X double click will start the applet
// X click outside stops applet
// X paint on startup
// X clean up boogery text stuff
// X make image update based on what's left behind
// X better solution for text in general
// X second double-click will stop applet
// X save new image after changes while running

public class PockyVision extends Window implements DbnEnvironment, Runnable {
  static final int HCOUNT = 46;
  static final int VCOUNT = 46;
  static final int COUNT = HCOUNT * VCOUNT;

  static final int WIDE = 101;
  static final int HIGH = 101;

  static final int IMAGE_WIDTH = HCOUNT * WIDE;
  static final int IMAGE_HEIGHT = VCOUNT * HIGH;
  static final int IMAGE_COUNT = IMAGE_WIDTH * IMAGE_HEIGHT;

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

  Thread thread;
  float fx, fy;
  float vx, vy;
  float px, py;
  int maxOffsetX;
  int maxOffsetY;


  static public void main(String args[]) {
    new PockyVision();
  }

  public PockyVision() {
    super(new Frame());
    app = new DbnApplication();
    app.frame.removeWindowListener(app.windowListener);
    app.frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	e.getWindow().hide();
      }
    });

    graphics = new DbnGraphics(WIDE, HIGH, Color.white);
    add(graphics);
    graphics.setVisible(false);

    pack();
    Toolkit tk = getToolkit();
    screen = tk.getScreenSize();
    setBounds(0, 0, screen.width, screen.height);
    maxOffsetX = IMAGE_WIDTH - screen.width;
    maxOffsetY = IMAGE_HEIGHT - screen.height;

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

    thread = new Thread(this);
    thread.start();
  }

  public boolean keyDown(Event e, int k) {
    if (k == 's') saveImage();
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

  public void paint(Graphics screeng) {
    if (image != null) {
      screeng.drawImage(image, offsetX, offsetY, null);

      screeng.setColor(Color.lightGray);
      if (offsetX > 0) 
	screeng.fillRect(0, 0, offsetX, screen.height);
      if (offsetY > 0)
	screeng.fillRect(0, 0, screen.width, offsetY);
      if (offsetX < -maxOffsetX)
	screeng.fillRect(offsetX + IMAGE_WIDTH, 0, 
			 screen.width, screen.height);
      if (offsetY < -maxOffsetY)
	screeng.fillRect(0, offsetY + IMAGE_HEIGHT,
			 screen.width, screen.height);
      //fx = (-maxOffsetX - offsetX) * TIGHTNESS;
      //if (offsetY < -maxOffsetY) fy = (-maxOffsetY - offsetY) * TIGHTNESS;
    }
  }


  synchronized public boolean mouseDown(Event e, int x, int y) {
    int selX = (-offsetX + x) / WIDE;
    int selY = (-offsetY + y) / HIGH;
    int sel = selY * HCOUNT + selX;

    if (e.controlDown()) {
      //String args[] = new String[1];
      //args[0] = filenames[sel];
      //DbnApplication.main(args);
      setProgramFile(filenames[sel]);
      mouseMode = IGNORING;
      return false;
    }

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
    if ((e.clickCount == 2) && (sel == runningIndex)) {
      // just stop what's running
      exterminate();
      return false;
    }
    exterminate();
    if (e.clickCount == 2) {
      //if (e.controlDown()) {
      //System.out.println("gonna start " + filenames[sel]);

      if (programs[sel] == null) {
	programs[sel] = app.readFile(filenames[sel]);
      }
      gx = (selX * WIDE) + offsetX;
      gy = (selY * HIGH) + offsetY;
      /*
	// by definition, this won't happen
      if (programs[sel] == null) {
	System.out.println(filenames[sel] + " not found");
	g.setColor(Color.orange);
	g.fillRect(gx, gy, WIDE, HIGH);
	long t = System.currentTimeMillis();
	while (System.currentTimeMillis() - t < 2000) { }
	update();
	return false;
      }
      */
      runner = new DbnRunner(programs[sel], graphics, this);
      runner.start();
      graphics.setVisible(true);
      runners.addElement(runner);

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


  public boolean mouseDrag(Event e, int x, int y) {
    //if (ignoreDrag) return false;
    if (mouseMode == MOVING) {
      int deltaX = x - lastMouseX;
      int deltaY = y - lastMouseY;

      fx += deltaX;
      fy += deltaY;
      /*
      offsetX += deltaX; 
      offsetY += deltaY; 
      gx += deltaX; 
      gy += deltaY;
      //graphics.setLocation(gx, gy);
      update();
      */

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


  synchronized public void exterminate() {
    if (runningIndex != -1) {
      //System.out.println("replacing");
      int selX = runningIndex % HCOUNT;
      int selY = runningIndex / HCOUNT;
      Graphics ig = image.getGraphics();
      ig.drawImage(graphics.dbnImage /*graphics.lastImage*/, 
		   selX*WIDE, selY*HIGH, WIDE, HIGH, null);
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


  public void run() {
    while (Thread.currentThread() == thread) {
      vx = (vx + fx) * 0.3f;
      vy = (vy + fy) * 0.3f;
      //px += vx;
      //py += vy;

      //System.out.println(vx + " " + vy);
      int dx = (int) vx;
      int dy = (int) vy;
      if ((dx != 0) && (dy != 0)) {
	offsetX += dx;
	offsetY += dy;

	final float TIGHTNESS = 0.75f;
	if (offsetX > 0) fx = -offsetX * TIGHTNESS;
	if (offsetY > 0) fy = -offsetY * TIGHTNESS;
	if (offsetX < -maxOffsetX) fx = (-maxOffsetX - offsetX) * TIGHTNESS;
	if (offsetY < -maxOffsetY) fy = (-maxOffsetY - offsetY) * TIGHTNESS;
	//if (offsetY < 0) fy = -offsetY;
	//if (offsetX > maxOffsetX) offsetX = maxOffsetX;
	
	//gx += (int) vx;
	//gy += (int) vy;
	//graphics.setLocation(gx, gy);
	
	fx *= 0.9f;
	fy *= 0.9f;
	vx *= 0.9f;
	vy *= 0.9f;
	/*
	  offsetX += deltaX; 
	  offsetY += deltaY; 
	  gx += deltaX; 
	  gy += deltaY;
	  //graphics.setLocation(gx, gy);
	*/
	update();
      }
      try {
	thread.sleep(20);
	//System.out.println(offsetX + " " + offsetY);
      } catch (InterruptedException e) { }
    }
  }


  public void saveImage() {
    try {
      FileOutputStream fos = new FileOutputStream("pocky.update.raw");
      int pixels[] = new int[IMAGE_COUNT];
      byte bytes[] = new byte[IMAGE_COUNT];

      System.out.println("grabbing pixels..");
      PixelGrabber pg = 
	new PixelGrabber(image, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT,
			 pixels, 0, IMAGE_WIDTH);
      try {
	pg.grabPixels();
      } catch (InterruptedException e) {
      }
      System.out.println("done grabbing");

      int lastPercent = 0;
      for (int i = 0; i < IMAGE_COUNT; i++) {
	fos.write((byte) (pixels[i] & 0xff));
	if ((i % IMAGE_WIDTH) == 0) 
	  System.out.println(((int) (100 * (float)i / (float)IMAGE_COUNT)) + "%");
      }
      //for (int i = 0; i < IMAGE_COUNT; i++) {
      //bytes[i] = (byte) (pixels[i] & 0xff);
      //}
      //fos.write(bytes);

      fos.flush();
      fos.close();
      System.out.println("Wrote new image, size is " + 
			 IMAGE_WIDTH + " x " + IMAGE_HEIGHT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public boolean gotFocus(Event e, Object o) {
    //System.out.println("got it");
    //getToolkit().beep();
    graphics.setCurrentDbnGraphics();
    return false;
  }

  public boolean lostFocus(Event e, Object o) {
    DbnEditor editor = (DbnEditor)app.environment;
    editor.graphics.setCurrentDbnGraphics();
    return false;
  }


  public void setProgramFile(String programFile) {
    DbnEditor editor = (DbnEditor)app.environment;
    app.frame.setTitle(programFile);
    editor.textarea.setText(app.readFile(programFile));
    File lastFileObject = new File(programFile);
    editor.lastDirectory = lastFileObject.getParent();
    editor.lastFile = lastFileObject.getName();
    //System.out.println(editor.lastDirectory + " " + editor.lastFile);
    app.frame.show();
    app.frame.toFront();
  }


  public void terminate() { } 

  public void error(DbnException e) { 
    e.printStackTrace();

    g.setColor(Color.orange);
    g.fillRect(gx, gy, WIDE, HIGH);
    long t = System.currentTimeMillis();
    while (System.currentTimeMillis() - t < 2000) { }
    update();
    //return false;
  }

  public void finished() { }
  public void message(String msg) { }
}
