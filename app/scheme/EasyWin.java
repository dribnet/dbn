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
import java.util.*;
import java.applet.*;

/**
 * this class provides methods for creating windows
 * and drawing buttons, labels, and other items on
 * the window.
 */

public interface MemWin {
    //public Component add(Component x);
    //public void addCallback(Component x, Closure y);
    //public void refresh();
    //public void clear();
    //public void drawLine(int a1, int a2, int a3, int a4);
    //public void drawOval(int a1, int a2, int a3, int a4);
    //public void drawRect(int a1, int a2, int a3, int a4);
}

public class EasyWin extends Frame implements MemWin
{
   public EasyWin()
   {  
      shapes = new Vector();
      objects = new Hashtable();
      evaluator = new Evaluator();
      setLayout(new FlowLayout());
      bgColor = Color.white;
      fgColor = Color.black;
      setBackground(bgColor);

      
   }

   public EasyWin(int width, int height, String title)
   {  
      this();
      this.resize(width,height);
      this.setTitle(title);
   }

   /*
     ****************************************************************
     Drawing routines.
     We provide a mechanism for drawing lines, rectangles, and ovals
     and for redrawing them when the window is moved, resized, etc.
     ****************************************************************
   */

  // remove all of the objects drawn on the window
   public void clear()
   {
      shapes = new Vector();
      repaint();
   }
         
  // add a drawn object to the window
   public void addShape(Object o)
   {
      shapes.addElement(o);
   }
      
  public void drawLine(int x1, int y1, int x2, int y2) {
    addShape(new Line(x1,y1,x2,y2));
    this.getGraphics().drawLine(x1,y1,x2,y2);
  }

  public void drawRect(int x1, int y1, int x2, int y2) {
    addShape(new Rect(x1,y1,x2,y2));
    this.getGraphics().drawRect(x1,y1,x2,y2);
  }

  public void drawOval(int x1, int y1, int x2, int y2) {
    addShape(new Oval(x1,y1,x2,y2));
    this.getGraphics().drawOval(x1,y1,x2,y2);
  }

   public void redraw()
   {
     //      System.out.println("redraw");
      repaint();
   }

   public void refresh()
    {
      System.out.println("refreshing");
      mypaint(this.getGraphics());
    }

   public boolean handleEvent(Event evt)
   {
     //      System.out.println("handleEvent "+evt);
     if(evt.id == Event.WINDOW_DESTROY) {
       if (evaluator != null) {
            evaluator.stop();
            evaluator = null;
       }
          this.dispose();
     }
      if(evt.id == Event.WINDOW_MOVED) paint(this.getGraphics());
      if(evt.id == Event.MOUSE_DOWN) paint(this.getGraphics());

      return super.handleEvent(evt);
   }
  


   public void addCallback(Component obj, Closure clos) {
         objects.put(obj,clos);
   }


   public boolean action(Event evt, Object arg)
   {
     //       System.out.println("\naction "+evt + "  "+arg);

       Closure clos = (Closure) objects.get(evt.target);

       System.out.println("\naction "+evt + "  "+arg + "  "+ clos );

       if (clos != null) {
	 //         evaluator.applyClosure(clos,null);
         evaluator = new Evaluator();
         evaluator.set_thunk(clos);
         try {
         evaluator.start();
	 } catch (Exception e) {
           ThrowError.error("Exception " + e + " while evaluating a callback\n");
           System.out.println("Callback causes exception "+e+"\n");
	 }
       }
       return true;

   }

   public void update(Graphics g) {
     //     System.out.println("updating");
     mypaint(g);
   }

   public void paint(Graphics g)
   {
       mypaint(g);
   }

   public void mypaint(Graphics g)
   {
      Enumeration e = shapes.elements();

      validate();

      while(e.hasMoreElements())
      {
       Object obj = e.nextElement();
       if(obj instanceof Line) 
        {  
           Line L = (Line)obj;
           g.drawLine(L.x1,L.y1,L.x2,L.y2);
        }
        
       else if(obj instanceof Rect)
        {
           Rect R = (Rect)obj;
           g.drawRect(R.x,R.y,R.rectWidth,R.rectHeight);
        }

        else if(obj instanceof Oval)
        {
           Oval O = (Oval)obj;
           g.drawOval(O.x,O.y,O.rectWidth,O.rectHeight);
        }

   else { ThrowError.error("no such shape defined"); }
      }
   }
   
   public Vector shapes;
   private Hashtable objects;
   private Evaluator evaluator;
   Color bgColor;
   Color fgColor;
};    


class Line {

   Line(int a, int b, int c, int d)
   {
      x1 = a; y1 = b; x2 = c; y2 = d;
   }
      
    int x1;
    int y1;
    int x2;
    int y2;
};


class Oval {

   Oval(int a, int b, int c, int d)
   {
      x = a; y = b; rectWidth = c; rectHeight = d;
   }

   int x;
   int y;
   int rectWidth;
   int rectHeight;
};
   
class Rect {

   Rect(int a, int b, int c, int d)
   {
      x = a; y = b; rectWidth = c; rectHeight = d;
   }

   int x;
   int y;
   int rectWidth;
   int rectHeight;
};
   

