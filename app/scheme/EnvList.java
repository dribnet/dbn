/*

Jscheme interpreter by Tim Hickey, Hao Xu, and Lei Wang

Copyright (C) 1997  Timothy J. Hickey, Hao Xu, and Lei Wang

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

You can contact the authors at the following addresses:

 email:   tim@cs.brandeis.edu

 us mail: Professor T. Hickey, 
          Michtom School of Computer Science, Mailstop 18,
          Volen Center for Complex Systems
          Brandeis University
          Waltham MA 02254

*/

package jscheme;

import java.awt.*;
import java.net.URL;
import java.io.InputStream;
import java.util.*;

import DbnGraphics;
//import DbnGui;

class Associate implements Cloneable
{
    public Associate(String s, Object o)
    {  key = s; content = o; }
   
    public String toString()
    {
	if (content instanceof EnvList) 
	    return key + " : " + "ENVIRONMENT" + "\n";
	else
	    return key + " : " + content + "\n";
    }

    public Object clone()
    {
	try {
	    Associate binding = (Associate)super.clone();
	    return binding;
	}
	catch(CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    public String key;
    public Object content;
};


class Environment implements Cloneable
{
    public Environment() {
	t = new Vector();     
    }   

    public Object clone() {
	try {
	    Environment e = (Environment)super.clone();
	    e.t = (Vector)this.t.clone();
	    int size = e.t.size();
	    for(int i = size - 1; i >=0; i--)
		e.t.setElementAt(((Associate)e.t.elementAt(i)).clone(),i);
	    return e;
	}
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    /**
     * lookup the value bound to s in the environment.
     * If it finds a pair (s,val), then
     * it returns a ListNode(s,val). Otherwise, it
     * returns null.
     */
    public ListNode lookup(String s)
    { 
	int size = t.size();
	int i;
	Associate item;

	// if key -> null, we can't distinguish them   
	for(i = size -1; i >=0 ; i--) 
	    {
		item = (Associate)t.elementAt(i);
		if(item.key.equals(s)) 
		    return new ListNode(s,item.content);
	    }    
   
	return null;
    }
      
      
    public void addEnv(String s, Object content)
    {
	t.addElement(new Associate(s,content));    //content cannot be null
    }
       

    /**
     * This removes the string s and its binding c 
     * from the environment.
     * It returns the cons cell (s.o) where o
     * is the old value bound to s. If s does not
     * appear in the environment, it returns null.
     */
    public ListNode remove(String s)
    {
	int size = t.size();
	int i;
	Associate item;
   
	// if key -> null, we can't distinguish them   
	for(i = size -1; i >=0 ; i--) 
	    {
		item = (Associate)t.elementAt(i);
		if(item.key.equals(s)) {
		    t.removeElementAt(i);
		    return new ListNode(s,item.content);
		}   
	    }    

	return null;
    }
   
    /**
     * This rebinds the string s to the value c.
     * It returns the cons cell (s.o) where o
     * is the old value bound to s. If s does not
     * appear in the environment, it returns null.
     */
    public ListNode setEnv(String s, Object c)
    {
	int size = t.size();
	int i;
	Associate item;
   
	// if key -> null, we can't distinguish them   
	for(i = size -1; i >=0 ; i--) 
	    { 
		item = (Associate)t.elementAt(i);
		if(item.key.equals(s)) {
		    Object oc = item.content;
		    item.content = c;
		    return new ListNode(s,oc);
		}   
	    }    
   
	return null;
    }
   
    public String toString() {
	return t.toString();
    }
    

    public static int argLength(Object args) {
	int len = 0;
	Object L = args;
	if (L==null) return 0;

	while (L instanceof ListNode) {
	    L = ((ListNode) L).cdr();
	    len ++;
	}
	if (L != null) {
	    len = -1;
	}

	return len;
    }

    public static String[] classNames(Object args) {
	if (args == null) return null;
	int len = argLength(args);
	if (len==-1) return null;

	String[] names = new String[len];

	for(int i = 0; i < len; i++) {
	    Object obj = ((ListNode)args).car();
	    if (obj != null)
		names[i] = (obj.getClass()).getName();
	    else
		names[i] = "null";
	    args = ((ListNode) args).cdr();
	}
	return names;
    }

    public static Object[] argList(Object args) {
	if (args == null) return null;
	int len = argLength(args);
	if (len==-1) return null;
	Object[] argval = new Object[len];

	for(int i = 0; i < len; i++) {
	    argval[i] = ((ListNode)args).car();
	    args = ((ListNode) args).cdr();
	}
	return argval;
    }


    private Vector t;
};


public class EnvList extends ListNode implements Cloneable
{
    //DbnGraphics dbnGraphics;
    public static MemWin appletwin = null;
    
    String bootstrapCode = 
	"(seq \n" +
	"(define (dbngraphics) (constructor \"DbnGraphics\"))\n" +
	"(define (dbnline d x y w h) (instance_method d \"line\" x y w h))\n" +
	"(define (dbnfillrect d x y w h) (instance_method d \"fillrect\" x y w h))\n" +
	"(define (dbndrawrect d x y w h) (instance_method d \"drawrect\" x y w h))\n" +
	"(define (dbnfield d x y w h f) (instance_method d \"field\" x y w h f))\n" +
	"(define (dbnpen d x) (instance_method d \"pen\" x))\n" +
	"(define (dbnrefresh d) (instance_method d\"refresh\"))\n" +
	"(define (dbnext d x y) (instance_method d \"ext\" x y))\n" +
	"(define (dbnpaper d x) (instance_method d \"paper\" x))\n" +
	"(define (dbngetpix d x y) (instance_method d \"getpix\" x y))\n" +
	"(define (dbnsetpix d x y v) (instance_method d \"setpix\" x y v))\n" +
	"(define dbg (dbngraphics))\n" +
	"(define (ext x y) (dbnext dbg x y))\n" +
	"(define (line x y w h) (dbnline dbg x y w h))\n" +
	"(define (fillrect x y w h) (dbnfillrect dbg x y w h))\n" +
	"(define (drawrect x y w h) (dbndrawrect dbg x y w h))\n" +
	"(define (field x y w h v) (dbnfield dbg x y w h v))\n" +
	"(define (getpix x y) (dbngetpix dbg x y))\n" +
	"(define (setpix x y v) (dbnsetpix dbg x y v))\n" +
	"(define (pen x) (dbnpen dbg x))\n" +
	"(define (paper x) (dbnpaper dbg x))\n" +
	"(define (refresh) (dbnrefresh dbg))\n" +
	"(define (mapcar f L) (if (null? L) () (cons (f (car L)) (mapcar f (cdr L)))))\n" +
	
	/*    
	      "(define (window w h t) (constructor \"jscheme.EasyWin\" w h t))\n" +
	      "(define (button t) (constructor \"java.awt.Button\" t))\n" +
	      "(define (label t) (constructor \"java.awt.Label\" t))\n" +
	      "(define (textfield t size) (constructor \"java.awt.TextField\" t size))\n" +
	      "(define (textarea t rows cols) (constructor \"java.awt.TextArea\" t rows cols))\n" +
	      "(define (add obj win) (instance_method win \"add\" obj))\n" +
	      "(define (addCallback obj callback win) (instance_method win \"addCallback\" obj callback))\n" +
	      "(define (validate win) (instance_method win \"validate\"))\n" +
	      "(define (refresh win) (instance_method win \"refresh\"))\n" +
	      "(define (hide win) (instance_method win \"hide\"))\n" +
	      "(define (show win) (instance_method win \"show\"))\n" +
	      "(define (clear win) (instance_method win \"clear\"))\n" +
	*/  
	"(define (sin x) (class_method \"java.lang.Math.sin\" x))\n" +
	"(define (cos x) (class_method \"java.lang.Math.cos\" x))\n" +
	"(define (sqrt x) (class_method \"java.lang.Math.sqrt\" x))\n" +
	"(define (random) (class_method \"java.lang.Math.random\"))\n" +
	"(define (round x) (class_method \"java.lang.Math.round\" x))\n" +
	/*
	  "(define (drawLine win a b c d) (instance_method win \"drawLine\" a b c d))\n" +
	  "(define (drawRect win a b c d) (instance_method win \"drawRect\" a b c d))\n" +
	  "(define (drawOval win a b c d) (instance_method win \"drawOval\" a b c d))\n" +
	*/
	"(define #t (= 0 0))\n"+
	"(define #f (= 0 1))\n"+
	"(define true (= 0 0))\n"+
	"(define false (= 0 1))\n"+
	"(define nil '())"+
	"(define (null? L) (eq L ()))\n"+
	"(define (getClass x) (instance_method x \"getClass\"))\n"+
	"(define (toString x) (instance_method x \"toString\"))\n"+
	/*
	  "(define (turtle X Y W) (constructor \"jscheme.Turtle\" X Y W))\n"+
	  "(define (turn A Sam) (instance_method Sam \"turn\" A))\n"+
	  "(define (turnto A Sam) (instance_method Sam \"turnto\" A))\n"+
	  "(define (go A Sam) (instance_method Sam \"go\" A))\n"+
	  "(define (goto A B Sam) (instance_method Sam \"goto\" A B))\n"+
	  "(define (penup Sam) (instance_method Sam \"penup\")) \n"+
	  "(define (pendown Sam) (instance_method Sam \"pendown\"))\n"+
	*/
	"(define (equal x y) (define (equalpair a b)\n"+
	"    (if (equal (car a) (car b)) (equal (cdr a) (cdr b)) false))\n" +
	"  (if (pair? x) (if (pair? y) (equalpair x y) false) \n"+
	"  (if (eq x y) true (if (= x y) true false))))\n" +
	"(define (evalURL U) \n" +
	"       (eval (cons 'seq (parse (readURL U))) (env)))\n" + 
	")\n";


    //initial environment
    public EnvList() {
	super();
	//this.dbnGraphics = dbnGraphics;
	Environment e = new Environment();
	
	e.addEnv("debugging",new Boolean(false));
	e.addEnv("tracing",new Boolean(false));
	e.addEnv("appletwin",appletwin);
	
	// Jscheme interface procedures: 3
	e.addEnv("constructor", new reflectionFunc("new"));
	e.addEnv("instance_method", new reflectionFunc("applyto"));
	e.addEnv("class_method", new reflectionFunc("apply"));

	e.addEnv("+",new jschemeFunc(jschemeFunc.ARITH,"+"));
	e.addEnv("-",new jschemeFunc(jschemeFunc.ARITH,"-"));
	e.addEnv("*",new jschemeFunc(jschemeFunc.ARITH,"*"));
	e.addEnv("/",new jschemeFunc(jschemeFunc.ARITH,"/"));
	e.addEnv("=",new jschemeFunc(jschemeFunc.ARITH,"="));
	e.addEnv("<",new jschemeFunc(jschemeFunc.ARITH,"<"));
	e.addEnv(">",new jschemeFunc(jschemeFunc.ARITH,">"));
	e.addEnv(">=",new jschemeFunc(jschemeFunc.ARITH,">="));
	e.addEnv("<=",new jschemeFunc(jschemeFunc.ARITH,"<="));
	e.addEnv("!=",new jschemeFunc(jschemeFunc.ARITH,"!="));

	e.addEnv("car",new jschemeFunc(jschemeFunc.CAR));
	e.addEnv("cdr",new jschemeFunc(jschemeFunc.CDR));
	e.addEnv("cons",new jschemeFunc(jschemeFunc.CONS));
	e.addEnv("list",new jschemeFunc(jschemeFunc.LIST));
	e.addEnv("seq",new jschemeFunc(jschemeFunc.SEQ));
	e.addEnv("eq",new jschemeFunc(jschemeFunc.EQ));

	e.addEnv("not",new jschemeFunc(jschemeFunc.NOT));
	e.addEnv("or",new jschemeFunc(jschemeFunc.OR));
	e.addEnv("and",new jschemeFunc(jschemeFunc.AND));

	e.addEnv("print",new jschemeFunc(jschemeFunc.PRINT));
	e.addEnv("explode",new jschemeFunc(jschemeFunc.EXPLODE));
	e.addEnv("implode",new jschemeFunc(jschemeFunc.IMPLODE));
	e.addEnv("length",new jschemeFunc(jschemeFunc.LENGTH));
	e.addEnv("eval",new jschemeFunc(jschemeFunc.EVAL));
	e.addEnv("parse",new jschemeFunc(jschemeFunc.PARSE));
	e.addEnv("read",new jschemeFunc(jschemeFunc.READ));
	e.addEnv("readtolist",new jschemeFunc(jschemeFunc.READTOLIST));
	e.addEnv("readstring",new jschemeFunc(jschemeFunc.READSTRING));
	e.addEnv("write",new jschemeFunc(jschemeFunc.WRITE));
	e.addEnv("writeatend",new jschemeFunc(jschemeFunc.WRITEATEND));
	e.addEnv("pair?",new jschemeFunc(jschemeFunc.PAIRp));
	e.addEnv("row",new jschemeFunc(jschemeFunc.ROW));
	e.addEnv("col",new jschemeFunc(jschemeFunc.COL));
	e.addEnv("grid",new jschemeFunc(jschemeFunc.GRID));
	e.addEnv("box",new jschemeFunc(jschemeFunc.BOX));
	e.addEnv("choice",new jschemeFunc(jschemeFunc.CHOICE));
	e.addEnv("cat",new jschemeFunc(jschemeFunc.CAT));
	e.addEnv("readURL",new jschemeFunc(jschemeFunc.READURL));

	head = e;
	
	Evaluator ev = new Evaluator();
	ev.eval(Parser.readString(bootstrapCode),this);
    }
    
    
    public EnvList(Environment e, EnvList envList) {
	super(e,envList);
    }
    
    
    public EnvList extendEnv(ListNode ps, ListNode as) {
	Environment e = new Environment();
	while(ps != null) {
	    e.addEnv((String)ps.car(),as.car());
	    ps = (ListNode)ps.cdr();
	    as = (ListNode)as.cdr();
	}   
	return new EnvList(e,this);    
    }


    public Object lookup(String s) {
	return doLookup(s,this); 
    }
   
    private Object doLookup(String s, EnvList EList) { 
	if (EList instanceof ListNode) {
	    ListNode r = ((Environment)EList.car()).lookup(s);
	    if (r != null) return r.cdr();
	    else return doLookup(s,(EnvList)EList.cdr());
	} else {
	    ThrowError.error("Variable "+s+" is undefined. Look for a spelling error!");
	    throw new RuntimeException("Variable "+s+" is undefined. Look for a spelling error!");
	}
    }                       


    public void addEnv(String s, Object content) {
	//this is really to add to the car of EList
	((Environment)car()).addEnv(s,content);
    }


    public Object remove(String s) {
	return doRemove(s,this);
    } 

   
    private Object doRemove(String s, EnvList EList)
    { 
	if (EList instanceof ListNode)
	    {
		ListNode r = ((Environment)EList.car()).remove(s);
		if(r != null) return r.cdr();
		else return doRemove(s,(EnvList)EList.cdr());
	    }
	else 
	    throw new RuntimeException(
				       "Variable "+s+" is undefined. It can't be undefined.");
    }             
   
    public Object setEnv(String s,Object c)
    {
	return doSetEnv(s,c,this);
    }
   
    private Object doSetEnv(String s, Object c, EnvList EList)
    {  
	if (EList instanceof ListNode)
	    {
		ListNode r = ((Environment)EList.car()).setEnv(s,c);
		if(r != null) return r.cdr();
		else return doSetEnv(s,c,(EnvList)EList.cdr());
	    }
	else 
	    throw new RuntimeException(
				       "Variable "+s+" is undefined. It's value can't be set.");
    }   
      
    public Object clone()
    {
	try {
	    EnvList e = (EnvList)super.clone();
	    e.head = (this.head == null)? null : ((Environment)this.head).clone();
	    e.tail = (this.tail == null)? null : ((EnvList)this.tail).clone();
	    return e;
	}
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }
};   
          

interface Function
{
    public abstract Object apply(Object obj);
    public abstract Object apply(Object obj,EnvList env);

};
           

abstract class FunctionObj implements Function
{
    public Object apply(Object obj,EnvList env) {
	return apply(obj);
    }


};
           

/**
 * This class implements the reflection operator for Java 1.0.2
 * Its creates functions whose format is one of the following
 * (constructor ClassName Arg1 Arg2 .... Argn).
 * (instance_method Obj MethodName Arg1 Arg2 .... Argn).
 * (class_method Obj MethodName Arg1 Arg2 .... Argn).
 * but it is only applicable to a few Classes/Methods as described in the
 * Jscheme 1.1 manual.
 */
class reflectionFunc extends FunctionObj
{
    static final int UNKNOWN = 0;
    static final int NEW     = 1;
    static final int APPLYTO = 2;
    static final int APPLY   = 3;
    int mode = 0;

    public reflectionFunc(String type) {
	if (type.equals("new")) {
	    mode = NEW;
	}else if (type.equals("applyto")) {
	    mode = APPLYTO;
	}else if (type.equals("apply")) {
	    mode = APPLY;
	}else {
	    mode = UNKNOWN;
	}
    }

    public String toString() {
	if (mode==NEW)
	    return "reflectionFunc(constructor)";
	else if (mode == APPLYTO)
	    return "reflectionFunc(instance_method)";
	else if (mode == APPLY)
	    return "reflectionFunc(class_method)";
	else return super.toString();
    }

    public Object apply(Object obj) {
	String[] names = Environment.classNames(obj);
	Object[]  args = Environment.argList(obj);

	switch (mode) {
	case NEW: return newApply(names,args); 
	case APPLYTO: return applytoApply(names,args);
	case APPLY: return applyApply(names,args);
	default: return null;
	}
    }


    public Object newApply(String names[], Object args[]) {
	String className = (String) args[0];

	if (className.equals("jscheme.EasyWin")) {
	    int width = ((Number)args[1]).intValue();
	    int height = ((Number)args[2]).intValue();
	    String title = args[3].toString();
	    EasyWin f = new EasyWin(width,height,title);
	    f.addNotify();
	    return f;
	}
	if (className.equals("DbnGraphics")) {
	    //return dbnGraphics;
	    //return new DbnGraphics();
	    return DbnGraphics.getCurrentGraphics();
	}
	else if (className.equals("jscheme.RowPanel")) {
	    return new RowPanel();
	}
	else if (className.equals("jscheme.ColPanel")) {
	    return new ColPanel();
	}
	else if (className.equals("java.awt.Button")) {
	    return new Button( (args[1]==null)?"":args[1].toString());
	}
	else if (className.equals("java.awt.Label")) {
	    return new Label( (args[1]==null)?"":args[1].toString());
	}
	else if (className.equals("java.awt.TextField")) {
	    return new TextField( (args[1]==null)?"":args[1].toString(),
				  ((Number) args[2]).intValue());
	}
	else if (className.equals("java.awt.TextArea")) {
	    return new TextArea( (args[1]==null)?"":args[1].toString(),
				 ((Number) args[2]).intValue(),
				 ((Number) args[3]).intValue());
	}
	//else if (className.equals("jscheme.Turtle")) {
	//return new Turtle(((Number) args[1]).doubleValue(), ((Number) args[2]).doubleValue(), (MemWin) args[3]);
	//}
	else if (className.equals("java.awt.Choice")) {
	    Choice c = new Choice();
	    //        args = Environment.argList(args[1]);
	    //        for (int i=0; i < args.length; i++) {
	    //           c.addItem(args[i].toString());
	    //	}
	    return c;
	}
	else {
	    ThrowError.error("Don't know how to create a "+className);
	}

	return null;

    }


    public Object applytoApply(String names[],Object args[]) {
	String methodName = (String) args[1];
	Object obj = args[0];
       
	// System.out.println("method is :"+methodName);
	if (methodName.equals("line")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).line(((Number)args[2]).intValue(),((Number)args[3]).intValue(),((Number)args[4]).intValue(),((Number)args[5]).intValue());
	    }
	} else if (methodName.equals("pen")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).pen(((Number)args[2]).intValue());
	    }
	} else if (methodName.equals("paper")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).paper(((Number)args[2]).intValue());
	    }
	} else if (methodName.equals("ext")) {
	    if (obj instanceof DbnGraphics) {
		// why not call idle now as io gets critical
		//(DbnGui.getCurrentDbnGui()).idle(System.currentTimeMillis());
		try {
		    return new Integer(((DbnGraphics)obj).getConnector(args[2].toString(),((Number)args[3]).intValue()));	 	
		} catch (Exception e) {
		    System.err.println("can't do a get on "+(args[2].toString()));
		    return new Integer(0);
		}
	    }
	   
	} else if (methodName.equals("refresh")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).refresh();
	    }
	    /*
	      } else if (methodName.equals("fillrect")) {
	      if (obj instanceof DbnGraphics) {
	      ((DbnGraphics)obj).fillrect(((Number)args[2]).intValue(),((Number)args[3]).intValue(),((Number)args[4]).intValue(),((Number)args[5]).intValue());
	      }
	      } else if (methodName.equals("ext")) {
	      if (obj instanceof DbnGraphics) {
	      return new Integer(((DbnGraphics)obj).ext(args[2].toString(),((Number)args[3]).intValue()));
	      }
	      } else if (methodName.equals("drawrect")) {
	      if (obj instanceof DbnGraphics) {
	      ((DbnGraphics)obj).drawrect(((Number)args[2]).intValue(),((Number)args[3]).intValue(),((Number)args[4]).intValue(),((Number)args[5]).intValue());
	      }
	    */
	} else if (methodName.equals("field")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).field(((Number)args[2]).intValue(),((Number)args[3]).intValue(),((Number)args[4]).intValue(),((Number)args[5]).intValue(),((Number)args[6]).intValue());
	    }
	} else if (methodName.equals("setpix")) {
	    if (obj instanceof DbnGraphics) {
		((DbnGraphics)obj).setPixel(((Number)args[2]).intValue(),((Number)args[3]).intValue(),((Number)args[4]).intValue());
	    }
	} else if (methodName.equals("getpix")) {
	    if (obj instanceof DbnGraphics) {
		return new Integer(((DbnGraphics)obj).getPixel(((Number)args[2]).intValue(),((Number)args[3]).intValue()));
	    }

	    }/*
	  else if (methodName.equals("add")) {
	  ((MemWin)obj).add((Component) args[2]);
	  }
	  else if (methodName.equals("addCallback")) {
	  ((MemWin)obj).addCallback(
	  (Component) args[2],
	  (Closure) args[3]);
      
	  }
	  else if (methodName.equals("validate")) {
	  ((Component)obj).validate();
	  }
	  else if (methodName.equals("hide")) {
	  ((Component)obj).hide();
	  }
	  else if (methodName.equals("show")) {
	  ((Component)obj).show();
	  }
	  else if (methodName.equals("clear")) {
	  ((MemWin)obj).clear();
	  }
	  else if (methodName.equals("getText")) {
	  if (obj instanceof java.awt.Label) {
	  return ((Label) obj).getText();
	  }
	  else if (obj instanceof java.awt.TextComponent) {
	  return ((TextComponent) obj).getText();
	  }
	  else
	  return null;
	  }
	  else if (methodName.equals("setText")) {
	  if (obj instanceof java.awt.Label) {
	  ((Label) obj).setText(args[2].toString());
	  }
	  else if (obj instanceof java.awt.TextComponent) {
	  ((TextComponent) obj).setText(
	  args[2].toString());
	  }
	  return null;
	  }
	  else if (methodName.equals("getLabel")) {
	  if (obj instanceof java.awt.Button) 
	  return ((Button) obj).getLabel();
	  else
	  return null;
	  }
	  else if (methodName.equals("setLabel")) {
	  if (obj instanceof java.awt.Button) 
	  ((Button) obj).setLabel(args[2].toString());
	  return null;
	  }
	  else if (methodName.equals("getSelectedItem")) {
	  if (obj instanceof java.awt.Choice) 
	  return ((Choice) obj).getSelectedItem();
	  else
	  return null;
	  }
	  else if (methodName.equals("setBorder")) {
	  if (obj instanceof RowPanel)
	  ((RowPanel) obj).set_draw_border((Boolean) args[2]);
	  else if (obj instanceof ColPanel)
	  ((ColPanel) obj).set_draw_border((Boolean) args[2]);
	  return null;
	  }
	  else if (methodName.equals("drawLine")) {
	  int a1 = ((Number)args[2]).intValue();
	  int a2 = ((Number)args[3]).intValue();
	  int a3 = ((Number)args[4]).intValue();
	  int a4 = ((Number)args[5]).intValue();
	  ((MemWin)obj).drawLine(a1,a2,a3,a4);
	  }
	  else if (methodName.equals("drawRect")) {
	  int a1 = ((Number)args[2]).intValue();
	  int a2 = ((Number)args[3]).intValue();
	  int a3 = ((Number)args[4]).intValue();
	  int a4 = ((Number)args[5]).intValue();
	  ((MemWin)obj).drawRect(a1,a2,a3,a4);
	  }
	  else if (methodName.equals("drawOval")) {
	  int a1 = ((Number)args[2]).intValue();
	  int a2 = ((Number)args[3]).intValue();
	  int a3 = ((Number)args[4]).intValue();
	  int a4 = ((Number)args[5]).intValue();
	  ((MemWin)obj).drawOval(a1,a2,a3,a4);
	  }
	*/
	/*
	  else if (methodName.equals("go")) {
	  ((Turtle)obj).go(((Number)args[2]).doubleValue());
	  }
	  else if (methodName.equals("goto")) {
	  ((Turtle)obj).go_to(((Number)args[2]).doubleValue(),((Number)args[3]).doubleValue());
	  }
	  else if (methodName.equals("turn")) {
	  ((Turtle)obj).turn(((Number)args[2]).doubleValue());
	  }
	  else if (methodName.equals("turnto")) {
	  ((Turtle)obj).turnto(((Number)args[2]).doubleValue());
	  }
	  else if (methodName.equals("penup")) {
	  ((Turtle)obj).penup();
	  }
	  else if (methodName.equals("pendown")) {
	  ((Turtle)obj).pendown();
	  }
	*/
	else if (methodName.equals("getClass")) {
	    return obj.getClass();
	}
	else if (methodName.equals("toString")) {
	    return obj.toString();
	}
	else {
	    ThrowError.error("Don't know how to apply "+methodName +" to "+ obj);
	}
	return null;

    }


    public Object applyApply(String names[],Object args[]) {
	String methodName = (String) args[0];
	
	if (methodName.equals("java.lang.Math.sin")) {
	    return new Double(java.lang.Math.sin(((Number) args[1]).doubleValue()));
	} else if (methodName.equals("java.lang.Math.cos")) {
	    return new Double(java.lang.Math.cos(((Number) args[1]).doubleValue()));
	} else if (methodName.equals("java.lang.Math.sqrt")) {
	    return new Double(java.lang.Math.sqrt(((Number) args[1]).doubleValue()));
	} else if (methodName.equals("java.lang.Math.round")) {
	    return new Integer((int) java.lang.Math.round(((Number) args[1]).doubleValue()));
	} else if (methodName.equals("java.lang.Math.random")) {
	    return new Double(java.lang.Math.random());
	} else {
	    ThrowError.error("Don't know how to apply "+methodName);
	}
	return null;
    }
}





