#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;
#use CGI qw(:standard);

$user = param("user");
$course = param("course");
$item = param("item");

ensure_valid_course($course);
standard_setup($course);
ensure_valid_user($user, $item);

parse_file();
parse_headers();
parse_routine("p","problems");
split_people();


beginHTML_header("Design By Numbers Courseware : $long_title", 1, 1);

#Draw the 'Work on Problems' table
print("<table border=\"0\" width=\"665\" cellpadding=\"10\" cellspacing=\"0\">");
if ($user ne "guest") {
print<<END_of_problems1;
  <tr> 
    <td width="20" align="right" valign="top">
	  <img src="$url/images/1pix.gif" width="20" height="1"><br>
    </td>
    <td colspan="2" width="605" valign="top">
    <font size="2" face="Arial, Helvetica, sans-serif" color="666666">
	<table border="0" width="90%" cellpadding="2" cellspacing="0" bgcolor="#E2E2E2">
          <tr>
            <td><font size="3" color="999999" size="3"><b>
               DBNの起動</b></font></td>
          </tr>
        </table>
        <br>
        <a href="$cgiurl/selectproblem.cgi?user=$user&course=$course&item=$item">
          <img src="$url/images/console.gif" width="110" height="58" align="top" border="0" hspace="0"></a>
	 <a href="$cgiurl/selectproblem.cgi?user=$user&course=$course&item=$item">ここをクリック</a> 
        して, Design By Numbers！</font>
	<br>
	<br></td>
  </tr>
END_of_problems1
}


#Draw the beginning of the problem set table
print<<END_of_problems1;
  <tr> 
    <td width="20" align="right" valign="top">
	<font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_problems1


#Draw the edit link if the user is the administrator
if (trimW(lc($user)) eq trimW(lc($real_admin_login))) { 
	print("<a href=\"problems.cgi?user=$user&course=$course&item=$item\"><b>Edit</b></a><br>");
}

print<<END_of_problems1;
	  <img src="$url/images/1pix.gif" width="20" height="1"><br></font>
    </td>
    <td colspan="2" width="605" valign="top">

<table border="0" width="90%" cellpadding="2" cellspacing="0" bgcolor="#E2E2E2">
          <tr> 
            <td><font size="2"><b><font color="#999999" size="3">課題一覧</font></b></font> </td>
          </tr>
        </table>


</td>
  </tr>
END_of_problems1


#Insert problem tr's here
$i = 1;
foreach $temp (@headers) {
  print("<tr valign=\"top\">\n"); 
  print("<td width=\"20\" valign=\"top\">&nbsp;</td>\n");
  print("<td width=\"20\" valign=\"top\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#333333\">\n");
  print("<a href=\"showproblems.cgi?no=$i&user=$user&course=$course&item=$item\">$i</a></font>\n");
  print("</td>\n");
  print("<td valign=\"top\" width=\"565\" valign=\"top\">\n");
  print("<font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#666666\">\n");
  print("$temp</font>\n");
  print("</td>\n");
  print("</tr>\n");
  $i++;
}


#Draw the remainder of the problems table
print<<END_of_problems2;
</table>
END_of_problems2


#Draw the people table
print<<END_of_people1;
<br>
<br>
<table border="0" cellpadding="10" cellspacing="0" width="665">
  <tr valign="top"> 
    <td width="20" align="right" valign="top">
	<font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people1

#Draw the edit link if the user is the administrator
if (lc(trimW($user)) eq lc(trimW($real_admin_login))) { 
	print("<a href=\"people.cgi?user=$user&course=$course&item=$item\"><b>Edit</b></a><br>");
}

print<<END_of_people2;
	  </font>
	  <img src="$url/images/1pix.gif" width="20" height="1"></td>
    <td colspan="5" valign="top"><font size="2" face="Arial, Helvetica, sans-serif">

    <table border="0" width="90%" cellpadding="2" cellspacing="0" bgcolor="#E2E2E2">
      <tr>
        <td>
          <b><font color="#999999" size="3">作例一覧</font></b></font>
        </td>
      <tr>
    </table>

     </td>
  </tr>
  <tr valign="top"> 
    <td width="20">&nbsp;</td>
    <td width="188" valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people2

#Writing the 1st of 3 groups of people
foreach $temp (@peoplebreak1) {
    print("<a href=\"view.cgi?problem=1A&person=$temp&user=$user&course=$course&item=$item\">$temp</a><br>\n");
}

print<<END_of_people3;
	</td>
    <td width="20">&nbsp;</td>
    <td width="188" valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people3
	
#Writing the 2nd of 3 groups of people
foreach $temp (@peoplebreak2) {
    print("<a href=\"view.cgi?problem=1A&person=$temp&user=$user&course=$course&item=$item\">$temp</a><br>\n");
}

print<<END_of_people4;	  
     </td>
	<td width="20">&nbsp;</td>
    <td width="188" valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people4

#Writing the 3rd of 3 groups of people
foreach $temp (@peoplebreak3) {
    print("<a href=\"view.cgi?problem=1A&person=$temp&user=$user&course=$course&item=$item\">$temp</a><br>\n");
}
	  
print<<END_of_people5;
      </font>
    </td>
  </tr>
</table>
END_of_people5

copyright();














