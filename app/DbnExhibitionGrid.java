import java.awt.*;
import java.util.*;


public class DbnExhibitionGrid extends Panel implements DbnEnvironment {
    static final int MARGARINE = 20;

    DbnApplet app;
    String programs[];
    String people[];
    String description[][];

    int gx[], gy[];
    int gwidth, gheight;
    DbnGraphics graphics[];
    int gcount;
    int gcurrent;
    DbnRunner runner;
    boolean inited;

    DbnGraphics glast;
    int blessedX, blessedY;

    Vector runners;

    Label nameLabel;
    Label paragraph[];

    /*
    static final int MOVER_STEPS = 15;
    double moverSteps = (double)MOVER_STEPS;
    double moverSteps1[] = new double[MOVER_STEPS];
    double moverSteps2[] = new double[MOVER_STEPS];
    
    DbnGraphics mover1, mover2;
    double mover1x, mover1y;
    double mover1dx, mover1dy;
    double mover2x, mover2y;
    double mover2dx, mover2dy;

    Thread mover;
    */

    public DbnExhibitionGrid(DbnApplet app, String programs[], String people[]) {
	this.app = app;
	this.programs = programs;
	this.people = people;

	gwidth = 101;
	gheight = 101;

	runners = new Vector();

	gcount = programs.length;
	graphics = new DbnGraphics[gcount];
	for (int i = 0; i < gcount; i++) {
	    graphics[i] = new DbnGraphics(gwidth, gheight);
	    graphics[i].disable();
	    add(graphics[i]);
	}
	gx = new int[gcount];
	gy = new int[gcount];
	
	setBackground(Color.orange);

	nameLabel = new Label();
	add(nameLabel);
	paragraph = new Label[6];
	for (int i = 0; i < paragraph.length; i++) {
	    paragraph[i] = new Label();
	    add(paragraph[i]);
	}
	gcurrent = -1;
	glast = null;
	/*
	for (int i = 0; i < MOVER_STEPS; i++) {
	    moverSteps1[i] = Math.sin(((double)i / moverSteps) * Math.PI / 2.0);
	    moverSteps2[i] = 1.0 - mover1[i];
	}
	mover = new Thread(this);
	mover.start();
	*/
    }

    /*
    double floater = 0.0;
    public void run() {
	while (Thread.currentThread() == mover) {
	    floater += 0.01;
	    if (gcurrent != -1) {
		graphics[gcurrent].setLocation((int) (Math.cos(floater) * 200.0),
					       (int) (Math.sin(floater) * 200.0));
	    }
	}
    }
    */

    public Dimension preferredSize() {
	//return new Dimension(gwidth + 150, 350);
	int count = programs.length;
	int wide = Math.min(count, 4);
	int high = (int)Math.ceil((float)count / (float)wide);
	//return new Dimension(50 + wide*gwidth + MARGARINE*(wide-1),
	//	     50 + high*gheight + MARGARINE*(high-1));
	return new Dimension(1024, 768);
    }

    //public void update(Graphics g) {
    //paint(g);
    //}

