#ifdef JDK11

import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class DbnApplication extends DbnApplet
{
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
	//app.applet = app;
	//app.application = true;
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
}

#endif
