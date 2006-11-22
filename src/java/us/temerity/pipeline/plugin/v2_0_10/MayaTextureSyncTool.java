// $Id: MayaTextureSyncTool.java,v 1.4 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.plugin.v2_0_10;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*; 

/*------------------------------------------------------------------------------------------*/
/*   M A Y A    T E X T U R E   T O O L                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * Synchronizes the texture files referenced by a Maya scene with the texture nodes
 * upstream of the target Maya scene node.
 */
public
class MayaTextureSyncTool
  extends BaseTool
  implements ComponentListener, ActionListener
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaTextureSyncTool()
  {
    super("MayaTextureSync", new VersionID("2.0.10"), "Temerity",
	  "Synchronizes the texture files referenced by a Maya scene with Pipeline.");

    pPhase = 1; 

    pTextureFormats = new ArrayList<String>(); 
    pTextureFormats.add("bmp");
    pTextureFormats.add("iff");
    pTextureFormats.add("gif");
    pTextureFormats.add("hdr");
    pTextureFormats.add("jpg");
    pTextureFormats.add("jpeg");
    pTextureFormats.add("map");
    pTextureFormats.add("png");
    pTextureFormats.add("ppm");
    pTextureFormats.add("psd");
    pTextureFormats.add("rgb");
    pTextureFormats.add("sgi");
    pTextureFormats.add("bw");
    pTextureFormats.add("tga");
    pTextureFormats.add("tif");
    pTextureFormats.add("tiff");

    pTextureFiles   = new TreeMap<File,File>();
    pFileShaders    = new TreeMap<File,TreeSet<String>> ();
    pInvalidShaders = new TreeMap<String,String>();     

    pWorkingPathFields = new TreeMap<File,JPathField>();

    pNodeTextures  = new TreeMap<NodeID,TreeSet<String>>();
    pCheckOutNodes = new TreeSet<NodeID>();      
    pRegisterNodes = new TreeSet<NodeID>();      
    pRenumberNodes = new TreeMap<NodeID,FrameRange>();      
    pLinkNodes     = new TreeSet<NodeID>();       
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
      
      pTargetSceneField = 
	UIFactory.createTitledPathField
	(tpanel, "Target Scene Node:", sTSize, 
	 vpanel, null, sVSize, 
	 "The node associated with the Maya scene being checked.");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      pTargetLinkField = 
	UIFactory.createTitledPathField
	(tpanel, "Target Link Node:", sTSize, 
	 vpanel, null, sVSize, 
	 "The node on the downstream side of all linked texture nodes being checked.");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
      
      {
	JComponent rcomps[] = 
	  UIFactory.createTitledBrowsablePathField
	  (tpanel, "Texture Directory:", sTSize, 
	   vpanel, null, sVSize, 
	   this, "relocate-browse", 
	   "The fully resolved working area destination directory to copy texture files " + 
	   "which are currently outside the working area.");
	
	pRelocatePathField  = (JPathField) rcomps[0];	   
	pRelocatePathButton = (JButton) rcomps[1];
      }

      UIFactory.addVerticalGlue(tpanel, vpanel);
    }
    
    /* validate node selections and initialize dialog components */ 
    {
      if(pPrimary == null)
	throw new PipelineException
	  ("The primary selection must be the Target Scene Node!");
      pTargetSceneField.setText(pPrimary);
      
      {
	NodeStatus status = pSelected.get(pPrimary);
	NodeID nodeID = status.getNodeID();
	if(!nodeID.getAuthor().equals(PackageInfo.sUser)) 
	  throw new PipelineException 
	    ("Only nodes owned by the current user (" + PackageInfo.sUser + ") can " +
	     "be used as the target of the Maya Texture Sync tool!");
	
	pView = nodeID.getView(); 
      }
      
      {
	Path wpath = new Path(PackageInfo.sWorkPath, PackageInfo.sUser + "/" + pView);
	pWorkRoot = wpath.toOsString();
      }
      
      if(pSelected.size() == 1) {
	pTargetLinkField.setText(pPrimary);
	
	File path = new File(pPrimary);
	pRelocatePathField.setText(path.getParent());
      }
      else if(pSelected.size() == 2) {
	for(String name : pSelected.keySet()) {
	  if(!name.equals(pPrimary)) {
	    pTargetLinkField.setText(name);
	    
	    File path = new File(name);
	    pRelocatePathField.setText(path.getParent());
	  }
	}
      }
      else {
	throw new PipelineException
	  ("Only one Target Link Node may be selected as the secondary selection!");
      }
    }
    
    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Maya Texture Sync: Target Nodes", body, "Continue");
    
    pRelocateDialog = 
      new JFileSelectDialog(diag, "Select Directory", 
			    "Select Texture Directory:", "Select");
    
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      pTargetScene = pTargetSceneField.getText();
      if((pTargetScene == null) || (pTargetScene.length() == 0))
	throw new PipelineException("Illegal Target Scene Node name!");
      
      pTargetLink = pTargetLinkField.getText();
      if((pTargetLink == null) || (pTargetLink.length() == 0))
	throw new PipelineException("Illegal Target Link Node name!");
      
      pRelocatePath = pRelocatePathField.getText();
      if((pRelocatePath == null) || (pRelocatePath.length() == 0))
	throw new PipelineException("Illegal Relocate Path name!");
	
      return ": Collecting Texture Information...";
    }
    else {
      return null;
    }	
  }

  /**
   * Presents the results of the texture validation and allows changes to the working
   * area locations of texture images.
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

      /* invalid maya file shaders */ 
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

	  if(pInvalidShaders.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(String shader : pInvalidShaders.keySet()) {
	      JPanel panel = new JPanel();
	      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	      panel.add(Box.createRigidArea(new Dimension(0, 4)));

	      {
		Box mbox = new Box(BoxLayout.X_AXIS);
		
		mbox.add(Box.createRigidArea(new Dimension(4, 0)));
		
		{
		  JTextArea area = new JTextArea(pInvalidShaders.get(shader), 0, 45);
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

	      JDrawer drawer = new JDrawer("Shader: " + shader, panel, true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Invalid Maya File Shaders:", hbox, false);
	vbox.add(drawer);
      }

      /* textures */ 
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

	  if(pTextureFiles.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(File original : pTextureFiles.keySet()) {
	      File working = pTextureFiles.get(original);

	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];

	      {
		JComponent rcomps[] = 
		  UIFactory.createTitledBrowsablePathField
		  (tpanel, "Working Directory:", sTSize-7, 
		   vpanel, working.getParent(), sVSize, 
		   this, "texture-browse:" + original, 
		   "The fully resolved working area directory where the texture file " + 
		   "should be located.");
		
		pWorkingPathFields.put(original, (JPathField) rcomps[0]);
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	      
	      {
		{
		  JLabel label = 
		    UIFactory.createFixedLabel
		    ("Maya Shaders:", sTSize-7, JLabel.RIGHT, 
		     "The names of the Maya shaders which use the texture file!");
		  tpanel.add(label);
		}

		tpanel.add(Box.createVerticalGlue());
	      
		boolean first = true;
		for(String shader : pFileShaders.get(original)) {
		  if(!first) 
		    vpanel.add(Box.createRigidArea(new Dimension(0, 3)));
		  first = false;

		  vpanel.add(UIFactory.createTextField(shader, sVSize, JLabel.CENTER));
		}
	      }

	      JDrawer drawer = 
		new JDrawer("Texture: " + original, (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Referenced Textures:", hbox, true);
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
      new JToolDialog("Maya Texture Sync: Texture Changes", scroll, "Continue");
    
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      for(File original : pWorkingPathFields.keySet()) {
	JPathField field = pWorkingPathFields.get(original);

	String path = field.getText();
	if((path != null) && (path.length() > 0)) {
	  Path tpath = new Path(PackageInfo.sWorkPath, 
				PackageInfo.sUser + "/" + pView + path);
	  if(!tpath.toFile().isDirectory()) 
	    throw new PipelineException
	      ("The Working Directory (" + path + ") specified for Texture " + 
	       "(" + original + ") does not exist!");

	  pTextureFiles.put(original, new File(path, original.getName()));
	}
	else {
	  throw new PipelineException
	    ("No Working Directory was specified for Texture (" + original + ")!");
	}
      }
	
      return ": Collecting Node Information...";
    }
    else {
      return null;
    }	
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

      /* nodes */ 
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

	  if(pNodeTextures.isEmpty() && pUnlinkNodes.isEmpty()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	    vpanel.add(Box.createHorizontalGlue());

	    ibox.add(comps[2]);
	  }
	  else {
	    for(NodeID nodeID : pNodeTextures.keySet()) {
	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      {
		{
		  JLabel label = 
		    UIFactory.createFixedLabel
		    ("Textures:", sTSize-7, JLabel.RIGHT, 
		     "The name of the texture file associated with the Pipeline node.");
		  tpanel.add(label);
		}
		
		tpanel.add(Box.createVerticalGlue());
		
		boolean first = true;
		for(String texture : pNodeTextures.get(nodeID)) {
		  if(!first) 
		    vpanel.add(Box.createRigidArea(new Dimension(0, 3)));
		  first = false;
		  
		  vpanel.add(UIFactory.createTextField(texture, sVSize2, JLabel.CENTER));
		}
	      }
	      
	      boolean registerNode = pRegisterNodes.contains(nodeID);
	      boolean renumberNode = pRenumberNodes.containsKey(nodeID);
	      boolean checkOutNode = pCheckOutNodes.contains(nodeID);
	      boolean linkNode     = pLinkNodes.contains(nodeID);
	      if(registerNode || renumberNode || checkOutNode || linkNode) {
		UIFactory.addVerticalSpacer(tpanel, vpanel, 9);
		
		if(registerNode) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Register Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to register a new node for the texture.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("register-changed:" + nodeID.getName());
		  
		  pRegisterFields.put(nodeID, field);
		}

		if(renumberNode) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Renumber Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to change the texture file sequences associated " +
		     "with the node node.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("renumber-changed:" + nodeID.getName());
		  
		  pRenumberFields.put(nodeID, field);
		}
		
		if(checkOutNode) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "CheckOut Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to check-out the latest version of the node associated with " + 
		     "the texture.");
		  field.setValue(true);
		  
		  field.addActionListener(this);
		  field.setActionCommand("check-out-changed:" + nodeID.getName());
		  
		  pCheckOutFields.put(nodeID, field);
		}
		
		if(linkNode) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JBooleanField field = 
		    UIFactory.createTitledBooleanField
		    (tpanel, "Link Node:", sTSize-7, 
		     vpanel, sVSize2, 
		     "Whether to link the node associated with the texture to the Target " + 
		     "Link Node.");
		  field.setValue(true);
		  
		  pLinkFields.put(nodeID, field);
		}
	      }
	      
	      JDrawer drawer = 
		new JDrawer("Node: " + nodeID.getName(), (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }
	    
	    for(NodeID nodeID : pUnlinkNodes) {
	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	
	      JBooleanField field = 
		UIFactory.createTitledBooleanField
		(tpanel, "Unlink Node:", sTSize-7, 
		 vpanel, sVSize2, 
		 "Whether to unlink the node not associated with any texture referenced " + 
		 "by the Maya scene from the Target Link Node.");
	      field.setValue(true);
	      
	      pUnlinkFields.put(nodeID, field);
	      
	      JDrawer drawer = 
		new JDrawer("Node: " + nodeID.getName(), (JComponent) comps[2], true);
	      ibox.add(drawer);
	    }
	  }

	  hbox.add(ibox);
	}

	JDrawer drawer = new JDrawer("Pipeline Nodes:", hbox, true);
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
      new JToolDialog("Maya Texture Sync: Node Changes", scroll, "Confirm");
    
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      for(NodeID nodeID : pRegisterFields.keySet()) {
	JBooleanField field = pRegisterFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pRegisterNodes.remove(nodeID);
      }

      for(NodeID nodeID : pCheckOutFields.keySet()) {
	JBooleanField field = pCheckOutFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pCheckOutNodes.remove(nodeID);
      }

      for(NodeID nodeID : pRenumberFields.keySet()) {
	JBooleanField field = pRenumberFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pRenumberNodes.remove(nodeID);
      }

      for(NodeID nodeID : pLinkFields.keySet()) {
	JBooleanField field = pLinkFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pLinkNodes.remove(nodeID);
      }

      for(NodeID nodeID : pUnlinkFields.keySet()) {
	JBooleanField field = pUnlinkFields.get(nodeID);
	Boolean value = field.getValue();
	if((value == null) || !value) 
	  pUnlinkNodes.remove(nodeID);
      }
	
      return ": Modifying Nodes...";
    }
    else {
      return null;
    }	
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
   * Collect texture information from the Maya scene.
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
    /* get the current status of the Maya scene node */ 
    {
      pTargetSceneID = new NodeID(PackageInfo.sUser, pView, pTargetScene);
      pTargetSceneStatus = mclient.status(pTargetSceneID);
      pTargetSceneMod = pTargetSceneStatus.getDetails().getWorkingVersion();
      if(pTargetSceneMod == null) 
	throw new PipelineException
	  ("No working version of the Target Scene Node (" + pTargetScene + ") exists " + 
	   "in the (" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");

      /* verify that it is a Maya scene */
      {
	FileSeq fseq = pTargetSceneMod.getPrimarySequence();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || (!suffix.equals("ma") && !suffix.equals("mb"))) 
	  throw new PipelineException
	    ("The Target Scene Node (" + pTargetScene + ") must have a Maya Scene as " + 
	     "its primary file sequence!");
	pTargetSceneFile = fseq.getFile(0);
	pTargetFileType = (suffix.equals("ma") ? "mayaAscii" : "mayaBinary");
      }
    }
    
    /* get the current status of the node downstream of the texture node links */ 
    {
      pTargetLinkID = new NodeID(PackageInfo.sUser, pView, pTargetLink);
      pTargetLinkStatus = findNodeStatus(pTargetLinkID, pTargetSceneStatus);
      if(pTargetLinkStatus == null) 
	throw new PipelineException
	  ("The Target Link Node (" + pTargetLink + ") was not a member of the tree " +
	   "of nodes upstream of the Target Scene Node (" + pTargetScene + ") in the " + 
	   "(" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");

      pTargetLinkMod = pTargetLinkStatus.getDetails().getWorkingVersion();
      if(pTargetLinkMod == null) 
	throw new PipelineException
	("No working version of the Target Link Node (" + pTargetLink + ") exists " + 
	 "in the (" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");
    }

    /* collect texture file information from Maya scene */ 
    TreeMap<String,String> fileInfo = new TreeMap<String,String>();
    {  
      File script = null;
      try {
	script = File.createTempFile("MayaTextureSyncTool-GetFileInfo.", ".mel", 
				     PackageInfo.sTempPath.toFile());
	FileCleaner.add(script);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to create the temporary MEL script used to collect " + 
	   "texture information from the Maya scene!");
      }
      
      File info = null;
      try {
	info = File.createTempFile("MayaTextureSyncTool-FileInfo.", ".txt", 
				   PackageInfo.sTempPath.toFile());
	FileCleaner.add(info);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to create the temporary text file used to store the texture " +
	   "information collected from the Maya scene!");
      }
      
      try {
	FileWriter out = new FileWriter(script);
	out.write
	  ("$out = `fopen \"" + info + "\" \"w\"`;\n" + 
	   "string $fileNodes[] = `ls -type file`;\n" + 
	   "for($node in $fileNodes) {\n" + 
	   "  string $tex = `getAttr ($node + \".fileTextureName\")`;\n" + 
	   "  fprint $out (\"Shader=\" + $node + \" Texture=\" + $tex + \"\\n\");\n" + 
	   "}\n" + 
	   "fclose $out;\n");
	
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary MEL script (" + script + ") used to collect " + 
	   "texture information from the Maya scene!");
      }
      
      /* run Maya to collect the information */ 
      try {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-batch");
	args.add("-script");
	args.add(script.getPath());
	args.add("-file");
	args.add(pTargetSceneFile.getPath());
	
	TreeMap<String,String> env = 
	  mclient.getToolsetEnvironment
	  (pTargetSceneID.getAuthor(), pTargetSceneID.getView(), 
	   pTargetSceneMod.getToolset(), PackageInfo.sOsType);
	
	Path wpath = new Path(PackageInfo.sProdPath, pTargetSceneID.getWorkingParent());

	/* added custom Mental Ray shader path to the environment */ 
	Map<String, String> nenv = env;
	String midefs = env.get("PIPELINE_MI_SHADER_PATH");
	if(midefs != null) {
	    nenv = new TreeMap<String, String>(env);
	    Path dpath = new Path(wpath, midefs);
	    nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
	}

	SubProcessLight proc = 
	  new SubProcessLight("MayaTextureSync-FileInfo", "maya", args, nenv, wpath.toFile());
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) {
	    throw new PipelineException
	      ("Failed to collect the texture information due to a Maya failure!\n\n" +
	       proc.getStdOut() + "\n\n" + 
	       proc.getStdErr());
	  }
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
      }
      catch(Exception ex) {
	throw new PipelineException(ex);
      }
      
      /* read the collected information in from the texture info file written by Maya */ 
      {
	StringBuilder buf = new StringBuilder();
	try {
	  FileReader in = new FileReader(info);
	  char cs[] = new char[4096];
	  while(true) {
	    int n = in.read(cs);
	    if(n == -1) 
	      break;
	    buf.append(cs, 0, n);
	  }
	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException 
	    ("Unable to read the collected texture information from (" + info + ")!");
	}
	
	String lines[] = buf.toString().split("\n");
	int lk;
	for(lk=0; lk<lines.length; lk++) {
	  if(lines[lk].length() > 0) {
	    String fields[] = lines[lk].split(" ");
	    if((fields.length != 2) || 
	       !fields[0].startsWith("Shader=") || !fields[1].startsWith("Texture=")) 
	      throw new PipelineException
		("The the texture information file (" + info + ") was unreadable!");
	    
	    String shader  = fields[0].substring(7);
	    String texture = fields[1].substring(8);
	    fileInfo.put(shader, texture);
	  }
	}
      }
    }

    /* validate the texture files */ 
    {
      for(String shader : fileInfo.keySet()) {
	String tex = fileInfo.get(shader);

	try {
	  if(tex.length() == 0) 
	    throw new PipelineException
	      ("No texture image was specified!");

	  String texture = tex;
	  if(tex.startsWith("$WORKING")) 
	    texture = (pWorkRoot + texture.substring(8));
	  
	  File file = null;
	  if(!texture.startsWith("/")) {
	    Path tpath = new Path(PackageInfo.sProdPath, 
				  pTargetSceneID.getWorkingParent() + "/" + texture);
	    file = tpath.toFile();
	  }
	  else {
	    file = new File(texture);
	  }

	  if(!file.isFile()) {
	    boolean hasOwningNode = false;
	    if(file.getPath().startsWith(pWorkRoot)) {
	      String path = file.getPath().substring(pWorkRoot.length());
	      String name = mclient.getNodeOwning(path);
	      if(name != null) {
		try {
		  NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);
		  mclient.status(nodeID);
		  hasOwningNode = true;
		}
		catch(PipelineException ex) {
		}
	      }
	    }

	    if(!hasOwningNode)
	      throw new PipelineException 
		("The texture image does not exist:\n\n" + tex);
	  }

	  File canon = null;
	  try {
	    canon = file.getCanonicalFile();
	  }
	  catch(IOException ex) {
	    throw new PipelineException 
	      ("Unable to determine the canonical path to texture image:\n\n" + 
	       tex);
	  }
	    
	  String prefix = null;
	  String suffix = null;
	  {
	    String parts[] = canon.getName().split("\\.");
	    switch(parts.length) {
	    case 2:
	    case 3:
	      prefix = parts[0];
	      suffix = parts[parts.length-1];
	      break;
	    
	    default:
	      throw new PipelineException 
		("The texture image filename did not conform to the " +
		 "(texture-name[.frame].suffix) file naming convention:\n\n" + 
		 tex);
	    }
	  }
	   
	  if((suffix == null) || !pTextureFormats.contains(suffix)) 
	    throw new PipelineException 
	      ("The texture image filename did not have a supported image file suffix:\n\n" + 
	       tex);

	  TreeSet<String> shaders = pFileShaders.get(canon);
	  if(shaders == null) {
	    shaders = new TreeSet<String>();
	    pFileShaders.put(canon, shaders);
	  }
	  shaders.add(shader);
	}
	catch(PipelineException ex) {
	  pInvalidShaders.put(shader, ex.getMessage());
	}
      }
    
      /* map the original texture path to working area paths */
      for(File file : pFileShaders.keySet()) {
	String path = file.getPath();
	if(path.startsWith(pWorkRoot)) 
	  pTextureFiles.put(file, new File(path.substring(pWorkRoot.length())));
	else 
	  pTextureFiles.put(file, new File(pRelocatePath + "/" + file.getName()));
      }
    }

    pPhase++; 
    return true;
  }
  
  /**
   * Collect information about the nodes associated with the referenced textures.
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
    /* determine which Pipeline nodes are associated with the textures */ 
    TreeMap<NodeID,TreeSet<String>> nodeTextures = new TreeMap<NodeID,TreeSet<String>>();
    for(File canon : pTextureFiles.keySet()) {
      File texture = pTextureFiles.get(canon);
      
      NodeID nodeID = null;
      {
	String prefix = null;
	{
	  String parts[] = texture.getName().split("\\.");
	  switch(parts.length) {
	  case 2:
	  case 3:
	    prefix = parts[0];
	    break;
	    
	  default:
	    throw new PipelineException 
	      ("The texture image filename did not conform to the " +
	       "(texture-name[.frame].suffix) file naming convention:\n\n" + 
	       texture);
	  }
	}

	String name = mclient.getNodeOwning(texture.getPath());
	if(name == null) {
	  nodeID = new NodeID(PackageInfo.sUser, pView, texture.getParent() + "/" + prefix);

	  TreeSet<String> textures = nodeTextures.get(nodeID);
	  if(textures == null) {
	    textures = new TreeSet<String>();
	    nodeTextures.put(nodeID, textures);
	  }
	  textures.add(texture.getName());
	}
	else {
	  nodeID = new NodeID(PackageInfo.sUser, pView, name);

	  TreeSet<String> textures = pNodeTextures.get(nodeID);
	  if(textures == null) {
	    textures = new TreeSet<String>();
	    pNodeTextures.put(nodeID, textures);
	  }
	  textures.add(texture.getName());
	}
      }
    }

    /* check whether textures not associated with existing nodes can be registered */ 
    for(NodeID nodeID : nodeTextures.keySet()) {
      TreeSet<String> textures = nodeTextures.get(nodeID);

      TreeSet<File> files = new TreeSet<File>(); 
      for(String texture : textures) 
	files.add(new File(texture));

      try {
	TreeSet<FileSeq> fseqs = FileSeq.collate(files, false);
	if(fseqs.size() != 1) {
	  StringBuilder buf = new StringBuilder();
	  buf.append
	    ("Unable to register node for the following texture files because they cannot " + 
	     "be members of the same file sequence:\n\n");
	  for(File file : files) 
	    buf.append("  " + file + "\n");
	  throw new PipelineException(buf.toString()); 
	}
	
	pNodeTextures.put(nodeID, textures);
      }
      catch(PipelineException ex) {
	pInvalidNodes.put(nodeID.getName(), ex.getMessage());
      }
    }

    /* determine which node operations need to be performed */ 
    for(NodeID nodeID : pNodeTextures.keySet()) {
      try {
	NodeStatus status = mclient.status(nodeID); 
	NodeDetails details = status.getDetails(); 
	NodeCommon com = null;
	if(details.getOverallNodeState() == OverallNodeState.CheckedIn) {
	  pCheckOutNodes.add(nodeID);
	  com = details.getLatestVersion();
	  pLinkNodes.add(nodeID);
	}
	else {
	  if(!pTargetLinkMod.getSourceNames().contains(nodeID.getName())) 
	    pLinkNodes.add(nodeID);
	  com = details.getWorkingVersion();
	}

	{
	  TreeSet<String> textures = pNodeTextures.get(nodeID);

	  TreeSet<File> files = new TreeSet<File>(); 
	  for(String texture : textures) 
	    files.add(new File(texture));

	  TreeSet<FileSeq> tfseqs = FileSeq.collate(files, false);
	  if(tfseqs.size() != 1)
	    throw new IllegalStateException(); 
	  FileSeq tfseq = tfseqs.first();
	  
	  for(FileSeq fseq : com.getSequences()) {
	    if(tfseq.similarTo(fseq)) {
	      files.addAll(fseq.getFiles());
	      
	      TreeSet<FileSeq> afseqs = FileSeq.collate(files, false);
	      if(afseqs.size() != 1)
		throw new IllegalStateException(); 
	      FileSeq afseq = afseqs.first();

	      if(!afseq.equals(fseq)) 
		pRenumberNodes.put(nodeID, afseq.getFrameRange());
	      
	      break;
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	pRegisterNodes.add(nodeID);
	pLinkNodes.add(nodeID);
      }
    }

    for(String name : pTargetLinkMod.getSourceNames()) {
      NodeStatus status = pTargetLinkStatus.getSource(name);
      NodeDetails details = status.getDetails();
      NodeMod mod = details.getWorkingVersion();
      
      FileSeq fseq = mod.getPrimarySequence();
      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix != null) && pTextureFormats.contains(suffix)) {
	NodeID nodeID = new NodeID(PackageInfo.sUser, pView, name);
	if(!pNodeTextures.containsKey(nodeID)) 
	  pUnlinkNodes.add(nodeID); 
      }
    }
    
    pPhase++; 
    return true;
  }

  /**
   * Modify nodes, copy texture files and fix the texture paths in the target Maya scene.
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
    /* register nodes */ 
    {
      String toolset = mclient.getDefaultToolsetName();
      for(NodeID nodeID : pRegisterNodes) {
	
	TreeSet<File> files = new TreeSet<File>(); 
	for(String texture : pNodeTextures.get(nodeID)) 
	  files.add(new File(texture));

	FileSeq primary = null;
	{
	  TreeSet<FileSeq> fseqs = FileSeq.collate(files, false);
	  if(fseqs.size() != 1) {
	    StringBuilder buf = new StringBuilder();
	    buf.append
	      ("Unable to register node (" + nodeID.getName() + ") for the following " +
	       "texture files because they cannot be members of the same file sequence:\n\n");
	    for(File file : files) 
	      buf.append("  " + file + "\n");
	    throw new PipelineException(buf.toString()); 
	  }

	  primary = fseqs.first();
	}
	
	BaseEditor editor = mclient.getEditorForSuffix(primary.getFilePattern().getSuffix());
	NodeMod mod = new NodeMod(nodeID.getName(), primary, null, toolset, editor);
 	mclient.register(PackageInfo.sUser, pView, mod);
      }
    }

    /* check-out nodes */ 
    for(NodeID nodeID : pCheckOutNodes) 
      mclient.checkOut(nodeID, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);

    /* renumber nodes */ 
    for(NodeID nodeID : pRenumberNodes.keySet()) 
      mclient.renumber(nodeID, pRenumberNodes.get(nodeID), false);

    /* link nodes */ 
    for(NodeID nodeID : pLinkNodes) 
      mclient.link(PackageInfo.sUser, pView, pTargetLink, nodeID.getName(), 
		   LinkPolicy.Dependency, LinkRelationship.All, null);

    /* unlink nodes */      
    for(NodeID nodeID : pUnlinkNodes) 
      mclient.unlink(PackageInfo.sUser, pView, pTargetLink, nodeID.getName());

    /* copy textures */ 
    {
      Map<String,String> env = System.getenv();
      for(File canon: pTextureFiles.keySet()) {
	File texture = new File(pWorkRoot + pTextureFiles.get(canon).getPath());
	if(!canon.equals(texture)) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  args.add(canon.getPath());
	  args.add(texture.getPath());
	  
	  SubProcessLight proc = 
	    new SubProcessLight("CopyTextures", "cp", 
				args, env, PackageInfo.sTempPath.toFile());
	  try {	    
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copying the texture file (" + canon + ") to the " + 
		 "working area location (" + texture + "):\n\n" + 
		 proc.getStdErr());
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying texture files!");
	  }
	}
      }      
    }

    /* fix texture paths in the Maya scene */  
    {
      File script = null;
      try {
	script = File.createTempFile("MayaTextureSyncTool-FixPaths.", ".mel", 
				     PackageInfo.sTempPath.toFile());
	FileCleaner.add(script);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to create the temporary MEL script used to fix the " + 
	   "texture filename paths in the Maya scene!");
      }
      
      try {
	FileWriter out = new FileWriter(script);
	
	out.write("file -rename \"" + pTargetSceneFile + "\";\n" + 
		  "file -type \"" + pTargetFileType + "\";\n\n");

	for(File canon : pFileShaders.keySet()) {
	  String value = ("$WORKING" + pTextureFiles.get(canon));
	  for(String shader : pFileShaders.get(canon)) {
	    out.write("setAttr -type \"string\" " + shader + ".fileTextureName " + 
		      "\"" + value + "\";\n");
	  }

	  out.write("file -save;\n");
	}
	  
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary MEL script (" + script + ") used to fix the " + 
	   "texture filename paths in the Maya scene!");
      }
      
      /* run Maya to fix the texture paths and save the scene */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-batch");
	args.add("-script");
	args.add(script.getPath());
	args.add("-file");
	args.add(pTargetSceneFile.getPath());
	
	TreeMap<String,String> env = 
	  mclient.getToolsetEnvironment
	  (PackageInfo.sUser, pView, pTargetSceneMod.getToolset(), PackageInfo.sOsType);	

	Path wpath = new Path(PackageInfo.sProdPath, pTargetSceneID.getWorkingParent());

	/* added custom Mental Ray shader path to the environment */ 
	Map<String, String> nenv = env;
	String midefs = env.get("PIPELINE_MI_SHADER_PATH");
	if(midefs != null) {
	    nenv = new TreeMap<String, String>(env);
	    Path dpath = new Path(wpath, midefs);
	    nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
	}

	SubProcessLight proc = 
	  new SubProcessLight("MayaTextureSync-FixPaths", "maya", args, env, wpath.toFile());
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) {
	    throw new PipelineException
	      ("Failed to fix the texture filename paths due to a Maya failure!\n\n" +
	       proc.getStdOut() + "\n\n" + 
	       proc.getStdErr());
	    }
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
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
    else if(cmd.startsWith("texture-browse:")) 
      doTextureBrowse(cmd.substring(15));
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
   * Browse for an alternative per-texture relocate directory path.
   */ 
  private void 
  doTextureBrowse
  (
   String texture
  )  
  {
    pRelocateDialog.setRootDir(new File(pWorkRoot));

    File key = new File(texture);
    JPathField field = pWorkingPathFields.get(key);

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

      ufield.setValue(null);
      ufield.setEnabled(false);
    }
    else {
      lfield.setValue(true);
      lfield.setEnabled(true);

      ufield.setValue(true);
      ufield.setEnabled(true);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively search the upstream links for the status of the given node.
   * 
   * @return
   *    The status or <CODE>null</CODE> if unable to find the node.
   */ 
  private NodeStatus 
  findNodeStatus
  (
   NodeID nodeID,
   NodeStatus status
  ) 
  {
    if(status.getNodeID().equals(nodeID)) 
      return status;
    
    for(NodeStatus lstatus : status.getSources()) {
      NodeStatus found = findNodeStatus(nodeID, lstatus);
      if(found != null) 
	return found;
    }

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1938310428741290205L;

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
   * The filename suffixes recognized as textures.
   */ 
  private ArrayList<String> pTextureFormats;



  /*-- FIRST PHASE: UI ---------------------------------------------------------------------*/

  /**
   * The node associated with the Maya scene being checked.
   */ 
  private JPathField  pTargetSceneField; 
  private String      pTargetScene;

  /**
   * The node on the downstream side of all linked texture nodes being checked.
   */ 
  private JPathField  pTargetLinkField; 
  private String      pTargetLink; 

  /**
   * The fully resolved working area destination of relocated texture files.
   */
  private JPathField         pRelocatePathField;
  private JButton            pRelocatePathButton;
  private JFileSelectDialog  pRelocateDialog;
  private String             pRelocatePath; 



  /*-- FIRST PHASE: EXECUTE ----------------------------------------------------------------*/

  /**
   * Node information for the Target Scene Node.
   */ 
  private NodeID      pTargetSceneID;
  private NodeStatus  pTargetSceneStatus;
  private NodeMod     pTargetSceneMod;
  private File        pTargetSceneFile; 
  private String      pTargetFileType; 
  
  /**
   * Node information for the Target Link Node.
   */ 
  private NodeID      pTargetLinkID;
  private NodeStatus  pTargetLinkStatus;
  private NodeMod     pTargetLinkMod;
  
  /** 
   * The working area texture files indexed by the validated canonical texture files.
   */ 
  private TreeMap<File,File> pTextureFiles; 
 
  /**
   * The names of the Maya "File" shaders index by the validated canonical texture files.
   */ 
  private TreeMap<File,TreeSet<String>> pFileShaders;

  /**
   * Error messages for the Maya "File" shaders which could not be validated indexed 
   * by the names of the shaders.
   */
  private TreeMap<String,String>  pInvalidShaders;



  /*-- SECOND PHASE: UI --------------------------------------------------------------------*/

  /**
   * The working directory fields indexed by the validated canonical texture files.
   */ 
  private TreeMap<File,JPathField>  pWorkingPathFields; 



  /*-- SECOND PHASE: EXECUTE --------------------------------------------------------------*/
 
  /**
   * The texture filenames indexed by unique working area IDs of the parent nodes.
   */ 
  private TreeMap<NodeID,TreeSet<String>>  pNodeTextures; 

  /**
   * The unique working area IDs of the nodes which need to be registered.
   */ 
  private TreeSet<NodeID>  pRegisterNodes; 

  /**
   * The new frame ranges indexed by the unique working area IDs.
   */ 
  private TreeMap<NodeID,FrameRange>  pRenumberNodes; 

  /**
   * The unique working area IDs of the nodes which need to be checked-out.
   */ 
  private TreeSet<NodeID>  pCheckOutNodes; 

  /**
   * The unique working area IDs of the nodes which need to be linked to the 
   * target link node (pTargetLink).
   */ 
  private TreeSet<NodeID>  pLinkNodes; 
  
  /**
   * The unique working area IDs of the nodes which need to be unlinked from the 
   * target link node (pTargetLink).
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

}

