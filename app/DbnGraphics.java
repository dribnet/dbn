import java.awt.*;
import java.util.*;


public class DbnGraphics
{
    public Image im;
    Graphics img;
    Color cols[] = new Color[101];
    long lastpapert = 0; // for use in autoslowdown
    
    byte[] pixels;
    byte penColor = 100;
    int pixelCount;
    boolean antialias = false;
    
    DbnRunner dbr;
    
    int cbw, cbh;
    int cbw1, cbh1;
    
    public int width, height;
    Panel dad;
    
    Hashtable connectorTable;
    
    int FDOT = 10;
    int FPAPER = 100;
    // need to tweak these for different purposes --> 
    // if interactive, want this to be 0
    int FLINE = 10; 
    int FFIELD = 10;
    int FMAX = 100;
    
    /********* block detection for 'ai' double buffering ***********/
    boolean insideforeverp = false;
    boolean aiflushp = false;
    short flushfire = -1; // 0 is paper, 1 is field
    short lastflushfire = -1;
    boolean insiderepeatp = false;
    boolean autoflushenablep = true;
    int repeatlevel = 0;
    
    int flushc = 0;


    public void flushNormal() {
	FDOT = 10;
	FPAPER = 100;
	FLINE = 10; 
	FFIELD = 50;
	FMAX = 100;
    }    


    public void flushReduced(int lev) {
	FDOT=10/lev; 
	FLINE=10/lev; 
	FFIELD=50/lev; 
	FPAPER=100;
    }
    

    public void resetBlockDetect() { 
	insideforeverp = false; 
	lastflushfire = flushfire = -1;
	insiderepeatp = false;
	autoflushenablep = true;
	repeatlevel = 0;
	flushNormal();
    }
    

    public void beginForever() {
	// fired on a '{'
	//System.out.println("begin forever");
	insideforeverp = true;
	flushfire = -1;
	autoflushenablep = false;
    }
    

    public void endForever() {
	// fired on a '}'
	//System.out.println("end->"+flushfire);
	if (flushfire == -1) {
	    // did not flush during a forever
	    // force it
	    forceFlush();
	    autoflushenablep= true;
	}
	flushfire = -1;
    }
    

    public void beginRepeat() {
	// if within a forever, should be disabled anyways
	//System.out.println("--****begrepeat");
	insiderepeatp = true;
	repeatlevel++;
	//System.out.println("repeatlevel: "+repeatlevel+"/"+autoflushenablep);
	if (insideforeverp) {
	    // not much to do except for default
	    if (repeatlevel == 1) { // special case
		flushNormal();
	    } else if (repeatlevel>1) {
		flushReduced(repeatlevel);
	    }
	} else  {
	    // disable flush, count up how much flush activity gets
	    if (repeatlevel == 1) { // special case
		autoflushenablep = true;
		flushNormal();
	    } else if (repeatlevel>1) {
		autoflushenablep = false;
		flushReduced(repeatlevel);
	    }
	}
	
    }
    

    public void endRepeat() {
	//System.out.println("--****endrepeat");
	if (repeatlevel==1)
	    insiderepeatp = false;
	repeatlevel--;
	flushNormal();
	if (!insideforeverp) {
	    if (flushc>0 && flushfire==-1) {
		forceFlush();
	    }
	}
    }
    

    public void paperFlush() {	
	//	  System.out.println("paperflush request");
	if (insideforeverp&&aiflushp) {
	    if (flushfire==-1) {
		flushfire = 0; // paper was first
		forceFlush();
	    } else {
		if (lastflushfire == 0) {
		    // could be consecutive paper -> legalize
		    forceFlush();
		} else {
   		    // ignore flush
		}
	    }
	} else {
	    myFlush(FPAPER);
	}
	lastflushfire = flushfire;
    }

    
    public void fieldFlush() {
	if (insideforeverp&&aiflushp) {
	    if (flushfire==-1) {
		flushfire = 1; // field was first
		forceFlush();
	    } else {
		// ignore flush
	    }
	} else {
	    myFlush(FFIELD);
	}
	lastflushfire = flushfire;
    }	
    
