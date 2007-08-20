// $Id: SetWorkGroupsExtFactory.java,v 1.1 2007/08/20 04:43:15 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E T   W O R K   G R O U P S   E X T   F A C T O R Y                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after setting the administrative work groups. 
 */
public 
class SetWorkGroupsExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param groups 
   *   The work groups.
   */ 
  public 
  SetWorkGroupsExtFactory
  (
   WorkGroups groups   
  ) 
  {
    pWorkGroups = groups; 
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
    return ext.hasPreSetWorkGroupsTest();
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
    ext.preSetWorkGroupsTest(pWorkGroups); 
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
    return ext.hasPostSetWorkGroupsTask();
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
    PostSetWorkGroupsTask task = new PostSetWorkGroupsTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostSetWorkGroupsTask
    extends BaseMasterTask
  {
    public 
    PostSetWorkGroupsTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostSetWorkGroupsTask:", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postSetWorkGroupsTask(pWorkGroups); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups. 
   */ 
  private WorkGroups  pWorkGroups; 

}