class jschemeFunc extends FunctionObj
{
    static final int UNKNOWN    = 0;
    static final int ARITH      = 1;
    static final int CAR        = 2;
    static final int CDR        = 3;
    static final int CONS       = 4;
    static final int EQ         = 5;
    static final int SEQ        = 6;
    static final int PRINT      = 7;
    static final int EVAL       = 8;
    static final int PARSE      = 9;
    static final int READ       = 10;
    static final int WRITE      = 11;
    static final int PAIRp      = 12;
    static final int LIST       = 13;
    static final int ROW        = 14;
    static final int COL        = 15;
    static final int CHOICE     = 16;
    static final int GRID       = 17;
    static final int BOX        = 18;
    static final int CAT        = 19;
    static final int READSTRING = 20;
    static final int READURL    = 21;
    static final int WRITEATEND = 22;
    static final int READTOLIST = 23;
    static final int EXPLODE = 24; // added by JM
    static final int LENGTH = 25; // added by JM
    static final int IMPLODE = 26; // added by JM
    static final int NOT = 27; // added by JM
    static final int OR = 28; // added by JM
    static final int AND = 29; // added by JM
	
    int mode = 0;
    String op="0";

    public jschemeFunc(int mode) {
	this.mode = mode;
    }

    public jschemeFunc(int mode,String operator) {
	this.mode = mode; this.op = operator;
    }

