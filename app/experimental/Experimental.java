import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.*;


public class Experimental extends DbnApplication implements ActionListener {
  MenuBar menubar;
  
  static final String goodieLabels[] = {
    "Make QuickTime movie...",
    null,
    "Convert editing area to Java applet...",
    "Convert .dbn file to Java applet...",
    null,
    "Run benchmark"
  };

  static public void main(String args[]) {
    new Experimental().frame.show();
  }

  public Experimental() {
    menubar = new MenuBar();
    MenuItem mi = null;

    Menu goodies = new Menu("Tasty");
    for (int i = 0; i < goodieLabels.length; i++) {
      if (goodieLabels[i] == null) {
	goodies.addSeparator();
	continue;
      }
      goodies.add(new MenuItem(goodieLabels[i]));
    }
    goodies.addActionListener(this);
    menubar.add(goodies);

    frame.setMenuBar(menubar);
  }

  public void actionPerformed(ActionEvent event) {
    String command = event.getActionCommand();
    for (int i = 0; i < goodieLabels.length; i++) {
      if (command.equals(goodieLabels[i])) {
	switch (i) {
	case 0:
	  ((DbnEditor)environment).doRecord();
	  break;
#ifdef CONVERTER
	case 2:
	  try {
	    convert(((DbnEditor)environment).textarea.getText());
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  break;
	case 3: 
	  try {
	    convert(null); 
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  break;
#endif
	case 5:
	  benchmark();
	  break;
	}
	return;
      }
    }
  }

  public void benchmark() {
    long t1 = System.currentTimeMillis();
    DbnEditor editor = (DbnEditor) environment;
    String oldText = editor.textarea.getText();
    editor.textarea.setText(readFile("lib\\benchmark.dbn"));
    editor.doPlay();
    while (editor.playing) { /*System.out.print(".");*/ }
    long t2 = System.currentTimeMillis();
    System.out.println("time was " + (t2-t1));
    editor.textarea.setText(oldText);
  }

#ifdef RECORDER
  static String lastGrabDirectory;

  static public int[] imageToArray(Image img, int w, int h) {
    //int w = img.getWidth(DbnApplet.applet);
    //int h = img.getHeight(DbnApplet.applet);
    int pix[] = new int[w * h];
    PixelGrabber pg = 
      new PixelGrabber(img, 0, 0, w, h, pix, 0, w);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
    }
    return pix;
  }

  static public void screenGrab(Image image, /*byte pixelBytes[], */
				int width, int height) {
    FileDialog fd = new FileDialog(new Frame(), 
				   "Save image as...", 
				   FileDialog.SAVE);
    if (lastGrabDirectory != null) {
      fd.setDirectory(lastGrabDirectory);
    }
    fd.show();
    
    try {
      String directory = fd.getDirectory();
      String name = fd.getFile();
      FileOutputStream fos = new FileOutputStream(new File(directory, name));
      
      /*
      int pixels[] = new int[width*height];
      for (int k = 0; k < pixels.length; k++) {
	pixels[k] = DbnRecorder.grays[pixelBytes[k]];
      }
      */
      int pixels[] = new int[width*height];
      PixelGrabber pg = 
	new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
      try {
	pg.grabPixels();
      } catch (InterruptedException e) {
      }
      //GifEncoder ge = new GifEncoder(pixels, width, fos);
      //GifEncoder ge = new GifEncoder(image, fos, false);
      GifEncoder ge = new GifEncoder(pixels, width, fos);
      ge.encode();
      fos.close();

      lastGrabDirectory = directory;
      
    } catch (IOException e) {
      System.err.println("An error occurred while trying to make a screen grab");
      e.printStackTrace();
    }
  }
#endif


#ifdef CONVERTER
  public void convert(String program) throws IOException, DbnException {
    String outputNameBase = null;
    String inputDirectory = null;

    if (program == null) {
      FileDialog fd = new FileDialog(new Frame(), 
				     "Select a DBN program to convert...", 
				     FileDialog.LOAD);
      fd.show();
      
      inputDirectory = fd.getDirectory();
      String inputFilename = fd.getFile();
      if (inputFilename == null) return; // user cancelled
      
      File inputFile = new File(inputDirectory, inputFilename);
      FileInputStream input = new FileInputStream(inputFile);
      int length = (int) inputFile.length();
      byte data[] = new byte[length];
      int count = 0;
      while (count != length) {
	data[count++] = (byte) input.read();
      }
      program = new String(data);  // not I18N compliant
      
      int suffixIndex = inputFilename.lastIndexOf(".");
      if (suffixIndex != -1) {
	String suffix = inputFilename.substring(suffixIndex);
	if (suffix.equals(".dbn")) {
	  outputNameBase = inputFilename.substring(0, suffixIndex);
	} else {
	  System.err.println("suffix no good: " + suffix);
	}
      }
    }
    FileDialog fd = new FileDialog(new Frame(), 
				   "Save converted program as...", 
				   FileDialog.SAVE);
    if (inputDirectory != null) fd.setDirectory(inputDirectory);
    if (outputNameBase != null) fd.setFile(outputNameBase);
    fd.show();
    
    String outputDirectory = fd.getDirectory();
    String outputName = fd.getFile();
    if (outputName == null) return;

    DbnParser parser = 
      new DbnParser(DbnPreprocessor.process(program));
    String converted = parser.getRoot().convert(outputName);
    File javaOutputFile = new File(outputDirectory, outputName + ".java");
    FileOutputStream fos = new FileOutputStream(javaOutputFile);
    PrintStream ps = new PrintStream(fos);
    ps.print(converted);
    ps.close();

    File htmlOutputFile = new File(outputDirectory, outputName + ".html");
    fos = new FileOutputStream(htmlOutputFile);
    ps = new PrintStream(fos);
    ps.println("<HTML> <BODY BGCOLOR=\"white\">");
    //ps.println("<APPLET CODE=\"DbnApplet\" WIDTH=101 HEIGHT=101>");
    ps.println("<APPLET CODE=\"DbnApplet\" ARCHIVE=\"");
    ps.print(outputName + ".jar");
    ps.println("\" WIDTH=101 HEIGHT=101>");
    ps.print("<PARAM NAME=\"program\" VALUE=\"");
    ps.print(outputName);
    ps.println("\">");
    ps.println("</APPLET>");
    ps.println("</BODY> </HTML>");
    ps.close();

    final String classes[] = {
      "DbnApplet.class", "DbnException.class",
      "DbnGraphics.class", "DbnPlayer.class"
    };

    for (int i = 0; i < 4; i++) {
      copyFile(new File("lib\\player", classes[i]),
	       new File(outputDirectory, classes[i]));
    }
    try {
      // execute javac with parameters:
      // String outputdir = new File(filename).getPath();
      // javac -classpath outputdir;%CLASSPATH% outputdir\*.java

      String args[] = new String[5];
      args[0] = "-classpath";
      args[1] = outputDirectory + File.pathSeparator + 
	System.getProperty("java.class.path");
      args[2] = "-d";
      args[3] = outputDirectory;
      args[4] = javaOutputFile.getCanonicalPath();
      sun.tools.javac.Main.main(args);

      if (!javaOutputFile.exists()) {
	System.err.println("compile failed.");
	return;
      }

      FileOutputStream zipOutputFile = 
	new FileOutputStream(new File(outputDirectory, outputName + ".jar"));
      ZipOutputStream zos = new ZipOutputStream(zipOutputFile);

      ZipEntry entry;
      for (int i = 0; i < classes.length; i++) {
	entry = new ZipEntry(classes[i]);
	zos.putNextEntry(entry);
	zos.write(grabFile(new File("lib\\player\\" + classes[i])));
      }

      entry = new ZipEntry(outputName + ".class");
      zos.putNextEntry(entry);
      zos.write(grabFile(new File(outputDirectory, outputName + ".class")));
      zos.flush();
      zipOutputFile.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected byte[] grabFile(File file) throws IOException {
    //File file = new File(filename);
    int size = (int) file.length();
    FileInputStream input = new FileInputStream(file);
    byte buffer[] = new byte[size];
    int offset = 0;
    int bytesRead;
    while ((bytesRead = input.read(buffer, offset, size-offset)) != -1) {
      offset += bytesRead;
    }
    return buffer;
  }

  protected void copyFile(File afile, File bfile) {
    try {
      FileInputStream from = new FileInputStream(afile);
      FileOutputStream to = new FileOutputStream(bfile);
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = from.read(buffer)) != -1) {
	to.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
#endif

}

    /*
    // copy DbnException.class, DbnGraphics.class, 
    // DbnApplet.class, and DbnPlayer.class to the directory
    copyFile(new File("lib\\player", "DbnApplet.class"),
	     new File(outputDirectory, "DbnApplet.class"));

    copyFile(new File("lib\\player", "DbnException.class"), 
	     new File(outputDirectory, "DbnException.class"));

    copyFile(new File("lib\\player", "DbnGraphics.class"), 
	     new File(outputDirectory, "DbnGraphics.class"));

    copyFile(new File("lib\\player", "DbnPlayer.class"), 
	     new File(outputDirectory, "DbnPlayer.class"));
    */
