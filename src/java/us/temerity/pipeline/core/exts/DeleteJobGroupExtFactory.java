// $Id: DeleteJobGroupExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D E L E T E   J O B   G R O U P   E X T   F A C T O R Y                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform after a completed job group has been marked for removal.
 */
public 
class DeleteJobGroupExtFactory
  implements QueueTestFactory, QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param user
   *   The name of the user attempting to delete the job group.
   * 
   * @param distribution
   *   Get the percentage of jobs in the group associated with a given JobState.
   * 
   * @param group
   *   The completed job group.
   */ 
  public 
  DeleteJobGroupExtFactory
  (
   String user, 
   TreeMap<JobState,Double> distribution, 
   QueueJobGroup group
  )      
  {
    pUser  = user; 
    pDist  = distribution;
    pGroup = group; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support pre-tests for this type of operation.
   */ 
  public boolean 
  hasTest
  (   
   BaseQueueExt ext
  )
  {
    return ext.hasPreDeleteJobGroupTest();
  }

  /**
   * Perform the pre-test passed for this type of operation.
   * 
   * @throws PipelineException
   *   If the test fails.
   */ 
  public void
  performTest
  (   
   BaseQueueExt ext
  )
    throws PipelineException
  {
    ext.preDeleteJobGroupTest(pUser, pDist, pGroup);
  }


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
    return ext.hasPostDeleteJobGroupTask();
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
    PostDeleteJobGroupTask task = new PostDeleteJobGroupTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostDeleteJobGroupTask
    extends BaseQueueTask
  {
    public 
    PostDeleteJobGroupTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostDeleteJobGroupTask:JobGroup[" + pGroup.getGroupID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postDeleteJobGroupTask(pGroup);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user attempting to delete the job group.
   */
  private String  pUser; 

  /**
   * The percentage of jobs in the group associated with a given JobState.
   */ 
  private TreeMap<JobState,Double> pDist; 

  /**
   * The completed job group.
   */
  private QueueJobGroup  pGroup;

}



