package courseware;


# Globals and goodness -- information required by all scripts

# Begin Unix, PC location information
   $path = "/mas/acg/web/docroot.dbn/courseware";
   $url = "http://dbn.media.mit.edu/courseware";
   $cgiurl = "http://dbn.media.mit.edu/courseware/cgi-bin";
# End Unix, PC location information

# Begin Mac location information
#   $path = "courseware"; # Webten wants to exec everything inside cgi-bin
#   $url = "http://www.yourwebsite.com";
#   $cgiurl = "http://www.yourwebsite.com";
#   $platform = "mac"; 
# End Mac location information

# NOTHING BELOW THIS LINE SHOULD TO BE EDITED



sub ensure_valid_course 
{
  $course = @_[0];
  if ((!(-d "$path/courses/$course")) || ($course eq "")) {
    print "content-type: text/html\n\n";
    print "<meta http-equiv=\"refresh\" content=\"0;url=$cgiurl/login.cgi\">";
    exit 0;
  } 
}


sub ensure_valid_user 
{
    $user = @_[0];
    $passpass = @_[1];
    $found_match = 0;

    # Let registered students pass
    for (@people) {
	if($_ eq $user && $passpass eq $real_general_password) { 
	#if($_ eq $user) {
	    $found_match = 1;
	}
    }

    # Let the administrator pass
    # if ($user eq $real_admin_login && $passpass eq $real_admin_password){
    if ($user eq $real_admin_login && $passpass eq $real_admin_password){
	$found_match = 1;
    }

    # Let the guest pass through
    if ($user eq "guest" && $passpass eq "guest") {
	$found_match = 1;
    }

    # If this person is not valid, jump send them to the login screen
    if (!$found_match) {
	print "content-type: text/html\n\n";
	print "<meta http-equiv=\"refresh\" content=\"0;url=$cgiurl/login.cgi\">";
	#print "<meta http-equiv=\"refresh\" content=\"0;url=login.cgi?course=$course\">";
	exit 0;
    }
}


sub ensure_admin_user 
{
    $user = @_[0];
    # if it isn't ADMIN then redirect back to login
    if (!($user eq $real_admin_login && $item eq $real_admin_password)) {
	print "Content-type: text/html\n\n";
	print "<meta http-equiv=\"refresh\" content=\"0;url=$cgiurl/login.cgi\">";
	#print "<meta http-equiv=\"refresh\" content=\"0;url=login.cgi?course=$course\">";
	exit 0;
    }
}


sub standard_setup 
{
    $course = @_[0];
    # Open given 'admin.txt' AND parse it into variables. Kill if can't open the file.
    if (!open(ADMIN , "$path/courses/$course/admin.txt")) {
	beginHTML_header("Viewing Problems", 0, 1);
	print gold("* Please consult your course leader for the correct login procedure.");
	copyright();
	exit 0;
    }
    # Else keep going as if nothing went wrong. Read information from files.
    parse_admin();
    close ADMIN;    
    parse_people();
}


# Delete the specified problem set from the $lines array
sub delete_set 
{
    foreach $tem (@lines) {
	if ($tem !~ /<.$_[0]>/) {
	    push(@newlines, $tem);
	}
    }
    # this is sloppy -- but it avoids changing other subroutines
    @lines = @newlines;   
}


# Write the copyright information
sub copyright 
{
print<<END_of_copyright1;
	<br>
	<br>
	<br>
	<table cellpadding="10" cellspacing="0" border="0" width="665">
	  <tr> 
	    <td width="20" height="100" valign="top">
	      <img src="$url/images/1pix.gif" width="20" height="1">
	    </td>
	    <td width="605" height="100" valign="top">
	      <font face="Arial, Helvetica, sans-serif" size="1" color="#666666">
END_of_copyright1

    if ($user ne "guest") {
	print("You are logged in as $user. ");
	print("If you are not $user click <a href=\"$cgiurl/login.cgi?course=$course\">here</a>.<br>");
	print("<br>");
    }

print<<END_of_copyright2;
		  MIT Media Laboratory. <a href="http://acg.media.mit.edu">Aesthetics 
		  + Computation Group.</a> Copyright 1999, Massachusetts Institiute of Technology<br></font>
	    </td>
      	  </tr>
	</table></center>
	</body>
	</html>
END_of_copyright2
}


# Write the copyright information without login information
sub copyright_simple 
{
print<<END_of_copyright;
	<br>
	<br>
	<br>
	<table cellpadding="10" cellspacing="0" border="0" width="665">
	  <tr> 
	    <td width="20" height="100" valign="top">
	      <img src="$url/images/1pix.gif" width="20" height="1">
	    </td>
		<td width="605" height="100" valign="top">
		    <font face="Arial, Helvetica, sans-serif" size="1" color="#666666">
			MIT Media Laboratory. <a href="http://acg.media.mit.edu">Aesthetics 
			    + Computation Group.</a> Copyright 1999, Massachusetts Institiute of Technology<br></font>
	    </td>
      	  </tr>
	</table></center>
	</body>
	</html>
END_of_copyright
}


# Print gold for error messages
sub gold
{
    return "<font size=\"2\" face=\"arial,helvetica,san-serif\" color=\"#CC9900\"><b>$_[0]</b></FONT>";
}


