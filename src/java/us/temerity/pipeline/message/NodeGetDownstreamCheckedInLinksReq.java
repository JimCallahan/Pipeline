// $Id: NodeGetDownstreamCheckedInLinksReq.java,v 1.1 2006/10/18 08:43:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   D O W N S T R E A M  C H E C K E D - I N   L I N K S   R E Q         */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the upstream links of all checked-in versions of the given node.
 */
public
class NodeGetDownstreamCheckedInLinksReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The fully resolved name of the upstream node.
   *
   * @param vid 
   *   The revision number of the checked-in upstream node.
   */
  public
  NodeGetDownstreamCheckedInLinksReq
  (
   String name, 
   VersionID vid
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Gets the revision number of the checked-in upstream node.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3688772787544768801L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */ 
  private String  pName; 

  /**
   * The revision number of the checked-in upstream node.
   */ 
  private VersionID pVersionID; 

}
  