    public String toString() {
	return "jschemeFunc("+mode+" , "+ op+")";
    }

    public Object apply(Object obj) {
	String[] names = Environment.classNames(obj);
	Object[]  args = Environment.argList(obj);
	//    System.out.println("jschemeApply: mode= "+mode+" op= "+op+" obj = "+obj);

	switch (mode) {
	case  ARITH:  return arithApply(op,names,args); 
	case   CAR:   return carApply(names,args);
	case   CDR:   return cdrApply(names,args);
	case   CONS:  return consApply(names,args);
	case   EQ:    return eqApply(names,args);
	case   SEQ:   return seqApply(names,args);
	case   PRINT: return printApply(names,args);
	case   EXPLODE: return explodeApply(names,args);
	case   IMPLODE: return implodeApply(names,args);
	case   AND: return andApply(names,args);
	case   OR: return orApply(names,args);
	case   NOT: return notApply(names,args);
	case   LENGTH: return lengthApply(names,args);
	case   EVAL:  return evalApply(names,args);
	case   PARSE: return parseApply(names,args);
	case   READ: return readApply(names,args);
	case   WRITE: return writeApply(names,args);
	case   PAIRp: return pairpApply(names,args);
	case   LIST:  return listApply(names,args);
	case   ROW:  return rowApply(names,args);
	case   COL:  return colApply(names,args);
	case   CHOICE:  return choiceApply(names,args);
	case   GRID:  return gridApply(names,args);
	case   BOX:  return boxApply(names,args);
	case   CAT:  return catApply(names,args);
	case   READSTRING: return readstringApply(names,args);
	case   READURL:  return readURLApply(names,args);
	case   WRITEATEND: return writeatendApply(names,args);
	case   READTOLIST: return readtolistApply(names,args);

	default: return null;
	}
    }


