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

$blank_line = "\n";

$temp_dir = "buzztemp";
if (-d $temp_dir) {
    `rm -rf $temp_dir`;
}
mkdir($temp_dir, 0777) || die $!;

if ($ENV{'WINDIR'} ne '') {
    $separator = "\\";
    $windows = 1;
} else {
    $separator = '/';
    $unix = 1;
}

$command = shift(@ARGV);

if ($command =~ /-classpath/) {
    die "cannot set classpath using this version of buzz";
}
$classpath = $ENV{"CLASSPATH"};
if ($classpath eq "") {
    # find java in the path
    if ($windows) {
	@elements = split(';', $ENV{"PATH"});
	foreach $element (@elements) {
	    print "trying $element\\java.exe\n";
	    if (-f "$element\\java.exe") {
		$classpath = "$element\\..\\lib\\classes.zip";
	    }
	}
    } else {
	die "code for searching path not written for unix";
    }
}


# if target directory, -d, option is used, add it to CLASSPATH
if ($command =~ /\-d\s(\S*)/) {
    if ($windows) {
	$classpath = "$1;$classpath";
    } else {
	$classpath = "$1:$classpath";
    }
}

foreach $arg (@ARGV) {
    if ($arg =~ /^-d(.*)/) {
	$params{$1} = 1;
    #} elsif ($arg =~/^-c(.*)/) {
	#$compiler = $1;
    } elsif ($arg =~ /\.java$/) {
	if ($arg =~ /(.*)\*\.java$/) {
	    # gotta expand * to all matching
	    #print "expanding *.java from \"$1\"\n";
	    $dir = $1;
	    if ($dir eq "") {
		$dir = '.';
	    } else {
		print "creating dir $temp_dir$separator$dir\n";
		mkdir("$temp_dir$separator$dir", 0777) || die $!;
	    }
	    opendir(DIR, $dir) || die $!;
	    @dcontents = readdir(DIR);
	    closedir(DIR);
	    foreach $file (@dcontents) {
		if ($file =~ /\.java$/) {
		    if ($dir eq '.') {
			$fullname = "$file";
		    } else {
			$fullname = "$dir$file";
		    }
		    #print "adding $fullname\n";
		    unshift @file_list, "$fullname";
		}
	    }
	    
	} else {
	    unshift @file_list, $arg;	
	}
    }
}

# support: define, ifdef, ifndef, else, endif
# no support: defined(x), elif, #define blah 12, nesting

print "processing...\n";
foreach $file (@file_list) {
    #print "$file\n";
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
	    #found #define
	    $changes = 1;
	    #print "setting \"$1\"\n";
	    $params{$1} = 1;
	    unshift(@new_contents, $blank_line); # maintain lf count
	} elsif ($line =~ /\#if(\w*)def\s+(\S+)/) {
	    unshift(@new_contents, $blank_line);
	    $changes = 1;
	    if ((($1 eq "") && ($params{$2} == 1)) ||   #ifdef found
		(($1 eq "n") && ($params{$2} != 1))) {  #ifndef found
		# include until endif/else
		for (;;) {
		    $line = shift(@contents);
		    last if ($line =~ /\#endif/);
		    last if ($line =~ /\#else/);
		    unshift(@new_contents, $line);
		}
		unshift(@new_contents, $blank_line);
		# if #else found, exclude until endif
		if ($line =~ /\#else/) {
		    for (;;) {
			$line = shift(@contents);
			last if ($line =~ /\#endif/);
			unshift(@new_contents, $blank_line);
		    }
		    unshift(@new_contents, $blank_line);
		}

	    } else {
		# exclude until endif/else
		for (;;) {
		    $line = shift(@contents);
		    last if ($line =~ /\#endif/);
		    last if ($line =~ /\#else/);
		    unshift(@new_contents, $blank_line);
		}
		unshift(@new_contents, $blank_line);
		# if #else found, include until endif
		if ($line =~ /\#else/) { # now write everything
		    for (;;) {
			$line = shift(@contents);
			last if ($line =~ /\#endif/);
			unshift(@new_contents, $line);
		    }
		    unshift(@new_contents, $blank_line);
		}
	    }
	} else {
	    unshift(@new_contents, $line);  # no change
	}
    }
    if ($changes == 1) {
	open(OUTPUT, ">$temp_dir$separator$file") || die $!;
	print OUTPUT reverse(@new_contents);
	close(OUTPUT);
	unshift(@new_file_list, "$temp_dir$separator$file");
    }
}

print "compiling...\n";
$files = join(' ', @new_file_list);
$compile_command = "$command -classpath $classpath $files";
#print "$compile_command\n";
print `$compile_command`;

# clean up
print "cleaning...\n";
#unlink $files;
#rmdir $temp_dir;
`rm -rf $temp_dir`;

# finished
print "done.\n";

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
