// $Id: JBaseCreateKeyDialog.java,v 1.3 2005/02/22 06:07:02 jim Exp $

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
/*   B A S E   C R E A T E   K E Y   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new license key.
 */ 
public 
class JBaseCreateKeyDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBaseCreateKeyDialog
  (
   Dialog owner,
   String title
  )
  {
    super(owner, title, true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];

	{
	  JIdentifierField field = 
	    UIFactory.createTitledIdentifierField(tpanel, "Key Name:", sTSize, 
						 vpanel, "", sVSize);
	  pKeyNameField = field;
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextArea area = 
	    UIFactory.createTitledEditableTextArea(tpanel, "Description:", sTSize, 
						  vpanel, "", sVSize, 3, false);
	  pDescriptionArea = area;
	  area.getDocument().addDocumentListener(this);
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI(title + ":", true, body, "Add", null, null, "Close");
      pack();
    }  

    pConfirmButton.setEnabled(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the key.
   */ 
  public String
  getKeyName()
  {
    return pKeyNameField.getText();
  }
  
  /**
   * Get the description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescriptionArea.getText();
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
      pKeyNameField.setText(null);
      pDescriptionArea.setText(null);
      pConfirmButton.setEnabled(false);
    }

    super.setVisible(isVisible);
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
    String desc = pDescriptionArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));
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
    String desc = pDescriptionArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5336226829405031429L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the license key.
   */ 
  private JIdentifierField  pKeyNameField; 

  /**
   * The description of the entity to create.
   */ 
  private JTextArea  pDescriptionArea;

}
