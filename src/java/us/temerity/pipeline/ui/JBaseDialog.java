// $Id: JBaseDialog.java,v 1.17 2009/07/13 17:26:02 jlee Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all model dialogs.
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
   * Construct a new dialog owned by a top-level frame.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  protected
  JBaseDialog
  (
   Frame owner,        
   String title
  ) 
  {
    super(owner, title, true);
  }

  /**
   * Construct a new dialog owned by another dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  protected
  JBaseDialog
  (
   Dialog owner,        
   String title
  ) 
  {
    super(owner, title, true);
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
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

    if(header != null) {
      JPanel panel = new JPanel();
      panel.setName("ModalDialogHeader");	
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      {
	JLabel label = new JLabel(header);
	pHeaderLabel = label;

	label.setName("DialogHeaderLabel");

	/* Specify a minimum and maximum size for the label, so that long header strings 
	   will be displayed with trailing "..." */
	{
	  int height = label.getPreferredSize().height;

	  label.setMinimumSize(new Dimension(1, height));
	  label.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
	}

	panel.add(label);	  
      }
      
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }	  
    
    if(body != null) 
      root.add(body);

    JButton[] extraBtns = null;
    pExtraButtons = new TreeMap<String,JButton>();
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
	JButton btn = UIFactory.createConfirmButton(confirm, "confirm", this, null); 
	pConfirmButton = btn;
	
	panel.add(btn);	  
      }
	
      if((confirm != null) && (apply != null))
	panel.add(Box.createRigidArea(new Dimension(20, 0)));
     
      if(apply != null) {
        JButton btn = UIFactory.createDialogButton(apply, "apply", this, null); 
	pApplyButton = btn;

	panel.add(btn);	  
      }

      if(((confirm != null) || (apply != null)) && (extra != null)) 
	panel.add(Box.createRigidArea(new Dimension(20, 0)));

      if(extra != null) {
	extraBtns = new JButton[extra.length];

	int wk;
	for(wk=0; wk<extra.length; wk++) {
	  if(extra[wk] != null) {
            JButton btn = 
              UIFactory.createDialogButton(extra[wk][0], extra[wk][1], this, null); 

	    extraBtns[wk] = btn;
            pExtraButtons.put(extra[wk][0], btn);

	    panel.add(btn);	  
	  }

	  if(wk<(extra.length-1)) 
	    panel.add(Box.createRigidArea(new Dimension(20, 0)));	     
	}
      }

      if(((confirm != null) || (apply != null) || (extra != null)) && (cancel != null))
	panel.add(Box.createRigidArea(new Dimension(40, 0)));
     
      if(cancel != null) {
	JButton btn = UIFactory.createCancelButton(cancel, "cancel", this, null); 
	pCancelButton = btn;

	panel.add(btn);	  
      }
      
      panel.add(Box.createRigidArea(new Dimension(20, 0)));
      panel.add(Box.createHorizontalGlue());
      
      root.add(panel);
    }

    setContentPane(root);

    pack();

    {
      Window owner = getOwner();
      if(owner != null) {
	setLocationRelativeTo(owner);
      }
      else {
	Rectangle bounds = getGraphicsConfiguration().getBounds();
	setLocation(bounds.x + bounds.width/2 - getWidth()/2, 
		    bounds.y + bounds.height/2 - getHeight()/2);		    
      }
    }

    return extraBtns;
  }
     


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
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
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the confirm foooter button or <CODE>null</CODE> if none exists.
   */ 
  public JButton
  getConfirmButton() 
  {
    return pConfirmButton; 
  }

  /**
   * Get the apply foooter button or <CODE>null</CODE> if none exists.
   */ 
  public JButton
  getApplyButton() 
  {
    return pApplyButton; 
  }

  /**
   * Get the extra btton with the given title.
   * 
   * @param title
   *   The title of the extra button given in the constructor.
   */ 
  public JButton
  getExtraButton
  (
   String title
  ) 
  {
    return pExtraButtons.get(title);
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
   * The extra footer buttons indexed by title.
   */ 
  private TreeMap<String,JButton>  pExtraButtons;

}
