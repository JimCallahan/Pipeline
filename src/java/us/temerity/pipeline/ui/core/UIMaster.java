// $Id: UIMaster.java,v 1.19 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
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

import net.java.games.jogl.*;

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
   * @param masterHost
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param masterPort
   *   The port number listened to by the <B>plmaster</B>(1) daemon for incoming connections.
   * 
   * @param queueHost
   *   The hostname running <B>plqueuemgr</B>(1).
   * 
   * @param queuePort
   *   The port number listened to by the <B>plqueuemgr</B>(1) daemon for incoming 
   *   connections.
   * 
   * @param jobPort
   *   The port number listened to by <B>pljobmgr</B>(1) daemons for incoming connections.
   * 
   * @param layout
   *   The name of the override panel layout or <CODE>null</CODE> to use the default layout.
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
   String masterHost, 
   int masterPort, 
   String queueHost, 
   int queuePort, 
   int jobPort, 
   String layout, 
   boolean restoreLayout,
   boolean restoreSelections, 
   boolean debugGL, 
   boolean traceGL
  ) 
  {
    pMasterMgrClient = new MasterMgrClient(masterHost, masterPort);
    pQueueMgrClient  = new QueueMgrClient(queueHost, queuePort);

    pJobPort = jobPort;

    pOpsLock = new ReentrantLock();

    pNodeBrowserPanels = new PanelGroup<JNodeBrowserPanel>();
    pNodeViewerPanels  = new PanelGroup<JNodeViewerPanel>();
    pNodeDetailsPanels = new PanelGroup<JNodeDetailsPanel>();
    pNodeHistoryPanels = new PanelGroup<JNodeHistoryPanel>();
    pNodeFilesPanels   = new PanelGroup<JNodeFilesPanel>();
    pNodeLinksPanels   = new PanelGroup<JNodeLinksPanel>();

    pQueueJobBrowserPanels = new PanelGroup<JQueueJobBrowserPanel>();
    pQueueJobViewerPanels  = new PanelGroup<JQueueJobViewerPanel>();
    pQueueJobDetailsPanels = new PanelGroup<JQueueJobDetailsPanel>();

    pOverrideLayoutName = layout;
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
   * @param masterHost
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param masterPort
   *   The port number listened to by the <B>plmaster</B>(1) daemon for incoming connections.
   * 
   * @param queueHost
   *   The hostname running <B>plqueuemgr</B>(1).
   * 
   * @param queuePort
   *   The port number listened to by the <B>plqueuemgr</B>(1) daemon for incoming 
   *   connections.
   * 
   * @param jobPort
   *   The port number listened to by <B>pljobmgr</B>(1) daemons for incoming connections.
   * 
   * @param layout
   *   The name of the override panel layout or <CODE>null</CODE> to use the default layout.
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
   String masterHost, 
   int masterPort, 
   String queueHost, 
   int queuePort, 
   int jobPort, 
   String layout,
   boolean restoreLayout,
   boolean restoreSelections,
   boolean debugGL, 
   boolean traceGL
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(masterHost, masterPort, 
			   queueHost, queuePort, jobPort, 
			   layout, restoreLayout, restoreSelections, 
			   debugGL, traceGL);
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
    return pMasterMgrClient;
  }

  /**
   * Get the network connection to <B>plqueuemgr</B>(1).
   */ 
  public QueueMgrClient
  getQueueMgrClient() 
  {
    return pQueueMgrClient;
  }

  /**
   * Get the port number used by <B>pljobmgr</B>(1) daemons.
   */ 
  public int
  getJobPort() 
  {
    return pJobPort; 
  }


 
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
   * Get the name of the current panel layout.
   * 
   * @return 
   *   The name or <CODE>null</CODE> if unset.
   */ 
  public String 
  getLayoutName() 
  {
    return pLayoutName;
  }

  /**
   * Set the name of the current panel layout.
   */ 
  public void 
  setLayoutName
  (
   String name
  ) 
  {
    pLayoutName = name;
    updateFrameHeaders();
  }


  /**
   * Get the name of the default panel layout.
   * 
   * @return 
   *   The name or <CODE>null</CODE> if unset.
   */ 
  public String 
  getDefaultLayoutName() 
  {
    return pDefaultLayoutName;
  }
  
  /**
   * Set the name of the default panel layout.
   */ 
  public void 
  setDefaultLayoutName
  (
   String name
  ) 
  {
    pDefaultLayoutName = name;
    updateFrameHeaders();
  }

  /** 
   * Add the layout name to the top-level frame headers.
   */ 
  private void 
  updateFrameHeaders()
  {
    String title = "plui";
    if(pLayoutName != null) {
      File path = new File(pLayoutName);
      String def = "";
      if((pDefaultLayoutName != null) && pDefaultLayoutName.equals(pLayoutName))
	def = " (default)";
      title = ("plui - Main | " + path.getName() + def);
    }

    pFrame.setTitle(title);    
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
   * Show the manage users dialog.
   */ 
  public void 
  showManageUsersDialog()
  {
    pManageUsersDialog.updateList();
    pManageUsersDialog.setVisible(true);
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
   * Show the manage editors dialog.
   */ 
  public void 
  showManageEditorMenusDialog()
  {
    pManageEditorMenusDialog.updateMenuLayout();
    pManageEditorMenusDialog.setVisible(true);
  }

  /**
   * Show the manage comparators dialog.
   */ 
  public void 
  showManageComparatorMenusDialog()
  {
    pManageComparatorMenusDialog.updateMenuLayout();
    pManageComparatorMenusDialog.setVisible(true);
  }

  /**
   * Show the manage tools dialog.
   */ 
  public void 
  showManageToolMenusDialog()
  {
    pManageToolMenusDialog.updateMenuLayout();
    pManageToolMenusDialog.setVisible(true);
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
   * Show the manage selection keys dialog.
   */ 
  public void 
  showManageSelectionKeysDialog()
  {
    pManageSelectionKeysDialog.updateSelectionKeys();
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
    ShowSubprocessFailureDialog task = new ShowSubprocessFailureDialog(header, proc);
    SwingUtilities.invokeLater(task);
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


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Show a dialog which graphs the resource usage history of queue servers.
   */ 
  public void 
  showResourceUsageHistoryDialog
  (
   TreeMap<String,ResourceSampleBlock> samples
  )
  {
    pResourceUsageHistoryDialog.updateSamples(samples);
    pResourceUsageHistoryDialog.setVisible(true);
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
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
    boolean aquired = pOpsLock.tryLock();

    if(aquired) 
      SwingUtilities.invokeLater(new BeginOpsTask(msg));
    else 
      Toolkit.getDefaultToolkit().beep();

    return aquired;
  }
  
  /**
   * Try to aquire a panel operation lock. <P> 
   * 
   * @return
   *   Whether the panel operation should proceed.
   */ 
  public boolean
  beginPanelOp() 
  {
    return beginPanelOp("");
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
    assert(pOpsLock.isLocked());
    SwingUtilities.invokeLater(new UpdateOpsTask(msg));
  }

  /**
   * Release the panel operation lock and notify the user that the operation has 
   * completed. <P>
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
    try {
      pOpsLock.unlock();  
      SwingUtilities.invokeLater(new EndOpsTask(msg)); 
    }
    catch(IllegalMonitorStateException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 "Internal Error:\n" + 
	 "  " + ex.getMessage());
    }
  }

  /**
   * Release the panel operation lock. <P>
   */ 
  public void 
  endPanelOp()
  {
    endPanelOp("");
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
    return GLDrawableFactory.getFactory().createGLCanvas(pGLCapabilities, pGLCanvas);
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
    if(pLayoutName != null) 
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
      File file = new File(PackageInfo.sHomeDir, 
			   PackageInfo.sUser + "/.pipeline/layouts/" + pLayoutName);
      
      File dir = file.getParentFile();
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
      
      LockedGlueFile.save(file, "PanelLayout", layouts);
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
   String name, 
   boolean restoreSelections
  ) 
  {
    pRestoreSelections = restoreSelections;
    SwingUtilities.invokeLater(new RestoreSavedLayoutTask(name));
  }

  /**
   * Make the current layout the default layout.
   */
  public void 
  doDefaultLayout() 
  {
    doDefaultLayout(pLayoutName);
  }

  /**
   * Make the given layout the default layout.
   */
  public void 
  doDefaultLayout
  (
   String layoutName
  )
  {
    if((layoutName != null) && (layoutName.equals(pLayoutName)))
      saveLayoutHelper();

    try {
      File file = new File(PackageInfo.sHomeDir, 
			   PackageInfo.sUser + "/.pipeline/default-layout");

      if(layoutName != null) 
	LockedGlueFile.save(file, "DefaultLayout", layoutName);
      else 
	file.delete();

      setDefaultLayoutName(layoutName);
    }
    catch(Exception ex) {
      showErrorDialog(ex);
    }    
  }

  /**
   * Close the network connection and exit.
   */ 
  public void 
  doQuit()
  {
    if(pMasterMgrClient != null) 
      pMasterMgrClient.disconnect();

    if(pQueueMgrClient != null) 
      pQueueMgrClient.disconnect();

    PluginMgrClient.getInstance().disconnect();

    /* save the collapsed node paths */ 
    synchronized(pCollapsedNodePaths) {
      File file = new File(PackageInfo.sHomeDir, 
			   PackageInfo.sUser + "/.pipeline/collapsed-nodes");
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
      /* make sure user preference exist */ 
      try {
	File base = new File(PackageInfo.sHomeDir, PackageInfo.sUser + "/.pipeline");
	String subdirs[] = { "layouts" };
	int wk;
	for(wk=0; wk<subdirs.length; wk++) {
	  File dir = new File(base, subdirs[wk]);
	  if(!dir.isDirectory()) {
	    if(!dir.mkdirs()) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Severe,
		 "Unable to create (" + dir + ")!");
	      LogMgr.getInstance().flush();
	      System.exit(1);	    
	    }
	    NativeFileSys.chmod(0700, dir);
	  }	  
	}

	{
	  File file = new File(base, "preferences");
	  if(file.isFile()) 	  
	    UserPrefs.load();
	}

	/* read the collapsed node paths */ 
	synchronized(pCollapsedNodePaths) {
	  File file = new File(base, "collapsed-nodes");
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
	pMasterMgrClient.createWorkingArea(PackageInfo.sUser, "default");
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
    	    pGLCanvas = GLDrawableFactory.getFactory().createGLCanvas(pGLCapabilities);
            
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
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 
	    
	    panel.add(Box.createRigidArea(new Dimension(2, 0)));
	    
	    {
	      JLabel label = new JLabel(sProgressLightIcon);
	      pProgressLight = label;
	      
	      Dimension size = new Dimension(15, 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      panel.add(label);
	    }
	    
	    {
	      JTextField field = UIFactory.createTextField(null, 200, JLabel.LEFT);
	      pProgressField = field;
	      
	      panel.add(field);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(8, 0)));
	    panel.add(Box.createHorizontalGlue());
	    
	    root.add(panel);
	  }
	  
	  root.add(Box.createRigidArea(new Dimension(0, 4)));
	  
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

	pSaveLayoutDialog     = new JSaveLayoutDialog();
	pManageLayoutsDialog  = new JManageLayoutsDialog();

	pErrorDialog     = new JErrorDialog();
	pUserPrefsDialog = new JUserPrefsDialog();
	pAboutDialog     = new JAboutDialog();
	pConfigDialog    = new JConfigDialog();

	pDefaultEditorsDialog = new JDefaultEditorsDialog(); 

	pManageUsersDialog           = new JManageUsersDialog();
	pManageToolsetsDialog        = new JManageToolsetsDialog();
	pManageEditorMenusDialog     = new JManageEditorMenusDialog();
	pManageComparatorMenusDialog = new JManageComparatorMenusDialog();
	pManageToolMenusDialog       = new JManageToolMenusDialog();
	pManageLicenseKeysDialog     = new JManageLicenseKeysDialog();
	pManageSelectionKeysDialog   = new JManageSelectionKeysDialog();

	pQueueJobsDialog = new JQueueJobsDialog();
	
	pSubProcessFailureDialog = new JSubProcessFailureDialog();

	pBackupDialog = 
	  new JFileSelectDialog("Backup Database", "Backup Database File:",
				"Backup As:", 64, "Backup"); 
	pBackupDialog.updateTargetFile(PackageInfo.sTempDir);

	pArchiveDialog = new JArchiveDialog();
	pOfflineDialog = new JOfflineDialog();
	pRestoreDialog = new JRestoreDialog();

	pResourceUsageHistoryDialog = new JResourceUsageHistoryDialog();
      }

      pSplashFrame.setVisible(false);

      {
	String layoutName = null;
	if(pRestoreLayout) {
	  layoutName = pOverrideLayoutName;
	  if(layoutName == null) {
	    try {
	      File file = new File(PackageInfo.sHomeDir, 
				   PackageInfo.sUser + "/.pipeline/default-layout"); 
	      if(file.isFile()) 
		layoutName = (String) LockedGlueFile.load(file);
	    }
	    catch(Exception ex) {
	      showErrorDialog(ex);
	    }   
	  }
	}
	  
	if(layoutName != null) {
	  setDefaultLayoutName(layoutName);
	  SwingUtilities.invokeLater(new RestoreSavedLayoutTask(layoutName));
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
     String msg
    ) 
    {
      super("UIMaster:BeginOpsTask");

      pMsg = msg;
    }

    public void 
    run() 
    {
      pProgressLight.setIcon(sProgressLightOnIcon);
      pProgressField.setText(pMsg);
    }

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
     String msg
    ) 
    {
      super("UIMaster:UpdateOpsTask");

      pMsg = msg;
    }

    public void 
    run() 
    {
      pProgressField.setText(pMsg);
    }

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
     String msg
    ) 
    {
      super("UIMaster:EndOpsTask");

      pMsg = msg;
    }

    public void 
    run() 
    {
      pProgressField.setText(pMsg);
      pProgressLight.setIcon(sProgressLightIcon);
    }

    private String  pMsg;
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
	pSaveLayoutDialog.updateLayouts(pLayoutName);
	pSaveLayoutDialog.setVisible(true);
	if(pSaveLayoutDialog.wasConfirmed()) {
	  String name = pSaveLayoutDialog.getSelectedName();
	  if((name != null) && (name.length() > 0)) {
	    setLayoutName(name);	    
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
	File file = new File(PackageInfo.sHomeDir, 
			     PackageInfo.sUser + "/.pipeline/layouts/" + pLayoutName);

	File dir = file.getParentFile();
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

	LockedGlueFile.save(file, "PanelLayout", layouts);
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
     String name
    ) 
    {
      super("UIMaster:RestoreSavedLayoutTask");

      pName = name;
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
	
	pQueueJobBrowserPanels.clear();
	pQueueJobViewerPanels.clear();
	pQueueJobDetailsPanels.clear();
      }
      
      /* show the splash screen */ 
      pRestoreSplashFrame.setVisible(true);
      
      RestoreSavedLayoutRefreshTask task = new RestoreSavedLayoutRefreshTask(pName);
      task.start();      
    }

    private String  pName;
  }

  private 
  class RestoreSavedLayoutRefreshTask
    extends Thread
  {
    public 
    RestoreSavedLayoutRefreshTask
    (
     String name
    ) 
    {
      super("UIMaster:RestoreSavedLayoutRefreshTask");
      
      pName = name;
    }

    public void 
    run() 
    {
      try {
	sleep(100);
      }
      catch(InterruptedException ex) {
      }

      SwingUtilities.invokeLater(new RestoreSavedLayoutLoaderTask(pName));
    }

    private String  pName;
    private JFrame  pSplash;
  }

  private 
  class RestoreSavedLayoutLoaderTask
    extends Thread
  {
    public 
    RestoreSavedLayoutLoaderTask
    (
     String name
    ) 
    {
      super("UIMaster:RestoreSavedLayoutLoaderTask");
      
      pName = name;
    }

    public void 
    run() 
    {
      /* restore saved panels */
      LinkedList<PanelLayout> layouts = null;
      {
	pIsRestoring.set(true);
	
	File file = new File(PackageInfo.sHomeDir, 
			     PackageInfo.sUser + "/.pipeline/layouts" + pName);
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

      /* restore selections and perform the initial update synchronously */ 
      if(pRestoreSelections) {
	for(JNodeViewerPanel panel : pNodeViewerPanels.getPanels()) 
	  panel.restoreSelection();

 	for(JQueueJobBrowserPanel panel : pQueueJobBrowserPanels.getPanels()) 
 	  panel.restoreSelection();
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
	
	setLayoutName(pName);
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

    private String  pName;
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
	pManageLayoutsDialog.updateLayouts(pLayoutName);
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
     SubProcessLight proc
    )
    {
      super("UIMaster:ShowSubprocessFailureDialog");

      pHeader = header;
      pProc = proc;
    }

    public void 
    run() 
    {
      pSubProcessFailureDialog.updateProc(pHeader, pProc);
      pSubProcessFailureDialog.setVisible(true);
    }

    private String           pHeader;
    private SubProcessLight  pProc;
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
     String author, 
     String view
    ) 
    {
      super(tname);

      pAuthorName = author;
      pViewName   = view; 
    }

    protected void
    postOp() 
    {}

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
     NodeCommon com, 
     String author, 
     String view
    ) 
    {
      super("UIMaster:EditTask", author, view);

      pNodeCommon = com;
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid, 
     String author, 
     String view
    ) 
    {
      super("UIMaster:EditTask", author, view);

      pNodeCommon    = com;
      pEditorName    = ename;
      pEditorVersion = evid; 
    }

    public void 
    run() 
    {
      SubProcessLight proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Editor...")) {
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
	      String ename = pEditorName;
	      if(ename == null) 
		ename = pNodeCommon.getEditor();
	      if(ename == null) 
		throw new PipelineException
		  ("No editor was specified for node (" + pNodeCommon.getName() + ")!");
	      
	      editor = PluginMgrClient.getInstance().newEditor(ename, pEditorVersion);
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = pNodeCommon.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + pNodeCommon.getName() + ")!");

	      MasterMgrClient client = master.getMasterMgrClient();
	      
	      String view = null;
	      if(mod != null)
		view = pViewName; 

	      /* passes pAuthorName so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment(pAuthorName, view, tname);

	      /* override these since the editor will be run as the current user */ 
	      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
	      env.put("USER", PackageInfo.sUser);
	    }
	    
	    /* get the primary file sequence */ 
	    FileSeq fseq = null;
	    File dir = null; 
	    {
	      String path = null;
	      if(mod != null) {
		File wpath = 
		  new File(PackageInfo.sWorkDir, 
			   pAuthorName + "/" + pViewName + "/" + pNodeCommon.getName());
		path = wpath.getParent();
	      }
	      else if(vsn != null) {
		path = (PackageInfo.sRepoDir + "/" + 
			vsn.getName() + "/" + vsn.getVersionID());
	      }
	      else {
		assert(false);
	      }
	  
	      fseq = new FileSeq(path, pNodeCommon.getPrimarySequence());
	      dir = new File(path);
	    }
	    
	    /* start the editor */ 
	    proc = editor.launch(fseq, env, dir);
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
	    master.endPanelOp("Done.");
	  }
	}

	/* wait for the editor to exit */ 
	if(proc != null) {
	  try {
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      master.showSubprocessFailureDialog("Editor Failure:", proc);
	  }
	  catch(InterruptedException ex) {
	    master.showErrorDialog(ex);
	  }
	}
      }
    }
 
    private NodeCommon  pNodeCommon; 
    private String      pEditorName;
    private VersionID   pEditorVersion; 
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
     String name,
     String author, 
     String view, 
     Integer batchSize, 
     Integer priority, 
     Integer interval, 
     TreeSet<String> selectionKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", author, view);

      pNames = new TreeSet<String>();
      pNames.add(name);

      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pSelectionKeys = selectionKeys;
    }

    public 
    QueueJobsTask
    (
     TreeSet<String> names,
     String author, 
     String view, 
     Integer batchSize, 
     Integer priority, 
     Integer interval, 
     TreeSet<String> selectionKeys
    ) 
    {
      super("UIMaster:QueueJobsTask", author, view);

      pNames = names;

      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = interval;
      pSelectionKeys = selectionKeys;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp()) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp("Submitting Jobs to the Queue: " + name);
	    master.getMasterMgrClient().submitJobs(pAuthorName, pViewName, name, null, 
						   pBatchSize, pPriority, pRampUp, 
						   pSelectionKeys);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	postOp();
      }
    }

    private TreeSet<String>  pNames;  
    private Integer          pBatchSize;
    private Integer          pPriority;
    private Integer          pRampUp; 
    private TreeSet<String>  pSelectionKeys;
  }

  /** 
   * Pause the given jobs.
   */ 
  public
  class PauseJobsTask
    extends BaseNodeTask
  {
    public 
    PauseJobsTask
    (
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super("UIMaster:PauseJobsTask", author, view);

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Pausing Jobs...")) {
	try {
	  master.getQueueMgrClient().pauseJobs(pAuthorName, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	postOp();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  public
  class ResumeJobsTask
    extends BaseNodeTask
  {
    public 
    ResumeJobsTask
    (
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super("UIMaster:ResumeJobsTask", author, view);

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Resuming Paused Jobs...")) {
	try {
	  master.getQueueMgrClient().resumeJobs(pAuthorName, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	postOp();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  public
  class KillJobsTask
    extends BaseNodeTask
  {
    public 
    KillJobsTask
    (
     TreeSet<Long> jobIDs,
     String author, 
     String view
    ) 
    {
      super("UIMaster:KillJobsTask", author, view);

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Killing Jobs...")) {
	try {
	  master.getQueueMgrClient().killJobs(pAuthorName, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	postOp();
      }
    }

    private TreeSet<Long>  pJobIDs; 
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
     String name,
     String author, 
     String view
    ) 
    {
      super("UIMaster:RemoveFilesTask", author, view);

      pNames = new TreeSet<String>();
      pNames.add(name);
    }

    public 
    RemoveFilesTask
    (
     TreeSet<String> names,
     String author, 
     String view
    ) 
    {
      super("UIMaster:RemoveFilesTask", author, view);

      pNames = names; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp()) {
	try {
	  for(String name : pNames) {
	    master.updatePanelOp("Removing Files: " + name);
	    master.getMasterMgrClient().removeFiles(pAuthorName, pViewName, name, null);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
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
  private static Icon sSplashIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("Splash.png"));

  private static Icon sRestoreSplashIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("RestoreSplash.png"));

  private static Icon sProgressLightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ProgressLightIcon.png"));

  private static Icon sProgressLightOnIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ProgressLightOnIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network interface to the <B>plmaster</B>(1) daemon.
   */ 
  private MasterMgrClient  pMasterMgrClient;

  /**
   * The network interface to the <B>plqueuemgr</B>(1) daemon.
   */ 
  private QueueMgrClient  pQueueMgrClient;

  /**
   * The port number to use when contacting <B>pljobmgr</B>(1) daemons.
   */ 
  private int  pJobPort; 


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
  private ReentrantLock pOpsLock;

  /**
   * The light which warns users that a panel operation is in progress.
   */ 
  private JLabel  pProgressLight;

  /**
   * The progress message field.
   */ 
  private JTextField  pProgressField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the last saved/loaded layout.
   */ 
  private String  pLayoutName;
 
  /**
   * The name of the default layout.
   */ 
  private String  pDefaultLayoutName;
 
  /**
   * The name of the override default layout.
   */ 
  private String  pOverrideLayoutName;
 
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
   * The manage users dialog.
   */ 
  private JManageUsersDialog  pManageUsersDialog;

  /**
   * The manage toolsets dialog.
   */ 
  private JManageToolsetsDialog  pManageToolsetsDialog;

  /**
   * The manage editors dialog.
   */ 
  private JManageEditorMenusDialog  pManageEditorMenusDialog;

  /**
   * The manage comparators dialog.
   */ 
  private JManageComparatorMenusDialog  pManageComparatorMenusDialog;

  /**
   * The manage tools dialog.
   */ 
  private JManageToolMenusDialog  pManageToolMenusDialog;

  /**
   * The manage license keys dialog.
   */ 
  private JManageLicenseKeysDialog  pManageLicenseKeysDialog;

  /**
   * The manage selection keys dialog.
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
   * The server resource usage history dialog.
   */
  private JResourceUsageHistoryDialog  pResourceUsageHistoryDialog;

}
