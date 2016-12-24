ALTER TABLE `test_outcomes` 
ADD `coarsest_coverage_level` ENUM( 'method', 'statement', 'branch', 'none' ) AFTER `exception_class_name` ,
ADD `exception_source_covered_elsewhere` ENUM( '1', '0' ) DEFAULT '0' NOT NULL AFTER `coarsest_coverage_level` ;

ALTER TABLE `test_outcomes` CHANGE `outcome` `outcome` ENUM( 'passed', 'failed', 'could_not_run', 'warning', 'error', 'not_implemented', 'huh', 'pmd', 'broken', 'timeout', 'uncovered_method' ) DEFAULT 'passed' NOT NULL ;

ALTER TABLE `test_outcomes` CHANGE `test_type` `outcome` ENUM( 'build', 'public', 'release', 'secret', 'findbugs', 'pmd', 'student', 'uncovered_method' ) DEFAULT 'build' NOT NULL ;
