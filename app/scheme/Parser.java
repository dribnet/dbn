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
import SchemeEngine;

/**
 * This class implements a Parser for scheme.
 * It is initialized with a string to be parsed.
 * Subsequent calls to the instance method parse()
 * return S-expressions (of type object).
 */
public class Parser
{
    public int depth = 0;
    
    public Parser(String s) {
	//      ts = new TokenStream(preprocess(s));
	ts = new TokenStream(s);
    }
    
    /**
     * Parse the next S-expression in the string
     * and return its Jscheme representation.
     */
    public Object parse() {
	Object R = internalParse();
	if ((R!=null) && (R.equals(")"))) {
	    ThrowError.error("Too many close parentheses");
	    ThrowError.showError(ts.prevpos,ts.pos);
	    ts.pos = ts.lastpos+1;
	    R = null;
	}
	
	//    System.out.println("\n\nParsed the expression\n  "+R);
	return R;
    }

    private void tab(int d) {
	while (d-- >0)
	    System.out.print("  ");
    }
    
    public Object internalParse() {
	String token;
	Object L,R;
	
	if (!ts.hasMoreTokens()) return null;
	
	token = (String) ts.nextToken();
	
	//if (Interpreter.debugging) {
	if (SchemeEngine.debugging) {
	    if ("(".equals(token)) {
		System.out.println(" "); tab(depth); depth++;
		System.out.print(token+" "); 

	    } else if (")".equals(token)) {
		//                 System.out.println(" "); 
		depth--; System.out.print(" ) ");
		//  tab(depth); System.out.println(token+" "); tab(depth);

	    } else System.out.print(token + " ");
	}

	if (token.charAt(0)=='"') {
	    if (token.length()==2) token = "";
	    else token = token.substring(1,token.length()-1);
	    R = new ListNode("quote",new ListNode(token,null));

	} else if (token.equals("(")) {
	    R= parselist();
	
	} else if (token.equals(")")) {
	    R= token;
	
	} else if (token.equals("'")) {
	    R= new ListNode(new String("quote"),new ListNode(internalParse(),null));
	} else {
	    R = token;
	    try {
		if (isInteger(token)) R = Integer.valueOf(token);
		else if (isFloat(token)) R = Double.valueOf(token);
		else R= token;
	    }
	    catch(Exception e) {
		System.out.println("Error in Jscheme Parser: "+e);
	    }
	}

	//      System.out.println("          internal parse -> "+R);

	return R;
	/*
	  while (ts.hasMoreTokens()) {
	  token = ts.nextToken();
	  //        System.out.println("'"+token+"'" + "has length "+ ((String)(token.toString())).length());
	  }
	*/
    }
   
    private boolean isInteger(String s) {
	int i, len = s.length();
	if (len > 0) {
	    if (!((s.charAt(0)=='-') || Character.isDigit(s.charAt(0))))
		return false;
	}
	for(i = 1; i<len;i++)
	    if (!Character.isDigit(s.charAt(i))) return false;

	return !(s.equals("+") || s.equals("-"));
    }

    private boolean isFloat(String s) {
	int i=0, len = s.length();

	// Verify that the string starts with a +,-, or digit
	if (len > 0) {
	    if (s.charAt(0)!='-') i++;
	    if ((s.charAt(0)=='.')) return false;
	}

	// look for the first non-digit (after a possible sign)
	for(; ((i<len)&&(Character.isDigit(s.charAt(i))));i++);

	// if we've reached the end then it is an integer, not a float!
	if (i==len) return false;

	// if we've stopped at anything but a decimal point, return false;
	if (s.charAt(i)!='.') return false;

	// skip the decimal point, and scan the rest for digits
	i++;
	for(; ((i<len)&&(Character.isDigit(s.charAt(i))));i++);

	// if all we've seen are digits, then we are done!
	if (i==len) return true;
  
	// scientific notation is not allowed, so otherwise return failure
	return false;
    }

    private ListNode parselist() {
	Object token;

	if (!ts.hasMoreTokens()) {
	    ThrowError.error("program ends prematurely");
	    return null;
	}

	token = internalParse();
	if ((token!=null) && token.equals(")")) return null;
	//    else return new ListNode(token,parselist());
	else return makeListNode(token,parselist());

    }

    private ListNode makeListNode(Object X, ListNode Y) {
	ListNode L;
	L =  new ListNode(X,Y);
	return L;
    }


    private static ListNode reverse(ListNode r) {
	ListNode s = null;

	while ((r != null)) {
	    s = new ListNode(r.car(),s);
	    r = (ListNode) r.cdr();
	}

	return s;
    }



    public static Object readString(String s) {
	Parser parser = new Parser(s);
	Object v=null, r=null;

	//    System.out.println("________________\n       Reading expressions");
	while(parser.ts.hasMoreTokens())
	    {
		v = parser.internalParse();
		//	System.out.println(v);
		r = new ListNode(v,r);
	    }    
	//    System.out.println("       Finished reading expressions\n______________");
    
	if (r==null) return(r);

	if (((ListNode)r).cdr()==null) 
	    r = (((ListNode)r).car());
	else
	    r = reverse((ListNode) r);

	//    System.out.println("\n   read structure: \n" + r+"\n\n");

	return r;

    }



    public static Object readStringToList(String s) {
	Parser parser = new Parser(s);
	Object v=null, r=null;

	//    System.out.println("________________\n       Reading expressions");
	while(parser.ts.hasMoreTokens())
	    {
		v = parser.internalParse();
		//	System.out.println(v);
		r = new ListNode(v,r);
	    }    
	//    System.out.println("       Finished reading expressions\n______________");
    
	if (r==null) return(r);

	r = reverse((ListNode) r);

	//    System.out.println("\n   read structure: \n" + r+"\n\n");

	return r;

    }


    private TokenStream ts;   
   
};                
