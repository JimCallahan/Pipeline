// $Id: MiscGetSizesRsp.java,v 1.5 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   P U L L   A D M I N   P R I V I L E G E S   R E Q                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Pull the work groups and administrative privileges from the MasterMgr.
 */
public
class MiscPullAdminPrivilegesRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param groups
   *   The work groups used to determine the scope of administrative privileges.
   * 
   * @param privs
   *   The administrative privileges for each user indexed by user name. 
   */
  public
  MiscPullAdminPrivilegesRsp
  (
   TaskTimer timer,
   WorkGroups groups, 
   TreeMap<String,Privileges> privs 
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException
	("The privileges cannot be (null)!");
    pWorkGroups = groups; 

    if(privs == null) 
      throw new IllegalArgumentException
	("The privileges cannot be (null)!");
    pPrivileges = privs;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.pullAdminPrivileges(): \n  " + getTimer());
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

  private static final long serialVersionUID = 5817628438945855122L;

  

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
  