    /********** end ai double buffer *****/

    
    public DbnGraphics(Panel parent, DbnRunner dbr, int w, int h) {
	this.dbr = dbr;

	// when dbngraphics comes up, query applet for parameters
	// this is to maintain illusion of a perfect 
	// automatic double buffer, of course elusive ...
	String s;			
	if ((s = dbr.app.getParameter("display_mode"))!=null) {
	    if (s.equals("plain")) {

	    } else if (s.equals("flush")) { // mainly flushes when ets paper
		FLINE = 0;
		FDOT = 0;

	    } else if (s.equals("flush_more")) {
		FLINE = 10;
		FDOT = 10;
		aiflushp = true; // 'ai is on'
		//System.out.println("auto flush is on");

	    } else if (s.equals("auto")) {
		aiflushp = true; // 'ai is on'
		//System.out.println("auto flush is on");
	    }
	}
	
	cbw = w; cbh = h;
	width = w; height = h;
	dad = parent;
	// assuming that cbw is 101, cbh is 101, needed minused versions
	cbw1 = cbw-1; cbh1=cbh-1;
	
	pixelCount = width * height;
	pixels = new byte[ pixelCount ];
	
	byte value = 0;
	for(int i = 0; i < pixelCount; ++ i ) {
	    pixels[i] = value;
	}
	penColor = 100;
	
	for (int i = 0; i < 101; i++) {
	    cols[i] = new Color(i*255/100,i*255/100,i*255/100);
	}
	
        // build the 'i' of io into it all
        connectorTable = new Hashtable();
        
        // mouse, key, time are arrays of ints
        connectorTable.put("mouse", new int[3]);
        connectorTable.put("key", new int[26]);
        connectorTable.put("time", new int[4]);
        connectorTable.put("net", new Object()); // int values not useful here
    }


    public Hashtable getConnectorTable() {
	return connectorTable; 
    }
	
    public void forceFlush()
    {
	flushc=flushc%100; //im.flush(); 
	flushc = 0;
	dbr.render(); 
    }
    
    public void myFlush(int v) {
	flushc+=v;
	if (autoflushenablep) {	
	    if (flushc>99) { 
		forceFlush();
	    }
	}
    }
    
    public void buildBuffers() {
	if (im == null) { 
	    //some sync problem forces this sequence to be important
	    im = dad.createImage(width, height);
	    img = im.getGraphics(); 
	    reset();
	}
    }


    public void reset() {
	//System.out.println("reset");
	penColor=100;
	for(int i=0;i<pixelCount;i++) pixels[i] = 0;
	flushc = 0;
	img.setColor(cols[penColor]);
	img.fillRect(0,0,cbw,cbh);
	resetBlockDetect();
	//		im.flush();
    }
    

    private int bound(int input, int upper) {
	if (input > upper) return upper;
	else if (input < 0) return 0;
	else return input;
    }


    public void paper(int val) {
	byte bVal;
	
	paperFlush();
	// voluntary slowdown
	try {
	    long curt = System.currentTimeMillis();
	    long sleept = curt-lastpapert;
	    // this enforces a 10 millisecond interval to occur 
	    // between papers (to compensate for fast/slwo machines)
	    if (sleept<10)
		sleept = 10-sleept;
	    else
		sleept=0;
	    Thread.sleep(sleept);
	    lastpapert = curt;
	} catch (Exception e) { }

	if (val > 100) 
	    val = 100;
	else if(val < 0) 
	    val = 0;
	bVal = (byte) val;
	for(int i=0;i<pixelCount;i++) pixels[i] = bVal;
	img.setColor(cols[100-bVal]);
	img.fillRect(0,0,cbw,cbh);
    }


