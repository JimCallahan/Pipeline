// $Id: JCreateLicenseKeyDialog.java,v 1.1 2004/07/24 18:28:45 jim Exp $

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
/*   C R E A T E   L I C E N S E   K E Y   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new license key.
 */ 
public 
class JCreateLicenseKeyDialog
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
  JCreateLicenseKeyDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New License Key", true);

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	JPanel tpanel = null;
	{
	  tpanel = new JPanel();
	  tpanel.setName("TitlePanel");
	  tpanel.setLayout(new BoxLayout(tpanel, BoxLayout.Y_AXIS));

	  body.add(tpanel);
	}

	JPanel vpanel = null;
	{
	  vpanel = new JPanel();
	  vpanel.setName("ValuePanel");
	  vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.Y_AXIS));

	  body.add(vpanel);
	}

	{
	  JIdentifierField field = 
	    UIMaster.createTitledIdentifierField(tpanel, "Key Name:", sTSize, 
						 vpanel, "", sVSize);
	  pKeyNameField = field;
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextArea area = 
	    UIMaster.createTitledEditableTextArea(tpanel, "Description:", sTSize, 
						  vpanel, "", sVSize, 3, false);
	  pDescriptionArea = area;
	  area.getDocument().addDocumentListener(this);
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("New License Key:", true, body, "Add", null, null, "Close");
      pack();
    }  

    pConfirmButton.setEnabled(false);
    setResizable(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the license key.
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

  private static final long serialVersionUID = -7369529337944272888L;

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
