CREATE TABLE `actions` (
  `action_id` smallint(6) NOT NULL auto_increment,
  `action_name` varchar(32) NOT NULL,
  `action_version` varchar(8) NOT NULL,
  `action_vendor` varchar(16) NOT NULL,
  UNIQUE KEY `action_id` (`action_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `authors` (
  `author_id` smallint(6) NOT NULL auto_increment,
  `author_name` varchar(32) NOT NULL,
  UNIQUE KEY `author_id` (`author_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `hosts` (
  `host_id` smallint(6) NOT NULL auto_increment,
  `host_name` varchar(32) NOT NULL,
  `host_os` enum('Unix','Windows','MacOS') default NULL,
  `num_procs` smallint(6) default NULL,
  `total_memory` bigint(20) default NULL,
  `total_disk` bigint(20) default NULL,
  UNIQUE KEY `host_id` (`host_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `jobs` (
  `job_id` bigint(20) unsigned NOT NULL default '0',
  `node_id` bigint(20) unsigned NOT NULL,
  `author_id` smallint(6) unsigned NOT NULL,
  `view_id` smallint(6) unsigned NOT NULL,
  `action_id` smallint(6) unsigned NOT NULL,
  `host_id` smallint(6) default NULL,
  `job_state` enum('Aborted','Finished','Failed') NOT NULL,
  `exit_code` smallint(6) default NULL,
  `submitted` timestamp NOT NULL default '0000-00-00 00:00:00',
  `started` timestamp NULL default NULL,
  `completed` timestamp NOT NULL default '0000-00-00 00:00:00',
  `user_time` double default NULL,
  `system_time` double default NULL,
  `page_faults` int(11) default NULL,
  `virtual_size` bigint(20) unsigned default NULL,
  `resident_size` bigint(20) unsigned default NULL,
  `swapped_size` bigint(20) unsigned default NULL,
  UNIQUE KEY `id` (`job_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `nodes` (
  `node_id` smallint(6) NOT NULL auto_increment,
  `node_name` text NOT NULL,
  UNIQUE KEY `node_id` (`node_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `samples` (
  `host_id` smallint(6) NOT NULL,
  `stamp` datetime NOT NULL,
  `num_jobs` smallint(6) NOT NULL,
  `system_load` float NOT NULL,
  `free_memory` bigint(20) NOT NULL,
  `free_disk` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `views` (
  `view_id` smallint(6) NOT NULL auto_increment,
  `view_name` varchar(32) NOT NULL,
  UNIQUE KEY `view_id` (`view_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
