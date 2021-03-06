// $Id: DeleteExtFactory.java,v 1.4 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D E L E T E   E X T   F A C T O R Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after deleting all working and checked-in 
 * versions of a node and optionally remove all associated working area files.  
 */
public 
class DeleteExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param workUser
   *   The name of the user performing the operation.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   */ 
  public 
  DeleteExtFactory
  (
   String workUser, 
   String name, 
   boolean removeFiles
  )      
  {
    pWorkUser    = workUser; 
    pName        = name;
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
    return ext.hasPreDeleteTest();
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
    ext.preDeleteTest(pName, pRemoveFiles);
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
    return ext.hasPostDeleteTask();
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
    PostDeleteTask task = new PostDeleteTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostDeleteTask
    extends BaseMasterTask
  {
    public 
    PostDeleteTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostDeleteTask:Node[" + pName + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postDeleteTask(pName, pRemoveFiles);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user performing the operation.
   */ 
  protected String pWorkUser; 

  /**
   * The fully resolved name of the node.
   */ 
  private String  pName; 

  /**
   * Should the files associated with the working version be deleted?
   */ 
  private boolean  pRemoveFiles;

}