    // I want to make this work for multiple args for * and +

    public Object arithApply(String op,String[] names, Object args[]) {
	if (args.length == 0) {
	    if (op.equals("+")||op.equals("-"))
		return new Integer(0);
	    else if (op.equals("*")||op.equals("/"))
		return new Integer(1);
	    else 
		return null;
	}
	else if (args.length == 1) {
	    return args[0];
	}
	else if (args.length == 2) {
	    //      System.out.println(" name[0]= "+names[0]+ " name[1]= "+names[1]);
	    //      System.out.println(" args[0]= "+args[0]+ " args[1]= "+args[1]);

	    if (names[0].equals("java.lang.Integer") && names[1].equals("java.lang.Integer")) {
		int x = ((Number) args[0]).intValue();
		int y = ((Number) args[1]).intValue();
		if (op.equals("+"))
		    return new Integer(x+y);
		else if (op.equals("-"))
		    return new Integer(x-y);
		else if (op.equals("*"))
		    return new Integer(x*y);
		else if (op.equals("/"))
		    return new Integer(x/y);
		else if (op.equals("="))
		    return new Boolean(x==y);
		else if (op.equals("!="))
		    return new Boolean(x!=y);
		else if (op.equals("<"))
		    return new Boolean(x<y);
		else if (op.equals("<="))
		    return new Boolean(x<=y);
		else if (op.equals(">"))
		    return new Boolean(x>y);
		else if (op.equals(">="))
		    return new Boolean(x>=y);
		else return null;
	    } else  if ((names[0].equals("java.lang.Double") || names[0].equals("java.lang.Integer")) 
			&& 
			(names[1].equals("java.lang.Double") || names[1].equals("java.lang.Integer"))
			) {
		double x = ((Number) args[0]).doubleValue();
		double y = ((Number) args[1]).doubleValue();
		if (op.equals("+"))
		    return new Double(x+y);
		else if (op.equals("-"))
		    return new Double(x-y);
		else if (op.equals("*"))
		    return new Double(x*y);
		else if (op.equals("/"))
		    return new Double(x/y);
		else if (op.equals("="))
		    return new Boolean(x==y);
		else if (op.equals("!="))
		    return new Boolean(x!=y);
		else if (op.equals("<"))
		    return new Boolean(x<y);
		else if (op.equals("<="))
		    return new Boolean(x<=y);
		else if (op.equals(">"))
		    return new Boolean(x>y);
		else if (op.equals(">="))
		    return new Boolean(x>=y);
		else return null;
	    } else 
		if (op.equals("=")) return new Boolean(false);
		else if (op.equals("!=")) return new Boolean(true);
		else return null;
	}
	else return null;
    }

