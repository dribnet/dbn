foreach $arg (@ARGV) {
    if ($arg =~ /^-D(.*)/) {
	$params{$1} = "1";
    } else if ($arg =~ /\.java/) {
	unshift @file_list, $arg;
	
    }
}

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
