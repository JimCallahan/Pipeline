// $Id: MiscUpdateAdminPrivilegesReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   U P D A T E   A D M I N   P R I V I L E G E S   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to update the work groups and administrative privileges from the MasterMgr.
 */
public
class MiscUpdateAdminPrivilegesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param groups
   *   The work groups used to determine the scope of administrative privileges.
   * 
   * @param privs
   *   The administrative privileges for each user indexed by user name. 
   */
  public
  MiscUpdateAdminPrivilegesReq
  (
   WorkGroups groups, 
   TreeMap<String,Privileges> privs
  )
  {
    if(groups == null) 
      throw new IllegalArgumentException
	("The privileges cannot be (null)!");
    pWorkGroups = groups; 

    if(privs == null) 
      throw new IllegalArgumentException
	("The privileges cannot be (null)!");
    pPrivileges = privs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the work groups used to determine the scope of administrative privileges.
   */ 
  public WorkGroups
  getWorkGroups() 
  {
    return pWorkGroups; 
  }
 
  /**
   * Gets the administrative privileges for each user indexed by user name. 
   */ 
  public TreeMap<String,Privileges>
  getPrivileges() 
  {
    return pPrivileges; 
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9005292794718765767L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups used to determine the scope of administrative privileges.
   */ 
  private WorkGroups  pWorkGroups; 

  /**
   * The administrative privileges for each user indexed by user name. 
   */ 
  private TreeMap<String,Privileges>  pPrivileges; 
  

}
  
