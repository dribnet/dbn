#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

$user = param("user");
$course = param("course");
$item = param("item");

$new_person = lc(trimW(param("new"))); #the new person to add
$mode = param("page"); #The name of the page the user is coming from

ensure_valid_course($course);
standard_setup($course);
ensure_admin_user($user, $item);



# There are three ways that people can get to this page:
#
# 1) entering from the edit people link on the homepage
# 2) re-entering the page hoping they have added a person
# 3) re-entering the page with the hope of having removed a person from the list


#The user has arrived at this page from home, add, or delete
if (($mode eq "add")) {
    # Add an element to the people array and then write to file
	
    if( person_err($new_person) eq "" ){
	# New name is a valid name -- go ahead and add
	push(@people, $new_person);
	$the_person="$path/courses/$course/$new_person";
	mkdir("$the_person", 0777);
	# This shouldn't be necessary, but it seems to be
	chmod(0777, "$the_person"); 			
	
	$people_file = "$path/courses/$course/people.txt";
	open (PEOPLEADD, ">$people_file") || die "can't open people.txt : $!";
	for (@people) {
	    print PEOPLEADD "$_\n";
	}
	close PEOPLEADD;
	chmod(0666, $people_file);
	$new_person = "";
    } else {
	$new_person_err = gold(" ".person_err($new_person));
    }
    
} elsif ($mode eq "remove") {
    $to_remove = param("name");
    
    $people_file="$path/courses/$course/people.txt";
    open (PEOPLEREM, ">$people_file") || die "can't open people.txt : $!";
    
    $trans = ();
    foreach $tem (@people) {
	if ($tem ne $to_remove) {
	    push(@trans, $tem); # add them all one by one, skip the one to remove, if you find it.
	}
    }
    
    foreach $temp (@trans) {
	print PEOPLEREM "$temp\n";
    }
    close PEOPLEREM;
    chmod(0666, $people_file);
    
    # transfer the contents of the trans array into the 
    # people array for display purposes
    @people = @trans;
    
} else {
    #Nothing is happening here now
}



beginHTML_header("Design By Numbers Courseware : Edit People", 0, 1);

#BEGIN THE SECOND TABLE
print<<END_of_middle1;
<table border="0" cellpadding="10" cellspacing="0" width="665">
  <tr> 
   <td width="20" height="100">
       <img src="$url/images/1pix.gif" width="20" height="8"><br>
   </td>
   <td width="605">
   <font size="3" face="Arial, Helvetica, sans-serif" color="#999999"><b>
       Add a person</font></b><br>
   <font size="2" face="Arial, Helvetica, sans-serif" color="#333333">To 
   add a new person to the course type in their name and click the &quot;Add&quot; 
   button. Please remember to limit the name to one word.<br>
   <br>
   <form action="people.cgi" method="post">
   <input type="hidden" name="user" value="$user">
   <input type="hidden" name="item" value="$item">
   <input type="hidden" name="course" value="$course">
   <input type="hidden" name="page" value="add">
   <input type="text" name="new" size="20" value="$new_person">
   <input type="submit" name="add" value="Add">$new_person_err
   <br>
   </form>
   <br>
   <br>
   </font><font size="3" face="Arial, Helvetica, sans-serif" color="#999999"><b>Remove a person</b></font>
   <br>
   <font size="2" face="Arial, Helvetica, sans-serif"  color="#333333">To remove 
   a person, select their check box and click the &quot;Remove&quot; button. <br>
   <form action="people.cgi" method="post">
   <select name="name">
   <option value="default">Select a person &nbsp;&nbsp;</option>
END_of_middle1


foreach $temp (@people) {
    print("<option value=\"$temp\">$temp<\/option>");
}


#COMPLETE THE MIDDLE TABLE
print<<END_of_middle2;
	</select>
	<input type="hidden" name="page" value="remove">
	<input type="hidden" name="user" value="$user">
        <input type="hidden" name="item" value="$item">
  	<input type="hidden" name="course" value="$course">
	<input type="submit" name="remove" value="Remove">
	</form>
	<br>
	<br>
	<hr size="1" width="95%" noshade align="left">
	<br>
	<form action="home.cgi" method="post">
	<input type="hidden" name="user" value="$user">
  	<input type="hidden" name="course" value="$course">
        <input type="hidden" name="item" value="$item">
	<input type="submit" value="Return">
	</font></p>
	</form>
    </td>
 	</tr>
	</table>
END_of_middle2

copyright();



sub person_err
{
    # must be a legal file handle
    if($_[0] =~ /.*[\\\/:\?\*"<>|\s].*/){ return "* cannot contain \\ / : * ? \" < > | or spaces" }
    #can't be blank
    if($_[0] eq ""){ return "* must enter a name" }
    #duplicate students?
    for(@people){
        if(lc($_) eq lc($_[0])){ return "* duplicate found" }
    }
    return "";
}















