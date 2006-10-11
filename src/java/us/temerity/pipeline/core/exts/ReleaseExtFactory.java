// $Id: ReleaseExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L E A S E   E X T   F A C T O R Y                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after releasing the working versions of nodes 
 * and optionally removing the associated working area files. 
 */
public 
class ReleaseExtFactory
  extends BaseWorkingAreaExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param names 
   *   The fully resolved names of the nodes to release.
   * 
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   */ 
  public 
  ReleaseExtFactory
  (
   String author, 
   String view, 
   TreeSet<String> names, 
   boolean removeFiles
  )      
  {
    super(author, view);

    pNodeNames   = names;
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
    return ext.hasPreReleaseTest();
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
    ext.preReleaseTest(pAuthor, pView, pNodeNames, pRemoveFiles);
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
    return ext.hasPostReleaseTask();
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
    PostReleaseTask task = new PostReleaseTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostReleaseTask
    extends BaseMasterTask
  {
    public 
    PostReleaseTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostReleaseTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postReleaseTask(pAuthor, pView, pNodeNames, pRemoveFiles);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the nodes to release.
   */ 
  private TreeSet<String>  pNodeNames;

  /**
   * Should the files associated with the working version be deleted?
   */ 
  private boolean  pRemoveFiles;

}



