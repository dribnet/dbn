import java.awt.*;
import java.io.*;
import java.net.*;


public class DbnIO
{
    DbnApplet app;

    
    public DbnIO(DbnApplet app)
    {
	//super();
	this.app = app;
    }	


    public String doLocalRead()
    {
	FileDialog fd = new FileDialog(new Frame(), "Open a DBN program...", 
				       FileDialog.LOAD);
	fd.show();
	
	String directory = fd.getDirectory();
	if (directory == null) return null; // user cancelled
	File file = new File(directory, fd.getFile());

	try {
	    FileInputStream input = new FileInputStream(file);
	    int length = (int) file.length();
	    byte data[] = new byte[length];
	    
	    int count = 0;
	    while (count != length) {
		data[count++] = (byte) input.read();
	    }
	    // once read all the bytes, convert it to the proper
	    // local encoding for this system.
	    return app.languageEncode(data);

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	    return null;
	
	} catch (IOException e2) {
	    e2.printStackTrace();
	    return null;
	}
    }
	

    public boolean doLocalWrite(String s)
    {
	FileDialog fd = new FileDialog(new Frame(), "Save DBN program as...", 
				       FileDialog.SAVE);
	fd.show();
	
	String directory = fd.getDirectory();
	if (directory == null) return false; // user cancelled
	File file = new File(directory, fd.getFile());

	try {
	    if (app.I18N) {
		FileWriter writer = new FileWriter(file);
		writer.write(s);
		writer.flush();
		writer.close();
	    } else {
		FileOutputStream output = new FileOutputStream(file);
		// no I18N, just blat out the low bytes
		byte data[] = new byte[s.length()];
		s.getBytes(0, s.length()-1, data, 0);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
	return true; // succesful
    }
    
    
    // dim is size of image (dim X dim) square
    // hexthumbnail is string of ascii encoded HEX (1 byte per pixel)
    // progtext is program
    public boolean doSnapshot(String progtext, String hexthumbnail, int dim)
    {
	// adopted from a javaworld article, java tip #34
	try {
	    URL url = new URL("http://" +
			      app.getCodeBase().getHost().toString() + 
			      "/save/snapshot.php3");
	    //System.err.println("url is " + url);
	    URLConnection conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    conn.setRequestProperty("Content-Type", 
				    "application/x-www-form-urlencoded");
	    
	    DataOutputStream printout = 
		new DataOutputStream(conn.getOutputStream());
	    
	    String user = app.getParameter("user");
	    if (user == null) {
		System.err.println("no user name set");
		return false;
	    }

	    //System.err.println("user is " + user);
	    //System.err.println("dim is " + dim);
	    //System.err.println("thumbnail is " + hexthumbnail);
	    //System.err.println("program is " + progtext);

	    String content = 
		"user=" + URLEncoder.encode(user) +
		"&dim=" + URLEncoder.encode(String.valueOf(dim)) +
		"&thumbnail=" + URLEncoder.encode(hexthumbnail) +
		"&program=" + URLEncoder.encode(progtext);

	    printout.writeBytes(content);
	    printout.flush();
	    printout.close();
	    
	    // what did they say back?
	    DataInputStream input = 
		new DataInputStream(conn.getInputStream());
	    String str = null;
	    while ((str = input.readLine()) != null) {
		System.out.println(str);
	    }
	    input.close();
	    
	    return true; // successful

	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }
}
