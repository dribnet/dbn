#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;
use POSIX;

$user = param("user");
$course = param("course");
$item = param("item");

$prob_num = param("problem");
$person = param("person");

ensure_valid_course($course);
standard_setup($course);
ensure_valid_user($user, $item);

parse_file();
parse_headers();
parse_routine("p","problems");

@let = qw( A B C D E F G H I J K L M N O P Q R S T U V W X Y Z );
%let_to_num = ('A', 0, 'B', 1, 'C', 2, 'D', 3, 'E', 4, 'F', 5, 'G', 6, 'H', 7, 'I', 8, 'J', 9, 'K', 10, 'L', 11, 'M', 12, 'N', 13, 'O', 14, 'P', 15, 'Q', 16, 'R', 17, 'S', 18, 'T', 19, 'U', 20, 'V', 21, 'W', 22, 'X', 23, 'Y', 24, 'Z', 25);


beginHTML_header("Design By Numbers Courseware : Solutions", 0, 1);

print("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"665\">");
		
if ($person eq "*everyone*") {

    $count_pe = 0;
    $count_pr = 0;
    $rows = ceil(($#people + 1) / 4);
    
    print("<tr>");
    print("<td width=\"20\"><img src=\"$url/images/1pix.gif\" width=\"28\" height=\"1\"><br>");
    print("</td>");
    #$height = 20 + 123 * $rows; 
    $height = 20 + 122*$rows;  # fixed for dbn 2.0.1
    print("<td width=\"512\" height=\"$height\" valign=\"top\">");
    
    print("<applet codebase=\"$url/dbn\" code=\"DbnApplet.class\" width=512 height=$height>\n");
    #print("<param name=\"display_mode\" value=\"auto\">\n");
    #print("<param name=\"run_mode\" value=\"immediate\">\n");
    print("<param name=\"mode\" value=\"grid\">\n");
    
    for $i (0 .. $#people) {
	$program = "courses/$course/$people[$count_pr]/$prob_num.dbn";
	if ($platform eq "mac") {
	    print("<param name=\"program$i\" value=\"$cgiurl/fetch.cgi?file=$program\">\n");
	} else {
	    if (-f "$path/$program") {  # if the program exists
		#print("<param name=\"program$i\" value=\"$path/$program\">\n");
		print("<param name=\"program$i\" value=\"$url/$program\">\n");
	    } else {
		print("<param name=\"program$i\" value=\"\">\n");
	    }
	}
	
	$count_pr++;
    }
    
    print("<param name=\"bg_color\" value=\"#FFFFFF\">\n");
    print("</applet>\n");
    
    print("</td>\n");
    print("<td valign=\"top\">\n");
    print("<table border=\"0\" cellpadding=\"20\" cellspacing=\"0\">\n");
    $count_pe = 0;
    $count_pr = 0;
    $rows = ceil(($#people + 1) / 4);
    
    for $i (1 .. $rows) {
	print("<tr>\n");
	print("<td width=\"85\" valign=\"top\">\n");
	print("<font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#666666\">\n");
	print("<img src=\"$url/images/1pix.gif\" width=\"1\" height=\"81\" align=\"right\">\n");
	
	$side_nav = '';
	for $j (0 .. 3) {	
	    if ($count_pe <= $#people) {
		#if(dbn exists, link it)
		if(open(F1, "<$path/courses/$course/$people[$count_pe]/$prob_num.dbn")) {
		    #if (-f "$path/courses/$course/$people[$count_pe]/$prob_num.dbn") {
		    $cgi_filename = "$cgiurl/fetch.cgi?format=html&file=courses/$course/$people[$count_pe]/$prob_num.dbn";
		    #$side_nav.="<a href=\"$url/courses/$course/$people[$count_pe]/$prob_num.dbn\">";
		    $side_nav.= "<a href=\"$cgi_filename\" target=\"$people[$count_pe]\">";
		    $side_nav.="$people[$count_pe]<br>";
		    $side_nav.='</a>';
		} else {
		    $side_nav.="$people[$count_pe]<br>";
		}
		close F1;
	    }
	    $count_pe++;
	}
	print $side_nav;
	
	print("</font>\n");  
	print("</td>\n");
	print("</tr>\n");
    }
    
    
    print("</table>\n");
    
    print("</td>\n");
    print("</tr>\n");
    
} else {

    # Truncate the letter off the problem ( ex. 3A -> 3 ) -- this is necessary to
    # find out how many elements are in the set
    $short_num = $prob_num;
    $short_num =~ s/[A-Z]//;
    
    $count_pe = 0;
    $count_pr = 0;
    $rows = ceil(($#{$problems[$short_num - 1]}+1) / 4);
    #print("$rows :: ");
    #$fakeceil = ceil(1.1);
    #print("$fakeceil");
    
    if ($rows < 1) {$rows = 1;}
    
    print("<tr>\n");
    print("<td width=\"20\"><img src=\"$url/images/1pix.gif\" width=\"28\" height=\"1\"><br>\n");
    print("</td>\n");
    $height = 20 + 123 * $rows;
    print("<td width=\"512\" height=\"$height\" valign=\"top\">\n");
    print("<applet codebase=\"$url/dbn\" code=\"DbnApplet\" width=512 height=$height>\n");
    #print("<param name=\"display_mode\" value=\"auto\">\n");
    #print("<param name=\"run_mode\" value=\"immediate\">\n");
    print("<param name=\"mode\" value=\"grid\">\n");
    
    for $i (0 .. $#{$problems[$short_num - 1]}) {
	#print("<param name=\"program$i\" value=\"$url/   \">\n");
	$program = "courses/$course/$person/$short_num$let[$count_pr].dbn";
	
	if ($platform eq "mac") {
	    print("<param name=\"program$i\" value=\"$cgiurl/fetch.cgi?file=$program\">\n");
	} else {
	    if (-f "$path/$program") {
		#print("<param name=\"program$i\" value=\"$path/$program\">\n");
		print("<param name=\"program$i\" value=\"$url/$program\">\n");
	    } else {
		print("<param name=\"program$i\" value=\"\">\n");
	    }
	}

	$count_pr++;
    }
    
    print("<param name=\"bg_color\" value=\"#FFFFFF\">\n");
    print("</applet>");
    
    print("</td>\n");
    print("<td valign=\"top\">\n");
    print("<table border=\"0\" cellpadding=\"20\" cellspacing=\"0\">\n");
    $count_pe = 0;
    $count_pr = 0;
    #$rows = ceil(($#people + 1) / 4);
    
    for $i (1 .. $rows) {
	print("<tr>\n");
	print("<td width=\"85\" valign=\"top\">\n");
	print("<font size=\"2\" face=\"Arial, Helvetica, sans-serif\" color=\"#666666\">\n");
	print("<img src=\"$url/images/1pix.gif\" width=\"1\" height=\"81\" align=\"right\">\n");
	
	$side_nav = '';
	for $j (0 .. 3) {	#if .dbn exists, link it 
	    if ($count_pe <= $#{$problems[$short_num - 1]}) {
		if(open(F1, "<$path/courses/$course/$person/$short_num$let[$count_pr].dbn")){
		    $side_nav.="<a href=\"$cgiurl/fetch.cgi?format=html&file=courses/$course/$person/$short_num$let[$count_pr].dbn\">";
		    $side_nav.="$short_num$let[$count_pr]<br>";
		    $side_nav.="</a>\n";
		} else {
		    $side_nav.="$short_num$let[$count_pr]<br>";
		}
		close F1;					
		
		$count_pe++;
	    }
	    $count_pr++;
	}
	print $side_nav;
	
	print("</font>\n");  
	print("</td>\n");
	print("</tr>\n");
    }
    
    
    print("</table>");
    
    print("</td>");
    print("</tr>");
    
}


# Draw the select elements

print<<END_of_midtable2;
</table>
    <table border="0" cellpadding="10" cellspacing="0" width="665">
    <tr> 
    <td width="20"><img src="$url/images/1pix.gif" width="20" height="1"><br></td>
    <td width="605" valign="top" align="left"> 
END_of_midtable2
    
    print("<font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"#666666\">");
    if ($person eq "*everyone*") {
	# this is where the information regarding the current problem is written
	$temp0 = $prob_num;
	$temp0 =~ s/[0-9]+//;
	#print("temp0 = $temp0\n");
	print("<b>課題 $prob_num</b> -- $problems[$prob_num - 1][$let_to_num{$temp0}]<br>");
    } else {
	# this is where the information regarding the current problem SET is written
	$temp0 = $prob_num;
	$temp0 =~ s/[A-Z]//;
	print("<font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"#666666\">");
	print("<b>課題番号 $temp0</b> -- $headers[$temp0 - 1]<br>");
    }
print("<br>");

print<<END_of_midtable25;				
	<hr size="1" width="95%" noshade align="left">
      <form method="post" action="view.cgi">
	  <table cellpadding="0" cellspacing="0" border="0">
          <tr> 
            <td valign="middle"><font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
			<b>作例:</b>&nbsp;&nbsp;&nbsp;&nbsp;</font></td>
            <td valign="middle"> 
              <select name="problem">
END_of_midtable25


#Puts all of problems into the form select element
$count = 1;
for $i (0 .. $#headers) {
    for $j (0 .. $#{$problems[$i]}) {
	if ($prob_num eq "$count$let[$j]") {
	    print("<option value=\"$count$let[$j]\" selected>課題 $count$let[$j]</option>\n");
	} else {
	    print("<option value=\"$count$let[$j]\">課題 $count$let[$j]</option>\n");
	}
    }
    $count++;
}
	
	
print<<END_of_midtable3;			
              </select>
            </td>
            <td valign="middle"><font face="Arial, Helvetica, sans-serif" size="2">&nbsp;for&nbsp;</font></td>
            <td valign="middle"> 
              <select name="person">
END_of_midtable3

if ($person eq "*everyone*") {
    print("<option value=\"*everyone*\" selected>全員</option>\n");
} else {
    print("<option value=\"*everyone*\">全員</option>\n");
}

#Puts all of the people into the form select element
foreach $tem (@people) {
    if ($person eq "$tem") {
	print("<option value=\"$tem\" selected>$tem</option>\n");
    } else {
	print("<option value=\"$tem\">$tem</option>\n");
    }
}
	
	
print<<END_of_midtable4;
              </select>
            </td>
	    <input type=hidden name="user" value="$user">
            <input type=hidden name="item" value="$item">
	    <input type=hidden name="course" value="$course">
            <td valign="top"> &nbsp;&nbsp;<input type="submit" name="submit" value="見てみよう"></td>
          </tr>
	</table>
      </form>
	  
    </td>
  </tr>
</table>
END_of_midtable4

copyright();

























