// $Id: JRenumberDialog.java,v 1.4 2005/03/11 06:33:44 jim Exp $

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
/*   R E N U M B E R   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for options to the node renumber operation.
 */ 
public 
class JRenumberDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRenumberDialog() 
  {
    super("Renumber Node", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	{
	  tpanel.add(UIFactory.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));

	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);

	    {
	      JIntegerField field = 
		UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
	      pStartFrameField = field;

	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createHorizontalGlue());
	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));

	    {
	      JLabel label = new JLabel("to");
	      hbox.add(label);
	    }

	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	    hbox.add(Box.createHorizontalGlue());

	    {
	      JIntegerField field = 
		UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
	      pEndFrameField = field;

	      hbox.add(field);
	    }

	    hbox.add(Box.createHorizontalGlue());
	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));

	    {
	      JLabel label = new JLabel("by");
	      hbox.add(label);
	    }

	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	    hbox.add(Box.createHorizontalGlue());

	    {
	      JIntegerField field = 
		UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
	      pByFrameField = field;

	      hbox.add(field);
	    }
	    
	    Dimension size = new Dimension(sVSize+1, 19);
	    hbox.setMinimumSize(size);
	    hbox.setMaximumSize(size);
	    hbox.setPreferredSize(size);

	    vpanel.add(hbox);
	  }
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, "Remove Obsolete Files:", sTSize, 
					      vpanel, sVSize);
	  pRemoveFilesField = field;
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("X", true, body, "Renumber", null, null, "Cancel");

      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the new frame range.
   */ 
  public FrameRange
  getFrameRange() 
  {
    return pFrameRange;
  }
  
  /**
   * Get whether to remove files from the old frame range which are no longer part of the new 
   * frame range.
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
   *   The working version to renumber.
   */ 
  public void 
  updateNode
  (
   NodeMod mod
  )
  {  
    pHeaderLabel.setText("Renumber Node:  " + mod.getPrimarySequence());
    
    FileSeq fseq = mod.getPrimarySequence();
    pOrigFrameRange = fseq.getFrameRange();
    assert(pOrigFrameRange != null);

    pStartFrameField.setValue(pOrigFrameRange.getStart());
    pEndFrameField.setValue(pOrigFrameRange.getEnd());
    pByFrameField.setValue(pOrigFrameRange.getBy());

    pByFrameField.setEnabled(mod.getSecondarySequences().isEmpty());

    pRemoveFilesField.setValue(false);
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(isVisible)
      pFrameRange = null;

    super.setVisible(isVisible);
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
    if(generateFrameRange()) 
      super.doConfirm();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a valid frame range from the current dialog configuration.
   */ 
  private boolean
  generateFrameRange() 
  {
    pFrameRange = null;

    try {
      Integer startFrame = pStartFrameField.getValue();
      if(startFrame == null) 
	throw new PipelineException
	  ("Unable to renumber node with an unspecified start frame!");
      
      Integer endFrame = pEndFrameField.getValue();
      if(endFrame == null) 
	throw new PipelineException
	  ("Unable to renumber node with an unspecified end frame!");
      
      if(startFrame > endFrame) {
	Integer tmp = endFrame;
	endFrame = startFrame;
	startFrame = tmp;
	
	pStartFrameField.setValue(startFrame);
	pEndFrameField.setValue(endFrame);
      }
      
      Integer byFrame = pByFrameField.getValue();
      if(byFrame == null) 
	throw new PipelineException
	  ("Unable to renumber node with an unspecified frame increment!");  

      if(!pByFrameField.isEnabled() &&  
	 ((((pOrigFrameRange.getStart() - startFrame) % byFrame) != 0) ||
	  (((pOrigFrameRange.getStart() - endFrame) % byFrame) != 0)))
	throw new PipelineException 
	  ("Unable to renumber node due to misalignment of the new frame range (" + 
	   startFrame + "-" + endFrame + "x" + byFrame + ") with the original frame " +
	   "range (" + pOrigFrameRange + ")!");
      
      try {
	pFrameRange = new FrameRange(startFrame, endFrame, byFrame);
      }
      catch(IllegalArgumentException ex) {
	throw new PipelineException
	  ("Unable to renumber node.  " + ex.getMessage());
      }

      if(pOrigFrameRange.equals(pFrameRange))
	throw new PipelineException
	  ("Unecessary to renumber node when the frame range (" + pOrigFrameRange + ") " + 
	   "has not been altered!");
    }
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
      return false;
    }

    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4839396417757886052L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 300;
  private static final int sVSize1 = 80;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The frame range specified by the dialog.
   */
  private FrameRange  pFrameRange; 
  
  /**
   * The original frame range.
   */ 
  private FrameRange  pOrigFrameRange;


  /**
   * The start frame.
   */ 
  private JIntegerField  pStartFrameField;

  /**
   * The end frame.
   */ 
  private JIntegerField  pEndFrameField;

  /**
   * The by frame.
   */ 
  private JIntegerField  pByFrameField;


  /**
   * Whether to remove files from the old frame range which are no longer part of the new 
   * frame range.
   */ 
  private JBooleanField  pRemoveFilesField;

}
