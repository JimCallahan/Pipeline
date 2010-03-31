// $Id: AdminPrivileges.java,v 1.11 2008/10/20 00:30:55 jim Exp $
 
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A D M I N    P R I V I L E G E S                                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * Common operations and tests shared by both MasterMgr and QueueMgr related to work groups
 * and administrative privileges.
 */
public
class AdminPrivileges
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and initilize administrative privileges.
   */ 
  public 
  AdminPrivileges() 
  {
    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");

    pWorkGroups = new WorkGroups();
    pPrivileges = new TreeMap<String,Privileges>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the requesting user has been granted full administrative privileges.
   *
   * @param req
   *   The request.
   */ 
  public synchronized boolean
  isMasterAdmin
  (
   PrivilegedReq req
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && privs.isMasterAdmin());      
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the requesting user has been granted developer privileges.
   * 
   * @param req
   *   The request.
   */ 
  public synchronized boolean
  isDeveloper
  (
   PrivilegedReq req
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && privs.isDeveloper());      
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the requesting user has been granted annotator privileges.
   * 
   * @param req
   *   The request.
   */ 
  public synchronized boolean
  isAnnotator
  (
   PrivilegedReq req
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && privs.isAnnotator());      
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the requesting user has been granted queue administrative privileges.
   * 
   * @param req
   *   The request.
   */ 
  public synchronized boolean
  isQueueAdmin
  (
   PrivilegedReq req
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && privs.isQueueAdmin());      
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the requesting user has been granted queue manager privileges over jobs
   * owned by the given user.
   * 
   * @param req
   *   The request.
   * 
   * @param uname
   *   The unique name of the user owning the jobs.
   */ 
  public synchronized boolean
  isQueueManaged
  (
   PrivilegedReq req, 
   String uname 
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser) || requestor.equals(uname))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && 
	    (privs.isMasterAdmin() || privs.isQueueAdmin() || 
	     (privs.isQueueManager() && pWorkGroups.isManagedUser(requestor, uname))));
  }

  /**
   * Whether the requesting user has been granted queue manager privileges over jobs
   * owned by the given user.
   * 
   * @param req
   *   The request.
   * 
   * @param nodeID
   *   The unique working version identifier of the node being modified.
   */ 
  public synchronized boolean
  isQueueManaged
  (
   PrivilegedReq req, 
   NodeID nodeID
  ) 
  {
    return isQueueManaged(req, nodeID.getAuthor());
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the requesting user has been granted node manager privileges over nodes
   * owned by the given user.
   * 
   * @param req
   *   The request.
   * 
   * @param uname
   *   The unique name of the user owning the node.
   */ 
  public synchronized boolean
  isNodeManaged
  (
   PrivilegedReq req, 
   String uname 
  ) 
  {
    String requestor = req.getRequestor();
    if(requestor.equals(PackageInfo.sPipelineUser) || requestor.equals(uname))
      return true;
    
    Privileges privs = pPrivileges.get(requestor);
    return ((privs != null) && 
	    (privs.isMasterAdmin() ||
	     (privs.isNodeManager() && pWorkGroups.isManagedUser(requestor, uname))));
  }

  /**
   * Whether the requesting user has been granted node manager privileges over nodes
   * owned by the given user.
   * 
   * @param req
   *   The request.
   * 
   * @param nodeID
   *   The unique working version identifier of the node being modified.
   */ 
  public synchronized boolean
  isNodeManaged
  (
   PrivilegedReq req, 
   NodeID nodeID
  ) 
  {
    return isNodeManaged(req, nodeID.getAuthor());
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether a user is a member or manager of a particular group.
   * 
   * @param uname
   *   The unique name of the user.
   * 
   * @param gname
   *   The unique name of the group.
   */
  public synchronized boolean 
  isWorkGroupMember
  (
   String uname, 
   String gname
  )
  {
    return (pWorkGroups.isMemberOrManager(uname, gname) != null);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given name is a known user or work group.
   */ 
  public synchronized boolean 
  isValidName
  (
   String name
  ) 
  {
    return (pWorkGroups.isUser(name) || pWorkGroups.isGroup(name));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a deep copy of the work groups used to determine the scope of administrative 
   * privileges.
   */
  public synchronized WorkGroups
  getWorkGroups() 
  {
    return new WorkGroups(pWorkGroups);
  }

  /**
   * Get the privileges granted to a specific user with respect to all other users. 
   * 
   * @param uname
   *   The name of the user who's privileges are being requested.
   * 
   * @return
   *   The privilege details.
   */ 
  public synchronized PrivilegeDetails
  getPrivilegeDetails
  (
   String uname
  )
  {
    PrivilegeDetails details = null;

    if(uname.equals(PackageInfo.sPipelineUser)) {
      Privileges privs = new Privileges();
      privs.setMasterAdmin(true);
      details = new PrivilegeDetails(privs, null);
    }
    else {
      Privileges privs = pPrivileges.get(uname);
      if(privs == null) 
        details = new PrivilegeDetails();
      else {
        Set<String> managed = null;
        if(privs.isQueueManager() || privs.isNodeManager()) 
          managed = pWorkGroups.getManagedUsers(uname);
        details = new PrivilegeDetails(privs, managed);
      }
    }

    return details;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get work group memberships for the given user.
   * 
   * @return 
   *   The table of membership information indexed by the names of the work groups of which 
   *   the user is a member. A value of <CODE>false</CODE>, means that the user is a member. 
   *   If the value is <CODE>true</CODE>, then the user is a manager.  If the value is 
   *   <CODE>null</CODE> or if the group is missing from the table, then the user is neither
   *   a member or manager of the group.
   */ 
  public synchronized TreeMap<String,Boolean>
  getWorkGroupMemberships
  (
   String user
  ) 
  {
    if(user == null) 
      throw new IllegalArgumentException
        ("The user name cannot be (null)!");

    TreeMap<String,Boolean> memberships = new TreeMap<String,Boolean>(); 
    for(String gname : pWorkGroups.getGroups()) {
      Boolean isManager = pWorkGroups.isMemberOrManager(user, gname);
      if(isManager != null) 
        memberships.put(gname, isManager);
    }

    return memberships;
  }
   
  /**
   * Make sure the given user has been added and if not create one with default permissions. 
   * 
   * @return 
   *   Whether a new user was created.
   */ 
  public synchronized boolean
  addMissingUser
  (
   String user
  ) 
    throws PipelineException
  {
    if(user == null) 
      throw new IllegalArgumentException
        ("The user name cannot be (null)!");

    if(pWorkGroups.isUser(user)) 
      return false; 

    pWorkGroups.addUser(user);
    writeWorkGroups();

    return true;
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   R E Q U E S T S                                                                      */
  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Get the request to update the administrative privileges.
   */ 
  public synchronized MiscUpdateAdminPrivilegesReq
  getUpdateRequest() 
  {
    return new MiscUpdateAdminPrivilegesReq(pWorkGroups, pPrivileges);
  }

  /**
   * Set the work groups and adminstrative privileges. <P> 
   * 
   * Used to update the QueueMgr privileges by cloning the MasterMgr privileges.
   * 
   * @param timer
   *   The task timer.
   * 
   * @param req 
   *   The request.
   */ 
  public synchronized void
  updateAdminPrivileges
  (
   TaskTimer timer, 
   MiscUpdateAdminPrivilegesReq req
  ) 
  {
    timer.resume();
    
    pWorkGroups = req.getWorkGroups(); 
    pPrivileges = req.getPrivileges();
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the work groups used to determine the scope of administrative privileges.
   * 
   * @param timer
   *   The task timer.
   * 
   * @return
   *   <CODE>MiscGetWorkGroupsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the work groups.
   */ 
  public synchronized Object
  getWorkGroupsRsp
  (
   TaskTimer timer 
  )
  {
    timer.resume();	
    return new MiscGetWorkGroupsRsp(timer, getWorkGroups());
  }
  
  /**
   * Set the work groups used to determine the scope of administrative privileges. <P> 
   * 
   * This operation requires Master Admin privileges 
   * (see {@link Privileges#isMasterAdmin isMasterAdmin 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param req 
   *   The request.
   * 
   * @throws PipelineException
   *   If unable to set the work groups.
   */ 
  public synchronized void
  setWorkGroupsFromReq
  (
   TaskTimer timer,
   MiscSetWorkGroupsReq req
  )
    throws PipelineException
  {
    timer.resume();
    
    if(!isMasterAdmin(req)) 
      throw new PipelineException
	("Only a user with Master Admin privileges may modify the work groups!");
    
    pWorkGroups = req.getGroups();
    writeWorkGroups();
    
    TreeSet<String> dead = new TreeSet<String>();
    for(String uname : pPrivileges.keySet()) {
      Privileges privs = pPrivileges.get(uname);
      if(!pWorkGroups.isUser(uname) || !privs.hasAnyPrivileges()) 
	dead.add(uname);
    }
    
    for(String uname : dead) 
      pPrivileges.remove(uname);
    
    if(!dead.isEmpty()) 
      writePrivileges();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the administrative privileges for all users.
   * 
   * @param timer
   *   The task timer.
   * 
   * @return
   *   <CODE>MiscGetPrivilegesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the privileges.
   */ 
  public synchronized Object
  getPrivilegesRsp
  (
   TaskTimer timer 
  )
  {
    timer.resume();	
    return new MiscGetPrivilegesRsp(timer, pPrivileges);
  }

  /**
   * Change the administrative privileges for the given users. <P> 
   * 
   * This operation requires Master Admin privileges 
   * (see {@link Privileges#isMasterAdmin isMasterAdmin 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param req 
   *   The request.
   * 
   * @throws PipelineException
   *   If unable to set the privileges.
   */ 
  public synchronized void 
  editPrivilegesFromReq
  (
   TaskTimer timer,
   MiscEditPrivilegesReq req
  )
    throws PipelineException 
  {
    timer.resume();
    
    if(!isMasterAdmin(req)) 
      throw new PipelineException
	("Only a user with Master Admin privileges may modify administrative privileges!");
    
    TreeSet<String> unknown = new TreeSet<String>();

    TreeMap<String,Privileges> table = req.getTable();
    for(String uname : table.keySet()) {
      if(pWorkGroups.isUser(uname)) {
        Privileges privs = table.get(uname);
        if(privs.hasAnyPrivileges()) 
          pPrivileges.put(uname, privs);
        else
          pPrivileges.remove(uname);
      }
      else {
        unknown.add(uname);
        pPrivileges.remove(uname);
      }
    }

    writePrivileges();

    if(!unknown.isEmpty()) {
      StringBuilder buf = new StringBuilder();
      buf.append
        ("While modifying administrative privileges, the following user names where " +
         "specified that where not known as users of Pipeline:\n\n"); 
      for(String uname : unknown) 
        buf.append("  " + uname + "\n");
      buf.append
        ("\n" + 
         "Changes in privileges for these specific users were ignored, however changes " + 
         "to privileges for all other users specified were performed successfully.");
      throw new PipelineException(buf.toString());
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the privileges granted to a specific user with respect to all other users. 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPrivilegeDetailsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the privileges.
   */ 
  public synchronized Object
  getPrivilegeDetailsRsp
  (
   TaskTimer timer, 
   MiscGetPrivilegeDetailsReq req   
  )
  {
    timer.resume();	
    PrivilegeDetails details = getPrivilegeDetails(req.getUserName());
    return new MiscGetPrivilegeDetailsRsp(timer, details);
  }

  /**
   * Get the privileges granted to a specific user with respect to all other users. 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param req 
   *   The request.
   */ 
  public synchronized PrivilegeDetails
  getPrivilegeDetailsFromReq
  (
   PrivilegedReq req
  )
  {	
    return getPrivilegeDetails(req.getRequestor());
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Read the work groups and adminstrative privileges from disk.
   * 
   * @throws PipelineException
   *   If unable to read the work groups file.
   */ 
  public synchronized void 
  readAll() 
    throws PipelineException    
  {
    readWorkGroups();
    readPrivileges();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the work groups to disk. 
   * 
   * @throws PipelineException
   *   If unable to write the work groups file.
   */ 
  private synchronized void 
  writeWorkGroups() 
    throws PipelineException
  {
    enforcePipelineWorkGroups();
    
    Path path = new Path(PackageInfo.sNodePath, "etc/work-groups");
    File file = path.toFile();
    if(file.exists()) {
      if(!file.delete())
	throw new PipelineException
	  ("Unable to remove the old work groups file (" + file + ")!");
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Work Groups.");
    
    try {     
      GlueEncoderImpl.encodeFile("WorkGroups", pWorkGroups, file);
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }
  
  /**
   * Read the work groups from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the work groups file.
   */ 
  private synchronized void 
  readWorkGroups() 
    throws PipelineException
  {
    Path path = new Path(PackageInfo.sNodePath, "etc/work-groups");
    File file = path.toFile();
    if(file.isFile()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Work Groups.");
      
      try {
        pWorkGroups = (WorkGroups) GlueDecoderImpl.decodeFile("WorkGroups", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }

    enforcePipelineWorkGroups();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the administrative privileges to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the privileges file.
   */ 
  private synchronized void 
  writePrivileges() 
    throws PipelineException
  {
    enforcePipelinePrivileges();

    Path path = new Path(PackageInfo.sNodePath, "etc/privileges");
    File file = path.toFile();
    if(file.exists()) {
      if(!file.delete())
	throw new PipelineException
	  ("Unable to remove the old adminstrative privileges file (" + file + ")!");
    }
    
    if(!pPrivileges.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Administrative Privileges.");
      
      try {
        GlueEncoderImpl.encodeFile("Privileges", pPrivileges, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }
  
  /**
   * Read the adminstrative privileges from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the adminstrative privileges file.
   */ 
  private synchronized void 
  readPrivileges() 
    throws PipelineException
  {
    Path path = new Path(PackageInfo.sNodePath, "etc/privileges");
    File file = path.toFile();
    if(file.isFile()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Administrative Privileges.");
      
      try {
       pPrivileges = 
         (TreeMap<String,Privileges>) GlueDecoderImpl.decodeFile("Privileges", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
    
    enforcePipelinePrivileges();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure the "pipeline" user always exists and is a Manager of every group.
   */ 
  private synchronized void 
  enforcePipelineWorkGroups() 
  {
    pWorkGroups.addUser(PackageInfo.sPipelineUser);
    for(String gname : pWorkGroups.getGroups()) 
      pWorkGroups.setMemberOrManager(PackageInfo.sPipelineUser, gname, new Boolean(true));    
  }

  /**
   * Make sure the ipeline" user has Master Admin privileges. 
   */ 
  private synchronized void 
  enforcePipelinePrivileges() 
  {
    Privileges privs = new Privileges();
    privs.setMasterAdmin(true);
    pPrivileges.put(PackageInfo.sPipelineUser, privs);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups used to determine the scope of administrative privileges.
   */ 
  private WorkGroups  pWorkGroups; 

  /**
   * The administrative privileges for each user indexed by user name. <P> 
   * 
   * If there is no entry for a given user, then no privileges are granted.
   */ 
  private TreeMap<String,Privileges>  pPrivileges; 
  
}
