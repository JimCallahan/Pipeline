// $Id: QueueStatsExt.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.plugin.v2_1_1;

import java.io.*;
import java.text.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S T A T S    E X T                                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * Inserts queue related information into an SQL database.
 */
public class 
QueueStatsExt
  extends BaseQueueExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  QueueStatsExt()
  {
    super("QueueStats", new VersionID("2.1.1"), "Temerity",
	  "Inserts queue related information into an SQL database."); 

    /* server configuration */ 
    {
      {
	ExtensionParam param = 
	  new StringExtensionParam
	  (aDatabaseHostname, 
	   "The hostname running the SQL database server.", 
	   "localhost");
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aDatabasePort, 
	   "The network port to use to contact the SQL database server.",
	   3306);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new StringExtensionParam
	  (aDatabaseUser, 
	   "The user name to use when connecting to the SQL database.",
	   "pipeline");
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new StringExtensionParam
	  (aDatabasePassword, 
	   "The password to use when connecting to the SQL database.",
	   null);
	addParam(param);
      }
    }

    /* statistics */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aCompletedJobStats, 
	   "Whether to record data about each completed job.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aJobServerStats, 
	   "Whether to record data about the dynamic resources of each job server.", 
	   true);
	addParam(param);
      }
    }

    {  
      LayoutGroup layout = new LayoutGroup(true); 

      {
	LayoutGroup sub = new LayoutGroup
	  ("ServerConfiguration", 
	   "The specification for establishing a connection to the SQL server.", true); 
	sub.addEntry(aDatabaseHostname);
	sub.addEntry(aDatabasePort);
	sub.addSeparator();
	sub.addEntry(aDatabaseUser);
	sub.addEntry(aDatabasePassword);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("Statistics", "Controls over the level of statistics recorded.", true);
	sub.addEntry(aCompletedJobStats);
	sub.addEntry(aJobServerStats);

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
    try {
      String hostname = (String) getParamValue(aDatabaseHostname);
      if((hostname == null) || (hostname.length() == 0))
	throw new PipelineException("No Database Hostname was specified!");
      
      Integer port = (Integer) getParamValue(aDatabasePort);
      if(port == null)
	throw new PipelineException("No Database Port was specified!");
      if(port <= 0)
	throw new PipelineException("Invalid Database Port (" + port + ")!");
      
      String user = (String) getParamValue(aDatabaseUser);
      if((user == null) || (user.length() == 0))
	throw new PipelineException("No Database User was specified!");
      
      String password = (String) getParamValue(aDatabasePassword);
      if((password == null) || (password.length() == 0))
	throw new PipelineException("No Database Password was specified!");
      
      sDb.connect(hostname, port, user, password);
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 getFullMessage(ex));
    }
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
    try {
      sDb.disconnect();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 getFullMessage(ex));
    }
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*  S E R V E R   O P S                                                                   */
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
    return isParamTrue(aCompletedJobStats); 
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
    try {
      sDb.insertJobs(jobs, infos);
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 getFullMessage(ex));
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after writing job server dynamic resource samples to disk.
   */  
  public boolean
  hasPostResourceSamplesTask() 
  {
    return isParamTrue(aJobServerStats);
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
   TreeMap<String,ResourceSampleBlock> samples
  ) 
  {
    try {
      sDb.insertResourceSamples(samples); 
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 getFullMessage(ex));
    }    
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after modifying host status or properties.
   */  
  public boolean
  hasPostModifyHostsTask() 
  {
    return (isParamTrue(aCompletedJobStats) || isParamTrue(aJobServerStats));
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
    try {
      sDb.modifyHosts(hosts); 
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 getFullMessage(ex));
    }        
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

  private static final long serialVersionUID = -364826528098694531L;


  private static final String  aDatabaseHostname = "DatabaseHostname"; 
  private static final String  aDatabasePort     = "DatabasePort"; 
  private static final String  aDatabaseFlavor   = "DatabaseFlavor"; 
  private static final String  aDatabaseUser     = "DatabaseUser"; 
  private static final String  aDatabasePassword = "DatabasePassword"; 

  private static final String  aCompletedJobStats = "CompletedJobStats"; 
  private static final String  aJobServerStats    = "JobServerStats";


  /**
   * The shared database connection.
   */ 
  private static QueueStatsDb sDb = new QueueStatsDb();

}
