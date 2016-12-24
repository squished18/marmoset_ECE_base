-- phpMyAdmin SQL Dump
-- version 2.8.0.2
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: May 08, 2006 at 03:12 PM
-- Server version: 4.1.16
-- PHP Version: 4.4.2
-- 
-- Database: `submitserver`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `spring2006_survey`
-- 

CREATE TABLE `spring2006_survey` (
  `pk` mediumint(8) unsigned NOT NULL auto_increment,
  `course_pk` mediumint(8) NOT NULL default '0',
  `student_pk` mediumint(8) unsigned default NULL,
  `first_semester_used` varchar(25) NOT NULL default '',
  `used_submit_server_before` enum('true','false') NOT NULL default 'true',
  `plugin_working` enum('true','false') default NULL,
  `overall_impression` smallint(3) NOT NULL default '0',
  `overall_impression_comments` mediumtext NOT NULL,
  `prefer_release_testing` smallint(3) NOT NULL default '0',
  `prefer_release_testing_comments` mediumtext NOT NULL,
  `good_use_of_feedback` smallint(3) NOT NULL default '0',
  `good_use_of_feedback_commentes` mediumtext NOT NULL,
  `feedback_feeling` smallint(3) NOT NULL default '0',
  `feedback_feeling_comments` mediumtext NOT NULL,
  `started_earlier` enum('true','false') NOT NULL default 'true',
  `started_earlier_comments` mediumtext NOT NULL,
  `average_release_tests_used` smallint(3) NOT NULL default '0',
  `educational_disadvantages` mediumtext NOT NULL,
  `suggestions` mediumtext NOT NULL,
  PRIMARY KEY  (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `survey_responses` (
  `student_pk` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`student_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
