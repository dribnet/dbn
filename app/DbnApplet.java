#ifndef KVM


import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;


public class DbnApplet extends Applet
{
  static DbnApplet applet;
  static Properties properties;
  boolean errorState;

#ifndef PLAYER
  String encoding;
  DbnEnvironment environment;
#endif

  public void init() {
    applet = this;
    //System.getProperties().list(System.out);
    //System.out.println("home = " + System.getProperty("user.home"));
    //System.out.println("prefix = " + System.getProperty("sys.prefix"));

#ifdef PLAYER
    // because it's the player version, cut out all the 
    // other crap, so that this file is as small as possible

    //} else if (mode.equals("player")) {
    // could also do a class.forname for jdk11
    //DbnPlayerProgram dpp = new DbnPlayerProgram(this);
    try {
      String program = get("program");
      DbnPlayer player = 
	((DbnPlayer) Class.forName(program).newInstance());
      add(player);
      //environment = player;
      player.init(this);
      player.start();
    } catch (Exception e) {
      e.printStackTrace();
      errorState = true;
    }
#else
    encoding = get("encoding");
    new DbnPreprocessor(this);

    String mode = get("mode", "editor");
    //System.err.println("mode is " + mode);
    if (mode.equals("editor")) {
#ifdef EDITOR
      //System.err.println("editor not yet complete");
      //System.err.println("editor dammit");
      //System.exit(0);
      boolean beautify = false; 
      String program = get("program"); 
      if (program != null) { 
	program = readFile(program);
      } else {
        program = get("inline_program"); 
      } 
      if (program != null) {
	// don't beautify if it's java code
	if (program.indexOf("extends DbnPlayer") == -1) {
	  // don't convert ; to \n if scheme  
	  if (program.charAt(0) != ';') {  
	    program = program.replace(';', '\n'); 
	    // not scheme, but don't beautify if it's python 
	    if (program.charAt(0) != '#') 
	      beautify = true; 
	  }  
	}
      } 
      //add(hostess = new DbnEditor(this, program));
      DbnEditor editor = new DbnEditor(this, program);
      if (beautify) editor.doBeautify(); 

      setLayout(new BorderLayout());
      add("Center", editor);
      environment = editor;

      convert();
#endif

    } else if (mode.equals("grid")) {
      // read 1 or more programs to be laid out in grid mode
      // first count how many programs
      int counter = 0;
      while (true) {
	if (get("program" + counter) == null)
	  break;
	counter++;
      }
      // next load the programs
      // what to do if zero programs in griddify?
      String filenames[] = new String[counter];
      String programs[] = new String[counter];
      for (int i = 0; i < counter; i++) {
	String filename = get("program" + i);
	programs[i] = readFile(filename);
      }
      DbnGrid grid = new DbnGrid(this, programs);
      setLayout(new BorderLayout());
      add("Center", grid);
      environment = grid;
    }
#endif PLAYER
  }


#ifndef PLAYER
  public void destroy() {
    if (environment != null) {
      environment.terminate();
    }
  }
#endif

  /*
#ifdef EDITOR
  // this is used by DbnFancy, but could be useful in other
  // contexts as well, i would imagine
  public void setProgram(String p) {
    if (environment instanceof DbnEditor) {
      ((DbnEditor)environment).setProgram(p);
    }
  }
#endif
  */

  public void paint(Graphics g) {
    if (errorState) {
      g.setColor(Color.red);
      Dimension d = size();
      g.fillRect(0, 0, d.width, d.height);
      //} else {
      //super(g);
    }
  }

#ifdef CONVERTER
  /*
    } else if (mode.equals("convert")) {
      convert(readFile(get("input_filename")), 
	      get("output_class"), get("output_filename"));
      System.exit(0);
  */

