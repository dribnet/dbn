#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

$course = lc(trimW(param("course")));
$user = trimW(lc(param("user")));
$password = lc(param("password"));
$submit = param("view");

# Modes:
$MODE_INIT = 0;
$MODE_BADLOGIN = 1;
$MODE_GOODLOGIN = 2;
$MODE_ADMINLOGIN = 3;


# For each letter of the password (item) convert it via the 'let_to_noise' hash and push it to another 
# variable, then replace the existing $item with the new 'noised' password
@uncode = ();
@chars = split //, $password;
for(@chars) {
    push(@uncode, $let_to_noise{$_});
}
$item = join '', @uncode;
$password = $item;


# this code can be half ripped out when someone wants to take the time
# to think about it an remove it. it's mostly represented in globals.pl.


#OPEN given admin.txt AND parse it into variables, check for missing information
if(!open(ADMIN , "$path/courses/$course/admin.txt"))
{
    #ERROR CAUGHT: INVALID or NON-EXISTENT course
    beginHTML_header("Login", 1, 1);
    opendir D1, "$path/courses";
    @courses = readdir(D1);
    close D1;
    for(@courses) {
	if (($_ ne ".")&&($_ ne "..")){
	    $long_title = '';
	    #get the long filename and put it in $long_title
	    if( open(F1 , "<$path/courses/$_/admin.txt")){
		#skip 4 lines and read the 5th
		$junk=<F1>;$junk=<F1>;$junk=<F1>;$junk=<F1>;
		$long_title=<F1>;
		$options.="<OPTION VALUE=\"$_\">$_: $long_title</OPTION>";
	    }
	}
    }
    error_page(); 
}

# Else keep going as if nothing ever went wrong
parse_admin();
close ADMIN;
parse_people();

# Check to see if the user exists
$found_our_guy = 0;
for(@people){
    if (lc($_) eq $user){
	$found_our_guy = 1;
    }
}

$mode = $MODE_BADLOGIN; # Assume nothing then change if conditions are met
if ((length($password)==0) && (length($user)==0)) { $mode = $MODE_INIT }
if ($found_our_guy && ($password eq $real_general_password)) { $mode = $MODE_GOODLOGIN }
if (($user eq $real_admin_login) && ( $password eq $real_admin_password)) { $mode = $MODE_ADMINLOGIN }

if ($mode==$MODE_BADLOGIN)   { $message_text = gold('You supplied an incorrect name or password,
                                                     please see your course leader for information.<br><br>') }
if ($mode==$MODE_INIT)       { $message_text = "" }
if ($mode==$MODE_GOODLOGIN)  { go_home($user, "" ) }
if ($mode==$MODE_ADMINLOGIN) { go_home($user, ($password)) }



beginHTML_header("Login", 1, 1);

# Begin the second table
print<<END_of_middle;
<table border="0" cellpadding="10" cellspacing="0" width="665">
  <tr> 
    <td width="20" height="100"><img src="$url/images/1pix.gif" width="20" height="1">&nbsp;</td>
    <td width="605" height="100" valign="top" align="left">$message_text
      <font size="2" face="arial,helvetica,san-serif" color="#666666">
      Input your name and password and press 'enter' to begin.
      <br>
      <br>
      <form action="login.cgi" method="post">
      Username:<br>
      <input type="hidden" name="course" value="$course">
      <input type="text" name="user" value="$user">
      <p>
      Password:<br>
      <input type="password" name="password" value="$password">
      <br>
      <br> 
      <br>
      <br>  
      <input type="submit" value="Enter" name="view">
      </form>
      </font>
    </td>
  </tr>
</table>
END_of_middle

copyright_simple();


sub go_home
{
    print "Content-type: text/html\n\n";
    print "<HTML>\n";
    print "<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0;url=home.cgi?course=$course&user=$_[0]&item=$item\">";
    print "</HTML>\n";
    exit 0;
}


sub error_page
{
print<<END_of_nocourse;
    <table border="0" cellpadding="10" cellspacing="0" width="665">
     <tr> 
      <td width="20" height="100"><img src="$url/images/1pix.gif" width="20" height="1"><br>
      </td>
      <td width="605" height="100" valign="top" align="left">
	  <font size="2" face="arial,helvetica,san-serif" color="#666666">
	  <font color="CC9900"><b>An error occurred.</b></font>
	  <br>
	  <br>Please select the name of the course you are trying to access and press 'continue'.<br>
	  <form method=post>
	  <SELECT name="course">
	     <OPTION value="" selected>Select a course</OPTION>
	     $options
	  </SELECT>	  
	  <input type=submit value="Continue">
	  </form>
       </td>
      </tr>
    </table>
END_of_nocourse

    copyright_simple();
    exit 0;
}








