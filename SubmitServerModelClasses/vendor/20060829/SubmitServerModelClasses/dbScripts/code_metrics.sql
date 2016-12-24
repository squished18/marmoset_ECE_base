CREATE TABLE `code_metrics` (
  `test_run_pk` mediumint(6) unsigned NOT NULL default '0',
  `md5sum_sourcefiles` varchar(32) NOT NULL default '',
  `md5sum_classfiles` varchar(32) NOT NULL default '',
  `code_segment_size` mediumint(8) NOT NULL default '0',
  PRIMARY KEY  (`test_run_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