    public int objlen(Object L)
    {
	int len = 0;
	if (L==null) return 0;

	while (L instanceof ListNode) {
	    L = ((ListNode) L).cdr();
	    len ++;
	}
	if (L != null) {
	    len = 1;
	}
	return len;
    }

    public Object objimplode(Object L)
    {
	String s = "";
	if (L==null) return null;

	while (L instanceof ListNode) {
	    s+=((ListNode) L).car().toString();
	    L = ((ListNode) L).cdr();
	}
	if (L != null) {
	    return null;
	}
	return s;
    }
	
    public Object implodeApply(String[] names, Object args[]) {
  
	return objimplode(args[0]);
    }

    public Object lengthApply(String[] names, Object args[]) {
  
	int len = 0;
	for(int i =0;i<args.length;i++)
	    len+=objlen(args[i]);
	return new Integer(len);
    }
  
    public Object carApply(String[] names, Object args[]) {
	return ((ListNode) args[0]).car();
    }

    
    public static final Boolean FALSE = new Boolean(false);
    public static final Boolean TRUE = new Boolean(true);

    public Object notApply(String[] names, Object args[]) {
	if (args[0]==null || args[0].equals(FALSE)) return TRUE;
	else return FALSE;
    }

    public Object andApply(String[] names, Object args[]) {
	boolean v=true;

	for(int i =0;i<args.length;i++)
	    if (args[i]==null || args[i].equals(FALSE)) {
		v=false;
		break;
	    }
	return v?TRUE:FALSE;
    }

