// $Id: RemoveWorkingAreaExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O V E   W O R K I N G   A R E A   E X T   F A C T O R Y                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after removing a new working area.
 */
public 
class RemoveWorkingAreaExtFactory
  extends BaseWorkingAreaExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public 
  RemoveWorkingAreaExtFactory
  (
   String author, 
   String view      
  )      
  {
    super(author, view); 
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
   BaseMasterExt ext
  )
  {
    return ext.hasPreRemoveWorkingAreaTest();
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
   BaseMasterExt ext
  )
    throws PipelineException
  {
    ext.preRemoveWorkingAreaTest(pAuthor, pView);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support post-tasks for this kind of operation.
   */ 
  public boolean 
  hasTask
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.hasPostRemoveWorkingAreaTask();
  }

  /**
   * Remove and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  ) 
  {
    PostRemoveWorkingAreaTask task = new PostRemoveWorkingAreaTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostRemoveWorkingAreaTask
    extends BaseMasterTask
  {
    public 
    PostRemoveWorkingAreaTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostRemoveWorkingAreaTask:[" + pAuthor + "|" + pView + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRemoveWorkingAreaTask(pAuthor, pView);
    }
  }
}



