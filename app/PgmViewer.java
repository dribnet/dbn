#ifdef PGM_VIEWER


import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;


// not a general pgm viewer, just reads dbn-created pgms
// would be easy to fix and make more general, but who need dat?

public class PgmViewer extends Applet {
  Image image;
  int pixels[];
  int width, height;
  //String document;
  URL documentUrl;
  Dimension appletSize;

  public void init() {
    try {
      String documentStr = getParameter("document_url");
      if (documentStr != null) {
	documentUrl = new URL(documentStr);
      }

      String imageStr = getParameter("image_url");
      if (imageStr == null) {
	throw new IOException("no image url set");
      }
      URL imageUrl = new URL(imageStr);
      InputStream stream = imageUrl.openStream();
      
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

      //System.out.println("width, height = " + width + ", " + height);
      while (temp[last] != '\n') last++;
	//System.out.println("tl = " + ((int)temp[last]));
      //last++;
      //}
      //      last++;

      pixels = new int[width * height];
      for (int i = 0; i < pixels.length; i++) {
	//System.out.println(i);
	int a = temp[last++] & 0xff;
	pixels[i] = 0xff000000 | (a << 16) | (a << 8) | a;
      }

    } catch (Exception e) {
      e.printStackTrace();
      pixels = null;
    }
    appletSize = size();
  }

  public void update(Graphics g) {
    paint(g);
  }

  // open url in browser
  public void paint(Graphics g) {
    if (image == null) {
      if (pixels == null) {
	paintError(g);
	return;
      }
      image = createImage(new MemoryImageSource(width, height, 
						pixels, 0, width));
    }
    if (image == null) {
      paintError(g);
      return;
    }
    g.drawImage(image, 0, 0, appletSize.width, appletSize.height, null);
  }

  private void paintError(Graphics g) {
    g.setColor(Color.red);
    g.fillRect(0, 0, appletSize.width, appletSize.height);
  }

  public boolean mouseDown(Event e, int x, int y) {
    if (documentUrl != null) {
      getAppletContext().showDocument(documentUrl);
    }
    return true;
  }
}

/*

  static public byte[] makePgmData(byte inData[], int width, int height) {
    //String headerStr = "P6 " + width + " " + height + " 255\n"; 
    String headerStr = "P5 " + width + " " + height + " 255\n";
#ifdef JDK11
    byte header[] = headerStr.getBytes();
#else
    byte header[] = new byte[headerStr.length()];
    headerStr.getBytes(0, header.length, header, 0);
#endif
    //int count = width * height * 3;
    int count = width * height;
    byte outData[] = new byte[header.length + count];
    System.arraycopy(header, 0, outData, 0, header.length);
    System.arraycopy(inData, 0, outData, header.length, count);
    return outData;
  }
*/


#endif