    public Object orApply(String[] names, Object args[]) {
	boolean v=false;

	for(int i =0;i<args.length;i++)
	    if (args[i]!=null) {
		v=true;
		break;
	    }
	return v?TRUE:FALSE;
    }

    
    public Object cdrApply(String[] names, Object args[]) {
	return ((ListNode) args[0]).cdr();
    }

    public Object consApply(String[] names, Object args[]) {
	return ListNode.cons(args[0],args[1]);
    }

    public Object listApply(String[] names, Object args[]) {
	ListNode XX = null;

	for(int i = args.length-1; i >= 0; i--) 
	    XX = ListNode.cons(args[i],XX);

	return XX;
    }

    public Object explodeApply(String[] names, Object args[]) {
	ListNode XX = null;
	int i,j,k;
	for(i=args.length-1;i>=0;i--) {
	    String s = args[i].toString();
	    for(j=s.length()-1;j>=0;j--) {
		XX=ListNode.cons(new String(""+s.charAt(j)),XX);
	    }
	}
	//    System.out.println("PRINT: "+ s);
	return XX;
    }


    public Object eqApply(String[] names, Object args[]) {
	if (args[0]==null) 
	    return new Boolean(args[1]==null); 
	else
	    return new Boolean(args[0].equals(args[1]));
    }


