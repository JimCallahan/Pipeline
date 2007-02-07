// $Id: JAuthorizeDialog.java,v 1.1 2007/02/07 21:19:36 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   A U T H O R I Z E   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for entering a user's Windows password.
 */ 
public 
class JAuthorizeDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JAuthorizeDialog
  ( 
   Frame owner
  ) 
  {
    super(owner, "Windows Authorize");

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	pPasswordField = 
	  UIFactory.createTitledPasswordField
	  (tpanel, "Windows Password:", sTSize, 
	   vpanel, sVSize, 
	   "Provide your Windows password to authorize Pipeline to run jobs on your behalf " +
	   "on Windows based Job Servers.");

	UIFactory.addVerticalSpacer(tpanel, vpanel, 6);

	pConfirmField = 
	  UIFactory.createTitledPasswordField
	  (tpanel, "Confirm Password:", sTSize, 
	   vpanel, sVSize, 
	   "Confirm your Windows password.");

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("Authorize Windows User:  " + PackageInfo.sUser, 
		   body, "Save", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current password. <P> 
   */ 
  public char[] 
  getPassword() 
  {
    return pPassword;
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
    if(isVisible) {
      pPasswordField.setText(null);
      pConfirmField.setText(null);
    }

    super.setVisible(isVisible);
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
    char[] pw1 = null;
    try {
      pw1 = pPasswordField.getPassword();
    }
    catch(NullPointerException ex) {
    }

    char[] pw2 = null;
    try {
      pw2 = pConfirmField.getPassword();
    }
    catch(NullPointerException ex) {
    }

    try {
      if((pw1 == null) || (pw1.length == 0)) 
	throw new PipelineException
	  ("You must supply a password!");

      if((pw2 == null)  || (pw2.length == 0)) 
	throw new PipelineException
	  ("You must confirm your password!");
      
      if(!Arrays.equals(pw1, pw2)) 
	throw new PipelineException
	  ("The supplied passwords do not match!");
      
      pPassword = pw1; 
      
      pPasswordField.setText(null);
      pConfirmField.setText(null);

      if(pw2 != null) 
	Arrays.fill(pw2, '?');

      super.doConfirm(); 
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4693560339644237725L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 150;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Last successfully entered password.
   */
  private char[]  pPassword;

  /**
   * Windows password fields.
   */
  private JPasswordField  pPasswordField; 
  private JPasswordField  pConfirmField; 

}
