DESIGN BY NUMBERS RELEASE NOTES
Version 3.0, Released August 10, 2001
http://dbn.media.mit.edu 
dbn-feedback@media.mit.edu


Welcome to the latest release of downloadable DBN.
This file contains useful updates, information, and
troubleshooting notes.


* FINAL VERSION OF DBN
We intend for version 3.0 to be the final release of 
the Design By Numbers system. While there may be upgrades 
for important bug fixes, this is the last version that
will include new features.


* DBNGRAPHICS.DBN and DBNLETTERS.DBN
These modules provide additional graphics functions
and primitive letter drawing, as documented in the book.
The files are included in the download, so to use them, 
add 'load dbnletters.dbn' or 'load dbngraphics.dbn' to 
the beginning of your program to include their functions.


* MACINTOSH PEOPLE READ THIS!
The Macintosh version requires Macintosh Runtime for 
Java (MRJ), 2.1.2 (or newer). This can be downloaded 
from Apple at http://www.apple.com/java. Previous 
versions of MRJ were incompatible, slow and buggy, so 
you should definitely be using the most recent version. 
MRJ in fact continues to be somewhat incompatible, slow 
and buggy, though Apple's small team of developers is 
slowly chipping away at the problems.

Also, make sure that the date on your Macintosh is not
set earlier than 1970. There is a problem with MRJ that
will cause your machine to behave highly erratically 
under this condition.


* WINDOWS PEOPLE READ THIS!
The Windows version of the DBN download includes JRE,
the Java Runtime Environment. This means that you won't
have to download anything to get DBN up and running. To
start DBN, just double-click the file titled 'run.bat'.

If you are using Windows 95, 98 (or perhaps even ME), 
you may have trouble with using the run file. If the
window opens and then closes quickly, instead move
dbn to your C drive, and try the run95.bat file.


* KNOWN ISSUES
There is a problem with functions that are too heavily
recursive. You will know you have run up against this 
problem if you get a "java.lang.StackOverFlowError" 
after your program has been running for a while. A fix
for this would require major work on dbn's internal engine,
and it's not clear if the problem could truly be resolved.


* IF YOU HAVE PROBLEMS
We're working to iron out any inconsistencies and 
outstanding issues. If you run across what appears to be
a bug, send email with the following:
1. What you were doing at the time (i.e. a copy of the 
program that you were working on)
2. Description of your system setup: Windows 95/98/NT 
or what version of MacOS, version of MRJ if Mac, etc.
3. Steps to reproduce the problem
Send this mail to dbn-feedback@media.mit.edu


* PEOPLE
DBN is the product of many people. Ben Fry is the chief 
architect of releases since 1.1. A previous release,
DBN 1.0.1 was created by Tom White. The original version 
was created by John Maeda.

Concurrently we have several DBN-related developments. 
A DBN courseware system architected by Casey Reas, 
a user site by Lauren Dubick, and a cast of DBN workshop 
material that has been administered in Seoul, Tokyo, LA, 
New York, and Cambridge. Other people that have contributed 
to DBN development are Peter Cho, Elise Co, Golan Levin, 
Jocelyn Lin, and Josh Nimoy. 


* HIDDEN FEATURES INSIDE DBN
For more information, visit the DBN web site.
- holding down the 'control' key and clicking in the 
  image area will save the current image to a .tiff
  file, that can be opened by photoshop or other
  applications. 
- holding down the shift key while clicking pixels 
  inside the graphics area will highlight the line of
  code that (most recently) drew that pixel.
- color is included in this version. instead of just
  'pen 45' or 'paper 30' to set a 45 or 30% gray, 
  use 'pen 10 30 50' for red, green, and blue values
  of 10, 30, and 50 (on a scale of 0 to 100)
  same goes for paper, i.e. 'paper 20 70 40'. because
  rgb works differently than gray, note that to get
  a 0% gray (paper 0) using rgb color, the command
  would be 'paper 100 100 100'.
- get/set are a little stranger, however. the command
  'set [50 50] 30' will set location 50, 50 to 30% gray;
  because color requires three entries, you have to 
  specify which one you'd like. so 'set [40 40 red] 30'
  will set the amount of red at location 40, 40 to 30%.
  similarly, 'set blah [40 40 red]' will set the variable
  'blah' to the red value at location 40, 40.
- the drawing area can be resized, using the 'size' command.
  'size 200 200' in your code will cause the drawing
  window to grow to 200 by 200 pixels (instead of the 
  usual 101 by 101).
