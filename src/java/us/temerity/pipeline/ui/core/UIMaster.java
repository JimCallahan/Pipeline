// $Id: UIMaster.java,v 1.62 2007/04/15 10:30:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.core.JobMgrPlgControlClient;
import us.temerity.pipeline.core.LockedGlueFile;
import us.temerity.pipeline.core.GlueLockException;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   U I   M A S T E R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides centralized management of the user interface components and network 
 * communication.
 */ 
public 
class UIMaster
   implements WindowListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   * 
   * @param layout
   *   The abstract pathname of the override panel layout or 
   *   <CODE>null</CODE> to use the default layout.
   * 
   * @param restoreLayout
   *   Whether to restore the panel layout.
   * 
   * @param restoreSelections
   *   Whether the restored layout should include node and/or job group selections.
   * 
   * @param debugGL
   *   Whether to check all OpenGL calls for errors.
   * 
   * @param traceGL
   *   Whether to print all OpenGL calls to STDOUT.
   */ 
  private 
  UIMaster
  (
   Path layout, 
   boolean restoreLayout,
   boolean restoreSelections, 
   boolean debugGL, 
   boolean traceGL
  ) 
  {
    pMasterMgrClients = new MasterMgrClient[10];
    pQueueMgrClients  = new QueueMgrClient[10];

    {
      pOpsLocks   = new ReentrantLock[10];
      pOpsRunning = new AtomicBoolean[10];
      pOpsTimers  = new TaskTimer[10];
      int wk; 
      for(wk=0; wk<pOpsLocks.length; wk++) {
	pOpsLocks[wk]   = new ReentrantLock();
	pOpsRunning[wk] = new AtomicBoolean(false);
      }
    }

    pEditorPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pComparatorPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pActionPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();  
    pToolPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pArchiverPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pMasterExtPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pQueueExtPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    
    pEditorLayouts     = new TreeMap<String,PluginMenuLayout>();                   
    pComparatorLayouts = new TreeMap<String,PluginMenuLayout>();                  
    pActionLayouts     = new TreeMap<String,PluginMenuLayout>();                    
    pToolLayouts       = new TreeMap<String,PluginMenuLayout>();                   
    pArchiverLayouts   = new TreeMap<String,PluginMenuLayout>();                   
    pMasterExtLayouts   = new TreeMap<String,PluginMenuLayout>();                   
    pQueueExtLayouts   = new TreeMap<String,PluginMenuLayout>();                   

    pNodeBrowserPanels = new PanelGroup<JNodeBrowserPanel>();
    pNodeViewerPanels  = new PanelGroup<JNodeViewerPanel>();
    pNodeDetailsPanels = new PanelGroup<JNodeDetailsPanel>();
    pNodeHistoryPanels = new PanelGroup<JNodeHistoryPanel>();
    pNodeFilesPanels   = new PanelGroup<JNodeFilesPanel>();
    pNodeLinksPanels   = new PanelGroup<JNodeLinksPanel>();

    pQueueJobServersPanels     = new PanelGroup<JQueueJobServersPanel>();
    pQueueJobServerStatsPanels = new PanelGroup<JQueueJobServerStatsPanel>();
    pQueueJobSlotsPanels       = new PanelGroup<JQueueJobSlotsPanel>();
    pQueueJobBrowserPanels     = new PanelGroup<JQueueJobBrowserPanel>();
    pQueueJobViewerPanels      = new PanelGroup<JQueueJobViewerPanel>();
    pQueueJobDetailsPanels     = new PanelGroup<JQueueJobDetailsPanel>();

    pOverrideLayoutPath = layout;
    pRestoreLayout      = restoreLayout;
    pRestoreSelections  = restoreSelections; 
    pIsRestoring        = new AtomicBoolean();

    pCollapsedNodePaths = new HashSet<String>();

    pDebugGL = debugGL;
    pTraceGL = traceGL; 
    pDisplayLists = new TreeSet<Integer>();

    SwingUtilities.invokeLater(new SplashFrameTask(this));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the user interface and connection to <B>plmaster<B>(1).
   * 
   * @param layout
   *   The abstract pathname of the override panel layout 
   *   or <CODE>null</CODE> to use the default layout.
   * 
   * @param restoreLayout
   *   Whether to restore the panel layout.
   * 
   * @param restoreSelections
   *   Whether the restored layout should include node and/or job group selections.
   * 
   * @param debugGL
   *   Whether to check all OpenGL calls for errors.
   * 
   * @param traceGL
   *   Whether to print all OpenGL calls to STDOUT.
   */ 
  public static void 
  init
  (
   Path layout,
   boolean restoreLayout,
   boolean restoreSelections,
   boolean debugGL, 
   boolean traceGL
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(layout, restoreLayout, restoreSelections, debugGL, traceGL);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the UIMaster instance.
   */ 
  public static UIMaster
  getInstance() 
  {
    return sMaster;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the network connection to <B>plmaster</B>(1).
   */ 
  public MasterMgrClient
  getMasterMgrClient() 
  {
    return getMasterMgrClient(0);
  }

  /**
   * Get the network connection to <B>plmaster</B>(1).
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public MasterMgrClient
  getMasterMgrClient
  (
   int channel
  ) 
  {
    assert((channel >= 0) && (channel < 10)); 

    if(pMasterMgrClients[channel] == null) 
      pMasterMgrClients[channel] = new MasterMgrClient();

    return pMasterMgrClients[channel];
  }


  /**
   * Get the network connection to <B>plqueuemgr</B>(1).
   */ 
  public QueueMgrClient
  getQueueMgrClient() 
  {
    return getQueueMgrClient(0);
  }

  /**
   * Get the network connection to <B>plqueuemgr</B>(1).
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public QueueMgrClient
  getQueueMgrClient
  (
   int channel
  ) 
  {
    assert((channel >= 0) && (channel < 10)); 

    if(pQueueMgrClients[channel] == null) 
      pQueueMgrClients[channel] = new QueueMgrClient();

    return pQueueMgrClients[channel];
  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create and show a new secondary panel frame.
   * 
   * @return 
   *   The newly created frame.
   */ 
  public JPanelFrame
  createWindow() 
  {
    JPanelFrame frame = new JPanelFrame(); 
    pPanelFrames.add(frame);

    frame.setVisible(true);

    return frame;
  }
  
  /**
   * Destroy an existing secondary panel frame.
   */ 
  public void 
  destroyWindow
  (
   JPanelFrame frame
  ) 
  {
    pPanelFrames.remove(frame);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the restored layouts should include node and/or job group selections.
   */ 
  public boolean
  restoreSelections() 
  {
    return pRestoreSelections;
  }

  /**
   * Whether the layout restore is in progress.
   */ 
  public boolean
  isRestoring() 
  {
    return pIsRestoring.get();
  }

  /**
   * Get the abstract pathname of the current panel layout.
   * 
   * @return 
   *   The path or <CODE>null</CODE> if unset.
   */ 
  public Path
  getLayoutPath() 
  {
    return pLayoutPath;
  }

  /**
   * Set the abstract pathname of the current panel layout.
   */ 
  public void 
  setLayoutPath
  (
   Path path
  ) 
  {
    pLayoutPath = path;
    updateFrameHeaders();
  }


  /**
   * Get the abstract pathname of the default panel layout.
   * 
   * @return 
   *   The path or <CODE>null</CODE> if unset.
   */ 
  public Path
  getDefaultLayoutPath() 
  {
    return pDefaultLayoutPath;
  }
  
  /**
   * Set the abstract pathname of the default panel layout.
   */ 
  public void 
  setDefaultLayoutPath
  (
   Path path
  ) 
  {
    pDefaultLayoutPath = path; 
    updateFrameHeaders();
  }

  /** 
   * Add the layout name to the top-level frame headers.
   */ 
  private void 
  updateFrameHeaders()
  {
    String title = "plui";
    if(pLayoutPath != null) {
      String def = "";
      if((pDefaultLayoutPath != null) && pDefaultLayoutPath.equals(pLayoutPath))
	def = " (default)";
      title = ("plui - Main | " + pLayoutPath.getName() + def);
    }

    pFrame.setTitle(title);    
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Save the collapsed state of the given viewer node.
   * 
   * @param path
   *   The unique path to the viewer node.
   * 
   * @param wasCollapsed
   *   Whether the viewer node is currently collapsed.
   */ 
  public void 
  setNodeCollapsed
  (
   String path,
   boolean wasCollapsed
  ) 
  {
    synchronized(pCollapsedNodePaths) {
      if(wasCollapsed) 
	pCollapsedNodePaths.add(path);
      else 
	pCollapsedNodePaths.remove(path);
    }
  }

  /**
   * Whether the given viewer node was previously collapsed.
   * 
   * @param path
   *   The unique path to the viewer node.
   */ 
  public boolean
  wasNodeCollapsed
  (
   String path 
  ) 
  {
    synchronized(pCollapsedNodePaths) {
      return pCollapsedNodePaths.contains(path);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Rebuild the Views Containing menu for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaContainingMenu
  ( 
   int channel, 
   String name,
   JMenu menu,
   ActionListener listener
  ) 
  {
    rebuildWorkingAreaContainingMenu(channel, name, null, menu, listener);     
  }

  /**
   * Rebuild the Views Containing menu for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaContainingMenu
  ( 
   int channel, 
   String name,
   JPopupMenu topmenu,
   JMenu menu,
   ActionListener listener
  ) 
  {
    TreeMap<String,TreeSet<String>> views = lookupWorkingAreaContainingMenus(channel, name); 
    ArrayList<TreeSet<String>> authors = groupWorkingAreaSubmenus(views);

    rebuildWorkingAreaMenuSingle(topmenu, menu, listener, views, authors);    
  }

  /**
   * Rebuild the Views Containing menu for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param topmenus
   *   The top-level popup menus containing the items.
   * 
   * @param menus
   *   The menus to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaContainingMenus
  ( 
   int channel, 
   String name,
   JPopupMenu topmenus[],
   JMenu menus[],
   ActionListener listener
  ) 
  {
    TreeMap<String,TreeSet<String>> views = lookupWorkingAreaContainingMenus(channel, name);
    ArrayList<TreeSet<String>> authors = groupWorkingAreaSubmenus(views);
    
    int wk;
    for(wk=0; wk<menus.length; wk++) 
      rebuildWorkingAreaMenuSingle(topmenus[wk], menus[wk], listener, views, authors);    
  }

  /**
   * Rebuild the Views Editing menu for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaEditingMenu
  ( 
   int channel, 
   String name,
   JMenu menu,
   ActionListener listener
  ) 
  {
    rebuildWorkingAreaEditingMenu(channel, name, null, menu, listener);
  }

  /**
   * Rebuild the Views Editing menu for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaEditingMenu
  ( 
   int channel, 
   String name,
   JPopupMenu topmenu, 
   JMenu menu,
   ActionListener listener
  ) 
  {
    TreeMap<String,TreeSet<String>> views = lookupWorkingAreaEditingMenus(channel, name); 
    ArrayList<TreeSet<String>> authors = groupWorkingAreaSubmenus(views);

    rebuildWorkingAreaMenuSingle(topmenu, menu, listener, views, authors);    
  }

  /**
   * Rebuild the Views Editing menu to for the given node.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param topmenus
   *   The top-level popup menus containing the items.
   * 
   * @param menus
   *   The menus to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void 
  rebuildWorkingAreaEditingMenus
  ( 
   int channel, 
   String name,
   JPopupMenu topmenus[],
   JMenu menus[],
   ActionListener listener
  ) 
  {
    TreeMap<String,TreeSet<String>> views = lookupWorkingAreaEditingMenus(channel, name); 
    ArrayList<TreeSet<String>> authors = groupWorkingAreaSubmenus(views);
    
    int wk;
    for(wk=0; wk<menus.length; wk++) 
      rebuildWorkingAreaMenuSingle(topmenus[wk], menus[wk], listener, views, authors);    
  }

  /**
   * Lookup the working areas containing a given node. 
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @return
   *   The names of the working area views indexed by author name.
   */ 
  private TreeMap<String,TreeSet<String>>
  lookupWorkingAreaContainingMenus
  (
   int channel, 
   String name
  )
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    if(name != null) {
      UIMaster master = UIMaster.getInstance();
      if(master.beginSilentPanelOp(channel)) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(channel);
	  views.putAll(client.getWorkingAreasContaining(name));
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endSilentPanelOp(channel);
	}
      }
    }

    return views;
  }

  /**
   * Lookup the working areas currently editing a given node. 
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @return
   *   The names of the working area views indexed by author name.
   */ 
  private TreeMap<String,TreeSet<String>>
  lookupWorkingAreaEditingMenus
  (
   int channel, 
   String name
  )
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    if(name != null) {
      UIMaster master = UIMaster.getInstance();
      if(master.beginSilentPanelOp(channel)) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient(channel);
	  views.putAll(client.getWorkingAreasEditing(name));
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endSilentPanelOp(channel);
	}
      }
    }

    return views;
  }

  /**
   * Group the working area views into reasonable submenus.
   * 
   * @param views
   *   The names of the working area views indexed by author name.
   * 
   * @return
   *   The names of the working area authors grouped by submenu.
   */ 
  private ArrayList<TreeSet<String>> 
  groupWorkingAreaSubmenus
  (
   TreeMap<String,TreeSet<String>> views
  ) 
  {
    ArrayList<TreeSet<String>> authors = new ArrayList<TreeSet<String>>();

    int numAuthors = views.size();
    int maxPerMenu = 12;
    if(numAuthors > maxPerMenu) {
      int numMenus = Math.max(numAuthors / maxPerMenu, 2);
      int perMenu  = numAuthors / numMenus;
      int extra    = numAuthors % perMenu;
      
      int cnt = 0;
      int max = 0;
      TreeSet<String> agroup = null;
      for(String author : views.keySet()) {
	if(cnt == 0) {
	  agroup = new TreeSet<String>();
	  authors.add(agroup);
	  
	  max = perMenu - 1;
	  if(extra > 0) 
	    max++;
	  extra--;
	}
	  
	agroup.add(author);
	cnt++;
	
	if(cnt > max) 
	  cnt = 0;
      }
    }

    return authors;
  }

  /**
   * Rebuild the Change Owner|View menu to for the working areas containing the given node.
   *
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   * 
   * @param views
   *   The names of the working area views indexed by author name.
   * 
   * @param authors
   *   The names of the working area authors grouped by submenu.
   */ 
  private synchronized void 
  rebuildWorkingAreaMenuSingle
  ( 
   JPopupMenu topmenu,
   JMenu menu,
   ActionListener listener, 
   TreeMap<String,TreeSet<String>> views, 
   ArrayList<TreeSet<String>> authors
  ) 
  {  
    if(views.isEmpty()) {
      menu.setEnabled(false);
      return;
    }

    menu.removeAll();
      
    if(!authors.isEmpty()) {
      for(TreeSet<String> agroup : authors) {
	JMenu gsub = new JMenu(agroup.first().substring(0, 3).toUpperCase() + "-" + 
			       agroup.last().substring(0, 3).toUpperCase());      
	menu.add(gsub);
	
	for(String author : agroup) 
	  rebuildWorkingAreaMenuHelper(topmenu, gsub, author, listener, views);
      }
    }
    else {
      for(String author : views.keySet()) 
	rebuildWorkingAreaMenuHelper(topmenu, menu, author, listener, views);
    }
    
    menu.setEnabled(true);
  }

  /**
   * Helper method for adding working area submenus.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param pmenu
   *   The parent menu.
   * 
   * @param author
   *   The owner of the working area.
   * 
   * @param listener
   *   The listener for menu selection events.
   * 
   * @param views
   *   The names of the working area views indexed by author name.
   */ 
  private void 
  rebuildWorkingAreaMenuHelper
  (
   JPopupMenu topmenu,
   JMenu pmenu, 
   String author, 
   ActionListener listener,
   TreeMap<String,TreeSet<String>> views
  ) 
  {
    JMenu sub = new JMenu(author); 
    pmenu.add(sub);
	
    for(String view : views.get(author)) {
      JPopupMenuItem item = new JPopupMenuItem(topmenu, author + " | " + view);
      item.setActionCommand("author-view:" + author + ":" + view);
      item.addActionListener(listener);
      sub.add(item);
    }
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    if(pRootPanel != null) {
      JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);
      if(mpanel != null) 
	mpanel.clearPluginCache();
    }
    
    for(JPanelFrame frame : pPanelFrames) {
      JManagerPanel mpanel = frame.getManagerPanel();
      if(mpanel != null) 
	mpanel.clearPluginCache();
    }

    synchronized(pEditorPlugins) {
      pEditorPlugins.clear();
    }
    synchronized(pComparatorPlugins) {
      pComparatorPlugins.clear();
    }
    synchronized(pActionPlugins) {
      pActionPlugins.clear();
    }
    synchronized(pToolPlugins) {
      pToolPlugins.clear();
    }
    synchronized(pArchiverPlugins) {
      pArchiverPlugins.clear();
    }
    synchronized(pMasterExtPlugins) {
      pMasterExtPlugins.clear();
    }
    synchronized(pQueueExtPlugins) {
      pQueueExtPlugins.clear();
    }

    synchronized(pEditorLayouts) {
      pEditorLayouts.clear();
    }
    synchronized(pComparatorLayouts) {
      pComparatorLayouts.clear();
    }
    synchronized(pActionLayouts) {
      pActionLayouts.clear();
    }
    synchronized(pToolLayouts) {
      pToolLayouts.clear();
    }  
    synchronized(pArchiverLayouts) {
      pArchiverLayouts.clear();
    }    
    synchronized(pMasterExtLayouts) {
      pMasterExtLayouts.clear();
    }    
    synchronized(pQueueExtLayouts) {
      pQueueExtLayouts.clear();
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Rebuild the contents of an editor plugin menu for the given toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildEditorMenu
  (
   int channel, 
   String tname, 
   JMenu menu, 
   ActionListener listener
  ) 
  {
    rebuildEditorMenu(null, channel, tname, menu, listener);
  }

  /**
   * Rebuild the contents of an editor plugin menu for the given toolset.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildEditorMenu
  (
   JPopupMenu topmenu,
   int channel, 
   String tname, 
   JMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);

      synchronized(pEditorPlugins) {
	plugins = pEditorPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetEditorPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getEditors();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.get(vendor).keySet()) {
	      for(VersionID vid : index.get(vendor).get(name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pEditorPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pEditorLayouts) {
	layout = pEditorLayouts.get(tname);
	if(layout == null) {
	  layout = client.getEditorMenuLayout(tname);
	  pEditorLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    menu.removeAll();
    if((layout != null) && !layout.isEmpty()) {
      for(PluginMenuLayout pml : layout) 
	menu.add(rebuildPluginMenuHelper(topmenu, pml, "edit-with", plugins, listener));
    }
    else {
      JPopupMenuItem item = new JPopupMenuItem(topmenu, "(None Specified)");
      item.setEnabled(false);
      menu.add(item);
    }
  }

  /**
   * Rebuild the contents of an comparator plugin menu for the given toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildComparatorMenu
  (
   int channel, 
   String tname, 
   JMenu menu, 
   ActionListener listener
  ) 
  {
    rebuildComparatorMenu(null, channel, tname, menu, listener);
  }

  /**
   * Rebuild the contents of an comparator plugin menu for the given toolset.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildComparatorMenu
  (
   JPopupMenu topmenu,
   int channel, 
   String tname, 
   JMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);

      synchronized(pComparatorPlugins) {
	plugins = pComparatorPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	     client.getToolsetComparatorPlugins(tname);
	  
	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getComparators();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.get(vendor).keySet()) {
	      for(VersionID vid : index.get(vendor).get(name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pComparatorPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pComparatorLayouts) {
	layout = pComparatorLayouts.get(tname);
	if(layout == null) {
	  layout = client.getComparatorMenuLayout(tname);
	  pComparatorLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    menu.removeAll();
    if((layout != null) && !layout.isEmpty()) {
      for(PluginMenuLayout pml : layout) 
	menu.add(rebuildPluginMenuHelper(topmenu, pml, "compare-with", plugins, listener));
    }
    else {  
      JPopupMenuItem item = new JPopupMenuItem(topmenu, "(None Specified)");
      item.setEnabled(false);
      menu.add(item);
    }
  }
  
  /**
   * Rebuild the contents of an tool plugin menu for the given toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildToolMenu
  (
   int channel, 
   String tname, 
   JPopupMenu menu, 
   ActionListener listener
  ) 
  {
    rebuildToolMenu(null, channel, tname, menu, listener);
  }

  /**
   * Rebuild the contents of an tool plugin menu for the given toolset.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildToolMenu
  (
   JPopupMenu topmenu,
   int channel, 
   String tname, 
   JPopupMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);

      synchronized(pToolPlugins) {
	plugins = pToolPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetToolPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getTools();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.get(vendor).keySet()) {
	      for(VersionID vid : index.get(vendor).get(name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pToolPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pToolLayouts) {
	layout = pToolLayouts.get(tname);
	if(layout == null) {
	  layout = client.getToolMenuLayout(tname);
	  pToolLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    menu.removeAll();
    if((layout != null) && !layout.isEmpty()) {
      for(PluginMenuLayout pml : layout) 
	menu.add(rebuildPluginMenuHelper(topmenu, pml, "run-tool", plugins, listener));
    }
    else {
      JPopupMenuItem item = new JPopupMenuItem(topmenu, "(None Specified)");
      item.setEnabled(false);
      menu.add(item);
    }
  }

  /**
   * Rebuild the contents of an tool plugin menu for the given toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildDefaultToolMenu
  (
   int channel, 
   JPopupMenu menu, 
   ActionListener listener
  ) 
  {
    rebuildDefaultToolMenu(null, channel, menu, listener);
  }

  /**
   * Rebuild the contents of an tool plugin menu for the given toolset.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param menu
   *   The menu to be rebuilt.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  public void
  rebuildDefaultToolMenu
  (
   JPopupMenu topmenu,
   int channel, 
   JPopupMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);
      String tname = client.getDefaultToolsetName();

      synchronized(pToolPlugins) {
	plugins = pToolPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetToolPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getTools();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.get(vendor).keySet()) {
	      for(VersionID vid : index.get(vendor).get(name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }
	  
	  pToolPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pToolLayouts) {
	layout = pToolLayouts.get(tname);
	if(layout == null) {
	  layout = client.getToolMenuLayout(tname);
	  pToolLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    menu.removeAll();
    if((layout != null) && !layout.isEmpty()) {
      for(PluginMenuLayout pml : layout) 
	menu.add(rebuildPluginMenuHelper(topmenu, pml, "run-tool", plugins, listener));
    }
    else {
      JPopupMenuItem item = new JPopupMenuItem(topmenu, "(None Specified)");
      item.setEnabled(false);
      menu.add(item);
    }
  }

  /**
   * Recursively build a plugin menu.
   * 
   * @param topmenu
   *   The top-level popup menu containing the items.
   * 
   * @param layout
   *   The current plugin submenu layout.
   * 
   * @param prefix
   *   The action command prefix.
   *
   * @param plugins
   *   The plugins supported by the current toolset.
   * 
   * @param listener
   *   The listener for menu selection events.
   */ 
  private JMenuItem
  rebuildPluginMenuHelper
  (
   JPopupMenu topmenu,
   PluginMenuLayout layout, 
   String prefix, 
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins, 
   ActionListener listener
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      item = new JPopupMenuItem(topmenu, layout.getTitle());
      item.setActionCommand(prefix + ":" + layout.getName() + ":" + 
			    layout.getVersionID() + ":" + layout.getVendor());
      item.addActionListener(listener);
   
      boolean enabled = false;
      Set<VersionID> vids = plugins.keySet(layout.getVendor(), layout.getName());
      if((vids != null) && vids.contains(layout.getVersionID())) {
	TreeSet<OsType> supported = 
	  plugins.get(layout.getVendor(), layout.getName(), layout.getVersionID());
	enabled = ((supported != null) && supported.contains(PackageInfo.sOsType));
      }
      item.setEnabled(enabled); 
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(rebuildPluginMenuHelper(topmenu, pml, prefix, plugins, listener));
      item = sub;
    }

    return item;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new editor plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createEditorSelectionField
  (
   int channel,
   int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);
      String tname = client.getDefaultToolsetName();

      synchronized(pEditorPlugins) {
	plugins = pEditorPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetEditorPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getEditors();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pEditorPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pEditorLayouts) {
	layout = pEditorLayouts.get(tname);
	if(layout == null) {
	  layout = client.getEditorMenuLayout(tname);
	  pEditorLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an editor plugin field for the given toolset.
   */ 
  public void 
  updateEditorPluginField
  (
   int channel,
   String tname, 
   JPluginSelectionField field
  ) 
  {
    if(tname == null) 
      return;

    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);

      synchronized(pEditorPlugins) {
	plugins = pEditorPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetEditorPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getEditors();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pEditorPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pEditorLayouts) {
	layout = pEditorLayouts.get(tname);
	if(layout == null) {
	  layout = client.getEditorMenuLayout(tname);
	  pEditorLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new action plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createActionSelectionField
  (
   int channel,
   int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);
      String tname = client.getDefaultToolsetName();

      synchronized(pActionPlugins) {
	plugins = pActionPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetActionPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getActions();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pActionPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pActionLayouts) {
	layout = pActionLayouts.get(tname);
	if(layout == null) {
	  layout = client.getActionMenuLayout(tname);
	  pActionLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an action plugin field for the given toolset.
   */ 
  public void 
  updateActionPluginField
  (
   int channel,
   String tname, 
   JPluginSelectionField field
  ) 
  {
    if(tname == null) 
      return;

    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient(channel);

      synchronized(pActionPlugins) {
	plugins = pActionPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetActionPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getActions();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pActionPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pActionLayouts) {
	layout = pActionLayouts.get(tname);
	if(layout == null) {
	  layout = client.getActionMenuLayout(tname);
	  pActionLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new archiver plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createArchiverSelectionField
  (
   int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();
      String tname = client.getDefaultToolsetName();

      synchronized(pArchiverPlugins) {
	plugins = pArchiverPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetArchiverPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getArchivers();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pArchiverPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pArchiverLayouts) {
	layout = pArchiverLayouts.get(tname);
	if(layout == null) {
	  layout = client.getArchiverMenuLayout(tname);
	  pArchiverLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an archiver plugin field for the given toolset.
   */ 
  public void 
  updateArchiverPluginField
  (
   String tname, 
   JPluginSelectionField field
  ) 
  {
    if(tname == null) 
      return;

    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();

      synchronized(pArchiverPlugins) {
	plugins = pArchiverPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetArchiverPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getArchivers();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pArchiverPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pArchiverLayouts) {
	layout = pArchiverLayouts.get(tname);
	if(layout == null) {
	  layout = client.getArchiverMenuLayout(tname);
	  pArchiverLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new master extension plugin selection field based on the default toolset.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createMasterExtSelectionField
  (
   int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();
      String tname = client.getDefaultToolsetName();

      synchronized(pMasterExtPlugins) {
	plugins = pMasterExtPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetMasterExtPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getMasterExts();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pMasterExtPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pMasterExtLayouts) {
	layout = pMasterExtLayouts.get(tname);
	if(layout == null) {
	  layout = client.getMasterExtMenuLayout(tname);
	  pMasterExtLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an master extension plugin field for the given toolset.
   */ 
  public void 
  updateMasterExtPluginField
  (
   String tname, 
   JPluginSelectionField field
  ) 
  {
    if(tname == null) 
      return;

    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();

      synchronized(pMasterExtPlugins) {
	plugins = pMasterExtPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetMasterExtPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getMasterExts();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pMasterExtPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pMasterExtLayouts) {
	layout = pMasterExtLayouts.get(tname);
	if(layout == null) {
	  layout = client.getMasterExtMenuLayout(tname);
	  pMasterExtLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new queue extension plugin selection field based on the default toolset.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createQueueExtSelectionField
  (
   int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();
      String tname = client.getDefaultToolsetName();

      synchronized(pQueueExtPlugins) {
	plugins = pQueueExtPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetQueueExtPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getQueueExts();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pQueueExtPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pQueueExtLayouts) {
	layout = pQueueExtLayouts.get(tname);
	if(layout == null) {
	  layout = client.getQueueExtMenuLayout(tname);
	  pQueueExtLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an queue extension plugin field for the given toolset.
   */ 
  public void 
  updateQueueExtPluginField
  (
   String tname, 
   JPluginSelectionField field
  ) 
  {
    if(tname == null) 
      return;

    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    try {
      MasterMgrClient client = getMasterMgrClient();

      synchronized(pQueueExtPlugins) {
	plugins = pQueueExtPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetQueueExtPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getQueueExts();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pQueueExtPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pQueueExtLayouts) {
	layout = pQueueExtLayouts.get(tname);
	if(layout == null) {
	  layout = client.getQueueExtMenuLayout(tname);
	  pQueueExtLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P A N E L   G R O U P S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the node browsers panel group.
   */ 
  public PanelGroup<JNodeBrowserPanel>
  getNodeBrowserPanels() 
  {
    return pNodeBrowserPanels;
  }

  /**
   * Get the node viewers panel group.
   */ 
  public PanelGroup<JNodeViewerPanel>
  getNodeViewerPanels() 
  {
    return pNodeViewerPanels;
  }

  /**
   * Get the node details panel group.
   */ 
  public PanelGroup<JNodeDetailsPanel>
  getNodeDetailsPanels() 
  {
    return pNodeDetailsPanels;
  }

  /**
   * Get the node history panel group.
   */ 
  public PanelGroup<JNodeHistoryPanel>
  getNodeHistoryPanels() 
  {
    return pNodeHistoryPanels;
  }

  /**
   * Get the node files panel group.
   */ 
  public PanelGroup<JNodeFilesPanel>
  getNodeFilesPanels() 
  {
    return pNodeFilesPanels;
  }

  /**
   * Get the node links panel group.
   */ 
  public PanelGroup<JNodeLinksPanel>
  getNodeLinksPanels() 
  {
    return pNodeLinksPanels;
  }


  /**
   * Get the job servers panel group.
   */ 
  public PanelGroup<JQueueJobServersPanel>
  getQueueJobServersPanels() 
  {
    return pQueueJobServersPanels;
  }

  /**
   * Get the job server stats panel group.
   */ 
  public PanelGroup<JQueueJobServerStatsPanel>
  getQueueJobServerStatsPanels() 
  {
    return pQueueJobServerStatsPanels;
  }

  /**
   * Get the job slots panel group.
   */ 
  public PanelGroup<JQueueJobSlotsPanel>
  getQueueJobSlotsPanels() 
  {
    return pQueueJobSlotsPanels;
  }

  /**
   * Get the job broswer panel group.
   */ 
  public PanelGroup<JQueueJobBrowserPanel>
  getQueueJobBrowserPanels() 
  {
    return pQueueJobBrowserPanels;
  }

  /**
   * Get the job viewer panel group.
   */ 
  public PanelGroup<JQueueJobViewerPanel>
  getQueueJobViewerPanels() 
  {
    return pQueueJobViewerPanels;
  }

  /**
   * Get the job details panel group.
   */ 
  public PanelGroup<JQueueJobDetailsPanel>
  getQueueJobDetailsPanels() 
  {
    return pQueueJobDetailsPanels;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D I A L O G S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Show the save layouts dialog.
   */ 
  public void 
  showSaveLayoutDialog()
  {
    SwingUtilities.invokeLater(new ShowSaveLayoutDialogTask());
  }
  
  /**
   * Show the save layouts dialog.
   */ 
  public void 
  showManageLayoutsDialog()
  {
    SwingUtilities.invokeLater(new ShowManageLayoutsDialogTask());
  }

  /**
   * Show an error message dialog for the given exception.
   */ 
  public void 
  showErrorDialog
  (
   Exception ex
  ) 
  {
    pErrorDialog.setMessage(ex);
    SwingUtilities.invokeLater(new ShowErrorDialogTask());
  }

  /**
   * Show an error message dialog with the given title and message.
   */ 
  public void 
  showErrorDialog
  (
   String title, 
   String msg
  ) 
  {
    pErrorDialog.setMessage(title, msg);
    SwingUtilities.invokeLater(new ShowErrorDialogTask());
  }


  /**
   * Show the user preferences dialog.
   */ 
  public void 
  showUserPrefsDialog()
  {
    pUserPrefsDialog.setVisible(true);
  }

  /**
   * Show the information dialog.
   */ 
  public void 
  showAboutDialog()
  {
    pAboutDialog.setVisible(true);
  }

  /**
   * Show the customer configuration profile dialog.
   */ 
  public void 
  showConfigDialog()
  {
    pConfigDialog.setVisible(true);
  }

  /**
   * Show the manage privileges dialog.
   */ 
  public void 
  showManagePrivilegesDialog()
  {
    pManagePrivilegesDialog.updateAll();
    pManagePrivilegesDialog.setVisible(true);
  }

  /**
   * Show the manage toolsets dialog.
   */ 
  public void 
  showManageToolsetsDialog()
  {
    pManageToolsetsDialog.updateAll();
    pManageToolsetsDialog.setVisible(true);
  }

  /**
   * If unfrozen Toolsets/Packages exist, 
   *   show a confirmation dialog for discarding the unsaved changes.
   */ 
  public boolean
  discardWorkingToolsets() 
  {
    String msg = pManageToolsetsDialog.getUnfrozenWarning();
    if(msg != null) {
      JConfirmDialog diag = 
	new JConfirmDialog(pFrame, "Discard Working Toolsets/Packages?", 
			   "The following unsaved changes to Toolsets were found.\n\n" + msg);
      diag.setVisible(true);
      return diag.wasConfirmed();
    }

    return true;
  }

  /**
   * Show the manage license keys dialog.
   */ 
  public void 
  showManageLicenseKeysDialog()
  {
    pManageLicenseKeysDialog.updateLicenseKeys();
    pManageLicenseKeysDialog.setVisible(true);
  }

  /**
   * Show the manage selection keys, groups and schedules dialog.
   */ 
  public void 
  showManageSelectionKeysDialog()
  {
    pManageSelectionKeysDialog.updateAll();
    pManageSelectionKeysDialog.setVisible(true);
  }

  /**
   * Show the manage editors dialog.
   */ 
  public void 
  showDefaultEditorsDialog()
  {
    pDefaultEditorsDialog.updateEditors();
    pDefaultEditorsDialog.setVisible(true);
  }

  /**
   * Show the job submission dialog.
   */ 
  public JQueueJobsDialog
  showQueueJobsDialog()
  {
    pQueueJobsDialog.updateKeys();
    pQueueJobsDialog.setVisible(true);
    return pQueueJobsDialog;
  }

  /**
   * Show an dialog giving details of the failure of the given subprocess.
   */ 
  public void 
  showSubprocessFailureDialog
  (
   String header, 
   SubProcessLight proc
  )
  {
    showSubprocessFailureDialog
      (header, proc.getExitCode(), proc.getCommand(), proc.getStdOut(), proc.getStdErr());
  }

  /**
   * Show an dialog giving details of the failure of the given subprocess.
   */ 
  public void 
  showSubprocessFailureDialog
  (
   String header, 
   int exitCode, 
   String command, 
   String stdout, 
   String stderr 
  )
  {
    ShowSubprocessFailureDialog task = 
      new ShowSubprocessFailureDialog(header, exitCode, command, stdout, stderr);
    SwingUtilities.invokeLater(task);
  }

  /**
   * Show the manage server extensions dialog.
   */ 
  public void 
  showManageServerExtensionsDialog()
  {
    pManageServerExtensionsDialog.updateAll(); 
    pManageServerExtensionsDialog.setVisible(true);
  }

  /**
   * Show a dialog for selecting the name of the database backup file.
   */ 
  public void 
  showBackupDialog()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss");
    pBackupDialog.updateTargetName("pipeline-db." + format.format(new Date()) + ".tgz");

    pBackupDialog.setVisible(true);
    if(pBackupDialog.wasConfirmed()) {
      File file = pBackupDialog.getSelectedFile();
      if(file != null) {
	BackupTask task = new BackupTask(file);
	task.start();	
      }
    }
  }

  /**
   * Show a dialog for archiving files associated with checked-in versions.
   */ 
  public void 
  showArchiveDialog()
  {
    pArchiveDialog.setVisible(true);
  }

  /**
   * Show a dialog for offlining files associated with checked-in versions.
   */ 
  public void 
  showOfflineDialog()
  {
    pOfflineDialog.setVisible(true);
  }

  /**
   * Show a dialog for restoring files associated with checked-in versions.
   */ 
  public void 
  showRestoreDialog()
  {
    pRestoreDialog.setVisible(true);
  } 

  /**
   * Show a dialog for browsing the contents of existing archive volumnes.
   */ 
  public void 
  showArchiveVolumesDialog()
  {
    pArchiveVolumesDialog.setVisible(true);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Show the log history dialog. 
   */ 
  public void 
  showLogsDialog()
  {
    pLogsDialog.setVisible(true);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Show a dialog which graphs the resource usage history of queue servers.
   */ 
  public void 
  showResourceUsageHistoryDialog
  (
   int channel,
   TreeSet<String> hosts
  )
  {
    pResourceUsageHistoryDialog.updateSamples(channel, hosts);
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Try to aquire a panel operation lock. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
  public boolean
  beginPanelOp()
  {
    return beginPanelOpHelper(0, "", false);
  }

  /**
   * Try to aquire a panel operation lock, but generate no progress messages. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
   public boolean
   beginSilentPanelOp
   (
    int channel
   ) 
   {
     return beginPanelOpHelper(channel, "", true);
   }

  /**
   * Try to aquire a panel operation lock. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
   public boolean
   beginPanelOp
   (
    int channel
   ) 
   {
     return beginPanelOpHelper(channel, "", false);
   }

  /**
   * Try to aquire a panel operation lock and if successfull notify the user that 
   * an operation is in progress. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @param msg
   *   A short message describing the operation.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
  public boolean
  beginPanelOp
  (
   String msg
  )
  {
    return beginPanelOpHelper(0, msg, false);
  }
  
  /**
   * Try to aquire a panel operation lock and if successfull notify the user that 
   * an operation is in progress. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param msg
   *   A short message describing the operation.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
  public boolean
  beginPanelOp
  (
   int channel,
   String msg
  )
  {
    return beginPanelOpHelper(channel, msg, false);
  }

  /**
   * Try to aquire a panel operation lock and if successfull notify the user that 
   * an operation is in progress. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was aquired and the operation 
   * can proceed.  Otherwise, the caller should abort the operation immediately. <P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp endPanelOp} to release the lock.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param msg
   *   A short message describing the operation.
   * 
   * @param silent
   *   Whether the operation should be performed without progress messages.
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
  private boolean
  beginPanelOpHelper
  (
   int channel,
   String msg, 
   boolean silent
  )
  {
    assert((channel >= 0) && (channel < 10)); 
    boolean aquired = pOpsLocks[channel].tryLock();

    if(!silent) {
      pOpsRunning[channel].set(true);
      pOpsTimers[channel] = new TaskTimer();
    }

    if(aquired) {
      if(!silent)
	SwingUtilities.invokeLater(new BeginOpsTask(channel, msg));
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }

    return aquired;
  }

 
  /**
   * Update the operation message in mid-operation.
   * 
   * @param msg
   *   A short message describing the operation.
   */ 
  public void
  updatePanelOp
  (
   String msg
  )
  {
    updatePanelOp(0, msg);
  }

  /**
   * Update the operation message in mid-operation.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param msg
   *   A short message describing the operation.
   */ 
  public void
  updatePanelOp
  (
   int channel,
   String msg
  )
  {
    assert((channel >= 0) && (channel < 10)); 
    assert(pOpsLocks[channel].isLocked());
    SwingUtilities.invokeLater(new UpdateOpsTask(channel, msg));
  }


  /**
   * Release the panel operation lock. <P>
   * 
   * @param msg
   *   A short completion message.
   */
  public void 
  endPanelOp
  (
   String msg   
  ) 
  {
    endPanelOpHelper(0, msg, false);
  }

  /**
   * Release the panel operation lock. <P>
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public void 
  endPanelOp
  ( 
   int channel
  ) 
  {
    endPanelOpHelper(channel, "", false);
  }

  /**
   * Release the panel operation lock, but generate no progress messages. <P>
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public void 
  endSilentPanelOp
  ( 
   int channel
  ) 
  {
    endPanelOpHelper(channel, "", true);
  }

  /**
   * Release the panel operation lock and notify the user that the operation has 
   * completed. <P>
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param msg
   *   A short completion message.
   */ 
  public void 
  endPanelOp
  (
   int channel,
   String msg
  )
  {
    endPanelOpHelper(channel, msg, false);
  }

  /**
   * Release the panel operation lock and notify the user that the operation has 
   * completed. <P>
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param msg
   *   A short completion message.
   */ 
  private void 
  endPanelOpHelper
  (
   int channel,
   String msg, 
   boolean silent
  )
  {
    assert((channel >= 0) && (channel < 10)); 
    try {
      String timedMsg = msg;
      if(!silent) {
	TaskTimer timer = pOpsTimers[channel]; 
	if(timer != null) {
	  timer.suspend();
	  timedMsg = 
            (msg + "   (" + TimeStamps.formatInterval(timer.getActiveDuration()) + ")");
	}

	pOpsRunning[channel].set(false);
      }

      pOpsLocks[channel].unlock();  

      if(!silent) 
	SwingUtilities.invokeLater(new EndOpsTask(channel, timedMsg)); 
    }
    catch(IllegalMonitorStateException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 "Internal Error:\n" + 
	 "  " + ex.getMessage());
    }
  }


  /**
   * Show only the panel operation progress components associated with used channel groups.
   */ 
  public void 
  updateOpsBar() 
  {
    if(pProgressBoxes == null) 
      return;

    int idx;
    for(idx=1; idx<pProgressBoxes.length; idx++) {
      boolean unused = 
 	(pNodeBrowserPanels.isGroupUnused(idx) && 
 	 pNodeViewerPanels.isGroupUnused(idx) && 
 	 pNodeDetailsPanels.isGroupUnused(idx) && 
 	 pNodeHistoryPanels.isGroupUnused(idx) && 
 	 pNodeFilesPanels.isGroupUnused(idx) && 
 	 pNodeLinksPanels.isGroupUnused(idx) && 
  	 pQueueJobServersPanels.isGroupUnused(idx) && 
  	 pQueueJobServerStatsPanels.isGroupUnused(idx) && 
  	 pQueueJobSlotsPanels.isGroupUnused(idx) && 
 	 pQueueJobBrowserPanels.isGroupUnused(idx) && 
 	 pQueueJobViewerPanels.isGroupUnused(idx) && 
 	 pQueueJobDetailsPanels.isGroupUnused(idx));

      if(unused) 
	pProgressBoxes[idx].setVisible(false);
    }

    pProgressPanel.revalidate();
    pProgressPanel.repaint();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively update all child panels to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    SwingUtilities.invokeLater(new UpdateUserPrefsTask()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O G L   U T I L I T I E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to check all OpenGL calls for errors.
   */ 
  public boolean 
  getDebugGL() 
  {
    return pDebugGL;
  }

  /**
   * Whether to print all OpenGL calls to STDOUT.
   */ 
  public boolean 
  getTraceGL() 
  {
    return pTraceGL;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new OpenGL rendering canvas shared among all GLCanvas instances.
   */ 
  public synchronized GLCanvas
  createGLCanvas() 
  {
    return new GLCanvas(pGLCapabilities, null, pGLCanvas.getContext(), null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new OpenGL display list. <P> 
   * 
   * @return 
   *   The OpenGL display list handle.
   * 
   * @throws GLException
   *   If unable to return a valid handle.
   */ 
  public synchronized int
  getDisplayList
  (
   GL gl   
  ) 
    throws GLException 
  {
    for(Integer dl : pDisplayLists) 
      gl.glDeleteLists(dl, 1);
    pDisplayLists.clear();

    int dl = gl.glGenLists(1);
    if(dl == 0) 
      throw new GLException("Unable to allocate any new display lists!");

    return dl;
  }

  /**
   * Add the given display list to the pool of lists to be freed at the next opportunity. <P> 
   * 
   * @param dl 
   *   The display list handle.
   */ 
  public synchronized void 
  freeDisplayList
  (
   int dl
  ) 
  {
    if(dl > 0) 
      pDisplayLists.add(dl);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the Window is set to be the active Window.
   */
  public void 
  windowActivated(WindowEvent e) {} 

  /**
   * Invoked when a window has been closed as the result of calling dispose on the window.
   */ 
  public void 	
  windowClosed(WindowEvent e) {} 

  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  public void 	
  windowClosing
  (
   WindowEvent e
  ) 
  {
    doQuit();
  }

  /**
   * Invoked when a Window is no longer the active Window.
   */ 
  public void 	
  windowDeactivated(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a minimized to a normal state.
   */ 
  public void 	
  windowDeiconified(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a normal to a minimized state.
   */ 
  public void 	
  windowIconified(WindowEvent e) {}

  /**
   * Invoked the first time a window is made visible.	
   */ 
  public void     
  windowOpened(WindowEvent e) {}



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Save the current panel layout.
   */
  public void 
  doSaveLayout()
  {
    if(pLayoutPath != null) 
      saveLayoutHelper();
    else 
      showSaveLayoutDialog();
  }

  /**
   * Save the current panel layout helper.
   */
  private void
  saveLayoutHelper() 
  {
    try {
      Path lpath = new Path(PackageInfo.sHomePath, 
			    PackageInfo.sUser + "/.pipeline/layouts");
      Path path = new Path(lpath, pLayoutPath);
      
      File dir = path.getParentPath().toFile();
      if(!dir.isDirectory()) 
	if(!dir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create parent directory (" + dir + ") to contain saved layout!");
      
      LinkedList<PanelLayout> layouts = new LinkedList<PanelLayout>();
      {
	JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);
	PanelLayout layout = new PanelLayout(mpanel, null, pFrame.getBounds());
	layouts.add(layout);
      }
      
      for(JPanelFrame frame : pPanelFrames) {
	JManagerPanel mpanel = frame.getManagerPanel();
	PanelLayout layout = 
	  new PanelLayout(mpanel, frame.getWindowName(), frame.getBounds());
	layouts.add(layout);
      }

      LockedGlueFile.save(path.toFile(), "PanelLayout", layouts);
    }
    catch(Exception ex) {
      showErrorDialog(ex);
    }
  }

  /**
   * Replace the current panels with those stored in the stored layout with the given name.
   */
  public void 
  doRestoreSavedLayout
  (
   Path path, 
   boolean restoreSelections
  ) 
  {
    pRestoreSelections = restoreSelections;
    SwingUtilities.invokeLater(new RestoreSavedLayoutTask(path));
  }

  /**
   * Make the current layout the default layout.
   */
  public void 
  doDefaultLayout() 
  {
    doDefaultLayout(pLayoutPath);
  }

  /**
   * Make the given layout the default layout.
   */
  public void 
  doDefaultLayout
  (
   Path layoutPath
  )
  {
    if((layoutPath != null) && (layoutPath.equals(pLayoutPath)))
      saveLayoutHelper();

    try {
      Path path = new Path(PackageInfo.sHomePath, 
			   PackageInfo.sUser + "/.pipeline/default-layout");
      File file = path.toFile();
      if(layoutPath != null) 
	LockedGlueFile.save(file, "DefaultLayout", layoutPath.toString());
      else 
	file.delete();

      setDefaultLayoutPath(layoutPath);
    }
    catch(Exception ex) {
      showErrorDialog(ex);
    }    
  }

  /**
   * Reset the current layout to a standardized panel layout.
   */
  public void 
  doResetLayout()
  {
    JConfirmDialog diag = new JConfirmDialog(pFrame, "Reset Current Layout?"); 
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      Path path = pLayoutPath;
      if(path == null) 
	path = new Path("Default"); 

      try {
	getMasterMgrClient().createInitialPanelLayout
	  (path.toString(), PackageInfo.sUser, "default");
      }
      catch(Exception ex) {
	showErrorDialog(ex);
      }    

      doRestoreSavedLayout(path, false);
    }  
  }

  /**
   * Perform any tasks which should occur before exiting.
   */ 
  public void
  doUponExit()
  {
    /* autosave layouts */ 
    {
      UserPrefs prefs = UserPrefs.getInstance();
      String choice = prefs.getAutoSaveLayout();
      if(choice.equals("Save Only") || choice.equals("Save & Make Default")) {
	try {
	  if(pLayoutPath != null) 
	    saveLayoutHelper();
	  else {
	    pSaveLayoutDialog.updateLayouts(pLayoutPath);
	    pSaveLayoutDialog.setVisible(true);
	    if(pSaveLayoutDialog.wasConfirmed()) {
	      Path path = pSaveLayoutDialog.getSelectedPath();
	      if(path != null) {
		setLayoutPath(path);	    
		saveLayoutHelper();
	      }
	    }
	  }

	  if(choice.equals("Save & Make Default"))
	    doDefaultLayout();
	}
	catch(Exception ex) {}
      }
    }

    /* save the collapsed node paths */ 
    synchronized(pCollapsedNodePaths) {
      Path path = new Path(PackageInfo.sHomePath, 
			   PackageInfo.sUser + "/.pipeline/collapsed-nodes");
      File file = path.toFile();
      if(pCollapsedNodePaths.isEmpty()) {
	file.delete();
      }
      else {
	try {
	  LockedGlueFile.save(file, "CollapsedNodePaths", pCollapsedNodePaths); 
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     "Unable to save (" + file + ")!");
	  LogMgr.getInstance().flush();
	}
      }
    }
  }

  /**
   * Close the network connection and exit.
   */ 
  public void 
  doQuit()
  {
    /* last chance to save toolsets/packages */ 
    if(!discardWorkingToolsets()) {
      showManageToolsetsDialog();
      return;
    }

    doUponExit();

    int idx;
    for(idx=0; idx<pMasterMgrClients.length; idx++) {
      if(pMasterMgrClients[idx] != null) 
	pMasterMgrClients[idx].disconnect();
      
      if(pQueueMgrClients[idx] != null) 
	pQueueMgrClients[idx].disconnect();
    }

    PluginMgrClient.getInstance().disconnect();

    /* give the sockets time to disconnect cleanly */ 
    try {
      Thread.sleep(500);
    }
    catch(InterruptedException ex) {
    }

    System.exit(0);
  }  

    

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Initialize the look-and-feel and display the splash screen.
   */ 
  private
  class SplashFrameTask
    extends Thread
  { 
    SplashFrameTask
    (
     UIMaster master
    ) 
    {
      super("UIMaster:SplashFrameTask");
      
      pMaster = master;
    }

    public void 
    run() 
    {  
      try {
	Path base = new Path(PackageInfo.sHomePath, 
			     PackageInfo.sUser + "/.pipeline");

	/* create an intial layout (if none exists) and make it the default */ 
	{
	  Path lpath = new Path(base, "layouts");
	  File dir = lpath.toFile(); 
	  if(!dir.isDirectory()) {
	    if(!dir.mkdirs()) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Severe,
		 "Unable to create (" + dir + ")!");
	      LogMgr.getInstance().flush();
	      System.exit(1);	    
	    }
	    NativeFileSys.chmod(0700, dir);
	    
	    getMasterMgrClient().createInitialPanelLayout
	      ("Default", PackageInfo.sUser, "default");

	    try {
	      Path dpath = new Path(base, "default-layout");
	      LockedGlueFile.save(dpath.toFile(), "DefaultLayout", "/Default");
	    }
	    catch(Exception ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Warning,
		 "Unable to set the initial default layout!");
	      LogMgr.getInstance().flush();
	    }    
	  }	  
	}
	
	/* make sure user preference exist */ 
	{
	  Path path = new Path(base, "preferences");
	  if(path.toFile().isFile()) 	  
	    UserPrefs.load();
	}

	/* read the collapsed node paths */ 
	synchronized(pCollapsedNodePaths) {
	  Path path = new Path(base, "collapsed-nodes");
	  File file = path.toFile();
	  if(file.isFile()) {
	    try {      
	      HashSet<String> collapsed = (HashSet<String>) LockedGlueFile.load(file);
	      if(collapsed != null) 
		pCollapsedNodePaths.addAll(collapsed);
	    }
	    catch(Exception ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Warning,
		 "Unable to load (" + file + ")!");
	      LogMgr.getInstance().flush();
	    }
	  }
	}
      }
      catch(Exception ex) {	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Unable to initialize the user preferences!\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	System.exit(1);	 
      }

      /* make sure that the default working area exists */ 
      try {
	getMasterMgrClient().createWorkingArea(PackageInfo.sUser, "default");
      }
      catch(PipelineException ex) {	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Unable to initialize the default working area!\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	System.exit(1);	 
      }

      /* load the look-and-feel */ 
      {
	try {
	  SynthLookAndFeel synth = new SynthLookAndFeel();
	  synth.load(LookAndFeelLoader.class.getResourceAsStream("synth.xml"),
		     LookAndFeelLoader.class);
	  UIManager.setLookAndFeel(synth);
	}
	catch(java.text.ParseException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     "Unable to parse the look-and-feel XML file (synth.xml):\n" + 
	     "  " + ex.getMessage());
	  System.exit(1);
	}
	catch(UnsupportedLookAndFeelException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     "Unable to load the Pipeline look-and-feel:\n" + 
	     "  " + ex.getMessage());
	  System.exit(1);
	}
      }
      
      /* application wide UI settings */ 
      {
	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
      }
      
      /* show the splash screen and do application startup tasks */ 
      {
	JFrame frame = new JFrame("plui");
	pSplashFrame = frame;

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	frame.setResizable(false);
	frame.setUndecorated(true);
	
	{
	  JPanel panel = new JPanel();
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));   
	
	  /* splash image */ 
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);   

	    hbox.add(Box.createHorizontalGlue());

	    {
	      JLabel label = new JLabel(sSplashIcon);
	      label.setName("Splash");
	    
	      hbox.add(label);
	    }

	    hbox.add(Box.createHorizontalGlue());
	    
	    panel.add(hbox);
	  }

	  /* texture loader bar */ 
	  {
	    pGLCapabilities = new GLCapabilities();  
	    pGLCapabilities.setDoubleBuffered(true);
     	    pGLCanvas = new GLCanvas(pGLCapabilities);
            
	    JTextureLoaderBar loader = 
 	      new JTextureLoaderBar(pGLCanvas, new MainFrameTask(pMaster));

 	    panel.add(loader);
	  }

	  frame.setContentPane(panel);
	}

	frame.pack();

	{
	  Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
	  frame.setLocation(bounds.x + bounds.width/2 - frame.getWidth()/2, 
			    bounds.y + bounds.height/2 - frame.getHeight()/2);
	}

	frame.setVisible(true);
      }

      /* create the restore layout splash screen */ 
      {
	JFrame frame = new JFrame("plui");
	pRestoreSplashFrame = frame;

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	frame.setResizable(false);
	frame.setUndecorated(true);
	
	JLabel label = new JLabel(sRestoreSplashIcon);	  
	frame.setContentPane(label);
	frame.pack();
	
	{
	  Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
	  frame.setLocation(bounds.x + bounds.width/2 - frame.getWidth()/2, 
 			   bounds.y + bounds.height/2 - frame.getHeight()/2);
	}
      }
    }

    private UIMaster  pMaster;
  }

  /** 
   * Initialize the user interface components. 
   */ 
  private
  class MainFrameTask
    extends Thread
  { 
    MainFrameTask
    (
     UIMaster master
    ) 
    { 
      super("UIMaster:MainFrameTask");

      pMaster = master;
    }

    public void 
    run() 
    {
      /* create and show the main application frame */ 
      {
	JFrame frame = new JFrame("plui");
	pFrame = frame;

	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	frame.addWindowListener(pMaster);

	{
	  JPanel root = new JPanel();
	  root.setName("GreyPanel"); 
	  
	  root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));   
	  
	  {
	    JPanel panel = new JPanel(new BorderLayout());
	    pRootPanel = panel;
	    panel.setName("RootPanel");
	    
	    {
	      JManagerPanel mpanel = new JManagerPanel();
	      mpanel.setContents(new JEmptyPanel());
	      
	      panel.add(mpanel);
	    }
	    
	    root.add(panel);
	  }
	  
	  root.add(Box.createRigidArea(new Dimension(0, 2)));
	  
	  {
	    JPanel panel = new JPanel();
	    pProgressPanel = panel;
 
	    panel.setName("GreyPanel"); 
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 19)));

	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);   
	      pNoProgressBox = hbox;
	      
	      hbox.add(Box.createRigidArea(new Dimension(6, 0)));
	      hbox.add(UIFactory.createTextField(null, 30, JLabel.LEFT));
	      hbox.add(Box.createRigidArea(new Dimension(6, 0)));
	      
	      panel.add(hbox);
	    }

	    {
	      pProgressBoxes  = new Box[10];
	      pProgressLights = new JLabel[10];
	      pProgressFields = new JTextField[10];

	      int idx;
	      for(idx=0; idx<pProgressLights.length; idx++) {
		Box hbox = new Box(BoxLayout.X_AXIS);   
		pProgressBoxes[idx] = hbox; 
		hbox.setVisible(false);

		hbox.add(Box.createRigidArea(new Dimension(6, 0)));

		{
		  JLabel label = new JLabel(sProgressFinishedIcons[idx]);
		  pProgressLights[idx] = label;
	      
		  Dimension size = new Dimension(19, 19);
		  label.setMinimumSize(size);
		  label.setMaximumSize(size);
		  label.setPreferredSize(size);
		  
		  hbox.add(label);
		}

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIFactory.createTextField(null, 30, JLabel.LEFT);
		  pProgressFields[idx] = field;
		  
		  hbox.add(field);
		}

		hbox.add(Box.createRigidArea(new Dimension(6, 0)));

		panel.add(hbox);
	      }
	    }
	    
	    root.add(panel);
	  }
	  
	  switch(PackageInfo.sOsType) {
	  case MacOS:
	    {
	      JPanel footer = new JPanel();
	      footer.setName("MacFooterPanel");
	      
	      footer.setMinimumSize(new Dimension(0, 16));
	      footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

	      root.add(footer);
	    }
	    break;

	  default:
	    root.add(Box.createRigidArea(new Dimension(0, 4)));
	  }

	  frame.setContentPane(root);
	}
	
	frame.setSize(520, 360);

	{
	  Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
	  frame.setLocation(bounds.x + bounds.width/2 - frame.getWidth()/2, 
			    bounds.y + bounds.height/2 - frame.getHeight()/2);
	}
      }

      {
	pPanelFrames = new LinkedList<JPanelFrame>();

	pSaveLayoutDialog     = new JSaveLayoutDialog(pFrame);
	pManageLayoutsDialog  = new JManageLayoutsDialog(pFrame);

	pErrorDialog     = new JErrorDialog(pFrame);
	pUserPrefsDialog = new JUserPrefsDialog();
	pAboutDialog     = new JAboutDialog();
	pConfigDialog    = new JConfigDialog();

	pDefaultEditorsDialog = new JDefaultEditorsDialog(); 

	pManagePrivilegesDialog    = new JManagePrivilegesDialog();
	pManageToolsetsDialog      = new JManageToolsetsDialog();
	pManageLicenseKeysDialog   = new JManageLicenseKeysDialog();
	pManageSelectionKeysDialog = new JManageSelectionKeysDialog();

	pQueueJobsDialog = new JQueueJobsDialog(pFrame);
	
	pSubProcessFailureDialog = new JSubProcessFailureDialog(pFrame);

	pManageServerExtensionsDialog = new JManageServerExtensionsDialog();

	pBackupDialog = 
	  new JFileSelectDialog(pFrame, "Backup Database", "Backup Database File:",
				"Backup As:", 64, "Backup"); 
	pBackupDialog.updateTargetFile(PackageInfo.sTempPath.toFile());

	pArchiveDialog        = new JArchiveDialog();
	pOfflineDialog        = new JOfflineDialog();
	pRestoreDialog        = new JRestoreDialog();
	pArchiveVolumesDialog = new JArchiveVolumesDialog();

	{
	  ArrayList<LogMgr.Kind> kinds = new ArrayList<LogMgr.Kind>();
	  kinds.add(LogMgr.Kind.Net);
	  kinds.add(LogMgr.Kind.Sub);

	  pLogsDialog = new JLogsDialog(kinds);
	}

	pResourceUsageHistoryDialog = new JResourceUsageHistoryDialog();

	JToolDialog.initRootFrame(pFrame);
      }

      pSplashFrame.setVisible(false);

      {
	Path layoutPath = null;
	if(pRestoreLayout) {
	  layoutPath = pOverrideLayoutPath;
	  if(layoutPath == null) {
	    try {
	      Path path = new Path(PackageInfo.sHomePath, 
				   PackageInfo.sUser + "/.pipeline/default-layout"); 
	      File file = path.toFile();
	      if(file.isFile()) {
		String lname = (String) LockedGlueFile.load(file);
		if(lname != null) 
		  layoutPath = new Path(lname);
	      }
	    }
	    catch(Exception ex) {
	      showErrorDialog(ex);
	    }   
	  }
	}
	  
	if(layoutPath != null) {
	  setDefaultLayoutPath(layoutPath);
	  SwingUtilities.invokeLater(new RestoreSavedLayoutTask(layoutPath));
	}
	else {
	  pFrame.setVisible(true);
	}
      }
    }

    private UIMaster  pMaster;
  }
  
  /**
   * Notify the user that a panel operation is underway.
   */ 
  private
  class BeginOpsTask
    extends Thread
  { 
    BeginOpsTask
    ( 
     int channel, 
     String msg
    ) 
    {
      super("UIMaster:BeginOpsTask");

      pIdx = channel;
      pMsg = msg;
    }

    public void 
    run() 
    { 
      pProgressLights[pIdx].setIcon(sProgressRunningIcons[pIdx]);
      pProgressFields[pIdx].setText(pMsg);
      
      if(pNoProgressBox.isVisible() || !pProgressBoxes[pIdx].isVisible()) {
	pNoProgressBox.setVisible(false);
	pProgressBoxes[pIdx].setVisible(true);
	pProgressPanel.revalidate();
	pProgressPanel.repaint();
      }
    }

    private int     pIdx; 
    private String  pMsg;
  }

  /* 
   * Update the operation message.
   */ 
  private
  class UpdateOpsTask
    extends Thread
  { 
    UpdateOpsTask
    ( 
     int channel, 
     String msg
    ) 
    {
      super("UIMaster:UpdateOpsTask");

      pIdx = channel;
      pMsg = msg;
    }

    public void 
    run() 
    {
      pProgressFields[pIdx].setText(pMsg);
    }

    private int     pIdx; 
    private String  pMsg;
  }

  /**
   * Notify the user that a panel operation is finished.
   */ 
  private
  class EndOpsTask
    extends Thread
  { 
    EndOpsTask
    ( 
     int channel, 
     String msg
    ) 
    {
      super("UIMaster:EndOpsTask");

      pIdx = channel;
      pMsg = msg;
    }

    public void 
    run() 
    {
      pProgressFields[pIdx].setText(pMsg);
      pProgressLights[pIdx].setIcon(sProgressFinishedIcons[pIdx]); 
      
      if(!pOpsRunning[pIdx].get()) {
	WaitHideOpsTask task = new WaitHideOpsTask(pIdx); 
	task.start();
      }
    }

    private int     pIdx; 
    private String  pMsg;
  }

  /**
   * Wait a few seconds before hiding the progress box.
   */ 
  private
  class WaitHideOpsTask
    extends Thread
  { 
    WaitHideOpsTask
    ( 
     int channel
    ) 
    {
      super("UIMaster:WaitHideOpsTask");

      pIdx = channel;
    }

    public void 
    run() 
    {
      try {
	sleep(5000);
      }
      catch(InterruptedException ex) {
      }
      
      SwingUtilities.invokeLater(new HideOpsTask(pIdx)); 
    }

    private int pIdx; 
  }
  
  /**
   * Hide the progress box if no operations are currently running.
   */ 
  private
  class HideOpsTask
    extends Thread
  { 
    HideOpsTask
    ( 
     int channel
    ) 
    {
      super("UIMaster:HideOpsTask");

      pIdx = channel;
    }

    public void 
    run() 
    {
      if(!pOpsRunning[pIdx].get() && pProgressBoxes[pIdx].isVisible()) {
	pProgressBoxes[pIdx].setVisible(false);
	
	boolean anyVisible = false;
	int wk; 
	for(wk=0; wk< pProgressBoxes.length; wk++) {
	  if(pProgressBoxes[wk].isVisible()) {
	    anyVisible = true;
	    break;
	  }
	}
	
	if(!anyVisible) 
	  pNoProgressBox.setVisible(true);
	
	pProgressPanel.revalidate();
	pProgressPanel.repaint();
      }
    }

    private int pIdx; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the save layout dialog.
   */ 
  private
  class ShowSaveLayoutDialogTask
    extends Thread
  { 
    public 
    ShowSaveLayoutDialogTask() 
    {
      super("UIMaster:ShowSaveLayoutDialogTask");
    }

    public void 
    run() 
    {
      try {
	pSaveLayoutDialog.updateLayouts(pLayoutPath);
	pSaveLayoutDialog.setVisible(true);
	if(pSaveLayoutDialog.wasConfirmed()) {
	  Path path = pSaveLayoutDialog.getSelectedPath();
	  if(path != null) {
	    setLayoutPath(path);	    
	    saveLayoutHelper();
	  }
	}
      }  
      catch(Exception ex) {
	showErrorDialog(ex);
      }
    }
  }

  /**
   * Save the current panel layout.
   */
  private 
  class SaveLayoutTask
    extends Thread
  {
    public 
    SaveLayoutTask() 
    {
      super("UIMaster:SaveLayoutTask");
    }

    public void 
    run() 
    {
      try {
	Path lpath = new Path(PackageInfo.sHomePath, 
			      PackageInfo.sUser + "/.pipeline/layouts");
	Path path = new Path(lpath, pLayoutPath);
	File dir = path.getParentPath().toFile();
	if(!dir.isDirectory()) 
	  dir.mkdirs();

	LinkedList<PanelLayout> layouts = new LinkedList<PanelLayout>();
	{
	  JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);
	  PanelLayout layout = new PanelLayout(mpanel, null, pFrame.getBounds());
	  layouts.add(layout);
	}
	  
	for(JPanelFrame frame : pPanelFrames) {
	  JManagerPanel mpanel = frame.getManagerPanel();
	  PanelLayout layout = 
	    new PanelLayout(mpanel, frame.getWindowName(), frame.getBounds());
	  layouts.add(layout);
	}

	LockedGlueFile.save(path.toFile(), "PanelLayout", layouts);
      }
      catch(Exception ex) {
	showErrorDialog(ex);
      }
    }
  }

  /**
   * Replace the current panels with those stored in the stored layout with the given name.
   */
  private 
  class RestoreSavedLayoutTask
    extends Thread
  {
    public 
    RestoreSavedLayoutTask
    (
     Path path
    ) 
    {
      super("UIMaster:RestoreSavedLayoutTask");

      pPath = path;
    }

    public void 
    run() 
    {
      /* clean up existing panels */ 
      {
	{
	  pFrame.setVisible(false);

	  JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);
	  if(mpanel != null) 
	    mpanel.releasePanelGroups();

	  pRootPanel.removeAll();
	}
	
	for(JPanelFrame frame : pPanelFrames) {
	  frame.removePanels();
	  frame.setVisible(false);
	}
	pPanelFrames.clear();
	
	pNodeBrowserPanels.clear();
	pNodeViewerPanels.clear();
	pNodeDetailsPanels.clear();
	pNodeHistoryPanels.clear();
	pNodeFilesPanels.clear();
	pNodeLinksPanels.clear();
	
 	pQueueJobServersPanels.clear();
 	pQueueJobServerStatsPanels.clear();
 	pQueueJobSlotsPanels.clear();
	pQueueJobBrowserPanels.clear();
	pQueueJobViewerPanels.clear();
	pQueueJobDetailsPanels.clear();
      }
      
      /* show the splash screen */ 
      pRestoreSplashFrame.setVisible(true);
      
      RestoreSavedLayoutRefreshTask task = new RestoreSavedLayoutRefreshTask(pPath);
      task.start();      
    }

    private Path pPath; 
  }

  private 
  class RestoreSavedLayoutRefreshTask
    extends Thread
  {
    public 
    RestoreSavedLayoutRefreshTask
    (
     Path path 
    ) 
    {
      super("UIMaster:RestoreSavedLayoutRefreshTask");
      
      pPath = path; 
    }

    public void 
    run() 
    {
      try {
	sleep(100);
      }
      catch(InterruptedException ex) {
      }

      SwingUtilities.invokeLater(new RestoreSavedLayoutLoaderTask(pPath));
    }

    private Path    pPath;
    private JFrame  pSplash;
  }

  private 
  class RestoreSavedLayoutLoaderTask
    extends Thread
  {
    public 
    RestoreSavedLayoutLoaderTask
    (
     Path path 
    ) 
    {
      super("UIMaster:RestoreSavedLayoutLoaderTask");
      
      pPath = path;
    }

    public void 
    run() 
    {
      /* restore saved panels */
      LinkedList<PanelLayout> layouts = null;
      {
	pIsRestoring.set(true);
	
	Path lpath = new Path(PackageInfo.sHomePath, 
			      PackageInfo.sUser + "/.pipeline/layouts");
	Path path = new Path(lpath, pPath);
	File file = path.toFile();
	try {      
	  if(!file.isFile()) 
	    throw new GlueException();
	  
	  layouts = (LinkedList<PanelLayout>) LockedGlueFile.load(file);
	}
	catch(GlueException ex) {
	  showErrorDialog("Error:", "Unable to load saved layout (" + file + ")!");
	}
	catch(Exception ex) {
	  showErrorDialog(ex);
	}
	
	pIsRestoring.set(false);
      }

      /* restore selections and perform the intial update */ 
      if(pRestoreSelections) {
	TreeSet<Integer> groupIDs = new TreeSet<Integer>();

	for(JNodeViewerPanel panel : pNodeViewerPanels.getPanels()) {
	  Integer gid = panel.getGroupID();
	  if(!groupIDs.contains(gid)) {
	    groupIDs.add(gid);
	    panel.restoreSelections();
	  }
	}

 	for(JQueueJobBrowserPanel panel : pQueueJobBrowserPanels.getPanels()) {
	  Integer gid = panel.getGroupID();
	  if(!groupIDs.contains(gid)) {
	    groupIDs.add(gid);
	    panel.restoreSelections();
	  }
	}
      }
      
      /* set window titles and placement */ 
      ArrayList<JFrame> frames = new ArrayList<JFrame>();
      if((layouts != null) && !layouts.isEmpty()) {
	boolean first = true;
	for(PanelLayout layout : layouts) {
	  JManagerPanel mpanel = layout.getRoot();
	  
	  if(first) {
	    first = false;
	    
	    pFrame.setBounds(layout.getBounds());
	    
	    pRootPanel.add(mpanel);
	    pRootPanel.validate();
	    pRootPanel.repaint();
	    
	    frames.add(pFrame);
	  }
	  else {
	    JPanelFrame frame = new JPanelFrame(); 
	    
	    frame.setBounds(layout.getBounds());
	    frame.setManagerPanel(mpanel);
	    frame.setWindowName(layout.getName());
	    
	    pPanelFrames.add(frame);
	    
	    frames.add(frame);
	  }
	}
	
	setLayoutPath(pPath); 
      }
      else {
	JManagerPanel mpanel = new JManagerPanel();
	mpanel.setContents(new JEmptyPanel()); 
	
	pRootPanel.add(mpanel);
	pRootPanel.validate();
	pRootPanel.repaint();
	
	frames.add(pFrame);
      }
      
      /* hide the splash screen */ 
      pRestoreSplashFrame.setVisible(false);
      
      /* show the restored windows */ 
      for(JFrame frame : frames) 
	frame.setVisible(true);
    }

    private Path pPath;
  }

  /**
   * Show the manage layouts dialog. 
   */ 
  private
  class ShowManageLayoutsDialogTask
    extends Thread
  { 
    public 
    ShowManageLayoutsDialogTask() 
    {
      super("UIMaster:ShowManageLayoutsDialogTask");
    }

    public void 
    run() 
    {
      try {
	pManageLayoutsDialog.updateLayouts(pLayoutPath);
	pManageLayoutsDialog.setVisible(true);
      }
      catch(Exception ex) {
	showErrorDialog(ex);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively update all child panels to reflect new user preferences.
   */ 
  private 
  class UpdateUserPrefsTask
    extends Thread
  {    
    public 
    UpdateUserPrefsTask() 
    {
      super("UIMaster:UpdateUserPrefsTask");
    }

    public void 
    run() 
    {
      try {
	if(pRootPanel != null) {
	  JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);
	  if(mpanel != null) 
	    mpanel.updateUserPrefs();
	}

	for(JPanelFrame frame : pPanelFrames) {
	  JManagerPanel mpanel = frame.getManagerPanel();
	  if(mpanel != null) 
	    mpanel.updateUserPrefs();
	}

	pResourceUsageHistoryDialog.updateUserPrefs();
	pManageSelectionKeysDialog.updateUserPrefs();

	{
	  ToolTipManager mgr = ToolTipManager.sharedInstance();
	  UserPrefs prefs = UserPrefs.getInstance();
	  mgr.setEnabled(prefs.getShowToolTips());
	  mgr.setInitialDelay(prefs.getToolTipDelay());
	  mgr.setDismissDelay(prefs.getToolTipDuration());
	}
      }
      catch(Exception ex) {
	showErrorDialog(ex);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the error dialog. <P> 
   * 
   * The reason for the thread wrapper is to allow the rest of the UI to repaint before
   * showing the dialog.
   */ 
  private
  class ShowErrorDialogTask
    extends Thread
  { 
    public 
    ShowErrorDialogTask() 
    {
      super("UIMaster:ShowErrorDialogTask");
    }

    public void 
    run() 
    {
      pErrorDialog.setVisible(true);
    }
  }
  
  /**
   * Show an dialog giving details of the failure of the given subprocess.
   */ 
  private
  class ShowSubprocessFailureDialog
    extends Thread
  { 
    public 
    ShowSubprocessFailureDialog
    (
     String header, 
     int exitCode, 
     String command, 
     String stdout, 
     String stderr 
    )
    {
      super("UIMaster:ShowSubprocessFailureDialog");

      pHeader   = header;
      pExitCode = exitCode; 
      pCommand  = command; 
      pStdOut   = stdout; 
      pStdErr   = stderr;
    }

    public void 
    run() 
    {
      pSubProcessFailureDialog.updateProc(pHeader + " [code " + pExitCode + "]", 
					  pCommand, pStdOut, pStdErr);
      pSubProcessFailureDialog.setVisible(true);
    }

    private String pHeader;
    private int    pExitCode; 
    private String pCommand; 
    private String pStdOut; 
    private String pStdErr; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The common base class for node tasks.
   */ 
  public
  class BaseNodeTask
    extends Thread
  {
    public 
    BaseNodeTask
    (
     String tname, 
     int channel, 
     String author, 
     String view
    ) 
    {
      super(tname);

      pChannel    = channel; 
      pAuthorName = author;
      pViewName   = view; 
    }

    protected void
    postOp() 
    {}

    protected int     pChannel; 
    protected String  pAuthorName; 
    protected String  pViewName; 
  }

  /** 
   * Edit/View the primary file sequence of the given node version.
   */ 
  public
  class EditTask
    extends BaseNodeTask
  {
    public 
    EditTask
    (
     int channel, 
     NodeCommon com,
     boolean useDefault,
     String author, 
     String view, 
     boolean substitute
    ) 
    {
      super("UIMaster:EditTask", channel, author, view);

      pNodeCommon = com;
      pUseDefault = useDefault;
      pSubstitute = substitute;
    }

    public 
    EditTask
    (
     int channel, 
     NodeCommon com, 
     String ename, 
     VersionID evid, 
     String evendor, 
     String author, 
     String view,
     boolean substitute
    ) 
    {
      super("UIMaster:EditTask", channel, author, view);

      pNodeCommon    = com;
      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor; 
      pSubstitute    = substitute;
    }


    @SuppressWarnings("deprecation")
    public void 
    run() 
    {
      MasterMgrClient client = null;
      SubProcessLight proc = null;
      Long editID = null;
      {
	UIMaster master = UIMaster.getInstance();
        boolean ignoreExitCode = false;
	if(master.beginPanelOp(pChannel, "Launching Node Editor...")) {
	  try {
	    client = master.getMasterMgrClient(pChannel);

	    NodeMod mod = null;
	    if(pNodeCommon instanceof NodeMod) 
	      mod = (NodeMod) pNodeCommon;

	    NodeVersion vsn = null;
	    if(pNodeCommon instanceof NodeVersion) 
	      vsn = (NodeVersion) pNodeCommon;

	    /* create an editor plugin instance */ 
	    BaseEditor editor = null;
	    {
	      if(pEditorName != null) {
		PluginMgrClient pclient = PluginMgrClient.getInstance();
		editor = pclient.newEditor(pEditorName, pEditorVersion, pEditorVendor);
	      }
	      else if (pUseDefault) {
		FilePattern fpat = pNodeCommon.getPrimarySequence().getFilePattern();
		String suffix = fpat.getSuffix();
		if(suffix != null) 
		  editor = client.getEditorForSuffix(suffix);
	      }

	      if(editor == null) 
		editor = pNodeCommon.getEditor();
		
	      if(editor == null) 
		throw new PipelineException
		  ("No Editor plugin was specified for node " + 
                   "(" + pNodeCommon.getName() + ")!");
              
              if(!editor.supports(PackageInfo.sOsType)) 
                throw new PipelineException
                  ("The Editor plugin (" + editor.getName() + " v" + 
                   editor.getVersionID() + ") from the vendor (" + editor.getVendor() + ") " +
                   "does not support the " + PackageInfo.sOsType.toTitle() + " operating " + 
                   "system!");

              ignoreExitCode = editor.ignoreExitCode();
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = pNodeCommon.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + pNodeCommon.getName() + ")!");

	      String view = null;
	      if(mod != null)
		view = pViewName; 

	      /* passes author so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment
		      (pAuthorName, view, tname, PackageInfo.sOsType);
	    }
	    
	    /* get the primary file sequence */ 
	    FileSeq fseq = null;
	    File dir = null; 
	    {
	      Path path = null;
	      if(mod != null) {
		Path wpath = 
		  new Path(PackageInfo.sWorkPath, 
			   pAuthorName + "/" + pViewName + "/" + pNodeCommon.getName());
		path = wpath.getParentPath();
	      }
	      else if(vsn != null) {
		path = new Path(PackageInfo.sRepoPath, 
				vsn.getName() + "/" + vsn.getVersionID());
	      }
	      else {
		assert(false);
	      }
	  
	      fseq = new FileSeq(path.toString(), pNodeCommon.getPrimarySequence());
	      dir = path.toFile();
	    }

	    /* start the editor */ 
	    if(pSubstitute) {
	      PrivilegeDetails details = client.getCachedPrivilegeDetails();
	      if(details.isNodeManaged(pAuthorName)) {
		EditAsTask task = new EditAsTask(editor, pAuthorName, fseq, env, dir);
		task.start();
	      }
	      else {
		throw new PipelineException
		  ("You do not have the necessary privileges to execute an editor as the " + 
		   "(" + pAuthorName + ") user!");
	      }
	    }
	    else {
	      editor.makeWorkingDirs(dir);
	      proc = editor.prep(PackageInfo.sUser, fseq, env, dir);
	      if(proc != null) 
		proc.start();
	      else 
		proc = editor.launch(fseq, env, dir);
	    }

	    /* generate the editing started event */ 
	    if(mod != null) {
	      NodeID nodeID = new NodeID(pAuthorName, pViewName, pNodeCommon.getName());
	      editID = client.editingStarted(nodeID, editor);
	    }
	  }
	  catch(IllegalArgumentException ex) {
	    master.showErrorDialog("Error:", ex.getMessage());	    
	    return;
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp(pChannel, "Done.");
	  }
	}

	/* wait for the editor to exit */ 
	if(proc != null) {
	  try {
	    proc.join();
	    if(!proc.wasSuccessful() && !ignoreExitCode) 
	      master.showSubprocessFailureDialog("Editor Failure:", proc);

	    if((client != null) && (editID != null))
	      client.editingFinished(editID);
	  }
	  catch(Exception ex) {
	    master.showErrorDialog(ex);
	  }
	}
      }
    }
 
    private NodeCommon  pNodeCommon; 
    private boolean     pUseDefault; 
    private String      pEditorName;
    private VersionID   pEditorVersion; 
    private String      pEditorVendor; 
    private boolean     pSubstitute; 
  }

  /** 
   * Launch editor as another user and monitor for errors. 
   */ 
  public
  class EditAsTask
    extends Thread
  {
    public 
    EditAsTask
    (
     BaseEditor editor, 
     String author, 
     FileSeq fseq,      
     Map<String,String> env,      
     File dir        
    )
    {
      super("UIMaster:EditAsTask"); 

      pEditor     = editor; 
      pAuthorName = author; 
      pFileSeq    = fseq; 
      pEnv        = env; 
      pDir        = dir; 
    } 

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      JobMgrPlgControlClient jclient = null; 
      try {
	jclient = new JobMgrPlgControlClient("localhost");
	Object[] results = jclient.editAs(pEditor, pAuthorName, pFileSeq, pEnv, pDir); 
	if((results != null) && !pEditor.ignoreExitCode())
	  master.showSubprocessFailureDialog
	    ("Editor Failure:", (Integer) results[0], 
	     (String) results[1], (String) results[2], (String) results[3]);
      }
      catch(Exception ex) {
	master.showErrorDialog(ex);
      }
      finally {
	if(jclient != null) 
	  jclient.disconnect();
      }
    }

    private BaseEditor          pEditor; 
    private String              pAuthorName; 
    private FileSeq             pFileSeq; 
    private Map<String,String>  pEnv; 
    private File                pDir;            
  }

  /** 
   * Queue jobs to the queue for the given nodes.
   */ 
  public
  class QueueJobsTask
    extends BaseNodeTask
  {
    public 
    QueueJobsTask
    (
     int channel, 
     String name,
     String author, 
     String view, 
     Integer batchSize, 
     Integer priority, 
     Integer interval, 
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", channel, author, view);

      pNames = new TreeSet<String>();
      pNames.add(name);

      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys; 
    }

    public 
    QueueJobsTask
    (
     int channel, 
     TreeSet<String> names,
     String author, 
     String view, 
     Integer batchSize, 
     Integer priority, 
     Integer interval, 
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", channel, author, view);

      pNames = names;

      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pChannel)) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp(pChannel, "Submitting Jobs to the Queue: " + name);
	    MasterMgrClient client = master.getMasterMgrClient(pChannel);
	    client.submitJobs(pAuthorName, pViewName, name, null, 
			      pBatchSize, pPriority, pRampUp, 
			      pSelectionKeys, pLicenseKeys);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pChannel, "Done.");
	}

	postOp();
      }
    }

    private TreeSet<String>  pNames;  
    private Integer          pBatchSize;
    private Integer          pPriority;
    private Integer          pRampUp; 
    private TreeSet<String>  pSelectionKeys;
    private TreeSet<String>  pLicenseKeys;
  }

  /** 
   * Abstract base class for all tasks which modify the state of jobs.
   */ 
  public abstract
  class BaseModifyJobsTask
    extends BaseNodeTask
  {
    public 
    BaseModifyJobsTask
    (
     String title, 
     String msg, 
     int channel, 
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super(title, channel, author, view);
      
      pMsg     = msg;
      pNodeIDs = nodeIDs; 
      pJobIDs  = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pChannel)) {
	try {
	  if((pNodeIDs != null) && !pNodeIDs.isEmpty()) {
            for(NodeID nodeID : pNodeIDs) {
              master.updatePanelOp(pChannel, pMsg + " Jobs for Node: " + nodeID.getName());
              performNodeOp(master.getQueueMgrClient(pChannel), nodeID); 
            }
          }

	  if((pJobIDs != null) && !pJobIDs.isEmpty()) {
            master.updatePanelOp(pChannel, pMsg + " Jobs...");
            performJobOps(master.getQueueMgrClient(pChannel), pJobIDs); 
          }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pChannel, "Done.");
	}

	postOp();
      }
    }

    protected abstract void 
    performNodeOp
    (
     QueueMgrClient qclient, 
     NodeID nodeID
    )
      throws PipelineException;

    protected abstract void 
    performJobOps
    (
     QueueMgrClient qclient, 
     TreeSet<Long> jobIDs
    )
      throws PipelineException;

    private String          pMsg; 
    private TreeSet<NodeID> pNodeIDs;
    private TreeSet<Long>   pJobIDs; 
  }

  /** 
   * Pause the given jobs.
   */ 
  public
  class PauseJobsTask
    extends BaseModifyJobsTask
  {
    public 
    PauseJobsTask
    (
     String panelTitle, 
     int channel, 
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super(panelTitle + ":PauseJobsTask", "Pausing", 
            channel, nodeIDs, jobIDs, author, view);
    }

    protected void 
    performNodeOp
    (
     QueueMgrClient qclient, 
     NodeID nodeID
    )
      throws PipelineException
    {
      qclient.pauseJobs(nodeID); 
    }

    protected void 
    performJobOps
    (
     QueueMgrClient qclient, 
     TreeSet<Long> jobIDs
    )
      throws PipelineException
    {
      qclient.pauseJobs(jobIDs);
    }
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  public
  class ResumeJobsTask
    extends BaseModifyJobsTask
  {
    public 
    ResumeJobsTask
    (
     String panelTitle, 
     int channel, 
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super(panelTitle + ":ResumeJobsTask", "Resuming Paused", 
            channel, nodeIDs, jobIDs, author, view);
    }

    protected void 
    performNodeOp
    (
     QueueMgrClient qclient, 
     NodeID nodeID
    )
      throws PipelineException
    {
      qclient.resumeJobs(nodeID); 
    }

    protected void 
    performJobOps
    (
     QueueMgrClient qclient, 
     TreeSet<Long> jobIDs
    )
      throws PipelineException
    {
      qclient.resumeJobs(jobIDs);
    }
  }

  /** 
   * Preempt the given jobs.
   */ 
  public
  class PreemptJobsTask
    extends BaseModifyJobsTask
  {
    public 
    PreemptJobsTask
    (
     String panelTitle, 
     int channel, 
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super(panelTitle + ":PreemptJobsTask", "Preempting", 
            channel, nodeIDs, jobIDs, author, view);
    }

    protected void 
    performNodeOp
    (
     QueueMgrClient qclient, 
     NodeID nodeID
    )
      throws PipelineException
    {
      qclient.preemptJobs(nodeID); 
    }

    protected void 
    performJobOps
    (
     QueueMgrClient qclient, 
     TreeSet<Long> jobIDs
    )
      throws PipelineException
    {
      qclient.preemptJobs(jobIDs);
    }
  }
 
  /** 
   * Kill the given jobs.
   */ 
  public
  class KillJobsTask
    extends BaseModifyJobsTask
  {
    public 
    KillJobsTask
    (
     String panelTitle, 
     int channel, 
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super(panelTitle + ":KillJobsTask", "Killing", 
            channel, nodeIDs, jobIDs, author, view);
    }

    protected void 
    performNodeOp
    (
     QueueMgrClient qclient, 
     NodeID nodeID
    )
      throws PipelineException
    {
      qclient.killJobs(nodeID); 
    }

    protected void 
    performJobOps
    (
     QueueMgrClient qclient, 
     TreeSet<Long> jobIDs
    )
      throws PipelineException
    {
      qclient.killJobs(jobIDs);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  public 
  class RemoveFilesTask
    extends BaseNodeTask
  {
    public 
    RemoveFilesTask
    (
     int channel, 
     String name,
     String author, 
     String view
    ) 
    {
      super("UIMaster:RemoveFilesTask", channel, author, view);

      pNames = new TreeSet<String>();
      pNames.add(name);
    }

    public 
    RemoveFilesTask
    (
     int channel, 
     TreeSet<String> names,
     String author, 
     String view
    ) 
    {
      super("UIMaster:RemoveFilesTask", channel, author, view);

      pNames = names; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pChannel)) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp(pChannel, "Removing Files: " + name);
	    MasterMgrClient client = master.getMasterMgrClient(pChannel);
	    client.removeFiles(pAuthorName, pViewName, name, null);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pChannel, "Done.");
	}

	postOp();
      }
    }

    private TreeSet<String>  pNames; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Backup the Pipeline database.
   */ 
  public 
  class BackupTask
    extends Thread
  {
    public 
    BackupTask
    (
     File file
    ) 
    {
      super("UIMaster:BackupTask");

      pBackupFile = file;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Database Backup...")) {
	try {
	  master.getMasterMgrClient().backupDatabase(pBackupFile);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
    }

    private File  pBackupFile; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance.
   */ 
  private static UIMaster  sMaster;

  /**
   * Icon images.
   */ 
  private static final Icon sSplashIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("Splash.png"));

  private static final Icon sRestoreSplashIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("RestoreSplash.png"));

  private static final Icon sProgressFinishedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("Group0Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group1Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group2Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group3Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group4Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group5Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group6Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group7Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group8Finished.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group9Finished.png"))
  };

  private static final Icon sProgressRunningIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("Group0Running.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group1Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group2Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group3Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group4Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group5Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group6Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group7Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group8Running.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("Group9Running.png"))
  };
 

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network interfaces to the <B>plmaster</B>(1) daemon.
   */ 
  private MasterMgrClient[]  pMasterMgrClients;

  /**
   * The network interfaces to the <B>plqueuemgr</B>(1) daemon.
   */ 
  private QueueMgrClient[]  pQueueMgrClients;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to check all OpenGL calls for errors.
   */ 
  private boolean pDebugGL;

  /**
   * Whether to print all OpenGL calls to STDOUT.
   */ 
  private boolean pTraceGL;
  

  /** 
   * The OpenGL capabilities used by all GLCanvas instances.
   */ 
  private GLCapabilities  pGLCapabilities;

  /**
   * The template GLCanvas which creates the shared OpenCL context in which textures and 
   * display lists are initialized.
   */ 
  private GLCanvas  pGLCanvas;
  

  /**
   * The OpenGL display list handles which have been previously allocated with glGenLists() 
   * but are no longer being used by any instance. <P> 
   * 
   * These display list handles are collected when objects which create OpenGL display 
   * lists are garbage collected.  The display lists where all created in the same OpenGL
   * context as pGLCanvas. <P> 
   */ 
  private TreeSet<Integer>  pDisplayLists; 


  /*----------------------------------------------------------------------------------------*/

 
  /**
   * The splash screen frame.
   */ 
  private JFrame  pSplashFrame;

  /**
   * The splash screen progress bar;
   */ 
  private JProgressBar  pSplashProgress;
  
  
  /**
   * The restore layout splash frame.
   */ 
  private JFrame  pRestoreSplashFrame; 


  /**
   * The main application frame.
   */ 
  private JFrame  pFrame;

  /**
   * The secondary panel frames.
   */ 
  private LinkedList<JPanelFrame>  pPanelFrames;


  /*----------------------------------------------------------------------------------------*/

  /**
   * A lock used to serialize panel operations.
   */ 
  private ReentrantLock[] pOpsLocks;

  /**
   * Whether a panel operation is currently running.
   */ 
  private AtomicBoolean[] pOpsRunning;

  /**
   * Timers used to measure and report the duration of panel operations.
   */ 
  private TaskTimer[] pOpsTimers; 

  /**
   * The top-level progress message container.
   */ 
  private JPanel  pProgressPanel; 

  /**
   * The channel progress message containiers.
   */ 
  private Box[]  pProgressBoxes;
  private Box    pNoProgressBox;

  /**
   * The icons warning that a panel operation is in progress for a given channel.
   */ 
  private JLabel[]  pProgressLights;

  /**
   * The channel progress message fields.
   */ 
  private JTextField[]  pProgressFields;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract pathname of the last saved/loaded layout.
   */ 
  private Path  pLayoutPath;
 
  /**
   * The abstract pathname of the default layout.
   */ 
  private Path  pDefaultLayoutPath;
 
  /**
   * The abstract pathname of the override default layout.
   */ 
  private Path  pOverrideLayoutPath;
 
  /** 
   * Whether to restore the initial panel layout.
   */
  private boolean  pRestoreLayout;

  /**
   * Whether the restored initial layout should include node and/or job group selections.
   */ 
  private boolean  pRestoreSelections; 

  /**
   * Whether a layout restore task is in progress.
   */ 
  private AtomicBoolean  pIsRestoring;

  /**
   * The parent of the root manager panel.
   */ 
  private JPanel  pRootPanel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Caches of plugin vendor, names, revision number and supported operating systems
   * indexed by toolset name.
   */ 
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pEditorPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pComparatorPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pActionPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pToolPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pArchiverPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pMasterExtPlugins;
  private TreeMap<String,
		  TripleMap<String,String,VersionID,TreeSet<OsType>>>  pQueueExtPlugins;

  /** 
   * Caches of plugin menu layouts indexed by toolset name.
   */ 
  private TreeMap<String,PluginMenuLayout>  pEditorLayouts; 
  private TreeMap<String,PluginMenuLayout>  pComparatorLayouts; 
  private TreeMap<String,PluginMenuLayout>  pActionLayouts; 
  private TreeMap<String,PluginMenuLayout>  pToolLayouts; 
  private TreeMap<String,PluginMenuLayout>  pArchiverLayouts; 
  private TreeMap<String,PluginMenuLayout>  pMasterExtLayouts; 
  private TreeMap<String,PluginMenuLayout>  pQueueExtLayouts; 



  /*----------------------------------------------------------------------------------------*/

  /**
   * The active node browser panels. <P> 
   */ 
  private PanelGroup<JNodeBrowserPanel>  pNodeBrowserPanels;

  /**
   * The active node viewer panels. <P> 
   */ 
  private PanelGroup<JNodeViewerPanel>  pNodeViewerPanels;

  /**
   * The active node detail panels. <P> 
   */ 
  private PanelGroup<JNodeDetailsPanel>  pNodeDetailsPanels;

  /**
   * The active node history panels. <P> 
   */ 
  private PanelGroup<JNodeHistoryPanel>  pNodeHistoryPanels;

  /**
   * The active node files panels. <P> 
   */ 
  private PanelGroup<JNodeFilesPanel>  pNodeFilesPanels;

  /**
   * The active node links panels. <P> 
   */ 
  private PanelGroup<JNodeLinksPanel>  pNodeLinksPanels;


  /**
   * The active job servers panels. <P> 
   */ 
  private PanelGroup<JQueueJobServersPanel>  pQueueJobServersPanels;

  /**
   * The active job server stats panels. <P> 
   */ 
  private PanelGroup<JQueueJobServerStatsPanel>  pQueueJobServerStatsPanels;

  /**
   * The active job slots panels. <P> 
   */ 
  private PanelGroup<JQueueJobSlotsPanel>  pQueueJobSlotsPanels;

  /**
   * The active job browser panels. <P> 
   */ 
  private PanelGroup<JQueueJobBrowserPanel>  pQueueJobBrowserPanels;

  /**
   * The active job viewer panels. <P> 
   */ 
  private PanelGroup<JQueueJobViewerPanel>  pQueueJobViewerPanels;

  /**
   * The active job details panels. <P> 
   */ 
  private PanelGroup<JQueueJobDetailsPanel>  pQueueJobDetailsPanels;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique paths to the previously collapsed viewer nodes.
   */ 
  private HashSet<String>  pCollapsedNodePaths;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The save layouts dialog.
   */ 
  private JSaveLayoutDialog  pSaveLayoutDialog;

  /**
   * The manage layouts dialog.
   */ 
  private JManageLayoutsDialog  pManageLayoutsDialog;

  /**
   * The error message dialog.
   */ 
  private JErrorDialog  pErrorDialog;

  /**
   * The user preferences dialog.
   */ 
  private JUserPrefsDialog  pUserPrefsDialog;

  /**
   * The information dialog.
   */ 
  private JAboutDialog  pAboutDialog;

  /**
   * The customer configuration profile data dialog.
   */ 
  private JConfigDialog  pConfigDialog;

  /**
   * The manage editors dialog.
   */ 
  private JDefaultEditorsDialog  pDefaultEditorsDialog;

  /**
   * The manage privileges dialog.
   */ 
  private JManagePrivilegesDialog  pManagePrivilegesDialog;

  /**
   * The manage toolsets dialog.
   */ 
  private JManageToolsetsDialog  pManageToolsetsDialog;

  /**
   * The manage license keys dialog.
   */ 
  private JManageLicenseKeysDialog  pManageLicenseKeysDialog;

  /**
   * The manage selection keys, groups and schedules dialog.
   */ 
  private JManageSelectionKeysDialog  pManageSelectionKeysDialog;

  /**
   * The queue job submission dialog.
   */ 
  private JQueueJobsDialog  pQueueJobsDialog;

  /**
   * The dialog giving details of the failure of a subprocess.
   */ 
  private JSubProcessFailureDialog  pSubProcessFailureDialog;

  /**
   * The manage server extensions dialog.
   */
  private JManageServerExtensionsDialog  pManageServerExtensionsDialog; 

  /**
   * The database backup dialog.
   */
  private JFileSelectDialog  pBackupDialog; 

  /**
   * The archive dialog.
   */
  private JArchiveDialog  pArchiveDialog; 

  /**
   * The offline dialog.
   */
  private JOfflineDialog  pOfflineDialog; 

  /**
   * The restore dialog.
   */
  private JRestoreDialog  pRestoreDialog; 
  
  /**
   * The archive volumes dialog.
   */
  private JArchiveVolumesDialog  pArchiveVolumesDialog; 

  /**
   * The log history dialog. 
   */
  private JLogsDialog  pLogsDialog; 

  /**
   * The server resource usage history dialog.
   */
  private JResourceUsageHistoryDialog  pResourceUsageHistoryDialog;

}
