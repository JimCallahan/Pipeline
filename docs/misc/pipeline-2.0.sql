
Features:

SELECT DISTINCT phpbb_topics.topic_id, phpbb_topics.topic_title
FROM bugs_reports, phpbb_posts, phpbb_topics
WHERE phpbb_topics.forum_id =12
AND project_id =1
AND bugs_reports.version_id >=56
AND bugs_reports.version_id <=63
AND bugs_reports.status_id =5
AND bugs_reports.post_id = phpbb_posts.post_id
AND phpbb_posts.topic_id = phpbb_topics.topic_id


Bugs: 

SELECT DISTINCT phpbb_topics.topic_id, phpbb_topics.topic_title
FROM bugs_reports, phpbb_posts, phpbb_topics
WHERE phpbb_topics.forum_id =12
AND project_id =1
AND bugs_reports.version_id >=56
AND bugs_reports.version_id <=63
AND bugs_reports.status_id =4
AND bugs_reports.post_id = phpbb_posts.post_id
AND phpbb_posts.topic_id = phpbb_topics.topic_id







-----------------------------------------------------------------------------------



FEATURES: 

173  	Renumber Should Kill Obsolete Frame Jobs
477 	Check-Out and Running Jobs
353 	Node Locking
530 	Should be able to remove default view
458 	Mac OS X Job Servers
546 	Custom Commandline Args for Render Actions
553 	Double Action Parameter Precision
561 	Check-Out KeepNewer is Depreciated
562 	Manual Job Preemption
511 	Time Varying Selection Keys
484 	'View' Menu item in the slots view
572 	Job Details Timing Stats
520 	Multiple Add/Replace/Hide Root
533 	Saving Output/Error Logs Locally
574 	Taller File Browser Dialog
565 	Maya-7.0.1 Update



UI: 

509 	Optional Status Updated after User Tool execution


474 	Group Administrator


Server Daemons: 

502 	Plugin Vendor Property
512 	Limit Node Cache Size
472 	Archive and Offline Locking
514 	Multiple Job Submission


Major Applications: 

524 	Adobe Creative Suite Plugins
538 	PRMan Plugins
566 	Gelato-2.0 Plugins
543 	AIR-4.0 Plugins
540 	BakeAIR Action Plugin
539 	MentalRay Standalone Plugins
516 	Shake Plugins


Misc Plugins: 

517 	Evince Editor Plugin
558 	VIM Editor

555 	EnvCrossToSeq Action
577 	ExtractSeq Action




-----------------------------------------------------------------------------------



Bugs: 

507  	setAction clears JobReq
508 	Bad Frozen/Release View Interaction
521 	Maya-7.0 Breaks MayaRender Action
523 	Maya-7.0 Breaks MRayRenderGlobals Action
529 	Release View exception when no checked out files
535 	Pre-Launch Action Failures and Logs
541 	Check-Out Keep-Modified Bug
551 	Job Server Tempfile Naming
547 	MayaRender Action (mental ray) ignores -rd option
544 	plscript --batch exception
569 	Remove Selection Key/Schedule Exception
570 	Preemption should not change timestamps
576 	Quote Exported Toolset Values
575 	Package Add Entry Exception
