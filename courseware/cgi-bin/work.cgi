#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

# first check to see if it's the applet talking, wanting to save files
if (param("save_program") ne "") {
    # it's the applet banging on this script, save files and exit
    $save_as = param("save_as");
    $save_program = param("save_program");
    $save_image = param("save_image");

    # maybe add a security check here so that people aren't
    # trying to put anything funny into 'save_as'

    # save the .dbn file
    open(OUTPUT, ">$path/courses/$save_as.dbn") || die "cannot write to $save_as.dbn $!";
    print OUTPUT $save_program;
    close OUTPUT;

    # save the .pgm file
    open(OUTPUT, ">$path/courses/$save_as.pgm") || die "cannot write to $save_as.pgm $!";
    print OUTPUT $save_image;
    close OUTPUT;

    print "Content-type: text/html\n\n";
    print "Success.\n";
    exit 0;
}


$user = param("user");
$course = param("course");
$item = param("item");

$set = param("set");
$num = param("num");
$sub = param("sub");

# vars user, image, program -> $save_as, $save_image, $save_program
$save_as = "$course/$user/$set$num";

ensure_valid_course($course);
standard_setup($course);
ensure_valid_user($user, $item);

parse_file();
parse_routine("p","problems");


beginHTML_header("Design By Numbers Courseware : Work on Problems", 0, 0); 

$totalnum = $#headers + 2; #used for the number of an 'added' set

#Begin the second table
print<<END_of_middle1;
<table border="0" cellpadding="10" cellspacing="0" width="660">
  <tr> 
     <td width="20" height="100">
	   <img src="$url/images/1pix.gif" width="20" height="8"><br>
     </td>
     <td width="600" height="100" valign="top" align="left">
	 
	 <font face="Arial, Helvetica, sans-serif" size="3" color="#666666"><b>
	 Your are working on Problem $set$num.</b>
	 Click <a href="$cgiurl/selectproblem.cgi?user=$user&item=$item&course=$course">here</a> to select another.<br>
	 <br></font>
	
	 <applet codebase="$url/dbn" code="DbnApplet" archive="dbn.jar" width=600 height=350>
	 <param name="save_as" value="$save_as">	 
END_of_middle1

	  $program = "courses/$course/$user/$set$num.dbn";
          #my $inline_program = fetch_program($program);
	  #print("<param name=\"inline_program\" value=\"$inline_program\">");
          if ($platform eq "mac") {
	      print("<param name=\"program\" value=\"$cgiurl/fetch.cgi?file=$program\">\n");
	  } else {
	     if (-f "$path/$program") { 
		 print("<param name=\"program\" value=\"$path/$program\">\n");
		 #print("<param name=\"program\" value=\"$url/$program\">\n");
	     } else {
		 print("<param name=\"program\" value=\"\">\n");
	     }  
	  }
	  
print<<END_of_middle1;
	 </applet> 
	 
	<br>
	<br>
	<font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
	<b>Problem $set$num</b><br>
	$problems[$set-1][$sub]<br>
	</font>

    </td>
  </tr>
</table>
END_of_middle1

copyright();












