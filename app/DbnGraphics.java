import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;


// TODO don't create a new Date() on every idle() step
// TODO write memoryimagesource code (call it jdk11_plus)
// DONE add method for setting hostname
// DONE switch grays around the correct way, setup palette

public class DbnGraphics extends Panel {
    // haha, don't want anybody fiddling with the grays
    static private Color grays[];  
    static {
	grays = new Color[101];
	for (int i = 0; i < 101; i++) {
	    int gray = ((100-i)*255/100);
	    grays[i] = new Color(gray, gray, gray);
	}
    }

#ifdef JDK11
    MemoryImageSource source;
#endif
    Image image;
    Graphics g;    
    byte[] pixels;
    byte penColor;
    int pixelCount;
    boolean antialias;
        
    int width, height;
    int width1, height1;

    //DbnRunner dbr;

    int mouse[] = new int[3];
    int key[] = new int[26];
    int array[] = new int[1000];
    long keyTime[] = new long[26];
    //String hostname;  // server for <net>

    long lastpapert;

    boolean explicitRefresh;
    int FDOT = 10;
    int FPAPER = 100;
    int FLINE = 10; 
    int FFIELD = 10;
    int FMAX = 100;

    boolean insideforeverp;
    boolean aiflushp;
    short flushfire = -1; // 0 is paper, 1 is field
    short lastflushfire = -1;
    boolean insiderepeatp = false;
    boolean autoflushenablep = true;
    int repeatlevel = 0;    
    int flushCount = 0;


    public DbnGraphics(int width, int height /*, DbnRunner dbr*/) {
	this.width = width;
	this.height = height;
	width1 = width - 1;
	height1 = height - 1;
	
	//this.dbr = dbr;
	aiflushp = true;

	pixelCount = width * height;
	pixels = new byte[pixelCount];  // all set to zero
	penColor = 100;

#ifdef JDK11
	//source = new MemoryImageSource();
#endif

#ifdef CRICKET
	openSensor();
#endif
	//currentDbnGraphics = this;
	//dbr.render();
    }


    // uglyish hack for scheme/python, the fix is even uglier, though
    static DbnGraphics currentDbnGraphics;

    static public DbnGraphics getCurrentGraphics() {
	return currentDbnGraphics;
    }

    static public void setCurrentGraphics(DbnGraphics dbg) {
	currentDbnGraphics = dbg;
    }

    public void setCurrentDbnGraphics() {
	currentDbnGraphics = this;
    }

    /////////////////////////////////////////////////////////////

    // actual methods relating to dbn calls

    
    public void norefresh() {
	explicitRefresh = true;
    }


    public void refresh() {
	flushCount = flushCount % 100; //im.flush(); 
	flushCount = 0;
	update();
	//dbr.render(); 
    }


    public void paper(int val) {
	paperFlush();
	// voluntary slowdown
	/*
	try {
	    long curt = System.currentTimeMillis();
	    long sleept = curt-lastpapert;
	    // this enforces a 10 millisecond interval to occur 
	    // between papers (to compensate for fast/slwo machines)
	    if (sleept<10)
		sleept = 10-sleept;
	    else
		sleept = 0;
	    Thread.sleep(sleept);
	    lastpapert = curt;
	} catch (Exception e) { }
	*/

	byte bval = (byte) bound(val, 100);
	for (int i = 0; i < pixelCount; i++) pixels[i] = bval;
	g.setColor(grays[bval]);
	g.fillRect(0, 0, width, height);
    }


    public void field(int x1, int y1, int x2, int y2, int val) {
	// don't even draw if it's completely offscreen
	if (((x1 < 0) && (x2 < 0)) || 
	    ((x1 > width1) && (x2 > width1))) return;
	if (((y1 < 0) && (y2 < 0)) ||
	    ((y1 > height1) && (y2 > height1))) return;
	  	
	x1 = bound(x1, width1);
	y1 = bound(y1, height1);
	x2 = bound(x2, width1);
	y2 = bound(y2, height1);
	val = bound(val, 100);
	fieldFlush();
	
	if (x2 < x1) { int dummy = x1; x1 = x2; x2 = dummy; }
	if (y2 < y1) { int dummy = y1; y1 = y2; y2 = dummy; }
	
	byte bval = (byte) val;
	for (int j = y1; j <= y2; j++) {
	    int pp = width*(height1-j);
	    for (int i = x1; i <= x2; i++) {
		pixels[pp+i] = bval;
	    }
	}		
	g.setColor(grays[val]);

	// don't look at this code too long, you'll hurt your head
	y1 = height1 - y1;
	y2 = height1 - y2;
	int w = x2 - x1 + 1;
	int h = y1 - y2 + 1;
	g.fillRect(x1, y2, w, h);
    }


