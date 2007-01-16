// $Id: RenumberTool.java,v 1.4 2007/01/16 00:21:23 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import java.awt.Component;
import java.awt.Dimension;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N U M B E R   T O O L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Renumbers the frame range of a target node and all nodes connected upstream
 * to the target node through links with a OneToOne relationship.
 * <P>
 * 
 * This tool operates identically to the built-in Renumber operation except that
 * it follows upstream links to also renumber all connected upstream nodes.
 */
public class 
RenumberTool 
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  RenumberTool()
  {
    super("Renumber", new VersionID("2.0.9"), "Temerity",
	  "Renumbers the frame range of a target node and all nodes connected upstream " + 
	  "to the target node through links with a OneToOne relationship.");

    pPhase = 1;

    pRenumberFields = new TreeMap<String, JBooleanField>();
    pPotentialNames = new TreeSet<String>(); 
    pRenumberNames = new TreeSet<String>();

    addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create and show graphical user interface components to collect information
   * from the user to use as input in the next phase of execution for the tool. <P>
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
    switch (pPhase) {
    case 1:
      return collectFirstPhaseInput();
      
    case 2:
      return collectSecondPhaseInput();
      
    default:
      throw new IllegalStateException(); 
    }
  }

  /**
   * Specify the renumbering parameters of the target node as per the built-in
   * Renumber operation.
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

      {
	pTargetNodeField = 
	  UIFactory.createTitledPathField
	    (tpanel, "Target Node:", sTSize, 
	     vpanel, new Path("/"), sVSize,
	     "The root of the tree of nodes being renumbered.");
      }
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

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


    /* validate node selections and initialize dialog components */
    {
      if(pPrimary == null)
	throw new PipelineException("The primary selection must be the Target Node!");

      pTargetNodeField.setText(pPrimary);

      {
	NodeStatus status = pSelected.get(pPrimary);
	NodeID nodeID = status.getNodeID();
	pUser = nodeID.getAuthor();
	pView = nodeID.getView();

	NodeDetails details = status.getDetails();
	if(details == null)
	  throw new PipelineException
	    ("The target node must have an existing status in order to be renumbered!");

	NodeMod mod = details.getWorkingVersion();
	if(mod == null)
	  throw new PipelineException
	    ("The target node must be checked-out in order to be renumbered!");

	{
	  FileSeq fseq = mod.getPrimarySequence();
	  if(!fseq.hasFrameNumbers())
	    throw new PipelineException
	      ("The Target Node (" + pTargetNode + ") must have frame numbers to be " + 
	       "renumbered!");
	  pOrigFrameRange = fseq.getFrameRange();
	  if(pOrigFrameRange == null)
	    throw new PipelineException
	      ("The target node must have frame numbers in order to be renumbered!");
	}

	pStartFrameField.setValue(pOrigFrameRange.getStart());
	pEndFrameField.setValue(pOrigFrameRange.getEnd());
	pByFrameField.setValue(pOrigFrameRange.getBy());

	pByFrameField.setEnabled(mod.hasIdenticalFrameRanges());

	pRemoveFilesField.setValue(false);
      }

      {
	Path wpath = new Path(PackageInfo.sWorkPath, pUser + "/" + pView);
	pWorkRoot = wpath.toOsString();
      }

      if(pSelected.size() != 1)
	throw new PipelineException
	  ("Only one Target Node may be selected to be renumbered.");
    }

    /* query the user */
    JToolDialog diag = new JToolDialog("Renumber:", body, "Continue");

    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      pTargetNode = pTargetNodeField.getText();
      if((pTargetNode == null) || (pTargetNode.length() == 0))
	throw new PipelineException("Illegal Target Node name!");
      
      /* collect new frame range info */
      {
	{
	  Integer frame = pStartFrameField.getValue();
	  if(frame == null)
	    throw new PipelineException
	      ("Unable to renumber node with an unspecified start frame!");
	  pStartFrame = frame;
	}
	
	{
	  Integer frame = pEndFrameField.getValue();
	  if(frame == null)
	    throw new PipelineException
	      ("Unable to renumber node with an unspecified end frame!");
	  pEndFrame = frame;
	}
	
	if(pStartFrame > pEndFrame) {
	  Integer tmp = pEndFrame;
	  pEndFrame = pStartFrame;
	  pStartFrame = tmp;
	  
	  pStartFrameField.setValue(pStartFrame);
	  pEndFrameField.setValue(pEndFrame);
	}
	
	{
	  Integer frame = pByFrameField.getValue();
	  if(frame == null)
	    throw new PipelineException
	      ("Unable to renumber node with an unspecified frame increment!");
	  pByFrame = frame;
	}
	
	pIsByEnabled = pByFrameField.isEnabled();
	
	{
	  Boolean tf = pRemoveFilesField.getValue();
	  pRemoveFiles = (tf != null) && tf;
	}
      }
      
      return ": Collecting Upstream Node Information...";
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
  collectSecondPhaseInput() 
    throws PipelineException
  {
    pRenumberNames.clear();
    pRenumberNames.addAll(pPotentialNames);

    pRenumberFields.clear();

    /* create dialog components */
    JScrollPane scroll = null;
    {
      Box ibox = new Box(BoxLayout.Y_AXIS);

      if(pPotentialNames.isEmpty()) {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	tpanel.add(Box.createRigidArea(new Dimension(sTSize - 7, 0)));
	vpanel.add(Box.createHorizontalGlue());
	
	ibox.add(comps[2]);
      } 
      else {
	for (String name : pPotentialNames) {
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, "Renumber Node:", sTSize - 7, 
					       vpanel, sVSize2,
					       "Whether to renumber the given node.");
	  field.setValue(true);
	  pRenumberFields.put(name, field);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  JDrawer drawer = new JDrawer("Node: " + name, (JComponent) comps[2], true);
	  ibox.add(drawer);
	}
      }
      
      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");

	spanel.setMinimumSize(new Dimension(sTSize + sVSize2, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize + sVSize2, 7));

	ibox.add(spanel);
      }

      {
	scroll = new JScrollPane(ibox);

	scroll
	  .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll
	  .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
    }

    /* query the user */
    JToolDialog diag = new JToolDialog("Renumber: Frame Range Changes", scroll,
				       "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      for (String name : pRenumberFields.keySet()) {
	JBooleanField field = pRenumberFields.get(name);
	Boolean value = field.getValue();
	if((value == null) || !value)
	  pRenumberNames.remove(name);
      }
      
      return ": Modifying Nodes...";
    } 
    else {
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one phase in the execution of the tool.<P>
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
    switch (pPhase) {
    case 1:
      return executeFirstPhase(mclient, qclient);
      
      case 2:
	return executeSecondPhase(mclient, qclient);
	
    default:
      throw new IllegalStateException();
    }
  }

  /**
   * Determine the nodes to be renumbered and collect any neeeded state
   * information.
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *    Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException
   *    If unable to sucessfully execute this phase of the tool.
   */
  private boolean 
  executeFirstPhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
  throws PipelineException
  {
    /* get the current status of the target node */
    {
      pTargetNodeID = new NodeID(pUser, pView, pTargetNode);
      pTargetNodeStatus = mclient.status(pTargetNodeID);
      pTargetNodeMod = pTargetNodeStatus.getDetails().getWorkingVersion();
      if(pTargetNodeMod == null)
	throw new PipelineException
	  ("No working version of the Target Node ("+ pTargetNode + ") exists " + 
	   "in the (" + pView + ") working area owned by (" + pUser + ")!");

      /* validate the new frame range */
      {
	pTargetFrameRange = null;

	if(!pIsByEnabled
	    && ((((pOrigFrameRange.getStart() - pStartFrame) % pByFrame) != 0) || 
		(((pOrigFrameRange.getStart() - pEndFrame) % pByFrame) != 0)))
	  throw new PipelineException
	    ("Unable to renumber node due to misalignment of the new frame range ("
	     + pStartFrame + "-" + pEndFrame + "x" + pByFrame + ") with the original " + 
	     "frame range (" + pOrigFrameRange + ")!");

	try {
	  pTargetFrameRange = new FrameRange(pStartFrame, pEndFrame, pByFrame);
	} 
	catch (IllegalArgumentException ex) {
	  throw new PipelineException("Unable to renumber node. " + ex.getMessage());
	}

	if(pOrigFrameRange.equals(pTargetFrameRange))
	  throw new PipelineException
	    ("Unecessary to renumber node when the frame range (" +
	     pOrigFrameRange + " " + "has not been altered!");
      }
    }

    /*
     * find all upstream nodes with the same number of frames connected via
     * OneToOne link
     */
    pPotentialNames.clear();
    findNodesToRenumber(pTargetNodeStatus);

    pPhase++;
    return true;
  }

  /**
   * Renumber the nodes.
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
    for(String name : pRenumberNames)
      mclient.renumber(pUser, pView, name, pTargetFrameRange, pRemoveFiles);
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*  H E L P E R S                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively search the upstream links for nodes to renumber.
   */
  private void 
  findNodesToRenumber
  (
   NodeStatus status
  )
  {
    NodeDetails details = status.getDetails();
    if(details == null)
      return;

    NodeMod mod = details.getWorkingVersion();
    if(mod == null)
      return;

    if(mod.getPrimarySequence().getFrameRange().equals(pOrigFrameRange)) {
      pPotentialNames.add(status.getName());
      
      for (NodeStatus lstatus : status.getSources()) {
	LinkMod link = mod.getSource(lstatus.getName());
	if(link.getRelationship() == LinkRelationship.OneToOne)
	  findNodesToRenumber(lstatus);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9120144129255872029L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 300;
  private static final int sVSize1 = 60;
  private static final int sVSize2 = 250; 



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The phase number.
   */
  private int pPhase;

  /**
   * The current working area user|view.
   */
  private String pUser;
  private String pView;

  /**
   * The directory prefix path of the current working area view.
   */
  private String pWorkRoot;


  /*-- FIRST PHASE: UI ---------------------------------------------------------------------*/

  /**
   * The root node being renumbered;
   */
  private JPathField pTargetNodeField;
  private String pTargetNode;
  private NodeID pTargetNodeID;
  private NodeMod pTargetNodeMod;

  /**
   * New frame range info.
   */ 
  private JIntegerField pStartFrameField;
  private int pStartFrame;

  private JIntegerField pEndFrameField;
  private int pEndFrame;

  private JIntegerField pByFrameField;
  private int pByFrame;
  private boolean pIsByEnabled;

  private JBooleanField pRemoveFilesField;
  private boolean pRemoveFiles;

  private FrameRange pOrigFrameRange;


  /*-- FIRST PHASE: EXECUTE ----------------------------------------------------------------*/

  /**
   * New frame range.
   */
  private FrameRange pTargetFrameRange;

  /**
   * Status of nodes to renumber.
   */
  private NodeStatus pTargetNodeStatus;

  /**
   * The names of the nodes which may potentially be renumbered.
   */
  private TreeSet<String> pPotentialNames;


  /*-- SECOND PHASE: UI --------------------------------------------------------------------*/

  /**
   * The per-node renumbering fields.
   */
  private TreeMap<String, JBooleanField> pRenumberFields;

  /**
   * The names of the nodes approved for renumbering.
   */
  private TreeSet<String> pRenumberNames;


}
