import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;

// might cause problems for jdk 1.0
import java.awt.event.*;


public class DbnApplet extends Applet
{
    DbnGui gui;
    DbnApplet applet;
    Properties properties;

    // I18N functions require jdk 1.1, set this to
    // false and it should remove the non jdk-1.0 code
    static final boolean I18N = true;
    private String languageEncoding;
    private char[][] languageTable;
    Hashtable languageHash;

    // use \u0000 for unicode characters, since most
    // versions of javac will choke on > 7 bit characters
    static final String keywords[][] = { {
	"en", "paper", "pen", "line", "repeat", "forever", 
	"set", "command", "number", "field", "refresh",
	"mouse", "keyboard", "net", "time",
	"same?", "notsame?", "smaller?", "notsmaller?"
    }, {
	"es", "papel", "stilo", "l\u00EDnea", "repita", "siempre",
	"ponga", "instruci\u00F3n", "n\u00FAmero", "\u00E1rea", "refrese",
	"rat\u00F3n", "teclado", "internet", "hora",
	"\u00BFigual?", "\u00BFnoigual?", "\u00BFmenos?", "\u00BFnomenos?"
	}, {
	"fr", "papier", "plume", "ligne", "r\u00E9peter", "toujours",
	"mettre", "fonction", "num\u00E9ro", "r\u00E9gion", "\u00E0neuf",
	"souris", "clef", "r\u00E9seau", "heure",
	"pareil?", "paspareil?", "pluspetit?", "plusgrand?"
    } };


