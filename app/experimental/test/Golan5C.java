public class Golan5C extends DbnPlayer
{
  void handlebackground(int vx, int vy) throws DbnException
  {
    int c;
    int b;
    int a;
    int rt;
    int rr;
    int rl = 0;
    int rc;
    int rb;

    for (a = 1; a <= 5; a++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray(a, ((graphics.getArray(a)+vx)%rw5));
      if (graphics.getArray(a) < rwn)
      {
        rl = (graphics.getArray(a)+rw4);
      }
      if (graphics.getArray(a) >= rwn)
      {
        rl = (graphics.getArray(a)-rw);
      }
      if (a == 1)
      {
        rb = graphics.getArray(11);
        rb = (rb+vy);
        if (rb < 0)
        {
          rb = (rb+rh4);
        }
        for (b = 11; b <= 15; b++)
        {
          if (state != RUNNER_STARTED) return;
          sleepIfTired();
          graphics.setArray(b, (rb));
          rb = ((rb+rh)%rh4);
        }
      }
      for (c = 1; c <= 5; c++)
      {
        if (state != RUNNER_STARTED) return;
        sleepIfTired();
        rb = (graphics.getArray((c+10))-rh);
        rr = (rl+rw);
        rt = (rb+rh);
        rc = ((graphics.getArray((a+20))+graphics.getArray((c+30)))+fade);
        graphics.field(rl, rb, rr, rt, rc);
      }
    }
  }

  void drawbug(int bx, int by) throws DbnException
  {
    int y1;
    int y0;
    int ny0;
    int x1;
    int x0;
    int nx0;
    int yi;
    int xi;

    graphics.pen(85);
    x0 = (graphics.getArray((301))+bx);
    y0 = (graphics.getArray((401))+by);
    xi = x0;
    yi = y0;
    for (n = 2; n <= npts; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      x1 = (graphics.getArray((300+n))+bx);
      y1 = (graphics.getArray((400+n))+by);
      graphics.line(x0, y0, x1, y1);
      x0 = x1;
      y0 = y1;
    }
    graphics.line(x0, y0, xi, yi);
    x0 = (graphics.getArray((313))+bx);
    y0 = (graphics.getArray((413))+by);
    for (n = 14; n <= 16; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      x1 = (graphics.getArray((300+n))+bx);
      y1 = (graphics.getArray((400+n))+by);
      graphics.line(x0, y0, x1, y1);
      x0 = x1;
      y0 = y1;
    }
    nx0 = (graphics.getArray((317))+bx);
    ny0 = (graphics.getArray((417))+by);
    graphics.field(nx0, ny0, (nx0+2), (ny0+2), 70);
  }

  void wiggleflagellum() throws DbnException
  {
    tailt = ((tailt+1)%8);
    if (tailt == 0)
    {
      graphics.setArray(113, 0);
      graphics.setArray(114, (0-1));
      graphics.setArray(115, (0-2));
      graphics.setArray(116, (0-2));
    }
    if (tailt == 1)
    {
      graphics.setArray(113, 1);
      graphics.setArray(114, 0);
      graphics.setArray(115, (0-1));
      graphics.setArray(116, (0-3));
    }
    if (tailt == 2)
    {
      graphics.setArray(113, 1);
      graphics.setArray(114, 1);
      graphics.setArray(115, 0);
      graphics.setArray(116, (0-2));
    }
    if (tailt == 3)
    {
      graphics.setArray(113, 1);
      graphics.setArray(114, 2);
      graphics.setArray(115, 1);
      graphics.setArray(116, 0);
    }
    if (tailt == 4)
    {
      graphics.setArray(113, 0);
      graphics.setArray(114, 1);
      graphics.setArray(115, 2);
      graphics.setArray(116, 2);
    }
    if (tailt == 5)
    {
      graphics.setArray(113, (0-1));
      graphics.setArray(114, 0);
      graphics.setArray(115, 1);
      graphics.setArray(116, 3);
    }
    if (tailt == 6)
    {
      graphics.setArray(113, (0-1));
      graphics.setArray(114, (0-1));
      graphics.setArray(115, 0);
      graphics.setArray(116, 2);
    }
    if (tailt == 7)
    {
      graphics.setArray(113, (0-1));
      graphics.setArray(114, (0-2));
      graphics.setArray(115, (0-1));
      graphics.setArray(116, 0);
    }
  }

  int cos(int a) throws DbnException
  {
    return sin(((a+3000)%4000));
  }

  void rotatebug(int angle) throws DbnException
  {
    int oldy;
    int oldx;

    for (n = 13; n <= 16; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray((300+n), (graphics.getArray((100+n))));
      graphics.setArray((400+n), (graphics.getArray((200+n))));
    }
    cosa = cos(angle);
    sina = sin(angle);
    for (n = 1; n <= ntotalpts; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      oldx = graphics.getArray((100+n));
      oldy = graphics.getArray((200+n));
      graphics.setArray((300+n), (((oldx*cosa)+(oldy*sina))/50));
      graphics.setArray((400+n), (((oldy*cosa)-(oldx*sina))/50));
    }
  }

  int random(int range) throws DbnException
  {
    int bl;

    bl = (seed*bb+1);
    seed = (bl%mm);
    return (seed%range);
  }

  int sin(int a) throws DbnException
  {
    int t2;
    int b;
    int t;
    int t3;

    b = 50;
    if (a < 2000)
    {
      if (a < 1000)
      {
        t = (a/10);
        t2 = (t*t);
        t3 = (t2*t);
        b = (100-134*t2/10000+34*t3/1000000);
        b = (b/2+50);
      }
      if (a >= 1000)
      {
        t = ((2000-a)/10);
        t2 = (t*t);
        t3 = (t2*t);
        b = (100-134*t2/10000+34*t3/1000000);
        b = (50-b/2);
      }
    }
    if (a >= 2000)
    {
      if (a < 3000)
      {
        t = ((a-2000)/10);
        t2 = (t*t);
        t3 = (t2*t);
        b = (100-134*t2/10000+34*t3/1000000);
        b = (50-b/2);
      }
      if (a >= 3000)
      {
        t = ((4000-a)/10);
        t2 = (t*t);
        t3 = (t2*t);
        b = (100-134*t2/10000+34*t3/1000000);
        b = (b/2+50);
      }
    }
    return (b-50);
  }

  int bugangle;
  int ry;
  int rx;
  int rw;
  int fade;
  int rwn;
  int bb;
  int rh5;
  int rh4;
  int rh;
  int sina;
  int slowness;
  int tailt;
  int cosa;
  int cy;
  int cx;
  int vy;
  int vx;
  int rhn;
  int npts;
  int ntotalpts;
  int rw5;
  int rw4;
  int seed;
  int v;
  int n;
  int bs;
  int mm;

  public void execute() throws DbnException
  {
    cx = 50;
    cy = 50;
    graphics.paper(50);
    rx = 0;
    ry = 0;
    rw = 40;
    rh = 40;
    rw4 = (4*rw);
    rw5 = (5*rw);
    rwn = (0-rw);
    rh4 = (4*rh);
    rh5 = (5*rh);
    rhn = (0-rh);
    graphics.setArray(21, 10);
    graphics.setArray(22, 20);
    graphics.setArray(23, 15);
    graphics.setArray(24, 20);
    graphics.setArray(25, 15);
    graphics.setArray(31, 4);
    graphics.setArray(32, 8);
    graphics.setArray(33, 12);
    graphics.setArray(34, 8);
    graphics.setArray(35, 0);
    npts = 12;
    ntotalpts = 17;
    graphics.setArray(101, 10);
    graphics.setArray(201, 1);
    graphics.setArray(102, 14);
    graphics.setArray(202, 3);
    graphics.setArray(103, 18);
    graphics.setArray(203, 9);
    graphics.setArray(104, 20);
    graphics.setArray(204, 19);
    graphics.setArray(105, 18);
    graphics.setArray(205, 29);
    graphics.setArray(106, 15);
    graphics.setArray(206, 34);
    graphics.setArray(107, 10);
    graphics.setArray(207, 37);
    graphics.setArray(108, 5);
    graphics.setArray(208, 34);
    graphics.setArray(109, 2);
    graphics.setArray(209, 29);
    graphics.setArray(110, 0);
    graphics.setArray(210, 19);
    graphics.setArray(111, 2);
    graphics.setArray(211, 9);
    graphics.setArray(112, 6);
    graphics.setArray(212, 3);
    graphics.setArray(113, 10);
    graphics.setArray(213, 36);
    graphics.setArray(114, 10);
    graphics.setArray(214, 41);
    graphics.setArray(115, 10);
    graphics.setArray(215, 46);
    graphics.setArray(116, 10);
    graphics.setArray(216, 51);
    graphics.setArray(117, 11);
    graphics.setArray(217, 10);
    for (n = 1; n <= ntotalpts; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray((100+n), (graphics.getArray((100+n))-10));
      graphics.setArray((200+n), (graphics.getArray((200+n))-19));
      graphics.setArray((300+n), (graphics.getArray((100+n))));
      graphics.setArray((400+n), (graphics.getArray((200+n))));
    }
    tailt = 0;
    cosa = 0;
    sina = 0;
    for (v = 1; v <= 5; v++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray(v, ((v-1)*rw));
      graphics.setArray((v+10), ((v-1)*rh));
    }
    fade = 80;
    bb = 198621;
    mm = 98621;
    seed = graphics.getTime(4);
    bugangle = (random(4000)%4000);
    bs = 100;
    graphics.norefresh();
    while (state == RUNNER_STARTED)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      slowness = (bs/12);
      vx = (sina/slowness);
      vy = (cosa/slowness);
      if (fade >= 1)
      {
        fade = (fade-4);
      }
      handlebackground(vx, vy);
      bs = (((9*bs)+(100+random(100)))/10);
      bugangle = ((((49*bugangle)+(random(4000)))/50)%4000);
      wiggleflagellum();
      rotatebug(bugangle);
      drawbug(cx, cy);
      graphics.refresh();
    }
  }

  long lastSleepTime;
  private final void sleepIfTired()
  {
    long t = System.currentTimeMillis();
    if (t - lastSleepTime < 1000) return;
    try {
      thread.sleep(5);
      lastSleepTime = t + 5;
    } catch (InterruptedException e) { }
  }
}