- it is also possible to use a magnification on the drawing
  area. add the amount to magnify by to the end of 
  the size command. for instance, a 4x magnification and
  a 200x200 drawing area would be: 'size 200 200 4'
- this version is speed-controlled. to disable speed control
  so that your applets run as fast as possible, add 
  'slowdown=false' to the 'dbn.properties' file 
  that's inside the 'lib' folder.


LICENSE
Massachusetts Institute of Technology ("MIT") hereby grants 
permission for you to copy and use Design by Numbers (DBN) 
software. MIT shall retain all right, title and interest, 
including copyright, in and to the Design by Numbers (DBN) 
software. 

THE Design by Numbers (DBN) SOFTWARE IS PROVIDED TO YOU "AS IS," 
AND MIT MAKES NO EXPRESS OR IMPLIED WARRANTIES WHATSOEVER WITH 
RESPECT TO ITS FUNCTIONALITY, OPERABILITY, OR USE, INCLUDING, 
WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE, OR INFRINGEMENT. MIT EXPRESSLY 
DISCLAIMS ANY LIABILITY WHATSOEVER FOR ANY DIRECT, INDIRECT, 
CONSEQUENTIAL, INCIDENTAL OR SPECIAL DAMAGES, INCLUDING, WITHOUT 
LIMITATION, LOST REVENUES, LOST PROFITS, LOSSES RESULTING FROM 
BUSINESS INTERRUPTION OR LOSS OF DATA, REGARDLESS OF THE FORM 
OF ACTION OR LEGAL THEORY UNDER WHICH THE LIABILITY MAY BE 
ASSERTED, EVEN IF ADVISED OF THE POSSIBILITY OR LIKELIHOOD OF 
SUCH DAMAGES. 


CHANGES (technical section that you're welcome to ignore)

Version 3.0: Changes since 2.0.1
- removed the need for 'norefresh'
- disabled antialias feature (it was poorly implemented)
- added automatic-slowdown feature
- ability to save tiff files
- support for color commands: pen, paper, get/set [ ]
- changing of dbn drawing area using the 'size' command
- ability to magnify drawing area

- dbnletters was broken.. it used functions called typeA,
  typeB, typeC; but the book used letterA, letterB, letterC
  the new dbnletters is based on the book examples
- fixed a bug for the example on page 57 of the book, 
  where code inside curly braces would fail to run
- made fixes so that the new DBN works with the courseware
- changed threading so that DBN stops hesitating every
  once in a while while running.


Version 2.0.1: Changes since 2.0

- oops! Andrew Otwell pointed out that <key> was having
  difficulties (thanks Andrew), because of an oversight
  by the programmer who was in too much of a hurry to get
  DBN2K out the door (boo Ben). this has been fixed.


Version 2.0: Changes since 1.3

- whizzy new user interface
- rewrite of everything else that wasn't redone for 1.2
- semi-automatic indenting of lines
- tabs magically turn into spaces
- no more unsightly boxes at the end of selected lines (win95/nt)
- correct line number used from python stack traces (courseware)


Version 1.3: Changes since 1.2.1

- added printing feature for downloadable dbn
- drawing is now faster on the mac and elsewhere
- added norefresh to DbnGraphics
- re-oriented the screen layout
- disabled paren balancing code for mac (paste causes crash)
- added parenthesis balancing code
- default directory and filename for file saving are
  now being respected or set.
- fixed a bug that would cause dbn to die if the user
  tried to divide by zero
- fixed a bug where field was not properly setting the 
  values of the screen's [x y] image array
- refixed that bug again because it hadn't been completely fixed
- fixed a bug where recursion was not working properly
- turns off anti-alias between program executions,
  before it wasn't paying attention and leaving
  anti-alias turned on for the rest of the dbn session


Version 1.21: Minor changes since 1.2

- this version will only be updated on the website,
  since the bug fix doesn't affect the downloadable version
- moved the application code out of DbnApplet so that it
  works with Netscape Navigator


Version 1.2: Changes and fixes since version 1.01

- (major) new parser and execution engine
  increased stability and error messages for syntax problems
- Multi-file viewing 'griddify' function
  view many dbnlets within a single applet
- Won't show snapshot menu option if "user" parameter not specified
- pgm file i/o methods replacing old snapshot code,
  code that talks to the dbn courseware better


Version 1.01: Changes and fixes since version 1.0

- Fixed end of line bug. If a program had a comment at
  the end of its final line, the line would not be read by
  the parser (end-of-line fix for multiple platforms)

