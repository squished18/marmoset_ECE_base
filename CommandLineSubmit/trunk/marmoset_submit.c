#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <pwd.h>
#include <time.h>
#include <string.h>
#include <libgen.h>

const char * JAVA_PATH   = "/u/cs_build/jdk-linux/bin/java";
const char * JAVA_OPTION = "-jar";
const char * JAR_FILE    = "/u/cs_build/bin/marmoset_submit.jar";

// Get invoker's user name
void getRealUserName( char ** user ) {
    uid_t uid = getuid();
    struct passwd *pw  = getpwuid( uid );

    if ( pw ) {
	*user = pw->pw_name;
    } else {
	perror( "Unable to determine user" );
	exit( -1 );
    }
}

// Returns 0 if string is not of the format 'csNNN'
char isCourseAccount( char * user ) {
    if ( strlen( user ) != 5 ) return 0;
    if ( user[0] != 'c' || user[1] != 's' ) return 0;
    return ( isdigit( user[2] ) && isdigit(user[3] ) && 
	    isdigit( user[4] ) );
}

int main( int argc, char *argv[] ) {
    char * acct;	// user that runs this program
    char * userName;	// for whom to submit the asst
    char * course;	// the course to submit the asst to

    getRealUserName( &acct );
    if ( isCourseAccount( acct ) ) {  // user is 'csNNN'
	if ( argc != 4 ) {
	    printf( "usage: %s userid project file\n", basename( argv[0] ) );
	    exit( -2 );
	}
	userName = argv[1];
	course   = acct;
    } else {
	if ( argc != 4 ) {
	    printf( "Usage: %s course project file\n", basename( argv[0] ) );
	    exit( -2 );
	}
	userName = acct;
	course   = argv[1];
	if ( ! isCourseAccount( course ) )
	    printf( "%s: \"%s\" does not seem to be a valid course name.\n",
		    basename( argv[0] ), argv[1] );
    }

    // Get month and year of local time
    time_t curtime = time( NULL );
    struct tm * loctime = localtime( &curtime );
    if ( loctime == NULL ) {
	printf( "Unable to determine time\n" );
	exit( -3 );
    }
    int year  = loctime->tm_year + 1900;
    int month = loctime->tm_mon + 1;

    char yearstr[5];
    sprintf( yearstr, "%d", year );

    char * term;
    if ( month < 1 || month > 12 ) {
	// This should never happen
	exit( -3 );
    } else if ( month <= 4 )
	term = "Winter";
    else if ( month <= 8 )
	term = "Spring";
    else
	term = "Fall";

    // Parameters passed to jar file:
    // course term year userid project file
    // e.g. cs136 Winter 2011 jdoe a0p1 a0.cc
    char * javaArgList[] = {
	(char*)JAVA_PATH, (char*)JAVA_OPTION, (char*)JAR_FILE,
	course, term, yearstr,
	userName,
	argv[2], // project
	argv[3], // file
	(char*)NULL
    };

    execv( JAVA_PATH, javaArgList );
    perror( "Unable to execv" );
    exit( -4 );
}
