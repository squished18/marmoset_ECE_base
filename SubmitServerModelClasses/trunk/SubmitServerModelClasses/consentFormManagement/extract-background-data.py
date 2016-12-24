#!/usr/bin/env python

import sys
import spacdb

def convertNoneToNULL(list):
    """Converts a list of elements that we got back from a database call into a format
    suitable for insertion into a new database statement (i.e. by converting None to NULL
    and turning all the strings into SQL string by surrounding them with single quotes"""
    result = []
    for elt in list:
        if elt==None:
            result.append('NULL')
        else:
            result.append("'%s'" % str(elt).replace("'", "\\'"))
    return result

sys.argv, conn = spacdb.getConnection(sys.argv)

cursor=conn.cursor()

sql = """
SELECT  gender, ethnic_racial_association, AmericanIndian, Asian, Black,
        Caucasian, LatinoLatina, age, high_school_country, prior_programming_experience,
        other_institution, a_score, ab_score, umcp_placement_exam,
        umcp_placement_exam_result, major,
        employee_num
FROM students, background_data
WHERE students.student_pk = background_data.student_pk
"""

print "use consent_forms;"
cursor.execute(sql)
for row in cursor.fetchall():
    employee_num = row[0]
    
    print """INSERT IGNORE INTO background_data
    SELECT unique_id, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
    FROM master_table
    WHERE employee_num = %s
    ;
    """ % tuple(convertNoneToNULL(row))

cursor.close()
