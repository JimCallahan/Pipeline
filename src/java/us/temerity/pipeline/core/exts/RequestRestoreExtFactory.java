// $Id: RequestRestoreExtFactory.java,v 1.3 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E Q U E S T   R E S T O R E   E X T   F A C T O R Y                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after requesting to restore the given set of 
 * checked-in versions.
 */
public 
class RequestRestoreExtFactory
  extends BaseVersionsExtFactory
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
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */ 
  public 
  RequestRestoreExtFactory
  (
   String workUser, 
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {
    super(workUser, versions); 
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
    return ext.hasPreRequestRestoreTest();
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
    ext.preRequestRestoreTest(pVersions);
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
    return ext.hasPostRequestRestoreTask();
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
    PostRequestRestoreTask task = new PostRequestRestoreTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostRequestRestoreTask
    extends BaseMasterTask
  {
    public 
    PostRequestRestoreTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostRequestRestoreTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRequestRestoreTask(pVersions); 
    }
  }

}



