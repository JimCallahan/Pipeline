// $Id: JobFinishedExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   F I N I S H E D   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after a job starts running.
 */
public 
class JobFinishedExtFactory
  extends BaseJobInfoExtFactory
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param job
   *   The job specification.
   * 
   * @param info
   *   Information about when and where the job was finished.
   */ 
  public 
  JobFinishedExtFactory
  (
   QueueJob job, 
   QueueJobInfo info
  )      
  {
    super(job, info); 
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
    return ext.hasPostJobFinishedTask();
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
    PostJobFinishedTask task = new PostJobFinishedTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private 
  class PostJobFinishedTask
    extends BaseQueueTask
  {
    public 
    PostJobFinishedTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostJobFinishedTask:Job[" + pJob.getJobID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postJobFinishedTask(pJob, pInfo);
    }
  }

}



