// $Id: JNewNameDialog.java,v 1.4 2006/10/20 21:01:35 jim Exp $

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
/*   N E W   I D E N T I F I E R   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for an identifier name.
 */ 
public 
class JNewNameDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog owned by the main application frame. <P> 
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param fieldTitle
   *   The title of the text field.
   * 
   * @param name
   *   The initial name. 
   * 
   * @param confirm
   *   The title of the confirm button.
   */ 
  public 
  JNewNameDialog
  (      
   Frame owner, 
   String title,  
   String fieldTitle, 
   String name, 
   String confirm
  )
  {
    super(owner, title);
    initUI(fieldTitle, name, confirm);
  }

  /**
   * Construct a new dialog owned by another dialog. <P> 
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param fieldTitle
   *   The title of the text field.
   * 
   * @param name
   *   The initial name. 
   * 
   * @param confirm
   *   The title of the confirm button.
   */ 
  public 
  JNewNameDialog
  (
   Dialog owner,       
   String title,  
   String fieldTitle, 
   String name, 
   String confirm
  )
  {
    super(owner, title);
    initUI(fieldTitle, name, confirm);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   ( 
   * @param fieldTitle
   *   The title of the text field.
   * 
   * @param name
   *   The initial name. 
   * 
   * @param confirm
   *   The title of the confirm button.
   */ 
  private void 
  initUI
  (      
   String fieldTitle, 
   String name, 
   String confirm
  ) 
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIFactory.createPanelLabel(fieldTitle));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextField field = UIFactory.createEditableTextField(name, 60, JLabel.LEFT);
	pNameField = field;
	
	field.getDocument().addDocumentListener(this);
	
	body.add(field);
      }
	  
      super.initUI(null, body, confirm, null, null, "Cancel");
    }  

    pConfirmButton.setEnabled((name != null) && (name.length() > 0));
    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
  public void 
  changedUpdate(DocumentEvent e) {}

  /**
   * Gives notification that there was an insert into the document.
   */
  public void
  insertUpdate
  (
   DocumentEvent e
  )
  {
    String name = pNameField.getText();
    pConfirmButton.setEnabled((name != null) && (name.length() > 0));
  }
  
  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void 
  removeUpdate
  (
   DocumentEvent e
  )
  {
    String name = pNameField.getText();
    pConfirmButton.setEnabled((name != null) && (name.length() > 0));    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the new name. 
   */ 
  public String
  getName() 
  {
    return (pNameField.getText());
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1405678737818875962L;




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The field containing the new name. <P> 
   */
  protected JTextField  pNameField;

}
