// $Id: GenUserPrefsApp.java,v 1.32 2006/10/11 22:45:41 jim Exp $

import java.awt.*; 
import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G E N   U S E R   P R E F S   A P P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates the <CODE>UserPrefs.java</CODE> and <CODE>JUserPrefsDialog.java</CODE> source
 * files.
 */ 
public 
class GenUserPrefsApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GenUserPrefsApp() 
  {
    pPrefs = new TreeMap<String,BasePref[]>();

    LinkedList<String> keys = new LinkedList<String>();
    keys.add("ALT");
    keys.add("CTRL");
    keys.add("SHIFT");
    keys.add("ALT+CTRL");
    keys.add("ALT+SHIFT");
    keys.add("CTRL+SHIFT");
    keys.add("ALT+CTRL+SHIFT");

    {
      BasePref prefs[] = {
	new ChoicePref
	("The function keys pressed in combination with MOUSE3 to show the Main Menu.", 
	 "MainMenuPopup", "Main Menu Popup:", keys, "ALT"),

	new BasePref(),

	new HotKeyPref
	("Rename the current window.", 
	 "ManagerRenameWindow", "Rename Window:"), 

	new BasePref(),

	new HotKeyPref
	("Change the working area view of the panel.", 
	 "ManagerChangeOwnerView", "Change Owner|View:",
	 false, true, false, 86), /* ALT+V */ 

	new BasePref(),
	
	new HotKeyPref
	("Save the current panel layout.",
	 "SaveLayout", "Save Layout:",
	 false, true, false, 83),  /* ALT+S */ 
	
	new HotKeyPref
	("Save the current panel layout with a new name.",
	 "SaveLayoutAs", "Save Layout As:"),
	
	new HotKeyPref
	("Manage the saved panel layouts.",
	 "ShowManageLayouts", "Manage Layouts:"),

	new HotKeyPref
	("Make the current panel layout the default layout.",
	 "SetDefaultLayout", "Set Default Layout:"),  
	
	new BasePref(),

	new HotKeyPref
	("Edit the user preferences.",
	 "ShowUserPrefs", "Preferences:",
	 false, true, false, 80),  /* ALT+P */ 

	new HotKeyPref
	("Manage the default editor for filename suffix.", 
	 "ShowDefaultEditors", "Default Editors:",
	 false, true, false, 69),  /* ALT+E */ 

	new HotKeyPref
	("Make sure that the latest plugins and plugin menus are being used.", 
	 "UpdatePlugins", "Update Plugins:",
	 false, true, false, 85),  /* ALT+U */ 

	new BasePref(),
	new BasePref(),
	
	new HotKeyPref
	("Quit.", 
	 "Quit", "Quit:",
	 false, false, true, 81)  /* CTRL+Q */ 
      };

      pPrefs.put("Main Menu|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Manage the user privileges.", 
	 "ShowManagePrivileges", "User Privileges:"),    

	new HotKeyPref
	("Manage the toolset environments.", 
	 "ShowManageToolsets", "Toolsets:"),    

	new BasePref(),

	new HotKeyPref
	("Manage the license keys.", 
	 "ShowManageLicenseKeys", "License Keys:"),    

	new HotKeyPref
	("Manage the selection keys.", 
	 "ShowManageSelectionKeys", "Selection Keys:"),

	new BasePref(),

	new HotKeyPref
	("Manage the server extension configurations.",
	 "ShowManageServerExtensions", "Master Extensions:"),
      };

      pPrefs.put("Main Menu|Admin|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Information about Pipeline.", 
	 "ShowAbout", "About:"),   

	new HotKeyPref
	("Display the node state quick reference page.", 
	 "ShowQuickReference", "Quick Reference:"),  

	new HotKeyPref
	("Display the Pipeline User Manual.", 
	 "ShowUserManual", "User Manual:"), 

	new BasePref(),
	
	new HotKeyPref
	("Display the Pipeline Home page.", 
	 "ShowHomePage", "Home Page:"),   

	new HotKeyPref
	("Display the Support Forums page.", 
	 "ShowSupportForums", "Support Forums:"),   
	
	new HotKeyPref
	("Display the Bug Database page.", 
	 "ShowBugDatabase", "Bug Database:"), 

	new BasePref(),

	new HotKeyPref
	("Display the local site configuration information.", 
	 "ShowConfig", "Site Configuration:"), 

	new HotKeyPref
	("Display the Pipeline license agreement.", 
	 "ShowLicenseAgreement", "License Agreement:"), 
      };

      pPrefs.put("Main Menu|Help|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new ChoicePref
	("The function keys pressed in combination with MOUSE3 to show the Group Menu.", 
	 "GroupMenuPopup", "Group Menu Popup:", keys, "CTRL"),

	new BasePref(),

	new HotKeyPref
	("No panel group.", 
	 "ManagerGroup0", "No Group:",
	 false, false, true, 48),  /* CTRL+0 */ 

	new BasePref(),

	new HotKeyPref
	("Set the panel group to (1).", 
	 "ManagerGroup1", "Group 1:",
	 false, false, true, 49),  /* CTRL+1 */ 

	new HotKeyPref
	("Set the panel group to (2).", 
	 "ManagerGroup2", "Group 2:",
	 false, false, true, 50),  /* CTRL+2 */ 

	new HotKeyPref
	("Set the panel group to (3).", 
	 "ManagerGroup3", "Group 3:",
	 false, false, true, 51),  /* CTRL+3 */ 

	new HotKeyPref
	("Set the panel group to (4).", 
	 "ManagerGroup4", "Group 4:",
	 false, false, true, 52),  /* CTRL+4 */ 

	new HotKeyPref
	("Set the panel group to (5).", 
	 "ManagerGroup5", "Group 5:",
	 false, false, true, 53),  /* CTRL+5 */ 

	new HotKeyPref
	("Set the panel group to (6).", 
	 "ManagerGroup6", "Group 6:",
	 false, false, true, 54),  /* CTRL+6 */ 

	new HotKeyPref
	("Set the panel group to (7).", 
	 "ManagerGroup7", "Group 7:",
	 false, false, true, 55),  /* CTRL+7 */ 

	new HotKeyPref
	("Set the panel group to (8).", 
	 "ManagerGroup8", "Group 8:",
	 false, false, true, 56),  /* CTRL+8 */ 

	new HotKeyPref
	("Set the panel group to (9).", 
	 "ManagerGroup9", "Group 9:",
	 false, false, true, 57)  /* CTRL+9 */ 
      };

      pPrefs.put("Main Menu|Panel Group|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Create a new window containing a Node Browser panel.", 
	 "ManagerNodeBrowserWindow", "Node Browser:",
	 false, true, false, 112),  /* ALT+F1 */ 

	new HotKeyPref
	("Create a new window containing a Node Viewer panel.", 
	 "ManagerNodeViewerWindow", "Node Viewer:",
	 false, true, false, 113),  /* ALT+F2 */ 

	new BasePref(),

	new HotKeyPref
	("Create a new window containing a Node Details panel.", 
	 "ManagerNodeDetailsWindow", "Node Details:",
	 false, true, false, 114),  /* ALT+F3 */ 

	new HotKeyPref
	("Create a new window containing a Node Files panel.", 
	 "ManagerNodeFilesWindow", "Node Files:",
	 false, true, false, 115),  /* ALT+F4 */ 

	new HotKeyPref
	("Create a new window containing a Node Links panel.", 
	 "ManagerNodeLinksWindow", "Node Links:",
	 false, true, false, 116),  /* ALT+F5 */ 

	new HotKeyPref
	("Create a new window containing a Node History panel.", 
	 "ManagerNodeHistoryWindow", "Node History:",
	 false, true, false, 117),  /* ALT+F6 */ 

	new BasePref(),

	new HotKeyPref
	("Create a new window containing a Job Browser panel.",
	 "ManagerJobBrowserWindow", "Job Browser:",
	 false, true, false, 118),  /* ALT+F7 */ 
	
	new HotKeyPref
	("Create a new window containing a Job Viewer panel.", 
	 "ManagerJobViewerWindow", "Job Viewer:",
	 false, true, false, 119),  /* ALT+F8 */ 

	new HotKeyPref
	("Create a new window containing a Job Details panel.", 
	 "ManagerJobDetailsWindow", "Job Details:",
	 false, true, false, 120),  /* ALT+F9 */ 

	new BasePref(),

	new HotKeyPref
	("Create a new window containing an empty panel.", 
	 "ManagerEmptyWindow", "None:",
	 false, true, false, 121),  /* ALT+F10 */ 	
      };

      pPrefs.put("Main Menu|Panel Window|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Change the panel type to a Node Browser panel.", 
	 "ManagerNodeBrowserPanel", "Node Browser:",
	 false, false, false, 112),  /* F1 */ 

	new HotKeyPref
	("Change the panel type to a Node Viewer panel.", 
	 "ManagerNodeViewerPanel", "Node Viewer:",
	 false, false, false, 113),  /* F2 */ 

	new BasePref(),

	new HotKeyPref
	("Change the panel type to a Node Details panel.", 
	 "ManagerNodeDetailsPanel", "Node Details:",
	 false, false, false, 114),  /* F3 */ 

	new HotKeyPref
	("Change the panel type to a Node Files panel.", 
	 "ManagerNodeFilesPanel", "Node Files:",
	 false, false, false, 115),  /* F4 */ 

	new HotKeyPref
	("Change the panel type to a Node Links panel.", 
	 "ManagerNodeLinksPanel", "Node Links:",
	 false, false, false, 116),  /* F5 */ 

	new HotKeyPref
	("Change the panel type to a Node History panel.", 
	 "ManagerNodeHistoryPanel", "Node History:",
	 false, false, false, 117),  /* F6 */ 

	new BasePref(),

	new HotKeyPref
	("Change the panel type to a Job Browser panel.",
	 "ManagerJobBrowserPanel", "Job Browser:",
	 false, false, false, 118),  /* F7 */ 
	
	new HotKeyPref
	("Change the panel type to a Job Viewer panel.", 
	 "ManagerJobViewerPanel", "Job Viewer:",
	 false, false, false, 119),  /* F8 */ 
	
	new HotKeyPref
	("Change the panel type to a Job Details panel.", 
	 "ManagerJobDetailsPanel", "Job Details:",
	 false, false, false, 120),  /* F9 */ 
	
	new BasePref(),

	new HotKeyPref
	("Change the panel type to an empty panel.", 
	 "ManagerEmptyPanel", "None:",
	 false, false, false, 121),  /* F9 */ 
      };

      pPrefs.put("Main Menu|Panel Type|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Add a tabbed panel.", 
	 "ManagerAddTab", "Add Tab:",
	 false, true, false, 84),  /* ALT+T */ 

	new HotKeyPref
	("Add a tab to the containing tabbed panel.", 
	 "ManagerAddTopTab", "Add Top Tab:",
	 true, true, false, 84),  /* ALT+SHIFT+T */ 

	new BasePref(),

	new HotKeyPref
	("Split the panel horizontally adding a new panel left.", 
	 "ManagerAddLeft", "Add Left:",
	 false, true, false, 76),  /* ALT+L */ 

	new HotKeyPref
	("Split the panel horizontally adding a new panel right.", 
	 "ManagerAddRight", "Add Right:",
	 false, true, false, 82),  /* ALT+R */ 

	new BasePref(),

	new HotKeyPref
	("Split the panel vertically adding a new panel above.", 
	 "ManagerAddAbove", "Add Above:",
	 false, true, false, 65),  /* ALT+A */ 

	new HotKeyPref
	("Split the panel vertically adding a new panel below.", 
	 "ManagerAddBelow", "Add Below:",
	 false, true, false, 66),  /* ALT+B */ 

	new BasePref(),

	new HotKeyPref
	("Close the current panel.", 
	 "ManagerClosePanel", "Close Panel:",
	 false, true, false, 88)   /* ALT+X */ 
      };

      pPrefs.put("Main Menu|Panel Layout|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {      
	new BooleanPref
	("Whether to show tool tips for editable fields.", 
	 "ShowToolTips", "Show Field Tool Tips:", true), 
	    
	new BooleanPref
	("Whether to show tool tool tips for menu items.", 
	 "ShowMenuToolTips", "Show Menu Tool Tips:", true), 
	
	new BasePref(),

	new BoundedIntegerPref
	("The initial delay before showing the tool tip.", 
	 "ToolTipDelay", "Tool Tip Delay:", 0, 4000, 1000), 

	new BoundedIntegerPref
	("The amount of time the tool tip is visible.", 
	 "ToolTipDuration", "Tool Tip Duration:", 1000, 10000, 4000), 
      };

      pPrefs.put("Main Menu|Tool Tips", prefs);
    }

    {
      LinkedList<String> options = new LinkedList();
      options.add("Disabled");
      options.add("Save Only");
      options.add("Save & Make Default");

      BasePref prefs[] = {
	new ChoicePref
	("Whether to automatically save the current layout on exit.",
	 "AutoSaveLayout", "Auto Save Layout:", options, "Disabled")
      };

      pPrefs.put("Main Menu|General", prefs);
    }

    {
      BasePref prefs[] = {
	new BooleanPref
	("Whether to show panel type labels.", 
	 "ShowPanelLabels", "Show Panel Labels:", false)
      };

      pPrefs.put("Panels|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Switch to the next tabbed panel.", 
	 "NextTab", "Next Tab:"),

	new HotKeyPref
	("Switch to the previous tabbed panel.", 
	 "PrevTab", "Previous Tab:"),
      };

      pPrefs.put("Panels|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Update the status of the node tree and selected nodes.", 
	 "Update", "Update Nodes:",
	 false, false, false, 32),  /* Space */ 

	new BasePref(),

	new HotKeyPref
	("Show the node filter dialog.", 
	 "NodeBrowserNodeFilter", "Node Filter:")	
      };

      pPrefs.put("Panels|Node Browser|Hot Keys", prefs);
    }

    {
      LinkedList<String> styles = new LinkedList();
      styles.add("None");
      styles.add("Name Only");
      styles.add("Pattern & Range");
      styles.add("Pattern & Range Below");

      BasePref prefs[] = {
	new ChoicePref
	("The information displayed by the node label.", 
	 "NodeLabelStyle", "Label Style:", styles, "Pattern & Range Below"), 

	new BasePref(),

	new BoundedDoublePref
	("The horizontal distance between nodes.", 
	 "NodeSpaceX", "Horizontal Space:", 2.5, 12.0, 3.5),

	new BoundedDoublePref
	("The vertical distance between nodes.", 
	 "NodeSpaceY", "Vertical Space:", 1.5, 3.0, 2.0),

	new BoundedDoublePref
	("The vertical offset distance for nodes with an odd depth level.",
	 "NodeOffset", "Vertical Offset:", 0.0, 1.0, 0.0),
	
	new BasePref(),

	new BooleanPref
	("Whether to draw graphics representating disabled actions.",
	 "DrawDisabledAction", "Draw Disabled Action:", true), 

	new BoundedDoublePref
	("The size of disabled action graphics.", 
	 "DisabledActionSize", "Disabled Action Size:", 0.05, 0.2, 0.15)
      };

      pPrefs.put("Panels|Node Viewer|Node|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Update connected node details panels.",
	 "Details", "Details:",
	 false, false, false, 68),  /* D */	

	new BasePref(),

	new HotKeyPref
	("Make the current primary selection the only root node.", 
	 "NodeViewerMakeRoot", "Set Root:"),

	new HotKeyPref
	("Add the current primary selection to the set of root nodes.",
	 "NodeViewerAddRoot", "Add Root:"),

	new HotKeyPref
	("Replace the root node of the current primary selection with the primary selection.",
	 "NodeViewerReplaceRoot", "Replace Root:"),

	new HotKeyPref
	("Hide the root node of the current primary selection.",
	 "NodeViewerRemoveRoot", "Hide Root:"),

	new BasePref(),
	
	new HotKeyPref
	("Edit primary file sequence of the current primary selection.",
	 "Edit", "Edit:", 
	 false, false, false, 10),  /* Enter */ 

	new HotKeyPref
	("Edit primary file sequence of the current primary selection using the default" + 
	 "editor for the file type.",
	 "EditWithDefault", "Edit With Default:", 
	 true, false, false, 10),  /* SHIFT+Enter */ 

	new BasePref(),
	
	new HotKeyPref
	("Link the secondary selected nodes to the current primary selection.",
	 "NodeViewerLink", "Link:"), 

	new HotKeyPref
	("Unlink the secondary selected nodes from the current primary selection.",
	 "NodeViewerUnlink", "Unlink:"), 

	new BasePref(),
	
	new HotKeyPref
	("Add a secondary file sequence to the current primary selection.",
	 "NodeViewerAddSecondary", "Add Secondary:"), 

	new BasePref(),

	new HotKeyPref
	("Submit jobs to the queue for the current primary selection.",
	 "QueueJobs", "Queue Jobs:", 
	 false, false, false, 81),  /* Q */ 

	new HotKeyPref
	("Submit jobs to the queue for the current primary selection with special job " + 
	 "requirements.",
	 "QueueJobsSpecial", "Queue Jobs Special:", 
	 true, false, false, 81),  /* SHIFT-Q */ 

	new HotKeyPref
	("Pause all jobs associated with the selected nodes.",
	 "PauseJobs", "Pause Jobs:",
	 false, false, false, 45),  /* Minus */ 

	new HotKeyPref
	("Resume execution of all jobs associated with the selected nodes.",
	 "ResumeJobs", "Resume Jobs:", 
	 false, false, false, 61),  /* Equals */ 

	new HotKeyPref
	("Preempt all jobs associated with the selected nodes.",
	 "PreemptJobs", "Preempt Jobs:"), 

	new HotKeyPref
	("Kill all jobs associated with the selected nodes.",
	 "KillJobs", "Kill Jobs:", 
	 false, false, false, 8),  /* Backspace */ 

	new BasePref(),
	
	new HotKeyPref
	("Check-in the current primary selection.",
	 "NodeViewerCheckIn", "Check-In:"), 

	new HotKeyPref
	("Check-out the current primary selection.",
	 "NodeViewerCheckOut", "Check-Out:"), 

	new HotKeyPref
	("Lock the current primary selection to a specific checked-in version.",
	 "NodeViewerLock", "Lock:"), 

	new HotKeyPref
	("Evolve the current primary selection.",
	 "NodeViewerEvolve", "Evolve Version:"), 

	new BasePref(),

	new HotKeyPref
	("Register a new node which is a clone of the current primary selection.",
	 "NodeViewerClone", "Clone:"), 

	new HotKeyPref
	("Export the node properties from the primary selection to the selected nodes.",
	 "NodeViewerExport", "Export:"), 

	new HotKeyPref
	("Rename the current primary selection.",
	 "NodeViewerRename", "Rename:"), 

	new HotKeyPref
	("Renumber the current primary selection.",
	 "NodeViewerRenumber", "Renumber:"), 
	
	new HotKeyPref
	("Remove all the primary/secondary files associated with the selected nodes.",
	 "RemoveFiles", "Remove Files:"), 

	new BasePref(),

	new HotKeyPref
	("Release the current primary selection.",
	 "NodeViewerRelease", "Release:"),

	new HotKeyPref
	("Release nodes from the current working area view.",
	 "NodeViewerReleaseView", "Release View:"),

	new HotKeyPref
	("Delete the current primary selection.",
	 "NodeViewerDelete", "Delete:")
      };

      pPrefs.put("Panels|Node Viewer|Node|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new ColorPref
	("The color to use for non-stale link lines.", 
	 "LinkColor", "Line Color:", Color.white), 

	new ColorPref
	("The color to use for stale link lines.", 
	 "StaleLinkColor", "Stale Line Color:", Color.yellow),
	
	new BoundedDoublePref
	("The thickness of link lines.", 
	 "LinkThickness", "Line Thickness:", 0.25, 3.0, 1.0),

	new BasePref(),

	new BoundedDoublePref
	("The distance between node and the start/end of link.", 
	 "LinkGap", "Node/Link Gap:", 0.0, 0.2, 0.05),

	new BoundedDoublePref
	("The position of the vertical crossbar as a percentage of the horizontal space.", 
	 "LinkVerticalCrossbar", "Vertical Crossbar:", 0.35, 0.55, 0.45), 

	new BasePref(),
	
	new BooleanPref
	("Whether to draw arrow heads showing the direction of the link.",
	 "DrawArrowHeads", "Draw Arrowheads:", true), 
	
	new BoundedDoublePref
	("The length of the link arrow head.", 
	 "ArrowHeadLength", "Arrowhead Length:", 0.08, 0.4, 0.2),
	
	new BoundedDoublePref
	("The width of the link arrow head.", 
	 "ArrowHeadWidth", "Arrowhead Width:", 0.04, 0.2, 0.08),

	new BasePref(),
	
	new BooleanPref
	("Whether to draw graphics representating LinkRelationship.",
	 "DrawLinkRelationship", "Draw Link Relationship:", true), 
	 
	new BooleanPref
	("Whether to draw graphics representating LinkPolicy.",
	 "DrawLinkPolicy", "Draw Link Policy:", true), 

	new BoundedDoublePref
	("The size of LinkPolicy graphics", 
	 "LinkPolicySize", "Link Policy Size:", 0.05, 0.2, 0.15)
      };

      pPrefs.put("Panels|Node Viewer|Links|Appearance", prefs);
    }


    {
      BasePref prefs[] = {
	new HotKeyPref
	("Edit the properties of the selected link.", 
	 "NodeViewerLinkEdit", "Edit Link:",	
	 false, false, false, 10),  /* Enter */ 

	new HotKeyPref
	("Remove the selected link.", 
	 "NodeViewerLinkUnlink", "UnLink:", 
	 false, false, false, 8),  /* Backspace */ 	
      };

      pPrefs.put("Panels|Node Viewer|Links|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new BooleanPref
	("Whether to initially show downstream links.",
	 "ShowDownstream", "Show Downstream:", false)
      };

      pPrefs.put("Panels|Node Viewer|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Update the status of all nodes.", 
	 "NodeViewerUpdate", "Update Nodes:", "Update"),
	
	new HotKeyPref
	("Register a new node.",
	 "NodeViewerRegisterNewNode", "Register New Node:",
	 false, false, false, 82), /* R */ 	

	new BasePref(),

	new HotKeyPref
	("Move the camera to frame the bounds of the currently selected nodes.",
	 "FrameSelection", "Frame Selection:",
	 false, false, false, 70),  /* F */ 
	
	new HotKeyPref
	("Move the camera to frame all active nodes.",
	 "FrameAll", "Frame All:", 
	 false, false, false, 71),  /* G */ 
	
	new BasePref(),

	new HotKeyPref
	("Automatically expand the first occurance of a node.",
	 "AutomaticExpand", "Automatic Expand:", 
	 false, false, false, 69),  /* E */

	new HotKeyPref
	("Expand all nodes.",
	 "ExpandAll", "Expand All:", 
	 false, false, false, 91),  /* Open Bracket */

	new HotKeyPref
	("Collapse all nodes.",
	 "CollapseAll", "Collapse All:", 
	 false, false, false, 93),  /* Close Bracket */

	new BasePref(),

	new HotKeyPref
	("Expand 1 level of nodes.",
	 "Expand1Level", "Expand 1 Level:", 
	 false, true, false, 49),  /* ALT+1 */

	new HotKeyPref
	("Expand the first 2 levels of nodes.",
	 "Expand2Levels", "Expand 2 Levels:", 
	 false, true, false, 50),  /* ALT+2 */

	new HotKeyPref
	("Expand the first 3 levels of nodes.",
	 "Expand3Levels", "Expand 3 Levels:", 
	 false, true, false, 51),  /* ALT+3 */

	new HotKeyPref
	("Expand the first 4 levels of nodes.",
	 "Expand4Levels", "Expand 4 Levels:", 
	 false, true, false, 52),  /* ALT+4 */

	new HotKeyPref
	("Expand the first 5 levels of nodes.",
	 "Expand5Levels", "Expand 5 Levels:", 
	 false, true, false, 53),  /* ALT+5 */

	new HotKeyPref
	("Expand the first 6 levels of nodes.",
	 "Expand6Levels", "Expand 6 Levels:", 
	 false, true, false, 54),  /* ALT+6 */

	new HotKeyPref
	("Expand the first 7 levels of nodes.",
	 "Expand7Levels", "Expand 7 Levels:", 
	 false, true, false, 55),  /* ALT+7 */

	new HotKeyPref
	("Expand the first 8 levels of nodes.",
	 "Expand8Levels", "Expand 8 Levels:", 
	 false, true, false, 56),  /* ALT+8 */

	new HotKeyPref
	("Expand the first 9 levels of nodes.",
	 "Expand9Levels", "Expand 9 Levels:", 
	 false, true, false, 57),  /* ALT+9 */

	new BasePref(),

	new HotKeyPref
	("Show/hide nodes downstream of the focus node.",
	 "NodeViewerShowHideDownstreamNodes", "Show/Hide Downstream:", 
	 false, false, false, 68),  /* D */

	new BasePref(),

	new HotKeyPref
	("Hide all of the roots nodes.",
	 "NodeViewerRemoveAllRoots", "Hide All Roots:")
      };

      pPrefs.put("Panels|Node Viewer|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Apply the changes to the working version.", 
	 "ApplyChanges", "Apply Changes:",
	 false, false, false, 155),   /* Insert */ 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("Edit primary file sequences of the current node.",
	 "NodeDetailsEdit", "Edit:", "Edit"), 

	new DuplicateHotKeyPref
	("Edit primary file sequence of the current node using the default" + 
	 "editor for the file type.",
	 "NodeDetailsEditWithDefault", "Edit With Default:", "EditWithDefault"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node.",
	 "NodeDetailsQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node with special job requirements.",
	 "NodeDetailsQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"),  

	new DuplicateHotKeyPref
	("Pause all jobs associated with the current node.",
	 "NodeDetailsPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all jobs associated with the current node.",
	 "NodeDetailsResumeJobs", "Resume Jobs:", "ResumeJobs"), 

	new DuplicateHotKeyPref
	("Preempt all jobs associated with the current node.",
	 "NodeDetailsPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all jobs associated with the current node.",
	 "NodeDetailsKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Remove all the primary/secondary files associated with the selected node.",
	 "NodeDetailsRemoveFiles", "Remove Files:", "RemoveFiles"), 
      };

      pPrefs.put("Panels|Node Details|Hot Keys", prefs);
    }
    
    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Apply the changes to the working version.", 
	 "NodeFilesApplyChanges", "Apply Changes:", "ApplyChanges"), 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("Edit primary file sequences of the current node.",
	 "NodeFilesEdit", "Edit:", "Edit"), 

	new DuplicateHotKeyPref
	("Edit primary file sequence of the current node using the default" + 
	 "editor for the file type.",
	 "NodeFilesEditWithDefault", "Edit With Default:", "EditWithDefault"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node.",
	 "NodeFilesQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node with special job requirements.",
	 "NodeFilesQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"),  

	new DuplicateHotKeyPref
	("Pause all jobs associated with the current node.",
	 "NodeFilesPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all jobs associated with the current node.",
	 "NodeFilesResumeJobs", "Resume Jobs:", "ResumeJobs"), 

	new DuplicateHotKeyPref
	("Preempt all jobs associated with the current node.",
	 "NodeFilesPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all jobs associated with the current node.",
	 "NodeFilesKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Remove all the primary/secondary files associated with the selected node.",
	 "NodeFilesRemoveFiles", "Remove Files:", "RemoveFiles"), 
      };

      pPrefs.put("Panels|Node Files|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Apply the changes to the working version.", 
	 "NodeLinksApplyChanges", "Apply Changes:", "ApplyChanges"), 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("Edit primary file sequences of the current node.",
	 "NodeLinksEdit", "Edit:", "Edit"), 

	new DuplicateHotKeyPref
	("Edit primary file sequence of the current node using the default" + 
	 "editor for the file type.",
	 "NodeLinksEditWithDefault", "Edit With Default:", "EditWithDefault"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node.",
	 "NodeLinksQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node with special job requirements.",
	 "NodeLinksQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"),  

	new DuplicateHotKeyPref
	("Pause all jobs associated with the current node.",
	 "NodeLinksPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all jobs associated with the current node.",
	 "NodeLinksResumeJobs", "Resume Jobs:", "ResumeJobs"), 

	new DuplicateHotKeyPref
	("Preempt all jobs associated with the current node.",
	 "NodeLinksPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all jobs associated with the current node.",
	 "NodeLinksKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Remove all the primary/secondary files associated with the selected node.",
	 "NodeLinksRemoveFiles", "Remove Files:", "RemoveFiles"), 
      };

      pPrefs.put("Panels|Node Links|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {	
	new DuplicateHotKeyPref
	("Edit primary file sequences of the current node.",
	 "NodeHistoryEdit", "Edit:", "Edit"), 

	new DuplicateHotKeyPref
	("Edit primary file sequence of the current node using the default" + 
	 "editor for the file type.",
	 "NodeHistoryEditWithDefault", "Edit With Default:", "EditWithDefault"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node.",
	 "NodeHistoryQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Submit jobs to the queue for the current node with special job requirements.",
	 "NodeHistoryQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"),  

	new DuplicateHotKeyPref
	("Pause all jobs associated with the current node.",
	 "NodeHistoryPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all jobs associated with the current node.",
	 "NodeHistoryResumeJobs", "Resume Jobs:", "ResumeJobs"), 

	new DuplicateHotKeyPref
	("Preempt all jobs associated with the current node.",
	 "NodeHistoryPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all jobs associated with the current node.",
	 "NodeHistoryKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Remove all the primary/secondary files associated with the selected node.",
	 "NodeHistoryRemoveFiles", "Remove Files:", "RemoveFiles"), 
      };

      pPrefs.put("Panels|Node History|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Update the job servers, slots and groups.", 
	 "JobBrowserUpdate", "Update:", "Update"), 
	
	new BasePref(),

	new HotKeyPref
	("Show the resource usage history for the selected servers.", 
	 "JobBrowserHostsHistory", "History:"), 

	new DuplicateHotKeyPref
	("Apply the changes to job server properties.", 
	 "JobBrowserHostsApply", "Apply Changes:", "ApplyChanges"), 
	
	new HotKeyPref
	("Add a new job server.", 
	 "JobBrowserHostsAdd", "Add Server:"), 

	new HotKeyPref
	("Remove the selected job servers.", 
	 "JobBrowserHostsRemove", "Remove Server:"), 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("Preempt the jobs running on the selected job server slots.", 
	 "JobBrowserSlotsPreemptJobs", "Preempt Slot Jobs:", "PreemptJobs"), 
	
	new DuplicateHotKeyPref
	("Kill the jobs running on the selected job server slots.", 
	 "JobBrowserSlotsKillJobs", "Kill Slot Jobs:", "KillJobs"), 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("View the target files of the selected job server slot.", 
	 "JobBrowserSlotsView", "View:", "Edit"), 

	new DuplicateHotKeyPref
	("View the target files of the selected job server slot using the default" + 
	 "editor for the file type.", 
	 "JobBrowserSlotsViewWithDefault", "View With Default:", "EditWithDefault"), 

	new BasePref(),

	new HotKeyPref
	("Toggle the view filter between single view, owned views and all views.", 
	 "JobBrowserToggleViewsFilter", "Toggle Views Filter:"), 
	
	new HotKeyPref
	("Show only job groups owned by the current view.", 
	 "JobBrowserSingleViewFilter", "Single View Filter:"), 
	
	new HotKeyPref
	("Show job groups from any view owned by the current user.", 
	 "JobBrowserOwnedViewsFilter", "Single View Filter:"), 
	
	new HotKeyPref
	("Show job groups from all views.", 
	 "JobBrowserAllViewsFilter", "Single View Filter:"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Resubmit aborted and failed jobs to the queue for the selected groups.",
	 "JobBrowserGroupsQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Resubmit aborted and failed jobs to the queue for the selected groups with " + 
	 "special job requirements.",
	 "JobBrowserGroupsQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"), 

	new DuplicateHotKeyPref
	("Pause all jobs associated with the selected groups.",
	 "JobBrowserGroupsPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all jobs associated with the selected groups.",
	 "JobBrowserGroupsResumeJobs", "Resume Jobs:", "ResumeJobs"), 

	new DuplicateHotKeyPref
	("Preempt all jobs associated with the selected groups.",
	 "JobBrowserGroupsPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all jobs associated with the selected groups.",
	 "JobBrowserGroupsKillJobs", "Kill Jobs:", "KillJobs"), 

	new HotKeyPref
	("Delete the selected completed job groups.",
	 "JobBrowserGroupsDelete", "Delete Groups:",
	 true, false, false, 8),  /* SHIFT + Backspace */ 

	new HotKeyPref
	("Delete all completed job groups.", 
	 "JobBrowserGroupsDeleteCompleted", "Delete Completed:")
      };

      pPrefs.put("Panels|Job Browser|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Update connected job details panels.",
	 "JobDetails", "Details:", "Details"), 
      
	new DuplicateHotKeyPref
	("View the target files of the primary selected job.", 
	 "JobView", "View:", "Edit"), 

	new DuplicateHotKeyPref
	("View the target files of the primary selected job using the default" + 
	 "editor for the file type.", 
	 "JobViewWithDefault", "View With Default:", "EditWithDefault"), 

	new BasePref(),
	
	new DuplicateHotKeyPref
	("Resubmit all aborted and failed selected jobs.",
	 "JobQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Resubmit all aborted and failed selected with special job requirements.",
	 "JobQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"), 

	new DuplicateHotKeyPref
	("Pause all selected jobs.",
	 "JobPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all selected jobs.",
	 "JobResumeJobs", "Resume Jobs:", "ResumeJobs"), 
      
	new DuplicateHotKeyPref
	("Preempt all selected jobs.", 
	 "JobPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all selected jobs.", 
	 "JobKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new HotKeyPref
	("Show the node which created the primary selected job in the Node Viewer.", 
	 "ShowNode", "Show Node:"), 
      };      

      pPrefs.put("Panels|Job Viewer|Job|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {	
	new DuplicateHotKeyPref
	("View the target files of the primary selected job group.", 
	 "JobGroupView", "View:", "Edit"), 

	new DuplicateHotKeyPref
	("View the target files of the primary selected job group using the default" + 
	 "editor for the file type.",
	 "JobGroupViewWithDefault", "View With Default:", "EditWithDefault"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Resubmit all aborted and failed selected jobs.",
	 "JobGroupQueueJobs", "Queue Jobs:", "QueueJobs"), 

	new DuplicateHotKeyPref
	("Resubmit all aborted and failed selected with special job requirements.",
	 "JobGroupQueueJobsSpecial", "Queue Jobs Special:", "QueueJobsSpecial"), 

	new DuplicateHotKeyPref
	("Pause all selected jobs.",
	 "JobGroupPauseJobs", "Pause Jobs:", "PauseJobs"), 

	new DuplicateHotKeyPref
	("Resume execution of all selected jobs.",
	 "JobGroupResumeJobs", "Resume Jobs:", "ResumeJobs"), 
      
	new DuplicateHotKeyPref
	("Preempt all selected jobs.", 
	 "JobGroupPreemptJobs", "Preempt Jobs:", "PreemptJobs"), 

	new DuplicateHotKeyPref
	("Kill all selected jobs.", 
	 "JobGroupKillJobs", "Kill Jobs:", "KillJobs"), 

	new BasePref(),

	new HotKeyPref
	("Delete the completed job groups.",
	 "DeleteJobGroups", "Delete Groups:",
	 true, false, false, 8),  /* SHIFT + Backspace */ 

	new BasePref(),

	new DuplicateHotKeyPref
	("Show the node which created the primary selected job group in the Node Viewer.", 
	 "GroupShowNode", "Show Node:", "ShowNode"), 
      };      

      pPrefs.put("Panels|Job Viewer|Job Group|Hot Keys", prefs);
    }

    {
      LinkedList<String> orient = new LinkedList<String>();
      orient.add("Horizontal");
      orient.add("Vertical");

      BasePref prefs[] = {
	new ChoicePref
	("The orientation of job group layout.", 
	 "JobViewerOrientation", "Orientation:", orient, "Horizontal"),
	
	new BoundedDoublePref
	("The distance between job groups.",
	 "JobGroupSpace", "Group Space:", 0.15, 3.0, 0.3)
      };

      pPrefs.put("Panels|Job Viewer|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Update the status of all jobs.", 
	 "JobViewerUpdate", "Update Jobs:", "Update"), 
	
	new BasePref(),
	
	new DuplicateHotKeyPref
	("Move the camera to frame the bounds of the currently selected jobs.",
	 "JobViewerFrameSelection", "Frame Selection:", "FrameSelection"), 
	
	new DuplicateHotKeyPref
	("Move the camera to frame all active jobs.",
	 "JobViewerFrameAll", "Frame All:", "FrameAll"), 
	
	new BasePref(),

	new DuplicateHotKeyPref
	("Automatically expand the first occurance of a job.",
	 "JobViewerAutomaticExpand", "Automatic Expand:", "AutomaticExpand"), 

	new DuplicateHotKeyPref
	("Expand all jobs.",
	 "JobViewerExpandAll", "Expand All:", "ExpandAll"), 

	new DuplicateHotKeyPref
	("Collapse all jobs.",
	 "JobViewerCollapseAll", "Collapse All:", "CollapseAll"), 

	new BasePref(),

	new DuplicateHotKeyPref
	("Expand 1 level of jobs.",
	 "JobViewerExpand1Level", "Expand 1 Level:", "Expand1Level"), 

	new DuplicateHotKeyPref
	("Expand the first 2 levels of jobs.",
	 "JobViewerExpand2Levels", "Expand 2 Levels:", "Expand2Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 3 levels of jobs.",
	 "JobViewerExpand3Levels", "Expand 3 Levels:", "Expand3Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 4 levels of jobs.",
	 "JobViewerExpand4Levels", "Expand 4 Levels:", "Expand4Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 5 levels of jobs.",
	 "JobViewerExpand5Levels", "Expand 5 Levels:", "Expand5Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 6 levels of jobs.",
	 "JobViewerExpand6Levels", "Expand 6 Levels:", "Expand6Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 7 levels of jobs.",
	 "JobViewerExpand7Levels", "Expand 7 Levels:", "Expand7Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 8 levels of jobs.",
	 "JobViewerExpand8Levels", "Expand 8 Levels:", "Expand8Levels"), 

	new DuplicateHotKeyPref
	("Expand the first 9 levels of jobs.",
	 "JobViewerExpand9Levels", "Expand 9 Levels:", "Expand9Levels"), 
	
      };

      pPrefs.put("Panels|Job Viewer|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Show the execution details dialog.", 
	 "ShowExecDetails", "Show Execution Details:",
	 false, false, false, 68),  /* D */
	
	new BasePref(),

	new HotKeyPref
	("Show the job output dialog.", 
	 "ShowJobOutput", "Show Job Output",
	 false, false, false, 79),  /* O */
	
	new HotKeyPref
	("Show the job errors dialog.",
	 "ShowJobErrors", "Show Job Errors",
	 false, false, false, 69)  /* E */
      };

      pPrefs.put("Panels|Job Details|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new DuplicateHotKeyPref
	("Update the resource usage history graphs.", 
	 "ResourceUsageHistoryUpdate", "Update History:", "Update"), 
	
	new BasePref(),

	new DuplicateHotKeyPref
	("Move the camera to frame all resource usage history graphs.",
	 "ResourceUsageFrameAll", "Frame All:", "FrameAll"), 

	new BasePref(),

	new HotKeyPref
	("Toggle the display of the system load graphs.",
	 "ToggleSystemLoad", "Show/Hide System Load"),

	new HotKeyPref
	("Toggle the display of the free memory graphs.",
	 "ToggleFreeMemory", "Show/Hide Free Memory"),

	new HotKeyPref
	("Toggle the display of the free temporary disk space graphs.",
	 "ToggleFreeDisk", "Show/Hide Free Disk"),

	new HotKeyPref
	("Toggle the display of the job count graphs.",
	 "ToggleJobCount", "Show/Hide Job Count"),

	new BasePref(),

	new HotKeyPref
	("Toggle the display of the full load bar.",
	 "ToggleFullLoadBar", "Show/Hide Full Load Bar"),

	new HotKeyPref
	("Toggle the display of the job slots level bar.", 
	 "ToggleJobSlotsBar", "Show/Hide Job Slots Bar")
      };

      pPrefs.put("Dialogs|Resource Usage History|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new ColorPref
	("The color used to render the system load background.", 
	 "SystemLoadBgColor", "System Load Background:", new Color(0.0f, 0.27f, 0.45f)), 
	
	new ColorPref
	("The color used to render system load sample values.", 
	 "SystemLoadFgColor", "System Load Foreground:", new Color(0.0f, 0.59f, 1.0f)),
	
	new ColorPref
	("The color used to render system load sample values which are larger than the " + 
	 "maximum value which can be shown.", 
	 "SystemLoadOverflowColor", "System Load Overflow:", new Color(0.5f, 0.79f, 1.0f)), 
	
	new BoundedIntegerPref
	("The range of system load values displayed.", 
	 "SystemLoadRange", "System Load Range:", 1, 15, 3), 

	new ColorPref
	("The color used to mark the level of full processor utilization.", 
	 "FullLoadColor", "Full Load Color:", Color.yellow),
	
	new BooleanPref
	("Whether to initially render the full load level bar.", 
	 "ShowFullLoadBar", "Show Full Load Bar:", true),

	new BasePref(),

	new ColorPref
	("The color used to render the free memory background.", 
	 "FreeMemoryBgColor", "Free Mem Background:", new Color(0.0f, 0.27f, 0.45f)), 
	
	new ColorPref
	("The color used to render free memory sample values.", 
	 "FreeMemoryFgColor", "Free Mem Foreground:", new Color(0.0f, 0.59f, 1.0f)),
	
	new BasePref(),
	
	new ColorPref
	("The color used to render the free disk background.", 
	 "FreeDiskBgColor", "Free Disk Background:", new Color(0.0f, 0.27f, 0.45f)), 
	
	new ColorPref
	("The color used to render free disk sample values.", 
	 "FreeDiskFgColor", "Free Disk Foreground:", new Color(0.0f, 0.59f, 1.0f)),
	
	new BasePref(),
	
	new ColorPref
	("The color used to render the job count background.", 
	 "JobCountBgColor", "Job Count Background:", new Color(0.0f, 0.43f, 0.0f)), 
	
	new ColorPref
	("The color used to render job count sample values.", 
	 "JobCountFgColor", "Job Count Foreground:", new Color(0.0f, 1.0f, 0.0f)),
	
	new ColorPref
	("The color used to render job count sample values which are larger than the " + 
	 "maximum value which can be shown.", 
	 "JobCountOverflowColor", "Job Count Overflow:", new Color(0.5f, 1.0f, 0.5f)), 
	
	new BoundedIntegerPref
	("The range of job count values displayed.", 
	 "JobCountRange", "Job Count Range:", 1, 10, 3), 

	new ColorPref
	("The color used to mark the current number of job slots.", 
	 "JobSlotsColor", "Job Slots Color:", Color.yellow), 
	
	new BooleanPref
	("Whether to initially render the job slots level bar.", 
	 "ShowJobSlotsBar", "Show Job Slots Bar:", true),
      };

      pPrefs.put("Dialogs|Resource Usage History|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Add a new user.", 
	 "PrivilegeUsersAdd", "Add User:"), 

	new HotKeyPref
	("Remove the selected users.", 
	 "PrivilegeUsersRemove", "Remove User:"), 

	new BasePref(),

	new HotKeyPref
	("Add a new work group.", 
	 "WorkGroupsAdd", "Add Work Group:"), 
      };

      pPrefs.put("Dialogs|Privileges|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("Add a new selection key.", 
	 "SelectionKeysAdd", "Add Key:"), 

	new HotKeyPref
	("Remove the selected selection keys.", 
	 "SelectionKeysRemove", "Remove Key:"), 

	new BasePref(),

	new HotKeyPref
	("Add a new selection group.", 
	 "SelectionGroupsAdd", "Add Group:"), 

	new HotKeyPref
	("Add a new selection group which is a copy of the selected group.", 
	 "SelectionGroupsClone", "Add Group:"), 

	new HotKeyPref
	("Remove the selected selection groups.", 
	 "SelectionGroupsRemove", "Remove Group:"), 

	new BasePref(),

	new HotKeyPref
	("Add a new selection schedule.", 
	 "SelectionSchedulesAdd", "Add Schedule:"), 

	new HotKeyPref
	("Add a new selection schedule which is a copy of the selected schedule.", 
	 "SelectionSchedulesClone", "Clone Schedule:"), 

	new HotKeyPref
	("Remove the selected selection schedules.", 
	 "SelectionSchedulesRemove", "Remove Schedule:"), 

	new BasePref(),

	new HotKeyPref
	("Add a new selection schedule rule.", 
	 "SelectionRulesAdd", "Add Rule:"), 

	new HotKeyPref
	("Add a new selection schedule rule which is a copy of the selected rule.", 
	 "SelectionRulesClone", "Clone Rule:"), 

	new HotKeyPref
	("Remove the selected selection schedule rules.", 
	 "SelectionRulesRemove", "Remove Rule:"), 
      };

      pPrefs.put("Dialogs|Selection Keys|Hot Keys", prefs);
    }

    /* panel ordering */ 
    {
      pPrefPanels = new LinkedList<String>();

      pPrefPanels.add("Main Menu|General");
      pPrefPanels.add("Main Menu|Tool Tips");
      pPrefPanels.add("Main Menu|Hot Keys");
      pPrefPanels.add("Main Menu|Admin|Hot Keys");
      pPrefPanels.add("Main Menu|Help|Hot Keys");
      pPrefPanels.add("Main Menu|Panel Group|Hot Keys");
      pPrefPanels.add("Main Menu|Panel Window|Hot Keys");
      pPrefPanels.add("Main Menu|Panel Type|Hot Keys");
      pPrefPanels.add("Main Menu|Panel Layout|Hot Keys");

      pPrefPanels.add("Panels|Appearance");
      pPrefPanels.add("Panels|Hot Keys");
      pPrefPanels.add("Panels|Node Browser|Hot Keys");

      pPrefPanels.add("Panels|Node Viewer|Appearance");
      pPrefPanels.add("Panels|Node Viewer|Hot Keys");
      pPrefPanels.add("Panels|Node Viewer|Node|Appearance");
      pPrefPanels.add("Panels|Node Viewer|Node|Hot Keys");
      pPrefPanels.add("Panels|Node Viewer|Links|Appearance");
      pPrefPanels.add("Panels|Node Viewer|Links|Hot Keys");

      pPrefPanels.add("Panels|Node Details|Hot Keys");
      pPrefPanels.add("Panels|Node Files|Hot Keys");
      pPrefPanels.add("Panels|Node Links|Hot Keys");
      pPrefPanels.add("Panels|Node History|Hot Keys");

      pPrefPanels.add("Panels|Job Browser|Hot Keys");
      pPrefPanels.add("Panels|Job Viewer|Job|Hot Keys");

      pPrefPanels.add("Panels|Job Viewer|Job Group|Hot Keys");
      pPrefPanels.add("Panels|Job Viewer|Appearance");
      pPrefPanels.add("Panels|Job Viewer|Hot Keys");
      pPrefPanels.add("Panels|Job Details|Hot Keys");

      pPrefPanels.add("Dialogs|Resource Usage History|Appearance");
      pPrefPanels.add("Dialogs|Resource Usage History|Hot Keys");

      pPrefPanels.add("Dialogs|Privileges|Hot Keys");
      pPrefPanels.add("Dialogs|Selection Keys|Hot Keys");
    }

    /* hot key groups */ 
    {
      TreeSet<String> manager = new TreeSet<String>();
      {
	/* windows */
	manager.add("ManagerNodeBrowserWindow");
	manager.add("ManagerNodeViewerWindow");
	manager.add("ManagerNodeDetailsWindow");
	manager.add("ManagerNodeFilesWindow");
	manager.add("ManagerNodeLinksWindow");
	manager.add("ManagerNodeHistoryWindow");
	manager.add("ManagerJobBrowserWindow");
	manager.add("ManagerJobViewerWindow");
	manager.add("ManagerJobDetailsWindow");
	manager.add("ManagerEmptyWindow");
	
	manager.add("ManagerRenameWindow");

	/* panels */ 
	manager.add("ManagerNodeBrowserPanel");
	manager.add("ManagerNodeViewerPanel");
	manager.add("ManagerNodeDetailsPanel");
	manager.add("ManagerNodeFilesPanel");
	manager.add("ManagerNodeLinksPanel");
	manager.add("ManagerNodeHistoryPanel");
	manager.add("ManagerJobBrowserPanel");
	manager.add("ManagerJobViewerPanel");
	manager.add("ManagerJobDetailsPanel");
	manager.add("ManagerEmptyPanel");
	
	/* layout */ 
	manager.add("ManagerAddLeft");
	manager.add("ManagerAddRight");
	manager.add("ManagerAddAbove");
	manager.add("ManagerAddBelow");
	manager.add("ManagerAddTab");
	manager.add("ManagerClosePanel");
	
	/* owner|view */      
	manager.add("ManagerChangeOwnerView");
	
	/* panel manager */ 
	manager.add("ManagerGroup0");
	manager.add("ManagerGroup1");
	manager.add("ManagerGroup2");
	manager.add("ManagerGroup3");
	manager.add("ManagerGroup4");
	manager.add("ManagerGroup5");
	manager.add("ManagerGroup6");
	manager.add("ManagerGroup7");
	manager.add("ManagerGroup8");
	manager.add("ManagerGroup9");
	
	/* UIMaster */ 
	manager.add("SaveLayout");
	manager.add("SaveLayoutAs");
	manager.add("ShowManageLayouts");
	manager.add("ShowUserPrefs");
	manager.add("ShowDefaultEditors");
	manager.add("UpdatePlugins");
	manager.add("ShowManagePrivileges");
	manager.add("ShowManageToolsets");
	manager.add("ShowManageLicenseKeys");
	manager.add("ShowManageSelectionKeys");
	manager.add("ShowManageServerExtensions");
	manager.add("Quit");
      
	/* help */ 
	manager.add("ShowAbout");
	manager.add("ShowQuickReference");
	manager.add("ShowUserManual");
	manager.add("ShowHomePage");
	manager.add("ShowSupportForums");
	manager.add("ShowBugDatabase");
	manager.add("ShowConfig");
	manager.add("ShowLicenseAgreement");

	/* misc */ 
	manager.add("NextTab");
	manager.add("PrevTab");	
      }
    
      TreeSet<String> jobs = new TreeSet<String>();
      {
	jobs.add("QueueJobs");
	jobs.add("QueueJobsSpecial");
	jobs.add("PauseJobs");
	jobs.add("ResumeJobs");
	jobs.add("PreemptJobs");
	jobs.add("KillJobs");
      }
      
      TreeSet<String> camera = new TreeSet<String>();
      {
	camera.add("FrameSelection");
	camera.add("FrameAll");
	camera.add("AutomaticExpand");
	camera.add("CollapseAll");
	camera.add("ExpandAll");	
	camera.add("Expand1Level");	
	camera.add("Expand2Levels");	
	camera.add("Expand3Levels");	
	camera.add("Expand4Levels");	
	camera.add("Expand5Levels");	
	camera.add("Expand6Levels");	
	camera.add("Expand7Levels");	
	camera.add("Expand8Levels");	
	camera.add("Expand9Levels");	
      }
      
      String details      = "Details";
      String update       = "Update";
      String edit         = "Edit";
      String editDefault  = "EditWithDefault";
      String applyChanges = "ApplyChanges";
      String removeFiles  = "RemoveFiles";
      String showNode     = "ShowNode"; 

      pHotKeyGroups = new TreeMap<String,TreeSet<String>>();

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeBrowser", group);

	group.addAll(manager);
	group.add(update);
	group.add("NodeBrowserNodeFilter");
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeViewerPanel", group);
      
	group.addAll(manager);
	group.add("NodeViewerRegisterNewNode");
	group.add(update);
	group.addAll(camera);
	group.add("NodeViewerShowHideDownstreamNodes");
	group.add("NodeViewerRemoveAllRoots");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeViewerNode", group);

	group.addAll(manager);
	group.add(details);
	group.add("NodeViewerMakeRoot");
	group.add("NodeViewerAddRoot");
	group.add("NodeViewerReplaceRoot");
	group.add("NodeViewerRemoveRoot");
	group.add(edit);
	group.add(editDefault);
	group.add("NodeViewerLink");
	group.add("NodeViewerUnlink");
	group.add("NodeViewerAddSecondary");
	group.addAll(jobs);
	group.add("NodeViewerCheckIn");
	group.add("NodeViewerCheckOut");
	group.add("NodeViewerLock");
	group.add("NodeViewerEvolve");
	group.add("NodeViewerClone");
	group.add("NodeViewerRelease");
	group.add("NodeViewerReleaseView");
	group.add(removeFiles);
	group.add("NodeViewerExport");
	group.add("NodeViewerRename");
	group.add("NodeViewerRenumber");
	group.add("NodeViewerDelete");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeViewerLink", group);

	group.addAll(manager);
	group.add("NodeViewerLinkEdit");
	group.add("NodeViewerLinkUnlink");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeDetails", group);
      
	group.addAll(manager);
	group.add(applyChanges);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add(removeFiles);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeFiles", group);
      
	group.addAll(manager);
	group.add(applyChanges);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add(removeFiles);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeLinks", group);
      
	group.addAll(manager);
	group.add(applyChanges);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add(removeFiles);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("NodeHistory", group);
      
	group.addAll(manager);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add(removeFiles);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobBrowserServer", group);
      
	group.addAll(manager);
	group.add(update);
	group.add("JobBrowserHostsHistory");
	group.add(applyChanges);
	group.add("JobBrowserHostsAdd");
	group.add("JobBrowserHostsRemove");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobBrowserSlot", group);
      
	group.addAll(manager);
	group.add(update);
	group.add("JobBrowserSlotsView");
	group.add("JobBrowserSlotsViewWithDefault");
	group.add("JobBrowserSlotsPreemptJobs");
	group.add("JobBrowserSlotsKillJobs");
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobBrowserGroup", group);
      
	group.addAll(manager);
	group.add(update);
	group.add("JobBrowserToggleViewsFilter");
	group.add("JobBrowserSingleViewFilter");
	group.add("JobBrowserOwnedViewsFilter");
	group.add("JobBrowserAllViewsFilter");
	group.addAll(jobs);
	group.add("JobBrowserGroupsDelete");
	group.add("JobBrowserGroupsDeleteCompleted");
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobViewerPanel", group);
      
	group.addAll(manager);
	group.add(update);
	group.addAll(camera);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobViewerJob", group);
      
	group.addAll(manager);
	group.add(details);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add(showNode);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobViewerGroup", group);
      
	group.addAll(manager);
	group.add(edit);
	group.add(editDefault);
	group.addAll(jobs);
	group.add("DeleteJobGroups");
	group.add(showNode);
      }
    
      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("JobDetails", group);
      
	group.addAll(manager);
	group.add("ShowExecDetails");
	group.add("ShowJobOutput");
	group.add("ShowJobErrors");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("ResourceUsageHistory", group);
      
	group.add("Update");
	group.add("FrameAll");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("Privileges", group);
      
	group.add("PrivilegeUsersAdd");
	group.add("PrivilegeUsersRemove");

	group.add("WorkGroupsAdd");
      }

      {
	TreeSet<String> group = new TreeSet<String>();
	pHotKeyGroups.put("SelectionKeys", group);
      
	group.add("SelectionKeysAdd");
	group.add("SelectionKeysRemove");

	group.add("SelectionGroupsAdd");
	group.add("SelectionGroupsClone");
	group.add("SelectionGroupsRemove");

	group.add("SelectionSchedulesAdd");
	group.add("SelectionSchedulesClone");
	group.add("SelectionSchedulesRemove");

	group.add("SelectionRulesAdd");
	group.add("SelectionRulesClone");
	group.add("SelectionRulesRemove");
      }
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function.
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    GenUserPrefsApp app = new GenUserPrefsApp();
    app.generateUserPrefsClass(new File("ui/core/UserPrefs.java"));
    app.generateJUserPrefsDialogClass(new File("ui/core/JUserPrefsDialog.java"));
    app.generateKeyExcludeClasses(new File("ui/core"));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the <CODE>UserPrefs.java</CODE> source file.
   */ 
  public void 
  generateUserPrefsClass
  (
   File file
  ) 
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append
      ("// $Id: GenUserPrefsApp.java,v 1.32 2006/10/11 22:45:41 jim Exp $\n" +
       "\n" + 
       "package us.temerity.pipeline.ui.core;\n" + 
       "\n" + 
       "import us.temerity.pipeline.*;\n" + 
       "import us.temerity.pipeline.core.BaseApp;\n" + 
       "import us.temerity.pipeline.core.LockedGlueFile;\n" + 
       "import us.temerity.pipeline.core.GlueLockException;\n" +
       "import us.temerity.pipeline.math.*;\n" + 
       "import us.temerity.pipeline.ui.*;\n" + 
       "import us.temerity.pipeline.glue.*;\n" + 
       "\n" + 
       "import java.io.*;\n" + 
       "\n" + 
       genHeader("USER PREFS") + 
       "/**\n" + 
       " * The user preferences for various aspects of the user interface.\n" + 
       " */\n" +
       "public\n" + 
       "class UserPrefs\n" + 
       "  implements Glueable\n" + 
       "{\n" + 
       genMinorHeader("CONSTRUCTOR") +
       "\n" + 
       "  /**\n" +
       "   * Construct the sole instance.\n" + 
       "   */\n" + 
       "  public\n" + 
       "  UserPrefs()\n" + 
       "  {\n" + 
       "    reset();\n" + 
       "  }\n" + 
       "\n" +
       "\n" + 
       "\n" + 
       genMinorHeader("ACCESS") +
       "\n" + 
       "  /**\n" + 
       "   * Get the UserPrefs instance.\n" + 
       "   */\n" + 
       "   public static UserPrefs\n" + 
       "   getInstance()\n" + 
       "   {\n" + 
       "     return sUserPrefs;\n" + 
       "   }\n" +
       "\n" +
       "\n");

    /* generate accessors */ 
    for(String group : pPrefs.keySet()) {
      BasePref prefs[] = pPrefs.get(group);
      
      {
	String gtitle = group.replace("|", " - ");
	buf.append("  /*-- " + gtitle + " ");
	int wk;
	for(wk=0; wk<(84 - gtitle.length()); wk++) 
	  buf.append("-");
	buf.append("*/\n\n");
      }

      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genAccessors(buf, 1);

      buf.append("\n");
    }
       
    /* generate reset method */ 
    {
      buf.append
	(genMinorHeader("RESET") +
	 "\n" +
	 "  public void\n" + 
	 "  reset()\n" + 
	 "  {\n");
      
      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genReset(buf, 3);

	buf.append("    }\n\n");
      }
      
      buf.append
	("  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* generate I/O methods */ 
    {
      buf.append
	(genMinorHeader("I/O") +
	 "\n" + 
	 "  /**\n" + 
	 "   * Save the preferences to disk.\n" + 
	 "   */\n" + 
	 "  public static void\n" + 
	 "  save()\n" + 
	 "    throws GlueException, GlueLockException\n" + 
	 "  {\n" + 
	 "    Path path = new Path(PackageInfo.sHomePath,\n" + 
	 "		           PackageInfo.sUser + \"/.pipeline/preferences\");\n" + 
	 "    LockedGlueFile.save(path.toFile(), \"UserPreferences\", sUserPrefs);\n" + 
	 "  }\n" + 
	 "\n" + 
	 "  /**\n" + 
	 "   * Load the preferences from disk.\n" + 
	 "   */\n" + 
	 "  public static void\n" + 
	 "  load()\n" + 
	 "    throws GlueException, GlueLockException\n" + 
	 "  {\n" + 
	 "    Path path = new Path(PackageInfo.sHomePath,\n" + 
	 "		           PackageInfo.sUser + \"/.pipeline/preferences\");\n" + 
	 "    sUserPrefs = (UserPrefs) LockedGlueFile.load(path.toFile());\n" + 
	 "  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* generate glueable methods */ 
    {
      buf.append
	(genMinorHeader("GLUEABLE") +
	 "\n" + 
	 "  public void\n" + 
	 "  toGlue\n" + 
	 "  (\n" + 
	 "    GlueEncoder encoder\n" +    
	 "  )\n" + 
	 "    throws GlueException\n" + 
	 "  {\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genToGlue(buf, 3);

	buf.append("    }\n\n");	
      }

      buf.append
	("  }\n" + 
	 "\n" + 
	 "  public void\n" + 
	 "  fromGlue\n" + 
	 "  (\n" + 
	 "    GlueDecoder decoder\n" +    
	 "  )\n" + 
	 "    throws GlueException\n" + 
	 "  {\n");
	 
      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genFromGlue(buf, 3);

	buf.append("    }\n\n");	
      }

      buf.append
	("  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* static internals */ 
    buf.append
      (genMinorHeader("STATIC INTERNALS") +
       "\n" + 
       "  /**\n" + 
       "   * The sole instance of this class.\n" + 
       "   */\n" + 
       "  private static UserPrefs sUserPrefs = new UserPrefs();\n" + 
       "\n" + 
       "\n" + 
       "\n");
    
    /* internals */ 
    {
      buf.append
	(genMinorHeader("INTERNALS") +
	 "\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	{
	  String gtitle = group.replace("|", " - ");
	  buf.append("  /*-- " + gtitle + " ");
	  int wk;
	  for(wk=0; wk<(84 - gtitle.length()); wk++) 
	    buf.append("-");
	  buf.append("*/\n\n");
	}

	int wk;
	for(wk=0; wk<prefs.length; wk++) {
	  prefs[wk].genDeclare(buf, 1);
	  buf.append("\n");
	}
	
	buf.append("\n");
      }
    }

    buf.append("}\n");


    /* write the file */ 
    try {
      FileWriter out = new FileWriter(file);
      out.write(buf.toString());
      out.close();
    }
    catch(IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the <CODE>UserPrefs.java</CODE> source file.
   */ 
  public void 
  generateJUserPrefsDialogClass
  (
   File file
  ) 
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append
      ("// $Id: GenUserPrefsApp.java,v 1.32 2006/10/11 22:45:41 jim Exp $\n" +
       "\n" + 
       "package us.temerity.pipeline.ui.core;\n" + 
       "\n" + 
       "import us.temerity.pipeline.*;\n" + 
       "import us.temerity.pipeline.ui.*;\n" + 
       "import us.temerity.pipeline.math.*;\n" + 
       "\n" + 
       "import java.awt.*;\n" + 
       "import java.awt.event.*;\n" + 
       "import java.util.*;\n" + 
       "import java.io.*;\n" + 
       "import javax.swing.*;\n" + 
       "import javax.swing.tree.*;\n" + 
       "import javax.swing.event.*;\n" + 
       "\n" + 
       genHeader("USER PREFS DIALOG") + 
       "\n" +
       "/**\n" +
       " * The user preferences dialog.\n" + 
       " */\n" + 
       "public\n" + 
       "class JUserPrefsDialog\n" + 
       "  extends JBaseUserPrefsDialog\n" + 
       "{\n" + 
       genMinorHeader("CONSTRUCTOR") +
       "\n" + 
       "  /**\n" + 
       "   * Construct a new user preferences dialog.\n" + 
       "   */ \n" + 
       "  public \n" + 
       "  JUserPrefsDialog()\n" + 
       "  {\n" + 
       "    super();\n" + 
       "    initUI();\n" + 
       "  }\n" + 
       "\n" +
       "\n" +
       "  " + genBar(92) + 
       "\n" +
       "  /**\n" + 
       "   * Initialize the common user interface components.\n" + 
       "   */\n" + 
       "  protected void\n" + 
       "  initUI()\n" + 
       "  {\n" + 
       "    super.initUI();\n" + 
       "\n");
    
    for(String group : pPrefPanels) {
      String gtitle = group.replace("|", " - ");

      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n" +
	 "      createTreeNodes(\"" + group + "\");\n" + 
	 "\n" + 
	 "      Component comps[] = UIFactory.createTitledPanels();\n" + 
	 "      JPanel tpanel = (JPanel) comps[0];\n" +
	 "      JPanel vpanel = (JPanel) comps[1];\n" + 
	 "\n");
	 
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genUI(buf, 3, wk==(prefs.length-1));
      
      buf.append
	("      UIFactory.addVerticalGlue(tpanel, vpanel);\n" + 
	 "\n" + 
	 "      pCardPanel.add(comps[2], \"" + gtitle + "\");\n" + 
	 "    }\n" +
	 "\n");
    }

    {
      buf.append
	("    /* hot key aliases */\n" + 
	 "    {\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genKeyGroupDeclare(buf, 3);
      }
      
      buf.append("\n");
      
      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genKeyGroupAdd(buf, 3);
      }
      
      buf.append
	("    }\n\n");
    }

    {
      buf.append
	("    /* hot key exlusion groups */\n" + 
	 "    {\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genKeyExcludeAdd(buf, 3);
      }

      buf.append
	("    }\n\n");
    }

    buf.append
      ("    /* expand all tree nodes */\n" + 
       "    {\n" + 
       "      int wk;\n" + 
       "      for(wk=0; wk<pTree.getRowCount(); wk++)\n" + 
       "        pTree.expandRow(wk);\n" + 
       "    }\n" + 
       "  }\n" + 
       "\n" + 
       "\n" +
       "\n" +
       genMinorHeader("ACCESS") +
       "\n" + 
       "  /**\n" + 
       "    * Set the user preferences from the current UI settings and save the prefs.\n" + 
       "    */ \n" + 
       "  public void \n" + 
       "  savePrefs()\n" + 
       "  {\n" + 
       "    UIMaster master = UIMaster.getInstance();\n" +
       "    UserPrefs prefs = UserPrefs.getInstance();\n" + 
       "\n");
  
    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");

      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n");

      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genSavePrefs(buf, 3);

      buf.append
	("    }\n" + 
	 "\n");
    }

    buf.append
      ("    try {\n" +
       "      UserPrefs.save();\n" + 
       "    }\n" +     
       "    catch(Exception ex) {\n" + 
       "      showErrorDialog(ex);\n" + 
       "      return;\n" + 
       "    }\n" +
       "\n" + 
       "    master.updateUserPrefs();\n" + 
       "  }\n" + 
       "\n" + 
       "  /**\n" + 
       "   * Update the current UI settings from the user preferences.\n" + 
       "   */ \n" + 
       "  public void \n" + 
       "  updatePrefs()\n" + 
       "  {\n" + 
       "    UserPrefs prefs = UserPrefs.getInstance();\n" +
       "\n");

    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");
      
      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n");

      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genUpdatePrefs(buf, 3);

      buf.append
	("    }\n" + 
	 "\n");
    }

    buf.append                                                               
      ("  }\n" +
       "\n" +  
       "\n" +
       "\n" +
       genMinorHeader("HOT KEY FIELD ACCESSORS") +
       "\n");
       
    for(String group : pPrefs.keySet()) {
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genKeyGetter(buf, 1);
    }

    buf.append 
      ("\n" + 
       "\n" +
       "\n" +
       genMinorHeader("STATIC INTERNALS") +
       "\n" +
       "  private static final long serialVersionUID = -7086626876853073990L;\n" + 
       "\n" + 
       "\n" + 
       "\n" + 
       genMinorHeader("INTERNALS") +
       "\n");
    
    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");
      
      buf.append
	("  /**\n" +
	 "   * " + gtitle + "\n" + 
	 "   */\n");
      
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genDeclareUI(buf, 1);
      
      buf.append
	("\n");
    }
	 
    buf.append
      ("}\n");

    /* write the file */ 
    try {
      FileWriter out = new FileWriter(file);
      out.write(buf.toString());
      out.close();
    }
    catch(IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }


  private void 
  generateKeyExcludeClasses
  (
   File dir 
  )
  {
    for(String group : pPrefs.keySet()) {
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].generateExcludeClass(dir, group);
    }
  }
    

  /*----------------------------------------------------------------------------------------*/

  private String 
  genHeader
  (
   String title
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append(genBar(94) + 
	       "/*  ");
    
    char cs[] = title.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) 
      buf.append(" " + cs[wk]);
    for(wk=0; wk<(88 - cs.length*2); wk++) 
      buf.append(" ");
    buf.append("*/\n");

    buf.append(genBar(94));

    return (buf.toString());
  }

  private String 
  genMinorHeader
  (
   String title
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append("  " + genBar(92) + 
	       "  /*  ");
    
    char cs[] = title.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) 
      buf.append(" " + cs[wk]);
    for(wk=0; wk<(86 - cs.length*2); wk++) 
      buf.append(" ");
    buf.append("*/\n");

    buf.append("  " + genBar(92));

    return (buf.toString());
  }

  private String 
  genBar
  (
   int size
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append("/*");
    int wk;
    for(wk=0; wk<size-4; wk++) 
      buf.append("-");
    buf.append("*/\n");
    
    return (buf.toString());
  }
  
  private String 
  indent
  (
   int level
  ) 
  {
    StringBuffer buf = new StringBuffer();
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    return buf.toString();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Base class of all preferences.
   */ 
  private 
  class BasePref
  {
    public 
    BasePref()
    {}

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {} 
      
    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genToGlue
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void
    genFromGlue
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genDeclare
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      if(!isLast) 
	buf.append(indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 9);\n\n");
    } 

    public void 
    genKeyGroupDeclare
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genKeyGroupAdd
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genKeyExcludeAdd
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    generateExcludeClass
    (
     File dir,
     String pname
    )
    {} 

    public void 
    genKeyGetter
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {} 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {} 
  }

  /**
   * 
   */ 
  private abstract
  class ValuedPref
    extends BasePref
  {
    public 
    ValuedPref
    (
     String desc, 
     String title, 
     String label, 
     String atype, 
     String gtype
    ) 
    {
      pDesc       = desc;
      pTitle      = title;
      pLabel      = label;
      pAtomicType = atype;
      pGlueType   = gtype;
    }

    public String
    getTitle()
    {
      return pTitle;
    }

    public String
    getLabel()
    {
      return pLabel;
    }

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * Get " + pDesc + "\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public " + pAtomicType + "\n" +
	 indent(level) + "get" + pTitle + "()\n" +
	 indent(level) + "{\n" + 
	 indent(level+1) + "return p" + pTitle + ";\n" + 
	 indent(level) + "}\n" + 
	 "\n" +
	 indent(level) + "/**\n" +
	 indent(level) + " * Set " + pDesc + "\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public void\n" +
	 indent(level) + "set" + pTitle + "\n" + 
	 indent(level) + "(\n" + 
	 indent(level) + " " + pAtomicType + " v\n" +
	 indent(level) + ")\n" + 
	 indent(level) + "{\n" + 
	 indent(level+1) + "p" + pTitle + " = v;\n" + 
	 indent(level) + "}\n" +
	 "\n" + 
	 "\n");
    }
      
    public void 
    genToGlue
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "encoder.encode(\"" + pTitle + "\", p" + pTitle + ");\n");
    }

    public void
    genFromGlue
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "{\n" + 
	 indent(level+1) + pGlueType + " v = (" + pGlueType + ") " + 
	                   "decoder.decode(\"" + pTitle + "\");\n" + 
	 indent(level+1) + "if(v != null)\n" + 
	 indent(level+2) + "p" + pTitle + " = v;\n" +
	 indent(level) + "}\n");
    }

    public void 
    genDeclare
    (
     StringBuffer buf,
     int level
    ) 
    {
      char cs[] = pDesc.toCharArray();
      cs[0] = Character.toUpperCase(cs[0]);
      String desc = new String(cs);

      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * " + desc + ".\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "private " + pAtomicType + "  p" + pTitle + ";\n");	 
    }

    protected String  pDesc; 
    protected String  pTitle;
    protected String  pLabel;
    protected String  pAtomicType;
    protected String  pGlueType;
  }

  /**
   * Boolean preference.
   */ 
  private 
  class BooleanPref
    extends ValuedPref
  {
    public 
    BooleanPref
    (
     String desc, 
     String title, 
     String label, 
     boolean defaultValue
    ) 
    {
      super(desc, title, label, "boolean", "Boolean");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIFactory.createTitledBooleanField\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize, vpanel, sVSize,\n" + 
	 indent(level+2) + " \"" + pDesc + "\");\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getValue());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JBooleanField  p" + pTitle + ";\n");
    } 

    protected boolean pDefaultValue;
  }

  /**
   * Unbounded Integer preference.
   */ 
  private 
  class IntegerPref
    extends ValuedPref
  {
    public 
    IntegerPref
    (
     String desc, 
     String title, 
     String label, 
     int defaultValue
    ) 
    {
      super(desc, title, label, "int", "Integer");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " = null;  // Not yet implemented!\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    protected int  pDefaultValue;
  }

  /**
   * Bounded Integer preference.
   */ 
  private 
  class BoundedIntegerPref
    extends IntegerPref
  {
    public 
    BoundedIntegerPref
    (
     String desc, 
     String title, 
     String label, 
     int minValue, 
     int maxValue, 
     int defaultValue
    ) 
    {
      super(desc, title, label, defaultValue);
      pMinValue = minValue;
      pMaxValue = maxValue;
    }
      
    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIFactory.createTitledSlider\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, " + pMinValue + ", " + pMaxValue + ", sVSize,\n" + 
	 indent(level+2) + " \"" + pDesc + "\");\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getValue());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JSlider  p" + pTitle + ";\n");
    } 

    protected int  pMinValue;      
    protected int  pMaxValue;      
  }

  /**
   * Unbounded Double preference.
   */ 
  private 
  class DoublePref
    extends ValuedPref
  {
    public 
    DoublePref
    (
     String desc, 
     String title, 
     String label, 
     double defaultValue
    ) 
    {
      super(desc, title, label, "double", "Double");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " = null;  // Not yet implemented!\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    protected double  pDefaultValue;
  }

  /**
   * Bounded Double preference.
   */ 
  private 
  class BoundedDoublePref
    extends DoublePref
  {
    public 
    BoundedDoublePref
    (
     String desc, 
     String title, 
     String label, 
     double minValue, 
     double maxValue, 
     double defaultValue
    ) 
    {
      super(desc, title, label, defaultValue);
      pMinValue = minValue;
      pMaxValue = maxValue;
    }
      
    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIFactory.createTitledSlider\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, " + pMinValue + ", " + pMaxValue + ", sVSize,\n" + 
	 indent(level+2) + " \"" + pDesc + "\");\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + 
		 "(((double) p" + pTitle + ".getValue())/1000.0);\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(" + 
		 "(int) (prefs.get" + pTitle + "()*1000.0));\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JSlider  p" + pTitle + ";\n");
    } 

    protected double  pMinValue;      
    protected double  pMaxValue;      
  }

  /**
   * Color preference.
   */ 
  private 
  class ColorPref
    extends ValuedPref
  {
    public 
    ColorPref
    (
     String desc, 
     String title, 
     String label, 
     Color defaultValue
    ) 
    {
      super(desc, title, label, "Color3d", "Color3d");
      pDefaultValue = defaultValue;
    }

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * Get " + pDesc + "\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public " + pAtomicType + "\n" +
	 indent(level) + "get" + pTitle + "()\n" +
	 indent(level) + "{\n" + 
	 indent(level+1) + "return new Color3d(p" + pTitle + ");\n" + 
	 indent(level) + "}\n" + 
	 "\n" +
	 indent(level) + "/**\n" +
	 indent(level) + " * Set " + pDesc + "\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public void\n" +
	 indent(level) + "set" + pTitle + "\n" + 
	 indent(level) + "(\n" + 
	 indent(level) + " " + pAtomicType + " v\n" +
	 indent(level) + ")\n" + 
	 indent(level) + "{\n" + 
	 indent(level+1) + "p" + pTitle + ".set(v);\n" + 
	 indent(level) + "}\n" +
	 "\n" + 
	 "\n");
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      float[] c = pDefaultValue.getColorComponents(null);
      buf.append
	(indent(level) + "p" + pTitle + " = " + 
	 "new Color3d(" + c[0] + ", " + c[1] + ", " + c[2] + ");\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      float[] c = pDefaultValue.getColorComponents(null);
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIFactory.createTitledColorField\n" + 
	 indent(level+2) + "(this,\n" + 
	 indent(level+2) + " tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, " + 
	 "new Color3d(" + c[0] + ", " + c[1] + ", " + c[2] + "), sVSize,\n" + 
	 indent(level+2) + " \"" + pDesc + "\");\n" + 
	 indent(level) + "p" + pTitle + ".setDialogTitle(\"Color Editor:  " + 
	 pLabel.substring(0, pLabel.length()-1) + "\");\n" +
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getValue());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JColorField  p" + pTitle + ";\n");
    } 

    private Color  pDefaultValue;
  }

  /**
   * String choice preference.
   */ 
  private 
  class ChoicePref
    extends ValuedPref
  {
    public 
    ChoicePref
    (
     String desc, 
     String title, 
     String label, 
     Collection<String> values, 
     String defaultValue
    ) 
    {
      super(desc, title, label, "String", "String");
      pValues       = values;
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = \"" + pDefaultValue + "\";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "{\n" + 
	 indent(level+1) + "ArrayList<String> values = new ArrayList<String>();\n");

      for(String v : pValues) 
	buf.append(indent(level+1) + "values.add(\"" + v + "\");\n");

      buf.append
	("\n" + 
	 indent(level+1) + "p" + pTitle + " =\n" + 
	 indent(level+2) + "UIFactory.createTitledCollectionField\n" + 
	 indent(level+3) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+3) + " vpanel, values, sVSize,\n" + 
	 indent(level+3) + " \"" + pDesc + "\");\n" + 
	 indent(level) + "}\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getSelected());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setSelected(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JCollectionField  p" + pTitle + ";\n");
    } 

    protected Collection<String>  pValues;
    protected String              pDefaultValue;
  }

  /**
   * HotKey preference.
   */ 
  private 
  class HotKeyPref
    extends ValuedPref
  {
    public 
    HotKeyPref
    (
     String desc, 
     String title, 
     String label
    ) 
    {
      super(desc, title, label, "HotKey", "HotKey");
    }

    public 
    HotKeyPref
    (
     String desc, 
     String title, 
     String label, 
     boolean shiftDown, 
     boolean altDown, 
     boolean ctrlDown, 
     int keyCode
    ) 
    {
      super(desc, title, label, "HotKey", "HotKey");

      pShiftDown = shiftDown;
      pAltDown   = altDown;
      pCtrlDown  = ctrlDown;
      pKeyCode   = keyCode;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append(indent(level) + "p" + pTitle + " = ");
      if(pShiftDown != null) {	 
	buf.append("new HotKey(" + 
		   pShiftDown + ", " + pAltDown + ", " + pCtrlDown + ", " + pKeyCode + 
		   ");\n");
      }
      else {
	buf.append("null;\n");
      }
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIFactory.createTitledHotKeyField\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, sVSize,\n" + 
	 indent(level+2) + " \"" + pDesc + "\");\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIFactory.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genKeyGroupDeclare
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + "KeyGroup = new HotKeyGroup();\n");
    }

    public void 
    genKeyGroupAdd
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + "KeyGroup.add(p" + pTitle + ");\n");
    } 

    public void 
    genKeyExcludeAdd
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + ".addActionListener" + 
	 "(new " + pTitle + "KeyExclude(this));\n");
    } 

    public void 
    generateExcludeClass
    (
     File dir,
     String pname
    )
    {
      String cname = (pTitle + "KeyExclude");

      StringBuffer buf = new StringBuffer();
      buf.append
	("// $Id: GenUserPrefsApp.java,v 1.32 2006/10/11 22:45:41 jim Exp $\n" +
	 "\n" + 
	 "package us.temerity.pipeline.ui.core;\n" + 
	 "\n" + 
	 "import us.temerity.pipeline.*;\n" + 
	 "import us.temerity.pipeline.ui.*;\n" +
	 "\n" + 
	 "import java.awt.*;\n" + 
	 "import java.awt.event.*;\n" + 
	 "\n" + 
	 genHeader(cname.toUpperCase()) + 
	 "\n" +
	 "/**\n" +
	 " * A hot key exclusion listener.\n" + 
	 " */\n" + 
	 "public\n" +  				     	   
	 "class " + cname + "\n" +
	 "  extends BaseKeyExclude\n" +  		     		   
	 "{\n" +  
	 genMinorHeader("CONSTRUCTOR") +
	 "\n" +
	 "  public\n" +  	 			     		   
	 "  " + cname + "\n" + 
	 "  (\n" + 
	 "    JUserPrefsDialog parent\n" +
	 "  )\n" +  		     
	 "  {\n" +
	 "    pParent = parent;\n" + 
	 "  }\n" +  					     		   
	 "\n" +  
	 "\n" + 
	 "\n" +
	 genMinorHeader("HELPERS") +
	 "\n" +
	 "  protected void\n" +  	 		     		   
	 "  validate()\n" +  				     		   
	 "    throws PipelineException\n" +  		     		   
	 "  {\n" +  					     		   
	 "    HotKey key = pParent.get" + pTitle + "HotKeyField().getHotKey();\n" + 
	 "    if(key == null)\n" +  	 		     		   
	 "      return;\n" +  				     	   
	 "\n");                                              
      
      String label = (pname + "|" + pLabel.substring(0, pLabel.length()-1));

      TreeMap<String,String> excluded = new TreeMap<String,String>();
      for(String key : getExcludedKeys()) {
	String path = null;
	for(String group : pPrefs.keySet()) {
	  BasePref prefs[] = pPrefs.get(group);
	  int wk;
	  for(wk=0; wk<prefs.length; wk++) {
	    if(prefs[wk] instanceof HotKeyPref) {
	      HotKeyPref hkp = (HotKeyPref) prefs[wk];
	      if(hkp.getTitle().equals(key)) {
		String elabel = hkp.getLabel();
		path = (group + "|" + elabel.substring(0, elabel.length()-1));
		break;
	      }
	    }
	  }

	  if(path != null) 
	    break;
	}

	excluded.put(key, path);
      }

      for(String key : excluded.keySet()) {
	buf.append
	  ("    if(key.equals(pParent.get" + key + "HotKeyField().getHotKey()))\n" +  
	   "      conflict(key,\n" + 
	   "               \"" + label + "\",\n" + 
	   "               \"" + excluded.get(key) + "\");\n\n");
      }

      buf.append
	("  }\n" +					     		   
	 "\n" +  
	 "\n" + 
	 "\n" +
	 genMinorHeader("INTERNALS") +
	 "\n" +
	 "  /**\n" + 
	 "   * The user preferences dialog.\n" + 
	 "   */\n" + 
	 "  private JUserPrefsDialog  pParent;\n" + 
	 "\n" +
	 "}\n");

      /* write the file */ 
      try {
	File file = new File(dir, cname + ".java");
	FileWriter out = new FileWriter(file);
	out.write(buf.toString());
	out.close();
      }
      catch(IOException ex) {
	ex.printStackTrace();
	System.exit(1);
      }
    }

    protected TreeSet<String>
    getExcludedKeys() 
    {    
      TreeSet<String> keys = new TreeSet<String>();

      for(String gname : pHotKeyGroups.keySet()) {
	TreeSet<String> group = pHotKeyGroups.get(gname);
	if(group.contains(pTitle)) 
	  keys.addAll(group);
      }
      
      keys.remove(pTitle);

      return keys;
    }	

    public void 
    genKeyGetter
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * Get the JHotKeyField for " + pDesc + "\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public JHotKeyField\n" +
	 indent(level) + "get" + pTitle + "HotKeyField()\n" +
	 indent(level) + "{\n" + 
	 indent(level+1) + "return p" + pTitle + ";\n" + 
	 indent(level) + "}\n" + 
	 "\n");
    }

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getHotKey());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setHotKey(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JHotKeyField  p" + pTitle + ";\n" + 
		 indent(level) + "private HotKeyGroup p" + pTitle + "KeyGroup;\n");
		 
    } 

    protected Boolean  pShiftDown;
    protected Boolean  pAltDown;
    protected Boolean  pCtrlDown;
    protected Integer  pKeyCode;
  }

  /**
   * Duplicate HotKey preference.
   */ 
  private 
  class DuplicateHotKeyPref
    extends HotKeyPref
  {
    public 
    DuplicateHotKeyPref
    (
     String desc, 
     String title, 
     String label, 
     String alias
    ) 
    {
      super(desc, title, label);
      pAlias = alias;
    }

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {}

    public void 
    genToGlue
    (
     StringBuffer buf,
     int level
    ) 
    {}

    public void
    genFromGlue
    (
     StringBuffer buf,
     int level
    ) 
    {}

    public void 
    genDeclare
    (
     StringBuffer buf,
     int level
    ) 
    {}

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genKeyGroupDeclare
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genKeyGroupAdd
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append
	(indent(level) + "p" + pAlias + "KeyGroup.add(p" + pTitle + ");\n");
    } 

    protected TreeSet<String>
    getExcludedKeys() 
    {    
      TreeSet<String> keys = new TreeSet<String>();

      for(String gname : pHotKeyGroups.keySet()) {
	TreeSet<String> group = pHotKeyGroups.get(gname);
	if(group.contains(pAlias)) 
	  keys.addAll(group);
      }

      keys.remove(pAlias);

      return keys;
    }	

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pAlias + "(p" + pTitle + ".getHotKey());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setHotKey(prefs.get" + pAlias + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JHotKeyField  p" + pTitle + ";\n");
		 
    } 

    protected String  pAlias; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of user preferences indexed by grouping name.
   */ 
  private TreeMap<String,BasePref[]>  pPrefs;

  /**
   * The ordering of the preference panels.
   */ 
  private LinkedList<String>  pPrefPanels;

  /**
   * The names of hot keys index by hot key exclusion group name.
   */ 
  private TreeMap<String,TreeSet<String>>  pHotKeyGroups;

}


