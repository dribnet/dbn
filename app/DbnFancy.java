#ifdef FANCY


import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class DbnFancy extends DbnApplet implements ActionListener
{
    // when using dbn as an application instead of an applet
    // (this will always be done with jdk 1.1 or higher)
    static public void main(String args[]) {
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

	//sapp = app;
	//frame.setMenuBar(setupMenu(sapp));
	new DbnFancy(frame, app);
    }


    static final String problems[] = {
	"7A", "7B", "7C", "8A", "8B", "8C"
    };
    String currentProblem = problems[0];

    static final String people[] = {
	"akilian", "ben", "cameron", "carsonr", "casey",
	"dana", "darkmoon", "dc", "elise", "golan", "hannes",
	"james", "jared", "kelly", "ppk", "shyam", "tom"
    };
    String currentPerson = people[0];

    DbnApplet applet;

    public DbnFancy(Frame frame, DbnApplet applet) {
	this.applet = applet;

	MenuBar mb = new MenuBar();
	Menu menu; 
	MenuItem mi;

	menu = new Menu("Problem");
	for (int i = 0; i < problems.length; i++) {
	    mi = new MenuItem(problems[i]);
	    menu.add(mi);
	}
	menu.addActionListener(this);
	mb.add(menu);

	menu = new Menu("People");
	for (int i = 0; i < people.length; i++) {
	    mi = new MenuItem(people[i]);
	    menu.add(mi);
	}
	menu.addActionListener(this);
	mb.add(menu);

	frame.setMenuBar(mb);
    }
    
    public void actionPerformed(ActionEvent event) {
	String cmd = event.getActionCommand();
	//System.out.println(cmd);
	if (Character.isDigit(cmd.charAt(0))) {
	    currentProblem = cmd;
	    //System.out.println("Problem is now " + currentProblem);
	} else {
	    currentPerson = cmd;
	}
	String course = "\\\\hub\\mas\\acg\\web\\docroot\\projects\\dbncourseware\\cgi-bin\\courseware\\courses\\mas961";
	String title = course + File.separator + 
	    currentPerson + File.separator + currentProblem + ".dbn";
	File file = new File(title);
	if (file.exists()) {
	    applet.setProgram(title);
	} else {
	    System.out.println(title + " not found.");
	}
    }
}


#endif

    //static String rootPath;
	
	/*
	MenuItem mi;
	File rootDir = new File("mas961");
	try {
	    rootPath = rootDir.getCanonicalPath();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	//System.out.println("path = " + rootPath);
	*/
