// $Id: MasterMgrDirectLightClient.java,v 1.1 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.event.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;


/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   M G R   D I R E C T   L I G H T   C L I E N T                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Read-only access to a limited set the Pipeline master manager operations. 
 */
public
class MasterMgrDirectLightClient
  implements MasterMgrLightClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager client.
   */
  public
  MasterMgrDirectLightClient
  (
   MasterMgr master
  ) 
  {
    if(master == null) 
      throw new IllegalArgumentException
        ("The master manager client cannot be (null)!");
    pMasterMgr = master;
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
  public WorkGroups
  getWorkGroups()
    throws PipelineException
  {
    Object obj = pMasterMgr.getWorkGroups(); 
    if(obj instanceof MiscGetWorkGroupsRsp) {
      MiscGetWorkGroupsRsp rsp = (MiscGetWorkGroupsRsp) obj;
      return rsp.getGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }
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
  public TreeMap<String,Privileges>   
  getPrivileges()
    throws PipelineException
  {
    Object obj = pMasterMgr.getPrivileges();
    if(obj instanceof MiscGetPrivilegesRsp) {
      MiscGetPrivilegesRsp rsp = (MiscGetPrivilegesRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }    
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
  public PrivilegeDetails
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
  public PrivilegeDetails
  getPrivilegeDetails
  (
    String uname
  )
    throws PipelineException
  {
    MiscGetPrivilegeDetailsReq req = new MiscGetPrivilegeDetailsReq(uname);
    
    Object obj = pMasterMgr.getPrivilegeDetails(req);
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
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default Unix toolset.
   * 
   * @throws PipelineException
   *   If unable to determine the default toolset name.
   */ 
  public String
  getDefaultToolsetName() 
    throws PipelineException
  {    
    Object obj = pMasterMgr.getDefaultToolsetName();
    if(obj instanceof MiscGetDefaultToolsetNameRsp) {
      MiscGetDefaultToolsetNameRsp rsp = (MiscGetDefaultToolsetNameRsp) obj;
      return rsp.getName();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently active Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public TreeSet<String>
  getActiveToolsetNames() 
    throws PipelineException
  {    
    Object obj = pMasterMgr.getActiveToolsetNames();
    if(obj instanceof MiscGetActiveToolsetNamesRsp) {
      MiscGetActiveToolsetNamesRsp rsp = (MiscGetActiveToolsetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public TreeSet<String>
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
  public TreeSet<String>
  getToolsetNames
  (
   OsType os
  ) 
    throws PipelineException
  {
    MiscGetToolsetNamesReq req = new MiscGetToolsetNamesReq(os);

    Object obj = pMasterMgr.getToolsetNames(req);
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
  public TreeMap<String,TreeSet<OsType>>
  getAllToolsetNames() 
    throws PipelineException
  {
    Object obj = pMasterMgr.getAllToolsetNames(); 
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
  public Toolset
  getToolset
  (
   String name
  ) 
    throws PipelineException
  {
    return getToolset(name, OsType.Unix);
  }

  /**
   * Get a OS specific toolset.
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
  public Toolset
  getToolset
  (
   String name, 
   OsType os
  ) 
    throws PipelineException
  {
    MiscGetToolsetReq req = new MiscGetToolsetReq(name, os);

    Object obj = pMasterMgr.getToolset(req);
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
  public TreeMap<OsType,Toolset>
  getOsToolsets
  (
   String name
  ) 
    throws PipelineException
  {
    MiscGetOsToolsetsReq req = new MiscGetOsToolsetsReq(name);

    Object obj = pMasterMgr.getOsToolsets(req);
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
  public TreeMap<String,String> 
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
  public TreeMap<String,String> 
  getToolsetEnvironment
  (
   String author, 
   String view,
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    MiscGetToolsetEnvironmentReq req = 
      new MiscGetToolsetEnvironmentReq(author, view, tname, os);

    Object obj = pMasterMgr.getToolsetEnvironment(req);
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
  /*  T O O L S E T   P A C K A G E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of all Unix toolset packages.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
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
  public TreeMap<String,TreeSet<VersionID>>
  getToolsetPackageNames
  (
   OsType os
  ) 
    throws PipelineException    
  {
    MiscGetToolsetPackageNamesReq req = new MiscGetToolsetPackageNamesReq(os);

    Object obj = pMasterMgr.getToolsetPackageNames(req);
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
  public DoubleMap<String,OsType,TreeSet<VersionID>>
  getAllToolsetPackageNames()
    throws PipelineException
  {
    Object obj = pMasterMgr.getAllToolsetPackageNames();
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
  public PackageVersion
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
  public PackageVersion
  getToolsetPackage
  (
   String name, 
   VersionID vid, 
   OsType os
  ) 
    throws PipelineException 
  {
    MiscGetToolsetPackageReq req = new MiscGetToolsetPackageReq(name, vid, os);

    Object obj = pMasterMgr.getToolsetPackage(req);
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
  public TripleMap<String,VersionID,OsType,PackageVersion> 
  getToolsetPackages
  (
   DoubleMap<String,VersionID,TreeSet<OsType>> packages
  ) 
    throws PipelineException  
  {
    if(packages.isEmpty()) 
      throw new PipelineException("No packages where specified!");

    MiscGetToolsetPackagesReq req = new MiscGetToolsetPackagesReq(packages);

    Object obj = pMasterMgr.getToolsetPackages(req);
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
  public TreeMap<String,TreeSet<String>>
  getWorkingAreas() 
    throws PipelineException
  {
    Object obj = pMasterMgr.getWorkingAreas();
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
  public TreeMap<String,TreeSet<String>>
  getWorkingAreasContaining
  (
   String name
  ) 
    throws PipelineException
  {
    NodeGetWorkingAreasContainingReq req = new NodeGetWorkingAreasContainingReq(name);

    Object obj = pMasterMgr.getWorkingAreasContaining(req);
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
  public TreeSet<String> 
  getNodeNames
  (
   String pattern
  )
    throws PipelineException
  {
    NodeGetNodeNamesReq req = new NodeGetNodeNamesReq(pattern);

    Object obj = pMasterMgr.getNodeNames(req);
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
  public NodeTreeComp
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
  public NodeTreeComp
  updatePaths
  (
   String author, 
   String view, 
   TreeMap<String,Boolean> paths
  ) 
    throws PipelineException
  {
    NodeUpdatePathsReq req = new NodeUpdatePathsReq(author, view, paths);

    Object obj = pMasterMgr.updatePaths(req);
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
  public String
  getNodeOwning
  (
   String path
  ) 
    throws PipelineException
  {
    NodeGetNodeOwningReq req = new NodeGetNodeOwningReq(path);

    Object obj = pMasterMgr.getNodeOwning(req);
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
   * Get a specific annotation for the given node.<P> 
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
  public BaseAnnotation
  getAnnotation
  (
   String nname, 
   String aname
  ) 
    throws PipelineException
  {
    NodeGetAnnotationReq req = new NodeGetAnnotationReq(nname, aname); 

    Object obj = pMasterMgr.getAnnotation(req);
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
   * Get all of the annotations for the given node.<P> 
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
  public TreeMap<String,BaseAnnotation> 
  getAnnotations
  (
   String name
  ) 
    throws PipelineException
  {
    NodeGetAnnotationsReq req = new NodeGetAnnotationsReq(name); 

    Object obj = pMasterMgr.getAnnotations(req);
    if(obj instanceof NodeGetAnnotationsRsp) {
      NodeGetAnnotationsRsp rsp = (NodeGetAnnotationsRsp) obj;
      return rsp.getAnnotations();
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
  public TreeSet<String> 
  getWorkingNames
  (
   String author, 
   String view, 
   String pattern
  )
    throws PipelineException
  {
    NodeGetWorkingNamesReq req = 
      new NodeGetWorkingNamesReq(author, view, pattern);

    Object obj = pMasterMgr.getWorkingNames(req);
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
  public NodeMod
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
  public NodeMod
  getWorkingVersion
  ( 
   NodeID nodeID
  ) 
    throws PipelineException
  {
    NodeGetWorkingReq req = new NodeGetWorkingReq(nodeID);

    Object obj = pMasterMgr.getWorkingVersion(req);
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
  public TreeSet<String> 
  getCheckedInNames
  (
   String pattern
  )
    throws PipelineException
  {
    NodeGetNodeNamesReq req = new NodeGetNodeNamesReq(pattern);

    Object obj = pMasterMgr.getCheckedInNames(req);
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
  public TreeSet<VersionID>
  getCheckedInVersionIDs
  ( 
   String name
  ) 
    throws PipelineException
  {	 
    NodeGetCheckedInVersionIDsReq req = new NodeGetCheckedInVersionIDsReq(name);

    Object obj = pMasterMgr.getCheckedInVersionIDs(req);
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
  public NodeVersion
  getCheckedInVersion
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {	 
    NodeGetCheckedInReq req = new NodeGetCheckedInReq(name, vid);

    Object obj = pMasterMgr.getCheckedInVersion(req);
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
  public TreeMap<VersionID,NodeVersion> 
  getAllCheckedInVersions
  ( 
   String name
  ) 
    throws PipelineException
  {	 
    NodeGetAllCheckedInReq req = new NodeGetAllCheckedInReq(name);

    Object obj = pMasterMgr.getAllCheckedInVersions(req);
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
  public TreeMap<VersionID,LogMessage> 
  getHistory
  ( 
   String name
  ) 
    throws PipelineException
  {	 
    NodeGetHistoryReq req = new NodeGetHistoryReq(name);

    Object obj = pMasterMgr.getHistory(req);
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
  public TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>
  getCheckedInFileNovelty
  ( 
   String name
  ) 
    throws PipelineException
  {    
    NodeGetCheckedInFileNoveltyReq req = new NodeGetCheckedInFileNoveltyReq(name);
    
    Object obj = pMasterMgr.getCheckedInFileNovelty(req); 
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
  public DoubleMap<VersionID,String,LinkVersion>
  getCheckedInLinks
  ( 
   String name
  ) 
    throws PipelineException
  {    
    NodeGetCheckedInLinksReq req = new NodeGetCheckedInLinksReq(name);
    
    Object obj = pMasterMgr.getCheckedInLinks(req);
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
  public DoubleMap<String,VersionID,LinkVersion>
  getDownstreamCheckedInLinks
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {    
    NodeGetDownstreamCheckedInLinksReq req = 
      new NodeGetDownstreamCheckedInLinksReq(name, vid);
    
    Object obj = pMasterMgr.getDownstreamCheckedInLinks(req);
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
  public TreeMap<Long,BaseNodeEvent>
  getNodeEvents
  (
   TreeSet<String> names, 
   TreeSet<String> users, 
   TimeInterval interval
  ) 
    throws PipelineException
  {
    NodeGetEventsReq req = new NodeGetEventsReq(names, users, interval); 

    Object obj = pMasterMgr.getNodeEvents(req);
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
  public TreeMap<String,TreeSet<String>>
  getWorkingAreasEditing
  (
   String name
  ) 
    throws PipelineException
  {
    NodeGetWorkingAreasEditingReq req = new NodeGetWorkingAreasEditingReq(name);

    Object obj = pMasterMgr.getWorkingAreasEditing(req); 
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle the simple Success/Failure response.
   * 
   * @param obj
   *   The response from the server.
   */ 
  private void 
  handleSimpleResponse
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(!(obj instanceof SuccessRsp))
      handleFailure(obj);
  }

  /**
   * Handle non-successful responses.
   * 
   * @param obj
   *   The response from the server.
   */ 
  private void 
  handleFailure
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      throw new PipelineException
	("Illegal response received from the server instance!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The master manager instance.
   */ 
  private MasterMgr  pMasterMgr;   

}

