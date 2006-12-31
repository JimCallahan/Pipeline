// $Id: NodeEditingStartedReq.java,v 1.1 2006/12/31 20:44:53 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.event.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E D I T I N G   S T A R T E D   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Signal that an Editor plugin has started editing files associated with the 
 * given working version of a node.
 */
public
class NodeEditingStartedReq
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
   */
  public
  NodeEditingStartedReq
  (
   NodeID nodeID,  
   BaseEditor editor
  )
  { 
    if(nodeID == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pNodeID = nodeID;

    if(editor == null) 
      throw new IllegalArgumentException("The editor cannot be (null)!");
    pEditorName      = editor.getName();
    pEditorVersionID = editor.getVersionID(); 
    pEditorVendor    = editor.getVendor();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editing event.
   */ 
  public EditedNodeEvent
  getEvent
  (
   String hostname
  ) 
  {
    String imposter = null;
    if(!pNodeID.getAuthor().equals(getRequestor()))
      imposter = getRequestor();
    
    return new EditedNodeEvent(new Date(), null, pNodeID, 
			       pEditorName, pEditorVersionID, pEditorVendor, 
			       hostname, imposter); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8866654831934854967L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier. 
   */ 
  private NodeID  pNodeID;

  /**
   * The short name of the Editor plugin.
   */ 
  private String  pEditorName;

  /**
   * The Editor plugin revision number.
   */ 
  private VersionID  pEditorVersionID;

  /**
   * The name of the Editor plugin vendor.
   */ 
  private String  pEditorVendor;
  
}
  
