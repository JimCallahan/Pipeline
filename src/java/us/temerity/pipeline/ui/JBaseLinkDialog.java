// $Id: JBaseLinkDialog.java,v 1.1 2004/06/28 23:39:45 jim Exp $

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
   */ 
  protected 
  JBaseLinkDialog
  (
   String title, 
   String header, 
   String confirm
  ) 
  {
    super(title, true);

    pLinkCatagories = new TreeMap<String,LinkCatagory>();

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
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  JCollectionField field = 
	    UIMaster.createTitledCollectionField(tpanel, "Link Catagory:", sTSize, 
						 vpanel, values, sVSize);
	  pCatagoryField = field;

	  field.addActionListener(this);
	  field.setActionCommand("catagory-changed");
	}
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pPolicyField =
	  UIMaster.createTitledTextField(tpanel, "Link Policy:", sTSize, 
					 vpanel, null, sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	
	{
	  JCollectionField field = 
	    UIMaster.createTitledCollectionField(tpanel, "Link Relationship:", sTSize, 
					       vpanel, LinkRelationship.titles(), sVSize);
	
	  pRelationshipField = field;

	  field.addActionListener(this);
	  field.setActionCommand("relationship-changed");
	}
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pOffsetField = 
	  UIMaster.createTitledIntegerField(tpanel, "Frame Offset:", sTSize, 
					    vpanel, null, sVSize);

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI(header, true, body, confirm, null, null, "Cancel");
      pack();
      setResizable(false);
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the link catagory.
   */
  public LinkCatagory
  getLinkCatagory() 
  {
    return pLinkCatagories.get(pCatagoryField.getSelected());
  }
    
  /**
   * Get the link relationship
   */ 
  public LinkRelationship
  getLinkRelationship() 
  {
    return LinkRelationship.values()[pRelationshipField.getSelectedIndex()];
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
   LinkCatagory lcat, 
   LinkRelationship rel, 
   Integer offset
  ) 
  {
    assert(((rel == LinkRelationship.OneToOne) && (offset != null)) || 
	   ((rel != LinkRelationship.OneToOne) && (offset == null)));

    UIMaster master = UIMaster.getInstance();
    try {      
      MasterMgrClient client = master.getMasterMgrClient();

      TreeMap<String,LinkCatagoryDesc> table = client.getLinkCatagoryDesc();
      TreeSet<String> active = client.getActiveLinkCatagoryNames();

      pLinkCatagories.clear();
      for(String cname : active) 
	pLinkCatagories.put(cname, new LinkCatagory(table.get(cname)));
      pLinkCatagories.put(lcat.getName(), lcat);      

      pCatagoryField.setValues(pLinkCatagories.keySet());
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }

    pCatagoryField.removeActionListener(this);
    {
      pCatagoryField.setSelected(lcat.getName());
      pPolicyField.setText(lcat.getPolicy().toTitle());
    }
    pCatagoryField.addActionListener(this);

    pRelationshipField.removeActionListener(this);
    {
      pRelationshipField.setSelectedIndex(rel.ordinal());
      pOffsetField.setValue(offset);    
    }
    pRelationshipField.addActionListener(this);
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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("catagory-changed")) 
      doCatagoryChanged();
    else if(cmd.equals("relationship-changed")) 
      doRelationshipChanged();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The link catagory has been changed.
   */ 
  public void 
  doCatagoryChanged()
  {
    pPolicyField.setText(getLinkCatagory().getPolicy().toTitle());
  }

  /**
   * The link catagory has been changed.
   */ 
  public void 
  doRelationshipChanged()
  {
    LinkRelationship rel = getLinkRelationship();
    if(rel == LinkRelationship.OneToOne) {
      Integer offset = pOffsetField.getValue();
      if(offset == null) 
	pOffsetField.setValue(0);
    }
    else {
      pOffsetField.setValue(null);
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
   * The active link catagories indexed by catagory name.
   */ 
  private TreeMap<String,LinkCatagory>  pLinkCatagories; 

  /**
   * The link catagory.
   */ 
  private JCollectionField pCatagoryField; 
  
  /**
   * The link policy.
   */ 
  private JTextField  pPolicyField; 


  /**
   * The link relationship field.
   */ 
  private JCollectionField  pRelationshipField;
   
  /**
   * The frame offset field.
   */ 
  private JIntegerField  pOffsetField;
  
}
