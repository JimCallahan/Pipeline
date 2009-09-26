// $Id: MasterMgrClient.java,v 1.143 2009/09/26 02:39:05 jlee Exp $

package us.temerity.pipeline;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.Map.*;
import java.util.regex.Pattern;

import us.temerity.pipeline.builder.ActionOnExistence;
import us.temerity.pipeline.event.BaseNodeEvent;
import us.temerity.pipeline.glue.Glueable;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;


/*------------------------------------------------------------------------------------------*/
/*   M A S T E R    M G R   C L I E N T                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline master manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline master manager daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B></A>(1).  This class represents the
 * interface used by all Pipeline client programs and end user tools to interact with the 
 * Pipeline system.
 */
public
class MasterMgrClient
  extends BaseMgrClient
  implements MasterMgrLightClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager client.
   */
  public
  MasterMgrClient()
  {
    super(PackageInfo.sMasterServer, PackageInfo.sMasterPort, 
	  MasterRequest.Disconnect, MasterRequest.Shutdown, "MasterMgr");
  }


  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   * 
   * @param shutdownJobMgrs
   *   Whether to command the queue manager to shutdown all job servers before exiting.
   * 
   * @param shutdownPluginMgr
   *   Whether to shutdown the plugin manager before exiting.
   */
  public synchronized void 
  shutdown
  (
   boolean shutdownJobMgrs,
   boolean shutdownPluginMgr
  ) 
    throws PipelineException 
  {
    PrivilegeDetails details = getPrivilegeDetails();
    if(!details.isMasterAdmin()) 
      throw new PipelineException
	("Only a user with Master Admin privileges may shutdown the servers!");

    verifyConnection();

    MiscShutdownOptionsReq req = 
      new MiscShutdownOptionsReq(shutdownJobMgrs, shutdownPluginMgr);
    shutdownTransaction(MasterRequest.ShutdownOptions, req); 
  }

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  @Override
  public synchronized void 
  shutdown() 
    throws PipelineException 
  {
    PrivilegeDetails details = getPrivilegeDetails();
    if(!details.isMasterAdmin()) 
      throw new PipelineException
	("Only a user with Master Admin privileges may shutdown the servers!");
    
    super.shutdown();
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the work groups used to determine the scope of administrative privileges.
   * 
   * @return 
   *   The work groups.
   * 
   * @throws PipelineException
   *   If unable to get the work groups.
   */ 
  public synchronized WorkGroups
  getWorkGroups()
    throws PipelineException  
  {
    verifyConnection();
	 
    Object obj = performTransaction(MasterRequest.GetWorkGroups, null);
    if(obj instanceof MiscGetWorkGroupsRsp) {
      MiscGetWorkGroupsRsp rsp = (MiscGetWorkGroupsRsp) obj;
      return rsp.getGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  
  /**
   * Set the work groups used to determine the scope of administrative privileges. <P> 
   * 
   * This operation requires Master Admin privileges 
   * (see {@link Privileges#isMasterAdmin isMasterAdmin}). 
   * 
   * @param groups 
   *   The work groups.
   * 
   * @throws PipelineException
   *   If unable to set the work groups.
   */ 
  public synchronized void
  setWorkGroups
  (
    WorkGroups groups
  )
    throws PipelineException 
  {
    verifyConnection();

    MiscSetWorkGroupsReq req = new MiscSetWorkGroupsReq(groups);

    Object obj = performTransaction(MasterRequest.SetWorkGroups, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the administrative privileges for all users.<P> 
   * 
   * If there is no entry for a given user, then no privileges are granted.
   * 
   * @return 
   *   The privileges for each user indexed by user name.
   * 
   * @throws PipelineException
   *   If unable to get the privileges.
   */ 
  public synchronized TreeMap<String,Privileges>   
  getPrivileges()
    throws PipelineException 
  {
    verifyConnection();
	 
    Object obj = performTransaction(MasterRequest.GetPrivileges, null);
    if(obj instanceof MiscGetPrivilegesRsp) {
      MiscGetPrivilegesRsp rsp = (MiscGetPrivilegesRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Change the administrative privileges for the given users. <P> 
   * 
   * This operation requires Master Admin privileges.
   * 
   * @param privs
   *   The privileges for each user indexed by user name.
   * 
   * @throws PipelineException
   *   If unable to set the privileges.
   */ 
  public synchronized void
  editPrivileges
  (
    TreeMap<String,Privileges> privs
  )
    throws PipelineException 
  {
    verifyConnection();

    MiscEditPrivilegesReq req = new MiscEditPrivilegesReq(privs);

    Object obj = performTransaction(MasterRequest.EditPrivileges, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the privileges granted to the current user with respect to all other users.
   * 
   * @return 
   *   The privileges of the current user.
   * 
   * @throws PipelineException
   *   If unable to determine the privileges.
   */
  public synchronized PrivilegeDetails
  getPrivilegeDetails()
    throws PipelineException 
  {
    return getPrivilegeDetails(PackageInfo.sUser);
  }

  /**
   * Get the privileges granted to a specific user with respect to all other users. <P> 
   * 
   * @param uname
   *   The unique name of the user.
   * 
   * @return 
   *   The privileges of the given user.
   * 
   * @throws PipelineException
   *   If unable to determine the privileges.
   */
  public synchronized PrivilegeDetails
  getPrivilegeDetails
  (
    String uname
  )
    throws PipelineException 
  {
    verifyConnection();
	 
    MiscGetPrivilegeDetailsReq req = new MiscGetPrivilegeDetailsReq(uname);

    Object obj = performTransaction(MasterRequest.GetPrivilegeDetails, req);
    if(obj instanceof MiscGetPrivilegeDetailsRsp) {
      MiscGetPrivilegeDetailsRsp rsp = (MiscGetPrivilegeDetailsRsp) obj;
      return rsp.getDetails();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current logging levels.
   */ 
  public synchronized LogControls
  getLogControls() 
    throws PipelineException 
  {
    verifyConnection();
	 
    Object obj = performTransaction(MasterRequest.GetLogControls, null);
    if(obj instanceof MiscGetLogControlsRsp) {
      MiscGetLogControlsRsp rsp = (MiscGetLogControlsRsp) obj;
      return rsp.getControls();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Set the current logging levels.
   */ 
  public synchronized void
  setLogControls
  (
   LogControls controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
	 
    MiscSetLogControlsReq req = new MiscSetLogControlsReq(controls);

    Object obj = performTransaction(MasterRequest.SetLogControls, req);
    handleSimpleResponse(obj);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   C O N T R O L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   */ 
  public synchronized MasterControls
  getRuntimeControls() 
    throws PipelineException 
  {
    verifyConnection();
	 
    Object obj = performTransaction(MasterRequest.GetMasterControls, null);
    if(obj instanceof MiscGetMasterControlsRsp) {
      MiscGetMasterControlsRsp rsp = (MiscGetMasterControlsRsp) obj;
      return rsp.getControls();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Set the current runtime performance controls.
   */ 
  public synchronized void
  setRuntimeControls
  (
   MasterControls controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
	 
    MiscSetMasterControlsReq req = new MiscSetMasterControlsReq(controls);

    Object obj = performTransaction(MasterRequest.SetMasterControls, req);
    handleSimpleResponse(obj);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default Unix toolset.
   * 
   * @throws PipelineException
   *   If unable to determine the default toolset name.
   */ 
  public synchronized String
  getDefaultToolsetName() 
    throws PipelineException
  {    
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetDefaultToolsetName, null);
    if(obj instanceof MiscGetDefaultToolsetNameRsp) {
      MiscGetDefaultToolsetNameRsp rsp = (MiscGetDefaultToolsetNameRsp) obj;
      return rsp.getName();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Set the default Unix toolset name. <P> 
   * 
   * Also makes the given toolset active if not already active. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param name
   *   The name of the new default toolset.
   * 
   * @throws PipelineException
   *   If unable to make the given toolset the default.
   */ 
  public synchronized void
  setDefaultToolsetName
  (
   String name
  ) 
    throws PipelineException
  {    
    verifyConnection();

    MiscSetDefaultToolsetNameReq req = new MiscSetDefaultToolsetNameReq(name);

    Object obj = performTransaction(MasterRequest.SetDefaultToolsetName, req);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently active Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public synchronized TreeSet<String>
  getActiveToolsetNames() 
    throws PipelineException
  {    
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetActiveToolsetNames, null);
    if(obj instanceof MiscGetActiveToolsetNamesRsp) {
      MiscGetActiveToolsetNamesRsp rsp = (MiscGetActiveToolsetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  
  /**
   * Set the active/inactive state of a Unix toolset. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.  The
   * current default toolset cannot be made inactive.
   * 
   * @param name
   *   The name of the toolset.
   *
   * @param isActive
   *   Whether the toolset should be active.
   * 
   * @throws PipelineException
   *   If unable to change the active state of the toolset.
   */ 
  public synchronized void
  setToolsetActive
  (
   String name, 
   boolean isActive
  ) 
    throws PipelineException
  {    
    verifyConnection();

    MiscSetToolsetActiveReq req = new MiscSetToolsetActiveReq(name, isActive);

    Object obj = performTransaction(MasterRequest.SetToolsetActive, req);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public synchronized TreeSet<String>
  getToolsetNames() 
    throws PipelineException
  {
    return getToolsetNames(OsType.Unix);
  }

  /**
   * Get the names of all toolsets for an operating system.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public synchronized TreeSet<String>
  getToolsetNames
  (
   OsType os
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetToolsetNamesReq req = new MiscGetToolsetNamesReq(os);

    Object obj = performTransaction(MasterRequest.GetToolsetNames, req);
    if(obj instanceof MiscGetToolsetNamesRsp) {
      MiscGetToolsetNamesRsp rsp = (MiscGetToolsetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Get the names of all toolsets for all operating systems.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public synchronized TreeMap<String,TreeSet<OsType>>
  getAllToolsetNames() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetAllToolsetNames, null);
    if(obj instanceof MiscGetAllToolsetNamesRsp) {
      MiscGetAllToolsetNamesRsp rsp = (MiscGetAllToolsetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a Unix toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  public synchronized Toolset
  getToolset
  (
   String name
  ) 
    throws PipelineException
  {
    return getToolset(name, OsType.Unix);
  }

  /**
   * Get an OS specific toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  public synchronized Toolset
  getToolset
  (
   String name, 
   OsType os
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetToolsetReq req = new MiscGetToolsetReq(name, os);

    Object obj = performTransaction(MasterRequest.GetToolset, req);
    if(obj instanceof MiscGetToolsetRsp) {
      MiscGetToolsetRsp rsp = (MiscGetToolsetRsp) obj;
      return rsp.getToolset();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get all OS specific toolsets with the given name. 
   * 
   * @param name
   *   The toolset name.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  public synchronized TreeMap<OsType,Toolset>
  getOsToolsets
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetOsToolsetsReq req = new MiscGetOsToolsetsReq(name);

    Object obj = performTransaction(MasterRequest.GetOsToolsets, req);
    if(obj instanceof MiscGetOsToolsetsRsp) {
      MiscGetOsToolsetsRsp rsp = (MiscGetOsToolsetsRsp) obj;
      return rsp.getToolsets();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the cooked Unix toolset environment. <P> 
   * 
   * If the <CODE>author</CODE> argument is not <CODE>null</CODE>, <CODE>HOME</CODE> and 
   * <CODE>USER</CODE> environmental variables will be added to the cooked environment. <P> 
   * 
   * If the <CODE>author</CODE> and <CODE>view</CODE> arguments are both not 
   * <CODE>null</CODE>, <CODE>HOME</CODE>, <CODE>USER</CODE> and <CODE>WORKING</CODE> 
   * environmental variables will be added to the cooked environment. <P> 
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  public synchronized TreeMap<String,String> 
  getToolsetEnvironment
  (
   String author, 
   String view,
   String tname
  ) 
    throws PipelineException
  {
    return getToolsetEnvironment(author, view, tname, OsType.Unix);
  }

  /**
   * Get the cooked OS specific toolset environment.
   * 
   * If the <CODE>author</CODE> argument is not <CODE>null</CODE>, <CODE>HOME</CODE> and 
   * <CODE>USER</CODE> environmental variables will be added to the cooked environment. <P> 
   * 
   * If the <CODE>author</CODE> and <CODE>view</CODE> arguments are both not 
   * <CODE>null</CODE>, <CODE>HOME</CODE>, <CODE>USER</CODE> and <CODE>WORKING</CODE> 
   * environmental variables will be added to the cooked environment. <P> 
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  public synchronized TreeMap<String,String> 
  getToolsetEnvironment
  (
   String author, 
   String view,
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetToolsetEnvironmentReq req = 
      new MiscGetToolsetEnvironmentReq(author, view, tname, os);

    Object obj = performTransaction(MasterRequest.GetToolsetEnvironment, req);
    if(obj instanceof MiscGetToolsetEnvironmentRsp) {
      MiscGetToolsetEnvironmentRsp rsp = (MiscGetToolsetEnvironmentRsp) obj;
      return rsp.getEnvironment();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new toolset from a set of toolset packages. <P>
   * 
   * A new toolset will only be created if the environment defined by evaluating 
   * the packages has no environment conflicts (see {@link Toolset#hasConflicts 
   * Toolset.hasConflicts}). <P> 
   * 
   * All packages given must already exist on the master server.  Only the names and 
   * revision numbers of the given packages are passed to the server which then looks up 
   * its own copy of the toolset package (identified by the name and revision number) to 
   * generate the toolset.  This insures that the contents of the toolset package cannot 
   * be altered by client programs. <P> 
   * 
   * The Unix specific toolset must be create before any other operating system 
   * specializations for the toolset can be created. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param name
   *   The name of the new toolset.
   * 
   * @param desc 
   *   The toolset description text.
   * 
   * @param packages
   *   The packages in order of evaluation.
   * 
   * @param os
   *   The operating system type.
   * 
   * @return 
   *   The newly created toolset.
   * 
   * @throws PipelineException
   *   If unable to create a new toolset.
   */
  public synchronized Toolset
  createToolset
  (
   String name, 
   String desc, 
   Collection<PackageVersion> packages,
   OsType os   
  ) 
    throws PipelineException  
  {
    verifyConnection();

    ArrayList<String> names = new ArrayList<String>();
    TreeMap<String,VersionID> versions = new TreeMap<String,VersionID>();
    for(PackageVersion pkg : packages) {
      names.add(pkg.getName());
      versions.put(pkg.getName(), pkg.getVersionID());
    }

    MiscCreateToolsetReq req =
      new MiscCreateToolsetReq(PackageInfo.sUser, name, desc, names, versions, os);

    Object obj = performTransaction(MasterRequest.CreateToolset, req); 
    if(obj instanceof MiscCreateToolsetRsp) {
      MiscCreateToolsetRsp rsp = (MiscCreateToolsetRsp) obj;
      return rsp.getToolset();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*  T O O L S E T   P A C K A G E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of all Unix toolset packages.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getToolsetPackageNames() 
    throws PipelineException    
  {
    return getToolsetPackageNames(OsType.Unix);
  }

  /**
   * Get the names and revision numbers of all OS specific toolset packages.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getToolsetPackageNames
  (
   OsType os
  ) 
    throws PipelineException    
  {
    verifyConnection();

    MiscGetToolsetPackageNamesReq req = new MiscGetToolsetPackageNamesReq(os);

    Object obj = performTransaction(MasterRequest.GetToolsetPackageNames, req);
    if(obj instanceof MiscGetToolsetPackageNamesRsp) {
      MiscGetToolsetPackageNamesRsp rsp = (MiscGetToolsetPackageNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the names and revision numbers of all toolset packages for all operating systems.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public synchronized DoubleMap<String,OsType,TreeSet<VersionID>>
  getAllToolsetPackageNames()
    throws PipelineException    
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetAllToolsetPackageNames, null);
    if(obj instanceof MiscGetAllToolsetPackageNamesRsp) {
      MiscGetAllToolsetPackageNamesRsp rsp = (MiscGetAllToolsetPackageNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a Unix toolset package. 
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @throws PipelineException
   *   If unable to find the toolset package.
   */ 
  public synchronized PackageVersion
  getToolsetPackage
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException    
  {
    return getToolsetPackage(name, vid, OsType.Unix);
  }

  /**
   * Get an OS specific toolset package. 
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to find the toolset package.
   */ 
  public synchronized PackageVersion
  getToolsetPackage
  (
   String name, 
   VersionID vid, 
   OsType os
  ) 
    throws PipelineException    
  {
    verifyConnection();

    MiscGetToolsetPackageReq req = new MiscGetToolsetPackageReq(name, vid, os);

    Object obj = performTransaction(MasterRequest.GetToolsetPackage, req);
    if(obj instanceof MiscGetToolsetPackageRsp) {
      MiscGetToolsetPackageRsp rsp = (MiscGetToolsetPackageRsp) obj;
      return rsp.getPackage();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get multiple OS specific toolset packages. 
   * 
   * @param packages
   *   The name, revision number and operating systems of the toolset packages to retrieve.
   * 
   * @return
   *   The packages indexed by name, revision number and operating system type.
   * 
   * @throws PipelineException
   *   If unable to find the toolset packages.
   */ 
  public synchronized TripleMap<String,VersionID,OsType,PackageVersion> 
  getToolsetPackages
  (
   DoubleMap<String,VersionID,TreeSet<OsType>> packages
  ) 
    throws PipelineException    
  {
    verifyConnection();

    if(packages.isEmpty()) 
      throw new PipelineException("No packages where specified!");

    MiscGetToolsetPackagesReq req = new MiscGetToolsetPackagesReq(packages);

    Object obj = performTransaction(MasterRequest.GetToolsetPackages, req);
    if(obj instanceof MiscGetToolsetPackagesRsp) {
      MiscGetToolsetPackagesRsp rsp = (MiscGetToolsetPackagesRsp) obj;
      return rsp.getPackages();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new read-only toolset package from a modifiable package. <P> 
   * 
   * Once created, the read-only package can not be altered and will remain accessable
   * forever.  Only read-only toolset packages can be used to create toolset, therefore
   * this method is a necessary prerequisite to building a new toolset from modifiable 
   * toolset packages. <P> 
   * 
   * The <CODE>level</CODE> argument may be <CODE>null</CODE> if this is the first 
   * revision of the package. <P> 
   * 
   * The Unix specific toolset must be create before any other operating system 
   * specializations for the toolset can be created. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param mod
   *   The source modifiable toolset package.
   * 
   * @param desc 
   *   The package description text.
   * 
   * @param level
   *   The revision number component level to increment.
   * 
   * @param os
   *   The operating system type.
   * 
   * @return 
   *   The newly created package.
   * 
   * @throws PipelineException
   *   If unable to create a new toolset packages.
   */
  public synchronized PackageVersion
  createToolsetPackage
  (
   PackageMod mod, 
   String desc, 
   VersionID.Level level, 
   OsType os
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscCreateToolsetPackageReq req =
      new MiscCreateToolsetPackageReq(PackageInfo.sUser, mod, desc, level, os);

    Object obj = performTransaction(MasterRequest.CreateToolsetPackage, req); 
    if(obj instanceof MiscCreateToolsetPackageRsp) {
      MiscCreateToolsetPackageRsp rsp = (MiscCreateToolsetPackageRsp) obj;
      return rsp.getPackage();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T   P L U G I N S  /  M E N U   L A Y O U T S                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the layout plugin menus for all plugin types associated with a toolset.<P> 
   * 
   * This is a convience method for getting the plugin menu layouts for all plugin 
   * types at one time.  It provides the same functionality as the get*MenuLayout() 
   * methods below, but avoids the network overhead of calling the more specialized 
   * methods individually.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of editor plugin menus indexed by plugin type.
   * 
   * @throws PipelineException
   *   If unable to determine the editor menu layout.
   */ 
  public synchronized TreeMap<PluginType,PluginMenuLayout> 
  getPluginMenuLayouts
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetPluginMenuLayouts, req); 
    if(obj instanceof MiscGetPluginMenuLayoutsRsp) {
      MiscGetPluginMenuLayoutsRsp rsp = (MiscGetPluginMenuLayoutsRsp) obj;
      return rsp.getLayouts();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Get all types of plugins associated with the given packages. <P> 
   * 
   * This is a convience method for getting the various plugin types for a large number
   * of package versions all at once.  It provides the same functionality as the 
   * getPackage*Plugins() methods below, but avoids the network overhead of calling the 
   * more specialized methods individually for each package.
   * 
   * @param packages
   *   The names and revision numbers of the packages.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated plugins indexed by
   *   the package names, revision numbers and plugin type.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized TripleMap<String,VersionID,PluginType,PluginSet>
  getSelectPackagePlugins
  (
   TreeMap<String,TreeSet<VersionID>> packages
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetSelectPackagePluginsReq req = new MiscGetSelectPackagePluginsReq(packages);

    Object obj = performTransaction(MasterRequest.GetSelectPackagePlugins, req);
    if(obj instanceof MiscGetSelectPackagePluginsRsp) {
      MiscGetSelectPackagePluginsRsp rsp = (MiscGetSelectPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get all types of plugins associated with the given packages. <P> 
   * 
   * This is a convience method for getting the various plugin types for specific 
   * package versions all at once.  It provides the same functionality as the 
   * getPackage*Plugins() methods below, but avoids the network overhead of calling the 
   * more specialized methods individually for each package.
   * 
   * @param pname
   *   The package name. 
   * 
   * @param pvid
   *   The package revision number.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated plugins indexed by
   *   plugin type.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized TreeMap<PluginType,PluginSet>
  getSelectPackagePlugins
  (
   String pname, 
   VersionID pvid 
  ) 
    throws PipelineException  
  {
    verifyConnection();

    if(pname == null) 
      throw new PipelineException
	("The package name cannot be (null)!"); 

    if(pvid == null) 
      throw new PipelineException
	("The package revisios number cannot be (null)!"); 

    TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
    TreeSet<VersionID> vids = new TreeSet<VersionID>();
    packages.put(pname, vids); 
    vids.add(pvid); 

    MiscGetSelectPackagePluginsReq req = new MiscGetSelectPackagePluginsReq(packages);

    Object obj = performTransaction(MasterRequest.GetSelectPackagePlugins, req);
    if(obj instanceof MiscGetSelectPackagePluginsRsp) {
      MiscGetSelectPackagePluginsRsp rsp = (MiscGetSelectPackagePluginsRsp) obj;
      TripleMap<String,VersionID,PluginType,PluginSet> psets = rsp.getPlugins();
      return psets.get(pname).get(pvid);
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the default layout of the editor plugin menu.
   * 
   * @return 
   *   The heirarchical set of editor plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the editor menu layout.
   */ 
  public synchronized PluginMenuLayout
  getEditorMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetEditorMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the editor plugin menu.
   * 
   * @param layout
   *   The heirarchical set of editor plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the editor menu layout.
   */ 
  public synchronized void 
  setEditorMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetEditorMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the editor plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of editor plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the editor menu layout.
   */ 
  public synchronized PluginMenuLayout
  getEditorMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetEditorMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the editor plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of editor plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the editor menu layout.
   */ 
  public synchronized void 
  setEditorMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetEditorMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the editor plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated editor plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetEditorPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetEditorPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the editor plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated editor plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageEditorPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageEditorPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the editor plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated editor plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageEditorPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageEditorPlugins, req); 
    handleSimpleResponse(obj); 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the comparator plugin menu.
   * 
   * @return 
   *   The heirarchical set of comparator plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the comparator menu layout.
   */ 
  public synchronized PluginMenuLayout
  getComparatorMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetComparatorMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the comparator plugin menu.
   * 
   * @param layout
   *   The heirarchical set of comparator plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the comparator menu layout.
   */ 
  public synchronized void 
  setComparatorMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetComparatorMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the comparator plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of comparator plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the comparator menu layout.
   */ 
  public synchronized PluginMenuLayout
  getComparatorMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetComparatorMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the comparator plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of comparator plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the comparator menu layout.
   */ 
  public synchronized void 
  setComparatorMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetComparatorMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the comparator plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated comparator plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetComparatorPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetComparatorPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the comparator plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated comparator plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageComparatorPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageComparatorPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the comparator plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated comparator plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageComparatorPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageComparatorPlugins, req); 
    handleSimpleResponse(obj); 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the action plugin menu.
   * 
   * @return 
   *   The heirarchical set of action plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the action menu layout.
   */ 
  public synchronized PluginMenuLayout
  getActionMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetActionMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the action plugin menu.
   * 
   * @param layout
   *   The heirarchical set of action plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the action menu layout.
   */ 
  public synchronized void 
  setActionMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetActionMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the action plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of action plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the action menu layout.
   */ 
  public synchronized PluginMenuLayout
  getActionMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetActionMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the action plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of action plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the action menu layout.
   */ 
  public synchronized void 
  setActionMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetActionMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the action plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated action plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetActionPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetActionPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the action plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated action plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageActionPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageActionPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the action plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated action plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageActionPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageActionPlugins, req); 
    handleSimpleResponse(obj); 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the tool plugin menu.
   * 
   * @return 
   *   The heirarchical set of tool plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the tool menu layout.
   */ 
  public synchronized PluginMenuLayout
  getToolMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetToolMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the tool plugin menu.
   * 
   * @param layout
   *   The heirarchical set of tool plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the tool menu layout.
   */ 
  public synchronized void 
  setToolMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetToolMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the tool plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of tool plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the tool menu layout.
   */ 
  public synchronized PluginMenuLayout
  getToolMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetToolMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the tool plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of tool plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the tool menu layout.
   */ 
  public synchronized void 
  setToolMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetToolMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the tool plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated tool plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetToolPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetToolPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the tool plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated tool plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageToolPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageToolPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the tool plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated tool plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageToolPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageToolPlugins, req); 
    handleSimpleResponse(obj); 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the archiver plugin menu.
   * 
   * @return 
   *   The heirarchical set of archiver plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the archiver menu layout.
   */ 
  public synchronized PluginMenuLayout
  getArchiverMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetArchiverMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the archiver plugin menu.
   * 
   * @param layout
   *   The heirarchical set of archiver plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the archiver menu layout.
   */ 
  public synchronized void 
  setArchiverMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetArchiverMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the archiver plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of archiver plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the archiver menu layout.
   */ 
  public synchronized PluginMenuLayout
  getArchiverMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetArchiverMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the archiver plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of archiver plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the archiver menu layout.
   */ 
  public synchronized void 
  setArchiverMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetArchiverMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the archiver plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated archiver plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetArchiverPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetArchiverPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the archiver plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated archiver plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageArchiverPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageArchiverPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the archiver plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated archiver plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageArchiverPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageArchiverPlugins, req); 
    handleSimpleResponse(obj); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the master extension plugin menu.
   * 
   * @return 
   *   The heirarchical set of master extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the master extension menu layout.
   */ 
  public synchronized PluginMenuLayout
  getMasterExtMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetMasterExtMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the master extension plugin menu.
   * 
   * @param layout
   *   The heirarchical set of master extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the master extension menu layout.
   */ 
  public synchronized void 
  setMasterExtMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetMasterExtMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the master extension plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of master extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the master extension menu layout.
   */ 
  public synchronized PluginMenuLayout
  getMasterExtMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetMasterExtMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the master extension plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of master extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the master extension menu layout.
   */ 
  public synchronized void 
  setMasterExtMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetMasterExtMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the master extension plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated master extension plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetMasterExtPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetMasterExtPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the master extension plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated master extension plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageMasterExtPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageMasterExtPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the master extension plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated master extension plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageMasterExtPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageMasterExtPlugins, req); 
    handleSimpleResponse(obj); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the queue extension plugin menu.
   * 
   * @return 
   *   The heirarchical set of queue extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the queue extension menu layout.
   */ 
  public synchronized PluginMenuLayout
  getQueueExtMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetQueueExtMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the queue extension plugin menu.
   * 
   * @param layout
   *   The heirarchical set of queue extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the queue extension menu layout.
   */ 
  public synchronized void 
  setQueueExtMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetQueueExtMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the queue extension plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of queue extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the queue extension menu layout.
   */ 
  public synchronized PluginMenuLayout
  getQueueExtMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetQueueExtMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the queue extension plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of queue extension plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the queue extension menu layout.
   */ 
  public synchronized void 
  setQueueExtMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetQueueExtMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the queue extension plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated queue extension plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetQueueExtPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetQueueExtPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the queue extension plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated queue extension plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageQueueExtPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageQueueExtPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the queue extension plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated queue extension plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageQueueExtPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageQueueExtPlugins, req); 
    handleSimpleResponse(obj); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the annotation plugin menu.
   * 
   * @return 
   *   The heirarchical set of annotation plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the annotation menu layout.
   */ 
  public synchronized PluginMenuLayout
  getAnnotationMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetAnnotationMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the annotation plugin menu.
   * 
   * @param layout
   *   The heirarchical set of annotation plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the annotation menu layout.
   */ 
  public synchronized void 
  setAnnotationMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetAnnotationMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the annotation plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of annotation plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the annotation menu layout.
   */ 
  public synchronized PluginMenuLayout
  getAnnotationMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetAnnotationMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the annotation plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of annotation plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the annotation menu layout.
   */ 
  public synchronized void 
  setAnnotationMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetAnnotationMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the annotation plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated annotation plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetAnnotationPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetAnnotationPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the annotation plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated annotation plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageAnnotationPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageAnnotationPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the annotation plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated annotation plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageAnnotationPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageAnnotationPlugins, req); 
    handleSimpleResponse(obj); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the key chooser plugin menu.
   * 
   * @return 
   *   The heirarchical set of key chooser plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the key chooser menu layout.
   */ 
  public synchronized PluginMenuLayout
  getKeyChooserMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetKeyChooserMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the key chooser plugin menu.
   * 
   * @param layout
   *   The heirarchical set of key chooser plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the key chooser menu layout.
   */ 
  public synchronized void 
  setKeyChooserMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetKeyChooserMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the key chooser plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The heirarchical set of key chooser plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the key chooser menu layout.
   */ 
  public synchronized PluginMenuLayout
  getKeyChooserMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetKeyChooserMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the key chooser plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The heirarchical set of key chooser plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the key chooser menu layout.
   */ 
  public synchronized void 
  setKeyChooserMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetKeyChooserMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the key chooser plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated key chooser plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetKeyChooserPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetKeyChooserPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the key chooser plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated key chooser plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageKeyChooserPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageKeyChooserPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the key chooser plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated key chooser plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageKeyChooserPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageKeyChooserPlugins, req); 
    handleSimpleResponse(obj); 
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default layout of the builder collection plugin menu.
   * 
   * @return 
   *   The hierarchical set of builder collection plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the builder collection menu layout.
   */ 
  public synchronized PluginMenuLayout
  getBuilderCollectionMenuLayout()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(null);

    Object obj = performTransaction(MasterRequest.GetBuilderCollectionMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the default layout of the builder collection plugin menu.
   * 
   * @param layout
   *   The hierarchical set of builder collection plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the builder collection menu layout.
   */ 
  public synchronized void 
  setBuilderCollectionMenuLayout
  (
   PluginMenuLayout layout
  ) 
    throws PipelineException      
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(null, layout);

    Object obj = performTransaction(MasterRequest.SetBuilderCollectionMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the layout of the builder collection plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The hierarchical set of builder collection plugin menus.
   * 
   * @throws PipelineException
   *   If unable to determine the builder collection menu layout.
   */ 
  public synchronized PluginMenuLayout
  getBuilderCollectionMenuLayout
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPluginMenuLayoutReq req = new MiscGetPluginMenuLayoutReq(name);

    Object obj = performTransaction(MasterRequest.GetBuilderCollectionMenuLayout, req); 
    if(obj instanceof MiscGetPluginMenuLayoutRsp) {
      MiscGetPluginMenuLayoutRsp rsp = (MiscGetPluginMenuLayoutRsp) obj;
      return rsp.getLayout();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Set the layout of the builder collection plugin menu associated with a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param layout
   *   The hierarchical set of builder collection plugin menus.
   * 
   * @throws PipelineException
   *   If unable to set the builder collection menu layout.
   */ 
  public synchronized void 
  setBuilderCollectionMenuLayout
  (
   String name, 
   PluginMenuLayout layout
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPluginMenuLayoutReq req = new MiscSetPluginMenuLayoutReq(name, layout);

    Object obj = performTransaction(MasterRequest.SetBuilderCollectionMenuLayout, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Get the builder collection plugins associated with all packages of a toolset.
   * 
   * @param name
   *   The toolset name.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated builder collection plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getToolsetBuilderCollectionPlugins
  (
   String name
  )
    throws PipelineException  
  {
    verifyConnection();

    MiscGetToolsetPluginsReq req = new MiscGetToolsetPluginsReq(name);

    Object obj = performTransaction(MasterRequest.GetToolsetBuilderCollectionPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Get the builder collection plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @return 
   *   The vendors, names and revision numbers of the associated builder collection plugins.
   * 
   * @throws PipelineException
   *   If unable to get the plugins.
   */ 
  public synchronized PluginSet
  getPackageBuilderCollectionPlugins
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetPackagePluginsReq req = new MiscGetPackagePluginsReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetPackageBuilderCollectionPlugins, req);
    if(obj instanceof MiscGetPackagePluginsRsp) {
      MiscGetPackagePluginsRsp rsp = (MiscGetPackagePluginsRsp) obj;
      return rsp.getPlugins();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the builder collection plugins associated with a toolset package.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the associated builder collection plugins.
   * 
   * @throws PipelineException
   *   If unable to set the plugins.
   */ 
  public synchronized void 
  setPackageBuilderCollectionPlugins
  ( 
   String name,  
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetPackagePluginsReq req = new MiscSetPackagePluginsReq(name, vid, plugins);

    Object obj = performTransaction(MasterRequest.SetPackageBuilderCollectionPlugins, req); 
    handleSimpleResponse(obj); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S E R V E R   E X T E N S I O N S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current master extension configurations. <P> 
   * 
   * @param name
   *   The name of the master extension configuration.
   * 
   * @return 
   *   The extension configuration 
   *   or <CODE>null</CODE> if no extension with the given name exists.
   * 
   * @throws PipelineException
   *   If unable to determine the extensions.
   */ 
  public synchronized MasterExtensionConfig
  getMasterExtensionConfig
  (
   String name
  ) 
    throws PipelineException  
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The extension configuration name cannot be (null)!");
    return getMasterExtensionConfigs().get(name);
  }

  /**
   * Get the current master extension configurations. <P> 
   * 
   * @return 
   *   The extension configurations indexed by configuration name.
   * 
   * @throws PipelineException
   *   If unable to determine the extensions.
   */ 
  public synchronized TreeMap<String,MasterExtensionConfig> 
  getMasterExtensionConfigs() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetMasterExtension, null); 
    if(obj instanceof MiscGetMasterExtensionsRsp) {
      MiscGetMasterExtensionsRsp rsp = (MiscGetMasterExtensionsRsp) obj;
      return rsp.getExtensions();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Remove an existing the master extension configuration. <P> 
   * 
   * @param name
   *   The name of the master extension configuration to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the extension.
   */ 
  public synchronized void
  removeMasterExtensionConfig
  (
   String name
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscRemoveMasterExtensionReq req = new MiscRemoveMasterExtensionReq(name);

    Object obj = performTransaction(MasterRequest.RemoveMasterExtension, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Add or modify an existing the master extension configuration. <P> 
   * 
   * @param extension
   *   The master extension configuration to add (or modify).
   * 
   * @throws PipelineException
   *   If unable to set the extension.
   */ 
  public synchronized void
  setMasterExtensionConfig
  (
   MasterExtensionConfig extension
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetMasterExtensionReq req = new MiscSetMasterExtensionReq(extension);

    Object obj = performTransaction(MasterRequest.SetMasterExtension, req); 
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   E D I T O R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current user's default editor plugin for a filename suffix. <P> 
   * 
   * @param suffix
   *   The filename suffix.
   * 
   * @return 
   *   The editor plugin instance or <CODE>null</CODE> if undefined.
   * 
   * @throws PipelineException
   *   If unable to determine the editor name.
   */ 
  public synchronized BaseEditor
  getEditorForSuffix
  (
   String suffix
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscGetEditorForSuffixReq req = 
      new MiscGetEditorForSuffixReq(PackageInfo.sUser, suffix);

    Object obj = performTransaction(MasterRequest.GetEditorForSuffix, req); 
    if(obj instanceof MiscGetEditorForSuffixRsp) {
      MiscGetEditorForSuffixRsp rsp = (MiscGetEditorForSuffixRsp) obj;
      return rsp.getEditor();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Get the current user's filename suffix to default editor mappings. <P> 
   * 
   * @return 
   *   The suffix/editor mappings.
   * 
   * @throws PipelineException
   *   If unable to determine the mappings.
   */ 
  public synchronized TreeSet<SuffixEditor> 
  getSuffixEditors()
    throws PipelineException  
  {
    verifyConnection();

    MiscGetSuffixEditorsReq req = new MiscGetSuffixEditorsReq(PackageInfo.sUser);

    Object obj = performTransaction(MasterRequest.GetSuffixEditors, req); 
    if(obj instanceof MiscGetSuffixEditorsRsp) {
      MiscGetSuffixEditorsRsp rsp = (MiscGetSuffixEditorsRsp) obj;
      return rsp.getEditors();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Get the site default filename suffix to default editor mappings. <P> 
   * 
   * @return 
   *   The suffix/editor mappings.
   * 
   * @throws PipelineException
   *   If unable to determine the mappings.
   */ 
  public synchronized TreeSet<SuffixEditor> 
  getDefaultSuffixEditors()
    throws PipelineException  
  {
    verifyConnection();
    
    MiscGetSuffixEditorsReq req = new MiscGetSuffixEditorsReq(PackageInfo.sPipelineUser);
    
    Object obj = performTransaction(MasterRequest.GetSuffixEditors, req); 
    if(obj instanceof MiscGetSuffixEditorsRsp) {
      MiscGetSuffixEditorsRsp rsp = (MiscGetSuffixEditorsRsp) obj;
      return rsp.getEditors();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

  /**
   * Set the current user's filename suffix to default editor mappings. <P> 
   * 
   * @param editors
   *   The suffix/editor mappings.
   * 
   * @throws PipelineException
   *   If unable to set the table.
   */ 
  public synchronized void
  setSuffixEditors
  (
   TreeSet<SuffixEditor> editors
  ) 
    throws PipelineException  
  {
    verifyConnection();

    MiscSetSuffixEditorsReq req = 
      new MiscSetSuffixEditorsReq(PackageInfo.sUser, editors);

    Object obj = performTransaction(MasterRequest.SetSuffixEditors, req); 
    handleSimpleResponse(obj);
  }
  
   

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   A R E A S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of current working area authors and views.
   *
   * @return 
   *   The table of working area view names indexed by author user name.
   *
   * @throws PipelineException
   *   If unable to determine the working areas.
   */
  public synchronized TreeMap<String,TreeSet<String>>
  getWorkingAreas() 
    throws PipelineException
  {
    verifyConnection();
	 
    Object obj = performTransaction(MasterRequest.GetWorkingAreas, null);
    if(obj instanceof NodeGetWorkingAreasRsp) {
      NodeGetWorkingAreasRsp rsp = (NodeGetWorkingAreasRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Get the table of the working areas containing the given node. <P> 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The table of working area view names indexed by author user name.  
   * 
   * @throws PipelineException
   *   If unable to determine the working areas.
   */
  public synchronized TreeMap<String,TreeSet<String>>
  getWorkingAreasContaining
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetWorkingAreasContaining, req);
    if(obj instanceof NodeGetWorkingAreasRsp) {
      NodeGetWorkingAreasRsp rsp = (NodeGetWorkingAreasRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Create a new empty working area. <P> 
   * 
   * If the working area already exists, the operation is successful even though 
   * nothing is actually done.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @throws PipelineException
   *   If unable to create the working area.
   */
  public synchronized void  
  createWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    verifyConnection();

    NodeCreateWorkingAreaReq req = new NodeCreateWorkingAreaReq(author, view);

    Object obj = performTransaction(MasterRequest.CreateWorkingArea, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove an entire working area. <P> 
   * 
   * This method will only succeed there are no working versions of any node currently
   * checked-out in the given working area.  All nodes must be released prior to removing 
   * the working area. <P> 
   * 
   * Any files not managed by Pipeline which exist in the working area directory will be 
   * deleted by this operation.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @throws PipelineException
   *   If unable to remove the working area.
   */
  public synchronized void  
  removeWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    verifyConnection();

    NodeRemoveWorkingAreaReq req = new NodeRemoveWorkingAreaReq(author, view);

    Object obj = performLongTransaction(MasterRequest.RemoveWorkingArea, req, 15000, 60000);  
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes who's name matches the given search pattern.
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> to match all nodes.
   * 
   * @return 
   *   The fully resolved names of the matching nodes. 
   * 
   * @throws PipelineException 
   *   If determine which working versions match the pattern.
   */ 
  public synchronized TreeSet<String> 
  getNodeNames
  (
   String pattern
  )
    throws PipelineException
  {
    verifyConnection();

    NodeGetNodeNamesReq req = new NodeGetNodeNamesReq(pattern);

    Object obj = performTransaction(MasterRequest.GetNodeNames, req);
    if(obj instanceof NodeGetNodeNamesRsp) {
      NodeGetNodeNamesRsp rsp = (NodeGetNodeNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the immediate children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * @param author 
   *   The of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param paths 
   *   The set of fully resolved node paths to update.
   * 
   * @throws PipelineException
   *   If unable to update the node paths.
   */
  public synchronized NodeTreeComp
  updatePaths
  (
   String author, 
   String view, 
   TreeSet<String> paths
  ) 
    throws PipelineException
  {
    TreeMap<String,Boolean> ipaths = new TreeMap<String,Boolean>();
    for(String path : paths) 
      ipaths.put(path, false);

    return updatePaths(author, view, ipaths);
  }


  /** 
   * Update the children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * @param author 
   *   The of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param paths 
   *   Whether to update all children (true) or only the immediate children (false) of the 
   *   given fully resolved node path indices.
   * 
   * @throws PipelineException
   *   If unable to update the node paths.
   */
  public synchronized NodeTreeComp
  updatePaths
  (
   String author, 
   String view, 
   TreeMap<String,Boolean> paths
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeUpdatePathsReq req = new NodeUpdatePathsReq(author, view, paths);

    Object obj = performTransaction(MasterRequest.UpdatePaths, req);
    if(obj instanceof NodeUpdatePathsRsp) {
      NodeUpdatePathsRsp rsp = (NodeUpdatePathsRsp) obj;
      return rsp.getRootComp();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the node associated with the given file. <P> 
   * 
   * @param path
   *   The fully resolved file path relative to the root working directory.
   * 
   * @return 
   *   The fully resolved node name or <CODE>null</CODE> if the file is not associated with
   *   any node.
   */ 
  public synchronized String
  getNodeOwning
  (
   String path
  ) 
    throws PipelineException    
  {
    verifyConnection();
	 
    NodeGetNodeOwningReq req = new NodeGetNodeOwningReq(path);

    Object obj = performTransaction(MasterRequest.GetNodeOwning, req);
    if(obj instanceof NodeGetNodeOwningRsp) {
      NodeGetNodeOwningRsp rsp = (NodeGetNodeOwningRsp) obj;
      return rsp.getName();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a specific per-node annotation for the given node.<P> 
   * 
   * @param nname 
   *   The fully resolved node name.
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @return 
   *   The named annotation for the node or <CODE>null</CODE> if none exists. 
   * 
   * @throws PipelineException 
   *   If unable to determine the annotations.
   */ 
  public synchronized BaseAnnotation
  getAnnotation
  (
   String nname, 
   String aname
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetAnnotationReq req = new NodeGetAnnotationReq(nname, aname); 

    Object obj = performTransaction(MasterRequest.GetAnnotation, req);
    if(obj instanceof NodeGetAnnotationRsp) {
      NodeGetAnnotationRsp rsp = (NodeGetAnnotationRsp) obj;
      return rsp.getAnnotation();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Get a unified view of a specific named annotation from both node and working version
   * of the node in a particular working area view.<P> 
   * 
   * If the named annotation exists as both a per-node and per-version annotation, then 
   * the per-version annotation will be returned without generating any warnings or
   * exceptions.  If the named annotation exists as only one of a per-node or per-version 
   * annotation, then which ever one exists will be be returned.  If neither exist, 
   * then <CODE>null</CODE> will be returned.<P> 
   * 
   * Note that this is merely a convienence method that provides a quicker way of 
   * looking up both per-node annotations and lookup the working version of a node and
   * then accessing its annotation properties.  No new functionality is provided by
   * this method, but it may be faster when needing a unified view of all annotations
   * and other working version information is not required.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param nname 
   *   The fully resolved node name.
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @return 
   *   The named annotation for the node or <CODE>null</CODE> if none exists. 
   * 
   * @throws PipelineException 
   *   If no working version of the new exists or otherwise unable to determine the 
   *   annotations.
   */ 
  public BaseAnnotation
  getAnnotation
  (
   String author, 
   String view, 
   String nname, 
   String aname
  ) 
    throws PipelineException
  {
    return getAnnotation(new NodeID(author, view, nname), aname);
  }

  /**
   * Get a unified view of a specific named annotation from both node and working version
   * of the node in a particular working area view.<P> 
   * 
   * If the named annotation exists as both a per-node and per-version annotation, then 
   * the per-version annotation will be returned without generating any warnings or
   * exceptions.  If the named annotation exists as only one of a per-node or per-version 
   * annotation, then which ever one exists will be be returned.  If neither exist, 
   * then <CODE>null</CODE> will be returned.<P> 
   * 
   * Note that this is merely a convienence method that provides a quicker way of 
   * looking up both per-node annotations and lookup the working version of a node and
   * then accessing its annotation properties.  No new functionality is provided by
   * this method, but it may be faster when needing a unified view of all annotations
   * and other working version information is not required.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @return 
   *   The named annotation for the node or <CODE>null</CODE> if none exists. 
   * 
   * @throws PipelineException 
   *   If no working version of the new exists or otherwise unable to determine the 
   *   annotations.
   */ 
  public BaseAnnotation
  getAnnotation
  (
   NodeID nodeID,
   String aname
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetBothAnnotationReq req = new NodeGetBothAnnotationReq(nodeID, aname); 

    Object obj = performTransaction(MasterRequest.GetBothAnnotation, req);
    if(obj instanceof NodeGetAnnotationRsp) {
      NodeGetAnnotationRsp rsp = (NodeGetAnnotationRsp) obj;
      return rsp.getAnnotation();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get all of the per-node annotations for the given node.<P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return 
   *   The annotations for the node indexed by annotation name (may be empty).
   * 
   * @throws PipelineException 
   *   If unable to determine the annotations.
   */ 
  public synchronized TreeMap<String,BaseAnnotation> 
  getAnnotations
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetAnnotations, req);
    if(obj instanceof NodeGetAnnotationsRsp) {
      NodeGetAnnotationsRsp rsp = (NodeGetAnnotationsRsp) obj;
      return rsp.getAnnotations();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Get a unified view of all annotation from both node and working version
   * of the node in a particular working area view.<P> 
   * 
   * If a given annotation exists as both a per-node and per-version annotation, then 
   * the per-version annotation will be returned without generating any warnings or
   * exceptions.  If a given annotation exists as only one of a per-node or per-version 
   * annotation, then which ever one exists will be be returned. <P> 
   * 
   * Note that this is merely a convienence method that provides a quicker way of 
   * looking up both per-node annotations and lookup the working version of a node and
   * then accessing its annotation properties.  No new functionality is provided by
   * this method, but it may be faster when needing a unified view of all annotations
   * and other working version information is not required.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return 
   *   The annotations for the node indexed by annotation name (may be empty).
   * 
   * @throws PipelineException 
   *   If no working version of the new exists or otherwise unable to determine the 
   *   annotations.
   */ 
  public TreeMap<String,BaseAnnotation> 
  getAnnotations
  (
   String author,
   String view, 
   String name
  ) 
    throws PipelineException
  {
    return getAnnotations(new NodeID(author, view, name));
  }
  
  /**
   * Get a unified view of all annotation from both node and working version
   * of the node in a particular working area view.<P> 
   * 
   * If a given annotation exists as both a per-node and per-version annotation, then 
   * the per-version annotation will be returned without generating any warnings or
   * exceptions.  If a given annotation exists as only one of a per-node or per-version 
   * annotation, then which ever one exists will be be returned. <P> 
   * 
   * Note that this is merely a convienence method that provides a quicker way of 
   * looking up both per-node annotations and lookup the working version of a node and
   * then accessing its annotation properties.  No new functionality is provided by
   * this method, but it may be faster when needing a unified view of all annotations
   * and other working version information is not required.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @return 
   *   The annotations for the node indexed by annotation name (may be empty).
   * 
   * @throws PipelineException 
   *   If no working version of the new exists or otherwise unable to determine the 
   *   annotations.
   */ 
  public TreeMap<String,BaseAnnotation> 
  getAnnotations
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetBothAnnotationsReq req = new NodeGetBothAnnotationsReq(nodeID); 

    Object obj = performTransaction(MasterRequest.GetBothAnnotations, req);
    if(obj instanceof NodeGetAnnotationsRsp) {
      NodeGetAnnotationsRsp rsp = (NodeGetAnnotationsRsp) obj;
      return rsp.getAnnotations();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Get a unified view of all annotation from both node and working version
   * for a set of nodes in working areas.<P> 
   * 
   * If a given annotation exists as both a per-node and per-version annotation, then 
   * the per-version annotation will be returned without generating any warnings or
   * exceptions.  If a given annotation exists as only one of a per-node or per-version 
   * annotation, then which ever one exists will be be returned. <P> 
   * 
   * Note that this is merely a convienence method that provides a quicker way of 
   * looking up both per-node annotations and lookup the working version of a node and
   * then accessing its annotation properties.  No new functionality is provided by
   * this method, but it may be faster when needing a unified view of all annotations
   * and other working version information is not required.
   * 
   * @param nodeIDs 
   *   The set of unique working version identifier. 
   * 
   * @return 
   *   The annotations for the node indexed by node id and annotation name (may be empty).
   * 
   * @throws PipelineException 
   *   If a working version for at least one of the node does not exists or if Pipeline is 
   *   otherwise unable to determine the annotations.
   */ 
  public DoubleMap<NodeID, String,BaseAnnotation> 
  getAnnotations
  (
    TreeSet<NodeID> nodeIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetAllBothAnnotationsReq req = new NodeGetAllBothAnnotationsReq(nodeIDs); 

    Object obj = performTransaction(MasterRequest.GetAllBothAnnotations, req);
    if(obj instanceof NodeGetAllAnnotationsRsp) {
      NodeGetAllAnnotationsRsp rsp = (NodeGetAllAnnotationsRsp) obj;
      return rsp.getAnnotations();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given annotation to the set of current annotations for the given node.<P> 
   * 
   * If an annotation with the same name already exists for the node, it will be replaced
   * by this new annotation.  This method can therefore be used to modify existing annotations
   * as well as add new ones.<P> 
   * 
   * When adding new annotations to a node or replacing an existing annotation with a plugin
   * instance with a different plugin name/version/vendor, this method will fail if the 
   * current user does not have Annotator privileges.  When only updating the values for an 
   * existing annotation, the {@link BaseAnnotation#isParamModifiable 
   * BaseAnnotation.isParamModifiable} method of the existing annotation will be used to 
   * determine whether the user has permission to modify each annotation parameter.  Only 
   * parameters with values not identical to those in the existing annotation will perform
   * this test.  Identical values will simply be ignored.
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   * 
   * @param annot 
   *   The new node annotation to add.
   *
   * @throws PipelineException 
   *   If unable to add the annotation.
   */ 
  public synchronized void
  addAnnotation
  (
   String nname, 
   String aname, 
   BaseAnnotation annot 
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeAddAnnotationReq req = new NodeAddAnnotationReq(nname, aname, annot); 

    Object obj = performTransaction(MasterRequest.AddAnnotation, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove a specific annotation from a node. <P> 
   * 
   * This method will fail if the current user does not have Annotator privileges. 
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   * 
   * @throws PipelineException 
   *   If unable to remove the annotation.
   */ 
  public synchronized void
  removeAnnotation
  (
   String nname,
   String aname
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeRemoveAnnotationReq req = new NodeRemoveAnnotationReq(nname, aname); 

    Object obj = performTransaction(MasterRequest.RemoveAnnotation, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove all annotations from a node. <P> 
   * 
   * This method will fail if the current user does not have Annotator privileges. 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException 
   *   If unable to remove the annotations.
   */ 
  public synchronized void
  removeAnnotations
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeRemoveAnnotationsReq req = new NodeRemoveAnnotationsReq(name);

    Object obj = performTransaction(MasterRequest.RemoveAnnotations, req);
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the nodes in a working matching the given search pattern.
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> to match all nodes.
   * 
   * @return 
   *   The fully resolved names of the matching working versions. 
   * 
   * @throws PipelineException 
   *   If unable to determine which working versions match the pattern.
   */ 
  public synchronized TreeSet<String> 
  getWorkingNames
  (
   String author, 
   String view, 
   String pattern
  )
    throws PipelineException
  {
    verifyConnection();

    NodeGetWorkingNamesReq req = 
      new NodeGetWorkingNamesReq(author, view, pattern);

    Object obj = performTransaction(MasterRequest.GetWorkingNames, req);
    if(obj instanceof NodeGetNodeNamesRsp) {
      NodeGetNodeNamesRsp rsp = (NodeGetNodeNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /** 
   * Get the working version of a node. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException
   *   If unable to retrieve the working version.
   */
  public synchronized NodeMod
  getWorkingVersion
  ( 
   String author, 
   String view, 
   String name
  ) 
    throws PipelineException
  {
    return getWorkingVersion(new NodeID(author, view, name));
  }  

  /** 
   * Get the working version of a node. <P> 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   *
   * @throws PipelineException
   *   If unable to retrieve the working version.
   */
  public synchronized NodeMod
  getWorkingVersion
  ( 
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetWorkingReq req = new NodeGetWorkingReq(nodeID);

    Object obj = performTransaction(MasterRequest.GetWorking, req);
    if(obj instanceof NodeGetWorkingRsp) {
      NodeGetWorkingRsp rsp = (NodeGetWorkingRsp) obj;
      return rsp.getNodeMod();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set the node properties of the working version of a node. <P> 
   * 
   * Node properties include: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   The toolset environment under which editors and actions are run. <BR>
   *   The name of the editor plugin used to edit the data files associated with the node.<BR>
   *   The regeneration action and its single and per-source parameters. <BR>
   *   The overflow policy, execution method and job batch size. <BR> 
   *   The job requirements. <P>
   * </DIV> 
   * 
   * Note that any links to upstream nodes contain in the <CODE>mod</CODE> argument will be 
   * ignored.  The {@link #link link} and {@link #unlink unlink} methods must be used to 
   * alter the connections between working node versions. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param mod 
   *   The working version containing the node property information to copy.
   * 
   * @throws PipelineException
   *   If unable to set the node properties.
   */
  public synchronized void 
  modifyProperties
  ( 
   String author, 
   String view, 
   NodeMod mod   
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(author, view, mod.getName());
    NodeModifyPropertiesReq req = new NodeModifyPropertiesReq(id, mod);

    Object obj = performTransaction(MasterRequest.ModifyProperties, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * If the <CODE>relationship</CODE> argument is <CODE>OneToOne</CODE> then the 
   * <CODE>offset</CODE> argument must not be <CODE>null</CODE>.  For all other 
   * link relationships, the <CODE>offset</CODE> argument must be <CODE>null</CODE>.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to connect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to connect.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @param relationship 
   *   The nature of the relationship between files associated with the source and 
   *   target nodes. 
   * 
   * @param offset 
   *   The frame index offset.
   * 
   * @throws PipelineException
   *   If unable to create or modify the link.
   */
  public synchronized void 
  link
  (
   String author, 
   String view, 
   String target, 
   String source,
   LinkPolicy policy,
   LinkRelationship relationship,  
   Integer offset
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(author, view, target);
    LinkMod link = new LinkMod(source, policy, relationship, offset);
    NodeLinkReq req = new NodeLinkReq(id, link);

    Object obj = performTransaction(MasterRequest.Link, req);
    handleSimpleResponse(obj);
  } 

  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * The LinkRelationship defaults to <CODE>All</CODE> with no frame offset. Use the other 
   * form of {@link #link} to specify a different LinkRelationship and frame offset.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to connect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to connect.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @throws PipelineException
   *   If unable to create or modify the link.
   */
  public synchronized void 
  link
  (
   String author, 
   String view, 
   String target, 
   String source,
   LinkPolicy policy
  ) 
    throws PipelineException
  {
    link(author, view, target, source, policy, LinkRelationship.All, null);
  } 

  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to connect.
   * 
   * @param link 
   *   Information about the relationship between the downstream node and the upstream 
   *   node to connect.
   * 
   * @throws PipelineException
   *   If unable to create or modify the link.
   */
  public synchronized void 
  link
  (
   String author, 
   String view, 
   String target, 
   LinkCommon link
  ) 
    throws PipelineException
  {
    link(author, view, target, 
         link.getName(), link.getPolicy(), link.getRelationship(), link.getFrameOffset());
  } 

  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * @param target
   *   The unique working version identifier of the downstream node to connect.
   * 
   * @param link 
   *   Information about the relationship between downstream node and the upstream 
   *   node to connect.
   * 
   * @throws PipelineException
   *   If unable to create or modify the link.
   */
  public synchronized void 
  link
  (
   NodeID target, 
   LinkCommon link
  ) 
    throws PipelineException
  {
    link(target.getAuthor(), target.getView(), target.getName(), 
         link.getName(), link.getPolicy(), link.getRelationship(), link.getFrameOffset());
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Destroy an existing link between the working versions. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to disconnect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to disconnect.
   * 
   * @throws PipelineException
   *   If unable to destroy the link.
   */
  public synchronized void 
  unlink
  (
   String author, 
   String view, 
   String target, 
   String source
  )
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(author, view, target);
    NodeUnlinkReq req = new NodeUnlinkReq(id, source);

    Object obj = performTransaction(MasterRequest.Unlink, req);
    handleSimpleResponse(obj);
  } 

  /**
   * Destroy an existing link between the working versions. <P> 
   * 
   * @param target
   *   The unique working version identifier of the downstream node to disconnect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to disconnect.
   * 
   * @throws PipelineException
   *   If unable to destroy the link.
   */
  public synchronized void 
  unlink
  (
   NodeID target, 
   String source
  )
    throws PipelineException
  {
    unlink(target.getAuthor(), target.getView(), target.getName(), source);
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a secondary file sequence to a working version.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The secondary file sequence to add.
   * 
   * @throws PipelineException
   *   If unable to add the file sequence.
   */
  public synchronized void 
  addSecondary
  (
   String author, 
   String view, 
   String name, 
   FileSeq fseq
  )
    throws PipelineException
  {
    addSecondary(new NodeID(author, view, name), fseq);
  } 
  
  /**
   * Add a secondary file sequence to a working version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to add.
   * 
   * @throws PipelineException
   *   If unable to add the file sequence.
   */
  public synchronized void 
  addSecondary
  (
   NodeID nodeID,
   FileSeq fseq
  )
    throws PipelineException
  {
    verifyConnection();

    NodeAddSecondaryReq req = new NodeAddSecondaryReq(nodeID, fseq);

    Object obj = performTransaction(MasterRequest.AddSecondary, req);
    handleSimpleResponse(obj);
  } 


  /**
   * Remove a secondary file sequence from a working version.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the file sequence.
   */
  public synchronized void 
  removeSecondary
  (
   String author, 
   String view, 
   String name, 
   FileSeq fseq
  )
    throws PipelineException
  {
    removeSecondary(new NodeID(author, view, name), fseq);
  } 
    
  /**
   * Remove a secondary file sequence from a working version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the file sequence.
   */
  public synchronized void 
  removeSecondary
  (
   NodeID nodeID,
   FileSeq fseq
  )
    throws PipelineException
  {
    verifyConnection();

    NodeRemoveSecondaryReq req = new NodeRemoveSecondaryReq(nodeID, fseq);

    Object obj = performTransaction(MasterRequest.RemoveSecondary, req);
    handleSimpleResponse(obj);
  } 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename a working version of a node which has never been checked-in. <P> 
   * 
   * This operation allows a user to change the name, frame number padding and suffix of a 
   * previously registered node before it is checked-in. If a working version is successfully 
   * renamed, all node connections will be preserved. <P> 
   * 
   * The new primary file sequence pattern passed as the <CODE>pattern</CODE> parameter
   * must have a fully resolved prefix which is used to determine the new name of the 
   * node.  If the original node had a frame number component, the new file pattern must
   * also include a frame number component. <P> 
   * 
   * In addition to changing the name of the working version, this operation can also 
   * rename the files associated with the working version to match the new node's file 
   * pattern if the <CODE>renameFiles</CODE> argument is <CODE>true</CODE>.  The primary 
   * file sequence will be renamed to match tne new file pattern.  The secondary file 
   * sequence prefixes will remain unchanged.  Both primary and secondary file sequences 
   * will be moved into the working directory which matches the new file pattern prefix. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name
   *   The current fully resolved node name.
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   * 
   * @param renameFiles 
   *   Should the files associated with the working version be renamed?
   * 
   * @throws PipelineException 
   *   If unable to rename the given node or its associated primary files.
   */ 
  public synchronized void 
  rename
  ( 
   String author, 
   String view, 
   String name, 
   FilePattern pattern,
   boolean renameFiles
  ) 
    throws PipelineException
  {
    rename(new NodeID(author, view, name), pattern, renameFiles);
  } 

  /**
   * Rename a working version of a node which has never been checked-in. <P> 
   * 
   * This operation allows a user to change the name, frame number padding and suffix of a 
   * previously registered node before it is checked-in. If a working version is successfully 
   * renamed, all node connections will be preserved. <P> 
   * 
   * The new primary file sequence pattern passed as the <CODE>pattern</CODE> parameter
   * must have a fully resolved prefix which is used to determine the new name of the 
   * node.  If the original node had a frame number component, the new file pattern must
   * also include a frame number component. <P> 
   * 
   * In addition to changing the name of the working version, this operation can also 
   * rename the files associated with the working version to match the new node's file 
   * pattern if the <CODE>renameFiles</CODE> argument is <CODE>true</CODE>.  The primary 
   * file sequence will be renamed to match tne new file pattern.  The secondary file 
   * sequence prefixes will remain unchanged.  Both primary and secondary file sequences 
   * will be moved into the working directory which matches the new file pattern prefix. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   * 
   * @param renameFiles 
   *   Should the files associated with the working version be renamed?
   * 
   * @throws PipelineException 
   *   If unable to rename the given node or its associated primary files.
   */ 
  public synchronized void 
  rename
  ( 
   NodeID nodeID, 
   FilePattern pattern, 
   boolean renameFiles
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeRenameReq req = new NodeRenameReq(nodeID, pattern, renameFiles);

    Object obj = performTransaction(MasterRequest.Rename, req);
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Renumber the frame ranges of the file sequences associated with a node. <P> 
   * 
   * See {@link NodeMod#adjustFrameRange adjustFrameRange} for the constraints on legal 
   * values for the given new frame range argument <CODE>range</CODE>.
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param range 
   *   The new frame range.
   * 
   * @param removeFiles 
   *   Whether to remove files from the old frame range which are no longer part of the new 
   *   frame range.
   * 
   * @return
   *   The job IDs of unfinished jobs associated with the obsolete frames or
   *   <CODE>null</CODE> if no jobs where found.
   * 
   * @throws PipelineException 
   *   If unable to renumber the given node or its associated primary files.
   */ 
  public synchronized TreeSet<Long>
  renumber
  ( 
   String author, 
   String view, 
   String name, 
   FrameRange range, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    return renumber(new NodeID(author, view, name), range, removeFiles);
  } 

  /**
   * Renumber the frame ranges of the file sequences associated with a node. <P> 
   * 
   * See {@link NodeMod#adjustFrameRange adjustFrameRange} for the constraints on legal 
   * values for the given new frame range argument <CODE>range</CODE>.
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   *
   * @param range 
   *   The new frame range.
   * 
   * @param removeFiles 
   *   Whether to remove files from the old frame range which are no longer part of the new 
   *   frame range.
   * 
   * @return
   *   The job IDs of unfinished jobs associated with the obsolete frames or
   *   <CODE>null</CODE> if no jobs where found.
   * 
   * @throws PipelineException 
   *   If unable to renumber the given node or its associated primary files.
   */ 
  public synchronized TreeSet<Long>
  renumber
  ( 
   NodeID nodeID,
   FrameRange range, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeRenumberReq req = new NodeRenumberReq(nodeID, range, removeFiles);

    Object obj = performTransaction(MasterRequest.Renumber, req); 
    if(obj instanceof GetUnfinishedJobsForNodeFilesRsp) {
      GetUnfinishedJobsForNodeFilesRsp rsp = (GetUnfinishedJobsForNodeFilesRsp) obj;
      return rsp.getJobIDs();
    }
    else if(obj instanceof SuccessRsp) {
      return null;
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the nodes with at least one checked-in version who's name matches 
   * the given search pattern.
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> to match all nodes.
   * 
   * @return 
   *   The fully resolved names of the matching checked-in versions. 
   * 
   * @throws PipelineException 
   *   If determine which working versions match the pattern.
   */ 
  public synchronized TreeSet<String> 
  getCheckedInNames
  (
   String pattern
  )
    throws PipelineException
  {
    verifyConnection();

    NodeGetNodeNamesReq req = new NodeGetNodeNamesReq(pattern);

    Object obj = performTransaction(MasterRequest.GetCheckedInNames, req);
    if(obj instanceof NodeGetNodeNamesRsp) {
      NodeGetNodeNamesRsp rsp = (NodeGetNodeNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /** 
   * Get the revision numbers of all checked-in versions of a node. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException
   *   If unable to retrieve the checked-in versions.
   */
  public synchronized TreeSet<VersionID>
  getCheckedInVersionIDs
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetCheckedInVersionIDs, req);
    if(obj instanceof NodeGetVersionIDsRsp) {
      NodeGetVersionIDsRsp rsp = (NodeGetVersionIDsRsp) obj;
      return rsp.getVersionIDs();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /** 
   * Get the revision numbers of all checked-in versions of a node do not save 
   * intermediate (temporary) version of files in the repository. <P>
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException
   *   If unable to retrieve the checked-in versions.
   */
  public synchronized TreeSet<VersionID>
  getIntermediateVersionIDs
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetIntermediateVersionIDs, req);
    if(obj instanceof NodeGetVersionIDsRsp) {
      NodeGetVersionIDsRsp rsp = (NodeGetVersionIDsRsp) obj;
      return rsp.getVersionIDs();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /** 
   * Get the checked-in version of a node. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param vid
   *   The revision number of the checked-in version or <CODE>null</CODE> for the latest 
   *   version.
   * 
   * @throws PipelineException
   *   If unable to retrieve the checked-in version.
   */
  public synchronized NodeVersion
  getCheckedInVersion
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetCheckedInReq req = new NodeGetCheckedInReq(name, vid);

    Object obj = performTransaction(MasterRequest.GetCheckedIn, req);
    if(obj instanceof NodeGetCheckedInRsp) {
      NodeGetCheckedInRsp rsp = (NodeGetCheckedInRsp) obj;
      return rsp.getNodeVersion();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /** 
   * Get all of the checked-in versions of a node. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return 
   *   The checked-in versions indexed by revision number.
   * 
   * @throws PipelineException
   *   If unable to retrieve the checked-in version.
   */
  public synchronized TreeMap<VersionID,NodeVersion> 
  getAllCheckedInVersions
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetAllCheckedIn, req);
    if(obj instanceof NodeGetAllCheckedInRsp) {
      NodeGetAllCheckedInRsp rsp = (NodeGetAllCheckedInRsp) obj;
      return rsp.getNodeVersions();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /** 
   * Get the log messages associated with all checked-in versions of a node.
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @return 
   *   The log messages indexed by revision number.
   * 
   * @throws PipelineException
   *   If unable to retrieve the log messages.
   */
  public synchronized TreeMap<VersionID,LogMessage> 
  getHistory
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetHistory, req);
    if(obj instanceof NodeGetHistoryRsp) {
      NodeGetHistoryRsp rsp = (NodeGetHistoryRsp) obj;
      return rsp.getHistory();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /**
   * Get whether each file associated with each checked-in version of a node 
   * contains new data not present in the previous checked-in versions. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @return 
   *   The per-file novelty flags indexed by revision number and file sequence.
   * 
   * @throws PipelineException
   *   If unable to determine the file revision history.
   */
  public synchronized TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>
  getCheckedInFileNovelty
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeGetByNameReq req = new NodeGetByNameReq(name);
    
    Object obj = performTransaction(MasterRequest.GetCheckedInFileNovelty, req);
    if(obj instanceof NodeGetCheckedInFileNoveltyRsp) {
      NodeGetCheckedInFileNoveltyRsp rsp = (NodeGetCheckedInFileNoveltyRsp) obj;
      return rsp.getFileNovelty();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }    
  
  /**
   * Get the upstream links of all checked-in versions of a node.
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @return 
   *   The checked-in links indexed by revision number of the target node and the node 
   *   name of the upstream node.
   * 
   * @throws PipelineException
   *   If unable to determine the checked-in links.
   */
  public synchronized DoubleMap<VersionID,String,LinkVersion>
  getCheckedInLinks
  ( 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeGetByNameReq req = new NodeGetByNameReq(name);
    
    Object obj = performTransaction(MasterRequest.GetCheckedInLinks, req);
    if(obj instanceof NodeGetCheckedInLinksRsp) {
      NodeGetCheckedInLinksRsp rsp = (NodeGetCheckedInLinksRsp) obj;
      return rsp.getLinks();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }    

  /**
   * Get the links from specific checked-in version to all other checked-in 
   * node versions downstream. 
   * 
   * @param name 
   *   The fully resolved name of the upstream node.
   *
   * @param vid 
   *   The revision number of the checked-in upstream node.
   *
   * @return 
   *   The checked-in links indexed by the name and revision number of the downstream node. 
   * 
   * @throws PipelineException
   *   If unable to determine the checked-in links.
   */
  public synchronized DoubleMap<String,VersionID,LinkVersion>
  getDownstreamCheckedInLinks
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeGetDownstreamCheckedInLinksReq req = 
      new NodeGetDownstreamCheckedInLinksReq(name, vid);
    
    Object obj = performTransaction(MasterRequest.GetDownstreamCheckedInLinks, req);
    if(obj instanceof NodeGetDownstreamCheckedInLinksRsp) {
      NodeGetDownstreamCheckedInLinksRsp rsp = (NodeGetDownstreamCheckedInLinksRsp) obj;
      return rsp.getLinks();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }    



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T U S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the heavyweight upstream status of the tree of nodes rooted at a node. <P> 
   * 
   * The status information for all nodes upstream of and including the root node will include
   * detailed per-file state and version information which is accessable by calling the 
   * {@link NodeStatus#getHeavyDetails NodeStatus.getHeavyDetails} method.  No downstream 
   * node status will be returned.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public synchronized NodeStatus
  status
  ( 
   String author, 
   String view, 
   String name
  ) 
    throws PipelineException
  {
    return status(new NodeID(author, view, name), false, DownstreamMode.None);
  } 

  /** 
   * Get the heavyweight upstream status of the tree of nodes rooted at a node. <P> 
   * 
   * The status information for all nodes upstream of and including the root node will include
   * detailed per-file state and version information which is accessable by calling the 
   * {@link NodeStatus#getHeavyDetails NodeStatus.getHeavyDetails} method.  No downstream 
   * node status will be returned.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public synchronized NodeStatus
  status
  ( 
   NodeID nodeID  
  ) 
    throws PipelineException
  {
    return status(nodeID, false, DownstreamMode.None);
  } 

  /** 
   * Get the status of the tree of nodes rooted at a node. <P> 
   * 
   * In addition to providing node status information for the given node, the returned 
   * <CODE>NodeStatus</CODE> instance can be used access the status of all nodes (both 
   * upstream and downstream) linked to the given node.  The status information for the 
   * upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getLightDetails NodeStatus.getLightDetails}
   * method or the {@link NodeStatus#getHeavyDetails NodeStatus.getHeavyDetails} depending
   * on whether lightweight or heavyweight details have been requested.  
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param lightweight
   *   Whether to report only lightweight node status detail information for the 
   *   upstream nodes.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public synchronized NodeStatus
  status
  ( 
   String author, 
   String view, 
   String name, 
   boolean lightweight, 
   DownstreamMode dmode
  ) 
    throws PipelineException
  {
    return status(new NodeID(author, view, name), lightweight, dmode);
  } 

  /** 
   * Get the status of the tree of nodes rooted at a node. <P> 
   * 
   * In addition to providing node status information for the given node, the returned 
   * <CODE>NodeStatus</CODE> instance can be used access the status of all nodes (both 
   * upstream and downstream) linked to the given node.  The status information for the 
   * upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getLightDetails NodeStatus.getLightDetails}
   * method or the {@link NodeStatus#getHeavyDetails NodeStatus.getHeavyDetails} depending
   * on whether lightweight or heavyweight details have been requested.  
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param lightweight
   *   Whether to report only lightweight node status detail information for the 
   *   upstream nodes.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public synchronized NodeStatus
  status
  ( 
   NodeID nodeID, 
   boolean lightweight, 
   DownstreamMode dmode
  ) 
    throws PipelineException
  {
    verifyConnection();
 
    NodeStatusReq req = new NodeStatusReq(nodeID, lightweight, dmode);

    Object obj = performTransaction(MasterRequest.Status, req);
    if(obj instanceof NodeStatusRsp) {
      NodeStatusRsp rsp = (NodeStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
       return null;
    }
  } 

  /** 
   * Get the status of multiple possibly overlapping trees of nodes. <P> 
   * 
   * For each of the root nodes given, a <CODE>NodeStatus</CODE> instance will be returned
   * which access the status of all nodes (both upstream and downstream) linked to the given 
   * node.  By default, the upstream node status details will be lightweight (see 
   * {@link NodeStatus#getLightDetails NodeStatus.getLightDetails}) for all nodes root at 
   * and included in the <CODE>rootNames</CODE> node name set.  However, heavyweight status 
   * (see {@link NodeStatus#getHeavyDetails NodeStatus.getHeavyDetails}) can be returned for 
   * upstream subtree of these root nodes by including them in the <CODE>heavyNames</CODE>
   * node name set.  If the contents of the <CODE>rootNames</CODE> and <CODE>heavyNames</CODE>
   * node name sets are identical, then all upstream nodes will have heavyweight node 
   * status details.<P> 
   * 
   * This method returns a table containing {@link NodeStatus} instances for each of the 
   * given nodes in the <CODE>rootNames</CODE> set indexed by their fully resolved node names.
   * If status for a root node is requested and the node does not exist, then the entry in 
   * this table for the missing node will be <CODE>null</CODE>.  To enable partial completion 
   * of this method when specified both existing and missing root nodes, a PipelineException 
   * will not be thrown when only a subset of the nodes are missing. <P> 
   * 
   * If not <CODE>null</CODE>, the <CODE>NodeStatus</CODE> for each root node can be used 
   * access the status of all nodes (both upstream and downstream) linked to the node.
   * The status information for the upstream nodes will also include detailed state and 
   * version information which is accessable by calling the {@link NodeStatus#getLightDetails 
   * NodeStatus.getLightDetails} method or the {@link NodeStatus#getHeavyDetails 
   * NodeStatus.getHeavyDetails} depending on whether lightweight or heavyweight details 
   * have been requested.  <P> 
   * 
   * For any given node, only one <CODE>NodeStatus</CODE> is actually returned but it may 
   * be accessible from multiple paths and from various root nodes.  This means that when 
   * computing node status where the given root nodes share a significant portion of their
   * upstream nodes, this method will be much more efficient than calling the single
   * node {@link #status status} method for each root node seperately. 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param rootNames
   *   The fully resolved names of the nodes for which node stats will be reported.
   * 
   * @param heavyNames
   *   The fully resolved names of the nodes which require heavyweight node status details.
   *   All nodes upstream will also return heavyweight details as well.  Note that in order
   *   for these heavyweight nodes to be returned, they must be included or reachable
   *   upstream from the <CODE>rootNames</CODE> set. 
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported for the nodes
   *   included in the <CODE>rootNames</CODE> set.
   * 
   * @return 
   *   The node status for each of the root nodes indexed by th root nodes fully resolved
   *   names of these nodes.  This table contain <CODE>null</CODE> values for some keys
   *   (see above).
   * 
   * @throws PipelineException
   *   If unable to determine the status of any of the root nodes.
   */ 
  public synchronized TreeMap<String,NodeStatus> 
  status
  ( 
   String author, 
   String view, 
   TreeSet<String> rootNames,
   TreeSet<String> heavyNames, 
   DownstreamMode dmode   
  ) 
    throws PipelineException
  {
    verifyConnection();
 
    NodeMultiStatusReq req = 
      new NodeMultiStatusReq(author, view, rootNames, heavyNames, dmode); 

    Object obj = performTransaction(MasterRequest.MultiStatus, req);
    if(obj instanceof NodeMultiStatusRsp) {
      NodeMultiStatusRsp rsp = (NodeMultiStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
       return null;
    }
  } 


  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the downstream only status of multiple nodes. <P> 
   * 
   * For each of the root nodes given, a <CODE>NodeStatus</CODE> instance will be returned
   * which access the status of all nodes downstream linked to the given node according the 
   * the criteria specified by the given downtream mode. <P> 
   * 
   * This method returns a table containing {@link NodeStatus} instances for each of the 
   * given nodes in the <CODE>rootNames</CODE> set indexed by their fully resolved node names.
   * If status for a root node is requested and the node does not exist, then the entry in 
   * this table for the missing node will be <CODE>null</CODE>.  To enable partial completion 
   * of this method when specified both existing and missing root nodes, a PipelineException 
   * will not be thrown when only a subset of the nodes are missing. <P> 
   * 
   * Note that all returned <CODE>NodeStatus</CODE> instances will not contain any detailed
   * status information, just the minimal status and connectivity information. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param rootNames
   *   The fully resolved names of the nodes for which node stats will be reported.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported for the nodes
   *   included in the <CODE>rootNames</CODE> set.
   * 
   * @return 
   *   The downstream node status for each of the root nodes indexed by th root nodes 
   *   fully resolved names of these nodes.  This table may contain <CODE>null</CODE> values 
   *   for some keys (see above).
   * 
   * @throws PipelineException
   *   If unable to determine the status of any of the root nodes.
   */ 
  public synchronized TreeMap<String,NodeStatus> 
  downstreamStatus
  ( 
   String author, 
   String view, 
   TreeSet<String> rootNames,
   DownstreamMode dmode   
  ) 
    throws PipelineException
  {
    verifyConnection();
 
    NodeDownstreamStatusReq req = 
      new NodeDownstreamStatusReq(author, view, rootNames, dmode); 

    Object obj = performTransaction(MasterRequest.DownstreamStatus, req);
    if(obj instanceof NodeMultiStatusRsp) {
      NodeMultiStatusRsp rsp = (NodeMultiStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
       return null;
    }
  } 
  


  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node. <P> 
   * 
   * The <CODE>mod</CODE> argument must have a node name which does not already exist and
   * does not match any of the path components of any existing node.  <P> 
   * 
   * The working version must be an inital version.  In other words, the 
   * {@link NodeMod#getWorkingID() NodeMod.getWorkingID} method must return 
   * <CODE>null</CODE>.  As an intial working version, the <CODE>mod</CODE> argument should
   * not contain any upstream node link information. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *  
   * @param author 
   *   The name of the user which owns the new working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @param mod
   *   The initial working version to register.
   *   
   * @return
   *   The NodeMod which represents the node that was just registered.
   * 
   * @throws PipelineException
   *   If unable to register the given node.
   */
  public synchronized NodeMod 
  register
  ( 
   String author, 
   String view, 
   NodeMod mod
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID nodeID = new NodeID(author, view, mod.getName());
    NodeRegisterReq req = new NodeRegisterReq(nodeID, mod);

    Object obj = performTransaction(MasterRequest.Register, req);
    if(obj instanceof NodeGetWorkingRsp) {
      NodeGetWorkingRsp rsp = (NodeGetWorkingRsp) obj;
      return rsp.getNodeMod();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Release the working version of a node and optionally remove the associated 
   * working area files. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param removeFiles 
   *   Should the files associated with the working version be deleted?
   *
   * @throws PipelineException 
   *   If unable to release the given node.
   */ 
  public synchronized void 
  release
  ( 
   String author, 
   String view, 
   String name, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    TreeSet<String> names = new TreeSet<String>();
    names.add(name);

    release(author, view, names, removeFiles);
  } 

  /**
   * Release the working versions of nodes and optionally remove the associated 
   * working area files. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param removeFiles 
   *   Should the files associated with the working version be deleted?
   *
   * @throws PipelineException 
   *   If unable to release the given node.
   */ 
  public synchronized void 
  release
  ( 
   NodeID nodeID,
   boolean removeFiles
  ) 
    throws PipelineException
  {
    TreeSet<String> names = new TreeSet<String>();
    names.add(nodeID.getName());

    release(nodeID.getAuthor(), nodeID.getView(), names, removeFiles);
  } 

  /**
   * Release the working version of a node and optionally remove the associated 
   * working area files. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param names 
   *   The fully resolved names of the nodes to release.
   *
   * @param removeFiles 
   *   Should the files associated with the working version be deleted?
   *
   * @throws PipelineException 
   *   If unable to release the given node.
   */ 
  public synchronized void 
  release
  ( 
   String author, 
   String view, 
   TreeSet<String> names, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    if(names.isEmpty()) 
      throw new PipelineException
	("At least one name of a node to release must be specified!");

    verifyConnection();

    NodeReleaseReq req = new NodeReleaseReq(author, view, names, removeFiles);

    Object obj = performLongTransaction(MasterRequest.Release, req, 15000, 60000); 
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Delete all working and checked-in versions of a node and optionally remove all  
   * associated working area files. <P> 
   * 
   * This operation may only be performed on nodes which have downstream or upstream links
   * to other nodes in any checked-in version.  Only privileged users may delete nodes.
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param removeFiles 
   *   Should the files associated with the working versions be deleted?
   *
   * @throws PipelineException 
   *   If unable to release the given node.
   */ 
  public synchronized void 
  delete
  ( 
   String name, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeDeleteReq req = new NodeDeleteReq(name, removeFiles);

    Object obj = performLongTransaction(MasterRequest.Delete, req, 15000, 60000); 
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-In the tree of nodes rooted at a working version. <P> 
   * 
   * The check-in operation proceeds in a depth-first manner checking-in the most upstream
   * nodes first.  The check-in operation aborts at the first failure of a particular node. 
   * It is therefore possible for the overall check-in to fail after already succeeding for 
   * some set of upstream nodes. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @throws PipelineException
   *   If unable to check-in the nodes.
   */ 
  public synchronized void
  checkIn
  ( 
   String author, 
   String view, 
   String name, 
   String msg, 
   VersionID.Level level   
  ) 
    throws PipelineException
  {
    checkIn(new NodeID(author, view, name), msg, level);
  } 

  /** 
   * Check-In the tree of nodes rooted at a working version. <P> 
   * 
   * The check-in operation proceeds in a depth-first manner checking-in the most upstream
   * nodes first.  The check-in operation aborts at the first failure of a particular node. 
   * It is therefore possible for the overall check-in to fail after already succeeding for 
   * some set of upstream nodes. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @throws PipelineException
   *   If unable to check-in the nodes.
   */ 
  public synchronized void
  checkIn
  ( 
   NodeID nodeID,
   String msg, 
   VersionID.Level level   
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeCheckInReq req = new NodeCheckInReq(nodeID, msg, level);

    Object obj = performLongTransaction(MasterRequest.CheckIn, req, 15000, 60000);  
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-Out the tree of nodes rooted at a working version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P>
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the node to check-out.
   * 
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   * 
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   * 
   * @return 
   *   The unfinished job IDs indexed by node name if aborted or 
   *   <CODE>null</CODE> if successful.
   * 
   * @throws PipelineException
   *   If unable to check-out the nodes.
   */ 
  public synchronized TreeMap<String,TreeSet<Long>>
  checkOut
  ( 
   String author, 
   String view, 
   String name, 
   VersionID vid, 
   CheckOutMode mode,
   CheckOutMethod method
  ) 
    throws PipelineException
  {
    return checkOut(new NodeID(author, view, name), vid, mode, method);
  } 

  /** 
   * Check-Out the tree of nodes rooted at a working version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P>
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid 
   *   The revision number of the node to check-out.
   * 
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   * 
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   * 
   * @return 
   *   The unfinished job IDs indexed by node name if aborted or 
   *   <CODE>null</CODE> if successful.
   * 
   * @throws PipelineException
   *   If unable to check-out the nodes.
   */ 
  public synchronized TreeMap<String,TreeSet<Long>>
  checkOut
  ( 
   NodeID nodeID,
   VersionID vid, 
   CheckOutMode mode,
   CheckOutMethod method
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeCheckOutReq req = new NodeCheckOutReq(nodeID, vid, mode, method);

    Object obj = performLongTransaction(MasterRequest.CheckOut, req, 15000, 60000); 
    if(obj instanceof GetUnfinishedJobsForNodesRsp) {
      GetUnfinishedJobsForNodesRsp rsp = (GetUnfinishedJobsForNodesRsp) obj;
      return rsp.getJobIDs();
    }
    else if(obj instanceof SuccessRsp) {
      return null;
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lock the working version of a node to a specific checked-in version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then lock to the base checked-in
   * version. <P>
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version is 
   *   being locked.
   * 
   * @throws PipelineException
   *   If unable to lock the nodes.
   */ 
  public synchronized void
  lock
  ( 
   String author, 
   String view, 
   String name,
   VersionID vid
  ) 
    throws PipelineException
  {
    lock(new NodeID(author, view, name), vid);
  } 

  /** 
   * Lock the working version of a node to a specific checked-in version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then lock to the base checked-in
   * version. <P>
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version is 
   *   being locked.
   * 
   * @throws PipelineException
   *   If unable to lock the nodes.
   */ 
  public synchronized void
  lock
  ( 
   NodeID nodeID,
   VersionID vid
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeLockReq req = new NodeLockReq(nodeID, vid);

    Object obj = performLongTransaction(MasterRequest.Lock, req, 15000, 60000);  
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Revert specific working area files to an earlier checked-in version of the files. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   * 
   * @throws PipelineException
   *   If unable to revert the files.
   */ 
  public synchronized void 
  revertFiles  
  ( 
   String author, 
   String view, 
   String name, 
   TreeMap<String,VersionID> files
  )
    throws PipelineException
  {
    revertFiles(new NodeID(author, view, name), files);
  }

  /**
   * Revert specific working area files to an earlier checked-in version of the files. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   * 
   * @throws PipelineException
   *   If unable to revert the files.
   */ 
  public synchronized void 
  revertFiles  
  ( 
   NodeID nodeID,
   TreeMap<String,VersionID> files
  )
    throws PipelineException
  {
    verifyConnection();

    NodeRevertFilesReq req = new NodeRevertFilesReq(nodeID, files);
    
    Object obj = performTransaction(MasterRequest.RevertFiles, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Register a new node who's properties are identical to an existing working version.
   * 
   * Clones all properties, actions, links and files. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *  
   * @param author 
   *   The name of the user which owns the cloned version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @param oldName
   *   The fully resolved name of the source node to clone.
   * 
   * @param newName
   *   The fully resolved name of the cloned node.
   *   
   * @return 
   *   A {@link NodeMod} representing the newly cloned node.
   * 
   * @throws PipelineException
   *   If unable to clone the given node.
   */
  public synchronized NodeMod 
  clone
  ( 
   String author, 
   String view, 
   String oldName, 
   String newName
  ) 
    throws PipelineException
  {
    return clone(author, view, oldName, newName, true, true, true, true);
  }

  /**
   * Register a new node who's properties are identical to an existing working version.
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *  
   * @param author 
   *   The name of the user which owns the cloned version.
   * @param view 
   *   The name of the user's working area view. 
   * @param oldName
   *   The fully resolved name of the source node to clone.
   * @param newName
   *   The fully resolved name of the cloned node.
   * @param cloneAction
   *   Whether to clone the action and action parameters of source node.
   * @param cloneLinks
   *   Whether to clone the links of the source node as well.
   * @param cloneFiles
   *   Whether to copy the files of source node as well.
   * @param cloneAnnotations
   *   Whether to copy of the annotations of the source node as well. If the user doing the 
   *   clone does not have Annotator privileges, this may call the clone to error out at
   *   the end.  However, all proceding steps will not be undone.
   * 
   * @return 
   *   A {@link NodeMod} representing the newly cloned node.
   * 
   * @throws PipelineException
   *   If unable to clone the given node.
   */
  public synchronized NodeMod 
  clone
  ( 
   String author, 
   String view, 
   String oldName, 
   String newName, 
   boolean cloneAction, 
   boolean cloneLinks, 
   boolean cloneFiles, 
   boolean cloneAnnotations 
  ) 
    throws PipelineException
  {
    NodeMod oldMod = getWorkingVersion(author, view, oldName);

    FileSeq newSeq = null;
    {
      FileSeq oldSeq = oldMod.getPrimarySequence();
      FilePattern oldPat = oldSeq.getFilePattern();

      Path path = new Path(newName);
      String name = path.getName();

      FrameRange range = null;
      FilePattern pat = null;

      if(oldSeq.hasFrameNumbers()) {
	range = oldSeq.getFrameRange();
	pat = new FilePattern(name, oldPat.getPadding(), oldPat.getSuffix());
      }
      else {
	pat = new FilePattern(name, oldPat.getSuffix());
      }
   
      newSeq = new FileSeq(pat, range);
    }

    NodeMod newMod = 
      new NodeMod(newName, newSeq, oldMod.getSecondarySequences(), oldMod.isIntermediate(), 
                  oldMod.getToolset(), oldMod.getEditor());

    register(author, view, newMod);

    if(cloneLinks) {
      for(LinkMod link : oldMod.getSources()) {
	link(author, view, newName, 
	     link.getName(), link.getPolicy(), link.getRelationship(), link.getFrameOffset());
      }
    }

    if(cloneAction) {
      if (oldMod.getAction() != null) {
        BaseAction action = new BaseAction(oldMod.getAction());
        newMod.setAction(action);
        modifyProperties(author, view, newMod);
      }
    }

    if(cloneFiles) {
      NodeID source = new NodeID(author, view, oldName);
      NodeID target = new NodeID(author, view, newName);
      cloneFiles(source, target);
    }
    
    if(cloneAnnotations) {
      TreeMap<String, BaseAnnotation> annots = getAnnotations(oldName);
      for (Entry<String, BaseAnnotation> entry  : annots.entrySet())
        addAnnotation(newName, entry.getKey(), entry.getValue());
      TreeMap<String, BaseAnnotation> perVer = oldMod.getAnnotations();
      for (Entry<String, BaseAnnotation> entry : perVer.entrySet()) 
        addAnnotation(newName, entry.getKey(), entry.getValue());
      modifyProperties(author, view, newMod);
    }
    
    return getWorkingVersion(author, view, newName);
  } 

  /**
   * Replace the primary files associated with one node with the primary files of 
   * another node. <P>
   * 
   * The two nodes must have exactly the same number of files in their primary file sequences
   * or the operation will fail. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   * 
   * @throws PipelineException
   *   If unable to clone the files.
   */ 
  public synchronized void 
  cloneFiles  
  ( 
    NodeID sourceID, 
    NodeID targetID
  )
    throws PipelineException
  {
    cloneFiles(sourceID, targetID, null);
  }
  
  /**
   * Replace the primary and selected secondary files associated with one node with the 
   * primary and selected secondary files of another node. <P>
   * 
   * The two nodes must have exactly the same number of files in their primary file sequences
   * or the operation will fail. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   *
   * @param secondarySequences
   *   A map of the secondary file sequences to be copied.  The keys are the secondary
   *   sequences of the source node and the values are the corresponding sequences in 
   *   the target node.
   * 
   * @throws PipelineException
   *   If unable to clone the files or if some of the specified secondary sequences do not
   *   exist.
   */ 
  public synchronized void 
  cloneFiles  
  ( 
    NodeID sourceID, 
    NodeID targetID,
    TreeMap<FileSeq, FileSeq> secondarySequences
  )
    throws PipelineException
  {
    verifyConnection();

    NodeCloneFilesReq req = new NodeCloneFilesReq(sourceID, targetID, secondarySequences); 
    
    Object obj = performTransaction(MasterRequest.CloneFiles, req);
    handleSimpleResponse(obj);
  }

  /**
   * Replace the primary and selected secondary files associated with one node with the 
   * primary and selected secondary files of another node. <P>
   * 
   * The two nodes must have exactly the same number of files in their primary file sequences
   * or the operation will fail. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   *
   * @param secondarySequences
   *   A map of the secondary file sequences to be copied.  The keys are the secondary
   *   sequences of the source node and the values are the corresponding sequences in 
   *   the target node.
   *   
   * @param sourceRange
   *   The frame range in the source node that will be copied.  If this is <code>null</code>
   *   then the entire frame range of the node will be copied.  If this range ends up having 
   *   a different number of frames than the targetRange, this request will fail.
   * 
   * @param targetRange
   *   The frame range in the target node what will be copied to.  If this is 
   *   <code>null</code> then the entire frame range of the node will be copied to.  If this 
   *   range ends up having a different number of frames than the sourceRange, this request
   *   will fail.
   * 
   * @throws PipelineException
   *   If unable to clone the files or if some of the specified secondary sequences do not
   *   exist or if the frame ranges passed in are not valid frame ranges for the nodes or do
   *   not contain the same number of frames.
   */ 
  public synchronized void 
  cloneFiles  
  ( 
    NodeID sourceID, 
    NodeID targetID,
    TreeMap<FileSeq, FileSeq> secondarySequences,
    FrameRange sourceRange,
    FrameRange targetRange
  )
    throws PipelineException
  {
    verifyConnection();

    if (sourceRange != null && targetRange != null) {
      if (sourceRange.numFrames() != targetRange.numFrames())
        throw new PipelineException
          ("Cowardly refusing to call cloneFiles() from source (" + sourceID + ") with " +
           "frame range (" + sourceRange + ") to target (" + targetID + ") with frame range " +
           "(" + targetRange + ") since the frame ranges are different lengths.");
    }
    
    NodeCloneFilesReq req = 
      new NodeCloneFilesReq(sourceID, targetID, secondarySequences, sourceRange, targetRange); 
    
    Object obj = performTransaction(MasterRequest.CloneFiles, req);
    handleSimpleResponse(obj);
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Change the checked-in version upon which the working version is based without 
   * modifying the working version properties, links or associated files. <P> 
   *
   * This operation can be used to resolve a conflict by changing to the latest checked-in
   * version. <P> 
   *
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid
   *   The revision number of the checked-in version or <CODE>null</CODE> for the latest
   *   checked-in version.
   * 
   * @throws PipelineException
   *   If unable to evolve the node. 
   */ 
  public synchronized void 
  evolve
  ( 
   String author, 
   String view, 
   String name, 
   VersionID vid
  )
    throws PipelineException
  {
    evolve(new NodeID(author, view, name), vid);
  }

  /**
   * Change the checked-in version upon which the working version is based without 
   * modifying the working version properties, links or associated files. <P> 
   *
   * This operation can be used to resolve a conflict by changing to the latest checked-in
   * version. <P> 
   *
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version or <CODE>null</CODE> for the latest
   *   checked-in version.
   * 
   * @throws PipelineException
   *   If unable to evolve the node. 
   */ 
  public synchronized void 
  evolve
  ( 
   NodeID nodeID,
   VersionID vid
  )
    throws PipelineException
  {
    verifyConnection();

    NodeEvolveReq req = new NodeEvolveReq(nodeID, vid);
    
    Object obj = performTransaction(MasterRequest.Evolve, req);
    handleSimpleResponse(obj);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   B U N D L E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new node bundle (JAR achive) by packing up a tree of nodes from a working 
   * area rooted at the given node.<P> 
   *
   * If successful, this will create a new node bundle containing the node properties, links
   * and associated working area data files for the entire tree of nodes rooted at the given
   * node.  The node bundle will contain full copies of all files associated with these nodes
   * regardless of whether they where checked-out modifiable, frozen or locked within the 
   * current working area.  All node metadata, including detailed information about the
   * toolsets and toolset packages required, will be written to a GLUE file included in the
   * node bundle. <P> 
   * 
   * The node bundle will always be written into the root directory of the working area 
   * containing the root node of the node tree being packed into the archive.  The name of
   * the archive is automatically generated based on the name of the root node and the 
   * time when the operation begins.  The full path to the create JAR file is returned by
   * this method if successfull. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status. All nodes to be packed
   * into the node bundle must be in a Finished (blue) state.  Any nodes not in a Finished
   * state will cause the entire operation to abort.<P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name of the root node.
   * 
   * @return
   *   The abstract file system path to the newly create node bundle.
   * 
   * @throws PipelineException
   *   If unable to create the node bundle containing the nodes. 
   * 
   * @see #extractBundle
   * @see #unpackNodes
   */ 
  public synchronized Path
  packNodes
  ( 
   String author, 
   String view, 
   String name
  )
    throws PipelineException
  {
    return packNodes(new NodeID(author, view, name)); 
  }

  /**
   * Create a new node bundle (JAR achive) by packing up a tree of nodes from a working 
   * area rooted at the given node.<P> 
   *
   * If successful, this will create a new node bundle containing the node properties, links
   * and associated working area data files for the entire tree of nodes rooted at the given
   * node.  The node bundle will contain full copies of all files associated with these nodes
   * regardless of whether they where checked-out modifiable, frozen or locked within the 
   * current working area.  All node metadata, including detailed information about the
   * toolsets and toolset packages required, will be written to a GLUE file included in the
   * node bundle. <P> 
   * 
   * The node bundle will always be written into the root directory of the working area 
   * containing the root node of the node tree being packed into the archive.  The name of
   * the archive is automatically generated based on the name of the root node and the 
   * time when the operation begins.  The full path to the create JAR file is returned by
   * this method if successfull. <P> 
   * 
   * Create a new node JAR archive by packing up a tree of nodes from a working area rooted 
   * at the given node.<P> 
   *
   * If the owner of the root node is different than the current user, this method 
   * will fail unless the current user has privileged access status. All nodes to be packed
   * into the JAR archive must be in a Finished (blue) state.  Any nodes not in a Finished
   * state will cause the entire operation to abort.<P> 
   * 
   * @param nodeID 
   *   The unique working version identifier of the root node.
   * 
   * @return
   *   The abstract file system path to the newly create node bundle.
   * 
   * @throws PipelineException
   *   If unable to create the node bundle containing the nodes. 
   * 
   * @see #extractBundle
   * @see #unpackNodes
   */ 
  public synchronized Path 
  packNodes
  ( 
   NodeID nodeID
  )
    throws PipelineException
  {
    verifyConnection();

    NodePackReq req = new NodePackReq(nodeID);
    
    Object obj = performLongTransaction(MasterRequest.Pack, req, 15000, 60000); 
    if(obj instanceof NodePackRsp) {
      NodePackRsp rsp = (NodePackRsp) obj;
      return rsp.getPath();
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Extract the node metadata from a node bundle containing a tree of nodes packed at 
   * another site. <P> 
   * 
   * This method is useful for querying for information about the toolsets being used by 
   * the nodes in the node bundle without unpacking them.  Using this information, a suitable
   * mapping of toolsets from the site which created the nodes to the toolsets at the local
   * site can be generated.  If this remapping of toolsets is not specified, than the default
   * toolset at the local site will be used for all unpacked nodes.  In many cases, this will
   * not be sufficient to insure that the nodes function properly after being unpacked.
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   * 
   * @throws PipelineException
   *   If unable to extract the node metadata from the node bundle.
   * 
   * @see #packNodes
   * @see #unpackNodes
   */ 
  public synchronized NodeBundle
  extractBundle
  (
   Path bundlePath
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeExtractBundleReq req = new NodeExtractBundleReq(bundlePath); 

    Object obj = performTransaction(MasterRequest.ExtractBundle, req);  
    if(obj instanceof NodeExtractBundleRsp) {
      NodeExtractBundleRsp rsp = (NodeExtractBundleRsp) obj;
      return rsp.getBundle();
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  }

  /**
   * Unpack a node bundle containing a tree of nodes packed at another site into the given
   * working area.<P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param releaseOnError
   *   Whether to release all newly registered and/or modified nodes from the working area
   *   if an error occurs in unpacking the node bundle.
   * 
   * @param actOnExist
   *   What steps to take when encountering previously existing local versions of nodes
   *   being unpacked.
   * 
   * @param lockedVersions
   *   The revision numbers for local checked-in node versions to use for specific nodes
   *   locked in the bundle.  A valid checked-in version must exist for each locked node 
   *   in the bundle or a PipelineException wil be thrown.
   * 
   * @param toolsetRemap
   *   A table mapping the names of toolsets associated with the nodes in the node bundle
   *   to toolsets at the local site.  Toolsets not found in this table will be remapped 
   *   to the local default toolset instead.
   * 
   * @param selectionKeyRemap
   *   A table mapping the names of selection keys associated with the nodes in the node 
   *   bundle to selection keys at the local site.  Any selection keys not found in this 
   *   table will be ignored.
   * 
   * @param licenseKeyRemap
   *   A table mapping the names of license keys associated with the nodes in the node 
   *   bundle to license keys at the local site.  Any license keys not found in this 
   *   table will be ignored.
   * 
   * @throws PipelineException
   *   If unable to unpack the nodes from the node bundle.
   * 
   * @see #packNodes
   * @see #extractBundle
   */ 
  public synchronized void 
  unpackNodes
  (
   Path bundlePath, 
   String author, 
   String view,    
   boolean releaseOnError, 
   ActionOnExistence actOnExist,
   TreeMap<String,VersionID> lockedVersions, 
   TreeMap<String,String> toolsetRemap,
   TreeMap<String,String> selectionKeyRemap,
   TreeMap<String,String> licenseKeyRemap,
   TreeMap<String,String> hardwareKeyRemap
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeUnpackReq req = 
      new NodeUnpackReq(bundlePath, author, view, releaseOnError, actOnExist, lockedVersions,
                        toolsetRemap, selectionKeyRemap, licenseKeyRemap, hardwareKeyRemap);
    
    Object obj = performLongTransaction(MasterRequest.Unpack, req, 15000, 60000); 
    handleSimpleResponse(obj);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S I T E   V E R S I O N S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Creates a JAR archive containing both files and metadata associated with a checked-in
   * version of a node suitable for transfer to a remote site.<P> 
   * 
   * The JAR archive will contain a copy of the original NodeVersion which has been altered 
   * from its original form in several ways:<P>
   * 
   * <DIV style="margin-left: 40px;">
   *   The full name of the target node as well as the names of any source nodes of this
   *   target node will have been changed to append the "localSiteName" as the last 
   *   directory component of the node path before the node prefix.  <P> 
   * 
   *   For each source node listed in the "referenceNames", the link type will be changed 
   *   to Reference.  The name of the source nodes will also be modified to include the 
   *   "localSiteName" as the last directory component.  Any source nodes not contained in 
   *   "referenceNames" will be removed as a source for the target node.<P> 
   * 
   *   Any action associated with the target node will be removed.
   * 
   *   A RemoteVersion per-version annotation will be added to the NodeVersion who's 
   *   annotation parameters included detailed information about the original node version
   *   being extracted.  This includes the original node name, local site name as well as
   *   information about when the JAR archive was created and by whom. 
   * </DIV><P> 
   *   
   * Each file associated with the target node will also be copied and included in the JAR 
   * archive generated.  These files will also be altered from their original in the 
   * following ways:<P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   The names of the files will similarly renamed to include the local site name as the 
   *   last directory component of the file path.<P> 
   * 
   *   If a file is part of one of the primary/secondary file sequences contained in 
   *   "replaceSeqs", then a series of string substitutions will be performed on each file 
   *   to make it portable to the new site.  All occurances of the names of source nodes 
   *   included in the "referenceNames" will be automatically changed to include the local 
   *   site name.  In addition, all key entries in the "replacements" table will replaced 
   *   by their value in this table.  This provides a way of adding in any arbitrary site 
   *   localization fixes which may be node specific.
   * </DIV><P> 
   * 
   * In addition to a GLUE format file containing the altered NodeVersion copy and associated
   * node files, a "README" text file will also be added to the JAR archive which details 
   * the contents and all changes made to the node version being extracted.<P> 
   * 
   * If successfull, the JAR archive file will be written to the directory named by "dir" 
   * and given a unique name based on the timestamp of when the archive was created.  The
   * full filesystem path of this uniquely named JAR file will be returned by this method.<P>
   * 
   * This method will go away when true multi-site support is added to Pipeline.<P>  
   * 
   * @param name
   *   The fully resolved node name of the node to extract.
   * 
   * @param vid
   *   The revision number of the node version to extract. 
   * 
   * @param referenceNames
   *   The fully resolved names of the source nodes to include as Reference links or
   *   <CODE>null</CODE> if no links should be included.
   * 
   * @param localSiteName
   *   Name for the local site which will be used to modify extracted node names.
   * 
   * @param replaceSeqs
   *   The primary and secondary file sequences associated with the node to which all 
   *   string replacements should be applied or <CODE>null</CODE> to skip all file contents 
   *   replacements.
   * 
   * @param replacements
   *   The table of additional string replacements to perform on the files associated
   *   with the node version being extracted or <CODE>null</CODE> if there are no
   *   additional replacements. 
   * 
   * @param dir
   *   The directory in which to place the JAR archive created.
   *
   * @return
   *   The full file system path of the created JAR archive.
   */ 
  public synchronized Path
  extractSiteVersion
  (
   String name, 
   VersionID vid, 
   TreeSet<String> referenceNames, 
   String localSiteName, 
   TreeSet<FileSeq> replaceSeqs, 
   TreeMap<String,String> replacements,
   Path dir
  )
    throws PipelineException
  {   
    verifyConnection();
    
    NodeExtractSiteVersionReq req = 
      new NodeExtractSiteVersionReq(name, vid, referenceNames, localSiteName, 
                                    replaceSeqs, replacements, dir); 
    
    Object obj = performLongTransaction(MasterRequest.ExtractSiteVersion, req, 15000, 60000);
    if(obj instanceof NodeExtractSiteVersionRsp) {
      NodeExtractSiteVersionRsp rsp = (NodeExtractSiteVersionRsp) obj;
      return rsp.getPath();
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  }

  /**
   * Lookup the NodeVersion contained within the extracted site version JAR archive.
   * 
   * @param jarPath
   *   The full file system path to the JAR archive containing the node version.
   */ 
  public synchronized NodeVersion
  lookupSiteVersion
  (
   Path jarPath
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeSiteVersionReq req = new NodeSiteVersionReq(jarPath);

    Object obj = performTransaction(MasterRequest.LookupSiteVersion, req); 
    if(obj instanceof NodeLookupSiteVersionRsp) {
      NodeLookupSiteVersionRsp rsp = (NodeLookupSiteVersionRsp) obj;
      return rsp.getNodeVersion();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Whether the extracted node contained in the given JAR archive has already been inserted
   * into the node database.
   * 
   * @param jarPath
   *   The full file system path to the JAR archive containing the node version to insert.
   */ 
  public synchronized boolean
  isSiteVersionInserted
  (
   Path jarPath
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeSiteVersionReq req = new NodeSiteVersionReq(jarPath); 
    
    Object obj = performTransaction(MasterRequest.IsSiteVersionInserted, req); 
    if(obj instanceof NodeIsSiteVersionInsertedRsp) {
      NodeIsSiteVersionInsertedRsp rsp = (NodeIsSiteVersionInsertedRsp) obj;
      return rsp.isInserted();
    }
    else {
      handleFailure(obj);
      return false;        
    } 
  }

  /**
   * Checks each of the source nodes referenced by the extracted node contained in the 
   * given JAR archive and returns the names and versions of any of them that are not
   * already in the node database.<P> 
   * 
   * @param jarPath
   *   The full file system path to the JAR archive containing the node version to insert.
   * 
   * @return
   *   The names and versions of the missing nodes.
   */ 
  public synchronized TreeMap<String,VersionID> 
  getMissingSiteVersionRefs
  (
   Path jarPath
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeSiteVersionReq req = new NodeSiteVersionReq(jarPath); 
    
    Object obj = performTransaction(MasterRequest.GetMissingSiteVersionRefs, req); 
    if(obj instanceof NodeGetMissingSiteVersionRefsRsp) {
      NodeGetMissingSiteVersionRefsRsp rsp = (NodeGetMissingSiteVersionRefsRsp) obj;
      return rsp.getMissingVersions();
    }
    else {
      handleFailure(obj);
      return null;        
    } 
  }

  /**
   * Inserts a node version into the local node database previously extraced from a remote
   * site using the {@link #extractSiteVersion} method.<P> 
   * 
   * There several conditions which must be met in order for the node version to be 
   * successfully inserted: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   If there is are any existing versions of the node being inserted, the RemoteNode
   *   per-node annotation must be currently associated with the node.  If not, this probably
   *   indicates a potential name conflict between a locally created node an the remotely
   *   created node being inserted.<P> 
   * 
   *   The specific version being inserted must not already exist in the node database.<P> 
   * 
   *   If the version being inserted has Reference source nodes, then the specific versions
   *   of these nodes required must already exist.<P> 
   * </DIV><P> 
   * 
   * If the node version being inserted is the first version to be created for a node, then 
   * a per-node RemoteNode annotation will also be added to the node.
   * 
   * @param jarPath
   *   The full file system path to the JAR archive containing the node version to insert.
   */ 
  public synchronized void
  insertSiteVersion
  (
   Path jarPath
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeSiteVersionReq req = new NodeSiteVersionReq(jarPath); 
    
    Object obj = performLongTransaction(MasterRequest.InsertSiteVersion, req, 15000, 60000); 
    handleSimpleResponse(obj);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   E V E N T S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Retrieve the record of all significant operations involving the given nodes and users
   * during the specified time interval.
   * 
   * @param names 
   *   Limit the events to those associated with the given fully resolved node names or
   *   <CODE>null</CODE> for all nodes.
   * 
   * @param users
   *   Limit the events to those generated by the given user names or
   *   <CODE>null</CODE> for all users.
   * 
   * @param interval
   *   Limit the events to those which occured within the given time interval or 
   *   <CODE>null</CODE> for all times.
   * 
   * @return 
   *   The events indexed by the timestamp (milliseconds since midnight, January 1, 1970 UTC)
   *   at which the events occurred.
   * 
   * @throws PipelineException
   *   If unable to retrieve the events.
   */ 
  public synchronized MappedLinkedList<Long,BaseNodeEvent>
  getNodeEvents
  (
   TreeSet<String> names, 
   TreeSet<String> users, 
   TimeInterval interval
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeGetEventsReq req = new NodeGetEventsReq(names, users, interval); 

    Object obj = performTransaction(MasterRequest.GetEvents, req);
    if(obj instanceof NodeGetEventsRsp) {
      NodeGetEventsRsp rsp = (NodeGetEventsRsp) obj;
      return rsp.getEvents(); 
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Signal that an Editor plugin has started editing files associated with the 
   * given working version of a node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param editor 
   *   The Editor plugin instance. 
   * 
   * @return
   *   A unique ID for the editing session.
   * 
   * @throws PipelineException
   *   If unable to contact the server.
   */ 
  public synchronized Long
  editingStarted
  (
   NodeID nodeID,  
   BaseEditor editor
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeEditingStartedReq req = new NodeEditingStartedReq(nodeID, editor); 
    
    Object obj = performTransaction(MasterRequest.EditingStarted, req);
    if(obj instanceof NodeEditingStartedRsp) {
      NodeEditingStartedRsp rsp = (NodeEditingStartedRsp) obj;
      return rsp.getEditID(); 
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
   
  /**
   * Signal that an Editor plugin has finished editing files associated with the 
   * working version of a node.
   * 
   * @param editID 
   *   The unique ID for the editing session.
   * 
   * @throws PipelineException
   *   If unable to contact the server.
   */ 
  public synchronized void 
  editingFinished
  (
   Long editID  
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeEditingFinishedReq req = new NodeEditingFinishedReq(editID);  
    
    Object obj = performTransaction(MasterRequest.EditingFinished, req);
    handleSimpleResponse(obj);    
  }

  /**
   * Get the table of the working areas in which the given node is currently being 
   * edited. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The table of working area view names indexed by author user name.  
   * 
   * @throws PipelineException
   *   If unable to determine the working areas.
   */
  public synchronized TreeMap<String,TreeSet<String>>
  getWorkingAreasEditing
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetWorkingAreasEditing, req);
    if(obj instanceof NodeGetWorkingAreasRsp) {
      NodeGetWorkingAreasRsp rsp = (NodeGetWorkingAreasRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   Q U E U E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with a tree of nodes rooted at the given node. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param indices
   *   The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   *   regenerate all <CODE>Stale</CODE> files.
   * 
   * @return 
   *   The list of newly submitted job groups.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized LinkedList<QueueJobGroup> 
  submitJobs
  ( 
   String author, 
   String view, 
   String name, 
   TreeSet<Integer> indices
  ) 
    throws PipelineException
  {
    return submitJobs(new NodeID(author, view, name), indices, 
                      null, null, null, null, null, null, null, null, null);
  }

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. <P> 
   * 
   * The <CODE>batchSize</CODE>, <CODE>priority</CODE> or <CODE>selectionKeys</CODE> 
   * parameters (if not <CODE>null</CODE>) will override the settings when creating jobs 
   * associated with the root node of this submisssion.  However, the node will not be 
   * modified by this operation and all jobs associated with nodes upstream of the root node
   * of the submission will be unaffected. <P>
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param indices
   *   The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   *   regenerate all <CODE>Stale</CODE> files.
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *    The ramp-up interval (in seconds) for the job.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
   * 
   * @return 
   *   The list of newly submitted job groups.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized LinkedList<QueueJobGroup> 
  submitJobs
  ( 
   String author, 
   String view, 
   String name, 
   TreeSet<Integer> indices, 
   Integer batchSize, 
   Integer priority, 
   Integer rampUp,
   Float maxLoad,              
   Long minMemory,              
   Long minDisk,  
   Set<String> selectionKeys,
   Set<String> licenseKeys,
   Set<String> hardwareKeys
  ) 
    throws PipelineException
  {
    return submitJobs(new NodeID(author, view, name), indices, 
		      batchSize, priority, rampUp, maxLoad, minMemory, minDisk, 
		      selectionKeys, licenseKeys, hardwareKeys);
  }

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *
   * @param nodeID 
   *   The unique working version identifier. 
   *
   * @param indices
   *   The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   *   regenerate all <CODE>Stale</CODE> files.
   * 
   * @return 
   *   The list of newly submitted job groups.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized LinkedList<QueueJobGroup> 
  submitJobs
  ( 
   NodeID nodeID,
   TreeSet<Integer> indices 
  ) 
    throws PipelineException
  {
    return submitJobs(nodeID, indices, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. <P> 
   * 
   * The <CODE>batchSize</CODE>, <CODE>priority</CODE> or <CODE>selectionKeys</CODE> 
   * parameters (if not <CODE>null</CODE>) will override the settings when creating jobs 
   * associated with the root node of this submisssion.  However, the node will not be 
   * modified by this operation and all jobs associated with nodes upstream of the root node
   * of the submission will be unaffected. <P>
   * 
   * It is possible that more than one job group will be generated when there are Association
   * links involved.  Pipeline treats the network of nodes upstream of an Assocation link
   * as independent from the nodes on the downstream side of the link in terms of staleness
   * and therefore job group generation.  When Association links are encountered, there may 
   * be additional job groups submitted rooted at the first stale node on the upstream side
   * of each Association link. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *
   * @param nodeID 
   *   The unique working version identifier. 
   *
   * @param indices
   *   The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   *   regenerate all <CODE>Stale</CODE> files.
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *    The ramp-up interval (in seconds) for the job.
   *    
   * @param maxLoad 
   *    The maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *    The minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *    The minimum amount of free temporary local disk space (in bytes) required on an 
   *    eligible host.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
   * 
   * @return 
   *   The list of newly submitted job groups.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized LinkedList<QueueJobGroup> 
  submitJobs
  ( 
   NodeID nodeID,
   TreeSet<Integer> indices,
   Integer batchSize, 
   Integer priority,  
   Integer rampUp,
   Float maxLoad,              
   Long minMemory,              
   Long minDisk,    
   Set<String> selectionKeys,
   Set<String> licenseKeys,
   Set<String> hardwareKeys
  ) 
    throws PipelineException
  {
    verifyConnection();

    JobReqsDelta delta = 
      new JobReqsDelta
      (0l, priority, rampUp, maxLoad, minMemory, minDisk, 
       licenseKeys, selectionKeys, hardwareKeys);
    
    NodeSubmitJobsReq req = 
      new NodeSubmitJobsReq(nodeID, indices, batchSize, delta);

    Object obj = performTransaction(MasterRequest.SubmitJobs, req);
    if(obj instanceof NodeSubmitJobsRsp) {
      NodeSubmitJobsRsp rsp = (NodeSubmitJobsRsp) obj;
      return rsp.getJobGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Resubmit the group of jobs needed to regenerate the selected 
   * {@link QueueState#Stale Stale} primary file sequences for the tree of nodes rooted at 
   * the given node. <P> 
   * 
   * This method is typically used to resubmit aborted or failed jobs.  The selected files
   * to regenerate are provided as target primary file sequences instead of file indices. 
   * The correct indices for each file defined by these target sequences will be computed
   * and new job batches will be submitted for these files. <P> 
   * 
   * The <CODE>batchSize</CODE>, <CODE>priority</CODE> or <CODE>selectionKeys</CODE> 
   * parameters (if not <CODE>null</CODE>) will override the settings when creating jobs 
   * associated with the root node of this submisssion.  However, the node will not be 
   * modified by this operation and all jobs associated with nodes upstream of the root node
   * of the submission will be unaffected. <P>
   * 
   * It is possible that more than one job group will be generated when there are Association
   * links involved.  Pipeline treats the network of nodes upstream of an Assocation link
   * as independent from the nodes on the downstream side of the link in terms of staleness
   * and therefore job group generation.  When Association links are encountered, there may 
   * be additional job groups submitted rooted at the first stale node on the upstream side
   * of each Association link. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   *
   * @param nodeID 
   *   The unique working version identifier. 
   *
   * @param targetSeqs
   *   The target primary file sequences to regenerate.
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *    The ramp-up interval (in seconds) for the job.
   *    
   * @param maxLoad 
   *    The maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *    The minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *    The minimum amount of free temporary local disk space (in bytes) required on an 
   *    eligible host.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
   * 
   * @return 
   *   The list of newly submitted job groups.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized LinkedList<QueueJobGroup> 
  resubmitJobs
  ( 
   NodeID nodeID,
   TreeSet<FileSeq> targetSeqs, 
   Integer batchSize, 
   Integer priority, 
   Integer rampUp, 
   Float maxLoad,              
   Long minMemory,              
   Long minDisk,  
   Set<String> selectionKeys,
   Set<String> licenseKeys,
   Set<String> hardwareKeys
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    JobReqsDelta delta = 
      new JobReqsDelta
      (0l, priority, rampUp, maxLoad, minMemory, minDisk, 
       licenseKeys, selectionKeys, hardwareKeys);

    NodeResubmitJobsReq req = 
      new NodeResubmitJobsReq(nodeID, targetSeqs, batchSize, delta);

    Object obj = performTransaction(MasterRequest.ResubmitJobs, req);
    if(obj instanceof NodeSubmitJobsRsp) {
      NodeSubmitJobsRsp rsp = (NodeSubmitJobsRsp) obj;
      return rsp.getJobGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Vouch for the up-to-date status of the working area files associated with a node. <P>  
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException 
   *   If unable to vouch for the files.
   */ 
  public synchronized void
  vouch
  (
   String author,  
   String view, 
   String name
  ) 
    throws PipelineException
  {
    vouch(new NodeID(author, view, name)); 
  }

  /**
   * Vouch for the up-to-date status of the working area files associated with a node. <P>  
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to vouch for the files.
   */ 
  public synchronized void
  vouch
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeVouchReq req = new NodeVouchReq(nodeID);

    Object obj = performTransaction(MasterRequest.Vouch, req);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the working area files associated with a node. <P>  
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param indices
   *   The file sequence indices of the files to remove or <CODE>null</CODE> to 
   *   remove all files.
   * 
   * @throws PipelineException 
   *   If unable to remove the files.
   */ 
  public synchronized void
  removeFiles
  (
   String author,  
   String view, 
   String name,
   TreeSet<Integer> indices
  ) 
    throws PipelineException
  {
    removeFiles(new NodeID(author, view, name), indices);
  }

  /**
   * Remove the working area files associated with a node. <P>  
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param indices
   *   The file sequence indices of the files to remove or <CODE>null</CODE> to 
   *   remove all files.
   * 
   * @throws PipelineException 
   *   If unable to remove the files.
   */ 
  public synchronized void
  removeFiles
  (
   NodeID nodeID,
   TreeSet<Integer> indices
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeRemoveFilesReq req = new NodeRemoveFilesReq(nodeID, indices);

    Object obj = performTransaction(MasterRequest.RemoveFiles, req);
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a database backup file. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup file will typically be named: <P> 
   * <DIV style="margin-left: 40px;">
   *   pipeline-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tgz<P>
   * </DIV>
   * 
   * Where <I>YYMMDD</I>.<I>HHMMSS</I> is the year, month, day, hour, minute and second of 
   * the backup.  The backup file is a <B>gzip</B>(1) compressed <B>tar</B>(1) archive of
   * the {@link Glueable GLUE} format files which make of the persistent storage of the
   * Pipeline database. <P> 
   * 
   * Only privileged users may create a database backup. <P> 
   * 
   * @param file
   *   The name of the backup file.
   * 
   * @throws PipelineException 
   *   If unable to perform the backup.
   */ 
  public synchronized void
  backupDatabase
  (
   File file
  ) 
    throws PipelineException
  {
    backupDatabase(file, null);
  }

  /**
   * Create a database backup file. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup file will typically be named: <P> 
   * <DIV style="margin-left: 40px;">
   *   pipeline-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tgz<P>
   * </DIV>
   * 
   * Where <I>YYMMDD</I>.<I>HHMMSS</I> is the year, month, day, hour, minute and second of 
   * the backup.  The backup file is a <B>gzip</B>(1) compressed <B>tar</B>(1) archive of
   * the {@link Glueable GLUE} format files which make of the persistent storage of the
   * Pipeline database. <P> 
   * 
   * Only privileged users may create a database backup. <P> 
   * 
   * @param file
   *   The name of the backup file.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   * 
   * @throws PipelineException 
   *   If unable to perform the backup.
   */ 
  public synchronized void
  backupDatabase
  (
   File file, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException
  {
    if(PackageInfo.sOsType != OsType.Unix)
      throw new PipelineException
	("The backup database operation can only be initiated from a Unix client!");

    verifyConnection();
    
    MiscBackupDatabaseReq req = new MiscBackupDatabaseReq(file, dryRunResults != null); 

    Object obj = performLongTransaction(MasterRequest.BackupDatabase, req, 15000, 60000);  
    if(obj instanceof DryRunRsp) {
      DryRunRsp rsp = (DryRunRsp) obj; 
      dryRunResults.append(rsp.getMessage());
    }
    else if(!(obj instanceof SuccessRsp)) {
      handleFailure(obj);
    }
  } 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get archive related information about the checked-in versions which match the 
   * given criteria. <P> 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> for all nodes.
   * 
   * @param maxArchives
   *   The maximum allowable number of archive volumes which contain the checked-in version
   *   in order for it to be inclued in the returned list or <CODE>null</CODE> for any number 
   *   of archives.
   * 
   * @return 
   *   Information about the archival state of each matching checked-in version. 
   * 
   * @throws PipelineException 
   *   If unable to determine which checked-in versions match the criteria.
   */ 
  public synchronized ArrayList<ArchiveInfo>
  archiveQuery
  (
   String pattern,
   Integer maxArchives
  )
    throws PipelineException
  {
    verifyConnection();

    MiscArchiveQueryReq req = 
      new MiscArchiveQueryReq(pattern, maxArchives);

    Object obj = performLongTransaction(MasterRequest.ArchiveQuery, req, 15000, 60000);  
    if(obj instanceof MiscArchiveQueryRsp) {
      MiscArchiveQueryRsp rsp = (MiscArchiveQueryRsp) obj;
      return rsp.getInfo();
    } 
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for archival purposes. <P> 
   * 
   * File sizes are computed from the target of any symbolic links and therefore reflects the 
   * amount of bytes that would need to be copied if the files where archived.  This may be
   * considerably more than the actual amount of disk space used when several versions of 
   * a node have some identical files. <P> 
   * 
   * @param versions
   *   The fully resolved node names and revision numbers of the checked-in versions.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getArchivedSizes
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscGetArchiveSizesReq req = new MiscGetArchiveSizesReq(versions);

    Object obj = performLongTransaction(MasterRequest.GetArchiveSizes, req, 15000, 60000);  
    if(obj instanceof MiscGetSizesRsp) {
      MiscGetSizesRsp rsp = (MiscGetSizesRsp) obj;
      return rsp.getSizes();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Archive the files associated with the given checked-in versions. <P> 
   * 
   * Only privileged users may create archives. <P> 
   * 
   * @param prefix
   *   A prefix to prepend to the created archive volume name.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed or 
   *   <CODE>null</CODE> to use the default toolset.
   * 
   * @return 
   *   The unique name given to the newly created archive. 
   * 
   * @throws PipelineException
   *   If unable to perform the archive operation succesfully.
   */
  public synchronized String
  archive
  (
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
   throws PipelineException
  {
    return archive(prefix, versions, archiver, toolset, null); 
  }

  /**
   * Archive the files associated with the given checked-in versions. <P> 
   * 
   * Only privileged users may create archives. <P> 
   * 
   * @param prefix
   *   A prefix to prepend to the created archive volume name.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed or 
   *   <CODE>null</CODE> to use the default toolset.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   * 
   * @return 
   *   The unique name given to the newly created archive. 
   * 
   * @throws PipelineException
   *   If unable to perform the archive operation succesfully.
   */
  public synchronized String
  archive
  (
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscArchiveReq req = 
      new MiscArchiveReq(prefix, versions, archiver, toolset, dryRunResults != null); 

    Object obj = performLongTransaction(MasterRequest.Archive, req, 15000, 60000);  
    if(obj instanceof MiscArchiveRsp) {
      MiscArchiveRsp rsp = (MiscArchiveRsp) obj;
      if(dryRunResults != null) 
        dryRunResults.append(rsp.getMessage());
      return rsp.getName();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get offline related information about the checked-in versions which match the 
   * given criteria. <P> 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> for all nodes.
   * 
   * @param excludeLatest
   *   The number of latest checked-in versions of the node to exclude from the returned list
   *   or <CODE>null</CODE> to include all versions.
   * 
   * @param minArchives
   *   The minimum number of archive volumes containing the checked-in version in order for 
   *   it to be inclued in the returned list or <CODE>null</CODE> for any number of archives.
   * 
   * @param unusedOnly
   *   Whether to only include checked-in versions which can be offlined.
   * 
   * @return 
   *   Information about the offline state of each matching checked-in version. 
   * 
   * @throws PipelineException 
   *   If unable to determine which checked-in versions match the criteria.
   */ 
  public synchronized ArrayList<OfflineInfo>
  offlineQuery
  (
   String pattern,
   Integer excludeLatest, 
   Integer minArchives, 
   boolean unusedOnly
  )
    throws PipelineException
  {
    verifyConnection();

    MiscOfflineQueryReq req = 
      new MiscOfflineQueryReq(pattern, excludeLatest, minArchives, unusedOnly);

    Object obj = performLongTransaction(MasterRequest.OfflineQuery, req, 15000, 60000);  
    if(obj instanceof MiscOfflineQueryRsp) {
      MiscOfflineQueryRsp rsp = (MiscOfflineQueryRsp) obj;
      return rsp.getInfo();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /**
   * Get the revision nubers of all offline checked-in versions of the given node. <P>
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException
   *   If unable to determine the offline versions.
   */ 
  public synchronized TreeSet<VersionID> 
  getOfflineVersionIDs
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetByNameReq req = new NodeGetByNameReq(name);

    Object obj = performTransaction(MasterRequest.GetOfflineVersionIDs, req);
    if(obj instanceof NodeGetVersionIDsRsp) {
      NodeGetVersionIDsRsp rsp = (NodeGetVersionIDsRsp) obj;
      return rsp.getVersionIDs();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for offlining purposes. <P> 
   * 
   * File sizes reflect the actual amount of bytes that will be freed from disk if the 
   * given checked-in versions are offlined.  A file will only contribute to this freed
   * size if it a regular file and there are no symbolic links from later online versions 
   * which target and which are not associated with the given versions. <P> 
   * 
   * @param versions
   *   The fully resolved node names and revision numbers of the checked-in versions.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getOfflineSizes
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscGetOfflineSizesReq req = new MiscGetOfflineSizesReq(versions);

    Object obj = performLongTransaction(MasterRequest.GetOfflineSizes, req, 15000, 60000);  
    if(obj instanceof MiscGetSizesRsp) {
      MiscGetSizesRsp rsp = (MiscGetSizesRsp) obj;
      return rsp.getSizes();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Remove the repository files associated with the given checked-in versions. <P> 
   * 
   * All checked-in versions to be offlined must have prevously been included in at least
   * one archive. <P> 
   * 
   * The offline operation will not be perfomed until any currently running database 
   * operations have completed.  Once the offline operation has begun, all new database 
   * operations will blocked until the offline operation is complete.  The this reason, 
   * this should be performed during non-peak hours. <P> 
   * 
   * Only privileged users may offline checked-in versions. <P> 
   *
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
   public synchronized void
   offline
   (
    TreeMap<String,TreeSet<VersionID>> versions
   ) 
     throws PipelineException
   {
     offline(versions, null); 
   }

  /**
   * Remove the repository files associated with the given checked-in versions. <P> 
   * 
   * All checked-in versions to be offlined must have prevously been included in at least
   * one archive. <P> 
   * 
   * The offline operation will not be perfomed until any currently running database 
   * operations have completed.  Once the offline operation has begun, all new database 
   * operations will blocked until the offline operation is complete.  The this reason, 
   * this should be performed during non-peak hours. <P> 
   * 
   * Only privileged users may offline checked-in versions. <P> 
   *
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   */ 
  public synchronized void
  offline
  (
   TreeMap<String,TreeSet<VersionID>> versions, 
   StringBuilder dryRunResults   
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscOfflineReq req = new MiscOfflineReq(versions, dryRunResults != null); 

    Object obj = performLongTransaction(MasterRequest.Offline, req, 15000, 60000);
    if(obj instanceof DryRunRsp) {
      DryRunRsp rsp = (DryRunRsp) obj; 
      dryRunResults.append(rsp.getMessage());
    }
    else if(!(obj instanceof SuccessRsp)) {
      handleFailure(obj);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of the offline checked-in versions who's names 
   * match the given criteria. <P> 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> for all nodes.
   * 
   * @return 
   *   The fully resolved node names and revision numbers of the matching versions.
   * 
   * @throws PipelineException 
   *   If unable to determine which checked-in versions match the criteria.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>> 
  restoreQuery
  (
   String pattern
  )
    throws PipelineException
  {
    verifyConnection();

    MiscRestoreQueryReq req = new MiscRestoreQueryReq(pattern);

    Object obj = performLongTransaction(MasterRequest.RestoreQuery, req, 15000, 60000);  
    if(obj instanceof MiscRestoreQueryRsp) {
      MiscRestoreQueryRsp rsp = (MiscRestoreQueryRsp) obj;
      return rsp.getVersions();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /**
   * Submit a request to restore the given set of checked-in versions.
   *
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */ 
  public synchronized void
  requestRestore
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscRequestRestoreReq req = new MiscRequestRestoreReq(versions);
    Object obj = performTransaction(MasterRequest.RequestRestore, req);
    handleSimpleResponse(obj);    
  }

  /**
   * Deny the request to restore the given set of checked-in versions.
   *
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */ 
  public synchronized void
  denyRestore
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscDenyRestoreReq req = new MiscDenyRestoreReq(versions);
    Object obj = performTransaction(MasterRequest.DenyRestore, req);
    handleSimpleResponse(obj);    
  }

  /**
   * Get the requests for restoration of checked-in versions.
   * 
   * @return 
   *   The restore requests for checked-in versions indexed by 
   *   fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,RestoreRequest>>
  getRestoreRequests() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetRestoreRequests, null);
    if(obj instanceof MiscGetRestoreRequestsRsp) {
      MiscGetRestoreRequestsRsp rsp = (MiscGetRestoreRequestsRsp) obj;
      return rsp.getRequests();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for restoration purposes. <P> 
   * 
   * File sizes reflect the total amount of bytes of disk space that will be need to be 
   * available in order to restore the given offline checked-in versions.  The actual 
   * amount of disk space used after the completion of the restore operation may be less
   * than this amount if some of the restored files are identical to the corresponding
   * files in an earlier online version.  <P> 
   * 
   * @param versions
   *   The fully resolved node names and revision numbers of the checked-in versions.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getRestoreSizes
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscGetRestoreSizesReq req = new MiscGetRestoreSizesReq(versions);

    Object obj = performTransaction(MasterRequest.GetRestoreSizes, req);
    if(obj instanceof MiscGetSizesRsp) {
      MiscGetSizesRsp rsp = (MiscGetSizesRsp) obj;
      return rsp.getSizes();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Restore the given checked-in versions from the given archive volume. <P> 
   * 
   * Only privileged users may restore checked-in versions. <P> 
   * 
   * If an alternative archiver plugin instance is passed as the <CODE>archiver</CODE>
   * parameter, it must have exactly the same plugin name and revision number as the
   * archiver plugin used to create the archive volume.  The archiver plugin parameters
   * can be different to allow for possible changes in file system or site organization.
   * 
   * @param name
   *   The unique name of the archive containing the checked-in versions to restore.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to restore.
   * 
   * @param archiver
   *   The alternative archiver plugin instance used to perform the restore operation
   *   or <CODE>null</CODE> to use the original archiver.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed
   *   or <CODE>null</CODE> to use the original toolset. 
   * 
   * @throws PipelineException
   *   If unable to restore the checked-in versions.
   */
   public synchronized void
   restore
   (
    String name, 
    TreeMap<String,TreeSet<VersionID>> versions, 
    BaseArchiver archiver, 
    String toolset
   ) 
     throws PipelineException
   {
     restore(name, versions, archiver, toolset, null);
   }

  /**
   * Restore the given checked-in versions from the given archive volume. <P> 
   * 
   * Only privileged users may restore checked-in versions. <P> 
   * 
   * If an alternative archiver plugin instance is passed as the <CODE>archiver</CODE>
   * parameter, it must have exactly the same plugin name and revision number as the
   * archiver plugin used to create the archive volume.  The archiver plugin parameters
   * can be different to allow for possible changes in file system or site organization.
   * 
   * @param name
   *   The unique name of the archive containing the checked-in versions to restore.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to restore.
   * 
   * @param archiver
   *   The alternative archiver plugin instance used to perform the restore operation
   *   or <CODE>null</CODE> to use the original archiver.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed
   *   or <CODE>null</CODE> to use the original toolset. 
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   * 
   * @throws PipelineException
   *   If unable to restore the checked-in versions.
   */
  public synchronized void
  restore
  (
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscRestoreReq req = 
      new MiscRestoreReq(name, versions, archiver, toolset, dryRunResults != null);

    Object obj = performLongTransaction(MasterRequest.Restore, req, 15000, 60000);  
    if(obj instanceof DryRunRsp) {
      DryRunRsp rsp = (DryRunRsp) obj; 
      dryRunResults.append(rsp.getMessage());
    }
    else if(!(obj instanceof SuccessRsp)) {
      handleFailure(obj);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and creation timestamps of all existing archives. <P> 
   *
   * @return 
   *   The timestamps (milliseconds since midnight, January 1, 1970 UTC) of when each 
   *   archive was created indexed by unique archive volume name.
   * 
   * @throws PipelineException
   *   If unable to determine when the archive volumes where created.
   */ 
  public synchronized TreeMap<String,Long> 
  getArchivedOn() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetArchivedOn, null);
    if(obj instanceof MiscGetArchivedOnRsp) {
      MiscGetArchivedOnRsp rsp = (MiscGetArchivedOnRsp) obj;
      return rsp.getIndex();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Get the names and restoration timestamps of all existing archives. <P> 
   *
   * @return 
   *   The timestamps (milliseconds since midnight, January 1, 1970 UTC) of when each 
   *   archive was restored indexed by unique archive volume name.
   * 
   * @throws PipelineException
   *   If unable to determine when the archive volumes where restored.
   */ 
  public synchronized TreeMap<String,TreeSet<Long>>
  getRestoredOn() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetRestoredOn, null);
    if(obj instanceof MiscGetRestoredOnRsp) {
      MiscGetRestoredOnRsp rsp = (MiscGetRestoredOnRsp) obj;
      return rsp.getIndex();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Get the STDOUT output from running the Archiver plugin during the creation of the 
   * given archive volume.
   *
   * @param aname
   *   The name of the archive volume.
   * 
   * @return 
   *   The STDOUT output.
   * 
   * @throws PipelineException
   *   If unable to find an archive volume with the given name.
   */ 
  public synchronized String
  getArchivedOutput
  (
   String aname
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetArchivedOutputReq req = new MiscGetArchivedOutputReq(aname);

    Object obj = performTransaction(MasterRequest.GetArchivedOutput, req);
    if(obj instanceof MiscGetArchiverOutputRsp) {
      MiscGetArchiverOutputRsp rsp = (MiscGetArchiverOutputRsp) obj;
      return rsp.getOutput();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /**
   * Get the STDOUT output from running the Archiver plugin during the restoration of the 
   * given archive volume at the given time.
   *
   * @param aname
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   *   archive volume was restored.
   * 
   * @return 
   *   The STDOUT output.
   * 
   * @throws PipelineException
   *   If unable to find an archive volume with the given name.
   */ 
  public synchronized String
  getRestoredOutput
  (
   String aname, 
   long stamp
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetRestoredOutputReq req = new MiscGetRestoredOutputReq(aname, stamp);

    Object obj = performTransaction(MasterRequest.GetRestoredOutput, req);
    if(obj instanceof MiscGetArchiverOutputRsp) {
      MiscGetArchiverOutputRsp rsp = (MiscGetArchiverOutputRsp) obj;
      return rsp.getOutput();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /**
   * Get the names of the archive volumes containing the given checked-in versions. <P> 
   * 
   * If there are no archive volume which contain a checked-in version, it will be ommitted
   * from the returned tables.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   *
   * @return 
   *   The names of the archives containing the requested checked-in versions indexed by 
   *   fully resolved node name and revision number.
   * 
   * @throws PipelineException 
   *   If determine which archives contain the versions.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,TreeSet<String>>>
  getArchivesContaining
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetArchivesContainingReq req = new MiscGetArchivesContainingReq(versions);
    Object obj = performLongTransaction(MasterRequest.GetArchivesContaining, req, 
					15000, 60000);  
    if(obj instanceof MiscGetArchivesContainingRsp) {
      MiscGetArchivesContainingRsp rsp = (MiscGetArchivesContainingRsp) obj;
      return rsp.getArchiveNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 

  /**
   * Get the complete information about the archive with the given name.
   * 
   * @param name
   *   The unique name of the archive.
   * 
   * @return 
   *   The archive information.
   * 
   * @throws PipelineException
   *   If unable to find the archive.
   */ 
  public synchronized ArchiveVolume
  getArchive
  (
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscGetArchiveReq req = new MiscGetArchiveReq(name);
    Object obj = performTransaction(MasterRequest.GetArchive, req);
    if(obj instanceof MiscGetArchiveRsp) {
      MiscGetArchiveRsp rsp = (MiscGetArchiveRsp) obj;
      return rsp.getArchive();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a default saved panel layout file. <P> 
   * 
   * The layout is copied from an initial default layout provided with the Pipeline release.
   * This layout is provided as a helpful starting point for new users when creating custom
   * layouts.  The created panels will be set to view the working area specified by the 
   * <CODE>author</CODE> and <CODE>view</CODE> parameters. 
   * 
   * @param name
   *   The name of the created layout.
   * 
   * @param author
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view.
   * 
   * @throws PipelineException 
   *   If unable to create the layout.
   */ 
  public synchronized void
  createInitialPanelLayout
  (
   String name,
   String author,  
   String view
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscCreateInitialPanelLayoutReq req = new MiscCreateInitialPanelLayoutReq(author, view);
    Object obj = performTransaction(MasterRequest.CreateInitialPanelLayout, req);
    if(obj instanceof MiscCreateInitialPanelLayoutRsp) {
      MiscCreateInitialPanelLayoutRsp rsp = (MiscCreateInitialPanelLayoutRsp) obj;

      Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts"); 
      Path path = new Path(lpath, name);
      File file = path.toFile();
      try {
	file.delete();
	FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
	FileLock lock = chan.tryLock();
	if(lock == null) 
	  throw new PipelineException
	    ("Unable to aquire lock for Glue file (" + file + ")!");
	
	try {
	  ByteBuffer buf = ByteBuffer.wrap(rsp.getContents().getBytes());
	  chan.write(buf);
	}
	finally {
	  lock.release();
	  chan.close();
	}
      }
      catch (Exception ex) {
	throw new PipelineException
	  ("Unable to save Glue file (" + file + "):\n" + 
	   "  " + ex);
      }
    }
    else {
      handleFailure(obj);
    }       
  } 

  /**
   * Retrieve the contents of the site default saved panel layout file. <P> 
   * 
   * The layout is copied from an initial default layout provided with the Pipeline release.
   * This layout is provided as a helpful starting point for new users when creating custom
   * layouts.  The created panels will be set to view the working area specified by the 
   * <CODE>author</CODE> and <CODE>view</CODE> parameters. 
   * 
   * @param author
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view.
   * 
   * @throws PipelineException 
   *   If unable to create the layout.
   */ 
  public synchronized String
  getInitialPanelLayout
  (
   String author,  
   String view
  ) 
    throws PipelineException
  {
    verifyConnection();

    MiscCreateInitialPanelLayoutReq req = new MiscCreateInitialPanelLayoutReq(author, view);
    Object obj = performTransaction(MasterRequest.CreateInitialPanelLayout, req);
    if(obj instanceof MiscCreateInitialPanelLayoutRsp) {
      MiscCreateInitialPanelLayoutRsp rsp = (MiscCreateInitialPanelLayoutRsp) obj;

      return rsp.getContents();
    }
    else {
      handleFailure(obj);
      return null;
    }       
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.
   */ 
  @Override
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new PluginInputStream(in);
  }

  
  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  @Override
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plmaster(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }

}

