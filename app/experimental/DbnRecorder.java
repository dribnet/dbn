#ifdef RECORDER

import java.awt.*;
import java.awt.image.*;
import java.io.*;


import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.sound.*;
import quicktime.std.image.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.util.*;

import quicktime.app.display.*;
import quicktime.app.image.*;
import quicktime.app.QTFactory;


public class DbnRecorder implements Paintable, StdQTConstants, Errors {
  static final boolean RECORD_TO_DISK = true;
  static DbnRecorder recorder;

  DbnEnvironment environment;
  int width, height;

  long lastTime;
  Image lastImage;
  int lastX, lastY;
  boolean lastButton;

  int tempFrameCount;
  File tempFile;
  DataOutputStream tempOutputStream;
  ByteArrayOutputStream arrayOutputStream;

  QTCanvas canvas;
  QTImageDrawer qid;
  
  Rectangle updateRects[];
  QTFile movieFile;
  Movie movie;
  Track videoTrack;
  VideoMedia videoMedia;
  RawEncodedImage compressedImage;
  QDRect rect;
  QDGraphics gw;
  QTHandle imageHandle;
  CSequence sequence;
  ImageDescription description;

  boolean cursorVisible;
  Color cursorUpColor;
  Color cursorDownColor;

  static public int grays[];
  static {
    grays = new int[101];
    for (int i = 0; i < 101; i++) {
      int gray = ((100-i)*255/100);
      grays[i] = 0xff000000 | (gray << 16) | (gray << 8) | gray;
    }
  }

  // a finalizer should call canvas.removeClient() 
  // and then QTSession.close(), frame.dispose() could come after that too

