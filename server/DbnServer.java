import java.io.*;
import java.net.*;
import java.util.*;


/*
  Simplistic server that handles DBN <net> messages.

  The packages of info from server to client look like:
  <int version> <long stamp> <int num> <int data>
  version is 1 for the current version of dbn
  num is 1 to 1000, corresponding with <net 1> .. <net 1000>
  data is the actual integer value, which can may even go outside
    the range 0..100 (though maybe we won't advertise that)

  mode of operation for the server is as such:

  server has 1000 queues that are each 10 elements in length (adjustable)
  (these will be an array with a mod-based index)

  Messages from client to server for a 'get'
  <int version = 1> <int command = 1> <int num> <long last_stamp> 
  the server will return a package like the one above, 
  with something that's newer than 'last stamp'

  Messages from client to server for a 'put'
  <int version = 1> <int command = 2> <int num> <int data>

  a packet should look like:
  <int version> <int message> <int subport> <int data> <long stamp>

  nevermind, taking out the 'stamp' marker
  there is no queue, it just polls the 'last' or 'current' value
  
  -- 
  
  <int version> <int message> <int number> <int value>
  
  version is 1
  message is 0 for get, 1 for set
  number is 1 to 1000, for <net 1> to <net 1000>
  value is an integer (32 bit signed number)
  
  unload connections that have been dormant for more than 20 seconds
*/

public class DbnServer extends Thread
{
  static final int PORT = 8000;
    
  static final int ERROR_MESSAGE = -1;
  static final int OK_MESSAGE = 0;
  static final int GET_MESSAGE = 1;
  static final int SET_MESSAGE = 2;

  static final int CONNECTOR_COUNT = 1000;
  static final int MAX_CLIENT_COUNT = 100;
  //static final int CLIENT_TIMEOUT_MILLIS = 20 * 1000; // 20 seconds
  static final int CLIENT_TIMEOUT_MILLIS = 120 * 1000;
    
  // the data being stored for sending
  int values[];
  long timeStamp[];

  DbnServerListener listener;

  int clientCount;
  Object clientLock = new Object();

  Socket clientSockets[];
  DataOutputStream outputStreams[];
  DataInputStream inputStreams[];
    
    
  public DbnServer() {
    clientCount = 0;
	
    clientSockets = new Socket[MAX_CLIENT_COUNT];
    inputStreams = new DataInputStream[MAX_CLIENT_COUNT];
    outputStreams = new DataOutputStream[MAX_CLIENT_COUNT];
    timeStamp = new long[MAX_CLIENT_COUNT];
    try {
      listener = new DbnServerListener(this);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    values = new int[CONNECTOR_COUNT];
    this.start();
  }


  public void addConnection(Socket newClient) {
    System.err.println("adding connection");
    synchronized (clientLock) {
      try {
	clientSockets[clientCount] = 
	  newClient;
	inputStreams[clientCount] = 
	  new DataInputStream(newClient.getInputStream());
	outputStreams[clientCount] = 
	  new DataOutputStream(newClient.getOutputStream());
	timeStamp[clientCount] = 
	  System.currentTimeMillis();
	clientCount++; 
      } catch (IOException e) {
	e.printStackTrace();
      }
    }
  }
    
  int counter;
  long now, then;

  public void run() {
    while (Thread.currentThread() == this) {
      then = now;
      now = System.currentTimeMillis();
      //System.out.println((now-then) + "ms since last time through");

      // check for get/set messages from the clients
      for (int i = 0; i < clientCount; i++) {
	if (clientSockets[i] == null) 
	  continue;
	//System.out.println("serving client " + i);
		
	try {
	  long t1 = System.currentTimeMillis();
	  int available = inputStreams[i].available();
	  if (available <= 0) {
	    if (now - timeStamp[i] > CLIENT_TIMEOUT_MILLIS) {
	      // disconnect 
	      System.err.println("he got boring");
	      killClient(i);
	      continue;
	    }
	    continue;
	  } else {
	    //System.err.println("getting " + available + " bytes");
	  }
	  if (available < 16) continue;

	  //System.out.println(available + " bytes available");

	  // <int version> <int message> <int number> <int value>
	  int version = inputStreams[i].readInt();
	  int message = inputStreams[i].readInt();
	  int number = inputStreams[i].readInt();
	  int value = inputStreams[i].readInt();

	  //System.err.print("(v" + version );
	  /*
	    if (message == GET_MESSAGE)
	    System.err.println("get " + number +
	    " (it's " + values[number-1] + ")");
	    else if (message == SET_MESSAGE)
	    System.err.println("set " + number + " " + value);
	    else
	    System.err.println(version + ", " + message + ", " + 
	    number + ", " + value);
	  */
	  long t2 = System.currentTimeMillis();
	  //System.out.println("read time was " + (t2-t1));
		    
	  if (version != 1) continue;

	  switch (message) {
	  case GET_MESSAGE:
	    outputStreams[i].writeInt(1);
	    outputStreams[i].writeInt(OK_MESSAGE);
	    outputStreams[i].writeInt(number);
	    outputStreams[i].writeInt(values[number-1]);
	    outputStreams[i].flush();
	    timeStamp[i] = now;
	    break;

	  case SET_MESSAGE:
	    if ((number > 0) && (number <= 1000)) {
	      // echo back an OK message w/ the new value
	      values[number-1] = value;
	      outputStreams[i].writeInt(1);
	      outputStreams[i].writeInt(OK_MESSAGE);
	      outputStreams[i].writeInt(number);
	      outputStreams[i].writeInt(values[number-1]);
	    } else {
	      // tell the client that's invalid
	      outputStreams[i].writeInt(1);
	      outputStreams[i].writeInt(ERROR_MESSAGE);
	      outputStreams[i].writeInt(number);
	      outputStreams[i].writeInt(0);
	    }
	    outputStreams[i].flush();
	    timeStamp[i] = now;
	    break;

	  default:
	    System.err.println("Illegal message from client");
	    break;
	  }
	  
	  long t3 = System.currentTimeMillis();
	  //System.out.println("respond time was " + (t3-t2));

	} catch (IOException e) {
	  // if error, disconnect
	  e.printStackTrace();
	  killClient(i);
	}
      }

      if ((counter % 100) == 0) {
	try {
	  Thread.sleep(5);
	} catch (InterruptedException e) {
	}
	counter = 0;
      } 
      counter++;
    }
  }

  protected void killClient(int which) {
    System.err.println("i don't like that guy");
    clientSockets[which] = null;
    inputStreams[which] = null;
    outputStreams[which] = null;
  }    

  public static void main(String args[]) {
    new DbnServer();
  }
}


class DbnServerListener extends Thread
{
  DbnServer parent;
  ServerSocket listenSocket;
  boolean stop = false;
    
  public DbnServerListener(DbnServer parent) throws IOException {
    super("listener");
    this.parent = parent;
    listenSocket = new ServerSocket(DbnServer.PORT);
    this.start();
    this.setPriority(MIN_PRIORITY);
  }
    
  public void pleaseStop() {
    this.stop = true;
    this.interrupt();
  }
    
  public void run() {
    while (!stop) {
      System.err.println("listening...");
      try {
	Socket client = listenSocket.accept();
	System.out.println("got connection");
	parent.addConnection(client);

	try {
	  Thread.sleep(1000);
	} catch (InterruptedException e) {
	}
      }
      catch (InterruptedIOException e1) {
	e1.printStackTrace();
      }
      catch (IOException e2) {
	e2.printStackTrace();
      }
    }
  }
}


