#ifdef EDITOR

import java.awt.*;
import java.util.*;


// play, stop, open, save, courseware, print, beautify
// height of button panel is 35

public class DbnEditor implements DbnEnvironment {
    static final String DEFAULT_PROGRAM = "// enter program\n";

    // set explicitly because different platforms use different colors
    static final Color panelBgColor = new Color(204, 204, 204);

    DbnApplet app;

    DbnEditorButtons buttons;
    DbnEditorGraphics graphics;

    Label status;
    TextArea textarea;

    String lastDirectory;
    String lastFile;


    public DbnEditor(DbnApplet app, String program) {
	this.app = app;
	setLayout(new BorderLayout());

	Color bgColor = 
	    DbnProperties.getColor("bg_color", new Color(51, 102, 153));
	Color bgStippleColor = 
	    DbnProperties.getColor("bg_stipple_color", null);
	Color tickColor = 
	    DbnProperties.getColor("tick_color", new Color(204, 204, 204));
	Color gutterBgColor =
	    DbnProperties.getColor("gutter_bg_color", new Color(0, 51, 102));
	Color buttonBgColor = 
	    DbnProperties.getColor("button_bg_color", new Color(153, 153, 153));
	Color statusBgColor = 
	    DbnProperties.getColor("status_bg_color", new Color(204, 204, 204));

	int gwidth = DbnProperties.getInteger("graphics_width", 101);
	int gheight = DbnProperties.getInteger("graphics_height", 101);

	add("North", new DbnEditorLicensePlate(this));

	Panel left = new Panel();
	left.setLayout(new BorderLayout());

	boolean privileges = DbnProperties.hasFullPrivileges();
	boolean courseware = DbnProperties.get("save_as") != null;
	buttons = new DbnEditorButtons(this, privileges, courseware, 
				       (privileges & !courseware), true);
	left.add("North", buttons);

	graphics = new DbnEditorGraphics(gwidth, gheight);
	left.add("Center", graphics);

	gutter = new Panel();
	gutter.setBackground(gutterBgColor);
	left.add("South", gutter);

	Panel right = new Panel();
	right.setLayout(new BorderLayout());

	Panel statusPanel = new Panel();
	statusPanel.setBackground(statusBgColor);
	statusPanel.add(status = new Label());
	right.add("North", statusPanel);

	if (program == null) program = DEFAULT_PROGRAM;
	textarea = new TextArea(program, 20, 48);
	textarea.setFont(DbnProperties.getFont("editor"));
	right.add("Center", text);

	this.add("West", left);
	this.add("East", right);

#ifdef JDK11
	if (!DbnProperties.isMacintosh()) {
	    DbnEditorListener listener = new DbnEditorListener(this);
	    textarea.addKeyListener(listener);
	    textarea.addFocusListener(listener);
	    textarea.addKeyListener(new DbnKeyListener(this));
	}
#endif
    }


    //public Color getPanelBgColor() {
    //return panelBgColor;
    //}

    /*
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
    */

/*
    public void runpanelrefreshed()
    {
	if (runMode!=null)
	    if (runMode.equals("immediate")) {
		if (!getrunningp()) // kick it to start running
		    {
			//			dbrp.initiate();
		    }
	    }
    }
*/
    /*
    public boolean action(Event evt, Object arg) {
    	if (evt.target == cmds) {
	    // could also do it here, i s'pose

    	} else if (evt.target == doitButton) {
	    String selected = cmds.getSelectedItem();
	    if (selected.equals(BEAUTIFY_ITEM)) doBeautify();
	    else if (selected.equals(SNAPSHOT_ITEM)) doSnapshot();
	    else if (selected.equals(SAVE_ITEM)) doSave();
	    else if (selected.equals(OPEN_ITEM)) doOpen();
	    else if (selected.equals(PRINT_ITEM)) doPrint();
    	}
        return true;
    }
    */

    public void doPrint() {
#ifdef JDK11
	Frame frame = new Frame(); // bullocks
	int screenWidth = getToolkit().getScreenSize().width;
	frame.reshape(screenWidth + 20, 100, screenWidth + 100, 200);
	frame.show();

	Properties props = new Properties();
	PrintJob pj = getToolkit().getPrintJob(frame, "DBN", props);
	if (pj != null) {
	    Graphics g = pj.getGraphics();
	    dbrp.runners[dbrp.current].dbg.print(g, 100, 100); 
	    // jdk 1.1 is a piece of crap, the following 
	    // line works only half the time.
	    //g.drawImage(dbrp.runners[dbrp.current].dbg.image, 100, 100, null);
	    g.dispose();
	    g = null;
	    pj.end();
	}
	frame.dispose();
#endif
    }

    public void doSnapshot() {
	dbcp.msg("Sending your file to the server...");

	String programStr = ta.getText();
	byte imageData[] = dbrp.runners[dbrp.current].dbg.getPixels();

	try {
	    URL appletUrl = app.getDocumentBase();
	    String document = appletUrl.getFile();
	    document = document.substring(0, document.lastIndexOf("?"));
	    URL url = new URL("http", appletUrl.getHost(), document);

	    URLConnection conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    conn.setRequestProperty("Content-Type", 
				    "application/x-www-form-urlencoded");
	    
	    DataOutputStream printout = 
		new DataOutputStream(conn.getOutputStream());

	    String saveAs = DbnProperties.get("save_as");
	    String imageStr = 
#ifdef JDK11
		new String(makePgmData(imageData, 101, 101));
#else
	        new String(makePgmData(imageData, 101, 101), 0);
#endif
	    String content = 
		"save_as=" + URLEncoder.encode(saveAs) + 
		"&save_image=" + URLEncoder.encode(imageStr) +
		"&save_program=" + URLEncoder.encode(programStr);

	    printout.writeBytes(content);
	    printout.flush();
	    printout.close();
	    
	    // what did they say back?
	    DataInputStream input = 
		new DataInputStream(conn.getInputStream());
	    String str = null;
	    while ((str = input.readLine()) != null) {
		//System.out.println(str);
	    }
	    input.close();	    
	    dbcp.msg("Done saving file.");

	} catch (Exception e) {
	    e.printStackTrace();
	    dbcp.msg("Problem: Your work could not be saved.");
	}
    }

