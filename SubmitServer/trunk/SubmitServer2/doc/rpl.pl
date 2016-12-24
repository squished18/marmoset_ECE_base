#!/usr/bin/perl

while (<>) {
    $_ =~ s/</&lt;/g;
    $_ =~ s/>/&gt;/g;
    print $_;
}
