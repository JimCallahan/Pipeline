// $Id: JNodeViewerPanel.java,v 1.7 2005/01/09 23:23:09 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.core.*; 
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

import net.java.games.jogl.*;

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
    super.initUI(128.0);

    /* initialize fields */ 
    {
      pRoots = new TreeMap<String,NodeStatus>();

      pShowDownstream = UserPrefs.getInstance().getShowDownstream();

      pViewerNodes = new HashMap<NodePath,ViewerNode>();
      pViewerLinks = new ViewerLinks();
      pSelected = new HashMap<NodePath,ViewerNode>();

      pRemoveSecondarySeqs = new TreeMap<String,FileSeq>();

      pEditorPlugins      = PluginMgr.getInstance().getEditors();
      pEditorMenuLayout   = new PluginMenuLayout();
      pRefreshEditorMenus = true; 

      pToolPlugins      = PluginMgr.getInstance().getTools();
      pToolMenuLayout   = new PluginMenuLayout();
      pRefreshToolMenu  = true; 
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu();  
      pPanelPopup.addPopupMenuListener(this);

      item = new JMenuItem("Update");
      item.setActionCommand("update");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Register...");
      pRegisterItem = item;
      item.setActionCommand("register");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
      
      item = new JMenuItem("Frame All");
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
       
      item = new JMenuItem("Automatic Expand");
      item.setActionCommand("automatic-expand");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Expand All");
      item.setActionCommand("expand-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Collapse All");
      item.setActionCommand("collapse-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();

      item = new JMenuItem();
      pShowHideDownstreamItem = item;
      item.setActionCommand("show-hide-downstream");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();

      item = new JMenuItem("Remove All Roots");
      item.setActionCommand("remove-all-roots");
      item.addActionListener(this);
      pPanelPopup.add(item);  
    }

    /* node popup menus */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pUndefinedNodePopup = new JPopupMenu();  
      pUndefinedNodePopup.addPopupMenuListener(this);

      pLockedNodePopup = new JPopupMenu();  
      pLockedNodePopup.addPopupMenuListener(this);

      pCheckedInNodePopup = new JPopupMenu();  
      pCheckedInNodePopup.addPopupMenuListener(this);

      pFrozenNodePopup = new JPopupMenu();  
      pFrozenNodePopup.addPopupMenuListener(this);

      pNodePopup = new JPopupMenu();  
      pNodePopup.addPopupMenuListener(this);

      pEditWithMenus = new JMenu[4];

      JPopupMenu menus[] = { 
	pUndefinedNodePopup, pLockedNodePopup, pCheckedInNodePopup, 
	pFrozenNodePopup, pNodePopup 
      };

      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem("Update Details");
	item.setActionCommand("details");
	item.addActionListener(this);
	menus[wk].add(item);  
	
	menus[wk].addSeparator();

	item = new JMenuItem("Make Root");
	item.setActionCommand("make-root");
	item.addActionListener(this);
	menus[wk].add(item);  
	
	item = new JMenuItem("Add Root");
	item.setActionCommand("add-root");
	item.addActionListener(this);
	menus[wk].add(item);  
	
	item = new JMenuItem("Replace Root");
	item.setActionCommand("replace-root");
	item.addActionListener(this);
	menus[wk].add(item);  
	
	item = new JMenuItem("Remove Root");
	item.setActionCommand("remove-root");
	item.addActionListener(this);
	menus[wk].add(item);  
	
	if(wk > 0) {
	  menus[wk].addSeparator();
	  
	  item = new JMenuItem((wk < 4) ? "View" : "Edit");
	  item.setActionCommand("edit");
	  item.addActionListener(this);
	  menus[wk].add(item);
	  
	  pEditWithMenus[wk-1] = new JMenu((wk < 4) ? "View With" : "Edit With");
	  menus[wk].add(pEditWithMenus[wk-1]);
	}

	if((wk == 2) || (wk == 3)) {
	  menus[wk].addSeparator();
	  
	  item = new JMenuItem("Check-Out...");
	  item.setActionCommand("check-out");
	  item.addActionListener(this);
	  menus[wk].add(item);
	}

	if(wk == 3) {
	  menus[wk].addSeparator();
	  
	  item = new JMenuItem("Release");
	  item.setActionCommand("release");
	  item.addActionListener(this);
	  menus[wk].add(item);
	}
      }
	
      {
	pNodePopup.addSeparator();
	
	item = new JMenuItem("Link...");
	pLinkItem = item;
	item.setActionCommand("link");
	item.addActionListener(this);
	pNodePopup.add(item);
	
	item = new JMenuItem("Unlink");
	pUnlinkItem = item;
	item.setActionCommand("unlink");
	item.addActionListener(this);
	pNodePopup.add(item);
	
	pNodePopup.addSeparator();
	
	item = new JMenuItem("Add Secondary...");
	item.setActionCommand("add-secondary");
	item.addActionListener(this);
	pNodePopup.add(item);
	
	sub = new JMenu("Remove Secondary");
	pRemoveSecondaryMenu = sub;
	sub.setEnabled(false);
	pNodePopup.add(sub);
	
	pNodePopup.addSeparator();
      
	item = new JMenuItem("Queue Jobs");
	item.setActionCommand("queue-jobs");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Queue Jobs Special...");
	item.setActionCommand("queue-jobs-special");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Pause Jobs");
	item.setActionCommand("pause-jobs");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Resume Jobs");
	item.setActionCommand("resume-jobs");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Kill Jobs");
	item.setActionCommand("kill-jobs");
	item.addActionListener(this);
	pNodePopup.add(item);

	pNodePopup.addSeparator();

	item = new JMenuItem("Check-In...");
	item.setActionCommand("check-in");
	item.addActionListener(this);
	pNodePopup.add(item);
  
	item = new JMenuItem("Check-Out...");
	pCheckOutItem = item;
	item.setActionCommand("check-out");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Evolve Version...");
	pEvolveItem = item;
	item.setActionCommand("evolve");
	item.addActionListener(this);
	pNodePopup.add(item);

	pNodePopup.addSeparator();
      
	item = new JMenuItem("Clone...");
	item.setActionCommand("clone");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Release");
	item.setActionCommand("release");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Remove Files");
	item.setActionCommand("remove-files");
	item.addActionListener(this);
	pNodePopup.add(item);

	pNodePopup.addSeparator();

	item = new JMenuItem("Export...");
	pExportItem = item;
	item.setActionCommand("export");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Rename...");
	pRenameItem = item;
	item.setActionCommand("rename");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Renumber...");
	pRenumberItem = item;
	item.setActionCommand("renumber");
	item.addActionListener(this);
	pNodePopup.add(item);

	item = new JMenuItem("Delete...");
	pDeleteItem = item;
	item.setActionCommand("delete");
	item.addActionListener(this);
	pNodePopup.add(item);
      }
    }

    /* link popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pLinkPopup = new JPopupMenu();  
      pLinkPopup.addPopupMenuListener(this);
       
      item = new JMenuItem("Edit Link...");
      item.setActionCommand("link-edit");
      item.addActionListener(this);
      pLinkPopup.add(item);

      item = new JMenuItem("Unlink");
      item.setActionCommand("link-unlink");
      item.addActionListener(this);
      pLinkPopup.add(item);
    }

    /* link popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pToolPopup = new JPopupMenu();  
      pToolPopup.addPopupMenuListener(this);
    }

    /* initialize components */ 
    {
      pCanvas.addKeyListener(this);
    }

    /* initialize child dialogs */ 
    {
      pAddSecondaryDialog = new JAddSecondaryDialog();

      pExportDialog   = new JExportDialog();
      pRenameDialog   = new JRenameDialog();
      pRenumberDialog = new JRenumberDialog();
      pRegisterDialog = new JRegisterDialog();
      pReleaseDialog  = new JReleaseDialog();
      pDeleteDialog   = new JDeleteDialog();
      pCheckInDialog  = new JCheckInDialog();
      pCheckOutDialog = new JCheckOutDialog();
      pEvolveDialog   = new JEvolveDialog();
      
      pCreateLinkDialog = new JCreateLinkDialog();
      pEditLinkDialog   = new JEditLinkDialog();
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
   * Update the state of all currently displayed roots.
   */
  public synchronized void 
  updateRoots()
  {
    for(String name : pRoots.keySet()) 
      pRoots.put(name, null);
    
    updateNodeBrowserSelection();
    updateNodeStatus(); 
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
  public synchronized void 
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
  public synchronized void 
  setRoots
  (
   TreeSet<String> names
  )
  {
    pRoots.clear();
    for(String name : names) 
      pRoots.put(name, null);
    
    updateNodeBrowserSelection();
    updateNodeStatus(); 
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
    
    updateNodeBrowserSelection();
    updateNodeStatus(); 
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
    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.removeAll(names);
    
    setRoots(roots);
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
    if(pRoots.containsKey(oldName)) {
      TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
      roots.remove(oldName);
      roots.add(newName);
      
      setRoots(roots);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the initial update which restores the selected nodes.
   */ 
  public void 
  restoreSelection() 
  {
    updateNodeBrowserSelection();

    StatusTask task = new StatusTask(this, false);
    try {
      task.start();
      task.join();
    }
    catch(InterruptedException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel menu.
   */ 
  public void 
  updatePanelMenu() 
  {
    pRegisterItem.setEnabled(!pIsLocked);

    pShowHideDownstreamItem.setText
      ((pShowDownstream ? "Hide" : "Show") + " Downstream");
  }

  /**
   * Update the node menu.
   */ 
  public void 
  updateNodeMenu() 
  {
    NodeDetails details = null;
    if(pPrimary != null) 
      details = pPrimary.getNodeStatus().getDetails();

    NodeMod mod = details.getWorkingVersion();

    boolean hasCheckedIn = (details.getLatestVersion() != null);
    boolean multiple     = (getSelectedNames().size() >= 2);

    pLinkItem.setEnabled(multiple);
    pUnlinkItem.setEnabled(multiple);

    pExportItem.setEnabled(multiple);
    pRenameItem.setEnabled(!hasCheckedIn);
    pRenumberItem.setEnabled(mod.getPrimarySequence().hasFrameNumbers());
    
    pCheckOutItem.setEnabled(hasCheckedIn);
    pEvolveItem.setEnabled(hasCheckedIn);
    
    {
      UIMaster master = UIMaster.getInstance();
      try {
	boolean isPrivileged = master.getMasterMgrClient().isPrivileged(true);
	pDeleteItem.setEnabled(isPrivileged);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    pRemoveSecondaryMenu.setEnabled(false);

    updateEditorMenus();

    /* rebuild remove secondary items */ 
    if(!pIsLocked) {
      JMenuItem item;

      pRemoveSecondaryMenu.removeAll();
      pRemoveSecondarySeqs.clear();

      if(mod != null) {
	for(FileSeq fseq : mod.getSecondarySequences()) {
	  String fname = fseq.toString();
	  
	  item = new JMenuItem(fname);
	  item.setActionCommand("remove-secondary:" + fname);
	  item.addActionListener(this);
	  pRemoveSecondaryMenu.add(item);
	  
	  pRemoveSecondarySeqs.put(fname, fseq);
	}
      }
      
      pRemoveSecondaryMenu.setEnabled(pRemoveSecondaryMenu.getItemCount() > 0);
    }
  }

  /**
   * Update the editor plugin menus.
   */ 
  private synchronized void 
  updateEditorMenus()
  {
    if(pRefreshEditorMenus) {
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) {
	pEditWithMenus[wk].removeAll();
	for(PluginMenuLayout pml : pEditorMenuLayout) 
	  pEditWithMenus[wk].add(buildPluginMenu(pml, "edit-with", pEditorPlugins));
      }
      
      pRefreshEditorMenus = false;
    }
  }

  /**
   * Update the tool plugin menus.
   */ 
  private synchronized void 
  updateToolMenu()
  {
    if(pRefreshToolMenu) {
      pToolPopup.removeAll();
      for(PluginMenuLayout pml : pToolMenuLayout)
	pToolPopup.add(buildPluginMenu(pml, "run-tool", pToolPlugins));

      pRefreshToolMenu = false;
    }
  }

  /**
   * Recursively update a plugin menu.
   */ 
  private JMenuItem
  buildPluginMenu
  (
   PluginMenuLayout layout, 
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> plugins
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      item = new JMenuItem(layout.getTitle());
      item.setActionCommand(prefix + ":" + layout.getName() + ":" + layout.getVersionID());
      item.addActionListener(this);
   
      TreeSet<VersionID> vids = plugins.get(layout.getName());
      item.setEnabled((vids != null) && vids.contains(layout.getVersionID()));
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(buildPluginMenu(pml, prefix, plugins));
      item = sub;
    }

    return item;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection in the node browser to be consistent with the root nodes
   * in the node viewer if they share the same group, author and view.
   */ 
  private synchronized void 
  updateNodeBrowserSelection() 
  {
    /* update the associated node viewer */ 
    UIMaster master = UIMaster.getInstance();
    if((pGroupID > 0) && !master.isRestoring()) {
      PanelGroup<JNodeBrowserPanel> panels = master.getNodeBrowserPanels();
      JNodeBrowserPanel browser = panels.getPanel(pGroupID);
      if(browser != null) {
	if(browser.getAuthor().equals(pAuthor) && browser.getView().equals(pView)) {
	  TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
	  browser.updateSelection(roots);
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the connected node subpanels with the given node status.
   */ 
  public synchronized void
  updateSubPanels
  (
   NodeStatus status, 
   boolean updateJobs
  ) 
  {
    if(pGroupID > 0) {
      UpdateSubPanelsTask task = 
	new UpdateSubPanelsTask(pGroupID, pAuthor, pView, status, updateJobs);
      task.start();
    }
    
    if(status != null) 
      pLastDetailsName = status.getName();
    else 
      pLastDetailsName = null;
  }

  /**
   * Update the connected node subpanels.
   */ 
  public synchronized void
  updateSubPanels
  (
   boolean updateJobs
  ) 
  {
    NodeStatus status = null;
    if(pLastDetailsName != null) {
      for(NodeStatus root : pRoots.values()) {
	status = updateSubPanelsHelper(root, pLastDetailsName);
	if(status != null) 
	  break;
      }
    }

    updateSubPanels(status, updateJobs);
  }
  
  /**
   * Recursively search the given node status and all upstream nodes for a node status 
   * with the given name.
   * 
   * @return 
   *   The found node status or <CODE>null</CODE> if not found.
   */ 
  private NodeStatus
  updateSubPanelsHelper
  (
   NodeStatus root, 
   String name
  ) 
  {
    if(root.getName().equals(name)) 
      return root;

    for(NodeStatus status : root.getSources()) {
      NodeStatus found = updateSubPanelsHelper(status, name);
      if(found != null) 
	return found;
    }

    return null;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the nodes being viewed.
   */ 
  private synchronized void 
  updateNodeStatus() 
  {
    if(UIMaster.getInstance().isRestoring())
      return;

    StatusTask task = new StatusTask(this, true);
    task.start();
  }

  /**
   * Update the visualization graphics and any connected subpanels.
   */
  private synchronized void 
  updateUniverse() 
  { 
    updateUniverse(true);
  }
  
  /**
   * Update the visualization graphics.
   * 
   * @param updateSubPanels
   *   Whether to also update any connected subpanels.
   */
  private synchronized void 
  updateUniverse
  (
   boolean updateSubPanels
  )
  { 
    /* refresh the plugins */    
    {
      PluginMgr plg = PluginMgr.getInstance();
      pEditorPlugins = plg.getEditors();
      pToolPlugins = plg.getTools();

      UIMaster master = UIMaster.getInstance(); 
      try {
	pEditorMenuLayout = master.getMasterMgrClient().getEditorMenuLayout();
	pRefreshEditorMenus = true;

	pToolMenuLayout = master.getMasterMgrClient().getToolMenuLayout();
	pRefreshToolMenu = true;
      } 
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

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
	  delta = new Vector2d(bbox.getCenter(), pInitialCenter);
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
      pLayoutPolicy = LayoutPolicy.Preserve;
    }

    /* render the changes */ 
    refresh();

    /* update the connected node details panels */ 
    if(updateSubPanels) 
      updateSubPanels(true);
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
   private double
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
  private void
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
  private void
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
  public String
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
  public String
  getPrimarySelectionRootName() 
  {
    if(pPrimary != null) 
      return pPrimary.getNodePath().getRootName();
    return null;
  }

  /**
   * Get the fully resolved names of all selected nodes.
   */ 
  public TreeSet<String>
  getSelectedNames() 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(ViewerNode vnode : pSelected.values()) 
      names.add(vnode.getNodeStatus().getName());

    return names;
  }
  
  /**
   * Get the fully resolved names of the most downstream selected nodes. <P> 
   * 
   * Any nodes which are selected and are upstream of another selected node will be 
   * omitted from the returned names.
   */ 
  public TreeSet<String>
  getSelectedRootNames() 
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
  public void
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
  public void 
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
  public void 
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
   * Toggle the selection of the given viewer node.
   */ 
  public void 
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ViewerNode or ViewerLinkRelationship under the current mouse position. <P> 
   */ 
  private Object
  objectAtMousePos() 
  {
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
   GLDrawable drawable
  )
  {
    super.display(drawable); 
    GL gl = drawable.getGL();

    /* render the scene geometry */ 
    {
      if(pSceneDL.get() == 0) 
	pSceneDL.set(UIMaster.getInstance().getDisplayList(gl));
      
      if(pRefreshScene) {
	for(ViewerNode vnode : pViewerNodes.values()) 
	  vnode.rebuild(gl);
	pViewerLinks.rebuild(gl);

	gl.glNewList(pSceneDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	{
	  for(ViewerNode vnode : pViewerNodes.values()) 
	    vnode.render(gl);
	  pViewerLinks.render(gl);
	}
	gl.glEndList();

	pRefreshScene = false;
      }
      else {
	gl.glCallList(pSceneDL.get());
      }
    }
  }

  /**
   * Return the previously allocated OpenGL display lists to the pool of display lists to be 
   * reused. 
   */ 
  public void 
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

	      updateUniverse(false);
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
	    
	      NodeDetails details = pPrimary.getNodeStatus().getDetails();
	      if(details != null) {
		if(pIsLocked) {
		  updateEditorMenus();
		  pLockedNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
		else if(details.getWorkingVersion() == null) {
		  updateEditorMenus();
		  pCheckedInNodePopup.show(e.getComponent(), e.getX(), e.getY());
		}
		else if(details.getWorkingVersion().isFrozen()) {
		  updateEditorMenus();
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
	      updateToolMenu();
	      pToolPopup.show(e.getComponent(), e.getX(), e.getY());
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
	    for(ViewerNode vnode : pViewerNodes.values()) {
	      if(vnode.isInsideOf(bbox)) 
		addSelect(vnode);
	    }
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    for(ViewerNode vnode : pViewerNodes.values()) {
	      if(vnode.isInsideOf(bbox)) {
		toggleSelect(vnode);
	      }
	    }
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    for(ViewerNode vnode : pViewerNodes.values()) {
	      if(vnode.isInsideOf(bbox)) {
		addSelect(vnode);
	      }
	    }
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
      
      else if((prefs.getNodeViewerMakeRoot() != null) &&
	      prefs.getNodeViewerMakeRoot().wasPressed(e))
	doMakeRoot();
      else if((prefs.getNodeViewerAddRoot() != null) &&
	      prefs.getNodeViewerAddRoot().wasPressed(e))
	doAddRoot();
      else if((prefs.getNodeViewerReplaceRoot() != null) &&
	      prefs.getNodeViewerReplaceRoot().wasPressed(e))
	doReplaceRoot();
      else if((prefs.getNodeViewerRemoveRoot() != null) &&
	      prefs.getNodeViewerRemoveRoot().wasPressed(e))
	doRemoveRoot();
      
      else if((prefs.getEdit() != null) &&
	      prefs.getEdit().wasPressed(e))
	doEdit();

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
      else if((prefs.getNodeViewerEvolve() != null) &&
	      prefs.getNodeViewerEvolve().wasPressed(e))
	doEvolve();
      
      else if((prefs.getNodeViewerClone() != null) &&
	      prefs.getNodeViewerClone().wasPressed(e))
	doClone();
      else if((prefs.getNodeViewerRelease() != null) &&
	      prefs.getNodeViewerRelease().wasPressed(e))
	doRelease();
      else if((prefs.getRemoveFiles() != null) &&
	      prefs.getRemoveFiles().wasPressed(e))
	doRemoveFiles();

      else if((prefs.getNodeViewerRename() != null) &&
	      prefs.getNodeViewerRename().wasPressed(e))
	doRename();
      else if((prefs.getNodeViewerRenumber() != null) &&
	      prefs.getNodeViewerRenumber().wasPressed(e))
	doRenumber();
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
      
      else if((prefs.getNodeViewerShowHideDownstreamNodes() != null) &&
		prefs.getNodeViewerShowHideDownstreamNodes().wasPressed(e))
	doShowHideDownstream();
      
      else if((prefs.getNodeViewerRemoveAllRoots() != null) &&
	      prefs.getNodeViewerRemoveAllRoots().wasPressed(e))
	doRemoveAllRoots();

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
    /* node menu events */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("details"))
      doDetails();

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

    else if(cmd.equals("edit"))
      doEdit();
    else if(cmd.startsWith("edit-with:")) 
      doEditWith(cmd.substring(10));    

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
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("check-in"))
      doCheckIn();
    else if(cmd.equals("check-out"))
      doCheckOut();
    else if(cmd.equals("evolve"))
      doEvolve();

    else if(cmd.equals("clone"))
      doClone();
    else if(cmd.equals("release"))
      doRelease();
    else if(cmd.equals("remove-files"))
      doRemoveFiles();

    else if(cmd.equals("export"))
      doExport();
    else if(cmd.equals("rename"))
      doRename();
    else if(cmd.equals("renumber"))
      doRenumber();
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
  private void
  doUpdate()
  { 
    clearSelection();
    updateRoots();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node details panels with the current primary selected node status.
   */ 
  private void
  doDetails()
  {
    if(pPrimary != null) 
      updateSubPanels(pPrimary.getNodeStatus(), false);

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make the current primary selection the only root node.
   */ 
  private void
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

    addRoot(getPrimarySelectionName());

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
    roots.remove(getPrimarySelectionRootName());
    roots.add(getPrimarySelectionName());
    setRoots(roots);

    clearSelection();
  }
  
  /**
   * Remove the root node of the current primary selection from the set of roots nodes.
   */ 
  private synchronized void
  doRemoveRoot()
  {
    removeRoot(getPrimarySelectionRootName());

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
   * Edit/View the primary selected node with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    String ename = null;
    VersionID evid = null;
    String parts[] = editor.split(":");
    switch(parts.length) {
    case 1:
      ename = editor;
      break;

    case 2:
      ename = parts[0];
      evid = new VersionID(parts[1]);
      break;

    default:
      assert(false);
    }

    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, ename, evid);
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
  private void 
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
  private void 
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
  private void 
  doAddSecondary() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen()) {
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
  private void 
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
  private void 
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
  private void 
  doRename() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen() && (details.getLatestVersion() == null)) { 
	  pRenameDialog.updateNode(mod);
	  pRenameDialog.setVisible(true);
	
	  if(pRenameDialog.wasConfirmed()) {
	    try {
	      String name = pRenameDialog.getNewName();

	      RenameTask task = 
		new RenameTask(mod.getName(), name, pRenameDialog.renameFiles());
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
  private void 
  doRenumber() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeMod mod = details.getWorkingVersion();
	if((mod != null) && !mod.isFrozen() && (mod.getPrimarySequence().hasFrameNumbers())) {
	  pRenumberDialog.updateNode(mod);
	  pRenumberDialog.setVisible(true);
	
	  if(pRenumberDialog.wasConfirmed()) {
	    FrameRange range    = pRenumberDialog.getFrameRange();
	    boolean removeFiles = pRenumberDialog.removeFiles();

	    RenumberTask task = new RenumberTask(mod.getName(), range, removeFiles); 
	    task.start();
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
  private void 
  doClone() 
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      if(details != null) {

	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();
	assert(com != null);
	
	pRegisterDialog.updateNode(pAuthor, pView, com);
	pRegisterDialog.setVisible(true);

	TreeSet<String> names = pRegisterDialog.getRegistered();
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
  private void 
  doRegister() 
  {
    pRegisterDialog.updateNode(pAuthor, pView, null);
    pRegisterDialog.setVisible(true); 

    TreeSet<String> names = pRegisterDialog.getRegistered();
    if(!names.isEmpty()) 
      addRoots(names);
  }

  /**
   * Release the primary selected node.
   */ 
  private void 
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

      pReleaseDialog.updateHeader(header);
      pReleaseDialog.setVisible(true);
      
      if(pReleaseDialog.wasConfirmed()) {
	ReleaseTask task = new ReleaseTask(names, pReleaseDialog.removeFiles());
	task.start();
      }
    }
    
    clearSelection();
    refresh(); 
  }

  /**
   * Remove all primary/secondary files associated with the selected nodes.
   */ 
  private void 
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
	      new JConfirmDialog("Remove from Nodes without enabled Actions?");
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
  private void 
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

	pDeleteDialog.updateHeader("Delete Node:  " + text);
	pDeleteDialog.setVisible(true);
      
	if(pDeleteDialog.wasConfirmed()) {
	  JConfirmDialog confirm = new JConfirmDialog("Are you sure?");
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
  private void 
  doQueueJobs() 
  {
    TreeSet<String> roots = new TreeSet<String>();
    for(String name : getSelectedRootNames()) {
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
  private void 
  doQueueJobsSpecial() 
  {
    TreeSet<String> roots = new TreeSet<String>();
    for(String name : getSelectedRootNames()) {
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
	  
	TreeSet<String> keys = null;
	if(diag.overrideSelectionKeys()) 
	  keys = diag.getSelectionKeys();

	QueueJobsTask task = new QueueJobsTask(roots, batchSize, priority, interval, keys);
	task.start();
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Pause all waiting jobs associated with the selected nodes.
   */ 
  private void 
  doPauseJobs() 
  {
    TreeSet<Long> paused = new TreeSet<Long>();
    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Queued:
	    assert(jobIDs[wk] != null);
	    paused.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!paused.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(paused);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Resume execution of all paused jobs associated with the selected nodes.
   */ 
  private void 
  doResumeJobs() 
  {
    TreeSet<Long> resumed = new TreeSet<Long>();
    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Paused:
	    assert(jobIDs[wk] != null);
	    resumed.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!resumed.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumed);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Kill all jobs associated with the selected nodes.
   */ 
  private void 
  doKillJobs() 
  {
    TreeSet<Long> dead = new TreeSet<Long>();
    for(ViewerNode vnode : pSelected.values()) {
      NodeStatus status = vnode.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
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
	    dead.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!dead.isEmpty()) {
      KillJobsTask task = new KillJobsTask(dead);
      task.start();
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Check-in the primary selected node.
   */ 
  private void 
  doCheckIn() 
  {
    try {
      TreeSet<String> roots = getSelectedRootNames();
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
  private void 
  doCheckOut() 
  {
    if(pPrimary != null) {
      NodeStatus status = pPrimary.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	UIMaster master = UIMaster.getInstance();
	ArrayList<VersionID> vids = new ArrayList<VersionID>();
	try {
	  vids.addAll(master.getMasterMgrClient().getCheckedInVersionIDs(status.getName()));
	}
	catch (PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}

	pCheckOutDialog.updateNameVersions("Check-Out:  " + status, vids);
	pCheckOutDialog.setVisible(true);
	
	if(pCheckOutDialog.wasConfirmed()) {
	  VersionID vid = pCheckOutDialog.getVersionID();
	  if(vid != null) {
	    CheckOutTask task = 
	      new CheckOutTask(status.getName(), vid, 
			       pCheckOutDialog.getMode(), pCheckOutDialog.getMethod());
	    task.start();
	  }
	}
      }
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Evolve the primary selected node.
   */ 
  private void 
  doEvolve() 
  {
    if(pPrimary != null) {
      NodeStatus status = pPrimary.getNodeStatus();
      NodeDetails details = status.getDetails();
      if(details != null) {
	NodeMod work = details.getWorkingVersion();
	if((work != null) && !work.isFrozen()) {
	  pEvolveDialog.updateNameVersions("Evolve Version:  " + status, work.getWorkingID(),
					   details.getVersionIDs());
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


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Edit an existing link.
   */ 
  private void 
  doLinkEdit()
  {
    if(pPrimary != null) {
      NodeDetails details = pPrimary.getNodeStatus().getDetails();
      NodeMod mod = details.getWorkingVersion();
      if((mod != null) && !mod.isFrozen()) {
	if((pSelectedLink != null) && (pSelectedLink instanceof LinkMod)) {
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
  private void 
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
  private void 
  doFrameSelection() 
  {
    doFrameNodes(pSelected.values());
  }

  /**
   * Move the camera to frame all active nodes.
   */ 
  private void 
  doFrameAll() 
  {
    doFrameNodes(pViewerNodes.values());
  }

  /**
   * Move the camera to frame the given set of nodes.
   */ 
  private void 
  doFrameNodes
  (
   Collection<ViewerNode> vnodes
  ) 
  {
    doFrameBounds(getNodeBounds(vnodes));
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the nodes.
   */ 
  private void
  doAutomaticExpand()
  {
    clearSelection();
    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the nodes.
   */ 
  private void
  doExpandAll()
  {
    clearSelection();
    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the nodes.
   */ 
  private void
  doCollapseAll()
  {
    clearSelection();
    pLayoutPolicy = LayoutPolicy.CollapseAll;
    updateUniverse();
  }

  
  /**
   * Show/Hide the downstream node tree.
   */ 
  private void
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
  private void 
  doRunTool
  (
   String name 
  ) 
  {
    String tname = null;
    VersionID tvid = null;
    String parts[] = name.split(":");
    switch(parts.length) {
    case 1:
      tname = name;
      break;

    case 2:
      tname = parts[0];
      tvid = new VersionID(parts[1]);
      break;

    default:
      assert(false);
    }
    
    try {
      BaseTool tool = PluginMgr.getInstance().newTool(tname, tvid);

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

      tool.setSelectedNodes(primary, selected);

      if(tool.isInteractive()) {
	JToolDialog diag = new JToolDialog(tool);
	tool.update();
	
	diag.setVisible(true);
	if(!diag.wasConfirmed()) 
	  return;
      }
	
      tool.validate();

      RunToolTask task = new RunToolTask(tool);
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
      encoder.encode("InitialCenter", bbox.getCenter());
    } 

    /* whether to show the downstram links */
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
    
    /* whether to show the downstram links */    
    Boolean show = (Boolean) decoder.decode("ShowDownstream");
    if(show != null) 
      pShowDownstream = show; 
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
      UIMaster.getInstance().super(com, pAuthor, pView);
      setName("JNodeViewerPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid
    ) 
    {
      UIMaster.getInstance().super(com, ename, evid, pAuthor, pView);
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
      if(master.beginPanelOp("Linking Nodes...")) {
	TreeSet<String> linked = new TreeSet<String>();
	try {
	  for(String source : pSources) {
	    master.getMasterMgrClient().link(pAuthor, pView, pTarget, source, 
					     pPolicy, pRelationship, pFrameOffset);
	    linked.add(source);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

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
      if(master.beginPanelOp("Unlinking Nodes...")) {
	TreeSet<String> unlinked = new TreeSet<String>();
	try {
	  for(String source : pSources) {
	    master.getMasterMgrClient().unlink(pAuthor, pView, pTarget, source);
	    unlinked.add(source);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
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
      if(master.beginPanelOp("Adding Secondary File Sequence...")) {
	try {
	  master.getMasterMgrClient().addSecondary(pAuthor, pView, pTarget, pFileSeq);
	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	  return;
 	}
	finally {
	  master.endPanelOp("Done.");
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
      if(master.beginPanelOp("Removing Secondary File Sequence...")) {
	try {
	  master.getMasterMgrClient().removeSecondary(pAuthor, pView, pTarget, pFileSeq);
	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	  return;
 	}
	finally {
	  master.endPanelOp("Done.");
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
      if(master.beginPanelOp("Exporting Node Properties...")) {
	StringBuffer warn = new StringBuffer();
	try {
	  MasterMgrClient client = master.getMasterMgrClient();
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
	      {
		BaseAction saction = smod.getAction(); 
		if((saction != null) && pExportDialog.exportAction()) {
		  
		  /* the action and parameters */ 
		  {
		    PluginMgr mgr = PluginMgr.getInstance();
		    if((taction == null) || !taction.getName().equals(saction.getName())) 
		      taction = mgr.newAction(saction.getName(), saction.getVersionID()); 
		    
		    for(ActionParam param : saction.getSingleParams()) {
		      if(pExportDialog.exportActionSingleParam(param.getName())) 
			taction.setSingleParamValue(param.getName(), param.getValue());
		    }
		    
		    if(pExportDialog.exportActionSourceParams()) {
		      for(String source : saction.getSourceNames()) {
			if(tmod.getSource(source) != null) {
			  taction.removeSourceParams(source);
			  taction.initSourceParams(source);
			  for(ActionParam param : saction.getSourceParams(source)) {
			    taction.setSourceParamValue(source, 
							param.getName(), param.getValue());
			  }
			}
		      }
		    }
		    
		    tmod.setAction(taction);
		  }

		  /* action enabled */ 
		  if(pExportDialog.exportActionEnabled()) 
		    tmod.setActionEnabled(smod.isActionEnabled()); 
		}
	      }
		  
	      if(taction != null) {
		/* execution details */ 
		{
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
	  master.endPanelOp("Done.");
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
     String oldName, 
     String newName,
     boolean renameFiles
    ) 
    {
      super("JNodeViewerPanel:RenameTask");

      pOldName     = oldName; 
      pNewName     = newName; 
      pRenameFiles = renameFiles;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Renaming Node...")) {
	try {
	  master.getMasterMgrClient().rename(pAuthor, pView, 
					     pOldName, pNewName, pRenameFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	renameRoot(pOldName, pNewName);
      }
    }

    private String   pOldName; 
    private String   pNewName; 
    private boolean  pRenameFiles; 
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
      if(master.beginPanelOp("Renumbering Node...")) {
	try {
	  master.getMasterMgrClient().renumber(pAuthor, pView, pName, 
					       pFrameRange, pRemoveFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateRoots();
      }
    }

    private String      pName; 
    private FrameRange  pFrameRange; 
    private boolean     pRemoveFiles; 
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
     TreeSet<String> names, 
     boolean removeFiles
    ) 
    {
      super("JNodeViewerPanel:ReleaseTask");

      pNames       = names; 
      pRemoveFiles = removeFiles;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp()) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp("Releasing Node: " + name);	
	    master.getMasterMgrClient().release(pAuthor, pView, name, pRemoveFiles);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateRoots();
      }
    }

    private TreeSet<String>  pNames; 
    private boolean          pRemoveFiles; 
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
      if(master.beginPanelOp("Deleting Node: " + pName)) {
	try {	
	  master.getMasterMgrClient().delete(pName, pRemoveFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
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
      UIMaster.getInstance().super(names, pAuthor, pView);
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
      this(names, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     TreeSet<String> names, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      UIMaster.getInstance().super(names, pAuthor, pView, 
				   batchSize, priority, rampUp, selectionKeys);
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
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeViewerPanel:PauseJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      updateRoots();
    }

    private TreeSet<Long>  pJobIDs; 
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
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeViewerPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      updateRoots();
    }

    private TreeSet<Long>  pJobIDs; 
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
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeViewerPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      updateRoots();
    }

    private TreeSet<Long>  pJobIDs; 
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
      if(master.beginPanelOp()) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp("Checking-In: " + name);
	    master.getMasterMgrClient().checkIn(pAuthor, pView, name, pDescription, pLevel);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
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
     String name, 
     VersionID vid, 
     CheckOutMode mode,
     CheckOutMethod method
    ) 
    {
      super("JNodeViewerPanel:CheckOutTask");

      pName      = name; 
      pVersionID = vid; 
      pMode      = mode; 
      pMethod    = method; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Checking-Out Nodes...")) {
	try {
	  master.getMasterMgrClient().checkOut(pAuthor, pView, pName, pVersionID, 
					       pMode, pMethod);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateRoots();
      }
    }

    private String          pName; 
    private VersionID       pVersionID; 
    private CheckOutMode    pMode;  
    private CheckOutMethod  pMethod; 
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
      if(master.beginPanelOp("Evolving Node: " + pName)) {
	try {
	  master.getMasterMgrClient().evolve(pAuthor, pView, pName, pVersionID);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateRoots();
      }
    }

    private String     pName; 
    private VersionID  pVersionID; 
  }


  /** 
   * Get the status of the root nodes.
   */ 
  private
  class StatusTask
    extends Thread
  {
    public 
    StatusTask
    (
     JNodeViewerPanel viewer, 
     boolean updateSubPanels 
    ) 
    {
      super("JNodeViewerPanel:StatusTask");

      pViewer = viewer;
      pUpdateSubPanels = updateSubPanels;
    }
 
    public void 
    run() 
    {
      synchronized(pViewer) {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Updating Node Status...")) {
	  try {
	    TreeSet<String> dead = new TreeSet<String>();
	    for(String name : pRoots.keySet()) {
	      if(pRoots.get(name) == null) {
		try {
		  MasterMgrClient client = master.getMasterMgrClient();
		  NodeStatus status = client.status(pAuthor, pView, name);
		  pRoots.put(name, status);
		}
		catch(PipelineException ex) {
		  dead.add(name);
		}
	      }
	    }
	    
	    for(String name : dead) 
	      pRoots.remove(name);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	  
	  updateUniverse(pUpdateSubPanels);
	}
      }
    }
    
    private JNodeViewerPanel  pViewer;
    private boolean           pUpdateSubPanels;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Run the given tool.
   */ 
  private
  class RunToolTask
    extends Thread
  {
    public 
    RunToolTask
    (
     BaseTool tool
    ) 
    {
      super("JNodeViewerPanel:RunToolTask");
      pTool = tool;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Running " + pTool.getName() + "...")) {
	try {
	  pTool.execute(master.getMasterMgrClient(), master.getQueueMgrClient());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateRoots();
      }
    }

    private BaseTool  pTool;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the subpanels.
   */
  private 
  class UpdateSubPanelsTask
    extends Thread
  {
    public 
    UpdateSubPanelsTask
    (
     int groupID, 
     String author, 
     String view, 
     NodeStatus status, 
     boolean updateJobs
    )
    {      
      super("JNodeViewerPanel:UpdateSubPanelsTask");

      pGroupID    = groupID;
      pAuthor     = author;
      pView       = view;
      pStatus     = status;
      pUpdateJobs = updateJobs; 
    }

    public void 
    run()
    {
      UIMaster master = UIMaster.getInstance();

      TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty = null;
      TreeMap<VersionID,LogMessage> history = null;

      TreeMap<Long,QueueJobGroup> jobGroups = null; 
      TreeMap<Long,JobStatus> jobStatus = null; 
      TreeMap<Long,QueueJobInfo> jobInfo = null;
      TreeMap<String,QueueHost> hosts = null;
      TreeMap<String,String> keys = null;
      
      if(pStatus != null) {
	{
	  PanelGroup<JNodeFilesPanel> panels = master.getNodeFilesPanels();
	  JNodeFilesPanel panel = panels.getPanel(pGroupID);
	  if(panel != null) {
	    if(master.beginPanelOp("Updating Node Files...")) {
	      try {
		MasterMgrClient client = master.getMasterMgrClient();
		novelty = client.getCheckedInFileNovelty(pStatus.getName());
	      }
	      catch(PipelineException ex) {
		master.showErrorDialog(ex);
	      }
	      finally {
		master.endPanelOp("Done.");
	      }
	    }
	  }
	}

	NodeDetails details = pStatus.getDetails();
	if((details != null) && (details.getLatestVersion() != null)) {
	  PanelGroup<JNodeHistoryPanel> panels = master.getNodeHistoryPanels();
	  JNodeHistoryPanel panel = panels.getPanel(pGroupID);
	  if(panel != null) {
	    if(master.beginPanelOp("Updating Node History...")) {
	      try {
		MasterMgrClient client = master.getMasterMgrClient();
		history = client.getHistory(pStatus.getName());
	      }
	      catch(PipelineException ex) {
		master.showErrorDialog(ex);
	      }
	      finally {
		master.endPanelOp("Done.");
	      }
	    }
	  }
	}
      }

      if(pUpdateJobs) {
	PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();
	JQueueJobBrowserPanel panel = panels.getPanel(pGroupID);

	if(panel != null) {
	  if(master.beginPanelOp("Updating Jobs...")) {
	    try {
	      QueueMgrClient client = master.getQueueMgrClient();

	      jobGroups = client.getJobGroups(); 
	      jobStatus = client.getJobStatus(new TreeSet<Long>(jobGroups.keySet()));
	      jobInfo   = client.getRunningJobInfo();
	      hosts     = client.getHosts(); 

	      keys = new TreeMap<String,String>();
	      for(SelectionKey key : client.getSelectionKeys()) 
		keys.put(key.getName(), key.getDescription());
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	    }
	    finally {
	      master.endPanelOp("Done.");
	    }
	  }
	}
      }

      UpdateSubPanelComponentsTask task = 
	new UpdateSubPanelComponentsTask(pGroupID, pAuthor, pView, pStatus, 
					 pEditorPlugins, pEditorMenuLayout, 
					 novelty, history, 
					 pUpdateJobs, jobGroups, jobStatus, jobInfo, 
					 hosts, keys);
      SwingUtilities.invokeLater(task);
    }
    
    private int         pGroupID;
    private String      pAuthor;
    private String      pView; 
    private NodeStatus  pStatus;
    private boolean     pUpdateJobs; 
  }
  
  /**
   * Update the subpanels UI components.
   */
  private 
  class UpdateSubPanelComponentsTask
    extends Thread
  {
    public 
    UpdateSubPanelComponentsTask
    (
     int groupID, 
     String author, 
     String view, 
     NodeStatus status, 
     TreeMap<String,TreeSet<VersionID>> editorPlugins, 
     PluginMenuLayout editorLayout,
     TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty,
     TreeMap<VersionID,LogMessage> history,
     boolean updateJobs, 
     TreeMap<Long,QueueJobGroup> jobGroups, 
     TreeMap<Long,JobStatus> jobStatus,
     TreeMap<Long,QueueJobInfo> jobInfo, 
     TreeMap<String,QueueHost> hosts, 
     TreeMap<String,String> keys
    )
    {      
      super("JNodeViewerPanel:UpdateSubPanelComponentsTask");

      pGroupID = groupID;
      pAuthor  = author;
      pView    = view;
      pStatus  = status;

      pEditorPlugins = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : editorPlugins.keySet()) 
	pEditorPlugins.put(name, new TreeSet<VersionID>(editorPlugins.get(name)));

      pEditorMenuLayout = new PluginMenuLayout(editorLayout);

      pNovelty = novelty;
      pHistory = history;

      pUpdateJobs = updateJobs; 
      pJobGroups  = jobGroups; 
      pJobStatus  = jobStatus;
      pJobInfo    = jobInfo;
      pHosts      = hosts; 
      pKeys       = keys;
    }

    public void 
    run()
    {
      UIMaster master = UIMaster.getInstance(); 

      {
	PanelGroup<JNodeDetailsPanel> panels = master.getNodeDetailsPanels();
	JNodeDetailsPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateNodeStatus(pAuthor, pView, pStatus, 
				 pEditorPlugins, pEditorMenuLayout);
	  panel.updateManagerTitlePanel();
	}
      }

      {
	PanelGroup<JNodeFilesPanel> panels = master.getNodeFilesPanels();
	JNodeFilesPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateNodeStatus(pAuthor, pView, pStatus, 
				 pEditorPlugins, pEditorMenuLayout, 
				 pNovelty);
	  panel.updateManagerTitlePanel();
	}
      }
      
      {
	PanelGroup<JNodeHistoryPanel> panels = master.getNodeHistoryPanels();
	JNodeHistoryPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateNodeStatus(pAuthor, pView, pStatus, 
				 pEditorPlugins, pEditorMenuLayout, 
				 pHistory);
	  panel.updateManagerTitlePanel();
	}
      }

      if(pUpdateJobs) {
	PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();
	JQueueJobBrowserPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateJobs(pAuthor, pView, 
			   pJobGroups, pJobStatus, pJobInfo, pHosts, pKeys);
	  panel.updateManagerTitlePanel();
	}
      }
    }
    
    private int         pGroupID;
    private String      pAuthor;
    private String      pView; 
    private NodeStatus  pStatus;

    private TreeMap<String,TreeSet<VersionID>>  pEditorPlugins; 
    private PluginMenuLayout  pEditorMenuLayout;

    private TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>  pNovelty;
    private TreeMap<VersionID,LogMessage>                  pHistory;

    private boolean                      pUpdateJobs; 
    private TreeMap<Long,QueueJobGroup>  pJobGroups;
    private TreeMap<Long,JobStatus>      pJobStatus; 
    private TreeMap<Long,QueueJobInfo>   pJobInfo; 
    private TreeMap<String,QueueHost>    pHosts;
    private TreeMap<String,String>       pKeys;
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
   * Whether to display the downstream tree of nodes.
   */ 
  private boolean  pShowDownstream;

  /**
   * The fully resolved name of the node who's status was last sent to the node 
   * details, links, files and history panels. 
   */ 
  private String  pLastDetailsName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached names and version numbers of the loaded editor plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pEditorPlugins; 

  /**
   * The menu layout for editor plugins.
   */ 
  private PluginMenuLayout  pEditorMenuLayout;

  /**
   * Whether the Swing editor menus need to be rebuild from the menu layout.
   */ 
  private boolean pRefreshEditorMenus; 


  /**
   * Cached names and version numbers of the loaded tool plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pToolPlugins; 

  /**
   * The menu layout for tool plugins.
   */ 
  private PluginMenuLayout  pToolMenuLayout;

  /**
   * Whether the Swing tool menus need to be rebuild from the menu layout.
   */ 
  private boolean pRefreshToolMenu; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The currently displayed nodes indexed by <CODE>NodePath</CODE>.
   */ 
  private HashMap<NodePath,ViewerNode>  pViewerNodes; 

  /**
   * The currently displayed node links. 
   */ 
  private ViewerLinks  pViewerLinks; 


  /**
   * The currently selected nodes indexed by <CODE>NodePath</CODE>.
   */ 
  private HashMap<NodePath,ViewerNode>  pSelected;

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
  private JMenuItem  pRegisterItem;
  private JMenuItem  pShowHideDownstreamItem;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The tool plugin popup menu.
   */ 
  private JPopupMenu  pToolPopup; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node popup menus.
   */ 
  private JPopupMenu  pUndefinedNodePopup; 
  private JPopupMenu  pLockedNodePopup; 
  private JPopupMenu  pCheckedInNodePopup; 
  private JPopupMenu  pFrozenNodePopup; 
  private JPopupMenu  pNodePopup; 
  
  /**
   * The node popup menu items.
   */ 
  private JMenuItem  pLinkItem;
  private JMenuItem  pUnlinkItem;
  private JMenuItem  pExportItem;
  private JMenuItem  pRenameItem;
  private JMenuItem  pRenumberItem;
  private JMenuItem  pCheckOutItem;
  private JMenuItem  pEvolveItem;
  private JMenuItem  pDeleteItem;

  /**
   * The edit with submenus.
   */ 
  private JMenu[]  pEditWithMenus; 

  /**
   * The remove secondary node submenu.
   */ 
  private JMenu  pRemoveSecondaryMenu;
  
  /** 
   * The table of secondary file sequences currently displayed in the remove 
   * secondary node submenu indexed by secondary sequence name.
   */ 
  private TreeMap<String,FileSeq>  pRemoveSecondarySeqs;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The link popup menu.
   */ 
  private JPopupMenu  pLinkPopup;


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
   * The release node dialog.
   */ 
  private JReleaseDialog  pReleaseDialog;

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
   * The evolve node dialog.
   */ 
  private JEvolveDialog  pEvolveDialog;


  /**
   * The link creation dialog.
   */ 
  private JCreateLinkDialog  pCreateLinkDialog;

  /**
   * The link editor dialog.
   */ 
  private JEditLinkDialog  pEditLinkDialog;

}
