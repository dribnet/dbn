#!/usr/local/bin/perl -w
# rename - Larry Wall filename fixer

%let_num = ("A", "1",
		  "B", "2", 
		  "C", "3",
		  "D", "4", 
		  "E", "5",
		  "F", "6",
		  "G", "7",
		  "H", "8",
		  "I", "9",
		  "J", "10",
		  "K", "11",
		  "L", "12",
		  "M", "13",
		  "N", "14",
		  "O", "15"); 
		  
%num_let = ("01", "A",
			"02", "B", 
			"03", "C",
			"04", "D", 
			"05", "E",
			"06", "F",
			"07", "G",
			"08", "H",
			"09", "I",
			"10", "J",
			"11", "K",
			"12", "L",
			"13", "M",
			"14", "N",
			"15", "O"); 


$op = shift or die "Usage: rename expr [files]\n";
chomp(@ARGV = <STDIN>) unless @ARGV;
for (@ARGV) {
    $was = $_;
    eval $op;
    die $@ if $@;
    rename($was, $_) unless $was eq $_;
}
