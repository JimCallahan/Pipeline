// $Id: JobStartedExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S T A R T E D   E X T   F A C T O R Y                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after a job starts running.
 */
public 
class JobStartedExtFactory
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
   *   Information about when and where the job was started.
   */ 
  public 
  JobStartedExtFactory
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
    return ext.hasPostJobStartedTask();
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
    PostJobStartedTask task = new PostJobStartedTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostJobStartedTask
    extends BaseQueueTask
  {
    public 
    PostJobStartedTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostJobStartedTask:Job[" + pJob.getJobID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postJobStartedTask(pJob, pInfo);
    }
  }

}



