#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI qw(:standard);

#welcoming party
$begin_message = "";

# Convert the form input
$short_title = lc(param("short_title")); 		#the name of the course directory
$long_title = param("long_title"); 			#the name of the course header
$admin_login = param("admin_login"); 		        #login for the administrator
$admin_password = lc(param("admin_password"));   	#password for administrator
$general_password = lc(param("general_password")); 	#student password
$structure = param("structure");			#radio button choice
$people = param("people"); 				#list of the people in the class


#putting radio buttons back into form
if($structure eq "scratch"){ $scratch_checked = " checked" }
if($structure eq "sample") { $sample_checked  = " checked" }


#make all the "* err strings to go by the fields
$short_title_err = "";
$long_title_err = "";
$admin_login_err = "";
$admin_password_err = "";
$general_password_err = "";
$people_err = "";

$conf = 0;   #This is the variable that will change to 1 if the form input checks out OK
$mode = param("mode");       #determine what they should go

if (!$mode) {
	$mode = "void";
} else {

    #CLEAN UP TIME
    #remove surrounding \s for $short_title $admin_login and each person's name

    #parse all of the students
    $people = trimW($people);
    @people = split(/\s*,+\s*,*/, $people); 	#getting that which is seperated by commas
    $people = join(', ', @people);  #put them back, all cleaned up
    
    $short_title = trimW($short_title);
    $admin_login = trimW($admin_login);


    #ERROR CHECK TIME
    #Check all input to see if it is present and doesn't conflict. If it's of OK, set $conf to 1.

    $conf = 0; #reset problem count
    #course dir blank?
    if(!length($short_title)){
	$short_title_err = errHTML("* required");
	$conf++;
    }
    #course name has illegal file characters?
    elsif($short_title =~ /.*[\\\/:\?\*"<>|\s].*/){
        $short_title_err = errHTML("* cannot contain: \\ / : * ? \" < > | or spaces");
	$conf++;
    }
    #course dir already exists ?
	elsif (-d "$path/courses/$short_title") {
	  #yes? set the (nonexiststent form) in confirm to visible.
	   $short_title_err = errHTML("* This course directory already exists.
			                        You can either choose another name
						or remove the existing \"$short_title\" directory.</a>");
	   $conf++;
	}	
	#admin login blank?
	if(!length($admin_login)){
	   $admin_login_err = errHTML("* required");
	   $conf++;
	}
		
	$anyone_wrong = 0;
	foreach(@people){
	   if($_ =~ /.*[\\\/:\?\*"<>|\s].*/){
	   	$people_err.=errHTML("$_").'<br>';
	       	$conf++;
		$anyone_wrong++;
	    }
        }
        if($anyone_wrong>0){$people_err = errHTML("* these names
					cannot contain \\ / : * ? \" < > | or spaces:").'<br>'.$people_err}
	#there must be at least one student
	if(!@people){
		$people_err = errHTML("* there must be at least one person in the course.").'<br>';
		$conf++;
	}
	#duplicate students?
		my $found_one = 0;
		foreach(@people){
			$this_guy = $_;
			my $the_one_match_allowed = 0;
			foreach(@people){
				if(lc($_) eq lc($this_guy)){
					if(++$the_one_match_allowed==2){
                    	$found_one++;
                    }
				}
			}
		}
		if ($found_one>0){
                                $people_err .= errHTML("* duplicates found<br>");
                                $conf+=($found_one/2);}
	

}#done checking


if ($mode eq "Create course") {
    
    #chdir("$path/courses");
    $courses_dir = "$path/courses";
		
    # Making the course directory
    $s = $short_title;
    $inc = 1;
    while(!mkdir("$courses_dir/$short_title", 0777)) {
	$inc++; 
	$short_title="$s$inc";
    } # make dir2, dir3, dirN
    #This shouldn't be necessary, but it seems to be
    chmod(0777, "$courses_dir/$short_title");                    
    
    # Change directory to the one just created
    #chdir("$courses_dir/$short_title");
    $course_dir = "$courses_dir/$short_title";
    
    @people = split(/, /, $people);
    # Make each student directory
    foreach $item (@people) {
	$item = lc($item);
	mkdir("$course_dir/$item", 0777) || die "can't mkdir $item : $!";
        # This shouldn't be necessary, but it seems to be
	chmod(0777, "$course_dir/$item");                   
    }    

    # Create/write the data files

    # Encode passwords
    # For each letter of the password (item) convert it via the 'let_to_noise' hash and push it to another 
    # variable, then replace the existing $item with the new 'noised' password
    @uncodecr = ();
    @chars = split //, $general_password;
    for(@chars) {
        push(@uncodecr, $let_to_noise{$_});
    }
    $item = join '', @uncodecr;
    $general_password = $item;

    @uncodecr = ();
    @chars = split //, $admin_password;
    for(@chars) {
        push(@uncodecr, $let_to_noise{$_});
    }
    $item = join '', @uncodecr;
    $admin_password = $item;
    
    # Make admin.txt
    open (ADMIN, ">$course_dir/admin.txt") || die "can't open admin.txt: $!";
    print ADMIN "$admin_login\n";
    print ADMIN ("$admin_password")."\n";
    print ADMIN ("$general_password")."\n";
    print ADMIN "$short_title\n";
    print ADMIN "$long_title\n";
    close (ADMIN) || die "can't close admin.txt: $!";
    
    # Make people.txt
    open (PEOPLE, ">$course_dir/people.txt") || die "can't create people.txt : $!";
    print PEOPLE join("\n" , @people)."\n";
    close PEOPLE;
    
    #make the customized html which calls login.cgi
    open (INDEX , ">$course_dir/index.html");
    print INDEX "<HTML><TITLE>$long_title</TITLE>";
    print INDEX "<META HTTP-EQUIV=\"REFRESH\" ";
    print INDEX "CONTENT=\"0;URL=$cgiurl/login.cgi?course=$short_title\">";
    print INDEX "<BODY BGCOLOR=\"#FFFFFF\"></BODY></HTML>";
    close INDEX;

    # Make 'problems.txt' - if they chose the sample course then
    # samplecourse.txt read samplecourse.txt in samplecourse.txt and write to problems.txt
    # If they chose from scratch, write a skeleton for problem 1

    if ($structure eq "sample") {
	#chdir("$path/courses");    #switch back to the course directory
	open (SAMPLE, "<$path/courses/samplecourse.txt") || 
	    die "can't open samplecourse.txt: $!";
	@lines = <SAMPLE>; #reads the entire file into @lines
	close SAMPLE;

	#chdir("$short_title");  
	$r = open (PROBLEMS, ">$course_dir/problems.txt") ||
	    die "can't create problems.txt : $!";

	foreach $item (@lines) {
	    print PROBLEMS $item;       #no '\n' printed here because no chomp used above
	}
	close PROBLEMS;
    } else {
	open (PROBLEMS, ">$course_dir/problems.txt") || 
	    die "can't create problems.txt : $!";
	print PROBLEMS "<h1>( Sample title )\n";
	print PROBLEMS "<p1>( Sample problem )\n";
	print PROBLEMS "<m1>dbn\n";
	close PROBLEMS;
    }
    
    chmod(0666, "$course_dir/admin.txt", "$course_dir/title.txt", 
	  "$course_dir/people.txt", "$course_dir/problems.txt");
    
    
    #Provide confirmation that the class has been created and a button to take them to the homepage
    beginHTML_header("Create", 1, 1);
    
    #WRITE THE BODY OF THE PAGE
    print<<END_of_body;
    
    <table border="0" cellpadding="10" cellspacing="0" width="665">
       <tr valign="top"> 
	 <td width="20" height="100">
	    <img src="$url/images/1pix.gif" width="20" height="8"><br>
         </td>
	 <td width="605" height="100" valign="top" align="left">
		<font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
		<font size="3" color="#999999"><b>Your course has been created</b></font><br>
		<br>
      		Please note the following information. You will need to know it to edit the course.<br>
 		<br>
      		<b>Administrator username:</b> $admin_login<br>
	       	<b>Administrator password (encoded):</b> $admin_password<br>
       		<br>
	       	<br>
		Your students will also need to know their usernames and password to view and upload work:<br>
		<br>
	      	<b>General password (encoded):</b> $general_password<br>
		<b>Student usernames:</b> $people<br>
	      	<br>
		<br>
                <hr size="1" width="95%" noshade align="left"><br>
                <font color="#006699"><b>To log-on to the course go to:</b></font><br><br>
                $cgiurl/login.cgi?course=$short_title      
                <br>
                <br>
                <br>
    
                </font>
              </td>
	    </tr>
	 </table>
END_of_body

copyright_simple();


}elsif($mode eq "Continue" && $conf>=1) { #there was at least one thing wrong with the entries.
	#reprint the initial skeleton, with errors
	
	if($conf==1){$begin_message= "There is a problem with your entry,
					denoted with an * beside the entry field.";

	}else{
	#make it into english number representation
	$wordnum = word_to_num($conf);
	$begin_message= "There were $wordnum problems with your entry,
					 denoted with an * beside the entry field.";
					}

	
	$begin_message = errHTML($begin_message);
	init_form();

}elsif($mode eq "Continue" && $conf==0) { # systems all clear, no booboos - prepare for course creation


    # Re-print the form information and ask the user to confirm
    beginHTML_header("Create", 1, 1);
    
    #WRITE THE BODY OF THE "CONFIRM CAPTURE" PAGE
print<<END_of_body;
    <table border="0" cellpadding="10" cellspacing="0" width="665">
    <tr valign="top"> 
       <td width="20" height="20">
	  <img src="$url/images/1pix.gif" width="20" height="8"><br>
       </td>
       <td width="605" height="100" valign="top" align="left">
          <font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
          <font size="3" color="#999999"><b>Confirm your input</b></font><br>
          <br>
          Please check and see if your data has been
          captured correctly. If you want to make changes 
          use the back button on your browser and
          alter the forms. The student and problem 
          statement information is very easy to change
          later, but the course title and passwords can't
          be changed.<br>
          <br>
	  <b>short title:</b> $short_title<br>
					<b>long title:</b> $long_title<br>
					<b>admin login:</b> $admin_login<br>
					<b>admin password:</b> $admin_password<br>
					<b>general password:</b> $general_password<br>
					<b>structure:</b> $structure<br>
					<b>people:</b> $people<br>
					<font COLOR="#00000" SIZE=3>
					<br>
					<br>
					<br>
					<hr size="1" width="95%" noshade align="left">
					<br>
					<form action="create.cgi" method="post">
						<input type="hidden" name="short_title" value="$short_title">
						<input type="hidden" name="long_title" value="$long_title">
						<input type="hidden" name="admin_login" value="$admin_login">
						<input type="hidden" name="admin_password" value="$admin_password">
						<input type="hidden" name="general_password" value="$general_password">
						<input type="hidden" name="structure" value="$structure">
						<input type="hidden" name="people" value="$people">
						<input type="submit" name="mode" value="Create course">
					</form>
					</font>
			</td>
		  </tr>
		</table>
END_of_body
	
	copyright_simple();

} else { # we've only just begun
	$sample_checked = " checked";
	$begin_message = <<END_BEGIN_DEFAULT_MESSAGE;
	<p><font size="3"><b><font color="#999999">Creating a course</font></b></font><br>
			<br>
			Welcome to Design By Numbers Courseware. The first step in creating a 
			new course is to fill out the form elements on this page.</p>
			</font><p>&nbsp;</p>
END_BEGIN_DEFAULT_MESSAGE

	init_form();
}






# BEGIN HTML SUBROUTINES

sub init_form
{
beginHTML_header("Create", 1, 1);
#WRITE THE BODY OF THE PAGE
print<<END_of_body;

<table border="0" cellpadding="10" cellspacing="0" width="665">
    <tr valign="top"> 
	<td width="20" height="100">
	    <img src="$url/images/1pix.gif" width="20" height="1"><br>
	</td>
	<td width="605" height="100" valign="top" align="left">
	    <font face="Arial, Helvetica, sans-serif" size="2" color="#666666"> 
	    $begin_message
	    <font face="Arial, Helvetica, sans-serif" size="2" color="#666666">
			
	    <form action="create.cgi" method="post">
	    <b>Step 1-- Course Title</b><br>
	    What is the title of your course? You need to select a long and short 
	    title. The short title will become the name of the folder/directory your 
            course will live in. The long title will appear at the top of every page 
       	in the course site.
	    <br>
		<br>
		    Short title <font size="1">(example: MAS110)</font><br>
			<input type="text" name="short_title" value="$short_title" size="12" maxlength="12">$short_title_err
			<br>
			<br>
			Long title <font size="1">(example: MAS110 -- Fundamentals of Computational 
					Media Design)</font><br>
			<input type="text" name="long_title" value="$long_title" size="55">$long_title_err
		      <br>
			<br>
			<br>
			<b><br>
			Step 2 -- Administration</b><br>
			You need to create two separate passwords: one for yourself (the administrator) 
			and one for everyone else. You must also select an administrator login 
			name for yourself.<br>
			<br>
			<font face="Arial, Helvetica, sans-serif" size="2" color="#666666"> 
				<table border="0" width="100%" cellspacing="0" cellpadding="2">
				<tr> 
					<td><font face="Arial, Helvetica, sans-serif" size="2" color="#666666">Admin 
					login<br>
					</td>
					<td>&nbsp;</td>
				</tr>
				<tr> 
					<td><font face="Arial, Helvetica, sans-serif">
					<input type="text" name="admin_login" value="$admin_login">$admin_login_err
					</td>
					<td>&nbsp;</td>
				</tr>
				<tr valign="bottom"> <font face="Arial, Helvetica, sans-serif">
					<td><font  face="Arial, Helvetica, sans-serif" 
						size="2" color="#666666">Admin password</font><br>
					</td>
					<!--td><font size="2" color="#666666">Re-enter admin password to confirm</font><br>
					</td-->
						</tr>
						<tr> 
					<td> <font face="Arial, Helvetica, sans-serif">
					<input type="text" name="admin_password" value="$admin_password">
					</td>
					<td> 
					<!--input type="text" name="admin_password_confirm"> $admin_password_err
					</td-->
				</tr>
				<tr> 
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr> 
					<td><font face="Arial, Helvetica, sans-serif" size="2"
							color="#666666">General password</font><b><br>
					</b></td>
					    </tr>
				<tr> 
					<td><b> <font  face="Arial, Helvetica, sans-serif" >
						<input type="text" name="general_password" value="$general_password">
						</b></td>
					<td> 
				</tr>
			</table>
			</font> 
			<p><br>
			<br>
			<b>Step 3 -- Structure</b><br>
			&nbsp;What is the structure of your course? You can start from scratch 
			or begin with the sample course. If you are starting from scratch, you 
			must also select the number of problem sets you want to create. Keep 
			in mind that DBN Courseware is designed to support multiple problems 
			within each problem set. <br>
			<br>
			<input type="radio" name="structure" value="sample" $sample_checked>
			Begin with sample course<br>
			<input type="radio" name="structure" value="scratch" $scratch_checked>
			Make your own course
			<br>
			<br>
			<br>
			<br>
			<b>Step 4 -- People</b> <br>
			How many people are in the course? Enter their user names in the text 
			field, each one separated with a comma. Each user name should be between 
			five and twelve alpha-numeric characters long and include no spaces. <br>
			<br>
			<font size="1">(example: dave, frank, smyslov, michael, hal) </font><br>$people_err
			<textarea name="people" cols="55" rows="3">$people</textarea>
			<br>
			<br>
			<br>
			<br>
			<hr size="1" width="95%" noshade align="left">
			<br><FONT COLOR="#000000">
			<input type="submit" value="Continue"><input type="hidden" name="mode" value="Continue">
			</form>
			<br>
			</font> </td>
		</tr>
	    </table>
END_of_body
copyright_simple();
}

sub errHTML
{
	return "<FONT face=\"Arial, Helvetica, sans-serif\" COLOR=\"#CC9900\" SIZE=\"2\"><B>@_</B></FONT>";
}


sub word_to_num
{
	if($_[0]==1){return "one"}
	elsif($_[0]==2){return "a couple of"}
	elsif($_[0]==3){return "a few"}
	else{return "some"}
}