  //public void convert(String program, String classname, String filename) {
  public void convert() {
    //System.getProperties.list(System.out);
    //System.getProperties().list(System.out);
    try {
      FileDialog fd = new FileDialog(new Frame(), 
				   "Select a DBN program to convert...", 
				   FileDialog.LOAD);
    fd.show();
	
    String inputDirectory = fd.getDirectory();
    String inputFilename = fd.getFile();
    if (inputFilename == null) return; // user cancelled

    int suffixIndex = inputFilename.lastIndexOf(".");
    String outputNameBase = null;
    if (suffixIndex != -1) {
      String suffix = inputFilename.substring(suffixIndex);
      if (suffix.equals(".dbn")) {
	outputNameBase = inputFilename.substring(0, suffixIndex);
      } else {
	System.err.println("suffix no good: " + suffix);
      }
    }
    fd = new FileDialog(new Frame(), 
			"Save converted program as...", 
			FileDialog.SAVE);
    fd.setDirectory(inputDirectory);
    if (outputNameBase != null) fd.setFile(outputNameBase);
    fd.show();

    String outputDirectory = fd.getDirectory();
    String outputName = fd.getFile();
    if (outputName == null) return;

    File inputFile = new File(inputDirectory, inputFilename);
    //FileInputStream fis = new FileInputStream(inputFile);
    FileInputStream input = new FileInputStream(inputFile);
    int length = (int) inputFile.length();
    byte data[] = new byte[length];
    int count = 0;
    while (count != length) {
      data[count++] = (byte) input.read();
    }
    // not I18N compliant
    String program = new String(data);

    DbnParser parser = 
      new DbnParser(DbnPreprocessor.process(program));
    String converted = parser.getRoot().convert(outputName);
    File javaOutputFile = new File(outputDirectory, outputName + ".java");
    FileOutputStream fos = new FileOutputStream(javaOutputFile);
    PrintStream ps = new PrintStream(fos);
    ps.print(converted);
    ps.close();
    
    File htmlOutputFile = new File(outputDirectory, outputName + ".html");
    fos = new FileOutputStream(htmlOutputFile);
    ps = new PrintStream(fos);
    ps.println("<HTML> <BODY BGCOLOR=\"white\">");
    ps.println("<APPLET CODE=\"DbnApplet\" WIDTH=101 HEIGHT=101>");
    ps.print("<PARAM NAME=\"program\" VALUE=\"");
    ps.print(outputName);
    ps.println("\">");
    ps.println("</APPLET>");
    ps.println("</BODY> </HTML>");
    ps.close();
    
    // copy DbnException.class, DbnGraphics.class, 
    // DbnApplet.class, and DbnPlayer.class to the directory
    copyFile(new File("lib\\player", "DbnApplet.class"),
	     new File(outputDirectory, "DbnApplet.class"));

    copyFile(new File("lib\\player", "DbnException.class"), 
	     new File(outputDirectory, "DbnException.class"));

    copyFile(new File("lib\\player", "DbnGraphics.class"), 
	     new File(outputDirectory, "DbnGraphics.class"));

    copyFile(new File("lib\\player", "DbnPlayer.class"), 
	     new File(outputDirectory, "DbnPlayer.class"));

    // execute javac with parameters:
    // String outputdir = new File(filename).getPath();
    // javac -classpath outputdir;%CLASSPATH% outputdir\*.java

    String args[] = new String[5];
    args[0] = "-classpath";
    args[1] = outputDirectory + File.pathSeparator + 
      System.getProperty("java.class.path");
    args[2] = "-d";
    args[3] = outputDirectory;
    args[4] = javaOutputFile.getCanonicalPath();
    sun.tools.javac.Main.main(args);

    // if that's no good (which it's not)
    // need to instead use sun.tools.javac.Main,
    // which should be separated out from the core source base
    // using metrowerks' nice binding stuff

    } catch (Exception e) { // dbn or ioex
      e.printStackTrace();
    }
  }

