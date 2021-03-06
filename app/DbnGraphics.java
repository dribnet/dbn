#ifndef KVM
import java.awt.*;
import java.awt.image.*;
#else
//import com.sun.kjava.Graphics;
#endif

import java.io.*;
import java.net.*;
import java.util.*;


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

  static final int QUICKTIME = 0;
  static final int TIFF = 1;
  static final int ILLUSTRATOR = 2;

  Graphics panelGraphics;

  Image baseImage; // all the background stuff
  Graphics baseGraphics;

  Image dbnImage;
  MemoryImageSource dbnSource;

  Image lastImage;
  Graphics lastGraphics;

  int gx, gy;

  int pixels[];
  int pixelCount;
  int penColor;

  int magnification = 1;

  Color bgColor;
  Frame frame;
  Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

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

  boolean slowdown;
  boolean slowdownParam;
  

  public DbnGraphics(int width, int height, Color bgColor) {
    this.bgColor = bgColor;

    currentDbnGraphics = this; // for python/scheme
#ifdef CRICKET
    openSensor();
#endif
    size(width, height);
  }


  // this may need to be preprocessed from the program,
  // at least its first instance. the idea of re-allocating
  // midway through could really cause a mess. 
  // more important, i don't want to reset the screen to 
  // 100x100 if the programmer is always using a larger area.

  // solution:
  // after parsing program, walk tree to check for instance
  // of a SIZE token. if one exists, don't resize the window
  // during reset(). if there is none, and the size is something
  // different than 101x101, reset the area to 101x101

  // if the size is a 'simple' value (just a number), it might 
  // make sense to change the screen's size at that time, 
  // but that seems needlessly complex. 

  public void size(int wide, int high) {
    if (wide < 1) wide = 1;
    if (high < 1) high = 1;

    if ((wide == width) && (high == height)) return;

    width = wide;
    height = high;
    width1 = width - 1;
    height1 = height - 1;

    pixelCount = width * height;
    pixels = new int[pixelCount];
    lines = new int[pixelCount];

    reset();

    dbnSource = new MemoryImageSource(width, height, pixels, 0, width);
    dbnSource.setAnimated(true);
    //dbnSource.setFullBufferUpdates(true);
    dbnImage = Toolkit.getDefaultToolkit().createImage(dbnSource);

    baseImage = null; // so that it gets reshaped

    repack();
  }


  public void size(int wide, int high, int magnify) {
    int mag = magnification;
    magnification = Math.max(magnify, 1);
    if ((wide == width) && (high == height)) {
      if (mag == magnification) {
	return;  // nothing has changed
      } else {
	baseImage = null;  // clear out bg so it gets updated
	repack();  // resize window and finish
      }
    } else {  // width, height should also change
      size(wide, high);
    }
  }

  /*
  public void magnify(int howmuch) {
    if (howmuch < 1) howmuch = 1;
    magnification = howmuch;
    repack();
    // fake out the setup function to make it look
    // like something has actually changed
    //height += 1000;
    //setup(width, height - 1000); 
  }
  */

  protected void repack() {
    invalidate();
    if (frame != null) frame.pack();
  }


  public void reset() {
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = 0xffffffff;
      lines[i] = -2;
    }
    penColor = 0xff000000;
    aiRefresh = true;
    slowdown = slowdownParam;

    flushCount = 0;
    resetBlockDetect();

    for (int i = 0; i < 3; i++)
      mouse[i] = 0;
    for (int i = 0; i < 26; i++) 
      key[i] = 0;
    for (int i = 0; i < 1000; i++)
      array[i] = 0;
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
    //slowdown();
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
    //slowdown();
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
    for (int y = y1; y <= y2; y++) {
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
    //slowdown();
  }

  public void setPixel(int x, int y, int which, int value) {
    aiFlush(FDOT);
    if (x < 0 || x > width1 || y < 0 || y > height1) return;
    if ((which < 0) || (which > 2)) return;
    int index = (height1-y)*width + x;
    int place = (2-which) * 8;
    value = bound(value, 100);
    pixels[index] &= ~(0xff << place);
    pixels[index] |= colorMap[value] << place;
    lines[index] = currentLine;
    //slowdown();
  }

  public int getPixel(int x, int y) {
    int pixel = pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		      ((x<0)?0:((x>width1)?width1:x))];
    // do the right thing and return an average of the pixel values
    return 100 - (100 * (((pixel >> 16) & 0xff) + 
			 ((pixel >> 8) & 0xff) + 
			 (pixel & 0xff)) / (3*255));
  }

  public int getPixel(int x, int y, int which) {
    if ((which < 0) || (which > 2)) return 0;
    int pixel = pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		      ((x<0)?0:((x>width1)?width1:x))];
    return ((100 * ((pixel >> (2-which)*8) & 0xff)) / 255);
  }


  ////////////////////////////////////////////////////////////

  // grab bag


  // called from code (DbnEngine), implicit 'norefresh'
  public void refresh() {
    aiRefresh = false;
    update();
  }


  public void fast() {
    slowdown = false;
  }


  public void pause(int amount) {
    long stopTime = System.currentTimeMillis() + (amount*10);
    do { } while (System.currentTimeMillis() < stopTime);
  }


  public void save(int format) {
    switch (format) {
#ifdef RECORDER
    case QUICKTIME:
      break;
#endif
    case TIFF:
      break;
    case ILLUSTRATOR:
      break;
    }
  }

  ////////////////////////////////////////////////////////////

  // internal methods, or likely to be called by alternate 
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

  /*
  public byte[] getPixels() {
    // called by editor to save the pixel array to the 
    // courseware server
    //return pixels;
    return null;
  }
  */


  ////////////////////////////////////////////////////////////

  // panel methods

  public Dimension preferredSize() {
    //System.out.println("setting new preferred size");
    //return new Dimension(width1*magnification /*+ 30*/, 
    //		 height1*magnification /*+ 30*/);
    //return new Dimension(width, height);

    return new Dimension(width*magnification, 
			 height*magnification);
  }


  public void update() {
    if (panelGraphics == null)
      panelGraphics = this.getGraphics();
    if (panelGraphics != null)
      paint(panelGraphics);

    flushCount = 0;
    if (dbnSource != null) {
      dbnSource.newPixels();
    }

#ifdef RECORDER
    //System.out.println("update calling addFrame");
    // maybe this should go inside DbnEditorGraphics? not sure
    DbnRecorder.addFrame(pixels, mouse[0], height1-mouse[1], 
			 (mouse[2] == 100));
#endif
  }

  boolean updateBase = false;

  public void update(Graphics g) {
    //System.out.println("calling update, should fix background");
    updateBase = true;
    paint(g);
  }


  public void base() {
    if (baseImage == null) updateBase = true;
    //if ((baseImage == null) || (updateBase)) {
    if (updateBase) {
      //System.out.println("reallocating baseImage");
      //Dimension dim = new Dimension(width + 100, height + 100);
      Dimension dim = preferredSize();
      baseImage = createImage(dim.width, dim.height);
      baseGraphics = baseImage.getGraphics();
      lastImage = createImage(width, height);
      lastGraphics = lastImage.getGraphics();

      gx = (dim.width - width*magnification) / 2;
      gy = (dim.height - height*magnification) / 2;

      // draw background
      Graphics g = baseGraphics;
      g.setColor(bgColor);
      g.fillRect(0, 0, dim.width, dim.height);

      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width*magnification+1, height*magnification+1);
    }
  }


  public void paint(Graphics g) {
    base();

    //if (dbnImage != null) {
    if (baseImage != null) {
      baseGraphics.drawImage(dbnImage, gx, gy, 
			     width*magnification, height*magnification, null);
      // copy into buffer for writing to a tiff or quicktime
      //System.out.println(lastGraphics);
      lastGraphics.drawImage(dbnImage, 0, 0, null); 
      
      //} else {
      //System.out.println("DBN IMAGE *WAS* NULL");
    }
    // avoid an exception during quit
    if ((g != null) && (baseImage != null)) {
      // blit to screen
      g.drawImage(baseImage, 0, 0, null);
      //screen.drawImage(image, gx, gy, 
      //	  width*magnification, height*magnification, null);
    }
    updateBase = false;
  }


  public void paint() {
    // blit only changed portion
    panelGraphics.drawImage(dbnImage, gx, gy, 
			    width*magnification, height*magnification, null);
  }


  static byte tiffHeader[] = {
    77, 77, 0, 42, 0, 0, 0, 8, 0, 9, 0, -2, 0, 4, 0, 0, 0, 1, 0, 0,
    0, 0, 1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 3, 0, 0, 0, 1, 
    0, 0, 0, 0, 1, 2, 0, 3, 0, 0, 0, 3, 0, 0, 0, 122, 1, 6, 0, 3, 0, 
    0, 0, 1, 0, 2, 0, 0, 1, 17, 0, 4, 0, 0, 0, 1, 0, 0, 3, 0, 1, 21, 
    0, 3, 0, 0, 0, 1, 0, 3, 0, 0, 1, 22, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 
    1, 23, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 8
  };

  static byte[] makeTiffData(int pixels[], int width, int height) {
    byte tiff[] = new byte[768 + width*height*3];
    System.arraycopy(tiffHeader, 0, tiff, 0, tiffHeader.length);
    tiff[30] = (byte) ((width >> 8) & 0xff);
    tiff[31] = (byte) ((width) & 0xff);
    tiff[42] = tiff[102] = (byte) ((height >> 8) & 0xff);
    tiff[43] = tiff[103] = (byte) ((height) & 0xff);
    int count = width*height*3;
    tiff[114] = (byte) ((count >> 24) & 0xff);
    tiff[115] = (byte) ((count >> 16) & 0xff);
    tiff[116] = (byte) ((count >> 8) & 0xff);
    tiff[117] = (byte) ((count) & 0xff);
    int index = 768;
    for (int i = 0; i < pixels.length; i++) {
      tiff[index++] = (byte) ((pixels[i] >> 16) & 0xff);
      tiff[index++] = (byte) ((pixels[i] >> 8) & 0xff);
      tiff[index++] = (byte) ((pixels[i] >> 0) & 0xff);
    }
    return tiff;
  }

  public byte[] makeTiffData() {
    return makeTiffData(pixels, width, height);
  }


  ////////////////////////////////////////////////////////////

  // event handling


  public void idle(long currentTime) {
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
    return false;
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
    setCursor(cursor); 
    return true;
  }

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


  ////////////////////////////////////////////////////////////

  // connector-related items


  public final int getMouse(int slot) { // throws DbnException {
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
      //DbnApplet applet = DbnApplet.applet;
      String hostname = DbnApplet.applet.getNetServer();
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


  boolean slowInited;
  long lastMillis;
  int slowCounter;

  static int SLOW_COUNT;
  static int SLOW_MILLIS;

  private final void slowdown() {
    if (!slowInited) {
      slowdownParam = DbnApplet.getBoolean("slowdown", true);
      SLOW_COUNT = DbnApplet.getInteger("slowdown.count", 2);
      SLOW_MILLIS = DbnApplet.getInteger("slowdown.millis", 100);
      slowdown = slowdownParam;
      slowInited = true;
    }

    if (!slowdown) return;
    //System.out.println("slow");

    if (lastMillis == 0) {
      lastMillis = System.currentTimeMillis();
      return;

    } else if (slowCounter == SLOW_COUNT) {
      long millis = System.currentTimeMillis();
      if (millis - lastMillis < SLOW_MILLIS) {
	//System.out.println("slowing down");
	while (System.currentTimeMillis() - lastMillis < SLOW_MILLIS) { }
	millis = System.currentTimeMillis();
	//} else {
	//System.out.println("millis - last = " + (millis-lastMillis));
      }
      refresh();
      slowCounter = 0;
      lastMillis = millis;	

    } else {
      slowCounter++;
    }
  }


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
    insideforeverp = true;
    flushfire = -1;
    autoflushenablep = false;
  }

  public void endForever() {
    if (flushfire == -1) {
      if (aiRefresh) update();
      autoflushenablep= true;
    }
    flushfire = -1;
  }

  // inside a repeat block, this done at the start of single iteration
  public void beginRepeat() {
    slowdown();

    insiderepeatp = true;
    repeatlevel++;
    if (insideforeverp) {
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

  // inside repeat block, this at end of iteration
  public void endRepeat() {
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
}
