#!/usr/bin/python

import MySQLdb
import sys
import getopt
import optparse
import os

USAGE="""Usage: %s
    [ -h ] print this message
    [ -u <user> / --user=<user> ] set db user
    [ -d <database> / --database=<database> ] name of database
    [ -p <password> / --password=<password> ] password
    [ -s <socket> / --socket=<socket> ] socket
    """ % sys.argv[0]

def thousands(s):
    i=int(s)
    result = ""
    while i > 1000:
	result = ",%d" % (i%1000) + result
	i /= 1000
    result = "%d" % i + result
    return result

def usage():
    print USAGE
    sys.exit(1)

def getConnection(args):
    # Mostly useful defaults
    socket="/export/projects/marmoset/research/submitserver.sock"
    database="submitserver"
    user="root"
    password=""

    # default properties
    dbprops={}

    # Now try the $HOME/.my.cnf file, unless --no-defaults is set
    noDefaults=False
    for arg in sys.argv:
        if arg=="--no-defaults":
            noDefaults=True
    if not noDefaults:
        try:
            f=open("%s/.my.cnf" % os.environ["HOME"],'r')
            for line in f:
                line=line.rstrip()
                if len(line)==0 or line[0]=="[":
                    continue
                (o,a)=line.rstrip().split("=")
                if o=="socket":
                    socket=a
                elif o=="database":
                    database=a
                elif o=="user":
                    user=a
                elif o=="password":
                    password=a
        except IOError:
            print "No .my.cnf file"

    # Now try command-line params
    try:
        opts, args = getopt.getopt(args[1:], "vhs:d:u:p:", ["verbose", "socket=","database=","user=","password="])
    except getopt.GetoptError:
        # print help information and exit:
        usage()

    for o, a in opts:
        if o == "-h":
            print o
            usage()
        if o in ("-s", "--socket"):
            socket=a
        if o in ("-d", "--database"):
            database=a
        if o in ("-p", "--password"):
            password=a
        if o in ("-u", "--user"):
            user=a
        if o in ("-v", "--verbose"):
            verbose=True

    # Now return the darn connection!
    return (args, MySQLdb.connect(unix_socket=socket, db=database, user=user, passwd=password))
