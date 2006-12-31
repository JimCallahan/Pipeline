// $Id: NodeEditingFinishedReq.java,v 1.1 2006/12/31 20:44:53 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E D I T I N G   F I N I S H E D   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Signal that an Editor plugin has finished editing files associated with the 
 * given working version of a node.
 */
public
class NodeEditingFinishedReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param editID 
   *   The unique ID for the editing session.
   */
  public
  NodeEditingFinishedReq
  (
   Long editID
  )
  { 
    if(editID == null) 
      throw new IllegalArgumentException("The editing session ID cannot be (null)!");
    pEditID = editID;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique ID for the editing session.
   */
  public Long
  getEditID() 
  {
    return pEditID;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5594450475001522336L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique ID for the editing session.
   */ 
  private Long  pEditID;

}
  
