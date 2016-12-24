ALTER TABLE `student_submit_status`
ADD `number_commits` smallint(10) unsigned NOT NULL default '0' AFTER `number_submissions`
;

ALTER TABLE `submissions`
ADD `commit_timestamp` datetime default NULL,
ADD `num_lines_changed` mediumint(10) default NULL,
ADD `net_change` mediumint(10) default NULL,
ADD `time_since_last_commit` time default NULL,
ADD `test_delta` smallint(6) default NULL,
ADD `diff_file` mediumtext,
ADD `commit_cvstag` varchar(20) default NULL,
ADD `commit_number` mediumint(9) NOT NULL default '0',
ADD `previous_commit_submission_pk` int(10) unsigned default NULL,
ADD KEY `submission_timestamp` (`submission_timestamp`)
;

ALTER TABLE `test_outcomes`
CHANGE `outcome` `outcome` enum('passed','failed','could_not_run','warning','error','not_implemented','huh') NOT NULL default 'passed',
ADD `exception_class_name` varchar(40) default NULL,
ADD  `details` mediumblob
;

ALTER TABLE `test_runs`
ADD `md5sum_classfiles` varchar(32) default NULL,
ADD `md5sum_sourcefiles` varchar(32) default NULL
;
