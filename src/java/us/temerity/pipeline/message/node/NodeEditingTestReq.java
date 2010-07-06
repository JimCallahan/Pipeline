// $Id: NodeEditingStartedReq.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.event.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E D I T I N G  T E S T   R E Q                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * Test whether the current user should be allowed to edit the given node.
 */
public
class NodeEditingTestReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param editor 
   *   The Editor plugin instance. 
   * 
   * @param imposter
   *   The name of the user impersonating the owner of the node to be edited or
   *   <CODE>null<CODE> if the editing user is the node's owner.
   */
  public
  NodeEditingTestReq
  (
   NodeID nodeID,  
   PluginID editorID 
  )
  { 
    if(nodeID == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pNodeID = nodeID;

    if(editorID == null) 
      throw new IllegalArgumentException("The editor ID cannot be (null)!");
    pEditorID = editorID;

    pImposter = null; 
    if(!pNodeID.getAuthor().equals(getRequestor()))
      pImposter = getRequestor();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique working version identifier. 
   */ 
  public NodeID
  getNodeID()
  {
    return pNodeID; 
  }
  
  /**
   * Get the Editor plugin instance. 
   */ 
  public PluginID
  getEditorID()
  {
    return pEditorID; 
  }
  
  /**
   * Get the name of the user impersonating the owner of the node to be edited or
   * <CODE>null<CODE> if the editing user is the node's owner.
   */ 
  public String
  getImposter()
  {
    return pImposter; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7809774001577307972L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier. 
   */ 
  private NodeID  pNodeID;

  /**
   * The unique identifier of the editor plugin.
   */ 
  private PluginID  pEditorID; 

  /**
   * The name of the user impersonating the owner of the node to be edited or
   * <CODE>null<CODE> if the editing user is the node's owner.
   */ 
  private String pImposter; 
  
}
  
