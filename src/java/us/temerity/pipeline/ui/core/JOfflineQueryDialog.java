// $Id: JOfflineQueryDialog.java,v 1.2 2005/03/14 16:08:21 jim Exp $

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
/*   O F F L I N E   Q U E R Y   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog for setting the offline query parameters.
 */ 
public 
class JOfflineQueryDialog
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
  JOfflineQueryDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Search Criteria", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	pPatternField = 
	  UIFactory.createTitledEditableTextField
	  (tpanel, "Node Name Pattern:", sTSize, 
	   vpanel, ".*", sVSize, 
	   "A regular expression used to select checked-in version names.");
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pExcludeLatestField = 
	  UIFactory.createTitledIntegerField
	  (tpanel, "Exclude Latest:", sTSize, 
	   vpanel, 0, sVSize, 
	   "Exclude this number of newer checked-in versions from the search.");
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pMinArchivesField = 
	  UIFactory.createTitledIntegerField
	  (tpanel, "Min Archives:", sTSize, 
	   vpanel, 2, sVSize, 
	   "Exclude checked-in versions which have not been archived at least this number " + 
	   "of times.");

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pUnusedOnlyField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Unused Only:", sTSize, 
	   vpanel, sVSize, 
	   "Whether to only include checked-in versions which can be offlined.");
	pUnusedOnlyField.setValue(true);
      }

      super.initUI("Checked-In Version Search Criteria:", true, body, 
		   "Search", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes to restore or <CODE>null</CODE> for all nodes.
   */ 
  public String
  getPattern() 
  {
    return pPatternField.getText();
  }

  /**
   * Get the number of newer checked-in versions of the node to exclude from the returned 
   * list or <CODE>null</CODE> to include all versions.
   */ 
  public Integer
  getExcludeLatest() 
  {
    return pExcludeLatestField.getValue();
  }

  /** 
   * Get the minimum number of archive volumes containing the checked-in version in order for 
   * it to be inclued in the returned list or <CODE>null</CODE> for any number of archives.
   */ 
  public Integer
  getMinArchives() 
  {
    return pMinArchivesField.getValue();
  }
  
  /** 
   * Whether to only include checked-in versions which can be offlined.
   */ 
  public boolean
  getUnusedOnly()
  {
    return pUnusedOnlyField.getValue();
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    validateFields();
    super.doConfirm();
  }

  /**
   * Cancel changes and close.
   */ 
  public void 
  doCancel()
  {
    validateFields();
    super.doCancel();
  }

  /**
   * Force fields to have legal values.
   */ 
  private void
  validateFields() 
  {
    {
      String pattern = pPatternField.getText();
      if((pattern == null) || (pattern.length() == 0))
	pPatternField.setText(".*");
    }
    
    {
      Integer exclude = pExcludeLatestField.getValue();
      if((exclude == null) || (exclude < 0))
	pExcludeLatestField.setValue(0);
    }

    {
      Integer archives = pMinArchivesField.getValue();
      if((archives == null) || (archives < 0)) 
	pMinArchivesField.setValue(0);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5905925038891039759L;

  private static final int sTSize = 150;
  private static final int sVSize = 250;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The search criteria fields.
   */ 
  private JTextField     pPatternField;
  private JIntegerField  pExcludeLatestField;
  private JIntegerField  pMinArchivesField;
  private JBooleanField  pUnusedOnlyField; 

}
