// $Id: NodeGetCheckedInReq.java,v 1.2 2004/10/09 16:55:08 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get a specific checked-in version of a node.                  
 * 
 * @see MasterMgr
 */
public
class NodeGetCheckedInReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param vid
   *   The revision number of the checked-in version or <CODE>null</CODE> for the latest 
   *   version.
   */
  public
  NodeGetCheckedInReq
  (
   String name, 
   VersionID vid
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

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
   * Get the revision number of the checked-in version or <CODE>null</CODE> for the latest 
   * version.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6962084608311796061L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private String  pName;

  /**
   * The revision number of the checked-in version or <CODE>null</CODE> for the latest 
   * version.
   */ 
  private VersionID  pVersionID;    

}
  
