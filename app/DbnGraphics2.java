#ifdef GRAPHICS2

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;


// need a better solution for getpixel
// maybe return an array for python
// or write into an array

public class DbnGraphics2 extends Panel {
  Image image;
  MemoryImageSource source;
  Graphics panelg;

  int pixels[];
  int pixelCount;
  int penColor;

  int width, height;
  int width1, height1;

  int mouse[] = new int[3];
  int array[] = new int[1000];
  int key[] = new int[26];
  long keyTime[] = new long[26];


  public DbnGraphics2(int width, int height) {
    this.width = width;
    this.height = height;
    width1 = width - 1;
    height1 = height - 1;
	
    pixelCount = width * height;
    pixels = new int[pixelCount];
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = 0xffffffff;
    }
    penColor = 0xff000000;
  }


  /////////////////////////////////////////////////////////////

  // actual methods relating to dbn calls

    
  public void refresh() {
    source.newPixels();
    update();
  }


  public void paper(float val) {
    paper(0, 0, 100 - bound(val, 100));
  }

  public void paper(float h, float s, float b) {
    // hopefully this comes back as opaque
    val = Color.HSBtoRGB(bound(h, 100)/100f, 
			 bound(s, 100)/100f, 
			 bound(b, 100)/100f);
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = val;
    }
  }


  public void drawRect(float fx1, float fy1, float fx2, float fy2) {
    line(fx1, fy1, fx2, fy1);
    line(fx1, fy2, fx2, fy2);
    line(fx1, fy1, fx1, fy2);
    line(fx2, fy1, fx2, fy2);
  }

  public void fillRect(float fx1, float fy1, float fx2, float fy2) {
    int x1 = (int)fx1; 
    int x2 = (int)fx2;
    int y1 = (int)fy1;
    int y2 = (int)fy2;

    // don't even draw if it's completely offscreen
    if (((x1 < 0) && (x2 < 0)) || 
	((x1 > width1) && (x2 > width1))) return;
    if (((y1 < 0) && (y2 < 0)) ||
	((y1 > height1) && (y2 > height1))) return;
	  	
    x1 = bound(x1, width1);
    y1 = bound(y1, height1);
    x2 = bound(x2, width1);
    y2 = bound(y2, height1);
	
    if (x2 < x1) { int dummy = x1; x1 = x2; x2 = dummy; }
    if (y2 < y1) { int dummy = y1; y1 = y2; y2 = dummy; }
	
    for (int j = y1; j <= y2; j++) {
      int pp = width*(height1-j);
      for (int i = x1; i <= x2; i++) {
	pixels[pp+i] = penColor;
      }
    }
  }


  public void pen(float val) {
    pen(0, 0, 100 - bound(val, 100));
  }

  public void pen(float h, float s, float b) {
    penColor = Color.HSBtoRGB(bound(h, 100)/100f, 
			      bound(s, 100)/100f, 
			      bound(b, 100)/100f);
  }


  // line clipping code appropriated from 
  // "Computer Graphics for Java Programmers"

  private final void intensifyPixel(int x, int y) {
    if (x<0 || x>width1 || y<0 || y>height1) return;
    pixels[(height1-y)*width + x] = penColor;
  }

  private final int clipCode(float x, float y) {
    return ((x < 0 ? 8 : 0) | (x > width1 ? 4 : 0) |
	    (y < 0 ? 2 : 0) | (y > height1 ? 1 : 0));
  }
    
  public void line(float fox1, float foy1, float fox2, float foy2) {
    int ox1 = (int) fox1;
    int oy1 = (int) foy1;
    int ox2 = (int) fox2;
    int oy2 = (int) foy2;

    // check for line clipping, do it if necessary
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

    // actually draw the line
    int dx, dy, incrE, incrNE, d, x, y, twoV, sdx=0, sdy=0;
    float invDenom, twoDX, scratch, slope;
    int x1, x2, y1, y2, incy=0, incx=0, which;
    boolean backwards = false, slopeDown = false;

    // first do horizontal line
    if (ox1==ox2) {
      x = ox1;
      if(oy1 < oy2) { y1 = oy1; y2 = oy2; }
      else { y1 = oy2; y2 = oy1; }
      for(y=y1;y<=y2;y++) {
	intensifyPixel(x, y);
      }
      return;
    }
    slope = (float)(oy2 - oy1)/(float)(ox2 - ox1);
    // check for which 1, 0 <= slope <= 1
    if (slope >= 0 && slope <= 1) which = 1;
    else if (slope > 1) which = 2;
    else if (slope < -1) which = 3;
    else which = 4;
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
    } else {
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
      intensifyPixel(x, y);
      
      while(x < x2) {
	if(d<0) {
	  twoV = d + dx;
	  d += incrE;
	  ++x;
	} else {
	  twoV = d - dx;
	  d += incrNE;
	  ++x;
	  y+=incy;
	}
	scratch = twoV * invDenom;
	intensifyPixel(x, y);
      }
    } else {
      twoV = 0;
      invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
      twoDX = 2 * sdy * invDenom;
      x = x1;
      y = y1;
      intensifyPixel(x, y);

      while(y < y2) {
	if(d<0) {
	  twoV = d + dy;
	  d += incrE;
	  ++y;
	} else {
	  twoV = d - dy;
	  d += incrNE;
	  ++y;
	  x+=incx;
	}
	scratch = twoV * invDenom;
	intensifyPixel(x, y);
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

  private final float bound(float input, float upper) {
    if (input > upper) return upper;
    else if (input < 0) return 0;
    else return input;
  }


  public int[] getPixels() {
    return pixels;
  }


  // awful way to do printing, but sometimes brute force is
  // just the way. java printing across multiple platforms is
  // outrageously inconsistent.
    
  public void print(Graphics printerg, int offsetX, int offsetY) {
    //g.drawImage(image, offsetX, offsetY, null);
	
    int index = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
	// hopefully little overhead in setting color
	printerg.setColor(new Color([pixels[index++]]));
	printerg.drawLine(offsetX + x, offsetY + y,
			  offsetX + x, offsetY + y);
      }
    }
  }
    

  public void reset() {
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = 0xffffffff;
    }
    penColor = 0xff000000;

    for (int i = 0; i < 3; i++)
      mouse[i] = 0;
    for (int i = 0; i < 26; i++) 
      key[i] = 0;
    for (int i = 0; i < 1000; i++)
      array[i] = 0;
  }


  public void setPixel(int x, int y, float gray) {
    setPixel(x, y, 0, 0, 100 - gray);
  }

  public void setPixel(int x, int y, float h, float s, float b) {
    if (x < 0 || x > width1 || y < 0 || y > height1) return;

    val = Color.HSBtoRGB(bound(h, 100)/100f, 
			 bound(s, 100)/100f, 
			 bound(b, 100)/100f);
    pixels[index] = val;
  }


  public float[] getPixel(int x, int y) {
    int val =  pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		     ((x<0)?0:((x>width1)?width1:x))];
    float hsb[] = new float[3];
    Color.RGBtoHSB((val >> 16) & 0xff, (val >> 8) & 0xff, 
		   val & 0xff, hsb);
    hsb[0] *= 100;
    hsb[1] *= 100;
    hsb[2] *= 100;
    return hsb;
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


  ////////////////////////////////////////////////////////////

  // panel methods, get connector input, etc.

  public Dimension preferredSize() {
    return new Dimension(width, height);
  }


  public void update() {
    if (panelg == null)
      panelg = this.getGraphics();
    if (panelg != null)
      paint(panelg);

#ifdef RECORDER
    // maybe this should go inside DbnEditorGraphics, 
    // but i'm not sure
    DbnRecorder.addFrame(pixels);
#endif
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics screen) {
    if (image == null) {
      source = new MemoryImageSource(width, height, pixels, 0, width);
      source.setAnimated(true);
      image = createImage(source);
      if (image == null) return;
    }
    // avoid an exception during quit
    if (screen != null) {
      screen.drawImage(image, 0, 0, null); //this);
    }
  }


  public void idle(long currentTime) {
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


  public boolean mouseDown(Event e, int x, int y) {
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
    return updateMouse(e, x, y);
  }

  public boolean mouseExit(Event e, int x, int y) {
    return updateMouse(e, x, y);
  }

  public boolean updateMouse(Event e, int x, int y) {
    mouse[0] = x;
    mouse[1] = height1 - y;
    return true;
  }
}


#endif
