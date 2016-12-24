ALTER TABLE `projects` ADD `best_submission_policy` VARCHAR( 100 ) CHARACTER SET utf8 COLLATE utf8_general_ci,
ADD `release_policy` ENUM( 'after_public', 'anytime' ) DEFAULT 'after_public' NOT NULL ,
ADD `stack_trace_policy` ENUM( 'test_name_only', 'restricted_exception_location', 'exception_location', 'full_stack_trace' ) DEFAULT 'test_name_only' NOT NULL ,
ADD `num_release_tests_revealed` SMALLINT DEFAULT '2' NOT NULL ;