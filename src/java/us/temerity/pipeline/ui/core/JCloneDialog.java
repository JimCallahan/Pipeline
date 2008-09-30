// $Id: JCloneDialog.java,v 1.18 2008/09/30 23:23:22 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   C L O N E   D I A L O G                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for information needed to clone an existing node. 
 */ 
public 
class JCloneDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JCloneDialog
  (
   int channel, 
   Frame owner
  ) 
  {
    super(owner, "Clone Node");

    /* initialize fields */ 
    {
      pChannel = channel;
      pRegistered = new TreeSet<String>();
    }

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	pPrefixField =
	  UIFactory.createTitledPathField(tpanel, "Filename Prefix:", sTSize, 
					  vpanel, new Path("/"), sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	
	pFileModeField = 
	  UIFactory.createTitledTextField(tpanel, "File Mode:", sTSize, 
					  vpanel, "", sVSize);
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	pFrameNumbersField = 
	  UIFactory.createTitledTextField(tpanel, "Frame Numbers:", sTSize, 
					  vpanel, "", sVSize);

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

	UIFactory.addVerticalSpacer(tpanel, vpanel, 16);

	pCopyFilesField = 
	  UIFactory.createTitledBooleanField(tpanel, "Copy Primary Files:", sTSize, 
					     vpanel, sVSize);

	vbox.add(comps[2]);
      }

      {
	JExportPanel panel = 
	  new JExportPanel(pChannel, "Clone All Properties:", sTSize, sVSize);
	pExportPanel = panel;

	vbox.add(panel);
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize+30, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize+30, 7));
	
	vbox.add(spanel);
      }
      
      vbox.add(Box.createVerticalGlue());
      
      JScrollPane scroll = UIFactory.createVertScrollPane(vbox, sTSize+sVSize+52, 312);

      String extra[][] = {
	{ "Browse",  "browse" }
      };

      super.initUI("Register Cloned Node:", scroll, "Confirm", "Apply", extra, "Close");

      pack();
    }  

    pFileSeqDialog = new JFileSeqSelectDialog(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the names of the newly registered nodes.
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
   * @param mod
   *   The node version.
   */ 
  public void 
  updateNode
  (
   String author, 
   String view, 
   NodeMod mod
  )
  {  
    pAuthor  = author; 
    pView    = view; 
    pNodeMod = mod; 

    synchronized(pRegistered) {
      pRegistered.clear();
    }

    pFileSeqDialog.updateHeader(author, view);
    pRootPath = new Path(PackageInfo.sWorkPath, author + "/" + view);

    pPrefixField.setPath(new Path(pNodeMod.getName()));

    FileSeq fseq = pNodeMod.getPrimarySequence();
    if(fseq.hasFrameNumbers()) {
      pFrameNumbersField.setText("YES");
      
      pStartFrameField.setEnabled(true);
      if(fseq.isSingle()) {
	pEndFrameField.setEnabled(false);
	pByFrameField.setEnabled(false);
      }
      else {
	pEndFrameField.setEnabled(true);
	pByFrameField.setEnabled(true);
      }

      pFramePaddingField.setEnabled(true);
    }
    else {
      pFrameNumbersField.setText("no");

      pStartFrameField.setValue(null);
      pStartFrameField.setEnabled(false);
      pEndFrameField.setValue(null);
      pEndFrameField.setEnabled(false);
      pByFrameField.setValue(null);
      pByFrameField.setEnabled(false);

      pFramePaddingField.setValue(null);
      pFramePaddingField.setEnabled(false);
    }

    pFileModeField.setText(fseq.isSingle() ? "Single File" : "File Sequence");
    pFrameNumbersField.setText(fseq.hasFrameNumbers() ? "YES" : "no");

    updateFileSeq(pNodeMod.getPrimarySequence());

    pCopyFilesField.setValue(true);
    pExportPanel.updateNode(mod);

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
    
    FileSeq ofseq = pNodeMod.getPrimarySequence();
    if(fseq.hasFrameNumbers() && ofseq.hasFrameNumbers()) {
      if(fseq.isSingle() && ofseq.isSingle()) {
	pStartFrameField.setValue(frange.getStart());
      }
      else if(!fseq.isSingle() && !ofseq.isSingle()) {
	pStartFrameField.setValue(frange.getStart());
	pEndFrameField.setValue(frange.getEnd());
	pByFrameField.setValue(frange.getBy());
      }

      pFramePaddingField.setValue(fpat.getPadding());
    }

    pSuffixField.setText(fpat.getSuffix());
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

    {
      FileSeq fseq = mod.getPrimarySequence();
      if(fseq.numFrames() > 10000) {
	JConfirmFrameRangeDialog diag = 
	  new JConfirmFrameRangeDialog(this, fseq.getFrameRange());
	diag.setVisible(true);
	if(!diag.wasConfirmed()) 
	  return;
      }
    }

    UIMaster master = UIMaster.getInstance();
    if(master.beginPanelOp("Registering Cloned Node: " + mod.getName())) {
      try {
	doClone(mod);
	synchronized(pRegistered) {
	  pRegistered.add(mod.getName());
	}
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
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
    
    {
      FileSeq fseq = mod.getPrimarySequence();
      if(fseq.numFrames() > 10000) {
	JConfirmFrameRangeDialog diag = 
	  new JConfirmFrameRangeDialog(this, fseq.getFrameRange());
	diag.setVisible(true);
	if(!diag.wasConfirmed()) 
	  return;
      }
    }

    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
    pCancelButton.setEnabled(false);

    CloneTask task = new CloneTask(mod);
    task.start();
  }

  /**
   * Browse for file sequences to use to fill in the frame range fields.
   */ 
  public void 
  doBrowse() 
  {
    pFileSeqDialog.setRootDir(pRootPath.toFile());

    Path prefix = pPrefixField.getPath();
    if(prefix != null) {
      Path path = new Path(pRootPath, prefix);
      File dir = path.toFile();
      if(!dir.isDirectory()) 
	dir = dir.getParentFile();

      pFileSeqDialog.updateTarget(dir);
    }

    pFileSeqDialog.setVisible(true);
    if(pFileSeqDialog.wasConfirmed()) {
      Path dpath = pFileSeqDialog.getDirectoryPath();
      if(dpath != null) {
	FileSeq fseq = pFileSeqDialog.getSelectedFileSeq();
	if(fseq != null) {
	  Path path = new Path(dpath, fseq.getFilePattern().getPrefix());
	  pPrefixField.setPath(path); 
	  updateFileSeq(fseq);
	}
	else {
	  pPrefixField.setPath(dpath); 
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the basic working node version described by the current dialog configuration. 
   */ 
  private NodeMod
  generateNodeMod() 
  {
    NodeMod mod = null;

    try {
      String name = pPrefixField.getText();
      String prefix = null;
      {
	if(!pPrefixField.isPathValid() || name.endsWith("/"))
	  throw new PipelineException
	    ("Unable to register node without a valid filename prefix!");
	
	Path path = new Path(name);
	prefix = path.getName();
      }

      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() == 0)) 
	suffix = null;
      
      FileSeq ofseq = pNodeMod.getPrimarySequence();

      FilePattern fpat = null;
      if(ofseq.hasFrameNumbers()) {
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
      if(ofseq.hasFrameNumbers()) {
	Integer startFrame = pStartFrameField.getValue();
	if(startFrame == null) 
	  throw new PipelineException
	    ("Unable to register node (" + name + ") which has frame numbers but an " + 
	     "unspecified start frame!");

	if(ofseq.isSingle()) {
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
	      ("Unable to register node (" + name + "):\n\n" + 
	       ex.getMessage());
	  }
	}
      }

      FileSeq primary = new FileSeq(fpat, frange);

      UIMaster master = UIMaster.getInstance();

      /* node properties */ 
      BaseEditor editor = null;
      if(pExportPanel.exportEditor()) 
	editor = pNodeMod.getEditor();
      else if(suffix != null) 
	editor = master.getMasterMgrClient(pChannel).getEditorForSuffix(suffix);

      String toolset = null;
      if(pExportPanel.exportToolset()) 
	toolset = pNodeMod.getToolset();
      else 
	toolset = master.getMasterMgrClient(pChannel).getDefaultToolsetName();
      
      mod = new NodeMod(name, primary, new TreeSet<FileSeq>(), toolset, editor);
    }
    catch(Exception ex) {
      showErrorDialog(ex);
    }

    return mod;
  }

  
  /**
   * Register the node and clone the selected links and files.
   */ 
  @SuppressWarnings("unchecked")
  private void
  doClone
  (
   NodeMod mod
  ) 
    throws PipelineException 
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient(pChannel);
    
    /* register the node */ 
    String name = mod.getName();
    client.register(pAuthor, pView, mod);

    /* upstream links */ 
    {
      boolean addedLinks = false;
      for(String source : pNodeMod.getSourceNames()) {
	if(pExportPanel.exportSource(source)) {
	  LinkMod link = pNodeMod.getSource(source);
	  client.link(pAuthor, pView, name, source, 
		      link.getPolicy(), link.getRelationship(), 
		      link.getFrameOffset());
	  addedLinks = true;
	}
      }
      
      /* update the links if we need them for per-source action parameters below */
      if(addedLinks && pExportPanel.exportActionSourceParams()) 
	mod = client.getWorkingVersion(pAuthor, pView, name);
    }
    
    /* actions */ 
    BaseAction action = null;
    {
      BaseAction oaction = pNodeMod.getAction(); 
      if((oaction != null) && pExportPanel.exportAction()) {
	  
	/* the action and parameters */ 
	{
	  PluginMgrClient mgr = PluginMgrClient.getInstance();
	  action = mgr.newAction(oaction.getName(), 
                                 oaction.getVersionID(), 
                                 oaction.getVendor()); 
	  
	  for(ActionParam param : oaction.getSingleParams()) {
	    if(pExportPanel.exportActionSingleParam(param.getName())) 
	      action.setSingleParamValue(param.getName(), param.getValue());
	  }
	  
	  if(pExportPanel.exportActionSourceParams()) 
	    action.setSourceParamValues(oaction);

	  mod.setAction(action);
	}
	
	/* action enabled */ 
	if(pExportPanel.exportActionEnabled()) 
	  mod.setActionEnabled(pNodeMod.isActionEnabled()); 
      }
    }
    
    if(action != null) {
      /* execution details */ 
      {
	if(pExportPanel.exportOverflowPolicy()) 
	  mod.setOverflowPolicy(pNodeMod.getOverflowPolicy());
	
	if(pExportPanel.exportExecutionMethod()) 
	  mod.setExecutionMethod(pNodeMod.getExecutionMethod());
	
	if(pExportPanel.exportBatchSize() && 
	   (pNodeMod.getExecutionMethod() == ExecutionMethod.Parallel))
	  mod.setBatchSize(pNodeMod.getBatchSize());
      }
      
      /* job requirements */ 
      {
	JobReqs jreqs = mod.getJobRequirements();
	JobReqs ojreqs = pNodeMod.getJobRequirements();
	
	if(pExportPanel.exportPriority()) 
	  jreqs.setPriority(ojreqs.getPriority());
	
	if (pExportPanel.exportRampUpInterval())
	  jreqs.setRampUp(ojreqs.getRampUp());
	
	if(pExportPanel.exportMaxLoad()) 
	  jreqs.setMaxLoad(ojreqs.getMaxLoad());
	
	if(pExportPanel.exportMinMemory()) 
	  jreqs.setMinMemory(ojreqs.getMinMemory());
	
	if(pExportPanel.exportMinDisk()) 
	  jreqs.setMinDisk(ojreqs.getMinDisk());
	
	for(String kname : pExportPanel.exportedLicenseKeys()) {
	  if(ojreqs.getLicenseKeys().contains(kname)) 
	    jreqs.addLicenseKey(kname);
	  else 
	    jreqs.removeLicenseKey(kname);
	}
	
	for(String kname : pExportPanel.exportedHardwareKeys()) {
	  if(ojreqs.getHardwareKeys().contains(kname)) 
	    jreqs.addHardwareKey(kname);
	  else 
	    jreqs.removeHardwareKey(kname);
	}

	for(String kname : pExportPanel.exportedSelectionKeys()) {
	  if(ojreqs.getSelectionKeys().contains(kname)) 
	    jreqs.addSelectionKey(kname);
	  else 
	    jreqs.removeSelectionKey(kname);
	}

	mod.setJobRequirements(jreqs);
      }
    }
    
    /* apply the changes */ 
    client.modifyProperties(pAuthor, pView, mod);
    
    /* Do the annotations */
    {
      TreeMap<String, BaseAnnotation> annots = client.getAnnotations(pNodeMod.getName());
      for (String aname : annots.keySet()) {
	if (pExportPanel.exportAnnotation(aname)) {
	  BaseAnnotation an = annots.get(aname);
	  PluginMgrClient mgr = PluginMgrClient.getInstance();
	  BaseAnnotation newAnnot = mgr.newAnnotation(an.getName(), 
	                                              an.getVersionID(), 
	                                              an.getVendor()); 
	  for (AnnotationParam param : an.getParams()) {
	    String paramName = param.getName();
	    Comparable value = param.getValue();
	    AnnotationParam newParam = newAnnot.getParam(paramName);
	    newParam.setValue(value);
	  }
	  client.addAnnotation(mod.getName(), aname, newAnnot);
	}
      }
    }

    /* copy the files */ 
    if(pCopyFilesField.getValue()) {
      client.cloneFiles(new NodeID(pAuthor, pView, pNodeMod.getName()), 
			new NodeID(pAuthor, pView, mod.getName()));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Clone a new node.
   */ 
  private
  class CloneTask
    extends Thread
  {
    public 
    CloneTask
    (
     NodeMod mod 
    ) 
    {
      super("JCloneDialog:CloneTask");
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Registering Cloned Node: " + pNodeMod.getName())) {
	try {
	  doClone(pNodeMod);
	  synchronized(pRegistered) {
	    pRegistered.add(pNodeMod.getName());
	  }
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }

      SwingUtilities.invokeLater(new DoneTask());
    }

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
      super("JCloneDialog:DoneTask");
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

  private static final long serialVersionUID = -4841769849544603138L;
  
  private static final int sTSize  = 150;
  private static final int sVSize  = 300;
  private static final int sVSize1 = 80;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract pathname of the root directory of the current working area.
   */ 
  private Path pRootPath;

  /**
   * The original working node version being cloned.
   */ 
  private NodeMod  pNodeMod; 

  /**
   * The names of the registered nodes.
   */ 
  private TreeSet<String>  pRegistered; 

  /**
   * The index of the update channel.
   */ 
  private int  pChannel; 



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
  private JTextField  pFileModeField;
  
  /**
   * The frame numbers field.
   */ 
  private JTextField  pFrameNumbersField;
  
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
   * Whether to copy the files.
   */ 
  private JBooleanField  pCopyFilesField;


  /**
   * Whether to clone the other node parameters, actions and links.
   */ 
  private JExportPanel  pExportPanel; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The file sequences dialog.
   */ 
  private JFileSeqSelectDialog  pFileSeqDialog;

}