    static public byte[] makePgmData(byte inData[], int width, int height) {
	//String headerStr = "P6 " + width + " " + height + " 255\n"; 
	String headerStr = "P5 " + width + " " + height + " 255\n";
#ifdef JDK11
	byte header[] = headerStr.getBytes();
#else
	byte header[] = new byte[headerStr.length()];
	headerStr.getBytes(0, header.length, header, 0);
#endif
	//int count = width * height * 3;
	int count = width * height;
	byte outData[] = new byte[header.length + count];
	System.arraycopy(header, 0, outData, 0, header.length);
	System.arraycopy(inData, 0, outData, header.length, count);
	return outData;
    }


    public void doSave() {
	dbcp.msg("Saving file...");
	String s = ta.getText();
	FileDialog fd = new FileDialog(new Frame(), 
				       "Save DBN program as...", 
				       FileDialog.SAVE);
	fd.setDirectory(lastDirectory);
	fd.setFile(lastFile);
	fd.show();
	
	String directory = fd.getDirectory();
	String filename = fd.getFile();
	if (filename == null) return false; // user cancelled
	File file = new File(directory, filename);

	try {
#ifdef JDK11
		FileWriter writer = new FileWriter(file);
		writer.write(s);
		writer.flush();
		writer.close();
#else
		System.err.println("untested code in doLocalWrite");

		FileOutputStream output = new FileOutputStream(file); 
		// no I18N, just blat out the low byte of each char
		byte data[] = new byte[s.length()]; 
		s.getBytes(0, s.length()-1, data, 0); 
		output.write(data); 
		output.flush(); 
		// NOT TESTED
#endif
	    lastDirectory = directory;
	    lastFile = filename;
	    dbcp.msg("Done saving file.");

	} catch (IOException e) {
	    e.printStackTrace();
	    dbcp.msg("Did not write file.");
	}
    }


    public void doOpen() {
	FileDialog fd = new FileDialog(new Frame(), 
				       "Open a DBN program...", 
				       FileDialog.LOAD);
	fd.setDirectory(lastDirectory);
	fd.setFile(lastFile);
	fd.show();
	
	String directory = fd.getDirectory();
	String filename = fd.getFile();
	if (filename == null) return null; // user cancelled
	File file = new File(directory, filename);

	try {
	    FileInputStream input = new FileInputStream(file);
	    int length = (int) file.length();
	    byte data[] = new byte[length];
	    
	    int count = 0;
	    while (count != length) {
		data[count++] = (byte) input.read();
	    }
	    // set the last dir and file, so that they're
	    // the defaults when you try to save again
	    lastDirectory = directory;
	    lastFile = filename;
	
	    // once read all the bytes, convert it to the proper
	    // local encoding for this system.
	    ta.setText(app.languageEncode(data));

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	
	} catch (IOException e2) {
	    e2.printStackTrace();
	}
    }


    public void doBeautify() {
	String prog = ta.getText();
	if ((prog.charAt(0) == '#') || (prog.charAt(0) == ';')) {
	    dbcp.msg("Only DBN code can be made beautiful.");
	    return;
	}
	char program[] = prog.toCharArray();
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


    public void reportError(DbnException e) {
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
	    //System.out.println("st/end: "+st+"/"+end);
            ta.select(st, end+1);
            //if (iexplorerp) {
	    //ta.invalidate();
	    //ta.repaint();
	    //}
	}
	dbcp.repaint(); // button should go back to 'play'
	//System.err.println(e.getMessage());
	dbcp.msg("Problem: " + e.getMessage());
	//showStatus(e.getMessage());
    }

    public void reportSuccess() {
	dbcp.terminated();
	dbcp.msg("Done.");
    }


    public void idle(long t) {
	dbrp.idle(t);
    }

    
    public boolean getrunningp() {
	//return dbrp.dbr.runningp();
	return dbrp.runners[dbrp.current].isRunning();
    }
  
    public void setProgram(String s) {
	if (getrunningp()) terminate();
	ta.setText(s);
    }

    public void initiate() {
	//System.out.println("dbngui initiated");
	dbrp.setProgram(ta.getText());
	dbrp.initiate();
	dbrp.requestFocus();
	dbcp.initiated();
    }
	
    public void terminate() {
	//System.out.println("dbngui terminated");
	dbrp.terminate();
	dbcp.msg("");
    }
	
    //public void terminated() {
	// tell when done
    //dbcp.terminated();
    //}
	
    public void heartbeat() {
	// important to verify run
	dbcp.idle();
    }	


    // uglyish hack for scheme, the fix is even uglier, though
    static DbnGui currentDbnGui;
    static public DbnGui getCurrentDbnGui() {
	return currentDbnGui;
    }
}

#endif
