// $Id: JRestoreDialog.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for selecting the checked-in versions to restore.
 */ 
public 
class JRestoreDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRestoreDialog() 
  {
    super("Restore Tool", false);

    /* create dialog body components */ 
    {
	
      JPanel lpanel = new JPanel();
      {
	lpanel.setName("MainDialogPanel");
	lpanel.setLayout(new BoxLayout(lpanel, BoxLayout.Y_AXIS));

	// ...
	
      }

      JPanel rpanel = new JPanel();
      {
	rpanel.setName("MainDialogPanel");
	rpanel.setLayout(new BoxLayout(rpanel, BoxLayout.Y_AXIS));

	// ...
	
      }


      JSplitPane body = new JHorzSplitPanel(lpanel, rpanel);
      
      super.initUI("Restore Tool:", false, body, "Confirm", "Apply", null, "Close");
    }
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  


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
    super.actionPerformed(e);


  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 398897653863238367L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 

}