    public void pen(int val) {
	penColor = (byte)bound(val, 100);
    }


    public void antialias(int m) {
	antialias = (m != 0);
    }


    // line clipping code appropriated from 
    // "Computer Graphics for Java Programmers"

    private void intensifyPixel(int x, int y, float dist) {
	int oldVal, newVal, val, index;
	
	//System.err.println("setting " + x + ", " + y);
	if (x<0 || x>width1 || y<0 || y>height1) return;
	if(dist < 0) dist = 0.0f - dist;
	if(dist > 1.0f) return;
	index = (height1-y)*width + x;
		
	val = (int)(pixels[index]*dist + penColor*(1.0f-dist));

	val = bound(val, 100);
	//if(val>100) val = 100;
	//else if(val < 0) val = 0;
	pixels[index] = (byte)val;
	
	if (antialias) {
	    g.setColor(grays[val]);
	    g.drawLine(x,height1-y,x,height1-y);
	}
    }

    private int clipCode(float x, float y) {
	return ((x < 0 ? 8 : 0) | (x > width1 ? 4 : 0) |
		(y < 0 ? 2 : 0) | (y > height1 ? 1 : 0));
    }
    
    public void line(int ox1, int oy1, int ox2, int oy2) {
	/* check for line clipping, do it if necessary */
	if ((ox1 < 0) || (oy1 < 0) || (ox2 > width1) || (oy2 > height1)) {
	    // clipping, converts to floats for dy/dx fun
	    // (otherwise there's just too much casting mess in here)
	    float xP = (float)ox1, yP = (float)oy1;
	    float xQ = (float)ox2, yQ = (float)oy2;
	    
	    int cP = clipCode(ox1, oy1);
	    int cQ = clipCode(ox2, oy2);
	    float dx, dy;
	    while ((cP | cQ) != 0) {  
		if ((cP & cQ) != 0) return;
		dx = xQ - xP; dy = yQ - yP;
		if (cP != 0) {  
		    if ((cP & 8) == 8) { 
			yP += (0-xP) * dy / dx; xP = 0; 
		    } else if ((cP & 4) == 4) { 
			yP += (width1-xP) * dy / dx; xP = width1; 
		    } else if ((cP & 2) == 2) { 
			xP += (0-yP) * dx / dy; yP = 0;
		    } else if ((cP & 1) == 1) {
			xP += (height1-yP) * dx / dy; yP = height1;
		    }  
		    cP = clipCode(xP, yP);
		} else if (cQ != 0) {
		    if ((cQ & 8) == 8) { 
			yQ += (0-xQ) * dy / dx; xQ = 0;
		    } else if ((cQ & 4) == 4) {
			yQ += (width1-xQ) * dy / dx; xQ = width1;
		    } else if ((cQ & 2) == 2) { 
			xQ += (0-yQ) * dx / dy; yQ = 0;
		    } else if ((cQ & 1) == 1) { 
			xQ += (height1-yQ) * dx / dy; yQ = height1;
		    }  
		    cQ = clipCode(xQ, yQ);
		}
	    }
	    ox1 = (int)xP; oy1 = (int)yP;
	    ox2 = (int)xQ; oy2 = (int)yQ;
	}
	
	/* actually draw the line */
	int dx, dy, incrE, incrNE, d, x, y, twoV, sdx=0, sdy=0;
	float invDenom, twoDX, scratch, slope;
	int x1, x2, y1, y2, incy=0, incx=0, which;
	boolean backwards = false, slopeDown = false;
	
	myFlush(FLINE);
	
	if (!antialias) { 
	    g.setColor(grays[penColor]);
	    g.drawLine(ox1,height1-oy1,ox2,height1-oy2);
	}
	
        /* first do horizontal line */
	if (ox1==ox2) {
	    x = ox1;
	    if(oy1 < oy2) { y1 = oy1; y2 = oy2; }
	    else { y1 = oy2; y2 = oy1; }
	    for(y=y1;y<=y2;y++) {
		intensifyPixel(x, y, 0);
	    }
	    return;
	}
	slope = (float)(oy2 - oy1)/(float)(ox2 - ox1);
	/* check for which 1, 0 <= slope <= 1 */
	if(slope >= 0 && slope <= 1) which = 1;
	else if(slope > 1) which = 2;
	else if(slope < -1) which = 3;
	else which=4;
	if (((which==1 || which==2 || which==4) && 
	     ox1 > ox2) || ((which==3) && ox1 < ox2)) {
	    x1 = ox2;
	    x2 = ox1;
	    y1 = oy2;
	    y2 = oy1;
	} else {
	    x1 = ox1;
	    y1 = oy1;
	    x2 = ox2;
	    y2 = oy2;
	}
	dx = x2 - x1;
	dy = y2 - y1;
	if (which==1) {
	    sdx = dx;
	    d = 2 * dy - dx;
	    incy = 1;
	    incrE = 2*dy;
	    incrNE = 2*(dy-dx);
	} else if (which==2) {
	    sdy = dy;
	    d = 2 * dx - dy;
	    incx = 1;
	    incrE = 2*dx;
	    incrNE = 2*(dx-dy);
	} else if (which==3) {
	    sdy = -dy;
	    d = -2 * dx - dy;
	    incx = -1;
	    incrE = -2*dx;
	    incrNE = -2*(dx+dy);
	} else /*if(which == 4)*/ {
	    sdx = -dx;
	    d = -2 * dy - dx;
	    incy = -1;
	    incrE = -2*dy;
	    incrNE = -2*(dy+dx);
	}
	if(which==1 || which==4) {
	    twoV = 0;
	    invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
	    twoDX = 2 * sdx * invDenom;
	    x = x1;
	    y = y1;
	    if (antialias) {
		intensifyPixel(x, y, 0);
		intensifyPixel(x, y+1, twoDX);
		intensifyPixel(x, y-1, twoDX);
	    } else {
		intensifyPixel(x, y, 0);
	    }
	    while(x < x2) {
		if(d<0) {
		    twoV = d + dx;
		    d += incrE;
		    ++x;
		}
		else {
		    twoV = d - dx;
		    d += incrNE;
		    ++x;
		    y+=incy;
		}
		scratch = twoV * invDenom;
		if(antialias) {
		    intensifyPixel(x, y, scratch);
		    intensifyPixel(x, y+1, twoDX - scratch);
		    intensifyPixel(x, y-1, twoDX + scratch);
		}
		else
		    intensifyPixel(x, y, 0);
	    }
	}
	else {
	    twoV = 0;
	    invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
	    twoDX = 2 * sdy * invDenom;
	    x = x1;
	    y = y1;
	    if(antialias) {
		intensifyPixel(x, y, 0);
		intensifyPixel(x+1, y, twoDX);
		intensifyPixel(x-1, y, twoDX);
	    }
	    else
		intensifyPixel(x, y, 0);
	    while(y < y2) {
		if(d<0) {
		    twoV = d + dy;
		    d += incrE;
		    ++y;
		}
		else {
		    twoV = d - dy;
		    d += incrNE;
		    ++y;
		    x+=incx;
		}
		scratch = twoV * invDenom;
		if(antialias) {
		    intensifyPixel(x, y, scratch);
		    intensifyPixel(x+1, y, twoDX - scratch);
		    intensifyPixel(x-1, y, twoDX + scratch);
		}
		else
		    intensifyPixel(x, y, 0);
	    }
	}
    }


