// Based on code from "Java Examples in a Nutshell"
// Copyright (c) 1997 by David Flanagan
// http://www.davidflanagan.com/javaexamples

import java.awt.*;
import java.util.*;


public class MultiLineLabel extends Component {
  // User-specified attributes
  Font font;
  String label;             // The label, not broken into lines
  int marginWidth;         // Left and right margins
  int marginHeight;        // Top and bottom margins
  int componentWidth;
  int alignment;            // The alignment of the text.

  static final int LEFT = 0;
  static final int CENTER = 1;
  static final int RIGHT = 2;

  // Computed state values
  protected int lineCount;            // The number of lines
  protected String[] lines;           // The label, broken into lines
  protected int[] lineWidths;        // How wide each line is
  //protected int maxWidth;            // The width of the widest line
  protected int lineHeight;          // Total height of the font
  protected int lineAscent;          // Font height above baseline
  //protected boolean measured = false; // Have the lines been measured?

  Image offscreen;
  int offscreenWidth;
  int offscreenHeight;


  // Here are five versions of the constructor
  public MultiLineLabel(String label, Font font, int componentWidth,
			int marginWidth, int marginHeight, int alignment) {
    this.label = label;
    //super.setFont(font);
    this.font = font;
    this.componentWidth = componentWidth;
    this.marginWidth = marginWidth;
    this.marginHeight = marginHeight;
    this.alignment = alignment;
    breakLabel();
  }


  // Methods to set and query the various attributes of the component
  // Note that some query methods are inherited from the superclass.
  public void setLabel(String label) {
    this.label = label;
    breakLabel();
    //newLabel();               // Break the label into lines
    //measured = false;         // Note that we need to measure lines
    repaint();                // Request a redraw
  }

  /*
  public void setFont(Font f) {
    super.setFont(f);         // tell our superclass about the new font
    breakLabel();
    //measured = false;         // Note that we need to remeasure lines
    repaint();                // Request a redraw
  }

  public void setForeground(Color c) {
    super.setForeground(c);   // tell our superclass about the new color
    repaint();                // Request a redraw (size is unchanged)
  }
  */

  //public void setAlignment(int a) { alignment = a; repaint(); }
  //public void setMarginWidth(int mw) { marginWidth = mw; repaint(); }
  //public void setMarginHeight(int mh) { marginHeight = mh; repaint(); }
  //public String getLabel() { return label; }
  //public int getAlignment() { return alignment; }
  //public int getMarginWidth() { return marginWidth; }
  //public int getMarginHeight() { return marginHeight; }


  public Dimension preferredSize() {
    //breakLabel();

    //if (!measured) measure();
    //return new Dimension(maxWidth + 2*marginWidth,
    //                   lineCount * lineHeight + 2*marginHeight);
    return new Dimension(componentWidth, 
			 lineCount * lineHeight + 2*marginHeight);
  }

  //public Dimension minimumSize() { 
  //return preferredSize(); 
  //}


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
    y = lineAscent + (size.height - lineCount * lineHeight)/2;
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

  protected synchronized void breakLabel() {
    int maxWidth = componentWidth - marginWidth*2;

    Vector vector = new Vector();
    StringTokenizer st = new StringTokenizer(label);
    FontMetrics metrics = getToolkit().getFontMetrics(font);

    String currentLine = st.hasMoreTokens() ? st.nextToken() : null;
    while (st.hasMoreTokens()) {
      String newWord = st.nextToken();
      int newWidth = metrics.stringWidth(currentLine + " " + newWord);
      if (newWidth > maxWidth) {
	vector.addElement(currentLine);
	currentLine = newWord;
      } else {
	currentLine += " " + newWord;
      }
    }
    vector.addElement((currentLine == null) ? "" : currentLine);

    lineCount = vector.size();
    lines = new String[lineCount];
    lineWidths = new int[lineCount];
    vector.copyInto(lines);
    for (int i = 0; i < lineCount; i++) {
      lineWidths[i] = metrics.stringWidth(lines[i]);
    }
    lineHeight = metrics.getHeight();
    lineAscent = metrics.getAscent();
  }


  /*
  protected synchronized void newLabel() {
    StringTokenizer t = new StringTokenizer(label, "\n");
    lineCount = t.countTokens();
    lines = new String[lineCount];
    lineWidths = new int[lineCount];
    for(int i = 0; i < lineCount; i++) lines[i] = t.nextToken();
  }
  */


  protected synchronized void measure() {
    /*
    FontMetrics fm = getToolkit().getFontMetrics(getFont());
    lineHeight = fm.getHeight();
    lineAscent = fm.getAscent();
    maxWidth = 0;
    for(int i = 0; i < lineCount; i++) {
      lineWidths[i] = fm.stringWidth(lines[i]);
      if (lineWidths[i] > maxWidth) maxWidth = lineWidths[i];
    }
    measured = true;
    */
  }
}
