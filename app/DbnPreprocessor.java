//import java.awt.*;
//import java.applet.Applet;
import java.util.*;


// the job of dbnpreproc is to hold onto/manage language 
// translation table as well as manage the 'load' command
// if no .dbn extension then gets passed as is to parser

public class DbnPreprocessor {
  static DbnApplet applet;

  // this is sticky, because the textfield really wants its
  // own type of newlines. this is because the parser needs
  // a newline at the end, and it wasn't getting it unless
  // it's the real type of newline. so, it's an unfortunate 
  // addition (all these vars) but it's gotta be.
  static int eolCount;
  static char eol[];
  static {
    // modified for dbn-ng, because it uses only \n internally
    eolCount = 1;
    eol = new char[1];
    eol[0] = '\n';
  }

  static char[][] languageTable;
  static Hashtable languageHash;

  // use \u0000 for unicode characters, since most
  // versions of javac will choke on > 7 bit characters
  static final String keywords[][] = { {
    "en", "paper", "pen", "line", "repeat", "forever", 
    "set", "command", "number", "field", "refresh",
    "mouse", "keyboard", "net", "time",
    "same?", "notsame?", "smaller?", "notsmaller?"
  }, {
    "es", "papel", "stilo", "l\u00EDnea", "repita", "siempre",
    "ponga", "instruci\u00F3n", "n\u00FAmero", "\u00E1rea", "refrese",
    "rat\u00F3n", "teclado", "internet", "hora",
    "\u00BFigual?", "\u00BFnoigual?", "\u00BFmenos?", "\u00BFnomenos?"
  }, {
    "fr", "papier", "plume", "ligne", "r\u00E9peter", "toujours",
    "mettre", "fonction", "num\u00E9ro", "r\u00E9gion", "\u00E0neuf",
    "souris", "clef", "r\u00E9seau", "heure",
    "pareil?", "paspareil?", "pluspetit?", "plusgrand?"
  }, {
    "jp", "\u304B\u307F", "\u30DA\u30F3", "\u305B\u3093",
    "\u304F\u308A\u304B\u3048\u3057", "\u305A\u3063\u3068",

    "\u304A\u304F", "\u30B3\u30DE\u30F3\u30C9", 
    "\u3070\u3093\u3054\u3046", "\u308A\u3087\u3046\u3044\u304D", 
    "\u30EA\u30D5\u30EC\u30C3\u30B7\u30E5",

    "\u30DE\u30A6\u30B9", "\u30AD\u30FC\u30DC\u30FC\u30C9",
    "\u30CD\u30C3\u30C8", "\u3058\u304B\u3093", 

    "\u304A\u306A\u3058\uFF1F", 
    "\u304A\u306A\u3058\u3067\u306A\u3044\uFF1F",
    "\u3059\u304F\u306A\u3044\uFF1F",
    "\u3059\u304F\u306A\u304F\u306A\u3044\uFF1F"
  } };


  static {
    languageHash = new Hashtable();
    int languageCount = keywords.length;
    int keywordCount = getKeywordCount();
    for (int i = 0; i < languageCount; i++) {
      String languageName = keywords[i][0];
      char characters[][] = new char[keywordCount][];
      for (int j = 0; j < keywordCount; j++) {
	characters[j] = keywords[i][j+1].toCharArray();
      }
      languageHash.put(languageName, characters);
    }
  }

  static public int getKeywordCount() {
    return keywords[0].length - 1;
  }


  public DbnPreprocessor(DbnApplet applet) {
    this.applet = applet;
	
    if (languageTable == null) {
      String lang = applet.get("language", null); 
      languageTable = (lang == null) ? 
	null : (char[][])languageHash.get(lang);
      if ((languageTable == null) & (lang != null)) {
	addLanguageTable("keywords-" + lang + ".txt", applet);
      }
    }
  }
    

