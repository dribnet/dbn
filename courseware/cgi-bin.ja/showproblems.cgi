#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

$user = param("user");
$course = param("course");
$item = param("item");

ensure_valid_course($course);
standard_setup($course);

parse_file();
parse_headers();
parse_routine("p","problems");
split_people();


beginHTML_header("Design By Numbers Courseware : Problem Set $problem", 0, 1);

#Begin second table -- contains the problem decriptions
print<<END_of_problem1;
<table border="0" width="665" cellpadding="10" cellspacing="0">
  <tr> 
    <td width="20"><img src="$url/images/1pix.gif" width="20" height="8"><br></td>
    <td colspan="2" width="605"><font face="Arial, Helvetica, sans-serif" color="#999999" size="3"><b>
END_of_problem1

#Determine which problem set to display
$problem = param("no");
@let = qw(0 A B C D E F G H I J K L M N O P Q R S T U V W X Y Z);

print("課題番号 $problem</b><font color=\"#666666\"> -- $headers[$problem - 1]");
print("</font></font><br>");
print("</td>");
print("</tr>");

#This is the problem set print loop
$count = 1;
for $i (0 .. $#{$problems[$problem - 1]}) {
    print("<tr valign=\"top\">");
    print("<td>&nbsp; <\/td>");
    print("<td width=\"20\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#333333\">");
    
    print("<a href=\"view.cgi?problem=$problem$let[$count]&person=*everyone*&user=$user&course=$course&item=$item\">$problem$let[$count]</a></font></td>");
    print("<td valign=\"top\" width=\"565\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#666666\">");
    print("$problems[$problem - 1][$count - 1]");
    print("</font></td>");
    print("</tr>");
    $count++;
}

print("</table>");

#Draw the people table
print<<END_of_people1;
<br>
<br>
<table border="0" cellpadding="10" cellspacing="0" width="665">
  <tr valign="top"> 
    <td width="20" align="right" valign="top">
	<font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people1

print<<END_of_people2;
	  </font>
	  <img src="$url/images/1pix.gif" width="20" height="1"></td>
    <td colspan="5" valign="top"><font face="Arial, Helvetica, sans-serif" color="#999999" size="3">
	<b>仲間の作例: </b></font></td>
  </tr>
  <tr valign="top"> 
    <td width="20">&nbsp;</td>
    <td width="188"valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people2

#Writing the 1st of 3 groups of people
foreach $temp (@peoplebreak1) {
    print("<a href=\"view.cgi?problem=$problem$let[1]&person=$temp&user=$user&course=$course&item=$item\">$temp<\/a><br>");
}

print<<END_of_people3;
	</td>
    <td width="20">&nbsp;</td>
    <td width="188"valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people3
	
#Writing the 2nd of 3 groups of people
foreach $temp (@peoplebreak2) {
    print("<a href=\"view.cgi?problem=$problem$let[1]&person=$temp&user=$user&course=$course&item=$item\">$temp<\/a><br>");
}

print<<END_of_people4;	  
     </td>
	<td width="20">&nbsp;</td>
    <td width="188" valign="top"><font size="2" face="Arial, Helvetica, sans-serif" color="#666666">
END_of_people4

#Writing the 3rd of 3 groups of people
foreach $temp (@peoplebreak3) {
    print("<a href=\"view.cgi?problem=$problem$let[1]&person=$temp&user=$user&course=$course&item=$item\">$temp<\/a><br>");
}
	  
print<<END_of_people5;
    </td>
  </tr>
</table>
END_of_people5

copyright();





