#!/usr/bin/env python

#
# Creates an sql script that will create/add to the master_table.
# The master_table is the one place in the research infrastructure
# where we maintain information about who each student really is.
# This is important because we need to be able to perform longitudinal
# studies of students over semesters, which requires having one place where
# we have their name, employee_num and campus_uid mapped to the unique_id
# that is used everyplace else.
#
# The master_table looks like this:
#
# unique_id, employee_num, firstname, lastname, campus_uid
#

import MySQLdb
import sys
import spacdb

sys.argv, conn = spacdb.getConnection(sys.argv)

cursor=conn.cursor()

sql = """
SELECT employee_num, students.firstname, students.lastname, campus_uid
FROM students, student_registration
WHERE students.student_pk = student_registration.student_pk
AND (student_registration.instructor_capability IS NULL
OR student_registration.instructor_capability = 'read-only')
"""

# I can't figure out a good way to process command line args by two diff. programs
# The getopt package gets unhappy when it sees options it doesn't recognize
fall2004=False
if fall2004:
    sql="""
    SELECT employee_num, students.firstname, students.lastname, campus_uid
    FROM students, student_registration
    WHERE students.student_pk = student_registration.student_pk
    """

cursor.execute(sql)

result = cursor.fetchall()

print "use consent_forms;"
for row in result:
    employee_num = row[0]
    # Fix the single quotes in names like O'Shea or D'Brickashaw
    # D'Brickashaw is a sweet name, btw
    firstname = row[1].replace("'", "\\'")
    lastname = row[2].replace("'", "\\'")
    campus_uid = row[3]

    print """INSERT IGNORE INTO master_table
    VALUES (DEFAULT, '%s', '%s', '%s', '%s'); """ % (employee_num, firstname, lastname, campus_uid)

