#ifdef FANCY


import java.awt.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class DbnFancy extends DbnApplication implements ItemListener {    
  //DbnApplet applet;
  //Frame frame;
  //TextArea description;

  //MultiLineLabel description;

  String location;
  String currentProblem;
  String currentPerson;

  Vector titles = new Vector();
  Vector titleOffsets = new Vector();
  Vector designations = new Vector();
  Vector descriptions = new Vector();

  Menu problemMenu;
  Menu peopleMenu;


  static MultiLineLabel description;

  static public Component makeDescription() {
    description = 
      new MultiLineLabel("", new Font("SansSerif", Font.PLAIN, 12),
			 475, 10, 15, MultiLineLabel.LEFT);
    description.setBackground(new Color(0xCC, 0xCC, 0xCC));
    description.setForeground(new Color(0x33, 0x33, 0x33));
    return description;
  }

  /*TextArea description, DbnApplet applet*/
  public DbnFancy() throws IOException {
    //frame.hide();

    //this.frame = frame;
    //this.description = description;
    //this.applet = applet;

    //description = new TextArea("", 5, 40);
    /*
    description = 
      new MultiLineLabel("", new Font("SansSerif", Font.PLAIN, 12),
			 650, 30, 30, MultiLineLabel.LEFT);
    description.setBackground(new Color(0xCC, 0xCC, 0xCC));
    description.setForeground(new Color(0x33, 0x33, 0x33));
    */
    //frame.add("South", description);
    //frame.pack();

    //try {
    //new DbnFancy(frame, textarea, app);
    //} catch (IOException e) {
    //e.printStackTrace();
    //System.exit(1);
    //}

    MenuBar mb = new MenuBar();
    
    location = get("courseware.location");
    //System.out.println("location is " + location);
    if (location == null) {
      System.err.println("must set courseware.location in dbn.properties");
      System.exit(0);
    }
    if (!location.endsWith("/")) {
      location += "/";
    }

    problemMenu = new Menu("Problem");

    String problems = readFile(location + "problems.txt");
    if (problems == null) {
      System.err.println("no problems found");
    }
    int problemOffset = 0;
    int problemNumber = 0;
    int problemLetter = 0;

    BufferedReader reader = new BufferedReader(new StringReader(problems));
    String line = null;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0) continue;
      if (line.charAt(0) != '<') continue;
      int closing = line.indexOf('>');
      int number = Integer.parseInt(line.substring(2, closing));
      String content = line.substring(closing + 1);

      switch (line.charAt(1)) {
      case 'h':
	titles.addElement(content);
	titleOffsets.addElement(new Integer(problemOffset));
	break;
      case 'p':
	if (number != problemNumber) {
	  problemLetter = 0;
	  problemNumber = number;
	}
	String designation = "" + problemNumber + (char)('A' + problemLetter);
	CheckboxMenuItem mi = new CheckboxMenuItem(designation);
	mi.addItemListener(this);
	problemMenu.add(mi);
	designations.addElement(designation);
	descriptions.addElement(content);
	problemLetter++;
	problemOffset++;
	break;
      case 'm':
	break;
      }
    }
    mb.add(problemMenu);

    peopleMenu = new Menu("People");
    String people = readFile(location + "people.txt");
    reader = new BufferedReader(new StringReader(people));
    line = null;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0) continue;
      CheckboxMenuItem mi = new CheckboxMenuItem(line);
      mi.addItemListener(this);
      peopleMenu.add(mi);
    }
    mb.add(peopleMenu);    

    frame.setMenuBar(mb);
  }


  public void itemStateChanged(ItemEvent event) {
    String cmd = event.getItem().toString();
    //System.out.println(cmd);
    if (Character.isDigit(cmd.charAt(0))) {
      currentProblem = cmd;
      reselect(problemMenu, cmd);

      int problemCount = descriptions.size();
      for (int i = 0; i < problemCount; i++) {
	if (((String)designations.elementAt(i)).equals(currentProblem)) {
	  description.setLabel((String)(descriptions.elementAt(i)));
	  frame.pack();
	  break;
	}
      }
    } else {
      currentPerson = cmd;
      reselect(peopleMenu, cmd);
    }
    if ((currentProblem == null) || 
	(currentPerson == null)) return;

    String filename = location + currentPerson + "/" + 
      currentProblem + ".dbn";
    File file = new File(filename);
    String theText = readFile(filename);
    if (theText != null) {
      frame.setTitle(currentProblem + " - " + currentPerson + " - DBN");
    } else {
      theText = "";
      environment.message(currentProblem + " for " + 
			  currentPerson + " not found.");
      frame.setTitle("DBN");
      // put up an error message inside the editor
    }
    ((DbnEditor)environment).textarea.setText(theText);
  }

  public void reselect(Menu menu, String which) {
    int itemCount = menu.getItemCount();
    for (int i = 0; i < itemCount; i++) {
      CheckboxMenuItem mi = (CheckboxMenuItem) menu.getItem(i);
      mi.setState(mi.getLabel().equals(which));
    }
  }


  static public void main(String args[]) {
    try {
      new DbnFancy().frame.show();
      
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  /*
    Frame frame = new Frame("DBN");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
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
    int width = app.getInteger("width", 600);
    int height = app.getInteger("height", 350);

    // ms jdk requires that BorderLayout is set explicitly
    frame.setLayout(new BorderLayout());
    frame.add("Center", app);
    app.init();
    Insets insets = frame.getInsets();
    frame.reshape(50, 50, width + insets.left + insets.right, 
		  height + insets.top + insets.bottom);
  */
}


#endif
