#!/perl/bin/perl

# needs to make a temporary directory, compile into that
# clear out contents of temporary directory at begin of compile
# create temp directory if it doesn't exist

# should take arguments for the compiler:
# jikes -d classes *.java
# instead looks like
# buzz "jikes -d classes" *.java
# maybe everything always goes in /tmp? 
# (no, don't want to leave code around)


$command = shift(@ARGV);
$temp = "buzztemp";
$newline = "\n";

if (!(-d $temp)) {
    mkdir($temp, 0777) || die $!;
}

foreach $arg (@ARGV) {
    if ($arg =~ /^-d(.*)/) {
	$params{$1} = 1;
    #} elsif ($arg =~/^-c(.*)/) {
	#$compiler = $1;
    } elsif ($arg =~ /\.java$/) {
	unshift @file_list, $arg;	
    }
}

# support: define, ifdef, ifndef, else, endif
# no support: defined(x), elif, #define blah 12, nesting

foreach $file (@file_list) {
    open(FILE, "$file") || die "error with $file, $!";
    @contents = <FILE>;
    close(FILE);

    my (@new_contents);
    #$changes = 0;
    $changes = 1;  # always set changes to 1, copies files into temp

    #foreach $line (@contents) {
    while ($line = shift(@contents)) {
	# determine if it contains preprocessing 
	if ($line =~ /\#define\s+(\S+)/) {
	    $changes = 1;
	    #print "setting \"$1\"\n";
	    $params{$1} = 1;
	    unshift(@new_contents, $newline); # maintain lf count
	} elsif ($line =~ /\#if(\w*)def\s+(\S+)/) {
	    $changes = 1;
	    if ((($1 eq "") && ($params{$2} == 1)) ||
		(($1 eq "n") && ($params{$2} != 1))) {
		# run until endif
		for (;;) {
		    $line = shift(@contents);
		    last if ($line =~ /\#endif/);
		    #print "ok: $line";
		    unshift(@new_contents, $line);
		}
		unshift(@new_contents, $newline);

	    } else {
		#print "\"$1\" \"$2\" $params{$2}\n";
		# search for occurence of #endif
		while (!($line =~ /\#endif/)) {
		    $line = shift(@contents);
		    #print "  : $line";
		    unshift(@new_contents, $newline);
		}
	    }
	} else {
	    #print "ok: $line";
	    unshift(@new_contents, $line);
	}
    }
    if ($changes == 1) {
	#rename($file, "$file.pre") || print $!;
	#open(OUTPUT, ">$file.out") || die $!;
	print "creating $temp/$file\n";
	open(OUTPUT, ">$temp/$file") || die $!;
	print OUTPUT reverse(@new_contents);
	close(OUTPUT);
	unshift(@new_file_list, "$temp/$file");
    }
}

$files = join(' ', @new_file_list);
print `$command $files`;

# clean up
unlink $files;
rmdir $temp;


# convert files out of file_list:
# open each file
#   if it contains preprocessing comments
#     rename the .java file to .pre
#     make a new java file based on the substitutions

# compile
# call javac, jikes or sj based on what's in use
# auto-detect if fast enough

# unconvert (rename) files
# delete the .java files from file_list
# rename each .pre file to .java from file_list
