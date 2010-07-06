// $Id: VouchExtFactory.java,v 1.1 2008/05/04 00:40:17 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T I N G   T E S T   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before a user is allowed to edit a node.
 */
public 
class EditingTestExtFactory
  extends BaseNodeExtFactory
  implements MasterTestFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param editorID
   *   The unique identifier of the Editor plugin which will be run.
   *
   * @param hostname
   *   The full name of the host on which the Editor will be run.
   *
   * @param imposter
   *   The name of the user impersonating the owner of the node to be edited or
   *   <CODE>null<CODE> if the editing user is the node's owner.
   */ 
  public 
  EditingTestExtFactory
  (
   NodeID nodeID, 
   PluginID editorID, 
   String hostname, 
   String imposter
  ) 
  {
    super(nodeID);
    
    pEditorID = editorID; 
    pHostname = hostname; 
    pImposter = imposter;
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
    return ext.hasPreEditingStartedTest(); 
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
    ext.preEditingStartedTest(pNodeID, pEditorID, pHostname, pImposter); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique identifier of the Editor plugin which will be run.
   */ 
  private PluginID  pEditorID; 

  /**
   * The full name of the host on which the Editor will be run.
   */ 
  private String  pHostname; 

  /**
   * The name of the user impersonating the owner of the node to be edited or
   * <CODE>null<CODE> if the editing user is the node's owner.
   */ 
  private String  pImposter; 

}


