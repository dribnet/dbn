import java.awt.*;
import java.io.*;


public class PockyBuilder {
  static final int IWIDTH = 101;
  static final int IHEIGHT = 101;
  static final int COLUMNS = 46;   // 46x101 = 4646

  int data[][];
  int count;

  FileOutputStream fileListing;


  static public void main(String args[]) {
    new PockyBuilder();
  }

  public PockyBuilder() {
    try {
      data = new int[3000][];

      fileListing = new FileOutputStream("pocky.list");
      File root = new File("courses");
      recurse(root);
      //write();

      fileListing.flush();
      fileListing.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void write() throws IOException {
    FileOutputStream output = new FileOutputStream("pocky.raw");
    int ticker = 0;
    for (int k = 0; k < (count-COLUMNS)+1; k += COLUMNS) {
      for (int j = 0; j < 101; j++) {
	for (int i = 0; i < COLUMNS; i++) {
	  //output.write(data[k+i], j*101, 101);
	  int which[] = data[k+i];
	  for (int x = 0; x < 101; x++) {
	    int gray = which[j*101 + x];
	    gray = (255 * (101-gray)) / 101;
	    output.write((byte) gray);
	    //output.write((byte) gray);
	    //output.write((byte) gray);
	  }
	}
      }
      System.out.println("k is " + k);
    }
    output.flush();
    output.close();
  }

  protected void recurse(File entry) throws IOException {
    if (entry.isDirectory()) {
      //System.out.println("entering " + entry);
      String list[] = entry.list();
      for (int i = 0; i < list.length; i++) {
	recurse(new File(entry, list[i]));
      }
    } else { 
      String filename = entry.getName(); 
      //System.out.println(entry);
      if (filename.indexOf(".pgm") != -1) { 
	data[count++] = readPgmFile(entry); 
	System.out.println("read " + count + 
			   ((count == 1) ? " image.." : " images.."));
      }
    }
  }

  public int[] readPgmFile(File file) throws IOException {
    String name = file.toString();
    String subname = name.substring(0, name.length()-3);
    //fileListing.write(file.toString().getBytes());
    String newname = subname + "dbn";
    fileListing.write(newname.getBytes());
    fileListing.write((byte)10);

    FileInputStream stream = new FileInputStream(file);
    int width, height;
    int pixels[];

    byte temp[] = new byte[16 * 1024];  // 16k
    int offset = 0;
    while (true) {
      int byteCount = stream.read(temp, offset, 1024);
      if (byteCount <= 0) break;
      offset += byteCount;
    }

    //byte program[] = new byte[offset];
    if ((temp[0] != 'P') || (temp[1] != '5')) {
      throw new IOException("invalid pgm format file");
    }
    int start = 3;
    int last = start + 1;
    while (temp[last] != ' ') last++;
    width = Integer.parseInt(new String(temp, start, last-start));

    start = last + 1;
    last = start + 1;
    while (temp[last] != ' ') last++;
    height = Integer.parseInt(new String(temp, start, last-start));

    while ((temp[last] != '\n') && (temp[last] != '\r')) last++; 
    
    pixels = new int[width * height];
    for (int i = 0; i < pixels.length; i++) {
      //System.out.println(i);
      int a = temp[last++] & 0xff;
      pixels[i] = 0xff000000 | (a << 16) | (a << 8) | a;
    }
    return pixels;
  }
}
