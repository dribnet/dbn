#ifdef KVM

public class KvmLacunae {
  static KvmLacunae buddy = new KvmLacunae();

  static final float sqrt(float in) {
    float out = in/2.0f;
    for (int i = 0; i < 10; i++) {
      out = ((out + (in/out))/2);
    }
    return out;
  }

  static final boolean isSpace(char c) {
    return (c == ' ');
  }

  static final int getHours() {
    return -1;
  }

  static final int getMinutes() {
    return -1;
  }

  static final int getSeconds() {
    return -1;
  }

  static final int indexOf(String input, String find) {
    return -1;
  }
}


public class Dimension {
  public Dimension(int w, int h) {
  }
}


public class Color {
  static final Color white = new Color(0xff, 0xff, 0xff);
  
  public Color(int r, int g, int b) {
  }
}


public class Event {
}


public class Image {
  public Graphics getGraphics() {
    return null;
  }
}


public class Panel {
  public Image createImage(int width, int height) {
    return null;
  }

  public Graphics getGraphics() {
    return null;
  }
}


public class Graphics {
  public void setColor(Color c) {
  }

  public void fillRect(int x, int y, int width, int height) {
  }

  public void drawLine(int x1, int y1, int x2, int y2) {
  }

  public void drawImage(Image image, int x, int y, Object ignored) {
  }
}

#endif
