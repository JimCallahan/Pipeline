// $Id: JArchiveQueryDialog.java,v 1.2 2005/03/14 16:08:21 jim Exp $

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
/*   A R C H I V E   Q U E R Y   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog for setting the archive query parameters.
 */ 
public 
class JArchiveQueryDialog
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
  JArchiveQueryDialog
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

	pMaxArchivesField = 
	  UIFactory.createTitledIntegerField
	  (tpanel, "Max Archives:", sTSize, 
	   vpanel, 2, sVSize, 
	   "Exclude checked-in versions which have been archived more than this number " + 
	   "of times.");
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
   * Get the maximum allowable number of archives which already contain the checked-in 
   * version in order for it to be inclued in the returned list or <CODE>null</CODE> for 
   * any number of archives.
   */ 
  public Integer
  getMaxArchives() 
  {
    return pMaxArchivesField.getValue();
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
      Integer archives = pMaxArchivesField.getValue();
      if((archives == null) || (archives < 1)) 
	pMaxArchivesField.setValue(1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 331203318632193453L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 250;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The search criteria fields.
   */ 
  private JTextField     pPatternField;
  private JIntegerField  pMaxArchivesField;

}
