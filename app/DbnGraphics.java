#ifndef KVM
import java.awt.*;
import java.awt.image.*;
#else
//import com.sun.kjava.Graphics;
#endif

import java.io.*;
import java.net.*;
import java.util.*;


// TODO write memoryimagesource code (call it jdk11_plus)
// DONE add method for setting hostname
// DONE switch grays around the correct way, setup palette
// DONE don't create a new Date() on every idle() step

public class DbnGraphics extends Panel {
  static int grayMap[] = new int[101];
  static int colorMap[] = new int[101];
  static {
    for (int i = 0; i < 101; i++) {
      int g = (100-i)*255 / 100;
      grayMap[i] = 0xff000000 | (g << 16) | (g << 8) | g;
      colorMap[i] = (i*255) / 100;
    }
  }

  MemoryImageSource source;
  Graphics panelg;

  Image screenImage;
  Image image;

  Graphics g;
  int gx, gy;

  int pixels[];
  int pixelCount;
  int penColor;

  //boolean antialias;
  int magnification = 1;

  //Image lastImage;
  //Graphics lastImageg;

  Color bgColor;
  Frame frame;

  int width, height;
  int width1, height1;

  int mouse[] = new int[3];
  int key[] = new int[26];
  int array[] = new int[1000];
  long keyTime[] = new long[26];

  int FDOT = 10;
  int FPAPER = 100;
  int FLINE = 10; 
  int FFIELD = 10;
  int FMAX = 100;

  boolean aiRefresh;
  boolean insideforeverp;
  short flushfire = -1; // 0 is paper, 1 is field
  short lastflushfire = -1;
  boolean insiderepeatp = false;
  boolean autoflushenablep = true;
  int repeatlevel = 0;    
  int flushCount = 0;


  public DbnGraphics(int width, int height, Color bgColor) {
    this.bgColor = bgColor;

    currentDbnGraphics = this; // for python/scheme
#ifdef CRICKET
    openSensor();
#endif
    setup(width, height);
  }


  public void setup(int width, int height) {
    int oldWidth = this.width;
    int oldHeight = this.height;

    // make sure the jokers don't ask for something ridiculous
    if ((width < 1) || (height < 1)) {
      if ((oldWidth == 0) && (oldHeight == 0)) {
	setup(101, 101);
      }
      return;  // otherwise just ignore
    }

    this.width = width;
    this.height = height;
    width1 = width - 1;
    height1 = height - 1;
    this.bgColor = bgColor;

    pixelCount = width * height;
    pixels = new int[pixelCount];
    lines = new int[pixelCount];
    //for (int i = 0; i < pixelCount; i++)
    //pixels[i] = 0xffffffff;

    reset();
    //rgbColor();
    //pen(100);
    //penColor = 0xff000000;
    //colorModel = HSB;
    //magnification = 1;

    source = new MemoryImageSource(width, height, pixels, 0, width);
    source.setAnimated(true);
    //source.setFullBufferUpdates(true);
    image = Toolkit.getDefaultToolkit().createImage(source);

    screenImage = null; // so that it gets reshaped

    if (oldWidth != width || oldHeight != height) 
      repack();

    update();
  }


  public void magnify(int howmuch) {
    if (howmuch < 1) howmuch = 1;
    magnification = howmuch;
    repack();
    // fake out the setup function to make it look
    // like something has actually changed
    //height += 1000;
    //setup(width, height - 1000); 
  }


  protected void repack() {
    if (getParent() != null) {
      getParent().getParent().getParent().doLayout();
      frame = (Frame) getParent().getParent().getParent().getParent();
      frame.pack();
      //System.err.println("packed");
    }
  }


  public void reset() {
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = 0xffffffff;
      lines[i] = -2;
    }

    if (g != null) {
      //g.setColor(grays[0]);
      //g.fillRect(0, 0, width, height);
    }
    //if (lastImageg != null) {
      //g.setColor(grays[0]);
      //g.fillRect(0, 0, width, height);
    //}
    //penColor = 100;
    penColor = 0xff000000;

