// $Id: JRegisterDialog.java,v 1.6 2004/09/08 19:23:23 jim Exp $

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
/*   R E G I S T E R   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for information needed to register a new node.
 */ 
public 
class JRegisterDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRegisterDialog() 
  {
    super("Register Node", true);

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
	
	pPrefixField =
	  UIMaster.createTitledPathField(tpanel, "Filename Prefix:", sTSize, 
					 vpanel, "", sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("Single File");
	  values.add("File Sequence");
	  JCollectionField field = 
	    UIMaster.createTitledCollectionField(tpanel, "File Mode:", sTSize, 
						 vpanel, values, sVSize);
	  pFileModeField = field;

	  field.addActionListener(this);
	  field.setActionCommand("update-frame-fields");
	}

	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, "Frame Numbers:", sTSize, 
					      vpanel, sVSize);
	  pFrameNumbersField = field;

	  field.addActionListener(this);
	  field.setActionCommand("update-frame-fields");
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  tpanel.add(UIMaster.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));

	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);

	    {
	      JIntegerField field = 
		UIMaster.createIntegerField(null, sVSize1, JLabel.CENTER);
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
		UIMaster.createIntegerField(null, sVSize1, JLabel.CENTER);
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
		UIMaster.createIntegerField(null, sVSize1, JLabel.CENTER);
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

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	
	pFramePaddingField = 
	  UIMaster.createTitledIntegerField(tpanel, "Frame Padding:", sTSize, 
					    vpanel, null, sVSize);

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  JAlphaNumField field = 
	    UIMaster.createTitledAlphaNumField(tpanel, "Filename Suffix:", sTSize, 
					       vpanel, null, sVSize);
	  pSuffixField = field;
	  
	  field.addActionListener(this);
	  field.setActionCommand("update-editor");
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	  pToolsetField = 
	    UIMaster.createTitledCollectionField(tpanel, "Toolset:", sTSize, 
						 vpanel, values, sVSize);
	}
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	  pEditorField = 
	    UIMaster.createTitledCollectionField(tpanel, "Editor:", sTSize, 
						 vpanel, values, sVSize);
	}

	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("Register New Node:", true, body, "Register", "Browse", null, "Cancel");

      pack();
      setResizable(false);
    }  

    doUpdateFrameFields();

    pFileSeqDialog = new JFileSeqSelectDialog(this);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the working node version described by the current dialog configuration. <P> 
   * 
   * @return 
   *   The working version or <CODE>null</CODE> if not fully specified.
   */
  public NodeMod
  getWorkingVersion()
  {
    return pNodeMod;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components based on the given node version.
   * 
   * @param node
   *   The node version.
   */ 
  public void 
  updateNode
  (
   String author, 
   String view, 
   NodeCommon node
  )
  {  
    String defaultToolset = null;
    {
      String toolset = pToolsetField.getSelected();

      UIMaster master = UIMaster.getInstance();
      TreeSet<String> tsets = new TreeSet<String>();
      try {
	tsets.addAll(master.getMasterMgrClient().getActiveToolsetNames());
	defaultToolset = master.getMasterMgrClient().getDefaultToolsetName();
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      
      if(tsets.isEmpty())
	tsets.add("-");

      pToolsetField.setValues(tsets);
      if(defaultToolset == null)
	defaultToolset = tsets.last();

      if((toolset != null) && pToolsetField.getValues().contains(toolset))
	pToolsetField.setSelected(toolset);
      else 
	pToolsetField.setSelected(defaultToolset);
    }

    {
      String editor = pEditorField.getSelected();

      PluginMgr mgr = PluginMgr.getInstance();
      ArrayList<String> values = new ArrayList<String>(mgr.getEditors().keySet());
      values.add("-");
      pEditorField.setValues(values);

      if((editor != null) && pEditorField.getValues().contains(editor))
	pEditorField.setSelected(editor);
      else 
	pEditorField.setSelected("-");      
    }

    pFileSeqDialog.updateHeader(author, view);
    pRootDir = new File(PackageInfo.sWorkDir, author + "/" + view);

    if(node != null) {
      pPrefixField.setText(node.getName() + "-clone");
      updateFileSeq(node.getPrimarySequence());

      String toolset = node.getToolset();
      if((toolset != null) && pToolsetField.getValues().contains(toolset))
	pToolsetField.setSelected(node.getToolset());
      else 
	pToolsetField.setSelected(defaultToolset);

      String editor = node.getEditor();
      if((editor != null) && (pEditorField.getValues().contains(editor))) 
	pEditorField.setSelected(editor);
      else 
	updateEditor();
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
      pFileModeField.setSelectedIndex(0);
 
      pFrameNumbersField.setValue(false);
      pFramePaddingField.setValue(null);
    }

    pSuffixField.setText(fpat.getSuffix());
  }

  /**
   * Update the editor based on the current filename suffix.
   */ 
  private void 
  updateEditor()
  {
    String suffix = pSuffixField.getText();
    if((suffix != null) && (suffix.length() > 0)) {
      UIMaster master = UIMaster.getInstance();
      try {
	String editor = master.getMasterMgrClient().getEditorForSuffix(suffix);
	if((editor != null) && (pEditorField.getValues().contains(editor))) {
	  pEditorField.setSelected(editor);
	  return;
	}
      }
      catch(PipelineException ex) {
      }
    }

    pEditorField.setSelected("-");
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
      pNodeMod = null;

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
    else if(cmd.equals("update-editor")) 
      updateEditor();
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
    if(generateNodeMod() != null) 
      super.doConfirm();
  }

  /**
   * Browse for file sequences to use to fill in the frame range fields.
   */ 
  public void 
  doApply() 
  {
    pFileSeqDialog.setRootDir(pRootDir);

    String prefix = pPrefixField.getText();
    if(prefix != null) {
      File dir = new File(pRootDir, prefix);
      if(!dir.isDirectory()) 
	dir = dir.getParentFile();

      pFileSeqDialog.updateTarget(dir);
    }

    pFileSeqDialog.setVisible(true);
    if(pFileSeqDialog.wasConfirmed()) {
      File dir = pFileSeqDialog.getDirectory();
      FileSeq fseq = pFileSeqDialog.getSelectedFileSeq();
      if((dir != null) && (fseq != null)) {
	pPrefixField.setText(dir + "/" + fseq.getFilePattern().getPrefix());
	updateFileSeq(fseq);
	updateEditor();
      }
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
   * Generate a working node version described by the current dialog configuration. 
   */ 
  private NodeMod
  generateNodeMod() 
  {
    pNodeMod = null;

    try {
      String name = pPrefixField.getText();
      String prefix = null;
      {
	if((name == null) || (name.length() == 0) || name.endsWith("/"))
	  throw new PipelineException
	    ("Unable to register node without a valid filename prefix!");
	
	File path = new File(name);
	prefix = path.getName();
      }

      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() == 0)) 
	suffix = null;
      
      FilePattern fpat = null;
      if(pFrameNumbersField.getValue()) {
	Integer padding = pFramePaddingField.getValue();
	if(padding == null) 
	  throw new PipelineException
	    ("Unable to register node (" + name + ") which has frame numbers but an " + 
	     "unspecified frame padding!");
	
	if(padding < 0) 
	  throw new PipelineException
	    ("Unable to register node (" + name + ") with a negative (" + padding + ") " + 
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
	    ("Unable to register node (" + name + ") which has frame numbers but an " + 
	     "unspecified start frame!");

	if(pFileModeField.getSelectedIndex() == 0) {
	  frange = new FrameRange(startFrame);
	}
	else {
	  Integer endFrame = pEndFrameField.getValue();
	  if(endFrame == null) 
	    throw new PipelineException
	      ("Unable to register node (" + name + ") which has frame numbers but an " + 
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
	      ("Unable to register node (" + name + ") which has frame numbers but an " + 
	       "unspecified frame increment!");  
	  
	  try {
	    frange = new FrameRange(startFrame, endFrame, byFrame);
	  }
	  catch(IllegalArgumentException ex) {
	    throw new PipelineException
	      ("Unable to register node (" + name + ").  " + ex.getMessage());
	  }
	}
      }

      FileSeq primary = new FileSeq(fpat, frange);

      String toolset = pToolsetField.getSelected();
      if((toolset == null) || toolset.equals("-")) 			      
	throw new PipelineException
	  ("Unable to register node (" + name + ") with an unspecified toolset!");

      String editor = pEditorField.getSelected();
      if((editor != null) && editor.equals("-"))
	editor = null;

      pNodeMod = new NodeMod(name, primary, new TreeSet<FileSeq>(), toolset, editor);
    }
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }

    return pNodeMod;
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
   * The working node version described by the current dialog configuration. 
   */ 
  private NodeMod  pNodeMod; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The filename prefix.
   */ 
  private JPathField  pPrefixField;
  
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
   * The toolset name.
   */ 
  private JCollectionField  pToolsetField;
  
  /**
   * The editor name.
   */ 
  private JCollectionField  pEditorField;


  /**
   * The file sequences dialog.
   */ 
  private JFileSeqSelectDialog  pFileSeqDialog;

}
