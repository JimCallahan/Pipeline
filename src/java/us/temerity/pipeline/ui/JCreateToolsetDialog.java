// $Id: JCreateToolsetDialog.java,v 1.2 2004/07/07 13:23:38 jim Exp $

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
/*   C R E A T E   T O O L S E T   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number and description of a new toolset toolset.
 */ 
public 
class JCreateToolsetDialog
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
  JCreateToolsetDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Toolset", true);

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
	  JTextArea area = 
	    UIMaster.createTitledEditableTextArea(tpanel, "Description:", sTSize, 
						  vpanel, "", sVSize, 5, false);
	  pDescriptionArea = area;
	  area.getDocument().addDocumentListener(this);
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Add", null, null, "Close");
      pack();
    }  

    pConfirmButton.setEnabled(false);
    setResizable(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescriptionArea.getText();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and clear the description text.
   * 
   * @param header
   *   The header label.
   */ 
  public void  
  updateHeader
  (
   String header
  ) 
  {
    pHeaderLabel.setText(header);
    pDescriptionArea.setText(null);
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

  private static final long serialVersionUID = 5371895217404267867L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The description of the entity to create.
   */ 
  private JTextArea  pDescriptionArea;

}
