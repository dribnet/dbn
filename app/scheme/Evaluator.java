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


import java.util.*;
import java.lang.String;

/**
 * this class implements the inner JScheme eval/apply loop
 */
public class Evaluator extends Thread {   
  public static boolean tracing = false;
  public static final boolean old_debugging = false;
  public static boolean debugging = false;
  public static final Boolean FALSE = new Boolean(false);
  public static final Boolean TRUE = new Boolean(true);
  public static boolean kill = false;
  private int depth = 0;
  public Closure global_thunk = null;
  
  public void set_thunk(Closure clos) {
    global_thunk = clos;
  }
  
  public void run() {
    //    setPriority(Thread.MIN_PRIORITY);
    if (global_thunk != null)
      applyClosure(global_thunk,null);
    System.out.println("Callback has been completed");
  }
  
  public boolean truth(Object x) { 
    if (x == null) {
      return false;
    } else if (x.equals(FALSE)) {
      return false;
    } else {
      return true;
    }
  }
  
  //now return Double, should be generic list-element type later
  public Object eval (Object L1, EnvList env1) {
    Object L = L1; 
    Object tmp;
    EnvList env = env1;
    
    try {
      while (true) {
	if (kill) {
	  throw new RuntimeException("not really");
	}
	tracing = TRUE.equals(env.lookup("tracing"));

	if (tracing) {
	  System.out.println("        ... evaluating "+L);
	}
	
	if (L == null) return L;
	
	else if ((L instanceof Integer) || 
		 (L instanceof Double)) return L;
	
	else if (L instanceof String) 
	  return env.lookup((String)L);
	
	else if (isSpecial(L)) {
	  //   return applySpecial(L,env);      
	  //   private Object applySpecial(Object obj, EnvList env)
	  
	  ListNode LN = (ListNode)L;
	  String s = (String)(LN.car());
	  
	  if (s.equals("quote") || s.equals("_quote")) {
	    if (LN.cdr()==null) return "";
	    else return LN.second();
	    
	  } else if (s.equals("if")) {
	    LN = (ListNode)LN.cdr();
	    
	    if (truth(tmp=eval(LN.car(),env))){
	      L = LN.second();
	    } else {
	      L = LN.third();
	    }
	  } else if (s.equals("cond")) {
	    LN = (ListNode)LN.cdr();
	    LN = (ListNode)LN.car(); // list of stuff
	    
	    //			System.out.println("hello?");
	    Object first = LN.car(); // first clause
	    
	    L = null;
	    
	    while (LN != null) {
	      if (truth(tmp=eval(((ListNode)first).car(),env))) {
		L = eval(((ListNode)first).second(),env);
		break;
	      }
	      LN = (ListNode)LN.cdr();
	      first = LN.car();
	    }
	  } else if (s.equals("forever")) {
	    LN = (ListNode)LN.cdr();
	    //for(;;) {
	    while (!kill) {
	      eval(LN.car(),env);
	    }
	    return "";
	    
	  } else if (s.equals("while")) {
	    LN = (ListNode)LN.cdr();
	    while (((tmp=eval(LN.car(),env)) != null) || 
		   (!tmp.equals(FALSE))) {
	      L=(eval(LN.second(),env));
	    }
	    
	  } else if (s.equals("define")) {
	    Object t = LN.second();
	    
	    if ((t instanceof String)) {
	      String str = (String)t;
	      env.addEnv(str,eval(LN.third(),env));
	      return str;

	    } else {  //(define (F A1 A2...) (body)) 
	      String name = (String) 
		((ListNode)LN.second()).car();
	      ListNode param = (ListNode) 
		((ListNode)LN.second()).cdr();
	      
	      Object rest =  ((ListNode)LN.cdr()).cdr();
	      ListNode body = new ListNode("seq",rest);
			    
	      env.addEnv(name,new Closure(param,body,env,name));
	      return name;
	    }
			
	  } else if (s.equals("lambda")) {
	    ListNode param = (ListNode)(LN.second());
	    Object body = LN.third();
	    
	    Closure c = new Closure(param, body, env, "lambda");
	    return c;
			
	  } else if (s.equals("sleep")) {
	    Object second = eval(LN.second(), env);
			
	    if (!(second instanceof Integer)) {
	      ThrowError.error("start: parameter must be a integer");
	      throw new RuntimeException("ERROR: calling thread with "+second+"is illegal!");
	    }
	    Integer time = (Integer) second;
	    Thread.currentThread().sleep(time.intValue());
	    return "wake up!";
	    
	  } else if (s.equals("seq")) {
	    LN = (ListNode)LN.cdr();
	    Object first = LN.car();
			
	    while (LN.cdr() != null) {
	      eval(first,env);
	      LN = (ListNode)LN.cdr();
	      first = LN.car();
	    }
	    L = first;
			
	  } else if(s.equals("env")) {
	    return env;
		
	  } else if(s.equals("undef")) {
	    Object t = LN.second();
	    if (t instanceof String) {
	      String str = (String) t;
	      return env.remove(str);                      
	    } else {
	      ThrowError.error("undef: parameter not a string");
	      return null;
	    }

	  } else if (s.equals("set!")) {
	    Object t = LN.second();
	    
	    if(t instanceof String) {
	      String str = (String)t;
	      Object old = env.setEnv(str,eval(LN.third(),env));
	      return old; 
	    } else {
	      ThrowError.error("set!: first parameter not a string");
	      return null;
	    }
	    
	  } else { 
	    ThrowError.error(L + " : not a legal special form"); 
	    return null;
	  }	    
	  // end of special form handling
	  
	} else if (L instanceof ListNode) {      
	  ListNode list = evalList((ListNode)L,env);
	  if (tracing) {
	    System.out.print("     evaluating "+L);
	    System.out.print("     arguments are "+list+"\n");
	  }
	  
	  Object first = list.car();
	  if(first instanceof Function) { 
	    Object second = list.cdr();
	    if (!(second instanceof ListNode)) {
	      ThrowError.error(second + " is not a legal parameter for "+ first+"\n");
	      throw new RuntimeException("ERROR: calling "+first+" with "+second+"is illegal!");
	    }
	    Object res = ((Function)first).apply(second,env);
	    return res; 
	    
	  } else if(first instanceof Closure) {
	    Closure F = (Closure) first;
	    ListNode argList = (ListNode) list.cdr();
	    EnvList e = F.getEnv();
            
	    if (old_debugging) {
	      System.out.println("\n>>"+depth+":" + " Applying "+ F.getName() +" to "+argList + "\n" +
				 "      substitute "+argList+" for "+ F.getParams()+ " in\n"+
				 "  "+ F.getBody());
	      
	      depth++;
	    }
	    
	    ListNode params = F.getParams();
	    
	    if (ListNode.length(argList) != ListNode.length(params)) {
	      ThrowError.error("ERROR: trying to call "+F.getName()+
			       " with wrong number of arguments\n"+
			       "  definition = " + new ListNode(F.getName(),params)+"\n"+
			       "  args = "+argList+"\n");
	      throw new RuntimeException("wrong number of args for "+F.getName());
	    }
	    L = F.getBody();
	    env = e.extendEnv(F.getParams(),argList);
	    
	  } else {
	    ThrowError.error(L + " : car of list is not a function");
	    throw new RuntimeException("ERROR: first element of a combination must be a function\n   "+L+"\n" );
	  }
	} else {
	  ThrowError.error(L + " : Not an element, nor a list");
	  throw new RuntimeException("ERROR:Scheme expression must be a number, bound variable, or a combination.\n    "+L+"\n");
	}  
      } // end of while(true) loop
      
    } catch (RuntimeException e){
      if (!e.getMessage().equals("not really"))
	System.out.println("  error in evaluating " + L);
      throw(e);
    } catch (Exception e) {
      ThrowError.error("env =\n" + L + "\n" +
		       "error = "+  e + "\n\n" + 
		       "       CONGRATULATIONS! \n\n" +
		       "  You found a bug in jscheme!!!\n"+
		       "  If you want, you can report the error to \n" +
		       "     tim@cs.brandeis.edu \n"+
		       "  Just cut and paste this error buffer"+
		       " into your mail program.\n"+
		       "  Thanks\n\n ");
    }
    return(null);
  }
  
  
  public static boolean isSpecial(Object obj) {
    if (!(obj instanceof ListNode)) return false;
    ListNode L = (ListNode)obj;
    Object t = L.car();
    if(!(t instanceof String)) return false;
    String s = (String)t;
    return (s.equals("define") || s.equals("quote") 
	    || s.equals("_quote")|| s.equals("undef")
	    || s.equals("set!") || s.equals("if") || s.equals("while") || 
	    s.equals("forever") ||s.equals("cond")
	    || s.equals("seq") || s.equals("env") 
	    || s.equals("cond") || s.equals("lambda") || s.equals("sleep"));
  }   
  
  
  private ListNode evalList(ListNode L, EnvList env) {
    if (L == null) return null;
    
    Object first = eval(L.car(),env);
    Object remain = L.cdr();
    ListNode pos,result;
    
    result = new ListNode(first,null);
    pos = result;
    
    while (remain instanceof ListNode) {
      first = eval(((ListNode)remain).car(),env);
      pos.setCdr(new ListNode(first,null));
      pos = (ListNode)pos.cdr();
      remain = ((ListNode)remain).cdr();
    }
    return result;
  }  

  public Object applyClosure(Closure F, ListNode argList) {
    EnvList e = F.getEnv();
    
    if (old_debugging) {
      System.out.println("\n>>"+depth+":" + 
			 " Applying "+ F.getName() +
			 " to " + argList + "\n" +
			 "      substitute " + argList + 
			 " for " + F.getParams()+ " in\n"+
			 "  " + F.getBody());
      depth++;
    }
    ListNode params = F.getParams();
    
    if (ListNode.length(argList) != ListNode.length(params)) {
      ThrowError.error("ERROR: trying to call "+F.getName()+
		       " with wrong number of arguments\n"+
		       " definition = " +
		       new ListNode(F.getName(),params)+"\n"+
		       "  args = "+argList+"\n");
      throw new RuntimeException("wrong number of args for " + 
				 F.getName());
    }
    Object res = eval(F.getBody(), e.extendEnv(F.getParams(),argList));
    
    if (old_debugging){
      depth--;
      System.out.println(" <<"+ depth+ ": "+ F.getName()+
			 " applied to "+ argList +" returns with "+ res);
    }
    return res;
  }
}
       
      
