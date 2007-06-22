// $Id: DenyRestoreExtFactory.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D E N Y   R E S T O R E   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after denying to restore the given set of 
 * checked-in versions.
 */
public 
class DenyRestoreExtFactory
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
  DenyRestoreExtFactory
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
    return ext.hasPreDenyRestoreTest();
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
    return ext.getPreDenyRestoreTestReqs();
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
    ext.preDenyRestoreTest(pVersions);
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
    return ext.hasPostDenyRestoreTask();
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
    return ext.getPostDenyRestoreTaskReqs();
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
    PostDenyRestoreTask task = new PostDenyRestoreTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostDenyRestoreTask
    extends BaseMasterTask
  {
    public 
    PostDenyRestoreTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostDenyRestoreTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postDenyRestoreTask(pVersions); 
    }
  }

}



