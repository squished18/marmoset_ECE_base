BACKUPDIR=$HOME/submitServerDatabaseBackup
DATESTRING=`date "+%F-%H-%M-%S"`

FILE=$BACKUPDIR/backup.$DATESTRING.sql

if [ ! -d $TEMP ]; then
    mkdir -p $TEMP
fi

mysqldump --single-transaction --databases submitserver > $FILE

gzip $FILE