import java.awt.*;
import java.applet.Applet;


public class DbnRunner implements Runnable {
  DbnGraphics graphics;
  DbnEnvironment env;
  String program;

  DbnEngine engine;

  static final int RUNNER_STARTED = 0;
  static final int RUNNER_FINISHED = 1;
  static final int RUNNER_ERROR = -1;
  static final int RUNNER_STOPPED = 2;
  int state = RUNNER_FINISHED;
	    
  Thread thread;
  boolean forceStop;
    

  public DbnRunner(String program, DbnGraphics graphics, DbnEnvironment env) {
    this.program = program;
    this.graphics = graphics;
    this.env = env;

    //this.start();
  }
    
  //public boolean isRunning() {
  //System.out.println("state of runner is " + state);
  //return (state == RUNNER_STARTED);
  //}

  public void start() {
    if (thread != null) {
      try { 
	thread.stop(); 
      } catch (Exception e) { }
      thread = null;
    }
    thread = new Thread(this);
    thread.start();
  }


  public void run() {
    //boolean donep = false;
    state = RUNNER_STARTED;
    graphics.reset();
    //cachedg = null;
	
    try {
      if (program.charAt(0) == ';') {
#ifdef SCHEME
	engine = new SchemeEngine(program);
	engine.start();
#endif
      } else if (program.charAt(0) == '#') {
#ifdef PYTHON
	forceStop = true;
	engine = new PythonEngine(program);
	engine.start();
	forceStop = false;
#endif
      } else {
	DbnParser parser = 
	  new DbnParser(DbnPreprocessor.process(program));
	engine = new DbnEngine(parser.getRoot(), graphics);
	engine.start();
      }
      state = RUNNER_FINISHED;
      env.finished();

    } catch (DbnException e) { 
      state = RUNNER_ERROR;
      forceStop = false;
      this.stop();
      env.error(e);

    } catch (Exception e) {
      e.printStackTrace();
      this.stop();
    }	
    //render();
    //gui.terminated();
  }


  public void stop() {
    if (engine != null) {
      engine.stop();
      if (forceStop) {
	thread.stop();
	thread = null;
      }
      engine = null;
    }
    //gui.msg(""); 
  }


  /*
    public void render() {
    //System.out.println(dbg);
    //System.out.println(dbg.image);
    //System.out.println(dbrp);
    dbrp.update(dbg.image);
    }
  */

  // the dbn engine calls this function which will 
  // make the little guy blink intermittently
    
  // i don't like this, so i'm disabling it. it doesn't
  // work for python or scheme anyway. maybe i'm gonna add it
  // back in, inside of dbngui as its own (gasp) thread
  /*
    public void idle() {
    long currentTime = System.currentTimeMillis();
    gui.idle(currentTime);

    if ((currentTime % 1000) > 800) {
    // beat the heart if a new beat
    long hba = currentTime / 1000;
    if (hba != heartbeatTime)
    gui.heartbeat();
    heartbeatTime = hba;
    }
    }
  */
}
