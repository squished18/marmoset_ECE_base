#!/usr/bin/perl

use strict qw(refs vars);
use FileHandle;
use Time::Local;
use POSIX;
use Getopt::Std;

#
# main
#
my %opts = ();
getopts("dh", \%opts);
my $DEBUG = $opts{d};
Usage() if $opts{h};

if (scalar @ARGV != 5) {
    Usage();
}

my ($cvsRoot, $module, $cvsAccount, $projectNumber, $outputDir) = @ARGV;

sub Usage() {
    print qq{$0: <CVSROOT> <module> <cvsAccount> <projectNumber> <outputDir>
-d (debug)
-h (print this help message)
NOTE: This script extracts all times in GMT time.
};
    exit -1;
}

my $reposDir = "$cvsRoot";
Debug("cvsRoot: " . $cvsRoot);
Debug("module: " . $module);
Debug("cvsAccount: " . $cvsAccount);
Debug("projectNumber: " . $projectNumber);
Debug("reposDir: " . $reposDir);

#
# Projects for CMSC132 from Fall2004 are actually rooted at: $CVSROOT/cs132XXX/projects
# Thus the modulePrefix command-line flag
#
#     my $module = "$projectNumber";
#     if ($modulePrefix) {
# 	$module = "$modulePrefix/$projectNumber";
#     }

#     if (! -d "$reposDir") {
# 	print STDERR "$reposDir does not appear to be a valid directory\n";
# 	exit 1;
#     }

#     if (! -d "$reposDir/$module") {
# 	print STDERR "$module does not appear to be a valid directory in repository $reposDir\n";
# 	exit 1;
#     }

my %timestampSet = ();

my @fileList = ();

#print "cd '$reposDir/$module' && find . -name '*,v' -print\n";

#
# parse the RCS log files
#
my $rcsFiles_fh = new FileHandle("cd '$reposDir/$module' && find . -name '*,v' -print|");
while (<$rcsFiles_fh>)
{
    chop;
    s,\./,,;
    #print "$_\n";

    my $rcsFile = $_;

    $rcsFile =~ /^(.*),v$/;
    my $plainFile = $1;

    #print "$plainFile\n";
    push @fileList, $plainFile;
}
$rcsFiles_fh->close();

my %tagToDate = ();

foreach my $plainFile (@fileList)
{
    next if ($plainFile =~ /(\.gif)|(\.jpg)/);
    #print "cvs -d$reposDir rlog $module/$plainFile\n";
    my $rlog_fh = new FileHandle("cvs -d$reposDir rlog $module/$plainFile|");
    
    my $rev;
    my %revToTag = ();
    my %revToDate = ();
    my %localTagToDate = ();

    my $state = 'tags';
    while (<$rlog_fh>) {

	if ($state eq 'tags')
	{
	    # First we need to find the tags
	    # and what versions they are mapped to
	    if (/\s+(t\d+):\s+(\d+\.\d+)/)
	    {
		#print "$2 -> $1\n";
		$revToTag{$2} = $1;
	    }
	    if (/keyword substitution:/)
	    {
		$state = 'revision';
	    }
	}
	elsif ($state eq 'revision') {
	    # Once we see the revision marker 
	    # then we start looking for the date
	    if (/^revision\s+(\d+)\.(\d+)\s*$/) {
		$rev = "$1.$2";
		$state = 'wantdate';
		#print "got revision: $1.$2\n";
	    }
	} elsif ($state eq 'wantdate') {
	    if (/^date:\s+((\d\d\d\d[-\/]\d\d[-\/]\d\d\s+\d\d:\d\d:\d\d)(\s+((\+|-)?\d\d\d\d))?)/)
	    {
		my $datestr = $1;
		$timestampSet{$datestr} = 1;

		# only replace the date this revision maps to 
		# if this date is the latest
		$revToDate{$rev} = $datestr;
		my $tag = $revToTag{$rev};

		if ($tag)
		{
		    $localTagToDate{$tag} = $datestr;
		    my $latestDate = $tagToDate{$tag};
		    # lookup the latest date so far, replace if necessary
		    if (not $latestDate or ($datestr gt $latestDate))
		    {
			$tagToDate{$tag} = $datestr;
		    }
		}
	    }
	    else {
		"Your version of CVS is returning dates in a format that doesn't match my RE: /^date:\s+((\d\d\d\d[-\/]\d\d[-\/]\d\d\s+\d\d:\d\d:\d\d)(\s+((\+|-)\?\d\d\d\d))\?)/\n";
	    }
	    $state = 'revision';
	}
    }
    $rlog_fh->close();
}

my %dateToTag = ();
foreach my $tag (sort keys %tagToDate)
{
    #print "$tag -> $tagToDate{$tag}\n";
    $dateToTag{$tagToDate{$tag}} = $tag;
}

#
# Now create a map from date/time strings to unix timestamps.
# Just stick with GMT rather than convert to localtime.
#
my @timestampList = ();
foreach my $date (sort keys %timestampSet) {
    #print "$ts\n";

    my $timestamp = Timestamp2UTC($date);

    # tack "000" to the end of the UTC time to go from seconds to millis
    push @timestampList, [$date, "${timestamp}000"];
}

my $outfileDir = "$outputDir/$cvsAccount/$projectNumber";
`mkdir -p $outfileDir` if (! -d "$outfileDir");

# Now export all timestamped versions and build zipfiles from them.
foreach my $pair (@timestampList) {
    my $date = $pair->[0];
    my $timestamp =$pair->[1];

    my $tag = $dateToTag{$date};
    my $zipBaseName = "$projectNumber.$cvsAccount";

    my $outfile = "$outfileDir/$zipBaseName.${timestamp}.$tag.zip";

    Spawn("rm -rf $zipBaseName");
    # NOTE: We need the GMT in the date because we extracted GMT dates
    # from the CVS history files
    Spawn("cvs -d$reposDir export  -d $zipBaseName -D '$date GMT' $module");
    Spawn("cd $zipBaseName && zip -9r $outfile .");
    Spawn("rm -rf $zipBaseName");
}

#
# Functions
#
sub FullPath
{
    my $inode = @_;
    if (-d $inode)
    {
	return `cd $inode && pwd`;
    }
    elsif (-f $inode)
    {
	my $pwd = `pwd`;
	$pwd =~ s/\n+//;
	return "$pwd/" . $inode;
    }
    else
    {
	die "$inode is neither a file nor a directory\n";
    }
}

sub Spawn
{
    my ($cmd) = @_;
    print "Command: $cmd\n";
    system($cmd)/256 == 0
	|| die "Couldn't spawn command $cmd: $!\n";
}

# does not convert from GMT
sub Timestamp2UTC($)
{
    my $tsWithoutZoneOffset = shift @_;
    $tsWithoutZoneOffset =~ /^(\d\d\d\d)[-\/](\d\d)[-\/](\d\d)\s+(\d\d):(\d\d):(\d\d)$/;
    my $year = $1 - 1900;
    my $month = $2 - 1;
    my $day = $3;
    my $hour = $4;
    my $minute = $5;
    my $second = $6;

    #print ("$year/$month/$day $hour:$minute:$second month: $month\n");

#    return mktime($second,$minute,$hour,$day,$month,$year);

    # Convert to seconds since epoch, UTC, GMT
    # The information I've been passed in this function is already in GMT
    # I'm going to use the 'timelocal' function to avoid any type of
    # conversions for daylights savings or anything else.
    # I just want to treat this time as itself and do nothing more with it!
    return timelocal($second,$minute,$hour,$day,$month,$year);
}

sub Debug($) {
    print "$_[0]\n" if ($DEBUG);
}
