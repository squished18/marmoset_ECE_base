#!/usr/bin/env python

import sys
import re
import spacdb

sys.argv, conn = spacdb.getConnection(sys.argv)
cursor=conn.cursor()

if len(sys.argv) < 2:
    print "Usage: create-upload-file.py [ db_options ] <course> <semester>"
    sys.exit(1)

sql="""
select lastname, firstname, master_table.unique_id, cvs_account, master_table.directory_id
from consent_by_semester, courses, master_table
where usable_consent = 'yes'
and consent_by_semester.unique_id = master_table.unique_id
and consent_by_semester.course_pk = courses.course_pk
and coursename like '%s'
and semester like '%s'
""" % (sys.argv[0], sys.argv[1])

cursor.execute(sql)
for row in cursor.fetchall():
    print "%s,%s,%s,0101,%s,%s" % row

conn.close()
