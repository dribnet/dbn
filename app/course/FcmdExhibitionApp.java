#ifdef EXHIBITION


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


// look into adding into a threadgroup to ensure killing threads
// or marking threads as 'dead' using a boolean

// don't try to run null applets in grid mode (duH!)


public class FcmdExhibitionApp extends DbnApplication
implements KeyListener {
  //static final Color backgroundColor = 
  //DbnApplet.getColor("bg_color", Color.gray);
  static Color backgroundColor = new Color(0x33, 0x33, 0x66);
  static Color textColor = new Color(0, 0, 0);
  //static final Color backgroundColor = Color.green;

  String gridPeople[];
  String gridProblem[];
  String gridName[];
  String gridDescription[];
  String gridPrograms[];

  FcmdLabel label;


  static public void main(String args[]) {
    new FcmdExhibitionApp().frame.show();
  }

  public FcmdExhibitionApp() {
    backgroundColor = DbnApplet.getColor("bg_color", Color.gray);
    textColor = DbnApplet.getColor("text_color", Color.gray);

    Vector titles = new Vector();
    Vector designations = new Vector();
    Vector descriptions = new Vector();
    Vector people = new Vector();
    try {
      DbnFancy.readCourse(titles, designations, descriptions, people);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    int count = getInteger("exhibition.count", 0);
    gridPeople = new String[count];
    gridProblem = new String[count];
    gridName = new String[count];
    gridDescription = new String[count];
    gridPrograms = new String[count];

    String location = DbnApplet.get("courseware.location");
    if (!location.endsWith("/")) {  location += "/";  }

    for (int i = 0; i < count; i++) {
      int ii = i + 1;
      gridPeople[i] = get("exhibition.person." + ii);
      gridProblem[i] = get("exhibition.problem." + ii);
      gridName[i] = get("exhibition.name." + ii);
      gridPrograms[i] = readFile(location + gridPeople[i] + 
				 "/" + gridProblem[i] + ".dbn");
      
      int index = 0;
      Enumeration e = designations.elements();
      while (e.hasMoreElements()) {
	String problem = (String) e.nextElement();
	if (problem.equals(gridProblem[i])) {
	  gridDescription[i] = (String) descriptions.elementAt(index);
	  break;
	}
	index++; 
      }
    }
    setBackground(backgroundColor);
    Panel panel = new Panel();
    panel.setLayout(new BorderLayout());
    panel.setBackground(backgroundColor);

    DbnGrid grid = new FcmdGrid(applet, gridPrograms, this);
    grid.setBackground(backgroundColor);
    panel.add("West", grid);

    label = new FcmdLabel(gridDescription[0], 
			  new Font("SansSerif", Font.PLAIN, 11),
			  250, 10, 10, FcmdLabel.LEFT);
    label.setBackground(backgroundColor);
    //label.setBackground(Color.orange);
    label.setForeground(textColor);
    label.setTitle(gridName[0]);
    panel.add("East", label);

    frame.setLayout(new BorderLayout());
    frame.add(panel);
    frame.pack();

    /*
    Insets insets = frame.getInsets();
    frame.setSize(800 + insets.left + insets.right, 
		  600 + insets.top + insets.bottom + 60);
    frame.setLocation(-insets.left, -insets.top);
    */
    frame.setSize(800, 600);
    frame.setLocation(0, 0);

    //labels.setSize(300, 100);
    addKeyListener(this);
  }


  public void swap(int index) {
    if (index == -1) {
      label.setLabel("");
      label.setTitle("");
      return;
    }
    label.setTitle(gridName[index]);
    label.setLabel(gridProblem[index] + " - " + 
		   gridDescription[index]);
  }


  public void keyPressed(KeyEvent e) {
    keyTyped(e);
  }

  public void keyReleased(KeyEvent e) {
    //graphics.keyUp(null, (int)e.getKeyChar());
  }

  public void keyTyped(KeyEvent e) {
    if (e.getKeyCode() == e.VK_ESCAPE) {
      System.exit(0);
    }
    //graphics.keyDown(null, (int)e.getKeyChar());
    //graphics.keyUp(null, (int)e.getKeyChar());
  }
}


#endif
