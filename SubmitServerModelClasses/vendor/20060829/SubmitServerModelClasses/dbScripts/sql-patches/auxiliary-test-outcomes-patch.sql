CREATE TABLE `auxiliary_test_outcomes` (
  `test_run_pk` int(8) NOT NULL default '0',
  `test_type` enum('public','release','secret','student') NOT NULL default 'public',
  `test_number` smallint(5) NOT NULL default '0',
  `outcome` enum('passed','failed','could_not_run','error','not_implemented','huh') NOT NULL default 'passed',
  `failing_only` enum('1','0') NOT NULL default '0',
  PRIMARY KEY  (`test_run_pk`,`test_type`,`test_number`,`outcome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='test_outcomes that cover code that is not covered by any pas';
