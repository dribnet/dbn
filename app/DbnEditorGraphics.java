#ifdef EDITOR


import java.awt.*;


public DbnEditorGraphics extends DbnGraphics {

    Image screenImage;
    int gx, gy;

    public DbnEditorGraphics(int width, int height) {
	super(width, height);
    }

    public Dimension preferredSize() {
	return new Dimension(width1 + 100, height1 + 100);
    }

    public void paint(Graphics screen) {
	if (screenImage == null) {
	    screenImage = createImage(width1 + 100, height1 + 100);
	    gx = gy = 50;

	    Graphics g = screenImage.getGraphics();

	    // make the background a color
	    g.setColor(bgColor);
	    g.fillRect(0, 0, size.width, size.height);
	    
	    // draw the tick marks
	    
	    // draw a dark frame around the runner
	    g.setColor(Color.black);
	    g.drawRect(gx-1, gy-1, width+2, height+2);
	}

	// draw the actual runner
	screenImage.drawImage(image, gx, gy, null);

	// blit to screen
	screen.drawImage(screenImage, 0, 0, null);
    }

    public boolean updateMouse(int x, int y) {
	mouse[0] = x - gx;
	mouse[1] = height1 - (y - gy);
	return true;
    }
}


#endif
