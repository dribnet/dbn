import java.awt.*;
//import java.applet.Applet;
//import java.util.*;


public class DbnGui extends Panel {
    static final String BEAUTIFY_ITEM = "Beautify";
    static final String SNAPSHOT_ITEM = "Save";
    static final String SAVE_ITEM = "Save as...";
    static final String OPEN_ITEM = "Open...";

    DbnApplet app;
    TextArea ta;
    DbnRunPanel dbrp;
    DbnControlPanelNull dbcp;
    Button doitbut;
    Choice cmds;
    boolean iexplorerp = false; // assume running navigator? ...
    String run_mode; // default
    String titlestr; // documentation string
    Color textcol; // color to draw it in (incl grid)
    Color panelBgColor;

    DbnIO io;  // object to take care of input/output

    // uglyish hack for scheme, the fix is even uglier, though
    static DbnGui currentDbnGui;
    static public DbnGui getCurrentDbnGui() {
	return currentDbnGui;
    }

    public DbnGui(DbnApplet app, String progs[]) {
	this.app = app;
	setLayout(new BorderLayout());
		
	// if ie is just even mentioned assume it is IE
	iexplorerp = (app.getParameter("ie")!=null); 
	// this flag necessary because of textarea behavior 
	// during syntax error signal for mac on IE
	run_mode = (app.getParameter("run_mode"));
	titlestr = (app.getParameter("title"));

	panelBgColor = new Color(204, 204, 204);
		
	if (textcol == null) {
	    String s;
	    if ((s = app.getParameter("text_color"))!=null) {
		if (s.indexOf("#") == 0) {
		    try {
			int v = Integer.parseInt(s.substring(1), 16);
			textcol = new Color(v);
		    } catch (Exception e) {
		    }
		}
	    }
	    if (textcol == null) {
		textcol = Color.white;
	    }
	}
	if (run_mode == null) {
	    buildeditgui(progs);
	} else if (run_mode.equals("immediate") || 
		   run_mode.equals("mouse_inside")) {
	    
	    add("Center", dbrp = new DbnRunPanel(app,this,progs));
	    ta = new TextArea("",80,24); // don't display
	    dbcp = new DbnControlPanelNull(app,this);

	} else {
	    run_mode = null;
	    buildeditgui(progs);
	}
	currentDbnGui = this;
	io = new DbnIO(app);
    }


    public Color getPanelBgColor() {
	return panelBgColor;
    }

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

    public void buildeditgui(String []progs)
    {
	/*
	 * +-----------------------+
	 * |                       |
	 * +-----------+-----------+
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * |           |           |
	 * +-----------+-----------+
	 * |           |           |
	 * +-----------+-----------+
	 */

	add("North", new DbnLicensePlate(this));

	Panel p1 = new Panel();
	p1.setLayout(new GridLayout(1,2));
	p1.add(dbrp = new DbnRunPanel(app, this, progs));
	p1.add(ta = new TextArea(progs[0], 20, 40));

	//DbnParenBalancer pb = new DbnParenBalancer(this);
	DbnEditorListener listener = new DbnEditorListener(this);
	ta.addKeyListener(listener);
	ta.addFocusListener(listener);

	ta.addKeyListener(new DbnKeyListener(this));

	// has to be capitalized. argh. (nope, that's not it either)
	//ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
	// that was causing problems, we'll go with the jdk 1.0 style
	// jdk 1.0 style is to call it 'courier'
	ta.setFont(new Font("monospaced", Font.PLAIN, 12));
	//ta.setFont(new Font("dialog", Font.PLAIN, 24));
	add("Center", p1);
	p1 = new Panel();
	p1.setLayout(new GridLayout(1,2));
	add("South",p1);
	Panel p2 = new Panel();
	p2.setBackground(getPanelBgColor());
	p2.add(new Label("Command:"));
	p2.add(cmds = new Choice());
	p2.add(doitbut = new Button(" Do it "));

	// can always beautify code
	cmds.addItem(BEAUTIFY_ITEM);
	
	// don't add snapshot command if running locally
	if (!app.isLocal() && (app.getParameter("save_as") != null)) {
	    cmds.addItem(SNAPSHOT_ITEM);
	}
	if (app.isLocal()) {
	    // only show these when run as a file
	    cmds.addItem(OPEN_ITEM);
	    cmds.addItem(SAVE_ITEM);
	}
				
	p1.add(dbcp = new DbnControlPanel(app,this)); 
	p1.add(p2);
    }
	
    public void runpanelrefreshed()
    {
	if (run_mode!=null)
	    if (run_mode.equals("immediate")) {
		if (!getrunningp()) // kick it to start running
		    {
			//			dbrp.initiate();
		    }
	    }
    }
	
    public boolean action(Event evt, Object arg) {
    	if (evt.target == cmds) {
	    // could also do it here, i s'pose

    	} else if (evt.target == doitbut) {
	    String selected = cmds.getSelectedItem();
	    if (selected.equals(BEAUTIFY_ITEM)) doBeautify();
	    else if (selected.equals(SNAPSHOT_ITEM)) doSnapshot();
	    else if (selected.equals(SAVE_ITEM)) doSave();
	    else if (selected.equals(OPEN_ITEM)) doOpen();
    	}
        return true;
    }

    public void doSnapshot() {
	msg("Taking snapshot...");
	if (!io.doSnapshot(ta.getText(), dbrp.dbr.dbg.getPixels())) {
	    msg("Could not make snapshot.");
	} else {
	    msg("Done taking snapshot.");
	}
    }

    public void doSave() {
	msg("Saving file...");
	if (io.doLocalWrite(ta.getText())) {
	    msg("Done saving file.");
	} else {
	    msg("Did not write file.");
	}	    
    }

    public void doOpen() {
	String program = io.doLocalRead();
	if (program != null) {
	    ta.setText(program);
	}
    }

    public void idle(long t) {
	dbrp.idle(t);
    }

    public void doBeautify() {
	char program[] = ta.getText().toCharArray();
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


    public void reporterror(DbnException e) {
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
	    // System.out.println("st/end: "+st+"/"+end);
            ta.select(st, end);
            if (iexplorerp) {
		ta.invalidate();
		ta.repaint();
            }
	}
	dbcp.repaint(); // button should go back to 'play'
	//System.err.println(e.getMessage());
	msg("Problem: " + e.getMessage());
	//showStatus(e.getMessage());
    }

    public boolean getrunningp()
    {
	return dbrp.dbr.runningp();
    }
  
    public void setProgram(String s)
    {
	if (getrunningp()) terminate();
	ta.setText(s);
    }
    public void initiate()
    {
	// tell to start
	dbrp.setProgram(ta.getText());
	dbrp.initiate();
	dbrp.requestFocus();
	dbcp.initiated();
    }
	
    public void success()
    {
	dbcp.terminated();
	dbcp.msg("Done.");
    }
	
    public void msg(String s)
    {
	dbcp.msg(s);
    }
	
    public void terminate()
    {
	dbrp.terminate();
	//dbcp.msg("Stopped.");
	dbcp.msg("");
    }
	
    public void terminated()
    {
	// tell when done
	dbcp.terminated();
    }
	
    public void heartbeat()
    {
	// important to verify run
	dbcp.idle();
    }	
}


