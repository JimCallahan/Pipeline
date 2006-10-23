// $Id: BaseQueueExt.java,v 1.2 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   Q U E U E   E X T E N S I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Queue Manager extension plugins. <P>
 * 
 * This class provides methods to be overloaded by subclasses in order to provide additional
 * functionality for the Queue Manager daemon.  <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public 
class BaseQueueExt
  extends BaseExt
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public
  BaseQueueExt() 
  {
    super();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the extension.
   * 
   * @param vid
   *   The extension plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the extension.
   */ 
  protected
  BaseQueueExt
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseQueueExt
  (
   BaseQueueExt extension
  ) 
  {
    super(extension); 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  public PluginType
  getPluginType()
  {
    return PluginType.QueueExt;
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
    return false;
  }

  /**
   * The task to perform after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually.
   */ 
  public void 
  postEnableTask()
  {}
 

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
    return false;
  }

  /**
   * The task to perform after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  public void 
  preDisableTask()
  {}
 


  /*----------------------------------------------------------------------------------------*/
  /*  S E R V E R   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after new jobs are submitted to the queue.
   */  
  public boolean
  hasPostSubmitJobsTask() 
  {
    return false;
  }

  /**
   * The task to perform after new jobs are submitted to the queue.
   * 
   * @param group
   *   The queue job group.
   * 
   * @param jobs
   *   The submitted jobs indexed by job ID.
   */  
  public void
  postSubmitJobsTask
  (
   QueueJobGroup group,
   TreeMap<Long,QueueJob> jobs
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a task after a completed job group has been marked for removal.
   */  
  public boolean
  hasPostDeleteJobGroupTask() 
  {
    return false;
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
  {}


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a task after the job garbage collector has cleaned-up jobs no longer
   * referenced by any remaining job group.<P> 
   * 
   * This is a good way to collect long term information about all jobs while having a 
   * very low impact on the queue manager.  All jobs are eventually cleaned up so this will 
   * provide the same information as the {@link #postJobFinishedTask}, but operates on large 
   * batches of jobs instead of after each individual job.  
   */  
  public boolean
  hasPostCleanupJobsTask() 
  {
    return false;
  }

  /**
   * The task to perform after the job garbage collector has cleaned-up jobs no longer
   * referenced by any remaining job group.<P> 
   * 
   * This is a good way to collect long term information about all jobs while having a 
   * very low impact on the queue manager.  All jobs are eventually cleaned up so this will 
   * provide the same information as the {@link #postJobFinishedTask}, but operates on large 
   * batches of jobs instead of after each individual job.  
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
  {}


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to test before adding a new job server host.
   */  
  public boolean
  hasPreAddHostTest() 
  {
    return false;
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
  {}


  /**
   * Whether to run a task after adding a new job server host.
   */  
  public boolean
  hasPostAddHostTask() 
  {
    return false;
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
  {}


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to test before removing some existing job server hosts.
   */  
  public boolean
  hasPreRemoveHostsTest() 
  {
    return false;
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
  {}


  /**
   * Whether to run a task after removing some existing job server hosts.
   */  
  public boolean
  hasPostRemoveHostsTask() 
  {
    return false;
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
  {}
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after modifying host status or properties.
   */  
  public boolean
  hasPostModifyHostsTask() 
  {
    return false;
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
  {}
  
  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after writing job server dynamic resource samples to disk.
   */  
  public boolean
  hasPostResourceSamplesTask() 
  {
    return false;
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
  {}
  
 


  /*----------------------------------------------------------------------------------------*/
  /*  D I S P A T C H E R   O P S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task if job has been aborted (cancelled). <P> 
   * 
   * Jobs which where already running when aborted will also invoke the post-finish task.
   * If a job is aborted before it began execution, then only this method will be called.
   */  
  public boolean
  hasPostJobAbortedTask() 
  {
    return false;
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
  {}


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
    return false;
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
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a job starts running.
   */  
  public boolean
  hasPostJobStartedTask() 
  {
    return false;
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
  {}
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a job is manually preempted.
   */  
  public boolean
  hasPostJobPreemptedTask() 
  {
    return false;
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
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after a job finishes running.
   */  
  public boolean
  hasPostJobFinishedTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after a job finishes running. <P> 
   * 
   * Jobs which are automatically requeued (balked or manually preempted) will not 
   * invoke this task until they have been restarted and exit normally.  Jobs invoking this
   * task will have a Aborted, Finished or Failed status.
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
  {}
  


  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseQueueExt)) {
      BaseQueueExt extension = (BaseQueueExt) obj;
      if(super.equals(obj) && 
	 equalParams(extension)) 
	return true;
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return (BaseQueueExt) super.clone();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7314267444241915587L;


}



