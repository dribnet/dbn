/* * a simple library for interacting with the Cricket. * hacked up by fry to talk to dbn */import java.io.*;import java.util.*;import javax.comm.*;public class SensorConnector implements SerialPortEventListener {    private SerialPort sp;	    public String portName = portsAvailable()[0];    private OutputStream output;    private InputStream input;    /*    Thread thread;        static final private int BLUECK_DATA_START = 1280;    static final private int BLUECK_SETDP = 131;    static final private int BLUECK_READ_BYTE = 132;    static final private int BLUECK_WRITE_BYTE = 132;    static final private int BLUECK_WRITE_LIST = 133;    static final private int BLUECK_DOIT = 134;    static final private int BLUECK_CHECK = 135;    static final private int BLUECK_CHECK_REPLY = 55;    static final int CHECK_GOOD = 0;    static final int CHECK_NO_INTERFACE = 1;    static final int CHECK_NO_BRICK = 2;        static public int uploadProgress = 0;    static public int uploadDataSetSize = 0;    static public boolean uploading = false;    static final private boolean DEBUG = false;    */    	//init();	//thread = new Thread(this);	//thread.setPriority(Thread.MIN_PRIORITY);	//System.out.println("initializing serial port");    //}    // why is this done multiple times?//private void init() {    public SensorConnector() {	try {	    sp = (SerialPort) (CommPortIdentifier.getPortIdentifier(portName).open("Blue Cricket Communications", 2000));	    sp.setSerialPortParams(9600, sp.DATABITS_8, 				   sp.STOPBITS_1, sp.PARITY_NONE);	    //sp.enableReceiveTimeout(300);	    sp.enableReceiveTimeout(100);	    // added by fry, hoping that this is gonna work..	    sp.addEventListener(this);	    sp.notifyOnDataAvailable(true);	    output = sp.getOutputStream();	    input = sp.getInputStream();	} catch (Exception e) {	    e.printStackTrace();	}    }	    int state[] = new int[2];  // bricks numbered 1 and 2    byte readBuffer[] = new byte[256];    /*    public void run() {	while (Thread.currentThread() == thread) {	    try {		if (input.available() > 0)		    read();		thread.sleep(50);	    } catch (IOException e) {		e.printStackTrace();	    } catch (InterruptedException e) {  }	}    }    */    /*    protected void read() {	try {	    while (input.available() > 0) { 		//System.out.println("got data len " + input.available());		if (input.available() > readBuffer.length) {		    readBuffer = new byte[input.available()];		}		int count = input.read(readBuffer); 		//if ((count % 2) != 0) {		//  System.err.println("got an odd number of values!");		//  return;		//} 		for (int i = 0; i < count; i++) {		    //int which = (int) readBuffer[i*2+0];		    int which = ((readBuffer[i] & 128) == 0) ? 1 : 2;		    int value = (int) readBuffer[i];		    if ((which >= 1) && (which <= state.length)) {			//System.out.println("  " + (which-1) + " = " + value);			synchronized (state) {			    state[which-1] = value;			}		    }		}	    }	} catch (IOException e) {	    e.printStackTrace();	}    }    */    public void serialEvent(SerialPortEvent event) {	//System.out.println("got event");	//System.out.println("avail const is " + SerialPortEvent.DATA_AVAILABLE);	//System.out.println("but this type is " + event.getEventType());	if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {	    try {		//int available = input.available();		//System.out.println("input.avail = " + input.available());		//while (input.available() > 0) { 		    //if (input.available() > readBuffer.length) {		    //readBuffer = new byte[input.available()];		    //}		/*		    int count = input.read(readBuffer);		    for (int i = 0; i < count; i++) { 			int which = ((readBuffer[i] & 128) == 0) ? 1 : 2; 			int value = (int) readBuffer[i]; 			System.out.println("  which, value = " +  					   which + ", " + value); 			if ((which >= 1) && (which <= state.length)) { 			    synchronized (state) { 				state[which-1] = value; 			    } 			} 		    }		*/		//System.out.println("trying");		while (input.available() > 0) {		    //System.out.println("input.avail > 0");		    int val = input.read();		    //System.out.println("got " + val);		    if (val != -1) {			// high bit determines which slot			int which = ((val & 128) == 0) ? 1 : 2; 			int value = val & 127; // low 7 bits for data			//System.out.println("  which, value = " + 				//	   which + ", " + value);			synchronized (state) {			    state[which-1] = value;			}		    }		}		//}	    } catch (IOException e) {		e.printStackTrace();	    }	}    }    public int getValue(int which) throws DbnException {	if ((which >= 1) && (which <= state.length)) {	    synchronized (state) {		return state[which-1];	    }	}	throw new DbnException("Brick must be between 1 and " + state.length);    }    /*        private void close() {	try {	    sp.close();	}	catch (Exception ioe) {	    System.out.println(ioe);	}    }    private int get() {	int b = -1;	try {	    b = input.read();	} catch (Exception ioe) {	    ioe.printStackTrace();	}	return b;    }    private void put(int b) throws IOException {	output.write(b);    }    private boolean setPointer(int addr) {	try {	    tyoHs((byte)BLUECK_SETDP);	    tyoHs(highByte(addr));	    tyoHs(lowByte(addr));	} catch (Exception ioe) {	    System.out.println(ioe);	}				return true;    }    public void doit(int addr) {	init();	setPointer(addr);	tyoHs((byte)BLUECK_DOIT);	close();    }    private int readByte() {	try {	    tyoHs((byte)BLUECK_READ_BYTE);	    tyoHs((byte)0);	    tyoHs((byte)1);	    return get();	} catch (Exception ioe) {	    System.out.println(ioe);	}	return -1;    }      private int[] readShortByteList(int n) throws BrickLostException { 	// reads n>1 bytes from current memory location	byte[] result= new byte[n];	tyoHs((byte)BLUECK_READ_BYTE);	tyoHs((byte) highByte (n));	tyoHs((byte) lowByte (n));	int i=0;	int batchLen=100;			int realBatchLen=0;			while (i<n) {	    if (n-i<100) {		batchLen=n-i;	    }	    try {		realBatchLen=input.read(result, i, batchLen);	    } catch (IOException IOE) {		realBatchLen= -1;	    }							    if (realBatchLen==-1) {		throw new BrickLostException();	    }	    i=i+realBatchLen;	    uploadProgress=uploadProgress+realBatchLen;	    System.out.println("got "+realBatchLen+" i="+i);	    //			try {		//		java.lang.Thread.sleep(30);			//	} catch (java.lang.InterruptedException ie) {			//}	    java.lang.Thread.yield();				}			int[] realResult= new int[n];					for (i=0; i<n; i++) {	    realResult[i]=(int)result[i];	    if (realResult[i]<0) {		realResult[i]=realResult[i]+256;	    }	}	return realResult;    }		    public void writeList(int addr, Object[] bytes) throws BrickLostException {	init();	setPointer(addr);			try {	    int count=bytes.length;				    if (count == 0) return;				    tyoHs((byte)BLUECK_WRITE_LIST);	    tyoHs(highByte(count));	    tyoHs(lowByte(count));					    for (int i=0; i< count; i++) {		tyoHs(((Integer)bytes[i]).byteValue());						if (get() == -1) throw new BrickLostException();	    }	}			finally {	    close();	}		    }	    public void writeWord(int addr, int word) throws BrickLostException {	Object[] wordList = new Object[2];			wordList[0] = new Integer(highByte(word));	wordList[1] = new Integer(lowByte(word));			writeList(addr, wordList);    }		    public Object uploadData() throws BrickLostException {	int[] result;	init();	Object checkResult= check();			if (checkResult instanceof CheckGood) {	    uploadDataSetSize=2500;	    uploadProgress=0;	    uploading=true;				    setPointer(1280);	    try {		result= readShortByteList(2500);	    } catch (BrickLostException ble) {		close();		uploading=false;		throw ble;	    }				} else {	    close();	    uploading=false;	    return checkResult;	}					close();	uploading=false;			return result;    }    	    private boolean writeByte(int b) {	try {	    tyoHs((byte)BLUECK_WRITE_BYTE);	    tyoHs((byte)0);	    tyoHs((byte)1);	    tyoHs((byte)lowByte(b));	    get();	} catch (Exception ioe) {	    ioe.printStackTrace();	}			return true;    }        private Object cricketCheck() { 	if (DEBUG) System.out.println("Starting cricket check...");			int b=0;	try {	    if (DEBUG) System.out.println("ck check: rxFlush");	    //			sp.rxFlush();	    input.skip(input.available());				    if (DEBUG) System.out.println("ck check: putByte blueck_check");	    put((byte)BLUECK_CHECK);			    if (DEBUG) System.out.println("ck check: wait for txbuf clear");	    //			while (sp.txBufCount() > 0) Thread.sleep(20);	    output.flush();				    if (DEBUG) System.out.println("ck check: get byte");	    b= get();	    if (b == -1) {		return new NoInterface();  // no interface	    }				    if (DEBUG) System.out.println("ck check: tyoHs 0");	    tyoHs((byte)0);				    if (DEBUG) System.out.println("ck check: getByte");				    b= get();				    if (DEBUG) System.out.println("ck check: got "+b+" for ck reply");				} catch (Exception ioe) {	    ioe.printStackTrace();	}	if (b != BLUECK_CHECK_REPLY) {	    //return new NoBrick(); // cricket didn't reply properly	    return CHECK_NO_BRICK;	}			//return new CheckGood();  // check successful!	return CHECK_GOOD;    }	    public Object check() {  // returns an instance of CheckGood, NoInterface, xor NoBrick	init();	Object result= cricketCheck();	close();	return result;    }        private byte lowByte(int word) {	return (byte)(word % 256);    }	    private byte highByte(int word) {	return lowByte(word / 256);    }			    private boolean tyoHs(byte b) {	int reply= -1;			try {	    put(b);	} catch (Exception ioe) {	    ioe.printStackTrace();	}			try {	    reply = get();	} catch (Exception ioe) {	    ioe.printStackTrace();	}				if (reply == -1) return false;	return true;    }    */        public String[] portsAvailable() {	Enumeration sportsAll = null;	Vector v = new Vector();	SerialPort sp;	CommPortIdentifier portID;	String currString;			try {	    sportsAll= CommPortIdentifier.getPortIdentifiers();	} catch (Exception e) {	    e.printStackTrace();	    //System.out.println(e);	}	int i = 0;	while (sportsAll.hasMoreElements()) {	    try {		currString = ((CommPortIdentifier)(sportsAll.nextElement())).getName();		portID = CommPortIdentifier.getPortIdentifier(currString);		if (portID.getPortType() == portID.PORT_SERIAL) {		    sp = (SerialPort)			portID.open("Blue Cricket Comm Test", 500);		    sp.close();		    v.addElement(currString);		}					    } catch (Exception e) {	    }	}     	String[] sportsAvail= new String[v.size()];	v.copyInto(sportsAvail);		if (sportsAvail.length>0) {	    return sportsAvail;	} else {	    String[] result = new String[1];	    result[0]="<no ports found>";	    return result;	    	}    }}