    public Object seqApply(String[] names, Object args[]) {
	if (args.length == 0) return null;
	else return args[args.length-1];
    }

    public Object printApply(String[] names, Object args[]) {
	String s;
	if (args[0]==null) {
	    s="PRINT: null";
	}
	else {
	    s = "PRINT: "+args[0].toString();
	}
	//(DbnGui.getCurrentDbnGui()).msg(s);
	System.err.println(s);
	return null;
    }

    public Object evalApply(String[] names, Object args[]) {
	return
	    (new Evaluator()).eval(args[0],(EnvList) args[1]);       

    }

    public Object parseApply(String[] names, Object args[]) {
	return Parser.readString((String) args[0].toString());
    }

    public Object pairpApply(String[] names, Object args[]) {
	return new Boolean((args[0] instanceof ListNode));
    }

    public Object readApply(String[] names, Object args[]) {
	Component c = (Component) args[0];
	String text;
	if (c instanceof java.awt.Label)
	    text = ((Label) c).getText();
	else
	    if (c instanceof java.awt.Button)
		text = ((Button) c).getLabel();
	    else
		if (c instanceof java.awt.TextComponent)
		    text = ((TextComponent) c).getText();
		else
		    if (c instanceof java.awt.Choice)
			text = ((Choice) c).getSelectedItem();
		    else
			text = "ERROR";

	if (text==null) return null;
	else return Parser.readString(text);
    }

    public Object readstringApply(String[] names, Object args[]) {
	Component c = (Component) args[0];
	String text;
	if (c instanceof java.awt.Label)
	    text = ((Label) c).getText();
	else
	    if (c instanceof java.awt.Button)
		text = ((Button) c).getLabel();
	    else
		if (c instanceof java.awt.TextComponent)
		    text = ((TextComponent) c).getText();
		else
		    if (c instanceof java.awt.Choice)
			text = ((Choice) c).getSelectedItem();
		    else
			text = "ERROR";

	return text;
    }

    public Object readtolistApply(String[] names, Object args[]) {
	Component c = (Component) args[0];
	String text;
	if (c instanceof java.awt.Label)
	    text = ((Label) c).getText();
	else
	    if (c instanceof java.awt.Button)
		text = ((Button) c).getLabel();
	    else
		if (c instanceof java.awt.TextComponent)
		    text = ((TextComponent) c).getText();
		else
		    if (c instanceof java.awt.Choice)
			text = ((Choice) c).getSelectedItem();
		    else
			text = "ERROR";

	if (text==null) return null;
	else return Parser.readStringToList(text);
    }

