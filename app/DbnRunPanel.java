import java.awt.*;
import java.util.*;


public class DbnRunPanel extends Panel {
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

    //static String TEMPER;

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

	DbnGraphics.setCurrentGraphics(dbr.dbg);
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
	//TEMPER = progs[0];
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
	
	//app.gui.titlestr = TEMPER;
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
	    int lead =12,lc=0;
	    //int lead = 25, lc = 0;
	    DbnRunner db = (DbnRunner)dbrv.elementAt(dbrv.size()-1);
	    int x=r.width/2, y=db.dispy+db.disph+lead+yoff;
	    //int x = 30, y = 20;
	    StringTokenizer st = new StringTokenizer(app.gui.titlestr,";");
	    //StringTokenizer st = new StringTokenizer(app.gui.titlestr,"\r\n");

	    g.setColor(app.gui.textcol);
	    while(st.hasMoreTokens()) {
		String s = st.nextToken();
		if (lc==0) {
		    x-=g.getFontMetrics().stringWidth(s)/2;
		}
		g.setFont(lc==0?fb:f);
		//g.setFont(new Font("cyberbit", Font.PLAIN, 24));
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
