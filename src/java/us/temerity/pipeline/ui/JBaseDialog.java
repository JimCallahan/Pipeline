// $Id: JBaseDialog.java,v 1.3 2004/05/08 15:06:13 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all application dialogs.
 */ 
public 
class JBaseDialog
  extends JDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog owned by the main application frame. <P> 
   * 
   * The button title arguments <CODE>confirm</CODE>, <CODE>apply</CODE> and 
   * <CODE>cancel</CODE> may be <CODE>null</CODE> to omit the button(s) from the dialog. 
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param modal
   *   Is the dialog modal?
   */ 
  protected
  JBaseDialog
  (
   String title,    
   boolean modal
  ) 
  {
    super(UIMaster.getInstance().getFrame(), title, modal);
  }

  /**
   * Construct a new dialog owned by another dialog. <P> 
   * 
   * The button title arguments <CODE>confirm</CODE>, <CODE>apply</CODE> and 
   * <CODE>cancel</CODE> may be <CODE>null</CODE> to omit the button(s) from the dialog. 
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param modal
   *   Is the dialog modal?
   */ 
  protected
  JBaseDialog
  (
   Dialog owner,        
   String title,  
   boolean modal
  ) 
  {
    super(owner, title, modal);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   * 
   * The button title arguments <CODE>confirm</CODE>, <CODE>apply</CODE> and 
   * <CODE>cancel</CODE> may be <CODE>null</CODE> to omit the button(s) from the dialog. 
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param modal
   *   Is the dialog modal?
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
   * @param cancel
   *   The title of the cancel button.
   */ 
  protected void 
  initUI
  (
   String title,  
   boolean modal, 
   JComponent body, 
   String confirm, 
   String apply, 
   String cancel
  ) 
  {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

    {
      JPanel panel = new JPanel();
      panel.setName(modal ? "ModalDialogHeader" : "DialogHeader");	
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      {
	JLabel label = new JLabel(title);
	label.setName("DialogHeaderLabel");
	
	panel.add(label);	  
      }
      
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }	  
    
    if(body != null) 
      root.add(body);

    {
      JPanel panel = new JPanel();
      
      panel.setName("DialogButtonPanel");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      panel.add(Box.createHorizontalGlue());
      panel.add(Box.createRigidArea(new Dimension(20, 0)));

      if(confirm != null) {
	JButton btn = new JButton(confirm);
	pConfirmButton = btn;
	btn.setName("RaisedConfirmButton");
	
	Dimension size = new Dimension(108, 31);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
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
	
	Dimension size = new Dimension(108, 31);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	btn.setActionCommand("apply");
	btn.addActionListener(this);
	
	panel.add(btn);	  
      }
      
      if(((confirm != null) || (apply != null)) && (cancel != null))
	panel.add(Box.createRigidArea(new Dimension(40, 0)));
     
      if(cancel != null) {
	JButton btn = new JButton(cancel);
	pCancelButton = btn;
	btn.setName("RaisedCancelButton");

	Dimension size = new Dimension(108, 31);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);

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
    setLocationRelativeTo(getOwner());
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
   boolean tf
  )
  {
    if(tf)
      pConfirmed = false;

    super.setVisible(tf);
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(e.getActionCommand().equals("confirm")) 
      doConfirm();
    else if(e.getActionCommand().equals("apply")) 
      doApply();
    else if(e.getActionCommand().equals("cancel")) 
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
   * The footer buttons.
   */ 
  protected JButton  pConfirmButton;
  protected JButton  pApplyButton;
  protected JButton  pCancelButton;
}
