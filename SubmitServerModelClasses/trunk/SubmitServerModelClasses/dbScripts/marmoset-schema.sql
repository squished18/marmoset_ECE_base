-- phpMyAdmin SQL Dump
-- version 2.6.1
-- http://www.phpmyadmin.net
-- 
-- Host: localhost:9306
-- Generation Time: Apr 26, 2006 at 01:46 PM
-- Server version: 4.1.9
-- PHP Version: 4.3.9
-- 
-- Database: `submitserver`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `auxiliary_test_outcomes`
-- 

CREATE TABLE `auxiliary_test_outcomes` (
  `test_run_pk` int(8) NOT NULL default '0',
  `test_type` enum('public','release','secret','student') NOT NULL default 'public',
  `test_number` smallint(5) NOT NULL default '0',
  `outcome` enum('passed','failed','could_not_run','error','not_implemented','huh') NOT NULL default 'passed',
  `failing_only_coarsest_coverage_level` enum('method','statement','branch','none') NOT NULL default 'none',
  PRIMARY KEY  (`test_run_pk`,`test_type`,`test_number`,`outcome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='test_outcomes that cover code that is not covered by any pas';

-- --------------------------------------------------------

-- 
-- Table structure for table `background_data`
-- 

CREATE TABLE `background_data` (
  `student_pk` int(20) unsigned NOT NULL default '0',
  `gender` enum('female','male','na') default NULL,
  `ethnic_racial_association` enum('yes','na') default NULL,
  `AmericanIndian` enum('American Indian') default NULL,
  `Asian` enum('Asian') default NULL,
  `Black` enum('Black') default NULL,
  `Caucasian` enum('Caucasian') default NULL,
  `LatinoLatina` enum('Latino/Latina') default NULL,
  `age` enum('18-22','23-29','30+','na') default NULL,
  `high_school_country` varchar(80) default NULL,
  `prior_programming_experience` enum('none','Community College','Other UM System Institution','Other non-UM System Institution','High School AP Course','Other High School Course','na') default NULL,
  `other_institution` varchar(80) default NULL,
  `a_score` int(1) default NULL,
  `ab_score` int(1) default NULL,
  `umcp_placement_exam` enum('none','cmsc131','cmsc132','cmsc212','na') default NULL,
  `umcp_placement_exam_result` enum('passed','marginally passed','failed') default NULL,
  `major` enum('CS','CE','Math','Other','na') default NULL,
  PRIMARY KEY  (`student_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `background_retests`
-- 

CREATE TABLE `background_retests` (
  `submission_pk` mediumint(10) unsigned NOT NULL default '0',
  `project_jarfile_pk` mediumint(10) unsigned NOT NULL default '0',
  `num_successful_background_retests` mediumint(10) unsigned NOT NULL default '0',
  `num_failed_background_retests` mediumint(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`submission_pk`,`project_jarfile_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `code_metrics`
-- 

CREATE TABLE `code_metrics` (
  `test_run_pk` mediumint(6) unsigned NOT NULL default '0',
  `md5sum_sourcefiles` varchar(32) NOT NULL default '',
  `md5sum_classfiles` varchar(32) NOT NULL default '',
  `code_segment_size` mediumint(8) NOT NULL default '0',
  PRIMARY KEY  (`test_run_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `courses`
-- 

CREATE TABLE `courses` (
  `course_pk` int(20) unsigned NOT NULL auto_increment,
  `semester` varchar(15) NOT NULL default '',
  `coursename` varchar(20) default NULL,
  `section` varchar(2) default NULL,
  `description` text,
  `url` varchar(72) default NULL,
  PRIMARY KEY  (`course_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `eclipse_launch_events`
-- 

CREATE TABLE `eclipse_launch_events` (
  `eclipse_launch_event_pk` mediumint(10) unsigned NOT NULL auto_increment,
  `student_registration_pk` mediumint(10) unsigned NOT NULL default '0',
  `project_number` varchar(20) NOT NULL default '',
  `md5sum` varchar(32) NOT NULL default '',
  `event` varchar(20) NOT NULL default '',
  `timestamp` datetime NOT NULL default '1000-01-01 00:00:00',
  `skew` mediumint(9) NOT NULL default '0',
  PRIMARY KEY  (`eclipse_launch_event_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `features_deltas`
-- 

CREATE TABLE `features_deltas` (
  `submission_pk` mediumint(6) unsigned NOT NULL default '0',
  `test_type` enum('findbugs','pmd','method','opcode','digest1','digest2','class') NOT NULL default 'findbugs',
  `test_name` varchar(100) NOT NULL default '',
  `priority` varchar(10) NOT NULL default '',
  `num` smallint(4) unsigned NOT NULL default '0',
  `delta` smallint(4) NOT NULL default '0',
  PRIMARY KEY  (`submission_pk`,`test_type`,`test_name`,`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `project_jarfile_archives`
-- 

CREATE TABLE `project_jarfile_archives` (
  `archive_pk` int(20) NOT NULL auto_increment,
  `archive` mediumblob NOT NULL,
  PRIMARY KEY  (`archive_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `project_jarfiles`
-- 

CREATE TABLE `project_jarfiles` (
  `project_jarfile_pk` int(20) unsigned NOT NULL auto_increment,
  `project_pk` int(20) unsigned NOT NULL default '0',
  `jarfile_status` enum('new','pending','tested','active','inactive','failed','broken') NOT NULL default 'new',
  `version` smallint(5) unsigned NOT NULL default '0',
  `date_posted` datetime default NULL,
  `comment` mediumtext,
  `test_run_pk` int(20) unsigned default '0',
  `num_total_tests` smallint(3) NOT NULL default '0',
  `num_build_tests` smallint(3) NOT NULL default '0',
  `num_public_tests` smallint(3) NOT NULL default '0',
  `num_release_tests` smallint(3) NOT NULL default '0',
  `num_secret_tests` smallint(3) NOT NULL default '0',
  `archive_pk` int(20) unsigned default NULL,
  PRIMARY KEY  (`project_jarfile_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `projects`
-- 

CREATE TABLE `projects` (
  `project_pk` int(20) unsigned NOT NULL auto_increment,
  `course_pk` int(20) unsigned NOT NULL default '0',
  `project_jarfile_pk` int(20) unsigned NOT NULL default '0',
  `project_number` varchar(30) NOT NULL default '0',
  `ontime` datetime NOT NULL default '1000-01-01 00:00:00',
  `late` datetime NOT NULL default '1000-01-01 00:00:00',
  `title` varchar(100) default NULL,
  `URL` varchar(100) default NULL,
  `description` text,
  `release_tokens` smallint(3) NOT NULL default '0',
  `regeneration_time` int(3) NOT NULL default '0',
  `initial_build_status` enum('new','accepted') NOT NULL default 'new',
  `visible_to_students` enum('yes','no') NOT NULL default 'no',
  `post_deadline_outcome_visibility` enum('nothing','everything') NOT NULL default 'nothing',
  `kind_of_late_penalty` enum('constant','multiplier') NOT NULL default 'constant',
  `late_multiplier` decimal(3,2) default NULL,
  `late_constant` smallint(4) unsigned default NULL,
  `canonical_student_registration_pk` int(20) unsigned NOT NULL default '0',
  `best_submission_policy` varchar(100) default NULL,
  `release_policy` enum('after_public','anytime') NOT NULL default 'after_public',
  `stack_trace_policy` enum('test_name_only','restricted_exception_location','exception_location','full_stack_trace') NOT NULL default 'test_name_only',
  `num_release_tests_revealed` smallint(6) NOT NULL default '2',
  PRIMARY KEY  (`project_pk`),
  KEY `course_pk` (`course_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `student_registration`
-- 

CREATE TABLE `student_registration` (
  `student_registration_pk` int(10) unsigned NOT NULL auto_increment,
  `course_pk` int(5) unsigned NOT NULL default '0',
  `student_pk` int(5) unsigned NOT NULL default '0',
  `cvs_account` varchar(100) default NULL,
  `instructor_capability` enum('read-only','modify','canonical') default NULL,
  `firstname` varchar(50) default NULL,
  `lastname` varchar(50) default NULL,
  PRIMARY KEY  (`student_registration_pk`),
  KEY `student_pk` (`student_pk`),
  KEY `course_pk` (`course_pk`),
  KEY `cvs_account` (`cvs_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `student_submit_status`
-- 

CREATE TABLE `student_submit_status` (
  `project_pk` int(20) unsigned NOT NULL default '0',
  `student_registration_pk` int(20) unsigned NOT NULL default '0',
  `one_time_password` varchar(20) NOT NULL default '',
  `number_submissions` int(20) unsigned NOT NULL default '0',
  `number_commits` smallint(10) unsigned NOT NULL default '0',
  `extension` smallint(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (`project_pk`,`student_registration_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `students`
-- 

CREATE TABLE `students` (
  `student_pk` int(20) unsigned NOT NULL auto_increment,
  `campus_uid` varchar(50) NOT NULL default '',
  `employee_num` varchar(12) NOT NULL default '',
  `firstname` varchar(50) default NULL,
  `lastname` varchar(50) default NULL,
  `superuser` enum('yes','no') NOT NULL default 'no',
  `given_consent` enum('yes','no','under 18','pending') NOT NULL default 'pending',
  `account_type` enum('normal','demo') NOT NULL default 'normal',
  `latest_submission_pk` int(20) unsigned default NULL,
  `password` varchar(20) default NULL,
  PRIMARY KEY  (`student_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `submission_archives`
-- 

CREATE TABLE `submission_archives` (
  `archive_pk` int(20) unsigned NOT NULL auto_increment,
  `archive` mediumblob NOT NULL,
  PRIMARY KEY  (`archive_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `submissions`
-- 

CREATE TABLE `submissions` (
  `submission_pk` int(20) unsigned NOT NULL auto_increment,
  `student_registration_pk` int(20) unsigned NOT NULL default '0',
  `project_pk` int(20) unsigned NOT NULL default '0',
  `num_test_outcomes` smallint(4) unsigned NOT NULL default '0',
  `current_test_run_pk` int(20) unsigned default '0',
  `submission_number` int(20) unsigned NOT NULL default '0',
  `submission_timestamp` datetime default NULL,
  `cvstag_timestamp` varchar(15) default NULL,
  `build_request_timestamp` datetime default NULL,
  `build_status` enum('new','pending','complete','accepted','retest','broken','background') NOT NULL default 'new',
  `submit_client` varchar(30) NOT NULL default 'unknown',
  `release_request` datetime default NULL,
  `release_eligible` enum('true','false') NOT NULL default 'false',
  `num_passed_overall` smallint(3) NOT NULL default '0',
  `num_build_tests_passed` smallint(3) NOT NULL default '0',
  `num_public_tests_passed` smallint(3) NOT NULL default '0',
  `num_release_tests_passed` smallint(3) NOT NULL default '0',
  `num_secret_tests_passed` smallint(3) NOT NULL default '0',
  `num_findbugs_warnings` smallint(3) NOT NULL default '0',
  `archive_pk` int(20) unsigned default NULL,
  `commit_timestamp` datetime default NULL,
  `num_lines_changed` mediumint(10) default NULL,
  `net_change` mediumint(10) default NULL,
  `time_since_last_commit` time default NULL,
  `time_since_last_compilable_commit` time default NULL,
  `test_delta` smallint(6) default NULL,
  `findbugs_delta` smallint(6) default NULL,
  `faults_delta` smallint(6) default NULL,
  `diff_file` mediumtext,
  `commit_cvstag` varchar(20) default NULL,
  `commit_number` mediumint(9) NOT NULL default '0',
  `previous_md5sum_classfiles` int(10) unsigned default NULL,
  `previous_md5sum_sourcefiles` mediumint(10) unsigned default NULL,
  `previous_submission_pk` int(20) unsigned default NULL,
  `new_faults` smallint(5) unsigned default NULL,
  `removed_faults` smallint(5) unsigned default NULL,
  `total_faults` smallint(5) unsigned default NULL,
  PRIMARY KEY  (`submission_pk`),
  KEY `student_pk` (`student_registration_pk`,`project_pk`),
  KEY `project_pk` (`project_pk`),
  KEY `student_registration_pk` (`student_registration_pk`),
  KEY `build_status` (`build_status`),
  KEY `submission_timestamp` (`submission_timestamp`),
  KEY `current_test_run_pk` (`current_test_run_pk`),
  KEY `previous_md5sum_classfiles` (`previous_md5sum_classfiles`),
  KEY `faults_delta` (`faults_delta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `test_outcomes`
-- 

CREATE TABLE `test_outcomes` (
  `test_run_pk` int(20) unsigned NOT NULL default '0',
  `test_type` enum('build','public','release','secret','findbugs','pmd','student','uncovered_method') NOT NULL default 'build',
  `test_number` int(20) unsigned NOT NULL default '0',
  `outcome` enum('passed','failed','could_not_run','warning','error','not_implemented','huh','pmd','broken','timeout','uncovered_method') NOT NULL default 'passed',
  `point_value` smallint(4) NOT NULL default '0',
  `test_name` varchar(100) NOT NULL default '',
  `short_test_result` varchar(200) NOT NULL default '',
  `long_test_result` text NOT NULL,
  `exception_class_name` varchar(75) default NULL,
  `coarsest_coverage_level` enum('method','statement','branch','none') default NULL,
  `exception_source_covered_elsewhere` enum('1','0') NOT NULL default '0',
  `details` mediumblob,
  PRIMARY KEY  (`test_run_pk`,`test_type`,`test_number`,`outcome`),
  KEY `test_type` (`test_type`),
  KEY `test_run_pk` (`test_run_pk`),
  KEY `outcome` (`outcome`),
  KEY `test_name` (`test_name`(8))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

-- 
-- Table structure for table `test_runs`
-- 

CREATE TABLE `test_runs` (
  `test_run_pk` int(20) unsigned NOT NULL auto_increment,
  `project_jarfile_pk` int(20) unsigned NOT NULL default '0',
  `submission_pk` int(20) unsigned NOT NULL default '0',
  `test_timestamp` datetime NOT NULL default '1000-01-01 00:00:00',
  `test_machine` varchar(100) NOT NULL default 'unknown',
  `num_passed_overall` smallint(3) NOT NULL default '0',
  `num_build_tests_passed` smallint(3) NOT NULL default '0',
  `num_public_tests_passed` smallint(3) NOT NULL default '0',
  `num_release_tests_passed` smallint(3) NOT NULL default '0',
  `num_secret_tests_passed` smallint(3) NOT NULL default '0',
  `num_findbugs_warnings` smallint(3) NOT NULL default '0',
  `md5sum_classfiles` varchar(32) default NULL,
  `md5sum_sourcefiles` varchar(32) default NULL,
  PRIMARY KEY  (`test_run_pk`),
  KEY `test_runs_ibfk_1` (`submission_pk`),
  KEY `md5sum_classfiles` (`md5sum_classfiles`(4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
        
