import java.awt.*;
import java.io.*;
import java.net.*;


public class DbnIO
{
    DbnApplet app;

    public DbnIO(DbnApplet app) {
	this.app = app;
    }

    public String doLocalRead() {
	FileDialog fd = new FileDialog(new Frame(), 
				       "Open a DBN program...", 
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
	

    public boolean doLocalWrite(String s) {
	FileDialog fd = new FileDialog(new Frame(), 
				       "Save DBN program as...", 
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
		// no I18N, just blat out the low byte of each char
		byte data[] = new byte[s.length()];
		s.getBytes(0, s.length()-1, data, 0);
		output.write(data);
		output.flush();
		// NOT TESTED
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
	return true; // succesful
    }


    public boolean doLocalWritePgm(byte grayData[], int width, int height) {
	FileDialog fd = new FileDialog(new Frame(), 
				       "Save DBN program as...", 
				       FileDialog.SAVE);
	fd.show();	
	String directory = fd.getDirectory();
	if (directory == null) return false; // user cancelled
	File file = new File(directory, fd.getFile());

	byte imageData[] = new byte[grayData.length * 3];
	int index = 0;
	for (int i = 0; i < grayData.length; i++) {
	    imageData[index++] = grayData[i];
	}

	try {
	    FileOutputStream output = new FileOutputStream(file);
	    output.write(imageData);

	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
	return true; // succesful
    }
    
    
    // width and height for imageData are always 101x101
    public boolean doSnapshot(String programStr, byte imageData[]) {
	// adopted from a javaworld article, java tip #34
	try {
	    //String saveUrl = app.getParameter("save_url");
	    //URL url = new URL(saveUrl);
	    //URL url = app.getDocumentBase();
	    String document = app.getDocumentBase().toString();
	    document = document.substring(0, document.lastIndexOf("?"));
	    URL url = new URL(document);
	    System.err.println("url is " + url);

	    URLConnection conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    conn.setRequestProperty("Content-Type", 
				    "application/x-www-form-urlencoded");
	    
	    DataOutputStream printout = 
		new DataOutputStream(conn.getOutputStream());

	    //String user = app.getParameter("user");
	    //if (user == null) {
	    //System.err.println("no user name set");
	    //return false;
	    //}

	    String saveAs = app.getParameter("save_as");
	    String imageStr = new String(makePgmData(imageData, 101, 101));
	    String content = 
		"save_as=" + URLEncoder.encode(saveAs) + 
		"&save_image=" + URLEncoder.encode(imageStr) +
		"&save_program=" + URLEncoder.encode(programStr);

	    //System.err.println("here da content:");
	    //System.err.println(content);

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


    // dim is size of image (dim X dim) square
    // hexthumbnail is string of ascii encoded HEX (1 byte per pixel)
    // progtext is program
    /*
    public boolean doSnapshot(String progtext, String hexthumbnail, int dim) {
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
    */

    static public byte[] makePgmData(byte inData[], int width, int height) {
	//String headerStr = "P6 " + width + " " + height + " 255\n"; 
	String headerStr = "P5 " + width + " " + height + " 255\n";
	byte header[] = headerStr.getBytes();
	//int count = width * height * 3;
	int count = width * height;
	byte outData[] = new byte[header.length + count];
	System.arraycopy(header, 0, outData, 0, header.length);
	System.arraycopy(inData, 0, outData, header.length, count);
	return outData;
    }


    // apparently, instead of a space, things can be separated
    // by newlines. also, a # should be read as a comment char
    // also, this ignores the last param, which i'm always setting to 255

    static public byte[] parsePgmData(byte inData[], int dim[]) {
	//if (inData[0] != 'P' || inData[1] != '6')
	if (inData[0] != 'P' || inData[1] != '5')
	    return null;
	
	int index = 3;
	int value = 0;
	// should be isSpaceChar for 1.1
	while (!Character.isSpace((char)inData[index])) {
	    value = (value*10) + (inData[index] - '0');
	    index++;
	}
	dim[0] = value;
	while (Character.isSpace((char)inData[index++])) { } 
	
	value = 0;
	while (!Character.isSpace((char)inData[index])) {
	    value = (value*10) + (inData[index] - '0');
	    index++;
	}
	dim[1] = value;
	while (Character.isSpace((char)inData[index++])) { }
	
	value = 0;
	while (!Character.isSpace((char)inData[index++])) { 
	    value = (value*10) + (inData[index] - '0');
	    index++;
	}
	
	//int count = dim[0] * dim[1] * 3;
	int count = dim[0] * dim[1];
	byte outData[] = new byte[count];
	System.arraycopy(inData, index, outData, 0, count);
	return outData;
    }
}