    // when using dbn as an application instead of an applet
    // (this will always be done with jdk 1.1 or higher)
    static public void main(String args[]) {
	//new MemoryReporter();

	Frame frame = new Frame("DBN");
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		//e.getWindow().dispose();
		System.exit(0);
	    }
	});
	Dimension screen = 
	    Toolkit.getDefaultToolkit().getScreenSize();
	frame.reshape(screen.width+10, 10, 100, 200);
	frame.show();
	DbnApplet app = new DbnApplet();
	app.applet = app;
	app.properties = new Properties();
	try {
	    app.properties.load(new FileInputStream("dbn.properties"));
	} catch (Exception e1) {
	    try {
		app.properties.load(new FileInputStream("lib/dbn.properties"));
	    } catch (Exception e) {
		System.err.println("Error reading dbn.properties");
		e.printStackTrace();
		System.exit(1);
	    }
	}
	int width = Integer.parseInt(app.properties.getProperty("width"));
	int height = Integer.parseInt(app.properties.getProperty("height"));
	frame.add(app);
	app.init();
	Insets insets = frame.getInsets();
	frame.reshape(50, 50, width + insets.left + insets.right, 
		      height + insets.top + insets.bottom);
	frame.pack();
	//frame.doLayout();
    }
    

    public void init() {
	String file, prog = null;
	setLayout(new BorderLayout());

	if (I18N) initLanguage();
	
	boolean wasInline = false;
	prog = getParameter("inline_program");
	if (prog == null) {
	    file = getParameter("program");
	    if (file != null && file.length() > 0) {
		prog = readFile(file);
	    }
	    if (prog == null || prog.length() == 0) {
		prog = new String("// type program here\n");
	    }
	} else {
	    prog = prog.replace(';','\n');
	    wasInline = true;
	}
	
	add("Center", gui = new DbnGui(this, prog));
	if (wasInline) gui.beautify(); // inline progs will look scary
    }

    public String getParameter(String name) {
	if (isApplet()) {
	    return super.getParameter(name);
	}
	return properties.getProperty(name);
    }

    public void destroy() {
	if (gui != null) gui.terminate();
    }


    /* loading order:
     * 0. if application, a file on the disk
     * 1. a file relative to the .html file containing the applet
     * 2. a url 
     * 3. a file relative to the .class files
     * 4. http://thehost/whatever.dbn
     */
    public String readFile(String filename) {
	URL url;
	InputStream stream = null;
	String openMe;
	byte temp[] = new byte[16384];  // 16k
	
	String status = "Loading " + filename;
	if (gui != null) gui.showStatus(status);

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
		// Now try to open it relative to the code base
		url = new URL(getCodeBase(), filename);
		stream = url.openStream();
	    } else {
		// this is jdk 1.1, but should be ok for app versions
		url = getClass().getResource(filename);
		stream = url.openStream();
	    } 

	} catch (Exception e2) { try {
	    // Try to open the param string as a URL
	    url = new URL(filename);
	    stream = url.openStream();
	
	} catch (Exception e3) { try {
	    if (isApplet()) {
		// desperation, try from the root of the codebase server
		url = getCodeBase();
		openMe = "http://" + url.getHost();
		openMe = openMe.concat(filename);
		url = new URL(openMe);
		stream = url.openStream();
	    } else {
		throw new Exception();
	    }

	} catch (Exception e6) {
	    e1.printStackTrace(); 
	    e2.printStackTrace();
	    e3.printStackTrace();
	    if (gui != null) gui.clearStatus(status);
	    return null;
	} } } }

	try {
	    //int length = connection.getContentLength();
	    //InputStream input = connection.getInputStream();
	    int start = 0;
	    //byte program[] = new byte[length];

	    while (true) {
		int byteCount = stream.read(temp, start, 1024);
		if (byteCount <= 0) break;
		start += byteCount;
		//length -= byteCount;
	    }
	    byte program[] = new byte[start];
	    System.arraycopy(temp, 0, program, 0, start);
	    if (gui != null) gui.clearStatus(status);
	    //return program;
	    
	    return languageEncode(program);

	} catch (Exception e) {
	    System.err.println("problem during download");
	    e.printStackTrace();
	    if (gui != null) gui.clearStatus(status);
	    return null;
	}
    }


    public String languageEncode(byte program[]) {
	if (I18N) {
	    // convert the bytes based on the current
	    // language and encoding setting
	    try {
		if (languageEncoding == null)
		    return new String(program);
		System.err.println("using encoding " + languageEncoding);
		return new String(program, languageEncoding);
	    } catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		languageEncoding = null;
		return new String(program);
	    }
	} else {
	    // use old-style jdk 1.0 constructor
	    return new String(program, 0);
	}
    }

    public boolean isApplet() {
	// counter-intuitive, but applet only set as application
	return (applet == null);
    }
	
    public boolean isLocal() {
	if (!isApplet()) return true;
	String codebase = getCodeBase().toString();
	return (codebase.indexOf("file") == 0);
    }

    public void showStatus(String status) {
	if (isApplet()) {
	    super.showStatus(status);
	}
    }

    /*
    public void paint(Graphics g) {
	Rectangle r = bounds(); // do this to make sure back is uniform color
	g.setColor(Color.gray);
	g.fillRect(0, 0, r.width, r.height);
    }
    */
    
    public void setProgram(String filename) {
	// for nn javascriptlink
	gui.setProgram(readFile(filename));
    }

    /* temporary, a little something for the kids */
    static public void debugString(String s) {
	byte output[] = s.getBytes();
	for (int i = 0; i < output.length; i++) {
	    if (output[i] >= 32) {
		System.out.print((char)output[i]);
	    } else {
		System.out.print("\\" + (int)output[i]);
            }
        }
	System.out.println();
    }


    public void initLanguage() {
	if (!I18N) return;
	
	languageHash = new Hashtable();
	int languageCount = keywords.length;
	int keywordCount = getKeywordCount();
	for (int i = 0; i < languageCount; i++) {
	    String languageName = keywords[i][0];
	    char characters[][] = new char[keywordCount][];
	    for (int j = 0; j < keywordCount; j++) {
		characters[j] = keywords[i][j+1].toCharArray();
	    }
	    languageHash.put(languageName, characters);
	}

	String parameter = getParameter("encoding");
	if (parameter == null) languageEncoding = null;

	parameter = getParameter("language"); 
	if (parameter == null) {
	    // this means no conversion
	    languageTable = null;
	} else {
	    //System.out.println(language + " " + languageHash);
	    languageTable = (char[][])languageHash.get(parameter);
	}
	/*
	for (int j = 0; j < languageHash.size()-1; j++) {
	    for (int i = 0; i < keywordCount; i++) {
		System.err.println(keywords[0][i+1] + "\t" + 
				   keywords[j+1][i+1]);
	    }
	    System.err.println();
	}
	*/
    }

    public int getKeywordCount() {
	return keywords[0].length - 1;
    }

    // returns a hashtable of char[][] where 
    // the keyword is the language 'identifier' such as
    // en, es, de (the iso specifiers) and the value
    // is char[][], which contains the chars for replace
    public char[][] getLanguageTable() {
	if (!I18N) return null;
	return languageTable;
    }

    public char[][] getEnglishTable() {
	if (!I18N) return null;
	return (char[][]) languageHash.get("en");
    }

    // parse language table using input from a file
    // read file using program methods above, 
    // then add the language to the languageHash
    public void addLanguageTable(String filename) {
	if (!I18N) return;

	char[] chars = readFile(filename).toCharArray();
	int keywordIndex = 0;
	char[] keyword = new char[32];
	int translationIndex = 0;
	char[] translation = new char[32];
	int keywordCount = keywords[0].length;
	char[][] keywordData = new char[keywordCount][];
	String languageName = null;

	for (int i = 0; i < chars.length; i++) {
	    while (Character.isWhitespace(chars[i])) i++;
	    if (i == chars.length) break;
	    
	    // look for comments, which start with slash-slash
	    // (just like dbn for sake of simplicity)
	    if ((chars[i] == '/') && (i < chars.length-1) &&
		(chars[i+1] == '/')) {
		// it's a comment, ignore
		while ((chars[i] != '\r') && (chars[i] != '\n')) {
		    i++; if (i == chars.length) break;
		}
	    }
	    if (i == chars.length) break;

	    // read the keyword
	    while (!Character.isWhitespace(chars[i]))
		keyword[keywordIndex++] = chars[i++];

	    while (Character.isWhitespace(chars[i])) i++;
	    
	    // read the translation of that keyword
	    while ((i != chars.length) &&
		   !Character.isWhitespace(chars[i]))
		translation[translationIndex++] = chars[i++];
	    if (i == chars.length) break;
	    
	    // match it up with the default language
	    // check each of the keywords in the default language
	    boolean foundMatch = false;
	    for (int j = 0; j < keywordCount; j++) {
		String one = new String(keywords[0][j]);
		String two = new String(keyword, 0, keywordIndex);
		if (one.equals(two)) {
		    keywordData[j] = new char[keywordIndex];
		    System.arraycopy(translation, 0, 
				     keywordData, 0, translationIndex);
		} else if (two.equals("language")) {
		    languageName = 
			new String(translation, 0, translationIndex);
		}
	    }
	    if (!foundMatch) {
		System.err.println("Error in language file: Could not find");
		System.err.println("find a match for keyword " + keyword);
		return;
	    }
	}
	if (languageName == null) {
	    System.err.println("Error in language file: ");
	    System.err.println("Language name not specified in file");
	    return;
	}
	languageHash.put(languageName, keywordData);
    }
}


/*
class MemoryReporter implements Runnable {
    Thread thread;

    public MemoryReporter() {
	thread = new Thread(this);
	thread.start();
    }

    public void run() {
	while (Thread.currentThread() == thread) {
	    try {
		thread.sleep(1000);
	    } catch (InterruptedException e) {
	    }
	    Runtime r = Runtime.getRuntime();
	    r.gc();
	    long mem = r.freeMemory();
	    long tmem = r.totalMemory();
	    //mem /= 1024;
	    System.err.println(mem + " " + tmem);
	}
    }
}
*/