    public void pause(int amount) {
	long stopTime = System.currentTimeMillis() + (amount*10);
	do { } while (System.currentTimeMillis() < stopTime);
    }


    ////////////////////////////////////////////////////////////

    // related methods, or likely to be called by alternate 
    // implementations like scheme and python

    static private int bound(int input, int upper) {
	if (input > upper) return upper;
	else if (input < 0) return 0;
	else return input;
    }


    public byte[] getPixels() {
	return pixels;
    }


    // awful way to do printing, but sometimes brute force is
    // just the way. java printing across multiple platforms is
    // outrageously inconsistent.
    
    public void print(Graphics g, int offsetX, int offsetY) {
	//g.drawImage(image, offsetX, offsetY, null);
	
	int index = 0;
	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < width; x++) {
		// hopefully little overhead in setting color
		g.setColor(grays[pixels[index++]]);
		g.drawLine(offsetX + x, offsetY + y,
			   offsetX + x, offsetY + y);
	    }
	}
    }
    

    public void reset() {
	for (int i = 0; i < pixelCount; i++) 
	    pixels[i] = 0;

	if (g != null) {
	  g.setColor(grays[0]);
	  g.fillRect(0, 0, width, height);
	}
	penColor = 100;

	antialias = false;
	explicitRefresh = false;

	flushCount = 0;
	resetBlockDetect();

	for (int i = 0; i < 3; i++)
	    mouse[i] = 0;
        for (int i = 0; i < 26; i++) 
	    key[i] = 0;
	for (int i = 0; i < 1000; i++)
	    array[i] = 0;
   
	//mouse = new int[3];
	//key = new int[26];
	//array = new int[1000];
    }

    public void setPixel(int x, int y, int val) {
        myFlush(FDOT);
	if (x < 0 || x > width1 || y < 0 || y > height1) return;
	int checkVal = bound(val, 100);
	pixels[(height1-y)*width + x] = (byte)checkVal;
	g.setColor(grays[checkVal]);
	g.drawLine(x, height1-y, x, height1-y);
    }


    public int getPixel(int x, int y) {
	return (int) 
	    pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		  ((x<0)?0:((x>width1)?width1:x))];
    }


    public final int getMouse(int slot) { // throws DbnException {
	//System.out.println("get");
	return mouse[slot-1];
    }

    public final int getKey(int slot) { // throws Exception {
	return key[slot-1];
    }

    public final int getTime(int slot) { // throws Exception {
	// wooaaah! garbage city!
	Date d = new Date(); 
	switch (slot) {
	case 1: return d.getHours();
	case 2: return d.getMinutes();
	case 3: return d.getSeconds();
	case 4: return (int) ((System.currentTimeMillis() % 1000)/10);
	}
	return 0;
    }

    public int getConnector(String name, int slot) throws DbnException {
	try {
	    if (name.equals("mouse")) {
		return getMouse(slot);
	    } else if (name.equals("key")) {
		return getKey(slot);
	    } else if (name.equals("time")) {
		return getTime(slot);
	    } else if (name.equals("array")) {
		return array[slot-1];
	    } else if (name.equals("net")) {
		return getNet(slot);
#ifdef CRICKET
	    } else if (name.equals("sensor")) {
		return getSensor(slot);
#endif
		//} else {
		//throw new DbnException("unknown external data " + name);
	    }
	} catch (Exception e) {
	    throw new DbnException("could not get slot " + 
				   slot + " of " + name);
	}
	return -1;
    }


    public void setConnector(String name, int slot, int value) 
	throws DbnException {
	try {
	    if (name.equals("net")) {
		setNet(slot, value); 
		return;
#ifdef CRICKET
	    } else if (name.equals("sensor")) {
		setSensor(slot, value);
		return;
#endif
	    } else if (name.equals("array")) {
		array[slot-1] = value;
		return;
	    }
	    //throw new DbnException("unknown external data " + name);

	} catch (Exception e) {
	    //System.out.println("should throw error here");
	    throw new DbnException("error setting " + name + " " + slot + 
				   " to " + value);
	}
    }



    ////////////////////////////////////////////////////////////

    // other connector-related items

    public boolean isConnector(String name) {
	return (name.equals("net") || name.equals("key") || 
		name.equals("mouse") || name.equals("time") || 
		name.equals("array") || name.equals("sensor"));
    }	


    // networking support
    static final int NET_PORT = 8000;
    static final int NET_ERROR_MESSAGE = -1;
    static final int NET_OK_MESSAGE = 0;
    static final int NET_GET_MESSAGE = 1;
    static final int NET_SET_MESSAGE = 2;
    
    DataInputStream netInputStream;
    DataOutputStream netOutputStream;
    boolean secondAttempt; 


    protected void openNetwork() throws DbnException {
	try {
	    DbnApplet applet = DbnApplet.applet;
	    String hostname = applet.getNetServer();
	    Socket socket = new Socket(hostname, NET_PORT);
	    netInputStream = new DataInputStream(socket.getInputStream());
	    netOutputStream = new DataOutputStream(socket.getOutputStream());
	    
	} catch (IOException e) {
	    netInputStream = null;
	    netOutputStream = null;
	    throw new DbnException("could not connect to server");
	}
    }


    protected int getNet(int number) throws DbnException {
	if (netInputStream == null) {
	    openNetwork();
	}

	int val = -1; // !#$(*@#$ compiler
	try {
	    netOutputStream.writeInt(1);
	    netOutputStream.writeInt(NET_GET_MESSAGE);
	    netOutputStream.writeInt(number);
	    netOutputStream.writeInt(0);
	    netOutputStream.flush();

	    int version = netInputStream.readInt();
	    int message = netInputStream.readInt();
	    int num = netInputStream.readInt();
	    val = netInputStream.readInt();

	    // past where an ioexception would be thrown
	    secondAttempt = false; 
	    if (message != NET_OK_MESSAGE) {
		throw new DbnException("bad data from the server");
	    }

	} catch (IOException e) {
	    if (!secondAttempt) { // re-connect and try again
		secondAttempt = true;
		openNetwork();
		return getNet(number);
	    } else {
		throw new DbnException("could not connect to server");
	    }
	}
	return val;
    }


    protected void setNet(int number, int value) throws DbnException {
	if (netInputStream == null) {
	    openNetwork();
	}
	try {
	    netOutputStream.writeInt(1);
	    netOutputStream.writeInt(NET_SET_MESSAGE);
	    netOutputStream.writeInt(number);
	    netOutputStream.writeInt(value);
	    netOutputStream.flush();

	    int version = netInputStream.readInt();
	    int message = netInputStream.readInt();
	    int num = netInputStream.readInt();
	    int val = netInputStream.readInt();

	    // past where an ioexception would be thrown
	    secondAttempt = false; 
	    if (message != NET_OK_MESSAGE) {
		throw new DbnException("could not set data on the server");
	    }

	} catch (IOException e) {
	    if (!secondAttempt) { // re-connect and try again
		secondAttempt = true;
		openNetwork();
		setNet(number, value);
	    } else {
		throw new DbnException("could not connect to server");
	    }
	}
    }