    public void paint(Graphics screen) {
	if (!inited) {
	    setLayout(null);
	    Dimension dim = size();

	    //int margin = 20;
	    int ncols = 4;
	    //int ncols = (dim.width - MARGARINE) / (gwidth+MARGARINE);
	    int marginX = (dim.width - (ncols*gwidth + (ncols-1)*MARGARINE)) / 2;
	    //int marginY = marginX;
	    //int nrows = (gcount + ncols-1) / ncols;
	    int nrows = 4;
	    int marginY = (dim.height - (nrows*gheight + (nrows-1)*MARGARINE)) / 2;
	    
	    if (gcount == 1) {
		gx[0] = (dim.width - gwidth) / 2;
		gy[0] = (dim.height - gheight) / 2;
	    } else {
		int x = 0, y = 0;
		for (int i = 0; i < gcount; i++) {
		    gx[i] = x + marginX;
		    gy[i] = y + marginY;
		    
		    if (((i+1) % ncols) == 0) {
			y += gheight + MARGARINE;
			x = 0;
		    } else {
			x += gwidth + MARGARINE;
		    }
		}
	    }
	    for (int i = 0; i < gcount-1; i++) {
		graphics[i].reshape(gx[i] - 150, gy[i], gwidth, gheight);
	    }
	    int textX = gx[3] + 50;
	    blessedX = textX;
	    blessedY = gy[7];
	    graphics[gcount-1].reshape(blessedX, blessedY, gwidth, gheight);

	    Font nameFont = new Font("helvetica", Font.BOLD, 11);
	    Font paraFont = new Font("helvetica", Font.PLAIN, 11);
	    FontMetrics nameMetrics = getFontMetrics(nameFont);
	    FontMetrics paraMetrics = getFontMetrics(paraFont);
	    int textY = gy[11];
	    int textHeight = nameMetrics.getAscent() + nameMetrics.getDescent();
	    nameLabel.setFont(nameFont);
	    nameLabel.reshape(textX, textY, 200, textHeight);
	    textY += textHeight;

	    for (int i = 0; i < paragraph.length; i++) {
		paragraph[i].setFont(paraFont);
		paragraph[i].reshape(textX, textY, 200, textHeight);
		textY += textHeight;
		//paragraph[i].setText("blah blah sdlfkjsdflkj " + i);
	    }
	    description = new String[gcount][];
	    for (int i = 0; i < gcount; i++) {
		description[i] = breakLines(programs[i], paraMetrics, 200);
	    }
	    inited = true;
	    //System.err.println("initializing!");
	    //initiate(16);
	}
	//paintAll(screen);
	// draw rectangles around each of the little guys
    }

    public String[] breakLines(String program, FontMetrics metrics, 
			       int maxWidth) {
	//System.out.println(program);
	Vector lines = new Vector();
	char stuff[] = program.toCharArray();
	int start = 0;
	String line = null;
	boolean finished = false;
	for (int i = 0; i < stuff.length; i++) {
	    //System.out.println(stuff[i]);
	    if (stuff[i] == '\r') {
		line = new String(stuff, start, i - start);
		if (stuff[i] == '\n') {
		    i++;
		}
	    } else if (stuff[i] == '\n') {
		line = new String(stuff, start, i - start);
	    }
	    if (line != null) {
		start = i + 1;
		if (line.length() == 0) {
		    finished = true;
		} else {
		    int offset = 0;
		    if ((line.charAt(0) == '/') && (line.charAt(1) == '/')) {
			offset += 2;
		    }
		    if ((line.charAt(0) == ';') || (line.charAt(0) == '#')) {
			offset += 1;
		    }
		    //while (line.charAt(offset) == ' ') offset++;
		    line = line.substring(offset);
		    line = line.trim();
		    if ((offset != 0) && (line.length() != 0)) {
			lines.addElement(line);
			line = null;
			//System.out.print("adding: ");
		    } else {
			if (lines.size() > 0) { // ignore if it's the first line
			    finished = true;
			    //System.err.println("FINISHED");
			}
		    }
		}
		//System.out.println("'" + line + "'");
		//line = null;
		//} else {
		//finished = true;
	    }
	    if (finished) {
		String list[] = new String[lines.size()];
		lines.copyInto(list);
		lines.removeAllElements();
		StringBuffer buffer = new StringBuffer();
		for (int j = 0; j < list.length; j++) {
		    buffer.append(list[j]);
		    buffer.append(' ');
		}
		String buff = buffer.toString();
		//System.out.println(buff);
		StringTokenizer st = new StringTokenizer(buff, " ");
		String newbie = st.hasMoreTokens() ? st.nextToken() : null;
		while (st.hasMoreTokens()) {
		    String s = st.nextToken();
		    int newWidth = metrics.stringWidth(newbie + " " + s);
		    if (newWidth > maxWidth) {
			lines.addElement(newbie);
			newbie = s;
		    } else {
			newbie += " " + s;
		    }
		}
		lines.addElement((newbie == null) ? "" : newbie);
		//System.out.println(newbie);
		break;
	    }
	}
	//System.out.println(lines.size());
	String potato[] = new String[lines.size()];
	lines.copyInto(potato);
	for (int i = 0; i < potato.length; i++) {
	    System.out.println(potato[i]);
	}
	System.out.println("-------------");
	//System.err.println("DONE.");
	return potato;
    }


