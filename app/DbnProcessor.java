import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class DbnProcessor {
    DbnApplet app;
    DbnGui gui;
    DbnGraphics graphics;
    Hashtable connectorTable; 
    DbnPreprocessor preprocessor;
    DbnEngine engine;

    long heartbeatTime = 0;
    

    // networking support
    static final int PORT = 8000;
    static final int ERROR_MESSAGE = -1;
    static final int OK_MESSAGE = 0;
    static final int GET_MESSAGE = 1;
    static final int SET_MESSAGE = 2;
    
    DataInputStream netInputStream;
    DataOutputStream netOutputStream;
    boolean secondAttempt; 


    public DbnProcessor(DbnGui gui, DbnGraphics graphics, DbnApplet app) {
	this.gui = gui;
	this.graphics = graphics;
	this.app = app;

	connectorTable = graphics.getConnectorTable(); 
	preprocessor = new DbnPreprocessor(gui, app);
    }


    public void idle() {
	long currentTime = System.currentTimeMillis();
	gui.idle(currentTime);

	//long hb;
	//if ((hb = currentTime%1000)>800) {
	if ((currentTime % 1000) > 800) {
	    // beat the heart if a new beat
	    long hba = currentTime / 1000;
	    if (hba != heartbeatTime)
		gui.heartbeat();
	    heartbeatTime = hba;
	}
	//graphics.forceFlush();
    }


    public void start(String inProgram) throws Exception {
	String program = preprocessor.process(inProgram);
	
	connectorTable = graphics.getConnectorTable(); 
	graphics.paper(0);
	graphics.pen(100);

	// do all the processing here
	// all exceptions are handled by the (calling) DbnRunner
	DbnParser parser = new DbnParser(program.toCharArray());
	engine = new DbnEngine(parser.getRoot(), graphics, this);
	engine.start();
    }


    public void stop() {
	if (engine != null) engine.stop();
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


    public boolean isConnector(String s) {
	return connectorTable.containsKey(s);
    }	


    public void connectorSet(String name, int slot, int value) 
	throws DbnException {
	if (name.equals("net")) {
	    networkSet(slot, value); 
	    return;
	}
	int values[] = (int[]) connectorTable.get(name);
	if (values == null) {
	    throw new DbnException("unknown external data " + name);
	}
	try {
	    values[slot-1] = value;
	} catch (Exception e) {
	    throw new DbnException("error setting " + name + " " + slot + 
				   " to " + value);
	}
    }


    public int connectorGet(String name, int slot) throws DbnException {
	if (name.equals("net")) {
	    return networkGet(slot);
	}
	int values[] = (int[]) connectorTable.get(name);
	if (values == null) {
	    throw new DbnException("unknown external data " + name);
	}
	try {
	    return values[slot-1];
	} catch (Exception e) {
	    throw new DbnException("could not get slot " + 
				   slot + " of " + name);
	}
    }
}
