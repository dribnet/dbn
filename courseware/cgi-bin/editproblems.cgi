#!/usr/bin/perl
#use CGI qw(:standard);
require 'globals.pl';
package courseware;
use CGI param;

$user = param("user");
$course = param("course");
$item = param("item");

ensure_valid_course($course);
standard_setup($course);
ensure_admin_user($user, $item);

# Determine why the person has arrived at this page
#
# 1) they are arriving to edit a specific problem set
# 2) they are creating a new problem set
# 3) they are trying to add a problem within the problem set
# 4) they are trying to remove a problem within the problem set


$mode = param("mode");        #determines intent, either 'Edit', 'Add'
$submode = param("submode");  #either 'Add a problem' or 'Remove the last problem'
$num = param("num");	      #number of the set to display

@let = qw(A B C D E F G H I J K L M N O P Q R S T U V W X Y Z);
$problems_txt = "$path/courses/$course/problems.txt";

if ($submode eq "addprob") {
    #delete_update();
	
    #Add two lines to the end of the file
    parse_file();
    open (PROBLEMSADD, ">>$problems_txt") || die "can't create problems.txt: $!";	
    print PROBLEMSADD "<p$num>( problem )\n";
    print PROBLEMSADD "<m$num>dbn\n";
    close PROBLEMSADD;
    chmod(0666, "$problems_txt");
    @lines = ();
    @problems = ();
	
} elsif ($submode eq "remove") { 
    #delete_update();
    @newlines = ();
    
    #Re-open "problems.txt" and remove the two lines the user want to remove
    parse_file();
    @REVERSED = reverse @lines;
    $poy = $moy = 0;
    foreach $tem (@REVERSED) {
        #Get rid of the first of each tag encountered for this problem
	if ($tem =~ m/<p$num>/ && $poy == 0) {
	    $poy = 1;
	} elsif ($tem =~ m/<m$num>/ && $moy == 0) {
	    $moy = 1;
	} else {
	    unshift(@newlines, $tem);
	}
    }
    #@newlines = reversed @newlinesrev
    #Re-write the file
    open (PROBLEMSDEL, ">$problems_txt") || die "can't open problems.txt: $!";
    foreach $tem (@newlines) {	
	print PROBLEMSDEL "$tem\n";
    }
    close PROBLEMSDEL;
    chmod(0666, "problems_txt");
    @lines = ();
    @problems = ();
}

if ($mode eq "Add") {
    if ($submode ne "addprob" && $submode ne "remove") {
	parse_file();
	parse_headers();
	$arnum = $#headers + 2;
	open (PROBLEMSADD, ">>$problems_txt") || die "can't append problems.txt: $!";
	print PROBLEMSADD "<h$arnum>( title )\n";
	print PROBLEMSADD "<p$arnum>( problem )\n";
	print PROBLEMSADD "<m$arnum>dbn\n";
	close PROBLEMSADD;
	chmod(0666, "problems_txt");
    }
    parse_file();
    parse_headers();
    parse_routine("p","problems");
     
} elsif ($mode eq "Edit") {
    parse_file();
    parse_headers();
    parse_routine("p","problems");
}



beginHTML_header("Design By Numbers Courseware : Add/Edit problem sets", 0, 1);

#Begin the second table
print<<END_of_middle1;
<table border="0" cellpadding="0" cellspacing="0" width="665">
  <tr> 
     <td width="20" height="100">
	   <img src="$url/images/1pix.gif" width="40" height="1"><br>
     </td>
     <td width="605" height="100" valign="top" align="left">
	   <form action="updateprob.cgi" method="post">
	   <table border="0" cellpadding="10" cellspacing="0" width="620">
	   <tr>
	   <td colspan="2">
	   <font size="3" face="Arial, Helvetica, sans-serif"><b><font color="#999999">
END_of_middle1


#Draw the title for the page
if ($mode eq "Add") {
   print("Add problem set $num");
} elsif ($mode eq "Edit" ) {
   print("Edit problem set $num");
} else {
   print("I don't know what you are trying to do.<br><br>");
}


print<<END_of_middle2;  
	</font><font color="#333333">
	<br>
	<br>
	</font></b><font size="2" face="Arial, Helvetica, sans-serif" font color="#666666">
	There are some limitations in adding and editing problems. Problems can 
	only be added and removed from the end of the list.<br><br></font></font>   
	</td></tr>
	<p><font face="Arial, Helvetica, sans-serif" size="2" color="#333333"> 
END_of_middle2


print("<tr>"); 
print("<td valign=\"top\"><font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"#666666\">");
print("<b>Title</b></font></td>");
print("<td valign=\"top\">"); 
print("<textarea name=\"header\" cols=\"40\" rows=\"3\">$headers[$num - 1]");
print("</textarea>");
print("</td>");
print("</tr>");
print("<tr>");
print("<td valign=\"top\">&nbsp;</td>");
print("<td valign=\"top\"><br>");
print("</td>");
print("</tr>");



#Begin loop

for $i (0 .. $#{$problems[$num - 1]}) {
	print("<tr>"); 
	print("<td valign=\"top\"><font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"#666666\">");
	print("<b>Problem&nbsp;$num$let[$i]</b></font></td>");
	print("<td valign=\"top\">"); 
	print("<textarea name=\"p_$let[$i]\" cols=\"40\" rows=\"3\">$problems[$num - 1][$i]");
	print("</textarea>");
	print("</td>");
	print("</tr>");
	print("<tr>");
	print("<td valign=\"top\">&nbsp;</td>");
	print("<td valign=\"top\"><br>");
	print("</td>");
	print("</tr>");
}

#End loop


print<<END_of_middle3;
     <tr><td colspan="2">
    <input type="hidden" name="num" value="$num">
    <input type="hidden" name="mode" value="$mode">
    <input type="hidden" name="user" value="$user">
    <input type="hidden" name="course" value="$course">
    <input type="hidden" name="item" value="$item">
    <input type="submit" name="submode" value="Add a problem">
    <input type="submit" name="submode" value="Remove the last problem">
    <br>
    <br>
    <br>
    <hr size="1" width="100%" noshade align="left">
    <br>
    <input type="submit" name="submode" value="Save and Return">
    </form>
    </td>
    </tr>
    </table>
    </td>
  </tr>
</table>
END_of_middle3


copyright();







