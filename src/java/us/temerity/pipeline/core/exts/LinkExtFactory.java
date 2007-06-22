// $Id: LinkExtFactory.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   E X T   F A C T O R Y                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after creating or modifing an existing link 
 * between the working node versions.
 */
public 
class LinkExtFactory
  extends BaseLinkExtFactory
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
   * @param target 
   *   The fully resolved name of the downstream node to connect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to connect.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @param relationship 
   *   The nature of the relationship between files associated with the source and 
   *   target nodes. 
   * 
   * @param offset 
   *   The frame index offset.
   */ 
  public 
  LinkExtFactory
  (
   String author, 
   String view, 
   String target, 
   String source,
   LinkPolicy policy,
   LinkRelationship relationship,  
   Integer offset  
  )      
  {
    super(author, view, target, source); 

    pPolicy = policy; 
    pRelationship = relationship; 
    pFrameOffset = offset; 
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
    return ext.hasPreLinkTest();
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
    return ext.getPreLinkTestReqs();
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
    ext.preLinkTest(pAuthor, pView, pTarget, pSource, pPolicy, pRelationship, pFrameOffset);
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
    return ext.hasPostLinkTask();
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
    return ext.getPostLinkTaskReqs();
  }

  /**
   * Remove and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  ) 
  {
    PostLinkTask task = new PostLinkTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostLinkTask
    extends BaseMasterTask
  {
    public 
    PostLinkTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostLinkTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postLinkTask(pAuthor, pView, pTarget, pSource, 
			      pPolicy, pRelationship, pFrameOffset);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The node state propogation policy.
   */ 
  private LinkPolicy  pPolicy;

  /**
   * The nature of the relationship between files associated with the source and 
   */ 
  private LinkRelationship  pRelationship;
  
  /**
   * The frame index offset.
   */ 
  private Integer  pFrameOffset; 

}



