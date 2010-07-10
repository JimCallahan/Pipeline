// $Id: UIMaster.java,v 1.122 2009/12/14 21:48:22 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.plaf.synth.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.core.exts.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.*;
import us.temerity.pipeline.ui.*;

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
   * @param remoteServer
   *   Whether to listen for network connections from plremote(1).
   * 
   * @param usePBuffers
   *   Whether to attempt to load textures offscreen using OpenGL pbuffers.
   * 
   * @param debugGL
   *   Whether to check all OpenGL calls for errors.
   * 
   * @param traceGL
   *   Whether to print all OpenGL calls to STDOUT.
   * 
   * @param debugSwing
   *   Whether to check for Swing thread violations. 
   */ 
  private 
  UIMaster
  (
   Path layout, 
   boolean restoreLayout,
   boolean restoreSelections, 
   boolean remoteServer, 
   boolean usePBuffers, 
   boolean debugGL, 
   boolean traceGL, 
   boolean debugSwing
  ) 
  {
    {
      pMasterMgrList = new LinkedList<MasterMgrClient>();
      pMasterMgrClientStack = new Stack<MasterMgrClient>();

      MasterMgrClient mclient = acquireMasterMgrClient();
      releaseMasterMgrClient(mclient);
    }

    {
      pQueueMgrList = new LinkedList<QueueMgrClient>();
      pQueueMgrClientStack = new Stack<QueueMgrClient>();

      QueueMgrClient qclient = acquireQueueMgrClient();
      releaseQueueMgrClient(qclient);
    }
    
    {
      pUICaches = new TreeMap<Integer, UICache>();
      for (int i = 0; i < 10; i++ ) {
        pUICaches.put(i, new UICache());
      }
    }


    if(remoteServer) 
      pRemoteServer = new RemoteServer(this);

    pOpLoggers = new TreeMap<Long, OpLogger>();
    pNextLoggerID = new AtomicLong(9L); 

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
    pAnnotationPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pKeyChooserPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    pBuilderCollectionPlugins = 
      new TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>();
    
    pBuilderLayoutGroups = new TripleMap<String, String, VersionID, LayoutGroup>();

    pAnnotationExtrasLock = new Object();
    
    pEditorLayouts            = new TreeMap<String,PluginMenuLayout>();                   
    pComparatorLayouts        = new TreeMap<String,PluginMenuLayout>();                  
    pActionLayouts            = new TreeMap<String,PluginMenuLayout>();                    
    pToolLayouts              = new TreeMap<String,PluginMenuLayout>();                   
    pArchiverLayouts          = new TreeMap<String,PluginMenuLayout>();                   
    pMasterExtLayouts         = new TreeMap<String,PluginMenuLayout>();                   
    pQueueExtLayouts          = new TreeMap<String,PluginMenuLayout>();                   
    pAnnotationLayouts        = new TreeMap<String,PluginMenuLayout>();                   
    pKeyChooserLayouts        = new TreeMap<String,PluginMenuLayout>();
    pBuilderCollectionLayouts = new TreeMap<String,PluginMenuLayout>();

    pNodeBrowserPanels = new PanelGroup<JNodeBrowserPanel>();
    pNodeViewerPanels  = new PanelGroup<JNodeViewerPanel>();

    pNodeDetailsPanels = new PanelGroup<JNodeDetailsPanel>();
    pNodeHistoryPanels = new PanelGroup<JNodeHistoryPanel>();
    pNodeFilesPanels   = new PanelGroup<JNodeFilesPanel>();
    pNodeLinksPanels   = new PanelGroup<JNodeLinksPanel>();

    pNodeAnnotationsPanels = new PanelGroup<JNodeAnnotationsPanel>();

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

    pDebugGL = debugGL;
    pTraceGL = traceGL; 
    pDisplayLists = new TreeSet<Integer>();

    pDebugSwing = debugSwing; 

    pUsePBuffers = usePBuffers;

    SwingUtilities.invokeLater(new SplashFrameTask(this));  // Does this need to be run 
                                                            // in the event thread???
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
   * @param remoteServer
   *   Whether to listen for network connections from plremote(1).
   * 
   * @param usePBuffers
   *   Whether to attempt to load textures offscreen using OpenGL pbuffers.
   * 
   * @param debugGL
   *   Whether to check all OpenGL calls for errors.
   * 
   * @param traceGL
   *   Whether to print all OpenGL calls to STDOUT.
   * 
   * @param debugSwing
   *   Whether to check for Swing thread violations. 
   */ 
  public static void 
  init
  (
   Path layout,
   boolean restoreLayout,
   boolean restoreSelections,
   boolean remoteServer, 
   boolean usePBuffers, 
   boolean debugGL, 
   boolean traceGL, 
   boolean debugSwing
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(layout, restoreLayout, restoreSelections, remoteServer, 
                           usePBuffers, debugGL, traceGL, debugSwing);
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
   * <p>
   * The connection that has been acquired should always be returned using the
   * {@link #releaseMasterMgrClient(MasterMgrClient)} method.
   * 
   * @return An unused connection to the MasterMgrClient.
   */ 
  public MasterMgrClient
  acquireMasterMgrClient() 
  {
    synchronized(pMasterMgrClientStack) {
      if(pMasterMgrClientStack.isEmpty()) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Net, LogMgr.Level.Finest,
           "Creating New Master Manager Client.");

        MasterMgrClient mclient = new MasterMgrClient();
        pMasterMgrList.add(mclient);
        return mclient;
      }
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Net, LogMgr.Level.Finest,
           "Reusing Master Manager Client: " + (pMasterMgrClientStack.size()-1) + 
           " inactive");

        return pMasterMgrClientStack.pop();
      }
    }
  }
  
  /**
   * Return an inactive connection to the master manager for reuse.
   * 
   *  @param mclient
   *    The inactive connection.
   */ 
  public void
  releaseMasterMgrClient
  (
    MasterMgrClient mclient  
  )
  {
    synchronized(pMasterMgrClientStack) {
      pMasterMgrClientStack.push(mclient);
      
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Net, LogMgr.Level.Finest,
         "Freed Master Manager Client: " + pMasterMgrClientStack.size() + " inactive");
    }
  }

  /**
   * Get the network connection to <B>plqueuemgr</B>(1).
   * <p>
   * The connection that has been acquired should always be returned using the
   * {@link #releaseQueueMgrClient(QueueMgrClient)} method.
   * 
   * @return An unused connection to the QueueMgrClient.
   */ 
  public QueueMgrClient
  acquireQueueMgrClient() 
  {
    synchronized(pQueueMgrClientStack) {
      if(pQueueMgrClientStack.isEmpty()) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Net, LogMgr.Level.Finest,
           "Creating New Queue Manager Client.");

        QueueMgrClient qclient = new QueueMgrClient();
        pQueueMgrList.add(qclient);
        return qclient;
      }
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Net, LogMgr.Level.Finest,
           "Reusing Queue Manager Client: " + (pQueueMgrClientStack.size()-1) + " inactive");

        return pQueueMgrClientStack.pop();
      }
    }
  }
  
  /**
   * Return an inactive connection to the queue manager for reuse.
   * 
   *  @param qclient
   *    The inactive connection.
   */ 
  public void
  releaseQueueMgrClient
  (
    QueueMgrClient qclient  
  )
  {
    synchronized(pQueueMgrClientStack) {
      pQueueMgrClientStack.push(qclient);
      
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Net, LogMgr.Level.Finest,
         "Freed Queue Manager Client: " + pQueueMgrClientStack.size() + " inactive");
    }
   }

  
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   U I   C A C H E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the UICache for a particular channel.
   * <p>
   * @param channel
   *   The channel whose cache should be retrieved.
   */
  public UICache
  getUICache
  (
    int channel  
  )
  {
//    LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
//      "Accessing UI Cache with ID (" + channel + ")");
    LogMgr.getInstance().flush(Kind.Ops, Level.Finest);
    assert(channel >= 0 && channel <= pUICaches.size());
    synchronized (pUICaches) {
      return pUICaches.get(channel);
    }
  }
  
  /**
   * Create a new UICache for a dialog.
   * <p>
   * Dialogs which extend {@link JFullCacheDialog} call this method to create a new cache
   * for themselves.
   * 
   * @return
   *   The unique ID number which the dialog can later use to access the cache.
   */
  public Integer
  registerUICache()
  {
    synchronized(pUICaches) {
      Integer cacheNum = pUICaches.lastKey() + 1;
      pUICaches.put(cacheNum, new UICache());
      
//      LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
//         "Creating UI Cache with ID (" + cacheNum + ")");
      return cacheNum;
    }
  }
  
  /**
   * Call the {@link UICache#invalidateCachedActiveToolsetNames()} method on the caches for 
   * all the channels.
   * <p>
   * This call should be made in any UI class which modifies the active toolset.
   */
  public void
  invalidateAllCachedActiveToolsetNames()
  {
    synchronized(pUICaches) {
      LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
        "Invalidating all Cached Active Toolsets");
      for (UICache cache : pUICaches.values()) {
        cache.invalidateCachedActiveToolsetNames();
      }
    }
  }
  
  /**
   * Call the {@link UICache#invalidateCachedDefaultToolsetName()} method on the caches for 
   * all the channels.
   * <p>
   * This call should be made in any UI class which modifies the default toolset.
   */
  public void
  invalidateAllCachedDefaultToolsetName()
  {
    synchronized(pUICaches) {
      LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
        "Invalidating all Cached Default Toolsets");
      for (UICache cache : pUICaches.values()) {
        cache.invalidateCachedDefaultToolsetName();
      }
    }
  }

  /**
   * Call the {@link UICache#invalidateCachedPrivilegeDetails} method on the caches for 
   * all the channels.
   * <p>
   * This call should be made in any UI class which modifies the privileges.
   */
  public void
  invalidateAllCachedPrivilegeDetails()
  {
    synchronized(pUICaches) {
      LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
        "Invalidating all Cached Privileged Details");
      for (UICache cache : pUICaches.values()) {
        cache.invalidateCachedPrivilegeDetails();
      }
    }
  }
  
  /**
   * Call the {@link UICache#invalidateCachedWorkGroups} method on the caches for 
   * all the channels.
   * <p>
   * This call should be made in any UI class which modifies work groups.
   */
  public void
  invalidateAllCachedWorkGroups()
  {
    synchronized(pUICaches) {
      LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
        "Invalidating all Cached Work Groups");
      for (UICache cache : pUICaches.values()) {
        cache.invalidateCachedWorkGroups();
      }
    }
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

    if(pDebugSwing) 
      RepaintManager.setCurrentManager(new ThreadingDebugRepaintManager());

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
    else {
      /* For layouts that have not been saved or restored from a saved file give 
         the main window a title that indicates it has not been saved. */

      title = ("plui - (untitled)+");
    }

    pFrame.setTitle(title);    
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
  public TreeMap<String,TreeSet<String>>
  lookupWorkingAreaContainingMenus
  (
   int channel, 
   String name
  )
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    if(name != null) {
      MasterMgrClient client = acquireMasterMgrClient();
      try {
        views.putAll(client.getWorkingAreasContaining(name));
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        releaseMasterMgrClient(client);
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
  public TreeMap<String,TreeSet<String>>
  lookupWorkingAreaEditingMenus
  (
   int channel, 
   String name
  )
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    if(name != null) {
      MasterMgrClient client = acquireMasterMgrClient();
      try {
        views.putAll(client.getWorkingAreasEditing(name));
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        releaseMasterMgrClient(client);
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
        String afirst = agroup.first();
        String first3 = afirst.substring(0, Math.min(3, afirst.length()));

        String alast  = agroup.last(); 
        String last3  = alast.substring(0, Math.min(3, alast.length()));

	JMenu gsub = new JMenu(first3.toUpperCase() + "-" + last3.toUpperCase()); 
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
    synchronized(pAnnotationPlugins) {
      pAnnotationPlugins.clear();
    }
    synchronized(pBuilderCollectionPlugins) {
      pBuilderCollectionPlugins.clear();
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
    synchronized(pAnnotationLayouts) {
      pAnnotationLayouts.clear();
    }
    synchronized(pBuilderCollectionLayouts) {
      pBuilderCollectionLayouts.clear();
    }

    synchronized(pAnnotationExtrasLock) {
      pAnnotationPermissions = null;
      pAnnotationContexts    = null;
    }

    synchronized (pBuilderLayoutGroups) {
      pBuilderLayoutGroups.clear();
    }
  }

  public void 
  clearExtPluginCaches() 
  {
    synchronized(pMasterExtPlugins) {
      pMasterExtPlugins.clear();
    }
    synchronized(pQueueExtPlugins) {
      pQueueExtPlugins.clear();
    }

    synchronized(pMasterExtLayouts) {
      pMasterExtLayouts.clear();
    }    
    synchronized(pQueueExtLayouts) {
      pQueueExtLayouts.clear();
    }    
  }

  public void 
  clearKeyChooserPluginCaches() 
  {
    synchronized(pKeyChooserPlugins) {
      pKeyChooserPlugins.clear();
    }

    synchronized(pKeyChooserLayouts) {
      pKeyChooserLayouts.clear();
    }    
  }
  
  public void 
  clearBuilderCollectionPluginCaches() 
  {
    synchronized(pBuilderCollectionPlugins) {
      pBuilderCollectionPlugins.clear();
    }
    synchronized(pBuilderCollectionLayouts) {
      pBuilderCollectionLayouts.clear();
    }
    synchronized (pBuilderLayoutGroups) {
      pBuilderLayoutGroups.clear();
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
   JMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
   * Rebuild the contents of an tool plugin menu for the default toolset.
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
   * Rebuild the contents of an tool plugin menu for the default toolset.
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      UICache cache = getUICache(channel);
      String tname = cache.getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
   * Rebuild the contents of an tool plugin menu for the default toolset.
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
   JMenu menu, 
   ActionListener listener
  ) 
  {
    rebuildDefaultToolMenu(null, channel, menu, listener);
  }

  /**
   * Rebuild the contents of an tool plugin menu for the default toolset.
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
   JMenu menu, 
   ActionListener listener
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      UICache cache = getUICache(channel);
      String tname = cache.getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
   * Rebuild the contents of an builder plugin menu for the given toolset.
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
   *   
   * @param enabled
   *   Should the menu entries be enabled?  This is false whenever the Node Viewer Builder
   *   menu is used in a working area where the user does not have Node Manager permissions.
   */ 
  public void
  rebuildDefaultBuilderCollectionMenu
  (
   JPopupMenu topmenu,
   int channel, 
   JMenu menu, 
   ActionListener listener,
   boolean enabled
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String, String, VersionID, LayoutGroup> builderLayouts = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    
    String tname = null;
    {
      MasterMgrClient client = acquireMasterMgrClient();
      try {
        tname = getUICache(channel).getCachedDefaultToolsetName();
      } 
      catch (PipelineException ex) {
        menu.removeAll();
        LogMgr.getInstance().log
          (Kind.Ops, Level.Warning, 
          "Unable to retrieve a default toolset when trying to create the Builder menus.");
        return;
      } 
      finally {
        releaseMasterMgrClient(client);
      }
    }

    MasterMgrClient client = acquireMasterMgrClient();
    try {
      synchronized(pBuilderCollectionPlugins) {
        plugins = pBuilderCollectionPlugins.get(tname);
        if(plugins == null) {
          DoubleMap<String,String,TreeSet<VersionID>> index = 
            client.getToolsetBuilderCollectionPlugins(tname);

          TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
            PluginMgrClient.getInstance().getBuilderCollections();

          plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
          for(String vendor : index.keySet()) {
            for(String name : index.get(vendor).keySet()) {
              for(VersionID vid : index.get(vendor).get(name)) {
                plugins.put(vendor, name, vid, all.get(vendor, name, vid));
              }
            }
          }
          
          pBuilderCollectionPlugins.put(tname, plugins);
        }
      }
      
      synchronized(pBuilderCollectionLayouts) {
        layout = pBuilderCollectionLayouts.get(tname);
        if(layout == null) {
          layout = client.getBuilderCollectionMenuLayout(tname);
          pBuilderCollectionLayouts.put(tname, layout);
        }
      }

      synchronized(pBuilderLayoutGroups) {
        if(pBuilderLayoutGroups.isEmpty()) {
          PluginMgrClient pclient = PluginMgrClient.getInstance();
          pBuilderLayoutGroups.putAll(pclient.getBuilderCollectionLayouts());
        }
        builderLayouts = 
          new TripleMap<String, String, VersionID, LayoutGroup>(pBuilderLayoutGroups);
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      releaseMasterMgrClient(client);
    }

    menu.removeAll();
    if((layout != null) && !layout.isEmpty()) {
      for(PluginMenuLayout pml : layout) 
        menu.add(rebuildCollectionPluginMenuHelper(topmenu, pml, "launch-builder", 
                                                   plugins, builderLayouts, listener, 
                                                   enabled));
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
  rebuildCollectionPluginMenuHelper
  (
   JPopupMenu topmenu,
   PluginMenuLayout layout, 
   String prefix, 
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins,
   TripleMap<String, String, VersionID, LayoutGroup> builderLayouts,
   ActionListener listener,
   boolean enabled
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      JMenu sub = new JMenu(layout.getName());
      LayoutGroup group = 
        builderLayouts.get(layout.getVendor(), layout.getName(), layout.getVersionID());
      if (group != null)
        rebuildMenuFromLayoutGroup(topmenu, sub, prefix + ":" + layout.getName() + ":" + 
                                   layout.getVersionID() + ":" + layout.getVendor(), group, 
                                   listener, enabled);
      item = sub;
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
        sub.add(rebuildCollectionPluginMenuHelper(topmenu, pml, prefix, plugins, 
          builderLayouts, listener, enabled));
      item = sub;
    }

    return item;
  }
  
  private void
  rebuildMenuFromLayoutGroup
  (
    JPopupMenu topmenu,
    JMenu parent,
    String prefix,
    LayoutGroup group,
    ActionListener listener,
    boolean enabled
  )
  {
    for (LayoutGroup subGroup : group.getSubGroups()) {
      JMenu sub = new JMenu(subGroup.getName());
      rebuildMenuFromLayoutGroup(topmenu, sub, prefix, subGroup, listener, enabled);
      parent.add(sub);
    }
    
    for (String entry : group.getEntries()) {
      JMenuItem item = new JPopupMenuItem(topmenu, entry);
      item.setEnabled(enabled);
      item.setActionCommand(prefix + ":" + entry);
      item.addActionListener(listener);
      parent.add(item);
    }
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
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
    int channel,
    int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new master extension plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createMasterExtSelectionField
  (
    int channel,
    int width
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new queue extension plugin selection field based on the default toolset.
   *
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createQueueExtSelectionField
  (
    int channel,
    int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

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
    finally {
      releaseMasterMgrClient(client);
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
    MasterMgrClient client = acquireMasterMgrClient();
    try {
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
    finally {
      releaseMasterMgrClient(client);
    }

    field.updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new per-node annotation plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createAnnotationSelectionField
  (
   int channel,
   int width
  ) 
  {
    return createOrUpdateAnnotationPluginField
      (channel, null, width, null, AnnotationContext.PerNode);
  }

  /**
   * Update the contents of an per-node annotation plugin field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param field
   *   The field to update. 
   */ 
  public void 
  updateAnnotationPluginField
  (
   int channel,
   JPluginSelectionField field
  ) 
  {
    createOrUpdateAnnotationPluginField
      (channel, null, 0, field, AnnotationContext.PerNode);
  }

  /**
   * Update the contents of an per-version annotation plugin field based on the 
   * given working toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param toolset
   *   The name of the toolset providing the menu layout.
   * 
   * @param field
   *   The field to update. 
   */ 
  public void 
  updateAnnotationPluginField
  (
   int channel,
   String toolset, 
   JPluginSelectionField field
  ) 
  {
    createOrUpdateAnnotationPluginField
      (channel, toolset, 0, field, AnnotationContext.PerVersion);
  }

  /**
   * Update or create the contents of an annotation plugin field.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param toolset
   *   The toolset which provides the menu layout or <CODE>null</CODE> to use the default
   *   toolset.
   * 
   * @param width
   *   The minimum and preferred width of the field (if creating).
   * 
   * @param field
   *   The field to update or <CODE>null</CODE> to create a new one.
   * 
   * @param context
   *   The context in which the annotations are to be used.
   * 
   * @return 
   *   The new or updated field.
   */ 
  private JPluginSelectionField
  createOrUpdateAnnotationPluginField
  (
   int channel,
   String toolset, 
   int width, 
   JPluginSelectionField field,
   AnnotationContext context
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    UICache cache = getUICache(channel);
    try {
      String tname = toolset;
      if(tname == null) 
        tname = cache.getCachedDefaultToolsetName();

      TripleMap<String,String,VersionID,TreeSet<OsType>> tsPlugins = null;
      synchronized(pAnnotationPlugins) {
	tsPlugins = pAnnotationPlugins.get(tname);
	if(tsPlugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetAnnotationPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getAnnotations();

	  tsPlugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		tsPlugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pAnnotationPlugins.put(tname, tsPlugins);
	}
      }

      synchronized(pAnnotationLayouts) {
	layout = pAnnotationLayouts.get(tname);
	if(layout == null) {
	  layout = client.getAnnotationMenuLayout(tname);
	  pAnnotationLayouts.put(tname, layout);
	}
      }
      
      boolean isAnnotator = cache.getCachedPrivilegeDetails().isAnnotator();

      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
      synchronized(pAnnotationExtrasLock) {
        if(pAnnotationPermissions == null) 
          pAnnotationPermissions =
            PluginMgrClient.getInstance().getAnnotationPermissions();

        if(pAnnotationContexts == null) 
          pAnnotationContexts = 
            PluginMgrClient.getInstance().getAnnotationContexts();
      
        for(String vendor : tsPlugins.keySet()) {
          for(String name : tsPlugins.keySet(vendor)) {
            for(VersionID vid : tsPlugins.keySet(vendor, name)) {
              TreeSet<AnnotationContext> contexts = 
                pAnnotationContexts.get(vendor, name, vid);
              if((contexts != null) && contexts.contains(context)) {
                switch(context) {
                case PerNode:
                  {
                    AnnotationPermissions perm = null;
                    if(!isAnnotator) 
                      perm = pAnnotationPermissions.get(vendor, name, vid); 

                    if(isAnnotator || ((perm != null) && perm.isUserCreatable()))
                      plugins.put(vendor, name, vid, tsPlugins.get(vendor, name, vid));
                  }
                  break;

                case PerVersion:
                  plugins.put(vendor, name, vid, tsPlugins.get(vendor, name, vid));
                }
              }
            }
          }
        }        
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      
      layout  = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
      
      if(field != null) 
        return null;
    }
    finally {
      releaseMasterMgrClient(client);
    }
    
    if(field == null) 
      field = UIFactory.createPluginSelectionField(layout, plugins, width);
    else 
      field.updatePlugins(layout, plugins);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new key chooser plugin selection field based on the default toolset.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JPluginSelectionField
  createKeyChooserSelectionField
  (
    int channel,
    int width  
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

      synchronized(pKeyChooserPlugins) {
	plugins = pKeyChooserPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetKeyChooserPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getKeyChoosers();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pKeyChooserPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pKeyChooserLayouts) {
	layout = pKeyChooserLayouts.get(tname);
	if(layout == null) {
	  layout = client.getKeyChooserMenuLayout(tname);
	  pKeyChooserLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }
    finally {
      releaseMasterMgrClient(client);
    }

    return UIFactory.createPluginSelectionField(layout, plugins, width);
  }

  /**
   * Update the contents of an key chooser plugin field.
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public void 
  updateKeyChooserPluginField
  (
    int channel,
    JPluginSelectionField field
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

      synchronized(pKeyChooserPlugins) {
	plugins = pKeyChooserPlugins.get(tname);
	if(plugins == null) {
	  DoubleMap<String,String,TreeSet<VersionID>> index = 
	    client.getToolsetKeyChooserPlugins(tname);

	  TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
	    PluginMgrClient.getInstance().getKeyChoosers();

	  plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
	  for(String vendor : index.keySet()) {
	    for(String name : index.keySet(vendor)) {
	      for(VersionID vid : index.get(vendor, name)) {
		plugins.put(vendor, name, vid, all.get(vendor, name, vid));
	      }
	    }
	  }

	  pKeyChooserPlugins.put(tname, plugins);
	}
      }
      
      synchronized(pKeyChooserLayouts) {
	layout = pKeyChooserLayouts.get(tname);
	if(layout == null) {
	  layout = client.getKeyChooserMenuLayout(tname);
	  pKeyChooserLayouts.put(tname, layout);
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }
    finally {
      releaseMasterMgrClient(client);
    }

    field.updatePlugins(layout, plugins);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new builder ID selection field based on the default toolset.
   *
   * @param channel
   *   The index of the update channel.
   *
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public JBuilderIDSelectionField
  createBuilderIDSelectionField
  (
    int channel,
    int width
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,LayoutGroup> builderLayouts = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

      synchronized(pBuilderCollectionPlugins) {
        plugins = pBuilderCollectionPlugins.get(tname);
        if(plugins == null) {
          DoubleMap<String,String,TreeSet<VersionID>> index = 
            client.getToolsetBuilderCollectionPlugins(tname);

          TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
            PluginMgrClient.getInstance().getBuilderCollections();

          plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
          for(String vendor : index.keySet()) {
            for(String name : index.keySet(vendor)) {
              for(VersionID vid : index.get(vendor, name)) {
                plugins.put(vendor, name, vid, all.get(vendor, name, vid));
              }
            }
          }

          pBuilderCollectionPlugins.put(tname, plugins);
        }
      }

      synchronized(pBuilderCollectionLayouts) {
        layout = pBuilderCollectionLayouts.get(tname);
        if(layout == null) {
          layout = client.getBuilderCollectionMenuLayout(tname);
          pBuilderCollectionLayouts.put(tname, layout);
        }
      }

      synchronized(pBuilderLayoutGroups) {
        if(pBuilderLayoutGroups.isEmpty()) {
          PluginMgrClient pclient = PluginMgrClient.getInstance();
          pBuilderLayoutGroups.putAll(pclient.getBuilderCollectionLayouts());
        }
        builderLayouts = 
          new TripleMap<String, String, VersionID, LayoutGroup>(pBuilderLayoutGroups);
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);

      layout = new PluginMenuLayout();
      plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    }
    finally {
      releaseMasterMgrClient(client);
    }

    return UIFactory.createBuilderIDSelectionField(layout, builderLayouts, plugins, width);
  }

  /**
   * Create a new set of builder ID related fields based on the default toolset with a 
   * title and add them to the given panels.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param tpanel
   *   The titles panel.
   *   
   * @param title
   *   The name for the field that should appear in the UI.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param builderID
   *   The initial builder ID. 
   * 
   * @param vwidth
   *   The minimum and preferred width of the text field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public JBuilderIDSelectionField
  createTitledBuilderIDSelectionField
  (
    int channel, 
    JPanel tpanel, 
    String title,  
    int twidth,
    JPanel vpanel, 
    BuilderID builderID, 
    int vwidth, 
    String tooltip
  ) 
  { 
    tpanel.add(UIFactory.createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));
    JBuilderIDSelectionField bfield = createBuilderIDSelectionField(channel, vwidth);
    vpanel.add(bfield);

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    {
      JLabel label = 
        UIFactory.createFixedLabel
        ("Version:", twidth, JLabel.RIGHT, 
         "The revision number of the builder collection."); 
      tpanel.add(label); 

      JTextField field = bfield.createVersionField(vwidth); 
      vpanel.add(field);
    }

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    {
      JLabel label = 
        UIFactory.createFixedLabel
        ("Vendor:", twidth, JLabel.RIGHT, 
         "The vendor of the builder collection."); 
      tpanel.add(label); 
      JTextField field = bfield.createVendorField(vwidth); 
      vpanel.add(field);
    }
    
    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    {
      JLabel label = 
        UIFactory.createFixedLabel
        ("OS Support:", twidth, JLabel.RIGHT, 
         "The operating systems supported by the builer collection."); 
      tpanel.add(label); 
      JOsSupportField field = bfield.createOsSupportField(vwidth); 
      vpanel.add(field);
    }

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    {
      JLabel label = 
        UIFactory.createFixedLabel
        ("Builder Name:", twidth, JLabel.RIGHT, 
         "The name of the selected builder within the builder collection."); 
      tpanel.add(label); 
      JTextField field = bfield.createBuilderNameField(vwidth); 
      vpanel.add(field);
    }

    bfield.setBuilderID(builderID); 

    return bfield;
  }

  /**
   * Update the contents of a builder ID selection field.
   * 
   * @param channel
   *   The index of the update channel.
   */ 
  public void 
  updateBuilderIDSelectionFieldchannel
  (
    JBuilderIDSelectionField field,
    int channel
  ) 
  {
    PluginMenuLayout layout = null;
    TripleMap<String,String,VersionID,LayoutGroup> builderLayouts = null;
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = null;
    MasterMgrClient client = acquireMasterMgrClient();
    try {
      String tname = getUICache(channel).getCachedDefaultToolsetName();

      synchronized(pBuilderCollectionPlugins) {
        plugins = pBuilderCollectionPlugins.get(tname);
        if(plugins == null) {
          DoubleMap<String,String,TreeSet<VersionID>> index = 
            client.getToolsetBuilderCollectionPlugins(tname);

          TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
            PluginMgrClient.getInstance().getBuilderCollections();

          plugins = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
          for(String vendor : index.keySet()) {
            for(String name : index.keySet(vendor)) {
              for(VersionID vid : index.get(vendor, name)) {
                plugins.put(vendor, name, vid, all.get(vendor, name, vid));
              }
            }
          }

          pBuilderCollectionPlugins.put(tname, plugins);
        }
      }
      
      synchronized(pBuilderCollectionLayouts) {
        layout = pBuilderCollectionLayouts.get(tname);
        if(layout == null) {
          layout = client.getBuilderCollectionMenuLayout(tname);
          pBuilderCollectionLayouts.put(tname, layout);
        }
      }

      synchronized(pBuilderLayoutGroups) {
        if(pBuilderLayoutGroups.isEmpty()) {
          PluginMgrClient pclient = PluginMgrClient.getInstance();
          pBuilderLayoutGroups.putAll(pclient.getBuilderCollectionLayouts());
        }
        builderLayouts = 
          new TripleMap<String, String, VersionID, LayoutGroup>(pBuilderLayoutGroups);
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }
    finally {
      releaseMasterMgrClient(client);
    }

    field.updatePlugins(layout, builderLayouts, plugins);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E M O T E                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle the request from plremote(1) to display a node network rooted at the given root 
   * node in one of the Node Browser/Viewer panels. <P> 
   * 
   * Depending on user preferences, this command will either automatically update or 
   * create new Node Browser/Viewer panels or cause a dialog to be displayed by which 
   * queries the user for which specific panels will have their node selection modified.  
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param postUpdateSelected
   *   The names of the nodes which should be selected in the NodeViewer after an update.
   */
  public void
  remoteWorkingSelect
  (
   String name, 
   TreeSet<String> postUpdateSelected
  ) 
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finer,
       "Selecting Working Version: " + name); 

    UserPrefs prefs = UserPrefs.getInstance();

    int channel = 1; 
    try {
      channel = new Integer(prefs.getRemoteUpdateChannel());
    }
    catch(NumberFormatException ex) {
    }

    Thread task = null;
    if(prefs.getRemoteSettings().equals("Display Dialog")) 
      task = new ShowRemoteWorkingSelectDialogTask(channel, name, postUpdateSelected);
    else {
      boolean replace = prefs.getRemoteUpdateMethod().equals("Replace Selecion");
      task = new RemoteWorkingSelectTask(channel, name, replace, postUpdateSelected);
    }

    SwingUtilities.invokeLater(task); 
  }
  
  /**
   * Performs the actual update the of Node Browser/Viewer panels to display a node network 
   * rooted at the given root by adding it to the current selection.
   *
   * @param channel 
   *   The update channel index.
   *
   * @param name
   *   The fully resolved node name.
   * 
   * @param postUpdateSelected
   *   The names of the nodes which should be selected in the NodeViewer after an update.
   */ 
  public void 
  remoteAddSelection
  (
   int channel, 
   String name, 
   TreeSet<String> postUpdateSelected
  ) 
  {
    JNodeViewerPanel viewer = pNodeViewerPanels.getPanel(channel); 
    if(viewer != null) 
      viewer.addRoot(name, postUpdateSelected);
    else 
      remoteCreateNewSelectionWindow(channel, name, postUpdateSelected);
  }
   
  /**
   * Performs the actual update the of Node Browser/Viewer panels to display a node network 
   * rooted at the given root by replacing the current selection.
   *
   * @param channel 
   *   The update channel index.
   *
   * @param name
   *   The fully resolved node name.
   * 
   * @param postUpdateSelected
   *   The names of the nodes which should be selected in the NodeViewer after an update.
   */ 
  public void 
  remoteReplaceSelection
  (
   int channel, 
   String name, 
   TreeSet<String> postUpdateSelected
  ) 
  {
    JNodeViewerPanel viewer = pNodeViewerPanels.getPanel(channel); 
    if(viewer != null) {  
      TreeSet<String> roots = new TreeSet<String>(); 
      roots.add(name);
      viewer.setRoots(roots, postUpdateSelected);
    }
    else {
      remoteCreateNewSelectionWindow(channel, name, postUpdateSelected);
    }
  }
   
  /**
   * Performs the actual update the of Node Browser/Viewer panels to display a node network 
   * rooted at the given root by creating a new top-level window containing a Node 
   * Browser/Viewer pair of panels.
   *
   * @param channel 
   *   The update channel index.
   *
   * @param name
   *   The fully resolved node name.
   * 
   * @param postUpdateSelected
   *   The names of the nodes which should be selected in the NodeViewer after an update.
   */ 
  public void 
  remoteCreateNewSelectionWindow
  (
   int channel, 
   String name, 
   TreeSet<String> postUpdateSelected
  ) 
  {
    JNodeViewerPanel viewer = null;
    {
      JPanelFrame frame = createWindow();
      frame.setSize(900, 600);

      JManagerPanel mgr = frame.getManagerPanel();

      JManagerPanel left = null;
      {
        left = new JManagerPanel();
        mgr.doGroup(channel);
        JNodeBrowserPanel panel = new JNodeBrowserPanel();
        left.setContents(panel); 
        left.doGroup(channel);
      }
      
      JManagerPanel right = null;
      {    
        right = new JManagerPanel();
        viewer = new JNodeViewerPanel();
        right.setContents(viewer); 
        right.doGroup(channel);
      }
      
      mgr.setContents(new JHorzSplitPanel(left, right));
      mgr.refocusOnChildPanel();
      
      frame.validate();
      frame.repaint();
    }

    TreeSet<String> roots = new TreeSet<String>(); 
    roots.add(name);
    viewer.setRoots(roots, postUpdateSelected);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Launch the Editor associated with the given checked-in node version in response
   * to a request from plremote(1).
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param vid
   *   The revision number or <CODE>null</CODE> for latest.
   */ 
  public void
  remoteCheckedInView
  (
   String name, 
   VersionID vid
  )
    throws PipelineException 
  {
    {
      MasterMgrClient client = acquireMasterMgrClient();
      try {
        NodeVersion vsn = client.getCheckedInVersion(name, vid);
        LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Finer,
         "Viewing Checked-In Version: " + name + " (v" + vsn.getVersionID() + ")"); 

        /* Adding PackageInfo.sUser for the author matches the behavior of a user 
	   right clicking a checked-in node and selecting one of the View menu items. */
	{
	  EditTask task = new EditTask(0, vsn, null, false, PackageInfo.sUser, null, false);
	  task.start();
	}
      }
      finally {
        releaseMasterMgrClient(client);
      }
    }
  }
    
  

  /*----------------------------------------------------------------------------------------*/
  /*   P A N E L   G R O U P S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get all top level panels which share the same panel group.
   */ 
  public ArrayList<JTopLevelPanel> 
  getChannelPanels
  (
   int channel
  ) 
  {
    ArrayList<JTopLevelPanel> topPanels = new ArrayList<JTopLevelPanel>();
    if((channel > 0) && (channel < 10)) {
      JTopLevelPanel panel = null;

      panel = getNodeBrowserPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeViewerPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeDetailsPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeFilesPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeLinksPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeHistoryPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getNodeAnnotationsPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobServerStatsPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobServersPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobSlotsPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobBrowserPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobViewerPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 

      panel = getQueueJobDetailsPanels().getPanel(channel);  
      if(panel != null) 
        topPanels.add(panel); 
    }

    return topPanels; 
  }


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
   * Get the node annotations panel group.
   */ 
  public PanelGroup<JNodeAnnotationsPanel>
  getNodeAnnotationsPanels() 
  {
    return pNodeAnnotationsPanels;
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
  /*   P A N E L   C O N T R O L                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Direct a Node Browser panel on the provided channel to show a set of nodes in a 
   * particular working area.
   * 
   * @param channel
   *   The channel to find the node browser on.  If there is no Node Browser on this channel, 
   *   then this command will have no effect.
   * 
   * @param author
   *   The author whose working area the node browser will display.
   *   
   * @param view
   *   The working area the node browser will display.
   *   
   * @param nodeNames
   *   The set of node names to select in the node browser.
   */
  public void
  selectAndShowNodes
  (
    int channel,
    String author,
    String view,
    TreeSet<String> nodeNames
  )
  {
    if (channel > 0) {
      JNodeBrowserPanel panel = pNodeBrowserPanels.getPanel(channel);
      if (panel != null) {
        panel.applyPanelUpdates(author, view, nodeNames);
        PanelUpdater pu = new PanelUpdater(panel, true);
        pu.execute();
      }
    }
  }
  
  
  /**
   * Direct a job browser panel to show only a specific set of job groups.
   * 
   * @param channel
   *   The channel to find the job browser on.  If there is no Job Browser on this channel, 
   *   then this command will have no effect.
   * 
   * @param jobGroups
   *   The set of job groups to show in the Job Browser.
   */
  public void
  selectAndShowJobGroups
  (
    int channel,
    TreeSet<Long> jobGroups
  )
  {
    JQueueJobBrowserPanel panel = pQueueJobBrowserPanels.getPanel(channel);
    if (panel != null) {
      panel.setFilterOverride(jobGroups);
      PanelUpdater pu = new PanelUpdater(panel, true);
      pu.execute();
    }
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
   Throwable ex
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
   * Show a short generic message dialog. 
   */ 
  public void 
  showInfoDialog
  (
   Frame owner,  
   String msg
  ) 
  {
    SwingUtilities.invokeLater(new ShowInfoDialogTask(owner, msg));
  }

  /**
   * Show a longer generic message dialog. 
   */ 
  public void 
  showInfoDialog
  (
   Frame owner,  
   String title, 
   String msg
  ) 
  {
    SwingUtilities.invokeLater(new ShowInfoDialogTask(owner, title, msg));
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
   * Show the manage hardware keys and groups dialog.
   */ 
  public void 
  showManageHardwareKeysDialog()
  {
    pManageHardwareKeysDialog.updateAll();
    pManageHardwareKeysDialog.setVisible(true);
  } 
  
  /**
   * Show the manage dispatch controls dialog.
   */ 
  public void 
  showManageDispatchControlsDialog()
  {
    pManageDispatchControlsDialog.updateAll();
    pManageDispatchControlsDialog.setVisible(true);
  }  
  
  public void
  showManageBalanceGroupsDialog()
  {
    pManageBalanceGroupsDialog.updateAll();
    pManageBalanceGroupsDialog.setVisible(true);
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
    return showQueueJobsDialog(null, null); 
  }
  
  /**
   * Show the job submission dialog.
   * 
   * @param wholeRange
   *   The entire target frame range indexed by fully resolved node name.  Value can be
   *   <CODE>null</CODE> for single frame nodes.
   * 
   * @param targetIndices
   *   The indices into the target frame range of files to regenerate for each node.   
   *   Value can be <CODE>null</CODE> for all frames. 
   */ 
  public JQueueJobsDialog
  showQueueJobsDialog
  (
   TreeMap<String,FrameRange> wholeRanges, 
   MappedSet<String,Integer> targetIndices
  ) 
  {
    pQueueJobsDialog.updateFrameRanges(wholeRanges, targetIndices); 
    pQueueJobsDialog.updateKeys();
    pQueueJobsDialog.setVisible(true);
    return pQueueJobsDialog;
  }
  
  /**
   * Show the change job requirement submission dialog.
   */ 
  public JChangeJobReqsDialog
  showChangeJobReqDialog()
  {
    pChangeJobReqsDialog.updateKeys();
    pChangeJobReqsDialog.setVisible(true);
    return pChangeJobReqsDialog;
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
    while(true) {
      pBackupDialog.setVisible(true);
      if(pBackupDialog.wasConfirmed()) {
        Path dir = pBackupDialog.getDirectory();
        boolean withQueueMgr = pBackupDialog.withQueueMgr(); 
        boolean withPluginMgr = pBackupDialog.withPluginMgr(); 
        if((dir != null) && dir.isAbsolute()) {
          JConfirmDialog diag = 
            new JConfirmDialog
            (pFrame, "Are you sure?", 
             "WARNING: A database backup will temporarily pause all Pipeline operations " + 
             "until the backup is complete and therefore should not be performed during " +
             "normal working hours.");
          
          diag.setVisible(true);
          if(diag.wasConfirmed()) {
            BackupTask task = new BackupTask(dir, withQueueMgr, withPluginMgr);
            task.start();	
          }

          return;
        }
        else {
          showErrorDialog("Error:", 
                          "The Backup Directory must be specified as an absolute path.");
        }
      }
      else {
        return;
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
   * Show a dialog for browsing the contents of existing archive volumes.
   */ 
  public void 
  showArchiveVolumesDialog()
  {
    pArchiveVolumesDialog.setVisible(true);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show a dialog for monitoring the user's currently active job groups.
   */ 
  public void 
  showJobMonitorDialog()
  {
    if (!pJobMonitorDialog.isVisible())
      pJobMonitorDialog.setVisible(true);
    else
      pJobMonitorDialog.toFront();
  }
  
  /**
   * Add jobs to the monitor panel.
   * 
   * @param jobGroups
   *   The list of job groups to add to the panel.
   */
  public void
  monitorJobGroups
  (
    LinkedList<QueueJobGroup> jobGroups 
  )
  {
    pJobMonitorDialog.addJobGroups(jobGroups);
    UserPrefs prefs = UserPrefs.getInstance();
    if (prefs.getAutoOpenJobMonitor())
      showJobMonitorDialog();
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

  /**
   * Show the log history dialog. 
   * 
   * @param enabled
   *   Whether logging is enabled at the start.
   */ 
  public void 
  showLogsDialog
  (
   boolean enabled
  )
  {
    pLogsDialog.setEnabled(enabled);
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
  /*   P L U G I N   H E L P                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the HTML docs for the given plugin.
   */ 
  public void 
  showPluginHelp
  (
   BasePlugin plugin
  )
  {
    if(plugin == null) 
      return; 

    try {
      File file = PackageInfo.getPluginDocs(plugin).toFile();
      if(!file.exists())
	throw new PipelineException
          ("The documentation (" + file + ") for plugin " + 
           "(" + plugin.getName() + " v" + plugin.getVersionID() + ") from Vendor " + 
           "(" + plugin.getVendor() + ") does not exist!");

      URI uri = file.toURI();
      Desktop.getDesktop().browse(uri);
    }
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }

  /**
   * Whether HTML exists for the given plugin.
   */ 
  public boolean
  hasPluginHelp
  (
   BasePlugin plugin
  )
  {
    if(plugin == null) 
      return false;
    return PackageInfo.getPluginDocs(plugin).toFile().exists();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Start a non-blocking dialog operation.<P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp} with the returned message handle.
   * 
   * @return
   *   A unique handle for this operation. 
   */ 
  public long
  beginDialogOp()
  {
    return beginDialogOp("");
  }

  /**
   * Start a non-blocking dialog operation.<P> 
   * 
   * Once the operation is complete or if it is aborted early, the caller
   * of this methods must call {@link #endPanelOp} with the returned message handle.
   * 
   * @param msg
   *   A short message describing the operation.
   * 
   * @return
   *   A unique handle for this operation. 
   */ 
  public long
  beginDialogOp
  (
   String msg
  )
  {
    long opID = pNextLoggerID.incrementAndGet();

    OpLogger logger = new OpLogger();
    synchronized(pOpLoggers) {
      pOpLoggers.put(opID, logger);
    }      
    logger.beginOp(msg);

    return opID;
  }

  /**
   * Update the operation message in mid-operation.
   * 
   * @param opID
   *   A unique handle for this operation. 
   * 
   * @param msg
   *   A short message describing the operation.
   */ 
  public void
  updateDialogOp
  (
   long opID,
   String msg 
  )
  {
    OpLogger logger = pOpLoggers.get(opID);
    if(logger != null) 
      logger.updateOp(msg);
  }

  /**
   * End a non-blocking dialog operation.<P> 
   * 
   * @param opID
   *   A unique handle for this operation. 
   * 
   * @param msg
   *   A short completion message.
   */
  public void 
  endDialogOp
  (
   long opID,
   String msg 
  ) 
  {
    OpLogger logger = null;
    synchronized(pOpLoggers) {
      logger = pOpLoggers.remove(opID); 
    }

    if(logger != null) {
      logger.endOp(msg);
    } 
    else {
      throw new IllegalStateException
        ("Somehow no non-blocking dialog operation with the ID (" + opID + ") exists!"); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Try to acquire a panel operation lock. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was acquired and the operation 
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
     return beginPanelOp(channel, "");
   }

  /**
   * Try to acquire a panel operation lock and if successfully notify the user that 
   * an operation is in progress. <P> 
   * 
   * If this method returns <CODE>true</CODE> then the lock was acquired and the operation 
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
    if((channel < 1) || (channel > 9))
      throw new IllegalArgumentException
        ("The panel update channel must be in the range 1-9 inclusive."); 

    boolean acquired = false;
    {
      OpLogger logger = null;
      synchronized(pOpLoggers) {
        logger = pOpLoggers.get((long) channel);
      }      

      if(logger != null) {
        acquired = logger.tryLock();
        if(acquired)
          logger.beginOp(msg);
      }
    }

    if(UIFactory.getBeepPreference())
      Toolkit.getDefaultToolkit().beep();

    return acquired;
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
    if((channel < 1) || (channel > 9))
      throw new IllegalArgumentException
        ("The panel update channel must be in the range 1-9 inclusive."); 

    OpLogger logger = null;
    synchronized(pOpLoggers) {
      logger = pOpLoggers.get((long) channel);
    }  

    if(logger != null) 
      logger.updateOp(msg);
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
    endPanelOp(channel, "");
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
    if((channel < 1) || (channel > 9))
      throw new IllegalArgumentException
        ("The panel update channel must be in the range 1-9 inclusive."); 

    OpLogger logger = null;
    synchronized(pOpLoggers) {
      logger = pOpLoggers.get((long) channel);
    }  

    if(logger != null) 
      logger.endOp(msg); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show only the panel operation progress components associated with used channel groups.
   */ 
  public void 
  updateOpsBar() 
  {
    if(pOpLoggers == null) 
      return;

    int idx;
    for(idx=1; idx<10; idx++) {
      boolean unused = 
 	(pNodeBrowserPanels.isGroupUnused(idx) && 
 	 pNodeViewerPanels.isGroupUnused(idx) && 
 	 pNodeDetailsPanels.isGroupUnused(idx) && 
 	 pNodeHistoryPanels.isGroupUnused(idx) && 
 	 pNodeFilesPanels.isGroupUnused(idx) && 
 	 pNodeLinksPanels.isGroupUnused(idx) && 
 	 pNodeAnnotationsPanels.isGroupUnused(idx) && 
  	 pQueueJobServersPanels.isGroupUnused(idx) && 
  	 pQueueJobServerStatsPanels.isGroupUnused(idx) && 
  	 pQueueJobSlotsPanels.isGroupUnused(idx) && 
 	 pQueueJobBrowserPanels.isGroupUnused(idx) && 
 	 pQueueJobViewerPanels.isGroupUnused(idx) && 
 	 pQueueJobDetailsPanels.isGroupUnused(idx));

      if(unused) {
        OpLogger logger = null;
        synchronized(pOpLoggers) {
          logger = pOpLoggers.get((long) idx);
        }  
        
        if(logger != null) 
          logger.invisible(); 
      }
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
   * Create a new OpenGL lightweight rendering area.
   * 
   * @return 
   *   The new GLJPanel or 
   *   <CODE>null</CODE> if the Java2d OpenGL rendering pipeline is disabled.
   */ 
  public synchronized GLJPanel
  createGLJPanel() 
  {
    if(PackageInfo.sUseJava2dGLPipeline) 
      return new GLJPanel(pGLCapabilities, null, pTextureLoader.getContext());
    return null;
  }

  /**
   * Create a new OpenGL rendering canvas shared among all GLCanvas instances.
   * 
   * @return 
   *   The new GLCanvas or 
   *   <CODE>null</CODE> if the Java2d OpenGL rendering pipeline is enabled.
   */ 
  public synchronized GLCanvas
  createGLCanvas() 
  {
    if(!PackageInfo.sUseJava2dGLPipeline) 
      return new GLCanvas(pGLCapabilities, null, pTextureLoader.getContext(), null);
    return null;
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
  /*   S W I N G   U T I L I T I E S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to check for Swing thread violations. 
   */ 
  public boolean 
  getDebugSwing() 
  {
    return pDebugSwing;
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
      Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts"); 
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
      Path path = new Path(PackageInfo.getSettingsPath(), "default-layout");
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
      pRestoreSelections = false;
      SwingUtilities.invokeLater(new ResetLayoutTask());
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

	  if(choice.equals("Save & Make Default") && pLayoutPath != null)
	    doDefaultLayout();
	}
	catch(Exception ex) {}
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
    cleanupNetworkConnections();

    System.exit(0);
  }  

  /**
   * Close all open network sockets.
   */ 
  public void 
  cleanupNetworkConnections()
  {
    for(MasterMgrClient client : pMasterMgrList) {
      if(client != null)
        client.disconnect();
    }
    
    for(QueueMgrClient client : pQueueMgrList) {
      if(client != null)
        client.disconnect();
    }

    PluginMgrClient.getInstance().disconnect();

    if(pRemoteServer != null) {
      pRemoteServer.shutdown();
      try {
        pRemoteServer.join();
      }
      catch(InterruptedException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Warning,
           "Interrupted while waiting for RemoteServer connection to shutdown!"); 
      }
    }

    /* give the sockets time to disconnect cleanly */ 
    try {
      Thread.sleep(1000);
    }
    catch(InterruptedException ex) {
    }
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

    @SuppressWarnings("unchecked")
    @Override
    public void 
    run() 
    {  
      try {
	Path base = PackageInfo.getSettingsPath();

	/* create an intial layout (if none exists) and make it the default */ 
	{
	  Path lpath = new Path(base, "layouts");
	  File dir = lpath.toFile(); 
	  if(!dir.isDirectory()) {
	    if(!dir.mkdirs()) {
	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Ops, LogMgr.Level.Severe,
		 "Unable to create (" + dir + ")!");
	      System.exit(1);	    
	    }
	    
	    MasterMgrClient client = acquireMasterMgrClient();
	    try {
	      client.createInitialPanelLayout
	        ("Default", PackageInfo.sUser, "default");
	    }
	    finally {
	      releaseMasterMgrClient(client);
	    }

	    try {
	      Path dpath = new Path(base, "default-layout");
	      LockedGlueFile.save(dpath.toFile(), "DefaultLayout", "/Default");
	    }
	    catch(Exception ex) {
	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Ops, LogMgr.Level.Warning,
		 "Unable to set the initial default layout!");
	    }    
	  }	  
	}
	
	/* make sure user preference exist */ 
	{
	  Path path = new Path(base, "preferences");
	  if(path.toFile().isFile()) 	  
	    UserPrefs.load();
	    
	    /* Set the beep preference */
	    UIFactory.setBeepPreference(UserPrefs.getInstance().getBeep());
	}
      }
      catch(Exception ex) {	
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Unable to initialize the user preferences!\n" + 
	   "  " + ex.getMessage());
	System.exit(1);	 
      }

      /* make sure that the default working area exists */
      MasterMgrClient client = acquireMasterMgrClient();
      try {
	client.createWorkingArea(PackageInfo.sUser, "default");
      }
      catch(PipelineException ex) {	
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Unable to initialize the default working area!\n" + 
	   "  " + ex.getMessage());
	System.exit(1);	 
      }
      finally {
        releaseMasterMgrClient(client);
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
      if(!PackageInfo.sUseJava2dGLPipeline) {
	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
      }
      
      /* create an offscreen pbuffer to load the textures */ 
      {
        pGLCapabilities = new GLCapabilities();  
        pGLCapabilities.setDoubleBuffered(true);

        pTextureLoader = 
          new TextureLoader(pUsePBuffers, pGLCapabilities, new MainFrameTask(pMaster));
      }

      /* create the restore layout splash screen */ 
      {
	JFrame frame = new JFrame("plui");
	pRestoreSplashFrame = frame;

        if(pDebugSwing) 
          RepaintManager.setCurrentManager(new ThreadingDebugRepaintManager());

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

    @Override
    public void 
    run() 
    {
      /* create and show the main application frame */ 
      {
	JFrame frame = new JFrame("plui");
	pFrame = frame;

        if(pDebugSwing) 
          RepaintManager.setCurrentManager(new ThreadingDebugRepaintManager());

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
	      int idx;
	      for(idx=1; idx<10; idx++) 
                pOpLoggers.put((long) idx, new OpLogger(idx));
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
	pManageHardwareKeysDialog  = new JManageHardwareKeysDialog();
	pManageDispatchControlsDialog = new JManageDispatchControlsDialog();
	pManageBalanceGroupsDialog = new JManageBalanceGroupsDialog();

	pQueueJobsDialog = new JQueueJobsDialog(pFrame);
	pChangeJobReqsDialog = new JChangeJobReqsDialog(pFrame);
	
	pSubProcessFailureDialog = new JSubProcessFailureDialog(pFrame);

	pManageServerExtensionsDialog = new JManageServerExtensionsDialog();

	pBackupDialog = new JBackupDialog(pFrame); 

	pArchiveDialog        = new JArchiveDialog();
	pOfflineDialog        = new JOfflineDialog();
	pRestoreDialog        = new JRestoreDialog();
	pArchiveVolumesDialog = new JArchiveVolumesDialog();
	
	pJobMonitorDialog     = new JJobMonitorDialog();

	{
	  ArrayList<LogMgr.Kind> kinds = new ArrayList<LogMgr.Kind>();
	  kinds.add(LogMgr.Kind.Arg);
	  kinds.add(LogMgr.Kind.Ops);
	  kinds.add(LogMgr.Kind.Bld);
	  kinds.add(LogMgr.Kind.Net);
	  kinds.add(LogMgr.Kind.Sub);

	  pLogsDialog = new JLogsDialog(kinds);
	}

	pResourceUsageHistoryDialog = new JResourceUsageHistoryDialog();

        pWorkingSelectDialog = new JWorkingSelectDialog(pFrame);
        
	JToolDialog.initRootFrame(pFrame);
      }

      /* hide any onscreen windows created during the texture loading process. */ 
      pTextureLoader.hide(); 

      {
	Path layoutPath = null;
	if(pRestoreLayout) {
	  layoutPath = pOverrideLayoutPath;
	  if(layoutPath == null) {
	    try {
	      Path path = new Path(PackageInfo.getSettingsPath(), "default-layout"); 
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

      if(pRemoteServer != null) 
        pRemoteServer.start();
    }

    private UIMaster  pMaster;
  }


  /*----------------------------------------------------------------------------------------*/
  
  private 
  class OpLogger
  {
    /** 
     * Create a new non-blocking dialog operation logger.
     */
    public 
    OpLogger()
    {
      this(0); 
    }

    /** 
     * Create a new blocking panel operation logger.
     * 
     * @param channel
     *   The channel icon number to display.
     */
    public 
    OpLogger
    (
     int channel
    ) 
    {
      if((channel < 0) || (channel > 9)) 
        throw new IllegalArgumentException
        ("The panel update channel must be in the range 0-9 inclusive."); 

      if(channel > 0) 
        pOpLock = new ReentrantLock();
      pChannel = channel;

      Box hbox = new Box(BoxLayout.X_AXIS);   
      pProgressBox = hbox; 
      hbox.setVisible(false);
      
      hbox.add(Box.createRigidArea(new Dimension(6, 0)));
      
      {
        JLabel label = new JLabel(sProgressFinishedIcons[channel]);
        pProgressLight = label;
	
        Dimension size = new Dimension(19, 19);
        label.setMinimumSize(size);
        label.setMaximumSize(size);
        label.setPreferredSize(size);
	
        hbox.add(label);
      }
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      
      {
        JTextField field = UIFactory.createTextField(null, 30, JLabel.LEFT);
        pProgressField = field;
	
        hbox.add(field);
      }
      
      hbox.add(Box.createRigidArea(new Dimension(6, 0)));
      
      pProgressPanel.add(hbox);
    }


    /*-- LOCKING METHODS -------------------------------------------------------------------*/

    /**
     * Try to acquire the operation lock (if any exists).
     */ 
    public synchronized boolean
    tryLock()
    {
      if(pOpLock != null) 
        return pOpLock.tryLock();  
      return true;
    }


    /*-- OPS -------------------------------------------------------------------------------*/

    /**
     * Mark the start of a logged operation displaying the given message.
     */ 
    public synchronized void 
    beginOp
    (
     String msg
    )
    {
      pMessage = msg;
      pTimer = new TaskTimer();
      pIsRunning = true;
      SwingUtilities.invokeLater(new BeginOpsTask(this));
    }
     
    /**
     * Update the operation message in mid-operation.
     */ 
    public synchronized void 
    updateOp
    (
     String msg
    )
    {
      if((pOpLock != null) && !pOpLock.isLocked())
        throw new IllegalStateException
          ("Somehow an update message was given when the channel was NOT locked!"); 

      pMessage = msg;
      SwingUtilities.invokeLater(new UpdateOpsTask(this));
    }

    /**
     * Mark the end of a logged operation displaying the given message.
     */ 
    public synchronized void 
    endOp
    (
     String msg
    )
    {
      if(pTimer != null) {
        pTimer.suspend();
        pMessage = 
          (msg + "   (" + TimeStamps.formatInterval(pTimer.getActiveDuration()) + ")");
      }
      else {
        pMessage = msg;
      }
      
      pIsRunning = false;
      
      try {
        if(pOpLock != null) 
          pOpLock.unlock();  
      }
      catch(IllegalMonitorStateException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Internal Error:\n" + 
           "  " + ex.getMessage());
      }

      SwingUtilities.invokeLater(new EndOpsTask(this)); 
    }
    

    /*-- UI METHODS ------------------------------------------------------------------------*/

    public synchronized void 
    invisible()
    {
      pProgressBox.setVisible(false);
    }
     
    public synchronized void 
    beginUI()
    {
      pProgressLight.setIcon(sProgressRunningIcons[pChannel]); 
      pProgressField.setText(pMessage);
      
      if(!pProgressBox.isVisible()) {
        pNoProgressBox.setVisible(false); 
	pProgressBox.setVisible(true);
	pProgressPanel.revalidate();
	pProgressPanel.repaint();
      }

      if(pChannel > 0) {
        ArrayList<JTopLevelPanel> panels = getChannelPanels(pChannel); 
        for(JTopLevelPanel panel : panels) 
          panel.prePanelOp();
      }
    }

    public synchronized void 
    updateUI()
    {
      pProgressField.setText(pMessage);
      pProgressPanel.repaint();
    }

    public synchronized void 
    endUI() 
    {
      if(pChannel > 0) {
        ArrayList<JTopLevelPanel> panels = getChannelPanels(pChannel); 
        for(JTopLevelPanel panel : panels) 
          panel.postPanelOp();
      }

      pProgressField.setText(pMessage); 
      pProgressLight.setIcon(sProgressFinishedIcons[pChannel]); 

      if(!pIsRunning) {
        WaitHideOpsTask task = new WaitHideOpsTask(this); 
	task.start();
      }
    }
     
    public synchronized void 
    hideUI()
    {
      if(!pIsRunning && pProgressBox.isVisible()) {
	if(pChannel > 0) 
          pProgressBox.setVisible(false);
        else 
          pProgressPanel.remove(pProgressBox); 

	pProgressPanel.revalidate();
	pProgressPanel.repaint();
      }
    }
     
    private int           pChannel;
    private ReentrantLock pOpLock; 
    private String        pMessage; 
    private TaskTimer     pTimer;
    private boolean       pIsRunning;

    private Box        pProgressBox;                                
    private JLabel     pProgressLight;
    private JTextField pProgressField;
  }

  /**
   * Base class for all panel operation logging tasks.
   */ 
  private
  class BaseOpsTask
    extends Thread
  { 
    BaseOpsTask
    ( 
     String title,
     OpLogger logger 
    ) 
    {
      super("UIMaster:" + title + "OpsTask");
      pLogger = logger;
    }

    protected OpLogger pLogger; 
  }

  /**
   * Notify the user that a panel operation is underway.
   */ 
  private
  class BeginOpsTask
    extends BaseOpsTask
  { 
    BeginOpsTask
    ( 
     OpLogger logger 
    ) 
    {
      super("Begin", logger);
    }

    @Override
    public void 
    run() 
    { 
      pLogger.beginUI();
    }
  }

  /* 
   * Update the operation message.
   */ 
  private
  class UpdateOpsTask
    extends BaseOpsTask
  { 
    UpdateOpsTask
    ( 
     OpLogger logger 
    ) 
    {
      super("Update", logger);
    }

    @Override
    public void 
    run() 
    {
      pLogger.updateUI();
    }
  }

  /**
   * Notify the user that a panel operation is finished.
   */ 
  private
  class EndOpsTask
    extends BaseOpsTask
  { 
    EndOpsTask
    ( 
     OpLogger logger
    ) 
    {
      super("End", logger);
    }

    @Override
    public void 
    run() 
    {
      pLogger.endUI();
    }
  }

  /**
   * Wait a few seconds before hiding the progress box.
   */ 
  private
  class WaitHideOpsTask
    extends BaseOpsTask
  { 
    WaitHideOpsTask
    ( 
     OpLogger logger 
    ) 
    {
      super("WaitHide", logger);
    }

    @Override
    public void 
    run() 
    {
      try {
	sleep(5000);
      }
      catch(InterruptedException ex) {
      }
      
      SwingUtilities.invokeLater(new HideOpsTask(pLogger)); 
    }
  }
  
  /**
   * Hide the progress box if no operations are currently running.
   */ 
  private
  class HideOpsTask
    extends BaseOpsTask
  { 
    HideOpsTask
    ( 
     OpLogger logger 
    ) 
    {
      super("Hide", logger);
    }

    @Override
    public void 
    run() 
    {
      pLogger.hideUI();
    }
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

    @Override
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

    @Override
    public void 
    run() 
    {
      try {
	Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts"); 
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
   * Reset the current panels with the site default layout.  This layout is not saved.
   */
  private
  class ResetLayoutTask
    extends Thread
  {
    public
    ResetLayoutTask()
    {
      super("UIMaster:ResetLayoutTask");
    }

    @Override
    public void
    run()
    {
      String layoutContents = null;
      {
	MasterMgrClient client = acquireMasterMgrClient();
	try {
	  layoutContents = client.getInitialPanelLayout
	    (PackageInfo.sUser, "default");

	  if(layoutContents == null)
	    throw new PipelineException("Unable to load the site default layout!");

	  SwingUtilities.invokeLater(new RestoreSavedLayoutTask(layoutContents));
	}
	catch(Exception ex) {
	  showErrorDialog(ex);
	}    
	finally {
	  releaseMasterMgrClient(client);
	}
      }
    }
  }

  /**
   * Replace the current panels with those stored in the stored layout with the given name 
     or those stored in a GLUE string.
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

    public 
    RestoreSavedLayoutTask
    (
     String layoutContents
    ) 
    {
      super("UIMaster:RestoreSavedLayoutTask");

      pLayoutContents = layoutContents;
    }

    @Override
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

	pNodeAnnotationsPanels.clear();
	
 	pQueueJobServersPanels.clear();
 	pQueueJobServerStatsPanels.clear();
 	pQueueJobSlotsPanels.clear();
	pQueueJobBrowserPanels.clear();
	pQueueJobViewerPanels.clear();
	pQueueJobDetailsPanels.clear();
      }
      
      /* show the splash screen */ 
      pRestoreSplashFrame.setVisible(true);
      
      SwingUtilities.invokeLater(new RestoreSavedLayoutRefreshTask(pPath, pLayoutContents));
    }

    private Path    pPath; 
    private String  pLayoutContents;
  }

  private 
  class RestoreSavedLayoutRefreshTask
    extends Thread
  {
    public 
    RestoreSavedLayoutRefreshTask
    (
     Path path, 
     String layoutContents
    ) 
    {
      super("UIMaster:RestoreSavedLayoutRefreshTask");
      
      pPath = path; 
      pLayoutContents = layoutContents;
    }

    @Override
    public void 
    run() 
    {
      try {
	sleep(100);
      }
      catch(InterruptedException ex) {
      }

      SwingUtilities.invokeLater(new RestoreSavedLayoutLoaderTask(pPath, pLayoutContents));
    }

    private Path    pPath;
    private String  pLayoutContents;
  }

  private 
  class RestoreSavedLayoutLoaderTask
    extends Thread
  {
    public 
    RestoreSavedLayoutLoaderTask
    (
     Path path, 
     String layoutContents
    ) 
    {
      super("UIMaster:RestoreSavedLayoutLoaderTask");
      
      pPath = path;
      pLayoutContents = layoutContents;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    run() 
    {
      /* restore saved panels */
      LinkedList<PanelLayout> layouts = null;
      {
	pIsRestoring.set(true);
	getUICache(0).invalidateCaches();
	
	if(pLayoutContents != null) {
	  try {
	    layouts = (LinkedList<PanelLayout>) GlueDecoderImpl.decodeString
	      ("SiteDefaultLayout", pLayoutContents);
	  }
	  catch(GlueException ex) {
	    showErrorDialog("Error:", "Unable to load the default layout!");
	  }
	  catch(Exception ex) {
	    showErrorDialog(ex);
	  }
	}
	else {
	  Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts"); 
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
	    
            if(pDebugSwing) 
              RepaintManager.setCurrentManager(new ThreadingDebugRepaintManager());

	    frame.setBounds(layout.getBounds());
	    frame.setManagerPanel(mpanel);
	    frame.setWindowName(layout.getName());
	    
	    pPanelFrames.add(frame);
	    
	    frames.add(frame);
	  }
	}
      }
      else {
	JManagerPanel mpanel = new JManagerPanel();
	mpanel.setContents(new JEmptyPanel()); 
	
	pRootPanel.add(mpanel);
	pRootPanel.validate();
	pRootPanel.repaint();
	
	frames.add(pFrame);
      }

      setLayoutPath(pPath);

      /* hide the splash screen */ 
      pRestoreSplashFrame.setVisible(false);
      
      /* show the restored windows */ 
      for(JFrame frame : frames) 
	frame.setVisible(true);
    }

    private Path    pPath;
    private String  pLayoutContents;
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

    @Override
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

    @Override
    public void 
    run() 
    {
      try {
	/* Set the beep preference */
	UIFactory.setBeepPreference(UserPrefs.getInstance().getBeep());

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
	pManageHardwareKeysDialog.updateUserPrefs();
	pManageDispatchControlsDialog.updateUserPrefs();
	pManageBalanceGroupsDialog.updateUserPrefs();

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
   * Show an generic message dialog. 
   * 
   * The reason for the thread wrapper is to allow the rest of the UI to repaint before
   * showing the dialog.
   */ 
  private
  class ShowInfoDialogTask
    extends Thread
  { 
    public 
    ShowInfoDialogTask
    (
     Frame owner,  
     String msg
    ) 
    {
      this(owner, msg, null); 
    }

    public 
    ShowInfoDialogTask
    (
     Frame owner,  
     String title, 
     String msg
    ) 
    {
      super("UIMaster:ShowInfoDialogTask");
      
      pMessageOwner = owner;
      pMessageTitle = title; 
      pMessageText = msg;
    }

    @Override
    public void 
    run() 
    {
      JInfoDialog diag = new JInfoDialog(pMessageOwner, pMessageTitle, pMessageText);
      diag.setVisible(true);
    }

    private Frame  pMessageOwner;
    private String pMessageTitle;
    private String pMessageText;
  }
  
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

    @Override
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

    @Override
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
     TreeSet<FileSeq> selectSeqs, 
     boolean useDefault,
     String author, 
     String view, 
     boolean substitute
    ) 
    {
      super("UIMaster:EditTask", channel, author, view);

      pNodeCommon = com;
      pSelectSeqs = selectSeqs; 
      pUseDefault = useDefault;
      pSubstitute = substitute;
    }

    public 
    EditTask
    (
     int channel, 
     NodeCommon com, 
     TreeSet<FileSeq> selectSeqs, 
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
      pSelectSeqs    = selectSeqs; 
      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor; 
      pSubstitute    = substitute;
    }


    @Override
    @SuppressWarnings("deprecation")
    public void 
    run() 
    {
      LinkedList<SubProcessLight> procs = new LinkedList<SubProcessLight>();
      Long editID = null;
      {
	UIMaster master = UIMaster.getInstance();
        boolean ignoreExitCode = false;
        if((pChannel == 0) || master.beginPanelOp(pChannel, "Launching Node Editor...")) {
	  MasterMgrClient client = master.acquireMasterMgrClient();
	  try {

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

            /* test whether editing should be allowed */ 
            NodeID nodeID = null; 
            if(mod != null) {
              nodeID = new NodeID(pAuthorName, pViewName, pNodeCommon.getName());
              client.editingTest(nodeID, editor.getPluginID()); 
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
	    
	    /* get the file sequences to edit */ 
	    TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>(); 
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

	      dir = path.toFile();
	  
              if(pSelectSeqs != null) {
                for(FileSeq target : pSelectSeqs) 
                  fseqs.add(new FileSeq(path.toString(), target));
              }
              else {
                FileSeq target = pNodeCommon.getPrimarySequence();
                fseqs.add(new FileSeq(path.toString(), target));
              }
	    }

            if(pSubstitute && (pChannel > 0)) {
              PrivilegeDetails details = getUICache(pChannel).getCachedPrivilegeDetails();
              if(!details.isNodeManaged(pAuthorName)) 
                throw new PipelineException
                  ("You do not have the necessary privileges to execute an editor as " + 
                   "the (" + pAuthorName + ") user!");
            }

	    /* start the editor(s) */ 
            for(FileSeq fseq : fseqs) {
              if(pSubstitute) {
                EditAsTask task = new EditAsTask(editor, pAuthorName, fseq, env, dir);
                task.start();
              }
              else {
                editor.makeWorkingDirs(dir);
                SubProcessLight proc = editor.prep(PackageInfo.sUser, fseq, env, dir);
                if(proc != null) 
                  proc.start();
                else 
                  proc = editor.launch(fseq, env, dir);
                procs.add(proc); 
              }
            }

	    /* generate the editing started event */ 
	    if(mod != null) 
	      editID = client.editingStarted(nodeID, editor);
	  }
	  catch(IllegalArgumentException ex) {
	    master.showErrorDialog("Error:", ex.getMessage());	    
	    return;
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  catch (LinkageError er) {
	    master.showErrorDialog(er);
            return;
	  }
	  finally {
	    master.releaseMasterMgrClient(client);
	    if(pChannel > 0) 
              master.endPanelOp(pChannel, "Done.");
	  }
	}

	/* wait for the editor(s) to exit */ 
	if(!procs.isEmpty()) {
          for(SubProcessLight proc : procs) {
            try {
              proc.join();
              if(!proc.wasSuccessful() && !ignoreExitCode) 
                master.showSubprocessFailureDialog("Editor Failure:", proc);

              MasterMgrClient client = acquireMasterMgrClient();
              try {
                if((client != null) && (editID != null))
                  client.editingFinished(editID);
              }
              finally {
                releaseMasterMgrClient(client);
              }
            }
            catch(Exception ex) {
              master.showErrorDialog(ex);
            }
          }
        }
      }
    }
 
    private NodeCommon        pNodeCommon; 
    private TreeSet<FileSeq>  pSelectSeqs;
    private boolean           pUseDefault; 
    private String            pEditorName;
    private VersionID         pEditorVersion; 
    private String            pEditorVendor; 
    private boolean           pSubstitute; 
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

    @Override
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
     TreeSet<Integer> indices, 
     Integer batchSize, 
     Integer priority, 
     Integer interval,
     Float maxLoad,              
     Long minMemory,              
     Long minDisk, 
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys,
     TreeSet<String> hardwareKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", channel, author, view);

      pIndices = new MappedSet<String,Integer>(); 
      pIndices.put(name, indices); 

      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pMaxLoad       = maxLoad;
      pMinMemory     = minMemory;
      pMinDisk       = minDisk;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys; 
      pHardwareKeys  = hardwareKeys;
    }

    public 
    QueueJobsTask
    (
     int channel, 
     MappedSet<String,Integer> indices,
     String author, 
     String view, 
     Integer batchSize, 
     Integer priority, 
     Integer interval,
     Float maxLoad,              
     Long minMemory,              
     Long minDisk,  
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys,
     TreeSet<String> hardwareKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", channel, author, view);

      pIndices       = indices;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pMaxLoad       = maxLoad;
      pMinMemory     = minMemory;
      pMinDisk       = minDisk;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys; 
      pHardwareKeys  = hardwareKeys;
    }

    @Override
    public void 
    run() 
    {
      StringBuilder buf = new StringBuilder();
      boolean errors = false;

      MasterMgrClient client = acquireMasterMgrClient();
      if(beginPanelOp(pChannel)) {
	try {
	  for(String name : pIndices.keySet()) {
	    updatePanelOp(pChannel, "Submitting Jobs to the Queue: " + name);
            try {
              LinkedList<QueueJobGroup> group = 
                client.submitJobs(pAuthorName, pViewName, name, 
                                  pIndices.get(name), pBatchSize, pPriority, pRampUp,
                                  pMaxLoad, pMinMemory, pMinDisk,
                                  pSelectionKeys, pLicenseKeys, pHardwareKeys);
              monitorJobGroups(group);
            }
            catch(PipelineException ex) {
              buf.append(ex.getMessage() + "\n\n"); 
              errors = true;
            }
          }            
	}
	finally {
	  releaseMasterMgrClient(client);
	  endPanelOp(pChannel, "Done.");
	}

        if(errors) 
	  showErrorDialog("Warning:", buf.toString());

	postOp();
      }
    }

    private MappedSet<String,Integer>  pIndices; 

    private Integer          pBatchSize;
    private Integer          pPriority;
    private Integer          pRampUp; 
    private Float            pMaxLoad;        
    private Long             pMinMemory;              
    private Long             pMinDisk;
    private TreeSet<String>  pSelectionKeys;
    private TreeSet<String>  pLicenseKeys;
    private TreeSet<String>  pHardwareKeys;
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

    @Override
    public void 
    run() 
    {
      if(beginPanelOp(pChannel)) {
        QueueMgrClient client = acquireQueueMgrClient();
	try {
	  if((pNodeIDs != null) && !pNodeIDs.isEmpty()) {
            for(NodeID nodeID : pNodeIDs) {
              updatePanelOp(pChannel, pMsg + " Jobs for Node: " + nodeID.getName());
              performNodeOp(client, nodeID); 
            }
          }

	  if((pJobIDs != null) && !pJobIDs.isEmpty()) {
            updatePanelOp(pChannel, pMsg + " Jobs...");
            performJobOps(client, pJobIDs); 
          }
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	  return;
	}
	finally {
	  releaseQueueMgrClient(client);
	  endPanelOp(pChannel, "Done.");
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
   * Vouch for the working area files associated with the given nodes.
   */ 
  public 
  class VouchTask
    extends BaseNodeTask
  {
    public 
    VouchTask
    (
     int channel, 
     String name,
     String author, 
     String view
    ) 
    {
      super("UIMaster:VouchTask", channel, author, view);

      pNames = new TreeSet<String>();
      pNames.add(name);
    }

    public 
    VouchTask
    (
     int channel, 
     TreeSet<String> names,
     String author, 
     String view
    ) 
    {
      super("UIMaster:VouchTask", channel, author, view);

      pNames = names; 
    }

    @Override
    public void 
    run() 
    {
      if(beginPanelOp(pChannel)) {
        MasterMgrClient client = acquireMasterMgrClient();
	try {
	  for(String name : pNames) {
	    updatePanelOp(pChannel, "Vouching for: " + name);
	    client.vouch(pAuthorName, pViewName, name);
	  }
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	  return;
	}
	finally {
	  releaseMasterMgrClient(client);
	  endPanelOp(pChannel, "Done.");
	}

	postOp();
      }
    }

    private TreeSet<String>  pNames; 
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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pChannel)) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  for(String name : pNames) {
	    master.updatePanelOp(pChannel, "Removing Files: " + name);
	    client.removeFiles(pAuthorName, pViewName, name, null);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
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
     Path dir, 
     boolean withQueueMgr, 
     boolean withPluginMgr 
    ) 
    {
      super("UIMaster:BackupTask");

      pBackupDir     = dir;
      pWithQueueMgr  = withQueueMgr;
      pWithPluginMgr = withPluginMgr;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      long opID = master.beginDialogOp("Database Backup...");
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        client.backupDatabase(pBackupDir, pWithQueueMgr, pWithPluginMgr);
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
        return;
      }
      finally {
        master.releaseMasterMgrClient(client);
        master.endDialogOp(opID, "Done.");
      }
    }

    private Path    pBackupDir; 
    private boolean pWithQueueMgr; 
    private boolean pWithPluginMgr; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Show the remote working node selection dialog. 
   */ 
  public 
  class ShowRemoteWorkingSelectDialogTask
    extends Thread
  {
    public 
    ShowRemoteWorkingSelectDialogTask
    (
     int channel, 
     String name, 
     TreeSet<String> postUpdateSelected
    ) 
    {
      super("UIMaster:ShowRemoteWorkingSelectDialogTask");

      pChannel = channel;
      pNodeName = name;
      pPostUpdateSelected = postUpdateSelected;
    }

    @Override
    public void 
    run() 
    {
     int idx;

     TreeSet<Integer> browserChannels = new TreeSet<Integer>();
     for(idx=1; idx<10; idx++) {
       if(!pNodeBrowserPanels.isGroupUnused(idx))
         browserChannels.add(idx);
     }

     TreeSet<Integer> viewerChannels = new TreeSet<Integer>();
     for(idx=1; idx<10; idx++) {
       if(!pNodeViewerPanels.isGroupUnused(idx))
         viewerChannels.add(idx);
     }

     pWorkingSelectDialog.updateSelection
       (pChannel, pNodeName, pPostUpdateSelected, browserChannels, viewerChannels);

      pWorkingSelectDialog.setVisible(true);	
    }

    private int              pChannel; 
    private String           pNodeName; 
    private TreeSet<String>  pPostUpdateSelected; 
  }

  /** 
   * Perform the remote working node selection. 
   */ 
  public 
  class RemoteWorkingSelectTask
    extends Thread
  {
    public 
    RemoteWorkingSelectTask
    (
     int channel, 
     String name, 
     boolean replace, 
     TreeSet<String> postUpdateSelected
    ) 
    {
      super("UIMaster:RemoteWorkingSelectTask");

      pChannel = channel;
      pNodeName = name;
      pReplace = replace; 
      pPostUpdateSelected = postUpdateSelected;
    }

    @Override
    public void 
    run() 
    {
      boolean hasBrowser = !pNodeBrowserPanels.isGroupUnused(pChannel);
      boolean hasViewer  = !pNodeViewerPanels.isGroupUnused(pChannel);

      if(!hasBrowser && !hasViewer) {
        remoteCreateNewSelectionWindow(pChannel, pNodeName, pPostUpdateSelected);
      }
      else if(hasBrowser && hasViewer) {
        if(pReplace) 
          remoteReplaceSelection(pChannel, pNodeName, pPostUpdateSelected);
        else 
          remoteAddSelection(pChannel, pNodeName, pPostUpdateSelected);
      }
      else { 
        /* nothing valid, have to show dialog... */ 
        Thread task = 
          new ShowRemoteWorkingSelectDialogTask(pChannel, pNodeName, pPostUpdateSelected);
        SwingUtilities.invokeLater(task); 
      }
    }

    private int              pChannel; 
    private String           pNodeName; 
    private boolean          pReplace; 
    private TreeSet<String>  pPostUpdateSelected; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * A replacement RepaintManager which checks the call stack to insure that non-event
   * threads are not making Swing calls.
   */ 
  public class 
  ThreadingDebugRepaintManager
    extends RepaintManager 
  {
    public
    ThreadingDebugRepaintManager() 
    {
      super();
    }
                              
    public synchronized void 
    addInvalidComponent
    (
      JComponent component
    ) 
    {
      checkThreadViolations(component);
      super.addInvalidComponent(component);
    }

    public void 
    addDirtyRegion
    (
     JComponent component, 
     int x, 
     int y, 
     int w, 
     int h
    )
    {
      checkThreadViolations(component);
      super.addDirtyRegion(component, x, y, w, h);
    }

    private void 
    checkThreadViolations
    (
     JComponent c
    ) 
    {
      if(!SwingUtilities.isEventDispatchThread()) {
        Exception ex = new Exception();

        boolean repaint = false;
        boolean fromSwing = false;
        for(StackTraceElement st : ex.getStackTrace()) {
          if(repaint && st.getClassName().startsWith("javax.swing.")) 
            fromSwing = true;

          if("repaint".equals(st.getMethodName())) 
            repaint = true;
        }

        /* no problems here, since repaint() is thread safe */ 
        if(repaint && !fromSwing) 
          return;

        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Warning,
           Exceptions.getFullMessage
           ("Detected call to Swing from outside the Event thread:", ex)); 
      }
    }
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
   * A pool of inactive connections to the master manager daemon: <B>plmaster<B>(1). <P> 
   * 
   * This field should not be access directly.  Instead a master manager connection should 
   * be obtained with the {@link #leaseMasterMgrClient leaseMasterMgrClient} method and 
   * returned to the inactive pool with {@link #returnMasterMgrClient returnMasterMgrClient}.
   */ 
  private Stack<MasterMgrClient>  pMasterMgrClientStack;
  
  /**
   * A list of all the master manager connections that have ever been handed out.
   * <p>
   * This data structure should NEVER be used for anything except disconnecting all the
   * servers
   */
  private LinkedList<MasterMgrClient> pMasterMgrList;
  
  /**
   * A pool of inactive connections to the queue manager daemon: <B>plqueuemgr<B>(1). <P> 
   * 
   * This field should not be access directly.  Instead a queue manager connection should 
   * be obtained with the {@link #leaseQueueMgrClient leaseQueueMgrClient} method and 
   * returned to the inactive pool with {@link #returnQueueMgrClient returnQueueMgrClient}.
   */ 
  private Stack<QueueMgrClient>  pQueueMgrClientStack;

  /**
   * A list of all the queue manager connections that have ever been handed out.
   * <p>
   * This data structure should NEVER be used for anything except disconnecting all the
   * servers
   */
  private LinkedList<QueueMgrClient> pQueueMgrList;

  /**
   * The remote control server thread or <CODE>null</CODE> if disabled.
   */ 
  private RemoteServer  pRemoteServer;
  
  private TreeMap<Integer, UICache> pUICaches;


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
   * Whether to attempt to load textures offscreen using OpenGL pbuffers.
   */
  private boolean pUsePBuffers; 

  /** 
   * The OpenGL capabilities used by all OpenGL drawable instances.
   */ 
  private GLCapabilities  pGLCapabilities;

  /**
   * Manages loading the shared textures and display lists used by all OpenGL drawables. 
   */ 
  private TextureLoader pTextureLoader; 

  /**
   * The OpenGL display list handles which have been previously allocated with glGenLists() 
   * but are no longer being used by any instance. <P> 
   * 
   * These display list handles are collected when objects which create OpenGL display 
   * lists are garbage collected.  The display lists where all created in the same OpenGL
   * context as pTexLoader.getContext(). <P> 
   */ 
  private TreeSet<Integer>  pDisplayLists; 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to check for Swing thread violations. 
   */ 
  private boolean pDebugSwing;


  /*----------------------------------------------------------------------------------------*/

 
  /**
   * The splash screen frame.
   */ 
  //  private JFrame  pSplashFrame;

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
   * The top-level progress message container.
   */ 
  private JPanel  pProgressPanel; 

  /**
   * The channel progress message containiers.
   */ 
  private Box pNoProgressBox;

  /**
   * The existing loggers indexed by panel operation ID.
   */ 
  private TreeMap<Long, OpLogger>  pOpLoggers;

  /**
   * The next unique panel operation ID to give out.
   */                                                
  private AtomicLong  pNextLoggerID;



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
  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pEditorPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pComparatorPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pActionPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pToolPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pArchiverPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pMasterExtPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pQueueExtPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pAnnotationPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pKeyChooserPlugins;

  private TreeMap<String,TripleMap<String,String,VersionID,TreeSet<OsType>>>  
            pBuilderCollectionPlugins;
  
  
  /**
   * Cache of plugins specific options, indexed by name, vendor, and revision number.
   */
  private TripleMap<String,String,VersionID,LayoutGroup> pBuilderLayoutGroups;

  /** 
   * Caches of annotation plugin related extra information indexed by plugin 
   * name, vendor, and revision number.  The pAnnotationExtrasLock to be used to 
   * synchronize access to these fields.
   */ 
  private Object pAnnotationExtrasLock;           
  private TripleMap<String,String,VersionID,AnnotationPermissions> pAnnotationPermissions;
  private TripleMap<String,String,VersionID,TreeSet<AnnotationContext>> pAnnotationContexts;


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
  private TreeMap<String,PluginMenuLayout>  pAnnotationLayouts; 
  private TreeMap<String,PluginMenuLayout>  pKeyChooserLayouts;
  private TreeMap<String,PluginMenuLayout>  pBuilderCollectionLayouts;




  /*----------------------------------------------------------------------------------------*/

  /**
   * The active top level panels.
   */ 
  private PanelGroup<JNodeBrowserPanel> pNodeBrowserPanels;
  private PanelGroup<JNodeViewerPanel>  pNodeViewerPanels;

  private PanelGroup<JNodeDetailsPanel>  pNodeDetailsPanels;
  private PanelGroup<JNodeHistoryPanel>  pNodeHistoryPanels;
  private PanelGroup<JNodeFilesPanel>    pNodeFilesPanels;
  private PanelGroup<JNodeLinksPanel>    pNodeLinksPanels;

  private PanelGroup<JNodeAnnotationsPanel>  pNodeAnnotationsPanels;

  private PanelGroup<JQueueJobServersPanel>      pQueueJobServersPanels;
  private PanelGroup<JQueueJobServerStatsPanel>  pQueueJobServerStatsPanels;
  private PanelGroup<JQueueJobSlotsPanel>        pQueueJobSlotsPanels;

  private PanelGroup<JQueueJobBrowserPanel>  pQueueJobBrowserPanels;
  private PanelGroup<JQueueJobViewerPanel>   pQueueJobViewerPanels;
  private PanelGroup<JQueueJobDetailsPanel>  pQueueJobDetailsPanels;

  

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
   * The manage hardware keys and groups dialog.
   */ 
  private JManageHardwareKeysDialog  pManageHardwareKeysDialog;
  
  /**
   * The manage dispatch controls dialog.
   */ 
  private JManageDispatchControlsDialog pManageDispatchControlsDialog;
  
  /**
   * The manage balance controls dialog.
   */
  private JManageBalanceGroupsDialog pManageBalanceGroupsDialog;

  /**
   * The queue job submission dialog.
   */ 
  private JQueueJobsDialog  pQueueJobsDialog;
  
  /**
   * The change job requirements dialog
   */
  private JChangeJobReqsDialog  pChangeJobReqsDialog;

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
  private JBackupDialog  pBackupDialog; 

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

  /**
   * The remote working node selection dialog.
   */
  private JWorkingSelectDialog  pWorkingSelectDialog;
  
  /**
   * The job monitor dialog.
   */
  private JJobMonitorDialog pJobMonitorDialog;
}
