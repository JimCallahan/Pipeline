// $Id: PrivilegeDetails.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E   D E T A I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The privileges granted to a specific user with respect to all other users.
 */
public
class PrivilegeDetails
  extends Privileges
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with no privileges.
   */ 
  public 
  PrivilegeDetails() 
  {
    super();
  }

  /**
   * Construct a modifiable set of privileges from read-only privileges.
   * 
   * @param privs
   *   The privileges granted.
   * 
   * @param managedUsers
   *   The names of the users which are a members (or managers) of at lease one group for
   *   which the privileged user is a manager.  If the privileged user does not have either
   *   Queue Manager or Node Manager privileges, <CODE>null</CODE> should be used instead. 
   */ 
  public 
  PrivilegeDetails
  (
   Privileges privs, 
   Set<String> sharedUsers
  ) 
  {
    super(privs);

    pIsMasterAdmin  = privs.isMasterAdmin(); 
    pIsDeveloper    = privs.isDeveloper(); 
    pIsQueueAdmin   = privs.isQueueAdmin(); 
    pIsQueueManager = privs.isQueueManager(); 
    pIsNodeManager  = privs.isNodeManager(); 

    if(!pIsMasterAdmin && ((!pIsQueueAdmin && pIsQueueManager) || pIsNodeManager)) {
      if(sharedUsers == null) 
	throw new IllegalArgumentException
	  ("The shared users cannot be (null) when either Queue Manager or Node Manager " +
	   "privileges have been granted.");
      pManagedUsers = Collections.unmodifiableSet(sharedUsers);
    }
  }

  /**
   * Copy constructor. 
   */
  public 
  PrivilegeDetails
  (
   PrivilegeDetails details
  ) 
  {
    this(details, details.pManagedUsers);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether queue manager privileges have been granted over the given user. <P> 
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
   * @param uname
   *   The unique name of the user. 
   *
   * @see WorkGroups
   */ 
  public boolean
  isQueueManaged
  (
   String uname
  ) 
  {
    return (pIsMasterAdmin || pIsQueueAdmin || 
	    (pIsQueueManager && pManagedUsers.contains(uname)));
  }
  
  /**
   * Whether node manager privileges have been granted over the given user. <P> 
   * 
   * Privileges granted include:<BR>
   * <DIV style="margin-left: 40px;">
   *  Check-In, Check-Out and Evolve Nodes
   *  Link, Unlink, Add and Remove Secondary Sequences
   *  Register, Clone, Export, Rename and Renumber Nodes
   *  Release Nodes and Add/Release Views
   *  Modify nodes using the Node Details, Node Files and Node Links Panels
   * <DIV><P> 
   * 
   * These privileges should be granted to users trusted to manage the nodes of users which 
   * belong to a common work group. 
   * 
   * @param uname
   *   The unique name of the user. 
   * 
   * @see WorkGroups
   */ 
  public boolean
  isNodeManaged
  (
   String uname
  ) 
  {
    return (pIsMasterAdmin ||
	    (pIsNodeManager && pManagedUsers.contains(uname)));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5421863036110785160L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the users which are a members (or managers) of at lease one group for
   * which the privileged user is a manager.  If the privileged user does not have either
   * Queue Manager or Node Manager privileges, the value will be <CODE>null</CODE>.
   */ 
  private Set<String>  pManagedUsers; 
  
}
