// $Id: RemoveHostsExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O V E   H O S T S   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The test before removing some existing job server hosts and task to perform after removing
 * the hosts. 
 */
public 
class RemoveHostsExtFactory
  implements QueueTestFactory, QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param hostname
   *   The fully resolved names of the hosts.
   */
  public 
  RemoveHostsExtFactory
  (
   TreeSet<String> hostnames
  )      
  {
    pHostnames = hostnames;
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
    return ext.hasPreRemoveHostsTest();
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
    ext.preRemoveHostsTest(pHostnames);
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
    return ext.hasPostRemoveHostsTask();
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
    PostRemoveHostsTask task = new PostRemoveHostsTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostRemoveHostsTask
    extends BaseQueueTask
  {
    public 
    PostRemoveHostsTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostRemoveHostsTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRemoveHostsTask(pHostnames);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the hosts.
   */
  private TreeSet<String>  pHostnames; 

}



