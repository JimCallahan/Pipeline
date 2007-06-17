// $Id: LogQueueActivityExt.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.LogQueueActivityExt.v2_1_1;

import java.io.*;
import java.text.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   Q U E U E   A C T I V I T Y    E X T                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A simple test extension which prints a message for each extendable queue operation.
 */
public class 
LogQueueActivityExt
  extends BaseQueueExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  LogQueueActivityExt()
  {
    super("LogQueueActivity", new VersionID("2.1.1"), "Temerity",
	  "A simple test extension which prints a message for each extendable queue " + 
	  "operation."); 

    /* plugin ops */ 
    {
      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aEnableDelay, 
	   "The length of time in milliseconds to delay enable.", 
	   10000);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aDisableDelay, 
	   "The length of time in milliseconds to delay disable.", 
	   10000);
	addParam(param);
      }
    }

    /* server ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogSubmitJobs, 
	   "Enable logging of the submission of new jobs.",
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogDeleteJobGroup, 
	   "Enable logging of deletion of job groups.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCleanupJobs, 
	   "Enable logging of the cleanup of obsolete jobs.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowAddHost, 
	   "Whether to allow new job server hosts to be added.",
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogAddHost, 
	   "Enable logging of the addition of new job server hosts.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRemoveHosts, 
	   "Whether to allow the removal of existing job server hosts.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRemoveHosts, 
	   "Enable logging of the removal of existing job server hosts.",
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogModifyHosts, 
	   "Enable logging of changes in host status or properties.",
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogResourceSamples, 
	   "Enable logging of dynamic resource sample writes to disk.", 
	   true);
	addParam(param);
      }
    }

    /* dispatcher ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogJobAbort, 
	   "Enable logging of job aborts.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogJobBalk, 
	   "Enable logging of job balks.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogJobStart,
	   "Enable logging of job startup.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogJobPreempt,
	   "Enable logging of job preemption.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogJobFinish,
	   "Enable logging of job completion.", 
	   true);
	addParam(param);
      }
    }
    

    {  
      LayoutGroup layout = new LayoutGroup(true); 

      {
	LayoutGroup sub = new LayoutGroup
	  ("PluginOps", "Plugin initialization and shutdown operations.", true); 
	sub.addEntry(aEnableDelay);
	sub.addEntry(aDisableDelay);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("ServerOps", "General queue mananger server operations.", true); 
	sub.addEntry(aLogSubmitJobs); 
	sub.addEntry(aLogDeleteJobGroup); 
	sub.addEntry(aLogCleanupJobs); 
	sub.addSeparator();
	sub.addEntry(aAllowAddHost); 
	sub.addEntry(aLogAddHost); 
	sub.addSeparator();
	sub.addEntry(aAllowRemoveHosts); 
	sub.addEntry(aLogRemoveHosts); 
	sub.addSeparator();  
	sub.addEntry(aLogModifyHosts); 
	sub.addEntry(aLogResourceSamples); 
	
	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("DispatcherOps", "Job dispatcher operations.", true);
	sub.addEntry(aLogJobAbort);
	sub.addEntry(aLogJobBalk);
	sub.addEntry(aLogJobStart);
	sub.addEntry(aLogJobPreempt);
	sub.addEntry(aLogJobFinish);
	
	layout.addSubGroup(sub);
      }
      
      setLayout(layout);  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*  P L U G I N   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually. 
   */  
  public boolean
  hasPostEnableTask()
  {
    return true;
  }

  /**
   * The task to perform after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually.
   */ 
  public void 
  postEnableTask()
  {
    /* just to prove that the server is waiting on this task to finish... */ 
    try {
      Integer delay = (Integer) getParamValue(aEnableDelay); 
      if((delay != null) && (delay > 0)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Info, 
	   "LogQueueActivity Enabling - " + 
	   "Please Wait (" + delay + ") milliseconds..."); 
	LogMgr.getInstance().flush();

	Thread.sleep(delay);
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "LogQueueActivity Enabled!"); 
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */  
  public boolean
  hasPreDisableTask()
  {
    return true;
  }

  /**
   * The task to perform after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  public void 
  preDisableTask()
  {
    /* just to prove that the server is waiting on this task to finish... */ 
    try {
      Integer delay = (Integer) getParamValue(aDisableDelay); 
      if((delay != null) && (delay > 0)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Info, 
	   "LogQueueActivity Disabling - " + 
	   "Please Wait (" + delay + ") milliseconds..."); 
	LogMgr.getInstance().flush();

	Thread.sleep(delay);
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "LogQueueActivity Disabled!"); 
  }

 
   
  /*----------------------------------------------------------------------------------------*/
  /*  S E R V E R   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after new jobs are submitted to the queue.
   */  
  public boolean
  hasPostSubmitJobsTask() 
  {
    return isParamTrue(aLogSubmitJobs); 
  }

  /**
   * The task to perform after new jobs are submitted to the queue.
   * 
   * @param group
   *   The queue job group.
   * 
   * @param jobs
   *   The submitted jobs indexed by job ID.
   * 
   * @throws PipelineException
   *   If unable to perform the task.
   */  
  public void
  postSubmitJobsTask
  (
   QueueJobGroup group,
   TreeMap<Long,QueueJob> jobs
  ) 
  {
    StringBuilder buf = new StringBuilder();

    {
      NodeID nodeID = group.getNodeID();
      buf.append
	("Job Group " + group.getGroupID() + " - SUBMITTED\n" + 
	 "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
	 "    Node : " + nodeID.getName() + "\n" + 
	 "  Target : " + group.getRootSequence());
    }

    for(Long jobID : jobs.keySet()) {
      QueueJob job = jobs.get(jobID);
      NodeID nodeID = job.getNodeID();
      buf.append      
	("\n" + 
	 "Job " + job.getJobID() + " - SUBMITTED\n" + 
	 "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
	 "    Node : " + nodeID.getName() + "\n" + 
	 "  Target : " + job.getActionAgenda().getPrimaryTarget());
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());        
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a task after a completed job group has been marked for removal.
   */  
  public boolean
  hasPostDeleteJobGroupTask() 
  {
    return isParamTrue(aLogDeleteJobGroup); 
  }

  /**
   * The task to perform after a completed job group has been marked for removal.
   * 
   * @param group
   *   The completed job group.
   */  
  public void
  postDeleteJobGroupTask
  (
   QueueJobGroup group
  ) 
  {
    NodeID nodeID = group.getNodeID();
    String msg = 
      ("Job Group " + group.getGroupID() + " - DELETED\n" + 
       "    Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "     Node : " + nodeID.getName() + "\n" + 
       "   Target : " + group.getRootSequence() + "\n" +
       "  Started : " + TimeStamps.format(group.getSubmittedStamp()) + "\n" + 
       " Finished : " + TimeStamps.format(group.getCompletedStamp()) + "\n" + 
       " Duration : " + TimeStamps.formatInterval(group.getCompletedStamp() - 
                                                  group.getSubmittedStamp()));
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);    
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a task after the job garbage collector has cleaned-up jobs no longer
   * referenced by any remaining job group.<P> 
   * 
   * This is a good way to collect long term information about all jobs while having a 
   * very low impact on the queue manager.  All jobs are eventually cleaned up so this will 
   * provide the same information as the {@link #postJobFinishedTask}, but operates 
   * on large batches of jobs instead of after each individual job.  
   */  
  public boolean
  hasPostCleanupJobsTask() 
  {
    return isParamTrue(aLogCleanupJobs); 
  }

  /**
   * The task to perform after the job garbage collector has cleaned-up jobs no longer
   * referenced by any remaining job group.<P> 
   * 
   * This is a good way to collect long term information about all jobs while having a 
   * very low impact on the queue manager.  All jobs are eventually cleaned up so this will 
   * provide the same information as the {@link #postJobFinishedTask}, but operates 
   * on large batches of jobs instead of after each individual job.  
   * 
   * @param jobs
   *   The completed jobs indexed by job ID.
   * 
   * @param infos
   *   Information about when and where the job was executed indexed by job ID.
   */  
  public void
  postCleanupJobsTask
  (
   TreeMap<Long,QueueJob> jobs,
   TreeMap<Long,QueueJobInfo> infos
  ) 
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "Cleaning Up (" + jobs.size() + ") Jobs...");    
    
    for(Long jobID : jobs.keySet()) 
      postJobFinishedTask(jobs.get(jobID), infos.get(jobID));
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to test before adding a new job server host.
   */  
  public boolean
  hasPreAddHostTest() 
  {
    return true;
  }

  /**
   * Test to perform before adding a new job server host.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preAddHostTest
  (
   String hostname   
  ) 
    throws PipelineException
  {
    Boolean tf = (Boolean) getParamValue(aAllowAddHost); 
    if((tf == null) || !tf) 
      throw new PipelineException
	("Adding the job server (" + hostname + ") is not allowed!");
  }


  /**
   * Whether to run a task after adding a new job server host.
   */  
  public boolean
  hasPostAddHostTask() 
  {
    return isParamTrue(aLogAddHost); 
  }

  /**
   * The task to perform after adding a new job server host.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   */  
  public void
  postAddHostTask
  (
   String hostname   
  ) 
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "Job Server [" + hostname + "] - ADDED"); 
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to test before removing some existing job server hosts.
   */  
  public boolean
  hasPreRemoveHostsTest() 
  {
    return true;
  }

  /**
   * Test to perform before removing some existing job server hosts.
   * 
   * @param hostnames
   *   The fully resolved names of the hosts.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRemoveHostsTest
  (
   TreeSet<String> hostnames
  ) 
    throws PipelineException
  {
    Boolean tf = (Boolean) getParamValue(aAllowRemoveHosts); 
    if((tf == null) || !tf) 
      throw new PipelineException
	("Removing job servers is not allowed!");
  }


  /**
   * Whether to run a task after removing some existing job server hosts.
   */  
  public boolean
  hasPostRemoveHostsTask() 
  {
    return isParamTrue(aLogRemoveHosts); 
  }

  /**
   * The task to perform after removing some existing job server hosts.
   * 
   * @param hostnames
   *   The fully resolved names of the hosts.
   */  
  public void
  postRemoveHostsTask
  (
   TreeSet<String> hostnames
  ) 
  {
    StringBuilder buf = new StringBuilder();
    for(String hname : hostnames) 
      buf.append("Job Server [" + hname + "] - REMOVED"); 

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString()); 
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after modifying host status or properties.
   */  
  public boolean
  hasPostModifyHostsTask() 
  {
    return isParamTrue(aLogModifyHosts);
  }

  /**
   * The task to perform after modifying host status or properties.<P> 
   *
   * A host may be modified either manually by users or automatically by the queue
   * manager itself.  Automatic modifications include marking unresponsive servers as 
   * Hung (or Disabled), re-Enabling servers which start responding again and changes
   * to the Selection Group caused by a Selection Schedule. <P> 
   *
   * The modified host information will not include any dynamic resource information such 
   * as the available memory, disk or system load.  This information can be obtained using
   * the {@link #postResourceSamplesTask} instead. 
   * 
   * @param hosts
   *   The information about the modified hosts indexed by fully resolved hostname.
   */  
  public void
  postModifyHostsTask
  (
   TreeMap<String,QueueHostInfo> hosts
  ) 
  {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(String hname : hosts.keySet()) {
      QueueHostInfo info = hosts.get(hname); 
      
      OsType os = info.getOsType();
      String res = info.getReservation();
      Integer procs = info.getNumProcessors();
      Long mem = info.getTotalMemory(); 
      Long disk = info.getTotalDisk();
      String sched = info.getSelectionSchedule();
      String group = info.getSelectionGroup();

      if(!first) 
	buf.append("\n");
      first = false;
      
      buf.append
	("Job Server [" + hname + "] - MODIFIED\n" + 
	 "        Status : " + info.getStatus() + "\n" + 
  	 "       OS Type : " + ((os != null) ? os : "-") + "\n" +
  	 "     Num Procs : " + ((procs != null) ? procs.toString() : "-") + "\n" + 
	 "  Total Memory : " + ((mem != null) ? mem : "-") + "\n" + 
	 "    Total Disk : " + ((disk != null) ? disk : "-") + "\n" + 
	 "     Job Slots : " + info.getJobSlots() + "\n" + 
	 "         Order : " + info.getOrder() + "\n" +
	 "   Reservation : " + ((res != null) ? res : "-") + "\n" + 
	 "         Group : " + ((group != null) ? group : "-") + "\n" +
	 "      Schedule : " + ((sched != null) ? sched : "-"));
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());    
  }
  
  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after writing job server dynamic resource samples to disk.
   */  
  public boolean
  hasPostResourceSamplesTask() 
  {
    return isParamTrue(aLogResourceSamples);  
  }

  /**
   * The task to perform after writing job server dynamic resource samples to disk.<P> 
   * 
   * The queue manager periodically writes a block of resource samples it has been caching
   * to disk to free up memory.  This method is invoked whenever the samples are saved. 
   * The default configuration is to write 1-minute averaged values at 30-minute intervals.
   * 
   * @param samples
   *   The dynamic resource samples indexed by fully resolved hostname.
   */  
  public void
  postResourceSamplesTask
  (
   TreeMap<String,ResourceSampleCache> samples
  ) 
  {
    DecimalFormat fmt = new DecimalFormat("###0.0");   
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(String hname : samples.keySet()) {
      if(!first) 
	buf.append("\n");
      first = false;

      buf.append("Job Server [" + hname + "] - RESOURCE SAMPLES\n");

      ResourceSampleCache cache = samples.get(hname); 
      int numSamples = cache.getNumSamples();
      if(numSamples > 0) {
	int minJobs = Integer.MAX_VALUE;
	int maxJobs = 0;

	float minLoad = Float.MAX_VALUE;
	float maxLoad = 0.0f;

	long minMem = Long.MAX_VALUE;
	long maxMem = 0L;

	long minDisk = Long.MAX_VALUE;
	long maxDisk = 0L;

	int wk;
	for(wk=0; wk<numSamples; wk++) {
	  int jobs = cache.getNumJobs(wk);
	  minJobs = Math.min(minJobs, jobs);
	  maxJobs = Math.max(maxJobs, jobs);

	  float load = cache.getLoad(wk);
	  minLoad = Math.min(minLoad, load);
	  maxLoad = Math.max(maxLoad, load);

	  long mem = cache.getMemory(wk);
	  minMem = Math.min(minMem, mem);
	  maxMem = Math.max(maxMem, mem);

	  long disk = cache.getDisk(wk);
	  minDisk = Math.min(minDisk, disk);
	  maxDisk = Math.max(maxDisk, disk);
	}

	buf.append
	  ("      Started : " + TimeStamps.format(cache.getFirstTimeStamp()) + "\n" + 
	   "        Ended : " + TimeStamps.format(cache.getLastTimeStamp()) + "\n" + 
	   "  Num Samples : " + numSamples);
	
	if(numSamples > 0) {
	  buf.append
	    ("\n" + 
	     "     Num Jobs : " + minJobs + " / " + maxJobs + " (min/max)\n" + 
	     "  System Load : " + minLoad + " / " + maxLoad + " (min/max)\n" + 
	     "  Free Memory : " + minMem + " / " + maxMem + " (min/max)\n" + 
	     "    Free Disk : " + minDisk + " / " + maxDisk + " (min/max)");
	}
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*  D I S P A T C H E R   O P S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task if job has been aborted (cancelled).<P> 
   * 
   * Jobs which where already running when aborted will also invoke the post-finish task.
   * If a job is aborted before it began execution, then only this method will be called.
   */  
  public boolean
  hasPostJobAbortedTask() 
  {
    return isParamTrue(aLogJobAbort); 
  }
  
  /**
   * The task to perform if job has been aborted (cancelled).<P> 
   * 
   * Jobs which where already running when aborted will also invoke the post-finish task.
   * If a job is aborted before it began execution, then only this method will be called.
   * 
   * @param job
   *   The job specification.
   */  
  public void
  postJobAbortedTask
  (
   QueueJob job
  ) 
  {
    NodeID nodeID = job.getNodeID();
    String msg = 
      ("Job " + job.getJobID() + " - ABORTED\n" + 
       "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "    Node : " + nodeID.getName() + "\n" + 
       "  Target : " + job.getActionAgenda().getPrimaryTarget());

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task if a job is unable to start (balked). <P> 
   * 
   * A job is considered to be balked if the particular job manager assigned to the job is
   * unable to be contacted by the queue manager in a timely manner. The job will be 
   * automatically requeued after a balk similar to how a preempted job is handled. 
   */  
  public boolean
  hasPostJobBalkedTask() 
  {
    return isParamTrue(aLogJobBalk); 
  }
  
  /**
   * The task to perform if a job is unable to start (balked). <P> 
   * 
   * A job is considered to be balked if the particular job manager assigned to the job is
   * unable to be contacted by the queue manager in a timely manner (Hung).  The job will be 
   * automatically requeued after a balk similar to how a preempted job is handled. 
   * 
   * @param job
   *   The job specification.
   * 
   * @param hostname
   *   The name of the host running the unresponsive job manager.
   */  
  public void
  postJobBalkedTask
  (
   QueueJob job,
   String hostname
  ) 
  {
    NodeID nodeID = job.getNodeID();
    String msg = 
      ("Job " + job.getJobID() + " - BALKED on [" + hostname + "]\n" + 
       "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "    Node : " + nodeID.getName() + "\n" + 
       "  Target : " + job.getActionAgenda().getPrimaryTarget());

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a job starts running.
   */  
  public boolean
  hasPostJobStartedTask() 
  {
    return isParamTrue(aLogJobStart); 
  }
  
  /**
   * The task to perform after a job starts running.
   * 
   * @param job
   *   The job specification.
   * 
   * @param info
   *   Information about when and where the job was started.
   */  
  public void
  postJobStartedTask
  (
   QueueJob job, 
   QueueJobInfo info
  ) 
  {
    NodeID nodeID = job.getNodeID();
    String msg = 
      ("Job " + job.getJobID() + " - STARTED on [" + info.getHostname() + "] at " + 
       TimeStamps.format(info.getStartedStamp()) + "\n" + 
       "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "    Node : " + nodeID.getName() + "\n" + 
       "  Target : " + job.getActionAgenda().getPrimaryTarget());

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a job finishes running.
   */  
  public boolean
  hasPostJobFinishedTask() 
  {
    return isParamTrue(aLogJobFinish);
  }
  
  /**
   * The task to perform after a job finishes running.
   * 
   * @param job
   *   The job specification.
   * 
   * @param info
   *   Information about when and where the job was executed.
   */  
  public void
  postJobFinishedTask
  (
   QueueJob job, 
   QueueJobInfo info
  ) 
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("Job " + job.getJobID() + " - ");
    
    QueueJobResults results = info.getResults();
    if(results == null) {
      buf.append("NEVER EXECUTED");
    }
    else {
      Integer code = results.getExitCode();
      if(code != null) {
	if(code == 0)
	  buf.append("SUCCEEDED");
	else 
	  buf.append("FAILED [" + code + "]");
      }

      Long started = info.getStartedStamp();
      Long completed = info.getCompletedStamp();
      if((started != null) && (completed != null)) {
	buf.append
	  (" at " + TimeStamps.format(completed) + 
	   " [" + TimeStamps.formatInterval(completed - started) + "]");
      }
    }
    
    NodeID nodeID = job.getNodeID();
    buf.append
      ("\n" + 
       "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "    Node : " + nodeID.getName() + "\n" + 
       "  Target : " + job.getActionAgenda().getPrimaryTarget());

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a 
   */  
  public boolean
  hasPostJobPreemptedTask() 
  {
    return isParamTrue(aLogJobPreempt); 
  }
  
  /**
   * The task to perform after a job is manually preempted.
   * 
   * @param job
   *   The job specification.
   * 
   * @param info
   *   Information about when and where the job was run prior to preemption.
   */  
  public void
  postJobPreemptedTask
  (
   QueueJob job, 
   QueueJobInfo info   
  ) 
  {
    NodeID nodeID = job.getNodeID();
    long now = System.currentTimeMillis(); 
    String msg = 
      ("Job " + job.getJobID() + " - Preempted on [" + info.getHostname() + "] at " +
       TimeStamps.format(now) + "\n" + 
       "   Owner : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" +
       "    Node : " + nodeID.getName() + "\n" + 
       "  Target : " + job.getActionAgenda().getPrimaryTarget() + "\n" + 
       " Runtime : " + TimeStamps.formatInterval(now - info.getStartedStamp()));

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given boolean extension parameter is currently true.
   */ 
  private boolean 
  isParamTrue
  (
   String pname
  ) 
  {
    try {
      Boolean tf = (Boolean) getParamValue(pname); 
      return ((tf != null) && tf);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
      
      return false;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6639988244348742755L;


  private static final String  aEnableDelay  = "EnableDelay"; 
  private static final String  aDisableDelay = "DisableDelay"; 

  private static final String  aLogSubmitJobs     = "LogSubmitJobs"; 
  private static final String  aLogDeleteJobGroup = "LogDeleteJobGroup"; 
  private static final String  aLogCleanupJobs    = "LogCleanupJobs"; 

  private static final String  aAllowAddHost     = "AllowAddHost"; 
  private static final String  aLogAddHost       = "LogAddHost"; 

  private static final String  aAllowRemoveHosts = "AllowRemoveHosts"; 
  private static final String  aLogRemoveHosts   = "LogRemoveHosts"; 

  private static final String  aLogModifyHosts     = "LogModifyHosts"; 
  private static final String  aLogResourceSamples = "LogResourceSamples";   

  private static final String  aLogJobAbort   = "LogJobAbort"; 
  private static final String  aLogJobBalk    = "LogJobBalk"; 
  private static final String  aLogJobStart   = "LogJobStart"; 
  private static final String  aLogJobPreempt = "LogJobPreempt"; 
  private static final String  aLogJobFinish  = "LogJobFinish";
 
}