    public void field(int x1, int y1, int x2, int y2, int val) {
	x1 = bound(x1, cbw1);
	y1 = bound(y1, cbh1);
	x2 = bound(x2, cbw1);
	y2 = bound(y2, cbh1);
	val = bound(val, 100);
	fieldFlush();
	
	if (x2 < x1) { int dum = x1; x1 = x2; x2 = dum; }
	if (y2 < y1) { int dum = y1; y1 = y2; y2 = dum; }
	
	// System.out.println(x1+"/"+x2+"/"+y1+"/"+y2);
	int i=0,j=0,pp=0;
	byte bVal = (byte) val;
	for(j=y1; j<=y2; j++) {
	    pp = cbw*j;// + x1;
	    for(i=x2; i<=x2; i++) {
		//System.out.println(pp+i);
		pixels[pp+i] = bVal;
	    }
	}		
	
	img.setColor(cols[100-bVal]);
	img.fillRect(x1,cbh-y1-(y2-y1)-1,(x2-x1)+1,(y2-y1)+1);
    }
    
    public void pen(int val) {
        if (val > 100) 
	    val = 100;
	else if(val < 0) 
	    val = 0;
	penColor = (byte)val;
    }

    public byte getPen() {
	return penColor;
    }

    public void setPixel(int x, int y, int val) {
	int checkX, checkY, checkVal;
	
        myFlush(FDOT);
        // NOTE::*******
	// ideally for optimize step here, should draw 
	// directly to screen without flushing
	// as drawing dots is so laborious...
	
	if (x<0 || x>cbw1 || y<0 || y>cbh1) return;
	if (val < 0) 
	    checkVal = 0;
	else if (val > 100) 
	    checkVal = 100;
	else 
	    checkVal = val;
	pixels[(cbh1-y)*cbw + x] = (byte)checkVal;
	img.setColor(cols[100-checkVal]);
	img.drawLine(x,cbh1-y,x,cbh1-y);
    }

    public int getPixel(int x, int y) {
	return (int) pixels[(cbh1-((y<0)?0:((y>cbh1)?cbh1:y)))*cbw + 
			   ((x<0)?0:((x>cbw1)?cbw1:x))];
    }

    public void setAntiAlias(int m) {
	antialias = (m > 50);
    }
    
    void intensifyPixel(int x, int y, float dist) {
	int oldVal, newVal, val, index;
	
	//System.err.println("setting " + x + ", " + y);
	if (x<0 || x>cbw1 || y<0 || y>cbh1) return;
	if(dist < 0) dist = 0.0f - dist;
	if(dist > 1.0f) return;
	index = (cbh1-y)*cbw + x;
		
	val = (int)(pixels[index]*dist + penColor*(1.0f-dist));

	if(val>100) val = 100;
	else if(val < 0) val = 0;
	
	pixels[index] = (byte)val;
	
	if (antialias) {
	    img.setColor(cols[100-val]);
	    img.drawLine(x,cbh1-y,x,cbh1-y);
	}
    }

    // line clipping code appropriated from 
    // "Computer Graphics for Java Programmers"

    private int clipCode(float x, float y) {
	return ((x < 0 ? 8 : 0) | (x > cbw1 ? 4 : 0) |
		(y < 0 ? 2 : 0) | (y > cbh1 ? 1 : 0));
    }
    
