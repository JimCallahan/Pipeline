// $Id: OfflineExtFactory.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   O F F L I N E   E X T   F A C T O R Y                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after removing the repository files associated 
 * with the given checked-in versions.
 */
public 
class OfflineExtFactory
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
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
  public 
  OfflineExtFactory
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
    return ext.hasPreOfflineTest();
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
    return ext.getPreOfflineTestReqs();
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
    ext.preOfflineTest(pVersions);
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
    return ext.hasPostOfflineTask();
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
    return ext.getPostOfflineTaskReqs();
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
    PostOfflineTask task = new PostOfflineTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostOfflineTask
    extends BaseMasterTask
  {
    public 
    PostOfflineTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostOfflineTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postOfflineTask(pVersions); 
    }
  }

}



