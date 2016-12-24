Assuming that you have the runsql script set up to access the correct
mysql installation and database, you should be able to do this:

runsql getnoninstructors.sql > noninstructors.txt
./merge.py > rename.sql
runsql rename.sql