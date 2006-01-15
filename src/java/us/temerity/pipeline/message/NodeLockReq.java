// $Id: NodeLockReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L O C K   R E Q                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to lock the working version of a node to a specific checked-in version
 * 
 * @see MasterMgr
 */
public
class NodeLockReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then lock to the base checked-in
   * version. <P>
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version is 
   *   being locked.
   */
  public
  NodeLockReq
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
   * Get the revision number to check-out.
   * 
   * @return
   *   The revision number or <CODE>null</CODE> for the base checked-in version. 
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4552878230079741518L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number to lock.
   */ 
  private VersionID  pVersionID;       

}
  
