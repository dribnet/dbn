import java.awt.*;
import java.applet.Applet;
import java.util.*;

import java.awt.event.*;


class DbnLicensePlate extends Panel implements Runnable {
    DbnGui gui;
    Font f = new Font("Helvetica", Font.PLAIN, 10);
    Thread thread;
    Image im;
    Graphics img;
    
    long birtht;
    int cbw, cbh;
    
    char cs[];
    int xs[];
    int xso[];
    int ys[];
    int slen;
    int dxs[];
    int dys[];
    Color cols[];
	
    String s1 = "Design By Numbers ";
    int s1x, s1y;
    String s2 = "MIT Media Laboratory. " +
	"Aesthetics + Computation Group. " +
	"(C) 1999, Massachusetts Institute of Technology";
    int s2x, s2y;

	
    public DbnLicensePlate(DbnGui gui) {
	super();
	this.gui = gui;
    }
    
    public boolean mouseDown(Event ev, int mx, int my)
    {
	if (thread==null) {
	    thread = new Thread(this);
	    thread.start();
	} else {
	    thread.stop();
	    thread = null;
	    unlaunch();
	}
	return true;
    }
    
    public void start() { }
    public void stop() { }
    
    public void launch(long t)
    {
	int i,k, x;
	Graphics g = this.getGraphics();
	g.setFont(f);
		
	int s1len = s1.length(), s2len = s2.length();
	cs = new char[s1len+s2len];
	xs = new int[s1len+s2len];
	xso = new int[s1len+s2len];
	ys = new int[s1len+s2len];
	dxs = new int[s1len+s2len];
	dys = new int[s1len+s2len];
	cols = new Color[s1len+s2len];
		
	x = s1x;
	for(i=0,k=0;i<s1len;i++,k++) {
	    cs[k] = s1.charAt(i); 
	    ys[k] = s1y;
	    xso[k]=xs[k] = x;
	    x+=g.getFontMetrics().stringWidth(String.valueOf(cs[k]));
	    dxs[k] = cs[k]/4;
	    dys[k] = 0;//cs[k]&0x3;
	    cols[k] = Color.white;
	}

	x = s2x;
	for(i=0;i<s2len;i++,k++) {
	    cs[k] = s2.charAt(i); 
	    ys[k] = s1y;
	    xso[k]=xs[k] = x;
	    x+=g.getFontMetrics().stringWidth(String.valueOf(cs[k]));
	    dxs[k] = cs[k]/4;
	    dys[k] = 0;//cs[k]&0x3;
	    cols[k] = Color.gray;
	}
	slen = s1len+s2len;
		
	birtht = t;
    }
	
    public void unlaunch()
    {
	int k,x,y;
	int n = 20;
	long startt = System.currentTimeMillis();
	long curt = System.currentTimeMillis();
	while((curt-startt)<1000) {
	    Graphics g = this.getGraphics();
	    int i;
	    img.setColor(Color.darkGray);
	    img.fillRect(0,0,cbw,cbh);

	    img.setColor(Color.white);
	    img.setFont(f);
	    float a = (curt-startt)/1000f;
	    a = (1-a);
	    a = a*a;
	    a = 1-a;
	    for(i=0;i<slen;i++) {
		//xs[i]=dxs[i];
		//ys[i]=dys[i];
		x = (int)(xs[i]+a*(xso[i]-xs[i]));
		y = ys[i];
		img.setColor(cols[i]);
		img.drawString(String.valueOf(cs[i]), x, y);
	    }		

	    g.drawImage(im,0,0,this);
	    curt = System.currentTimeMillis();
	}
	paint(this.getGraphics());
    }
	
    public void animate(long t)
    {
	Graphics g = this.getGraphics();
	int i;
	img.setColor(Color.darkGray);
	img.fillRect(0,0,cbw,cbh);

	img.setColor(Color.white);
	img.setFont(f);
	for(i=0;i<slen;i++) {
	    xs[i]+=dxs[i];
	    ys[i]+=dys[i];
	    if (xs[i]>cbw) xs[i]-=cbw;
	    if (ys[i]>cbh) ys[i]-=cbh;
	    if (xs[i]<0) xs[i]+=cbw;
	    if (ys[i]<0) ys[i]+=cbh;
	    img.setColor(cols[i]);
	    img.drawString(String.valueOf(cs[i]),xs[i],ys[i]);
	}		

	g.drawImage(im,0,0,this);
    }
	
