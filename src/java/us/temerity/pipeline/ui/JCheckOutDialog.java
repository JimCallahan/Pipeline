// $Id: JCheckOutDialog.java,v 1.1 2004/06/28 00:22:06 jim Exp $

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
   * 
   * @param owner
   *   The parent dialog.
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
					 vpanel, "-", sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  pVersionField = 
	    UIMaster.createTitledCollectionField(tpanel, "Check-Out Version:", sTSize, 
						 vpanel, values, sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pKeepNewerField = 
	  UIMaster.createTitledBooleanField(tpanel, "Keep Newer:", sTSize, 
					    vpanel, sVSize); 

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
   * Whether upstream nodes which have a newer revision number than the version to be 
   * checked-out should be skipped? 
   */ 
  public boolean
  keepNewer() 
  {
    return pKeepNewerField.getValue();
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

      pKeepNewerField.setValue(false);

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
      
      pKeepNewerField.setValue(false);

      pConfirmButton.setEnabled(true);
    }
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
   * Whether upstream nodes which have a newer revision number than the version to be 
   * checked-out should be skipped? 
   */ 
  private JBooleanField  pKeepNewerField;

}
