DESIGN BY NUMBERS RELEASE NOTES
Version 1.01, August 6, 1999
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
from Apple at http://www.apple.com/java/. Previous 
versions of MRJ were incompatible, slow and buggy, so 
you should definitely be using the most recent version. 
MRJ in fact continues to be somewhat incompatible, slow 
and buggy, though Apple's small team of developers is 
slowly chipping away at the problems.


* WINDOWS PEOPLE READ THIS!
The Windows version of the DBN download includes JRE,
the Java Runtime Environment. This means that you won't
have to download anything to get DBN up and running. To
start DBN, just double-click the file entitled 'run.bat'.


* KNOWN ISSUES
We're working to iron out any inconsistencies and 
outstanding issues. If you run across a bug, send email
with the following:
1. What you were doing at the time (i.e. a copy of the 
program that you were working on)
2. Description of your system setup: Windows 95/98/NT 
or what version of MacOS, version of MRJ if Mac, etc.
3. Steps to reproduce the problem
Send this mail to dbn-feedback@media.mit.edu


PEOPLE
The DBN development team is composed of Tom White (chief 
architect), Ben Fry (graphics + i/o + multilanguage 
architect), Lauren Dubick (user experience + db architect), 
Casey Reas (courseware experience + architect), Elise Co 
and Jocelyn Lin (cgi-ers), and John Maeda.


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


CHANGES

1.01 from 1.00
* Fixed an 'end of line' bug. If a program had a comment at
the end of its final line, the line would not be read by
the parser (end-of-line fix for multiple platforms)