    public void run()
    {
	launch(System.currentTimeMillis());
	try {
	    while(true) {
		animate(System.currentTimeMillis());
		Thread.sleep(20);
	    }
	} catch(Exception e) {
	}
    }
	
    public void paint(Graphics gv)
    {
	Rectangle r = bounds();
	
	if (r.width>1) {
	    cbw = r.width; cbh = r.height;
	    im = createImage(r.width,r.height);
	    img = im.getGraphics();

	}
	Graphics g = img;
		
	g.setColor(Color.darkGray); g.fillRect(0,0,r.width,r.height);
	g.setFont(f);
	g.setColor(Color.white);
	g.drawString(s1, s1x=5,s1y=16);
	g.setColor(Color.gray);
	g.drawString(s2, s2x=g.getFontMetrics().stringWidth(s1)+4+s1x,s2y=s1y);
	g.setColor(Color.black);
	g.drawLine(0,r.height-1,r.width,r.height-1);
		
	gv.drawImage(im,0,0,this);	
    }

    public Dimension preferredSize()
    {
        return new Dimension(48,24);
    }	
}



class DbnRunButton extends Panel {
    DbnGui gui;
	
    int tc = 0;
    Rectangle cb;
    int cbw, cbh;
	
    int[] playxs, playys;
    int[] stopxs, stopys;
    int playn, stopn;
	
    int marg = 4;
	
    public DbnRunButton(DbnGui gui) {
	super();
	this.gui = gui;
    }
	
    public void idle() {
	Graphics g = this.getGraphics();
		
	tc++; tc=tc%2;
	if (tc == 0) {
	    g.setColor(Color.white);
	} else {
	    g.setColor(Color.black);
	}
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
    }
	
    public void initiated() {
	//repaint();
    }

    public void terminated()
    {
	repaint();
    }
	
    public boolean mouseEnter(Event ev, int x, int y) {
	Graphics g = this.getGraphics();
	g.setColor(Color.black);
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
	return true;
    }
	
    public boolean mouseExit(Event ev, int x, int y) {
	Graphics g = this.getGraphics();
	g.setColor(Color.gray);
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
	return true;
    }
	
    public void drawmain(Graphics g, boolean runningp) {
	g.setColor(gui.getPanelBgColor()); 
	g.fillRect(0, 0, cbw, cbh);
		
	g.setColor(Color.darkGray);
		
	g.fillRect(marg,marg,cbh-marg*2,cbh-marg*2);
		
	if (runningp) 
	    g.setColor(Color.orange);
	else
	    g.setColor(Color.green);
	if (runningp) {
	    g.fillPolygon(stopxs,stopys,stopn);	
	} else {
	    g.fillPolygon(playxs,playys,playn);	
	}
    }
	
    public boolean mouseDown(Event ev, int x, int y) {
	if (!gui.getrunningp()) {
	    drawmain(this.getGraphics(),true);
	    gui.initiate();
	} else {
	    drawmain(this.getGraphics(),false);
	    gui.terminate();
	}
	return true;
    }
	
    public void paint(Graphics g)
    {	
	Rectangle r = bounds();
	//g.setColor(Color.white);
	//g.fillRect(r.x, r.y, r.width, r.height);

	if (r.width > 1) {
	    cb = r; 
	    cbw = cb.width; 
	    cbh = cb.height; 
	    
	    if (playxs == null) {
		playxs = new int[3];
		playys = new int[3];
		stopxs = new int[4];
		stopys = new int[4]; playn = 3; stopn = 4;
		playxs[0] = marg*3+1;
		playys[0] = marg*3;
		playxs[1] = cbh-marg*3+1;
		playys[1] = cbh/2;
		playxs[2] = marg*3+1;
		playys[2] = cbh-marg*3;
		
		stopxs[0] = marg*3;
		stopys[0] = marg*3+1;
		stopxs[1] = cbh-marg*3+1;
		stopys[1] = marg*3+1;
		stopxs[2] = cbh-marg*3+1;
		stopys[2] = cbh-marg*3+1;
		stopxs[3] = marg*3;
		stopys[3] = cbh-marg*3+1;
	    }
	}
	drawmain(g, gui.getrunningp());
			
	g.setColor(Color.gray);
	g.drawRect(marg, marg, cbh-marg*2, cbh-marg*2);
    }

