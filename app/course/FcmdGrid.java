#ifdef EXHIBITION


import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class FcmdGrid extends DbnGrid {
  FcmdExhibitionApp fea;


  public FcmdGrid(DbnApplet app, String progs[], FcmdExhibitionApp fea) {
    super(app, progs);
    this.fea = fea;
  }

  public void mouseClicked(MouseEvent e) {
    Object source = e.getSource();

    if (gcurrent != -1) {
      if (source == graphics[gcurrent]) {
	System.out.println("(ignoring.. clicked the same 2)");
	return;
      } else {
	System.out.println("(new selection.. killing2)");
	terminate();
	fea.swap(-1);
      }
    }
    gcurrent = -1;
    for (int i = 0; i < gcount; i++) {
      if (source == graphics[i]) {
	System.out.println("2 setting gcurrent to " + i);
	gcurrent = i;
	fea.swap(gcurrent);
	break;
      }
    }
    System.out.println("  gcurrent = " + gcurrent);
    if (gcurrent == -1) {
      System.out.println("  source is " + source);
    }
    if (gcurrent != -1) {
      runner = new DbnRunner(programs[gcurrent], graphics[gcurrent], this);
      runner.start();
      graphics[gcurrent].enable();
      graphics[gcurrent].setCurrentDbnGraphics();
      runners.addElement(runner);
      System.out.println("  starting " + gcurrent);
    }
    return;
  }
}


#endif

