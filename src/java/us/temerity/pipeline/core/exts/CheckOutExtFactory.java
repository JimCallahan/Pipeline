// $Id: CheckOutExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - O U T   E X T   F A C T O R Y                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after checking-in an individual node.
 */
public 
class CheckOutExtFactory
  extends BaseNodeExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  CheckOutExtFactory() 
  {
    super(null);
  }

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vsn
   *   The checked-in node version to check-out.
   * 
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   * 
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   * 
   */ 
  public 
  CheckOutExtFactory
  (
   NodeID nodeID,
   NodeVersion vsn, 
   CheckOutMode mode,
   CheckOutMethod method
  )      
  {
    super(nodeID);

    pNodeVsn = vsn; 
    pMode    = mode; 
    pMethod  = method;
  }

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   */
  public 
  CheckOutExtFactory
  (
   NodeID nodeID, 
   NodeMod mod
  )      
  {
    super(nodeID);

    pNodeMod = mod; 
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
    return ext.hasPreCheckOutTest();
  }

  /*
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
    ext.preCheckOutTest(pNodeID, pNodeVsn, pMode, pMethod); 
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
    return ext.hasPostCheckOutTask();
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
    PostCheckOutTask task = new PostCheckOutTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostCheckOutTask
    extends BaseMasterTask
  {
    public 
    PostCheckOutTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostCheckOutTask:Node[" + pNodeID.getName() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postCheckOutTask(pNodeID, pNodeMod);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in node version to check-out.
   */ 
  private NodeVersion  pNodeVsn;

  /**
   * The criteria used to determine whether nodes upstream of the root node of the check-out
   * should also be checked-out.
   */ 
  private CheckOutMode  pMode;

  /**
   * The method for creating working area files/links from the checked-in files.
   */ 
  private CheckOutMethod  pMethod;

  /**
   * The working version of the node.
   */ 
  private NodeMod  pNodeMod;

}