    public Dimension preferredSize()
    {
        return new Dimension(36, 24);
    }	
}



class DbnRunPanel extends Panel {
    DbnApplet app;
    Vector dbrv;
    int dbrvlen=0;

    public DbnRunner dbr;
    public int emouse[], ekey[], etime[];

    // boolean []keys = new boolean[26];
    long []keyt = new long[26];

    Font f = new Font("Helvetica", Font.PLAIN, 10);
    Font fb = new Font("Helvetica", Font.BOLD, 10);
    Image pat;
    Color fgcol, bgcol;

    static String TEMPER;

    public void idle(long curt)
    {
	Date d = new Date();
	// handle time
	servicekeys(curt);
	etime[0] = d.getHours();
	etime[1] = d.getMinutes();
	etime[2] = d.getSeconds();
	etime[3] = (int) (curt%1000)/10;
    }
	
    public void setcurdbr(DbnRunner db)
    {
	dbr = db;

	Hashtable connectorTable = dbr.dbg.getConnectorTable();
	emouse = (int[]) connectorTable.get("mouse");
	ekey = (int[]) connectorTable.get("key");
	etime = (int[]) connectorTable.get("time");
    }

    public DbnRunner newdbr_at(DbnApplet app, DbnGui gui, String prog, 
			       int x, int y, int w, int h)
    {
	DbnRunner db = new DbnRunner(app, gui, this, x,y,w,h, prog);
	dbrv.addElement(db);
	return db;
    }

    public DbnRunPanel(DbnApplet app, DbnGui gui, String []progs)
    {
	int i;
	int x, y, w, h ;
	this.app = app;
	dbrv = new Vector();
	for(i=0;i<progs.length;i++)  { // doesn't look at x and y actually
	    setcurdbr(newdbr_at(app,gui,progs[i],0,0,101,101));
	}
	dbrvlen = progs.length;
	TEMPER = progs[0];
    }
    
    public boolean keyDown(Event ev, int n) {
	//System.err.println("key was " + n);
	// on key 27, stop
	if (n == 27) app.gui.terminate();  // ooh.. ugly

        if (n>='a'&&n<='z') {
            int ind = n-'a';
            keyt[ind] = System.currentTimeMillis();
            ekey[ind] = 100;
        } else if (n>='A'&&n<='Z') {
            int ind = n-'A';
            keyt[ind] = System.currentTimeMillis();
            ekey[ind] = 100;
        }
        return true;
    }

    public boolean keyUp(Event ev, int n) {
        if (n>='a'&&n<='z') {
            int ind = n-'a';
            keyt[ind] = -1;
            ekey[ind] = 0;
        } else if (n>='A'&&n<='Z') {
            int ind = n-'A';
            keyt[ind] = -1;
            ekey[ind] = 0;
        }
        return true;
    }

    // there is a bug in how key is trapped by win java (expect
    // worse in mac java). does not match keydowns with keyups
    // workaround: store when key comes down, allow valid for
    // certain amount of time, if exceed then flush. autokey
    // events come in as keydown so should refresh naturally

    public void servicekeys(long curt) {
        int i; 
        for(i=0;i<26;i++) {
            if (ekey[i]==100) {
                if (curt-keyt[i]>1000) {
                    keyt[i] = -1;
                    ekey[i] = 0;
                }
            }
        }
    }
    
    public void initiate() {
        for(int i=0;i<26;i++) {
            ekey[i] = 0;
            keyt[i] = -1;
        }
        for(int i=0;i<3;i++)
	    emouse[i] = 0;
	dbr.start();
    }
	
    public void terminate() {
	dbr.stop();
    }
	
    public void setProgram(String s) {
	dbr.setProgram(s);
    }
	
