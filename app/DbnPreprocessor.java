import java.awt.*;
import java.applet.Applet;
import java.util.*;


/*
 * the job of dbnpreproc is to hold onto/manage language translation table
 * as well as manage the 'load' command
 *
 * i.e. load http://acg.media.mit.edu/foo.dbn
 *      load foo.dbn (assuming same path)
 *      -> need path resoution issue
 *
 * if no .dbn extension then gets passed as is to parser
 */ 
public class DbnPreprocessor {
    DbnGui gui;
    DbnApplet applet;

    // this is sticky, because the textfield really wants its
    // own type of newlines. this is because the parser needs
    // a newline at the end, and it wasn't getting it unless
    // it's the real type of newline. so, it's an unfortunate 
    // addition (all these vars) but it's gotta be.
    static int eolCount;
    static char eol[];
    static {
	/*
	String separator = System.getProperty("line.separator");
	eolCount = separator.length();
	eol = new char[eolCount];
	for (int i = 0; i < eolCount; i++) {
	    eol[i] = separator.charAt(i);
	}
	*/
	
	// modified for dbn-ng, because it uses only \n internally
	eolCount = 1;
	eol = new char[1];
	eol[0] = '\n';
    }

    public DbnPreprocessor(DbnGui gui, DbnApplet applet) {
	this.gui = gui;
	this.applet = applet;
	
	// build hashtable here
	// with some nicely defined param interface for language 
	// replacement don't forget that some languages have 2-byte 
	// code. want to define the 'space' character replacement
	// perhaps as enclosuers in singlebyte parens?
	// (#),( ),(papel),(paper) or some universally unicode 
	// compliant enclosure of a string start/end then can 
	// stringtokenize based upon whitespace char/chars.
	// * try not to make too much garbage.
    }
    
    // hopefully this will just inline
    private final char[] growArray(char stuff[]) {
	if (stuff == null) return new char[16384];
	char temp[] = new char[stuff.length*2];
	System.arraycopy(stuff, 0, temp, 0, stuff.length);
	return stuff;
    }

    // applet parameter tag has language en, ja, es, de
    // can't mix multiple languages together
    public char[] translateLanguage(char program[]) {
#ifndef JDK11
	return program;
#else
	// default language (english), no changes
	char[][] table = applet.getLanguageTable();
	char[][] translated = applet.getEnglishTable();
	if (table == null) return program;

	int expansionIndex = 0;
	char[] expansion = new char[program.length*2];
	
	int lastChunk = 0;

	// at this point, the applet has already converted all
	// of the input data (file or otherwise) to unicode
	// in the format of chars. all that's left is to
	// substitute the keyword strings with english versions.
	int indices[] = new int[applet.getKeywordCount()];

	//klens = table[i].length;

	for (int i = 0; i < program.length; i++) {
	    if (Character.isWhitespace(program[i]) ||
		(i == program.length)) {
		for (int j = 0; j < indices.length; j++) {
		    // see if anything matched
		    if (indices[j] == table[j].length) {
			// make sure there's enough room
			//int needed = expansionIndex + (i-lastChunk) + indices[j];
			int needed = (expansionIndex + (i-lastChunk) +
				      translated[j].length);
			if (expansion.length < needed) {
			    expansion = growArray(expansion);
			}
			// save everything so far, except for the keyword
			System.arraycopy(program, lastChunk,
					 expansion, expansionIndex,
					 i - lastChunk - indices[j]);
			expansionIndex += i - lastChunk - indices[j];
			// spout out the keyword
			//System.err.println("replacing with " + 
				//	   new String(translated[j]));
			System.arraycopy(translated[j], 0, // table[j], 0, 
					 expansion, expansionIndex,
					 translated[j].length); //table[j].length);
			expansionIndex += translated[j].length;
			lastChunk = i;
		    }
		    indices[j] = 0;
		}
	    
	    } else {
		//if (program[i] < 32) System.err.println((int) program[i]);
		// increment the matching counters
		for (int j = 0; j < indices.length; j++) {
		    if ((table[j].length > indices[j]) &&
			(table[j][indices[j]] == program[i])) {
			//System.err.println("matched: " + table[j][indices[j]]);
			indices[j]++;
		    } else {
			indices[j] = 0;
		    }
		}
	    }
	}
	// copy out the remaining
	if (expansion.length < expansionIndex + (program.length-lastChunk)) {
	    expansion = growArray(expansion);
	}
	System.arraycopy(program, lastChunk, expansion, expansionIndex,
			 program.length - lastChunk); 
	expansionIndex += (program.length - lastChunk);

	// copy everything into memory of the exact size
	char output[] = new char[expansionIndex];
	System.arraycopy(expansion, 0, output, 0, expansionIndex);
	//DBNApplet.debugString(new String(output));
	return output;
#endif
    }


    // this should return the string that literally loads in the contents
    // of 'load foo.dbn' (i.e. in the command bar 'beautify, expand') for
    // replacement (mutation) in the main textarea buffer

