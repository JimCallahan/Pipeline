// $Id: RenameExtFactory.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N A M E   E X T   F A C T O R Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after renaming a working version of a node 
 * which has never been checked-in.
 */
public 
class RenameExtFactory
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
   * @param pattern
   *   The new fully resolved file pattern.
   * 
   * @param renameFiles 
   *   Should the files associated with the working version be renamed?
   */ 
  public 
  RenameExtFactory
  (
   NodeID nodeID, 
   FilePattern pattern, 
   boolean renameFiles
  )      
  {
    super(nodeID);

    pFilePat     = pattern;
    pRenameFiles = renameFiles; 
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
    return ext.hasPreRenameTest();
  }

  /**
   * Get the requirements to for the pre-operation test. 
   */ 
  public ExtReqs
  getTestReqs
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.getPreRenameTestReqs();
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
    ext.preRenameTest(pNodeID, pFilePat, pRenameFiles);
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
    return ext.hasPostRenameTask();
  }

  /**
   * Get the requirements to for the post-operation task. 
   */ 
  public ExtReqs
  getTaskReqs
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.getPostRenameTaskReqs();
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
    PostRenameTask task = new PostRenameTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostRenameTask
    extends BaseMasterTask
  {
    public 
    PostRenameTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostRenameTask:Node[" + pNodeID.getName() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRenameTask(pNodeID, pFilePat, pRenameFiles);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The new fully resolved file pattern.
   */ 
  private FilePattern  pFilePat; 

  /**
   * Should the files associated with the working version be renamed?
   */ 
  private boolean  pRenameFiles; 

}



