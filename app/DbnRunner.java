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
    

  public DbnRunner(DbnGraphics graphics, DbnEnvironment env) {
    this("", graphics, env);
  }

  public DbnRunner(String program, DbnGraphics graphics, DbnEnvironment env) {
    this.program = program;
    this.graphics = graphics;
    this.env = env;
  }


  public void setProgram(String program) {
    this.program = program;
  }


  public void start() {
    if (thread != null) {
      try { 
	thread.stop(); 
      } catch (Exception e) { }
      thread = null;
    }
    thread = new Thread(this, "DbnRunner");
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
#else
	System.err.println("scheme support not compiled!");
#endif
      } else if (program.charAt(0) == '#') {
#ifdef PYTHON
	forceStop = true;
	engine = new PythonEngine(program);
	engine.start();
	forceStop = false;
#else
	System.err.println("python support not compiled!");
#endif
      } else if (program.indexOf("extends DbnPlayer") != -1) {
#ifdef JAVAC
	engine = new JavacEngine(program, graphics);
	engine.start();
	//System.out.println("going");
	//forceStop = true;
#endif
      } else {
	String pre = "set red 0; set green 1; set blue 2; " + 
	  "set quicktime 0; set tiff 1; set illustrator 2; ";
	DbnParser parser = 
	  new DbnParser(DbnPreprocessor.process(pre + program));
	
	DbnToken root = parser.getRoot();
	if (!root.findToken(DbnToken.SIZE)) {
	  graphics.size(101, 101, 1);
	}
	if (root.findToken(DbnToken.REFRESH)) {
	  graphics.aiRefresh = false;
	}
	engine = new DbnEngine(root, graphics);
	engine.start();
      }
      //System.out.println("finished");
      state = RUNNER_FINISHED;
      env.finished();
      graphics.update();

    } catch (DbnException e) { 
      state = RUNNER_ERROR;
      forceStop = false;
      this.stop();
      env.error(e);

    } catch (Exception e) {
#ifndef KVM
      e.printStackTrace();
#endif
      this.stop();
    }	
    //System.out.println("gone");
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
  }


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
