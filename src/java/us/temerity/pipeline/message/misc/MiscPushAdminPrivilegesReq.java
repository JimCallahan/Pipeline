// $Id: MiscSetAdminPrivilegesReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   P U S H   A D M I N   P R I V I L E G E S   R E Q                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to push the work groups and administrative privileges from the MasterMgr to the
 * QueueMgr or PluginMgr.
 */
public
class MiscPushAdminPrivilegesReq
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
  MiscPushAdminPrivilegesReq
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

  private static final long serialVersionUID = -6684831777597290004L;

  

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
  
