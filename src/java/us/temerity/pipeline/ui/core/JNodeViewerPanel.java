// $Id: JNodeViewerPanel.java,v 1.78 2007/04/15 13:37:11 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.math.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   V I E W E R   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of {@link NodeStatus NodeStatus} trees as graphs of state icons connected by
 * lines showing the upstream/downstream connectivity between nodes.
 */ 
public  
class JNodeViewerPanel
  extends JBaseViewerPanel
  implements KeyListener, PopupMenuListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeViewerPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeViewerPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  protected synchronized void 
  initUI()
  {  
    super.initUI(128.0, true);

    /* initialize fields */ 
    UserPrefs prefs = UserPrefs.getInstance();
    {
      pRoots = new TreeMap<String,NodeStatus>();

      pShowDetailHints = prefs.getShowDetailHints();
      pShowDownstream  = prefs.getShowDownstream();

      pViewerNodeHint = 
	new ViewerNodeHint(this, 
			   prefs.getShowToolsetHints(), 
			   prefs.getShowEditorHints(), 
			   prefs.getShowActionHints()); 			   

      pViewerNodes = new TreeMap<NodePath,ViewerNode>();
      pViewerLinks = new ViewerLinks();
      pSelected = new TreeMap<NodePath,ViewerNode>();

      pRemoveSecondarySeqs = new TreeMap<String,FileSeq>();

      pRefreshDefaultToolMenu = true; 
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu();  
      pPanelPopup.addPopupMenuListener(this);

      item = new JMenuItem("Update");
      pUpdateItem = item;
      item.setActionCommand("update");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Register...");
      pRegisterItem = item;
      item.setActionCommand("register");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Release View...");
      pReleaseViewItem = item;
      item.setActionCommand("release-view");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Request Restore...");     
      pPanelRestoreItem = item;
      item.setActionCommand("restore");
      item.addActionListener(this);
      pPanelPopup.add(item);
      
      pPanelPopup.addSeparator();
      
      item = new JMenuItem("Frame Selection");
      pFrameSelectionItem = item;
      item.setActionCommand("frame-selection");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame All");
      pFrameAllItem = item;
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
       
      item = new JMenuItem("Automatic Expand");
      pAutomaticExpandItem = item;
      item.setActionCommand("automatic-expand");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Expand All");
      pExpandAllItem = item;
      item.setActionCommand("expand-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Collapse All");
      pCollapseAllItem = item;
      item.setActionCommand("collapse-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();

      item = new JMenuItem();
      pShowHideDownstreamItem = item;
      item.setActionCommand("show-hide-downstream");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Hide All Roots");
      pRemoveAllRootsItem = item;
      item.setActionCommand("remove-all-roots");
      item.addActionListener(this);
      pPanelPopup.add(item); 

      pPanelPopup.addSeparator();

      item = new JMenuItem();
      pShowHideDetailHintsItem = item;
      item.setActionCommand("show-hide-detail-hints");
      item.addActionListener(this);
      pPanelPopup.add(item);   

      item = new JMenuItem();
      pShowHideToolsetHintItem = item;
      item.setActionCommand("show-hide-toolset-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem();
      pShowHideEditorHintItem = item;
      item.setActionCommand("show-hide-editor-hint");
      item.addActionListener(this);
      pPanelPopup.add(item); 
 
      item = new JMenuItem();
      pShowHideActionHintItem = item;
      item.setActionCommand("show-hide-action-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);   
    }

    /* node popup menus */ 
    {
      JPopupMenuItem item;
      
      pUndefinedNodePopup = new JPopupMenu();  
      pUndefinedNodePopup.addPopupMenuListener(this);

      pPanelLockedNodePopup = new JPopupMenu();  
      pPanelLockedNodePopup.addPopupMenuListener(this);

      pCheckedInNodePopup = new JPopupMenu();  
      pCheckedInNodePopup.addPopupMenuListener(this);

      pFrozenNodePopup = new JPopupMenu();  
      pFrozenNodePopup.addPopupMenuListener(this);

      pNodePopup = new JPopupMenu();  
      pNodePopup.addPopupMenuListener(this);

      pViewsContainingMenus = new JMenu[5];
      pViewsEditingMenus    = new JMenu[5];

      pEditWithMenus = new JMenu[4];

      JPopupMenu menus[] = { 
	pUndefinedNodePopup, pPanelLockedNodePopup, pCheckedInNodePopup, 
	pFrozenNodePopup, pNodePopup 
      };
      pNodeMenus = menus;

      pFirstRecentIndex     = new int[5];
      pRecentMenuItems      = new ArrayList<LinkedList<String>>(5);
      pRecentActionCommands = new ArrayList<TreeMap<String,String>>(5);

      pUpdateDetailsItems   = new JPopupMenuItem[5];
      pUpdateBranchItems    = new JPopupMenuItem[4];
      pMakeRootItems        = new JPopupMenuItem[5];
      pAddRootItems         = new JPopupMenuItem[5];
      pReplaceRootItems     = new JPopupMenuItem[5];
      pRemoveRootItems      = new JPopupMenuItem[5];
      pEditItems            = new JPopupMenuItem[4];
      pEditWithDefaultItems = new JPopupMenuItem[4];
      pCheckOutItems        = new JPopupMenuItem[3];
      pLockItems            = new JPopupMenuItem[3];
      pRestoreItems         = new JPopupMenuItem[3];
      pReleaseItems         = new JPopupMenuItem[2];

      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JPopupMenuItem(menus[wk], "Update Details");
	pUpdateDetailsItems[wk] = item;
	item.setActionCommand("details");
	item.addActionListener(this);
	menus[wk].add(item);  
	
        if(wk > 0) {
          item = new JPopupMenuItem(menus[wk], "Update Branch");
          pUpdateBranchItems[wk-1] = item;
          item.setActionCommand("update-branch");
          item.addActionListener(this);
          menus[wk].add(item);          
        }

	menus[wk].addSeparator();
	menus[wk].addSeparator();

        {
          JMenu sub = new JMenu("Node Roots");
          menus[wk].add(sub);  

          item = new JPopupMenuItem(menus[wk], "Set Root");
          pMakeRootItems[wk] = item;
          item.setActionCommand("make-root");
          item.addActionListener(this);
          sub.add(item);  
          
          item = new JPopupMenuItem(menus[wk], "Add Root");
          pAddRootItems[wk] = item;
          item.setActionCommand("add-root");
          item.addActionListener(this);
          sub.add(item);  
          
          item = new JPopupMenuItem(menus[wk], "Replace Root");
          pReplaceRootItems[wk] = item;
          item.setActionCommand("replace-root");
          item.addActionListener(this);
          sub.add(item);  
          
          item = new JPopupMenuItem(menus[wk], "Hide Root");
          pRemoveRootItems[wk] = item;
          item.setActionCommand("remove-root");
          item.addActionListener(this);
          sub.add(item);  
        }
	
	menus[wk].addSeparator();

	pViewsContainingMenus[wk] = new JMenu("Views Containing");
	menus[wk].add(pViewsContainingMenus[wk]);

	pViewsEditingMenus[wk] = new JMenu("Views Editing");
	menus[wk].add(pViewsEditingMenus[wk]);
	
	if(wk > 0) {
          menus[wk].addSeparator();

          String prefix = (wk < 4) ? "View" : "Edit";

          item = new JPopupMenuItem(menus[wk], prefix);
	  pEditItems[wk-1] = item;
	  item.setActionCommand("edit");
	  item.addActionListener(this);
	  menus[wk].add(item);
	  
	  pEditWithMenus[wk-1] = new JMenu(prefix + " With");
	  menus[wk].add(pEditWithMenus[wk-1]);

	  item = new JPopupMenuItem(menus[wk], prefix + " With Default"); 
	  pEditWithDefaultItems[wk-1] = item;
	  item.setActionCommand("edit-with-default");
	  item.addActionListener(this);
	  menus[wk].add(item);

	  if(wk == 4) {
	    item = new JPopupMenuItem(menus[wk], "Edit As Owner");
	    pEditAsOwnerItem = item;
	    item.setActionCommand("edit-as-owner");
	    item.addActionListener(this);
	    menus[wk].add(item);
	  }

	  menus[wk].addSeparator();
	}

        if(wk == 4) {
          JMenu sub = new JMenu("Modify");
          menus[wk].add(sub);  
          
          item = new JPopupMenuItem(menus[wk], "Link...");
          pLinkItem = item;
          item.setActionCommand("link");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Unlink");
          pUnlinkItem = item;
          item.setActionCommand("unlink");
          item.addActionListener(this);
          sub.add(item);
          
          sub.addSeparator();
          
          item = new JPopupMenuItem(menus[wk], "Add Secondary...");
          pAddSecondaryItem = item;
          item.setActionCommand("add-secondary");
          item.addActionListener(this);
          sub.add(item);
          
          JMenu sub2 = new JMenu("Remove Secondary");
          pRemoveSecondaryMenu = sub2;
          sub2.setEnabled(false);
          sub.add(sub2);
          
          sub.addSeparator();

          item = new JPopupMenuItem(menus[wk], "Clone...");
          pCloneItem = item;
          item.setActionCommand("clone");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Export...");
          pExportItem = item;
          item.setActionCommand("export");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Rename...");
          pRenameItem = item;
          item.setActionCommand("rename");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Renumber...");
          pRenumberItem = item;
          item.setActionCommand("renumber");
          item.addActionListener(this);
          sub.add(item);
        }
       
        if(wk == 4) {
          JMenu sub = new JMenu("Regenerate");
          menus[wk].add(sub);  

          item = new JPopupMenuItem(menus[wk], "Queue Jobs");
          pQueueJobsItem = item;
          item.setActionCommand("queue-jobs");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Queue Jobs Special...");
          pQueueJobsSpecialItem = item;
          item.setActionCommand("queue-jobs-special");
          item.addActionListener(this);
          sub.add(item);
          
          sub.addSeparator();

          item = new JPopupMenuItem(menus[wk], "Pause Jobs");
          pPauseJobsItem = item;
          item.setActionCommand("pause-jobs");
          item.addActionListener(this);
          sub.add(item);
          
          item = new JPopupMenuItem(menus[wk], "Resume Jobs");
          pResumeJobsItem = item;
          item.setActionCommand("resume-jobs");
          item.addActionListener(this);
          sub.add(item);

          item = new JPopupMenuItem(menus[wk], "Preempt Jobs");
          pPreemptJobsItem = item;
          item.setActionCommand("preempt-jobs");
          item.addActionListener(this);
          sub.add(item);

          item = new JPopupMenuItem(menus[wk], "Kill Jobs");
          pKillJobsItem = item;
          item.setActionCommand("kill-jobs");
          item.addActionListener(this);
          sub.add(item);

          sub.addSeparator();

          item = new JPopupMenuItem(menus[wk], "Remove Files");
          pRemoveFilesItem = item;
          item.setActionCommand("remove-files");
          item.addActionListener(this);
          sub.add(item);         
        }

   
        if((wk == 2) || (wk == 3) || (wk == 4)) {   
          JMenu sub = new JMenu("Version");
          menus[wk].add(sub);  

          if(wk == 4) {
            item = new JPopupMenuItem(menus[wk], "Check-In...");
            pCheckInItem = item;
            item.setActionCommand("check-in");
            item.addActionListener(this);
            sub.add(item);
          }

          if((wk == 2) || (wk == 3) || (wk == 4)) {
            item = new JPopupMenuItem(menus[wk], "Check-Out...");
            pCheckOutItems[wk-2] = item;
            item.setActionCommand("check-out");
            item.addActionListener(this);
            sub.add(item);
            
            item = new JPopupMenuItem(menus[wk], "Lock...");
            pLockItems[wk-2] = item;
            item.setActionCommand("lock");
            item.addActionListener(this);
            sub.add(item);         
            
            item = new JPopupMenuItem(menus[wk], "Request Restore...");
            pRestoreItems[wk-2] = item;
            item.setActionCommand("restore");
            item.addActionListener(this);
            sub.add(item);         
          }
          
          if(wk == 4) {
            sub.addSeparator();

            item = new JPopupMenuItem(menus[wk], "Evolve Version...");
            pEvolveItem = item;
            item.setActionCommand("evolve");
            item.addActionListener(this);
            sub.add(item);
          }

          if((wk == 3) || (wk == 4)) {
            sub.addSeparator();
	  
            item = new JPopupMenuItem(menus[wk], "Release...");
            pReleaseItems[wk-3] = item;
            item.setActionCommand("release");
            item.addActionListener(this);
            sub.add(item);
          }

          if(wk == 4) {
            item = new JPopupMenuItem(menus[wk], "Delete...");
            pDeleteItem = item;
            item.setActionCommand("delete");
            item.addActionListener(this);
            sub.add(item);
          }
	}

        pFirstRecentIndex[wk] = menus[wk].getComponentCount();
        pRecentMenuItems.add(new LinkedList<String>());
        pRecentActionCommands.add(new TreeMap<String,String>());

        if(prefs.getShowMostRecent()) {
          menus[wk].addSeparator();
          menus[wk].addSeparator();

          item = new JPopupMenuItem(menus[wk], "(None Yet)");
          item.setEnabled(false);
          menus[wk].add(item);
        }
      }
    }

    /* link popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pLinkPopup = new JPopupMenu();  
      pLinkPopup.addPopupMenuListener(this);
       
      item = new JMenuItem("Edit Link...");
      pLinkEditItem = item;
      item.setActionCommand("link-edit");
      item.addActionListener(this);
      pLinkPopup.add(item);

      item = new JMenuItem("Unlink");
      pLinkUnlinkItem = item;
      item.setActionCommand("link-unlink");
      item.addActionListener(this);
      pLinkPopup.add(item);
    }

    /* tool popup menu */ 
    {
      pToolPopup = new JPopupMenu();  
      pToolPopup.addPopupMenuListener(this);

      pDefaultToolPopup = new JPopupMenu();  
      pDefaultToolPopup.addPopupMenuListener(this);
    }

    updateMenuToolTips();

    /* initialize components */ 
    {
      pCanvas.addKeyListener(this);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Node Viewer";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }
  
  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    if(pRoots != null) 
      updateRoots();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the status of all currently displayed roots indexed by root node name.
   */ 
  public synchronized TreeMap<String,NodeStatus> 
  getRoots()
  {
    return new TreeMap<String,NodeStatus>(pRoots); 
  }
  

  /**
   * Perform a lightweight update of the state of all currently displayed roots.
   */
  private synchronized void 
  updateRoots() 
  {
    for(String name : pRoots.keySet()) 
      pRoots.put(name, null);
    
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Set the root nodes displayed by the viewer. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param names
   *   The fully resolved names of the root nodes.
   */
  private synchronized void 
  setRoots
  (
   String author, 
   String view,
   TreeSet<String> names
  )
  {
    super.setAuthorView(author, view);
    setRoots(names);
  }

  /**
   * Set the root nodes displayed by the viewer. <P> 
   * 
   * @param names
   *   The fully resolved names of the root nodes.
   */
  private synchronized void 
  setRoots
  (
   TreeSet<String> names
  )
  {
    pRoots.clear();
    for(String name : names) 
      pRoots.put(name, null);
    
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Add the given node name to the root nodes displayed by the viewer. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name
   *   The fully resolved node name.
   */
  public synchronized void 
  addRoot
  (
   String author, 
   String view,
   String name
  )
  {
    super.setAuthorView(author, view);
    addRoot(name);
  }

  /**
   * Add the given node name to the root nodes displayed by the viewer. <P> 
   * 
   * @param name
   *   The fully resolved node name.
   */
  public synchronized void 
  addRoot
  (
   String name
  )
  {
    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.add(name);
    
    setRoots(roots);
  }

  /**
   * Add the given node names to the root nodes displayed by the viewer. <P> 
   * 
   * A full update is performed.
   * 
   * @param names
   *   The fully resolved node names.
   */
  public synchronized void 
  addRoots
  (
   TreeSet<String> names
  )
  {    
    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.addAll(names);

    setRoots(roots);
  }

  /**
   * Remove the given node name from the root nodes displayed by the viewer. <P> 
   * 
   * The tree of nodes rooted at the given node is hidden but no update is performed.
   * 
   * @param name
   *   The fully resolved node name.
   */
  private synchronized void
  removeRoot
  (
   String name
  )
  {
    pRoots.remove(name);
    
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Remove the given node names from the root nodes displayed by the viewer. <P> 
   * 
   * A full update is performed.
   * 
   * @param names
   *   The fully resolved node names.
   */
  private synchronized void
  removeRoots
  (
   TreeSet<String> names
  )
  {
    for(String name : names) 
      pRoots.remove(name);
    
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Rename the given root node displayed by the viewer. <P> 
   * 
   * @param oldName
   *   The old fully resolved node name.
   * 
   * @param newName
   *   The new fully resolved node name.
   */
  private synchronized void
  renameRoot
  (
   String oldName,
   String newName
  )
  {
    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    if(pRoots.containsKey(oldName)) {
      roots.remove(oldName);
      roots.add(newName);
    }
    setRoots(roots);
  }

  /**
   * Invalidate the state of all current root nodes which have the given node as an 
   * upstream dependency.  These roots will be updated at the next status update.
   */ 
  private synchronized void
  invalidateRootsContaining
  (
   String name
  ) 
  {
    TreeSet<String> dead = new TreeSet<String>();
    for(String root : pRoots.keySet()) {
      NodeStatus status = pRoots.get(root);
      if(root.equals(name) || status.hasUpstreamNamed(name)) 
	dead.add(root);
    }

    for(String root : dead)
      pRoots.put(root, null);
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the node used to update the node details panels.
   */ 
  public String 
  getDetailedNodeName() 
  {
    return pLastDetailsName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the initial update after restoring a layout. 
   */ 
  public void 
  restoreSelections() 
  {
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param roots
   *   The current status for all root nodes.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   TreeMap<String,NodeStatus> roots
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    pRoots.clear();
    pRoots.putAll(roots); 
    
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    pShowDetailHints = prefs.getShowDetailHints();
    pShowDownstream  = prefs.getShowDownstream();

    updateUniverse();
    updateMenuToolTips();
    updateMostRecentNodeMenuItemPrefs();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
  
    /* panel menu */ 
    updateMenuToolTip
      (pUpdateItem, prefs.getUpdate(), 
       "Update the status of all nodes.");
    updateMenuToolTip
      (pRegisterItem, prefs.getNodeViewerRegisterNewNode(), 
       "Register a new node.");
    updateMenuToolTip
      (pReleaseViewItem, prefs.getNodeViewerReleaseView(), 
       "Release nodes from the current working area view.");
    updateMenuToolTip
      (pPanelRestoreItem, prefs.getNodeViewerCheckOut(), 
       "Submit requests to restore offline checked-in versions.");

    updateMenuToolTip
      (pFrameSelectionItem, prefs.getFrameSelection(), 
       "Move the camera to frame the bounds of the currently selected nodes.");
    updateMenuToolTip
      (pFrameAllItem, prefs.getFrameAll(), 
       "Move the camera to frame all active nodes.");
    updateMenuToolTip
      (pAutomaticExpandItem, prefs.getAutomaticExpand(), 
       "Automatically expand the first occurance of a node.");
    updateMenuToolTip
      (pExpandAllItem, prefs.getExpandAll(), 
       "Expand all nodes.");
    updateMenuToolTip
      (pCollapseAllItem, prefs.getCollapseAll(), 
       "Collapse all nodes.");

    updateMenuToolTip
      (pShowHideDownstreamItem, prefs.getNodeViewerShowHideDownstreamNodes(), 
       "Show/hide nodes downstream of the focus node.");
    updateMenuToolTip
      (pRemoveAllRootsItem, prefs.getHideAll(), 
       "Hide all of the root nodes.");

    updateMenuToolTip
      (pShowHideDetailHintsItem, prefs.getNodeViewerShowHideDetailHints(), 
       "Show/hide node status detail hints.");
    updateMenuToolTip
      (pShowHideToolsetHintItem, prefs.getNodeViewerShowHideToolsetHint(), 
       "Show/hide the Toolset property as part of the node detail hints."); 
    updateMenuToolTip
      (pShowHideEditorHintItem, prefs.getNodeViewerShowHideEditorHint(), 
       "Show/hide the Editor property as part of the node detail hints."); 
    updateMenuToolTip
      (pShowHideActionHintItem, prefs.getNodeViewerShowHideActionHint(), 
       "Show/hide the Action property as part of the node detail hints."); 
    

    /* node menus */ 
    int wk;
    for(wk=0; wk<5; wk++) {
      updateMenuToolTip
	(pUpdateDetailsItems[wk], prefs.getDetails(), 
	 "Update connected node details panels.");
      updateMenuToolTip
	(pMakeRootItems[wk], prefs.getNodeViewerMakeRoot(), 
	 "Make the current primary selection the only root node.");
      updateMenuToolTip
	(pAddRootItems[wk], prefs.getNodeViewerAddRoot(), 
	 "Add the current primary selection to the set of root nodes.");
      updateMenuToolTip
	(pReplaceRootItems[wk], prefs.getNodeViewerReplaceRoot(), 
	 "Replace the root node of the current primary selection with the " +
	 "primary selection.");
      updateMenuToolTip
	(pRemoveRootItems[wk], prefs.getHideSelected(), 
	 "Remove the root node of the current primary selection from the set " +
	 "of roots nodes.");
    }

    for(wk=0; wk<4; wk++) {
      updateMenuToolTip
	(pUpdateBranchItems[wk], prefs.getUpdateBranch(), 
	 "Update the status of all nodes upstream of the primary selection.");

      updateMenuToolTip
	(pEditItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection.");

      updateMenuToolTip
	(pEditWithDefaultItems[wk], prefs.getEditWithDefault(), 
	 "Edit primary file sequences of the current primary selection using the default" + 
	 "editor for the file type.");
    }

    updateMenuToolTip
      (pEditAsOwnerItem, prefs.getEditAsOwner(), 
       "Edit primary file sequences of the current primary selection with the permissions " +
       "of the owner of the node.");

    for(wk=0; wk<3; wk++) {
      updateMenuToolTip
	(pCheckOutItems[wk], prefs.getNodeViewerCheckOut(), 
	 "Check-out the current primary selection.");

      updateMenuToolTip
	(pLockItems[wk], prefs.getNodeViewerLock(), 
	 "Lock the current primary selection to a specific checked-in version.");

      updateMenuToolTip
	(pRestoreItems[wk], prefs.getNodeViewerCheckOut(), 
	 "Submit requests to restore offline checked-in versions of the selected ndoes.");
    }

    for(wk=0; wk<2; wk++) {
      updateMenuToolTip
	(pReleaseItems[wk], prefs.getNodeViewerRelease(), 
	 "Release the current primary selection.");
    }

    updateMenuToolTip
      (pLinkItem, prefs.getNodeViewerLink(), 
       "Link the secondary selected nodes to the current primary selection.");
    updateMenuToolTip
      (pUnlinkItem, prefs.getNodeViewerUnlink(), 
       "Unlink the secondary selected nodes from the current primary selection.");
    updateMenuToolTip
      (pAddSecondaryItem, prefs.getNodeViewerAddSecondary(), 
       "Add a secondary file sequence to the current primary selection.");
    updateMenuToolTip
      (pQueueJobsItem, prefs.getQueueJobs(), 
       "Submit jobs to the queue for the current primary selection.");
    updateMenuToolTip
      (pQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Submit jobs to the queue for the current primary selection with special job " + 
       "requirements.");
    updateMenuToolTip
      (pPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pPreemptJobsItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pCheckInItem, prefs.getNodeViewerCheckIn(), 
       "Check-in the current primary selection.");
    updateMenuToolTip
      (pEvolveItem, prefs.getNodeViewerEvolve(), 
       "Evolve the current primary selection.");
    updateMenuToolTip
      (pCloneItem, prefs.getNodeViewerClone(), 
       "Register a new node which is a clone of the current primary selection.");
    updateMenuToolTip
      (pRemoveFilesItem, prefs.getRemoveFiles(), 
       "Remove all the primary/secondary files associated with the selected nodes.");
    updateMenuToolTip
      (pExportItem, prefs.getNodeViewerExport(), 
       "Export the node properties from the primary selection to the selected nodes.");
    updateMenuToolTip
      (pRenameItem, prefs.getNodeViewerRename(), 
       "Rename the current primary selection.");
    updateMenuToolTip
      (pRenumberItem, prefs.getNodeViewerRenumber(), 
       "Renumber the current primary selection.");
    updateMenuToolTip
      (pDeleteItem, prefs.getNodeViewerDelete(), 
       "Delete the current primary selection.");

    /* link menu */ 
    updateMenuToolTip
      (pLinkEditItem, prefs.getNodeViewerLinkEdit(), 
       "Edit the properties of the selected link.");
    updateMenuToolTip
      (pLinkUnlinkItem, prefs.getNodeViewerLinkUnlink(), 
       "Remove the selected link.");
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel menu.
   */ 
  public void 
  updatePanelMenu() 
  {
    pRegisterItem.setEnabled(!isLocked());
    pReleaseViewItem.setEnabled(!isLocked());

    pShowHideDownstreamItem.setText
      ((pShowDownstream ? "Hide" : "Show") + " Downstream");

    pShowHideDetailHintsItem.setText
      ((pShowDetailHints ? "Hide" : "Show") + " Detail Hints");
    pShowHideDetailHintsItem.setEnabled(true);

    if(pViewerNodeHint != null) {
      pShowHideToolsetHintItem.setText
	((pViewerNodeHint.showToolset() ? "Hide" : "Show") + " Toolset Hint");
      pShowHideToolsetHintItem.setEnabled(true);

      pShowHideEditorHintItem.setText
	((pViewerNodeHint.showEditor() ? "Hide" : "Show") + " Editor Hint");
      pShowHideEditorHintItem.setEnabled(true);

      pShowHideActionHintItem.setText
	((pViewerNodeHint.showAction() ? "Hide" : "Show") + " Action Hint");
      pShowHideActionHintItem.setEnabled(true);
    }
    else {
      pShowHideToolsetHintItem.setText("Show Toolset Hint");
      pShowHideToolsetHintItem.setEnabled(false);

      pShowHideEditorHintItem.setText("Show Editor Hint");
      pShowHideEditorHintItem.setEnabled(false);

      pShowHideActionHintItem.setText("Show Action Hint");
      pShowHideActionHintItem.setEnabled(false);
    }
  }

  /**
   * Update the locked node menu.
   */ 
  public void 
  updateLockedNodeMenu() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
    pUpdateBranchItems[0].setEnabled(!prefs.getHeavyweightUpdates());

    updateEditorMenus();
  }

  /**
   * Update the checked-in node menu.
   */ 
  public void 
  updateCheckedInNodeMenu() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
    pUpdateBranchItems[1].setEnabled(!prefs.getHeavyweightUpdates());

    updateEditorMenus();
  }

  /**
   * Update the frozen node menu.
   */ 
  public void 
  updateFrozenNodeMenu() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
    pUpdateBranchItems[2].setEnabled(!prefs.getHeavyweightUpdates());

    updateEditorMenus();
  }

  /**
   * Update the node menu.
   */ 
  public void 
  updateNodeMenu() 
  {
    NodeDetails details = pPrimary.getNodeStatus().getDetails();
    NodeMod mod = details.getWorkingVersion();

    boolean queuePrivileged = 
      (PackageInfo.sUser.equals(pAuthor) || pPrivilegeDetails.isQueueManaged(pAuthor));

    boolean nodePrivileged = 
      (PackageInfo.sUser.equals(pAuthor) || pPrivilegeDetails.isNodeManaged(pAuthor));

    boolean hasCheckedIn = (details.getLatestVersion() != null);
    boolean multiple     = (getSelectedNames().size() >= 2);
    
    UserPrefs prefs = UserPrefs.getInstance();
    pUpdateBranchItems[3].setEnabled(!prefs.getHeavyweightUpdates());

    pLinkItem.setEnabled(multiple && nodePrivileged);
    pUnlinkItem.setEnabled(multiple && nodePrivileged);

    pAddSecondaryItem.setEnabled(nodePrivileged);

    /* rebuild remove secondary items */ 
    pRemoveSecondaryMenu.setEnabled(false);
    if(!isLocked() && nodePrivileged) {
      JPopupMenuItem item;

      pRemoveSecondaryMenu.removeAll();
      pRemoveSecondarySeqs.clear();

      if(mod != null) {
	for(FileSeq fseq : mod.getSecondarySequences()) {
	  String fname = fseq.toString();
	  
	  item = new JPopupMenuItem(pNodePopup, fname);
	  item.setActionCommand("remove-secondary:" + fname);
	  item.addActionListener(this);
	  pRemoveSecondaryMenu.add(item);
	  
	  pRemoveSecondarySeqs.put(fname, fseq);
	}
      }
      
      pRemoveSecondaryMenu.setEnabled(pRemoveSecondaryMenu.getItemCount() > 0);
    }

    pQueueJobsItem.setEnabled(queuePrivileged);
    pQueueJobsSpecialItem.setEnabled(queuePrivileged);

    pPauseJobsItem.setEnabled(queuePrivileged);
    pResumeJobsItem.setEnabled(queuePrivileged);
    pPreemptJobsItem.setEnabled(queuePrivileged);
    pKillJobsItem.setEnabled(queuePrivileged);
    pRemoveFilesItem.setEnabled(nodePrivileged); 

    pCheckInItem.setEnabled(nodePrivileged);
    pCheckOutItems[2].setEnabled(hasCheckedIn && nodePrivileged);
    pLockItems[2].setEnabled(hasCheckedIn && nodePrivileged);
    pReleaseItems[1].setEnabled(pPrivilegeDetails.isMasterAdmin());
    pDeleteItem.setEnabled(pPrivilegeDetails.isMasterAdmin());

    pEvolveItem.setEnabled(hasCheckedIn && nodePrivileged);
    pCloneItem.setEnabled(nodePrivileged);

    pExportItem.setEnabled(multiple && nodePrivileged);
    pRenameItem.setEnabled(!hasCheckedIn && nodePrivileged);
    pRenumberItem.setEnabled(mod.getPrimarySequence().hasFrameNumbers() && nodePrivileged);

    pRestoreItems[2].setEnabled(hasCheckedIn && nodePrivileged);

    updateEditorMenus();
  }

  /**
   * Update the dynamic menus based on working area usage of nodes.
   */ 
  private synchronized void 
  updateWorkingAreaMenus()
  {    
    String name = null;
    if(pPrimary != null) 
      name = pPrimary.getNodePath().getCurrentName();
    
    UIMaster master = UIMaster.getInstance();
    master.rebuildWorkingAreaContainingMenus
      (pGroupID, name, pNodeMenus, pViewsContainingMenus, this);
    master.rebuildWorkingAreaEditingMenus
      (pGroupID, name, pNodeMenus, pViewsEditingMenus, this);
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    pEditorMenuToolset = null;
    pToolMenuToolset = null;
    pRefreshDefaultToolMenu = true;
  }

  /**
   * Update the editor plugin menus.
   */ 
  private synchronized void 
  updateEditorMenus()
  {    
    String toolset = null;
    if(pPrimary != null) {
      NodeStatus status = pPrimary.getNodeStatus();
      if(status != null) {
	NodeDetails details = status.getDetails();
	if(details != null) {
	  if(details.getWorkingVersion() != null) 
	    toolset = details.getWorkingVersion().getToolset();
	  else if(details.getLatestVersion() != null) 
	    toolset = details.getLatestVersion().getToolset();
	}
      }
    }
    
    if((toolset != null) && !toolset.equals(pEditorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) 
	master.rebuildEditorMenu(pNodeMenus[wk+1], 
                                 pGroupID, toolset, pEditWithMenus[wk], this);
      
      pEditorMenuToolset = toolset;
    }    

    pEditAsOwnerItem.setEnabled(pPrivilegeDetails.isNodeManaged(pAuthor) && 
                                !PackageInfo.sUser.equals(pAuthor) && 
                                (PackageInfo.sOsType != OsType.Windows));
  }

  /**
   * Update the tool plugin menus (over a node).
   */ 
  private synchronized void 
  updateToolMenu()
  {
    String toolset = null;
    if(pPrimary != null) {
      NodeStatus status = pPrimary.getNodeStatus();
      if(status != null) {
	NodeDetails details = status.getDetails();
	if(details != null) {
	  if(details.getWorkingVersion() != null) 
	    toolset = details.getWorkingVersion().getToolset();
	  else if(details.getLatestVersion() != null) 
	    toolset = details.getLatestVersion().getToolset();
	}
      }
    }

    if((toolset != null) && !toolset.equals(pToolMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildToolMenu(pToolPopup, pGroupID, toolset, pToolPopup, this);
      
      pToolMenuToolset = toolset;
    }    
  }

  /**
   * Update the default tool plugin menus (nothing under the mouse).
   */ 
  private synchronized void 
  updateDefaultToolMenu()
  {
    if(pRefreshDefaultToolMenu) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildDefaultToolMenu(pDefaultToolPopup, pGroupID, pDefaultToolPopup, this);
      
      pRefreshDefaultToolMenu = false; 
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the most recently chosen node menu items.
   */ 
  private void 
  updateRecentNodeMenuItems
  (
   ActionEvent e
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* determine which item and parent menu is involved */ 
    JPopupMenuItem item = null;
    JPopupMenu menu = null;
    LinkedList<String> recent = null; 
    TreeMap<String,String> commands = null; 
    int maxIdx = -1;
    {
      Object source = e.getSource();
      if(source instanceof JPopupMenuItem) {
        item = (JPopupMenuItem) source;

        menu = item.getTopLevelMenu();
        int idx = -1;
        {
          int wk;
          for(wk=0; wk<pNodeMenus.length; wk++) {
            if(pNodeMenus[wk] == menu) {
              idx = wk;
              break;
            }
          }
        }

        if(idx == -1)
          return;
            
        recent   = pRecentMenuItems.get(idx);
        commands = pRecentActionCommands.get(idx);
        maxIdx   = pFirstRecentIndex[idx]; 
      }

      if(item == null) 
        return;
    }
    
    String title = item.getText(); 
    String cmd   = item.getActionCommand();

    /* if this item is already the most recent, do nothing */ 
    if(!recent.isEmpty() && 
       title.equals(recent.peek()) && cmd.equals(commands.get(title))) 
      return;

    /* update the recent data structures */ 
    recent.remove(title);
    recent.addFirst(title);
    commands.put(title, cmd);

    /* don't exceed the max number */ 
    while(recent.size() > prefs.getNumMostRecent()) { 
      String ltitle = recent.removeLast();
      commands.remove(ltitle);
    }

    /* rebuild the menu items */ 
    rebuildMostRecentNodeMenuItems(menu, recent, commands, maxIdx);
  }

  /**
   * Reinitialize the most recent menu items after a user preference change.
   */ 
  private void 
  updateMostRecentNodeMenuItemPrefs() 
  {
    int wk;
    for(wk=0; wk<pNodeMenus.length; wk++) {
      rebuildMostRecentNodeMenuItems(pNodeMenus[wk], pRecentMenuItems.get(wk), 
                                     pRecentActionCommands.get(wk), pFirstRecentIndex[wk]); 
    }
  }

  /**
   * Rebuild the menu items from the set of most recent operations and action commands.
   */ 
  private void 
  rebuildMostRecentNodeMenuItems
  (
   JPopupMenu menu,
   LinkedList<String> recent,
   TreeMap<String,String> commands, 
   int maxIdx
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* don't exceed the max number */ 
    while(recent.size() > prefs.getNumMostRecent()) { 
      String ltitle = recent.removeLast();
      commands.remove(ltitle);
    }

    /* rebuild the menu items */ 
    while(menu.getComponentCount() > maxIdx) 
      menu.remove(maxIdx);

    if(prefs.getShowMostRecent()) {
      menu.addSeparator();
      menu.addSeparator();

      if(recent.isEmpty()) {
        JPopupMenuItem item = new JPopupMenuItem(menu, "(None Yet)");
        item.setEnabled(false);
        menu.add(item);
      }
      else {
        for(String mtitle : recent) {
          JPopupMenuItem mitem = new JPopupMenuItem(menu, mtitle);
          mitem.setActionCommand(commands.get(mtitle));
          mitem.addActionListener(this);
          menu.add(mitem);
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the visualization graphics and any connected subpanels.
   */
  private synchronized void 
  updateUniverse() 
  { 
    /* get current user privileges */ 
    updatePrivileges();

    /* compute the center of the current layout if no pinned node is set */ 
    if(pPinnedPath == null) {
      pPinnedPos = null;
      BBox2d bbox = getNodeBounds(pViewerNodes.values());
      if(bbox != null) 
	pPinnedPos = bbox.getCenter(); 
    }

    /* remove all previous nodes and links */ 
    pViewerNodes.clear();
    pViewerLinks.clear();
    pLastViewerNodeHint = null;

    /* rebuild the viewer nodes and links */ 
    if(!pRoots.isEmpty()) {
      double anchorHeight = 0.0;
      for(String name : pRoots.keySet()) {
	NodeStatus status = pRoots.get(name);
	if(status != null) {
	  NodePath path = new NodePath(name);
	  Point2d anchor = new Point2d(0.0, anchorHeight);
	  
	  /* layout the upstream nodes */ 
	  double uheight = 0.0;
	  {
	    TreeSet<String> seen = new TreeSet<String>();
	    uheight = layoutNodes(true, true, status, path, anchor, seen);
	  }
	  
	  /* layout the downstream nodes */ 
	  double dheight = 0.0;  
	  if(pShowDownstream) {
	    TreeSet<String> seen = new TreeSet<String>();
	    dheight = layoutNodes(true, false, status, path, anchor, seen);
	  }
	  
	  /* shift the upstream/downstream nodes so that they line up vertically */ 
	  if(uheight > dheight) {
	    shiftUpstreamNodes(true, status, path, 
			       anchorHeight + dheight*0.5, (dheight - uheight)*0.5);
	  }
	  else {
	    shiftUpstreamNodes(true, status, path, anchorHeight + uheight*0.5, 0.0);
	    if(pShowDownstream) 
	      shiftDownstreamNodes(true, status, path, (uheight - dheight)*0.5);
	  }
	
	  anchorHeight += Math.min(uheight, dheight);
	}
      }

      /* shift entire layout */ 
      {
	Vector2d delta = null;

	/* shift all active nodes so that the pinned node remains stationary */ 
	if(pPinnedPath != null) {
	  ViewerNode vnode = pViewerNodes.get(pPinnedPath);
	  if(vnode != null) 
	    delta = new Vector2d(vnode.getPosition(), pPinnedPos);
	}

	/* shift all active nodes so that center of the new layout is the same as the 
	     center of the previous layout */ 
	else if(pPinnedPos != null) {
	  BBox2d bbox = getNodeBounds(pViewerNodes.values());
	  if(bbox != null) 
	    delta = new Vector2d(bbox.getCenter(), pPinnedPos);
	}

	/* this is the first layout, shift all nodes so that they lie in the same position
	     they did when the layout was saved */ 
	else if(pInitialCenter != null) {
	  BBox2d bbox = getNodeBounds(pViewerNodes.values());
	  if(bbox != null) 
	    delta = new Vector2d(bbox.getCenter(), pInitialCenter);
	  else 
	    delta = new Vector2d(0.0, 0.0);
	  pInitialCenter = null;
	}

	if(delta != null) {
	  for(ViewerNode vnode : pViewerNodes.values()) 
	    vnode.movePosition(delta);
	}

	pPinnedPos  = null;
	pPinnedPath = null;
      }
   
      /* preserve the current layout */ 
      pExpandDepth  = null; 
      pLayoutPolicy = LayoutPolicy.Preserve;
    }

    /* render the changes */ 
    refresh();
  }
  
  /**
   * Recursively layout the nodes.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param upstream
   *   Whether to traverse the nodes in an upstream direction (or downstream).
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param anchor
   *   The upper-left corner of the layout area for the current node.
   * 
   * @param seen
   *   The fully resolved node names of processed nodes.
   * 
   * @return 
   *   The height of the layout area of the viewer node including its children.
   */ 
   private synchronized double
   layoutNodes
   (
    boolean isRoot, 
    boolean upstream,
    NodeStatus status, 
    NodePath path, 
    Point2d anchor, 
    TreeSet<String> seen
   ) 
   {
     UserPrefs prefs = UserPrefs.getInstance();

     ViewerNode vnode = null;
     if(!isRoot || upstream) {
       vnode = new ViewerNode(status, path);
       pViewerNodes.put(path, vnode);
     }
     else {
       vnode = pViewerNodes.get(path);
       assert(vnode != null);
     }

     /* set the collapsed state of the node */ 
     UIMaster master = UIMaster.getInstance();
     if((upstream && status.hasSources()) || 
	(!upstream && status.hasTargets() && !isRoot)) {

       if(pExpandDepth != null) {
	 boolean collapsed = (path.getNumNodes() >= pExpandDepth);
	 vnode.setCollapsed(collapsed); 
	 master.setNodeCollapsed(path.toString(), collapsed);
       }
       else {
	 switch(pLayoutPolicy) {
	 case Preserve:
	   vnode.setCollapsed(master.wasNodeCollapsed(path.toString()));
	   break;
	   
	 case AutomaticExpand:
	   {
	     boolean collapsed = seen.contains(status.getName());
	     vnode.setCollapsed(collapsed);
	     master.setNodeCollapsed(path.toString(), collapsed);
	   }
	   break;
	 
	 case CollapseAll:
	   vnode.setCollapsed(true);
	   master.setNodeCollapsed(path.toString(), true);
	   break;
	   
	 case ExpandAll:
	   master.setNodeCollapsed(path.toString(), false);	   
	 }
       }
     }
       
     seen.add(status.getName());

     double height = 0.0;
     if(vnode.isCollapsed() && !(isRoot && !upstream)) {
       height = -prefs.getNodeSpaceY();
     }
     else {      
       Collection<NodeStatus> children = null;
       if(upstream && status.hasSources())
 	children = status.getSources();
       else if(!upstream && status.hasTargets()) 
 	children = status.getTargets();

       if(children != null) {
 	for(NodeStatus cstatus : children) {
 	  NodePath cpath = new NodePath(path, cstatus.getName());
	  
 	  /* layout the child node */ 
 	  {
 	    double sign = upstream ? 1.0 : -1.0;
	    Point2d canchor = 
	      Point2d.add(anchor, new Vector2d(sign*prefs.getNodeSpaceX(), height));
 	    height += layoutNodes(false, upstream, cstatus, cpath, canchor, seen);
 	  }

 	  /* add a link between this node and the child node */ 
 	  {
  	    ViewerNode cvnode = pViewerNodes.get(cpath);
  	    if(upstream) {
  	      LinkCommon link = null;
  	      {
  		NodeDetails details = status.getDetails();
  		if(details.getWorkingVersion() != null) 
  		  link = details.getWorkingVersion().getSource(cstatus.getName());
  		else if(details.getLatestVersion() != null)
		  link = details.getLatestVersion().getSource(cstatus.getName());
  	      }
  	      assert(link != null);	    
	      
  	      pViewerLinks.addUpstreamLink(vnode, cvnode, link, 
					   status.isStaleLink(link.getName()));
  	    }
  	    else {
  	      pViewerLinks.addDownstreamLink(cvnode, vnode);
  	    }				   
  	  }  
 	}
       }
       else {
 	height = -prefs.getNodeSpaceY();
       }
     }

     if(!isRoot) {
       double offset = ((path.getNumNodes() % 2) == 0) ? prefs.getNodeOffset() : 0.0;
       vnode.setPosition(Point2d.add(anchor, new Vector2d(0.0, 0.5*height + offset)));
     }

     return height;
   }
  
  /**
   * Recursively shift the upstream nodes by the given vertical offset.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param ry
   *   The vertical position of the root node.
   * 
   * @param offset
   *   The vertical distance to shift all nodes except the root node.
   */ 
  private synchronized void
  shiftUpstreamNodes
  (
   boolean isRoot, 
   NodeStatus status, 
   NodePath path, 
   double ry, 
   double offset
  ) 
  {
    ViewerNode vnode = pViewerNodes.get(path);
    if(vnode == null) 
      return;

    if(isRoot) 
      vnode.setPosition(new Point2d(0.0, ry));
    else 
      vnode.movePosition(new Vector2d(0.0, offset));

    for(NodeStatus cstatus : status.getSources()) {
      NodePath cpath = new NodePath(path, cstatus.getName());
      shiftUpstreamNodes(false, cstatus, cpath, ry, offset);
    }
  }

  /**
   * Recursively shift the downstream nodes by the given vertical offset.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param offset
   *   The vertical distance to shift all nodes.
   */ 
  private synchronized void
  shiftDownstreamNodes
  (
   boolean isRoot, 
   NodeStatus status, 
   NodePath path, 
   double offset
  ) 
  {
    ViewerNode vnode = pViewerNodes.get(path);
    if(vnode == null) 
      return;

    if(!isRoot) 
      vnode.movePosition(new Vector2d(0.0, offset));

    for(NodeStatus cstatus : status.getTargets()) {
      NodePath cpath = new NodePath(path, cstatus.getName());
      shiftDownstreamNodes(false, cstatus, cpath, offset);
    }
  }

  /**
   * Get the bounding box which contains the given viewer nodes. <P> 
   * 
   * @return 
   *   The bounding box or <CODE>null</CODE> if no nodes are given.
   */ 
  private BBox2d
  getNodeBounds
  (
   Collection<ViewerNode> nodes
  ) 
  {
    BBox2d bbox = null;
    for(ViewerNode vnode : nodes) {
      if(bbox == null) 
	bbox = new BBox2d(vnode.getPosition(), vnode.getPosition());
      else 
	bbox.grow(vnode.getPosition());
    }

    if(bbox != null) {
      UserPrefs prefs = UserPrefs.getInstance();
      Vector2d margin = new Vector2d(prefs.getNodeSpaceX(), prefs.getNodeSpaceY());
      margin.mult(0.5);
      bbox.bloat(margin);
    }    

    return bbox;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the fully resolved name of the primary node selection.
   * 
   * @return 
   *   The node name or <CODE>null</CODE> if there is no primary selection.
   */ 
  public synchronized String
  getPrimarySelectionName() 
  {
    if(pPrimary != null) 
      return pPrimary.getNodeStatus().getName();
    return null;
  }

  /**
   * Get the fully resolved name of root node of the primary node selection.
   * 
   * @return 
   *   The node name or <CODE>null</CODE> if there is no primary selection.
   */ 
  public synchronized String
  getPrimarySelectionRootName() 
  {
    if(pPrimary != null) 
      return pPrimary.getNodePath().getRootName();
    return null;
  }

  /**
   * Get the fully resolved names of all selected nodes.
   */ 
  public synchronized TreeSet<String>
  getSelectedNames() 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) 
      names.add(vnode.getNodeStatus().getName());

    return names;
  }
  
  /**
   * Get the fully resolved name of root node of the primary node selection.
   * 
   * @return 
   *   The node name or <CODE>null</CODE> if there is no primary selection.
   */ 
  public synchronized TreeSet<String>
  getSelectedRootNames() 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) 
      names.add(vnode.getNodePath().getRootName());

    return names;
  }

  /**
   * Get the fully resolved names of the most downstream selected nodes. <P> 
   * 
   * Any nodes which are selected and are upstream of another selected node will be 
   * omitted from the returned names.
   */ 
  public synchronized TreeSet<String>
  getMostDownstreamOfSelectedNames() 
  {
    TreeSet<String> all = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) 
      all.add(vnode.getNodeStatus().getName());
   
    TreeSet<String> names = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) {
      String name = vnode.getNodeStatus().getName();

      Collection<String> downstream = vnode.getNodePath().getNames();
      for(String dname : downstream) {
	if(dname.equals(name)) 
	  names.add(name);
	else if(all.contains(dname)) 
	  break;
      }
    }

    return names;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear the current selection.
   */ 
  public synchronized void
  clearSelection()
  {
    for(ViewerNode vnode : pSelected.values()) 
      vnode.setSelectionMode(SelectionMode.Normal);

    pSelected.clear();
    pPrimary = null;
    
    pSelectedLink = null;
  }
  
  /**
   * Make the given viewer node the primary selection.
   */ 
  public synchronized void 
  primarySelect
  (
   ViewerNode vnode
  ) 
  {
    switch(vnode.getSelectionMode()) {
    case Normal:
      pSelected.put(vnode.getNodePath(), vnode);
      
    case Selected:
      if(pPrimary != null) 
	pPrimary.setSelectionMode(SelectionMode.Selected);
      pPrimary = vnode;
      vnode.setSelectionMode(SelectionMode.Primary);
    }
  }

  /**
   * Add the given viewer node to the selection.
   */ 
  public synchronized void 
  addSelect
  (
   ViewerNode vnode
  ) 
  {
    switch(vnode.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	pPrimary = null;
      }

    case Normal:
      vnode.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vnode.getNodePath(), vnode);
    }
  }

  /**
   * Add the viewer nodes inside the given bounding box to the selection.
   */ 
  public synchronized void 
  addSelect
  (
   BBox2d bbox
  ) 
  {
    for(ViewerNode vnode : pViewerNodes.values()) {
      if(vnode.isInsideOf(bbox)) 
	addSelect(vnode);
    }
  }

  /**
   * Toggle the selection of the given viewer node.
   */ 
  public synchronized void 
  toggleSelect
  (
   ViewerNode vnode
  ) 
  {
    switch(vnode.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	pPrimary = null;
      }

    case Selected:
      vnode.setSelectionMode(SelectionMode.Normal);
      pSelected.remove(vnode.getNodePath());
      break;

    case Normal:
      vnode.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vnode.getNodePath(), vnode);      
    }    
  }

  /**
   * Toggle the selection of the viewer nodes inside the given bounding box.
   */ 
  public synchronized void 
  toggleSelect
  (
   BBox2d bbox
  ) 
  {
    for(ViewerNode vnode : pViewerNodes.values()) {
      if(vnode.isInsideOf(bbox)) 
	toggleSelect(vnode);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ViewerNode or ViewerLinkRelationship under the current mouse position. <P> 
   */ 
  private synchronized Object
  objectAtMousePos() 
  {
    if(pMousePos == null) 
      return null;

    /* compute world coordinates */ 
    Point2d pos = new Point2d(pMousePos);
    {
      Dimension size = pCanvas.getSize();
      Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
      
      double f = -pCameraPos.z() * pPerspFactor;
      Vector2d persp = new Vector2d(f * pAspect, f);
      
      Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());
      
      pos.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
    }
    
    /* check node icons */ 
    for(ViewerNode vnode : pViewerNodes.values()) {
      if(vnode.isInside(pos)) 
	return vnode; 
    }

    /* check link relationship icons */ 
    return pViewerLinks.pickLinkRelationship(pos);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- GL EVENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Called by the drawable to initiate OpenGL rendering by the client.
   */ 
  public void 
  display
  (
   GLAutoDrawable drawable
  )
  {
    super.display(drawable); 
    GL gl = drawable.getGL();

    /* render the scene geometry */ 
    {
      if(pRefreshScene) {
	rebuildAll(gl);

	{
	  UIMaster master = UIMaster.getInstance(); 
	  master.freeDisplayList(pSceneDL.getAndSet(master.getDisplayList(gl)));
	}

	gl.glNewList(pSceneDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	  renderAll(gl);
	gl.glEndList();

	pRefreshScene = false;
      }
      else {
	gl.glCallList(pSceneDL.get());
      }

      pViewerNodeHint.rebuild(gl);
      pViewerNodeHint.render(gl);
    }
  }

  /** 
   * Syncronized display list building helper.
   */ 
  private synchronized void
  rebuildAll
  (
   GL gl
  ) 
  {
    for(ViewerNode vnode : pViewerNodes.values()) 
      vnode.rebuild(gl);
    pViewerLinks.rebuild(gl);
  }
  
  /** 
   * Syncronized rendering helper.
   */ 
  private synchronized void 
  renderAll
  (
   GL gl
  ) 
  {
    for(ViewerNode vnode : pViewerNodes.values()) 
      vnode.render(gl);
    pViewerLinks.render(gl);
  }
  

  /**
   * Return the previously allocated OpenGL display lists to the pool of display lists to be 
   * reused. 
   */ 
  public synchronized void 
  freeDisplayLists() 
  {
    super.freeDisplayLists();

    if(pViewerLinks != null) 
      pViewerLinks.freeDisplayLists();
  }



  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();

    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local mouse events */ 
    Object under = objectAtMousePos();

    /* mouse press is over a pickable object viewer node */ 
    if(under != null) {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
	if(under instanceof ViewerNode) {
	  ViewerNode vunder = (ViewerNode) under;	

	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	    
	  
	  int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK);
	  
	  int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);

	  if(e.getClickCount() == 1) {
	    /* BUTTON1: replace selection */ 
	    if((mods & (on1 | off1)) == on1) {
	      clearSelection();
	      addSelect(vunder);
	      refresh();
	    }
	    
	    /* BUTTON1+SHIFT: toggle selection */ 
	    else if((mods & (on2 | off2)) == on2) {
	      toggleSelect(vunder);
	      refresh();
	    }
	    
	    /* BUTTON1+SHIFT+CTRL: add to the selection */ 
	    else if((mods & (on3 | off3)) == on3) {
	      addSelect(vunder);
	      refresh();
	    }
	  }
	  else if(e.getClickCount() == 2) {
	    /* BUTTON1 (double click): send node status details panels */ 
	    if((mods & (on1 | off1)) == on1) {
	      primarySelect(vunder);
	      refresh();

	      doDetails();
	    }
	  }
	}
	break;

      case MouseEvent.BUTTON2:
	if(under instanceof ViewerNode) {
	  ViewerNode vunder = (ViewerNode) under;

	  int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON2: expand/collapse a node */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(vunder.getNodeStatus().hasSources()) {
	      vunder.toggleCollapsed();
	      UIMaster master = UIMaster.getInstance();
	      master.setNodeCollapsed(vunder.getNodePath().toString(), vunder.isCollapsed());

	      pPinnedPos  = vunder.getPosition();
	      pPinnedPath = vunder.getNodePath();

	      updateUniverse();
	    }
	  }
	}
	break;
	  
      case MouseEvent.BUTTON3:
	{
	  int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);


	  int on2  = (MouseEvent.BUTTON3_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK);
	  
	  int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON3: node/link popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
	    
	    /* node popup menu */ 
	    if(under instanceof ViewerNode) {
	      ViewerNode vunder = (ViewerNode) under;

	      primarySelect(vunder);
	      refresh();

	      updateWorkingAreaMenus();
	    
	      NodeDetails details = pPrimary.getNodeStatus().getDetails();
	      if(details != null) {
		if(isLocked()) {
                  updateLockedNodeMenu();
		  pPanelLockedNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
		else if(details.getWorkingVersion() == null) {
                  updateCheckedInNodeMenu();
		  pCheckedInNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
		else if(details.getWorkingVersion().isFrozen()) {
                  updateFrozenNodeMenu();
		  pFrozenNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
		else {
		  updateNodeMenu();
		  pNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
	      }
	      else {
		pUndefinedNodePopup.show(e.getComponent(), e.getX(), e.getY());
	      }
	    }

	    /* link popup menu */ 
	    else if(under instanceof ViewerLinkRelationship) {
	      ViewerLinkRelationship lunder = (ViewerLinkRelationship) under;
	      ViewerNode vunder = lunder.getViewerNode();

	      NodeDetails details = vunder.getNodeStatus().getDetails();
	      if(details != null) {
		NodeMod mod = details.getWorkingVersion();
		if((mod != null) && !mod.isFrozen()) {
		  clearSelection();
		  primarySelect(vunder);
		  refresh();		  
		  
		  pSelectedLink = lunder.getLink();
		  pLinkPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	      }
	      else {
		Toolkit.getDefaultToolkit().beep();
	      }
	    }
	  }

	  /* BUTTON3+SHIFT: tool popup menu */ 
	  else if((mods & (on2 | off2)) == on2) {
	    if(under instanceof ViewerNode) {
	      ViewerNode vunder = (ViewerNode) under;
	      primarySelect(vunder);
	      refresh();
	    }
	    
	    updateToolMenu();
	    pToolPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	}
      }
    }

    /* mouse press is over an unused spot on the canvas */ 
    else {
      if(handleMousePressed(e)) 
	return;
      else {
	switch(e.getButton()) {
	case MouseEvent.BUTTON3:
	  {
	    int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	    
	    int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
			MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.SHIFT_DOWN_MASK |
			MouseEvent.ALT_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	    

	    int on2  = (MouseEvent.BUTTON3_DOWN_MASK |
			MouseEvent.SHIFT_DOWN_MASK);
	      
	    int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
			MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.ALT_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	    
	    /* BUTTON3: panel popup menu */ 
	    if((mods & (on1 | off1)) == on1) {
	      updatePanelMenu();
	      pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	    }

	    /* BUTTON3+SHIFT: tool popup menu */ 
	    else if((mods & (on2 | off2)) == on2) {
	      updateDefaultToolMenu();
	      pDefaultToolPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	  }
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  ) 
  {    
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(pRbStart != null) {
	int on1  = 0;
       
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
       
       
	int on2  = (MouseEvent.SHIFT_DOWN_MASK);
       
	int off2 = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
       
	
	int on3  = (MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
       
	int off3 = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
       
	/* a rubberband drag has completed */ 
	if(pRbEnd != null) {
	
	  /* compute world space bounding box of rubberband drag */ 
	  BBox2d bbox = null;
	  {
	    Point2d rs = new Point2d(pRbStart);
	    Point2d re = new Point2d(pRbEnd);
	    
	    Dimension size = pCanvas.getSize();
	    Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
	    
	    double f = -pCameraPos.z() * pPerspFactor;
	    Vector2d persp = new Vector2d(f * pAspect, f);
	    
	    Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());
	    
	    rs.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
	    re.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
	    
	    bbox = new BBox2d(rs, re);
	  }
	  
	  /* BUTTON1: replace selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    clearSelection();	 
	    addSelect(bbox);
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    toggleSelect(bbox);
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    addSelect(bbox);
	  }
	}

	/* rubberband drag started but never updated: 
	     clear the selection unless SHIFT or SHIFT+CTRL are down */ 
	else if(!(((mods & (on2 | off2)) == on2) || 
		  ((mods & (on3 | off3)) == on3))) {
	  clearSelection();
	}
      }
    }
   
    /* reinitialize for the next rubberband drag */ 
    pRbStart = null;
    pRbEnd   = null;
   
    /* refresh the view */ 
    pCanvas.setCursor(Cursor.getDefaultCursor());
    refresh();
  }


  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
 
  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved 
  (
   MouseEvent e
  ) 
  {
    super.mouseMoved(e);

    UserPrefs prefs = UserPrefs.getInstance();
    if(pShowDetailHints) {  
      Object under = objectAtMousePos();
      if((under != null) && (under instanceof ViewerNode)) {
	ViewerNode vunder = (ViewerNode) under;

	if((pLastViewerNodeHint == null) || 
	   !pLastViewerNodeHint.getName().equals(vunder.getNodeStatus().getName())) {
	  pLastViewerNodeHint = vunder.getNodeStatus(); 
	  pViewerNodeHint.setNodeStatus(pLastViewerNodeHint);
	  pViewerNodeHint.setPosition(vunder.getPosition());
	  pViewerNodeHint.setVisible(true);
	  refresh();
	}
      }
      else if(pViewerNodeHint.isVisible()) {
	pLastViewerNodeHint = null;
	pViewerNodeHint.setVisible(false);
	refresh();
      }
    }
    else if(pViewerNodeHint.isVisible()) {
      pLastViewerNodeHint = null;
      pViewerNodeHint.setVisible(false);
      refresh();
    }
  }


  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    /* if the mouse is over a node which has a status, 
         set the name of the node to use when updating new node subpanels */ 
    Object under = objectAtMousePos();
    if(under instanceof ViewerNode) {
      ViewerNode vunder = (ViewerNode) under;
      NodeStatus status = vunder.getNodeStatus();
      if(status != null) 
	pLastDetailsName = status.getName();
    }

    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    boolean undefined = false;

    /* node actions */
    if(under instanceof ViewerNode) {
      ViewerNode vunder = (ViewerNode) under;
      
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;

      default:
	primarySelect(vunder);
	refresh();  
      }
      
      if((prefs.getDetails() != null) &&
	 prefs.getDetails().wasPressed(e))
	doDetails();

      else if((prefs.getUpdateBranch() != null) &&
	 prefs.getUpdateBranch().wasPressed(e))
	doUpdateBranch();
      
      else if((prefs.getNodeViewerMakeRoot() != null) &&
	      prefs.getNodeViewerMakeRoot().wasPressed(e))
	doMakeRoot();
      else if((prefs.getNodeViewerAddRoot() != null) &&
	      prefs.getNodeViewerAddRoot().wasPressed(e))
	doAddRoot();
      else if((prefs.getNodeViewerReplaceRoot() != null) &&
	      prefs.getNodeViewerReplaceRoot().wasPressed(e))
	doReplaceRoot();
      else if((prefs.getHideSelected() != null) &&
	      prefs.getHideSelected().wasPressed(e))
	doRemoveRoot();
      
      else if((prefs.getEdit() != null) &&
	      prefs.getEdit().wasPressed(e))
	doEdit();
      else if((prefs.getEditWithDefault() != null) &&
	      prefs.getEditWithDefault().wasPressed(e))
	doEditWithDefault();
      else if((prefs.getEditAsOwner() != null) &&
	      prefs.getEditAsOwner().wasPressed(e))
	doEditAsOwner();

      else if((prefs.getNodeViewerLink() != null) &&
	      prefs.getNodeViewerLink().wasPressed(e))
	doLink();
      else if((prefs.getNodeViewerUnlink() != null) &&
	      prefs.getNodeViewerUnlink().wasPressed(e))
	doUnlink();
      
      else if((prefs.getNodeViewerAddSecondary() != null) &&
	      prefs.getNodeViewerAddSecondary().wasPressed(e))
	doAddSecondary();

      else if((prefs.getQueueJobs() != null) &&
	      prefs.getQueueJobs().wasPressed(e))
	doQueueJobs();
      else if((prefs.getQueueJobsSpecial() != null) &&
	      prefs.getQueueJobsSpecial().wasPressed(e))
	doQueueJobsSpecial();
      else if((prefs.getPauseJobs() != null) &&
	      prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
      else if((prefs.getResumeJobs() != null) &&
	      prefs.getResumeJobs().wasPressed(e))
	doResumeJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
	doKillJobs();

      else if((prefs.getNodeViewerCheckIn() != null) &&
	      prefs.getNodeViewerCheckIn().wasPressed(e))
	doCheckIn();
      else if((prefs.getNodeViewerCheckOut() != null) &&
	      prefs.getNodeViewerCheckOut().wasPressed(e))
	doCheckOut();
      else if((prefs.getNodeViewerLock() != null) &&
	      prefs.getNodeViewerLock().wasPressed(e))
	doLock();
      else if((prefs.getNodeViewerEvolve() != null) &&
	      prefs.getNodeViewerEvolve().wasPressed(e))
	doEvolve();
      
      else if((prefs.getNodeViewerClone() != null) &&
	      prefs.getNodeViewerClone().wasPressed(e))
	doClone();
      else if((prefs.getNodeViewerExport() != null) &&
	      prefs.getNodeViewerExport().wasPressed(e))
	doExport();
      else if((prefs.getNodeViewerRename() != null) &&
	      prefs.getNodeViewerRename().wasPressed(e))
	doRename();
      else if((prefs.getNodeViewerRenumber() != null) &&
	      prefs.getNodeViewerRenumber().wasPressed(e))
	doRenumber();
      else if((prefs.getRemoveFiles() != null) &&
	      prefs.getRemoveFiles().wasPressed(e))
	doRemoveFiles();

      else if((prefs.getNodeViewerRelease() != null) &&
	      prefs.getNodeViewerRelease().wasPressed(e))
	doRelease();
      else if((prefs.getNodeViewerReleaseView() != null) &&
	      prefs.getNodeViewerReleaseView().wasPressed(e))
	doReleaseView();
      else if((prefs.getNodeViewerDelete() != null) &&
	      prefs.getNodeViewerDelete().wasPressed(e))
	doDelete();

      else 
	undefined = true;
    }
    
    /* link actions */
    else if(under instanceof ViewerLinkRelationship) {
      ViewerLinkRelationship lunder = (ViewerLinkRelationship) under;
      
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	{
	  clearSelection();
	  primarySelect(lunder.getViewerNode());
	  refresh(); 
	}
      }
      
      pSelectedLink = lunder.getLink();
      
      if((prefs.getNodeViewerLinkEdit() != null) &&
	 prefs.getNodeViewerLinkEdit().wasPressed(e))
	doLinkEdit();
      else if((prefs.getNodeViewerLinkUnlink() != null) &&
	 prefs.getNodeViewerLinkUnlink().wasPressed(e))
	doLinkUnlink();
      else 
	undefined = true;
    }
    
    /* panel actions */
    else {
      if((prefs.getUpdate() != null) &&
	 prefs.getUpdate().wasPressed(e))
	doUpdate();
      else if((prefs.getNodeViewerRegisterNewNode() != null) &&
	      prefs.getNodeViewerRegisterNewNode().wasPressed(e))
	doRegister();
      
      else if((prefs.getFrameSelection() != null) &&
	      prefs.getFrameSelection().wasPressed(e))
	doFrameSelection();
      else if((prefs.getFrameAll() != null) &&
	      prefs.getFrameAll().wasPressed(e))
	doFrameAll();
      
      else if((prefs.getAutomaticExpand() != null) &&
	      prefs.getAutomaticExpand().wasPressed(e))
	doAutomaticExpand();
      else if((prefs.getCollapseAll() != null) &&
	      prefs.getCollapseAll().wasPressed(e))
	doCollapseAll();
      else if((prefs.getExpandAll() != null) &&
	      prefs.getExpandAll().wasPressed(e))
	doExpandAll();

      else if((prefs.getExpand1Level() != null) &&
	      prefs.getExpand1Level().wasPressed(e))
	doExpandDepth(1);
      else if((prefs.getExpand2Levels() != null) &&
	      prefs.getExpand2Levels().wasPressed(e))
	doExpandDepth(2);
      else if((prefs.getExpand3Levels() != null) &&
	      prefs.getExpand3Levels().wasPressed(e))
	doExpandDepth(3);
      else if((prefs.getExpand4Levels() != null) &&
	      prefs.getExpand4Levels().wasPressed(e))
	doExpandDepth(4);
      else if((prefs.getExpand5Levels() != null) &&
	      prefs.getExpand5Levels().wasPressed(e))
	doExpandDepth(5);
      else if((prefs.getExpand6Levels() != null) &&
	      prefs.getExpand6Levels().wasPressed(e))
	doExpandDepth(6);
      else if((prefs.getExpand7Levels() != null) &&
	      prefs.getExpand7Levels().wasPressed(e))
	doExpandDepth(7);
      else if((prefs.getExpand8Levels() != null) &&
	      prefs.getExpand8Levels().wasPressed(e))
	doExpandDepth(8);
      else if((prefs.getExpand9Levels() != null) &&
	      prefs.getExpand9Levels().wasPressed(e))
	doExpandDepth(9);
      
      else if((prefs.getNodeViewerShowHideDownstreamNodes() != null) &&
		prefs.getNodeViewerShowHideDownstreamNodes().wasPressed(e))
	doShowHideDownstream();      
      else if((prefs.getHideAll() != null) &&
	      prefs.getHideAll().wasPressed(e))
	doRemoveAllRoots();

      else if((prefs.getNodeViewerShowHideDetailHints() != null) &&
	      prefs.getNodeViewerShowHideDetailHints().wasPressed(e))
	doShowHideDetailHints();
      else if((prefs.getNodeViewerShowHideToolsetHint() != null) &&
	      prefs.getNodeViewerShowHideToolsetHint().wasPressed(e))
	doShowHideToolsetHint();
      else if((prefs.getNodeViewerShowHideEditorHint() != null) &&
	      prefs.getNodeViewerShowHideEditorHint().wasPressed(e))
	doShowHideEditorHint();
      else if((prefs.getNodeViewerShowHideActionHint() != null) &&
	      prefs.getNodeViewerShowHideActionHint().wasPressed(e))
	doShowHideActionHint();

      else
	undefined = true;
    } 

    if(undefined) {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
	clearSelection();
	refresh(); 
      }
    }
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 


  /*-- POPUP MENU LISTNER METHODS ----------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled. 
   */ 
  public void 
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  { 
    clearSelection();
    refresh();
  }
   
  /**
   * This method is called before the popup menu becomes invisible. 
   */ 
  public void
  popupMenuWillBecomeInvisible(PopupMenuEvent e) {} 
  
  /**
   * This method is called before the popup menu becomes visible. 
   */ 
  public void 	
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    updateRecentNodeMenuItems(e);

    /* node menu events */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("details"))
      doDetails();  
    else if(cmd.equals("update-branch"))
      doUpdateBranch();

    else if(cmd.equals("make-root"))
      doMakeRoot();
    else if(cmd.equals("add-root"))
      doAddRoot();
    else if(cmd.equals("replace-root"))
      doReplaceRoot();
    else if(cmd.equals("remove-root"))
      doRemoveRoot();
    else if(cmd.equals("remove-all-roots"))
      doRemoveAllRoots();

    else if(cmd.startsWith("author-view:")) 
      doChangeAuthorView(cmd.substring(12));    

    else if(cmd.equals("edit"))
      doEdit();
    else if(cmd.equals("edit-with-default"))
      doEditWithDefault();
    else if(cmd.startsWith("edit-with:")) 
      doEditWith(cmd.substring(10));  
    else if(cmd.equals("edit-as-owner"))
      doEditAsOwner();  

    else if(cmd.equals("link")) 
      doLink();    
    else if(cmd.equals("unlink")) 
      doUnlink(); 

    else if(cmd.equals("add-secondary")) 
      doAddSecondary();
    else if(cmd.startsWith("remove-secondary:")) 
      doRemoveSecondary(cmd.substring(17)); 

    else if(cmd.equals("queue-jobs"))
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special"))
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("preempt-jobs"))
      doPreemptJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("check-in"))
      doCheckIn();
    else if(cmd.equals("check-out"))
      doCheckOut();
    else if(cmd.equals("lock"))
      doLock();
    else if(cmd.equals("evolve"))
      doEvolve();
    else if(cmd.equals("restore"))
      doRestore();

    else if(cmd.equals("clone"))
      doClone();
    else if(cmd.equals("export"))
      doExport();
    else if(cmd.equals("rename"))
      doRename();
    else if(cmd.equals("renumber"))
      doRenumber();
    else if(cmd.equals("remove-files"))
      doRemoveFiles();

    else if(cmd.equals("release"))
      doRelease();
    else if(cmd.equals("release-view"))
      doReleaseView();
    else if(cmd.equals("delete"))
      doDelete();

    /* link menu events */ 
    else if(cmd.equals("link-edit")) 
      doLinkEdit(); 
    else if(cmd.equals("link-unlink"))
      doLinkUnlink();

    /* panel menu events */ 
    else if(cmd.equals("update"))
      doUpdate();
    else if(cmd.equals("register"))
      doRegister();
    else if(cmd.equals("frame-selection"))
      doFrameSelection();
    else if(cmd.equals("frame-all"))
      doFrameAll();
    else if(cmd.equals("automatic-expand"))
      doAutomaticExpand();
    else if(cmd.equals("expand-all"))
      doExpandAll();
    else if(cmd.equals("collapse-all"))
      doCollapseAll();
    else if(cmd.equals("show-hide-downstream"))
      doShowHideDownstream();
    else if(cmd.equals("show-hide-detail-hints"))
      doShowHideDetailHints();
    else if(cmd.equals("show-hide-toolset-hint"))
      doShowHideToolsetHint();
    else if(cmd.equals("show-hide-editor-hint"))
      doShowHideEditorHint();
    else if(cmd.equals("show-hide-action-hint"))
      doShowHideActionHint();

    /* tool menu events */ 
    else if(cmd.startsWith("run-tool:")) 
      doRunTool(cmd.substring(9));    

    else {
      clearSelection();
      refresh();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the status of all nodes.
   */ 
  private synchronized void
  doUpdate()
  { 
    clearSelection();

    for(String name : pRoots.keySet()) 
      pRoots.put(name, null);
    
    PanelUpdater pu = new PanelUpdater(this, false, false, null);
    pu.start();
  }
  
  /**
   * Update the status of all upstread of the primary selected node. 
   */ 
  private synchronized void
  doUpdateBranch()
  { 
    TreeSet<String> branches = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) {
      NodeDetails details = vnode.getNodeStatus().getDetails();
      if(details != null) 
        branches.add(details.getName());
    }

    if(branches.isEmpty())
      return;
    
    clearSelection();

    for(String name : pRoots.keySet()) 
      pRoots.put(name, null);
    
    PanelUpdater pu = new PanelUpdater(this, false, true, branches);
    pu.start();    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node details panels with the current primary selected node status.
   */ 
  private synchronized void
  doDetails()
  {
    if(pPrimary != null) {
      pLastDetailsName = pPrimary.getNodeStatus().getName();

      PanelUpdater pu = new PanelUpdater(this, true, true, null);
      pu.start();
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make the current primary selection the only root node.
   */ 
  private synchronized void
  doMakeRoot()
  {
    pPinnedPos  = pPrimary.getPosition();
    pPinnedPath = new NodePath(pPrimary.getNodePath().getCurrentName());

    TreeSet<String> roots = new TreeSet<String>();
    roots.add(getPrimarySelectionName());
    setRoots(roots);

    clearSelection();
  }

  /**
   * Add the current primary selection to the set of root nodes.
   */ 
  private synchronized void
  doAddRoot()
  {
    pPinnedPos  = pPrimary.getPosition();
    pPinnedPath = new NodePath(pPrimary.getNodePath().getCurrentName());

    addRoots(getSelectedNames());

    clearSelection();
  }

  /**
   * Replace the root node of the current primary selection with the primary selection.
   */ 
  private synchronized void
  doReplaceRoot()
  {
    pPinnedPos  = pPrimary.getPosition();
    pPinnedPath = new NodePath(pPrimary.getNodePath().getCurrentName());

    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.removeAll(getSelectedRootNames());
    roots.addAll(getSelectedNames());
    setRoots(roots);

    clearSelection();
  }
  
  /**
   * Remove the root node of the current primary selection from the set of roots nodes.
   */ 
  private synchronized void
  doRemoveRoot()
  {
    removeRoots(getSelectedRootNames());

    clearSelection();
  }

  /**
   * Remove all of the roots nodes.
   */ 
  private synchronized void
  doRemoveAllRoots()
  {
    setRoots(new TreeSet<String>());
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Change working area view.
   */ 
  private void 
  doChangeAuthorView
  (
   String workingArea
  ) 
  {
    String parts[] = workingArea.split(":");
    assert(parts.length == 2);

    String author = parts[0];
    String view   = parts[1];
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      setAuthorView(author, view);       
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the primary selected node with the editor specified by the node version.
   */ 
  private void 
  doEdit() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com);
	  task.start();
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Edit/View the primary selected node using the default editor for the file type.
   */ 
  private void 
  doEditWithDefault() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, true, false);
	  task.start();
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Edit/View the primary selected node with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    String parts[] = editor.split(":");
    assert(parts.length == 3);
    
    String ename   = parts[0];
    VersionID evid = new VersionID(parts[1]);
    String evendor = parts[2];

    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, ename, evid, evendor);
	  task.start();
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Edit/View the primary selected node with the permissions of the owner of the node.
   */ 
  private void 
  doEditAsOwner() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	boolean isWorking = true;
	NodeCommon com = details.getWorkingVersion();
	if(com == null) {
	  com = details.getLatestVersion();
	  isWorking = false;
	}

	if(com != null) {
	  EditTask task = new EditTask(com, false, isWorking);
	  task.start();
	}
      }
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create (or modify) a link from the secondary selected nodes to the primary selected node.
   */ 
  private synchronized void 
  doLink() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
	  TreeSet<String> sources = new TreeSet<String>(); 
	  for(ViewerNode vnode : pSelected.values()) {
	    NodeDetails sdetails = vnode.getNodeStatus().getDetails();
	    if((sdetails != null) && (sdetails.getWorkingVersion() != null)) 
	      sources.add(sdetails.getName());
	  }
	  sources.remove(details.getName());
	  
	  if(!sources.isEmpty()) {  
	    if(pCreateLinkDialog == null) 
	      pCreateLinkDialog = new JCreateLinkDialog(getTopFrame());

	    pCreateLinkDialog.updateLink();
	    pCreateLinkDialog.setVisible(true);
	    
	    if(pCreateLinkDialog.wasConfirmed()) {
	      LinkPolicy policy    = pCreateLinkDialog.getPolicy();
	      LinkRelationship rel = pCreateLinkDialog.getRelationship();
	      Integer offset       = pCreateLinkDialog.getFrameOffset();
	      
	      LinkTask task = 
		new LinkTask(details.getName(), sources, policy, rel, offset);
	      task.start();
	    }
	  }
	}
      }
    }
    
    clearSelection();
    refresh(); 
  }


  /**
   * Unlink the secondary selected nodes from the primary selected node.
   */ 
  private synchronized void 
  doUnlink()
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
	  TreeSet<String> sources = new TreeSet<String>(); 
	  for(ViewerNode vnode : pSelected.values()) {
	    NodeDetails sdetails = vnode.getNodeStatus().getDetails();
	    if(sdetails.getWorkingVersion() != null) 
	      sources.add(sdetails.getName());
	  }
	  sources.remove(details.getName());
	  
	  if(!sources.isEmpty()) {    
	    UnlinkTask task = new UnlinkTask(details.getName(), sources);
	    task.start();
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a secondary file sequence.
   */ 
  private synchronized void 
  doAddSecondary() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
	  if(pAddSecondaryDialog == null) 
	    pAddSecondaryDialog = new JAddSecondaryDialog(getTopFrame());

	  pAddSecondaryDialog.updateNode(pAuthor, pView, mod);
	  pAddSecondaryDialog.setVisible(true);
	  
	  if(pAddSecondaryDialog.wasConfirmed()) {
	    FileSeq fseq = pAddSecondaryDialog.getFileSequence();
	    if(fseq != null) {
	      AddSecondaryTask task = new AddSecondaryTask(mod.getName(), fseq);
	      task.start();
	    }
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Remove a secondary file sequence.
   */ 
  private synchronized void 
  doRemoveSecondary
  (
   String name
  ) 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
	  FileSeq fseq = pRemoveSecondarySeqs.get(name);
	  if(fseq != null) {
	    RemoveSecondaryTask task = new RemoveSecondaryTask(details.getName(), fseq);
	    task.start();
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Export the node properties of the primary selected node to the rest of the selected 
   * nodes.
   */ 
  private synchronized void 
  doExport() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	TreeSet<String> targets = new TreeSet<String>(); 
	for(ViewerNode vnode : pSelected.values()) {
	  NodeDetails tdetails = vnode.getNodeStatus().getDetails();
	  if(tdetails != null) {
	    NodeMod tmod = tdetails.getWorkingVersion();
	    if((tmod != null) && !tmod.isFrozen())
	      targets.add(tdetails.getName());
	  }
	}
	targets.remove(details.getName());
	
	if(!targets.isEmpty()) {
	  NodeMod mod = details.getWorkingVersion();
	  if(mod != null) {
	    if(pExportDialog == null) 
	      pExportDialog = new JExportDialog(pGroupID, getTopFrame());

	    synchronized(pExportDialog) {
	      pExportDialog.updateNode(mod);
	      pExportDialog.setVisible(true);
	    }
	    
	    if(pExportDialog.wasConfirmed()) {
	      ExportTask task = new ExportTask(mod.getName(), targets);
	      task.start();	      
	    }
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Rename the primary seleted node.
   */ 
  private synchronized void 
  doRename() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen() && (details.getLatestVersion() == null)) { 
	  if(pRenameDialog == null) 
	    pRenameDialog = new JRenameDialog(getTopFrame());

	  pRenameDialog.updateNode(mod);
	  pRenameDialog.setVisible(true);
	
	  if(pRenameDialog.wasConfirmed()) {
	    try {
	      FilePattern fpat = pRenameDialog.getNewFilePattern();

	      RenameTask task = 
		new RenameTask(pPrimary.getNodePath(), fpat, pRenameDialog.renameFiles());
	      task.start();
	    }
	    catch(PipelineException ex) {
	      UIMaster.getInstance().showErrorDialog(ex);
	    }
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Renumber frame range of the primary seleted node.
   */ 
  private synchronized void 
  doRenumber() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen() && (mod.getPrimarySequence().hasFrameNumbers())) {
	  if(pRenumberDialog == null) 
	    pRenumberDialog = new JRenumberDialog(getTopFrame());

	  pRenumberDialog.updateNode(mod);
	  pRenumberDialog.setVisible(true);
	
	  if(pRenumberDialog.wasConfirmed()) {
	    FrameRange range    = pRenumberDialog.getFrameRange();
	    boolean removeFiles = pRenumberDialog.removeFiles();

	    boolean confirmed = true;
	    if(range.numFrames() > 10000) {
	      JConfirmFrameRangeDialog diag = 
		new JConfirmFrameRangeDialog(getTopFrame(), range);
	      diag.setVisible(true);
	      confirmed = diag.wasConfirmed();
	    }

	    if(confirmed) {
	      RenumberTask task = new RenumberTask(mod.getName(), range, removeFiles); 
	      task.start();
	    }
	  }
	}
      }
    }
    
    clearSelection();
    refresh(); 
  }
  
  /**
   * Register a new node based on the primary selected node.
   */ 
  private synchronized void 
  doClone() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeMod mod = details.getWorkingVersion();
	if(mod == null) 
	  return; 
	    
	if(pCloneDialog == null) 
	  pCloneDialog = new JCloneDialog(pGroupID, getTopFrame());

	pCloneDialog.updateNode(pAuthor, pView, mod);
	pCloneDialog.setVisible(true);

	TreeSet<String> names = pCloneDialog.getRegistered();
	if(!names.isEmpty()) 
	  addRoots(names);
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Register a new node.
   */ 
  private synchronized void 
  doRegister() 
  {
    if(pRegisterDialog == null) 
      pRegisterDialog = new JRegisterDialog(pGroupID, getTopFrame());

    pRegisterDialog.updateNode(pAuthor, pView);
    pRegisterDialog.setVisible(true); 

    TreeSet<String> names = pRegisterDialog.getRegistered();
    if(!names.isEmpty()) 
      addRoots(names);
  }

  /**
   * Release the primary selected node.
   */ 
  private synchronized void 
  doRelease() 
  {
    String text = null;
    TreeSet<String> names = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if(mod != null) {
	  names.add(mod.getName());
	  text = mod.getPrimarySequence().toString();
	}
      }
    }

    if(!names.isEmpty()) {
      String header = null;
      if(names.size() == 1) 
	header = ("Release Node:  " + text);
      else 
	header = ("Release Multiple Nodes:");

      if(pReleaseDialog == null) 
	pReleaseDialog = new JReleaseDialog(getTopFrame());

      pReleaseDialog.updateHeader(header);
      pReleaseDialog.setVisible(true);
      if(pReleaseDialog.wasConfirmed()) {

	boolean wasConfirmed = false;
	if(names.size() == 1) {
	  JConfirmDialog confirm = new JConfirmDialog(getTopFrame(), "Are you sure?");
	  confirm.setVisible(true);
	  wasConfirmed = confirm.wasConfirmed();
	}
	else {
	  JConfirmListDialog confirm = 
	    new JConfirmListDialog(getTopFrame(), "Are you sure?", 
				   "Nodes to Release:", names);
	  confirm.setVisible(true);
	  wasConfirmed = confirm.wasConfirmed();
	}

	if(wasConfirmed) {
	  ReleaseTask task = 
	    new ReleaseTask(pAuthor, pView, names, pReleaseDialog.removeFiles(), false);
	  task.start();
	}
      }
    }
    
    clearSelection();
    refresh(); 
  }

  /**
   * Release nodes from the current working area view.
   */ 
  private synchronized void 
  doReleaseView() 
  {
    if(pReleaseViewDialog == null) 
      pReleaseViewDialog = new JReleaseViewDialog(getTopFrame());

    pReleaseViewDialog.setVisible(true);
    if(pReleaseViewDialog.wasConfirmed()) {
      ReleaseViewTask task = 
	new ReleaseViewTask(pAuthor, pView, pReleaseViewDialog.getPattern(), 
			    pReleaseViewDialog.removeFiles(), 
			    pReleaseViewDialog.removeWorkingArea());
      task.start();
    }
 
    clearSelection();
    refresh(); 
  }

  /**
   * Remove all primary/secondary files associated with the selected nodes.
   */ 
  private synchronized void 
  doRemoveFiles() 
  {
    TreeSet<String> names = new TreeSet<String>();
    boolean confirmed = false;
    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
	  if(confirmed || mod.isActionEnabled()) 
	    names.add(mod.getName());
	  else {
	    JConfirmDialog confirm = 
	      new JConfirmDialog(getTopFrame(), 
				 "Remove from Nodes without enabled Actions?");
	    confirm.setVisible(true);
	    confirmed = confirm.wasConfirmed(); 

	    if(confirmed) 
	      names.add(mod.getName());
	  }
	}
      }
    }

    if(!names.isEmpty()) {
      RemoveFilesTask task = new RemoveFilesTask(names);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Delete the primary selected node.
   */ 
  private synchronized void 
  doDelete() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	
	String text = null;
	{
	  NodeMod mod = details.getWorkingVersion();
	  NodeVersion vsn = details.getLatestVersion();
	  if(mod != null) 
	    text = mod.getPrimarySequence().toString();
	  else if(vsn != null) 
	    text = mod.getPrimarySequence().toString();
	}
    
	if(pDeleteDialog == null) 
	  pDeleteDialog = new JDeleteDialog(getTopFrame());

	pDeleteDialog.updateHeader("Delete Node:  " + text);
	pDeleteDialog.setVisible(true);
      
	if(pDeleteDialog.wasConfirmed()) {
	  JConfirmDialog confirm = new JConfirmDialog(getTopFrame(), "Are you sure?");
	  confirm.setVisible(true);
	  if(confirm.wasConfirmed()) {
	    DeleteTask task = new DeleteTask(details.getName(), pDeleteDialog.removeFiles());
	    task.start();
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Queue jobs to the queue for the primary selected node and all nodes upstream of it.
   */ 
  private synchronized void 
  doQueueJobs() 
  {
    TreeSet<String> roots = new TreeSet<String>();
    for(String name : getMostDownstreamOfSelectedNames()) {
      for(ViewerNode vnode : pSelected.values()) {
	NodeStatus status = vnode.getNodeStatus();
	if((status != null) && status.getName().equals(name)) {
	  NodeDetails details = status.getDetails();
	  if(details != null) {
	    NodeMod mod = details.getWorkingVersion();
	    if((mod != null) && !mod.isFrozen()) 
	      roots.add(name);
	  }
	  break;
	}
      }
    }
    
    if(!roots.isEmpty()) {
      QueueJobsTask task = new QueueJobsTask(roots);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Queue jobs to the queue for the primary selected node and all nodes upstream of it 
   * with special job requirements.
   */ 
  private synchronized void 
  doQueueJobsSpecial() 
  {
    TreeSet<String> roots = new TreeSet<String>();
    for(String name : getMostDownstreamOfSelectedNames()) {
      for(ViewerNode vnode : pSelected.values()) {
	NodeStatus status = vnode.getNodeStatus();
	if((status != null) && status.getName().equals(name)) {
	  NodeDetails details = status.getDetails();
	  if(details != null) {
	    NodeMod mod = details.getWorkingVersion();
	    if((mod != null) && !mod.isFrozen()) 
	      roots.add(name);
	  }
	  break;
	}
      }
    }
    
    if(!roots.isEmpty()) {
      JQueueJobsDialog diag = UIMaster.getInstance().showQueueJobsDialog();
      if(diag.wasConfirmed()) {
	Integer batchSize = null;
	if(diag.overrideBatchSize()) 
	  batchSize = diag.getBatchSize();

	Integer priority = null;
	if(diag.overridePriority()) 
	  priority = diag.getPriority();

	Integer interval = null;
	if(diag.overrideRampUp()) 
	  interval = diag.getRampUp();
	  
	TreeSet<String> selectionKeys = null;
	if(diag.overrideSelectionKeys()) 
	  selectionKeys = diag.getSelectionKeys();

	TreeSet<String> licenseKeys = null;
	if(diag.overrideLicenseKeys()) 
	  licenseKeys = diag.getLicenseKeys();

	QueueJobsTask task = 
	  new QueueJobsTask(roots, batchSize, priority, interval, 
			    selectionKeys, licenseKeys);
	task.start();
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Pause all waiting jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doPauseJobs() 
  {
    TreeSet<NodeID> pausedNodes = new TreeSet<NodeID>();
    TreeSet<Long> pausedJobs    = new TreeSet<Long>();

    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          pausedNodes.add(status.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);

          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
              assert(jobIDs[wk] != null);
              pausedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!pausedNodes.isEmpty() || !pausedJobs.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(pausedNodes, pausedJobs);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Resume execution of all paused jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doResumeJobs() 
  {
    TreeSet<NodeID> resumedNodes = new TreeSet<NodeID>();
    TreeSet<Long> resumedJobs    = new TreeSet<Long>();

    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          resumedNodes.add(status.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);

          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Paused:
              assert(jobIDs[wk] != null);
              resumedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!resumedNodes.isEmpty() || !resumedJobs.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumedNodes, resumedJobs);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Preempt all jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doPreemptJobs() 
  {
    TreeSet<NodeID> preemptedNodes = new TreeSet<NodeID>();
    TreeSet<Long> preemptedJobs    = new TreeSet<Long>();

    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          preemptedNodes.add(status.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);

          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
            case Paused:
            case Running:
              assert(jobIDs[wk] != null);
              preemptedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!preemptedNodes.isEmpty() || !preemptedJobs.isEmpty()) {
      PreemptJobsTask task = new PreemptJobsTask(preemptedNodes, preemptedJobs);
      task.start();
    }

    clearSelection();
    refresh(); 
  }
    
  /**
   * Kill all jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doKillJobs() 
  {
    TreeSet<NodeID> killedNodes = new TreeSet<NodeID>();
    TreeSet<Long> killedJobs    = new TreeSet<Long>();

    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          killedNodes.add(status.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);

          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
            case Paused:
            case Running:
              assert(jobIDs[wk] != null);
              killedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!killedNodes.isEmpty() || !killedJobs.isEmpty()) {
      KillJobsTask task = new KillJobsTask(killedNodes, killedJobs);
      task.start();
    }

    clearSelection();
    refresh();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Check-in the primary selected node.
   */ 
  private synchronized void 
  doCheckIn() 
  {
    try {
      TreeSet<String> roots = getMostDownstreamOfSelectedNames();
      if(!roots.isEmpty()) {
	String header    = null;
	VersionID latest = null;
	boolean multiple = false; 
	
	if(roots.size() > 1) {
	  header = "Check-In:  Multiple Nodes";
	  multiple = true;
	}
	else {
	  NodeStatus status   = null;
	  NodeDetails details = null;
	  for(ViewerNode vnode : pSelected.values()) {
	    status = vnode.getNodeStatus();
	    if((status != null) && status.getName().equals(roots.first())) {
	      details = status.getDetails();
	      break;
	    }
	  }
	  
	  if(details == null) 
	    return; 
	  
	  header = ("Check-In:  " + status);
	  
	  if(details.getLatestVersion() != null) 
	    latest = details.getLatestVersion().getVersionID();
	}
 		
	if(pCheckInDialog == null) 
	  pCheckInDialog = new JCheckInDialog(getTopFrame());

	pCheckInDialog.updateNameVersion(header, latest, multiple);
	pCheckInDialog.setVisible(true);
	
	if(pCheckInDialog.wasConfirmed()) {
	  String desc = pCheckInDialog.getDescription();
	  assert((desc != null) && (desc.length() > 0));
	  
	  VersionID.Level level = pCheckInDialog.getLevel();
	  
	  CheckInTask task = new CheckInTask(roots, desc, level);
	  task.start();
	}
      }
    }
    finally {
      clearSelection();
      refresh(); 
    }
  }
      

  /**
   * Check-out the primary selected node.
   */ 
  private synchronized void 
  doCheckOut() 
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();

    TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
    TreeMap<String,TreeSet<VersionID>> offline  = new TreeMap<String,TreeSet<VersionID>>();

    for(String name : getMostDownstreamOfSelectedNames()) {
      if(!versions.containsKey(name)) {
	try {
	  versions.put(name, client.getCheckedInVersionIDs(name));
	  offline.put(name, client.getOfflineVersionIDs(name));
	}
	catch (PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
      }
    }
    
    if(pCheckOutDialog == null) 
      pCheckOutDialog = new JCheckOutDialog(getTopFrame());

    pCheckOutDialog.updateVersions(versions, offline);
    pCheckOutDialog.setVisible(true);	
    if(pCheckOutDialog.wasConfirmed()) {
      CheckOutTask task = 
	new CheckOutTask(pCheckOutDialog.getVersionIDs(), 
			 pCheckOutDialog.getModes(), 
			 pCheckOutDialog.getMethods());
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Lock the primary selected node to specific checked-in version.
   */ 
  private synchronized void 
  doLock() 
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();

    TreeMap<String,VersionID> base = new TreeMap<String,VersionID>();
    TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
    TreeMap<String,TreeSet<VersionID>> offline  = new TreeMap<String,TreeSet<VersionID>>();

    for(String name : getMostDownstreamOfSelectedNames()) {
      if(!base.containsKey(name)) {
	try {
	  NodeMod mod = client.getWorkingVersion(pAuthor, pView, name);
	  if(mod != null) {
	    VersionID vid = mod.getWorkingID();
	    if(vid != null) 
	      base.put(name, vid);
	  }
	}
	catch (PipelineException ex) {
	  base.put(name, null);
	}

	try {
	  versions.put(name, client.getCheckedInVersionIDs(name));
	  offline.put(name, client.getOfflineVersionIDs(name));
	}
	catch (PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
      }
    }
    
    if(base.isEmpty() && versions.isEmpty()) {
      master.showErrorDialog("Error:", "None of the selected nodes can be locked!");
      return;
    }
    
    if(pLockDialog == null) 
      pLockDialog = new JLockDialog(getTopFrame());

    pLockDialog.updateVersions(base, versions, offline);
    pLockDialog.setVisible(true);	
    if(pLockDialog.wasConfirmed()) {
      LockTask task = new LockTask(pLockDialog.getVersionIDs());
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Evolve the primary selected node.
   */ 
  private synchronized void 
  doEvolve() 
  {
    if(pPrimary != null) {
      NodeStatus status = pPrimary.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	NodeMod work = details.getWorkingVersion();
	if((work != null) && !work.isFrozen()) {
	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.getMasterMgrClient();

	  TreeSet<VersionID> versions = null;
	  TreeSet<VersionID> offline  = null;
	  try {
	    versions = client.getCheckedInVersionIDs(status.getName());
	    offline  = client.getOfflineVersionIDs(status.getName());
	  }
	  catch (PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  	
	  if(pEvolveDialog == null) 
	    pEvolveDialog = new JEvolveDialog(getTopFrame());

	  pEvolveDialog.updateNameVersions
	    ("Evolve Version:  " + status, work.getWorkingID(), versions, offline);
	  pEvolveDialog.setVisible(true);
	  
	  if(pEvolveDialog.wasConfirmed()) {
	    VersionID vid = pEvolveDialog.getVersionID();
	    if(vid != null) {
	      EvolveTask task = new EvolveTask(status.getName(), vid);
	      task.start();
	    }
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Restore offline checked-in versions of the primary selected node.
   */ 
  private synchronized void 
  doRestore() 
  {
    RestoreQueryTask task = new RestoreQueryTask(getSelectedNames());
    task.start();

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Edit an existing link.
   */ 
  private synchronized void 
  doLinkEdit()
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      NodeMod mod = details.getWorkingVersion();
      if((mod != null) && !mod.isFrozen()) {
	if((pSelectedLink != null) && (pSelectedLink instanceof LinkMod)) {
	  if(pEditLinkDialog == null) 
	    pEditLinkDialog = new JEditLinkDialog(getTopFrame());

	  LinkMod link = (LinkMod) pSelectedLink;
	  pEditLinkDialog.updateLink(link);
	  pEditLinkDialog.setVisible(true);
	  
	  if(pEditLinkDialog.wasConfirmed()) {
	    LinkPolicy policy    = pEditLinkDialog.getPolicy();
	    LinkRelationship rel = pEditLinkDialog.getRelationship();
	    Integer offset       = pEditLinkDialog.getFrameOffset();
	    
	    TreeSet<String> sources = new TreeSet<String>();
	    sources.add(link.getName());
	    
	    LinkTask task = 
	      new LinkTask(details.getName(), sources, policy, rel, offset);
	    task.start();
	  }
	}
      }
    }
    
    clearSelection();
    refresh(); 
  }

  /**
   * Unlink nodes of the currently selected link.
   */ 
  private synchronized void 
  doLinkUnlink()
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails(); 
      NodeMod mod = details.getWorkingVersion();
      if((mod != null) && !mod.isFrozen()) {
	if((pSelectedLink != null) && (pSelectedLink instanceof LinkMod)) {
	  LinkMod link = (LinkMod) pSelectedLink;
	  
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(link.getName());

	  UnlinkTask task = new UnlinkTask(details.getName(), sources);
	  task.start();
	}
      }
    }

    clearSelection();
    refresh(); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the bounds of the currently selected nodes.
   */ 
  private synchronized void 
  doFrameSelection() 
  {
    doFrameNodes(pSelected.values());
  }

  /**
   * Move the camera to frame all active nodes.
   */ 
  private synchronized void 
  doFrameAll() 
  {
    doFrameNodes(pViewerNodes.values());
  }

  /**
   * Move the camera to frame the given set of nodes.
   */ 
  private synchronized void 
  doFrameNodes
  (
   Collection<ViewerNode> vnodes
  ) 
  {
    doFrameBounds(getNodeBounds(vnodes));
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set a fixed node expansion depth.
   */
  private synchronized void 
  doExpandDepth
  (
   int depth
  ) 
  {
    pExpandDepth = depth;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the nodes.
   */ 
  private synchronized void
  doAutomaticExpand()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the nodes.
   */ 
  private synchronized void
  doExpandAll()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the nodes.
   */ 
  private synchronized void
  doCollapseAll()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.CollapseAll;
    updateUniverse();
  }

  /**
   * Show/Hide the node status detail hints.
   */ 
  private synchronized void
  doShowHideDetailHints()
  {
    clearSelection();
    pShowDetailHints = !pShowDetailHints;
    updateUniverse();
  }
  
  /**
   * Show/Hide the Toolset property as part of the node detail hints.
   */ 
  private synchronized void
  doShowHideToolsetHint()
  {
    clearSelection();
    if(pViewerNodeHint != null) 
      pViewerNodeHint.setShowToolset(!pViewerNodeHint.showToolset());
    updateUniverse();
  }
  
  /**
   * Show/Hide the Editor property as part of the node detail hints.
   */ 
  private synchronized void
  doShowHideEditorHint()
  {
    clearSelection();
    if(pViewerNodeHint != null) 
      pViewerNodeHint.setShowEditor(!pViewerNodeHint.showEditor());
    updateUniverse();
  }
  
  /**
   * Show/Hide the Action property as part of the node detail hints.
   */ 
  private synchronized void
  doShowHideActionHint()
  {
    clearSelection();
    if(pViewerNodeHint != null) 
      pViewerNodeHint.setShowAction(!pViewerNodeHint.showAction());
    updateUniverse();
  }
  
  /**
   * Show/Hide the downstream node tree.
   */ 
  private synchronized void
  doShowHideDownstream()
  {
    clearSelection();
    pShowDownstream = !pShowDownstream;
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the given tool plugin.
   */ 
  private synchronized void 
  doRunTool
  (
   String name 
  ) 
  {
    String parts[] = name.split(":");
    assert(parts.length == 3);
    
    String tname   = parts[0];
    VersionID tvid = new VersionID(parts[1]);
    String tvendor = parts[2];
    
    try {
      BaseTool tool = PluginMgrClient.getInstance().newTool(tname, tvid, tvendor);

      String primary = null;
      if(pPrimary != null) 
	primary = pPrimary.getNodeStatus().getName();
      
      TreeMap<String,NodeStatus> selected = new TreeMap<String,NodeStatus>();
      for(ViewerNode vnode : pSelected.values()) {
	NodeStatus status = vnode.getNodeStatus();
	NodeStatus ostatus = selected.get(status.getName());
	if((ostatus == null) || (ostatus.getDetails() == null)) 
	  selected.put(status.getName(), status);
      }

      TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
      tool.initExecution(primary, selected, roots);

      ToolOpTask task = new ToolOpTask(tool, pGroupID);
      task.start();
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
    finally {
      clearSelection();
      refresh(); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    /* root nodes */ 
    if(!pRoots.isEmpty()) 
      encoder.encode("Roots", new TreeSet<String>(pRoots.keySet()));

    /* the initial center of the node layout */ 
    {
      BBox2d bbox = getNodeBounds(pViewerNodes.values());
      if(bbox != null) 
	encoder.encode("InitialCenter", bbox.getCenter());
    } 

    /* whether to show the downstram links */
    encoder.encode("ShowDetailHints", pShowDetailHints);

    if(pViewerNodeHint != null) {
      encoder.encode("ShowToolsetHints", pViewerNodeHint.showToolset());
      encoder.encode("ShowActionHints", pViewerNodeHint.showAction());
      encoder.encode("ShowEditorHints", pViewerNodeHint.showEditor());
    }

    encoder.encode("ShowDownstream", pShowDownstream);
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    /* root nodes */  
    if(UIMaster.getInstance().restoreSelections()) {
      TreeSet<String> roots = (TreeSet<String>) decoder.decode("Roots");
      if(roots != null) {
	for(String name : roots) 
	  pRoots.put(name, null);
      }
    }

    /* the initial center of the node layout */ 
    pInitialCenter = (Point2d) decoder.decode("InitialCenter");
    
    /* whether to show the node status detail hints */    
    {
      Boolean show = (Boolean) decoder.decode("ShowDetailHints");
      if(show != null) 
	pShowDetailHints = show; 

      if(pViewerNodeHint != null) {
	Boolean tset = (Boolean) decoder.decode("ShowToolsetHints");
	if(tset != null) 
	  pViewerNodeHint.setShowToolset(tset);
	
	Boolean act = (Boolean) decoder.decode("ShowActionHints");
	if(act != null) 
	  pViewerNodeHint.setShowAction(act);

	Boolean edit = (Boolean) decoder.decode("ShowEditorHints");
	if(edit != null) 
	  pViewerNodeHint.setShowEditor(edit);
      }
    }

    /* whether to show the downstram links */    
    {
      Boolean show = (Boolean) decoder.decode("ShowDownstream");
      if(show != null) 
	pShowDownstream = show; 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Edit/View the primary file sequence of the given node version.
   */ 
  private
  class EditTask
    extends UIMaster.EditTask
  {
    public 
    EditTask
    (
     NodeCommon com
    ) 
    {
      UIMaster.getInstance().super(pGroupID, com, false, pAuthor, pView, false);
      setName("JNodeViewerPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     boolean useDefault, 
     boolean substitute
    ) 
    {
      UIMaster.getInstance().super(pGroupID, com, useDefault, pAuthor, pView, substitute);
      setName("JNodeViewerPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid, 
     String evendor
    ) 
    {
      UIMaster.getInstance().super
	(pGroupID, com, ename, evid, evendor, pAuthor, pView, false);
      setName("JNodeViewerPanel:EditTask");
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Create (or modify) a link from the given source nodes to the target node.
   */ 
  private
  class LinkTask
    extends Thread
  {
    public 
    LinkTask
    (
     String target, 
     TreeSet<String> sources, 
     LinkPolicy policy, 
     LinkRelationship rel, 
     Integer offset
    ) 
    {
      super("JNodeViewerPanel:LinkTask");

      pTarget = target;
      pSources = sources;
      pPolicy = policy; 
      pRelationship = rel;
      pFrameOffset = offset;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Linking Nodes...")) {
	TreeSet<String> linked = new TreeSet<String>();
	try {
	  for(String source : pSources) {
	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    client.link(pAuthor, pView, pTarget, source, 
			pPolicy, pRelationship, pFrameOffset);
	    linked.add(source);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	invalidateRootsContaining(pTarget);
	removeRoots(linked);
      }
    }
    
    private String pTarget;
    private TreeSet<String> pSources;
    private LinkPolicy pPolicy; 
    private LinkRelationship pRelationship; 
    private Integer pFrameOffset;
  }
  
  /** 
   * Unlink the given source nodes from the target node.
   */ 
  private
  class UnlinkTask
    extends Thread
  {
    public 
    UnlinkTask
    (
     String target, 
     TreeSet<String> sources
    ) 
    {
      super("JNodeViewerPanel:UnlinkTask");

      pTarget = target;
      pSources = sources;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Unlinking Nodes...")) {
	TreeSet<String> unlinked = new TreeSet<String>();
	try {
	  for(String source : pSources) {
	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    client.unlink(pAuthor, pView, pTarget, source);
	    unlinked.add(source);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	addRoots(unlinked);
      }
    }
    
    private String           pTarget;
    private TreeSet<String>  pSources;
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Add a secondary file sequence to the given node.
   */ 
  private
  class AddSecondaryTask
    extends Thread
  {
    public 
    AddSecondaryTask
    (
     String target, 
     FileSeq fseq
    ) 
    {
      super("JNodeViewerPanel:AddSecondaryTask");

      pTarget = target;
      pFileSeq = fseq; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Adding Secondary File Sequence...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  client.addSecondary(pAuthor, pView, pTarget, pFileSeq);
	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	  return;
 	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }
    
    private String   pTarget;
    private FileSeq  pFileSeq; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove a secondary file sequence to the given node.
   */ 
  private
  class RemoveSecondaryTask
    extends Thread
  {
    public 
    RemoveSecondaryTask
    (
     String target, 
     FileSeq fseq
    ) 
    {
      super("JNodeViewerPanel:RemoveSecondaryTask");

      pTarget = target;
      pFileSeq = fseq; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Removing Secondary File Sequence...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  client.removeSecondary(pAuthor, pView, pTarget, pFileSeq);
	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	  return;
 	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }
    
    private String   pTarget;
    private FileSeq  pFileSeq; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Export the node properties 
   */ 
  private
  class ExportTask
    extends Thread
  {
    public 
    ExportTask
    (
     String source,
     TreeSet<String> targets
    ) 
    {
      super("JNodeViewerPanel:ExportTask");

      pSource  = source; 
      pTargets = targets;
    }
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Exporting Node Properties...")) {
	StringBuilder warn = new StringBuilder();
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  synchronized(pExportDialog) {
	    NodeMod smod = client.getWorkingVersion(pAuthor, pView, pSource);
	    for(String tname : pTargets) {
	      NodeMod tmod = client.getWorkingVersion(pAuthor, pView, tname);

	      /* upstream links */ 
	      {
		boolean addedLinks = false;
		for(String source : smod.getSourceNames()) {
		  if(pExportDialog.exportSource(source)) {
		    if(tmod.getSource(source) == null) {
		      LinkMod link = smod.getSource(source);
		      client.link(pAuthor, pView, tmod.getName(), source, 
				  link.getPolicy(), link.getRelationship(), 
				  link.getFrameOffset());
		      addedLinks = true;
		    }
		    else {
		      warn.append
			("Skipped adding link from source node (" + source + ") to " + 
			 "target node (" + tname + ") because a link already existed " + 
			 "between those nodes!\n\n");
		    }
		  }
		}

		/* update the links if we need them for per-source action parameters below */
		if(addedLinks && pExportDialog.exportActionSourceParams()) 
		  tmod = client.getWorkingVersion(pAuthor, pView, tname);
	      }

	      /* node properties */ 
	      {
		if(pExportDialog.exportToolset()) 
		  tmod.setToolset(smod.getToolset());

		if(pExportDialog.exportEditor()) 
		  tmod.setEditor(smod.getEditor());
	      }

	      /* actions */ 
	      BaseAction taction = tmod.getAction(); 
	      BaseAction saction = smod.getAction(); 
	      {
		if((saction != null) && pExportDialog.exportAction()) {
		  
		  /* the action and parameters */ 
		  {
		    PluginMgrClient mgr = PluginMgrClient.getInstance();
		    if((taction == null) || 
		       !taction.getName().equals(saction.getName()) || 
		       !taction.getVersionID().equals(saction.getVersionID()) ||
		       !taction.getVendor().equals(saction.getVendor()))
		      taction = mgr.newAction(saction.getName(), 
					      saction.getVersionID(), 
					      saction.getVendor()); 
		    
		    for(ActionParam param : saction.getSingleParams()) {
		      if(pExportDialog.exportActionSingleParam(param.getName())) 
			taction.setSingleParamValue(param.getName(), param.getValue());
		    }
		    
		    if(pExportDialog.exportActionSourceParams()) 
		      taction.setSourceParamValues(saction);
		    
		    tmod.setAction(taction);
		  }

		  /* action enabled */ 
		  if(pExportDialog.exportActionEnabled()) 
		    tmod.setActionEnabled(smod.isActionEnabled()); 
		}
	      }
		  
	      if(taction != null) {
		/* execution details */ 
		if(saction != null) {
		  if(pExportDialog.exportOverflowPolicy()) 
		    tmod.setOverflowPolicy(smod.getOverflowPolicy());
		  
		  if(pExportDialog.exportExecutionMethod()) 
		    tmod.setExecutionMethod(smod.getExecutionMethod());
		  
		  if(pExportDialog.exportBatchSize() && 
		     (smod.getExecutionMethod() == ExecutionMethod.Parallel) && 
		     (tmod.getExecutionMethod() == ExecutionMethod.Parallel))
		    tmod.setBatchSize(smod.getBatchSize());
		}
		  
		/* job requirements */ 
		{
		  JobReqs sjreqs = smod.getJobRequirements();
		  JobReqs tjreqs = tmod.getJobRequirements();
		  
		  if(pExportDialog.exportPriority()) 
		    tjreqs.setPriority(sjreqs.getPriority());
		  
		  if(pExportDialog.exportMaxLoad()) 
		    tjreqs.setMaxLoad(sjreqs.getMaxLoad());
		  
		  if(pExportDialog.exportMinMemory()) 
		    tjreqs.setMinMemory(sjreqs.getMinMemory());
		  
		  if(pExportDialog.exportMinDisk()) 
		    tjreqs.setMinDisk(sjreqs.getMinDisk());
		  
		  for(String kname : pExportDialog.exportedLicenseKeys()) {
		    if(sjreqs.getLicenseKeys().contains(kname)) 
		      tjreqs.addLicenseKey(kname);
		    else 
		      tjreqs.removeLicenseKey(kname);
		  }
		  
		  for(String kname : pExportDialog.exportedSelectionKeys()) {
		    if(sjreqs.getSelectionKeys().contains(kname)) 
		      tjreqs.addSelectionKey(kname);
		    else 
		      tjreqs.removeSelectionKey(kname);
		  }
		  
		  tmod.setJobRequirements(tjreqs);
		}
	      }

	      /* apply the changes */ 
	      client.modifyProperties(pAuthor, pView, tmod);	      
	    }
	  }
	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	if(warn.length() > 0) 
	  master.showErrorDialog("Warning:", warn.toString()); 

	updateRoots();
      }
    }
    
    private String          pSource;
    private TreeSet<String> pTargets;
  }
    


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Rename a given node.
   */ 
  private
  class RenameTask
    extends Thread
  {
    public 
    RenameTask
    (
     NodePath oldPath,
     FilePattern pattern, 
     boolean renameFiles
    ) 
    {
      super("JNodeViewerPanel:RenameTask");
      
      pOldPath     = new NodePath(oldPath);
      pPattern     = pattern; 
      pRenameFiles = renameFiles;
    }

    public void 
    run() 
    {
      String oname = pOldPath.getCurrentName();
      String nname = pPattern.getPrefix();

      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Renaming Node...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  client.rename(pAuthor, pView, oname, pPattern, pRenameFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	if(!oname.equals(nname)) {
	  LinkedList<String> comps = new LinkedList<String>(pOldPath.getNames());
	  comps.removeLast();
	  comps.add(nname);

	  NodePath npath = new NodePath(comps);

	  boolean wasCollapsed = master.wasNodeCollapsed(pOldPath.toString());
	  master.setNodeCollapsed(npath.toString(), wasCollapsed); 
	  master.setNodeCollapsed(pOldPath.toString(), false);

	  renameRoot(oname, nname);
	}
	else {
	  updateRoots();
	}
      }
    }

    private NodePath     pOldPath; 
    private FilePattern  pPattern; 
    private boolean      pRenameFiles; 
  }

  /** 
   * Renumber a given node.
   */ 
  private
  class RenumberTask
    extends Thread
  {
    public 
    RenumberTask
    (
     String name, 
     FrameRange range, 
     boolean removeFiles
    ) 
    {
      super("JNodeViewerPanel:RenumberTask");

      pName        = name;
      pFrameRange  = range;
      pRemoveFiles = removeFiles;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Renumbering Node...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  TreeSet<Long> jobIDs = 
	    client.renumber(pAuthor, pView, pName, pFrameRange, pRemoveFiles);

	  if((jobIDs != null) && !jobIDs.isEmpty()) {
	    ShowObsoleteJobsTask task = new ShowObsoleteJobsTask(pName, jobIDs);
	    SwingUtilities.invokeLater(task);
	    return;
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private String      pName; 
    private FrameRange  pFrameRange; 
    private boolean     pRemoveFiles; 
  }

  /** 
   * Show the obsolete jobs dialog.
   */ 
  private
  class ShowObsoleteJobsTask
    extends Thread
  {
    public ShowObsoleteJobsTask
    (
     String name, 
     TreeSet<Long> jobIDs
    ) 
    {
      super("JNodeViewerPanel:ShowObsoleteJobsTask");

      pName   = name;
      pJobIDs = jobIDs;
    }

    public void 
    run() 
    {
      JConfirmKillObsoleteJobsDialog diag = 
	new JConfirmKillObsoleteJobsDialog(getTopFrame(), pName, pJobIDs);
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	KillJobsTask task = new KillJobsTask(null, pJobIDs);
	task.start();
      }
    }

    private String  pName; 
    private TreeSet<Long>  pJobIDs;
  }

  /** 
   * Release the matching nodes in the given view.
   */ 
  private
  class ReleaseViewTask
    extends Thread
  {
    public 
    ReleaseViewTask
    (
     String author, 
     String view, 
     String pattern, 
     boolean removeFiles,
     boolean removeArea
    ) 
    {
      super("JNodeViewerPanel:ReleaseViewTask");

      pAuthor      = author; 
      pView        = view; 
      pPattern     = pattern; 
      pRemoveFiles = removeFiles;
      pRemoveArea  = removeArea;
    }

    public void 
    run() 
    {
      TreeSet<String> names = null;
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Finding Working Versions...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  names = client.getWorkingNames(pAuthor, pView, pPattern);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
    
      ReleaseViewConfirmTask task =
	new ReleaseViewConfirmTask(pAuthor, pView, names, pRemoveFiles, pRemoveArea);
      SwingUtilities.invokeLater(task);
    }

    private String   pAuthor; 
    private String   pView; 
    private String   pPattern;
    private boolean  pRemoveFiles; 
    private boolean  pRemoveArea; 
  }

  /** 
   * Release a given node.
   */ 
  private
  class ReleaseViewConfirmTask
    extends Thread
  {
    public 
    ReleaseViewConfirmTask
    (
     String author, 
     String view, 
     TreeSet<String> names, 
     boolean removeFiles,
     boolean removeArea
    ) 
    {
      super("JNodeViewerPanel:ReleaseViewConfirmTask");

      pAuthor      = author; 
      pView        = view; 
      pNames       = names; 
      pRemoveFiles = removeFiles;
      pRemoveArea  = removeArea;
    }

    public void 
    run() 
    {
      boolean wasConfirmed = false;
      if(pNames.isEmpty()) {
	JConfirmDialog confirm = new JConfirmDialog(getTopFrame(), "Are you sure?");
	confirm.setVisible(true);
	wasConfirmed = confirm.wasConfirmed();
      }
      else {
	JConfirmListDialog confirm = 
	  new JConfirmListDialog(getTopFrame(), "Are you sure?", 
				 "Nodes to Release:", pNames);
	confirm.setVisible(true);
	wasConfirmed = confirm.wasConfirmed();
      }
      
      if(wasConfirmed) {
	ReleaseTask task =
	  new ReleaseTask(pAuthor, pView, pNames, pRemoveFiles, pRemoveArea);
	task.start();
      }      
    }

    private String           pAuthor; 
    private String           pView; 
    private TreeSet<String>  pNames; 
    private boolean          pRemoveFiles; 
    private boolean          pRemoveArea; 
  }

  /** 
   * Release a given node.
   */ 
  private
  class ReleaseTask
    extends Thread
  {
    public 
    ReleaseTask
    (
     String author, 
     String view, 
     TreeSet<String> names, 
     boolean removeFiles,
     boolean removeArea
    ) 
    {
      super("JNodeViewerPanel:ReleaseTask");

      pAuthor      = author; 
      pView        = view; 
      pNames       = names; 
      pRemoveFiles = removeFiles;
      pRemoveArea  = removeArea;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Releasing Nodes...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);

	  if(!pNames.isEmpty()) 
	    client.release(pAuthor, pView, pNames, pRemoveFiles);

	  if(pRemoveArea) 
	    client.removeWorkingArea(pAuthor, pView);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private String           pAuthor; 
    private String           pView; 
    private TreeSet<String>  pNames; 
    private boolean          pRemoveFiles; 
    private boolean          pRemoveArea; 
  }

  /** 
   * Delete a given node.
   */ 
  private
  class DeleteTask
    extends Thread
  {
    public 
    DeleteTask
    (
     String name,
     boolean removeFiles
    ) 
    {
      super("JNodeViewerPanel:DeleteTask");

      pName        = name;
      pRemoveFiles = removeFiles;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Deleting Node: " + pName)) {
	try {	
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  client.delete(pName, pRemoveFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private String   pName; 
    private boolean  pRemoveFiles; 
  }

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  private
  class RemoveFilesTask
    extends UIMaster.RemoveFilesTask
  {
    public 
    RemoveFilesTask
    (
     TreeSet<String> names
    ) 
    {
      UIMaster.getInstance().super(pGroupID, names, pAuthor, pView);
      setName("JNodeViewerPanel:RemoveFilesTask");
    }
    
    protected void
    postOp() 
    {
      updateRoots();
    }    
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Queue jobs to the queue for the given node.
   */ 
  private
  class QueueJobsTask
    extends UIMaster.QueueJobsTask
  {
    public 
    QueueJobsTask
    (
     TreeSet<String> names
    ) 
    {
      this(names, null, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     TreeSet<String> names, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys
    ) 
    {
      UIMaster.getInstance().super(pGroupID, names, pAuthor, pView, 
				   batchSize, priority, rampUp, 
				   selectionKeys, licenseKeys);
      setName("JNodeViewerPanel:QueueJobsTask");
    }

    protected void
    postOp() 
    {
      updateRoots();
    }
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends UIMaster.PauseJobsTask
  {
    public 
    PauseJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JNodeViewerPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updateRoots();
    }
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends UIMaster.ResumeJobsTask
  {
    public 
    ResumeJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JNodeViewerPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updateRoots();
    }
  }

  /** 
   * Preempt the given jobs.
   */ 
  private
  class PreemptJobsTask
    extends UIMaster.PreemptJobsTask
  {
    public 
    PreemptJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JNodeViewerPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updateRoots();
    }
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends UIMaster.KillJobsTask
  {
    public 
    KillJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JNodeViewerPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updateRoots();
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-in a given node.
   */ 
  private
  class CheckInTask
    extends Thread
  {
    public 
    CheckInTask
    (
     TreeSet<String> names, 
     String desc,
     VersionID.Level level
    ) 
    {
      super("JNodeViewerPanel:CheckInTask");

      pNames       = names; 
      pDescription = desc;
      pLevel       = level;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp(pGroupID, "Checking-In: " + name);
	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    client.checkIn(pAuthor, pView, name, pDescription, pLevel);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private TreeSet<String>  pNames; 
    private String           pDescription;  
    private VersionID.Level  pLevel;
  }

  /** 
   * Check-out a given node.
   */ 
  private
  class CheckOutTask
    extends Thread
  {
    public 
    CheckOutTask
    (
     TreeMap<String,VersionID> versions, 
     TreeMap<String,CheckOutMode> modes, 
     TreeMap<String,CheckOutMethod> methods
    ) 
    {
      super("JNodeViewerPanel:CheckOutTask");
      
      pVersions = versions;
      pModes    = modes; 
      pMethods  = methods; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
	try {
	  for(String name : pVersions.keySet()) {
	    master.updatePanelOp(pGroupID, "Checking-Out: " + name);

	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    TreeMap<String,TreeSet<Long>> jobIDs = 
	      client.checkOut(pAuthor, pView, name, 
			      pVersions.get(name), pModes.get(name), pMethods.get(name));

	    if((jobIDs != null) && !jobIDs.isEmpty()) {
	      ShowUnfinishedJobsTask task = new ShowUnfinishedJobsTask(name, jobIDs);
	      SwingUtilities.invokeLater(task);
	      return;
	    }
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private TreeMap<String,VersionID>       pVersions;
    private TreeMap<String,CheckOutMode>    pModes;  
    private TreeMap<String,CheckOutMethod>  pMethods; 
  }

  /** 
   * Show the unfinished jobs dialog.
   */ 
  private
  class ShowUnfinishedJobsTask
    extends Thread
  {
    public ShowUnfinishedJobsTask
    (
     String name, 
     TreeMap<String,TreeSet<Long>> jobIDs
    ) 
    {
      super("JNodeViewerPanel:ShowUnfinshedJobsTask");

      pName   = name;
      pJobIDs = jobIDs;
    }

    public void 
    run() 
    {
      JConfirmKillUnfinishedJobsDialog diag = 
	new JConfirmKillUnfinishedJobsDialog(getTopFrame(), pName, pJobIDs);
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	TreeSet<Long> dead = new TreeSet<Long>();
	for(String name : pJobIDs.keySet()) 
	  dead.addAll(pJobIDs.get(name));

	KillJobsTask task = new KillJobsTask(null, dead);
	task.start();
      }
    }

    private String  pName; 
    private TreeMap<String,TreeSet<Long>>  pJobIDs;
  }

  /** 
   * Lock a given node.
   */ 
  private
  class LockTask
    extends Thread
  {
    public 
    LockTask
    (
     TreeMap<String,VersionID> versions
    ) 
    {
      super("JNodeViewerPanel:LockTask");
      
      pVersions = versions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  for(String name : pVersions.keySet()) {
	    master.updatePanelOp(pGroupID, "Locking: " + name);
	    client.lock(pAuthor, pView, name, pVersions.get(name));
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private TreeMap<String,VersionID>  pVersions;
  }

  /** 
   * Evolve a given node.
   */ 
  private
  class EvolveTask
    extends Thread
  {
    public 
    EvolveTask
    (
     String name, 
     VersionID vid
    ) 
    {
      super("JNodeViewerPanel:EvolveTask");

      pName      = name; 
      pVersionID = vid; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Evolving Node: " + pName)) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  client.evolve(pAuthor, pView, pName, pVersionID);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updateRoots();
      }
    }

    private String     pName; 
    private VersionID  pVersionID; 
  }

  /** 
   * Get the names and revision numbers of the nodes to restore.
   */ 
  private
  class RestoreQueryTask
    extends Thread
  {
    public 
    RestoreQueryTask
    (
     TreeSet<String> names
    ) 
    {
      super("JNodeViewerPanel:RestoreQueryTask");
      pNames = names;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      TreeMap<String,TreeSet<VersionID>> offline = null; 
      if(master.beginPanelOp(pGroupID, "Searching for Offline Versions...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	  if(pNames.isEmpty()) {
	    offline = client.restoreQuery(null);
	  }
	  else {
	    offline = new TreeMap<String,TreeSet<VersionID>>();
	    for(String name : pNames) {
	      TreeMap<String,TreeSet<VersionID>> versions = client.restoreQuery(name);
	      TreeSet<VersionID> vids = versions.get(name);
	      if((vids != null) && !vids.isEmpty())
		offline.put(name, vids);
	    }
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
	
	ShowRequestRestoreTask task = new ShowRequestRestoreTask(offline);
	SwingUtilities.invokeLater(task);
      }
    }

    private TreeSet<String>  pNames; 
  }

  /** 
   * Update the contents of the request restore dialog and query the user for a selection.
   */ 
  private
  class ShowRequestRestoreTask
    extends Thread
  {
    public 
    ShowRequestRestoreTask
    (
     TreeMap<String,TreeSet<VersionID>> versions   
    ) 
    {
      super("JNodeViewerPanel:ShowRequestRestoreTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      if(pRestoreDialog == null) 
	pRestoreDialog = new JRequestRestoreDialog(pGroupID, getTopFrame());

      pRestoreDialog.setVersions(pVersions);
      pRestoreDialog.setVisible(true);
      if(pRestoreDialog.wasConfirmed()) {
	TreeMap<String,TreeSet<VersionID>> selected = pRestoreDialog.getSelectedVersions();
	if(!selected.isEmpty()) {
	  RestoreTask task = new RestoreTask(selected);
	  task.start();	  
	}
      }
    }

    private TreeMap<String,TreeSet<VersionID>>  pVersions;   
  }

  /** 
   * Restore the given versions of the node.
   */ 
  private
  class RestoreTask
    extends Thread
  {
    public 
    RestoreTask
    (
     TreeMap<String,TreeSet<VersionID>> versions   
    ) 
    {
      super("JNodeViewerPanel:RestoreTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Requesting Restore...")) {
	try {
	  master.getMasterMgrClient(pGroupID).requestRestore(pVersions);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
    }

    private TreeMap<String,TreeSet<VersionID>>   pVersions;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Aquire the channel operation lock and release it when the tool is done.
   */ 
  private
  class ToolOpTask
    extends Thread
  {
    public 
    ToolOpTask
    (
     BaseTool tool, 
     int groupID
    ) 
    {
      super("JNodeViewerPanel:ToolOpTask");

      pTool = tool;
      pGID  = groupID;
      pLock = new Object(); 
    }

    public void 
    endTool
    (
     boolean success
    ) 
    {
      synchronized(pLock) {
	pSuccess = success;
	pMessage = pTool.getName() + (pSuccess ? " Done." : " Aborted.");
	pLock.notify();
      }
    }

    public void 
    updateTool
    (
     String msg
    ) 
    {
      synchronized(pLock) {
	pMessage = msg;
	pLock.notify();
      }
    }

    public void 
    run() 
    {	
      UIMaster master = UIMaster.getInstance();
      if(!master.beginPanelOp(pGID, "Running " + pTool.getName() + pMessage))
	return;

      try {
	SwingUtilities.invokeLater(new ToolInputTask(pTool, pGID, this));

	synchronized(pLock) {
	  while(pSuccess == null) {
	    pLock.wait();
	    
	    if(pMessage != null) 
	      master.updatePanelOp(pGID, pMessage);
	  }

          master.endPanelOp(pGID, pMessage);
	}

        if((pSuccess) && pTool.updateOnExit()) 
          setRoots(pTool.rootsOnExit());
      }
      catch(Exception ex) {
	pMessage = "Unexpected Failure!";
	master.showErrorDialog(ex);
	master.endPanelOp(pGID, pMessage);
      }
    }

    private BaseTool  pTool;
    private int       pGID; 
    private Object    pLock; 
    private Boolean   pSuccess;
    private String    pMessage; 
  }

  /** 
   * Collect user input for the next tool phase.
   */ 
  private
  class ToolInputTask
    extends Thread
  {
    public 
    ToolInputTask
    (
     BaseTool tool, 
     int groupID,
     ToolOpTask task
    ) 
    {
      super("JNodeViewerPanel:ToolInputTask");

      pTool   = tool;
      pGID    = groupID;
      pOpTask = task;
    }

    public void 
    run() 
    {	
      UIMaster master = UIMaster.getInstance();
      try {
	String msg = pTool.collectPhaseInput();
	if(msg != null) {
	  RunToolTask task = new RunToolTask(pTool, pGID, pOpTask, msg);
	  task.start();	
	}
	else {
	  pOpTask.endTool(true); 
	}
      }
      catch(Exception ex) {
	pOpTask.endTool(false); 
	master.showErrorDialog(ex);
      }
    }

    private BaseTool    pTool;
    private int         pGID; 
    private ToolOpTask  pOpTask; 
  }

  /** 
   * Run the next tool phase. 
   */ 
  private
  class RunToolTask
    extends Thread
  {
    public 
    RunToolTask
    (
     BaseTool tool, 
     int groupID,
     ToolOpTask task, 
     String msg
    ) 
    {
      super("JNodeViewerPanel:RunToolTask");

      pTool    = tool;
      pGID     = groupID;
      pOpTask  = task;
      pMessage = msg; 
    }

    public void 
    run() 
    {
      pOpTask.updateTool("Running " + pTool.getName() + pMessage); 

      UIMaster master = UIMaster.getInstance();
      try {
	MasterMgrClient mclient = master.getMasterMgrClient(pGID); 
	QueueMgrClient  qclient = master.getQueueMgrClient(pGID); 
	if(pTool.executePhase(mclient, qclient)) {
	  pOpTask.updateTool("Completed " + pTool.getName() + " Phase.");
	  SwingUtilities.invokeLater(new ToolInputTask(pTool, pGID, pOpTask));
	}
	else {
	  pOpTask.endTool(true);
// 	  if(pTool.updateOnExit()) 
// 	    setRoots(pTool.rootsOnExit());
	}
      }
      catch(Exception ex) {
	pOpTask.endTool(false); 
	master.showErrorDialog(ex);
      }
    }

    private BaseTool    pTool;
    private int         pGID; 
    private ToolOpTask  pOpTask; 
    private String      pMessage; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6047073003000120503L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The status of the root nodes and all of its upstream/downstream connections indexed
   * by the fully resolved names of the root nodes. <P> 
   *
   * If the status is <CODE>null</CODE> for a given root node, then it will be updated
   * by the <CODE>StatusTask</CODE> the next time it is run.
   */ 
  private TreeMap<String,NodeStatus>  pRoots;

  /**
   * Whether to display the node status detail hints.
   */ 
  private boolean  pShowDetailHints;

  /**
   * Whether to display the downstream tree of nodes.
   */ 
  private boolean  pShowDownstream;

  /**
   * The fully resolved name of the node who's status was last sent to the node 
   * details, links, files and history panels. 
   */ 
  private String  pLastDetailsName;


  /**
   * The toolset used to build the editor menu.
   */ 
  private String  pEditorMenuToolset;

  /**
   * The toolset used to build the tool menu.
   */ 
  private String  pToolMenuToolset;

  /**
   * Whether the default toolset menu needs to be rebuilt.
   */ 
  private boolean pRefreshDefaultToolMenu; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node status hint graphic.
   */ 
  private NodeStatus      pLastViewerNodeHint; 
  private ViewerNodeHint  pViewerNodeHint; 


  /**
   * The currently displayed nodes indexed by <CODE>NodePath</CODE>.
   */ 
  private TreeMap<NodePath,ViewerNode>  pViewerNodes; 

  /**
   * The currently displayed node links. 
   */ 
  private ViewerLinks  pViewerLinks; 


  /**
   * The currently selected nodes indexed by <CODE>NodePath</CODE>.
   */ 
  private TreeMap<NodePath,ViewerNode>  pSelected;

  /**
   * The primary selection.
   */ 
  private ViewerNode  pPrimary;

  /**
   * The currently selected link.
   */
  private LinkCommon  pSelectedLink;


  /**
   * The initial center of the node layout.
   */ 
  private Point2d  pInitialCenter; 
  

  /**
   * The position of the node which should remain stationary after a relayout of nodes.
   */ 
  private Point2d  pPinnedPos; 

  /**
   * The node path to the viewer node in the new layout which should be positioned at 
   * pPinnedPos.
   */ 
  private NodePath  pPinnedPath; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel popup menu.
   */ 
  private JPopupMenu  pPanelPopup; 

  /**
   * The panel popup menu items.
   */
  private JMenuItem  pUpdateItem;
  private JMenuItem  pRegisterItem;
  private JMenuItem  pReleaseViewItem;
  private JMenuItem  pPanelRestoreItem;
  private JMenuItem  pFrameAllItem;
  private JMenuItem  pFrameSelectionItem;
  private JMenuItem  pAutomaticExpandItem;
  private JMenuItem  pExpandAllItem;
  private JMenuItem  pCollapseAllItem;
  private JMenuItem  pShowHideDownstreamItem;
  private JMenuItem  pRemoveAllRootsItem;
  private JMenuItem  pShowHideDetailHintsItem;
  private JMenuItem  pShowHideToolsetHintItem;
  private JMenuItem  pShowHideEditorHintItem;
  private JMenuItem  pShowHideActionHintItem;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The tool plugin popup menus.
   */ 
  private JPopupMenu  pToolPopup; 
  private JPopupMenu  pDefaultToolPopup; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node popup menus.
   */ 
  private JPopupMenu  pUndefinedNodePopup; 
  private JPopupMenu  pPanelLockedNodePopup; 
  private JPopupMenu  pCheckedInNodePopup; 
  private JPopupMenu  pFrozenNodePopup; 
  private JPopupMenu  pNodePopup; 

  /** 
   * The Node Menus:
   * 
   *   { pUndefinedNodePopup, pPanelLockedNodePopup, pCheckedInNodePopup, 
   *     pFrozenNodePopup, pNodePopup }
   */
  private JPopupMenu[]  pNodeMenus; 

  
  /**
   * The node popup menu items.
   */ 
  private JPopupMenuItem[]  pUpdateDetailsItems;
  private JPopupMenuItem[]  pUpdateBranchItems;
  private JPopupMenuItem[]  pMakeRootItems;
  private JPopupMenuItem[]  pAddRootItems;
  private JPopupMenuItem[]  pReplaceRootItems;
  private JPopupMenuItem[]  pRemoveRootItems;
  private JPopupMenuItem[]  pEditItems;
  private JPopupMenuItem[]  pEditWithDefaultItems;
  private JPopupMenuItem    pEditAsOwnerItem; 
  private JPopupMenuItem[]  pCheckOutItems;
  private JPopupMenuItem[]  pLockItems;
  private JPopupMenuItem[]  pRestoreItems;
  private JPopupMenuItem[]  pReleaseItems;

  private JPopupMenuItem  pLinkItem;
  private JPopupMenuItem  pUnlinkItem;
  private JPopupMenuItem  pAddSecondaryItem;
  private JPopupMenuItem  pQueueJobsItem;
  private JPopupMenuItem  pQueueJobsSpecialItem;
  private JPopupMenuItem  pPauseJobsItem;
  private JPopupMenuItem  pResumeJobsItem;
  private JPopupMenuItem  pPreemptJobsItem;
  private JPopupMenuItem  pKillJobsItem;
  private JPopupMenuItem  pCheckInItem;
  private JPopupMenuItem  pEvolveItem;
  private JPopupMenuItem  pCloneItem;
  private JPopupMenuItem  pRemoveFilesItem;
  private JPopupMenuItem  pExportItem;
  private JPopupMenuItem  pRenameItem;
  private JPopupMenuItem  pRenumberItem;
  private JPopupMenuItem  pDeleteItem;

  /**
   * The dynamic submenus.
   */ 
  private JMenu[]  pEditWithMenus; 
  private JMenu[]  pViewsContainingMenus;
  private JMenu[]  pViewsEditingMenus;

  /**
   * The remove secondary node submenu.
   */ 
  private JMenu  pRemoveSecondaryMenu;
  
  /** 
   * The table of secondary file sequences currently displayed in the remove 
   * secondary node submenu indexed by secondary sequence name.
   */ 
  private TreeMap<String,FileSeq>  pRemoveSecondarySeqs;
 
  /**
   * A list of the N most recently used item titles for each node menu and the 
   * ActionCommand for each item indexed by item title.
   */ 
  private int[]                              pFirstRecentIndex;
  private ArrayList<LinkedList<String>>      pRecentMenuItems; 
  private ArrayList<TreeMap<String,String>>  pRecentActionCommands; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The link popup menu.
   */ 
  private JPopupMenu  pLinkPopup;

  /**
   * The link popup menu items.
   */ 
  private JMenuItem  pLinkEditItem;
  private JMenuItem  pLinkUnlinkItem;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The add secondary dialog;
   */ 
  private JAddSecondaryDialog  pAddSecondaryDialog;

  
  /**
   * The export node properties dialog.
   */ 
  private JExportDialog  pExportDialog;

  /**
   * The rename node dialog.
   */ 
  private JRenameDialog  pRenameDialog;

  /**
   * The renumber node dialog.
   */ 
  private JRenumberDialog  pRenumberDialog;

  /**
   * The register node dialog.
   */ 
  private JRegisterDialog  pRegisterDialog;

  /**
   * The clone node dialog.
   */ 
  private JCloneDialog  pCloneDialog;

  /**
   * The release node dialog.
   */ 
  private JReleaseDialog  pReleaseDialog;

  /**
   * The release view dialog.
   */ 
  private JReleaseViewDialog  pReleaseViewDialog;

  /**
   * The delete node dialog.
   */ 
  private JDeleteDialog  pDeleteDialog;

  /** 
   * The check-in node dialog.
   */ 
  private JCheckInDialog  pCheckInDialog;

  /** 
   * The check-out node dialog.
   */ 
  private JCheckOutDialog  pCheckOutDialog;

  /** 
   * The lock node dialog.
   */ 
  private JLockDialog  pLockDialog;

  /** 
   * The evolve node dialog.
   */ 
  private JEvolveDialog  pEvolveDialog;

  /** 
   * The restore node dialog.
   */ 
  private JRequestRestoreDialog  pRestoreDialog;


  /**
   * The link creation dialog.
   */ 
  private JCreateLinkDialog  pCreateLinkDialog;

  /**
   * The link editor dialog.
   */ 
  private JEditLinkDialog  pEditLinkDialog;

}