    //antialias = false;
    //explicitRefresh = false;
    aiRefresh = true;

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


  ////////////////////////////////////////////////////////////


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


  ////////////////////////////////////////////////////////////

  // line-buffer recorder

  int lines[];
  int currentLine;


  public void setLine(int which) {
    currentLine = which;
  }

  public int getLine(int x, int y) {
    return lines[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		((x<0)?0:((x>width1)?width1:x))];
  }


  /////////////////////////////////////////////////////////////
  
  // the deprecated bin

  public void norefresh() { }

  public void antialias(int m) { }


  /////////////////////////////////////////////////////////////

  // core primitives


  public void paper(int val) {
    int gray = 100 - bound(val, 100);
    paper(gray, gray, gray);
  }

  public void paper(int red, int green, int blue) {
    paperFlush();
    int color = makeColor(red, green, blue);
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = color;
      lines[i] = currentLine;
    }
  }


  public void pen(int gray) {
    penColor = makeGray(gray);
  }

  public void pen(int red, int green, int blue) {
    penColor = makeColor(red, green, blue);
  }


  // line clipping code appropriated from 
  // "Computer Graphics for Java Programmers"

  public void line(int ox1, int oy1, int ox2, int oy2) {
    // check for line clipping, do it if necessary
    if ((ox1 < 0) || (oy1 < 0) || (ox2 > width1) || (oy2 > height1)) {
      // clipping, converts to floats for dy/dx fun
      // (otherwise there's just too much casting mess in here)
      float xP = (float)ox1, yP = (float)oy1;
      float xQ = (float)ox2, yQ = (float)oy2;
	    
      int cP = lineClipCode(ox1, oy1);
      int cQ = lineClipCode(ox2, oy2);
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
	  cP = lineClipCode(xP, yP);
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
	  cQ = lineClipCode(xQ, yQ);
	}
      }
      ox1 = (int)xP; oy1 = (int)yP;
      ox2 = (int)xQ; oy2 = (int)yQ;
    }
	
    // actually draw the line
    int dx, dy, incrE, incrNE, d, x, y, twoV, sdx=0, sdy=0;
    float invDenom, twoDX, scratch, slope;
    int x1, x2, y1, y2, incy=0, incx=0, which;
    boolean backwards = false, slopeDown = false;

    /*
    if (useGraphics) {
      aiFlush(FLINE);
      if (!antialias) { 
	g.setColor(grays[penColor]);
	g.drawLine(ox1,height1-oy1,ox2,height1-oy2);
      }
    }
    */	

    // first do horizontal line
    if (ox1==ox2) {
      x = ox1;
      if (oy1 < oy2) { y1 = oy1; y2 = oy2; }
      else { y1 = oy2; y2 = oy1; }
      for (y = y1; y <= y2; y++) {
	linePixel(x, y);
	//intensifyPixel(x, y, 0);
      }
      return;
    }
    slope = (float)(oy2 - oy1)/(float)(ox2 - ox1);
    // check for which 1, 0 <= slope <= 1
    if (slope >= 0 && slope <= 1) which = 1;
    else if (slope > 1) which = 2;
    else if (slope < -1) which = 3;
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
#ifdef KVM
      invDenom = 1.0f / (2.0f * (float)KvmLacunae.sqrt(dx*dx + dy*dy));
#else
      invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
