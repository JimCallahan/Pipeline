// $Id: JWindowRenameDialog.java,v 1.1 2004/10/22 14:02:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   W I N D O W   R E N A M E   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a new name for the current top-level window.
 */ 
public 
class JWindowRenameDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JWindowRenameDialog
  (
   String name
  )
  {
    super("Rename Window", true); 

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIMaster.createPanelLabel("New Window Name:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextField field = UIMaster.createEditableTextField(name, 60, JLabel.LEFT);
	pNameField = field;
	
	body.add(field);
      }
	  
      super.initUI(null, true, body, "Rename", null, null, "Cancel");
    }  

    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the new folder. <P>
   * 
   * @return
   *   The folder name.
   */ 
  public String
  getName() 
  {
    return (pNameField.getText());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8360927556757670807L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The field containing the new window name. <P> 
   */
  protected JTextField  pNameField;

}

