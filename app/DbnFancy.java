#ifdef FANCY


import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class DbnFancy extends DbnApplet implements ItemListener /*ActionListener*/ {    
  DbnApplet applet;
  Frame frame;

  String location;
  String currentProblem;
  String currentPerson;

  Vector titles = new Vector();
  Vector titleOffsets = new Vector();
  Vector designations = new Vector();
  Vector descriptions = new Vector();

  Menu problemMenu;
  Menu peopleMenu;

  //String titles[];
  //int titleOffsets[];

  //String indices[];
  //String descriptions[][];


  public DbnFancy(Frame frame, DbnApplet applet) throws IOException {
    this.applet = applet;
    this.frame = frame;

    MenuBar mb = new MenuBar();
    //Menu menu; 
    //MenuItem mi;
    
    location = applet.get("courseware.location");
    if (location == null) {
      System.err.println("must set courseware.location in dbn.properties");
      System.exit(0);
    }
    if (!location.endsWith("/")) {
      location += "/";
    }

    problemMenu = new Menu("Problem");

    String problems = applet.readFile(location + "problems.txt");
    //Vector titleVector = new Vector();
    //int currentIndex = -1;
    //Vector problemVector = new Vector();
    int problemOffset = 0;
    //int problemCount = 0;
    int problemNumber = 0;
    int problemLetter = 0;

    BufferedReader reader = new BufferedReader(new StringReader(problems));
    String line = null;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0) continue;
      if (line.charAt(0) != '<') continue;
      int closing = line.indexOf('>');
      int number = Integer.parseInt(line.substring(2, closing));
      //System.out.println("parsing #" + number);
      String content = line.substring(closing);

      //if (index != currentIndex) {
      //if (currentIndex != -1) {
	  // copy what's in there so far into 
      //}
      //}
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
    //problemMenu.addActionListener(this);
    mb.add(problemMenu);

    peopleMenu = new Menu("People");
    String people = applet.readFile(location + "people.txt");
    reader = new BufferedReader(new StringReader(people));
    line = null;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0) continue;
      CheckboxMenuItem mi = new CheckboxMenuItem(line);
      mi.addItemListener(this);
      peopleMenu.add(mi);
    }
    //peopleMenu.addActionListener(this);
    //peopleMenu.addItemListener(this);
    mb.add(peopleMenu);    

    /*
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
    */
    frame.setMenuBar(mb);
  }

  /*
  static final String problems[] = {
    //"7A", "7B", "7C", "8A", "8B", "8C"
    //"2A", "2B", "2C"
    //"5A", "5B", "5C"
    "7A", "7B"
  };

  
  static final String people[] = { 
    "a_goel", "anne", "awchau", "b_piper", "beck", "bernie", "btking",
    "drrich", "escudero", "girit", "govango", "jarnold", "jaylee",
    "jeeeee5", "jeppig", "jic", "jleblanc", "jpatten", "jroth",
    "jshafer", "leeh", "lfsulliv", "lintina", "megan", "mlpriebe",
    "mpalmer", "mshah", "mstmegs", "mstring", "neptune", "nhsia",
    "okhan", "rstreit", "sini", "sloan2", "tony"

    //"akilian", "ben", "cameron", "carsonr", "casey",
    //"dana", "darkmoon", "dc", "elise", "golan", "hannes",
    //"james", "jared", "kelly", "ppk", "shyam", "tom"
  };
  */

  //public void actionPerformed(ActionEvent event) {
  public void itemStateChanged(ItemEvent event) {
    //String cmd = event.getActionCommand();
    String cmd = event.getItem().toString();
    System.out.println(cmd);
    if (Character.isDigit(cmd.charAt(0))) {
      currentProblem = cmd;
      reselect(problemMenu, cmd);
    } else {
      currentPerson = cmd;
      reselect(peopleMenu, cmd);
    }
    if ((currentProblem == null) || 
	(currentPerson == null)) return;
    /*
    String course = "courses\\fcmd";
    //String course = "\\\\hub\\mas\\acg\\web\\docroot\\projects\\dbncourseware\\cgi-bin\\courseware\\courses\\mas961";
    String title = course + File.separator + 
      currentPerson + File.separator + currentProblem + ".dbn";
    */
    String filename = location + currentPerson + "/" + 
      currentProblem + ".dbn";
    //System.out.println(filename);
    File file = new File(filename);
    String theText = applet.readFile(filename);
    if (theText != null) {
      frame.setTitle(currentProblem + " - " + currentPerson + " - DBN");
    } else {
      theText = "";
      System.out.println(filename + " not found.");
      frame.setTitle("DBN");
      // put up an error message inside the editor
    }
    //String theText = file.exists() ?  : "";
    ((DbnEditor)applet.environment).textarea.setText(theText);
    //System.out.println(title + " not found.");
  }

  public void reselect(Menu menu, String which) {
    int itemCount = menu.getItemCount();
    for (int i = 0; i < itemCount; i++) {
      CheckboxMenuItem mi = (CheckboxMenuItem) menu.getItem(i);
      //if (mi.getLabel().
      //System.out.println(which + " ? " + mi.getLabel());
      mi.setState(mi.getLabel().equals(which));
    }
  }

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
    //int width = Integer.parseInt(app.properties.getProperty("width"));
    //int height = Integer.parseInt(app.properties.getProperty("height"));
    int width = app.getInteger("width", 600);
    int height = app.getInteger("height", 350);

    // ms jdk requires that BorderLayout is set explicitly
    frame.setLayout(new BorderLayout());
    frame.add("Center", app);
    app.init();
    Insets insets = frame.getInsets();
    frame.reshape(50, 50, width + insets.left + insets.right, 
		  height + insets.top + insets.bottom);
    frame.pack();

    //sapp = app;
    //frame.setMenuBar(setupMenu(sapp));
    try {
      new DbnFancy(frame, app);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
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
