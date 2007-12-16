// $Id: BaseMasterExt.java,v 1.9 2007/12/16 06:33:59 jesse Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.builder.*;
import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M A S T E R   E X T E N S I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Master Manager extension plugins. <P>
 * 
 * This class provides methods to be overloaded by subclasses in order to provide additional
 * functionality for the Master Manager daemon.  <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public 
class BaseMasterExt
  extends BaseExt
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public
  BaseMasterExt() 
  {
    super();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the extension.
   * 
   * @param vid
   *   The extension plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the extension.
   */ 
  protected
  BaseMasterExt
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseMasterExt
  (
   BaseMasterExt extension
  ) 
  {
    super(extension); 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  public final PluginType
  getPluginType()
  {
    return PluginType.MasterExt; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*  P L U G I N   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually. 
   */  
  public boolean
  hasPostEnableTask()
  {
    return false;
  }

  /**
   * The task to perform after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually.
   */ 
  public void 
  postEnableTask()
  {}
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */  
  public boolean
  hasPreDisableTask()
  {
    return false;
  }

  /**
   * The task to perform after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  public void 
  preDisableTask()
  {}
 


  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to test before setting the work groups used to determine the scope of 
   * administrative privileges. <P>
   */  
  public boolean
  hasPreSetWorkGroupsTest() 
  {
    return false;
  }

  /**
   * Test to perform before setting the work groups used to determine the scope of 
   * administrative privileges. <P>
   * 
   * @param groups 
   *   The work groups.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preSetWorkGroupsTest
  (
   WorkGroups groups   
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after setting the work groups used to determine the scope of 
   * administrative privileges. <P>
   */  
  public boolean
  hasPostSetWorkGroupsTask() 
  {
    return false;
  }

  /**
   * The task to perform after setting the work groups used to determine the scope of 
   * administrative privileges. <P>
   * 
   * @param groups 
   *   The work groups.
   */  
  public void
  postSetWorkGroupsTask
  (
   WorkGroups groups  
  ) 
  {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   A R E A   O P S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new empty working area. <P>
   */  
  public boolean
  hasPreCreateWorkingAreaTest() 
  {
    return false;
  }

  /**
   * Test to perform before creating a new empty working area. <P>
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCreateWorkingAreaTest
  (
   String author, 
   String view   
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after creating a new empty working area. <P>
   */  
  public boolean
  hasPostCreateWorkingAreaTask() 
  {
    return false;
  }

  /**
   * The task to perform after creating a new empty working area. <P>
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */  
  public void
  postCreateWorkingAreaTask
  (
   String author, 
   String view   
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing an entire working area. <P>
   */  
  public boolean
  hasPreRemoveWorkingAreaTest() 
  {
    return false;
  }

  /**
   * Test to perform before removing an entire working area. <P>
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRemoveWorkingAreaTest
  (
   String author, 
   String view   
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after removing an entire working area. <P>
   */  
  public boolean
  hasPostRemoveWorkingAreaTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after removing an entire working area. <P>
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */  
  public void
  postRemoveWorkingAreaTask
  (
   String author, 
   String view   
  ) 
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before adding an annotation to a node. <P>
   */  
  public boolean
  hasPreAddAnnotationTest() 
  {
    return false;
  }

  /**
   * Test to perform before adding an annotation to a node. <P>
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
   *   To abort the operation.
   */  
  public void
  preAddAnnotationTest
  (
   String nname, 
   String aname, 
   BaseAnnotation annot 
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after adding an annotation to a node. <P>
   */  
  public boolean
  hasPostAddAnnotationTask() 
  {
    return false;
  }

  /**
   * The task to perform after adding an annotation to a node. <P>
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   * 
   * @param annot 
   *   The new node annotation added.
   */  
  public void
  postAddAnnotationTask
  (
   String nname, 
   String aname, 
   BaseAnnotation annot 
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing an annotation from a node. <P>
   */  
  public boolean
  hasPreRemoveAnnotationTest() 
  {
    return false;
  }

  /**
   * Test to perform before removing an annotation from a node. <P>
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   *
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRemoveAnnotationTest
  (
   String nname, 
   String aname
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after removing an annotation from a node. <P>
   */  
  public boolean
  hasPostRemoveAnnotationTask() 
  {
    return false;
  }

  /**
   * The task to perform after removing an annotation from a node. <P>
   * 
   * @param nname 
   *   The fully resolved node name.
   *
   * @param aname 
   *   The name of the annotation. 
   */  
  public void
  postRemoveAnnotationTask
  (
   String nname, 
   String aname
  ) 
  {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before modifying the properties of a working version of a node. <P>
   */  
  public boolean
  hasPreModifyPropertiesTest() 
  {
    return false;
  }

  /**
   * Test to perform before modifying the properties of a working version of a node. <P>
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version containing the node property information to copy.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preModifyPropertiesTest
  (
   NodeID nodeID, 
   NodeMod mod   
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after modifying the properties of a working version of a node. <P>
   */  
  public boolean
  hasPostModifyPropertiesTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after modifying the properties of a working version of a node. <P>
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version containing the node property information to copy.
   */  
  public void
  postModifyPropertiesTask
  (
   NodeID nodeID, 
   NodeMod mod   
  ) 
  {}

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating or modifying an existing link between two 
   * node working versions. <P>
   */  
  public boolean
  hasPreLinkTest() 
  {
    return false;
  }

  /**
   * Test to perform before creating or modifying an existing link between two 
   * node working versions. <P>
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
   *   To abort the operation.
   */  
  public void
  preLinkTest
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
  {}


  /**
   * Whether to run a task after creating or modifying an existing link between two 
   * node working versions. <P>
   */  
  public boolean
  hasPostLinkTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after creating or modifying an existing link between two 
   * node working versions. <P>
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
   */  
  public void
  postLinkTask
  (
   String author, 
   String view, 
   String target, 
   String source,
   LinkPolicy policy,
   LinkRelationship relationship,  
   Integer offset
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before destroying an existing link between the working versions.
   */  
  public boolean
  hasPreUnlinkTest() 
  {
    return false;
  }

  /**
   * Test to perform before destroying an existing link between the working versions. 
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
   *   To abort the operation.
   */  
  public void
  preUnlinkTest
  (
   String author, 
   String view, 
   String target, 
   String source
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after destroying an existing link between the working versions.
   */  
  public boolean
  hasPostUnlinkTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after destroying an existing link between the working versions. 
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
   */  
  public void
  postUnlinkTask
  (
   String author, 
   String view, 
   String target, 
   String source
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before adding a secondary file sequence to a working version.
   */  
  public boolean
  hasPreAddSecondaryTest() 
  {
    return false;
  }

  /**
   * Test to perform before adding a secondary file sequence to a working version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to add.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preAddSecondaryTest
  (
   NodeID nodeID,
   FileSeq fseq
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after adding a secondary file sequence to a working version.
   */  
  public boolean
  hasPostAddSecondaryTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after adding a secondary file sequence to a working version. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to add.
   */  
  public void
  postAddSecondaryTask
  (
   NodeID nodeID,
   FileSeq fseq
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing a secondary file sequence to a working version.
   */  
  public boolean
  hasPreRemoveSecondaryTest() 
  {
    return false;
  }

  /**
   * Test to perform before removing a secondary file sequence to a working version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRemoveSecondaryTest
  (
   NodeID nodeID,
   FileSeq fseq
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after removing a secondary file sequence to a working version.
   */  
  public boolean
  hasPostRemoveSecondaryTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after removing a secondary file sequence to a working version. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   */  
  public void
  postRemoveSecondaryTask
  (
   NodeID nodeID,
   FileSeq fseq
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before renaming a working version of a node which has never 
   * been checked-in.
   */  
  public boolean
  hasPreRenameTest() 
  {
    return false;
  }

  /**
   * Test to perform before renaming a working version of a node which has never 
   * been checked-in.
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
   *   To abort the operation.
   */  
  public void
  preRenameTest
  (
   NodeID nodeID,
   FilePattern pattern, 
   boolean renameFiles
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after renaming a working version of a node which has never 
   * been checked-in.
   */  
  public boolean
  hasPostRenameTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after renaming a working version of a node which has never 
   * been checked-in. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   * 
   * @param renameFiles 
   *   Should the files associated with the working version be renamed?
   */  
  public void
  postRenameTask
  (
   NodeID nodeID,
   FilePattern pattern, 
   boolean renameFiles
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before renumbering the frame ranges of the file sequences 
   * associated with a node.
   */  
  public boolean
  hasPreRenumberTest() 
  {
    return false;
  }

  /**
   * Test to perform before renumbering the frame ranges of the file sequences 
   * associated with a node.
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
   *   To abort the operation.
   */  
  public void
  preRenumberTest
  (
   NodeID nodeID,
   FrameRange range, 
   boolean removeFiles
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a task after renumbering the frame ranges of the file sequences 
   * associated with a node.
   */  
  public boolean
  hasPostRenumberTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after renumbering the frame ranges of the file sequences 
   * associated with a node.
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
   */  
  public void
  postRenumberTask
  (
   NodeID nodeID,
   FrameRange range, 
   boolean removeFiles
  ) 
  {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L   O P S                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to test before registering an initial working version of a node. 
   */  
  public boolean
  hasPreRegisterTest() 
  {
    return false;
  }

  /**
   * Test to perform before registering an initial working version of a node. 
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The initial working version to register.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRegisterTest
  (
   NodeID nodeID, 
   NodeMod mod 
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after registering an initial working version of a node. 
   */  
  public boolean
  hasPostRegisterTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after registering an initial working version of a node. 
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The initial working version registered.
   */  
  public void
  postRegisterTask
  (
   NodeID nodeID, 
   NodeMod mod
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before releasing the working versions of nodes and optionally 
   * remove the associated working area files. 
   */  
  public boolean
  hasPreReleaseTest() 
  {
    return false;
  }

  /**
   * Test to perform before releasing the working versions of nodes and optionally 
   * removing the associated working area files. 
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
   *   To abort the operation.
   */  
  public void
  preReleaseTest
  (
   String author, 
   String view, 
   TreeSet<String> names, 
   boolean removeFiles
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after releasing the working versions of nodes and optionally 
   * remove the associated working area files. 
   */  
  public boolean
  hasPostReleaseTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after releasing the working versions of nodes and optionally 
   * removing the associated working area files. 
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
   *   Where the files associated with the working version deleted?
   */  
  public void
  postReleaseTask
  (
   String author, 
   String view, 
   TreeSet<String> names, 
   boolean removeFiles
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   */  
  public boolean
  hasPreDeleteTest() 
  {
    return false;
  }

  /**
   * Test to perform before deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param removeFiles 
   *   Should the files associated with the working versions be deleted?
   *
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preDeleteTest
  (
   String name, 
   boolean removeFiles
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   */  
  public boolean
  hasPostDeleteTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param removeFiles 
   *   Should the files associated with the working versions be deleted?
   */  
  public void
  postDeleteTask
  (
   String name, 
   boolean removeFiles
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
  public boolean
  hasPreCheckInTest() 
  {
    return false;
  }

  /**
   * Test to perform before checking-in an individual node.
   * 
   * @param rname
   *   The fully resolved node name of the root node of the check-in operation.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCheckInTest
  (
   String rname, 
   NodeID nodeID, 
   NodeMod mod,
   VersionID.Level level, 
   String msg
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after checking-in an individual node.
   */  
  public boolean
  hasPostCheckInTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after checking-in an individual node.
   * 
   * @param vsn
   *   The newly created checked-in node version. 
   */  
  public void
  postCheckInTask
  (
   NodeVersion vsn
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-out an individual node.
   */  
  public boolean
  hasPreCheckOutTest() 
  {
    return false;
  }

  /**
   * Test to perform before checking-out an individual node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vsn
   *   The checked-in node version to check-out.
   * 
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   * 
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCheckOutTest
  (
   NodeID nodeID,
   NodeVersion vsn, 
   CheckOutMode mode,
   CheckOutMethod method
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after checking-out an individual node.
   */  
  public boolean
  hasPostCheckOutTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after checking-out an individual node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param mod
   *   The newly checked-out working version of the node.
   */  
  public void
  postCheckOutTask
  (
   NodeID nodeID,
   NodeMod mod
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before locking the working version of a node to a specific 
   * checked-in version.
   */  
  public boolean
  hasPreLockTest() 
  {
    return false;
  }

  /**
   * Test to perform before locking the working version of a node to a specific 
   * checked-in version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version is 
   *   being locked.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preLockTest
  (
   NodeID nodeID,
   VersionID vid
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after locking the working version of a node to a specific 
   * checked-in version.
   */  
  public boolean
  hasPostLockTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after locking the working version of a node to a specific 
   * checked-in version.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid 
   *   The revision number of the checked-in version to which the working version 
   *   has been locked.
   */  
  public void
  postLockTask
  (
   NodeID nodeID,
   VersionID vid
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before changing the checked-in version upon which the working version 
   * is based without modifying the working version properties, links or associated files.
   */  
  public boolean
  hasPreEvolveTest() 
  {
    return false;
  }

  /**
   * Test to perform before changing the checked-in version upon which the working version 
   * is based without modifying the working version properties, links or associated files.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preEvolveTest
  (
   NodeID nodeID,
   VersionID vid
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after changing the checked-in version upon which the working 
   * version is based without modifying the working version properties, links or associated 
   * files.
   */  
  public boolean
  hasPostEvolveTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after changing the checked-in version upon which the working 
   * version is based without modifying the working version properties, links or associated 
   * files.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */  
  public void
  postEvolveTask
  (
   NodeID nodeID,
   VersionID vid
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new node bundle by packing up a tree of working 
   * area nodes rooted at the given node.
   */  
  public boolean
  hasPrePackTest() 
  {
    return false;
  }

  /**
   * Test to perform before creating a new node bundle by packing up a tree of working 
   * area nodes rooted at the given node.
   * 
   * @param nodeID 
   *   The unique working version identifier of the root node.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  prePackTest
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after creating a new node bundle by packing up a tree of 
   * working area nodes rooted at the given node.
   */  
  public boolean
  hasPostPackTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after creating a new node bundle by packing up a tree of 
   * working area nodes rooted at the given node.
   * 
   * @param bundle
   *   The node bundle metadata. 
   */  
  public void
  postPackTask
  (
   NodeBundle bundle
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
   */  
  public boolean
  hasPreUnpackTest() 
  {
    return false;
  }

  /**
   * Test to perform before unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
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
   * @param toolsetRemap
   *   A table mapping the names of toolsets associated with the nodes in the bundle
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
   *   To abort the operation.
   */  
  public void
  preUnpackTest
  (
   Path bundlePath, 
   String author, 
   String view, 
   boolean releaseOnError, 
   ActionOnExistence actOnExist,   
   TreeMap<String,String> toolsetRemap,
   TreeMap<String,String> selectionKeyRemap,
   TreeMap<String,String> licenseKeyRemap,
   TreeMap<String,String> hardwareKeyRemap
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
   */  
  public boolean
  hasPostUnpackTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
   * 
   * @param bundlePath
   *   The abstract file system path to the node node bundle.
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
   * @param bundle
   *   The node bundle metadata. 
   */  
  public void
  postUnpackTask
  (
   Path bundlePath, 
   String author, 
   String view,    
   boolean releaseOnError, 
   ActionOnExistence actOnExist,
   TreeMap<String,String> toolsetRemap,
   TreeMap<String,String> selectionKeyRemap,
   TreeMap<String,String> licenseKeyRemap,
   TreeMap<String,String> hardwareKeyRemap,
   NodeBundle bundle
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before reverting specific working area files to an earlier 
   * checked-in version of the files.
   */  
  public boolean
  hasPreRevertFilesTest() 
  {
    return false;
  }

  /**
   * Test to perform before reverting specific working area files to an earlier 
   * checked-in version of the files.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRevertFilesTest
  (
   NodeID nodeID,
   TreeMap<String,VersionID> files
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after reverting specific working area files to an earlier 
   * checked-in version of the files.
   */  
  public boolean
  hasPostRevertFilesTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after reverting specific working area files to an earlier 
   * checked-in version of the files.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   */  
  public void
  postRevertFilesTask
  (
   NodeID nodeID,
   TreeMap<String,VersionID> files
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before replacing the primary files associated with one node with 
   * the primary files of another node.
   */  
  public boolean
  hasPreCloneFilesTest() 
  {
    return false;
  }

  /**
   * Test to perform before replacing the primary files associated with one node with 
   * the primary files of another node.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCloneFilesTest
  (
   NodeID sourceID, 
   NodeID targetID
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after replacing the primary files associated with one node with 
   * the primary files of another node.
   */  
  public boolean
  hasPostCloneFilesTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after replacing the primary files associated with one node with 
   * the primary files of another node.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   */  
  public void
  postCloneFilesTask
  (
   NodeID sourceID, 
   NodeID targetID
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing the working area files associated with a node.
   */  
  public boolean
  hasPreRemoveFilesTest() 
  {
    return false;
  }

  /**
   * Test to perform before removing the working area files associated with a node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param indices
   *   The file sequence indices of the files to remove or <CODE>null</CODE> to 
   *   remove all files.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRemoveFilesTest
  (
   NodeID nodeID,
   TreeSet<Integer> indices
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after removing the working area files associated with a node.
   */  
  public boolean
  hasPostRemoveFilesTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after removing the working area files associated with a node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param indices
   *   The file sequence indices of the files to remove or <CODE>null</CODE> to 
   *   remove all files.
   */  
  public void
  postRemoveFilesTask
  (
   NodeID nodeID,
   TreeSet<Integer> indices
  ) 
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N   O P S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a database backup file.
   */  
  public boolean
  hasPreBackupDatabaseTest() 
  {
    return false;
  }

  /**
   * Test to perform before creating a database backup file.
   * 
   * @param file
   *   The name of the backup file.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preBackupDatabaseTest
  (
   File file
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after creating a database backup file.
   */  
  public boolean
  hasPostBackupDatabaseTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after creating a database backup file.
   * 
   * @param file
   *   The name of the backup file.
   */  
  public void
  postBackupDatabaseTask
  (
   File file
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before archiving the files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPreArchiveTest() 
  {
    return false;
  }

  /**
   * Test to perform before archiving the files associated with the given 
   * checked-in versions.
   * 
   * @param name 
   *   The name of the archive volume. 
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preArchiveTest
  (
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after archiving the files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPostArchiveTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after archiving the files associated with the given 
   * checked-in versions.
   * 
   * @param name 
   *   The name of the archive volume. 
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed.
   */  
  public void
  postArchiveTask
  (
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing the repository files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPreOfflineTest() 
  {
    return false;
  }

  /**
   * Test to perform before removing the repository files associated with the given 
   * checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preOfflineTest
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after removing the repository files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPostOfflineTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after removing the repository files associated with the given 
   * checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   */  
  public void
  postOfflineTask
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before requesting to restore the given set of checked-in versions.
   */  
  public boolean
  hasPreRequestRestoreTest() 
  {
    return false;
  }

  /**
   * Test to perform before requesting to restore the given set of checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRequestRestoreTest
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after requesting to restore the given set of checked-in versions.
   */  
  public boolean
  hasPostRequestRestoreTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after requesting to restore the given set of checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */  
  public void
  postRequestRestoreTask
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before denying the request to restore the given set of 
   * checked-in versions.
   */  
  public boolean
  hasPreDenyRestoreTest() 
  {
    return false;
  }

  /**
   * Test to perform before denying the request to restore the given set of 
   * checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preDenyRestoreTest
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after denying the request to restore the given set of 
   * checked-in versions.
   */  
  public boolean
  hasPostDenyRestoreTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after denying the request to restore the given set of 
   * checked-in versions.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */  
  public void
  postDenyRestoreTask
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before restoring the given checked-in versions from the given 
   * archive volume.
   */  
  public boolean
  hasPreRestoreTest() 
  {
    return false;
  }

  /**
   * Test to perform before restoring the given checked-in versions from the given 
   * archive volume.
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
   *   The name of the toolset environment under which the archiver is executed.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preRestoreTest
  (
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
    throws PipelineException
  {}


  /**
   * Whether to run a tesk after restoring the given checked-in versions from the given 
   * archive volume.
   */  
  public boolean
  hasPostRestoreTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after restoring the given checked-in versions from the given 
   * archive volume.
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
   *   The name of the toolset environment under which the archiver is executed.
   */  
  public void
  postRestoreTask
  (
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a tesk after running the node garbage collector.
   */  
  public boolean
  hasPostNodeGarbageCollectTask() 
  {
    return false;
  }
  
  /**
   * The task to perform after running the node garbage collector.
   * 
   * @param cached
   *   The total number of node versions currently cached.
   * 
   * @param freed
   *   The number of node versions freed during this collection cycle.
   */  
  public void
  postNodeGarbageCollectTask
  (
   long cached, 
   long freed
  ) 
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public final boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseMasterExt)) {
      BaseMasterExt extension = (BaseMasterExt) obj;
      if(super.equals(obj) && 
	 equalParams(extension)) 
	return true;
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public final Object 
  clone()
  {
    return (BaseMasterExt) super.clone();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7183765011892413354L;


}



