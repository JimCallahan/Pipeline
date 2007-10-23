// $Id: PackExtFactory.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K   E X T   F A C T O R Y                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after creating a new node bundle by transcribing 
 * a tree of working area nodes. 
 */
public 
class PackExtFactory
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
   *   The unique working version identifier of the root node.
   */ 
  public 
  PackExtFactory
  (
   NodeID nodeID
  ) 
  {
    super(nodeID);
  }

  /**
   * Construct a task factory.
   * 
   * @param bundle
   *   The node bundle metadata. 
   */ 
  public 
  PackExtFactory
  (
   NodeBundle bundle
  ) 
  {
    super(bundle.getRootNodeID());
    pBundle = bundle;
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
    return ext.hasPrePackTest();
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
    ext.prePackTest(pNodeID); 
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
    return ext.hasPostPackTask();
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
    PostPackTask task = new PostPackTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostPackTask
    extends BaseMasterTask
  {
    public 
    PostPackTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostPackTask:RootNode[" + pNodeID.getName() + "]", 
	    config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postPackTask(pBundle);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The node bundle metadata. 
   */ 
  private NodeBundle  pBundle; 

}



