// $Id: JDeleteDialog.java,v 1.3 2005/02/22 06:07:02 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.core.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   D E L E T E   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the node delete operation.
 */ 
public 
class JDeleteDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JDeleteDialog() 
  {
    super("Delete Node", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
		
	{
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, "Remove Working Files:", sTSize, 
					      vpanel, sVSize);
	  pRemoveFilesField = field;
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Delete", null, null, "Cancel");

      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get whether to remove the working files associated with the deleted node.
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

  private static final long serialVersionUID = -2391833497249062891L;

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
