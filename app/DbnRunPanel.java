import java.awt.*;
import java.util.*;

// make entire paint method render into an offscreen buffer
// this is what's so slow on the mac!

public class DbnRunPanel extends Panel {
    DbnApplet app;

    int runnerCount;
    DbnRunner runners[];
    int runnerX[];
    int runnerY[];
    int runnerWidth;
    int runnerHeight;
    int current;

    int emouse[], ekey[], etime[];
    long keyt[] = new long[26];

    Font plainFont = new Font("Helvetica", Font.PLAIN, 10);
    //Font boldFont = new Font("Helvetica", Font.BOLD, 10);

    String titling;
    Color titlingColor;
    Color tickColor;
    Color bgColor;
    Color bgStippleColor;

    //int dispx, dispy, dispw, disph;
    //int dispX, dispY;

    //Graphics screen;
    Image offscreen;
    boolean offscreenValid;
    Dimension offscreenDim;


    public DbnRunPanel(DbnApplet app, DbnGui gui, String progs[],
		       String titling, Color titlingColor, Color tickColor,
		       Color bgColor, Color bgStippleColor) {
	this.app = app;

	this.titling = titling;
	this.titlingColor = titlingColor;
	this.tickColor = tickColor;
	this.bgColor = bgColor;
	this.bgStippleColor = bgStippleColor;

	runnerWidth = 101;
	runnerHeight = 101;

	runnerCount = progs.length;
	runners = new DbnRunner[runnerCount];
	for (int i = 0; i < runnerCount; i++) {
	    runners[i] = new DbnRunner(app, gui, this, 
				       runnerWidth, runnerHeight, 
				       progs[i]);
	}
	runnerX = new int[runnerCount];
	runnerY = new int[runnerCount];
	setRunner(0);
    }


    public void setProgram(String s) {
	runners[current].setProgram(s);
    }


    public void initiate() {
        for (int i = 0; i < 26; i++) {
            ekey[i] = 0;
            keyt[i] = -1;
        }
        for (int i = 0; i < 3; i++) {
	    emouse[i] = 0;
	}
	runners[current].start();
    }


    public void idle(long currentTime) {
	Date d = new Date(); // wooaaah! garbage city!
	etime[0] = d.getHours();
	etime[1] = d.getMinutes();
	etime[2] = d.getSeconds();
	etime[3] = (int) (currentTime % 1000)/10;

	// there is a bug in how key is trapped by win java (expect
	// worse in mac java). does not match keydowns with keyups
	// workaround: store when key comes down, allow valid for
	// certain amount of time, if exceed then flush. autokey
	// events come in as keydown so should refresh naturally
	
	for (int i = 0; i < 26; i++) {
	    if ((ekey[i] == 100) && (currentTime-keyt[i] > 1000)) {
		keyt[i] = -1;
		ekey[i] = 0;
	    }
	}
    }


    public void terminate() {
	runners[current].stop();
    }


    public void setRunner(int current) {
	this.current = current;

	Hashtable connectorTable = 
	    runners[current].dbg.getConnectorTable();
	emouse = (int[]) connectorTable.get("mouse");
	ekey = (int[]) connectorTable.get("key");
	etime = (int[]) connectorTable.get("time");
	
	DbnGraphics.setCurrentGraphics(runners[current].dbg);
	//DbnGraphics.setCurrentGraphics(dbr.dbg);
    }


    private final int letterKey(int n) {
        if ((n >= 'a') && (n <= 'z')) return n - 'a';
	if ((n >= 'A') && (n <= 'Z')) return n - 'A';
	return -1;
    }

    public boolean keyDown(Event ev, int n) {
	if (n == 27) app.gui.terminate();  // ooh.. ugly

	int which = letterKey(n);
	if (which == -1) return false;
	keyt[which] = System.currentTimeMillis();
	ekey[which] = 100;
        return true;
    }

    public boolean keyUp(Event ev, int n) {
	int which = letterKey(n);
	if (which == -1) return false;
	keyt[which] = -1;
	ekey[which] = 0;
        return true;
    }


    public boolean mouseDown(Event ev, int x, int y) {
	if (runnerCount != 1) {
	    int clicked = -1;
	    for (int i = 0; i < runnerCount; i++) {
		if ((x > runnerX[i]) && 
		    (x < runnerX[i] + runnerWidth) &&
		    (y > runnerY[i]) && 
		    (y < runnerY[i] + runnerHeight)) {
		    clicked = i; break;
		}
	    }
	    if (clicked != -1) {
		if (clicked != current) {
		    // hit something new, terminate old
		    terminate();
		    setRunner(clicked);
		    initiate();
		} else {
		    // if it is not running, initiate it again
		    if (!runners[current].isRunning()) initiate();
		}
	    } else {
		// didn't hit anything, terminate 
		terminate();
	    }
	}
	updateMouse(x, y);
	emouse[2] = 100;
	return true;
    }


