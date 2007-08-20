// $Id: AddAnnotationExtFactory.java,v 1.1 2007/08/20 04:43:15 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D   A N N O T A T I O N   E X T   F A C T O R Y                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after adding an annotation to a node.
 */
public 
class AddAnnotationExtFactory
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
   * 
   * @param annot 
   *   The new node annotation to add.
   */ 
  public 
  AddAnnotationExtFactory
  (
   String nname, 
   String aname, 
   BaseAnnotation annot 
  ) 
  {
    pNodeName   = nname; 
    pAnnotName  = aname; 
    pAnnotation = annot; 
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
    return ext.hasPreAddAnnotationTest();
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
    ext.preAddAnnotationTest(pNodeName, pAnnotName, pAnnotation); 
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
    return ext.hasPostAddAnnotationTask();
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
    PostAddAnnotationTask task = new PostAddAnnotationTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostAddAnnotationTask
    extends BaseMasterTask
  {
    public 
    PostAddAnnotationTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostAddAnnotationTask: " + pNodeName + "[" + pAnnotName + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postAddAnnotationTask(pNodeName, pAnnotName, pAnnotation); 
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

  /**
   * The new node annotation to add.
   */ 
  private BaseAnnotation pAnnotation;

}



