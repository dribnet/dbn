import java.awt.*;
import java.applet.Applet;
import java.util.*;
import java.lang.*;


public class DbnProcessorG3 extends DbnProcessor 
{
    ParserHandler ph= null;
	
    /* Default constructor will do fine, thank you */
    public DbnProcessorG3(DbnGui _gui, DbnGraphics _db, DbnApplet _app) {
	super(_gui, _db, _app);
    }	
    
    public void pleaseQuit() {
	if(ph!=null) ph.pleaseQuit();
    }
    
    public void process(String prog) throws Exception 
    {
	int i = 0;
	
	/* John says to call prep */
	prep();
	
	// this should go in the preprocessor -- it went
	//		prog = prog.toLowerCase() + "\n";
	prog = preprocess(prog);
	
	ph = new ParserHandler(this, prog);
	ph.go();
	//		pt.start();
	
	
		//
		// the <foo > is called "external data", that is why i have 'getext'		
		//
		// note that 'field' is a valid command for dbngraphics
		//
		/*
		for(;;) {
				// erase screen, set pen
				dbg.paper((int)(Math.random()*100));

				dbg.pen(100-getext("mouse",2));
				dbg.line(0,0,getext("time",4), getext("mouse",2));
				
				// throw a dbnexception for fun
				if (getext("mouse",3)==100) throw new DBNException("error at line 0",0);
								
				// test the key event
				for(i=1;i<27;i++) {
					dbg.line(i*4,0,getext("key",i),100);
				}
				
				// check the heartbeat, update keyboard/mouse/time events
				idle();

		}
		*/
	}
	
	// all dangerous stuff goes inside here (not inside innocent dbngraphics)
	
/* duh, all this stuff is inherited!

	public void hasext(String s) {
		return(exthash.containsKey(s));
	}	

	public void setext(String s, int n, int v) throws DBNException
	{
		int []vv = (int[])exthash.get(s);
		if (vv==null) {
			throw new DBNException("unknown external data "+s);
		}
		try{
			vv[n-1] = v;
		} catch (Exception e) {
			throw new DBNException("bounds problem with set "+s+"-->"+n);
		}
	}

	public int getext(String s, int n) throws DBNException
	{
		int []vv = (int[])exthash.get(s);
		if (vv==null) {
			throw new DBNException("unknown external data "+s);
		}
		try{
			return vv[n-1];
		} catch (Exception e) {
			throw new DBNException("bounds problem with get "+s+"-->"+n);
		}
	}
	*/
}
