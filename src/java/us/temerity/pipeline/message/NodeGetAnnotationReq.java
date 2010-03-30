// $Id: NodeGetAnnotationReq.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A N N O T A T I O N   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get a specific annotation for the given node.
 */
public
class NodeGetAnnotationReq
  implements Serializable
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
  NodeGetAnnotationReq
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

  private static final long serialVersionUID = -8848513829591487968L;

  

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
  