    public char[] loadExpand(char data[]) {
	// stream has already been converted to ascii, so this is safe
	int lastChunk = 0;
	int expansionIndex = 0;
	boolean interesting = false;
	char expansion[] = null;

	for (int i = 0; i < data.length; i++) {
	    if ((data[i] & 64) > 0) {  // uppercase, make lowercase
		if (data[i] <= 'Z') data[i] += 'a' - 'A';
	    }
	}

	// convert \r and \r\n to \n
	int offset = 0;
	for (int i = 0; i < data.length; i++) {
	    if (data[i] == '\r') {
		if ((i != data.length-1) && (data[i+1] == '\n')) {
		    // it's \r\n, windows style
		    i += 1;
		}
		data[offset++] = '\n';

	    } else {
		data[offset++] = data[i];
	    }
	}
	// make the remaining into spaces
	for (int i = offset; i < data.length; i++) data[i] = ' ';

	//if ((data[i] & 64) > 0) 
	// minus 5 because at least 5 bytes are needed for the load,
	// so don't bother parsing those guys, just finish them off
	for (int i = 0; i < data.length-5; i++) {
	    //System.err.print("." + i + ".");
	    if ((data[i] == '/') && (data[i+1] == '/')) {  // comment time! 
		interesting = true;
		//System.err.println("comment at " + i);
		// strip comments, because when they concatenate for
		// insertion, you'll wind up with one big commented-out line
		// save everything so far (same as below)
		if ((expansion == null) ||
		    (expansion.length < expansionIndex + (i-lastChunk))) {
		    expansion = growArray(expansion);
		}
		if (i != 0) {
		    System.arraycopy(data, lastChunk, expansion, expansionIndex,
				     i - lastChunk); 
		    expansionIndex += i-lastChunk;
		}
		// ignore everything until newline
		i += 2;
		while ((i < data.length) && 
		       (data[i] != '\r') && (data[i] != '\n')) i++;
		lastChunk = i;
		//System.err.println("skipping to " + i);
		
	    } else if ((data[i+0] == 'l') && (data[i+1] == 'o') &&
		       (data[i+2] == 'a') && (data[i+3] == 'd') &&
		       (data[i+4] == ' ')) {
		// got load command

		// allocate expansion if not already
		if ((expansion == null) ||
		    (expansion.length < expansionIndex + (i-lastChunk))) {
		    expansion = growArray(expansion);
		}
		// save everything since beginning or last replacement
		if (i != 0) {
		    System.arraycopy(data, lastChunk, expansion, expansionIndex,
				     i - lastChunk); 
		    expansionIndex += i-lastChunk;
		}

		// eat any extra whitespace
		int index = i + 5;
		while (data[index] == ' ') index++;
		
		// read name of file, up until \s or \r or \n
		char filename[] = new char[256];
		int fileIndex = 0;
		while ((index < data.length) && (data[index] != '\r') && 
		       (data[index] != '\n') && (data[index] != ' ') &&
		       (fileIndex < filename.length)) {
		    filename[fileIndex++] = data[index++];
		}
		String filenameStr = new String(filename, 0, fileIndex);
		// if it's not a .dbn file, false alarm
		if (filenameStr.indexOf(".dbn") != -1) {
		    interesting = true;
		    //System.err.println("inserting " + filenameStr);
		    String insertStr = applet.readFile(filenameStr);
		    char insert[] = null;
		    if (insertStr == null) {
			System.err.println("could not find/open " + filenameStr);
			insert = "!asiagocheese!".toCharArray();
		    } else {
			insert = insertStr.toCharArray();
		    }

		    // needs language translation
		    insert = translateLanguage(insert);

		    // has to be *after* language translation
		    insert = loadExpand(insert);

		    // translate newlines to semicolons
		    // turns windows-style \r\n into ;; but that's ok
		    for (int m = 0; m < insert.length; m++) {
			if ((insert[m] == '\r') || (insert[m] == '\n')) {
			    insert[m] = ';';
			}
		    }

		    // copy into the main program buffer to be parsed
		    System.arraycopy(insert, 0, expansion, expansionIndex, 
				     insert.length);
		    expansionIndex += insert.length;
		    lastChunk = index;
		    i = index;
		}
	    }
	}
	// if nothing changed, return original array
	if (!interesting) {
	    if (data.length == 0) return data;
	    char last = data[data.length-1];
	    // last line must be a newline
	    if ((last == '\r') || (last == '\n')) {
		return data;
	    }
	    // add an extra newline for the parser
	    expansion = new char[data.length + eolCount];
	    System.arraycopy(data, 0, expansion, 0, data.length);
	    for (int i = 0; i < eolCount; i++) {
		expansion[data.length + i] = eol[i];
	    }
	    return expansion;
	}

	// write out anything remaining
	if ((expansion == null) ||
	    (expansion.length < expansionIndex + (data.length-lastChunk))) {
	    expansion = growArray(expansion);
	}
	System.arraycopy(data, lastChunk, expansion, expansionIndex,
			 data.length - lastChunk); 
	expansionIndex += (data.length - lastChunk);

	// add an extra newline for the parser
	if (expansion.length < expansionIndex + eolCount) {
	    expansion = growArray(expansion);
	}
	for (int i = 0; i < eolCount; i++) {
	    expansion[expansionIndex++] = eol[i];
	}

	// copy everything into memory of the exact size
	//return new String(expansion, 0, expansionIndex);
	char output[] = new char[expansionIndex];
	System.arraycopy(expansion, 0, output, 0, expansionIndex);
	return output;
    }


    // first do language translation for the main code
    // then do load expansion for the main code
    // (do this recursively)
    public String process(String string) {
	//System.err.println("preproc: doit " + s);
	char program[] = string.toCharArray();
	
	// walk through string, convert to hashcodes and compare 
	// as fast as possible break into 2 phases as might want 
	// to do an expand from the outside.
	program = translateLanguage(program);
	
	// do load expansion
	// NOTE: there is a bug in DBN, it likes to have a '\n' 
	// at the end be sure to attach this (see comments above, 
	// the newline is added by the preproc
	program = loadExpand(program);
	
	//DbnApplet.debugString(new String(program));

	return new String(program);
    }
}

