// $Id: MasterMgrLightClient.java,v 1.7 2009/12/14 03:20:56 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.event.*;

import java.util.*;
import java.util.regex.*;


/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   M G R   L I G H T   C L I E N T                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Read-only access to a limited set the Pipeline master manager operations. 
 */
public
interface MasterMgrLightClient
{  
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
    throws PipelineException;


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
    throws PipelineException;


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
    throws PipelineException;

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
    throws PipelineException;



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
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently active Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public TreeSet<String>
  getActiveToolsetNames() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all Unix toolsets.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public TreeSet<String>
  getToolsetNames() 
    throws PipelineException;

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
    throws PipelineException;

  /**
   * Get the names of all toolsets for all operating systems.
   * 
   * @throws PipelineException
   *   If unable to determine the toolset names.
   */ 
  public TreeMap<String,TreeSet<OsType>>
  getAllToolsetNames() 
    throws PipelineException;


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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;


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
    throws PipelineException;

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
    throws PipelineException;



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
    throws PipelineException;

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
    throws PipelineException;

  /**
   * Get the names and revision numbers of all toolset packages for all operating systems.
   * 
   * @throws PipelineException
   *   If unable to determine the package names.
   */ 
  public DoubleMap<String,OsType,TreeSet<VersionID>>
  getAllToolsetPackageNames()
    throws PipelineException;


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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;



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
    throws PipelineException;

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
    throws PipelineException;



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
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the immediate children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * All hidden nodes (and directories) are always included in the results from this method.
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
    throws PipelineException;

  /** 
   * Update the children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * All hidden nodes (and directories) are always included in the results from this method.
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
    throws PipelineException;

  /** 
   * Update the children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * All hidden nodes (and directories) are always included in the results from this method.
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
   * @param showHidden
   *   Whether hidden nodes (or directories) should be included in the results.
   * 
   * @throws PipelineException
   *   If unable to update the node paths.
   */
  public NodeTreeComp
  updatePaths
  (
   String author, 
   String view, 
   TreeMap<String,Boolean> paths, 
   boolean showHidden
  ) 
    throws PipelineException;


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
    throws PipelineException;



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
  public BaseAnnotation
  getAnnotation
  (
   String nname, 
   String aname
  ) 
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get all per-node the annotations for the given node.<P> 
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
    throws PipelineException;
  
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
    throws PipelineException;
  
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
    throws PipelineException;
  


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
    throws PipelineException;

  /**
   * Get the names of the most downstream nodes in a working area.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @return 
   *   The fully resolved names of the root working versions. 
   * 
   * @throws PipelineException 
   *   If unable to determine which working versions are the roots.
   */ 
  public TreeSet<String> 
  getWorkingRootNames
  (
   String author, 
   String view
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

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
    throws PipelineException;

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
    throws PipelineException;
  
  
  /** 
   * Get the working versions for a set of nodes. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param names 
   *   The fully resolved node names.
   *
   * @return 
   *   The found working versions indexed by fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the working versions.
   */
  public TreeMap<String,NodeMod> 
  getWorkingVersions
  ( 
   String author, 
   String view, 
   TreeSet<String> names
  ) 
    throws PipelineException;



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
    throws PipelineException;

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
    throws PipelineException;

  /** 
   * Get the revision numbers of all checked-in versions for the given set of node names. <P> 
   * 
   * @param names 
   *   The fully resolved node names.
   *
   * @return 
   *   The revision numbers indexed by node name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the checked-in versions.
   */
  public MappedSet<String,VersionID>
  getCheckedInVersionIDs
  ( 
   TreeSet<String> names
  ) 
    throws PipelineException;

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
  public TreeSet<VersionID>
  getIntermediateVersionIDs
  ( 
   String name
  ) 
    throws PipelineException; 

  /** 
   * Get the revision numbers of all checked-in versions of the given set of nodes that do 
   * not save intermediate (temporary) version of files in the repository. <P>
   * 
   * @param names
   *   The fully resolved node names.
   *
   * @return 
   *   The revision numbers indexed by node name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the checked-in versions.
   */
  public MappedSet<String,VersionID>
  getIntermediateVersionIDs
  ( 
   TreeSet<String> names
  ) 
    throws PipelineException; 

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
    throws PipelineException;

  /** 
   * Get the checked-in versions for each of the given nodes. <P> 
   * 
   * @param vids 
   *   The set of revision numbers to lookup indexed by the fully resolved node names.
   * 
   * @return 
   *   The checked-in versions indexed by node name and revision number.
   * 
   * @throws PipelineException
   *   If unable to retrieve the checked-in versions.
   */
  public DoubleMap<String,VersionID,NodeVersion> 
  getCheckedInVersions
  ( 
   MappedSet<String,VersionID> vids
  ) 
    throws PipelineException; 

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
    throws PipelineException;

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
    throws PipelineException;

  /** 
   * Get the log messages associated with all checked-in versions for the given set of 
   * nodes. 
   * 
   * @param names 
   *   The fully resolved node names.
   *
   * @return 
   *   The log messages indexed by node name and revision number.
   * 
   * @throws PipelineException
   *   If unable to retrieve the log messages.
   */
  public DoubleMap<String,VersionID,LogMessage> 
  getHistory
  ( 
   TreeSet<String> names
  ) 
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;



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
  public MappedLinkedList<Long,BaseNodeEvent>
  getNodeEvents
  (
   TreeSet<String> names, 
   TreeSet<String> users, 
   TimeInterval interval
  ) 
    throws PipelineException;


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
    throws PipelineException;



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
  public Path
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
    throws PipelineException;

}

