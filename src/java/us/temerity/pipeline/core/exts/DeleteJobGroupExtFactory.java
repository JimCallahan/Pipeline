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
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param group
   *   The completed job group.
   */ 
  public 
  DeleteJobGroupExtFactory
  (
   QueueJobGroup group
  )      
  {
    pGroup = group; 
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
   * The completed job group.
   */
  private QueueJobGroup  pGroup;

}



