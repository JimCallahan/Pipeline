// $Id: JNodeLinksPanel.java,v 1.38 2009/09/24 20:57:33 jlee Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L I N K S   P A N E L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the upstream links associated with the working and checked-in versions of a 
 * node.
 */ 
public  
class JNodeLinksPanel
  extends JBaseNodeDetailPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeLinksPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeLinksPanel
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
      pLinks = new TreeMap<VersionID,TreeMap<String,LinkVersion>>();

      pArrows             = new TreeMap<String,JLinkArrow>();
      pPolicyFields       = new TreeMap<String,JCollectionField>();
      pRelationshipFields = new TreeMap<String,JCollectionField>();
      pFrameOffsetFields  = new TreeMap<String,JIntegerField>();
      pLinkComps          = new TreeMap<String,ArrayList<JComponent[]>>();
    }

    /* initialize the popup menus */ 
    {
      initBasicMenus(true, false); 
      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
        pApplyToolTipText   = "Replace the working links with the selected checked-in files.";
        pUnApplyToolTipText = "There are no unsaved changes to Apply at this time."; 

	JPanel panel = initHeader(true); 
	add(panel);
      }
    
      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
        initNameField(this);
        pNodeNameField.setFocusable(true);     
        pNodeNameField.addKeyListener(this);   
        pNodeNameField.addMouseListener(this); 
      }

      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pLinksBox = vbox;

        vbox.add(UIFactory.createFiller(sTSize+sVSize+sCSize)); 

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  pScroll = scroll;
	  
	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sVSize+sCSize+22, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null, null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  @Override
  public String 
  getTypeName() 
  {
    return "Node Links";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  @Override
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeLinksPanel> panels = master.getNodeLinksPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  @Override
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeLinksPanel> panels = UIMaster.getInstance().getNodeLinksPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  @Override
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }

  /**
   * Set the author and view.
   */ 
  @Override
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    updatePanels();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all panels which share the current update channel.
   */ 
  @Override
  public void 
  updatePanels() 
  {
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param status
   *   The current status for the node being displayed. 
   * 
   * @param links
   *   The check-in links. 
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   NodeStatus status,
   TreeMap<VersionID,TreeMap<String,LinkVersion>> links,
   TreeSet<VersionID> offline
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateNodeStatus(status, links, offline);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   * 
   * @param links
   *   The check-in links.
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions. 
   */
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<VersionID,TreeMap<String,LinkVersion>> links,
   TreeSet<VersionID> offline
  ) 
  {
    super.updateNodeStatus(status, false);

    pOffline = offline;

    pLinks.clear();
    if(links != null) 
      pLinks.putAll(links);

    NodeDetailsLight details = null;
    if(pStatus != null) 
      details = pStatus.getLightDetails();

    NodeMod mod = null;
    if(details != null) 
      mod = details.getWorkingVersion();

    /* links */ 
    {
      pLinksBox.removeAll();
      pArrows.clear();
      pPolicyFields.clear();
      pRelationshipFields.clear();
      pFrameOffsetFields.clear();
      pLinkComps.clear();

      TreeSet<String> lnames = new TreeSet<String>();
      if(mod != null) 
	lnames.addAll(mod.getSourceNames());
      for(VersionID vid : pLinks.keySet()) 
	lnames.addAll(pLinks.get(vid).keySet());

      for(String lname : lnames) {
	LinkMod link = null;
	if(mod != null) 
	  link = mod.getSource(lname);

	boolean isModifiable     = ((mod != null) && !pIsFrozen);
	boolean isLinkModifiable = ((link != null) && !pIsFrozen);

	Component comps[] = createCommonPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  {
	    Dimension size = new Dimension(sVSize, 163);
	    vpanel.setMinimumSize(size);
	    vpanel.setMaximumSize(size);
	    vpanel.setPreferredSize(size);
	  }	  

	  {
	    tpanel.add(Box.createRigidArea(new Dimension(0, 52)));  

	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      {
		JButton btn = new JButton();
		btn.setName("CloseButton");
		
		Dimension size = new Dimension(15, 19);
		btn.setMinimumSize(size);
		btn.setMaximumSize(size);
		btn.setPreferredSize(size);

		if(isLinkModifiable) {
		  btn.setActionCommand("unlink:" + lname);
		  btn.addActionListener(this);
		  btn.setToolTipText(UIFactory.formatToolTip("Unlinks the upstream node."));
		}
		else {
		  btn.setEnabled(false);
		}
		
		hbox.add(btn);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      vpanel.add(hbox);
	    }

	    vpanel.add(Box.createRigidArea(new Dimension(0, 11)));  	      
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);

	      hbox.add(Box.createHorizontalGlue());

	      {
		JLinkArrow arrow = new JLinkArrow(isModifiable); 
		pArrows.put(lname, arrow);

		hbox.add(arrow);
	      }

	      hbox.add(Box.createHorizontalGlue());

	      vpanel.add(hbox);
	    }

	    {
	      String vstr = "-";
	      NodeStatus lstatus = pStatus.getSource(lname);
	      if(lstatus != null) {
		NodeDetailsLight ldetails = lstatus.getLightDetails();
		if(ldetails != null) {
		  NodeVersion lvsn = ldetails.getBaseVersion(); 
		  if(lvsn != null) 
		    vstr = ("v" + lvsn.getVersionID());
		}
	      }
	      
	      UIFactory.createTitledTextField(tpanel, "Revision Number:", sTSize, 
					      vpanel, vstr, sVSize);
	    }
	
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);	    

	    if(isLinkModifiable) {
	      JCollectionField field = 
		UIFactory.createTitledCollectionField
		(tpanel, "Link Policy:", sTSize, 
		 vpanel, LinkPolicy.titles(), sVSize);

	      field.setSelectedIndex(link.getPolicy().ordinal()); 

	      field.addActionListener(this);
	      field.setActionCommand("policy-changed:" + lname);

	      pPolicyFields.put(lname, field);
	    }
	    else {
	      UIFactory.createTitledTextField(tpanel, "Link Policy:", sTSize, 
					      vpanel, "-", sVSize);
	    }
	
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    if(isLinkModifiable) {	      
	      JCollectionField field = 
		UIFactory.createTitledCollectionField
		(tpanel, "Link Relationship:", sTSize, 
		 vpanel, LinkRelationship.titles(), sVSize);

	      field.setSelectedIndex(link.getRelationship().ordinal());

	      field.addActionListener(this);
	      field.setActionCommand("relationship-changed:" + lname);

	      pRelationshipFields.put(lname, field);
	    }
	    else {
	      UIFactory.createTitledTextField(tpanel, "Link Relationship:", sTSize, 
					      vpanel, "-", sVSize);	      
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    if(isLinkModifiable) {
	      JIntegerField field = 
		UIFactory.createTitledIntegerField(tpanel, "Frame Offset:", sTSize, 
						   vpanel, null, sVSize);

	      field.setValue(link.getFrameOffset());
	      field.setEnabled(link.getRelationship() == LinkRelationship.OneToOne);

	      field.addActionListener(this);
	      field.setActionCommand("offset-changed:" + lname);

	      pFrameOffsetFields.put(lname, field);
	    }
	    else {
	      UIFactory.createTitledTextField(tpanel, "Frame Offset:", sTSize, 
					      vpanel, "-", sVSize);		      
	    }

	    if(isLinkModifiable) {
	      updatePolicyColors(lname);
	      updateRelationshipColors(lname);
	      updateOffsetColors(lname);
	    }

	    UIFactory.addVerticalGlue(tpanel, vpanel);
	  }
	  
	  {
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	    /* column header */ 
	    JViewport headerViewport = null;
	    {
	      JPanel panel = new JPanel();
	      
	      panel.setName("TitleValuePanel");
	      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	      
	      {
		JViewport view = new JViewport();
		headerViewport = view; 
		
		{
		  Box hbox = new Box(BoxLayout.X_AXIS);
		  
		  VersionID bvid = null;
		  if((details != null) && (details.getBaseVersion() != null)) 
		    bvid = details.getBaseVersion().getVersionID();
		  
		  ArrayList<VersionID> rvids = new ArrayList<VersionID>(pLinks.keySet());
		  Collections.reverse(rvids);
		  
		  for(VersionID vid : rvids) {
		    JButton btn = new JButton("v" + vid);
		    btn.setName("TableHeaderButton");
		    
		    boolean isOffline = pOffline.contains(vid);

		    if((bvid != null) && bvid.equals(vid)) 
		      btn.setForeground(Color.cyan);
		    else if(isOffline) 
		      btn.setForeground(new Color(0.75f, 0.75f, 0.75f));
		    
		    if(isModifiable && !isOffline) {
		      btn.addActionListener(this);
		      btn.setActionCommand("version-pressed:" + lname + ":" + vid);
		    }
		    
		    btn.setFocusable(false);
		    
		    Dimension size = new Dimension(70, 23);
		    btn.setMinimumSize(size);
		    btn.setMaximumSize(size);
		    btn.setPreferredSize(size);
		    
		    hbox.add(btn);
		  }
		  
		  Dimension size = new Dimension(70*pLinks.size(), 23); 
		  hbox.setMinimumSize(size);
		  hbox.setMaximumSize(size);
		  hbox.setPreferredSize(size);
		  
		  view.setView(hbox);
		}
		
		panel.add(view);
	      }
	      
	      panel.setMinimumSize(new Dimension(70, 29));
	      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
	      panel.setPreferredSize(new Dimension(70, 29));
	      
	      vbox.add(panel);	    
	    }
	    
	    /* table contents */ 
	    {
	      JLinksPanel panel = new JLinksPanel(this, lname, isModifiable); 
	      
	      {
		JScrollPane scroll = 
                  UIFactory.createScrollPane
                  (panel,
                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, 
                   ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 
                   new Dimension(70, 21), null, null); 
	      
		{
		  AdjustLinkage linkage = 
		    new AdjustLinkage(scroll.getViewport(), headerViewport);
		  scroll.getHorizontalScrollBar().addAdjustmentListener(linkage);
		}
		
		vbox.add(scroll);
	      }
	    }

	    Box pbox = (Box) comps[2];
	    pbox.add(vbox);
	  }

	  JDrawer drawer = new JDrawer(lname + ":", (JComponent) comps[2], true);
	  drawer.setToolTipText(UIFactory.formatToolTip("Upstream link node name."));
	  pLinksBox.add(drawer);
	}
      }

      pLinksBox.add(UIFactory.createFiller(sTSize+sVSize+sCSize));

      pLinksBox.revalidate();
    }
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
    Component comps[] = UIFactory.createTitledPanels();

    {
      JPanel panel = (JPanel) comps[0];
      panel.setFocusable(true);
      panel.addKeyListener(this);
      panel.addMouseListener(this); 
    }

    return comps;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  @Override
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.startsWith("policy-changed:")) 
      doPolicyChanged(cmd.substring(15));
    else if(cmd.startsWith("relationship-changed:")) 
      doRelationshipChanged(cmd.substring(21));
    else if(cmd.startsWith("offset-changed:")) 
      doOffsetChanged(cmd.substring(15));
    
    else if(cmd.startsWith("version-pressed:")) {
      String comps[] = cmd.split(":");
      doVersionPressed(comps[1], new VersionID(comps[2]));
    }
    else if(cmd.startsWith("link-checked:")) 
      doLinkChecked((JLinkCheckBox) e.getSource(), cmd.substring(13));

    else if(cmd.startsWith("unlink:")) 
      doUnlink(cmd.substring(7));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace working files with the selected checked-in files.
   */ 
  @Override
  public void 
  doApply()
  {
    super.doApply();

    if(pIsFrozen) 
      return;

    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeMod mod = pStatus.getLightDetails().getWorkingVersion(); 
      if(mod != null) {
        TreeMap<String,LinkCommon> links = new TreeMap<String,LinkCommon>();
        
        /* collect the selected checked-in link properties */ 
        for(String name : pLinkComps.keySet()) {
          ArrayList<JComponent[]> vcomps = pLinkComps.get(name);
          for(JComponent comps[] : vcomps) {
            if((comps != null) && (comps[0] instanceof JLinkCheckBox)) {
              JLinkCheckBox check = (JLinkCheckBox) comps[0];
              if(check.isSelected()) 
                links.put(name, check.getLink());
            }
          }
        }
        
        /* collect the modified link properties (not already set) */ 
        for(String name : mod.getSourceNames()) {
          if(!links.containsKey(name)) {
            JCollectionField pfield = pPolicyFields.get(name);
            JCollectionField rfield = pRelationshipFields.get(name);
            JIntegerField ofield = pFrameOffsetFields.get(name);
            if((pfield != null) && (rfield != null) && (ofield != null)) {
              LinkPolicy policy = LinkPolicy.values()[pfield.getSelectedIndex()];
              LinkRelationship rel = LinkRelationship.values()[rfield.getSelectedIndex()];
              Integer offset = ofield.getValue();
              
              LinkMod link = new LinkMod(name, policy, rel, offset);
              links.put(name, link);
            }
          }
        }
	
        ApplyTask task = new ApplyTask(links);
        task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The link policy has been changed.
   */ 
  private void 
  doPolicyChanged
  (
   String name
  )
  {
    if(updatePolicyColors(name)) 
      unsavedChange("Link Policy: " + name); 
  }
  
  /**
   * The update the color of the link policy field.
   */ 
  private boolean 
  updatePolicyColors
  (
   String name
  )
  {
    JCollectionField field = pPolicyFields.get(name);
    LinkPolicy policy = LinkPolicy.values()[field.getSelectedIndex()];

    boolean isModified = true;
    if(pStatus.hasLightDetails()) {
      NodeVersion vsn = pStatus.getLightDetails().getBaseVersion();
      if(vsn != null) {
        LinkVersion link = vsn.getSource(name);
        if((link != null) && link.getPolicy().equals(policy))
          isModified = false;
      }
    }

    field.setForeground(isModified ? Color.cyan : Color.white);
    
    return isModified;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The link relationship has been changed.
   */ 
  private void 
  doRelationshipChanged
  (
   String name
  ) 
  {
    JCollectionField rfield = pRelationshipFields.get(name);
    LinkRelationship rel = LinkRelationship.values()[rfield.getSelectedIndex()];

    JIntegerField ofield = pFrameOffsetFields.get(name);

    switch(rel) {
    case OneToOne:
      {
	ofield.setEnabled(true);
	Integer offset = ofield.getValue();
	if(offset == null) 
	  ofield.setValue(0);
      }
      break;

    default:
      ofield.setValue(null);
      ofield.setEnabled(false);
    }
    
    if(updateRelationshipColors(name)) 
      unsavedChange("Link Relationship: " + name);

    if(updateOffsetColors(name)) 
      unsavedChange("Link Frame Offset: " + name); 
  }
  
  /**
   * The update the color of the link relationship field.
   */ 
  private boolean 
  updateRelationshipColors
  (
   String name
  ) 
  {
    JCollectionField field = pRelationshipFields.get(name);
    LinkRelationship rel = LinkRelationship.values()[field.getSelectedIndex()];

    boolean isModified = true;
    if(pStatus.hasLightDetails()) {
      NodeVersion vsn = pStatus.getLightDetails().getBaseVersion();
      if(vsn != null) {
        LinkVersion link = vsn.getSource(name);
        if((link != null) && link.getRelationship().equals(rel))
          isModified = false;
      }
    }

    field.setForeground(isModified ? Color.cyan : Color.white);

    return isModified;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The link frame offset has been changed.
   */ 
  private void 
  doOffsetChanged
  (
   String name
  ) 
  {
    if(updateOffsetColors(name)) 
      unsavedChange("Link Frame Offset: " + name);
  }
  
  /**
   * The update the color of the link relationship field.
   */ 
  private boolean 
  updateOffsetColors
  (
   String name
  ) 
  {
    JIntegerField field = pFrameOffsetFields.get(name);
    Integer offset = field.getValue();

    boolean isModified = true;
    if(pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();
      NodeMod mod = details.getWorkingVersion();
      NodeVersion vsn = details.getBaseVersion();
      if((vsn != null) && (mod != null)) {
        LinkVersion link = vsn.getSource(name);
        if(link != null) {
          Integer loffset  = link.getFrameOffset();
          if(((offset == null) && (loffset == null)) || 
             ((offset != null) && offset.equals(loffset)))
            isModified = false;
        }
      }
    }

    field.setForeground(isModified ? Color.cyan : Color.white);

    return isModified;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * One of the checked-in version buttons was pressed.
   */ 
  private void 
  doVersionPressed
  (
   String name, 
   VersionID vid
  ) 
  {
    ArrayList<JComponent[]> vcomps = pLinkComps.get(name);
    
    int wk;
    for(wk=0; wk<vcomps.size(); wk++) {
      JComponent comps[] = vcomps.get(wk);
      if(comps != null) {
	if(comps[0] instanceof JLinkCheckBox) {
	  JLinkCheckBox check = (JLinkCheckBox) comps[0];
	  if(check.getVersionID().equals(vid)) {
	    check.setSelected(!check.isSelected());
	    doLinkChecked(check, name);
	    return;
	  }
	}
	else if(comps[0] instanceof JLinkBar) {
	  JLinkBar bar = (JLinkBar) comps[0];
	  if(bar.getVersionID().equals(vid)) {
	    for(wk++; wk<vcomps.size(); wk++) {
	      comps = vcomps.get(wk);
	      if(comps[0] instanceof JLinkCheckBox) {
		JLinkCheckBox check = (JLinkCheckBox) comps[0];
		check.setSelected(!check.isSelected());
		doLinkChecked(check, name);
		return;
	      }
	    }
	  }
	}    
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * One of the checked-in version check boxes was pressed.
   */ 
  private void 
  doLinkChecked
  (
   JLinkCheckBox check, 
   String name
  ) 
  {
    boolean selected = check.isSelected();
    
    ArrayList<JComponent[]> vcomps = pLinkComps.get(name);
      
    /* deselect the entire row */ 
    int idx = -1;
    int wk;
    for(wk=0; wk<vcomps.size(); wk++) {
      JComponent comps[] = vcomps.get(wk);
      if(comps != null) {
	int ck; 
	for(ck=0; ck<comps.length; ck++) {
	  if(comps[ck] instanceof JLinkCheckBox) {
	    JLinkCheckBox cb = (JLinkCheckBox) comps[ck];
	    cb.setSelected(false);
	    if(cb == check) 
	      idx = wk;
	  }
	  else if(comps[ck] instanceof JLinkBar) {
	    JLinkBar bar = (JLinkBar) comps[ck];
	    bar.setSelected(false);
	  }
	  else if(comps[ck] instanceof JTextField) {
	    JTextField field = (JTextField) comps[ck];
	    field.setForeground(Color.white);
	  }
	}
      }
    }
    assert(idx >= 0);

    /* reselect the checkbox and associated components */ 
    if(selected) {
      check.setSelected(true);

      for(wk=idx; wk>=0; wk--) {
	JComponent comps[] = vcomps.get(wk);
	if((comps != null) && 
	   ((comps[0] instanceof JLinkBar) || (wk == idx))) {
	  int ck; 
	  for(ck=0; ck<comps.length; ck++) {
	    if(comps[ck] instanceof JLinkBar) {
	      JLinkBar bar = (JLinkBar) comps[ck];
	      bar.setSelected(true);
	    }
	    else if(comps[ck] instanceof JTextField) {
	      JTextField field = (JTextField) comps[ck];
	      field.setForeground(Color.yellow);
	    }
	  }
	}
	else {
	  break;
	}
      }
    }

    /* update the arrow selection */ 
    JLinkArrow arrow = pArrows.get(name);
    arrow.setSelected(selected);

    unsavedChange("Revert Links: " + name);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Unlink the given upstream node.
   */ 
  private void 
  doUnlink
  (
   String name
  )
  {
    UnlinkTask task = new UnlinkTask(name);
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * A label used to represent to horizontal bars for links which are not novel.
   */ 
  private 
  class JLinkBar
    extends JLabel
  {
    public 
    JLinkBar
    (
     boolean isExtended, 
     VersionID vid     
    ) 
    {
      super();

      pIsExtended = isExtended;
      pVersionID  = vid;

      setSelected(false);

      Dimension size = new Dimension(70, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public boolean
    isExtended()
    {
      return pIsExtended;
    }

    public VersionID 
    getVersionID() 
    {
      return pVersionID;
    }    

    public boolean
    isSelected()
    {
      return pIsSelected;
    }

    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;
      if(pIsSelected) 
	setIcon(pIsExtended ? sFileBarExtBothIconSelected : sFileBarExtRightIconSelected);
      else
	setIcon(pIsExtended ? sFileBarExtBothIcon : sFileBarExtRightIcon);
    }

    private static final long serialVersionUID = -8182004303495407765L;

    private VersionID  pVersionID; 
    protected boolean  pIsExtended;
    protected boolean  pIsSelected; 
  }
  
  private 
  class JExtraLinkBar
    extends JLinkBar
  {
    public 
    JExtraLinkBar
    (
     boolean isExtended, 
     VersionID vid
    ) 
    {
      super(isExtended, vid);
    }

    @Override
    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;
      if(pIsSelected) 
	setIcon(pIsExtended ? sLinkBarExtendIconSelected : sLinkBarIconSelected);
      else
	setIcon(pIsExtended ? sLinkBarExtendIcon : sLinkBarIcon);
    }

    private static final long serialVersionUID = -8692287670599793208L;
  }

  /**
   * A label used to indicate links which will be replaced by the apply operation.
   */ 
  private 
  class JLinkArrow
    extends JLabel
  {
    public 
    JLinkArrow
    (
     boolean isActive
    ) 
    {
      super();

      pIsActive = isActive;
      setSelected(false);

      Dimension size = new Dimension(12, 22);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }
    
    public boolean
    isSelected()
    {
      return pIsSelected;
    }

    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;

      if(pIsActive) {
	if(pIsSelected) 
	  setIcon(sFileArrowIconSelected);
	else 
	  setIcon(sFileArrowIcon);
      }
      else {
	setIcon(sFileArrowIconDisabled);
      }
    }

    private static final long serialVersionUID = 1437991126704684880L;

    private boolean  pIsSelected; 
    private boolean  pIsActive;
  }

  /**
   * A JCheckBox used to select a checked-in link.
   */ 
  private 
  class JLinkCheckBox
    extends JCheckBox
  {
    public 
    JLinkCheckBox
    (
     LinkVersion link, 
     VersionID vid
    ) 
    {
      super();

      pLink      = link;
      pVersionID = vid; 

      setName("FileCheck");
      setSelected(false);
      
      Dimension size = new Dimension(12, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public VersionID 
    getVersionID() 
    {
      return pVersionID;
    }    

    public LinkVersion
    getLink() 
    {
      return pLink; 
    }

    private static final long serialVersionUID = 1623467331258706403L;

    private LinkVersion  pLink; 
    private VersionID    pVersionID; 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A panel which displays the current state and revision history of an upstream link.
   */ 
  private 
  class JLinksPanel 
    extends JPanel 
    implements Scrollable
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
  
    /**
     * Construct a new panel.
     * 
     * @param parent
     *   The parent panel.
     * 
     * @param name
     *   The fully resolved name of the upstream node.
     * 
     * @param isModifiable
     *   Whether the link properties can be edited.
     */ 
    public 
    JLinksPanel
    (
     JNodeLinksPanel parent,
     String lname, 
     boolean isModifiable
    ) 
    {
      super();
      
      ArrayList<VersionID> rvids = new ArrayList<VersionID>(pLinks.keySet());
      Collections.reverse(rvids);

      ArrayList<LinkVersion> links = new ArrayList<LinkVersion>();
      for(VersionID vid : rvids) 
	links.add(pLinks.get(vid).get(lname));

      ArrayList<JComponent[]> vcomps = new ArrayList<JComponent[]>(); 
      pLinkComps.put(lname, vcomps);

      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createRigidArea(new Dimension(27, 0)));

	  int wk = 0;
	  for(VersionID vid : rvids) {
	    JComponent comps[] = null;
	    LinkVersion link = pLinks.get(vid).get(lname);
	    if(link != null) {
	      comps = new JComponent[5];

	      boolean isOffline = pOffline.contains(vid);

	      { 
		LinkVersion rlink = null;
		if((wk+1) < links.size()) 
		  rlink = links.get(wk+1);
		
		LinkVersion llink = null;
		if(wk > 0) 
		  llink = links.get(wk-1);
		
		if(link.equals(rlink)) {
		  boolean isExtended = link.equals(llink); 
		  if(!isExtended) 
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));	

		  JLinkBar bar = new JLinkBar(isExtended, vid);
		  comps[0] = bar; 

		  hbox.add(bar); 
		}
		else {
		  if(!link.equals(llink)) 
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));	

		  JLinkCheckBox check = new JLinkCheckBox(link, vid);
		  comps[0] = check;
		  
		  if(isModifiable && !isOffline) {
		    check.addActionListener(parent);
		    check.setActionCommand("link-checked:" + lname);
		  }
		  else {
		    check.setEnabled(false);
		  }

		  hbox.add(check);
		  hbox.add(Box.createRigidArea(new Dimension(56, 0)));
		}
	      }
	    }
	    else {
	      hbox.add(Box.createRigidArea(new Dimension(70, 0)));
	    }
	  
	    vcomps.add(comps); 
	    wk++;
	  }

	  add(hbox);
	}

	add(Box.createRigidArea(new Dimension(0, 3)));
	    
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createRigidArea(new Dimension(1, 0)));

	  int wk = 0;
	  for(VersionID vid : rvids) {
	    LinkVersion link = pLinks.get(vid).get(lname);
	    if(link != null) {
	      JComponent comps[] = vcomps.get(wk);
	      
	      if(comps[0] instanceof JLinkCheckBox) {	      
		Box vbox = new Box(BoxLayout.Y_AXIS);
	      
		{
		  JTextField field = 
		    UIFactory.createTextField("v" + link.getVersionID(), 67, JLabel.CENTER);
		  comps[1] = field;

		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		
		{
		  String text = null;
		  switch(link.getPolicy()) {
		  case Association: 
		    text = "Asc";
		    break;

		  case Reference: 
		    text = "Ref";
		    break;
		    
		  case Dependency:
		    text = "Dep";
		  }
		  
		  JTextField field = 
		    UIFactory.createTextField(text, 67, JLabel.CENTER);
		  comps[2] = field;
		  
		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		
		{
		  JTextField field = 
		    UIFactory.createTextField(link.getRelationship().toTitle(), 
					      67, JLabel.CENTER);
		  comps[3] = field;
		
		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	      
		{
		  String text = "-";
		  Integer offset = link.getFrameOffset();
		  if(offset != null) 
		    text = offset.toString();
		  
		  JTextField field = 
		  UIFactory.createTextField(text, 67, JLabel.CENTER);
		  comps[4] = field;
		  
		  vbox.add(field);
		}
		
		hbox.add(vbox);

		if((wk+1) < vcomps.size())
		  hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	      }
	      else if(comps[0] instanceof JLinkBar) {
		JLinkBar vbar = (JLinkBar) comps[0];
		
		Box vbox = new Box(BoxLayout.Y_AXIS);

		boolean isExtended = vbar.isExtended();
		int bk;
		for(bk=0; bk<4; bk++) {
		  JExtraLinkBar bar = new JExtraLinkBar(isExtended, vid);
		  comps[bk+1] = bar; 

		  vbox.add(bar);

		  if(bk<3) 
		    vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		}

		hbox.add(vbox);
	      } 
	    }
	    else {
	      hbox.add(Box.createRigidArea(new Dimension(70, 84)));
	    }
	  
	    wk++;
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(3, 0)));

	  add(hbox);
	}
	  
	Dimension size = new Dimension(70*pLinks.size(), 106);
	setMinimumSize(size);
	setMaximumSize(size);
	setPreferredSize(size);
	
	pViewportSize = new Dimension(70, 22);
      }
    }
  
    /*--------------------------------------------------------------------------------------*/
    /*   S C R O L L A B L E                                                                */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Returns the preferred size of the viewport for a view component.
     */ 
    public Dimension 	
    getPreferredScrollableViewportSize()
    {
      return pViewportSize;
    }
    
    /**
     * Components that display logical rows or columns should compute the scroll increment 
     * that will completely expose one block of rows or columns, depending on the value of 
     * orientation.
     */ 
    public int 	
    getScrollableBlockIncrement
    (
     Rectangle visibleRect, 
     int orientation, 
     int direction
    )
    {
      return getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment 
     * that will completely expose one new row or column, depending on the value of 
     * orientation.
     */ 
    public int 	
    getScrollableUnitIncrement
    (
     Rectangle visibleRect, 
     int orientation, 
     int direction
    )
    {
      switch(orientation) {
      case SwingConstants.VERTICAL:
	return 22;

      case SwingConstants.HORIZONTAL:
	return 70;

      default:
	assert(false);
	return 0;
      }
    }

    /**
     * Return true if a viewport should always force the height of this Scrollable to match 
     * the height of the viewport.
     */
    public boolean 
    getScrollableTracksViewportHeight()
    {
      return true;
    }

    /**
     * Return true if a viewport should always force the width of this Scrollable to match 
     * the width of the viewport.
     */ 
    public boolean 	
    getScrollableTracksViewportWidth()
    {
      return false;
    }


    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = 3431758112709330334L;


    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The preferred viewport dimensions.
     */ 
    private Dimension pViewportSize;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Apply the changes to the link properties.
   */ 
  private
  class ApplyTask
    extends Thread
  {
    public 
    ApplyTask
    (
     TreeMap<String,LinkCommon> links
    ) 
    {
      super("JNodeLinksPanel:ApplyTask");

      pLinksLocal = links;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Modifying Link Properties...")) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  for(String sname : pLinksLocal.keySet()) {
	    LinkCommon link = pLinksLocal.get(sname);
	    client.link(pAuthor, pView, pStatus.getName(), sname, 
			link.getPolicy(), link.getRelationship(), link.getFrameOffset());
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private  TreeMap<String,LinkCommon>  pLinksLocal; 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Unlink the given upstream node.
   */ 
  private
  class UnlinkTask
    extends Thread
  {
    public 
    UnlinkTask
    (
     String name
    ) 
    {
      super("JNodeLinksPanel:UnlinkTask");

      pSourceName = name;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Unlinking Node...")) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  client.unlink(pAuthor, pView, pStatus.getName(), pSourceName);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private String pSourceName; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7734595313030641540L;

  private static final int  sTSize = 120;
  private static final int  sVSize = 156;
  private static final int  sCSize = 208;

  
  private static final Icon sLinkBarIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarIcon.png"));

  private static final Icon sLinkBarExtendIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarExtendIcon.png"));

  private static final Icon sLinkBarIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarIconSelected.png"));

  private static final Icon sLinkBarExtendIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarExtendIconSelected.png"));


  private static final Icon sFileBarExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtRightIcon.png"));

  private static final Icon sFileBarExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtBothIcon.png"));

  private static final Icon sFileBarExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtRightIconSelected.png"));

  private static final Icon sFileBarExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtBothIconSelected.png"));


  private static final Icon sFileArrowIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIcon.png"));

  private static final Icon sFileArrowIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIconSelected.png"));

  private static final Icon sFileArrowIconDisabled = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIconDisabled.png"));


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The upstream links of all checked-in versions of the given node.
   */ 
  private TreeMap<VersionID,TreeMap<String,LinkVersion>>  pLinks; 

  /**
   * The revision numbers of the offline checked-in versions.
   */ 
  private TreeSet<VersionID>  pOffline; 

  /**
   * The toolset used to build the editor menu.
   */ 
  private String  pEditorMenuToolset;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu items.
   */ 
  private JMenuItem  pApplyItem;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * An icon which indicates whether the working version is frozen.
   */
  private boolean  pIsFrozen; 
  private JLabel   pFrozenLabel;

  /**
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;

  /**
   * The link components box.
   */ 
  private Box  pLinksBox;

  /**
   * The link arrows indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JLinkArrow>  pArrows; 

  /**
   * The link policy fields indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JCollectionField>  pPolicyFields; 

  /**
   * The link relationship fields indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JCollectionField>  pRelationshipFields; 

  /**
   * The frame offset indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JIntegerField>  pFrameOffsetFields; 

  /**
   * The link novelty UI components indexed by upstream node name. <P> 
   * 
   * The value of an entry in the ArrayList be <CODE>null</CODE> if there is no link
   * for the checked-in version.  The JComponents array contains the components vertically
   * from top to bottom of the table column under a particular checked-in version.
   */ 
  private TreeMap<String,ArrayList<JComponent[]>>  pLinkComps;

  /**
   * The scroll panel containing the link components.
   */ 
  private JScrollPane  pScroll; 

}