    public boolean mouseDown(Event ev, int x, int y) {
	int i; 
	if (dbrvlen == 1) {
	} else {
	    DbnRunner df = null;
	    for(i=0;i<dbrvlen;i++) {
		DbnRunner db = (DbnRunner)dbrv.elementAt(i);
		if (db.insidep(x,y)) {
		    df = db; break;
		}
	    }
	    if (df!=null) {
		if (df!=dbr) {
		    // hit something new
		    // terminate old
		    terminate();
		    setcurdbr(df);
		    initiate();
		} else {
		    // if it is not running, initiate it again
		    if (!dbr.runningp()) {
			initiate();
		    }
		}
	    } else {
		// didn't hit anything, terminate 
		terminate();
	    }
	}
	
	updatemouse(x,y);
	emouse[2] = 100;
	return true;
    }
	
    public boolean mouseUp(Event ev, int x, int y)
    {
	updatemouse(x,y);
	emouse[2] = 0;
	return true;
    }
	
    public boolean mouseEnter(Event ev, int x, int y)
    {

	updatemouse(x,y);
	/* if (app.gui.run_mode!=null) 
	    if (app.gui.run_mode.equals("mouse_inside")) {
		initiate();
		}*/
	return true;
    }
	
    public boolean mouseExit(Event ev, int x, int y)
    {
	updatemouse(x,y);
	// is this the auto-stopper?
	//terminate();
	/*if (app.gui.run_mode!=null) 
	    if (app.gui.run_mode.equals("mouse_inside")) {
		terminate();
		}*/
	return true;
    }

    public void updatemouse(int x, int y)
    {
	x-=dbr.dispx;
	y-=dbr.dispy;
	y=dbr.disph-y;
	emouse[0] = x; emouse[1] = y;
    }

    public boolean mouseMove(Event ev, int x, int y)
    {
	updatemouse(x,y);
	return true;
    }
 
    public boolean mouseDrag(Event ev, int x, int y)
    {
	updatemouse(x,y);
	return true;
    }

    public void griddify(int cbw, int cbh)
    {
	// make compliant
	int i,x=0,y=0;
	int j;
	int nrows, ncols;
	int dbw = dbr.dispw;
	int dbh = dbr.disph;
	int marg = 20;
	int xmarg, ymarg;
	int tw;

	ncols = (cbw-marg)/(dbw+marg);
	xmarg = (cbw-ncols*(dbw+marg)+marg)/2;
	ymarg = xmarg;
	
	if (dbrv.size()==1) {
	    xmarg = (cbw-dbw)/2;
	    ymarg = (cbh-dbh)/2;
	}
	//	System.err.println("cols is: "+ncols +" in "+cbw+"/"+cbh);
	for(i=0;i<dbrv.size();i++) {
	    DbnRunner db = (DbnRunner)dbrv.elementAt(i);
	    db.setDisplayXY(x+xmarg,y+ymarg);
	    if ((((i+1)%ncols)==0)) {
		y+=(db.disph+marg);
		x = 0;
	    } else
		x+=(db.dispw+marg);
	}
    }

