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


//Any object that is passed to the method of ListNode must
//have already been tested for its validity of a scheme object

public class ListNode
{
   public ListNode(Object head, Object tail)
   {
      setCar(head);
      setCdr(tail);
   }
   
   public ListNode()
   {
      head = tail = null;
   }
  
  public static ListNode cons(Object h, Object t) {
    return new ListNode(h,t);
  }

  public static Object car(ListNode n){
    if (n==null) {
      ThrowError.error(
         "ERROR: trying to take car of empty object!!");
      return(null);
    }
    else
     return n.head;
  }

  public static Object cdr(ListNode n){
    if (n==null) {
      ThrowError.error(
         "ERROR: trying to take cdr of empty object!!");
      return(null);
    }
    else
     return n.tail;
  }



   public void setCar(Object head)
   {  
      this.head = head;
   }   
   
   public void setCdr(Object tail) 
   {
      this.tail = tail;
   }

   public Object car()
   {  return head; }

   public Object cdr()
   {  return tail; }
   
   public Object second()
   {
      return ((ListNode)tail).car();
   }
   
   public Object third()
   {
      ListNode L = (ListNode) (((ListNode)tail).cdr());
      if(L == null)
	return null;
      else
        return L.car(); 
   }
   
  public static int length(ListNode n){
    int i=0;
    while (n!=null){
      i++;
      n = (ListNode) (n.cdr());
    }
    return i;
  }


   public Object getNth(int n)
   {
      ListNode L = this;
      Object r = null;
      
      for(int i = 0; i < n; i++)
      {
	  if(L != null) {
	      r = L.car();
	      L = (ListNode)L.cdr();
	  }
	  else return null;
	  
      }
      
      return r;
   }
                  
   public String toString()
   {
      return print(this);      
   }
      
   protected String print(Object obj)
   {
      if (!(obj instanceof ListNode))
      {
         return obj.toString();
      }
      
      ListNode L = (ListNode)obj;
      String s = "(";
      Object v;
      
      while(true)
      {
         v = L.car();
         if(v == null)s += "()";
         else s += print(v);
         Object tmp = L.cdr();
         if( tmp == null) {s += ")"; return s;}
         else if (!(tmp instanceof ListNode))
         {
            s += " . " + print(tmp) + ")";
            return s;
         }           
         s += " ";
         L = (ListNode)tmp;
      }   
   }
      
   protected Object head;
   protected Object tail;
};
