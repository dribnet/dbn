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

public class ThrowError
{
   public static void error(String s)
   {
      System.out.println("**************\nERROR: "+ s + "\n************");
      if (ta != null)
	  ta.appendText(s + "\n");
   }    
   
  public static void showError(int start, int end) {
     System.out.println("Error in positions "+start+" to "+ end);
     if (taIn!= null)
	 taIn.select(start,end);
  }

   public static void setOutput(TextArea t)
   {
      ta = t;
   }
   
   public static void setInput(TextArea t)
   {
      taIn = t;
   }
   
   private static TextArea ta;   
   private static TextArea taIn;   
};      




