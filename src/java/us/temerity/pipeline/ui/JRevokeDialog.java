// $Id: JRevokeDialog.java,v 1.1 2004/06/14 22:55:00 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   R E V O K E   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the node revoke operation.
 */ 
public 
class JRevokeDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRevokeDialog() 
  {
    super("Revoke Node", true);

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
		
	{
	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, "Remove Files:", sTSize, 
					      vpanel, sVSize);
	  pRemoveFilesField = field;
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Revoke", null, null, "Cancel");

      pack();
      setResizable(false);
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get whether to remove the files associated with the revoked node.
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
   * @param mod 
   *   The working version to revoke.
   */ 
  public void 
  updateNode
  (
   NodeMod mod
  )
  {  
    pHeaderLabel.setText("Revoke Node:  " + mod.getPrimarySequence());
    pRemoveFilesField.setValue(true);
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8701942318291685416L;

  private static final int sTSize  = 100;
  private static final int sVSize  = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to remove the files associated with the revoked node.
   */ 
  private JBooleanField  pRemoveFilesField;

}
