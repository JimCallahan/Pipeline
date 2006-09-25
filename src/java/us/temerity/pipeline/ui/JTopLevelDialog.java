// $Id: JTopLevelDialog.java,v 1.1 2006/09/25 12:11:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   T O P   L E V E L   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all top-level non-modal application dialogs.
 */ 
public 
class JTopLevelDialog
  extends JFrame
  implements ActionListener, WindowListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new top-level frame. 
   * 
   * @param title
   *   The title of the dialog window.
   * 
   * @param modal
   *   Is the dialog modal?
   */ 
  protected
  JTopLevelDialog
  (
   String title
  ) 
  {
    super(title); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   * 
   * The button title arguments <CODE>confirm</CODE>, <CODE>apply</CODE> and 
   * <CODE>cancel</CODE> may be <CODE>null</CODE> to omit the button(s) from the dialog. 
   * 
   * @param header
   *   The text displayed in the dialog header. 
   * 
   * @param body
   *   The component containing the body of the dialog.
   * 
   * @param confirm
   *   The title of the confirm button.
   * 
   * @param apply
   *   The title of the apply button.
   * 
   * @param extra
   *   An array of title/action-command strings pairs used to create extra buttons.
   * 
   * @param cancel
   *   The title of the cancel button.
   * 
   * @return 
   *   The array of created extra buttons or <CODE>null</CODE> if extra was <CODE>null</CODE>.
   */ 
  protected JButton[]
  initUI
  (
   String header, 
   JComponent body, 
   String confirm, 
   String apply, 
   String[][] extra,
   String cancel
  ) 
  {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(this);

    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

    if(header != null) {
      JPanel panel = new JPanel();
      panel.setName("DialogHeader");	
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      {
	JLabel label = new JLabel(header);
	pHeaderLabel = label;

	label.setName("DialogHeaderLabel");	

	panel.add(label);	  
      }
      
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }	  
    
    if(body != null) 
      root.add(body);

    JButton[] extraBtns = null;
    {
      JPanel panel = new JPanel();
      
      String mac = "";
      switch(PackageInfo.sOsType) {
      case MacOS:
	mac = "Mac";
      }
      panel.setName(mac + ((body != null) ? "DialogButtonPanel" : "DialogButtonPanel2"));

      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      panel.add(Box.createHorizontalGlue());
      panel.add(Box.createRigidArea(new Dimension(20, 0)));

      if(confirm != null) {
	JButton btn = new JButton(confirm);
	pConfirmButton = btn;
	btn.setName("RaisedConfirmButton");
	
	Dimension size = btn.getPreferredSize();
	btn.setMinimumSize(new Dimension(108, 31));
	btn.setMaximumSize(new Dimension(size.width, 31));
	
	btn.setActionCommand("confirm");
	btn.addActionListener(this);
	
	panel.add(btn);	  
      }
	
      if((confirm != null) && (apply != null))
	panel.add(Box.createRigidArea(new Dimension(20, 0)));
     
      if(apply != null) {
	JButton btn = new JButton(apply);
	pApplyButton = btn;
	btn.setName("RaisedButton");
	
	Dimension size = btn.getPreferredSize();
	btn.setMinimumSize(new Dimension(108, 31));
	btn.setMaximumSize(new Dimension(size.width, 31));
	
	btn.setActionCommand("apply");
	btn.addActionListener(this);
	
	panel.add(btn);	  
      }

      if(((confirm != null) || (apply != null)) && (extra != null)) 
	panel.add(Box.createRigidArea(new Dimension(20, 0)));

      if(extra != null) {
	extraBtns = new JButton[extra.length];

	int wk;
	for(wk=0; wk<extra.length; wk++) {
	  if(extra[wk] != null) {
	    JButton btn = new JButton(extra[wk][0]);
	    extraBtns[wk] = btn;
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand(extra[wk][1]);
	    btn.addActionListener(this);

	    panel.add(btn);	  
	  }

	  if(wk<(extra.length-1)) 
	    panel.add(Box.createRigidArea(new Dimension(20, 0)));	     
	}
      }

      if(((confirm != null) || (apply != null) || (extra != null)) && (cancel != null))
	panel.add(Box.createRigidArea(new Dimension(40, 0)));
     
      if(cancel != null) {
	JButton btn = new JButton(cancel);
	pCancelButton = btn;
	btn.setName("RaisedCancelButton");

	Dimension size = btn.getPreferredSize();
	btn.setMinimumSize(new Dimension(108, 31));
	btn.setMaximumSize(new Dimension(size.width, 31));

	btn.setActionCommand("cancel");
	btn.addActionListener(this);

	panel.add(btn);	  
      }
      
      panel.add(Box.createRigidArea(new Dimension(20, 0)));
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }

    setContentPane(root);

    pack();

    {
      Rectangle bounds = getGraphicsConfiguration().getBounds();
      setLocation(bounds.x + bounds.width/2 - getWidth()/2, 
		  bounds.y + bounds.height/2 - getHeight()/2);		    
    }

    return extraBtns;
  }

     

  /*----------------------------------------------------------------------------------------*/
  /*   D I A L O G S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Show an error message dialog for the given exception.
   */ 
  public void 
  showErrorDialog
  (
   Exception ex
  ) 
  {
    if(pErrorDialog == null) 
      pErrorDialog = new JErrorDialog(this);

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
    if(pErrorDialog == null) 
      pErrorDialog = new JErrorDialog(this);

    pErrorDialog.setMessage(title, msg);
    SwingUtilities.invokeLater(new ShowErrorDialogTask());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Was the dialog confirmed?
   */ 
  public boolean 
  wasConfirmed()
  {
    return pConfirmed;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(isVisible)
      pConfirmed = false;

    super.setVisible(isVisible);
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
    doCancel();
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
    String cmd = e.getActionCommand();
    if(cmd.equals("confirm")) 
      doConfirm();
    else if(cmd.equals("apply")) 
      doApply();
    else if(cmd.equals("cancel")) 
      doCancel();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    pConfirmed = true;
    setVisible(false);
  }

  /**
   * Apply changes and continue. 
   */ 
  public void 
  doApply()
  {}

  /**
   * Cancel changes and close.
   */ 
  public void 
  doCancel()
  {
    setVisible(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
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
      super("JTopLevelPanel:ShowErrorDialogTask");
    }

    public void 
    run() 
    {
      pErrorDialog.setVisible(true);
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5264874588692597213L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The state of the dialog upon closing.
   */
  protected boolean  pConfirmed;


  /**
   * The dialog header label.
   */ 
  protected JLabel  pHeaderLabel;


  /**
   * The footer buttons.
   */ 
  protected JButton  pConfirmButton;
  protected JButton  pApplyButton;
  protected JButton  pCancelButton;


  /**
   * The error message dialog.
   */ 
  private JErrorDialog  pErrorDialog;

}
