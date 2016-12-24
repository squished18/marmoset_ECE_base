#! /bin/sh

# Use the BuildServer to run FindBugs on a number of submissions.

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

if [ "$#" -ne 2 ]; then
	echo "Usage: testSubmission.sh <config file> <submission list>"
	exit 1
fi
config_file=$1
submission_list=$2

# Construct BuildServer runtime classpath.
# This should be kept in sync with the runBuildServer2 script
# that runs the BuildServer in daemon mode.
classpath="$BUILDSERVER_ROOT/bin"
for j in modelClasses findbugs commons-httpclient commons-logging junit bcel log4j dom4j-full; do
	classpath="$classpath:$BUILDSERVER_ROOT/lib/$j.jar"
done

echo "classpath is $classpath"

java -classpath "$classpath" \
	-Xmx64m \
	$props \
	edu.umd.cs.buildServer.FindBugsBuildServer \
	"$config_file" \
	"$submission_list"
