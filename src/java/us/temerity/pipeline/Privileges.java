// $Id: Privileges.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of privileges granted to a user. <P> 
 * 
 * Pipeline uses privileges to determine which operations can be performed by a user.  Most
 * adminstrative operations can only be peformed by users with special privileges.  There are
 * several levels of adminstrative privileges provided to allow some "power users" and 
 * supervisors to perform operations normally reserved for system administrators. <P> 
 * 
 * Most node/job operations can always be performed on those nodes/jobs associated with
 * working areas owned by the current user.  Manager privileges also allow some users to 
 * perform these operations on node/jobs owned by other users in a comon work group. 
 * 
 * @see WorkGroups
 */
public
class Privileges
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new set of privileges.
   */ 
  public 
  Privileges() 
  {}

  /**
   * Copy constructor. 
   */ 
  public 
  Privileges
  (
   Privileges privs
  ) 
  {
    pIsMasterAdmin  = privs.isMasterAdmin(); 
    pIsDeveloper    = privs.isDeveloper(); 
    pIsQueueAdmin   = privs.isQueueAdmin(); 
    pIsQueueManager = privs.isQueueManager(); 
    pIsNodeManager  = privs.isNodeManager(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted full administrative privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Manage Work Groups and Privileges<BR>
   *   Archive, Offline and Restore Nodes<BR>
   *   Database Backup<BR>
   *   Manage Toolsets and Toolset Packages<BR>
   *   Delete Nodes
   * <DIV><P> 
   *   
   * Includes all privileges granted to: <BR>
   * <DIV style="margin-left: 40px;">
   *   Developer<BR>
   *   Queue Admin<BR>
   *   Queue Manager<BR>
   *   Node Manager
   * <DIV><P> 
   * 
   * These privileges should only be granted to system adminstrators. <P> 
   * 
   * Note that the special "pipeline" user is always treated as if granted full
   * administrative privileges.  
   */ 
  public boolean
  isMasterAdmin() 
  {
    return pIsMasterAdmin;
  }

  /**
   * Set whether the user should be granted full administrative privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Manage Privileges<BR>
   *   Archive, Offline and Restore Nodes<BR>
   *   Database Backup<BR>
   *   Manage Toolsets and Toolset Packages<BR>
   *   Delete Nodes
   * <DIV><P> 
   *   
   * Includes all privileges granted to: <BR>
   * <DIV style="margin-left: 40px;">
   *   Developer<BR>
   *   Queue Admin<BR>
   *   Queue Manager<BR>
   *   Node Manager
   * <DIV><P> 
   * 
   * These privileges should only be granted to system adminstrators. <P> 
   * 
   * Note that the special "pipeline" user is always treated as if granted full
   * administrative privileges.  
   */ 
  public void
  setMasterAdmin
  (
   boolean isMasterAdmin
  ) 
  {
    pIsMasterAdmin = isMasterAdmin; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted developer privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Ability to install new plugins<BR> 
   *   Manage Editor, Action, Comparator and Tool Plugin Menus
   * <DIV><P> 
   * 
   * These privileges should be granted to users which develop in-house Pipeline plugins to
   * allow them to install and test these plugins.
   */ 
  public boolean
  isDeveloper() 
  {
    return (pIsMasterAdmin || pIsDeveloper);
  }

  /**
   * Set whether the user should be granted developer privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Ability to install new plugins<BR> 
   *   Manage Editor, Action, Comparator and Tool Plugin Menus
   * <DIV><P> 
   * 
   * These privileges should be granted to users which develop in-house Pipeline plugins to
   * allow them to install and test these plugins.
   */ 
  public void 
  setDeveloper
  (
   boolean isDeveloper
  ) 
  {
    pIsDeveloper = isDeveloper; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted queue administration privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Manage License Keys<BR>
   *   Manage Selection Keys, Groups and Schedules<BR>
   *   Add and Remove Job Servers<BR>
   *   Edit Job Server Status, Slots, Reservations, Order, Groups and Schedules
   * <DIV><P> 
   *   
   * Includes all privileges granted to: <BR>
   * <DIV style="margin-left: 40px;">
   *   Queue Manager
   * <DIV><P> 
   * 
   * These privileges should be granted to system adminstrators and supervisors responsible
   * for maintaining the fundamental operation of the queue.
   */ 
  public boolean
  isQueueAdmin() 
  {
    return (pIsMasterAdmin || pIsQueueAdmin);
  }

  /**
   * Set whether the user should be granted queue administration privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Manage License Keys<BR>
   *   Manage Selection Keys, Groups and Schedules<BR>
   *   Add and Remove Job Servers<BR>
   *   Edit Job Server Status, Slots, Reservations, Order, Groups and Schedules
   * <DIV><P> 
   *   
   * Includes all privileges granted to: <BR>
   * <DIV style="margin-left: 40px;">
   *   Queue Manager
   * <DIV><P> 
   * 
   * These privileges should be granted to system adminstrators and supervisors responsible
   * for maintaining the fundamental operation of the queue.
   */ 
  public void
  setQueueAdmin
  (
   boolean isQueueAdmin
  ) 
  {
    pIsQueueAdmin = isQueueAdmin;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted queue manager privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Queue, Pause, Resume, Preempt and Kill Jobs<BR>
   *   Remove Files
   * <DIV><P> 
   * 
   * These privileges should be granted to users trusted to manage the jobs of users which 
   * belong to a common work group. 
   * 
   * @see WorkGroups
   */ 
  public boolean
  isQueueManager() 
  {
    return (pIsMasterAdmin || pIsQueueAdmin || pIsQueueManager); 
  }
  
  /**
   * Set whether the user should be granted queue manager privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *   Queue, Pause, Resume, Preempt and Kill Jobs<BR>
   *   Remove Files
   * <DIV><P> 
   * 
   * These privileges should be granted to users trusted to manage the jobs of users which 
   * belong to a common work group. 
   * 
   * @see WorkGroups
   */ 
  public void 
  setQueueManager
  (
   boolean isQueueManager
  ) 
  {
    pIsQueueManager = isQueueManager; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted node manager privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *  Creating and Removing Working Areas<BR>
   *  Check-In, Check-Out, Lock and Evolve Nodes<BR>
   *  Link and Unlink Nodes<BR>
   *  Add and Remove Secondary Sequences<BR>
   *  Register, Clone, Export, Rename and Renumber Nodes<BR>
   *  Release Nodes and Add/Release Views<BR>
   *  Modify nodes using the Node Details, Node Files and Node Links Panels
   * <DIV><P> 
   * 
   * These privileges should be granted to users trusted to manage the nodes of users which 
   * belong to a common work group. 
   * 
   * @see WorkGroups
   */ 
  public boolean
  isNodeManager() 
  {
    return (pIsMasterAdmin || pIsNodeManager); 
  }  
 
  /**
   * Set whether the user should be granted node manager privileges. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *  Creating and Removing Working Areas<BR>
   *  Check-In, Check-Out, Lock and Evolve Nodes<BR>
   *  Link and Unlink Nodes<BR>
   *  Add and Remove Secondary Sequences<BR>
   *  Register, Clone, Export, Rename and Renumber Nodes<BR>
   *  Release Nodes and Add/Release Views<BR>
   *  Modify nodes using the Node Details, Node Files and Node Links Panels
   * <DIV><P> 
   * 
   * These privileges should be granted to users trusted to manage the nodes of users which 
   * belong to a common work group. 
   * 
   * @see WorkGroups
   */ 
  public void 
  setNodeManager
  (
   boolean isNodeManager
  )
  {
    pIsNodeManager = isNodeManager;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted any privileges.
   */ 
  public boolean
  hasAnyPrivileges() 
  {
    return (pIsMasterAdmin || pIsDeveloper || 
	    pIsQueueAdmin || pIsQueueManager || pIsNodeManager);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("IsMasterAdmin", isMasterAdmin());
    encoder.encode("IsDeveloper", isDeveloper());
    encoder.encode("IsQueueAdmin", isQueueAdmin());
    encoder.encode("IsQueueManager", isQueueManager());
    encoder.encode("IsNodeManager", isNodeManager());
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    {
      Boolean tf = (Boolean) decoder.decode("IsMasterAdmin"); 
      pIsMasterAdmin = ((tf != null) && tf);
    }

    {
      Boolean tf = (Boolean) decoder.decode("IsDeveloper"); 
      pIsDeveloper = ((tf != null) && tf);
    }

    {
      Boolean tf = (Boolean) decoder.decode("IsQueueAdmin"); 
      pIsQueueAdmin = ((tf != null) && tf);
    }

    {
      Boolean tf = (Boolean) decoder.decode("IsQueueManager"); 
      pIsQueueManager = ((tf != null) && tf);
    }

    {
      Boolean tf = (Boolean) decoder.decode("IsNodeManager"); 
      pIsNodeManager = ((tf != null) && tf);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6619800266260510211L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the user has been granted full administrative privileges.
   */
  protected boolean  pIsMasterAdmin;
  
  /**
   * Whether the user has been granted developer privileges.
   */
  protected boolean  pIsDeveloper;

  /**
   * Whether the user has been granted queue administration privileges.
   */
  protected boolean  pIsQueueAdmin;

  /**
   * Whether the user has been granted queue manager privileges.
   */
  protected boolean  pIsQueueManager; 

  /**
   * Whether the user has been granted node manager privileges.
   */
  protected boolean  pIsNodeManager; 

}