    public void paint(Graphics g)
    {
	Rectangle r = bounds();
	int i, j;

	terminate();

	if (fgcol == null) {
	    String colorStr = null;
	    if ((colorStr = app.getParameter("bg_color")) != null) {
		if (colorStr.indexOf("#") == 0) {
		    colorStr = colorStr.substring(1);
		    try {
			fgcol = new Color(Integer.parseInt(colorStr, 16));
		    } catch (Exception e) { }
		}
	    }
	    if (fgcol == null) {
		// make it ugly so that people know something didn't work
		//fgcol = Color.gray;
		// changed my mind, want to have less applet params
		fgcol = new Color(0, 51, 102);
	    }
	    if ((colorStr = app.getParameter("bg_color_stipple")) != null) {
		// power user action
		if (colorStr.indexOf("#") == 0) {
		    colorStr = colorStr.substring(1);
		    try {
			bgcol = new Color(Integer.parseInt(colorStr, 16));
		    } catch (Exception e) { }
		}
	    }
	    if (bgcol == null) {
		bgcol = fgcol; 
	    }
	}

	// notify dbr of display dimensions
	griddify(r.width,r.height);

	// build stipple
        if (pat == null) {
            pat = createImage(16,16);
            Graphics pg = pat.getGraphics();
            for(i=0;i<16;i++)
                for(j=0;j<16;j++) {
                    pg.setColor(((i+j)%2==0)?fgcol:bgcol);
                    pg.drawLine(i,j,i,j);
                }

        }
	// render it
        for(i=0;i<r.width/16+1;i++)
            for(j=0;j<r.height/16+1;j++)
		g.drawImage(pat,i*16,j*16,this);

	// blast to screen
	for(i=0;i<dbrv.size();i++) {
	    DbnRunner db = (DbnRunner)dbrv.elementAt(i);
	    db.render(g);
	    // surround with frame
	    g.setColor(Color.black);
	    g.drawRect(db.dispx-1,db.dispy-1,db.dispw+1,db.disph+1);
	}
		
	// put ticks around
	int yoff = 2;
	g.setColor(app.gui.textcol);

	// don't draw ticks if special display mode
	if (app.gui.run_mode == null) { 
	    g.setFont(f);
	    g.setColor(Color.white);
	    for(i=0;i<=100;i+=20) {
		int x,y;
		y = dbr.dispy+dbr.disph-dbr.disph*i/100-1;
		g.drawLine(dbr.dispx-2,y,dbr.dispx-4,y);
 	        g.drawString(String.valueOf(i), 
			     dbr.dispx-4-g.getFontMetrics().
			     stringWidth(String.valueOf(i)), y);
		x = dbr.dispx + dbr.dispw*i/100; 
		y = dbr.dispy + dbr.disph+1;
        	g.drawLine(x,y,x,y+2);
        	g.drawString(String.valueOf(i),
			     dbr.dispx+dbr.dispw*i/100+1,
			     dbr.dispy+dbr.disph+12);
	    }
		
	    g.setColor(bgcol);
	    g.drawRect(0,0,r.width-1,r.height-1);
	    yoff += 16;
	} else yoff+=16;
	
	app.gui.titlestr = TEMPER;
	g.setFont(new Font("monospaced", Font.PLAIN, 24));

	if (app.gui.titlestr!=null) {
	    /*
	    g.setColor(Color.orange); 
	    g.setFont(new Font("monospaced", Font.PLAIN, 24));
	    g.drawString(TEMPER, 10, 50);
	    System.out.println((int)TEMPER.charAt(2)); 
	    System.out.println((int)TEMPER.charAt(3)); 
	    //g.drawString("woah", 10, 30);
	    */
	    //int lead =12,lc=0;
	    int lead = 25, lc = 0;
	    DbnRunner db = (DbnRunner)dbrv.elementAt(dbrv.size()-1);
	    //int x=r.width/2, y=db.dispy+db.disph+lead+yoff;
	    int x = 30, y = 20;
	    //StringTokenizer st = new StringTokenizer(app.gui.titlestr,";");
	    StringTokenizer st = new StringTokenizer(app.gui.titlestr,"\r\n");

	    g.setColor(app.gui.textcol);
	    while(st.hasMoreTokens()) {
		String s = st.nextToken();
		if (lc==0) {
		    x-=g.getFontMetrics().stringWidth(s)/2;
		}
		//g.setFont(lc==0?fb:f);
	g.setFont(new Font("cyberbit", Font.PLAIN, 24));
		g.drawString(s,x,y);
		y+=lead;lc++;
	    }
	}
		
	// okay class, if i am not in special editdisplay 
	// mode then tell dbngui that 
	// it's okay to go ahead and do its thing
	app.gui.runpanelrefreshed();
    }
    
    /*
    public Dimension preferredSize() {
	return new Dimension(101, 101);
    }
    */
}



class DbnControlPanelNull extends Panel 
{
    DbnApplet app;
    DbnGui gui;

    public DbnControlPanelNull(DbnApplet app, DbnGui gui)
    {
	this.app = app;	
	this.gui = gui;
	setBackground(gui.getPanelBgColor());
    }

    public void msg(String s) { }
    public void initiated() { }
    public void terminated() { }
    public void idle() { }
}



class DbnControlPanel extends DbnControlPanelNull {
    Label msgta;
    DbnRunButton dbrb;
	
    public DbnControlPanel(DbnApplet app, DbnGui gui)
    {
	super(app, gui);
	setLayout(new BorderLayout());
	add("West", dbrb = new DbnRunButton(gui));
	add("Center", msgta = new Label("Hello."));
    }
	
    public void msg(String s)
    {
	//System.err.println("setting message " + s);
	msgta.setText(s);
    }
	
    public void initiated()
    {
	msgta.setText("Running ...");
	dbrb.initiated();
    }
	
