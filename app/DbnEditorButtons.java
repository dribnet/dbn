#ifdef EDITOR


import java.awt.*;


public class DbnEditorButtons extends Panel {
    static final int BUTTON_COUNT = 7;
    static final int BUTTON_WIDTH = 24;
    static final int BUTTON_HEIGHT = 24;

    static final int PLAY = 0;
    static final int STOP = 1;
    static final int OPEN = 2;
    static final int SAVE = 3;
    static final int COURSEWARE = 4;
    static final int PRINT = 5;
    static final int BEAUTIFY = 6;

    DbnEditor editor;

    Image offscreen;
    int width, height;

    Image normal[];
    Image selected[];
    int state[];
    int where[]; // mapping indices to implementation



    public DbnEditorButtons(DbnEditor editor, boolean useOpenSave,
			    boolean useCourseware, boolean usePrint, 
			    boolean useBeautify) {
	Image image = DbnProperties.grabImage("buttons.gif");	
    }

    public Dimension preferredSize() {
	return new Dimension(200, 35);
    }
    
    public void paint(Graphics screen) {
	if (normal = null) {
	    normal = new Image[BUTTON_COUNT];
	    selected = new Image[BUTTON_COUNT];
	    state = new int[IMAGE_COUNT];
	    Graphics g;
	    for (int i = 0; i < BUTTON_COUNT; i++) {
		normal[i] = createImage(BUTTON_WIDTH, BUTTON_HEIGHT);
		g = normal[i].getGraphics();
		g.drawImage(buttons, -(i*BUTTON_WIDTH), 0, null);
		g = selected[i].getGraphics();
		g.drawImage(buttons, -(i*BUTTON_WIDTH), -BUTTON_HEIGHT, null);
	    }
	}
	Dimension size = size();
	if ((offscreen == null) || 
	    (size.width != width) || (size.height != height)) {
	    offscreen = createImage(size.width, size.height);
	    width = size.width;
	    height = size.height;	    
	}
	Graphics g = offscreen.getGraphics();
	g.setColor(getBackground());
	g.fillRect(0, 0, width, height);
	for (int i = 0; i < 2; i++) {
	    
	}
	screen.drawImage(
    }
}


#endif
