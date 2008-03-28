// $Id: NukeSourceSyncTool.java,v 1.3 2008/03/28 21:26:42 jim Exp $

package us.temerity.pipeline.plugin.NukeSourceSyncTool.v2_3_4;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*; 
import static java.lang.Math.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   S O U R C E   S Y N C   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Synchronizes the source nodes linked to a Nuke script node with the 
 * sequences referenced by the script itself.
 */
public
class NukeSourceSyncTool
  extends BaseTool
  implements ComponentListener, ActionListener
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeSourceSyncTool()
  {
    super("NukeSourceSync", new VersionID("2.3.4"), "Temerity",
	  "Synchronizes the source sequences referenced Nuke comp with Pipeline.");

    underDevelopment();
    
    pPhase = 1; 

    pImageFormats = new ArrayList<String>(); 
    pImageFormats.add("bmp");
    pImageFormats.add("iff");
    pImageFormats.add("gif");
    pImageFormats.add("hdr");
    pImageFormats.add("jpg");
    pImageFormats.add("jpeg");
    pImageFormats.add("map");
    pImageFormats.add("png");
    pImageFormats.add("ppm");
    pImageFormats.add("psd");
    pImageFormats.add("rgb");
    pImageFormats.add("sgi");
    pImageFormats.add("bw");
    pImageFormats.add("tga");
    pImageFormats.add("tif");
    pImageFormats.add("tiff");

    pSequences = new TreeMap<String,pSequenceInfo>();

    pWorkingPathFields = new TreeMap<String,JPathField>();

    pUnlinkNodes   = new TreeSet<NodeID>();       
    pInvalidNodes  = new TreeMap<String,String>();

    pRegisterFields = new TreeMap<NodeID,JBooleanField>();
    pRenumberFields = new TreeMap<NodeID,JBooleanField>();
    pCheckOutFields = new TreeMap<NodeID,JBooleanField>(); 
    pLinkFields     = new TreeMap<NodeID,JBooleanField>(); 
    pUnlinkFields   = new TreeMap<NodeID,JBooleanField>();
    
    addSupport(OsType.MacOS);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    switch(pPhase) {
    case 1:
      
      // change this to true to enable debugging log
      if (false) {
	try {
	  File tempFile = File.createTempFile("NukeSourceSyncTool-DebugInfo.", ".txt", 
	     PackageInfo.sTempPath.toFile());
	  tempOutput = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
	} 
        catch ( IOException e ) {
	  throw new PipelineException(e); 
	}
      }
	
      return collectFirstPhaseInput();

    case 2:
      return collectSecondPhaseInput();

    case 3:
      return collectThirdPhaseInput();

    default:
      throw new IllegalStateException(); 
    }
  }


  /**
   * Select the target nodes and relocation directory.
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */ 
  private String
  collectFirstPhaseInput() 
    throws PipelineException 
  {
    /* create dialog components */ 
    JComponent body = null;
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      body = (JComponent) comps[2];
      
      pTargetScriptField = 
	UIFactory.createTitledPathField
	(tpanel, "Target Script Node:", sTSize, 
	 vpanel, new Path("/"), sVSize, 
	 "The node associated with the Nuke script being checked.");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
            
      {
	JComponent rcomps[] = 
	  UIFactory.createTitledBrowsablePathField
	  (tpanel, "Image Copy Directory:", sTSize, 
	   vpanel, new Path("/"), sVSize, 
	   this, "relocate-browse", 
	   "The fully resolved working area destination directory to copy image sequences " + 
	   "which are currently outside the working area.");
	
	pRelocatePathField  = (JPathField) rcomps[0];	   
      }

      UIFactory.addVerticalGlue(tpanel, vpanel);
    }
    
    /* validate node selections and initialize dialog components */ 
    {
      if(pPrimary == null)
	throw new PipelineException
	  ("The primary selection must be the Target Script Node!");
      pTargetScriptField.setText(pPrimary);
      
      {
	NodeStatus status = pSelected.get(pPrimary);
	NodeID nodeID = status.getNodeID();
	if(!nodeID.getAuthor().equals(PackageInfo.sUser)) 
	  throw new PipelineException 
	    ("Only nodes owned by the current user (" + PackageInfo.sUser + ") can " +
	     "be used as the target of the Nuke Source Sync tool!");
	
	pView = nodeID.getView(); 
      }
      
      {
	Path wpath = new Path(PackageInfo.sWorkPath, PackageInfo.sUser + "/" + pView);
	pWorkRoot = wpath.toOsString();
      }
      
      if(pSelected.size() == 1) {
	File path = new File(pPrimary);
	pRelocatePathField.setText(path.getParent());
      }
      else if(pSelected.size() == 2) {
	for(String name : pSelected.keySet()) {
	  if(!name.equals(pPrimary)) {
	    File path = new File(name);
	    pRelocatePathField.setText(path.getParent());
	  }
	}
      }
      else {
	throw new PipelineException
	  ("Please select one Target Script Node as the primary selection, " +
	    "and optionally one node from which to derive the Image Copy Directory.");
      }
    }
    
    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Nuke Source Sync: Target Script Node", body, "Continue");
    
    pRelocateDialog = 
      new JFileSelectDialog(diag, "Select Directory", 
			    "Select Image Copy Directory:", "Select");
    
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      pTargetScript = pTargetScriptField.getText();
      if((pTargetScript == null) || (pTargetScript.length() == 0))
	throw new PipelineException("Illegal Target Script Node name!");
      
      pRelocatePath = pRelocatePathField.getText();
      if((pRelocatePath == null) || (pRelocatePath.length() == 0))
	throw new PipelineException("Illegal Image Copy Directory name!");
	
      return ": Collecting Source Information...";
    }
    else
      return null;
  }

  /**
   * Presents the results of the sequence validation and allows changes to the working
   * area locations of sequences.
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */ 
  private String
  collectSecondPhaseInput() 
    throws PipelineException 
  {
    /* create dialog components */ 
    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      /* invalid sequences */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}
	
	{
	  Box ibox = new Box(BoxLayout.Y_AXIS);

	  TreeMap<String,pSequenceInfo> seqs = pGetSequences(SequenceState.INVALID);
	  if(seqs.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(String sequence : seqs.keySet()) {
	      JPanel panel = new JPanel();
	      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	      panel.add(Box.createRigidArea(new Dimension(0, 4)));

	      {
		Box mbox = new Box(BoxLayout.X_AXIS);
		
		mbox.add(Box.createRigidArea(new Dimension(4, 0)));
		
		{
		  JTextArea area = new JTextArea(seqs.get(sequence).errorMessage, 0, 45);
		  area.setName("HistoryTextArea");
		  
		  area.setLineWrap(true);
		  area.setWrapStyleWord(true);
		
		  area.setEditable(false);

		  Dimension size = area.getPreferredSize();
		  size.height = Integer.MAX_VALUE;
		  area.setMaximumSize(size);
		  
		  mbox.add(area);
		}
		
		mbox.add(Box.createRigidArea(new Dimension(4, 0)));
		mbox.add(Box.createHorizontalGlue());
		
		panel.add(mbox);
	      }
	      
	      panel.add(Box.createRigidArea(new Dimension(0, 4)));

	      JDrawer drawer = new JDrawer("Sequence: " + sequence, panel, true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Missing Sequences:", hbox, true);
	vbox.add(drawer);
      }

      /* valid sequences */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}
	
	{
	  Box ibox = new Box(BoxLayout.Y_AXIS);
	  
	  TreeMap<String,pSequenceInfo> seqs = pGetSequences(SequenceState.VALID);
	  if(seqs.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(String sequence : seqs.keySet()) {
	      pSequenceInfo seqInfo = seqs.get(sequence); 
	      NodeID nodeID = seqInfo.nodeID;

	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
      
	      {
		{
		  JLabel label = 
		    UIFactory.createFixedLabel
		    ("Nuke Nodes:", sTSize-7, JLabel.RIGHT, 
		     "The names of the Nuke nodes that reference this sequence.");
		  tpanel.add(label);
		}

		tpanel.add(Box.createVerticalGlue());
	      
		String nodeNames = "";
		for(String nukeNode : seqInfo.nukeNodeNames) {
		  nodeNames = nodeNames + " " + nukeNode;
		}
		vpanel.add(Box.createRigidArea(new Dimension(0, 3)));
		vpanel.add(UIFactory.createTextField(nodeNames.trim(), sVSize, JLabel.CENTER));
	      }
	      
	      {
	      	{
		  JLabel label = 
		    UIFactory.createFixedLabel
		    ("Status:", sTSize-7, JLabel.RIGHT, 
		     "Status of this Source Sequence");
		  tpanel.add(label);
		}

		tpanel.add(Box.createVerticalGlue());
	      
		String status;
		if (nodeID != null) {
		  status = "Sequence already registered with Pipeline.";
		  if (!sequence.startsWith("WORKING"))
		    status += " Path will be converted to WORKING-style.";
		  else
		    status += " Path is correct!";
		}
		else 
		  status = "Sequence will be copied to (or left in) the following " +
		  	   "working directory.";
		vpanel.add(UIFactory.createTextField(status, sVSize, JLabel.CENTER));
	      }

	      if (nodeID == null) {
		
		JComponent rcomps[] = 
		  UIFactory.createTitledBrowsablePathField
		  (tpanel, "Working Directory:", sTSize-7, 
		   vpanel, new Path(seqInfo.relocateDir), sVSize, 
		   this, "working-directory-browse:" + sequence, 
		   "The fully resolved working area directory where the sequence " + 
		   "should be located.");
		
		pWorkingPathFields.put(sequence, (JPathField) rcomps[0]);
	      }
	      
	      JDrawer drawer = 
		new JDrawer("Sequence: " + sequence, (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Existing Sequences:", hbox, true);
	vbox.add(drawer);
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	
	vbox.add(spanel);
      }

      {
	scroll = new JScrollPane(vbox);
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
	Dimension size = new Dimension(sTSize+sVSize+52, 500);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
    }    

    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Nuke Source Sync: Sequence Changes", scroll, "Continue");
    
    diag.setVisible(true);
    diag.pack();
    if(diag.wasConfirmed()) {
      for(String sequence : pWorkingPathFields.keySet()) {
	JPathField field = pWorkingPathFields.get(sequence);

	String path = field.getText();
	if((path != null) && (path.length() > 0)) {
	  Path tpath = new Path(pWorkRoot + "/" + path);
	  if(!tpath.toFile().isDirectory()) 
	    throw new PipelineException
	      ("The Working Directory (" + path + ") specified for Sequence " + 
	       "(" + sequence + ") does not exist!");

	  pSequences.get(sequence).relocateDir = new File(path);
	}
	else {
	  throw new PipelineException
	    ("No Working Directory was specified for Sequence (" + sequence + ")!");
	}
      }
	
      return ": Collecting Node Information...";
    }
    else
      return null;
  }

  /**
   * Confirm the changes to be made to the nodes.
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */ 
  private String
  collectThirdPhaseInput() 
    throws PipelineException 
  {
    /* create dialog components */ 
    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      /* invalid nodes */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}

	{
	  Box ibox = new Box(BoxLayout.Y_AXIS);

	  if(pInvalidNodes.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(String name : pInvalidNodes.keySet()) {
	      JPanel panel = new JPanel();
	      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	      panel.add(Box.createRigidArea(new Dimension(0, 4)));

	      {
		Box mbox = new Box(BoxLayout.X_AXIS);
		
		mbox.add(Box.createRigidArea(new Dimension(4, 0)));
		
		{
		  JTextArea area = new JTextArea(pInvalidNodes.get(name), 0, 45);
		  area.setName("HistoryTextArea");
		  
		  area.setLineWrap(true);
		  area.setWrapStyleWord(true);
		
		  area.setEditable(false);

		  Dimension size = area.getPreferredSize();
		  size.height = Integer.MAX_VALUE;
		  area.setMaximumSize(size);
		  
		  mbox.add(area);
		}
		
		mbox.add(Box.createRigidArea(new Dimension(4, 0)));
		mbox.add(Box.createHorizontalGlue());
		
		panel.add(mbox);
	      }
	      
	      panel.add(Box.createRigidArea(new Dimension(0, 4)));

	      JDrawer drawer = new JDrawer("Node: " + name, panel, true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Invalid Pipeline Nodes:", hbox, false);
	vbox.add(drawer);
      }

      /* Referenced Sequences */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}

	{
	  Box ibox = new Box(BoxLayout.Y_AXIS);

	  TreeMap<String,pSequenceInfo> seqs = pGetSequences(SequenceState.VALID);
 	  if(seqs.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    
	    TreeSet<NodeID> processedNodes = new TreeSet<NodeID>();
	    for(String sequence : seqs.keySet()) {
	      pSequenceInfo seqInfo = seqs.get(sequence);
	      NodeID nodeID = seqInfo.nodeID;
	      if (processedNodes.contains(nodeID))
		continue;
	      
	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      {
		{
		  JLabel label = 
		    UIFactory.createFixedLabel
		    ("Nuke Nodes:", sTSize-7, JLabel.RIGHT, 
		     "The names of the Nuke nodes that reference this sequence.");
		  tpanel.add(label);
		}
		
		tpanel.add(Box.createVerticalGlue());

		String nodeNames = "";
		for(pSequenceInfo sInfo : pGetSequences(nodeID))
		  for(String nukeNode : sInfo.nukeNodeNames)
		    nodeNames = nodeNames + " " + nukeNode;

		vpanel.add(Box.createRigidArea(new Dimension(0, 3)));
		vpanel.add(UIFactory.createTextField(nodeNames.trim(), sVSize, JLabel.CENTER));
	      }

	      
	      NodeMod mod = seqInfo.workingVersion;
	      String status;
	      if (mod == null) {
		if (seqInfo.doRegister)
		  status = "No Pipeline node exists.";
		else 
		  status = "Node needs checkout.";
	      } 
	      else {
		if ( mod.isLocked() )
		  status = "Node exists locally and is locked";
		else if ( mod.isFrozen() )
		  status = "Node exists locally and is frozen";
		else {
		  status = "Node exists locally and is modifiable";
		}
	      }	
	      UIFactory.createTitledTextField(tpanel, "Node Existence", sTSize - 7,
		vpanel, status, sVSize2);

	      
	      if(seqInfo.doRegister || seqInfo.doRenumber || seqInfo.doCheckout || seqInfo.doLink) {
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		if(seqInfo.doRegister) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Register Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to register a new node for the sequence.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("register-changed:" + nodeID.getName());
		  
		  pRegisterFields.put(nodeID, field);
		}

		if(seqInfo.doRenumber) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Renumber Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to change the frame range of the node to match " +
		     "the referenced sequences.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("renumber-changed:" + nodeID.getName());
		  
		  pRenumberFields.put(nodeID, field);
		}
		
		if(seqInfo.doCheckout) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "CheckOut Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to check-out the latest version of the node associated with " + 
		     "the sequence.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("check-out-changed:" + nodeID.getName());
		  
		  pCheckOutFields.put(nodeID, field);
		}
		
		if(seqInfo.doLink) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Link Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to link the node associated with the sequence to the Target " + 
		     "Node.");
		  field.setValue(true);
		  
		  pLinkFields.put(nodeID, field);
		}
	      }
	      
	      processedNodes.add(nodeID);
	      JDrawer drawer = 
		new JDrawer("Pipeline Node: " + nodeID.getName(), (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }	
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Referenced Nodes:", hbox, true);
	vbox.add(drawer);
      }
      
      /* Unreferenced Sequences */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}

	{
	  Box ibox = new Box(BoxLayout.Y_AXIS);

	  if(pUnlinkNodes.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(NodeID nodeID : pUnlinkNodes) {
	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	
	      JBooleanField field = 
		UIFactory.createTitledBooleanField
		(tpanel, "Unlink Node:", sTSize-7, 
		 vpanel, sVSize2, 
		 "Whether to unlink the node.  (Sequence is not referenced " + 
		 "by the Nuke script.");
	      field.setValue(true);
	      
	      pUnlinkFields.put(nodeID, field);
	      
	      JDrawer drawer = 
		new JDrawer("Node: " + nodeID.getName(), (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Unreferenced Nodes:", hbox, true);
	vbox.add(drawer);
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize2, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize2, 7));
	
	vbox.add(spanel);
      }
      
      {
	scroll = new JScrollPane(vbox);
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
	Dimension size = new Dimension(sTSize+sVSize2+52, 500);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);
	
	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
    }    

    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Nuke Source Sync: Node Changes", scroll, "Confirm");
    
    diag.setVisible(true);
    diag.pack();
    if(diag.wasConfirmed()) {
      for(NodeID nodeID : pRegisterFields.keySet()) {
	JBooleanField field = pRegisterFields.get(nodeID);
	Boolean value = field.getValue();
	if(value == null)
	  for(pSequenceInfo seqInfo : pGetSequences(nodeID))
	    seqInfo.doRegister = value;
      }

      for(NodeID nodeID : pCheckOutFields.keySet()) {
	JBooleanField field = pCheckOutFields.get(nodeID);
	Boolean value = field.getValue();
	if(value == null) 
	  for(pSequenceInfo seqInfo : pGetSequences(nodeID))
	    seqInfo.doCheckout = value;
      }

      for(NodeID nodeID : pRenumberFields.keySet()) {
	JBooleanField field = pRenumberFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  for(pSequenceInfo seqInfo : pGetSequences(nodeID))
	    seqInfo.doRenumber = value;
      }

      for(NodeID nodeID : pLinkFields.keySet()) {
	JBooleanField field = pLinkFields.get(nodeID);
	Boolean value = field.getValue();
	if(value == null) 
	  for(pSequenceInfo seqInfo : pGetSequences(nodeID))
	    seqInfo.doLink = value;
      }

      for(NodeID nodeID : pUnlinkFields.keySet()) {
	JBooleanField field = pUnlinkFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pUnlinkNodes.remove(nodeID);
      }
	
      return ": Modifying Nodes...";
    }
    else
      return null;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one phase in the execution of the tool. <P> 
   *    
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    switch(pPhase) {
    case 1:
      return executeFirstPhase(mclient, qclient);

    case 2:
      return executeSecondPhase(mclient, qclient);

    case 3:
      return executeThirdPhase(mclient, qclient);

    default:
      throw new IllegalStateException(); 
    }
  }

  /**
   * Collect source sequence info from the Nuke script.
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  private boolean
  executeFirstPhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {    
    /* get the current status of the Nuke script node */ 
    {
      pTargetScriptID = new NodeID(PackageInfo.sUser, pView, pTargetScript);
      pTargetScriptStatus = mclient.status(pTargetScriptID);
      pTargetScriptMod = pTargetScriptStatus.getDetails().getWorkingVersion();
      if(pTargetScriptMod == null) 
	throw new PipelineException
	  ("No working version of the Target Script Node (" + pTargetScript + ") exists " + 
	   "in the (" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");

      /* verify that it is a Nuke script */
      {
	FileSeq fseq = pTargetScriptMod.getPrimarySequence();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || (!suffix.equals("nk") && !suffix.equals("nuke"))) 
	  throw new PipelineException
	    ("The Target Script Node (" + pTargetScript + ") must have a Nuke Script as " + 
	     "its primary file sequence!");
	pTargetScriptFile = fseq.getFile(0);
	pTargetFullName = new Path(PackageInfo.sProdPath, pTargetScriptID.getWorkingParent()
	    			+ "/" + fseq.getFile(0));
      }
    }
    
    /* collect source information from Nuke script */ 
    try {
      BufferedReader in = new BufferedReader(new FileReader(pTargetFullName.toFile())); 

      try { 
        boolean isRead     = false;
        Integer first = -1;
        Integer last = -1;
        String nukeSeq = null;
        while(true) {
          String line = in.readLine();
          if(line == null) 
            break;

          /* TODO:  this was cribbed from NukeCompAction.  It doesn't look like very robust,
           * as it depends on whitespace.  Probably should replace with a regex, or at least
           * tokenization.  Even better would be to run Nuke in batch mode, and ask it to 
           * spit out the info for us.
           */
          if(line.startsWith("Read {"))
            isRead = true;
          else if(line.startsWith("}")) {
            isRead = false;
            first = -1;
            last = -1;
          }
          else if(isRead) {
            if (line.startsWith(" file "))
              nukeSeq = line.substring(6);
            else if (line.startsWith(" first "))
              first = new Integer(line.substring(7));	
            else if (line.startsWith(" last "))
              last = new Integer(line.substring(6));
            else if (line.startsWith(" name ")) {
              String currentName = line.substring(6);
              FrameRange range = null;
              if (last > -1) {
        	if (! (first > -1))
        	  first = 1;
        	range = new FrameRange(first, last, 1);       
              }
              log("found: "+nukeSeq);
              pSequenceInfo newSeq = pSequences.get(nukeSeq);
              /* unique sequence - create a new pSequenceInfo */
              if (newSeq == null) {
        	newSeq = new pSequenceInfo(nukeSeq);
                newSeq.srcFileSeq = parseNukeSequence(nukeSeq, range);
              } 
              /* duplicate sequence - check frame ranges */
              else if (range != null) {
        	FileSeq fseq = newSeq.srcFileSeq;
        	FilePattern fpat = fseq.getFilePattern();
        	FrameRange oldRange = fseq.getFrameRange();
               	range = new FrameRange(min(first, oldRange.getStart()), 
               	  			max(last, oldRange.getEnd()), 1);
               	if (!range.equals(oldRange))
               	  newSeq.srcFileSeq = new FileSeq(fpat, range);
              }
              newSeq.nukeNodeNames.add(currentName);
              pSequences.put(nukeSeq, newSeq);
            }
          }
        }
      }
      finally {
        in.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to read info from Nuke script file (" + pTargetScript + ")!\n" +
         ex.getMessage());
    }

    /* Check the source sequences. */ 
    {
      for(String sequence : pSequences.keySet()) {
	
	pSequenceInfo seqInfo = pSequences.get(sequence);
	try {
	  log("doing "+sequence);
	  if(sequence.length() == 0) 
	    throw new PipelineException
	      ("No sequence was specified!");

	  FileSeq srcFileSeq = seqInfo.srcFileSeq;
	  if (srcFileSeq == null) {
	    seqInfo.isValid = false;
	    seqInfo.errorMessage = "Nuke sequence does not conform to the " +
		 			"Pipeline naming convention.";
	    continue;
	  }

	  /* Test for the file or sequence's existence.
	   * If we're dealing with a file sequence, just test the directory it lives in.
	   * Why?  It could be a rendered sequence from 3D, or some such, with broken or
	   * missing frames.  These will be caught by pipeline when the node for the 
	   * sequence is created.
	   */
	  File file = srcFileSeq.getFile(0);

	  log("checking for "+file);
	  /* Check for existence */
	  if ((srcFileSeq.isSingle() && file.isFile()) ||
	      (!srcFileSeq.isSingle() && file.getParentFile().isDirectory())) {
	    seqInfo.isValid = true;
	  } else {
	    seqInfo.isValid = false;
	    seqInfo.errorMessage = "Sequence does not exist on disk.";
	    continue;
	  }
	    
	  {
	    /* Try to figure out the Pipeline node name, based on whether or not it appears 
	     * to be a Pipeline style path.  Who knows where the hell the user got the path:
	     * Nuke browser, someone emailing it to him, copying the script directly from 
	     * someone else's working directory, etc.
	     * 
	     * TODO:  This is not OS-independent!  Per jesse:  this assumes that all paths
	     * coming from Nuke will be Unix-style.  Probably safer to do the path mangling
	     * in an abstract space.  
	     * 
	     * TODO:  Also, MayaTextureSync, from which I've been stealing, was validating 
	     * its sources using the "canonical" paths.  This tool does not.  Again, per 
	     * jesse, this should be o.k.
	     */
	    String workingFile = file.getPath();

	    /* Correctly prefixed WORKING path:  This is where we want to be. 
	     */
	    if (sequence.startsWith("WORKING")) {
	      workingFile = workingFile.substring(pWorkRoot.length());
	      seqInfo.relocateDir = new File(workingFile).getParentFile();
	    }
	    /* Absolute path, but still points to a working area:  
	     * NOT where we want to be, but there may still be a node. 
	     */
	    else if (workingFile.startsWith(PackageInfo.sWorkPath.toString())) {
	      workingFile = workingFile.substring(PackageInfo.sWorkPath.toString().length());
	      workingFile = workingFile.replaceFirst("/[^/.]+/[^/.]+", "");
	      seqInfo.relocateDir = new File(workingFile).getParentFile();
	      seqInfo.doFixPath = true;
	    }
	    /* Absolute path, but points to a repository version:  
	     * NOT where we want to be, but there should be a node. 
	     * TODO: parse the version number, and pass it to the check-out.
	     * This currently ignores version, and forces the latest.
	     */
	    else if (workingFile.startsWith(PackageInfo.sProdPath.toString()+"/repository")) {
	      workingFile = workingFile.substring(PackageInfo.sProdPath.toString().length() + 11);
	      workingFile = workingFile.replaceFirst("/[^/.]+/[0-9]+\\.[0-9]+\\.[0-9]+/", "/");
	      seqInfo.relocateDir = new File(workingFile).getParentFile();
	    }
	    /* Otherwise, it's some other random path, and we'll have to relocate it */
	    else {
	      seqInfo.relocateDir = new File(pRelocatePath);
	    }

	    /* Try to find the node that owns this path.  If no node is found here, it is
	     * assumed that a new one will have to be created. */
	    log("looking for file "+workingFile);
	    String name = mclient.getNodeOwning(workingFile);
	    if(name != null) {
	      log("pwnd by "+name);
	      seqInfo.nodeID = new NodeID(PackageInfo.sUser, pView, name);
	      NodeDetails details = mclient.status(seqInfo.nodeID).getDetails(); 
	      NodeCommon com = null;
	      
	      /* figure out *which* sequence we are within the owning node */
	      if(details.getOverallNodeState() == OverallNodeState.CheckedIn)
		com = details.getLatestVersion();
	      else {
		seqInfo.workingVersion = details.getWorkingVersion();
		com = seqInfo.workingVersion;
	      }
	      FileSeq testSeq = new FileSeq(seqInfo.getSrcFilePrefix(), seqInfo.getSrcFileSuffix());
	      log("TESTSEQ " + testSeq.getFilePattern());
	      for(FileSeq fseq : com.getSequences()) {
		log("\t"+fseq.getFilePattern());
		if(fseq.similarTo(testSeq)) {
		  seqInfo.registeredFileSeq = fseq;
		  log("\tFOUND");
		  break;
		}
	      }
	    }
	  } /* end - get pipeline node */
	  
	}
	catch(PipelineException ex) {
	  seqInfo.isValid = false;
	  seqInfo.errorMessage = ex.getMessage();
	}
      }
    }
    
    pPhase++; 
    return true;
  }
  
  /**
   * Collect information about the nodes associated with the referenced sequences.
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  private boolean
  executeSecondPhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  { 
    TreeMap<String,pSequenceInfo> seqs = pGetSequences(SequenceState.VALID);
    for(String sequence : seqs.keySet()) {

      pSequenceInfo seqInfo = seqs.get(sequence);
      NodeID nodeID = seqInfo.nodeID;

      /* Sequence not associated with a node - need to register a new one */
      if(nodeID == null) {
	String filePrefix = seqInfo.getSrcFilePrefix();
	nodeID = new NodeID(PackageInfo.sUser, pView,seqInfo.relocateDir + "/" + filePrefix);
	seqInfo.nodeID = nodeID;
	seqInfo.doRegister = true;
	seqInfo.doLink = true;
	log("Node Will Be Created:  " + nodeID.getName());
      }
      /* Node exists - optionally check it out, and link to it */
      else {
	seqInfo.doRegister = false;
	log("Node Exists:  " + nodeID.getName());

	if (seqInfo.workingVersion == null) {
	  seqInfo.doCheckout = true;
	  seqInfo.doLink = true;
	}
	/* Otherwise, just link */
	else {
	  if(!pTargetScriptMod.getSourceNames().contains(nodeID.getName())) 
	    seqInfo.doLink = true;
	  log(nodeID.getName() + " exists in the working area");
	}

	/* Check to see if we're referencing frames outside of the 
	 * already registered range.
	 * TODO:  turn renumbering funcionality back on, instead of throwing
	 * an exception. */
	if (seqInfo.srcFileSeq.hasFrameNumbers() && 
	  seqInfo.registeredFileSeq.hasFrameNumbers()) {

	  FrameRange newRange = seqInfo.srcFileSeq.getFrameRange();
	  FrameRange oldRange = seqInfo.registeredFileSeq.getFrameRange();

	  boolean throwIt = false;
	  if (newRange == null || oldRange == null)
	    throwIt = true;
	  else { 
	    newRange = new FrameRange(min(newRange.getStart(), oldRange.getStart()), 
	      				max(newRange.getEnd(), oldRange.getEnd()), 1);
	    if (!newRange.equals(oldRange))
	      throwIt = true;
	  }

	  if (throwIt) { 
	    String msg = "ERROR:  the Nuke script refers to frames outside of the " +
	    "range registered in Pipeline.  Please perform a Renumber " +
	    "operation on the Pipeline node, to include all existing " +
	    "frames, and run this tool again.\n\n";
	    msg += "Pipeline Node: " + nodeID.getName() + "\nFrame Range: " + 
	    oldRange + "\n\n";
	    msg += "Nuke Nodes / Frame Ranges:\n";
	    for (pSequenceInfo seq : pGetSequences(nodeID)) {
	      for (String node : seq.nukeNodeNames)
		msg += node + " ";
	      msg += ": " + seq.srcFileSeq.getFrameRange() + "\n";
	    }
	    throw new PipelineException(msg);
	  }
	}
      }
    }

    /*
     * Find unreferenced nodes to unlink.
     */
    for(String name : pTargetScriptMod.getSourceNames()) {
      NodeStatus status = pTargetScriptStatus.getSource(name);
      NodeDetails details = status.getDetails();
      NodeMod mod = details.getWorkingVersion();
      
      /* TODO:  we're testing for hard-coded suffixes in order to determine
       * which sources to consider image sequences.  This is a lousy idea.  
       * At the very least, the list of accepted image formats should be moved
       * somewhere higher up in the package, and be made configurable or something.
       * Also - only the primary sequence is tested for image-ness.
       */
      FileSeq fseq = mod.getPrimarySequence();
      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix != null) && pImageFormats.contains(suffix)) {
	NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);	
	if(pGetSequences(nodeID).size() == 0) {
	  pUnlinkNodes.add(nodeID); 
	  log("UNLINK: "+name);
	}
      }
    }
    
    pPhase++; 
    return true;
  }

  /**
   * Modify nodes, copy sequences and fix the sequence paths in the target Nuke script.
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  private boolean
  executeThirdPhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {       
    TreeMap<String,pSequenceInfo> seqs;
    
    /* register nodes */ 
    {
      String toolset = mclient.getDefaultToolsetName();
      seqs = pGetSequences(SequenceState.REGISTER);
      for(String sequence : seqs.keySet()) {
	pSequenceInfo seqInfo = seqs.get(sequence);
	NodeID nodeID = seqInfo.nodeID;
	
	FileSeq srcFileSeq = seqInfo.srcFileSeq;
	FilePattern srcFilePat = srcFileSeq.getFilePattern();
	String prefix = seqInfo.getSrcFilePrefix();
	String suffix = seqInfo.getSrcFileSuffix();
	
	FileSeq primarySeq;
	FileSeq dstFileSeq;
	if (! srcFileSeq.hasFrameNumbers()) {
	  primarySeq = new FileSeq(prefix, suffix);
	  dstFileSeq = new FileSeq(pWorkRoot + "/" + nodeID.getName(), suffix);
	} else {
	  FilePattern fpat = new FilePattern(prefix, srcFilePat.getPadding(), suffix);
	  primarySeq = new FileSeq(fpat, srcFileSeq.getFrameRange());
	  fpat = new FilePattern(pWorkRoot + "/" + nodeID.getName(), 
	    			 srcFilePat.getPadding(), suffix);
	  dstFileSeq = new FileSeq(fpat, srcFileSeq.getFrameRange());
	}
	
	BaseEditor editor = mclient.getEditorForSuffix(primarySeq.getFilePattern().getSuffix());
	seqInfo.workingVersion = new NodeMod(nodeID.getName(), primarySeq, null, toolset, editor);
 	mclient.register(PackageInfo.sUser, pView, seqInfo.workingVersion);
 	seqInfo.doFixPath = true;

 	/* Copy sequences into the appropriate place, if necessary.
 	 * TODO:  Shouldn't we be doing this through Java, in an OS-independent way?
 	 */
	if (! srcFileSeq.getPath(0).toOsString().equals(dstFileSeq.getPath(0).toOsString())) {
	  Map<String,String> env = mclient.getToolsetEnvironment(nodeID.getAuthor(), 
	    							 nodeID.getView(), 
	    							 seqInfo.workingVersion.getToolset());
	  String dst = dstFileSeq.getPath(0).getParentPath().toOsString();
	  for (int i=0; i<srcFileSeq.numFrames(); i++) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    String src = srcFileSeq.getPath(i).toOsString();
	    args.add(src);
	    args.add(dst);

	    log ("cp "+args);
	    SubProcessLight proc = 
	      	new SubProcessLight("CopyFrame", "cp", args, env, PackageInfo.sTempPath.toFile());
	    try {	    
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		("Unable to copy the image file (" + src + ") to the " + 
		  "working area location (" + dst + "):\n\n" + 
		  proc.getStdErr());
	    } catch(InterruptedException ex) {
	      throw new PipelineException
	      ("Interrupted while copying image file ("+src+")!");
	    }	  
	  }
	}
      }
    }

    /* check-out nodes */ 
    seqs = pGetSequences(SequenceState.CHECKOUT);
    for(String sequence : seqs.keySet()) {
      pSequenceInfo seqInfo = seqs.get(sequence);
      NodeID nodeID = seqInfo.nodeID;
      mclient.checkOut(nodeID, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
      seqInfo.workingVersion = mclient.status(nodeID).getDetails().getWorkingVersion();
      seqInfo.doFixPath = true;
    }

    /* TODO - renumber nodes.  This would be a neat feature, but it's too much of a pain
     * in the ass for now.  
     * 
    seqs = pGetSequences(SequenceState.RENUMBER);
    for(String sequence : seqs.keySet()) {
      pSequenceInfo seqInfo = seqs.get(sequence);
      NodeID nodeID = seqInfo.nodeID;
      mclient.renumber(nodeID, seqInfo.srcFileSeq.getFrameRange(), false);
    }*/

    /* link nodes */ 
    seqs = pGetSequences(SequenceState.LINK);
    for(String sequence : seqs.keySet()) {
      pSequenceInfo seqInfo = seqs.get(sequence);
      NodeID nodeID = seqInfo.nodeID;
      log("LINKING :"+nodeID.getName());
      mclient.link(PackageInfo.sUser, pView, pTargetScript, nodeID.getName(), 
		   LinkPolicy.Dependency, LinkRelationship.All, null);
      seqInfo.doFixPath = true;
    }

    /* unlink nodes */      
    for(NodeID nodeID : pUnlinkNodes) 
      mclient.unlink(PackageInfo.sUser, pView, pTargetScript, nodeID.getName());

    /* fix sequence paths in the Nuke script */  
    {
      File script = null;
      try {
	script = File.createTempFile("NukeSourceSyncTool-FixPaths.", ".nk", 
	  PackageInfo.sTempPath.toFile());
	FileCleaner.add(script);

	BufferedReader in = new BufferedReader(new FileReader(pTargetFullName.toFile())); 
	FileWriter out = new FileWriter(script); 

	/* get the list of sequences with paths to be fixed */
	seqs = pGetSequences(SequenceState.FIXPATH);
	
	int lnum = 1;
	boolean isRead     = false;
	boolean isDisabled = false;
	while(true) {
	  String line = in.readLine();
	  if(line == null) 
	    break;

	  /* TODO:  this was cribbed from NukeCompAction.  It doesn't look like very robust,
	   * as it depends on whitespace.  Probably should replace with a regex, or at least
	   * tokenization.  Even better would be to run Nuke in batch mode, and ask it to 
	   * spit out the info for us.
	   */
	  boolean wasWritten = false;
	  if(line.startsWith("Read {"))
	    isRead = true;
	  else if(line.startsWith("}")) {
	    if(isDisabled) 
	      out.write(" disable true\n");
	    isRead     = false;
	    isDisabled = false;            
	  }
	  else if(isRead && line.startsWith(" file ")) {
	    String nukeSeq = line.substring(6);
	    pSequenceInfo seqInfo = seqs.get(nukeSeq);
	    if (seqInfo != null) {
	      NodeID nodeID = seqInfo.nodeID;
	      String newSeq = nukeSeq;
	      if (nodeID != null) {
		String seqName = nukeSeq.substring(nukeSeq.lastIndexOf("/")+1);
		Path newPath = new Path(PackageInfo.sProdPath, 
		  			nodeID.getWorkingParent() + "/" + seqName);
		newSeq = newPath.toString();
	      }
	      if (newSeq.startsWith(pWorkRoot)) {
		newSeq = "WORKING" + newSeq.substring(pWorkRoot.length());
	      }
	      log("REPLACE "+nukeSeq);
	      log("WITH "+newSeq);
	      out.write(" file " + newSeq + "\n");
	      wasWritten = true;
	    } 
	  }

	  if(!wasWritten) 
	    out.write(line + "\n");
	}

	in.close();
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	("Unable to create the temporary Nuke script. " + 
	  "Sequence paths have NOT been updated!  Please make sure all the paths " +
	  "in your script are of the correct WORKING/* format, and point to the " +
	  "working-area versions of the files.");
      }
      
      /* move the temp script over the original
       * TODO:  shouldn't we be doing this through Java, in an OS-independent way?
       */
      ArrayList<String> args = new ArrayList<String>();
      args.add("--force");
      args.add(script.getAbsolutePath());
      String dst = pTargetFullName.toOsString();
      args.add(dst);

      SubProcessLight proc = 
	new SubProcessLight("CopyNukeScript", "mv", args, System.getenv(), 
	  		    PackageInfo.sTempPath.toFile());
      try {	    
	proc.start();
	proc.join();
	if(!proc.wasSuccessful()) 
	  throw new PipelineException
	  	("Unable to edit the Nuke script. (" + dst + ") " +
		  "Sequence paths have NOT been updated!  Please make sure all the paths " +
		  "in your script are of the correct WORKING/* format, and point to the " +	
	  	  "working-area versions of the files.\n\n" +
	  	  proc.getStdErr());
      } catch(InterruptedException ex) {
	throw new PipelineException
	("Interrupted while writing over Nuke script ("+dst+")!");
      }	  

    }
    
    return false;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden(ComponentEvent e) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved(ComponentEvent e) {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   ComponentEvent e
  )
  {
    Box box = (Box) e.getComponent();
    
    Dimension size = box.getComponent(1).getSize();

    JPanel spacer = (JPanel) box.getComponent(0);
    spacer.setMaximumSize(new Dimension(7, size.height));
    spacer.revalidate();
    spacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}


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
    if(cmd.equals("relocate-browse"))
      doRelocateBrowse();
    else if(cmd.startsWith("working-directory-browse:")) 
      doDirectoryBrowse(cmd.substring(25));
    else if(cmd.startsWith("register-changed:")) 
      doRegisterChanged(cmd.substring(17));
    else if(cmd.startsWith("renumber-changed:")) 
      doRenumberChanged(cmd.substring(17));
    else if(cmd.startsWith("check-out-changed:")) 
      doCheckOutChanged(cmd.substring(18));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Browse for an alternative relocate directory path.
   */ 
  private void 
  doRelocateBrowse()  
  {
    pRelocateDialog.setRootDir(new File(pWorkRoot));

    String text = pRelocatePathField.getText();
    if((text != null) && (text.length() > 0)) {
      File path = new File(pWorkRoot, text);
      pRelocateDialog.updateTargetFile(path);
    }

    pRelocateDialog.setVisible(true);
    if(pRelocateDialog.wasConfirmed()) {
      File dir = pRelocateDialog.getSelectedFile();
      if((dir != null) && (dir.isDirectory())) {
	String path = dir.getPath();
	if(path.startsWith(pWorkRoot)) 
	  pRelocatePathField.setText(path.substring(pWorkRoot.length()));
      }
    }
  }

  /**
   * Browse for an alternative working directory path.
   */ 
  private void 
  doDirectoryBrowse
  (
   String sequence
  )  
  {
    pRelocateDialog.setRootDir(new File(pWorkRoot));

    JPathField field = pWorkingPathFields.get(sequence);

    String text = field.getText();
    if((text != null) && (text.length() > 0)) {
      File path = new File(pWorkRoot, text);
      pRelocateDialog.updateTargetFile(path);
    }

    pRelocateDialog.setVisible(true);
    if(pRelocateDialog.wasConfirmed()) {
      File dir = pRelocateDialog.getSelectedFile();
      if((dir != null) && (dir.isDirectory())) {
	String path = dir.getPath();
	if(path.startsWith(pWorkRoot)) 
	  field.setText(path.substring(pWorkRoot.length()));
      }
    }
  }

  /**
   * Update the link field based on the value of the register field.
   */ 
  private void 
  doRegisterChanged
  (
   String name
  )  
  {
    NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);

    JBooleanField rfield = pRegisterFields.get(nodeID);
    JBooleanField lfield = pLinkFields.get(nodeID);
    
    Boolean value = rfield.getValue();
    if((value == null) || !value) {
      lfield.setValue(null);
      lfield.setEnabled(false);
    }
    else {
      lfield.setValue(true);
      lfield.setEnabled(true);
    }
  }

  /**
   * Update the link field based on the value of the renumber field.
   */ 
  private void 
  doRenumberChanged
  (
   String name
  )  
  {
    NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);

    JBooleanField rfield = pRenumberFields.get(nodeID);
    JBooleanField lfield = pLinkFields.get(nodeID);
    
    Boolean value = rfield.getValue();
    if((value == null) || !value) {
      lfield.setValue(null);
      lfield.setEnabled(false);
    }
    else {
      lfield.setValue(true);
      lfield.setEnabled(true);
    }
  }

  /**
   * Update the link field based on the value of the check-out field.
   */ 
  private void 
  doCheckOutChanged
  (
   String name
  )  
  {
    NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);

    JBooleanField rfield = pCheckOutFields.get(nodeID);
    JBooleanField lfield = pLinkFields.get(nodeID);
    JBooleanField ufield = pRenumberFields.get(nodeID);
    
    Boolean value = rfield.getValue();
    if((value == null) || !value) {
      lfield.setValue(null);
      lfield.setEnabled(false);

      if (ufield != null) ufield.setValue(null);
      if (ufield != null) ufield.setEnabled(false);
    }
    else {
      lfield.setValue(true);
      lfield.setEnabled(true);

      if (ufield != null) ufield.setValue(true);
      if (ufield != null) ufield.setEnabled(true);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Turn a nuke sequence specification into a FilePattern
   */
  private FileSeq
  parseNukeSequence(String nukeSeq, FrameRange range)
  throws PipelineException
  {
    String prefix = null;
    String suffix = null;
    String frameSpec = null;
    
    String seq = nukeSeq;
    if(seq.startsWith("WORKING")) {
      seq = pWorkRoot + seq.substring(7);
    }
    String seqName = seq.substring(seq.lastIndexOf("/")+1);

    FileSeq fseq = null;  
    int dot = seqName.indexOf(".");
    
    /* One dot in the filename - try to parse as a single */
    if (dot == seqName.lastIndexOf(".")) {
      dot += seq.length() - seqName.length();
      prefix = seq.substring(0, dot);
      suffix = seq.substring(dot+1);
      if (suffix.matches("[-_a-zA-Z0-9]+"))
	fseq = new FileSeq(prefix, suffix);
      log("SINGLE: "+nukeSeq);
    } 
    
    /* Otherwise, try to parse as a sequence */
    else {
 
      /* a normal sequence. */
      String components[] = seq.split("\\.%0[1-9]+d\\.");
      if (components.length == 2) {
	prefix = components[0];
	suffix = components[1];
	frameSpec = seq.substring(prefix.length()+2, seq.length() - suffix.length() -2);
	Integer padding = new Integer(frameSpec);
	FilePattern fpat = new FilePattern(prefix, padding, suffix);
	/* Nuke default range is (1,1) */
	if (range == null)
	  fseq = new FileSeq(fpat, new FrameRange(1,1,1));
	else 
	  fseq = new FileSeq(fpat, range);
      } 
      /* a single numbered frame (no range specified in Nuke) */
      else {
	components = seq.split("\\.[0-9]+\\.");
	if (components.length == 2) {
	  prefix = components[0];
	  suffix = components[1];
	  frameSpec = seq.substring(prefix.length()+1, seq.length() - suffix.length() -1);
	  Integer frame = new Integer(frameSpec);
	  FilePattern fpat = new FilePattern(prefix, frameSpec.length(), suffix);
	  FrameRange newRange = new FrameRange(frame, frame, 1);
	  fseq = new FileSeq(fpat, newRange);
	  log("SINGLE RANGE: "+fseq.isSingle()+" "+fseq.getFrameRange().getEnd());
	}
      }    
   }
    if (fseq == null)
      log("COULDN'T PARSE");
    return fseq;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6507466134771101920L;
  
  private static final int sTSize  = 150;
  private static final int sVSize  = 400;
  private static final int sVSize2 = 250;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The phase number. 
   */
  private int  pPhase;

  /**
   * The current working area view.
   */ 
  private String  pView; 

  /**
   * The directory prefix path of the current working area view.
   */ 
  private String  pWorkRoot; 

  /**
   * The filename suffixes recognized as images.
   */ 
  private ArrayList<String> pImageFormats;

  /**
   * A private class for collection information about sequences.
   *
   */
  private class pSequenceInfo
  {
    /** the literal sequence spec from the Nuke file (EXECUTE_1)*/
    String nukeSequence;
    
    /** the FileSeq that nukeSequence points to - NOTE: prefix contains a full, absolute
     *  path, which does NOT necessarily conform to Pipeline naming convention.  This is
     *  the "incoming" sequence, and thus might be anywhere, or anything. (EXECUTE_1) */
    FileSeq srcFileSeq;
    
    /** FileSeq from the working version of the Nuke node that owns this sequence.
     *  NOTE:  this is a Pipeline-valid FileSeq, and is useful in ways that srcFileSeq
     *  is not.
     */
    FileSeq registeredFileSeq;
    
    /** names of Nuke Read nodes referencing this sequence (EXECUTE_1)*/
    LinkedList<String> nukeNodeNames;	
    
    /** does the sequence exist on disk? (EXECUTE_1)*/
    boolean isValid;	
    
    /** if not isValid, this is why. (EXECUTE_1) */
    String errorMessage;		

    /** working directory to copy sequence to, if any. (EXECUTE_1, COLLECT_2) */
    File relocateDir;			
    
    /** NodeID of working version. (EXECUTE_1, EXECUTE_2) */
    NodeID nodeID;	
    
    /** NodeMod of working version. (EXECUTE_2) */
    NodeMod workingVersion;		

    /** register a new node for this sequence. (EXECUTE_2, COLLECT_3) */
    boolean doRegister;	
    /** link a (new or existing) node for this sequence. (EXECUTE_2, COLLECT_3) */
    boolean doLink;			
    /** check-out an existing node for this sequence. (EXECUTE_2, COLLECT_3) */
    boolean doCheckout;			
    /** fix the frame range of the node for this sequence. (EXECUTE_2, COLLECT_3) */
    boolean doRenumber;			
    /** fix the Nuke script path for this sequence. (EXECUTE_1, EXECUTE_3) */
    boolean doFixPath;			

    /** constructor */
    public pSequenceInfo(String nukeSeq) 
    {
      nukeSequence = nukeSeq;
      srcFileSeq = null;
      nukeNodeNames = new LinkedList<String>();
      isValid = false;
      errorMessage = null;
      relocateDir = null;
      nodeID = null;
      workingVersion = null;
      doRegister = false;
      doLink = false;
      doCheckout = false;
      doRenumber = false;
      doFixPath = false;
    }

    /** (convenience function) 
     *  returns the prefix of the name of the srcFileSeq (so no full path) 
     */    
    public String getSrcFilePrefix()
    { 
      return (new File(srcFileSeq.getFilePattern().getPrefix())).getName();
    }
    
    /** (convenience function) 
     *  returns the suffix of the name of the srcFileSeq (so no full path) 
     */    
    public String getSrcFileSuffix()
    { 
        return srcFileSeq.getFilePattern().getSuffix();
    }
  }
 
  /**
   * Collection of all info for all sequences 
   */
  private TreeMap<String,pSequenceInfo> pSequences;
 
  /**
   * Get sequence infos based on NodeID
   */
  private ArrayList<pSequenceInfo>
  pGetSequences(NodeID nodeID)
  {
    ArrayList<pSequenceInfo> seqs = new ArrayList<pSequenceInfo>();
    for (String seq : pSequences.keySet()) {
      pSequenceInfo seqInfo = pSequences.get(seq);
      if (seqInfo.nodeID != null && seqInfo.nodeID.equals(nodeID))
	seqs.add(seqInfo);
    }
    return seqs;
  }

  /**
   * Method for filtering pSequences based on a criteria
   */
  private enum SequenceState { VALID, INVALID, REGISTER, CHECKOUT, LINK, RENUMBER, FIXPATH };
  private TreeMap<String,pSequenceInfo>
  pGetSequences(SequenceState state)
  {
    TreeMap<String,pSequenceInfo> seqs = new TreeMap<String,pSequenceInfo>();
    for (String seq : pSequences.keySet()) {
      pSequenceInfo info = pSequences.get(seq);
      switch (state) {
      case VALID:
	if (info.isValid) seqs.put(seq, info);
	break;
      case INVALID:
	if (!info.isValid) seqs.put(seq, info);
	break;
      case REGISTER:
	if (info.doRegister) seqs.put(seq, info);
	break;
      case CHECKOUT:
	if (info.doCheckout) seqs.put(seq, info);
	break;
      case LINK:
	if (info.doLink) seqs.put(seq, info);
	break;
      case FIXPATH:
	if (info.doFixPath) seqs.put(seq, info);
	break;
      }
    }
    return seqs;
  }

  
  
  /*-- FIRST PHASE: UI ---------------------------------------------------------------------*/

  /**
   * The node associated with the Maya scene being checked.
   */ 
  private String      pTargetScript;
  private JPathField  pTargetScriptField; 

  /**
   * The fully resolved working area destination of relocated sequences.
   */
  private String             pRelocatePath; 
  private JPathField         pRelocatePathField;
  private JFileSelectDialog  pRelocateDialog;



  /*-- FIRST PHASE: EXECUTE ----------------------------------------------------------------*/

  /**
   * Node information for the Target Nuke Script Node.
   */ 
  private NodeID      pTargetScriptID;
  private NodeStatus  pTargetScriptStatus;
  private NodeMod     pTargetScriptMod;
  private File        pTargetScriptFile; 
  private String      pTargetFileType; 
  private Path 	      pTargetFullName;

  

  /*-- SECOND PHASE: UI --------------------------------------------------------------------*/

  /**
   * The working directory fields indexed by sequence.
   */ 
  private TreeMap<String,JPathField> pWorkingPathFields; 



  /*-- SECOND PHASE: EXECUTE --------------------------------------------------------------*/
 
  /**
   * The unique working area IDs of the nodes which need to be unlinked from the 
   * target link node (pTarge).
   */ 
  private TreeSet<NodeID>  pUnlinkNodes; 

  /**
   * Error messages for the Pipeline nodes which could not be validated indexed 
   * by the names of the nodes.
   */
  private TreeMap<String,String>  pInvalidNodes;



  /*-- THIRD PHASE: UI --------------------------------------------------------------------*/

  /**
   * The register flags indexed by the unique working area IDs of the node.
   */ 
  private TreeMap<NodeID,JBooleanField>  pRegisterFields; 

  /**
   * The renumber flags indexed by the unique working area IDs of the node.
   */ 
  private TreeMap<NodeID,JBooleanField>  pRenumberFields; 

  /**
   * The check-out flags indexed by the unique working area IDs of the node.
   */ 
  private TreeMap<NodeID,JBooleanField>  pCheckOutFields; 

  /**
   * The link flags indexed by the unique working area IDs of the node.
   */ 
  private TreeMap<NodeID,JBooleanField>  pLinkFields; 
  
  /**
   * The unlink flags indexed by the unique working area IDs of the node.
   */ 
  private TreeMap<NodeID,JBooleanField>  pUnlinkFields;
  
  
  
  /*-- Logging and Error Checking File ----------------------------------------------------*/
  private PrintWriter tempOutput;
  
  void log(String s)
  {
    if (tempOutput != null) {
      tempOutput.println(s);
      tempOutput.flush();
    }
  }

}

