// $Id: JNewFolderDialog.java,v 1.1 2004/05/11 19:17:03 jim Exp $

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
/*   N E W   F O L D E R   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JNewFolderDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewFolderDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Folder", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("NewFolderPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JLabel label = new JLabel("New Folder Name:");
	  label.setName("PanelLabel");
	  
	  hbox.add(label);
	}
	
	hbox.add(Box.createHorizontalGlue());
	
	body.add(hbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JIdentifierField field = UIMaster.createIdentifierField(null, 60, JLabel.LEFT);
	pNameField = field;
	
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
    String text = pNameField.getText();
    pConfirmButton.setEnabled((text != null) && (text.length() > 0));
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
    String text = pNameField.getText();
    pConfirmButton.setEnabled((text != null) && (text.length() > 0));    
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

  private static final long serialVersionUID = -4320568430731398212L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The field containing the new folder name. <P> 
   */
  private JIdentifierField  pNameField;

}
