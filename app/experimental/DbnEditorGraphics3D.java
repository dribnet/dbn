#ifdef EDITOR
#ifdef OPENGL

import java.awt.*;
import gl4java.*;
import gl4java.awt.*;



public class DbnEditorGraphics3D extends DbnEditorGraphics
implements GLEnum, GLUEnum {
  //static Font plainFont = new Font("Helvetica", Font.PLAIN, 10);

  //Color tickColor;
  //Color bgStippleColor;
  //DbnEditor editor;

  ExperimentalCanvas canvas;


  public DbnEditorGraphics3D(int width, int height, Color tickColor,
			     Color bgColor, Color bgStippleColor, 
			     DbnEditor editor) {
    super(width, height, tickColor, bgColor, bgStippleColor, editor);

    canvas = new ExperimentalCanvas(width, height);
    setLayout(null);
    add(canvas);
  }

  public ExperimentalCanvas getCanvas() {
    return canvas;
  }

  //public Dimension preferredSize() {
  //return new Dimension(width1*magnification + 100, 
  //		 height1*magnification + 100);
  //}


  public void base() {
    if (baseImage == null) updateBase = true;

    if (updateBase) {
      // these few lines completely identical to DbnGraphics.base()
      Dimension dim = preferredSize();
      baseImage = createImage(dim.width, dim.height);
      baseGraphics = baseImage.getGraphics();
      lastImage = createImage(width, height);
      lastGraphics = lastImage.getGraphics();

      gx = (dim.width - width*magnification) / 2;
      gy = (dim.height - height*magnification) / 2;

      // draw background
      Graphics g = baseGraphics;
      g.setColor(bgColor);
      g.fillRect(0, 0, dim.width, dim.height);
      if (!bgColor.equals(bgStippleColor)) {
	g.setColor(bgStippleColor);
	int count = 2 * Math.max(dim.width, dim.height);
	for (int i = 0; i < count; i += 2) {
	  g.drawLine(0, i, i, 0);
	}
      }
      g.setFont(plainFont);
      FontMetrics metrics = g.getFontMetrics();
      int lineheight = metrics.getAscent() + 
	metrics.getDescent();

      // put ticks around (only if in edit mode)
      g.setColor(tickColor);
      int increment = (width > 101) ? 25 : 20;
      int x, y;
      y = gy + height*magnification;
      for (x = 0; x <= width; x += increment) {
	int xx = x * magnification;
	g.drawLine(gx + xx, y, gx + xx, y + 4);
	String num = String.valueOf(x);
	g.drawString(num, gx + xx - 1, y + 2 + lineheight);
      }
      for (y = 0; y <= height; y += increment) {
	int yy = y * magnification;
	g.drawLine(gx - 4, gy + yy, gx, gy + yy);
	String num = String.valueOf(y);
	int numWidth = metrics.stringWidth(num);
	g.drawString(num, gx - 6 - numWidth, 
		     gy + height*magnification - yy);
      }
      // draw a dark frame around the runner
      g.setColor(Color.black);
      g.drawRect(gx-1, gy-1, width*magnification+1, height*magnification+1);
    }
  }



  public void paint(Graphics g) {
    base();

    if (baseImage != null) {
      baseGraphics.drawImage(dbnImage, gx, gy, 
			     width*magnification, height*magnification, null);
      // copy into buffer for writing to a tiff or quicktime
      lastGraphics.drawImage(dbnImage, 0, 0, null); 
    }
    // avoid an exception during quit
    if ((g != null) && (baseImage != null)) {
      // blit to screen
      g.drawImage(baseImage, 0, 0, null);
      
      canvas.setBounds(gx, gy, width*magnification, height*magnification);
      canvas.display();
    }
    updateBase = false;
  }


  public boolean updateMouse(Event e, int x, int y) {
    super.updateMouse(e, x, y);

    if (e.controlDown() && (mouse[2] == 100)) {
      editor.doSaveTiff();
    }

    if (e.shiftDown()) {
      editor.highlightLine(getLine(x, height1 - y));
    }

    return true;
  }
}


/*
#
import DbnEditorGraphics3D
import ExperimentalCanvas

glg = DbnEditorGraphics3D.getCurrentGraphics()
glc = glg.canvas

gl = glc.getGL()
glj = glc.getGLJ()

glj.gljMakeCurrent();

gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
gl.glColor3f(0.6, 0, 0);
gl.glRectf(-0.4, -0.4, 0.4, 0.4);

glj.gljSwap();
glj.gljCheckGL();

glj.gljFree();




#
import DbnEditorGraphics3D
import ExperimentalCanvas

glg = DbnEditorGraphics3D.getCurrentGraphics()
glc = glg.canvas

print glc.getGLJ()
 */

/*
#
import DbnEditorGraphics3D
import ExperimentalCanvas
g = DbnEditorGraphics3D.getCurrentGraphics()
glc = glg.canvas
gl = glc.getGL()
glj = glc.getGLJ()

glj.gljMakeCurrent();

gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
gl.glColor3f(0.4, 0.5, 0.5);
gl.glRectf(-0.8, -0.6, 0.8, 0.8);
gl.glColor3f(0.4, 1.0, 1.0);
gl.glRectf(-0.9, -0.4, 0.4, 0.4);

glj.gljSwap();
glj.gljCheckGL();
glj.gljFree();
*/


/*
#

glc.beginFrame()

gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
gl.glColor3f(0.4, 0.5, 0.5);
gl.glRectf(-0.8, -0.6, 0.8, 0.8);
gl.glColor3f(0.4, 1.0, 1.0);
gl.glRectf(-0.9, -0.4, 0.4, 0.4);

glc.endFrame()
*/

public class ExperimentalCanvas extends GLCanvas {
  boolean inited; 

  public ExperimentalCanvas(int width, int height) {
    super(width, height); //, null, null);
    inited = false;
  }

  public GLFunc getGL() { return gl; }
  public GLContext getGLJ() { return glj; }
  
  public void beginFrame() {
    glj.gljMakeCurrent();
    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  }

  public void endFrame() {
    glj.gljSwap();
    glj.gljCheckGL();
    glj.gljFree();
  }

  //preInit - initialisation before creating GLContext
  public void preInit() {
    System.out.println("preinit");
  }

  //init - 1st initialisation after creating GLContext
  public void init() {
    System.out.println("init");
    inited = true;
  }

  //doCleanup - OGL cleanup prior to context deletion
  public void doCleanup() {
    System.out.println("docleanup");
  }

  //display - render your frame
  public void display() {
    
    if (!inited) return;
    if (glj.gljMakeCurrent() == false) return;

    /*
    //System.out.println("displaying");
    //glj.gljMakeCurrent();

    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    gl.glColor3f(0.6f, 0, 0);
    gl.glRectf(-0.4f, -0.4f, 0.4f, 0.4f);

    glj.gljSwap();
    glj.gljCheckGL();
    */
    glj.gljFree();
    
  }

  //reshape - to reshape (window resize), gljResize() is allready invoked !
  public void reshape(int w, int h) {
    System.out.println("resize to " + w + " " + h);
  }
}


#endif
#endif

