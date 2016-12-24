#! /bin/sh

# Test a submission and project jarfile from the command line.
# This script assumes that a directory can be created
# in /tmp (and so probably won't work on Windows).

#BUILDSERVER_ROOT=$HOME/workspace/BuildServer
echo "BUILDSERVER_ROOT is $BUILDSERVER_ROOT"

# Make sure BUILDSERVER_ROOT is set
if [ -z "$BUILDSERVER_ROOT" ]; then
	echo "Environment variable BUILDSERVER_ROOT is not set"
	echo "Please set it to the directory where the BuildServer is checked out"
	exit 1
fi

props=""

while [ "$#" -gt 0 ] && [ `expr "$1" : "-"` -gt 0 ]; do
	opt="$1"
	shift
	
	case $opt in
		-debugSecurity)
			props="-Ddebug.security=true $props"
			;;

		-D*)
			props="$opt $props"
			;;
		
		*)
			echo "Unknown option: $opt"
			exit 1
			;;
	esac
done

# Get submission zipfile and project jarfile
if [ "$#" -ne 2 ]; then
	echo "Usage: testSubmission.sh <submission zipfile> <test setup jarfile>"
	exit 1
fi
submission_zipfile=$1
test_jarfile=$2

# Create a temporary directory to use for the build and testfiles directories
workdir=BuildServer$$
if [ -e "$workdir" ]; then
	echo "Could not run because scratch directory $workdir already exists"
	exit 1
fi
echo Workdir:
echo $workdir
echo

# Construct BuildServer runtime classpath.
# This should be kept in sync with the runBuildServer2 script
# that runs the BuildServer in daemon mode.
classpath="$BUILDSERVER_ROOT/bin"
for j in modelClasses findbugs clover commons-httpclient commons-logging commons-io junit bcel log4j dom4j jaxen; do
	classpath="$classpath:$BUILDSERVER_ROOT/lib/$j.jar"
done

echo "classpath is $classpath"

mkdir $workdir || (echo "Could not create working directory $workdir"; exit 1)

java -classpath "$classpath" \
	$props \
	edu.umd.cs.buildServer.BuildServerTestHarness \
	"$submission_zipfile" \
	"$test_jarfile" \
	"$workdir"

