// $Id: JReleaseViewDialog.java,v 1.3 2005/03/11 06:33:44 jim Exp $

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
/*   R E L E A S E   V I E W   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the release view operation.
 */ 
public 
class JReleaseViewDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JReleaseViewDialog() 
  {
    super("Release View", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
		
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("Entire Working Area");
	  values.add("Matching Pattern");
	  
	  JCollectionField field = 
	    UIFactory.createTitledCollectionField
	    (tpanel, "Release Nodes:", sTSize, 
	     vpanel, values, sVSize, 
	     "The method for selectiing the nodes to be released.");
	  pMethodField = field;
	  
	  field.setSelectedIndex(0);

	  field.addActionListener(this);
	  field.setActionCommand("method-changed");
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pPatternField = 
	  UIFactory.createTitledEditableTextField
	  (tpanel, "Node Name Pattern:", sTSize, 
	   vpanel, ".*", sVSize, 
	   "A regular expression used to select working versions to release.");

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	pRemoveFilesField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Remove Working Files:", sTSize, 
	   vpanel, sVSize, 
	   "Whether to remove the working area files associated with the released nodes.");
	pRemoveFilesField.setValue(true);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pRemoveWorkingAreaField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Remove Working Area:", sTSize, 
	   vpanel, sVSize, 
	   "Whether to recursively remove the entire working area directory and all " +
	   "files contained in the directory for the current view.");

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      doMethodChanged();

      super.initUI("Release View:", true, body, "Release", null, null, "Cancel");
      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes to release or <CODE>null</CODE> for all nodes.
   */ 
  public String
  getPattern() 
  {
    if(pMethodField.getSelectedIndex() == 1) {
      String pattern = pPatternField.getText();
      if((pattern != null) && (pattern.length() > 0)) 
	return pattern;
    }

    return null;
  }

  /**
   * Whether to remove the files associated with the released node.
   */
  public boolean
  removeFiles() 
  {
    return pRemoveFilesField.getValue();
  }

  /**
   * Whether to remove the entire working area directory. 
   */
  public boolean
  removeWorkingArea() 
  {
    return pRemoveWorkingAreaField.getValue();
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
    if(cmd.equals("method-changed")) 
      doMethodChanged();
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The release node selection method changed.
   */ 
  private void 
  doMethodChanged()
  {
    if(pMethodField.getSelectedIndex() == 0) {
      pPatternField.setText(null);
      pPatternField.setEnabled(false);
    }
    else {
      pPatternField.setEnabled(true);
      pPatternField.setText(".*");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6053050284662722414L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 250;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The method for selectiing the nodes to be released.
   */ 
  private JCollectionField   pMethodField; 

  /**
   * A regular expression used to select working versions to release.
   */ 
  private JTextField     pPatternField;

  /**
   * Whether to remove the files associated with the released nodes.
   */ 
  private JBooleanField  pRemoveFilesField;

  /**
   * Whether to recursively remove the entire working area directory. 
   */ 
  private JBooleanField  pRemoveWorkingAreaField;

}
