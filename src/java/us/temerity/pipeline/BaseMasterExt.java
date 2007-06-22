// $Id: BaseMasterExt.java,v 1.5 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline;

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
  /*   W O R K I N G   A R E A   O P S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new empty working area. <P>
   */  
  public final boolean
  hasPreCreateWorkingAreaTest() 
  {
    return (getPreCreateWorkingAreaTestReqs() != null); 
  }

  /**
   * Get the operation requirements to test before creating a new empty working area. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreCreateWorkingAreaTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostCreateWorkingAreaTask() 
  {
    return (getPostCreateWorkingAreaTaskReqs() != null); 
  }
  	 
  /**
   * Get the operation requirements to run a task after creating a new empty working area.<P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostCreateWorkingAreaTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRemoveWorkingAreaTest() 
  {
    return (getPreRemoveWorkingAreaTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before removing an entire working area. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRemoveWorkingAreaTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRemoveWorkingAreaTask() 
  {
    return (getPostRemoveWorkingAreaTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after removing an entire working area. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRemoveWorkingAreaTaskReqs() 
  {
    return null;
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
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before modifying the properties of a working version of a node. <P>
   */  
  public final boolean
  hasPreModifyPropertiesTest() 
  {
    return (getPreModifyPropertiesTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before modifying the properties of a working 
   * version of a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreModifyPropertiesTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostModifyPropertiesTask() 
  {
    return (getPostModifyPropertiesTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after modifying the properties of a 
   * working version of a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostModifyPropertiesTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreLinkTest() 
  {
    return (getPreLinkTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before creating or modifying an existing 
   * link between two node working versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreLinkTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostLinkTask() 
  {
    return (getPostLinkTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after creating or modifying an existing 
   * link between two node working versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostLinkTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreUnlinkTest() 
  {
    return (getPreUnlinkTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before destroying an existing link between 
   * the working versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreUnlinkTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostUnlinkTask() 
  {
    return (getPostUnlinkTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after destroying an existing link 
   * between the working versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostUnlinkTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreAddSecondaryTest() 
  {
    return (getPreAddSecondaryTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before adding a secondary file sequence to 
   * a working version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreAddSecondaryTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostAddSecondaryTask() 
  {
    return (getPostAddSecondaryTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after adding a secondary file sequence 
   * to a working version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostAddSecondaryTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRemoveSecondaryTest() 
  {
    return (getPreRemoveSecondaryTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before removing a secondary file sequence to 
   * a working version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRemoveSecondaryTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRemoveSecondaryTask() 
  {
    return (getPostRemoveSecondaryTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after removing a secondary file sequence 
   * to a working version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRemoveSecondaryTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRenameTest() 
  {
    return (getPreRenameTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before renaming a working version of a node 
   * which has never been checked-in. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRenameTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRenameTask() 
  {
    return (getPostRenameTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after renaming a working version of a 
   * node which has never been checked-in. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRenameTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRenumberTest() 
  {
    return (getPreRenumberTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before renumbering the frame ranges of the 
   * file sequences associated with a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRenumberTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRenumberTask() 
  {
    return (getPostRenumberTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a task after renumbering the frame ranges of the 
   * file sequences associated with a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRenumberTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRegisterTest() 
  {
    return (getPreRegisterTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before registering an initial working version 
   * of a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRegisterTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRegisterTask() 
  {
    return (getPostRegisterTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after registering an initial working 
   * version of a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRegisterTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreReleaseTest() 
  {
    return (getPreReleaseTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before releasing the working versions of nodes 
   * and optionally remove the associated working area files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreReleaseTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostReleaseTask() 
  {
    return (getPostReleaseTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after releasing the working versions 
   * of nodes and optionally remove the associated working area files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostReleaseTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreDeleteTest() 
  {
    return (getPreDeleteTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before deleting all working and checked-in 
   * versions of a node and optionally remove all associated working area files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreDeleteTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostDeleteTask() 
  {
    return (getPostDeleteTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after deleting all working and checked-in 
   * versions of a node and optionally remove all associated working area files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostDeleteTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreCheckInTest() 
  {
    return (getPreCheckInTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before checking-in an individual node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreCheckInTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostCheckInTask() 
  {
    return (getPostCheckInTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after checking-in an individual node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostCheckInTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreCheckOutTest() 
  {
    return (getPreCheckOutTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before checking-out an individual node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreCheckOutTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostCheckOutTask() 
  {
    return (getPostCheckOutTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after checking-out an individual node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostCheckOutTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreLockTest() 
  {
    return (getPreLockTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before locking the working version of a node 
   * to a specific checked-in version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreLockTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostLockTask() 
  {
    return (getPostLockTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after locking the working version of 
   * a node to a specific checked-in version. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostLockTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreEvolveTest() 
  {
    return (getPreEvolveTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before changing the checked-in version upon 
   * which the working version is based without modifying the working version properties, 
   * links or associated files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreEvolveTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostEvolveTask() 
  {
    return (getPostEvolveTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after changing the checked-in version 
   * upon which the working version is based without modifying the working version 
   * properties, links or associated files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostEvolveTaskReqs() 
  {
    return null;
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
   * Whether to test before reverting specific working area files to an earlier 
   * checked-in version of the files.
   */  
  public final boolean
  hasPreRevertFilesTest() 
  {
    return (getPreRevertFilesTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before reverting specific working area files 
   * to an earlier checked-in version of the files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRevertFilesTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRevertFilesTask() 
  {
    return (getPostRevertFilesTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after reverting specific working area 
   * files to an earlier checked-in version of the files. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRevertFilesTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreCloneFilesTest() 
  {
    return (getPreCloneFilesTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before replacing the primary files associated 
   * with one node with the primary files of another node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreCloneFilesTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostCloneFilesTask() 
  {
    return (getPostCloneFilesTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after replacing the primary files 
   * associated with one node with the primary files of another node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostCloneFilesTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRemoveFilesTest() 
  {
    return (getPreRemoveFilesTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before removing the working area files 
   * associated with a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRemoveFilesTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRemoveFilesTask() 
  {
    return (getPostRemoveFilesTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after removing the working area files 
   * associated with a node. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRemoveFilesTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreArchiveTest() 
  {
    return (getPreArchiveTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before archiving the files associated with 
   * the given checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreArchiveTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostArchiveTask() 
  {
    return (getPostArchiveTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after archiving the files associated 
   * with the given checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostArchiveTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreOfflineTest() 
  {
    return (getPreOfflineTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before removing the repository files associated 
   * with the given checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreOfflineTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostOfflineTask() 
  {
    return (getPostOfflineTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after removing the repository files 
   * associated with the given checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostOfflineTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRequestRestoreTest() 
  {
    return (getPreRequestRestoreTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before requesting to restore the given set of 
   * checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRequestRestoreTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRequestRestoreTask() 
  {
    return (getPostRequestRestoreTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after requesting to restore the given set 
   * of checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRequestRestoreTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreDenyRestoreTest() 
  {
    return (getPreDenyRestoreTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before denying the request to restore the given 
   * set of checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreDenyRestoreTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostDenyRestoreTask() 
  {
    return (getPostDenyRestoreTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after denying the request to restore 
   * the given set of checked-in versions. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostDenyRestoreTaskReqs() 
  {
    return null;
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
  public final boolean
  hasPreRestoreTest() 
  {
    return (getPreRestoreTestReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to test before restoring the given checked-in versions 
   * from the given archive volume. <P>
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable this test.
   */  
  public ExtReqs
  getPreRestoreTestReqs() 
  {
    return null;
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
  public final boolean
  hasPostRestoreTask() 
  {
    return (getPostRestoreTaskReqs() != null); 	       
  }
 	 
  /**
   * Get the operation requirements to run a tesk after restoring the given checked-in 
   * versions from the given archive volume. <P>  
   * 
   * Subclasses should override this to return a non-<CODE>null</CODE> set of requirements
   * in order to enable perform this task. 
   */  
  public ExtReqs
  getPostRestoreTaskReqs() 
  {
    return null;
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



