// $Id: BackupDatabaseExtFactory.java,v 1.3 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A C K U P   D A T A B A S E   E X T   F A C T O R Y                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after creating a database backup file.
 */
public 
class BackupDatabaseExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param file
   *   The name of the backup file.
   */ 
  public 
  BackupDatabaseExtFactory
  (
   File file
  ) 
  {
    pFile = file; 
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
    return new LinkedList<String>();
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    return null;
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
    return ext.hasPreBackupDatabaseTest();
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
    return new ExtReqs();
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
    ext.preBackupDatabaseTest(pFile); 
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
    return ext.hasPostBackupDatabaseTask();
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
    return new ExtReqs();
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
    PostBackupDatabaseTask task = new PostBackupDatabaseTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostBackupDatabaseTask
    extends BaseMasterTask
  {
    public 
    PostBackupDatabaseTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostBackupDatabaseTask:File[" + pFile + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postBackupDatabaseTask(pFile); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the backup file.
   */ 
  private File  pFile; 

}



