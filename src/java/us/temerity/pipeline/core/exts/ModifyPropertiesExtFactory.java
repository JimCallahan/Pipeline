// $Id: ModifyPropertiesExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M O D I F Y   P R O P E R T I E S   E X T   F A C T O R Y                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after modifying the properties of a working 
 * version of a node.
 */
public 
class ModifyPropertiesExtFactory
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
   * @param mod
   *   The working version containing the node property information to copy.
   */ 
  public 
  ModifyPropertiesExtFactory
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
    return ext.hasPreModifyPropertiesTest();
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
    ext.preModifyPropertiesTest(pNodeID, pNodeMod);
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
    return ext.hasPostModifyPropertiesTask();
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
    PostModifyPropertiesTask task = new PostModifyPropertiesTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostModifyPropertiesTask
    extends BaseMasterTask
  {
    public 
    PostModifyPropertiesTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostModifyPropertiesTask:NodeID[" + pNodeMod + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postModifyPropertiesTask(pNodeID, pNodeMod);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The working version containing the node property information to copy.
   */ 
  protected NodeMod  pNodeMod; 
}



