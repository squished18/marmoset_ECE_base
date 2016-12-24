#!/bin/bash

# checkDaemon.sh --- Restart the BuildServer if it is not running

# Requires:
#  - that the "runBuildServer2.pl" script is on the PATH
#  - that the configuration properties file is in the current
#    directory

force=no
if [ "$1" = "-force" ]; then
	force=yes
	shift
fi

# The existence of the "do_not_run" file means that we
# should not try to start the daemon.
if [ "$force" == "no" ] && [ -r "do_not_run" ]; then
    echo "do_not_run file exists"
	exit 0
fi

propsfile=config.properties
if [ ! -z "$1" ]; then
	propsfile="$1"
fi

osname=`uname -s`
if [ "$osname" != "Linux" ] && [ "$osname" != "SunOS" ]; then
	echo "This script only runs on Linux or Solaris"
	exit 1
fi

if [ -r "buildserver.pid" ]; then
	pid=`cat buildserver.pid`
	if [ -r "/proc/$pid" ]; then
		# Daemon is still running. Cool.
		exit 0
	fi
fi

# Try to respawn.
# But don't actually wait to find out whether it succeeded.
./runBuildServer $propsfile > /dev/null 2>&1 &
exit 0
