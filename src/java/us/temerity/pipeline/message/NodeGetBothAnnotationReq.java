// $Id: NodeGetBothAnnotationReq.java,v 1.1 2009/03/20 03:10:39 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   B O T H   A N N O T A T I O N   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get a specific annotation for the given node.
 */
public
class NodeGetBothAnnotationReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param aname 
   *   The name of the annotation. 
   */
  public
  NodeGetBothAnnotationReq
  (
   NodeID id,
   String aname
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(aname == null) 
      throw new IllegalArgumentException("The annotation name cannot be (null)!");
    pAnnotationName = aname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
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

  private static final long serialVersionUID = -424081245812099573L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The annotation name.
   */ 
  private String  pAnnotationName;

}
  