    public void line(int ox1, int oy1, int ox2, int oy2) {
	/* check for line clipping, do it if necessary */
	if ((ox1 < 0) || (oy1 < 0) || (ox2 > cbw1) || (oy2 > cbh1)) {
	    // clipping, converts to floats for dy/dx fun
	    // (otherwise there's just too much casting mess in here)
	    float xP = (float)ox1, yP = (float)oy1;
	    float xQ = (float)ox2, yQ = (float)oy2;
	    
	    int cP = clipCode(ox1, oy1);
	    int cQ = clipCode(ox2, oy2);
	    float dx, dy;
	    while ((cP | cQ) != 0) {  
		if ((cP & cQ) != 0) return;
		dx = xQ - xP; dy = yQ - yP;
		if (cP != 0) {  
		    if ((cP & 8) == 8) { 
			yP += (0-xP) * dy / dx; xP = 0; 
		    } else if ((cP & 4) == 4) { 
			yP += (cbw1-xP) * dy / dx; xP = cbw1; 
		    } else if ((cP & 2) == 2) { 
			xP += (0-yP) * dx / dy; yP = 0;
		    } else if ((cP & 1) == 1) {
			xP += (cbh1-yP) * dx / dy; yP = cbh1;
		    }  
		    cP = clipCode(xP, yP);
		} else if (cQ != 0) {
		    if ((cQ & 8) == 8) { 
			yQ += (0-xQ) * dy / dx; xQ = 0;
		    } else if ((cQ & 4) == 4) {
			yQ += (cbw1-xQ) * dy / dx; xQ = cbw1;
		    } else if ((cQ & 2) == 2) { 
			xQ += (0-yQ) * dx / dy; yQ = 0;
		    } else if ((cQ & 1) == 1) { 
			xQ += (cbh1-yQ) * dx / dy; yQ = cbh1;
		    }  
		    cQ = clipCode(xQ, yQ);
		}
	    }
	    ox1 = (int)xP; oy1 = (int)yP;
	    ox2 = (int)xQ; oy2 = (int)yQ;
	}
	
	/* actually draw the line */
	int dx, dy, incrE, incrNE, d, x, y, twoV, sdx=0, sdy=0;
	float invDenom, twoDX, scratch, slope;
	int x1, x2, y1, y2, incy=0, incx=0, which;
	boolean backwards = false, slopeDown = false;
	
	myFlush(FLINE);
	
	if (!antialias) { 
	    img.setColor(cols[100-penColor]);
	    img.drawLine(ox1,cbh1-oy1,ox2,cbh1-oy2);
	}
	
        /* first do horizontal line */
	if (ox1==ox2) {
	    x = ox1;
	    if(oy1 < oy2) { y1 = oy1; y2 = oy2; }
	    else { y1 = oy2; y2 = oy1; }
	    for(y=y1;y<=y2;y++) {
		intensifyPixel(x, y, 0);
	    }
	    return;
	}
	slope = (float)(oy2 - oy1)/(float)(ox2 - ox1);
	/* check for which 1, 0 <= slope <= 1 */
	if(slope >= 0 && slope <= 1) which = 1;
	else if(slope > 1) which = 2;
	else if(slope < -1) which = 3;
	else which=4;
	if (((which==1 || which==2 || which==4) && 
	     ox1 > ox2) || ((which==3) && ox1 < ox2)) {
	    x1 = ox2;
	    x2 = ox1;
	    y1 = oy2;
	    y2 = oy1;
	} else {
	    x1 = ox1;
	    y1 = oy1;
	    x2 = ox2;
	    y2 = oy2;
	}
	dx = x2 - x1;
	dy = y2 - y1;
	if (which==1) {
	    sdx = dx;
	    d = 2 * dy - dx;
	    incy = 1;
	    incrE = 2*dy;
	    incrNE = 2*(dy-dx);
	} else if (which==2) {
	    sdy = dy;
	    d = 2 * dx - dy;
	    incx = 1;
	    incrE = 2*dx;
	    incrNE = 2*(dx-dy);
	} else if (which==3) {
	    sdy = -dy;
	    d = -2 * dx - dy;
	    incx = -1;
	    incrE = -2*dx;
	    incrNE = -2*(dx+dy);
	} else /*if(which == 4)*/ {
	    sdx = -dx;
	    d = -2 * dy - dx;
	    incy = -1;
	    incrE = -2*dy;
	    incrNE = -2*(dy+dx);
	}
	if(which==1 || which==4) {
	    twoV = 0;
	    invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
	    twoDX = 2 * sdx * invDenom;
	    x = x1;
	    y = y1;
	    if (antialias) {
		intensifyPixel(x, y, 0);
		intensifyPixel(x, y+1, twoDX);
		intensifyPixel(x, y-1, twoDX);
	    } else {
		intensifyPixel(x, y, 0);
	    }
	    while(x < x2) {
		if(d<0) {
		    twoV = d + dx;
		    d += incrE;
		    ++x;
		}
		else {
		    twoV = d - dx;
		    d += incrNE;
		    ++x;
		    y+=incy;
		}
		scratch = twoV * invDenom;
		if(antialias) {
		    intensifyPixel(x, y, scratch);
		    intensifyPixel(x, y+1, twoDX - scratch);
		    intensifyPixel(x, y-1, twoDX + scratch);
		}
		else
		    intensifyPixel(x, y, 0);
	    }
	}
	else {
	    twoV = 0;
	    invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
	    twoDX = 2 * sdy * invDenom;
	    x = x1;
	    y = y1;
	    if(antialias) {
		intensifyPixel(x, y, 0);
		intensifyPixel(x+1, y, twoDX);
		intensifyPixel(x-1, y, twoDX);
	    }
	    else
		intensifyPixel(x, y, 0);
	    while(y < y2) {
		if(d<0) {
		    twoV = d + dy;
		    d += incrE;
		    ++y;
		}
		else {
		    twoV = d - dy;
		    d += incrNE;
		    ++y;
		    x+=incx;
		}
		scratch = twoV * invDenom;
		if(antialias) {
		    intensifyPixel(x, y, scratch);
		    intensifyPixel(x+1, y, twoDX - scratch);
		    intensifyPixel(x-1, y, twoDX + scratch);
		}
		else
		    intensifyPixel(x, y, 0);
	    }
	}
    }