    public void terminated()
    {
	dbrb.terminated();
    }
	
    public void idle()
    {
	dbrb.idle();
    }
}



public class DbnGui extends Panel {
    static final String BEAUTIFY_ITEM = "Beautify";
    static final String SNAPSHOT_ITEM = "Save";
    static final String SAVE_ITEM = "Save as...";
    static final String OPEN_ITEM = "Open...";

    DbnApplet app;
    TextArea ta;
    DbnRunPanel dbrp;
    DbnControlPanelNull dbcp;
    Button doitbut;
    Choice cmds;
    boolean iexplorerp = false; // assume running navigator? ...
    String run_mode; // default
    String titlestr; // documentation string
    Color textcol; // color to draw it in (incl grid)
    Color panelBgColor;

    DbnIO io;  // object to take care of input/output

    // uglyish hack for scheme, the fix is even uglier, though
    static DbnGui currentDbnGui;
    static public DbnGui getCurrentDbnGui() {
	return currentDbnGui;
    }

    public DbnGui(DbnApplet app, String progs[]) {
	this.app = app;
	setLayout(new BorderLayout());
		
	// if ie is just even mentioned assume it is IE
	iexplorerp = (app.getParameter("ie")!=null); 
	// this flag necessary because of textarea behavior 
	// during syntax error signal for mac on IE
	run_mode = (app.getParameter("run_mode"));
	titlestr = (app.getParameter("title"));

	panelBgColor = new Color(204, 204, 204);
		
	if (textcol == null) {
	    String s;
	    if ((s = app.getParameter("text_color"))!=null) {
		if (s.indexOf("#") == 0) {
		    try {
			int v = Integer.parseInt(s.substring(1), 16);
			textcol = new Color(v);
		    } catch (Exception e) {
		    }
		}
	    }
	    if (textcol == null) {
		textcol = Color.white;
	    }
	}
	if (run_mode == null) {
	    buildeditgui(progs);
	} else if (run_mode.equals("immediate") || 
		   run_mode.equals("mouse_inside")) {
	    
	    add("Center", dbrp = new DbnRunPanel(app,this,progs));
	    ta = new TextArea("",80,24); // don't display
	    dbcp = new DbnControlPanelNull(app,this);

	} else {
	    run_mode = null;
	    buildeditgui(progs);
	}
	currentDbnGui = this;
	io = new DbnIO(app);
    }


    public Color getPanelBgColor() {
	return panelBgColor;
    }

    // show a string in the browser status bar
    // like "Loading cheesedanish.dbn..."
    String currentStatus;
    public void showStatus(String status) {
	if (currentStatus == null) {
	    app.showStatus(status);
	    currentStatus = status;
	}
    }
    
    // needs to say what to clear, so things don't
    // recursively wind up clearing each other
    public void clearStatus(String status) {
	if ((currentStatus != null) &&
	    (status.equals(currentStatus))) {
	    app.showStatus("");
	    currentStatus = null;
	}
    }

    public void buildeditgui(String []progs)
    {
	/*
	 * +-----------------------+
	 * |                       |
	 * +-----------+-----------+
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * +-----------+-----------+
	 * |           |           |
	 * +-----------+-----------+
	 */

	add("North", new DbnLicensePlate(this));

	Panel p1 = new Panel();
	p1.setLayout(new GridLayout(1,2));
	p1.add(dbrp = new DbnRunPanel(app, this, progs));
	p1.add(ta = new TextArea(progs[0], 20, 40));

	//DbnParenBalancer pb = new DbnParenBalancer(this);
	DbnEditorListener listener = new DbnEditorListener(this);
	ta.addKeyListener(listener);
	ta.addFocusListener(listener);

	ta.addKeyListener(new DbnKeyListener(this));

	// has to be capitalized. argh. (nope, that's not it either)
	//ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
	// that was causing problems, we'll go with the jdk 1.0 style
	// jdk 1.0 style is to call it 'courier'
	ta.setFont(new Font("monospaced", Font.PLAIN, 12));
	//ta.setFont(new Font("dialog", Font.PLAIN, 24));
	add("Center", p1);
	p1 = new Panel();
	p1.setLayout(new GridLayout(1,2));
	add("South",p1);
	Panel p2 = new Panel();
	p2.setBackground(getPanelBgColor());
	p2.add(new Label("Command:"));
	p2.add(cmds = new Choice());
	p2.add(doitbut = new Button(" Do it "));

	// can always beautify code
	cmds.addItem(BEAUTIFY_ITEM);
	
	// don't add snapshot command if running locally
	if (!app.isLocal() && (app.getParameter("save_as") != null)) {
	    cmds.addItem(SNAPSHOT_ITEM);
	}
	if (app.isLocal()) {
	    // only show these when run as a file
	    cmds.addItem(OPEN_ITEM);
	    cmds.addItem(SAVE_ITEM);
	}
				
	p1.add(dbcp = new DbnControlPanel(app,this)); 
	p1.add(p2);
    }
	
