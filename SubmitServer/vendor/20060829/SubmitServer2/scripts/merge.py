#!/usr/bin/python

academy = open('academy.txt', 'r')
actorList = []
for actor in academy:
    actor = actor.rstrip()
    actorList.append(actor)

people = open('noninstructors.txt', 'r')

dict = {}
account = 1
for student in people:
    student = student.rstrip()
    (cvsAccount, studentPK, studentRegistrationPK, employeeNum) = student.split()

    actor = actorList[account]
    (firstname, lastname) = actor.split("\t")
    print "--%s\t%s\t%s\t%s\tcmsc132%03d" % (firstname, lastname, cvsAccount, employeeNum, account)
    account += 1
    
    print " UPDATE students SET firstname = '%s', lastname = '%s', campus_uid = 'qwerty' WHERE student_pk = %s; " % (firstname, lastname, studentPK)
    print " UPDATE student_registration SET firstname = '%s', lastname = '%s', cvs_account = 'cmsc132%03d' WHERE student_registration_pk = %s; " % \
          (firstname, lastname, account, studentRegistrationPK)
#    print ""
