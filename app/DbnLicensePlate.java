import java.awt.*;

// only used by gui, did not used to be public

public class DbnLicensePlate extends Panel implements Runnable {
    DbnGui gui;
    Font f = new Font("Helvetica", Font.PLAIN, 10);
    Thread thread;
    Image im;
    Graphics img;
    
    long birtht;
    int cbw, cbh;
    
    char cs[];
    int xs[];
    int xso[];
    int ys[];
    int slen;
    int dxs[];
    int dys[];
    Color cols[];
	
    String s1 = "Design By Numbers ";
    int s1x, s1y;
    String s2 = "MIT Media Laboratory. " +
	"Aesthetics + Computation Group. " +
	"(C) 1999, Massachusetts Institute of Technology";
    int s2x, s2y;

	
    public DbnLicensePlate(DbnGui gui) {
	super();
	this.gui = gui;
    }
    
    public boolean mouseDown(Event ev, int mx, int my)
    {
	if (thread==null) {
	    thread = new Thread(this);
	    thread.start();
	} else {
	    thread.stop();
	    thread = null;
	    unlaunch();
	}
	return true;
    }
    
    public void start() { }
    public void stop() { }
    
    public void launch(long t)
    {
	int i,k, x;
	Graphics g = this.getGraphics();
	g.setFont(f);
		
	int s1len = s1.length(), s2len = s2.length();
	cs = new char[s1len+s2len];
	xs = new int[s1len+s2len];
	xso = new int[s1len+s2len];
	ys = new int[s1len+s2len];
	dxs = new int[s1len+s2len];
	dys = new int[s1len+s2len];
	cols = new Color[s1len+s2len];
		
	x = s1x;
	for(i=0,k=0;i<s1len;i++,k++) {
	    cs[k] = s1.charAt(i); 
	    ys[k] = s1y;
	    xso[k]=xs[k] = x;
	    x+=g.getFontMetrics().stringWidth(String.valueOf(cs[k]));
	    dxs[k] = cs[k]/4;
	    dys[k] = 0;//cs[k]&0x3;
	    cols[k] = Color.white;
	}

	x = s2x;
	for(i=0;i<s2len;i++,k++) {
	    cs[k] = s2.charAt(i); 
	    ys[k] = s1y;
	    xso[k]=xs[k] = x;
	    x+=g.getFontMetrics().stringWidth(String.valueOf(cs[k]));
	    dxs[k] = cs[k]/4;
	    dys[k] = 0;//cs[k]&0x3;
	    cols[k] = Color.gray;
	}
	slen = s1len+s2len;
		
	birtht = t;
    }
	
    public void unlaunch()
    {
	int k,x,y;
	int n = 20;
	long startt = System.currentTimeMillis();
	long curt = System.currentTimeMillis();
	while((curt-startt)<1000) {
	    Graphics g = this.getGraphics();
	    int i;
	    img.setColor(Color.darkGray);
	    img.fillRect(0,0,cbw,cbh);

	    img.setColor(Color.white);
	    img.setFont(f);
	    float a = (curt-startt)/1000f;
	    a = (1-a);
	    a = a*a;
	    a = 1-a;
	    for(i=0;i<slen;i++) {
		//xs[i]=dxs[i];
		//ys[i]=dys[i];
		x = (int)(xs[i]+a*(xso[i]-xs[i]));
		y = ys[i];
		img.setColor(cols[i]);
		img.drawString(String.valueOf(cs[i]), x, y);
	    }		

	    g.drawImage(im,0,0,this);
	    curt = System.currentTimeMillis();
	}
	paint(this.getGraphics());
    }
	
    public void animate(long t)
    {
	Graphics g = this.getGraphics();
	int i;
	img.setColor(Color.darkGray);
	img.fillRect(0,0,cbw,cbh);

	img.setColor(Color.white);
	img.setFont(f);
	for(i=0;i<slen;i++) {
	    xs[i]+=dxs[i];
	    ys[i]+=dys[i];
	    if (xs[i]>cbw) xs[i]-=cbw;
	    if (ys[i]>cbh) ys[i]-=cbh;
	    if (xs[i]<0) xs[i]+=cbw;
	    if (ys[i]<0) ys[i]+=cbh;
	    img.setColor(cols[i]);
	    img.drawString(String.valueOf(cs[i]),xs[i],ys[i]);
	}		

	g.drawImage(im,0,0,this);
    }
	
    public void run()
    {
	launch(System.currentTimeMillis());
	try {
	    while(true) {
		animate(System.currentTimeMillis());
		Thread.sleep(20);
	    }
	} catch(Exception e) {
	}
    }
	
    public void paint(Graphics gv)
    {
	Rectangle r = bounds();
	
	if (r.width>1) {
	    cbw = r.width; cbh = r.height;
	    im = createImage(r.width,r.height);
	    img = im.getGraphics();

	}
	Graphics g = img;
		
	g.setColor(Color.darkGray); g.fillRect(0,0,r.width,r.height);
	g.setFont(f);
	g.setColor(Color.white);
	g.drawString(s1, s1x=5,s1y=16);
	g.setColor(Color.gray);
	g.drawString(s2, s2x=g.getFontMetrics().stringWidth(s1)+4+s1x,s2y=s1y);
	g.setColor(Color.black);
	g.drawLine(0,r.height-1,r.width,r.height-1);
		
	gv.drawImage(im,0,0,this);	
    }

    public Dimension preferredSize()
    {
        return new Dimension(48,24);
    }	
}
