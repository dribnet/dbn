import java.awt.*;


public class DbnControlPanel extends DbnControlPanelNull {
    Label msgta;
    DbnRunButton dbrb;
	
    public DbnControlPanel(DbnApplet app, DbnGui gui)
    {
	super(app, gui);
	setLayout(new BorderLayout());
	add("West", dbrb = new DbnRunButton(gui));
	add("Center", msgta = new Label("Hello."));
    }
	
    public void msg(String s)
    {
	//System.err.println("setting message " + s);
	msgta.setText(s);
    }
	
    public void initiated()
    {
	msgta.setText("Running ...");
	dbrb.initiated();
    }
	
    public void terminated()
    {
	dbrb.terminated();
    }
	
    public void idle()
    {
	dbrb.idle();
    }
}
