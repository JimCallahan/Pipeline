// $Id: UIMaster.java,v 1.68 2004/12/30 01:55:17 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
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
 * communication of <B>plui</B>(1).
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
   */ 
  private 
  UIMaster
  (
   String masterHost, 
   int masterPort, 
   String queueHost, 
   int queuePort, 
   int jobPort, 
   String layout
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

    pQueueJobBrowserPanels = new PanelGroup<JQueueJobBrowserPanel>();
    pQueueJobViewerPanels  = new PanelGroup<JQueueJobViewerPanel>();
    pQueueJobDetailsPanels = new PanelGroup<JQueueJobDetailsPanel>();

    pOverrideLayoutName = layout;

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
   */ 
  public static void 
  init
  (
   String masterHost, 
   int masterPort, 
   String queueHost, 
   int queuePort, 
   int jobPort, 
   String layout
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(masterHost, masterPort, queueHost, queuePort, jobPort, layout);
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
   * Show a dialog for restoring files associated with checked-in versions.
   */ 
  public void 
  showRestoreDialog()
  {
    pRestoreDialog.setVisible(true);
  }

  /**
   * Show a dialog for editing colors.
   */ 
  public JColorEditorDialog
  showColorEditorDialog
  (
   String title, 
   Color3d color
  )
  {
    pColorEditorDialog.setHeaderTitle(title);
    pColorEditorDialog.setColor(color);
    pColorEditorDialog.setVisible(true);

    return pColorEditorDialog;
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
      Logs.ops.severe("Internal Error:\n" + 
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
  /*   C O M P O N E N T   C R E A T I O N                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new label. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The label text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JLabel
  createLabel
  (
   String text, 
   int width,
   int align
  )
  {
    return createLabel(text, width, align, null);
  }
  
  /**
   * Create a new label with a tooltip. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The label text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JLabel
  createLabel
  (
   String text, 
   int width,
   int align, 
   String tooltip
  )
  {
    JLabel label = new JLabel(text);

    Dimension size = new Dimension(width, 19);
    label.setMinimumSize(size);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    label.setPreferredSize(size);
    
    label.setHorizontalAlignment(align);

    if(tooltip != null) 
      label.setToolTipText(formatToolTip(tooltip));
    
    return label;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new fixed size label. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The label text.
   * 
   * @param width
   *   The fixed width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JLabel
  createFixedLabel
  (
   String text, 
   int width,
   int align
  )
  {
    return createFixedLabel(text, width, align, null);
  }
  
  /**
   * Create a new fixed size label with a tooltip. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The label text.
   * 
   * @param width
   *   The fixed width.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JLabel
  createFixedLabel
  (
   String text, 
   int width,
   int align,
   String tooltip
  )
  {
    JLabel label = createLabel(text, width, align);
    label.setMaximumSize(new Dimension(width, 19));
    
    if(tooltip != null) 
      label.setToolTipText(formatToolTip(tooltip));
    
    return label;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new panel title label. <P> 
   * 
   * @param text
   *   The label text.
   */ 
  public static Component
  createPanelLabel
  (
   String text
  )
  {
    Box hbox = new Box(BoxLayout.X_AXIS);
	
    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
    {
      JLabel label = new JLabel(text);
      label.setName("PanelLabel");

      hbox.add(label);
    }
    
    hbox.add(Box.createHorizontalGlue());
    
    return hbox;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new non-editable text field. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JTextField
  createTextField
  (
   String text, 
   int width,
   int align
  )
  {
    JTextField field = new JTextField(text);
    field.setName("TextField");

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(false);
    
    return field;
  }

  /**
   * Create a new editable text field. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JTextField
  createEditableTextField
  (
   String text, 
   int width,
   int align
  )
  {
    JTextField field = createTextField(text, width, align);
    field.setName("EditableTextField");

    field.setEditable(true);
    
    return field;
  }

  /**
   * Create a new editable text field which can only contain identifiers. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JIdentifierField
  createIdentifierField
  (
   String text, 
   int width,
   int align
  )
  {
    JIdentifierField field = new JIdentifierField();
    field.setName("EditableTextField");

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setText(text);
    
    return field;
  }

  /**
   * Create a new editable text field which can only contain identifier paths. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JPathField
  createPathField
  (
   String text, 
   int width,
   int align
  )
  {
    JPathField field = new JPathField();
    field.setName("EditableTextField");

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setText(text);
    
    return field;
  }

  /**
   * Create a new editable text field which can only contain alphanumeric characters. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JAlphaNumField
  createAlphaNumField
  (
   String text, 
   int width,
   int align
  )
  {
    JAlphaNumField field = new JAlphaNumField();
    field.setName("EditableTextField");

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setText(text);
    
    return field;
  }

  /**
   * Create a new editable text field which can only contain integers. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JIntegerField
  createIntegerField
  (
   Integer value,
   int width,
   int align
  )
  {
    JIntegerField field = new JIntegerField();

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setValue(value);

    return field;
  }

  /**
   * Create a new editable text field which can only contain integer byte sizes. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JByteSizeField
  createByteSizeField
  (
   Long value,
   int width,
   int align
  )
  {
    JByteSizeField field = new JByteSizeField();

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setValue(value);

    return field;
  }

  /**
   * Create a new editable text field which can only contain float values. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JFloatField
  createFloatField
  (
   Float value,  
   int width,
   int align
  )
  {
    JFloatField field = new JFloatField();

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setValue(value);

    return field;
  }

  /**
   * Create a new editable text field which can only contain double values. <P> 
   * 
   * See {@link JLabel#setHorizontalAlignment JLabel.setHorizontalAlignment} for valid
   * values for the <CODE>align</CODE> argument.
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param align
   *   The horizontal alignment.
   */ 
  public static JDoubleField
  createDoubleField
  (
   Double value,  
   int width,
   int align
  )
  {
    JDoubleField field = new JDoubleField();

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    field.setValue(value);

    return field;
  }

  /**
   * Create a new color field. <P> 
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width.
   */ 
  public static JColorField
  createColorField
  (
   Color3d value,  
   int width
  )
  {
    JColorField field = new JColorField(value);

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);

    return field;
  }  

  /**
   * Create a new non-editable text area.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param rows
   *   The initial number of rows.
   */ 
  public static JTextArea
  createTextArea
  (
   String text, 
   int width,
   int rows
  )
  {
    JTextArea area = new JTextArea(text, rows, 0);
    area.setName("TextArea");

    area.setLineWrap(true);
    area.setWrapStyleWord(true);

    area.setEditable(false);
    
    return area;
  }

  /**
   * Create a new editable text area.
   * 
   * @param text
   *   The initial text.
   * 
   * @param width
   *   The minimum and preferred width.
   * 
   * @param rows
   *   The initial number of rows.
   */ 
  public static JTextArea
  createEditableTextArea
  (
   String text, 
   int width,
   int rows
  )
  {
    JTextArea area = createTextArea(text, width, rows);
    area.setName("EditableTextArea");

    area.setEditable(true);
    
    return area;
  }

  /**
   * Create a collection field.
   * 
   * @param values
   *   The initial collection values.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public static JCollectionField
  createCollectionField
  (
   Collection<String> values,
   int width
  ) 
  {
    return createCollectionField(values, null, width);
  }

  /**
   * Create a collection field.
   * 
   * @param values
   *   The initial collection values.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public static JCollectionField
  createCollectionField
  (
   Collection<String> values,
   JDialog parent, 
   int width
  ) 
  {
    JCollectionField field = new JCollectionField(values, parent);

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);

    return field;
  }

  /**
   * Create a boolean field.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public static JBooleanField
  createBooleanField
  (
   int width
  ) 
  {
    JBooleanField field = new JBooleanField();

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);

    return field;
  }

  /**
   * Create a boolean field.
   * 
   * @param value
   *   The initial value.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */ 
  public static JBooleanField
  createBooleanField
  (
   Boolean value, 
   int width
  ) 
  {
    JBooleanField field = createBooleanField(width);
    field.setValue(value);

    return field;
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  public static Component[]
  createTitledPanels()
  {
    Component comps[] = new Component[3];
    
    Box body = new Box(BoxLayout.X_AXIS);
    comps[2] = body;
    {
      {
	JPanel panel = new JPanel();
	comps[0] = panel;
	
	panel.setName("TitlePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	body.add(panel);
      }
    }

    return comps;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new non-editable text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text field.
   */ 
  public static JTextField
  createTitledTextField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth
  )
  {
    return createTitledTextField(tpanel, title, twidth, vpanel, text, vwidth, null);
  }

  /**
   * Create a new non-editable text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JTextField
  createTitledTextField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JTextField field = createTextField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }
    
    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new editable text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text field.
   */ 
  public static JTextField
  createTitledEditableTextField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth
  )
  {
    return createTitledEditableTextField(tpanel, title, twidth, vpanel, text, vwidth, null);
  }

  /**
   * Create a new editable text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JTextField
  createTitledEditableTextField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JTextField field = createEditableTextField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new identifier text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JIdentifierField
  createTitledIdentifierField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth
  )
  {
    return createTitledIdentifierField(tpanel, title, twidth, vpanel, text, vwidth, null);
  }

  /**
   * Create a new identifier text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JIdentifierField
  createTitledIdentifierField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JIdentifierField field = createIdentifierField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new alphanumeric text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JAlphaNumField
  createTitledAlphaNumField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth
  )
  {
    return createTitledAlphaNumField(tpanel, title, twidth, vpanel, text, vwidth, null);
  }

  /**
   * Create a new alphanumeric text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JAlphaNumField
  createTitledAlphaNumField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JAlphaNumField field = createAlphaNumField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new path text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the path field.
   */ 
  public static JPathField
  createTitledPathField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth
  )
  {    
    return createTitledPathField(tpanel, title, twidth, vpanel, text, vwidth, null);
  }

  /**
   * Create a new path text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the path field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JPathField
  createTitledPathField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JPathField field = createPathField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new integer text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JIntegerField
  createTitledIntegerField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Integer value, 
   int vwidth
  )
  {
    return createTitledIntegerField(tpanel, title, twidth, vpanel, value, vwidth, null);
  }

  /**
   * Create a new integer text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JIntegerField
  createTitledIntegerField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Integer value, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JIntegerField field = createIntegerField(value, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new byte size text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JByteSizeField
  createTitledByteSizeField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Long value, 
   int vwidth
  )
  {
    return createTitledByteSizeField(tpanel, title, twidth, vpanel, value, vwidth, null);
  }

  /**
   * Create a new byte size text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JByteSizeField
  createTitledByteSizeField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Long value, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JByteSizeField field = createByteSizeField(value, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new float text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JFloatField
  createTitledFloatField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Float value, 
   int vwidth
  )
  {
    return createTitledFloatField(tpanel, title, twidth, vpanel, value, vwidth, null);
  }

  /**
   * Create a new float text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JFloatField
  createTitledFloatField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Float value, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JFloatField field = createFloatField(value, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new double text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JDoubleField
  createTitledDoubleField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Double value, 
   int vwidth
  )
  {
    return createTitledDoubleField(tpanel, title, twidth, vpanel, value, vwidth, null);
  }
  
  /**
   * Create a new double text field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JDoubleField
  createTitledDoubleField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Double value, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JDoubleField field = createDoubleField(value, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new color field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   */ 
  public static JColorField
  createTitledColorField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Color3d value, 
   int vwidth
  )
  {
    return createTitledColorField(tpanel, title, twidth, vpanel, value, vwidth, null);
  }
  
  /**
   * Create a new color field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param value
   *   The initial value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the identifier field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JColorField
  createTitledColorField
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   Color3d value, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JColorField field = createColorField(value, vwidth);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new hot key field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vwidth
   *   The minimum and preferred width of the hot key field.
   */ 
  public static JHotKeyField
  createTitledHotKeyField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vwidth
  )
  {
    return createTitledHotKeyField(tpanel, title, twidth, vpanel, vwidth, null);
  }
  
  /**
   * Create a new hot key field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vwidth
   *   The minimum and preferred width of the hot key field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JHotKeyField
  createTitledHotKeyField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vwidth, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JHotKeyField field = new JHotKeyField();
    field.setName("HotKeyField"); 

    Dimension size = new Dimension(vwidth, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    vpanel.add(field);

    return field;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new non-editable text area with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text area.
   * 
   * @param rows
   *   The initial number of rows.
   */ 
  public static JTextArea
  createTitledTextArea
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth,
   int rows, 
   boolean isScrolled
  )
  {
    return createTitledTextArea
      (tpanel, title, twidth, vpanel, text, vwidth, rows, isScrolled, null);
  }
  
  /**
   * Create a new non-editable text area with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text area.
   * 
   * @param rows
   *   The initial number of rows.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JTextArea
  createTitledTextArea
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth,
   int rows, 
   boolean isScrolled, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));
    tpanel.add(Box.createRigidArea(new Dimension(0, 19*(rows-1))));

    JTextArea area = createTextArea(text, vwidth, rows);
    if(isScrolled) {
      area.setName("ScrolledTextArea");

      JScrollPane scroll = new JScrollPane(area);
	
      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy
	(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      
      vpanel.add(scroll);
    }
    else {
      Dimension size = new Dimension(vwidth, 19*rows);
      area.setMinimumSize(size);
      area.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
      area.setPreferredSize(size);

      vpanel.add(area);
    }

    return area;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new editable text area with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text area.
   * 
   * @param rows
   *   The initial number of rows.
   */ 
  public static JTextArea
  createTitledEditableTextArea
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth,
   int rows, 
   boolean isScrolled
  )
  {
    return createTitledEditableTextArea
      (tpanel, title, twidth, vpanel, text, vwidth, rows, isScrolled, null);
  }

  /**
   * Create a new editable text area with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param text
   *   The initial text.
   * 
   * @param vwidth
   *   The minimum and preferred width of the text area.
   * 
   * @param rows
   *   The initial number of rows.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JTextArea
  createTitledEditableTextArea
  (
   JPanel tpanel, 
   String title,  
   int twidth,
   JPanel vpanel, 
   String text, 
   int vwidth,
   int rows, 
   boolean isScrolled, 
   String tooltip
  )
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));
    tpanel.add(Box.createRigidArea(new Dimension(0, 19*(rows-1))));

    JTextArea area = createEditableTextArea(text, vwidth, rows);
    if(isScrolled) {
      area.setName("ScrolledTextArea");

      JScrollPane scroll = new JScrollPane(area);
      
      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      
      vpanel.add(scroll);
    }
    else {
      Dimension size = new Dimension(vwidth, 19*rows);
      area.setMinimumSize(size);
      area.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
      area.setPreferredSize(size);

      vpanel.add(area);
    }

    return area;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a slider with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vmin
   *   The minimum slider value.
   * 
   * @param vmax
   *   The maximum slider value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   */ 
  public static JSlider 
  createTitledSlider
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vmin, 
   int vmax,
   int vwidth
  ) 
  {
    return createTitledSlider(tpanel, title, twidth, vpanel, vmin, vmax, vwidth, null);
  }
  
  /**
   * Create a slider with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vmin
   *   The minimum slider value.
   * 
   * @param vmax
   *   The maximum slider value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JSlider 
  createTitledSlider
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vmin, 
   int vmax,
   int vwidth, 
   String tooltip
  ) 
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JSlider slider = new JSlider(vmin, vmax, vmin);
      
    Dimension size = new Dimension(vwidth, 19);
    slider.setMinimumSize(size);
    slider.setMaximumSize(size);
    slider.setPreferredSize(size);

    slider.setPaintLabels(false);
    slider.setPaintTicks(false);
    slider.setPaintTrack(true);

    vpanel.add(slider);

    return slider;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a floating-point slider with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vmin
   *   The minimum slider value.
   * 
   * @param vmax
   *   The maximum slider value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   */ 
  public static JSlider 
  createTitledSlider
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   double vmin, 
   double vmax,
   int vwidth
  ) 
  {
    return createTitledSlider(tpanel, title, twidth, vpanel, vmin, vmax, vwidth, null);
  }

  /**
   * Create a floating-point slider with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vmin
   *   The minimum slider value.
   * 
   * @param vmax
   *   The maximum slider value.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JSlider 
  createTitledSlider
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   double vmin, 
   double vmax,
   int vwidth, 
   String tooltip
  ) 
  {
    return createTitledSlider(tpanel, title, twidth, 
			      vpanel, (int)(vmin*1000.0), (int)(vmax*1000.0), vwidth, 
			      tooltip);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a tree set field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param values
   *   The initial collection values.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   */ 
  public static JCollectionField
  createTitledCollectionField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel,
   Collection<String> values,
   int vwidth
  ) 
  {
    return createTitledCollectionField(tpanel, title, twidth, vpanel, values, vwidth, null);
  }

  /**
   * Create a tree set field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param values
   *   The initial collection values.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JCollectionField
  createTitledCollectionField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel,
   Collection<String> values,
   int vwidth, 
   String tooltip
  ) 
  {
    return createTitledCollectionField(tpanel, title, twidth, 
				       vpanel, values, null, vwidth, 
				       tooltip);
  }

  /**
   * Create a tree set field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param values
   *   The initial collection values.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JCollectionField
  createTitledCollectionField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel,
   Collection<String> values,
   JDialog parent, 
   int vwidth, 
   String tooltip
  ) 
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));
    
    JCollectionField field = createCollectionField(values, parent, vwidth);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a boolean field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   */ 
  public static JBooleanField
  createTitledBooleanField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vwidth
  ) 
  {
    return createTitledBooleanField(tpanel, title, twidth, vpanel, vwidth, null);
  }

  /**
   * Create a boolean field with a title and add them to the given panels.
   * 
   * @param tpanel
   *   The titles panel.
   * 
   * @param twidth
   *   The minimum and preferred width of the title.
   * 
   * @param title
   *   The title text.
   * 
   * @param vpanel
   *   The values panel.
   * 
   * @param vwidth
   *   The minimum and preferred width of the value field.
   * 
   * @param tooltip
   *   The tooltip text.
   */ 
  public static JBooleanField
  createTitledBooleanField
  (
   JPanel tpanel, 
   String title, 
   int twidth,
   JPanel vpanel, 
   int vwidth, 
   String tooltip
  ) 
  {
    tpanel.add(createFixedLabel(title, twidth, JLabel.RIGHT, tooltip));

    JBooleanField field = createBooleanField(vwidth);
    vpanel.add(field);

    return field;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add vertical space into the given panels.
   */ 
  public static void 
  addVerticalSpacer
  (
   JPanel tpanel, 
   JPanel vpanel, 
   int height
  ) 
  {
    tpanel.add(Box.createRigidArea(new Dimension(0, height)));
    vpanel.add(Box.createRigidArea(new Dimension(0, height)));
  }

  /**
   * Add vertical glue into the given panels.
   */ 
  public static void 
  addVerticalGlue
  (
   JPanel tpanel, 
   JPanel vpanel
  ) 
  {
    tpanel.add(Box.createVerticalGlue());
    vpanel.add(Box.createVerticalGlue());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create list panel components.
   * 
   * @param box
   *   The parent horizontal box.
   * 
   * @param title
   *   The title of the list.
   * 
   * @param size
   *   The preferred size of the list.
   */ 
  public static JList 
  createListComponents
  (
   Box box, 
   String title, 
   Dimension size
  ) 
  {
    return createListComponents(box, title, size, true, true);
  }

  /**
   * Create list panel components.
   * 
   * @param box
   *   The parent horizontal box.
   * 
   * @param title
   *   The title of the list.
   * 
   * @param size
   *   The preferred size of the list.
   * 
   * @param headerSpacer
   *   Add a vertical header spacer?
   * 
   * @param footerSpacer
   *   Add a vertical footer spacer?
   */ 
  public static JList 
  createListComponents
  (
   Box box, 
   String title, 
   Dimension size, 
   boolean headerSpacer, 
   boolean footerSpacer
  ) 
  {
    Box vbox = new Box(BoxLayout.Y_AXIS);	

    if(headerSpacer)
      vbox.add(Box.createRigidArea(new Dimension(0, 20)));
    
    vbox.add(createPanelLabel(title));
    
    vbox.add(Box.createRigidArea(new Dimension(0, 4)));

    JList lst = null;
    {
      lst = new JList(new DefaultListModel());
      lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lst.setCellRenderer(new JListCellRenderer());

      {
	JScrollPane scroll = new JScrollPane(lst);
	
	scroll.setMinimumSize(new Dimension(150, 150));
	scroll.setPreferredSize(size);
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	vbox.add(scroll);
      }
    }

    if(footerSpacer) 
      vbox.add(Box.createRigidArea(new Dimension(0, 20)));

    box.add(vbox);

    return lst;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   U T I L I T I E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Formats tool tip text as HTML, breaking up long tool tips into multiple lines. 
   * 
   * @param text 
   *   The unformatted tool tip text.
   */ 
  public static String
  formatToolTip
  (
   String text
  ) 
  {
    if(text == null) 
      return null;

    int line = 85;
    if(text.length() < line) {
      return ("<html><font color=\"#000000\">" + text + "</font></html>");
    }
    else {
      StringBuffer buf = new StringBuffer();
      buf.append("<html><font color=\"#000000\">");
      int wk, cnt;
      String words[] = text.split("\\s");
      for(wk=0, cnt=0; wk<words.length; wk++) {
	int wlen = words[wk].length();
	if(wlen > 0) {
	  if(cnt == 0) { 
	    buf.append(words[wk]);
	    cnt = wlen;
	  }
	  else if((cnt+wlen+1) < line) {
	    buf.append(" " + words[wk]);
	    cnt += wlen + 1;
	  }
	  else {
	    buf.append("<br>" + words[wk]);
	    cnt = wlen;
	  }
	}
      }
      buf.append("</font></html>");

      return buf.toString();
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   J O G L   U T I L I T I E S                                                          */
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
   String name
  ) 
  {
    SwingUtilities.invokeLater(new RestoreSavedLayoutTask(name));
  }

  /**
   * Make the given panel layout the default layout.
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
	      Logs.ops.severe("Unable to create (" + dir + ")!");
	      Logs.flush();
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
      }
      catch(Exception ex) {	
	Logs.ops.severe("Unable to initialize the user preferences!\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);	 
      }

      /* make sure that the default working area exists */ 
      try {
	pMasterMgrClient.createWorkingArea(PackageInfo.sUser, "default");
      }
      catch(PipelineException ex) {	
	Logs.ops.severe("Unable to initialize the default working area!\n" + 
			"  " + ex.getMessage());
	Logs.flush();
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
	  Logs.ops.severe("Unable to parse the look-and-feel XML file (synth.xml):\n" + 
			  "  " + ex.getMessage());
	  System.exit(1);
	}
	catch(UnsupportedLookAndFeelException ex) {
	  Logs.ops.severe("Unable to load the Pipeline look-and-feel:\n" + 
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

	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createHorizontalGlue());

	  {
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    
	    vbox.add(Box.createVerticalGlue());

	    {
	      JLabel label = new JLabel("version " + PackageInfo.sVersion);
	      label.setForeground(Color.cyan);
	      label.setOpaque(false);
	    
	      vbox.add(label);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 26)));

	    hbox.add(vbox);
	  }

	  hbox.add(Box.createRigidArea(new Dimension(19, 0)));
	  
	  frame.setGlassPane(hbox);
	  frame.getGlassPane().setVisible(true);
	}
	
	frame.pack();

	{
	  Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
	  frame.setLocation(bounds.x + bounds.width/2 - frame.getWidth()/2, 
			    bounds.y + bounds.height/2 - frame.getHeight()/2);
	}

	frame.setVisible(true);
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
	      JTextField field = createTextField(null, 200, JLabel.LEFT);
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

	pManageUsersDialog         = new JManageUsersDialog();
	pManageToolsetsDialog      = new JManageToolsetsDialog();
	pManageLicenseKeysDialog   = new JManageLicenseKeysDialog();
	pManageSelectionKeysDialog = new JManageSelectionKeysDialog();

	pQueueJobsDialog = new JQueueJobsDialog();
	
	pSubProcessFailureDialog = new JSubProcessFailureDialog();

	pBackupDialog = 
	  new JFileSelectDialog("Backup Database", "Backup Database File:",
				"Backup As:", 64, "Backup"); 
	pBackupDialog.updateTargetFile(PackageInfo.sTempDir);

	pArchiveDialog = new JArchiveDialog();
	pRestoreDialog = new JRestoreDialog();

	pColorEditorDialog = new JColorEditorDialog();
      }

      ArrayList<JFrame> frames = new ArrayList<JFrame>();
      {
	String layoutName = pOverrideLayoutName;
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
	  
	if(layoutName != null) {
	  setDefaultLayoutName(layoutName);
	  frames.addAll(restoreSavedLayout(layoutName));
	}
	else {
	  frames.add(pFrame);
	}
      }

      pSplashFrame.setVisible(false);

      for(JFrame frame : frames) 
	frame.setVisible(true);
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
      ArrayList<JFrame> frames = restoreSavedLayout(pName);
      for(JFrame frame : frames) 
	frame.setVisible(true);
    }

    private String  pName;
  }

  private ArrayList<JFrame>
  restoreSavedLayout
  (
   String name
  ) 
  {
    /* clean up existing panels */ 
    {
      pRootPanel.removeAll();
      
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
      
      pQueueJobBrowserPanels.clear();
      pQueueJobViewerPanels.clear();
      pQueueJobDetailsPanels.clear();
    }
    
    /* restore saved panels */
    LinkedList<PanelLayout> layouts = null;
    {
      File file = new File(PackageInfo.sHomeDir, 
			   PackageInfo.sUser + "/.pipeline/layouts" + name);
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
      
      setLayoutName(name);
    }
    else {
      JManagerPanel mpanel = new JManagerPanel();
      mpanel.setContents(new JEmptyPanel()); 
      
      pRootPanel.add(mpanel);
      pRootPanel.validate();
      pRootPanel.repaint();

      frames.add(pFrame);
    }

    return frames;
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
	      
	      editor = PluginMgr.getInstance().newEditor(ename, pEditorVersion);
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
   * The OpenGL capabilities used by all GLCanvas instances.
   */ 
  private GLCapabilities  pGLCapabilities;

  /**
   * The template GLCanvas which creates the shared OpenCL context in which textures and 
   * display lists are initialized.
   */ 
  private GLCanvas  pGLCanvas;


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
   * The restore dialog.
   */
  private JRestoreDialog  pRestoreDialog; 

  /**
   * The color editor dialog.
   */ 
  private JColorEditorDialog  pColorEditorDialog; 
}
