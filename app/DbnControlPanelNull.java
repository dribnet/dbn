import java.awt.*;


public class DbnControlPanelNull extends Panel 
{
    DbnApplet app;
    DbnGui gui;

    public DbnControlPanelNull(DbnApplet app, DbnGui gui)
    {
	this.app = app;	
	this.gui = gui;
	setBackground(gui.getPanelBgColor());
    }

    public void msg(String s) { }
    public void initiated() { }
    public void terminated() { }
    public void idle() { }
}
