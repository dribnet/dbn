#ifdef JDK11


import java.awt.*;
import java.awt.event.*;


public class DbnKeyListener extends KeyAdapter {
    DbnGui gui;

    public DbnKeyListener(DbnGui gui) {
	this.gui = gui;
    }

    public void keyPressed(KeyEvent event) {
	switch ((int) event.getKeyChar()) {
	case  2: gui.doBeautify(); break;  // control b for beautify
	case 15: gui.doOpen(); break;  // control o for open
	case 16: gui.doPrint(); break;  // control p for print
	case 18: gui.initiate(); break;  // control r for run
	case 19: gui.doSave(); break;  // control s for save
	case 20: gui.doSnapshot(); break;  // control t for snapshot
	    // escape only works from the runpanel, because that's
	    // who's getting all the key events while running
	    //case 27: gui.terminate(); break;  // escape to stop	
	}
    }
}


#endif
