#! /bin/sh

if [ "$#" -ne 1 ]; then
	echo "Usage: dumpTestOutcomes.sh <test outcome file>"
	exit 1
fi

filename="$1"

# Make sure BUILDSERVER_ROOT is set
if [ -z "$BUILDSERVER_ROOT" ]; then
	echo "Environment variable BUILDSERVER_ROOT is not set"
	echo "Please set it to the directory where the BuildServer is checked out"
	exit 1
fi

classpath="$BUILDSERVER_ROOT/bin"
for j in modelClasses commons-httpclient commons-logging junit log4j dom4j; do
	classpath="$classpath:$BUILDSERVER_ROOT/lib/$j.jar"
done

java -classpath "$classpath" edu.umd.cs.buildServer.DumpTestOutcomes "$filename"
