#ifdef EDITOR

import java.awt.*;


public class DbnEditorGraphics extends DbnGraphics {
  static Font plainFont = new Font("Helvetica", Font.PLAIN, 10);

  //Image screenImage;
  //Graphics screenImageGraphics;
  //int gx, gy;

  Color tickColor;
  //Color bgColor;
  Color bgStippleColor;

  DbnEditor editor;
  //Frame frame;


  public DbnEditorGraphics(int width, int height, Color tickColor,
			   Color bgColor, Color bgStippleColor, 
			   DbnEditor editor /*, Frame frame*/) {
    super(width, height, bgColor);
    this.tickColor = tickColor;
    //this.bgColor = bgColor;
    this.bgStippleColor = bgStippleColor;
    this.editor = editor;
    //this.frame = frame;
  }


  public Dimension preferredSize() {
    return new Dimension(width1*magnification + 100, 
			 height1*magnification + 100);
  }


  public void base() {
    //if ((baseImage == null) || (updateBase)) {
    if (baseImage == null) updateBase = true;

    if (updateBase) {
      // these few lines completely identical to DbnGraphics.base()
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
      if (!bgColor.equals(bgStippleColor)) {
	g.setColor(bgStippleColor);
	int count = 2 * Math.max(dim.width, dim.height);
	for (int i = 0; i < count; i += 2) {
	  g.drawLine(0, i, i, 0);
	}
      }
      g.setFont(plainFont);
      FontMetrics metrics = g.getFontMetrics();
      int lineheight = metrics.getAscent() + 
	metrics.getDescent();

      // put ticks around (only if in edit mode)
      g.setColor(tickColor);
      int increment = (width > 101) ? 25 : 20;
      int x, y;
      y = gy + height*magnification;
      for (x = 0; x <= width; x += increment) {
	int xx = x * magnification;
	g.drawLine(gx + xx, y, gx + xx, y + 4);
	String num = String.valueOf(x);
	g.drawString(num, gx + xx - 1, y + 2 + lineheight);
      }
      for (y = 0; y <= height; y += increment) {
	int yy = y * magnification;
	g.drawLine(gx - 4, gy + yy, gx, gy + yy);
	String num = String.valueOf(y);
	int numWidth = metrics.stringWidth(num);
	g.drawString(num, gx - 6 - numWidth, 
		     gy + height*magnification - yy);
      }
      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width*magnification+1, height*magnification+1);
    }
  }

  /*
  public void paint(Graphics screen) {
    if (image != null) {
      if (screenImageGraphics == null)
	screenImageGraphics = screenImage.getGraphics();
      if (screenImageGraphics != null)
	screenImageGraphics.drawImage(image, gx, gy, null);
    }
    // blit to screen
    if (screen != null) {
      screen.drawImage(screenImage, 0, 0, null);
    }
    //screen.drawImage(image, gx, gy, null);
  }


  public boolean mouseEnter(Event e, int x, int y) {    
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

#ifdef RECORDER
    if (e.controlDown() && (mouse[2] == 100)) {
      Experimental.screenGrab(lastImage, width, height);
    }
#endif

    if (e.shiftDown()) {
      //System.out.println(getLine(x, height1 - y));
      editor.highlightLine(getLine(x, height1 - y));
      return true;
    }
    mouse[0] = x;
    mouse[1] = height1 - y;
    return true;
  }
*/ 

  public boolean updateMouse(Event e, int x, int y) {
    super.updateMouse(e, x, y);

    //#ifdef RECORDER
    if (e.controlDown() && (mouse[2] == 100)) {
      //Experimental.screenGrab(lastImage, width, height);
      editor.doSaveTiff();
    }
    //#endif

    if (e.shiftDown()) {
      editor.highlightLine(getLine(x, height1 - y));
    }

    return true;
  }
}


#endif
