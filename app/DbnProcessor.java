import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


/*
 * your processing object should hold onto this 'dbnprocessor' object.
 * 
 * 1. it gets the dbngraphics context from it
 * 2. it also must call idle (with current time) at a steady rate to
 *    make sure heartbeat keeps beating
 * 3. if it gets an error, throw a DbnException with message/linenum
 * 4. note that <time <mouse <key (and also <net) are special 'getext' and 'setext'
 *    accessed things
 * 5. note that 'field' should be a real command (not connector)
 *
 */  
public class DbnProcessor {
    DbnApplet app;
    DbnGui gui;
    DbnGraphics dbg;
    Hashtable exthash; // begat from dbngraphics
    DbnPreprocessor dbp; // the new dbn preprocessor

    long heartbeatt = 0;

    // networking support
    static final int PORT = 8000;
    static final int ERROR_MESSAGE = -1;
    static final int OK_MESSAGE = 0;
    static final int GET_MESSAGE = 1;
    static final int SET_MESSAGE = 2;
    
    DataInputStream netInputStream;
    DataOutputStream netOutputStream;
    boolean secondAttempt; 


    public DbnProcessor(DbnGui gui, DbnGraphics dbg, DbnApplet app)
    {
	this.gui = gui;
	this.dbg = dbg;
	this.app = app;

	exthash = dbg.getexthash(); 
	dbp = new DbnPreprocessor(gui, app);
    }
	
    public String preprocess(String s)
    {
	return dbp.doit(s);
    }
    
    // YOU HAVE TO CALL THIS ONE FROM WITH YOUR MAIN PROCESSING LOOP
    // SHOULD DO AUTOMATIC SLOWDOWNIN HERE TOO ...
    // removed curt because it bugged tom
    public void idle()
    {
	long curt = System.currentTimeMillis();
	long hb, hba;
		
	gui.idle(curt);
		
	if ((hb = curt%1000)>800) {
	    // beat the heart if a new beat
	    hba = curt/1000;
	    if (hba!=heartbeatt)
		gui.heartbeat();
	    heartbeatt = hba;
	}
    }
    
    public void pleaseQuit() {
	/* you should shutdown and quit */
    }
    
    public void prep()
    {
	// tommy do your magic (have to do this once here, not sure why)
	exthash = dbg.getexthash(); 
	dbg.paper(0);
	dbg.pen(100);
    }
    
    // if you subclass from this class, be sure to call prep and idle
    public void process(String prog) throws Exception 
    {
	int i = 0;
	prep();
		
	// the <foo > is called "external data", that is why i have 'getext'
	// note that 'field' is a valid command for dbngraphics
		
	for(;;) {
	    // erase screen, set pen
	    dbg.paper(0);
	    dbg.pen(100-getext("mouse",2));
	    dbg.line(0,0,getext("time",4), getext("mouse",2));
	    
	    // throw a DbnException for fun
	    if (getext("mouse",3)==100) 
		throw new DbnException("error at line 0",0);
	    
	    // test the key event
	    for (i = 1; i < 27; i++) {
		dbg.line(i*4,0,getext("key",i),100);
	    }
	    
	    // check the heartbeat, update keyboard/mouse/time events
	    idle();
	}
    }
    
    protected void networkOpen() throws DbnException {
	try {
	    String hostname = app.isLocal() ? 
		"localhost" : app.getCodeBase().getHost();

	    Socket socket = new Socket(hostname, PORT);
	    netInputStream = new DataInputStream(socket.getInputStream());
	    netOutputStream = new DataOutputStream(socket.getOutputStream());
	    
	} catch (IOException e) {
	    netInputStream = null;
	    netOutputStream = null;
	    throw new DbnException("could not connect to server");
	}
    }

    protected int networkGet(int number) throws DbnException {
	if (netInputStream == null) {
	    networkOpen();
	}

	int val = -1; // !#$(*@#$ compiler
	try {
	    netOutputStream.writeInt(1);
	    netOutputStream.writeInt(GET_MESSAGE);
	    netOutputStream.writeInt(number);
	    netOutputStream.writeInt(0);
	    netOutputStream.flush();

	    int version = netInputStream.readInt();
	    int message = netInputStream.readInt();
	    int num = netInputStream.readInt();
	    val = netInputStream.readInt();

	    // past where an ioexception would be thrown
	    secondAttempt = false; 
	    if (message != OK_MESSAGE) {
		throw new DbnException("bad data from the server");
	    }

	} catch (IOException e) {
	    if (!secondAttempt) { // re-connect and try again
		secondAttempt = true;
		networkOpen();
		return networkGet(number);
	    } else {
		throw new DbnException("could not connect to server");
	    }
	}
	return val;
    }

    protected void networkSet(int number, int value) throws DbnException {
	if (netInputStream == null) {
	    networkOpen();
	}
	try {
	    netOutputStream.writeInt(1);
	    netOutputStream.writeInt(SET_MESSAGE);
	    netOutputStream.writeInt(number);
	    netOutputStream.writeInt(value);
	    netOutputStream.flush();

	    int version = netInputStream.readInt();
	    int message = netInputStream.readInt();
	    int num = netInputStream.readInt();
	    int val = netInputStream.readInt();

	    // past where an ioexception would be thrown
	    secondAttempt = false; 
	    if (message != OK_MESSAGE) {
		throw new DbnException("could not set data on the server");
	    }

	} catch (IOException e) {
	    if (!secondAttempt) { // re-connect and try again
		secondAttempt = true;
		networkOpen();
		networkSet(number, value);
	    } else {
		throw new DbnException("could not connect to server");
	    }
	}
    }

    public boolean hasext(String s) {
	return exthash.containsKey(s);
    }	

    public void setext(String s, int n, int v) throws DbnException {
	// if this is net, hit the server to update slot n with v
	if (s.equals("net")) {
	    networkSet(n, v);
	    return;
	}
		
	int vv[] = (int[])exthash.get(s);
	if (vv == null) {
	    throw new DbnException("unknown external data "+s);
	}
	try{
	    vv[n-1] = v;
	} catch (Exception e) {
	    throw new DbnException("bounds problem with set "+s+"-->"+n);
	}
    }

    public int getext(String s, int n) throws DbnException {
	if (s.equals("net")) {
	    return networkGet(n);
	}
		
	int vv[] = (int[])exthash.get(s);
	if (vv == null) {
	    throw new DbnException("unknown external data "+s);
	}
	try{
	    return vv[n-1];
	} catch (Exception e) {
	    throw new DbnException("bounds problem with get "+s+"-->"+n);
	}
    }
}
