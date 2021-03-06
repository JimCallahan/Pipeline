// $Id: RenumberExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N U M B E R   E X T   F A C T O R Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after renumbering the frame ranges of the file 
 * sequences associated with a node.
 */
public 
class RenumberExtFactory
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
   * @param range 
   *   The new frame range.
   * 
   * @param removeFiles 
   *   Whether to remove files from the old frame range which are no longer part of the new 
   *   frame range.
   */ 
  public 
  RenumberExtFactory
  (
   NodeID nodeID, 
   FrameRange range, 
   boolean removeFiles
  )      
  {
    super(nodeID);

    pFrameRange  = range;
    pRemoveFiles = removeFiles; 
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
    return ext.hasPreRenumberTest();
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
    ext.preRenumberTest(pNodeID, pFrameRange, pRemoveFiles); 
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
    return ext.hasPostRenumberTask();
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
    PostRenumberTask task = new PostRenumberTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostRenumberTask
    extends BaseMasterTask
  {
    public 
    PostRenumberTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostRenumberTask:Node[" + pNodeID.getName() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRenumberTask(pNodeID, pFrameRange, pRemoveFiles); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The new frame range.
   */ 
  private FrameRange  pFrameRange; 

  /**
   * Whether to remove files from the old frame range which are no longer part of the new 
   * frame range.
   */ 
  private boolean  pRemoveFiles;

}



