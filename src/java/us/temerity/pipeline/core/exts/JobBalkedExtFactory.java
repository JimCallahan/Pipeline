// $Id: JobBalkedExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   B A L K E D   E X T   F A C T O R Y                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after a job is unable to start (balked).
 */
public 
class JobBalkedExtFactory
  extends BaseJobExtFactory
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
   * @param hostname
   *   The name of the host running the unresponsive job manager.
   */ 
  public 
  JobBalkedExtFactory
  (
   QueueJob job, 
   String hostname
  )      
  {
    super(job);

    pHostname = hostname; 
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
    return ext.hasPostJobBalkedTask();
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
    PostJobBalkedTask task = new PostJobBalkedTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostJobBalkedTask
    extends BaseQueueTask
  {
    public 
    PostJobBalkedTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostJobBalkedTask:Job[" + pJob.getJobID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postJobBalkedTask(pJob, pHostname);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the host running the unresponsive job manager.
   */
  private String  pHostname; 

}