    public boolean mouseUp(Event ev, int x, int y) {
	emouse[2] = 0;
	return updateMouse(x, y);
    }

    public boolean mouseMove(Event ev, int x, int y) {
	return updateMouse(x, y);
    }
 
    public boolean mouseDrag(Event ev, int x, int y) {
	return updateMouse(x, y);
    }

    public boolean mouseEnter(Event ev, int x, int y) {
	return updateMouse(x, y);
    }

    public boolean mouseExit(Event ev, int x, int y) {
	//terminate();
	//return true;
	return updateMouse(x, y);
    }

    private final boolean updateMouse(int x, int y) {
	emouse[0] = x - runnerX[current];
	emouse[1] = runnerHeight - (y - runnerY[current]);
	return true;
    }


    public void griddify(int width, int height) {
	final int margin = 20;
	int ncols = (width - margin) / (runnerWidth+margin);
	int marginX = (width - (ncols*runnerWidth + (ncols-1)*margin)) / 2;
	//int marginY = marginX;
	int nrows = (runnerCount + ncols-1) / ncols;
	int marginY = (height - (nrows*runnerHeight + (nrows-1)*margin)) / 2;

	if (runnerCount == 1) {
	    runnerX[0] = (width - runnerWidth) / 2;
	    runnerY[0] = (height - runnerHeight) / 2;
	    return;
	}

	int x = 0, y = 0;
	for (int i = 0; i < runnerCount; i++) {
	    runnerX[i] = x + marginX;
	    runnerY[i] = y + marginY;

	    if (((i+1) % ncols) == 0) {
		y += runnerHeight + margin;
		x = 0;
	    } else {
		x += runnerWidth + margin;
	    }
	}
    }

    public Dimension preferredSize() {
	return new Dimension(runnerWidth + 150, 350);
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics screen) {
	Dimension dim = size();
	if ((offscreen == null) ||
	    (dim.width != offscreenDim.width) ||
	    (dim.height != offscreenDim.height)) {
	    offscreen = createImage(dim.width, dim.height);
	    offscreenDim = dim;
	    offscreenValid = false;
	}
	terminate();
	if (!offscreenValid) {
	    Graphics g = offscreen.getGraphics();
	    griddify(offscreenDim.width, offscreenDim.height);

	    // draw background
	    g.setColor(bgColor);
	    g.fillRect(0, 0, offscreenDim.width, offscreenDim.height);
	    if (!bgColor.equals(bgStippleColor)) {
		g.setColor(bgStippleColor);
		int count = 2 * Math.max(offscreenDim.width, 
					 offscreenDim.height);
		for (int i = 0; i < count; i += 2) {
		    g.drawLine(0, i, i, 0);
		}
	    }
	    g.setFont(plainFont);
	    FontMetrics metrics = g.getFontMetrics();
	    int lineheight = metrics.getAscent() + 
		metrics.getDescent();

	    int rx = runnerX[current];
	    int ry = runnerY[current];

	    // put ticks around (only if in edit mode)
	    if (tickColor != null) {
		g.setColor(tickColor);
		final int increment = 20;
		int x, y;
		y = ry + runnerHeight;
		for (x = 0; x < runnerWidth; x += increment) {
		    g.drawLine(rx + x, y, rx + x, y + 4);
		    String num = String.valueOf(x);
		    g.drawString(num, rx + x - 1, y + 4 + lineheight);
		}
		for (y = 0; y < runnerHeight; y += increment) {
		    g.drawLine(rx - 4, ry + y, rx, ry + y);
		    String num = String.valueOf(y);
		    int numWidth = metrics.stringWidth(num);
		    g.drawString(num, rx - 6 - numWidth, 
				 ry + runnerHeight - y);
		}
	    }
	    // put the little message below
	    if (titling != null) {
		g.setColor(titlingColor);
		int y = ry + runnerHeight + lineheight*2 + 4;
		StringTokenizer st = new StringTokenizer(titling,";");
		
		while (st.hasMoreTokens()) {
		    String s = st.nextToken();
		    g.drawString(st.nextToken(), rx, y);
		    y += lineheight;
		}
	    }
	    // draw DbnRunners
	    //runners[current].render(); // hmph
	    for (int i = 0; i < runnerCount; i++) {
		//runners[i].render();  // doesn't really draw..
		g.setColor(Color.black);
		g.drawRect(runnerX[i]-1, runnerY[i]-1,
			   runnerWidth+1, runnerHeight+1);
	    }		
	}
	screen.drawImage(offscreen, 0, 0, this);
	runners[current].render();
    }


    Graphics screeng;  // this seems ugly
    public void update(Image image) {
	if (screeng == null) screeng = this.getGraphics();
	screeng.drawImage(image, runnerX[current], 
			  runnerY[current], this);
    }
}
