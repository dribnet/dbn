#ifdef JAVAC


import java.io.*;
import java.util.*;
//import sun.tools.javac.*;

/*
	OutputStream out = System.err;
	// back-compatibility for IDE vendors
	if (Boolean.getBoolean("javac.pipe.output")) {
	    out = System.out;
	}
	Main compiler = new Main(out, "javac");
	System.exit(compiler.compile(argv) ? 0 : 1);
*/

public class JavacEngine extends DbnEngine {
  String program;
  DbnGraphics graphics;
  String classname;
  DbnPlayer player;

  public JavacEngine(String program, DbnGraphics graphics) {
    this.program = program;
    this.graphics = graphics;
  }

  // this should not exit until the player is stopped

  public synchronized void start() throws DbnException {
    try {
      String firstPart = "public class ";
      int where = program.indexOf(firstPart) + firstPart.length();
      if (where == -1) {
	throw new DbnException("could not find class name");
      }
      classname = program.substring(where);
      classname = classname.substring(0, classname.indexOf(' '));
      //System.out.println("found potential class name '" + classname + "'");

      FileOutputStream fo = new FileOutputStream(classname + ".java");
      PrintStream pfo = new PrintStream(fo);
      pfo.print(program);
      fo.close();

      String args[] = new String[3];
      args[0] = "-d";
      args[1] = ".\\lib";
      args[2] = classname + ".java";
      OutputStream out = System.err;
      sun.tools.javac.Main compiler = new sun.tools.javac.Main(out, "javac");
      // need to destroy the .java file
      if (!compiler.compile(args)) {

      } else {
	Class c = Class.forName(classname);
	player = (DbnPlayer) c.newInstance();
	player.init(graphics);
	//player.graphics.update();
	try {
	  player.start();
	  stopFlag = false;
	  while (true) { }
	} catch (Exception e) {
	  e.printStackTrace();
	  if (!stopFlag) throw new DbnException(e.toString());
	}
      }
      System.out.println("outie");
    } catch (Exception e) {
      e.printStackTrace();
    }
    /*
    stopFlag = false;
    try {
      interpreter.exec("import DbnGraphics");
      interpreter.exec("g = DbnGraphics.getCurrentGraphics()");
      interpreter.exec(program);
    } catch (Exception e) {
      //e.printStackTrace();
      //if (!stopFlag) throw new DbnException(e.toString());
      if (!stopFlag) {
	e.printStackTrace();
	String s = e.toString();
	int num = s.lastIndexOf("line ");
	if (num != -1) {
	  s = s.substring(num + 5);
	  num = 0;
	  while (Character.isDigit(s.charAt(num))) num++;
	  //System.out.println("gonna go ." + s.substring(0, num) + ".");
	  int linenum = 0;
	  try {
	    linenum = Integer.parseInt(s.substring(0, num));
	  } catch (NumberFormatException e2) {
	    throw new DbnException("Python error, check the console.");
	  }
	  throw new DbnException("Python error on line " + linenum, linenum-1);
	} else {
	  //throw new DbnException(e.toString());
	  throw new DbnException("Python error, check the console.");
	}
      }
    }
    */
  }

  public void stop() {
    // killing the thread (setting it to null) throws an
    // exception, so when the stopFlag is set, the exception won't
    // be passed to the calling application. wow, i hope this 
    // works in exploder.

    // player gets stopped in DbnEditor.terminate();
    /*
    System.err.println("stopping");
    player.stop();
    player = null;
    System.err.println("stopped");
    stopFlag = true;
    */
  }
}


#endif
