#!/usr/bin/python

import sys
import spacdb

#
# XXX cvstag_timestamps are *NOT* unique!
# They aren't unique between students, and aren't unique between classes, either
# There could be cvstag_timestamps from different courses that are the SAME!
# Two students in the same course might have the same cvstag_timestamp for two submissions
#
# To fix:  Have to match up at least the cvs_account to prevent mismatches in the same semester,
# and will have to figure out a way to match courses to prevent mismatches between semesters
#

#
# TODO: spacdb.getConnection() should get what it needs to make
# a connection, then check for the parameters needed by this program
# then create the connection
#
sys.argv, conn = spacdb.getConnection(sys.argv)

if len(sys.argv) < 1:
    print "Usage: release-request.py <semester>"
    conn.close()
    sys.exit(1)
semester = sys.argv[0]

cursor=conn.cursor()

# SQL to get all the courses
sql = """
select release_request, cvstag_timestamp
from submissions
where release_request is not null
and submit_client != 'web'
and submit_client not like '%Command%'
"""

cursor.execute(sql)

result = cursor.fetchall()
print "use %s;" % semester
for row in result:
    release_request = row[0]
    cvstag_timestamp = row[1]
    print """
    update submissions
    set release_request = '%s'
    where cvstag_timestamp = '%s';
    """ % (release_request, cvstag_timestamp)

conn.close()