#endif
      twoDX = 2 * sdx * invDenom;
      x = x1;
      y = y1;
      //if (antialias) {
      //intensifyPixel(x, y, 0);
      //intensifyPixel(x, y+1, twoDX);
      //intensifyPixel(x, y-1, twoDX);
      //} else {
      //intensifyPixel(x, y, 0);
      linePixel(x, y);
      //}
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
	//if(antialias) {
	//intensifyPixel(x, y, scratch);
	//intensifyPixel(x, y+1, twoDX - scratch);
	//intensifyPixel(x, y-1, twoDX + scratch);
	//}
	//else
	//intensifyPixel(x, y, 0);
	linePixel(x, y);
      }
    }
    else {
      twoV = 0;
#ifdef KVM
      invDenom = 1.0f / (2.0f * (float)KvmLacunae.sqrt(dx*dx + dy*dy));
#else
      invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
#endif
      twoDX = 2 * sdy * invDenom;
      x = x1;
      y = y1;
      //if(antialias) {
      //intensifyPixel(x, y, 0);
      //intensifyPixel(x+1, y, twoDX);
      //intensifyPixel(x-1, y, twoDX);
      //}
      //else
      //intensifyPixel(x, y, 0);
      linePixel(x, y);
      while (y < y2) {
	if (d < 0) {
	  twoV = d + dy;
	  d += incrE;
	  ++y;
	} else {
	  twoV = d - dy;
	  d += incrNE;
	  ++y;
	  x += incx;
	}
	scratch = twoV * invDenom;
	//if(antialias) {
	//intensifyPixel(x, y, scratch);
	//intensifyPixel(x+1, y, twoDX - scratch);
	//intensifyPixel(x-1, y, twoDX + scratch);
	//}
	//	else
	//intensifyPixel(x, y, 0);
	linePixel(x, y);
      }
    }
  }

  private void linePixel(int x, int y) { //, float dist) {
    if (x<0 || x>width1 || y<0 || y>height1) return;
    int index = (height1-y)*width + x;
    pixels[index] = penColor;
    lines[index] = currentLine;
  }

  private int lineClipCode(float x, float y) {
    return ((x < 0 ? 8 : 0) | (x > width1 ? 4 : 0) |
	    (y < 0 ? 2 : 0) | (y > height1 ? 1 : 0));
  }


  public void field(int x1, int y1, int x2, int y2, int gray) {
    int oldColor = penColor;
    penColor = makeGray(gray);
    if (y2 < y1) { 
      int temp = y1; y1 = y2; y2 = temp; 
    }
    for (int y = y1; y1 <= y2; y1++) {
      line(x1, y, x2, y);
    }
    penColor = oldColor;
  }


  ////////////////////////////////////////////////////////////

  // pixel operations, the bracket [ ] commands


  public void setPixel(int x, int y, int gray) {
    aiFlush(FDOT);
    if (x < 0 || x > width1 || y < 0 || y > height1) return;
    int index = (height1-y)*width + x;
    //gray = 100 - bound(gray, 100);
    //setPixel(x, y, gray, gray, gray);
    pixels[index] = makeGray(gray); //makeColor(red, green, blue);
    lines[index] = currentLine;
  }

  public void setPixel(int x, int y, int which, int value) {
    aiFlush(FDOT);
    if ((which < 0) || (which > 2)) return;
    if (x < 0 || x > width1 || y < 0 || y > height1) return;
    int index = (height1-y)*width + x;
    int place = (2-which) * 8;
    value = bound(value, 100);
    pixels[index] &= ~(0xff << place);
    pixels[index] |= colorMap[value] << place;
    lines[index] = currentLine;
  }

  /*
  public void setPixel(int x, int y, int red, int green, int blue) {
  }
  */

  public int getPixel(int x, int y) {
    int pixel = pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		      ((x<0)?0:((x>width1)?width1:x))];
    // do the right thing and return an average of the pixel values
    return 100 * (((pixel >> 16) & 0xff) + 
		  ((pixel >> 8) & 0xff) + 
		  (pixel & 0xff)) / (3*255);
  }

  public int getPixel(int x, int y, int which) {
    if ((which < 0) || (which > 2)) return 0;
    int pixel = pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		      ((x<0)?0:((x>width1)?width1:x))];
    return (100 * ((pixel >> (2-which)*8) & 0xff)) / 255;
  }


  ////////////////////////////////////////////////////////////

  // grab bag


  // called from code (DbnEngine), implicit 'norefresh'
  public void refresh() {
    aiRefresh = false;
    update();
  }


  public void pause(int amount) {
    long stopTime = System.currentTimeMillis() + (amount*10);
    do { } while (System.currentTimeMillis() < stopTime);
  }


  ////////////////////////////////////////////////////////////

  // related methods, or likely to be called by alternate 
  // implementations like scheme and python

  public final int bound(int input, int upper) {
    if (input > upper) return upper;
    else if (input < 0) return 0;
    else return input;
  }

  public final int makeColor(int red, int green, int blue) {
    return (0xff000000 | 
	    (colorMap[bound(red, 100)] << 16) |
	    (colorMap[bound(green, 100)] << 8) | 
	    (colorMap[bound(blue, 100)]));
  }

  public final int makeGray(int gray) {
    return grayMap[bound(gray, 100)];
  }

  public byte[] getPixels() {
    // called by editor to save the pixel array to the 
    // courseware server
    //return pixels;
    return null;
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
	//g.setColor(grayMap[pixels[index++]]);
	g.setColor(new Color(pixels[index++]));
	g.drawLine(offsetX + x, offsetY + y,
		   offsetX + x, offsetY + y);
      }
    }
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
#ifdef KVM
    KvmLacunae d = KvmLacunae.buddy;
