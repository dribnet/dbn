#ifdef FANCY


import java.awt.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class DbnFancy extends DbnApplication implements ItemListener {    
  String location;
  String currentProblem;
  String currentPerson;

  Vector titles;
  Vector designations;
  Vector descriptions;
  Vector people;

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


  public DbnFancy() throws IOException {
    MenuBar mb = new MenuBar();

    titles = new Vector();
    designations = new Vector();
    descriptions = new Vector();
    people = new Vector();
    readCourse(titles, designations, descriptions, people);

    problemMenu = new Menu("Problem");
    Enumeration e = designations.elements();
    while (e.hasMoreElements()) {
      String designation = (String) e.nextElement();
      CheckboxMenuItem mi = new CheckboxMenuItem(designation);
      mi.addItemListener(this);
      problemMenu.add(mi);
    }
    mb.add(problemMenu);

    peopleMenu = new Menu("People");
    e = people.elements();
    while (e.hasMoreElements()) {
      String person = (String) e.nextElement();
      CheckboxMenuItem mi = new CheckboxMenuItem(person);
      mi.addItemListener(this);
      peopleMenu.add(mi);
    }
    mb.add(peopleMenu);

    frame.setMenuBar(mb);
  }


  static public void readCourse(Vector titles, Vector designations, 
				Vector descriptions, Vector people) 
    throws IOException {
    String location = DbnApplet.get("courseware.location");
    if (location == null) {
      System.err.println("must set courseware.location in dbn.properties");
      System.exit(1);
    }
    if (!location.endsWith("/")) {
      location += "/";
    }

    String problems = DbnApplet.applet.readFile(location + "problems.txt");
    if (problems == null) {
      System.err.println(location + "problems.txt not found");
      System.exit(1);
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
	//titleOffsets.addElement(new Integer(problemOffset));
	break;
      case 'p':
	if (number != problemNumber) {
	  problemLetter = 0;
	  problemNumber = number;
	}
	String designation = "" + problemNumber + (char)('A' + problemLetter);
	designations.addElement(designation);
	descriptions.addElement(content);
	problemLetter++;
	problemOffset++;
	break;
      case 'm':
	break;
      }
    }

    String persons = DbnApplet.applet.readFile(location + "people.txt");
    reader = new BufferedReader(new StringReader(persons));
    line = null;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0) continue;
      people.addElement(line);
    }
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
      environment.message(currentProblem + " - " + 
			  currentPerson);
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
}


#endif
