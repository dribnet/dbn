#!/usr/bin/perl

require 'globals.pl';
package courseware;

$admin = shift(@ARGV);
open(ADMIN, "$admin") || die $!;
@contents = <ADMIN>;
close(ADMIN);

print `mv $admin $admin.old`;
open(NADMIN, ">$admin") || die $!;

print NADMIN shift(@contents);

$a = shift(@contents); chop $a;
$a = &coder($a);
print NADMIN "$a\n";

$a = shift(@contents); chop $a;
$a = &coder($a);
print NADMIN "$a\n";

print NADMIN shift(@contents);
print NADMIN shift(@contents);

close NADMIN;



# For each letter of the password (item) convert it via the 'let_to_noise' hash and push it to another 
# variable, then replace the existing $item with the new 'noised' password
sub coder {
    my $password = @_[0];
    @uncode = ();
    @chars = split //, $password;
    for(@chars) {
	push(@uncode, $let_to_noise{$_});
    }
    $item = join '', @uncode;
    $password = $item;
    return $password;
}
