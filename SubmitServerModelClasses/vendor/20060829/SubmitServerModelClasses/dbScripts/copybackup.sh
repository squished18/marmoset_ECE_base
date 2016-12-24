#!/bin/bash

#
# Copy most recent backup over to fromage where it will be taped
#

BACKUPDIR=/export/projects/submit/submitServerDatabaseBackup

file=`ls $BACKUPDIR | sort | tail -n 1`
# echo $file

scp $BACKUPDIR/$file jspacco@fromage:/export/data/jspacco/spring2005.backups > /dev/null