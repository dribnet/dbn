import java_cup.runtime.Symbol;
import java.util.Vector;

class ParserHandler {
    /* this guy handles everything the DrawSpace used to do */
    static boolean do_debug_parse = false;
    public parser parser_obj = new parser();
    Symbol parse_tree;
    DbnProcessor ds;
    String ps;
    
    public ParserHandler(DbnProcessor ds, String ps) {
	this.ds = ds;
	this.ps = ps;
	
	parser_obj = new parser();
	parse_tree = null;
	parser_obj.setParseString(ps);
	parser_obj.setParserHandler(this);
	//try {parse_tree = parser_obj.debug_parse();}
    }
    
    public void go() 
	throws DbnException {
	try {parse_tree = parser_obj.parse();}
	catch (Exception e) {
	    if(e instanceof DbnException) throw (DbnException)e;
	    else {
		System.out.println("Unexpected parsing error: " +e);
		e.printStackTrace();
	    }
	}
    }
    
    public String doLoad(String className) {
	/*
	  Class cl;
	  Object o;
	  DBNconnector dc;
	  
	  try {
	  cl = Class.forName(className);
			o = cl.newInstance();
			if(o instanceof DBNconnector) {
				dc = (DBNconnector)o;
				dc.ready(this);
			}
		}
		catch(Exception e) {
			System.out.println("problem loading " + className + ": " + e);
		}
*/
		return "paper 25\nline 10 10 50 90\n";
    }
	
    public void setAlias() {
	ds.dbg.setAntiAlias(0);
    }
    
    public void setAntialias() {
	ds.dbg.setAntiAlias(100);
    }
    
    public void beginforever()
    {
  	ds.dbg.beginforever();
    }
    
    public void endforever()
    {
  	ds.dbg.endforever();
    }
    
    public void beginrepeat()
    {
	//  	System.out.println("begin repeat");
  	ds.dbg.beginrepeat();
    }
    
    public void endrepeat()
    {
	//  	System.out.println("end repeat");
  	ds.dbg.endrepeat();
    }
    
    public void idle() {
	ds.idle();
    }
    
    public void setPaper(int val) {
	ds.dbg.paper(val);
    }
    
    public void setPen(int val) {
	ds.dbg.pen(val);
    }
    
    public void drawLine(int ox1, int oy1, int ox2, int oy2) {
	ds.dbg.line(ox1, oy1, ox2, oy2);
    }

    public void doField(int x, int y, int w, int h, int c) {
	ds.dbg.field(x, y, w, h, c);
    }

    public void doPause(int p) {
	try {
	    Thread.sleep(p*10);
	}
	catch(Exception stupid) {}
    }

    public void setDot(int x, int y, int val) {
	ds.dbg.setDot(x,y,val);
    }

    public int getDot(int x, int y) {
	return ds.dbg.getDot(x,y);
    }
	
    public void setConnector(String conName, int channel, int conValue) 
	throws DbnException {
		
	/* tom's debug printing 
	   System.out.print("setConnector called on: " + conName + "[");
	   System.out.println("" + channel + "], " + conValue); */
		
	ds.setext(conName, channel, conValue);

	//		if(!connectors.containsKey(conName)) return;
	//		DBNconnector dc = (DBNconnector)connectors.get(conName);
	//		dc.set(numbers, conValue);
    }
	
    public boolean hasConnector(String conName) {
	//		return(connectors.containsKey(conName));
	return ds.hasext(conName);
    }

    public int getConnector(String conName, Vector numbers) 
	throws DbnException {
	Integer iger;
		
	/* tom's debug printing
	   System.out.print("getConnector called on: " + conName + "[");
	   for(int i=0;i<numbers.size();i++) {
	   System.out.print(" " + ((Integer)numbers.elementAt(i)).intValue());
	   }
	   System.out.println("]"); */

	iger = (Integer)numbers.elementAt(0);
	return ds.getext(conName, iger.intValue());
	/*
	  if(!connectors.containsKey(conName)) return 0;
	  DBNconnector dc = (DBNconnector)connectors.get(conName);
	  return dc.get(numbers);
	*/
	//		return 50;
    }

    public void pleaseQuit() {
	if(parser_obj != null) parser_obj.quitOnNextSymbol();
    }
	
    public void syntaxError(String errString, int lineNum, int charNum) 
	throws DbnException {
	if(parser_obj != null) parser_obj.quitOnNextSymbol();
	//System.err.println("First syntax error: " + errString);
	if(lineNum != -1) throw new DbnException("syntax error, line " + (lineNum+1), lineNum);
	else throw new DbnException("syntax error:" + errString);
    }
}


