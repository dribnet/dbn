DESIGN BY NUMBERS RELEASE NOTES
Version 1.X, Released XXXX
http://dbn.media.mit.edu 
dbn-feedback@media.mit.edu


Welcome to the latest release of downloadable DBN.
This file contains useful updates, information, and
troubleshooting notes.


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


* KNOWN ISSUES
There is a problem with functions that are too heavily
recursive. You will know you have run up against this 
problem if you get a "java.lang.StackOverFlowError" 
after your program has been running for a while. A fix
for this would require major work on dbn's internal engine.


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


PEOPLE
DBN is the product of many people. Ben Fry is the chief 
architect of the most current release, DBN 1.2. The previous 
release, DBN 1.0.1 was created by Tom White. The original 
version DBN 1.0 was created by John Maeda.

Concurrently we have several DBN-related developments. 
A DBN courseware system architected by Casey Reas, 
a user site by Lauren Dubick, and a cast of DBN workshop 
material that has been administered in Seoul, Tokyo, LA, 
New York, and Cambridge. Other people that have contributed 
to DBN development are Peter Cho, Elise Co, Golan Levin, 
Jocelyn Lin, and Josh Nimoy. 


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


CHANGES (technical section, can be ignored)

IN PROGRESS: Version 1.X: Changes since 1.2.1

- fixed a bug where field was not properly setting the 
  values of the screen's [x y] image array

- fixed a bug where recursion was not working properly

- turns off anti-alias between program executions,
  before it wasn't paying attention and leaving
  anti-alias turned on for the rest of the dbn session

Version 1.2.1: Minor changes since 1.2

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

