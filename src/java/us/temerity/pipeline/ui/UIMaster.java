// $Id: UIMaster.java,v 1.18 2004/05/21 21:17:51 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;

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
   * @param hostname
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param port
   *   The port number listened to by <B>plmaster</B>(1) for incoming connections.
   */ 
  private 
  UIMaster
  (
   String hostname, 
   int port
  ) 
  {
    pMasterMgrClient = new MasterMgrClient(hostname, port);

    pOpsLock = new ReentrantLock();

    pNodeBrowsers  = new JNodeBrowserPanel[10];
    pNodeViewers   = new JNodeViewerPanel[10];

    SwingUtilities.invokeLater(new SplashFrameTask(this));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the user interface and connection to <B>plmaster<B>(1).
   * 
   * @param hostname
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param port
   *   The port number listened to by <B>plmaster</B>(1) for incoming connections.
   */ 
  public static void 
  init
  (
   String hostname, 
   int port
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(hostname, port);
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the main application frame.
   */
  public JFrame
  getFrame()
  {
    return pFrame;
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
  private void 
  setLayoutName
  (
   String name
  ) 
  {
    pLayoutName = name;

    File path = new File(pLayoutName);
    pFrame.setTitle("plui [" + path.getName() + "]");
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Assign the given node browser to the given group.
   */ 
  public void 
  assignNodeBrowserGroup
  (
   JNodeBrowserPanel panel, 
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    assert(pNodeBrowsers[groupID] == null);
    pNodeBrowsers[groupID] = panel;
  }

  /**
   * Make the given node browser group available.
   */ 
  public void 
  releaseNodeBrowserGroup 
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    assert(pNodeBrowsers[groupID] != null);
    pNodeBrowsers[groupID] = null;
  }

  /**
   * Is the given node browser group currently unused.
   */ 
  public boolean
  isNodeBrowserGroupUnused
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    return (pNodeBrowsers[groupID] == null);      
  }

  /**
   * Get the node browser belonging to the given group.
   * 
   * @return 
   *   The node browser or <CODE>null</CODE> if no browser exists for the group.
   */ 
  public JNodeBrowserPanel
  getNodeBrowser
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    return pNodeBrowsers[groupID];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Assign the given node viewer to the given group.
   */ 
  public void 
  assignNodeViewerGroup
  (
   JNodeViewerPanel panel, 
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    assert(pNodeViewers[groupID] == null);
    pNodeViewers[groupID] = panel;
  }

  /**
   * Make the given node viewer group available.
   */ 
  public void 
  releaseNodeViewerGroup 
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    assert(pNodeViewers[groupID] != null);
    pNodeViewers[groupID] = null;
  }

  /**
   * Is the given node viewer group currently unused.
   */ 
  public boolean
  isNodeViewerGroupUnused
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    return (pNodeViewers[groupID] == null);      
  }

  /**
   * Get the node viewer belonging to the given group.
   * 
   * @return 
   *   The node viewer or <CODE>null</CODE> if no viewer exists for the group.
   */ 
  public JNodeViewerPanel
  getNodeViewer
  (
   int groupID
  ) 
  {
    assert((groupID > 0) && (groupID < 10));
    return pNodeViewers[groupID];
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
    JLabel label = new JLabel(text);

    Dimension size = new Dimension(width, 19);
    label.setMinimumSize(size);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    label.setPreferredSize(size);
    
    label.setHorizontalAlignment(align);
    
    return label;
  }
  
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

  
  /**
   * Create a new non-editable text field.
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
   * Create a new editable text field.
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
    JTextField field = new JTextField(text);
    field.setName("EditableTextField");

    Dimension size = new Dimension(width, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(align);
    field.setEditable(true);
    
    return field;
  }

  /**
   * Create a new editable text field which can only contain identifiers.
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

    JTextField field = createTextField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

    JTextField field = createEditableTextField(text, vwidth, JLabel.CENTER);
    vpanel.add(field);

    return field;
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

    JHotKeyField field = new JHotKeyField();
    field.setName("HotKeyField"); 

    Dimension size = new Dimension(vwidth, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
    field.setPreferredSize(size);
    
    field.setHorizontalAlignment(JLabel.CENTER);
    
    vpanel.add(field);

    return field;
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

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
    return createTitledSlider(tpanel, title, twidth, 
			      vpanel, (int)(vmin*1000.0), (int)(vmax*1000.0), vwidth);
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

    JCollectionField field = new JCollectionField(values);

    Dimension size = new Dimension(vwidth, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(size);

    vpanel.add(field);

    return field;
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
    tpanel.add(UIMaster.createLabel(title, twidth, JLabel.RIGHT));

    JBooleanField field = new JBooleanField();

    Dimension size = new Dimension(vwidth, 19);
    field.setMinimumSize(size);
    field.setMaximumSize(size);

    vpanel.add(field);

    return field;
  }



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
      SwingUtilities.invokeLater(new SaveLayoutTask());
    else 
      showSaveLayoutDialog();
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
   * Close the network connection and exit.
   */ 
  public void 
  doQuit()
  {
    if(pMasterMgrClient != null) 
      pMasterMgrClient.disconnect();

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
      pMaster = master;
    }

    public void 
    run() 
    {  
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

	  /* progress bar */ 
	  {
	    JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 146);
	    pSplashProgress = bar;

	    bar.setValue(1);
	    
	    panel.add(bar);
	  }

	  frame.setContentPane(panel);
	}

	frame.pack();
	frame.setLocationRelativeTo(null);

	frame.setVisible(true);
      }
      
      /* perform application startup tasks */ 
      StartupTask task = new StartupTask(pMaster);
      task.start();
    }

    private UIMaster  pMaster;
  }

  /** 
   * Perform application startup tasks.
   */ 
  private
  class StartupTask
    extends Thread
  {
    StartupTask
    (
     UIMaster master
    ) 
    {
      pMaster = master;
      pCnt    = 1;
    }

    public void 
    run() 
    {  
      int i;

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
      
      /* load textures */ 
      try {
	TextureMgr mgr = TextureMgr.getInstance();

	for(OverallNodeState nstate : OverallNodeState.all()) {
	  for(OverallQueueState qstate : OverallQueueState.all()) {
	    for(SelectionMode mode : SelectionMode.all()) {
	      mgr.verifyTexture(nstate + "-" + qstate + "-" + mode);
	      update();
	    }
	  }
	}

	for(SelectionMode mode : SelectionMode.all()) {
	  mgr.verifyTexture("Blank-" + mode);
	  update();

	  mgr.verifyTexture("Collapsed-" + mode);
	  update();
	}

	{
	  mgr.registerFont("CharterBTRoman", new CharterBTRomanFontGeometry());
	  mgr.verifyFontTextures("CharterBTRoman");
	  pCnt += 10;
	  SwingUtilities.invokeLater(new UpdateStartupProgress(pCnt));
	}

	mgr.verifySimpleTexture("White");
	mgr.verifySimpleTexture("Yellow");
	mgr.verifySimpleTexture("LightGrey");
	update();

	for(LinkRelationship rel : LinkRelationship.all()) {
	  mgr.verifyTexture("LinkRelationship-" + rel);
	  update();
	}
      }
      catch(IOException ex) {
	Logs.tex.severe("Unable to load textures!\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);
      }
      
      SwingUtilities.invokeLater(new MainFrameTask(pMaster));
    }

    private void
    update()
    {
      SwingUtilities.invokeLater(new UpdateStartupProgress(pCnt++));
    }

    private UIMaster  pMaster;
    private int       pCnt;
  }

  /** 
   * Update the splash frame progress bar.
   */ 
  private
  class UpdateStartupProgress
    extends Thread
  {
    public 
    UpdateStartupProgress
    (
     int value
    ) 
    {
      pValue = value;
    }

    public void 
    run() 
    {  
      pSplashProgress.setValue(pValue);
    }

    private int pValue;
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
      pMaster = master;
    }

    public void 
    run() 
    {
      /* hide the splash screen */ 
      pSplashFrame.setVisible(false);
      
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
	      JManagerPanel mgr = new JManagerPanel();
	      pRootManagerPanel = mgr;
	      mgr.setContents(new JEmptyPanel());
	      
	      panel.add(mgr);
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
	
	frame.pack();
	frame.setLocationRelativeTo(null);

      }

      {
	pSaveLayoutDialog    = new JSaveLayoutDialog();
	pManageLayoutsDialog = new JManageLayoutsDialog();
	pErrorDialog         = new JErrorDialog();
	pUserPrefsDialog     = new JUserPrefsDialog();
	pAboutDialog         = new JAboutDialog();
	pConfigDialog        = new JConfigDialog();
      }
      
      pFrame.setVisible(true);
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
	    SwingUtilities.invokeLater(new SaveLayoutTask());
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
    public void 
    run() 
    {
      try {
	File file = new File(PackageInfo.sHomeDir, 
			     PackageInfo.sUser + "/.pipeline/layouts/" + pLayoutName);

	System.out.print("SaveLayout = " + pLayoutName + "\n");

	File dir = file.getParentFile();
	if(!dir.isDirectory()) 
	  dir.mkdirs();

	PanelLayout layout = new PanelLayout(pRootManagerPanel, pFrame.getSize());
	LockedGlueFile.save(file, "PanelLayout", layout);
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
      pName = name;
    }

    public void 
    run() 
    {
      /* clean up existing panels */ 
      {
	pRootManagerPanel = null;
	pRootPanel.removeAll();
	
	int wk;
	for(wk=1; wk<10; wk++) {
	  pNodeBrowsers[wk] = null;
	  pNodeViewers[wk] = null;
	}
      }

      /* restore saved panels */ 
      PanelLayout layout = null;
      try {      
	File file = new File(PackageInfo.sHomeDir, 
			     PackageInfo.sUser + "/.pipeline/layouts" + pName);
	layout = (PanelLayout) LockedGlueFile.load(file);

	setLayoutName(pName);
	pFrame.setSize(layout.getSize());
	pRootManagerPanel = layout.getRoot();
      }
      catch(Exception ex) {
	pRootManagerPanel = new JManagerPanel();
	pRootManagerPanel.setContents(new JEmptyPanel());
	showErrorDialog(ex);
      }

      /* replace the root panel */ 
      pRootPanel.add(pRootManagerPanel);
      pRootPanel.validate();
      pRootPanel.repaint();
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
    public void 
    run() 
    {
      try {
	if(pRootManagerPanel != null) 
	  pRootManagerPanel.updateUserPrefs();
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
    public void 
    run() 
    {
      pErrorDialog.setVisible(true);
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


  /**
   * The name of the last saved/loaded layout.
   */ 
  private String  pLayoutName;
 
  /**
   * The parent of the root manager panel.
   */ 
  private JPanel  pRootPanel; 

  /**
   * The root manager panel.
   */ 
  private JManagerPanel  pRootManagerPanel; 


  /**
   * The table of active node browsers indexed by assigned group: [1-9]. <P> 
   * 
   * If no node browser is assigned to the group, the element will be <CODE>null</CODE>. 
   * The (0) element is always <CODE>null</CODE>, because the (0) group ID means unassinged.
   */ 
  private JNodeBrowserPanel[]  pNodeBrowsers;

  /**
   * The table of active node viewers indexed by assigned group: [1-9]. <P> 
   * 
   * If no node viewer is assigned to the group, the element will be <CODE>null</CODE>. 
   * The (0) element is always <CODE>null</CODE>, because the (0) group ID means unassinged.
   */ 
  private JNodeViewerPanel[]  pNodeViewers;


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
}
