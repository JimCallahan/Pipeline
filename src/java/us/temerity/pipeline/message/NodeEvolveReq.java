// $Id: NodeEvolveReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E V O L V E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the checked-in version upon which the working version is based without 
 * modifying the working version properties, links or associated files. 
 * 
 * @see MasterMgr
 */
public
class NodeEvolveReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vid
   *   The revision number of the checked-in version or <CODE>null</CODE> for the latest
   *   checked-in version.
   */
  public
  NodeEvolveReq
  (
   NodeID id, 
   VersionID vid
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pVersionID = vid;
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
   * Get revision number of the checked-in version or <CODE>null</CODE> for the latest
   * checked-in version.
   */ 
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4491150976825491422L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number of the checked-in version or <CODE>null</CODE> for the latest
   * checked-in version.
   */ 
  private VersionID  pVersionID;

}
  
