import java.awt.*;


public class FcmdLabel extends MultiLineLabel {
  String title;
  Font titleFont = new Font("SansSerif", Font.PLAIN, 21);
  int titleAscent;
  int titleHeight;


  public FcmdLabel(String label, Font font, int componentWidth,
			int marginWidth, int marginHeight, int alignment) {
    super(label, font, componentWidth, 
	  marginWidth, marginHeight, alignment);
  }

  public void setTitle(String title) {
    this.title = title;
    repaint();
  }

  public Dimension preferredSize() {
    Dimension d = super.preferredSize();
    return new Dimension(d.width, d.height + titleHeight);
  }
  
  public void paint(Graphics screen) {
    screen.setFont(font);
    Dimension size = size();
    int savedWidth = componentWidth;
    if (size.width < componentWidth) {
      componentWidth = size.width;
      breakLabel();
      //componentWidth = savedWidth;
    }
    if ((offscreen == null) || 
	(offscreenWidth < size.width) ||
	(offscreenHeight < size.height)) {
      offscreenWidth = size.width;
      offscreenHeight = size.height;
      offscreen = createImage(offscreenWidth, offscreenHeight);
    }
    Graphics g = offscreen.getGraphics();
    g.setColor(getBackground());
    g.fillRect(0, 0, offscreenWidth, offscreenHeight);
    g.setColor(getForeground());

    int x, y;
    //if (!measured) measure();
    y = titleAscent + (size.height - (titleHeight + lineCount*lineHeight))/2;

    g.setFont(titleFont);
    x = (size.width - componentWidth)/2 + marginWidth; 
    g.drawString(title, x, y);
    y += titleHeight - titleAscent;
    y += lineHeight;
    g.setFont(font);

    for (int i = 0; i < lineCount; i++) {
      switch(alignment) {
      default:
      case LEFT:    x = (size.width - componentWidth)/2 + marginWidth; break;
      case CENTER:  x = (size.width - lineWidths[i])/2; break;
      case RIGHT:   x = size.width - marginWidth - lineWidths[i]; break;
      }
      g.drawString(lines[i], x, y);
      y += lineHeight;
    }
    screen.drawImage(offscreen, 0, 0, null);
    if (componentWidth != savedWidth) componentWidth = savedWidth;
  }

}
