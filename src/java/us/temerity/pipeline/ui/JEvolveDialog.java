// $Id: JEvolveDialog.java,v 1.1 2004/10/03 19:42:18 jim Exp $

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
/*   E V O L V E   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number of the checked-in version to use as the new basis
 * for the working version. 
 */ 
public 
class JEvolveDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JEvolveDialog() 
  {
    super("Evolve Version", true);

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
	
	pCurrentVersionField = 
	  UIMaster.createTitledTextField(tpanel, "Current Version:", sTSize, 
					 vpanel, "-", sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 6);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  pVersionField = 
	    UIMaster.createTitledCollectionField(tpanel, "Evolve To Version:", sTSize, 
						 vpanel, values, sVSize);
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Evolve", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the checked-in version. <P> 
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
    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and the revision numbers.
   * 
   * @param header
   *   The header label.
   * 
   * @param currentID
   *   The revision number of the current checked-in version upon which the working 
   *   version is based.
   * 
   * @param vids
   *   The revision numbers of the checked-in versions of the node.
   */ 
  public void 
  updateNameVersions
  (
   String header,
   VersionID currentID, 
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
      
      pCurrentVersionField.setText("v" + currentID); 
      
      ArrayList<String> values = new ArrayList<String>();
      for(VersionID vid : pVersionIDs) 
	values.add("v" + vid.toString());
      
      pVersionField.setValues(values);
      pVersionField.setSelectedIndex(0);

      pConfirmButton.setEnabled(true);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7261599346057727439L;
  
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
   * The current revision number field.
   */ 
  private JTextField  pCurrentVersionField;

  /**
   * The field for selecting the revision number.
   */ 
  private JCollectionField  pVersionField; 

}