    public boolean mouseDown(Event ev, int x, int y) {
	if (gcurrent != -1) {
	    //Rectangle r = graphics[gcurrent].getBounds();
	    if (graphics[gcurrent].contains(x, y)) {
		return true;
	    } else {
		// kill the currently running applet
		//graphics[gcurrent].terminate();
		terminate();
		//graphics[gcurrent].disable();
		//gcurrent = -1;
	    }
	}
	// figure out what was selected
	for (int i = 0; i < gcount; i++) {
	    Rectangle r = graphics[i].getBounds();
	    //if (graphics[i].contains(x, y)) {
	    if (r.contains(x, y)) {
		System.err.println("starting " + i);
		initiate(i);
		return true;
	    }
	}
	return true;
    }


    protected void initiate(int which) {
	//graphics[gcurrent].reset();
	nameLabel.setText(people[which]);
	//System.out.println(description + " " + paragraph);
	int howmany = Math.min(description[which].length, paragraph.length);
	for (int i = 0; i < howmany; i++) {
	    paragraph[i].setText(description[which][i]);
	}
	for (int i = howmany; i < paragraph.length; i++) {
	    paragraph[i].setText("");
	}
	// move the selected one to the location of the 
	// previously selected one
	/*
	Point lastloc, newloc;
	DbnGraphics gnew = graphics[which];
	newloc = gnew.getLocation();
	if (glast != null) {
	    lastloc = glast.getLocation();
	    glast.setLocation(newloc);
	} else {
	    lastloc = new Point(blessedX, blessedY);
	}
	gnew.setLocation(lastloc);
	*/
	/*
	Point blessed, newblessed;
	newblessed = graphics[which].getLocation();
	if (glast != null) {
	    blessed = glast.getLocation();
	    glast.setLocation(newblessed.x, newblessed.y);
	} else {
	    blessed = new Point(blessedX, blessedY);
	}
	newblessed.setLocation(blessed.x, blessed.y);
	*/
	gcurrent = which;
	glast = graphics[gcurrent];
	graphics[gcurrent].setCurrentDbnGraphics();
	runner = new DbnRunner(programs[which], graphics[gcurrent], this);
	runner.start();
	graphics[gcurrent].enable();
	runners.addElement(runner);
    }


    // DbnEnvironment duties

    public void terminate() {
	System.err.println("stopping " + runner);
	if (runner != null) {
	    runner.stop();
	    if (gcurrent != -1) {  // how does this happen?
		graphics[gcurrent].disable();
	    }
	    gcurrent = -1;
	    runners.removeElement(runner);
	    runner = null;
	}
	if (runners.size() > 0) {
	    // some bizarre state that can show up on an
	    // inconsistency of some kind. yuck.
	    System.err.println("golan GOLAN GOLLANNN!");
	    Enumeration e = runners.elements();
	    while (e.hasMoreElements()) {
		DbnRunner deadguy = (DbnRunner)e.nextElement();
		deadguy.stop();
	    }
	    runners.removeAllElements();
	    for (int i = 0; i < gcount; i++) {
		graphics[i].disable();
	    }
	}
	//System.err.println("forced termination not implemented");
    }
    
    public void error(DbnException e) {
	System.err.println(e.getMessage() + " (line " + e.line + ")");
	e.printStackTrace();
    }

    public void finished() { }

    public void message(String msg) {
	System.out.println(msg);
    }
}

/*
class DbnExhibitionMover implements Runnable {
    double x1, y1, dx1, dy1;
    double x2, y2, dx2, dy2;
    DbnGraphics g1, g2;
    
    int step;


    public class DbnExhibitionMover(DbnGraphics g1, DbnGraphics g2) {
	Rectangle r1 = 
    }
}
*/
