// $Id: JBaseDialog.java,v 1.1 2004/05/02 12:13:34 jim Exp $

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
   * Construct a new dialog owned by the main application frame.
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
    initUI(title, modal);
  }

  /**
   * Construct a new dialog owned by another dialog.
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
    initUI(title, modal);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param modal
   *   Is the dialog modal?
   */ 
  private void 
  initUI
  (
   String title,  
   boolean modal
  ) 
  {
    String prefix = modal ? "Modal" : "";

    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

    {
      JPanel panel = new JPanel();
      panel.setName(prefix + "DialogHeader");	
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      {
	JLabel label = new JLabel(title);
	label.setName(prefix + "DialogHeaderLabel");
	
	panel.add(label);	  
      }
      
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }	  
    

    // ...
    
    
    root.add(Box.createVerticalGlue());

    // ...
    

    {
      JPanel panel = new JPanel();
      
      panel.setName("DialogButtonPanel");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      panel.add(Box.createHorizontalGlue());
      panel.add(Box.createRigidArea(new Dimension(20, 0)));

      {
	JButton btn = new JButton("Confirm");
	btn.setName("RaisedConfirmButton");

	Dimension size = new Dimension(108, 31);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);

	btn.setActionCommand("confirm");
	btn.addActionListener(this);

	panel.add(btn);	  
      }
	
      panel.add(Box.createRigidArea(new Dimension(40, 0)));
     
      {
	JButton btn = new JButton("Cancel");
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  // ...


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
  protected boolean pConfirmed;

  
  
}
