// $Id: JRenameDialog.java,v 1.5 2005/03/29 03:48:56 jim Exp $

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
	
	pPrefixField =
	  UIFactory.createTitledPathField(tpanel, "Filename Prefix:", sTSize, 
					  vpanel, "", sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pFramePaddingField = 
	  UIFactory.createTitledIntegerField(tpanel, "Frame Padding:", sTSize, 
					    vpanel, null, sVSize);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	pSuffixField = 
	  UIFactory.createTitledAlphaNumField(tpanel, "Filename Suffix:", sTSize, 
					      vpanel, null, sVSize);

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
   * Get the new file pattern. 
   */ 
  public FilePattern
  getNewFilePattern()
    throws PipelineException
  {
    String prefix = pPrefixField.getText();
    if((prefix != null) && (prefix.length() > 0)) {
      if(!pPrefixField.isPathValid()) 
	throw new PipelineException
	  ("The new node name (" + prefix + ") is not valid!");

      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() == 0)) 
	suffix = null;

      if(pPattern.hasFrameNumbers()) {
	Integer padding = pFramePaddingField.getValue();
	if(padding == null) 
	  throw new PipelineException
	    ("Unable to rename node (" + prefix + ") which has frame numbers but an " + 
	     "unspecified new frame padding!");
	
	if(padding < 0) 
	  throw new PipelineException
	    ("Unable to rename node (" + prefix + ") with a negative (" + padding + ") " + 
	     "frame padding!");
	
	return new FilePattern(prefix, padding, suffix);
      }
      else {
	return new FilePattern(prefix, suffix);
      }
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
    FileSeq fseq = mod.getPrimarySequence();
    pPattern = fseq.getFilePattern();

    pHeaderLabel.setText("Rename Node:  " + fseq);
    pPrefixField.setText(mod.getName());
    
    if(pPattern.hasFrameNumbers()) {
      pFramePaddingField.setEnabled(true);
      pFramePaddingField.setValue(pPattern.getPadding());
    }
    else {
      pFramePaddingField.setValue(null);
      pFramePaddingField.setEnabled(false);
    }

    pSuffixField.setText(pPattern.getSuffix());

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
   * The original file pattern.
   */
  private FilePattern  pPattern;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The new filename prefix.
   */ 
  private JPathField  pPrefixField;

  /**
   * The new frame number padding.
   */ 
  private JIntegerField  pFramePaddingField;

  /**
   * The new filename suffix.
   */ 
  private JAlphaNumField  pSuffixField;

  /**
   * Whether to rename the files associated with the node.
   */ 
  private JBooleanField  pRenameFilesField;

}
