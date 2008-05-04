// $Id: VouchExtFactory.java,v 1.1 2008/05/04 00:40:17 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V O U C H   E X T   F A C T O R Y                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after vouching for working area files associated 
 * with a node.
 */
public 
class VouchExtFactory
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
   */ 
  public 
  VouchExtFactory
  (
   NodeID nodeID
  ) 
  {
    super(nodeID);
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
    return ext.hasPreVouchTest();
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
    ext.preVouchTest(pNodeID); 
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
    return ext.hasPostVouchTask();
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
    PostVouchTask task = new PostVouchTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostVouchTask
    extends BaseMasterTask
  {
    public 
    PostVouchTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostVouchTask:Node[" + pNodeID.getName() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postVouchTask(pNodeID); 
    }
  }

}



