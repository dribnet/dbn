#ifdef EDITOR

import java.awt.*;


public class DbnEditorGraphics extends DbnGraphics {
  static Font plainFont = new Font("Helvetica", Font.PLAIN, 10);

  Image screenImage;
  Graphics screenImageGraphics;
  int gx, gy;

  Color tickColor;
  Color bgColor;
  Color bgStippleColor;
  //Image bgImage;

  public DbnEditorGraphics(int width, int height, Color tickColor,
			   Color bgColor, Color bgStippleColor) {
    super(width, height);
    this.tickColor = tickColor;
    this.bgColor = bgColor;
    this.bgStippleColor = bgStippleColor;
  }

  public Dimension preferredSize() {
    return new Dimension(width1 + 100, height1 + 100);
  }

  public void paint(Graphics screen) {
    if (image == null) {  // from superclass
      //System.err.println("creating new image");
      image = createImage(width, height);
      if (image == null) return;
      g = image.getGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
    }
    if (screenImage == null) {
      //System.err.println("working on screenImage");
      //Dimension dim = size();
      Dimension dim = new Dimension(width + 100, height + 100);
      screenImage = createImage(dim.width, dim.height);
      //screenImage = createImage(width1 + 100, height1 + 100);
      Graphics g = screenImage.getGraphics();
      //gx = gy = 50;
      gx = (dim.width - width) / 2;
      gy = (dim.height - height) / 2;

      // draw background
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
      //if (tickColor != null) {
      g.setColor(tickColor);
      int increment = 20;
      int x, y;
      y = gy + height;
      for (x = 0; x < width; x += increment) {
	g.drawLine(gx + x, y, gx + x, y + 4);
	String num = String.valueOf(x);
	g.drawString(num, gx + x - 1, y + 2 + lineheight);
      }
      for (y = 0; y < height; y += increment) {
	g.drawLine(gx - 4, gy + y, gx, gy + y);
	String num = String.valueOf(y);
	int numWidth = metrics.stringWidth(num);
	g.drawString(num, gx - 6 - numWidth, 
		     gy + height - y);
      }

      /*
      // put the little message below
      if (titling != null) {
	g.setColor(titlingColor);
	int y = gy + runnerHeight + lineheight*2 + 4;
	StringTokenizer st = new StringTokenizer(titling,";");
	
	while (st.hasMoreTokens()) {
	  String s = st.nextToken();
	  g.drawString(st.nextToken(), gx, y);
	  y += lineheight;
	}
      }
      */

      //}
      // make the background a color
      //Dimension dim = size();
      //g.setColor(bgColor);
      //g.fillRect(0, 0, dim.width, dim.height);
	    
      // draw the tick marks
	    
      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width+1, height+1);
    }

    if (image != null) {
      if (screenImageGraphics == null)
	screenImageGraphics = screenImage.getGraphics();
      if (screenImageGraphics != null)
	screenImageGraphics.drawImage(image, gx, gy, null);
    }
    // blit to screen
    screen.drawImage(screenImage, 0, 0, null);
  }

  public boolean updateMouse(int x, int y) {
    mouse[0] = x - gx;
    mouse[1] = height1 - (y - gy);
    return true;
  }
}


#endif
