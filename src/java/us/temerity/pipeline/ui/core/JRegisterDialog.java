// $Id: JRegisterDialog.java,v 1.7 2005/03/30 22:42:53 jim Exp $

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

    pRegistered = new TreeSet<String>();

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

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	  pToolsetField = 
	    UIFactory.createTitledCollectionField(tpanel, "Toolset:", sTSize, 
						 vpanel, values, this, sVSize, null);
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	  pEditorField = 
	    UIFactory.createTitledCollectionField(tpanel, "Editor:", sTSize, 
						 vpanel, values, this, sVSize, null);
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      String extra[][] = {
	{ "Browse",  "browse" }
      };

      super.initUI("Register New Node:", true, body, "Confirm", "Apply", extra, "Close");

      pack();
    }  

    doUpdateFrameFields();

    pFileSeqDialog = new JFileSeqSelectDialog(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the names of the registered nodes.
   */ 
  public TreeSet<String> 
  getRegistered() 
  {
    synchronized(pRegistered) {
      return pRegistered;
    }
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
    pAuthor = author; 
    pView   = view; 

    synchronized(pRegistered) {
      pRegistered.clear();
    }

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

      PluginMgrClient pclient = PluginMgrClient.getInstance();
      try {
	pclient.update();
      } 
      catch(PipelineException ex) {
	UIMaster.getInstance().showErrorDialog(ex);
      }    

      ArrayList<String> values = new ArrayList<String>(pclient.getEditors().keySet());
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

    pack();
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
    String cmd = e.getActionCommand();
    if(cmd.equals("browse")) 
      doBrowse();
    else if(cmd.equals("update-frame-fields")) 
      doUpdateFrameFields();
    else if(cmd.equals("update-editor")) 
      updateEditor();
    else 
      super.actionPerformed(e);
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
    NodeMod mod = generateNodeMod();
    if(mod == null) 
      return; 
    
    UIMaster master = UIMaster.getInstance();
    if(master.beginPanelOp("Registering New Node: " + mod.getName())) {
      try {
	master.getMasterMgrClient().register(pAuthor, pView, mod);
	synchronized(pRegistered) {
	  pRegistered.add(mod.getName());
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      finally {
	master.endPanelOp("Done.");
      }
    }

    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  public void 
  doApply()
  {
    NodeMod mod = generateNodeMod();
    if(mod == null) 
      return; 
    
    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
    pCancelButton.setEnabled(false);

    RegisterTask task = new RegisterTask(pAuthor, pView, mod);
    task.start();
  }

  /**
   * Browse for file sequences to use to fill in the frame range fields.
   */ 
  public void 
  doBrowse() 
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
      if(dir != null) {
	FileSeq fseq = pFileSeqDialog.getSelectedFileSeq();
	if(fseq != null) {
	  pPrefixField.setText(dir + "/" + fseq.getFilePattern().getPrefix());
	  updateFileSeq(fseq);	    
	}
	else {
	  pPrefixField.setText(dir.toString());
	  pFileModeField.setSelectedIndex(0);	  
	  pFrameNumbersField.setValue(false);
	  pFramePaddingField.setValue(null);
	  pSuffixField.setText(null);
	}

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
	if(!pPrefixField.isPathValid() || name.endsWith("/"))
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Register a new node.
   */ 
  private
  class RegisterTask
    extends Thread
  {
    public 
    RegisterTask
    (
     String author, 
     String view, 
     NodeMod mod 
    ) 
    {
      super("JRegisterDialog:RegisterTask");

      pAuthor  = author; 
      pView    = view; 
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Registering New Node: " + pNodeMod.getName())) {
	try {
	  master.getMasterMgrClient().register(pAuthor, pView, pNodeMod);
	  synchronized(pRegistered) {
	    pRegistered.add(pNodeMod.getName());
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }

      SwingUtilities.invokeLater(new DoneTask());
    }

    private String   pAuthor; 
    private String   pView; 
    private NodeMod  pNodeMod;
  }


  /** 
   * Renable buttons.
   */ 
  private
  class DoneTask
    extends Thread
  {
    public 
    DoneTask() 
    {
      super("JRegisterDialog:DoneTask");
    }

    public void 
    run() 
    {
      pConfirmButton.setEnabled(true);
      pApplyButton.setEnabled(true);
      pCancelButton.setEnabled(true);
    }
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

  /**
   * The names of the registered nodes.
   */ 
  private TreeSet<String>  pRegistered; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The owner of the current working area.
   */ 
  private String   pAuthor; 

  /** 
   * The name of the current working area. 
   */ 
  private String   pView; 


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

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file sequences dialog.
   */ 
  private JFileSeqSelectDialog  pFileSeqDialog;

}
