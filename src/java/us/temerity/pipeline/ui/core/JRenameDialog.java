// $Id: JRenameDialog.java,v 1.3 2005/02/22 06:07:02 jim Exp $

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
/*   R E N A M E   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the node rename operation.
 */ 
public 
class JRenameDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRenameDialog() 
  {
    super("Rename Node", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	{
	  JPathField field = 
	    UIFactory.createTitledPathField(tpanel, "Old Filename Prefix:", sTSize, 
					   vpanel, "", sVSize);
	  pOldPrefixField = field;

	  field.setEditable(false);
	  field.setEnabled(false);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	pNewPrefixField =
	  UIFactory.createTitledPathField(tpanel, "New Filename Prefix:", sTSize, 
					 vpanel, "", sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, "Rename Files:", sTSize, 
					      vpanel, sVSize);
	  pRenameFilesField = field;
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Rename", null, null, "Cancel");

      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the new node name.
   */ 
  public String
  getNewName() 
    throws PipelineException
  {
    String name = pNewPrefixField.getText();
    if((name != null) && (name.length() > 0)) {
      if(!pNewPrefixField.isPathValid()) 
	throw new PipelineException
	  ("The new node name (" + name + ") is not valid!");
      return name;
    }
    else {
      throw new PipelineException
	("No new node name was specified!");
    }      
  }
  
  /**
   * Get whether to rename the files associated with the node.
   */
  public boolean
  renameFiles() 
  {
    return pRenameFilesField.getValue();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components based on the given node working version.
   * 
   * @param mod 
   *   The working version to rename.
   */ 
  public void 
  updateNode
  (
   NodeMod mod
  )
  {  
    pHeaderLabel.setText("Rename Node:  " + mod.getPrimarySequence());
    pOldPrefixField.setText(mod.getName());
    pNewPrefixField.setText(mod.getName());
    pRenameFilesField.setValue(true);
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6669413368066234717L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The old filename prefix.
   */ 
  private JPathField  pOldPrefixField;
  
  /**
   * The new filename prefix.
   */ 
  private JPathField  pNewPrefixField;

  /**
   * Whether to rename the files associated with the node.
   */ 
  private JBooleanField  pRenameFilesField;

}
