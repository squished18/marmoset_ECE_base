#!/usr/bin/env python

import sys
import spacdb

sys.argv, conn = spacdb.getConnection(sys.argv)

cursor=conn.cursor()

sql = """
SELECT employee_num, cvs_account, given_consent, coursename, semester
FROM students, student_registration, courses
WHERE students.student_pk = student_registration.student_pk
AND student_registration.course_pk = courses.course_pk
"""

print "use consent_forms;"
cursor.execute(sql)
for row in cursor.fetchall():
    employee_num = row[0]
    cvs_account = row[1]
    given_consent = row[2]
    coursename = row[3].replace(' ', "%")
    semester = row[4].replace(' ', "%")

    print """INSERT IGNORE INTO consent_by_semester
    SELECT unique_id,'%s','%s', course_pk, NULL, '%s'
    FROM master_table, courses
    WHERE employee_num = '%s'
    AND courses.coursename LIKE '%s'
    AND courses.semester LIKE '%s'
    ;
    """ % (cvs_account, given_consent, given_consent, employee_num, coursename, semester)

