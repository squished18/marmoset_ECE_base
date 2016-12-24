#!/usr/bin/env python

import sys

print "use consent_forms;"
for unique_id, course_pk in [line.split("\t") for line in open(sys.argv[1])]:
    if unique_id=="unique_id":
        continue
    print """
    update consent_by_semester set usable_consent = 'yes'
    where unique_id = %s
    and course_pk = %s;""" % (unique_id, course_pk)
