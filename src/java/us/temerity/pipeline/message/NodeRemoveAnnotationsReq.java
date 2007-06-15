// $Id: NodeRemoveAnnotationsReq.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E M O V E   A N N O T A T I O N S   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove all annotations from a node.
 */
public
class NodeRemoveAnnotationsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param nname 
   *   The fully resolved node name.
   */
  public
  NodeRemoveAnnotationsReq
  (
   String nname
  )
  { 
    if(nname == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pNodeName = nname;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */
  public String
  getNodeName() 
  {
    return pNodeName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8755889041856319896L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */ 
  private String  pNodeName;

}
  
