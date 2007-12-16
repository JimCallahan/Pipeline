// $Id: SubmitJobsExtFactory.java,v 1.2 2007/12/16 06:28:42 jesse Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B M I T   J O B S   E X T   F A C T O R Y                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after new jobs are submitted to the queue.
 */
public 
class SubmitJobsExtFactory
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param group
   *   The queue job group.
   * 
   * @param jobs
   *   The submitted jobs indexed by job ID.
   */ 
  public 
  SubmitJobsExtFactory
  (
   QueueJobGroup group,
   TreeMap<Long,QueueJob> jobs
  )      
  {
    pGroup = group;
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
    return ext.hasPostSubmitJobsTask();
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
    PostSubmitJobsTask task = new PostSubmitJobsTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostSubmitJobsTask
    extends BaseQueueTask
  {
    public 
    PostSubmitJobsTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostSubmitJobsTask:JobGroup[" + pGroup.getGroupID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postSubmitJobsTask(pGroup, pJobs); 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue job group.
   */
  private QueueJobGroup  pGroup; 

  /**
   * The submitted jobs indexed by job ID.
   */
  private TreeMap<Long,QueueJob>  pJobs;

}



