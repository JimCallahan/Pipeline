// $Id: MasterMgrClient.java,v 1.36 2004/11/02 23:06:44 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R    M G R   C L I E N T                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline master server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1).  This class represents the
 * interface used by all Pipeline client programs and end user tools to interact with the 
 * Pipeline system.
 */
public
class MasterMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>plmaster</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plmaster</B>(1).
   */
  public
  MasterMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  MasterRequest.Disconnect, MasterRequest.Shutdown);
  }

  /** 
   * Construct a new master manager client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--master-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--master-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  MasterMgrClient() 
  {
    super(PackageInfo.sMasterServer, PackageInfo.sMasterPort, 
	  MasterRequest.Disconnect, MasterRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  public synchronized void 
  shutdown() 
    throws PipelineException 
  {
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may shutdown the servers!");
    
    super.shutdown();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default toolset.
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
   * Set the default toolset name. <P> 
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
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may set the default toolset!");

    verifyConnection();

    MiscSetDefaultToolsetNameReq req = new MiscSetDefaultToolsetNameReq(name);

    Object obj = performTransaction(MasterRequest.SetDefaultToolsetName, req);
    handleSimpleResponse(obj);    
  }


  /**
   * Get the names of the currently active toolsets.
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
   * Set the active/inactive state of the toolset with the given name. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
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
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may change the active status of a toolset!");

    verifyConnection();

    MiscSetToolsetActiveReq req = new MiscSetToolsetActiveReq(name, isActive);

    Object obj = performTransaction(MasterRequest.SetToolsetActive, req);
    handleSimpleResponse(obj);    
  }


  /**
   * Get the names of all toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public synchronized TreeSet<String>
  getToolsetNames() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetToolsetNames, null);
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
   * Get the toolset with the given name.
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
    verifyConnection();

    MiscGetToolsetReq req = new MiscGetToolsetReq(name);

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
   * Get the cooked toolset environment specific to the given user and working area. <P> 
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
    verifyConnection();

    MiscGetToolsetEnvironmentReq req = new MiscGetToolsetEnvironmentReq(author, view, tname);

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

  /**
   * Create a new toolset and possibly new package versions from the given set of 
   * packages. <P>
   * 
   * New packages will only be created if the generated toolset defined by evaluating 
   * the <CODE>packages</CODE> argument has no environment conflicts (see 
   * {@link Toolset#hasConflicts Toolset.hasConflicts}).  If any conflicts are detected, 
   * an exception will be thrown and no new toolset will be created. <P> 
   * 
   * All packages given must already exist on the master server.  Only the names and 
   * revision numbers of the given packages are passed to the server which then looks up 
   * its own copy of the toolset package (identified by the name and revision number) to 
   * generate the toolset.  This insures that the contents of the toolset package cannot 
   * be altered by client programs. <P> 
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
   Collection<PackageVersion> packages
  ) 
    throws PipelineException  
  {
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may create new toolsets!");

    verifyConnection();

    ArrayList<String> names = new ArrayList<String>();
    TreeMap<String,VersionID> versions = new TreeMap<String,VersionID>();
    for(PackageVersion pkg : packages) {
      names.add(pkg.getName());
      versions.put(pkg.getName(), pkg.getVersionID());
    }

    MiscCreateToolsetReq req =
      new MiscCreateToolsetReq(PackageInfo.sUser, name, desc, names, versions);

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

  /**
   * Get the names and revision numbers of all toolset packages.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getToolsetPackageNames() 
    throws PipelineException    
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetToolsetPackageNames, null);
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
   * Get the toolset package with the given name and revision number. 
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
    verifyConnection();

    MiscGetToolsetPackageReq req = new MiscGetToolsetPackageReq(name, vid);

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
   * Create a new read-only toolset package from the given modifiable package. <P> 
   * 
   * Once created, the read-only package can not be altered and will remain accessable
   * forever.  Only read-only toolset packages can be used to create toolset, therefore
   * this method is a necessary prerequisite to building a new toolset from modifiable 
   * toolset packages. <P> 
   * 
   * The <CODE>level</CODE> argument may be <CODE>null</CODE> if this is the first 
   * revision of the package. <P> 
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
   VersionID.Level level
  ) 
    throws PipelineException  
  {
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may create new toolset packages!");

    verifyConnection();

    MiscCreateToolsetPackageReq req =
      new MiscCreateToolsetPackageReq(PackageInfo.sUser, mod, desc, level);

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
  /*   E D I T O R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get default editor name for the given filename suffix and current user. <P> 
   * 
   * @param suffix
   *   The filename suffix.
   * 
   * @return 
   *   The editor name of <CODE>null</CODE> if undefined.
   * 
   * @throws PipelineException
   *   If unable to determine the editor name.
   */ 
  public synchronized String
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
   * Get the filename suffix to default editor mappings for the current user. <P> 
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
   * Set the filename suffix to default editor mappings for the current user. <P> 
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
  /*   P R I V I L E G E D   U S E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the privileged users. <P> 
   * 
   * Privileged users are allowed to perform operations which are restricted for normal
   * users. In general privileged access is required when an operation is dangerous or 
   * involves making changes which affect all users. The "pipeline" user is always 
   * privileged. <P> 
   * 
   * Each client caches the set of privileged users recieved from the master server the 
   * first time this method is called and uses this cache instead of network communication
   * for subsequent calls.  This cache can be ignored and rebuilt if the <CODE>useCache</CODE>
   * argument is set to <CODE>false</CODE>.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized TreeSet<String> 
  getPrivilegedUsers
  (
   boolean useCache   
  ) 
    throws PipelineException
  {
    if(!useCache || (pPrivilegedUsers == null)) 
      updatePrivilegedUsers();

    return new TreeSet<String>(pPrivilegedUsers);
  }

  /**
   * Is the given user privileged? <P> 
   * 
   * Privileged users are allowed to perform operations which are restricted for normal
   * users. In general privileged access is required when an operation is dangerous or 
   * involves making changes which affect all users. The "pipeline" user is always 
   * privileged. <P> 
   * 
   * Each client caches the set of privileged users recieved from the master server the 
   * first time this method is called and uses this cache instead of network communication
   * for subsequent calls.  This cache can be ignored and rebuilt if the <CODE>useCache</CODE>
   * argument is set to <CODE>false</CODE>.
   * 
   * @param author
   *   The user in question.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  (
   String author, 
   boolean useCache
  ) 
    throws PipelineException
  {
    if(author.equals(PackageInfo.sPipelineUser)) 
      return true;

    if(!useCache || (pPrivilegedUsers == null)) 
      updatePrivilegedUsers();
    assert(pPrivilegedUsers != null);

    return pPrivilegedUsers.contains(author);
  }

  /**
   * Is the given user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with a <CODE>useCache</CODE> argument of <CODE>true</CODE>.
   * 
   * @param author
   *   The user in question.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  (
   String author
  ) 
    throws PipelineException
  {
    return isPrivileged(author, true);
  }

  /**
   * Is the current user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with the current user as the <CODE>author</CODE> argument.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the current user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  ( 
   boolean useCache
  ) 
    throws PipelineException
  {
    return isPrivileged(PackageInfo.sUser, useCache);
  }

  /**
   * Is the current user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with the current user as the <CODE>author</CODE> argument and a <CODE>useCache</CODE> 
   * argument of <CODE>true</CODE>.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged() 
    throws PipelineException
  {
    return isPrivileged(PackageInfo.sUser, true);
  }

  /**
   * Update the local cache of privileged users.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  private synchronized void 
  updatePrivilegedUsers() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.GetPrivilegedUsers, null);
    if(obj instanceof MiscGetPrivilegedUsersRsp) {
      MiscGetPrivilegedUsersRsp rsp = (MiscGetPrivilegedUsersRsp) obj;
      pPrivilegedUsers = rsp.getUsers();
    }
    else {
      handleFailure(obj);
      return;
    }
  }
  

  /**
   * Grant the given user privileged access status. <P> 
   * 
   * This method may only be called by the "pipeline" user.  An exception will be thrown
   * if called by any other user.
   * 
   * @param author
   *   The user to make privileged.
   * 
   * @throws PipelineException
   *   If unable to make the given user privileged.
   */
  public synchronized void 
  grantPrivileges
  (
   String author
  ) 
    throws PipelineException    
  {
    if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
      throw new PipelineException
	("Only the \"pipeline\" user may change a user's privileges!");

    /* invalidate the cache */ 
    pPrivilegedUsers = null;

    verifyConnection();

    MiscGrantPrivilegesReq req = new MiscGrantPrivilegesReq(author);

    Object obj = performTransaction(MasterRequest.GrantPrivileges, req);
    handleSimpleResponse(obj);
  }
   
  /**
   * Remove the given user's privileged access status. <P> 
   * 
   * This method may only be called by the "pipeline" user.  An exception will be thrown
   * if called by any other user.
   * 
   * @param author
   *   The user to remove privileges from.
   * 
   * @throws PipelineException
   *   If unable to remove the given user's privileges.
   */
  public synchronized void 
  removePrivileges
  (
   String author
  ) 
    throws PipelineException    
  {
    if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
      throw new PipelineException
	("Only the \"pipeline\" user may change a user's privileges!");

    /* invalidate the cache */ 
    pPrivilegedUsers = null;

    verifyConnection();

    MiscRemovePrivilegesReq req = new MiscRemovePrivilegesReq(author);

    Object obj = performTransaction(MasterRequest.RemovePrivileges, req);
    handleSimpleResponse(obj);
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   A R E A S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of current working area authors and views
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
   * Create a new empty working area for the given user and view. <P> 
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
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may create working areas owned by another user!");

    verifyConnection();

    NodeCreateWorkingAreaReq req = new NodeCreateWorkingAreaReq(author, view);

    Object obj = performTransaction(MasterRequest.CreateWorkingArea, req);
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   P A T H S                                                                  */
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
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the working version of the node for the given user. <P> 
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
   * Get the working version of the node for the given user. <P> 
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
   * Set the node properties of the working version of the node for the given user. <P> 
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
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may modify nodes owned by another user!");

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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a secondary file sequence to the given working version.
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
   * Add a secondary file sequence to the given working version.
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
   * Remove a secondary file sequence from the given working version.
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
   * Remove a secondary file sequence from the given working version.
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
   * Rename a working version of a node owned by the given user which has never 
   * been checked-in. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in. If a working version is successfully renamed, all node connections 
   * will be preserved. <P> 
   * 
   * In addition to changing the name of the working version, this operation can also 
   * rename the files associated with the working version to match the new node name if 
   * the <CODE>renameFiles</CODE> argument is <CODE>true</CODE>.  The primary file sequence
   * will be renamed to have a prefix which is identical to the last component of the 
   * <CODE>newName</CODE> argument.  The secondary file sequence prefixes will remain
   * unchanged. Both primary and secondary file sequences will be moved into the working 
   * directory based on the new node name. <P> 
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
   * @param oldName 
   *   The current fully resolved node name.
   * 
   * @param newName 
   *   The new fully resolved node name.
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
   String oldName, 
   String newName,
   boolean renameFiles
  ) 
    throws PipelineException
  {
    rename(new NodeID(author, view, oldName), newName, renameFiles);
  } 

  /**
   * Rename a working version of a node owned by the given user which has never 
   * been checked-in. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in. If a working version is successfully renamed, all node connections 
   * will be preserved. <P> 
   * 
   * In addition to changing the name of the working version, this operation can also 
   * rename the files associated with the working version to match the new node name if 
   * the <CODE>renameFiles</CODE> argument is <CODE>true</CODE>.  The primary file sequence
   * will be renamed to have a prefix which is identical to the last component of the 
   * <CODE>newName</CODE> argument.  The secondary file sequence prefixes will remain
   * unchanged. Both primary and secondary file sequences will be moved into the working 
   * directory based on the new node name. <P> 
   * 
   * If the <CODE>nodeID</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param newName 
   *   The new fully resolved node name.
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
   String newName,
   boolean renameFiles
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may rename nodes owned by another user!");

    verifyConnection();

    NodeRenameReq req = new NodeRenameReq(nodeID, newName, renameFiles);

    Object obj = performTransaction(MasterRequest.Rename, req);
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Renumber the frame ranges of the file sequences associated with the given node. <P> 
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
   * @throws PipelineException 
   *   If unable to renumber the given node or its associated primary files.
   */ 
  public synchronized void 
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
    renumber(new NodeID(author, view, name), range, removeFiles);
  } 

  /**
   * Renumber the frame ranges of the file sequences associated with the given node. <P> 
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
   * @throws PipelineException 
   *   If unable to renumber the given node or its associated primary files.
   */ 
  public synchronized void 
  renumber
  ( 
   NodeID nodeID,
   FrameRange range, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may renumber nodes owned by another user!");

    verifyConnection();

    NodeRenumberReq req = new NodeRenumberReq(nodeID, range, removeFiles);

    Object obj = performTransaction(MasterRequest.Renumber, req);
    handleSimpleResponse(obj);
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the revision numbers of all checked-in versions of the given node. <P> 
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
	 
    NodeGetCheckedInVersionIDsReq req = new NodeGetCheckedInVersionIDsReq(name);

    Object obj = performTransaction(MasterRequest.GetCheckedInVersionIDs, req);
    if(obj instanceof NodeGetCheckedInVersionIDsRsp) {
      NodeGetCheckedInVersionIDsRsp rsp = (NodeGetCheckedInVersionIDsRsp) obj;
      return rsp.getVersionIDs();      
    }
    else {
      handleFailure(obj);
      return null;
    }
  }  

  /** 
   * Get the checked-in version of the node with the given revision number. <P> 
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
   * Get the log messages associated with all checked-in versions of the given node.
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
	 
    NodeGetHistoryReq req = new NodeGetHistoryReq(name);

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
   * Get whether each file associated with each checked-in version of the given node 
   * contains new data not present in the previous checked-in versions. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @return 
   *   The table of per-file novelty flags indexed by revision number and file sequence.
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
    
    NodeGetCheckedInFileNoveltyReq req = new NodeGetCheckedInFileNoveltyReq(name);
    
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
  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T U S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the status of the tree of nodes rooted at the given node. <P> 
   * 
   * In addition to providing node status information for the given node, the returned 
   * <CODE>NodeStatus</CODE> instance can be used access the status of all nodes (both 
   * upstream and downstream) linked to the given node.  The status information for the 
   * upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method.
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
    return status(new NodeID(author, view, name));
  } 

  /** 
   * Get the status of the tree of nodes rooted at the given node. <P> 
   * 
   * In addition to providing node status information for the given node, the returned 
   * <CODE>NodeStatus</CODE> instance can be used access the status of all nodes (both 
   * upstream and downstream) linked to the given node.  The status information for the 
   * upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method.
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
    verifyConnection();
 
    NodeStatusReq req = new NodeStatusReq(nodeID);

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


  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node owned by the given user. <P> 
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
   * @throws PipelineException
   *   If unable to register the given node.
   */
  public synchronized void 
  register
  ( 
   String author, 
   String view, 
   NodeMod mod
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may register nodes owned by another user!");

    verifyConnection();

    NodeID id = new NodeID(author, view, mod.getName());
    NodeRegisterReq req = new NodeRegisterReq(id, mod);

    Object obj = performTransaction(MasterRequest.Register, req);
    handleSimpleResponse(obj);
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
    release(new NodeID(author, view, name), removeFiles);
  } 

  /**
   * Release the working version of a node and optionally remove the associated 
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
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may release nodes owned by another user!");

    verifyConnection();

    NodeReleaseReq req = new NodeReleaseReq(nodeID, removeFiles);

    Object obj = performTransaction(MasterRequest.Release, req);
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
    if(!isPrivileged(false))
      throw new PipelineException
	("Only privileged users may delete nodes!"); 

    verifyConnection();

    NodeDeleteReq req = new NodeDeleteReq(name, removeFiles);

    Object obj = performTransaction(MasterRequest.Delete, req);
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-In the tree of nodes owned by the given user rooted at the given working 
   * version. <P> 
   * 
   * The check-in operation proceeds in a depth-first manner checking-in the most upstream
   * nodes first.  The check-in operation aborts at the first failure of a particular node. 
   * It is therefore possible for the overall check-in to fail after already succeeding for 
   * some set of upstream nodes. <P> 
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method. <P>
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
   * Check-In the tree of nodes owned by the given user rooted at the given working 
   * version. <P> 
   * 
   * The check-in operation proceeds in a depth-first manner checking-in the most upstream
   * nodes first.  The check-in operation aborts at the first failure of a particular node. 
   * It is therefore possible for the overall check-in to fail after already succeeding for 
   * some set of upstream nodes. <P> 
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method. <P>
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
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may check-in nodes owned by another user!");

    verifyConnection();

    NodeCheckInReq req = new NodeCheckInReq(nodeID, msg, level);

    Object obj = performTransaction(MasterRequest.CheckIn, req);
    handleSimpleResponse(obj);
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-Out the tree of nodes owned by the given user rooted at the given working 
   * version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P>
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method. <P>
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
   * @throws PipelineException
   *   If unable to check-out the nodes.
   */ 
  public synchronized void
  checkOut
  ( 
   String author, 
   String view, 
   String name, 
   VersionID vid, 
   CheckOutMode mode
  ) 
    throws PipelineException
  {
    checkOut(new NodeID(author, view, name), vid, mode);
  } 

  /** 
   * Check-Out the tree of nodes owned by the given user rooted at the given working 
   * version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P>
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method. <P>
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
   * @throws PipelineException
   *   If unable to check-out the nodes.
   */ 
  public synchronized void
  checkOut
  ( 
   NodeID nodeID,
   VersionID vid, 
   CheckOutMode mode
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may check-in nodes owned by another user!");

    verifyConnection();

    NodeCheckOutReq req = new NodeCheckOutReq(nodeID, vid, mode);

    Object obj = performTransaction(MasterRequest.CheckOut, req);
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
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may revert files owned by another user!");
    
    verifyConnection();

    NodeRevertFilesReq req = new NodeRevertFilesReq(nodeID, files);
    
    Object obj = performTransaction(MasterRequest.RevertFiles, req);
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
   *   If unable to revert the files.
   */ 
  public synchronized void 
  evolve
  ( 
   NodeID nodeID,
   VersionID vid
  )
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may evolve nodes owned by another user!");
    
    verifyConnection();

    NodeEvolveReq req = new NodeEvolveReq(nodeID, vid);
    
    Object obj = performTransaction(MasterRequest.Evolve, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   Q U E U E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. <P> 
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
   *   The submitted job group.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized QueueJobGroup
  submitJobs
  ( 
   String author, 
   String view, 
   String name, 
   TreeSet<Integer> indices
  ) 
    throws PipelineException
  {
    return submitJobs(new NodeID(author, view, name), indices, null, null, null);
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
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @return 
   *   The submitted job group.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized QueueJobGroup
  submitJobs
  ( 
   String author, 
   String view, 
   String name, 
   TreeSet<Integer> indices, 
   Integer batchSize, 
   Integer priority, 
   Set<String> selectionKeys   
  ) 
    throws PipelineException
  {
    return submitJobs(new NodeID(author, view, name), indices, 
		      batchSize, priority, selectionKeys);
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
   *   The submitted job group.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized QueueJobGroup
  submitJobs
  ( 
   NodeID nodeID,
   TreeSet<Integer> indices 
  ) 
    throws PipelineException
  {
    return submitJobs(nodeID, indices, null, null, null);
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
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @return 
   *   The submitted job group.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized QueueJobGroup
  submitJobs
  ( 
   NodeID nodeID,
   TreeSet<Integer> indices,
   Integer batchSize, 
   Integer priority, 
   Set<String> selectionKeys   
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may submit jobs for nodes owned by another user!");

    verifyConnection();

    NodeSubmitJobsReq req = 
      new NodeSubmitJobsReq(nodeID, indices, batchSize, priority, selectionKeys);

    Object obj = performTransaction(MasterRequest.SubmitJobs, req);
    if(obj instanceof NodeSubmitJobsRsp) {
      NodeSubmitJobsRsp rsp = (NodeSubmitJobsRsp) obj;
      return rsp.getJobGroup();
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
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @return 
   *   The submitted job group.
   * 
   * @throws PipelineException
   *   If unable to generate or submit the jobs.
   */ 
  public synchronized QueueJobGroup
  resubmitJobs
  ( 
   NodeID nodeID,
   TreeSet<FileSeq> targetSeqs, 
   Integer batchSize, 
   Integer priority, 
   Set<String> selectionKeys   
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may submit jobs for nodes owned by another user!");

    verifyConnection();

    NodeResubmitJobsReq req = 
      new NodeResubmitJobsReq(nodeID, targetSeqs, batchSize, priority, selectionKeys);

    Object obj = performTransaction(MasterRequest.ResubmitJobs, req);
    if(obj instanceof NodeSubmitJobsRsp) {
      NodeSubmitJobsRsp rsp = (NodeSubmitJobsRsp) obj;
      return rsp.getJobGroup();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the working area files associated with the given node. <P>  
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
   * Remove the working area files associated with the given node. <P>  
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
    if(!PackageInfo.sUser.equals(nodeID.getAuthor()) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may remove files owned by another user!");
    
    verifyConnection();

    NodeRemoveFilesReq req = new NodeRemoveFilesReq(nodeID, indices);

    Object obj = performTransaction(MasterRequest.RemoveFiles, req);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a database backup file and write it to the given directory. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup file will be named: <P> 
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
    if(!isPrivileged(false))
      throw new PipelineException
	("Only privileged users may backup the database!"); 

    verifyConnection();

    MiscBackupDatabaseReq req = new MiscBackupDatabaseReq(file);
    Object obj = performTransaction(MasterRequest.BackupDatabase, req);
    handleSimpleResponse(obj);    
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plmaster(1) server daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached names of the privileged users. <P> 
   *
   * May be <CODE>null</CODE> if the cache has been invalidated.
   */ 
  private TreeSet<String>  pPrivilegedUsers;
  
}

