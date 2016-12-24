This lists the different python scripts available for managing the
research data.



* spacdb.py:	Python library that needs to be imported by each other
python script for accessing the database.  Requires that you set the
shell variable PYTHON_HOME to this directory (consentFormManagement),
something like this:

export PYTHON_HOME=$HOME/consentFormManagement

Note that the current widely available version of python (2.2.3)
cannot connect to MySQL 4.1.X directly without adding a new user
account whose password is encrypted using OLD_PASSWORD.  This is
annoying, and the other drivers for Java don't seem to have a problem
with this.

* create-master-table.py:	Run against the desired semester's
database snapshot.  Produces SQL statements that will insert new rows into
the master-table.

* extract-by-semester.py:	Run against the desired semester's
database snapshot.  Produces SQL statements that will insert new rows
into the consent_by_semester table (will use joins using the
coursename and semester to figure out the course_pk).

Note that to run this for a new cousre for a new semester, you need to
add a new entry to consent_forms.courses.

* Now we need to do some fancy mapping around.  What we want to do is
  to propogate any given_conset='yes' forward to future semesters and
  back to previous semesters, except that when propogating forward, we
  stop when we hit a 'no', but when propogating backward, we can just
  blow up the 'no' responses and replace them with 'yes'.

  Currenlty we do this with a series of scripts called by a shell
  script named 'map-consent-responses.sh'.  This in turn calls
  'find.sql' and 'go.py'.  This could/should be re-written as a
  single python script but I haven't bothered yet.

* create-upload-file.py:   Creates a .csv file for upload into the
  research database.

* get-account-list-by-semester:	Gets the list of accounts that need to
  unpacked and submitted for each semester.