package jscheme;

import java.util.*;
import java.lang.String;


/**
 * this class represents closures in the standard way.
 */
public class Closure
{
   Closure()
   {
      param = null;
      body  = null;
      env   = null;
      name  = "closure";
   }
   
   Closure(ListNode p, Object b, EnvList e)
   {
      param = p;
      body  = b;
      env   = e;
      name  = "closure";
   }   
  
   Closure(ListNode p, Object b, EnvList e, String n)
   {
      param = p;
      body  = b;
      env   = e;
      name  = n;
   }   
  
  public String toShortString(){
    return "  (((" + name + "(" + param + ")" + ":=" + (body.toString()).substring(0,Math.min(20,(body.toString()).length())) + "....)))  ";
  }

  public String toString(){
    //    return "  (((" + name + "(" + param + ")" + ":=" + (body.toString()) ;
    //    return    "(lambda " + param + " " + body.toString() + " )" ;
    return "PROCEDURE:"+name;
  }

   ListNode getParams() { return param; }
   Object getBody()  { return body;  }
   EnvList getEnv()   { return env;   }
   String getName()   { return name;   }
     
   private ListNode param;
   private Object body;
   private EnvList  env;
   private String name;
};

