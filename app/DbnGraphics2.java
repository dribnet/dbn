#ifdef GRAPHICS2


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;


// TODO reset magnify, screen size, color settings at restart

// TODO gracefully kill python when someone hits play again

// OK fix miscellaneous update bugs

// DONE don't update if it's the same size

// DONE figure out a good get/set pixel setup

// DONE fix mouse input

// DONE switch to toggle between hsb and rgb
//      useRGB, useHSB, useRgbColors, setRGB(true)

// DONE need a better solution for getpixel
//      maybe return an array for python
//      or write into an array

public class DbnGraphics2 extends DbnGraphics {
  MemoryImageSource source;
  Graphics panelg;

  Image screenImage;
  int gx, gy;

  Color bgColor;

  int pixels[];
  int pixelCount;
  int penColor;

  static final int HSB = 0;
  static final int RGB = 1;
  int colorModel;

  int magnification = 1;
  Frame frame;

  

  public DbnGraphics2(int width, int height, Color bgColor) {
    this.bgColor = bgColor;
    currentDbnGraphics = this;
    setup(width, height);
  }


  /////////////////////////////////////////////////////////////

  // methods relating to dbn calls


  public void useRgbColor() {
    colorModel = RGB;
  }

  public void useHsbColor() {
    colorModel = HSB;
  }

  public void refresh() {
    if (source != null) {
      source.newPixels();
      update();
    }
  }


  public void magnify(int howmuch) {
    if (howmuch < 1) howmuch = 1;

    magnification = howmuch;
    // fake out the setup function to make it look
    // like something has actually changed
    height += 1000;
    setup(width, height - 1000); 
  }


  public void setup(int width, int height) {
    int oldWidth = this.width;
    int oldHeight = this.height;

    // make sure the jokers don't ask for something ridiculous
    if ((width < 1) || (height < 1)) {
      if ((oldWidth != 0) && (oldHeight != 0))
	return;
      setup(101, 101);
      return;
    }

    this.width = width;
    this.height = height;
    width1 = width - 1;
    height1 = height - 1;
    this.bgColor = bgColor;

    pixelCount = width * height;
    pixels = new int[pixelCount];
    for (int i = 0; i < pixelCount; i++)
      pixels[i] = 0xffffffff;
    penColor = 0xff000000;
    colorModel = HSB;
    //magnification = 1;

    source = new MemoryImageSource(width, height, pixels, 0, width);
    source.setAnimated(true);
    //source.setFullBufferUpdates(true);
    image = Toolkit.getDefaultToolkit().createImage(source);

    screenImage = null; // so that it gets reshaped
    update();

    if (oldWidth != width || oldHeight != height) {
      if (getParent() != null) {
	getParent().getParent().getParent().doLayout();
	frame = (Frame) getParent().getParent().getParent().getParent();
	frame.pack();
	//System.err.println("packed");
      }
    }
  }


  public void paper(int val) {
    paper((float)val);
  }

  public void paper(float valf) {
    int val = 255 - (int) (boundf(valf, 100) * 2.55f);
    val = 0xff000000 | (val << 16) | (val << 8) | val;
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = val;
    }
  }

  public void paperColor(float h, float s, float b) {
    int val = 0;
    if (colorModel == HSB) {
      val = Color.HSBtoRGB(boundf(h, 100)/100f, 
			   boundf(s, 100)/100f, 
			   boundf(b, 100)/100f);
    } else {
      int red = (int) (h * 2.55);
      int green = (int) (s * 2.55);
      int blue = (int) (b * 2.55);
      val = 0xff000000 | (red << 16) | (green << 8) | blue;      
    }
    //System.out.println(val);
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


  public void pen(int val) {
    pen((float)val);
  }

  public void pen(float valf) {
    int val = 255 - (int) (boundf(valf, 100) * 2.55);
    penColor = 0xff000000 | (val << 16) | (val << 8) | val;
  }

  public void penColor(float h, float s, float b) {
    if (colorModel == HSB) {
      penColor = Color.HSBtoRGB(boundf(h, 100)/100f, 
				boundf(s, 100)/100f, 
				boundf(b, 100)/100f);
    } else {
      int red = (int) (h * 2.55);
      int green = (int) (s * 2.55);
      int blue = (int) (b * 2.55);
      penColor = 0xff000000 | (red << 16) | (green << 8) | blue;      
    }
  }


  public void intensifyPixel(int x, int y, float ignored) {
    if (x<0 || x>width1 || y<0 || y>height1) return;
    pixels[(height1-y)*width + x] = penColor;
  }

  public void line(int ox1, int oy1, int ox2, int oy2) {
    bresenham(ox1, oy1, ox2, oy2, false);
  }

  public void line(float fox1, float foy1, float fox2, float foy2) {
    bresenham((int)fox1, (int)foy1, (int)fox2, (int)foy2, false);
  }


  ////////////////////////////////////////////////////////////

  // unsupported methods


  public void norefresh() {
    System.err.println("norefresh notneeded, just be sure to use refresh");
  }

  public void field(int a, int b, int c, int d, int e) {
    System.err.println("field not supported, use fillRect(x1, y1, x2, y2) instead");
  }

  public void antialias(int m) {
    System.err.println("antialias not supported, don't bother");
  }


  ////////////////////////////////////////////////////////////

  // related methods, or likely to be called by alternate 
  // implementations like scheme and python

  private final float boundf(float input, float upper) {
    if (input > upper) return upper;
    else if (input < 0) return 0;
    else return input;
  }


  public void print(Graphics printg, int offsetX, int offsetY) {
    int index = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
	printg.setColor(new Color(pixels[index++]));
	printg.drawLine(offsetX + x, offsetY + y,
			offsetX + x, offsetY + y);
      }
    }
  }


  public void reset() {
    for (int i = 0; i < pixelCount; i++) {
      pixels[i] = 0xffffffff;
    }
    penColor = 0xff000000;
    colorModel = HSB;
    magnification = 1;

    for (int i = 0; i < 3; i++)
      mouse[i] = 0;
    for (int i = 0; i < 26; i++) 
      key[i] = 0;
    for (int i = 0; i < 1000; i++)
      array[i] = 0;
  }


  public void setPixel(int x, int y, float gray) {
    setPixelColor(x, y, 0, 0, 100 - gray);
  }

  public void setPixelColor(int x, int y, float h, float s, float b) {
    if (x < 0 || x > width1 || y < 0 || y > height1) return;

    int val = Color.HSBtoRGB(boundf(h, 100)/100f, 
			     boundf(s, 100)/100f, 
			     boundf(b, 100)/100f);
    pixels[(height1-y) + x] = val;
  }

  public void setPixelInt(int x, int y, int val) {
    pixels[(height1-y) + x] = val;
  }


  public float[] getPixelColor(int x, int y) {
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

  public int getPixelInt(int x, int y) {
    return pixels[(height1-((y<0)?0:((y>height1)?height1:y)))*width + 
		 ((x<0)?0:((x>width1)?width1:x))];
  }


  ////////////////////////////////////////////////////////////

  // panel methods, get connector input, etc.

  public Dimension preferredSize() {
    return new Dimension(width1*magnification + 30, 
			 height1*magnification + 30);
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

  public boolean updateMouse(Event e, int x, int y) {
    x -= gx;
    y -= gy;
    x /= magnification;
    y /= magnification;
    mouse[0] = x;
    mouse[1] = height1 - y;
    return true;
  }
}


#endif
