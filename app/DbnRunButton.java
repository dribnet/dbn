import java.awt.*;


public class DbnRunButton extends Panel {
    DbnGui gui;
	
    int tc = 0;
    Rectangle cb;
    int cbw, cbh;
	
    int[] playxs, playys;
    int[] stopxs, stopys;
    int playn, stopn;
	
    int marg = 4;
	
    public DbnRunButton(DbnGui gui) {
	super();
	this.gui = gui;
    }
	
    public void idle() {
	Graphics g = this.getGraphics();
		
	tc++; tc=tc%2;
	if (tc == 0) {
	    g.setColor(Color.white);
	} else {
	    g.setColor(Color.black);
	}
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
    }
	
    public void initiated() {
	//repaint();
    }

    public void terminated()
    {
	repaint();
    }
	
    public boolean mouseEnter(Event ev, int x, int y) {
	Graphics g = this.getGraphics();
	g.setColor(Color.black);
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
	return true;
    }
	
    public boolean mouseExit(Event ev, int x, int y) {
	Graphics g = this.getGraphics();
	g.setColor(Color.gray);
	g.drawRect(marg,marg,cbh-marg*2,cbh-marg*2);
	return true;
    }
	
    public void drawmain(Graphics g, boolean runningp) {
	g.setColor(gui.getPanelBgColor()); 
	g.fillRect(0, 0, cbw, cbh);
		
	g.setColor(Color.darkGray);
		
	g.fillRect(marg,marg,cbh-marg*2,cbh-marg*2);
		
	if (runningp) 
	    g.setColor(Color.orange);
	else
	    g.setColor(Color.green);
	if (runningp) {
	    g.fillPolygon(stopxs,stopys,stopn);	
	} else {
	    g.fillPolygon(playxs,playys,playn);	
	}
    }
	
    public boolean mouseDown(Event ev, int x, int y) {
	if (!gui.getrunningp()) {
	    drawmain(this.getGraphics(),true);
	    gui.initiate();
	} else {
	    drawmain(this.getGraphics(),false);
	    gui.terminate();
	}
	return true;
    }
	
    public void paint(Graphics g)
    {	
	Rectangle r = bounds();
	//g.setColor(Color.white);
	//g.fillRect(r.x, r.y, r.width, r.height);

	if (r.width > 1) {
	    cb = r; 
	    cbw = cb.width; 
	    cbh = cb.height; 
	    
	    if (playxs == null) {
		playxs = new int[3];
		playys = new int[3];
		stopxs = new int[4];
		stopys = new int[4]; playn = 3; stopn = 4;
		playxs[0] = marg*3+1;
		playys[0] = marg*3;
		playxs[1] = cbh-marg*3+1;
		playys[1] = cbh/2;
		playxs[2] = marg*3+1;
		playys[2] = cbh-marg*3;
		
		stopxs[0] = marg*3;
		stopys[0] = marg*3+1;
		stopxs[1] = cbh-marg*3+1;
		stopys[1] = marg*3+1;
		stopxs[2] = cbh-marg*3+1;
		stopys[2] = cbh-marg*3+1;
		stopxs[3] = marg*3;
		stopys[3] = cbh-marg*3+1;
	    }
	}
	drawmain(g, gui.getrunningp());
			
	g.setColor(Color.gray);
	g.drawRect(marg, marg, cbh-marg*2, cbh-marg*2);
    }

    public Dimension preferredSize()
    {
        return new Dimension(36, 24);
    }	
}
