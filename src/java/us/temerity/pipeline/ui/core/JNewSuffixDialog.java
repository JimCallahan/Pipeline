// $Id: JNewSuffixDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   N E W   S U F F I X   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a filename suffix.
 */ 
public 
class JNewSuffixDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JNewSuffixDialog
  (
   Dialog owner
  )
  {
    super(owner, "New Suffix", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIFactory.createPanelLabel("New Filename Suffix:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JAlphaNumField field = UIFactory.createAlphaNumField(null, 60, JLabel.LEFT);
	pSuffixField = field;
	
	field.getDocument().addDocumentListener(this);
	
	body.add(field);
      }
	  
      super.initUI(null, true, body, "Add", null, null, "Cancel");
    }  

    pConfirmButton.setEnabled(false);
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
    String name = pSuffixField.getText();
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
    String name = pSuffixField.getText();
    pConfirmButton.setEnabled((name != null) && (name.length() > 0));    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the new suffix. 
   */ 
  public String
  getSuffix() 
  {
    return (pSuffixField.getText());
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8938852512853651143L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The field containing the new folder name. <P> 
   */
  protected JAlphaNumField  pSuffixField;

}
