#BACKUPDIR=$HOME/marmoset/researchDBs
BACKUPDIR=backups

DATESTRING=`date "+%F-%H-%M-%S"`

FILE=$BACKUPDIR/consent_forms.$DATESTRING.sql

if [ ! -d $TEMP ]; then
    mkdir -p $TEMP
fi

mysqldump --single-transaction --databases consent_forms > $FILE

gzip $FILE