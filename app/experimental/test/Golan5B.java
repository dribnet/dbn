public class Golan5B extends DbnPlayer
{
  int abs(int in) throws DbnException
  {
    int out;

    out = in;
    if (in < 0)
    {
      out = (0-in);
    }
    return out;
  }

  int sqrt(int in) throws DbnException
  {
    int out;
    int i;

    out = (in/2);
    for (i = 1; i <= 8; i++)
    {
      //if (state != RUNNER_STARTED) return;
      //sleepIfTired();
      out = ((out+(in/out))/2);
    }
    return out;
  }

  int cos(int a) throws DbnException
  {
    return sin(((a+3000)%4000));
  }

  void init() throws DbnException
  {
    int y1;
    int sina;
    int cosa;
    int nny;
    int nnx;
    int x1;
    int vy;
    int vx;
    int n;
    int k;

    for (n = 1; n <= npoints; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      angle = (angle+da);
      if (angle >= 4000)
      {
        angle = (angle%4000);
      }
      cosa = (cos(angle)-50);
      sina = (sin(angle)-50);
      x1 = (cx+cosa*rad);
      y1 = (cy+sina*rad);
      graphics.setArray(n, x1);
      graphics.setArray((n+npoints), y1);
    }
    nnx = (cx/scale);
    nny = (cy/scale);
    for (k = 1; k <= nmitochondria; k++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray((500+k), (nnx+random(10)));
      graphics.setArray((600+k), (nny+random(10)));
      vx = random(3);
      vy = random(3);
      graphics.setArray((700+k), vx);
      graphics.setArray((800+k), vy);
      if (vx == 0)
      {
        if (vy == 0)
        {
          k = (k-1);
        }
      }
    }
  }

  int sgn(int in) throws DbnException
  {
    int out;

    out = 1;
    if (in < 0)
    {
      out = (0-1);
    }
    return out;
  }

  void dosimulation(int j) throws DbnException
  {
    int ndy;
    int ndx;
    int ndys;
    int nyy;
    int ny;
    int nx;
    int sy;
    int sx;
    int py;
    int px;
    int ndxs;
    int s;
    int nxx;
    int n;

    nx = graphics.getArray((500+j));
    ny = graphics.getArray((600+j));
    ndx = graphics.getArray((700+j));
    ndy = graphics.getArray((800+j));
    if (nx < 0)
    {
      ndx = (0-ndx);
      nx = (50+random(6));
    }
    if (nx >= 100)
    {
      ndx = (0-ndx);
      nx = (50+random(6));
    }
    if (ny < 0)
    {
      ndy = (0-ndy);
      ny = (50+random(6));
    }
    if (ny >= 100)
    {
      ndy = (0-ndy);
      ny = (50+random(6));
    }
    if (graphics.getPixel(nx, ny) == linecolor)
    {
      ndx = ((0-sgn(ndx))*(random(3)+4)/2);
      ndy = ((0-sgn(ndy))*(random(3)+4)/2);
      ndxs = (ndx*scale2);
      ndys = (ndy*scale2);
      nxx = (nx*scale);
      nyy = (ny*scale);
      for (n = 1; n <= npoints; n++)
      {
        if (state != RUNNER_STARTED) return;
        sleepIfTired();
        px = graphics.getArray(n);
        py = graphics.getArray((n+npoints));
        sx = (px-nxx);
        sy = (py-nyy);
        s = (abs(sx)+abs(sy));
        if (s < tol)
        {
          graphics.setArray(n, (px-ndxs));
          graphics.setArray((n+npoints), (py-ndys));
        }
      }
    }
    graphics.setArray((500+j), (nx+ndx));
    graphics.setArray((600+j), (ny+ndy));
    graphics.setArray((700+j), ndx);
    graphics.setArray((800+j), ndy);
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
    return b;
  }

  void smooth() throws DbnException
  {
    int py2;
    int py1;
    int py0;
    int y2;
    int y1;
    int y0;
    int px2;
    int px1;
    int px0;
    int x2;
    int x1;
    int x0;
    int n;

    x0 = npoints;
    x1 = 1;
    x2 = 2;
    y0 = (npoints2);
    y1 = (1+npoints);
    y2 = (2+npoints);
    px0 = graphics.getArray(x0);
    py0 = graphics.getArray(y0);
    px1 = graphics.getArray(x1);
    py1 = graphics.getArray(y1);
    px2 = graphics.getArray(x2);
    py2 = graphics.getArray(y2);
    graphics.setArray(x1, ((px0+(smoothd2*px1)+px2)/smoothd));
    graphics.setArray(y1, ((py0+(smoothd2*py1)+py2)/smoothd));
    for (n = 2; n <= nptsm1; n++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      x1 = n;
      x0 = (n-1);
      x2 = (n+1);
      y1 = (n+npoints);
      y0 = (n+npoints-1);
      y2 = (n+npoints+1);
      px0 = graphics.getArray(x0);
      py0 = graphics.getArray(y0);
      px1 = graphics.getArray(x1);
      py1 = graphics.getArray(y1);
      px2 = graphics.getArray(x2);
      py2 = graphics.getArray(y2);
      graphics.setArray(x1, ((px0+(smoothd2*px1)+px2)/smoothd));
      graphics.setArray(y1, ((py0+(smoothd2*py1)+py2)/smoothd));
    }
    x0 = nptsm1;
    x1 = npoints;
    x2 = 1;
    y0 = (npoints+nptsm1);
    y1 = (npoints2);
    y2 = (1+npoints);
    px0 = graphics.getArray(x0);
    py0 = graphics.getArray(y0);
    px1 = graphics.getArray(x1);
    py1 = graphics.getArray(y1);
    px2 = graphics.getArray(x2);
    py2 = graphics.getArray(y2);
    graphics.setArray(x1, ((px0+(smoothd2*px1)+px2)/smoothd));
    graphics.setArray(y1, ((py0+(smoothd2*py1)+py2)/smoothd));
  }

  int y1p1;
  int nmitochondria;
  int nptsm1;
  int x0p1;
  int y1m1;
  int bb;
  int scale2;
  int npoints;
  int x0m1;
  int y1;
  int y0;
  int rad;
  int v1;
  int linecolor;
  int npoints4;
  int da;
  int npoints3;
  int h1;
  int npoints2;
  int x1p1;
  int cy;
  int cx;
  int x1;
  int x0;
  int y0p1;
  int x1m1;
  int y0m1;
  int smoothd2;
  int seed;
  int q;
  int angle;
  int n;
  int j;
  int smoothd;
  int tol;
  int scale;
  int mm;

  public void execute() throws DbnException
  {
    graphics.norefresh();
    bb = 198621;
    mm = 98621;
    seed = graphics.getTime(4);
    scale = 10000;
    tol = (10*scale);
    scale2 = (4*scale);
    linecolor = 100;
    npoints = 25;
    nptsm1 = (npoints-1);
    nmitochondria = 5;
    npoints2 = (npoints*2);
    npoints3 = (npoints*3);
    npoints4 = (npoints*4);
    angle = 0;
    rad = (50*scale/100);
    cx = (50*scale);
    cy = (50*scale);
    da = (4000/npoints);
    for (q = 200; q <= 400; q++)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.setArray(q, (50+random(50)));
    }
    smoothd = 14;
    smoothd2 = (smoothd-2);
    init();
    while (state == RUNNER_STARTED)
    {
      if (state != RUNNER_STARTED) return;
      sleepIfTired();
      graphics.paper(40);
      for (j = 1; j <= nmitochondria; j++)
      {
        if (state != RUNNER_STARTED) return;
        sleepIfTired();
        h1 = graphics.getArray((500+j));
        v1 = graphics.getArray((600+j));
        graphics.field((h1-1), (v1-2), (h1+1), (v1+2), 75);
        graphics.field((h1-2), (v1-1), (h1+2), (v1+1), 75);
        graphics.setPixel(h1, v1, 50);
      }
      smooth();
      x0 = (graphics.getArray(npoints)/scale);
      y0 = (graphics.getArray((npoints+npoints))/scale);
      graphics.pen(linecolor);
      for (n = 1; n <= npoints; n++)
      {
        if (state != RUNNER_STARTED) return;
        sleepIfTired();
        x1 = (graphics.getArray(n)/scale);
        y1 = (graphics.getArray((n+npoints))/scale);
        x0m1 = (x0-1);
        x0p1 = (x0+1);
        y0m1 = (y0-1);
        y0p1 = (y0+1);
        x1m1 = (x1-1);
        x1p1 = (x1+1);
        y1m1 = (y1-1);
        y1p1 = (y1+1);
        graphics.line(x0, y0, x1, y1);
        graphics.line(x0m1, y0, x1m1, y1);
        graphics.line(x0p1, y0, x1p1, y1);
        graphics.line(x0, y0p1, x1, y1p1);
        graphics.line(x0m1, y0p1, x1m1, y1p1);
        graphics.line(x0p1, y0p1, x1p1, y1p1);
        graphics.line(x0, y0m1, x1, y1m1);
        graphics.line(x0p1, y0m1, x1p1, y1m1);
        graphics.line(x0m1, y0m1, x1m1, y1m1);
        x0 = x1;
        y0 = y1;
      }
      for (j = 1; j <= nmitochondria; j++)
      {
        if (state != RUNNER_STARTED) return;
        sleepIfTired();
        dosimulation(j);
      }
      graphics.refresh();
      if (graphics.getMouse(3) == 100)
      {
        init();
      }
    }
  }

  long lastSleepTime;
  int howmany = 0;
  private final void sleepIfTired()
  {
    return;
    /*
    if ((howmany % 1000) != 0) {
      howmany++;
      return;
    }
    long t = System.currentTimeMillis();
    if (t - lastSleepTime < 1000) return;
    try {
      thread.sleep(5);
      lastSleepTime = t + 5;
    } catch (InterruptedException e) { }
    */
  }
}
