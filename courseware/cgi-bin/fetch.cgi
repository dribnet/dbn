#!/usr/bin/perl
require 'globals.pl';
package courseware;
use CGI qw(:standard);
use Cwd;


# i've left the debugging stuff in as comments until we can get
# a chance to check this on the mac to see if it works. ha ha.

my $filename = param("file");
my $format = param("format");

#print "Content-type: text/html\n\n";

use Cwd 'chdir';
chdir '.';  # chdir ''; also seems to work
#print "after chdir . -- $ENV{PWD} <BR>";
my $old_dir = $ENV{PWD};

#print "path is -- $path <BR>";
chdir $path; 
$full_parent = $ENV{PWD};
#print "after chdir to path -- $full_parent <BR>";

chdir $old_dir;
#print "$ENV{PWD} <BR>";

$filename_parent = "$path/$filename";
$filename_parent =~ s/\/[^\/]*$//;
chdir $filename_parent;
$full_filename_parent = $ENV{PWD};
#print "after chdir to path/filename's parent -- $full_filename_parent <BR>";

# reset things so they're happy when i try to open the file
chdir $old_dir;

#print "$full_parent <BR> $full_filename\n";

#print "$full_filename_parent <BR>";
#print "must begin with $full_parent <BR>";
if (!($full_filename_parent =~ /$full_parent/)) {
    # someone's trying to access something illegal
    print "you can't do dat\n";
    exit;
}

#@parent_pieces = split('/', $full_parent);
#@filename_pieces = split('/', $full_filename_parent);

#shift @parent_pieces;
#shift @filename_pieces;

#$working = 1;
#while ($working) {
#$bad = 0;
#for (;;) {
#    $a = shift @parent_pieces;
#    last if ($a eq '');
#    $b = shift @filename_pieces;
#    print "'$a' '$b' <BR>\n";
#    #last if ($a ne $b);
#    if ($a ne $b) {
#	$bad = 1;
#    }
#}
#print "$bad\n";
#exit;


open(INPUT, "$path/$filename"); # || print "$! $filename\n";
@contents = <INPUT>;
close(INPUT);

$first_line = @contents[0];
$language = "dbn";
if ($first_line =~ /^\#/) {  # python
    $language = "python";
} elsif ($first_line =~ /^\;/) {  # scheme
    $language = "scheme";
}

if ($format eq "html") {
    print "Content-type: text/html\n\n";

    print "<PRE>";
    
    if ($filename ne "") {
	foreach $line (@contents) {
	    chomp $line;
	    $line =~ s/\</&lt;/g; # less than
	    $line =~ s/\>/&gt;/g; # greater than
	    if ($line =~ /http\:\/\// ) {
		$line =~ s/(http\:\/\/\S*)/<a href=\"$1\">$1<\/a>/g;
	    } elsif ($line =~ /www\./) {
		# the smartass didn't include http
		$line =~ s/(www\.\S*)/<a href=\"http\:\/\/$1\">$1<\/a>/g;
	    }
	    if ($language eq "dbn") {
		if ($line =~ /^\/\//) { # if line contains a comment
		    $line =~ s/(\/\/.*)$/<font color=\"\#999999\">$1<\/font>/;
		}
	    } 
	    print "$line\r\n";
	}
	#print @contents;
    }
    print "</PRE>";

} else {
    print "Content-type: text/plain\n\n";

    if ($filename ne "") {
	open(INPUT, "$parsed_filename"); #|| print "$! $filename\n";
	@contents = <INPUT>;
	close(INPUT);
	print @contents;
    }
}
