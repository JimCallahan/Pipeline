// $Id: LockExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L O C K   E X T   F A C T O R Y                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after locking the working version of a node to 
 * a specific checked-in version.
 */
public 
class LockExtFactory
  extends BaseNodeExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version is 
   *   being locked.
   */ 
  public 
  LockExtFactory
  (
   NodeID nodeID,
   VersionID vid
  ) 
  {
    super(nodeID);

    pVersionID = vid; 
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
    return ext.hasPreLockTest();
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
    ext.preLockTest(pNodeID, pVersionID);
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
    return ext.hasPostLockTask();
  }

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  ) 
  {
    PostLockTask task = new PostLockTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostLockTask
    extends BaseMasterTask
  {
    public 
    PostLockTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostLockTask:Node[" + pNodeID.getName() + " v" + pVersionID + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postLockTask(pNodeID, pVersionID);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the checked-in version to which the working version is 
   * being locked.
   */ 
  private VersionID  pVersionID; 


}