    public void runpanelrefreshed()
    {
	if (run_mode!=null)
	    if (run_mode.equals("immediate")) {
		if (!getrunningp()) // kick it to start running
		    {
			//			dbrp.initiate();
		    }
	    }
    }
	
    public boolean action(Event evt, Object arg) {
    	if (evt.target == cmds) {
	    // could also do it here, i s'pose

    	} else if (evt.target == doitbut) {
	    String selected = cmds.getSelectedItem();
	    if (selected.equals(BEAUTIFY_ITEM)) doBeautify();
	    else if (selected.equals(SNAPSHOT_ITEM)) doSnapshot();
	    else if (selected.equals(SAVE_ITEM)) doSave();
	    else if (selected.equals(OPEN_ITEM)) doOpen();
    	}
        return true;
    }

    public void doSnapshot() {
	msg("Taking snapshot...");
	if (!io.doSnapshot(ta.getText(), dbrp.dbr.dbg.getPixels())) {
	    msg("Could not make snapshot.");
	} else {
	    msg("Done taking snapshot.");
	}
    }

    public void doSave() {
	msg("Saving file...");
	if (io.doLocalWrite(ta.getText())) {
	    msg("Done saving file.");
	} else {
	    msg("Did not write file.");
	}	    
    }

    public void doOpen() {
	String program = io.doLocalRead();
	if (program != null) {
	    ta.setText(program);
	}
    }

    public void idle(long t) {
	dbrp.idle(t);
    }

    public void doBeautify() {
	char program[] = ta.getText().toCharArray();
	StringBuffer buffer = new StringBuffer();
	boolean gotBlankLine = false;
	int index = 0;
	int level = 0;
	
	while (index != program.length) {
	    int begin = index;
	    while ((program[index] != '\n') &&
		   (program[index] != '\r')) {
		index++;
		if (program.length == index)
		    break;
	    }
	    int end = index;
	    if (index != program.length) {
		if ((index+1 != program.length) &&
		    // treat \r\n from windows as one line
		    (program[index] == '\r') && 
		    (program[index+1] == '\n')) {
		    index += 2;
		} else {
		    index++;
		}		
	    } // otherwise don't increment

	    String line = new String(program, begin, end-begin);
	    line = line.trim();
	    
	    if (line.length() == 0) {
		if (!gotBlankLine) {
		    // let first blank line through
		    buffer.append('\n');
		    gotBlankLine = true;
		}
	    } else {
		if (line.charAt(0) == '}') {
		    level--;
		}
		for (int i = 0; i < level*3; i++) {
		    buffer.append(' ');
		}
		buffer.append(line);
		buffer.append('\n');
		if (line.charAt(0) == '{') {
		    level++;
		}
		gotBlankLine = false;
	    }
	}
	ta.setText(buffer.toString());
    }


    public void reporterror(DbnException e) {
	if (e.line >= 0) {
            String s = ta.getText();
            int len = s.length();
            int lnum = e.line;
            int st = -1, end = -1;
            int lc = 0;
            if (lnum == 0) st = 0;
            for (int i = 0; i < len; i++) {
                //if ((s.charAt(i) == '\n') || (s.charAt(i) == '\r')) {
		boolean newline = false;
		if (s.charAt(i) == '\r') {
		    if ((i != len-1) && (s.charAt(i+1) == '\n')) i++;
		    lc++;
		    newline = true;
		} else if (s.charAt(i) == '\n') {
                    lc++;
		    newline = true;
		}
		if (newline) {
		    if (lc == lnum)
			st = i+1;
		    else if (lc == lnum+1) {
			end = i;
			break;
		    }
		}
	    }
            if (end == -1) end = len;
	    // System.out.println("st/end: "+st+"/"+end);
            ta.select(st, end);
            if (iexplorerp) {
		ta.invalidate();
		ta.repaint();
            }
	}
	dbcp.repaint(); // button should go back to 'play'
	//System.err.println(e.getMessage());
	msg("Problem: " + e.getMessage());
	//showStatus(e.getMessage());
    }

