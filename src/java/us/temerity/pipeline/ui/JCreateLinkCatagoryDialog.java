// $Id: JCreateLinkCatagoryDialog.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   L I N K   C A T A G O R Y   D I A L O G                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Quieries the user for information needed to create a new 
 * {@link LinkCatagoryDesc LinkCatagoryDesc}.
 */ 
public 
class JCreateLinkCatagoryDialog
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
  JCreateLinkCatagoryDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "New Link Catagory", true);

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
	    UIMaster.createTitledIdentifierField(tpanel, "Name:", sTSize, 
						 vpanel, null, sVSize);
	  pNameField = field;

	  field.addActionListener(this);
	  field.setActionCommand("name-changed");
	}
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pPolicyField = 
	  UIMaster.createTitledCollectionField(tpanel, "Link Policy:", sTSize, 
					       vpanel, LinkPolicy.titles(), sVSize);

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextArea area = 
	    UIMaster.createTitledEditableTextArea(tpanel, "Description:", sTSize, 
						  vpanel, null, sVSize, 3);
	  pDescriptionArea = area;
	  area.getDocument().addDocumentListener(this);
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("New Link Catagory:", true, body, "Add", null, null, "Cancel");
      pack();
      setResizable(false);
    }  

    pConfirmButton.setEnabled(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the link catagory.
   */ 
  public String 
  getName() 
  {
    return pNameField.getText();
  }

  /**
   * Get the link policy.
   */
  public LinkPolicy
  getPolicy() 
  {
    return LinkPolicy.values()[pPolicyField.getSelectedIndex()];
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
    validateFields();
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
    validateFields();
  }



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

    if(e.getActionCommand().equals("name-changed")) 
      validateFields();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  validateFields() 
  {
    String name = pNameField.getText();
    String desc = pDescriptionArea.getText();
    pConfirmButton.setEnabled((name != null) && (name.length() > 0) && 
			      (desc != null) && (desc.length() > 0));
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5603251255566976834L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the link catagory.
   */ 
  private JIdentifierField  pNameField;

  /**
   * The link policy.
   */ 
  private JCollectionField  pPolicyField; 

  /**
   * The description of the link catagory.
   */ 
  private JTextArea  pDescriptionArea;

}
