// $Id: JNodeDetailsPanel.java,v 1.1 2004/06/14 22:55:00 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A viewer/editor of node properties. <P> 
 * 
 * The node properties displayed include: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   The toolset environment under which editors and actions are run. <BR>
 *   The name of the editor plugin used to edit the data files associated with the node. <BR>
 *   The regeneration action and its single and per-dependency parameters. <BR>
 *   The job requirements. <BR>
 *   The IgnoreOverflow and IsSerial flags. <BR>
 *   The job batch size. <BR> 
 * </DIV> <BR> 
 * 
 */ 
public  
class JNodeDetailsPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeDetailsPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeDetailsPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    /* initialize fields */ 
    {
      pCheckedInVersions = new TreeMap<VersionID,NodeVersion>();

      pActionParamComponents = new TreeMap<String,Component[]>();

      pLinkActionParamValues    = new ArrayList<String>();
      pLinkActionParamNodeNames = new ArrayList<String>();

      pTextParamValues = new TreeMap<String,String>();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = new JPanel();	

	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());

	{
	  JButton btn = new JButton();		
	  pApplyButton = btn;
	  btn.setName("ApplyHeaderButton");
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("apply");
	  btn.addActionListener(this);
	  
	  panel.add(btn);
	} 

	panel.add(Box.createRigidArea(new Dimension(15, 0)));
      
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIMaster.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  field.setFocusable(true);
	  field.addKeyListener(this);
	  field.addMouseListener(this); 

	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	add(hbox);
      }
	
      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	/* versions panel */ 
	{
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* version state */ 
	    {
	      pVersionStateField = 
		UIMaster.createTitledTextField(tpanel, "Version State:", sTSize, 
					       vpanel, "-", sSSize);
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 6);

	    /* revision number */ 
	    { 
	      tpanel.add(UIMaster.createFixedLabel("Revision Number:", sTSize, JLabel.RIGHT));

	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pBaseVersionField = field;

		  hbox.add(field);
		}

		hbox.add(Box.createRigidArea(new Dimension(8, 0)));
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pCheckedInVersionField = field;

		  field.addActionListener(this);
		  field.setActionCommand("update-version");

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }
	  }
	
	  JDrawer drawer = new JDrawer("Versions:", (JComponent) comps[2], true);	
	  vbox.add(drawer);
	}

	/* properties panel */ 
	{
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* property state */ 
	    {
	      pPropertyStateField = 
		UIMaster.createTitledTextField(tpanel, "Property State:", sTSize, 
					       vpanel, "-", sSSize);
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 6);

	    /* toolset */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Toolset:", sTSize, JLabel.RIGHT);
		pToolsetTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingToolsetField = field;
		  
		  field.setActionCommand("toolset-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetToolsetButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-toolset");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInToolsetField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* editor */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Editor:", sTSize, JLabel.RIGHT);
		pEditorTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingEditorField = field;
		  
		  field.setActionCommand("editor-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetEditorButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-editor");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInEditorField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Properties:", (JComponent) comps[2], true);
	  vbox.add(drawer);
	}
	
	/* actions panel */ 
	{
	  Box abox = new Box(BoxLayout.Y_AXIS);
	  pActionBox = abox;

	  {
	    Component comps[] = createCommonPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    tpanel.setName("TopTitlePanel");
	    JPanel vpanel = (JPanel) comps[1];
	    vpanel.setName("TopValuePanel");

	    /* action */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Action:", sTSize, JLabel.RIGHT);
		pActionTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingActionField = field;
		
		  field.setActionCommand("action-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetActionButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-action");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    abox.add(comps[2]);
	  }

	  {
	    Box apbox = new Box(BoxLayout.Y_AXIS);
	    pActionParamsBox = apbox;

	    abox.add(apbox);
	  }
	  
	  JDrawer drawer = new JDrawer("Regeneration Action:", abox, true);
	  vbox.add(drawer);
	}
	
	/* job requirements */ 
	{
	  JDrawer drawer = new JDrawer("Job Requirements:");
	  
	  // ...
	  
	  vbox.add(drawer);
	}

	vbox.add(Box.createVerticalGlue());

	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	  
	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sSSize+40, 120);
      setMinimumSize(size);
      setPreferredSize(size);

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null);

    pEditTextDialog = new JTextDialog(true);
    pViewTextDialog = new JTextDialog(false);
  }


  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  private Component[]
  createCommonPanels()
  {
    Component comps[] = new Component[3];
    
    Box body = new Box(BoxLayout.X_AXIS);
    comps[2] = body;
    {
      {
	JPanel panel = new JPanel();
	comps[0] = panel;
	
	panel.setName("TitlePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
	panel.setFocusable(true);
	panel.addKeyListener(this);
	panel.addMouseListener(this); 

	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	panel.setFocusable(true);
	panel.addKeyListener(this);
	panel.addMouseListener(this); 

	body.add(panel);
      }
    }

    return comps;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    if(pGroupID > 0)
      master.releaseNodeDetailsGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && master.isNodeDetailsGroupUnused(groupID)) {
      master.assignNodeDetailsGroup(this, groupID);
      pGroupID = groupID;
    }
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    UIMaster master = UIMaster.getInstance();
    return master.isNodeDetailsGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the currently displayed node.
   * 
   * @return 
   *   The fully resolved node name or <CODE>null</CODE> if undefined.
   */ 
  public synchronized String 
  getNodeName() 
  {
    if(pStatus != null) 
      return pStatus.getName();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current node have a working version?
   */ 
  private boolean 
  hasWorking() 
  {
    return (getWorkingVersion() != null);
  }

  /**
   * Get the working version of the current node.
   * 
   * @return 
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  private NodeMod
  getWorkingVersion() 
  {
    if((pStatus != null) && (pStatus.getDetails() != null))
      return pStatus.getDetails().getWorkingVersion();
    return null;
  }


  /**
   * Initialize the temporary regeneration action with the action of the working version 
   * of the current node.
   * 
   * @return 
   *   The working action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  initWorkingAction() 
  {
    pWorkingAction = null;

    NodeMod mod = getWorkingVersion();
    if(mod != null) 
      pWorkingAction = mod.getAction();

    return pWorkingAction;
  }

  /**
   * Get the temporary regeneration action of the working version of the current node.
   * 
   * @return 
   *   The working action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  getWorkingAction() 
  {
    return pWorkingAction;
  }

  /**
   * Set the temporary regeneration action of the working version of the current node.
   * 
   * @param action
   *   The working action.
   */ 
  private void
  setWorkingAction
  (
   BaseAction action
  ) 
  {
    pWorkingAction = action;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current node have any checked-in versions?
   */ 
  private boolean 
  hasCheckedIn() 
  {
    return ((pStatus != null) && (pStatus.getDetails() != null) && 
	    (pStatus.getDetails().getLatestVersion() != null));
  }

  /**
   * Get the selected checked-in version of the current node.
   * 
   * @return 
   *   The checked-in version or <CODE>null</CODE> if none exists.
   */ 
  private NodeVersion
  getCheckedInVersion() 
  {
    NodeVersion vsn = null;
    if((pStatus != null) && (pStatus.getDetails() != null)) {
      NodeDetails details = pStatus.getDetails();
      if(pStatus.getDetails().getLatestVersion() != null) {
	ArrayList<VersionID> vids = details.getVersionIDs();
	Collections.reverse(vids);
	VersionID vid = vids.get(pCheckedInVersionField.getSelectedIndex());
	
	vsn = pCheckedInVersions.get(vid);
	if(vsn == null) {
	  UIMaster master = UIMaster.getInstance();
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();
	    vsn = client.getCheckedInVersion(pStatus.getName(), vid);
	    pCheckedInVersions.put(vid, vsn);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return null;
	  }
	}
	assert(vsn != null);
      }
    }

    return vsn;
  }

  /**
   * Get the regeneration action of the selected checked-in version of the current node.
   * 
   * @return 
   *   The checked-in action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  getCheckedInAction() 
  {
    NodeVersion vsn = getCheckedInVersion();
    if(vsn != null) 
      return vsn.getAction();
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param status
   *   The current node status.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status);
  }

  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param status
   *   The current node status.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status
  ) 
  {
    pStatus = status;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    NodeMod work = null;
    NodeVersion base = null;
    NodeVersion latest = null;
    if(details != null) {
      work = details.getWorkingVersion();
      base = details.getBaseVersion();
      latest = details.getLatestVersion();
    }

    {
      pCheckedInVersions.clear();

      if(base != null) 
	pCheckedInVersions.put(base.getVersionID(), base);

      if(latest != null) 
	pCheckedInVersions.put(latest.getVersionID(), latest);
    }

    /* header */ 
    {
      String name = "Blank-Normal";
      if(pStatus != null) {
	if(details != null) 
	  name = (details.getOverallNodeState() + "-" + 
		  details.getOverallQueueState() + "-Normal");
		
	pHeaderLabel.setText(pStatus.toString());
	pNodeNameField.setText(pStatus.getName());
      }
      else {
	pHeaderLabel.setText(null);
	pNodeNameField.setText(null);
      }
      
      try {
	pHeaderLabel.setIcon(TextureMgr.getInstance().getIcon(name));
      }
      catch(IOException ex) {
	Logs.tex.severe("Internal Error:\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);
      } 
    }   

    /* versions panel */ 
    {
      if(details != null) 
	pVersionStateField.setText(details.getVersionState().toTitle());
      else 
	pVersionStateField.setText("-");

      /* revision number */ 
      {
	if(base != null) 
	  pBaseVersionField.setText("v" + base.getVersionID().toString());
	else 
	  pBaseVersionField.setText("-");
	
	{
	  ArrayList<String> values = new ArrayList<String>();
	  if(details != null) {
	    for(VersionID vid : details.getVersionIDs()) 
	      values.add("v" + vid.toString());
	  }
	  Collections.reverse(values);
	  
	  if(values.isEmpty()) 
	    values.add("-");

	  pCheckedInVersionField.removeActionListener(this);
 	    pCheckedInVersionField.setValues(values);
	    pCheckedInVersionField.setSelectedIndex(0);
	  pCheckedInVersionField.addActionListener(this);

	  pCheckedInVersionField.setEnabled(latest != null);
	}
      }
    }

    /* properties panel */ 
    {
      if(details != null) 
	pPropertyStateField.setText(details.getPropertyState().toTitle());
      else 
	pPropertyStateField.setText("-");
      
      /* toolset */ 
      {
	pWorkingToolsetField.removeActionListener(this);
	{
	  TreeSet<String> toolsets = new TreeSet<String>();
	  if(work != null) {
	    UIMaster master = UIMaster.getInstance();
	    try {
	      toolsets.addAll(master.getMasterMgrClient().getActiveToolsetNames());
	      if((work.getToolset() != null) && !toolsets.contains(work.getToolset()))
		toolsets.add(work.getToolset());
	    }
	    catch(PipelineException ex) {
	    }
	  }
	  
	  if(toolsets.isEmpty())
	    toolsets.add("-");
	  
	  pWorkingToolsetField.setValues(toolsets);

	  if((work != null) && (work.getToolset() != null)) 
	    pWorkingToolsetField.setSelected(work.getToolset());
	  else 
	    pWorkingToolsetField.setSelected("-");

	  pWorkingToolsetField.setEnabled(!pIsLocked && (work != null));
	}
	pWorkingToolsetField.addActionListener(this);
	
	pSetToolsetButton.setEnabled(!pIsLocked && (work != null) && (latest != null));
	
	{
	  if(latest != null)
	    pCheckedInToolsetField.setText(latest.getToolset());
	  else 
	    pCheckedInToolsetField.setText("-");

	  pCheckedInToolsetField.setEnabled(latest != null);
	}

	doToolsetChanged();
      }

      /* editor */ 
      { 
	pWorkingEditorField.removeActionListener(this);
	{
	  TreeSet<String> editors = new TreeSet<String>();
	  if(work != null) 
	    editors.addAll(Plugins.getEditorNames());
	  editors.add("-");
	  pWorkingEditorField.setValues(editors);
	  
	  if((work != null) && 
	     (work.getEditor() != null) && (editors.contains(work.getEditor())))
	    pWorkingEditorField.setSelected(work.getEditor());
	  else 
	    pWorkingEditorField.setSelected("-");
	  
	  pWorkingEditorField.setEnabled(!pIsLocked && (work != null));
	}
	pWorkingEditorField.addActionListener(this);
	
	pSetEditorButton.setEnabled(!pIsLocked && (work != null) && (latest != null));
	
	{
	  if((latest != null) && (latest.getEditor() != null))
	    pCheckedInEditorField.setText(latest.getEditor());
	  else 
	    pCheckedInEditorField.setText("-");
	  
	  pCheckedInEditorField.setEnabled(latest != null);
	}

	doEditorChanged();
      }
    }
    
    /* actions panel */ 
    {
      pWorkingActionField.removeActionListener(this);
      {
	TreeSet<String> actions = new TreeSet<String>();
	if(work != null) 
	  actions.addAll(Plugins.getActionNames());
	actions.add("-");
	pWorkingActionField.setValues(actions);
	
	BaseAction waction = initWorkingAction();
	if((waction != null) && (actions.contains(waction.getName())))
	  pWorkingActionField.setSelected(waction.getName());
	else 
	  pWorkingActionField.setSelected("-");
	
	pWorkingActionField.setEnabled(!pIsLocked && (work != null));
      }
      pWorkingActionField.addActionListener(this);

      pSetActionButton.setEnabled(!pIsLocked && (work != null) && (latest != null));

      {
	BaseAction caction = getCheckedInAction();	
	if(caction != null) 
	  pCheckedInActionField.setText(caction.getName());
	else 
	  pCheckedInActionField.setText("-");
      }

      pActionParamComponents.clear();
      doActionChanged();
    }

    
    // ...


    pApplyButton.setEnabled(false);
  }

  /**
   * Update checked-in version related values of all fields.
   */ 
  private void 
  updateVersion() 
  {
    /* lookup the selected checked-in version */ 
    NodeVersion vsn = getCheckedInVersion();
    assert(vsn != null);

    /* properties panel */ 
    {
      /* toolset */ 
      {
	if(vsn.getToolset() != null)
	  pCheckedInToolsetField.setText(vsn.getToolset());
	else 
	  pCheckedInToolsetField.setText("-");

	doToolsetChanged();
      }

      /* editor */ 
      {
	if(vsn.getEditor() != null)
	  pCheckedInEditorField.setText(vsn.getEditor());
	else 
	  pCheckedInEditorField.setText("-");

	doEditorChanged();
      }
    }
    
    /* actions panel */ 
    updateActionParams();      


    // ...


  }


  /**
   * Update the UI components associated with the working and checked-in actions.
   */ 
  private void 
  updateActionParams()
  {
    pActionParamsBox.removeAll();

    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    tpanel.setName("BottomTitlePanel");
    JPanel vpanel = (JPanel) comps[1];
    vpanel.setName("BottomValuePanel");

    if((action == null) || (!action.hasSingleParams() && !action.supportsSourceParams())) {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }
    else {
      {
	pLinkActionParamValues.clear();
	for(String sname : pStatus.getSourceNames()) 
	  pLinkActionParamValues.add(pStatus.getSource(sname).toString());
	pLinkActionParamValues.add("-");
	
	pLinkActionParamNodeNames.clear();
	pLinkActionParamNodeNames.addAll(pStatus.getSourceNames());
	pLinkActionParamNodeNames.add(null);

	pTextParamValues.clear();
      }


      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

      for(BaseActionParam param : action.getSingleParams()) {
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	Component pcomps[] = new Component[3];
	
	{
	  JLabel label = 
	    UIMaster.createFixedLabel(param.getNameUI() + ":", sTSize, JLabel.RIGHT);
	  pcomps[0] = label;
	  
	  tpanel.add(label);
	}
	
	{ 
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  {
	    BaseActionParam aparam = null;
	    if(waction != null) 
	      aparam = waction.getSingleParam(param.getName());
	    
	    if(aparam != null) {
	      if(aparam instanceof IntegerActionParam) {
		Integer value = (Integer) aparam.getValue();
		JIntegerField field = 
		  UIMaster.createIntegerField(value, sVSize, JLabel.CENTER);
		pcomps[1] = field;

		field.addActionListener(this);
		field.setActionCommand("action-param-changed:" + aparam.getName());

		field.setEnabled(!pIsLocked);

		hbox.add(field);
	      }
	      else if(aparam instanceof DoubleActionParam) {
		Double value = (Double) aparam.getValue();
		JDoubleField field = 
		  UIMaster.createDoubleField(value, sVSize, JLabel.CENTER);
		pcomps[1] = field;
		
		field.addActionListener(this);
		field.setActionCommand("action-param-changed:" + aparam.getName());

		field.setEnabled(!pIsLocked);

		hbox.add(field);
	      }
	      else if(aparam instanceof StringActionParam) {
		String value = (String) aparam.getValue();
		JTextField field = 
		  UIMaster.createEditableTextField(value, sVSize, JLabel.CENTER);
		pcomps[1] = field;
		
		field.addActionListener(this);
		field.setActionCommand("action-param-changed:" + aparam.getName());

		field.setEnabled(!pIsLocked);

		hbox.add(field);
	      }
	      else if(aparam instanceof TextActionParam) {
		pTextParamValues.put(aparam.getName(), (String) aparam.getValue());

		JButton btn = new JButton("Edit...");
		pcomps[1] = btn;
		
		btn.setName("ValuePanelButton");
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		  
		Dimension size = new Dimension(sVSize, 19);
		btn.setMinimumSize(size);
		btn.setPreferredSize(size);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

		btn.addActionListener(this);
		btn.setActionCommand("edit-text-param:" + aparam.getName());
		
		btn.setEnabled(!pIsLocked);

		hbox.add(btn);	      
	      }
	      else if(aparam instanceof EnumActionParam) {
		EnumActionParam eparam = (EnumActionParam) aparam;
		JCollectionField field = 
		  UIMaster.createCollectionField(eparam.getTitles(), sVSize);
		pcomps[1] = field;
	      
		field.setSelectedIndex(((Enum) eparam.getValue()).ordinal());
		
		field.addActionListener(this);
		field.setActionCommand("action-param-changed:" + aparam.getName());

		field.setEnabled(!pIsLocked);

		hbox.add(field);
	      }
	      else if(aparam instanceof LinkActionParam) {
		JCollectionField field = 
		  UIMaster.createCollectionField(pLinkActionParamValues, sVSize);
		pcomps[1] = field;

		String value = null;
		{
		  String source = (String) aparam.getValue();
		  if(source != null) {
		    NodeStatus status = pStatus.getSource(source);
		    if(status != null) 
		      value = status.toString();
		  }
		}

		if((value != null) && field.getValues().contains(value)) 
		  field.setSelected(value);
		else 
		  field.setSelected("-");
			
		field.addActionListener(this);
		field.setActionCommand("action-param-changed:" + aparam.getName());

		field.setEnabled(!pIsLocked);

		hbox.add(field);
	      }
	    }
	    else {
	      JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
	      pcomps[1] = field;
	      
	      hbox.add(field);
	    }
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
	  
	  {
	    BaseActionParam aparam = null;
	    if((caction != null) && 
	       ((waction == null) || caction.getName().equals(waction.getName())))
	      aparam = caction.getSingleParam(param.getName());
	    
	    if(aparam != null) {
	      if(aparam instanceof TextActionParam) {
		JButton btn = new JButton("View...");
		pcomps[2] = btn;
		
		btn.setName("ValuePanelButton");
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		
		Dimension size = new Dimension(sVSize, 19);
		btn.setMinimumSize(size);
		btn.setPreferredSize(size);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
		
		btn.addActionListener(this);
		btn.setActionCommand("view-text-param:" + aparam.getName());

		hbox.add(btn);	      
	      }
	      else {
		String value = aparam.getValue().toString();
		JTextField field = UIMaster.createTextField(value, sVSize, JLabel.CENTER);
		pcomps[2] = field;
		
		hbox.add(field);
	      }
	    }
	    else {
	      JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
	      pcomps[2] = field;
	      
	      hbox.add(field);
	    }
	  }
	  
	  vpanel.add(hbox);
	}
	
	pActionParamComponents.put(param.getName(), pcomps);
      }

      
      // per-depend params...


    }
    
    pActionParamsBox.add(comps[2]);
    pActionBox.revalidate();
    pActionBox.repaint();
  }

  /**
   * Update the color of the UI components associated with working and checked-in action.
   */ 
  private void 
  updateActionColors()
  {
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      if(((waction == null) && (caction != null)) ||
	 ((waction != null) && !waction.equals(caction)))
	color = Color.cyan;
      else 
	color = null;
    }
    
    Color fg = color;
    if(fg == null) 
      fg = Color.white;

    pActionTitle.setForeground(fg);
    pWorkingActionField.setForeground(fg);
    pCheckedInActionField.setForeground(fg);

    if(action != null) {
      for(BaseActionParam param : action.getSingleParams()) 
	updateActionParamColor(param.getName(), color);
    }
  }

  /**
   * Update the color of the UI components associated with an action parameter.
   */ 
  private void
  updateActionParamColor
  (
   String pname, 
   Color color
  ) 
  {
    Component pcomps[] = pActionParamComponents.get(pname);
    if(pcomps == null)
      return;

    Color fg = color;
    if(fg == null) {
      BaseAction waction = getWorkingAction();
      BaseAction caction = getCheckedInAction();

      String wtext = null;
      {
	BaseActionParam aparam = null;
	if(waction != null) 
	  aparam = waction.getSingleParam(pname);
      
	if(aparam != null) {
	  if(aparam instanceof IntegerActionParam) {
	    JIntegerField field = (JIntegerField) pcomps[1];
	    wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof DoubleActionParam) {
	    JDoubleField field = (JDoubleField) pcomps[1];
	    wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof StringActionParam) {
	    JTextField field = (JTextField) pcomps[1];
	    wtext = field.getText();
	  }
	  else if(aparam instanceof TextActionParam) {
	    wtext = pTextParamValues.get(aparam.getName());
	  }
	  else if(aparam instanceof EnumActionParam) {
	    JCollectionField field = (JCollectionField) pcomps[1];
	    wtext = field.getSelected();
	  }
	  else if(aparam instanceof LinkActionParam) {
	    JCollectionField field = (JCollectionField) pcomps[1];
	    wtext = pLinkActionParamNodeNames.get(field.getSelectedIndex());
	  }
	}
      }
      
      String ctext = null;
      {
	BaseActionParam aparam = null;
	if((caction != null) && 
	   ((waction == null) || caction.getName().equals(waction.getName())))
	  aparam = caction.getSingleParam(pname);
	
	if(aparam != null) 
	  ctext = aparam.getValue().toString();
      }

      if(((wtext == null) && (ctext == null)) ||
	 ((wtext != null) && wtext.equals(ctext)))
	fg = Color.white;
      else
	fg = Color.cyan;
    }

    int wk;
    for(wk=0; wk<pcomps.length; wk++) 
      pcomps[wk].setForeground(fg);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
  
    //...
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    // ...
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    requestFocusInWindow();
  }

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed(MouseEvent e) {}
  
  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();

    if((prefs.getNodeDetailsApplyChanges() != null) &&
       prefs.getNodeDetailsApplyChanges().wasPressed(e))
      doApply();    
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 


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
    if(cmd.equals("apply")) 
      doApply();
    if(cmd.equals("update-version")) 
      updateVersion();
    else if(cmd.equals("set-toolset")) 
      doSetToolset();
    else if(cmd.equals("toolset-changed")) 
      doToolsetChanged();
    else if(cmd.equals("set-editor")) 
      doSetEditor();
    else if(cmd.equals("editor-changed")) 
      doEditorChanged();
    else if(cmd.equals("set-action")) 
      doSetAction();
    else if(cmd.equals("action-changed")) 
      doActionChanged();
    else if(cmd.startsWith("action-param-changed:")) 
      doActionParamChanged(cmd.substring(21));
    else if(cmd.startsWith("edit-text-param:"))
      doEditTextParam(cmd.substring(16));
    else if(cmd.startsWith("view-text-param:"))
      doViewTextParam(cmd.substring(16));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Modify the working version of the node based on the current settings if the 
   * UI components.
   */ 
  private void 
  doApply()
  {
    if(!pIsLocked && (pStatus != null) && (pStatus.getDetails() != null)) {
      NodeMod work = pStatus.getDetails().getWorkingVersion();
      if(work != null) {
	try { 
	  NodeMod mod = new NodeMod(work);

	  /* properties panel */ 
	  {
	    String toolset = pWorkingToolsetField.getSelected();
	    if((toolset != null) && !toolset.equals("-"))
	      mod.setToolset(toolset);
	    
	    String editor = pWorkingEditorField.getSelected();
	    if((editor != null) && !editor.equals("-"))
	      mod.setEditor(editor);
	    else 
	      mod.setEditor(null);	
	  }
	
	  /* action panel */ 
	  {
	    BaseAction waction = getWorkingAction();
	    if(waction != null) {
	      for(BaseActionParam aparam : waction.getSingleParams()) {
		Component pcomps[] = pActionParamComponents.get(aparam.getName());
		Comparable value = null;
		if(aparam instanceof IntegerActionParam) {   
		  JIntegerField field = (JIntegerField) pcomps[1];
		  value = field.getValue();
		  if(value == null) 
		    value = new Integer(0);
		}
		else if(aparam instanceof DoubleActionParam) { 
		  JDoubleField field = (JDoubleField) pcomps[1];
		  value = field.getValue();
		  if(value == null) 
		    value = new Double(0.0);
		}
		else if(aparam instanceof StringActionParam) {
		  JTextField field = (JTextField) pcomps[1];
		  value = field.getText();
		}
		else if(aparam instanceof TextActionParam) {
		  value = pTextParamValues.get(aparam.getName()); 
		}
		else if(aparam instanceof EnumActionParam) {
		  JCollectionField field = (JCollectionField) pcomps[1];
		  EnumActionParam eparam = (EnumActionParam) aparam;
		  value = eparam.getValueOfIndex(field.getSelectedIndex());
		}
		else if(aparam instanceof LinkActionParam) {
		  JCollectionField field = (JCollectionField) pcomps[1];
		  value = pLinkActionParamNodeNames.get(field.getSelectedIndex());
		}
		
		waction.setSingleParamValue(aparam.getName(), value);
	      }
	      
	      mod.setAction(waction);
	    }
	    else {
	      mod.setAction(null);
	    }	  

	    setWorkingAction(null);
	  }

	  
	  // ...
	

	  pApplyButton.setEnabled(false);
	  
	  ModifyTask task = new ModifyTask(mod);
	  task.start();
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working toolset field from the value of the checked-in field.
   */ 
  private void 
  doSetToolset()
  { 
    pWorkingToolsetField.removeActionListener(this);
    {
      String toolset = pCheckedInToolsetField.getText();
      if(!toolset.equals("-")) {
	if(!pWorkingToolsetField.getValues().contains(toolset)) {
	  TreeSet<String> values = new TreeSet<String>(pWorkingToolsetField.getValues());
	  values.add(toolset);
	  pWorkingToolsetField.setValues(values);
	}
	
	pWorkingToolsetField.setSelected(toolset);
      }
    }
    pWorkingToolsetField.addActionListener(this);
  
    doToolsetChanged();
  }

  /**
   * Update the appearance of the toolset field after a change of value.
   */ 
  private void 
  doToolsetChanged() 
  {
    pApplyButton.setEnabled(true);

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wtset = pWorkingToolsetField.getSelected();
      String ctset = pCheckedInToolsetField.getText();
      if(!ctset.equals(wtset))
	color = Color.cyan;
    }

    pToolsetTitle.setForeground(color);
    pWorkingToolsetField.setForeground(color);
    pCheckedInToolsetField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working editor field from the value of the checked-in field.
   */ 
  private void 
  doSetEditor()
  { 
    pWorkingEditorField.removeActionListener(this);
    {
      String editor = pCheckedInEditorField.getText();
      if(pWorkingEditorField.getValues().contains(editor))
	pWorkingEditorField.setSelected(editor);
      else 
	pWorkingEditorField.setSelected("-");
    }
    pWorkingEditorField.addActionListener(this);

    doEditorChanged();
  }

  /**
   * Update the appearance of the editor field after a change of value.
   */ 
  private void 
  doEditorChanged() 
  {
    pApplyButton.setEnabled(true);

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String weditor = pWorkingEditorField.getSelected();
      String ceditor = pCheckedInEditorField.getText();
      if(!ceditor.equals(weditor))
	color = Color.cyan;
    }

    pEditorTitle.setForeground(color);
    pWorkingEditorField.setForeground(color);
    pCheckedInEditorField.setForeground(color);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working action and parameters fields from the value of the checked-in action..
   */ 
  private void 
  doSetAction()
  { 
    pWorkingActionField.removeActionListener(this);
    {
      BaseAction action = getCheckedInAction();
      if((action != null) && 
	 pWorkingActionField.getValues().contains(action.getName()))
	pWorkingActionField.setSelected(action.getName());
      else 
	pWorkingActionField.setSelected("-");
    }
    pWorkingActionField.addActionListener(this);
      
    pActionParamComponents.clear();
    doActionChanged();
  }

  /**
   * Update the appearance of the action fields after a change of value.
   */ 
  private void 
  doActionChanged() 
  {
    pApplyButton.setEnabled(true);

    {
      String aname = pWorkingActionField.getSelected();
      if(aname.equals("-")) {
	setWorkingAction(null);
	pActionParamComponents.clear();
      }
      else if((pWorkingAction== null) || !pWorkingAction.getName().equals(aname)) {
	try {
	  setWorkingAction(Plugins.newAction(aname));
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);

	  setWorkingAction(null);

	  pWorkingActionField.removeActionListener(this);
  	    pWorkingActionField.setSelected("-");
	  pWorkingActionField.addActionListener(this);
	}

	pActionParamComponents.clear();
      }

      updateActionParams();
    }
    
    updateActionColors();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the action parameter fields after a change of parameter value.
   */ 
  private void 
  doActionParamChanged
  (
   String pname
  ) 
  {
    pApplyButton.setEnabled(true);
        
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    Color color = null;
    if(hasWorking() && hasCheckedIn()) {
      if(((waction == null) && (caction != null)) ||
	 ((waction != null) && !waction.equals(caction)))
	color = Color.cyan;
      else 
	color = Color.white;
    }

    updateActionParamColor(pname, color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show a dialog for editing the value of the given working text action parameter.
   */ 
  private void 
  doEditTextParam
  (
   String pname
  ) 
  {
    BaseAction waction = getWorkingAction();
    if(waction != null) {
      BaseActionParam param = waction.getSingleParam(pname);

      pEditTextDialog.updateText("Edit " + param.getNameUI() + ":", 
				 pTextParamValues.get(pname)); 

      pEditTextDialog.setVisible(true);      
      if(pEditTextDialog.wasConfirmed()) {
	pTextParamValues.put(pname, pEditTextDialog.getText());
	doActionParamChanged(pname);
      }
    }
  }

  /**
   * Show a dialog for viewing the value of the given checked-in text action parameter.
   */ 
  private void 
  doViewTextParam
  (
   String pname
  ) 
  {
    BaseAction caction = getCheckedInAction();
    if(caction != null) {
      BaseActionParam param = caction.getSingleParam(pname);

      pViewTextDialog.updateText("View " + param.getNameUI() + ":", 
				 (String) param.getValue());

      pViewTextDialog.setVisible(true);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Modify the properties of the given working version of a node.
   */ 
  private
  class ModifyTask
    extends Thread
  {
    public 
    ModifyTask
    (
     NodeMod mod
    ) 
    {
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Modifying Node...")) {
	try {
	  master.getMasterMgrClient().modifyProperties(pAuthor, pView, pNodeMod);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  JNodeViewerPanel viewer = UIMaster.getInstance().getNodeViewer(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private NodeMod  pNodeMod;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2714804145579513176L;


  private static final int  sTSize = 110;
  private static final int  sVSize = 130;
  private static final int  sSSize = 273;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * Cached checked-in versions associated with the current node.
   */ 
  private TreeMap<VersionID,NodeVersion>  pCheckedInVersions; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;

  /**
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The version state field.
   */ 
  private JTextField pVersionStateField;

  /**
   * The base revision number field.
   */ 
  private JTextField pBaseVersionField;

  /**
   * The checked-in revision numbers field.
   */ 
  private JCollectionField pCheckedInVersionField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The property state field.
   */ 
  private JTextField pPropertyStateField;


  /**
   * The toolset title label.
   */ 
  private JLabel  pToolsetTitle;

  /**
   * The working toolset field.
   */ 
  private JCollectionField pWorkingToolsetField;

  /**
   * The set toolset button.
   */ 
  private JButton  pSetToolsetButton;

  /**
   * The checked-in toolset field.
   */ 
  private JTextField pCheckedInToolsetField;


  /**
   * The editor title label.
   */ 
  private JLabel  pEditorTitle;

  /**
   * The working editor field.
   */ 
  private JCollectionField pWorkingEditorField;

  /**
   * The set editor button.
   */ 
  private JButton  pSetEditorButton;

  /**
   * The checked-in editor field.
   */ 
  private JTextField pCheckedInEditorField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The action title label.
   */ 
  private JLabel  pActionTitle;

  /**
   * The working action field.
   */ 
  private JCollectionField pWorkingActionField;

  /**
   * The set action button.
   */ 
  private JButton  pSetActionButton;

  /**
   * The checked-in action field.
   */ 
  private JTextField pCheckedInActionField;


  /**
   * The top-level container of the actions drawer.
   */ 
  private Box  pActionBox;

  /**
   * The action parameters container.
   */ 
  private Box  pActionParamsBox;

  /**
   * The temporary working regeneration action.
   */ 
  private BaseAction  pWorkingAction;

  /**
   * The title, working and checked-in action parameter components indexed by 
   * action parameter name.
   */ 
  private TreeMap<String,Component[]>  pActionParamComponents;

  /**
   * The JCollectionField values and corresponding fully resolved names of the 
   * upstream nodes used by LinkActionParam fields.
   */ 
  private ArrayList<String>  pLinkActionParamValues;
  private ArrayList<String>  pLinkActionParamNodeNames;

  /**
   * The temporary working text action parameter values indexed by action parameter name.
   */ 
  private TreeMap<String,String>  pTextParamValues;


  /**
   * The dialog used to edit working text action parameter values.
   */ 
  private JTextDialog  pEditTextDialog;

  /**
   * The dialog used to view checked-in text action parameter values.
   */ 
  private JTextDialog  pViewTextDialog;
  
}
