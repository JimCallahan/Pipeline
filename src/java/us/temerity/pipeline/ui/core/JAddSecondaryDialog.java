// $Id: JAddSecondaryDialog.java,v 1.3 2005/02/22 06:07:02 jim Exp $

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
/*   A D D   S E C O N D A R Y   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for information needed to add a secondary file sequence to a node.
 */ 
public 
class JAddSecondaryDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JAddSecondaryDialog() 
  {
    super("Add Secondary", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	pPrefixField =
	  UIFactory.createTitledAlphaNumField(tpanel, "Filename Prefix:", sTSize, 
					     vpanel, "", sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("Single File");
	  values.add("File Sequence");
	  JCollectionField field = 
	    UIFactory.createTitledCollectionField(tpanel, "File Mode:", sTSize, 
						 vpanel, values, sVSize);
	  pFileModeField = field;

	  field.addActionListener(this);
	  field.setActionCommand("update-frame-fields");
	}

	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, "Frame Numbers:", sTSize, 
					      vpanel, sVSize);
	  pFrameNumbersField = field;

	  field.addActionListener(this);
	  field.setActionCommand("update-frame-fields");
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

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
	      pToLabel = label;

	      label.setName("DisableLabel");

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
	      pByLabel = label;

	      label.setName("DisableLabel");

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

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	pFramePaddingField = 
	  UIFactory.createTitledIntegerField(tpanel, "Frame Padding:", sTSize, 
					    vpanel, null, sVSize);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  JAlphaNumField field = 
	    UIFactory.createTitledAlphaNumField(tpanel, "Filename Suffix:", sTSize, 
					       vpanel, null, sVSize);
	  pSuffixField = field;
	  
	  field.addActionListener(this);
	  field.setActionCommand("update-editor");
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("Add Secondary File Sequence:", true, body, 
		   "Add", "Browse", null, "Cancel");

      pack();
    }  

    doUpdateFrameFields();

    pFileSeqDialog = new JFileSeqSelectDialog(this);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the secondary file sequence described by the current dialog configuration. <P> 
   * 
   * @return 
   *   The file sequence or <CODE>null</CODE> if not fully specified.
   */
  public FileSeq
  getFileSequence()
  {
    return pFileSeq;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components based on the given node version.
   * 
   * @param mod
   *   The current working version of the node.
   */ 
  public void 
  updateNode
  (
   String author, 
   String view, 
   NodeMod mod
  )
  {  
    pFileSeqDialog.updateHeader(author, view);

    {
      File path = new File(PackageInfo.sWorkDir, author + "/" + view + mod.getName());
      pRootDir = path.getParentFile();
    }

    if(mod != null) {
      updateFileSeq(mod.getPrimarySequence());
      pPrefixField.setText(null);
    }
  }

  /**
   * Update the file sequences related fields.
   */ 
  private void 
  updateFileSeq
  (
   FileSeq fseq
  ) 
  {
    FilePattern fpat = fseq.getFilePattern();
    FrameRange frange = fseq.getFrameRange();
    
    pPrefixField.setText(fpat.getPrefix());

    if(fpat.hasFrameNumbers()) {
      pFrameNumbersField.setValue(true);

      if(fseq.isSingle()) {
	pFileModeField.setSelectedIndex(0);
	
	if(frange != null) 
	  pStartFrameField.setValue(frange.getStart());
	else
	  pStartFrameField.setValue(null);
      }
      else {
	pFileModeField.setSelectedIndex(1);
	
	if(frange != null) {
	  pStartFrameField.setValue(frange.getStart());
	  pEndFrameField.setValue(frange.getEnd());
	  pByFrameField.setValue(frange.getBy());
	}
	else {
	  pStartFrameField.setValue(null);
	} 
      }

      pFramePaddingField.setValue(fpat.getPadding());
    }
    else {    
      pFrameNumbersField.setValue(false);
      pFramePaddingField.setValue(null);
    }

    pSuffixField.setText(fpat.getSuffix());
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
      pFileSeq = null;

    super.setVisible(isVisible);
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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("update-frame-fields")) 
      doUpdateFrameFields();
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
    if(generateFileSequence() != null) 
      super.doConfirm();
  }

  /**
   * Browse for file sequences to use to fill in the frame range fields.
   */ 
  public void 
  doApply() 
  {
    if(!pRootDir.isDirectory()) {
      UIMaster.getInstance().showErrorDialog
	("Error:", 
	 "Unable to browse for secondary file sequences because the node working " + 
	 "directory (" + pRootDir + ") does not exist!");
      return;
    }

    pFileSeqDialog.setRootDir(pRootDir);
    pFileSeqDialog.updateTarget(null);

    pFileSeqDialog.setVisible(true);
    if(pFileSeqDialog.wasConfirmed()) {
      FileSeq fseq = pFileSeqDialog.getSelectedFileSeq();
      if(fseq != null) 
	updateFileSeq(fseq);
    }
  }
 
  /**
   * Update the enabled state of the frame related fields.
   */ 
  private void 
  doUpdateFrameFields()
  {
    /* single file mode */ 
    if(pFileModeField.getSelectedIndex() == 0) {
      pFrameNumbersField.setEnabled(true);

      if(pFrameNumbersField.getValue()) {
	if(pStartFrameField.getValue() == null) 
	  pStartFrameField.setValue(new Integer(0));
	pStartFrameField.setEnabled(true);
      }
      else {
	pStartFrameField.setValue(null);
	pStartFrameField.setEnabled(false);
      }	

      pToLabel.setEnabled(false);

      pEndFrameField.setValue(null);
      pEndFrameField.setEnabled(false);
      	
      pByLabel.setEnabled(false);
      
      pByFrameField.setValue(null);
      pByFrameField.setEnabled(false);

      if(pFrameNumbersField.getValue()) {
	if(pFramePaddingField.getValue() == null) 
	  pFramePaddingField.setValue(new Integer(4));
	pFramePaddingField.setEnabled(true);
      }
      else {
	pFramePaddingField.setValue(null);
	pFramePaddingField.setEnabled(false);	
      }
    }

    /* file sequence mode */ 
    else {		
      pFrameNumbersField.removeActionListener(this);
        pFrameNumbersField.setValue(true);
	pFrameNumbersField.setEnabled(false);
      pFrameNumbersField.addActionListener(this);

      if(pStartFrameField.getValue() == null) 
	pStartFrameField.setValue(new Integer(0));
      pStartFrameField.setEnabled(true);

      pToLabel.setEnabled(true);

      if(pEndFrameField.getValue() == null) 
	pEndFrameField.setValue(pStartFrameField.getValue());
      pEndFrameField.setEnabled(true);

      pByLabel.setEnabled(true);

      if(pByFrameField.getValue() == null) 
	pByFrameField.setValue(new Integer(1));
      pByFrameField.setEnabled(true);

      if(pFramePaddingField.getValue() == null) 
	pFramePaddingField.setValue(new Integer(4));
      pFramePaddingField.setEnabled(true);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the secondary file sequence described by the current dialog configuration. 
   */ 
  private FileSeq
  generateFileSequence() 
  {
    pFileSeq = null;

    try {
      String prefix = pPrefixField.getText();
      if((prefix == null) || (prefix.length() == 0))
	throw new PipelineException
	  ("Unable to add a secondary file sequence without a valid filename prefix!");

      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() == 0)) 
	suffix = null;
      
      FilePattern fpat = null;
      if(pFrameNumbersField.getValue()) {
	Integer padding = pFramePaddingField.getValue();
	if(padding == null) 
	  throw new PipelineException
	    ("Unable to add a secondary file sequence which has frame numbers but an " + 
	     "unspecified frame padding!");
	
	if(padding < 0) 
	  throw new PipelineException
	    ("Unable to add a secondary file sequence with a negative (" + padding + ") " + 
	     "frame padding!");
	
	fpat = new FilePattern(prefix, padding, suffix);
      }
      else {
	fpat = new FilePattern(prefix, suffix);
      }

      FrameRange frange = null;
      if(pFrameNumbersField.getValue()) {
	Integer startFrame = pStartFrameField.getValue();
	if(startFrame == null) 
	  throw new PipelineException
	    ("Unable to add a secondary file sequence which has frame numbers but an " + 
	     "unspecified start frame!");

	if(pFileModeField.getSelectedIndex() == 0) {
	  frange = new FrameRange(startFrame);
	}
	else {
	  Integer endFrame = pEndFrameField.getValue();
	  if(endFrame == null) 
	    throw new PipelineException
	      ("Unable to add a secondary file sequence which has frame numbers but an " + 
	       "unspecified end frame!");
	  
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
	      ("Unable to add a secondary file sequence which has frame numbers but an " + 
	       "unspecified frame increment!");  
	  
	  try {
	    frange = new FrameRange(startFrame, endFrame, byFrame);
	  }
	  catch(IllegalArgumentException ex) {
	    throw new PipelineException
	      ("Unable add a secondary file sequence.  " + ex.getMessage());
	  }
	}
      }
      
      pFileSeq = new FileSeq(fpat, frange);
    }
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
    
    return pFileSeq;
  }
   

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4324791195860761883L;
  
  private static final int sTSize  = 150;
  private static final int sVSize  = 300;
  private static final int sVSize1 = 80;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root directory of the current working area.
   */ 
  private File  pRootDir;

  /**
   * The file sequence described by the current dialog configuration. 
   */ 
  private FileSeq  pFileSeq;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The filename prefix.
   */ 
  private JAlphaNumField  pPrefixField;
  
  /**
   * The single/sequence file mode.
   */ 
  private JCollectionField  pFileModeField;
  
  /**
   * The frame numbers field.
   */ 
  private JBooleanField  pFrameNumbersField;
  
  /**
   * The start frame.
   */ 
  private JIntegerField  pStartFrameField;

  /**
   * The "to" label.
   */ 
  private JLabel pToLabel;
  
  /**
   * The end frame.
   */ 
  private JIntegerField  pEndFrameField;

  /**
   * The "by" label.
   */ 
  private JLabel pByLabel;
  
  /**
   * The by frame.
   */ 
  private JIntegerField  pByFrameField;

  /**
   * The frame number padding.
   */ 
  private JIntegerField  pFramePaddingField;

  /**
   * The filename suffix.
   */ 
  private JAlphaNumField  pSuffixField;
  
  /**
   * The file sequences dialog.
   */ 
  private JFileSeqSelectDialog  pFileSeqDialog;

}
