import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;


public class DbnApplet extends Applet
{
    static DbnApplet applet;
    static boolean application;
    static Properties properties;

    String encoding;

    static final String DEFAULT_PROGRAM = "// enter program\n";

    DbnEnvironment environment;


    public void init() {
	applet = this;
	encoding = get("encoding", null);
	new DbnPreprocessor(this);

	//String file, prog = null;
	//String progs[] = null;
	//setLayout(new BorderLayout());
	//System.getProperties().list(System.out);
	//System.out.println("home = " + System.getProperty("user.home"));
	//System.out.println("prefix = " + System.getProperty("sys.prefix"));

	String mode = get("mode", "editor");
	if (mode.equals("editor")) {
	    System.err.println("editor not yet complete");
	    System.exit(0);
	    /*
	    boolean beautify = false; 
	    String program = get("program", null); 
	    if (program == null) { 
		program = get("inline_program", null); 
	    } 
	    if (program != null) { 
		// don't convert ; to \n if scheme
		if (program.charAt(0) != ';') { 
		    program = program.replace(';', '\n'); 
		    // not scheme, but don't beautify if it's python
		    if (program.charAt(0) != '#') 
			beautify = true; 
		} 
	    } else { 
		program = DEFAULT_PROGRAM; 
	    } 
	    add(hostess = new DbnEditor(this, program));
	    DbnEditor editor = new DbnEditor(this, program);
	    if (beautify) {
	    editor.doBeautify();
	    }
	    add(editor);
	    environment = editor;
	    */
	} else if (mode.equals("grid")) {
	    // read 1 or more programs to be laid out in grid mode
	    // first count how many programs
	    int counter = 0;
	    while (true) {
		if (get("program" + counter, null) == null)
		    break;
		counter++;
	    }
	    // next load the programs
	    // what to do if zero programs in griddify?
	    String filenames[] = new String[counter];
	    String programs[] = new String[counter];
	    for (int i = 0; i < counter; i++) {
		String filename = get("program" + i, null);
		programs[i] = readFile(filename);
	    }
	    DbnGrid grid = new DbnGrid(this, programs);
	    add(grid);
	    environment = grid;

	} else if (mode.equals("player")) {
	    // could also do a class.forname for jdk11
	    DbnPlayerProgram dpp = new DbnPlayerProgram(this);
	    add(dpp);
	    environment = dpp;
	    dpp.start();

#ifdef CONVERTER
	} else if (mode.equals("convert")) {
	    try {
		String program = readFile(get("program", null));
		DbnParser parser = 
		    new DbnParser(DbnPreprocessor.process(program));
		String converted = parser.getRoot().convert();
		FileOutputStream fos = 
		    new FileOutputStream("DbnPlayerProgram.java");
		PrintStream ps = new PrintStream(fos);
		ps.print(converted);
		ps.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    System.exit(0);
#endif

	} else if (mode.equals("exhibition")) {
	    Hashtable names = new Hashtable();
	    names.put("akilian", "Axel Kilian");
	    names.put("carsonr", "Carson Reynolds");
	    names.put("darkmoon", "Chris McEniry");
	    names.put("golan", "Golan Levin");
	    names.put("jared", "Jared Schiffman");
	    names.put("shyam", "Shyam Krishnamoorthy");
	    names.put("ben", "Ben Fry");
	    names.put("casey", "Casey Reas");
	    names.put("dc", "David Chiou");
	    names.put("hannes", "Hannes Vilhjalmsson");
	    names.put("kelly", "Kelly Heaton");
	    names.put("tom", "Tom White");
	    names.put("cameron", "Cameron Marlow");
	    names.put("dana", "Dana Spiegel");
	    names.put("elise", "Elise Co");
	    names.put("james", "James Seo");
	    names.put("ppk", "Pengkai Pan");

	    int counter = 0; 
	    while (true) {
		if (get("program" + counter, null) == null)
		    break;
		counter++;
	    }
	    String filenames[] = new String[counter];
	    String programs[] = new String[counter];
	    String students[] = new String[counter];
	    for (int i = 0; i < counter; i++) {
		String filename = get("program" + i, null);
		String userid = filename.substring(0, filename.lastIndexOf("/"));
		userid = userid.substring(userid.lastIndexOf("/") + 1);
		students[i] = (String) names.get(userid);
		programs[i] = readFile(filename);
	    }
	    DbnExhibitionGrid grid = 
		new DbnExhibitionGrid(this, programs, students);
	    add(grid);
	    environment = grid;
	}
    }


    public void destroy() {
	if (environment != null) {
	    environment.terminate();
	}
    }


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


    // all the information from DbnProperties

    static public String get(String attribute, String defaultValue) {
	String value = application ?
	    properties.getProperty(attribute) : applet.getParameter(attribute);

	return (value == null) ? 
	    defaultValue : value;
    }

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

    static public boolean isApplet() {
	return (!application);
	//return (applet != null);
    }

    static public boolean isMacintosh() {
	return System.getProperty("os.name").toLowerCase().indexOf("mac") != -1;
    }

    static public boolean hasFullPrivileges() {
	//if (applet == null) return true;  // application
	//return false;
	return !isApplet();
    }

    public String getNetServer() {
	String host = get("net_server", null);
	if (host != null) return host;

	if (isApplet()) {
	    return getCodeBase().getHost();
	}
	return "dbn.media.mit.edu";
    }

    static public Font getFont(String which) {
	if (which.equals("editor")) {
	    // 'Monospaced' and 'courier' also caused problems.. ;-/
	    return new Font("monospaced", Font.PLAIN, 12);
	}
	return null;
    }
}


	/*
	String separator = System.getProperty("line.separator");
	eolCount = separator.length();
	eol = new char[eolCount];
	for (int i = 0; i < eolCount; i++) {
	    eol[i] = separator.charAt(i);
	}
	*/


	/* not so useful
    public boolean isLocal() {
	if (!isApplet()) return true;
	String codebase = getCodeBase().toString();
	return (codebase.indexOf("file") == 0);
    }
	*/


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