  public DbnRecorder(DbnEnvironment environment, 
		     int width, int height) throws Exception {
    this.environment = environment;
    this.width = width;
    this.height = height;

    cursorVisible = DbnApplet.getBoolean("cursor_visible", false);
    cursorDownColor = DbnApplet.getColor("cursor_down_color", Color.black);
    cursorUpColor = DbnApplet.getColor("cursor_up_color", Color.gray);

    FileDialog fd = new FileDialog(new Frame(), "Save movie as...", 
				   FileDialog.SAVE);
    fd.show();
    if (fd.getFile() == null) {
      throw new Exception("user cancelled movie creation");
    }
    try {
      QTSession.open();

      // NumberPainter.<init>, because setClient will call paint()
      updateRects = new Rectangle[1];
      updateRects[0] = new Rectangle(0, 0, width, height);

      Frame frame = new Frame("DbnRecorder");

      canvas = new QTCanvas(QTCanvas.kInitialSize, 0.5f, 0.5f);
      frame.add("Center", canvas);
      qid = new QTImageDrawer(this, new Dimension(width, height),
			      Redrawable.kMultiFrame);
      qid.setRedrawing(true);
      canvas.setClient(qid, true);

      frame.pack();
      frame.setLocation(100, 550);

      movieFile = new QTFile(fd.getDirectory() + fd.getFile());

      movie = Movie.createMovieFile(movieFile, kMoviePlayer, 
				    createMovieFileDeleteCurFile | 
				    createMovieFileDontCreateResFile);

      // from CreateMovie.addVideoTrack()
      videoTrack = movie.addTrack(width, height, 0);  // no volume
      videoMedia = new VideoMedia(videoTrack, 1000);  // timescale
      videoMedia.beginEdits();

      // from CreateMovie.addVideoSample()
      rect = new QDRect(width, height);
      gw = new QDGraphics(rect);
      int size = QTImage.getMaxCompressionSize(gw, rect, 
					       gw.getPixMap().getPixelSize(),
					       codecNormalQuality, 
					       kAnimationCodecType, 
					       CodecComponent.anyCodec);

      imageHandle = new QTHandle(size, true);
      imageHandle.lock();
      compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
      sequence = new CSequence(gw, rect, gw.getPixMap().getPixelSize(), 
			       kAnimationCodecType, 
			       CodecComponent.bestFidelityCodec,
			       codecNormalQuality, codecNormalQuality, 
			       10, null, 0);
      description = sequence.getDescription();

      if (RECORD_TO_DISK) {
	tempFile = new File(fd.getDirectory(), fd.getFile() + ".tmp");
	tempOutputStream =
	  new DataOutputStream(new FileOutputStream(tempFile));
      } else {
	arrayOutputStream = new ByteArrayOutputStream(5 * 1024 * 8);
	tempOutputStream = new DataOutputStream(arrayOutputStream);
      }
      tempFrameCount = 0;
      lastTime = System.currentTimeMillis();
      environment.message("Recording movie...");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  synchronized static public void start(DbnEnvironment environment,
					int width, int height) {
    try {
      recorder = new DbnRecorder(environment, width, height);
    } catch (Exception e) {
      recorder = null;
    }
  }


  static public void addFrame(/*Image image,*/ int pixels[],
			      int mouseX, int mouseY, boolean mouseDown) {
    //if ((recorder != null) && !finishing) {
    if (recorder != null) {
      //System.out.println("adding frame");
      recorder.add(/*image,*/ pixels, mouseX, mouseY, mouseDown);
      //} else {
      //System.out.println("not adding frame");
    }
  }

  //boolean adding = false;
  // the synchronized makes sure that this function finished
  // before stop() and subsequently finish() get called
  synchronized public void add(/*Image image,*/ int pixels[], 
			       int mouseX, int mouseY, boolean mouseDown) {
    //if ((recorder == null) || (tempOutputStream == null)) return;
    if (recorder == null) return;
    //if (adding) return;
    //adding = true;


    long currentTime = System.currentTimeMillis();
    if (currentTime - lastTime < 1000/30) return; // limit to 30 fps
    if (lastTime == 0) { 
      lastTime = currentTime;
      return; 
    } 
    int frameDuration = (int) (currentTime - lastTime); 
    if (frameDuration == 0) return; 
    lastTime = currentTime;

    try {
      //synchronized (tempOutputStream) {
      //System.out.println("write tos");
      tempOutputStream.writeInt(frameDuration);
      tempOutputStream.writeInt(mouseX);
      tempOutputStream.writeInt(mouseY);
      tempOutputStream.writeBoolean(mouseDown);
      //tempOutputStream.write(pixels);
      for (int i = 0; i < pixels.length; i++) {
	tempOutputStream.writeInt(pixels[i]);
      }
      tempFrameCount++;
      //System.out.println("done tos");
      //}

    } catch (IOException e) {
      e.printStackTrace();
    }
    //adding = false;
  }

  public void makeQuickTime() throws IOException {
    InputStream is = null;

    tempOutputStream.flush();
    if (RECORD_TO_DISK) {
      tempOutputStream.close();
      is = new FileInputStream(tempFile);
    } else {
      byte array[] = arrayOutputStream.toByteArray();
      tempOutputStream.close();
      is = new ByteArrayInputStream(array);
    }
    DataInputStream tempInputStream = new DataInputStream(is);

    //byte pixelBytes[] = new byte[width*height];
    int pixels[] = new int[width*height];

    // run through the temp file and get to work
    for (int i = 0; i < tempFrameCount; i++) {
      int frameDuration = tempInputStream.readInt();
      int mouseX = tempInputStream.readInt();
      int mouseY = tempInputStream.readInt();
      boolean mouseDown = tempInputStream.readBoolean();
      for (int j = 0; j < pixels.length; j++) {
	pixels[j] = tempInputStream.readInt();
      }
      //tempInputStream.readFully(pixelBytes);

      //for (int k = 0; k < pixels.length; k++) {
      //pixels[k] = grays[pixelBytes[k]];
      //}
      MemoryImageSource mis = 
	new MemoryImageSource(width, height, pixels, 0, width);
      Image image = Toolkit.getDefaultToolkit().createImage(mis);
      try {
	environment.message("Saving frame " + (i+1) + 
			    " of " + tempFrameCount);
	addQuickTime(image, mouseX, mouseY, mouseDown, frameDuration);
      } catch (Exception e) {
	System.err.println("having trouble with duration " + frameDuration);
	e.printStackTrace();
      }
    }
    tempInputStream.close();

    if (RECORD_TO_DISK) {
      tempFile.delete();
    }
  }

  public void addQuickTime(Image image, int mouseX, int mouseY, 
			   boolean mouseDown, int frameDuration) 
    throws QTException {
    
    qid.setGWorld(gw);
    qid.setDisplayBounds(rect);
    
    lastImage = image;
    qid.redraw(null);
    
    CompressedFrameInfo info = 
      sequence.compressFrame(gw, rect, codecFlagUpdatePrevious, 
			     compressedImage);
    boolean isKeyFrame = (info.getSimilarity() == 0);
    // 0 is offset, 1 is the number of samples
    // frameDuration is in units of timescale above/second
    videoMedia.addSample(imageHandle, 0, info.getDataSize(), 
			 frameDuration, description, 1, 
			 (isKeyFrame ? 0 : mediaSampleNotSync));
    //System.out.println("duration=" + frameDuration + ", " +
    //		 "keyframe=" + isKeyFrame + ", " +
    //		 "similarity=" + info.getSimilarity());
    
    //print out ImageDescription for the last video media data ->
    //this has a sample count of 1 because we add each "frame" 
    //as an individual media sample
    //System.out.println(desc);
    
    //redraw after finishing...
    qid.setGWorld(canvas.getPort());
    //np.setCurrentFrame (numFrames);
    qid.redraw(null);
    
    //lastTime = currentTime;
    //lastImage = currentImage;
    lastX = mouseX;
    lastY = mouseY;
    lastButton = mouseDown;
  }


  //static boolean stopping = false;

  static public void stop() {
    if (recorder == null) return;
    //if (stopping) return;
    //stopping = true;

    recorder.finish();
    recorder = null;
    //stopping = false;
  }

  public void finish() {
    //if (finishing) return;
    try {
      // be sure to get the very last frame
      // set the clock to make the frame not exceed 30 fps
      // then update the graphics so that the last frame is added
      lastTime = System.currentTimeMillis() - 100;
      ((DbnEditor)environment).graphics.update();

      //System.out.println("making quicktime");
      makeQuickTime();

      // the end of CreateMovie.addVideoTrack()
      videoMedia.endEdits();
      // trackstart, mediatime, duration, mediarate
      //System.out.println("duration = " + videoMedia.getDuration());
      videoTrack.insertMedia(0, 0, videoMedia.getDuration(), 1);

      OpenMovieFile outStream = OpenMovieFile.asWrite(movieFile); 
      movie.addResource(outStream, movieInDataForkResID, movieFile.getName());
      outStream.close();
      QTSession.close();

      environment.message("Done recording.");

    } catch (IOException e) {
      e.printStackTrace();

    } catch (QTException e) {
      //System.err.println("doing things");
      e.printStackTrace();
    }
  }


  // paintable methods

  public void newSizeNotified(QTImageDrawer drawer, Dimension d) {
    //System.out.println("notified size");
    if ((d.width != width) || (d.height != height)) {
      System.err.println("notified of size " + 
			 d.width + ", " + d.height + ", instead of " +
			 width + ", " + height);
    }
  }

  public Rectangle[] paint(Graphics g) {
    //System.out.println("painting");
    //g.setColor(Color.white);
    //g.fillRect(0, 0, width, height);
    //g.setColor(Color.red);
    //g.fillRect(0, 0, 20, 20);
    if (lastImage != null) {
      g.drawImage(lastImage, 0, 0, null);
      if (cursorVisible) {
	g.setColor(lastButton ? cursorDownColor : cursorUpColor);
	g.drawLine(lastX - 5, lastY, lastX + 5, lastY);
	g.drawLine(lastX, lastY - 5, lastX, lastY + 5);
      }
    } else {
      //System.out.println("drawing nothing");
    }
    return updateRects;
  }
}

/*
public class DbnRecorder {
  static DbnRecorder recorder;
  //static DbnRecorder lastRecorder;

  int width, height;

  long startTime;
  int pixelCount;
  DbnRecorderFrame head;  // first frame in linked list
  DbnRecorderFrame last;  // last frame that's been grabbed
  

  // should probably add an option to create a large number
  // of frames 'pre-cached'. byte array allocation takes a
  // while, so it'd be nice to have 1000 or so frames ready
  // to be used beforehand.

  // DONE
  // should also set a 'maximum' framerate, if things aren't
  // really keeping up. although that could also be a good 
  // post-processing step.

  // could also just make a stream of images, then fill in 
  // the blanks with the *same* image, to keep things at an
  // even 30fps. then the qt compressor could take care of
  // the work to remove the duplications.

  public DbnRecorder(int width, int height) {
    this.width = width;
    this.height = height;
  }

  static public void start(int width, int height) {
    recorder = new DbnRecorder(width, height);
    recorder.startTime = System.currentTimeMillis();
    //System.out.println("starting dbnrecorder");
  }

  static long then = 0;

  static public void addFrame(byte pixels[]) {
    //System.out.println("got frame");
    if (recorder == null) return;

    int now = (int) (System.currentTimeMillis() - recorder.startTime);
    if (now - then < 1000/30) return;

    DbnRecorderFrame newbie = new DbnRecorderFrame(now, pixels);
    if (recorder.head == null) {
      recorder.head = newbie;
    } else {
      recorder.last.next = newbie;
    }
    recorder.last = newbie;
    then = now;
  }

  // may not be useful for the version that includes the mouse cursor
  public void removeDuplicates() {
    // remove the first frame, probably worthless
    head = head.next;
    if (head == null) return;

    // removes frames that aren't any different from one another
    DbnRecorderFrame previous = head;
    DbnRecorderFrame current = head.next;
    while (current != null) {
      if (DbnRecorderFrame.equivalent(previous, current)) {
	// remove the frame from the list
	current.next = current.next.next;
      }
      current = current.next;
    }
  }

  public void calcDurations() {
    if (head == null) return;
    DbnRecorderFrame previous = head;
    DbnRecorderFrame current = head.next;
    while (current != null) {
      previous.duration = current.timestamp - previous.timestamp;
    }
    previous.duration = 1000; // last is one second?
  }

  static public void stop() {
    System.out.println("stopped recorder");
    //recorder.removeDuplicates();
    //recorder.calcDurations();
    try {
      writeFiles(recorder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    recorder = null;
    System.out.println("done stopping.");
  }

  static public void writePgmFiles(DbnRecorder rec) throws IOException {
    FileOutputStream smilfos = new FileOutputStream("sequence.smi");
    PrintStream smil = new PrintStream(smilfos);
    smil.println("<smil> <body> <seq>");

    int frameCount = 0;
    DbnRecorderFrame f = rec.head;
    DbnRecorderFrame lastFrame = null;
    while (f != null) {
      frameCount++;
      lastFrame = f;
      f = f.next;
    }
    int frameZeroCount = String.valueOf(frameCount).length();
    int timestampZeroCount = String.valueOf(lastFrame.timestamp).length();

    f = rec.head;
    int frameIndex = 0;
    while (f != null) {
      String filename = "frame-" + 
	zeroPad(frameIndex, frameZeroCount) + "-" +
	zeroPad(f.timestamp, timestampZeroCount) + ".pgm";
      FileOutputStream fos = new FileOutputStream(filename);
      fos.write(DbnEditor.makePgmData(f.pixels, rec.width, rec.height));
      fos.close();

      // <head> <layout> <region id="blah" width="101" height="101" />
      // </layout> </head>
      // dur="5s" region="blah"/>

      // subframes are 100ths of a frame
      int theSubframe = (f.timestamp / 3) % 100;

      int whatFrame = f.timestamp / (1000/30);
      int theFrame = whatFrame % 30;
      int whatSecond = whatFrame / 30;
      int theSecond = whatSecond % 60;
      int whatMinute = whatSecond / 60;
      int theMinute = whatMinute % 60;

      smil.println("<img src=\"" + filename + 
		   "\" clip-begin=\"00:" + 
		   zeroPad(theMinute, 2) + ":" + 
		   zeroPad(theSecond, 2) + ":" + 
		   zeroPad(theFrame, 2) + "." +
		   zeroPad(theSubframe, 2) + "\" />");

      frameIndex++;
      f = f.next;
    }

    smil.println("</seq> </body> </smil>");
  }


  static public void writeFiles(DbnRecorder rec) throws IOException {
    FileOutputStream smilfos = new FileOutputStream("sequence.smi");
    PrintStream smil = new PrintStream(smilfos);
    smil.println("<smil>");
    smil.println("<head> <layout> <region id=\"blah\" width=\"101\" height=\"101\" /> </layout> </head>");
    smil.println("<body>");

    int frameCount = 0;
    DbnRecorderFrame f = rec.head;
    DbnRecorderFrame lastFrame = null;
    while (f != null) {
      frameCount++;
      lastFrame = f;
      f = f.next;
    }
    int frameZeroCount = String.valueOf(frameCount).length();
    int timestampZeroCount = String.valueOf(lastFrame.timestamp).length();
    f = rec.head;
    int frameIndex = 0;
    while (f != null) {
      String filename = "frame-" + 
	zeroPad(frameIndex, frameZeroCount) + "-" +
	zeroPad(f.timestamp, timestampZeroCount) + ".gif";
      FileOutputStream fos = new FileOutputStream(filename);
      int array[] = new int[f.pixels.length];
      for (int i = 0; i < array.length; i++) {
	int g = f.pixels[i];
	array[i] = 0xff000000 | (g << 16) | (g << 8) | g;
      }
      new GifEncoder(array, rec.width, fos, false).encode();
      //fos.write(DbnEditor.makePgmData(f.pixels, rec.width, rec.height));
      fos.close();

      // subframes are 100ths of a frame
      int theSubframe = (f.timestamp / 3) % 100;

      int whatFrame = f.timestamp / (1000/30);
      int theFrame = whatFrame % 30;
      int whatSecond = whatFrame / 30;
      int theSecond = whatSecond % 60;
      int whatMinute = whatSecond / 60;
      int theMinute = whatMinute % 60;

      smil.print("<img src=\"" + filename + "\"");
		 //"\" clip-begin=\"" + millisToTimeCode(f.timestamp));
		 
      if (f.next != null) {
	//smil.print(" clip-end=\"" + 
	//   millisToTimeCode(f.next.timestamp-1) + "\"");
	//smil.print(" dur=\"" + 
	//   millisToTimeCode(f.next.timestamp - f.timestamp) + "\"");
	smil.print(" region=\"blah\" dur=\"1s\"");
      }
      smil.println(" />");

// 00:" + 
//		   zeroPad(theMinute, 2) + ":" + 
//		   zeroPad(theSecond, 2) + ":" + 
//		   zeroPad(theFrame, 2) + "." +
//		   zeroPad(theSubframe, 2) + "\" />");
//

      frameIndex++;
      f = f.next;
    }

    smil.println("</body> </smil>");
  }

  static String millisToTimeCode(int millis) {
      // subframes are 100ths of a frame
      int theSubframe = (millis / 3) % 100;

      int whatFrame = millis / (1000/30);
      int theFrame = whatFrame % 30;
      int whatSecond = whatFrame / 30;
      int theSecond = whatSecond % 60;
      int whatMinute = whatSecond / 60;
      int theMinute = whatMinute % 60;

      return "00:" + zeroPad(theMinute, 2) + ":" + 
	zeroPad(theSecond, 2) + ":" + 
	zeroPad(theFrame, 2) + "." +
	zeroPad(theSubframe, 2);
  }
  
  static String zeroPad(int what, int count) {
    StringBuffer buffer = new StringBuffer();
    String temp = String.valueOf(what);
    int strCount = temp.length();
    for (int i = 0; i < count - strCount; i++) {
      buffer.append('0');
    }
    buffer.append(temp);
    return buffer.toString();
  }
}

class DbnRecorderFrame {
  int timestamp;
  int duration;
  byte pixels[];
  DbnRecorderFrame next;

  public DbnRecorderFrame(int inTimestamp, byte inPixels[]) {
    timestamp = inTimestamp;
    int pixelCount = inPixels.length;
    pixels = new byte[pixelCount];
    System.arraycopy(inPixels, 0, pixels, 0, pixelCount);
  }

  static boolean equivalent(DbnRecorderFrame one, DbnRecorderFrame two) {
    int count = one.pixels.length;
    byte a[] = one.pixels;
    byte b[] = two.pixels;

    for (int i = 0; i < count; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}
*/

#endif