  protected void copyFile(File afile, File bfile) {
    try {
      FileInputStream from = new FileInputStream(afile);
      FileOutputStream to = new FileOutputStream(bfile);
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = from.read(buffer)) != -1) {
	to.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
#endif


#ifndef PLAYER
  /* loading order:
   * 0. if application, a file on the disk
   * 1. a file relative to the .html file containing the applet
   * 2. a url 
   * 3. a file relative to the .class files
   */
  public String readFile(String filename) {
    if (filename.length() == 0) {
      return null;
    }
    URL url;
    InputStream stream = null;
    String openMe;
    byte temp[] = new byte[65536];  // 64k, 16k was too small

    try {
      // this is two cases, one is bound to throw (or work)
      if (isApplet()) {
	// Try to open it relative to the document base
	url = new URL(getDocumentBase(), filename);
	stream = url.openStream();
      } else {
	// if running as an application, get file from disk
	stream = new FileInputStream(filename);
      }

    } catch (Exception e1) { try {
      if (isApplet()) {
	// now try to open it relative to the code base
	url = new URL(getCodeBase(), filename);
	stream = url.openStream();
      } else {
#ifdef JDK11
	url = getClass().getResource(filename);
	stream = url.openStream();
#else
	throw new DbnException(); // move to next
#endif
      } 

    } catch (Exception e2) { try {
      // Try to open the param string as a URL
      url = new URL(filename);
      stream = url.openStream();
	
    } catch (Exception e3) {
      e1.printStackTrace(); 
      e2.printStackTrace();
      return null;
    } } }

    try {
      int offset = 0;
      while (true) {
	int byteCount = stream.read(temp, offset, 1024);
	if (byteCount <= 0) break;
	offset += byteCount;
      }
      byte program[] = new byte[offset];
      System.arraycopy(temp, 0, program, 0, offset);

      //return languageEncode(program);
#ifdef JDK11
      // convert the bytes based on the current encoding
      try {
	if (encoding == null)
	  return new String(program);
	return new String(program, encoding);
      } catch (UnsupportedEncodingException e) {
	e.printStackTrace();
	encoding = null;
	return new String(program);
      }
#else
      // use old-style jdk 1.0 constructor
      return new String(program, 0);
#endif 

    } catch (Exception e) {
      System.err.println("problem during download");
      e.printStackTrace();
      return null;
    }
  }

#ifdef EDITOR
  static public Image readImage(String name) {
    Image image = null;
    if (isApplet()) {
      image = applet.getImage(applet.getCodeBase(), name);
    } else {
      Toolkit tk = Toolkit.getDefaultToolkit();
      image =  tk.getImage("lib/" + name);
      //URL url = DbnApplet.class.getResource(name);
      //image = tk.getImage(url);
    }
    MediaTracker tracker = new MediaTracker(applet);
    tracker.addImage(image, 0);
    try {
      tracker.waitForAll();
    } catch (InterruptedException e) { }      
    return image;
  }
#endif  // EDITOR

#endif  // !PLAYER

  // all the information from DbnProperties

  static public String get(String attribute) {
    return get(attribute, null);
  }

  static public String get(String attribute, String defaultValue) {
    String value = (properties != null) ?
      properties.getProperty(attribute) : applet.getParameter(attribute);

    return (value == null) ? 
      defaultValue : value;
  }

#ifndef PLAYER
  static public boolean getBoolean(String attribute, boolean defaultValue) {
    String value = get(attribute, null);
    return (value == null) ? defaultValue : 
      (new Boolean(value)).booleanValue();
  }

  static public int getInteger(String attribute, int defaultValue) {
    String value = get(attribute, null);
    return (value == null) ? defaultValue : 
      Integer.parseInt(value);
  }

  static public Color getColor(String name, Color otherwise) {
    Color parsed = null;
    String s = get(name, null);
    if ((s != null) && (s.indexOf("#") == 0)) {
      try {
	int v = Integer.parseInt(s.substring(1), 16);
	parsed = new Color(v);
      } catch (Exception e) {
      }
    }
    if (parsed == null) return otherwise;
    return parsed;
  }

  static public boolean isMacintosh() {
    return System.getProperty("os.name").toLowerCase().indexOf("mac") != -1;
  }

  static public boolean hasFullPrivileges() {
    //if (applet == null) return true;  // application
    //return false;
    return !isApplet();
  }

  static public Font getFont(String which) {
    if (which.equals("editor")) {
      // 'Monospaced' and 'courier' also caused problems.. ;-/
      //return new Font("monospaced", Font.PLAIN, 12);
      return new Font("Monospaced", Font.PLAIN, 12);
    }
    return null;
  }
#endif  // PLAYER

  public String getNetServer() {
    String host = get("net_server", null);
    if (host != null) return host;

    if (isApplet()) {
      return getCodeBase().getHost();
    }
    return "dbn.media.mit.edu";
  }

  static public boolean isApplet() {
    return (properties == null);
  }
}


#else  // if it is the KVM


public class DbnApplet {
  public DbnApplet() { 
  }

  String get(String something) {
    return get(something, null);
  }

  String get(String something, String otherwise) {
    return null;
  }

  String readFile(String name) {
    // grab something out of the database
    return null;
  }
}


#endif


/* temporary, a little something for the kids */
/*
  static public void debugString(String s) {
  byte output[] = s.getBytes();
  for (int i = 0; i < output.length; i++) {
  if (output[i] >= 32) {
  System.out.print((char)output[i]);
  } else {
  System.out.print("\\" + (int)output[i]);
  if (output[i] == '\n') System.out.println();
  }
  }
  System.out.println();
  }
*/