    public Object writeApply(String[] names, Object args[]) {
	Component c = (Component) args[0];
	String text = "";
	if (args[1]!=null) 
	    text = args[1].toString();

	if (c instanceof java.awt.Label)
	    ((Label) c).setText(text);
	else
	    if (c instanceof java.awt.Button)
		((Button) c).setLabel(text);
	    else
		if (c instanceof java.awt.TextComponent)
		    ((TextComponent) c).setText(text);

	return null;
    }


    public Object writeatendApply(String[] names, Object args[]) {
	Component c = (Component) args[0];
	String text;
	if (args[1] == null) text="()";
	else text = args[1].toString();
	if (c instanceof java.awt.TextArea)
	    ((TextArea) c).appendText("\n"+text);

	return null;
    }


    public Object boxApply(String[] names, Object args[]) {
	RowPanel rr = new RowPanel();

	for(int i = 0; i < args.length; i++) 
	    rr.add((Component)args[i]);

	rr.set_draw_border(new Boolean(true));
	return rr;
    }


    public Object rowApply(String[] names, Object args[]) {
	RowPanel rr = new RowPanel();

	for(int i = 0; i < args.length; i++) 
	    rr.add((Component)args[i]);

	return rr;
    }


    public Object colApply(String[] names, Object args[]) {
	ColPanel cc = new ColPanel();

	for(int i = 0; i < args.length; i++) 
	    cc.add((Component)args[i]);

	return cc;

    }

    public Object gridApply(String[] names, Object args[]) {
	int rows = ((Number)args[0]).intValue();
	int cols = ((Number)args[1]).intValue();
	GridPanel rr = new GridPanel(rows,cols);

	for(int i = 2; i < args.length; i++) 
	    rr.add((Component)args[i]);

	return rr;
    }


    public Object choiceApply(String[] names, Object args[]) {
	Choice cc = new Choice();

	for(int i = 0; i < args.length; i++) 
	    cc.addItem(args[i].toString());

	return cc;

    }

    public Object catApply(String[] names, Object args[]) {
	String ss = "";

	for(int i = 0; i < args.length; i++) 
	    ss = ss + args[i].toString();

	return ss;

    }

    /* has one argument which is the URL of a file.
       It opens a connections to that file and reads it
       into a string variable! I can then parse and
       evaluate it later. 
    */
    public Object readURLApply(String[] names, Object args[]) {
	char[] cc = new char[8192];
	int count=0;
	String ss = "";
	String URLstring = args[0].toString();
	try {
	    URL url = new URL(URLstring);
    
	    InputStream in = url.openStream();
	    if (in!=null) {
		int avail = in.available();
		if (avail > 8192) cc = new char[avail];
		else avail = 8192;
		for(int c = in.read(); ((c>0)&&(c < avail)); c=in.read()) {
		    cc[count++]=(char) c;
		}
	    }
	    return new String(cc,0,count);
	} catch (Exception e) {
	    ThrowError.error("Problem loading from URL "+URLstring+"\n  "+e+"\n");
	}
    
	return null;

    }





}

class RowPanel extends Panel {
    public static GridBagLayout gridbag = new GridBagLayout();
    public static GridBagConstraints cgridbag = new GridBagConstraints();
    public boolean draw_border = false;
   
    static {
	cgridbag.gridx = GridBagConstraints.RELATIVE;
	cgridbag.gridy = 0;
	cgridbag.gridwidth  = 1;
	cgridbag.gridheight = 1;
	cgridbag.fill  = GridBagConstraints.NONE;
	cgridbag.ipadx = 1;
	cgridbag.ipady = 1;
	cgridbag.insets = new Insets(5,5,5,5);
	cgridbag.anchor= GridBagConstraints.WEST;
	cgridbag.weightx  = 0.0;
	cgridbag.weighty  = 0.0;
    }

    public RowPanel() {
	super();
	this.setLayout(gridbag);
    }

    public void set_draw_border(Boolean b) {
	draw_border = b.booleanValue();

    }

    public void paint(Graphics g) {
	if (draw_border)
	    g.drawRect(1,1,this.size().width-2,this.size().height-2);
	super.paintComponents(g);
    }

    public Component add(Component c) {
	gridbag.setConstraints(c,cgridbag);
	return super.add(c);
    }

}


class ColPanel extends Panel {
    public static GridBagLayout gridbag = new GridBagLayout();
    public static GridBagConstraints cgridbag = new GridBagConstraints();
    public boolean draw_border = false;
   
    static {
	cgridbag.gridx = 0;
	cgridbag.gridy = GridBagConstraints.RELATIVE;
	cgridbag.gridwidth  = 1;
	cgridbag.gridheight = 1;
	cgridbag.fill  = GridBagConstraints.NONE;
	cgridbag.ipadx = 1;
	cgridbag.ipady = 1;
	cgridbag.insets = new Insets(5,5,5,5);
	cgridbag.anchor= GridBagConstraints.WEST;
	cgridbag.weightx  = 0.0;
	cgridbag.weighty  = 0.0;
    }

    public ColPanel() {
	super();
	this.setLayout(gridbag);
    }

    public void set_draw_border(Boolean b) {
	draw_border = b.booleanValue();
    }

    public void paint(Graphics g) {
	if (draw_border) 
	    g.drawRect(1,1,this.size().width-2,this.size().height-2);
	super.paintComponents(g);
    }

    public Component add(Component c) {
	gridbag.setConstraints(c,cgridbag);
	return super.add(c);
    }

}


class GridPanel extends Panel {

    public GridPanel(int rows, int cols) {
	super();
	this.setLayout(new GridLayout(rows,cols, 2 , 2));
    }

    public void paint(Graphics g) {
	super.paintComponents(g);
    }

    public Component add(Component c) {
	return super.add(c);
    }
}