    public boolean getrunningp()
    {
	return dbrp.dbr.runningp();
    }
  
    public void setProgram(String s)
    {
	if (getrunningp()) terminate();
	ta.setText(s);
    }
    public void initiate()
    {
	// tell to start
	dbrp.setProgram(ta.getText());
	dbrp.initiate();
	dbrp.requestFocus();
	dbcp.initiated();
    }
	
    public void success()
    {
	dbcp.terminated();
	dbcp.msg("Done.");
    }
	
    public void msg(String s)
    {
	dbcp.msg(s);
    }
	
    public void terminate()
    {
	dbrp.terminate();
	//dbcp.msg("Stopped.");
	dbcp.msg("");
    }
	
    public void terminated()
    {
	// tell when done
	dbcp.terminated();
    }
	
    public void heartbeat()
    {
	// important to verify run
	dbcp.idle();
    }	
}


class DbnEditorListener extends KeyAdapter implements FocusListener {
    DbnGui gui;
    boolean balancing = false;
    TextArea tc;
    int selectionStart, selectionEnd;
    int position;

    public DbnEditorListener(DbnGui gui) {
	this.gui = gui;
    }

    public void keyPressed(KeyEvent event) {
	// only works with TextArea, because it needs 'insert'
	//TextComponent tc = (TextComponent) event.getSource();
	tc = (TextArea) event.getSource();
	deselect();
	char c = event.getKeyChar();
	//System.err.println((int)c);
	switch ((int) c) {
	case ')':
	    position = tc.getCaretPosition() + 1;
	    char contents[] = tc.getText().toCharArray();
	    int counter = 1; // char not in the textfield yet
	    //int index = contents.length-1;
	    int index = tc.getCaretPosition() - 1;
	    boolean error = false;
	    if (index == -1) {  // special case for first char
		counter = 0;
		error = true;
	    }
	    while (counter != 0) {
		if (contents[index] == ')') counter++;
		if (contents[index] == '(') counter--;
		index--;
		if ((index == -1) && (counter != 0)) {
		    error = true;
		    break;
		}
	    }
	    if (error) {
		//System.err.println("mismatched paren");
		Toolkit.getDefaultToolkit().beep();
		tc.select(0, 0);
		tc.setCaretPosition(position);
	    }
	    tc.insert(")", position-1);
	    event.consume();
	    if (!error) {
		selectionStart = index+1;
		selectionEnd = index+2;
		tc.select(selectionStart, selectionEnd);
		balancing = true;
	    }
	    break;

	case  1: tc.selectAll(); break;  // control a for select all
	}
    }

    protected void deselect() {
	if (!balancing || (tc == null)) return;	
	// bounce back, otherwise will write over stuff
	if ((selectionStart == tc.getSelectionStart()) &&
	    (selectionEnd == tc.getSelectionEnd()))
	    tc.setCaretPosition(position);
	balancing = false;
    }

    public void focusGained(FocusEvent event) { }

    public void focusLost(FocusEvent event) {
	deselect();
    }
}


class DbnKeyListener extends KeyAdapter {
    DbnGui gui;

    public DbnKeyListener(DbnGui gui) {
	this.gui = gui;
    }

    public void keyPressed(KeyEvent event) {
	switch ((int) event.getKeyChar()) {
	case  2: gui.doBeautify(); break;  // control b for beautify
	case 15: gui.doOpen(); break;  // control o for open
	case 18: gui.initiate(); break;  // control r for run
	case 19: gui.doSave(); break;  // control s for save
	case 20: gui.doSnapshot(); break;  // control t for snapshot
	    // escape only works from the runpanel, because that's
	    // who's getting all the key events while running
	    //case 27: gui.terminate(); break;  // escape to stop	
	}
    }
}