# Get a list of all users and put into @people. Kill if can't open file.
sub parse_people
{
    chomp($course);
    if (!open(PEOPLE, "$path/courses/$course/people.txt")) {
	beginHTML_header("Error", 0, 1);
	print ("$!  $path/courses/$course/people.txt\n");
	print gold("* Could not parse 'people.txt'\n");
	copyright();
	exit 0;
    } else {
	while (<PEOPLE>) { 
	    chomp; 
	    unshift(@people, $_); 
	}
	close PEOPLE;
	@people = sort(@people);
    }
}


# Parse the file 'admin.txt'
sub parse_admin
{
    $real_admin_login = lc(<ADMIN>);	    chomp($real_admin_login);
    $real_admin_password = lc(<ADMIN>);     chomp($real_admin_password);
    $real_general_password = lc(<ADMIN>);   chomp($real_general_password);
    $course = <ADMIN>;		            chomp($course);
    $long_title = <ADMIN>;		    chomp($long_title);
}


# Trims the whitespace off the edges of a string and returns it
sub trimW
{
    @tempray = split( /\s+/g , '* '.$_[0]);
    shift @tempray;
    return join(" ", @tempray);
}


#Open "problems.txt" and parse titles into array. Kill if it can't open file.
sub parse_file 
{
    #open (PROBLEMS, "$path/courses/$course/problems.txt") || die "can't create problems.txt: $!";
    #@lines = <PROBLEMS>; #reads the entire file into @lines
    #close PROBLEMS;
    #chomp(@lines);

    if (!open(PROBLEMS , "$path/courses/$course/problems.txt")) {
	beginHTML_header("Error", 0, 1);
	print gold("* There was an error in trying to read the problems.");
	copyright_simple();
	exit 0;
    }
    # Else keep going as if nothing went wrong. Read information from files.
    @lines = <PROBLEMS>; #reads the entire file into @lines
    close PROBLEMS;
    chomp(@lines);
}


sub parse_routine 
{
    $count = 0;
    for $maxcounter (1 .. 50) {
	if (grep{/^<$_[0]$maxcounter>/} @lines) {
	    @tmp = grep{/^<$_[0]$maxcounter>/} @lines;
	    @newtmp = ();
	    foreach $element (@tmp) {
		$element =~ s/<$_[0]\d+\>//;
		push(@newtmp, $element);
	    }
	    $_[1][$count] = [@newtmp];
	    $count++;
	}
    }
}


# Begin HTML and write the header information
sub beginHTML_header 
{
print<<END_of_header1;
Content-type: text/html

	<html>
	<title>$_[0]</title>
	<body bgcolor="#FFFFFF" link="#336699" vlink="#669999" alink="#003366">
	<center><table border="0" cellpadding="10" cellspacing="0" width="665">
	  <tr> 
		<td width="20" valign="top" align="right">
END_of_header1

    # Print either a blue arrow or a spacer gif depending on the page/parameter
    if ($_[1]) {
	print("<img src=\"$url/images/1pix.gif\" width=\"20\" height=\"1\"><br>");
    } else {
	print("<a href=\"home.cgi?user=$user&course=$course&item=$item\">");
	print("<img src=\"$url/images/arrow_blue.gif\" width=\"20\" height=\"16\" border=\"0\"></a>");
    }

    print("</td>");
    print("<td width=\"605\" valign=\"top\" align=\"left\">");
    print("<font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"#666666\">$long_title</font>");
    print("<br>");

    if ($_[2]) {
	print("<img src=\"$url/images/designbynumbers.gif\" width=\"190\" height=\"51\"><br>");
	print("<br>");
    }
		
    print("</td></tr></table>");
}


# Parse @lines -- put the problem headers into @headers, strip out the <h.> tags
sub parse_headers 
{
    @headers = ();
    for $maxcounter (1 .. 50) {
	if (grep{/^<h$maxcounter>/} @lines) {
	    @tmp = grep{/^<h$maxcounter>/} @lines;
	    foreach $piece (@tmp) {
		$piece =~ s/<h\d+\>//;
		chomp($piece);
		push(@headers, $piece);
	    }
	}
    }
}


# Parse "@people" into array three separate arrays -- i don't like the way this is splitting
sub split_people
{
    @people_cp = @people;
    while(scalar(@people_cp)>0){
	if(scalar(@people_cp)>0){push(@peoplebreak1 , shift(@people_cp))}
	if(scalar(@people_cp)>0){push(@peoplebreak2 , shift(@people_cp))}
	if(scalar(@people_cp)>0){push(@peoplebreak3 , shift(@people_cp))}
    }
}


# Delete the specified problem set from the $lines array
sub delete_set 
{
    foreach $tem (@lines) {
	if ($tem !~ /<.$_[0]>/) {
	    push(@newlines, $tem);
	}
    }
    # this is sloppy -- but it avoids changing other subroutines
    @lines = @newlines;   
}

%let_to_noise = ('a', 'h', 
                 'b', 'w', 
                 'c', 'c', 
                 'd', 'r', 
                 'e', 'q', 
                 'f', 'm', 
                 'g', 'b', 
                 'h', 'j', 
                 'i', 'a', 
                 'j', 's', 
                 'k', 't', 
                 'l', 'i', 
                 'm', 'o', 
                 'n', 'n', 
                 'o', 'y', 
                 'p', 'd', 
                 'q', 'g', 
                 'r', 'k', 
                 's', 'l', 
                 't', 'v', 
                 'u', 'x', 
                 'v', 'z', 
                 'w', 'u', 
                 'x', 'p', 
                 'y', 'f', 
                 'z', 'e');




