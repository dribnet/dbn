#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

$user = param("user");
$course = param("course");
$item = param("item");

ensure_valid_course($course);
standard_setup($course);
ensure_admin_user($user, $item);


# There are four ways that people can get to this page:
#
# 1) entering from the edit problems link on the homepage
# 2) re-entering hoping they have edited a problem -- coming from edit problems
# 3) re-entering hoping they have created a problem -- coming from edit problems
# 4) re-entering hoping they have removed a problem


$mode = param("mode"); #determines intent, either 'Done', 'Remove'
$num = param("num");

if ($mode eq "Remove") {
	parse_file();
	delete_set("$num");
	create_arrays();
	write_problems();
	clear_arrays();
}

parse_file();
parse_headers();


beginHTML_header("Design By Numbers Courseware : Edit Problems", 0, 1); 

$totalnum = $#headers + 2; #used for the number of an 'added' set

#Begin the second table
print<<END_of_middle1;
<table border="0" cellpadding="10" cellspacing="0" width="665">
  <tr> 
     <td width="20" height="100">
	   <img src="$url/images/1pix.gif" width="20" height="8"><br>
     </td>
     <td width="605" height="100" valign="top" align="left">
	    <font face="Arial, Helvetica, sans-serif" size="3" color="#999999"><b>Add 
	    a Problem Set</b></font><br>
	    <font face="Arial, Helvetica, sans-serif" size="2" color="#666666">Click 
	    &quot;Add a new set&quot; to create a new Problem Set. You will be asked 
	    to define the related assignments.<br> 
	    <form method="post" action="editproblems.cgi">
		   <input type="hidden" name="user" value="$user">
                   <input type="hidden" name="item" value="$item">
		   <input type="hidden" name="course" value="$course">
		   <input type="hidden" name="num" value="$totalnum">
		   <input type="hidden" name="mode" value="Add">
		   <input type="submit" name="button" value="Add a new set">
		   
		   
	    </font></form>
	    <br>
	    <!--<br>-->
	    <font face="Arial, Helvetica, sans-serif" size="3" color="#999999">
		<b>Edit a Problem Set</b></font><br>
	    <font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
	    To edit a problem set, select from the pulldown menu and click the &quot;Edit&quot; 
	    button.
	    <br> 
	    <form method="post" action="editproblems.cgi">
		<select name="num">
	       <!--<option value="default">Select a problem set&nbsp;&nbsp;</option>-->
END_of_middle1

$counter = 1;
foreach $temp (@headers) {
  print("<option value=\"$counter\">Problem Set $counter<\/option>");
  $counter++;
}

print<<END_of_middle2;
		</select>
		<input type="hidden" name="user" value="$user">
		<input type="hidden" name="item" value="$item">  
		<input type="hidden" name="course" value="$course">
		<input type="hidden" name="mode" value="Edit">
		<input type="submit" name="button" value="Edit">
		</font></form>
		<br>
		<!--<br>-->
		<font face="Arial, Helvetica, sans-serif" size="3" color="#999999"><b>Remove 
		a Problem Set</b></font>
		<font size="2" face="Arial, Helvetica, sans-serif" font color="#666666"><br>
		To edit a problem set, select from the pulldown menu and click the &quot;Remove&quot; 
		button.<br>
		<form method="post" action="problems.cgi">
		   <select name="num">
		   <!--<option value="default">Select a problem set &nbsp;&nbsp;</option>-->
END_of_middle2

$counter = 1;
foreach $temp (@headers) {
  print("<option value=\"$counter\">Problem Set $counter<\/option>");
  $counter++;
}

print<<END_of_middle3;
            </select>
		    <input type="hidden" name="user" value="$user">
		    <input type="hidden" name="course" value="$course">
		    <input type="hidden" name="item" value="$item">
	        <input type="submit" name="mode" value="Remove">
	    </font></form>
		<br>
		<br>
		<hr size="1" width="95%" noshade align="left">
		<form action="home.cgi" method="post">
		<input type="hidden" name="user" value="$user">
		<input type="hidden" name="item" value="$item">
		<input type="hidden" name="course" value="$course">
		<input type="submit"  value="Done">
        </form>
    </td>
  </tr>
</table>
END_of_middle3

copyright();





# BEGIN PERL SUBROUTINES

#Writes "problems.txt" from the @headers, @problems, @media arrays
sub write_problems 
{
	open (PROBLEMSDEL, ">$path/courses/$course/problems.txt") ||  die "can't open problems.txt: $!";
	$maxcounter = 1;
	for $i (0 .. $#headers) {
	    print PROBLEMSDEL "<h$maxcounter>$headers[$i]\n";
	    for $j (0 .. $#{$problems[$i]}) {
		print PROBLEMSDEL "<p$maxcounter>$problems[$i][$j]\n";
		print PROBLEMSDEL "<m$maxcounter>$media[$i][$j]\n";
	    }
	    $maxcounter++;
	}
	close PROBLEMSDEL;
	chmod(0666, "$path/courses/$course/problems.txt");
}

# Delete the specified problem set from the $lines array
sub delete_set 
{
    foreach $tem (@lines) {
	if ($tem !~ /<.$_[0]>/) {
	    push(@newlines, $tem);
	}
    }
    @lines = @newlines;   #This is sloppy -- it avoids changing other subroutines
}

#Remove all information from arrays, they are stale now (just in case)
sub clear_arrays 
{
    @headers = ();
    @problems = ();
    @media = ();
}

#Parse modified @lines into it's separate elements
sub create_arrays 
{
    parse_headers();		       	#Creates @headers
    parse_routine("p","problems");	#Creates @problems
    parse_routine("m","media");		#Creates @media
}

sub delete_update 
{
    parse_file();		        #Creates @lines
    parse_routine("p","problems");	#Creates @problems
    delete_set("$num");		        #First delete the current set in the array

    @n_problems = ();
    @n_media = ();
    $header = param("header");
    for $i (0 .. $#{$problems[$num - 1]}) {
	$n_problems[$i] = param("p_$let[$i + 1]");
	$n_media[$i] = param("m_$let[$i + 1]");
    }
    
    #Add to the end of the file, the line 'tags' must be correct
    open (PROBLEMSRE, ">$path/courses/$course/problems.txt") || die "can't create problems.txt: $!";	
    foreach $tem (@lines) {
	print PROBLEMSRE "$tem\n";
    }
    print PROBLEMSRE "<h$num>$header\n";
    for $i (0 .. $#{$problems[$num - 1]}) {
	print PROBLEMSRE "<p$num>$n_problems[$i]\n";
	print PROBLEMSRE "<m$num>$n_media[$i]\n";
    }
    close PROBLEMSRE;
    chmod(0666, "$path/courses/$course/problems.txt");
    @lines = ();
    @problems = ();
}









