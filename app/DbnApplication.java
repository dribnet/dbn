#ifdef JDK11

import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


// when using dbn as an application instead of an applet
// (this will always be done with jdk 1.1 or higher)

public class DbnApplication extends DbnApplet {
  Frame frame;

  static public void main(String args[]) {
    new DbnApplication().frame.show();
    //frame.show();
  }

  public DbnApplication() {
    frame = new Frame("DBN");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	System.exit(0);
      }
    });
    /*
    Dimension screen = 
      Toolkit.getDefaultToolkit().getScreenSize();
    frame.reshape(screen.width+10, 10, 100, 200);
    frame.show();
    */
    //frame.pack();

    // why is a new dbnapplet created? *this* is a dbnapplet!
    
    /*
    DbnApplet app = new DbnApplet();
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
    frame.pack();
    frame.show();
    */

    properties = new Properties();
    try {
      properties.load(new FileInputStream("dbn.properties"));
    } catch (Exception e1) {
      try {
	properties.load(new FileInputStream("lib/dbn.properties"));
      } catch (Exception e) {
	System.err.println("Error reading dbn.properties");
	e.printStackTrace();
	System.exit(1);
      }
    }
    int width = getInteger("width", 600);
    int height = getInteger("height", 350);
    // ms jdk requires that BorderLayout is set explicitly
    frame.setLayout(new BorderLayout());
    frame.add("Center", this);
    init();
    Insets insets = frame.getInsets();
    frame.reshape(50, 50, width + insets.left + insets.right, 
		  height + insets.top + insets.bottom);
    frame.pack();
    //frame.show();
  }
}

#endif
