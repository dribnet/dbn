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

public class TokenStream
{         
  private String s;
  int lastpos;
  int pos=0;
  int prevpos=0;

   //methods
   public TokenStream(String s1)
   {
      char c;
      s = s1;

      StringBuffer cs = new StringBuffer(s1+" ");

      lastpos = cs.length()-1;
      for(int i=0;i<=lastpos;i++) {
        if (!printable(cs.charAt(i))) {
          cs.setCharAt(i,' ');
          ThrowError.error(
            "Using blank instead of \n  Non-printable character '"+
             cs.charAt(i)
             +"'\n  with ASCII number"+((int)s1.charAt(i))+" at position "+i);
	}
      }
      s = cs.toString();
      advanceToToken();
  }

   private void advanceToToken() {
      // first we advance past the whitespace and comments

      skipSpaces();
      while ((pos < lastpos) && (s.charAt(pos)==';')) {
        skipToEndOfLine();
        skipSpaces();
      }
      if ((pos==lastpos) && 
          ( (Character.isSpace(s.charAt(pos))) || (s.charAt(pos)==';')))
      pos++;

      // at this point either (pos = lastpos+1) or
      // the current character is a non-semicolon, non-space
      //      and is the beginning of a token
   }
   
   public boolean hasMoreTokens()
   {
     return (pos<=lastpos);
   } 
   
  private boolean isDelimiter(char c) {
    if (Character.isSpace(c)) return true;
    return
       ((c=='(') || 
        (c==')') || 
        (c==';') || 
        (c=='"')
	|| (c==''')
       );
  }

   private boolean printable(char c) {
    return 
      ((c <127) &&
       ((c>=32) || (c=='\n') ||(c=='\t') ||(c=='\r')
       )
      );
  }

   private void skipSpaces() {
      while ((pos < lastpos) && 
             (Character.isSpace(s.charAt(pos)) || !printable(s.charAt(pos)))
            ) pos++;
   }

  private void skipToEndOfLine() {
      while ((pos<lastpos) && 
             (s.charAt(pos)!='\n') && 
             (s.charAt(pos)!='\r')) 
      pos++;
  }

  public Object nextToken()
   {
     int tokenpos=pos;
     String token;
     char c;

     // first check to see if you have a delimiter!
     if (isDelimiter(c=s.charAt(tokenpos))) {
       // quoted strings are handled differently
       if (c=='"') {
          tokenpos++;
          while ((tokenpos<lastpos) && 
                 (((c=s.charAt(tokenpos))!='"') || (s.charAt(tokenpos-1)=='\\'))
                 &&
                 (c != '\n') &&
                 (c != '\r')) tokenpos++;
          if ((c=='\n') || (c=='\r')) {
             tokenpos --;
             ThrowError.error("          Missing end of quotation "+ s.substring(pos,tokenpos+1));
             ThrowError.showError(pos,tokenpos);
             pos = lastpos+1;
             return("\"ERROR\"");
           }
       }
       token = s.substring(pos,tokenpos+1);
       prevpos=pos;
       pos = tokenpos+1;
       advanceToToken();
       //       System.out.println("          "+ token + "    was found ");
       return token;
      }


     // advance tokenpos to next whitespace or delimiter or end
     while ((tokenpos<lastpos) && !(isDelimiter(c=s.charAt(tokenpos+1)))) tokenpos++; 

     token = s.substring(pos,tokenpos+1);
     prevpos=pos;
     pos = tokenpos+1;
     advanceToToken();
     //     System.out.println("          "+token + "    was found ");
     return token;
   }

   public String toString()
   {
      return "Tokenstream: "+s;
   }
   
   
}

