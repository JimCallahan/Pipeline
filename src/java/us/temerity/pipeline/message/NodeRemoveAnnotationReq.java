// $Id: NodeRemoveAnnotationReq.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E M O V E   A N N O T A T I O N   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove a specific annotation from a node.
 */
public
class NodeRemoveAnnotationReq
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
   * 
   * @param aname 
   *   The name of the annotation. 
   */
  public
  NodeRemoveAnnotationReq
  (
   String nname, 
   String aname
  )
  { 
    if(nname == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pNodeName = nname;

    if(aname == null) 
      throw new IllegalArgumentException("The annotation name cannot be (null)!");
    pAnnotationName = aname;
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

  /**
   * Gets the annotation name.
   */
  public String
  getAnnotationName() 
  {
    return pAnnotationName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2957322865647059135L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */ 
  private String  pNodeName;

  /**
   * The annotation name.
   */ 
  private String  pAnnotationName;

}
  