    public void pause(int amount) {
	long stopTime = System.currentTimeMillis() + (amount*10);
	do { } while (System.currentTimeMillis() < stopTime);
    }


    public void refresh() {
	forceFlush();
    }


    public byte[] getPixels() {
	return pixels;
    }
}


    // this will go away soon
    /*
    public String gethexthumbnail(int n) 
    { 
	return gethexthumbnail(n, 4); 
    }
  
    public String gethexthumbnail(int n, int bitdepth)
    {
	// input is byte[] pixels, int cbw, int cbh
	// output is n by n (resampled to 4-bit) ascii HEX String
	// this only produces 4 or 8 bit representations
	String s = "";
	final String[] HEX_CODES = { "0", "1", "2", "3", 
				     "4", "5", "6", "7", 
				     "8", "9", "A", "B",
				     "C", "D", "E", "F"};
	
	if ((cbw != 0) && (cbh != 0)){
	    int p = 0;
	    int srcindex = 0;
	    int nsrcpixels = cbw*cbh;
	    int srcx, srcy, newx, newy;
	    float xratio = (float)cbw/(float)n;
	    float yratio = (float)cbh/(float)n;
	    float valueScale = (float) (2.55/16.0);
	    
	    for (newy=0; newy<n; newy++){
		for (newx=0; newx<n; newx++){
		    // for each new pixel, get the location of its source pixel
		    srcx = (int) Math.floor(newx*xratio);
		    srcy = (int) Math.floor(newy*yratio);
		    // get the value at that location from the source image
		    srcindex = cbh*srcy + srcx;
		    if (srcindex < nsrcpixels){
			p = (int) (2.55f*(float)pixels[srcindex]); // 0-100, scaled to 0-255
			// get the high-order bits,
			// fetch their equivalent hexcode string 
			// and tack it onto our output
			switch (bitdepth){
			case 8: s += HEX_CODES[p/16]; // high bits
			    s += HEX_CODES[p%16]; // low bits
			    break;
			default:
			case 4: s += HEX_CODES[p/16]; break;
			}
			
		    }
		}
	    }
	}
	return s;
    }
}
    */
