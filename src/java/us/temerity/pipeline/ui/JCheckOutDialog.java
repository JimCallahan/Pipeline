// $Id: JCheckOutDialog.java,v 1.6 2004/12/10 10:26:21 jim Exp $

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
/*   C H E C K - O U T   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number and other options of the node to check-out.
 */ 
public 
class JCheckOutDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCheckOutDialog() 
  {
    super("Check-Out Node", true);

    pVersionIDs = new ArrayList<VersionID>();

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
	
	pLatestVersionField = 
	  UIMaster.createTitledTextField(tpanel, "Latest Version:", sTSize, 
					 vpanel, "-", sVSize, 
					 "The revision number of the latest version.");
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  pVersionField = 
	    UIMaster.createTitledCollectionField(tpanel, "Check-Out Version:", sTSize, 
						 vpanel, values, this, sVSize, 
						 "The revision number of the version to " +
						 "check-out.");
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pModeField = 
	  UIMaster.createTitledCollectionField(tpanel, "Check-Out Mode:", sTSize, 
					       vpanel, CheckOutMode.titles(), sVSize, 
					       "The criteria used to determine whether " + 
					       "working versions should be replaced.");

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pMethodField = 
	  UIMaster.createTitledCollectionField(tpanel, "Check-Out Method:", sTSize, 
					       vpanel, CheckOutMethod.titles(), sVSize,
					       "The method for replacing working files.");

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Check-Out", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the node to check-out. <P> 
   * 
   * @return 
   *   The selected revision number or <CODE>null</CODE> if none exists.
   */
  public VersionID
  getVersionID() 
  {
    if(pVersionIDs.size() > 0) 
      return pVersionIDs.get(pVersionField.getSelectedIndex());
    return null;
  }
    
  /**
   * Get the criteria used to determine whether nodes upstream of the root node of 
   * the check-out should also be checked-out.
   */ 
  public CheckOutMode
  getMode() 
  {
    return CheckOutMode.values()[pModeField.getSelectedIndex()];
  }

  /**
   * Get the method for creating working area files/links from the checked-in files.
   */ 
  public CheckOutMethod
  getMethod() 
  {
    return CheckOutMethod.values()[pMethodField.getSelectedIndex()];
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and the revision numbers.
   * 
   * @param header
   *   The header label.
   * 
   * @param vids
   *   The revision numbers of the checked-in versions of the node.
   */ 
  public void 
  updateNameVersions
  (
   String header,
   ArrayList<VersionID> vids
  )
  { 
    pHeaderLabel.setText(header);

    if((vids == null) || (vids.isEmpty()))  {
      pVersionIDs.clear(); 
      
      ArrayList<String> values = new ArrayList<String>();
      values.add("-");

      pVersionField.setValues(values);

      pConfirmButton.setEnabled(false);
    }
    else {
      pVersionIDs.clear(); 
      pVersionIDs.addAll(vids);
      Collections.reverse(pVersionIDs);
      
      pLatestVersionField.setText("v" + pVersionIDs.get(0));
      
      ArrayList<String> values = new ArrayList<String>();
      for(VersionID vid : pVersionIDs) 
	values.add("v" + vid.toString());
      
      pVersionField.setValues(values);
      pVersionField.setSelectedIndex(0);
      
      pConfirmButton.setEnabled(true);
    }

    pModeField.setSelectedIndex(2);
    pMethodField.setSelectedIndex(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6486928125261792739L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers of the currently checked-in versions of the node.
   */ 
  private ArrayList<VersionID>  pVersionIDs;


  /**
   * The latest revision number field.
   */ 
  private JTextField  pLatestVersionField;

  /**
   * The field for selecting the revision number to check-out.
   */ 
  private JCollectionField  pVersionField; 

  /**
   * The criteria used to determine whether nodes upstream of the root node of the check-out
   * should also be checked-out.
   */ 
  private JCollectionField  pModeField;

  /**
   * The method for creating working area files/links from the checked-in files.
   */
  private JCollectionField  pMethodField;

}
