// $Id: DeleteExtFactory.java,v 1.3 2007/06/22 01:26:09 jim Exp $

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
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames()
  {
    LinkedList<String> names = new LinkedList<String>();
    names.add(pName);
    
    return names;
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    return pWorkUser; 
  }


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
   * Get the requirements to for the pre-operation test. 
   */ 
  public ExtReqs
  getTestReqs
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.getPreDeleteTestReqs();
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
   * Get the requirements to for the post-operation task. 
   */ 
  public ExtReqs
  getTaskReqs
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.getPostDeleteTaskReqs();
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



