#ifdef EDITOR

import java.awt.*;


public class DbnEditorGraphics extends DbnGraphics {
  Image screenImage;
  int gx, gy;

  Color bgColor;
  Color bgStippleColor;
  //Image bgImage;

  public DbnEditorGraphics(int width, int height,
			   Color bgColor, Color bgStippleColor) {
    super(width, height);
    this.bgColor = bgColor;
    this.bgStippleColor = bgStippleColor;
  }

  public Dimension preferredSize() {
    return new Dimension(width1 + 100, height1 + 100);
  }

  public void paint(Graphics screen) {
    if (screenImage == null) {
      screenImage = createImage(width1 + 100, height1 + 100);
      gx = gy = 50;

      Graphics g = screenImage.getGraphics();

      // make the background a color
      Dimension dim = size();
      g.setColor(bgColor);
      g.fillRect(0, 0, dim.width, dim.height);
	    
      // draw the tick marks
	    
      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width+2, height+2);
    }

    // draw the actual runner
    //g = 
    //g.drawImage(image, gx, gy, null);
    if (image != null)
      screenImage.getGraphics().drawImage(image, gx, gy, null);

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
