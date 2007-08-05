CREATE DATABASE `pltasks` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `pltasks`;

-- ----------------------------------------------------------------------------------------

CREATE TABLE `auth` (
  `ident_id` smallint(5) unsigned NOT NULL,
  `password` varchar(128) NOT NULL,
  PRIMARY KEY  (`ident_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `builders` (
  `builder_id` mediumint(8) unsigned NOT NULL auto_increment,
  `builder_path` varchar(2048) NOT NULL,
  PRIMARY KEY  (`builder_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `events` (
  `event_id` smallint(5) unsigned NOT NULL auto_increment,
  `task_id` smallint(5) unsigned NOT NULL,
  `ident_id` smallint(5) unsigned NOT NULL,
  `stamp` datetime NOT NULL,
  `note_id` mediumint(8) unsigned NOT NULL,
  `new_active_id` smallint(5) unsigned default NULL,
  `new_status_id` smallint(5) unsigned default NULL,
  `builder_id` mediumint(8) unsigned default NULL,
  PRIMARY KEY  (`event_id`),
  KEY `task_id` (`task_id`),
  KEY `ident_id` (`ident_id`),
  KEY `stamp` (`stamp`),
  KEY `new_active_id` (`new_active_id`),
  KEY `new_status_id` (`new_status_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `idents` (
  `ident_id` smallint(5) unsigned NOT NULL auto_increment,
  `ident_name` varchar(32) NOT NULL,
  `is_group` tinyint(1) NOT NULL,
  PRIMARY KEY  (`ident_id`),
  KEY `is_group` (`is_group`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `node_info` (
  `info_id` mediumint(8) unsigned NOT NULL auto_increment,
  `node_id` mediumint(8) unsigned NOT NULL,
  `node_version` varchar(16) NOT NULL,
  `event_id` mediumint(8) unsigned NOT NULL,
  `is_edit` tinyint(1) NOT NULL,
  `is_submit` tinyint(1) NOT NULL,
  `is_focus` tinyint(1) NOT NULL,
  `is_thumb` tinyint(1) NOT NULL,
  `is_approve` tinyint(1) NOT NULL,
  PRIMARY KEY  (`info_id`),
  KEY `node_id` (`node_id`),
  KEY `event_id` (`event_id`),
  KEY `is_edit` (`is_edit`),
  KEY `is_submit` (`is_submit`),
  KEY `is_focus` (`is_focus`),
  KEY `is_approve` (`is_approve`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `node_names` (
  `node_id` mediumint(8) unsigned NOT NULL auto_increment,
  `node_name` varchar(2048) NOT NULL,
  PRIMARY KEY  (`node_id`),
  KEY `node_name` (`node_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `notes` (
  `note_id` mediumint(8) unsigned NOT NULL auto_increment,
  `note_text` text NOT NULL,
  PRIMARY KEY  (`note_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `supervisors` (
  `task_id` smallint(5) unsigned NOT NULL,
  `ident_id` smallint(5) unsigned default NULL,
  KEY `task_id` (`task_id`),
  KEY `ident_id` (`ident_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `task_activity` (
  `active_id` smallint(5) unsigned NOT NULL auto_increment,
  `active_name` varchar(32) NOT NULL,
  `active_desc` text,
  PRIMARY KEY  (`active_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=4 ;

CREATE TABLE `task_status` (
  `status_id` smallint(5) unsigned NOT NULL auto_increment,
  `status_name` varchar(32) NOT NULL,
  `status_desc` text,
  PRIMARY KEY  (`status_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=8 ;

CREATE TABLE `task_titles` (
  `title_id` mediumint(8) unsigned NOT NULL auto_increment,
  `title_name` varchar(32) NOT NULL,
  `title_desc` text,
  PRIMARY KEY  (`title_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `task_types` (
  `type_id` smallint(5) unsigned NOT NULL auto_increment,
  `type_name` varchar(32) NOT NULL,
  `type_desc` text,
  PRIMARY KEY  (`type_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=9 ;

CREATE TABLE `tasks` (
  `task_id` smallint(5) unsigned NOT NULL auto_increment,
  `title_id` smallint(5) unsigned NOT NULL,
  `type_id` smallint(5) unsigned NOT NULL,
  `active_id` smallint(5) unsigned NOT NULL,
  `assigned_to` smallint(5) unsigned default NULL,
  `status_id` smallint(5) unsigned NOT NULL,
  `last_modified` datetime NOT NULL,
  PRIMARY KEY  (`task_id`),
  KEY `title_id` (`title_id`),
  KEY `type_id` (`type_id`),
  KEY `active_id` (`active_id`),
  KEY `assigned_to` (`assigned_to`),
  KEY `status_id` (`status_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `thumb_info` (
  `thumb_info_id` mediumint(8) unsigned NOT NULL,
  `focus_info_id` mediumint(8) unsigned NOT NULL,
  `thumb_path` varchar(2048) NOT NULL,
  PRIMARY KEY  (`thumb_info_id`),
  KEY `focus_info_id` (`focus_info_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------------------

INSERT INTO `task_activity` (`active_id`, `active_name`, `active_desc`) VALUES 
(1, 'Inactive', 'No work is currently be done to complete the task.'),
(2, 'Active', 'The assigned artist (or work group) is currently working to complete the task.'),
(3, 'Submitted', 'Work is temporarily suspended on the task until the supervisor(s) of the task have reviewed the most recent changes submitted for approval.');

INSERT INTO `task_status` (`status_id`, `status_name`, `status_desc`) VALUES 
(1, 'Unapproved', 'Work completed to date on the task has not yet been reviewed by its supervisor(s) and is therefore not approved for use by other tasks.'),
(2, 'Changes Required', 'The task has been reviewed by its supervisor(s) who have made notes about the changes which must be completed before the task can be approved.'),
(3, 'Approved', 'The task has been reviewed and provisionally approved for use by other tasks.'),
(4, 'On Hold', 'All work should cease on the task until further notice.'),
(5, 'Could Be Better', 'The task has been reviewed by its supervisor(s) and is considered to be sufficiently complete to satisfy the client, but should be improved if possible after all other tasks are complete.'),
(6, 'Finalled', 'The task has been reviewed by its supervisor(s) and is judged to be absolutely completed.  No further work should be performed on this task!'), 
(7, 'Building', 'A post-approval Builder program is currently rebuilding the product nodes for the task and will post the approval information for the task when done.');

INSERT INTO `task_types` (`type_id`, `type_name`, `type_desc`) VALUES 
(1, 'Modeling', 'Create the geometry and any required UV space mappings required for an asset.'),
(2, 'Rigging', 'Create and attach the geometry of an asset the complete animation rig: bones, clusters, deformers, handles, expressions, etc... '),
(3, 'Look Dev', 'Encompasses all aspects of developing the complete rendered look of an asset including: shaders, color textures, normal maps, displacement maps, etc... '),
(4, 'Layout', 'Creating the initial placement of all sets, props and characters in a shot in preparation for animation.'),
(5, 'Animation', 'Generating the motion data for all animatable objects in a shot.'),
(6, 'Effects', 'Procedural modeling, animation and rendering including: particles, cloth, dynamics, fluids, etc...'),
(7, 'Lighting', 'Placing, animating and all lights in a scene, fine tuning shaders and rendering all image passes required to composite a shot.'),
(8, 'Compositing', 'Combining all render passes and other imagery to produce the final unedited images for a shot.');