#ifdef CRICKET
    SensorConnector sensor;

    protected void openSensor() {
	sensor = new SensorConnector();
    }

    protected int getSensor(int slot) throws DbnException {
	return sensor.getValue(slot);
    }

    protected void setSensor(int slot, int value) throws DbnException {
	throw new DbnException("Cannot talk to sensor, only listen");
    }
#endif


    ////////////////////////////////////////////////////////////

    // methods used by ai refresh (tm)

    public void myFlush(int v) {
	flushCount+=v;
	if (autoflushenablep) {	
	    if (flushCount>99) { 
		if (!explicitRefresh) refresh();
	    }
	}
    }


    public void flushNormal() {
	FDOT = 10;
	FPAPER = 100;
	FLINE = 10; 
	FFIELD = 50;
	FMAX = 100;
    }    


    public void flushReduced(int lev) {
	FDOT=10/lev; 
	FLINE=10/lev; 
	FFIELD=50/lev; 
	FPAPER=100;
    }
    

    public void resetBlockDetect() { 
	insideforeverp = false; 
	lastflushfire = flushfire = -1;
	insiderepeatp = false;
	autoflushenablep = true;
	repeatlevel = 0;
	flushNormal();
    }
    

    public void beginForever() {
	// fired on a '{'
	//System.out.println("begin forever");
	insideforeverp = true;
	flushfire = -1;
	autoflushenablep = false;
    }
    

    public void endForever() {
	// fired on a '}'
	//System.out.println("end->"+flushfire);
	if (flushfire == -1) {
	    // did not flush during a forever
	    // force it
	    if (!explicitRefresh) refresh();
	    autoflushenablep= true;
	}
	flushfire = -1;
    }
    

    public void beginRepeat() {
	// if within a forever, should be disabled anyways
	//System.out.println("--****begrepeat");
	insiderepeatp = true;
	repeatlevel++;
	//System.out.println("repeatlevel: "+repeatlevel+"/"+autoflushenablep);
	if (insideforeverp) {
	    // not much to do except for default
	    if (repeatlevel == 1) { // special case
		flushNormal();
	    } else if (repeatlevel>1) {
		flushReduced(repeatlevel);
	    }
	} else  {
	    // disable flush, count up how much flush activity gets
	    if (repeatlevel == 1) { // special case
		autoflushenablep = true;
		flushNormal();
	    } else if (repeatlevel>1) {
		autoflushenablep = false;
		flushReduced(repeatlevel);
	    }
	}
	
    }
    

    public void endRepeat() {
	//System.out.println("--****endrepeat");
	if (repeatlevel==1)
	    insiderepeatp = false;
	repeatlevel--;
	flushNormal();
	if (!insideforeverp) {
	    if (flushCount>0 && flushfire==-1) {
		if (!explicitRefresh) refresh();
	    }
	}
    }
    

    public void paperFlush() {	
	//	  System.out.println("paperflush request");
	if (insideforeverp&&aiflushp) {
	    if (flushfire==-1) {
		flushfire = 0; // paper was first
		if (!explicitRefresh) refresh();
	    } else {
		if (lastflushfire == 0) {
		    // could be consecutive paper -> legalize
		    if (!explicitRefresh) refresh();
		} else {
   		    // ignore flush
		}
	    }
	} else {
	    myFlush(FPAPER);
	}
	lastflushfire = flushfire;
    }

    
    public void fieldFlush() {
	if (insideforeverp&&aiflushp) {
	    if (flushfire==-1) {
		flushfire = 1; // field was first
		if (!explicitRefresh) refresh();
	    } else {
		// ignore flush
	    }
	} else {
	    myFlush(FFIELD);
	}
	lastflushfire = flushfire;
    }	


    ////////////////////////////////////////////////////////////

    // panel methods, get connector input, etc.

    public Dimension preferredSize() {
	return new Dimension(width, height);
    }

    public void update() {
	paint(this.getGraphics());
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics screen) {
	if (image == null) {
	    image = createImage(width, height);
	    if (image == null) return;
	    g = image.getGraphics();
	    g.setColor(Color.white);
	    g.fillRect(0, 0, width, height);
	}
	// screen goes null on quit, throws an exception
	if (screen != null) {  
	    screen.drawImage(image, 0, 0, this);
	}
    }


    //public void initiate() {
    /*
	for (int i = 0; i < 3; i++)
	    mouse[i] = 0;
        for (int i = 0; i < 26; i++) 
	    key[i] = 0;
	for (int i = 0; i < 1000; i++)
	    array[i] = 0;
    */
    //}


    public void idle(long currentTime) {
	//Date d = new Date(); // wooaaah! garbage city!
	//time[0] = d.getHours();
	//time[1] = d.getMinutes();
	//time[2] = d.getSeconds();
	//time[3] = (int) (currentTime % 1000)/10;

	System.out.println("graphics idling.. good");
	// mac java doesn't always post key-up events, 
	// so time out the characters after a second
	for (int i = 0; i < 26; i++) {
	    if ((key[i] == 100) && (currentTime-keyTime[i] > 1000)) {
		keyTime[i] = -1;
		key[i] = 0;
	    }
	}
    }


    private final int letterKey(int n) {
        if ((n >= 'a') && (n <= 'z')) return n - 'a';
	if ((n >= 'A') && (n <= 'Z')) return n - 'A';
	return -1;
    }

    public boolean keyDown(Event ev, int n) {
	//if (n == 27) app.gui.terminate();  // ooh.. ugly

	int which = letterKey(n);
	if (which == -1) return false;
	keyTime[which] = System.currentTimeMillis();
	key[which] = 100;
        return true;
    }

    public boolean keyUp(Event ev, int n) {
	int which = letterKey(n);
	if (which == -1) return false;
	keyTime[which] = -1;
	key[which] = 0;
        return true;
    }


    public boolean mouseDown(Event ev, int x, int y) {
	mouse[2] = 100;
	return updateMouse(x, y);
    }

    public boolean mouseUp(Event ev, int x, int y) {
	mouse[2] = 0;
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
	return updateMouse(x, y);
    }

    public boolean updateMouse(int x, int y) {
	//System.out.println("updateMouse " + x + ", " + y);
	mouse[0] = x;
	mouse[1] = height1 - y;
	return true;
    }
}