  // parse language table using input from a file downloaded via
  // the applet, and then add the language to the languageHash
  static public void addLanguageTable(String filename, DbnApplet applet) {
#ifndef JDK11
    return;
#else
    char[] chars = applet.readFile(filename).toCharArray();
    int keywordIndex = 0;
    char[] keyword = new char[32];
    int translationIndex = 0;
    char[] translation = new char[32];
    int keywordCount = keywords[0].length;
    char[][] keywordData = new char[keywordCount][];
    String languageName = null;

    for (int i = 0; i < chars.length; i++) {
      while (Character.isWhitespace(chars[i])) i++;
      if (i == chars.length) break;
	    
      // look for comments, which start with slash-slash
      // (just like dbn for sake of simplicity)
      if ((chars[i] == '/') && (i < chars.length-1) &&
	  (chars[i+1] == '/')) {
	// it's a comment, ignore
	while ((chars[i] != '\r') && (chars[i] != '\n')) {
	  i++; if (i == chars.length) break;
	}
      }
      if (i == chars.length) break;

      // read the keyword
      while (!Character.isWhitespace(chars[i]))
	keyword[keywordIndex++] = chars[i++];

      while (Character.isWhitespace(chars[i])) i++;
	    
      // read the translation of that keyword
      while ((i != chars.length) &&
	     !Character.isWhitespace(chars[i]))
	translation[translationIndex++] = chars[i++];
      if (i == chars.length) break;
	    
      // match it up with the default language
      // check each of the keywords in the default language
      boolean foundMatch = false;
      for (int j = 0; j < keywordCount; j++) {
	String one = new String(keywords[0][j]);
	String two = new String(keyword, 0, keywordIndex);
	if (one.equals(two)) {
	  keywordData[j] = new char[keywordIndex];
	  System.arraycopy(translation, 0, 
			   keywordData, 0, translationIndex);
	} else if (two.equals("language")) {
	  languageName = 
	    new String(translation, 0, translationIndex);
	}
      }
      if (!foundMatch) {
#ifndef KVM
	System.err.println("Error in language file: Could not find");
	System.err.println("find a match for keyword " + keyword);
#endif
	return;
      }
    }
    if (languageName == null) {
#ifndef KVM
      System.err.println("Error in language file: ");
      System.err.println("Language name not specified in file");
#endif
      return;
    }
    languageHash.put(languageName, keywordData);
#endif
  }


  // hopefully this will just inline
  static private final char[] growArray(char stuff[]) {
    if (stuff == null) return new char[16384];
    char temp[] = new char[stuff.length*2];
    System.arraycopy(stuff, 0, temp, 0, stuff.length);
    return stuff;
  }

  // applet parameter tag has language en, ja, es, de
  // can't mix multiple languages together
  static public char[] translateLanguage(char program[]) {
#ifndef JDK11
    return program;
#else
    // default language (english), no changes
    char[][] table = languageTable; 
    char[][] translated = (char[][]) languageHash.get("en"); 
    if (table == null) return program;

    int expansionIndex = 0;
    char[] expansion = new char[program.length*2];
	
    int lastChunk = 0;

    // at this point, the applet has already converted all
    // of the input data (file or otherwise) to unicode
    // in the format of chars. all that's left is to
    // substitute the keyword strings with english versions.
    int indices[] = new int[getKeywordCount()];

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
    return output;
#endif
  }


  // this should return the string that literally loads in the contents
  // of 'load foo.dbn' (i.e. in the command bar 'beautify, expand') for
  // replacement (mutation) in the main textarea buffer

  static public char[] loadExpand(char data[]) {
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
#ifndef KVM
	boolean isDbnFile = (filenameStr.indexOf(".dbn") != -1);
#else
	boolean isDbnFile = (KvmLacunae.indexOf(".dbn", filenameStr) != -1);
#endif
	if (isDbnFile) {
	  interesting = true;
	  //System.err.println("inserting " + filenameStr);
	  String insertStr = applet.readFile(filenameStr);
	  char insert[] = null;
	  if (insertStr == null) {
#ifndef KVM
	    System.err.println("could not find/open " + filenameStr);
#endif
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
  static public char[] process(String string) {
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

    return program;
    //return new String(program);
  }
}

