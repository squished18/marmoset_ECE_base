This directory contains sql scripts that migrate the DB as new columns and tables
are added to support continuing work on the SubmitServer.

These should *NOT* need to be applied if you always use as your schema the most
recent production database.

auxiliary-test-outcomes.sql:	Creates extra tables that are keyed to the test-outcomes
	table and track whether test outcomes only cover failing code (Java-only).  This information
	is for research purposes only; there is no code in the server itself that uses this info.

project-patch.sql:				Adds the best_submission_policy, release_policy, 
	stack_trace_policy, and num_release_tests_revealed to the projects table.

project-starter-files-archive.sql:	Creates the project-starter-files table and adds an 
	archive_pk to the projects table.

spring2006_survey.sql:			Adds tables for a survey conducted at the end of Spring2006.
	Should be expanded or changed so that we can take this survey every semester.

test-outcomes-patch.sql:		Adds news columns, coarsest_coverage_level and 
	exception_source_covered_elsewhere, to the test-outcomes table.
	
submissions.sql:			Adds a previous_submission_pk column to the submissions table.

addpasswordcolumn.sql:		Adds the password column to the students table in the database.