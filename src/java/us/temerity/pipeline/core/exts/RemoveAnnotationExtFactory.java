// $Id: RemoveAnnotationExtFactory.java,v 1.1 2007/08/20 04:43:15 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O V E   A N N O T A T I O N   E X T   F A C T O R Y                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after removing an annotation to a node.
 */
public 
class RemoveAnnotationExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   */ 
  public 
  RemoveAnnotationExtFactory
  (
   String nname, 
   String aname
  ) 
  {
    pNodeName  = nname; 
    pAnnotName = aname; 
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
    return ext.hasPreRemoveAnnotationTest();
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
    ext.preRemoveAnnotationTest(pNodeName, pAnnotName); 
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
    return ext.hasPostRemoveAnnotationTask();
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
    PostRemoveAnnotationTask task = new PostRemoveAnnotationTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostRemoveAnnotationTask
    extends BaseMasterTask
  {
    public 
    PostRemoveAnnotationTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostRemoveAnnotationTask: " + pNodeName + "[" + pAnnotName + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postRemoveAnnotationTask(pNodeName, pAnnotName); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */ 
  private String pNodeName; 

  /**
   * The name of the annotation. 
   */ 
  private String pAnnotName;

}



