// $Id: LogMasterActivityExt.java,v 1.11 2010/01/06 23:34:09 jim Exp $

package us.temerity.pipeline.plugin.LogMasterActivityExt.v2_1_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.event.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   M A S T E R   A C T I V I T Y   E X T                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A simple test extension which prints a message for each extendable master operation.
 */
public class 
LogMasterActivityExt
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  LogMasterActivityExt()
  {
    super("LogMasterActivity", new VersionID("2.1.1"), "Temerity",
	  "A test extension which prints a message for each extendable master operation.");

    /* plugin ops */ 
    {
      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aEnableDelay, 
	   "The length of time in milliseconds to delay enable.", 
	   10000);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aDisableDelay, 
	   "The length of time in milliseconds to delay disable.", 
	   10000);
	addParam(param);
      }
    }

    /* administrative privileges ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowSetWorkGroups,
	   "Whether to allow setting the work groups.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogSetWorkGroups,
	   "Enable logging of setting work groups.",
	   true);
	addParam(param);
      }
    }

    /* toolset ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCreatePackage, 
	   "Whether to allow the creation of new toolset packages.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCreatePackage,
	   "Enable logging of new toolset package creation.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCreateToolset, 
	   "Whether to allow the creation of new toolsets.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCreateToolset,
	   "Enable logging of new toolset creation.", 
	   true);
	addParam(param);
      }
    }

    /* working area ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCreateWorkingArea,
	   "Whether to allow the creation of new working areas.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCreateWorkingArea,
	   "Enable logging of new working area.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRemoveWorkingArea,
	   "Whether to allow the removal of existing working areas.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRemoveWorkingArea,
	   "Enable logging of the removal of working areas.",
	   true);
	addParam(param);
      }
    }

    /* annotation ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowAddAnnotation,
	   "Whether to allow the addition of node annotations.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogAddAnnotation,
	   "Enable logging the addition of node annotations.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRemoveAnnotation,
	   "Whether to allow removal node annotations.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRemoveAnnotation,
	   "Enable logging the removal of node annotations.", 
	   true);
	addParam(param);
      }
    }

    /* working version ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowModifyProperties, 
	   "Whether to allow the modification of working node properties.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogModifyProperties, 
	   "Enable logging of the modification of working node properties.", 
	   true);
	addParam(param);
      }
      

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowLink, 
	   "Whether to allow creating or modifying an existing link between two " +
	   "node working versions.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogLink, 
	   "Enable logging of the creation or modification of links between two " +
	   "node working versions.", 
	   true);
	addParam(param);
      }
      

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowUnlink, 
	   "Whether to allow the destruction of an existing link between the working " +
	   "node versions.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogUnlink, 
	   "Enable logging of the destruction of an existing link between the working " +
	   "node versions.", 
	   true);
	addParam(param);
      }
      

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowAddSecondary, 
	   "Whether to allow the addition of a secondary file sequence to a " + 
	   "working version.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogAddSecondary, 
	   "Enable logging of the addition of a secondary file sequence to a " + 
	   "working version.", 
	   true);
	addParam(param);
      }
      

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRemoveSecondary, 
	   "Whether to allow the removal of a secondary file sequence to a working version.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRemoveSecondary, 
	   "Enable logging of the removal of a secondary file sequence to a " + 
	   "working version.", 
	   true);
	addParam(param);
      }
      
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRename, 
	   "Whether to allow the renaming of a working version of a node which has never " + 
	   "been checked-in.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRename, 
	   "Enable logging of renaming of a working version of a node which has never " + 
	   "been checked-in.", 
	   true);
	addParam(param);
      }
      

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRenumber, 
	   "Whether to allow the renumbering the frame ranges of the file sequences " + 
	   "associated with a node.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRenumber, 
	   "Enable logging of renumbering the frame ranges of the file sequences " + 
	   "associated with a node.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowEditing,
	   "Whether to allow editing of nodes.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogEditingStarted, 
	   "Enable logging of when Editor plugins are started.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogEditingFinished, 
	   "Enable logging of when Editor plugins finish.", 
	   true);
	addParam(param);
      }
    }

    /* revision control ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRegister,
	   "Whether to allow registration of new nodes.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRegister,
	   "Enable logging of the node registration.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRelease,
	   "Whether to allow releasing working versions of nodes.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRelease,
	   "Enable logging of releasing nodes.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowDelete,
	   "Whether to allow the deletion of existing node.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogDelete,
	   "Enable logging of node deleltion.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCheckIn,
	   "Whether to allow the check-in of nodes.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCheckIn,
	   "Enable logging of node check-in.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCheckOut,
	   "Whether to allow the check-out of nodes.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCheckOut,
	   "Enable logging of node check-out.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowLock,
	   "Whether to allow locking of nodes.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogLock,
	   "Enable logging of node locking.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowEvolve,
	   "Whether to allow node evolution.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogEvolve,
	   "Enable logging of node evolution.", 
	   true);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRevertFiles,
	   "Whether to allow reverting node files to eariler versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRevertFiles,
	   "Enable logging of node file reversion.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowCloneFiles,
	   "Whether to allow copying files from one node version to another.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogCloneFiles,
	   "Enable logging of node file cloning.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRemoveFiles,
	   "Whether to allow the deletion of working are node files.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRemoveFiles,
	   "Enable logging of the deletion of working are node files.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowPackBundle,
	   "Whether to allow creation of new node bundles.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogPackBundle,
	   "Enable logging of creating new node bundles.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowUnpackBundle,
	   "Whether to allow unpacking of node bundles.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogUnpackBundle,
	   "Enable logging of the unpacking of node bundles.",
	   true);
	addParam(param);
      }

    }

    /* administration ops */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowBackupDatabase,
	   "Whether to allow database backups.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogBackupDatabase,
	   "Enable logging of datbase backups.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowArchive,
	   "Whether to allow the archiving of checked-in versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogArchive,
	   "Enable logging of node archiving.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowOffline,
	   "Whether to allow the removal of previously archived checked-in versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogOffline,
	   "Enable logging of node offlining.", 
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRequestRestore,
	   "Whether to allow requests to restore offline node versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRequestRestore,
	   "Enable logging of requests to restore offline node versions.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowDenyRestore,
	   "Whether to allow the denial of requests to restore offline node versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogDenyRestore,
	   "Enable logging of denials of requests to restore offline node versions.",
	   true);
	addParam(param);
      }


      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAllowRestore,
	   "Whether to allow the restoration of offlined node versions.",
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aLogRestore,
	   "Enable logging of the restoration of offlined node versions.",
	   true);
	addParam(param);
      }
    }


    {  
      LayoutGroup layout = new LayoutGroup(true); 

      {
	LayoutGroup sub = new LayoutGroup
	  ("PluginOps", "Plugin initialization and shutdown operations.", true); 
	sub.addEntry(aEnableDelay);
	sub.addEntry(aDisableDelay);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("AdminPrivilegesOps", "Administrative privileges operations.", true);
	sub.addEntry(aAllowSetWorkGroups);
	sub.addEntry(aLogSetWorkGroups); 

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("ToolsetOps", "Toolset related operations.", true);
	sub.addEntry(aAllowCreatePackage);
	sub.addEntry(aLogCreatePackage);
	sub.addSeparator(); 
	sub.addEntry(aAllowCreateToolset);
	sub.addEntry(aLogCreateToolset);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("WorkingAreaOps", "Working area management operations.", true);
	sub.addEntry(aAllowCreateWorkingArea);
	sub.addEntry(aLogCreateWorkingArea);
	sub.addSeparator();
	sub.addEntry(aAllowRemoveWorkingArea);
	sub.addEntry(aLogRemoveWorkingArea);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("AnnotationOps", "Node annotation operations.", true);
	sub.addEntry(aAllowAddAnnotation); 
	sub.addEntry(aLogAddAnnotation);
	sub.addSeparator();
	sub.addEntry(aAllowRemoveAnnotation);
	sub.addEntry(aLogRemoveAnnotation);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("WorkingVersionOps", "Operations which modify working versions of nodes.", true);
	sub.addEntry(aAllowModifyProperties);
	sub.addEntry(aLogModifyProperties);
	sub.addSeparator();
	sub.addEntry(aAllowLink);
	sub.addEntry(aLogLink);
	sub.addSeparator();
	sub.addEntry(aAllowUnlink);
	sub.addEntry(aLogUnlink);
	sub.addSeparator();
	sub.addEntry(aAllowAddSecondary);
	sub.addEntry(aLogAddSecondary);
	sub.addSeparator();
	sub.addEntry(aAllowRemoveSecondary);
	sub.addEntry(aLogRemoveSecondary);
	sub.addSeparator();
	sub.addEntry(aAllowRename);
	sub.addEntry(aLogRename);
	sub.addSeparator();
	sub.addEntry(aAllowRenumber);
	sub.addEntry(aLogRenumber);
	sub.addSeparator();
        sub.addEntry(aAllowEditing); 
	sub.addEntry(aLogEditingStarted);
	sub.addEntry(aLogEditingFinished);

	layout.addSubGroup(sub);
      }

      {
	LayoutGroup sub = new LayoutGroup
	  ("RevisionControlsOps", "Node versionining operations.", true); 
	sub.addEntry(aAllowRegister); 
	sub.addEntry(aLogRegister); 
	sub.addSeparator();
	sub.addEntry(aAllowRelease); 
	sub.addEntry(aLogRelease); 
	sub.addSeparator();
	sub.addEntry(aAllowDelete); 
	sub.addEntry(aLogDelete); 
	sub.addSeparator();
	sub.addEntry(aAllowCheckIn); 
	sub.addEntry(aLogCheckIn); 
	sub.addSeparator();
	sub.addEntry(aAllowCheckOut); 
	sub.addEntry(aLogCheckOut); 
	sub.addSeparator();
	sub.addEntry(aAllowLock); 
	sub.addEntry(aLogLock); 
	sub.addSeparator();
	sub.addEntry(aAllowEvolve); 
	sub.addEntry(aLogEvolve); 
	sub.addSeparator();
	sub.addEntry(aAllowRevertFiles); 
	sub.addEntry(aLogRevertFiles); 
	sub.addSeparator();
	sub.addEntry(aAllowCloneFiles); 
	sub.addEntry(aLogCloneFiles); 
	sub.addSeparator();
	sub.addEntry(aAllowRemoveFiles); 
	sub.addEntry(aLogRemoveFiles); 
	sub.addSeparator();
	sub.addEntry(aAllowPackBundle); 
	sub.addEntry(aLogPackBundle); 
	sub.addSeparator();
	sub.addEntry(aAllowUnpackBundle); 
	sub.addEntry(aLogUnpackBundle); 

	layout.addSubGroup(sub);
      }
      
      {
	LayoutGroup sub = new LayoutGroup
	  ("AdministrationOps", "Administrative operations.", true); 
	sub.addEntry(aAllowBackupDatabase); 
	sub.addEntry(aLogBackupDatabase); 
	sub.addSeparator();
	sub.addEntry(aAllowArchive); 
	sub.addEntry(aLogArchive); 
	sub.addSeparator();
	sub.addEntry(aAllowOffline); 
	sub.addEntry(aLogOffline); 
	sub.addSeparator();
	sub.addEntry(aAllowRequestRestore); 
	sub.addEntry(aLogRequestRestore); 
	sub.addSeparator();
	sub.addEntry(aAllowDenyRestore); 
	sub.addEntry(aLogDenyRestore); 
	sub.addSeparator();
	sub.addEntry(aAllowRestore); 
	sub.addEntry(aLogRestore); 

	layout.addSubGroup(sub);
      }

      setLayout(layout);  
    }
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
    return true;
  }

  /**
   * The task to perform after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually.
   */ 
  public void 
  postEnableTask()
  {
    /* just to prove that the server is waiting on this task to finish... */ 
    try {
      Integer delay = (Integer) getParamValue(aEnableDelay); 
      if((delay != null) && (delay > 0)) {
	getLogMgr().logAndFlush
	  (LogMgr.Kind.Ext, LogMgr.Level.Info, 
	   "LogMasterActivity Enabling - " + 
	   "Please Wait (" + delay + ") milliseconds..."); 

	Thread.sleep(delay);
      }
    }
    catch(Exception ex) {
      getLogMgr().logAndFlush
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "LogMasterActivity Enabled!"); 
  }
  

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
    return true;
  }

  /**
   * The task to perform after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  public void 
  preDisableTask()
  {
    /* just to prove that the server is waiting on this task to finish... */ 
    try {
      Integer delay = (Integer) getParamValue(aDisableDelay); 
      if((delay != null) && (delay > 0)) {
	getLogMgr().logAndFlush
	  (LogMgr.Kind.Ext, LogMgr.Level.Info, 
	   "LogMasterActivity Disabling - " + 
	   "Please Wait (" + delay + ") milliseconds..."); 

	Thread.sleep(delay);
      }
    }
    catch(Exception ex) {
      getLogMgr().logAndFlush
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       "LogMasterActivity Disabled!"); 
  }

  
 
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
    return true;
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
  {
    if(!isParamTrue(aAllowSetWorkGroups)) 
      throw new PipelineException
	("Setting of work groups is not allowed!");
  }


  /**
   * Whether to run a task after setting the work groups used to determine the scope of 
   * administrative privileges. <P>
   */  
  public boolean
  hasPostSetWorkGroupsTask() 
  {
    return true;
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
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append("SET WORK GROUPS\n"); 

    for(String uname : groups.getUsers()) {
      buf.append("  User : " + uname + "\n");

      for(String gname : groups.getGroups()) {
        Boolean mm = groups.isMemberOrManager(uname, gname);
        if(mm != null) 
          buf.append("    Group : " + gname + " [" + (mm ? "MANAGER" : "Member") + "]\n");
      }
    }    

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new read-only package from the given 
   * modifiable package.
   */  
  public boolean
  hasPreCreateToolsetPackageTest() 
  {
    return true;
  }

  /**
   * Test to perform before creating a new read-only package from the given 
   * modifiable package.
   * 
   * @param author
   *   The name of the user creating the package.
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
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCreateToolsetPackageTest
  (
   String author, 
   PackageMod mod, 
   String desc, 
   VersionID.Level level, 
   OsType os
  ) 
    throws PipelineException
  {
    if(!isParamTrue(aAllowCreatePackage)) 
      throw new PipelineException
	("Creating a new read-only toolset package version is not allowed!");
  }


  /**
   * Whether to run a task after creating a new read-only package from the given 
   * modifiable package.
   */  
  public boolean
  hasPostCreateToolsetPackageTask() 
  {
    return isParamTrue(aLogCreatePackage);
  }

  /**
   * The task to perform after creating a new read-only package from the given 
   * modifiable package.
   * 
   * @param pkg
   *   The newly created toolset package version.
   * 
   * @param os
   *   The operating system type.
   */  
  public void
  postCreateToolsetPackageTask
  (
   PackageVersion pkg, 
   OsType os
  ) 
  {     
    String msg = 
      ("CREATE TOOLSET PACKAGE\n" +
       "  Package : " + pkg.getName() + " v" + pkg.getVersionID() + "\n" + 
       "  OS Type : " + os);

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new toolset from the given toolset packages.
   */  
  public boolean
  hasPreCreateToolsetTest() 
  {
    return true; 
  }

  /**
   * Test to perform before creating a new toolset from the given toolset packages.
   * 
   * @param author
   *   The name of the user creating the toolset.
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
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCreateToolsetTest
  (  
   String author, 
   String name, 
   String desc, 
   Collection<PackageVersion> packages,
   OsType os   
  ) 
    throws PipelineException
  {
    if(!isParamTrue(aAllowCreateToolset))
      throw new PipelineException
	("Creating a new read-only toolset is not allowed!");
  }


  /**
   * Whether to run a task after creating a new toolset from the given toolset packages.
   */  
  public boolean
  hasPostCreateToolsetTask() 
  {
    return isParamTrue(aLogCreateToolset);
  }

  /**
   * The task to perform after creating a new toolset from the given toolset packages.
   * 
   * @param tset
   *   The newly created Toolset.
   * 
   * @param os
   *   The operating system type.
   */  
  public void
  postCreateToolsetTask
  (
   Toolset tset, 
   OsType os
  ) 
  {
    String msg = 
      ("CREATE TOOLSET\n" +
       "   Toolset : " + tset.getName() + "\n" + 
       "   OS Type : " + os);

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   A R E A   O P S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new empty working area. <P>
   */  
  public boolean
  hasPreCreateWorkingAreaTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowCreateWorkingArea)) 
      throw new PipelineException
	("Creating new working area (" + author + "|" + view + ") is not allowed!");
  }


  /**
   * Whether to run a task after creating a new empty working area. <P>
   */  
  public boolean
  hasPostCreateWorkingAreaTask() 
  {
    return isParamTrue(aLogCreateWorkingArea);
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
  {
    String msg = 
      ("CREATE WORKING AREA\n" +
       "  Working Area : " + author + "|" + view); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing an entire working area. <P>
   */  
  public boolean
  hasPreRemoveWorkingAreaTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRemoveWorkingArea)) 
      throw new PipelineException
	("Removing existing working area (" + author + "|" + view + ") is not allowed!");
  }


  /**
   * Whether to run a task after removing an entire working area. <P>
   */  
  public boolean
  hasPostRemoveWorkingAreaTask() 
  {
    return isParamTrue(aLogRemoveWorkingArea);
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
  {
    String msg = 
      ("REMOVE WORKING AREA\n" +
       "  Working Area : " + author + "|" + view); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before adding an annotation to a node. <P>
   */  
  public boolean
  hasPreAddAnnotationTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowAddAnnotation)) 
      throw new PipelineException
	("Adding annotation (" + aname + ") to node (" + nname + ") is not allowed!");
  }


  /**
   * Whether to run a task after adding an annotation to a node. <P>
   */  
  public boolean
  hasPostAddAnnotationTask() 
  {
    return true;
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
  {
    String msg = 
      ("ADD ANNNOTATION\n" +
       "        Node Name : " + nname + "\n" + 
       "  Annotation Name : " + aname + "\n" + 
       "      Plugin Name : " + annot.getName() + "\n" + 
       "   Plugin Version : v" + annot.getVersionID() + "\n" + 
       "    Plugin Vendor : " + annot.getVendor());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing an annotation from a node. <P>
   */  
  public boolean
  hasPreRemoveAnnotationTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowAddAnnotation)) 
      throw new PipelineException
	("Removing annotation (" + aname + ") to node (" + nname + ") is not allowed!");
  }


  /**
   * Whether to run a task after removing an annotation from a node. <P>
   */  
  public boolean
  hasPostRemoveAnnotationTask() 
  {
    return true;
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
  {
    String msg = 
      ("REMOVE ANNNOTATION\n" +
       "        Node Name : " + nname + "\n" + 
       "  Annotation Name : " + aname);

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before modifying the properties of a working version of a node. <P>
   */  
  public boolean
  hasPreModifyPropertiesTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowModifyProperties)) 
      throw new PipelineException
	("Modifying node properties of node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a task after modifying the properties of a working version of a node. <P>
   */  
  public boolean
  hasPostModifyPropertiesTask() 
  {
    return isParamTrue(aLogModifyProperties); 
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
  {
    String msg = 
      ("MODIFY NODE PROPERTIES\n" +
       "  Modified Node : " + nodeID.getName() + "\n" +
       "   Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating or modifying an existing link between two 
   * node working versions. <P>
   */  
  public boolean
  hasPreLinkTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowLink)) 
      throw new PipelineException
	("Creating (or modifying) links between target node (" + target + ") and source " + 
	 "node (" + source + ") in working area (" + author + "|" + view + ") is not " +
	 "allowed!");
  }


  /**
   * Whether to run a task after creating or modifying an existing link between two 
   * node working versions. <P>
   */  
  public boolean
  hasPostLinkTask() 
  {
    return isParamTrue(aLogLink); 
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
  {
    String msg = 
      ("LINK NODES\n" +
       "   Target Node : " + target + "\n" +
       "   Source Node : " + target + "\n" +
       "  Working Area : " + author + "|" + view + "\n" + 
       "        Policy : " + policy + "\n" +
       "  Relationship : " + relationship + "\n" +
       "  Frame Offset : " + ((offset != null) ? offset : "-"));

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before destroying an existing link between the working versions.
   */  
  public boolean
  hasPreUnlinkTest() 
  {
    return true;
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
  {     
    if(!isParamTrue(aAllowUnlink)) 
      throw new PipelineException
	("Destroying links between target node (" + target + ") and source " + 
	 "node (" + source + ") in working area (" + author + "|" + view + ") is not " +
	 "allowed!");
  }


  /**
   * Whether to run a task after destroying an existing link between the working versions.
   */  
  public boolean
  hasPostUnlinkTask() 
  {
    return isParamTrue(aLogUnlink); 
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
  {
    String msg = 
      ("UNLINK NODES\n" +
       "   Target Node : " + target + "\n" +
       "   Source Node : " + target + "\n" +
       "  Working Area : " + author + "|" + view);

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before adding a secondary file sequence to a working version.
   */  
  public boolean
  hasPreAddSecondaryTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowAddSecondary)) 
      throw new PipelineException
	("Adding secondary sequence (" + fseq + ") to node (" + nodeID.getName() + ") " + 
	 "in working area (" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is " + 
	 "not allowed!");
  }


  /**
   * Whether to run a task after adding a secondary file sequence to a working version.
   */  
  public boolean
  hasPostAddSecondaryTask() 
  {
    return isParamTrue(aLogAddSecondary); 
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
  {
    String msg = 
      ("ADD SECONDARY SEQUENCE\n" +
       "   Modified Node : " + nodeID.getName() + "\n" +
       "    Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
       "  Added Sequence : " + fseq); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing a secondary file sequence to a working version.
   */  
  public boolean
  hasPreRemoveSecondaryTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRemoveSecondary)) 
      throw new PipelineException
	("Removing secondary sequence (" + fseq + ") from node (" + nodeID.getName() + ") " + 
	 "in working area (" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is " + 
	 "not allowed!");
  }


  /**
   * Whether to run a task after removing a secondary file sequence to a working version.
   */  
  public boolean
  hasPostRemoveSecondaryTask() 
  {
    return isParamTrue(aLogRemoveSecondary); 
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
  {
    String msg = 
      ("REMOVE SECONDARY SEQUENCE\n" +
       "     Modified Node : " + nodeID.getName() + "\n" +
       "      Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
       "  Removed Sequence : " + fseq); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before renaming a working version of a node which has never 
   * been checked-in.
   */  
  public boolean
  hasPreRenameTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRename)) 
      throw new PipelineException
	("Renaming node (" + nodeID.getName() + ") to (" + pattern + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a task after renaming a working version of a node which has never 
   * been checked-in.
   */  
  public boolean
  hasPostRenameTask() 
  {
    return isParamTrue(aLogRename);
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
  {
    String msg = 
      ("RENAME NODE\n" +
       "   Modified Node : " + nodeID.getName() + "\n" +
       "    Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
       "        New Name : " + pattern + "\n" + 
       "    Rename Files : " + renameFiles); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before renumbering the frame ranges of the file sequences 
   * associated with a node.
   */  
  public boolean
  hasPreRenumberTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRenumber))
      throw new PipelineException
	("Renumbering node (" + nodeID.getName() + ") to (" + range + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a task after renumbering the frame ranges of the file sequences 
   * associated with a node.
   */  
  public boolean
  hasPostRenumberTask() 
  {
    return isParamTrue(aLogRenumber);
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
  {
    String msg = 
      ("RENUMBER NODE\n" +
       "          Modified Node : " + nodeID.getName() + "\n" +
       "           Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
       "        New Frame Range : " + range + "\n" + 
       "  Remove Obsolete Files : " + removeFiles); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to test before an Editor plugin can be started for a working version of a node.
   */  
  public boolean
  hasPreEditingStartedTest() 
  {
    return true; 
  }

  /**
   * Test to perform before an Editor plugin can be started for a working version of a node.
   *
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param editorID
   *   The unique identifier of the Editor plugin which will be run.
   *
   * @param hostname
   *   The full name of the host on which the Editor will be run.
   *
   * @param imposter
   *   The name of the user impersonating the owner of the node to be edited or
   *   <CODE>null<CODE> if the editing user is the node's owner.
   */
  public void 
  preEditingStartedTest
  (
   NodeID nodeID,
   PluginID editorID, 
   String hostname,
   String imposter
  )
    throws PipelineException
  {
    if(!isParamTrue(aAllowEditing)) {
      String msg = 
        ("EDITING PREVENTED\n" +
         "  Modified Node : " + nodeID.getName() + "\n" +
         "   Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
         "       Hostname : " + hostname + "\n" + 
         "       Imposter : " + ((imposter != null) ? imposter : "NONE")); 
      
      getLogMgr().logAndFlush
        (LogMgr.Kind.Ext, LogMgr.Level.Warning, 
         msg);
      
      throw new PipelineException("Editing has been disabled!"); 
    }
  }
  
  /**
   * Whether to run a task after an Editor plugin has been started for a working version 
   * of a node. <P>
   */  
  public boolean
  hasPostEditingStartedTask() 
  {
    return isParamTrue(aLogEditingStarted);
  }
  
  /**
   * The task to perform after an Editor plugin has been started for a working version 
   * of a node. <P>
   * 
   * @param editID
   *   The unique ID for the editing session.
   * 
   * @param event
   *   The information known about the editing session.
   */  
  public void
  postEditingStartedTask
  (
   long editID, 
   EditedNodeEvent event
  ) 
  {
    String msg = 
      ("EDITING STARTED\n" +
       "     Edited Node : " + event.getNodeName() + "\n" +
       "    Working Area : " + event.getAuthor() + "|" + event.getView() + "\n" + 
       "      Session ID : " + editID + "\n" +
       "         Started : " + TimeStamps.format(event.getTimeStamp()) + "\n" + 
       "     Editor Name : " + event.getEditorName() + "\n" + 
       "  Editor Version : " + event.getEditorVersionID() + "\n" + 
       "   Editor Vendor : " + event.getEditorVendor()); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after an Editor plugin has finished for a working version 
   * of a node. <P>
   */  
  public boolean
  hasPostEditingFinishedTask() 
  {
    return isParamTrue(aLogEditingFinished);
  }
  
  /**
   * The task to perform after an Editor plugin has finished for a working version 
   * of a node. <P>
   * 
   * Note that there can be more than one Editor running for a single working node, so the 
   * editID should be used to match up start/finish pairs.  Its also possible that a editing
   * started event might not have a matching finish event because its possible to stop 
   * whatever program launched the Editor plugin before the underlying external program 
   * itself exits.
   * 
   * @param editID
   *   The unique ID for the editing session.
   * 
   * @param event
   *   The information known about the editing session.
   */  
  public void
  postEditingFinishedTask
  (
   long editID, 
   EditedNodeEvent event
  ) 
  {
    String msg = 
      ("EDITING FINISHED\n" +
       "     Edited Node : " + event.getNodeName() + "\n" +
       "    Working Area : " + event.getAuthor() + "|" + event.getView() + "\n" + 
       "      Session ID : " + editID + "\n" + 
       "         Started : " + TimeStamps.format(event.getTimeStamp()) + "\n" +
       "        Finished : " + TimeStamps.format(event.getFinishedStamp()) + "\n" + 
       "     Editor Name : " + event.getEditorName() + "\n" + 
       "  Editor Version : " + event.getEditorVersionID() + "\n" + 
       "   Editor Vendor : " + event.getEditorVendor()); 
       
    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L   O P S                                              */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to test before registering an initial working version of a node. 
   */  
  public boolean
  hasPreRegisterTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRegister)) 
      throw new PipelineException
	("Registering node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a tesk after registering an initial working version of a node. 
   */  
  public boolean
  hasPostRegisterTask() 
  {
    return isParamTrue(aLogRegister);
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
  {    
    String msg = 
      ("REGISTERED NEW NODE\n" +
       "      New Node : " + nodeID.getName() + "\n" + 
       "  Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before releasing the working versions of nodes and optionally 
   * remove the associated working area files. 
   */  
  public boolean
  hasPreReleaseTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRelease)) 
      throw new PipelineException
	("Releasing nodes from working area (" + author + "|" + view + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after releasing the working versions of nodes and optionally 
   * remove the associated working area files. 
   */  
  public boolean
  hasPostReleaseTask() 
  {
    return isParamTrue(aLogRelease); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("RELEASED NODES\n" +
       "  Working Area : " + author + "|" + view + "\n" + 
       "  Remove Files : " + removeFiles + "\n" + 
       "         Nodes : ");

    boolean first = true;
    for(String name : names) {
      if(!first) 
	buf.append("\n               : ");
      first = false;

      buf.append(name); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   */  
  public boolean
  hasPreDeleteTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowDelete)) 
      throw new PipelineException
	("Deleting node (" + name + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after deleting all working and checked-in versions of a node and 
   * optionally remove all associated working area files.  
   */  
  public boolean
  hasPostDeleteTask() 
  {
    return isParamTrue(aLogDelete);
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
  {
    String msg = 
      ("DELETED NODE\n" +
       "  Deleted Node : " + name + "\n" + 
       "  Remove Files : " + removeFiles); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
  public boolean
  hasPreCheckInTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowCheckIn)) 
      throw new PipelineException
	("Checking-in node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!\n\n" + 
         "The root node of the check-in operation was (" + rname + ").");
  }


  /**
   * Whether to run a tesk after checking-in an individual node.
   */  
  public boolean
  hasPostCheckInTask() 
  {
    return isParamTrue(aLogCheckIn); 
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
  {
    String msg = 
      ("CHECKED-IN NODE\n" +
       "  Node Version : " + vsn.getName() + " v" + vsn.getVersionID());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-out an individual node.
   */  
  public boolean
  hasPreCheckOutTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowCheckOut)) 
      throw new PipelineException
	("Checking-out node (" + nodeID.getName() + " v" + vsn.getVersionID() + ") in " + 
	 "working area (" + nodeID.getAuthor() + "|" + nodeID.getView() + ") " + 
	 "is not allowed!");
  }


  /**
   * Whether to run a tesk after checking-out an individual node.
   */  
  public boolean
  hasPostCheckOutTask() 
  {
    return isParamTrue(aLogCheckOut);
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
  {
    String msg = 
      ("CHECKED-OUT NODE\n" +
       "  Node Version : " + nodeID.getName() + " v" + mod.getWorkingID() + "\n" + 
       "  Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before locking the working version of a node to a specific 
   * checked-in version.
   */  
  public boolean
  hasPreLockTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowLock)) 
      throw new PipelineException
	("Locking node (" + nodeID.getName() + " v" + vid + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after locking the working version of a node to a specific 
   * checked-in version.
   */  
  public boolean
  hasPostLockTask() 
  {
    return isParamTrue(aLogLock);
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
  {
    String msg = 
      ("LOCKED NODE\n" +
       "  Node Version : " + nodeID.getName() + " v" + vid); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before changing the checked-in version upon which the working version 
   * is based without modifying the working version properties, links or associated files.
   */  
  public boolean
  hasPreEvolveTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowEvolve)) 
      throw new PipelineException
	("Evolving node (" + nodeID.getName() + " v" + vid + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after changing the checked-in version upon which the working 
   * version is based without modifying the working version properties, links or associated 
   * files.
   */  
  public boolean
  hasPostEvolveTask() 
  {
    return isParamTrue(aLogEvolve); 
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
  {
    String msg = 
      ("EVOLVED NODE\n" +
       "  Node Version : " + nodeID.getName() + " v" + vid); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a new node bundle by packing up a tree of working 
   * area nodes rooted at the given node.
   */  
  public boolean
  hasPrePackBundleTest() 
  {
    return true;
  }

  /**
   * Test to perform before creating a new node bundle by packing up a tree of working 
   * area nodes rooted at the given node.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  prePackBundleTest
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    if(!isParamTrue(aAllowPackBundle)) 
      throw new PipelineException
	("Creating a node bundle from the tree of nodes rooted at " +
         "(" + nodeID.getName() + ") in working area " + 
         "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }

  /**
   * Whether to run a tesk after creating a new node bundle by packing up a tree of 
   * working area nodes rooted at the given node.
   */  
  public boolean
  hasPostPackBundleTask() 
  {
    return true;
  }
  
  /**
   * The task to perform after creating a new node bundle by packing up a tree of 
   * working area nodes rooted at the given node.
   * 
   * @param bundle
   *   The node bundle metadata. 
   */  
  public void
  postPackBundleTask
  (
   NodeBundle bundle
  ) 
  {
    StringBuilder buf = new StringBuilder();

    NodeID root = bundle.getRootNodeID();
    
    buf.append
      ("PACK NODE BUNDLE\n" +
       "  Working Area : " + root.getAuthor() + "|" + root.getView() + "\n" + 
       "     Root Node : " + root.getName() + "\n"); 

    {
      buf.append("         Nodes : "); 
      boolean first = true;
      for(NodeMod mod : bundle.getWorkingVersions()) {
        if(!first) 
          buf.append("               : "); 
        first = false; 

        buf.append(mod.getName() + "\n");
      }
    }

    {
      buf.append("      Toolsets : "); 
      boolean first = true;
      for(String tname : bundle.getAllToolsetNames()) {
        TreeMap<OsType,Toolset> toolsets = bundle.getOsToolsets(tname);

        if(!first) 
          buf.append("               : "); 
        first = false; 

        boolean firstOs = true;
        buf.append(tname + " ["); 
        for(OsType os : toolsets.keySet()) {
          if(!firstOs) 
            buf.append(", "); 
          firstOs = false;
          
          buf.append(os.toString());
        }
        buf.append("]\n");
      }
    }

    {
      DoubleMap<String,OsType,TreeSet<VersionID>> pkgs = bundle.getAllToolsetPackageNames();

      buf.append("      Packages : "); 
      boolean first = true;
      for(String pname : pkgs.keySet()) {
        if(!first) 
          buf.append("               : "); 
        first = false; 

        for(OsType os : pkgs.keySet(pname)) {
          buf.append(pname + " [" + os + "] [");

          boolean firstVsn = true;
          for(VersionID vid : pkgs.get(pname, os)) {
            if(!firstVsn) 
              buf.append(" "); 
            firstVsn = false;
          
            buf.append("v" + vid); 
          }
          buf.append("]\n");
        }
      }
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
   */  
  public boolean
  hasPreUnpackTest() 
  {
    return true;
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
   TreeMap<String,String> licenseKeyRemap
  ) 
    throws PipelineException
  {
    if(!isParamTrue(aAllowUnpackBundle)) 
      throw new PipelineException
	("Unpacking node bundle (" + bundlePath + ") in the working area " + 
         "(" + author + "|" + view + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after unpacking a bundle containing a tree of nodes packed at 
   * another site into the given working area.
   */  
  public boolean
  hasPostUnpackTask() 
  {
    return true;
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
   NodeBundle bundle
  ) 
  {
    StringBuilder buf = new StringBuilder();

    NodeID root = bundle.getRootNodeID();
    
    buf.append
      ("UNPACK NODE BUNDLE\n" +
       "          Node Bundle : " + bundlePath + "\n" +
       "         Working Area : " + author + "|" + view + "\n" + 
       "     Release On Error : " + (releaseOnError ? "YES" : "no") + "\n" +
       "  Action On Existence : " + actOnExist + "\n");

    {
      buf.append("        Toolset Remap : "); 
      if(!toolsetRemap.isEmpty()) {
        boolean first = true;
        for(String otname : toolsetRemap.keySet()) {
          String ltname = toolsetRemap.get(otname); 
          
          if(!first) 
            buf.append("                      : "); 
          first = false; 
          
          buf.append(otname + " -> " + ltname + "\n"); 
        }
      }
      else {
        buf.append("[None]\n"); 
      }
    }
    
    {
      buf.append("  Selection Key Remap : "); 
      if(!selectionKeyRemap.isEmpty()) {
        boolean first = true;
        for(String okname : selectionKeyRemap.keySet()) {
          String lkname = selectionKeyRemap.get(okname); 
          
          if(!first) 
            buf.append("                      : "); 
          first = false; 
          
          buf.append(okname + " -> " + lkname + "\n"); 
        }
      }
      else {
        buf.append("[None]\n"); 
      }
    }
    
    {
      buf.append("    License Key Remap : "); 
      if(!selectionKeyRemap.isEmpty()) {
        boolean first = true;
        for(String okname : licenseKeyRemap.keySet()) {
          String lkname = licenseKeyRemap.get(okname); 
          
          if(!first) 
            buf.append("                      : "); 
          first = false; 
        
          buf.append(okname + " -> " + lkname + "\n"); 
        }
      }
      else {
        buf.append("[None]\n"); 
      }
    }
    
    {
      buf.append("        Bundles Nodes : "); 
      boolean first = true;
      for(NodeMod mod : bundle.getWorkingVersions()) {
        if(!first) 
          buf.append("                      : "); 
        first = false; 

        buf.append(mod.getName() + "\n");
      }
    }

    {
      buf.append("             Toolsets : "); 
      boolean first = true;
      for(String tname : bundle.getAllToolsetNames()) {
        TreeMap<OsType,Toolset> toolsets = bundle.getOsToolsets(tname);

        if(!first) 
          buf.append("                      : "); 
        first = false; 

        boolean firstOs = true;
        buf.append(tname + " ["); 
        for(OsType os : toolsets.keySet()) {
          if(!firstOs) 
            buf.append(", "); 
          firstOs = false;
          
          buf.append(os.toString());
        }
        buf.append("]\n");
      }
    }

    {
      DoubleMap<String,OsType,TreeSet<VersionID>> pkgs = bundle.getAllToolsetPackageNames();

      buf.append("             Packages : "); 
      boolean first = true;
      for(String pname : pkgs.keySet()) {
        for(OsType os : pkgs.keySet(pname)) {
          if(!first) 
            buf.append("                      : "); 
          first = false; 

          buf.append(pname + " [" + os + "] [");

          boolean firstVsn = true;
          for(VersionID vid : pkgs.get(pname, os)) {
            if(!firstVsn) 
              buf.append(" "); 
            firstVsn = false;
          
            buf.append("v" + vid); 
          }
          buf.append("]\n");
        }
      }
    }
    
    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before reverting specific working area files to an earlier 
   * checked-in version of the files.
   */  
  public boolean
  hasPreRevertFilesTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRevertFiles)) 
      throw new PipelineException
	("Reverting files for node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after reverting specific working area files to an earlier 
   * checked-in version of the files.
   */  
  public boolean
  hasPostRevertFilesTask() 
  {
    return isParamTrue(aLogRevertFiles); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("REVERTED FILES\n" + 
       "  Reverted Node : " + nodeID.getName() + "\n" +
       "   Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView() + "\n" + 
       "  File Versions : ");

    boolean first = true;
    for(String name : files.keySet()) {
      if(!first) 
	buf.append("\n                : ");
      first = false;

      buf.append(name + " v" + files.get(name)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before replacing the primary files associated with one node with 
   * the primary files of another node.
   */  
  public boolean
  hasPreCloneFilesTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRegister)) 
      throw new PipelineException
	("Copying files from node (" + sourceID.getName() + ") to node " + 
	 "(" + targetID.getName() + ") in working area " + 
	 "(" + sourceID.getAuthor() + "|" + sourceID.getView() + ") is not allowed!");
  }
  
  
  /**
   * Whether to run a tesk after replacing the primary files associated with one node with 
   * the primary files of another node.
   */  
  public boolean
  hasPostCloneFilesTask() 
  {
    return true;
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
  {
    String msg = 
      ("FILES CLONED\n" +
       "   Source Node : " + sourceID.getName() + "\n" + 
       "   Target Node : " + targetID.getName() + "\n" + 
       "  Working Area : " + sourceID.getAuthor() + "|" + sourceID.getView());

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing the working area files associated with a node.
   */  
  public boolean
  hasPreRemoveFilesTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRemoveFiles)) 
      throw new PipelineException
	("Removing files associated with node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after removing the working area files associated with a node.
   */  
  public boolean
  hasPostRemoveFilesTask() 
  {
    return isParamTrue(aLogRemoveFiles); 
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
  {
    String msg = 
      ("REMOVED FILES\n" + 
       "   Parent Node : " + nodeID.getName() + "\n" + 
       "  Working Area : " + nodeID.getAuthor() + "|" + nodeID.getView()); 

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N   O P S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before creating a database backup file.
   */  
  public boolean
  hasPreBackupDatabaseTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowBackupDatabase)) 
      throw new PipelineException
	("Backing up database to file (" + file + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after creating a database backup file.
   */  
  public boolean
  hasPostBackupDatabaseTask() 
  {
    return isParamTrue(aLogBackupDatabase); 
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
  {
    String msg = 
      ("DATABASE BACKED-UP\n" + 
       "  Backup File : " + file);

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       msg);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before archiving the files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPreArchiveTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowArchive)) 
      throw new PipelineException
	("Creating Archive (" + name + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after archiving the files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPostArchiveTask() 
  {
    return isParamTrue(aLogArchive); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("ARCHIVED VERSIONS\n" + 
       "  Archive Volume : " + name + "\n" + 
       "        Archiver : " + archiver.getName() + " v" + archiver.getVersionID() + "  " + 
                            "(" + archiver.getVendor() + ")\n" + 
       "         Toolset : " + toolset + "\n" + 
       "   Node Versions : ");

    boolean first = true;
    for(String vname : versions.keySet()) {
      if(!first) 
	buf.append("\n                 : ");
      first = false;

      buf.append(vname + " v" + versions.get(vname)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before removing the repository files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPreOfflineTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowOffline)) 
      throw new PipelineException
	("Offlining previously archived node versions is not allowed!");
  }


  /**
   * Whether to run a tesk after removing the repository files associated with the given 
   * checked-in versions.
   */  
  public boolean
  hasPostOfflineTask() 
  {
    return isParamTrue(aLogOffline); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("OFFLINED VERSIONS\n" + 
       "  Node Versions : ");

    boolean first = true;
    for(String name : versions.keySet()) {
      if(!first) 
	buf.append("\n                : ");
      first = false;

      buf.append(name + " v" + versions.get(name)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before requesting to restore the given set of checked-in versions.
   */  
  public boolean
  hasPreRequestRestoreTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRequestRestore)) 
      throw new PipelineException
	("Requests to restore offlined node versions are not allowed!");
  }


  /**
   * Whether to run a tesk after requesting to restore the given set of checked-in versions.
   */  
  public boolean
  hasPostRequestRestoreTask() 
  {
    return isParamTrue(aLogRequestRestore); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("RESTORE VERSIONS REQUESTED\n" + 
       "  Node Versions : ");

    boolean first = true;
    for(String name : versions.keySet()) {
      if(!first) 
	buf.append("\n                : ");
      first = false;

      buf.append(name + " v" + versions.get(name)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before denying the request to restore the given set of 
   * checked-in versions.
   */  
  public boolean
  hasPreDenyRestoreTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowDenyRestore)) 
      throw new PipelineException
	("Denials of requests to restore offlined node versions are not allowed!");
  }


  /**
   * Whether to run a tesk after denying the request to restore the given set of 
   * checked-in versions.
   */  
  public boolean
  hasPostDenyRestoreTask() 
  {
    return isParamTrue(aLogDenyRestore); 
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("RESTORE VERSIONS DENIED\n" + 
       "  Node Versions : ");

    boolean first = true;
    for(String name : versions.keySet()) {
      if(!first) 
	buf.append("\n                : ");
      first = false;

      buf.append(name + " v" + versions.get(name)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before restoring the given checked-in versions from the given 
   * archive volume.
   */  
  public boolean
  hasPreRestoreTest() 
  {
    return true;
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
  {
    if(!isParamTrue(aAllowRestore)) 
      throw new PipelineException
	("Restoring nodes versions from Archive (" + name + ") is not allowed!");
  }


  /**
   * Whether to run a tesk after restoring the given checked-in versions from the given 
   * archive volume.
   */  
  public boolean
  hasPostRestoreTask() 
  {
    return isParamTrue(aLogRestore);
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
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("RESTORED VERSIONS\n" + 
       "  Archive Volume : " + name + "\n" + 
       "        Archiver : " + archiver.getName() + " v" + archiver.getVersionID() + "  " + 
                            "(" + archiver.getVendor() + ")\n" + 
       "         Toolset : " + toolset + "\n" + 
       "   Node Versions : ");

    boolean first = true;
    for(String vname : versions.keySet()) {
      if(!first) 
	buf.append("\n                 : ");
      first = false;

      buf.append(vname + " v" + versions.get(vname)); 
    }

    getLogMgr().logAndFlush
      (LogMgr.Kind.Ext, LogMgr.Level.Info, 
       buf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given boolean extension parameter is currently true.
   */ 
  private boolean 
  isParamTrue
  (
   String pname
  ) 
  {
    try {
      Boolean tf = (Boolean) getParamValue(pname); 
      return ((tf != null) && tf);
    }
    catch(PipelineException ex) {
      getLogMgr().logAndFlush
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
      
      return false;
    }
  }

  /**
   * Get and optionally initialize the logger.
   */ 
  private LogMgr
  getLogMgr()
  { 
    boolean first = !LogMgr.exists("MasterActivity");
    LogMgr log = LogMgr.getInstance("MasterActivity");
    if(first)
      log.logToFile(new Path(PackageInfo.sInstPath, "/logs/master-activity.log"), 
                    4, 10485760L);
    return log;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9072466995033457519L;


  private static final String  aEnableDelay             = "EnableDelay"; 	   
  private static final String  aDisableDelay 	        = "DisableDelay";  	   

  private static final String  aAllowSetWorkGroups      = "AllowSetWorkGroups";   
  private static final String  aLogSetWorkGroups        = "LogSetWorkGroups"; 	  

  private static final String  aAllowCreatePackage      = "AllowCreatePackage";   
  private static final String  aLogCreatePackage        = "LogCreatePackage"; 	   

  private static final String  aAllowCreateToolset      = "AllowCreateToolset";   
  private static final String  aLogCreateToolset        = "LogCreateToolset"; 	   

  private static final String  aAllowCreateWorkingArea  = "AllowCreateWorkingArea";   
  private static final String  aLogCreateWorkingArea	= "LogCreateWorkingArea"; 	   

  private static final String  aAllowRemoveWorkingArea  = "AllowRemoveWorkingArea";   
  private static final String  aLogRemoveWorkingArea	= "LogRemoveWorkingArea"; 	   

  private static final String  aAllowAddAnnotation      = "AllowAddAnnotation";
  private static final String  aLogAddAnnotation        = "LogAddAnnotation";

  private static final String  aAllowRemoveAnnotation   = "AllowRemoveAnnotation";
  private static final String  aLogRemoveAnnotation     = "LogRemoveAnnotation";

  private static final String  aAllowModifyProperties   = "AllowModifyProperties";    
  private static final String  aLogModifyProperties 	= "LogModifyProperties";  	   

  private static final String  aAllowLink 		= "AllowLink";  		   
  private static final String  aLogLink 		= "LogLink";  		   

  private static final String  aAllowUnlink 		= "AllowUnlink";  		   
  private static final String  aLogUnlink 		= "LogUnlink";  		   

  private static final String  aAllowAddSecondary 	= "AllowAddSecondary";  	   
  private static final String  aLogAddSecondary 	= "LogAddSecondary";  	   

  private static final String  aAllowRemoveSecondary    = "AllowRemoveSecondary";     
  private static final String  aLogRemoveSecondary 	= "LogRemoveSecondary";  	   

  private static final String  aAllowRename 		= "AllowRename";  		   
  private static final String  aLogRename 	        = "LogRename";  		   

  private static final String  aAllowRenumber 	        = "AllowRenumber";  	   
  private static final String  aLogRenumber 		= "LogRenumber";  		   

  private static final String  aAllowEditing            = "AllowEditing"; 
  private static final String  aLogEditingStarted       = "LogEditingStarted";  
  private static final String  aLogEditingFinished      = "LogEditingFinished";  

  private static final String  aAllowRegister	        = "AllowRegister";    
  private static final String  aLogRegister             = "LogRegister";               

  private static final String  aAllowRelease            = "AllowRelease";      
  private static final String  aLogRelease              = "LogRelease";      

  private static final String aAllowDelete              = "AllowDelete";      
  private static final String aLogDelete                = "LogDelete";      

  private static final String aAllowCheckIn             = "AllowCheckIn";      
  private static final String aLogCheckIn               = "LogCheckIn";      

  private static final String aAllowCheckOut            = "AllowCheckOut";      
  private static final String aLogCheckOut              = "LogCheckOut";      

  private static final String aAllowLock                = "AllowLock";      
  private static final String aLogLock                  = "LogLock";      

  private static final String aAllowEvolve              = "AllowEvolve";      
  private static final String aLogEvolve                = "LogEvolve";      

  private static final String aAllowPackBundle          = "AllowPackBundle";      
  private static final String aLogPackBundle            = "LogPackBundle";      

  private static final String aAllowUnpackBundle        = "AllowUnpackBundle";      
  private static final String aLogUnpackBundle          = "LogUnpackBundle";      

  private static final String aAllowRevertFiles         = "AllowRevertFiles";      
  private static final String aLogRevertFiles           = "LogRevertFiles";      

  private static final String aAllowCloneFiles          = "AllowCloneFiles";      
  private static final String aLogCloneFiles            = "LogCloneFiles";      

  private static final String aAllowRemoveFiles         = "AllowRemoveFiles";      
  private static final String aLogRemoveFiles           = "LogRemoveFiles";      

  private static final String aAllowBackupDatabase      = "AllowBackupDatabase";      
  private static final String aLogBackupDatabase        = "LogBackupDatabase";      

  private static final String aAllowArchive             = "AllowArchive";      
  private static final String aLogArchive               = "LogArchive";      

  private static final String aAllowOffline             = "AllowOffline";      
  private static final String aLogOffline               = "LogOffline";      

  private static final String aAllowRequestRestore      = "AllowRequestRestore";      
  private static final String aLogRequestRestore        = "LogRequestRestore";      

  private static final String aAllowDenyRestore         = "AllowDenyRestore";      
  private static final String aLogDenyRestore           = "LogDenyRestore";      

  private static final String aAllowRestore             = "AllowRestore";      
  private static final String aLogRestore               = "LogRestore";      

}
