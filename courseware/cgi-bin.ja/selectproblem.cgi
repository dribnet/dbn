#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

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


beginHTML_header("Design By Numbers Courseware : Problem Set $problem", 0, 1);

#Begin second table -- contains the problem decriptions
print<<END_of_problem1;
<table border="0" width="665" cellpadding="10" cellspacing="0">
  <tr> 
    <td width="20"><img src="$url/images/1pix.gif" width="20" height="8"><br></td>
    <td colspan="12" width="605"><font color="#999999" size="3">
	<b>課題を選びます</b><br>
	<font color="#666666" size="2">課題には一つづつ取り組んで下さい。一つの課題を終えると、このページに戻りますので次に進んで下さい。<br>前に終えた課題をもう一度選べば、新たにやり直しができます。
	</font></font><br>
	</td>
    </tr>
END_of_problem1

#Determine which problem set to display
$problem = param("no");
@let = qw(0 A B C D E F G H I J K L M N O P Q R S T U V W X Y Z);
$r = 1;

#check to see is there are six or less problem sets remaining, hold current number in varable $problem_numbers
while ($r < $#headers + 2) {
    print("<tr valign=\"top\">");
    print("<td width=\"20\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#333333\">&nbsp;<\/td>");
    for $k ($r .. $r + 5) {
	print("<td valign=\"top\" width=\"400\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#666666\">");
	#This is the problem set print loop
	for $i (0 .. $#{$problems[$k - 1]}) {
	    print("<a href=\"work.cgi?set=$k&num=$let[$i + 1]&sub=$i&user=$user&course=$course&item=$item\">$k$let[$i + 1]<\/a><br>");
	}
	print("<\/font><\/td>");
	print("<td width=\"20\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#333333\">&nbsp;<\/td>");
	$r++;
    }
    print("<\/tr>");
    if ($r < $#headers + 2) {
	print("<tr valign=\"top\">");
	print("<td width=\"20\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#333333\">&nbsp;<\/td>");
	print("<td width=\"600\" colspan=\"12\">&nbsp;<\/td>");
	print("<\/tr>");
    }
}

print("<\/table>");

copyright();







