#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI param;

$user = param("user");
$course = param("course");
$item = param("item");

#ensure_valid_course($course);
#standard_setup($course);
#ensure_admin_user($user, $item);

my $mode = param("mode"); 	# determine which cgi script they will return to
my $submode = param("submode"); # either 'Add a problem' or 'Remove the last problem'
my $num = param("num");		# number of the set to display

if ($submode eq "Add a problem") {
    $submode = "addprob";
    $page = "editproblems.cgi";
} elsif ($submode eq "Remove the last problem") {
    $submode = "remove";
    $page = "editproblems.cgi";
} elsif ($submode eq "Save and Return") {
    $submode = "remove";
    $page = "problems.cgi";
}

@let = qw(A B C D E F G H I J K L M N O P Q R S T U V W X Y Z);

#chdir("$path/courses/$course");

parse_file();		       # creates @lines
parse_routine("p","problems"); # creates @problems
delete_set("$num");	       # first delete the current set in the array

# collect information from the form fields
my $header = param("header");
for $i (0 .. $#{$problems[$num - 1]}) {
    $n_problems[$i] = param("p_$let[$i]");
    $n_media[$i] = param("m_$let[$i]");
}

#Add to the end of the file, the line 'tags' must be correct
open (PROBLEMSRE, ">$path/courses/$course/problems.txt") || 
    die "can't create problems.txt: $!";
foreach $item (@lines) {
    print PROBLEMSRE "$item\n";
}
print PROBLEMSRE "<h$num>$header\n";
for $i (0 .. $#{$problems[$num - 1]}) {
    print PROBLEMSRE "<p$num>$n_problems[$i]\n";
    print PROBLEMSRE "<m$num>$n_media[$i]\n";
}
close PROBLEMSRE;
chmod(0666, "$path/courses/$course/problems.txt");


# execute a redirect
if ($page eq "editproblems.cgi") {
    print("Location: $cgiurl/$page?num=$num&mode=$mode&submode=$submode&user=$user&course=$course&item=$item\n\n");
} elsif ($page eq "problems.cgi") { 
    print("Location: $cgiurl/$page?user=$user&course=$course&item=$item\n\n");
} else {
    print("Location: $cgiurl/$page?user=$user&course=$course\n\n");
}





















