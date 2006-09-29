// $Id: AdminPrivileges.java,v 1.3 2006/09/29 03:03:21 jim Exp $
 
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
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
  getWorkGroups
  (
   TaskTimer timer 
  )
  {
    timer.resume();	
    return new MiscGetWorkGroupsRsp(timer, pWorkGroups);
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
  setWorkGroups
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
      pPrivileges.remove(dead);
    
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
  getPrivileges
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
  editPrivileges
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
    
    TreeMap<String,Privileges> table = req.getTable();
    for(String uname : table.keySet()) {
      Privileges privs = table.get(uname);
      if(pWorkGroups.isUser(uname) && privs.hasAnyPrivileges()) 
	pPrivileges.put(uname, privs);
      else
	pPrivileges.remove(uname);
    }

    writePrivileges();
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
  getPrivilegeDetails
  (
   TaskTimer timer, 
   MiscGetPrivilegeDetailsReq req   
  )
  {
    timer.resume();	

    PrivilegeDetails details = null;
    {
      String uname = req.getUserName();
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
    }

    return new MiscGetPrivilegeDetailsRsp(timer, details);
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
      String glue = null;
      try {
	GlueEncoder ge = new GlueEncoderImpl("WorkGroups", pWorkGroups);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of the work groups!");
	LogMgr.getInstance().flush();
	
	throw new IOException(ex.getMessage());
      }
      
      {
	FileWriter out = new FileWriter(file);
	out.write(glue);
	out.flush();
	out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write the work groups file (" + file + ")...\n" + 
	 "    " + ex.getMessage());
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
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pWorkGroups = (WorkGroups) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The work groups file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the work groups " + 
	   "file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Privileges", pPrivileges);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the " + 
	     "adminstrative privileges!");
	  LogMgr.getInstance().flush();
	  
	  throw new IOException(ex.getMessage());
	}
	
	{
	  FileWriter out = new FileWriter(file);
	  out.write(glue);
	  out.flush();
	  out.close();
	}
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to write the adminstrative privileges " + 
	   "file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
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
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pPrivileges = (TreeMap<String,Privileges>) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The adminstrative privileges file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the adminstrative privileges " + 
	   "file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
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
