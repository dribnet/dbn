// The encoding inputReader/outputRead stuff is based on code from:
// Java Examples in a Nutshell (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples

// the hex conversion code and the re-packaging by Ben Fry (fry@netscape.com)


import java.io.*;


public class Encoding2Ascii {
    static final byte hex[] = { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static void main(String[] args) {
	String from = null, to = null;
    	String infile = null, outfile = null;

    	for (int i = 0; i < args.length; i++) {  // Parse command-line arguments.
	    if (i == args.length-1) usage();      // All legal args require another.
	    if (args[i].equals("-from")) from = args[++i];
	    //else if (args[i].equals("-to")) to = args[++i];
	    else if (args[i].equals("-in")) infile = args[++i];
	    else if (args[i].equals("-out")) outfile = args[++i];
	    else usage();
	}

    	try {
	    convert(infile, outfile, from, to);
    	}
    	catch (Exception e) {
	    System.err.println("error during conversion: " + e);
	    System.exit(1);
    	}
    }


    public static void usage() {
	System.err.println("Encoding2Ascii, converts a file in another encoding\n" +
			   "to a plain text file using unicode-style escape characters.\n"+
			   "Based on an example from Java In a Nutshell\n" +
			   "Hacked up and redone by Ben Fry (fry@netscape.com)\n");

    	System.err.println("  Usage: java Encoding2Ascii <options> or\n" +
    	                   "         enc2ascii <options> on windows\n\n");

        System.err.println("Options: -from <encoding>\n" +
                           "         -in <input file>\n" +
                           "         -out <output file>");
    	System.exit(1);
    }


    public static void convert(String infile, String outfile, String from, String to)
	throws IOException, UnsupportedEncodingException
    {
    	// Set up byte streams.
    	InputStream in;
    	if (infile != null)
	    in = new FileInputStream(infile);
    	else
	    in = System.in;

    	OutputStream out;
    	if (outfile != null)
	    out = new FileOutputStream(outfile);
    	else
	    out = System.out;

    	// Use default encoding if no encoding is specified.
    	if (from == null) from = System.getProperty("file.encoding");
    	if (to == null) to = System.getProperty("file.encoding");

    	// Set up character streams.
    	BufferedReader r = new BufferedReader(new InputStreamReader(in, from));
    	BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, to));

	while (true) {
	    String line = r.readLine();
	    if (line != null) {
		w.write(convert(line));
		w.newLine();
	    } else {
		break;
	    }
	}
	r.close();
	w.flush();
	w.close();
    }


    static private String convert(String what) {
	char chars[] = what.toCharArray();
	int length = chars.length;
	//System.err.println("length is " + length);

	StringBuffer out = new StringBuffer();
	for (int i = 0; i < length; i++) {
	    out.append("\\u");
	    int value = (int) chars[i];
	    //System.out.println(value);

	    //int top = (value & 0xFF00) >> 16;
	    int top = (value & 0xFF00) >> 8;
	    out.append((char) hex[top/16]);
	    out.append((char) hex[top%16]);
	    //System.out.println(top);

	    int bottom = (value & 0x00FF);
	    out.append((char) hex[bottom/16]);
	    out.append((char) hex[bottom%16]);
	}
	return out.toString();
    }
}
