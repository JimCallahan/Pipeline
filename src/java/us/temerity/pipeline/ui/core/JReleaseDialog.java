// $Id: JReleaseDialog.java,v 1.6 2005/03/11 06:33:44 jim Exp $

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
/*   R E L E A S E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the node release operation.
 */ 
public 
class JReleaseDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JReleaseDialog() 
  {
    super("Release Node", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
		
	pRemoveFilesField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Remove Working Files:", sTSize, 
	   vpanel, sVSize, 
	   "Whether to remove the working area files associated with the released nodes.");

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Release", null, null, "Cancel");

      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get whether to remove the files associated with the released node.
   */
  public boolean
  removeFiles() 
  {
    return pRemoveFilesField.getValue();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components based on the given node working version.
   * 
   * @param header
   *   The dialog header text. 
   */ 
  public void 
  updateHeader
  (
   String header
  )
  {  
    pHeaderLabel.setText(header);
    pRemoveFilesField.setValue(true);
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8701942318291685416L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 150;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to remove the files associated with the released node.
   */ 
  private JBooleanField  pRemoveFilesField;

}
