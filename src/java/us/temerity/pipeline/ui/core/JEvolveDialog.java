// $Id: JEvolveDialog.java,v 1.4 2006/09/25 12:11:44 jim Exp $

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
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JEvolveDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Evolve Version");

    pVersionIDs = new ArrayList<VersionID>();

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	pCurrentVersionField = 
	  UIFactory.createTitledTextField(tpanel, "Current Version:", sTSize, 
					 vpanel, "-", sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 6);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  pVersionField = 
	    UIFactory.createTitledCollectionField(tpanel, "Evolve To Version:", sTSize, 
						 vpanel, values, this, sVSize, null);
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", body, "Evolve", null, null, "Cancel");
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
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */ 
  public void 
  updateNameVersions
  (
   String header,
   VersionID currentID, 
   TreeSet<VersionID> vids,
   TreeSet<VersionID> offline
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
	values.add("v" + vid.toString() + (offline.contains(vid) ? " - Offline" : ""));
      
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
