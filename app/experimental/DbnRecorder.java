#ifdef RECORDER

import java.io.*;


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

  /*
  static public boolean isRecording() {
    return (recorder != null);
  }
  */

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

  /*
  static public DbnRecorderFrame getMovie() {
    return lastRecorder.head;
  }
  */

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

      /*
	<head> <layout> <region id="blah" width="101" height="101" />
	</layout> </head>

	dur="5s" region="blah"/>
       */

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

/* 00:" + 
		   zeroPad(theMinute, 2) + ":" + 
		   zeroPad(theSecond, 2) + ":" + 
		   zeroPad(theFrame, 2) + "." +
		   zeroPad(theSubframe, 2) + "\" />");
*/

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

#endif
