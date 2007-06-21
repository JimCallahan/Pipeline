// $Id: JBaseLinkDialog.java,v 1.6 2007/06/21 16:40:50 jim Exp $

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
/*   B A S E   L I N K   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Quieries the user for information needed to create/modify links between nodes.
 */ 
public 
class JBaseLinkDialog
  extends JBaseDialog
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
  protected 
  JBaseLinkDialog
  (
   Frame owner,
   String title, 
   String header, 
   String confirm
  ) 
  {
    super(owner, title);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	{
	  JCollectionField field = 
	    UIFactory.createTitledCollectionField(tpanel, "Link Policy:", sTSize, 
                                                  vpanel, LinkPolicy.titles(), sVSize);
	  pPolicyField = field;

	  field.addActionListener(this);
	  field.setActionCommand("policy-changed");
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add(LinkRelationship.All.toTitle());
	  values.add(LinkRelationship.OneToOne.toTitle());

	  JCollectionField field = 
	    UIFactory.createTitledCollectionField(tpanel, "Link Relationship:", sTSize, 
						 vpanel, values, sVSize);
	  pRelationshipField = field;

	  field.addActionListener(this);
	  field.setActionCommand("relationship-changed");
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pOffsetField = 
	  UIFactory.createTitledIntegerField(tpanel, "Frame Offset:", sTSize, 
					    vpanel, null, sVSize);

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI(header, body, confirm, null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the link catagory.
   */
  public LinkPolicy
  getPolicy() 
  {
    return LinkPolicy.values()[pPolicyField.getSelectedIndex()];
  }
    
  /**
   * Get the link relationship
   */ 
  public LinkRelationship
  getRelationship() 
  {
    if(pRelationshipField.getSelected().equals("1:1")) 
      return LinkRelationship.OneToOne;
    else if(pRelationshipField.getSelected().equals("All")) 
      return LinkRelationship.All;

    assert(false);
    return null;
  }

  /**
   * Get the frame offset.
   */ 
  public Integer
  getFrameOffset() 
  {
    return pOffsetField.getValue();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the UI components.
   */ 
  protected void
  updateLink
  (
   LinkPolicy policy, 
   LinkRelationship rel, 
   Integer offset
  ) 
  {
    pPolicyField.setSelectedIndex(policy.ordinal());

    switch(rel) {
    case OneToOne:
      pRelationshipField.setSelected("1:1");
      break;
      
    case All:
      pRelationshipField.setSelected("All");
    }
    
    if(offset != null) 
      pOffsetField.setValue(offset);    
  }
  


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
    String cmd = e.getActionCommand();
    if(cmd.equals("policy-changed")) 
      doPolicyChanged();
    else if(cmd.equals("relationship-changed")) 
      doRelationshipChanged();
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The link policy has been changed.
   */ 
  public void 
  doPolicyChanged()
  {
    LinkRelationship rel = getRelationship();
    
    pRelationshipField.removeActionListener(this);
    {
      ArrayList<String> values = new ArrayList<String>();
      values.add(LinkRelationship.All.toTitle());
      values.add(LinkRelationship.OneToOne.toTitle());
      pRelationshipField.setValues(values);
      pRelationshipField.setEnabled(true);
    }
    pRelationshipField.addActionListener(this);
    
    pRelationshipField.setSelected(rel.toTitle());
  }

  /**
   * The link relationship has been changed.
   */ 
  public void 
  doRelationshipChanged()
  {
    switch(getRelationship()) {
    case OneToOne:
      {
	pOffsetField.setEnabled(true);
	Integer offset = pOffsetField.getValue();
	if(offset == null) 
	  pOffsetField.setValue(0);
      }
      break;

    default:
      pOffsetField.setValue(null);
      pOffsetField.setEnabled(false);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9134252748976773169L;
  
  private static final int sTSize = 120;
  private static final int sVSize = 180;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The link policy field.
   */ 
  private JCollectionField  pPolicyField; 

  /**
   * The link relationship field.
   */ 
  private JCollectionField  pRelationshipField;
   
  /**
   * The frame offset field.
   */ 
  private JIntegerField  pOffsetField;
  
}
