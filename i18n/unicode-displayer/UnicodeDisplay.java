// This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples

// Modified to include Cyberbit, display at a larger size, etc. 
// fry@media.mit.edu

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This program displays Unicode glyphs using user-specified fonts
 * and font styles.
 **/
public class UnicodeDisplay extends Frame implements ActionListener
{
    int page = 0; 
    UnicodePanel p;
    Scrollbar b;
    String fontfamily = "Serif";
    int fontstyle = Font.PLAIN;


    public UnicodeDisplay(String name) {
	super(name);
	this.setLayout(new BorderLayout());
	p = new UnicodePanel();                // Create the panel
	p.setBase((char)(page * 0x100));       // Initialize it
	this.add(p, "Center");                 // Center it

	// Create and set up a scrollbar, and put it on the right
	b = new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, 0xFF);
	b.setUnitIncrement(1);
	b.setBlockIncrement(0x10);
	b.addAdjustmentListener(new AdjustmentListener() {
	    public void adjustmentValueChanged(AdjustmentEvent e) {
		page = e.getValue();
		p.setBase((char)(page * 0x100));
	    }
	});
	this.add(b, "East");

	// Set things up so we respond to window close requests
	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		UnicodeDisplay.this.dispose();
		System.exit(0);
	    }
	});

	// Handle Page Up and Page Down and the up and down arrow keys
	this.addKeyListener(new KeyAdapter() {
	    public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		int oldpage = page;
		if ((code == KeyEvent.VK_PAGE_UP) || 
		    (code == KeyEvent.VK_UP)) {
		    if (e.isShiftDown()) page -= 0x10;
		    else page -= 1;
		    if (page < 0) page = 0;
		}
		else if ((code == KeyEvent.VK_PAGE_DOWN) || 
			 (code==KeyEvent.VK_DOWN)) {
		    if (e.isShiftDown()) page += 0x10;
		    else page += 1;
		    if (page > 0xff) page = 0xff;
		}
		if (page != oldpage) {              // if anything has changed...
		    p.setBase((char) (page * 0x100)); // update the display
		    b.setValue(page);                 // and update scrollbar to match
		}
	    }
	});

	// Set up a menu system to change fonts.  Use a convenience method.
	MenuBar menubar = new MenuBar();
	this.setMenuBar(menubar);
	menubar.add(makemenu("Font Family", new String[] {
	    "Cyberbit", "Serif", "SansSerif", "Monospaced" }, this));
	menubar.add(makemenu("Font Style", new String[] {
	    "Plain", "Italic", "Bold", "BoldItalic" }, this));
    }

    /** This method handles the items in the menubars */
    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	if (cmd.equals("Serif")) fontfamily = "Serif";
	else if (cmd.equals("SansSerif")) fontfamily = "SansSerif";
	else if (cmd.equals("Monospaced")) fontfamily = "Monospaced";
	else if (cmd.equals("Cyberbit")) fontfamily = "Cyberbit";
	else if (cmd.equals("Plain")) fontstyle = Font.PLAIN;
	else if (cmd.equals("Italic")) fontstyle = Font.ITALIC;
	else if (cmd.equals("Bold")) fontstyle = Font.BOLD;
	else if (cmd.equals("BoldItalic")) fontstyle = Font.BOLD + Font.ITALIC;
	p.setFont(fontfamily, fontstyle);
    }

    /** A convenience method to create a Menu from an array of items */
    private Menu makemenu(String name, String[] itemnames, 
			  ActionListener listener) {
	Menu m = new Menu(name);
	for(int i = 0; i < itemnames.length; i++) {
	    MenuItem item = new MenuItem(itemnames[i]);
	    item.addActionListener(listener);
	    item.setActionCommand(itemnames[i]);  // okay here, though
	    m.add(item);
	}
	return m;
    }

    /** The main() program just create a window, packs it, and shows it */
    public static void main(String[] args) {
	UnicodeDisplay f = new UnicodeDisplay("Unicode Displayer");
	f.pack();
	f.show();
    }

    /** 
     * This nested class is the one that displays one "page" of Unicode
     * glyphs at a time.  Each "page" is 256 characters, arranged into 16
     * rows of 16 columns each.
     **/
    public static class UnicodePanel 
	extends Canvas implements MouseListener {

	static final int fontsize = 32;
	static final int lineheight = 40;
	static final int charspacing = 34;
	static final int x0 = 65; 
	static final int y0 = 50;
	static final int y1 = y0 + 15;
	static final int y2 = 40; // top of mouse for selection

	protected char base;  // What character we start the display at
	protected Font font = new Font("cyberbit", Font.PLAIN, fontsize);
	protected Font headingfont = new Font("monospaced", Font.BOLD, 18);
    
	public UnicodePanel() {
	    this.addMouseListener(this);
	}

	static final char hex[] = {
	    '0', '1', '2', '3', '4', '5', '6', '7', 
	    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	public void mouseClicked(MouseEvent e) {
	    int x = (e.getX() - x0) / charspacing;
	    int y = (e.getY() - y2) / lineheight;
	    if ((x >= 0) && (x <= 15) && (y >= 0) && (x <= 15)) {
		int start = (int)base & 0xfff0;
		int which = start + (y*16) + x;
		if ((which > 32) && (which < 128)) {
		    System.out.print((char)which);
		} else {
		    System.out.print("\\u");
		    System.out.print(hex[(which >> 12) & 0xf]);
		    System.out.print(hex[(which >> 8) & 0xf]);
		    System.out.print(hex[(which >> 4) & 0xf]);
		    System.out.print(hex[(which >> 0) & 0xf]);
		}
	    }
	}
	
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }

	/** Specify where to begin displaying, and re-display */
	public void setBase(char base) { this.base = base; repaint(); }
    
	/** Set a new font name or style, and redisplay */
	public void setFont(String family, int style) { 
	    this.font = new Font(family, style, fontsize); 
	    repaint(); 
	}
    
	/**
	 * The paint() method actually draws the page of glyphs 
	 **/
	public void paint(Graphics g) {
	    int start = (int)base & 0xFFF0;  // Start on a 16-character boundary
	    // Draw the headings in a special font
	    g.setFont(headingfont);
      
	    // Draw 0..F on top
	    for(int i=0; i < 16; i++) {
		String s = Integer.toString(i, 16);
		g.drawString(s, x0 + i*charspacing, y0-20);
	    }
      
	    // Draw column down left.
	    for(int i = 0; i < 16; i++) {
		int j = start + i*16;
		String s = Integer.toString(j, 16);
		g.drawString(s, 10, 15 + y0+i*lineheight);
	    }

	    // Now draw the characters
	    g.setFont(font);
	    char[] c = new char[1];
	    for(int i = 0; i < 16; i++) {
		for(int j = 0; j < 16; j++) {
		    c[0] = (char)(start + j*16 + i);
		    g.drawChars(c, 0, 1, 
				x0 + i*charspacing, 
				y1 + j*lineheight);
		}
	    }
	}
    
	public Dimension getPreferredSize() { 
	    //return new Dimension(410, 430); 
	    return new Dimension(630, 670);
	}
    }
}