#else
    Date d = new Date(); 
#endif
    switch (slot) {
    case 1: return d.getHours();
    case 2: return d.getMinutes();
    case 3: return d.getSeconds();
    case 4: return (int) ((System.currentTimeMillis() % 1000)/10);
    }
    return 0;
  }

  public final int getArray(int slot) throws DbnException {
    try {
      return array[slot-1];
    } catch (Exception e) {
      throw new DbnException("could not read slot " + slot + 
			     " from array");
    }
  }

  public final void setArray(int slot, int what) throws DbnException {
    try {
      array[slot-1] = what;
    } catch (Exception e) {
      throw new DbnException("could not set slot " + slot + 
			     " of array");
    }
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
#ifndef KVM
      } else if (name.equals("net")) {
	return getNet(slot);
#endif
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
      if (name.equals("array")) {
	array[slot-1] = value;
	return;

#ifdef CRICKET
      } else if (name.equals("sensor")) {
	setSensor(slot, value);
	return;
#endif

#ifndef KVM
      } else if (name.equals("net")) {
	setNet(slot, value); 
	return;
#endif
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

#ifndef KVM
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
#endif


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

  public void aiFlush(int v) {
    flushCount+=v;
    if (autoflushenablep) {	
      if (flushCount>99) { 
	//if (!explicitRefresh) update();
	if (!aiRefresh) update();
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
      if (aiRefresh) update();
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
	if (aiRefresh) update();
      }
    }
  }
    
  public void paperFlush() {	
    //	  System.out.println("paperflush request");
    if (insideforeverp && aiRefresh) {
      if (flushfire==-1) {
	flushfire = 0; // paper was first
	if (aiRefresh) update();
      } else {
	if (lastflushfire == 0) {
	  // could be consecutive paper -> legalize
	  if (aiRefresh) update();
	} else {
	  // ignore flush
	}
      }
    } else {
      aiFlush(FPAPER);
    }
    lastflushfire = flushfire;
  }


  public void fieldFlush() {
    if (insideforeverp && aiRefresh) {
      if (flushfire==-1) {
	flushfire = 1; // field was first
	if (!aiRefresh) update();
      } else {
	// ignore flush
      }
    } else {
      aiFlush(FFIELD);
    }
    lastflushfire = flushfire;
  }	


  ////////////////////////////////////////////////////////////

  // panel methods, get connector input, etc.

  public Dimension preferredSize() {
    return new Dimension(width1*magnification + 30, 
			 height1*magnification + 30);
    //return new Dimension(width, height);
  }


  public void update() {
    if (panelg == null)
      panelg = this.getGraphics();
    if (panelg != null)
      paint(panelg);

    flushCount = 0;
    if (source != null) {
      source.newPixels();
    }

#ifdef RECORDER
    // maybe this should go inside DbnEditorGraphics, 
    // but i'm not sure
    //DbnRecorder.addFrame(pixels);
    DbnRecorder.addFrame(image, pixels, mouse[0], 
			 height1-mouse[1], (mouse[2] == 100));
#endif
    /*
    if ((lastImage == null) && (image != null)) {
      lastImage = createImage(width, height);
      if (lastImage != null) {
	lastImageg = lastImage.getGraphics();
      }
    }
    if (lastImageg != null) {
      lastImageg.drawImage(image, 0, 0, null);
    }
    */
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics screen) {
    if (screenImage == null) {
      //Dimension dim = new Dimension(width + 100, height + 100);
      Dimension dim = preferredSize();
      screenImage = createImage(dim.width, dim.height);
      Graphics g = screenImage.getGraphics();
      gx = (dim.width - width*magnification) / 2;
      gy = (dim.height - height*magnification) / 2;

      // draw background
      g.setColor(bgColor);
      g.fillRect(0, 0, dim.width, dim.height);

      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width*magnification+1, height*magnification+1);
    }

    if (image != null) {
      Graphics g = screenImage.getGraphics();
      g.drawImage(image, gx, gy, 
		  width*magnification, height*magnification, null);
    }

    // avoid an exception during quit
    if ((screen != null) && (screenImage != null)) {
      // blit to screen
      screen.drawImage(screenImage, 0, 0, null);
    }
  }

  /*
  public void paint(Graphics screen) {
    //System.out.println("painting");
    if (image == null) {
      //System.out.println("creating new image");
      image = createImage(width, height);
      if (image == null) return;
      g = image.getGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
    }
    // screen goes null on quit, throws an exception
    if (screen != null) {
      screen.drawImage(image, 0, 0, null); //this);
    }
  }
  */


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

    //System.out.println("graphics idling.. good");
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
    //System.out.println("got key " + n);
    int which = letterKey(n);
    if (which == -1) return false;
    keyTime[which] = System.currentTimeMillis();
    key[which] = 100;
    //return true;
    return false;
  }

  public boolean keyUp(Event ev, int n) {
    int which = letterKey(n);
    if (which == -1) return false;
    keyTime[which] = -1;
    key[which] = 0;
    //return true;
    return false;
  }


  public boolean mouseDown(Event e, int x, int y) {
    //System.out.println("mouse down in graphics");
    mouse[2] = 100;
    return updateMouse(e, x, y);
  }

  public boolean mouseUp(Event e, int x, int y) {
    mouse[2] = 0;
    return updateMouse(e, x, y);
  }

  public boolean mouseMove(Event e, int x, int y) {
    return updateMouse(e, x, y);
  }
 
  public boolean mouseDrag(Event e, int x, int y) {
    return updateMouse(e, x, y);
  }

  public boolean mouseEnter(Event e, int x, int y) {    
    //System.out.println("entering");
    if (frame == null) {
      // shhh! don't tell anyone!
      frame = (Frame) getParent().getParent().getParent().getParent();
      // that is the nastiest piece of code in the codebase
    }
    frame.setCursor(Frame.CROSSHAIR_CURSOR);
    return super.mouseEnter(e, x, y);
  }

  //public boolean mouseEnter(Event e, int x, int y) {
  //return updateMouse(e, x, y);
  //}

  public boolean mouseExit(Event e, int x, int y) {
    return updateMouse(e, x, y);
  }


  public boolean updateMouse(Event e, int x, int y) {
    x -= gx;
    y -= gy;
    x /= magnification;
    y /= magnification;
    mouse[0] = x;
    mouse[1] = height1 - y;
    return true;
  }

  /*
  public boolean updateMouse(Event e, int x, int y) {
    //System.out.println("DbnGraphics.updateMouse " + x + ", " + y);
    mouse[0] = x;
    mouse[1] = height1 - y;
    //return true;
    return false;
  }
  */
}
