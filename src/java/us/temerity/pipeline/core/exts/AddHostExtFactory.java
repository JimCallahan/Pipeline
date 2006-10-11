// $Id: AddHostExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D   H O S T   E X T   F A C T O R Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The test before adding a new job server host and task to perform after adding the host.
 */
public 
class AddHostExtFactory
  implements QueueTestFactory, QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   */
  public 
  AddHostExtFactory
  (
   String hostname   
  )      
  {
    pHostname = hostname;
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
    return ext.hasPreAddHostTest();
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
    ext.preAddHostTest(pHostname);
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
    return ext.hasPostAddHostTask();
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
    PostAddHostTask task = new PostAddHostTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-operation task.
   */
  private
  class PostAddHostTask
    extends BaseQueueTask
  {
    public 
    PostAddHostTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostAddHostTask:Host[" + pHostname + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postAddHostTask(pHostname);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the host.
   */
  private String  pHostname; 

}



