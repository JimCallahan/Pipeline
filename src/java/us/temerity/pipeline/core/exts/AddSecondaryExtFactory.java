// $Id: AddSecondaryExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D   S E C O N D A R Y   E X T   F A C T O R Y                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after adding a secondary file sequence to a 
 * working version.
 */
public 
class AddSecondaryExtFactory
  extends BaseSecondaryExtFactory
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
   * @param fseq
   *   The secondary file sequence to add.
   */ 
  public 
  AddSecondaryExtFactory
  (
   NodeID nodeID, 
   FileSeq fseq
  )      
  {
    super(nodeID, fseq);
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
    return ext.hasPreAddSecondaryTest();
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
    ext.preAddSecondaryTest(pNodeID, pFileSeq);
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
    return ext.hasPostAddSecondaryTask();
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
    PostAddSecondaryTask task = new PostAddSecondaryTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostAddSecondaryTask
    extends BaseMasterTask
  {
    public 
    PostAddSecondaryTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostAddSecondaryTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postAddSecondaryTask(pNodeID, pFileSeq);
    }
  }
}



