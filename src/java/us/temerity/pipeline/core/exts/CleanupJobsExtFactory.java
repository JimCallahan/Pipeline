// $Id: CleanupJobsExtFactory.java,v 1.2 2007/12/16 06:28:42 jesse Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C L E A N U P   J O B S   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after the job garbage collector has cleaned-up jobs no longer
 * referenced by any remaining job group.
 */
public 
class CleanupJobsExtFactory
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param jobs
   *   The completed jobs indexed by job ID.
   * 
   * @param infos
   *   Information about when and where the job was executed indexed by job ID.
   */ 
  public 
  CleanupJobsExtFactory
  (
   TreeMap<Long,QueueJob> jobs,
   TreeMap<Long,QueueJobInfo> infos
  )      
  {
    pInfos = infos; 
    pJobs  = new TreeMap<Long, QueueJob>();
    for (Long id : jobs.keySet())
      pJobs.put(id, jobs.get(id).queryOnlyCopy());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support post-tasks for this kind of operation.
   */ 
  public boolean 
  hasTask
  (   
   BaseQueueExt ext
  ) 
  {
    return ext.hasPostCleanupJobsTask();
  }

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   QueueExtensionConfig config, 
   BaseQueueExt ext
  ) 
  {
    PostCleanupJobsTask task = new PostCleanupJobsTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostCleanupJobsTask
    extends BaseQueueTask
  {
    public 
    PostCleanupJobsTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostCleanupJobsTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postCleanupJobsTask(pJobs, pInfos); 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The completed jobs indexed by job ID.
   */
  private TreeMap<Long,QueueJob>  pJobs;

  /**
   * Information about when and where the job was executed indexed by job ID.
   */
  private TreeMap<Long,QueueJobInfo>  pInfos; 

}



