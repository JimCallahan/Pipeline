// $Id: JRestoreQueryDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

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
/*   R E S T O R E   Q U E R Y   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog for setting the restore query parameters.
 */ 
public 
class JRestoreQueryDialog
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
  JRestoreQueryDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Search Criteria");

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
      }

      super.initUI("Offline Version Search Criteria:", body, 
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
    String pattern = pPatternField.getText();
    if((pattern == null) || (pattern.length() == 0))
      pPatternField.setText(".*");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -809877051727753315L;

  private static final int sTSize = 150;
  private static final int sVSize = 250;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The search criteria fields.
   */ 
  private JTextField  pPatternField;

}
