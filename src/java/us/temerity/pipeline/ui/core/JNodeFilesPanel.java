// $Id: JNodeFilesPanel.java,v 1.24 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   F I L E S   P A N E L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the per-file {@link FileState FileState} and {@link QueueState QueueState} for 
 * the files associated with a node. <P> 
 * 
 * The per-file revision history is also displayed and a mechanism is provided for selecting
 * specific file revisions to replace the current working copies of these files.
 */ 
public  
class JNodeFilesPanel
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
  JNodeFilesPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeFilesPanel
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
      pSelected = new TreeSet<Integer>();
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;

      pWorkingPopup   = new JPopupMenu();  
      pFrozenPopup    = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu();  

      {
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
	item.setActionCommand("apply");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();
      }

      pEditItems            = new JMenuItem[3];
      pEditWithDefaultItems = new JMenuItem[3];
      pEditWithMenus        = new JMenu[3];

      JPopupMenu menus[] = { pWorkingPopup, pFrozenPopup, pCheckedInPopup };
      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem((wk > 0) ? "View" : "Edit");
	pEditItems[wk] = item;
	item.setActionCommand("edit");
	item.addActionListener(this);
	menus[wk].add(item);
	
	pEditWithMenus[wk] = new JMenu((wk > 0) ? "View With" : "Edit With");
	menus[wk].add(pEditWithMenus[wk]);

	item = new JMenuItem((wk > 0) ? "View With Default" : "Edit With Default");
	pEditWithDefaultItems[wk] = item;
	item.setActionCommand("edit-with-default");
	item.addActionListener(this);
	menus[wk].add(item);
      }
      
      {
	pWorkingPopup.addSeparator();
	
	item = new JMenuItem("Queue Jobs");
	pQueueJobsItem = item;
	item.setActionCommand("queue-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Queue Jobs Special...");
	pQueueJobsSpecialItem = item;
	item.setActionCommand("queue-jobs-special");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Pause Jobs");
	pPauseJobsItem = item;
	item.setActionCommand("pause-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      
	item = new JMenuItem("Resume Jobs");
	pResumeJobsItem = item;
	item.setActionCommand("resume-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Preempt Jobs");
	pPreemptJobsItem = item;
	item.setActionCommand("preempt-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	item = new JMenuItem("Kill Jobs");
	pKillJobsItem = item;
	item.setActionCommand("kill-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();

	item = new JMenuItem("Remove Files");
	pRemoveFilesItem = item;
	item.setActionCommand("remove-files");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      }
      
      {
	pCheckedInPopup.addSeparator();
	
	pCompareWithMenu = new JMenu("Compare With");
	pCheckedInPopup.add(pCompareWithMenu);
      }	

      updateMenuToolTips();
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
	  JLabel label = new JLabel();
	  pHeaderIcon = label;
	  
	  label.addMouseListener(this); 

	  panel.add(label);	  
	}
	
	panel.add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());
      
	{
	  JLabel label = new JLabel(sFrozenIcon);
	  pFrozenLabel = label;
	  panel.add(label);	  
	}

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

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Replace the working area files with the selected checked-in files."));

	  panel.add(btn);
	} 

	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
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
	pFileSeqBox = vbox;
	
	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
	  scroll.getVerticalScrollBar().setUnitIncrement(23);

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sSize+22, 120);
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
  public String 
  getTypeName() 
  {
    return "Node Files";
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
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeFilesPanel> panels = master.getNodeFilesPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
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
    PanelGroup<JNodeFilesPanel> panels = UIMaster.getInstance().getNodeFilesPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the per-file status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param status
   *   The current node status.
   * 
   * @param novelty
   *   The per-file novelty flags.
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty, 
   TreeSet<VersionID> offline
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, novelty, offline);
  }

  /**
   * Update the UI components to reflect the current per-file status.
   * 
   * @param status
   *   The current node status.
   * 
   * @param novelty
   *   The per-file novelty flags.
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty,
   TreeSet<VersionID> offline
  ) 
  {
    updatePrivileges();

    pStatus  = status;
    pNovelty = novelty; 
    pOffline = offline;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    /* header */ 
    {
      {
	String name = "Blank-Normal";
	if(pStatus != null) {
	  if(details != null) {
	    if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
	      VersionID wvid = details.getWorkingVersion().getWorkingID();
	      VersionID lvid = details.getLatestVersion().getVersionID();
	      switch(wvid.compareLevel(lvid)) {
	      case Major:
		name = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
		break;
		
	      case Minor:
		name = ("NeedsCheckOut-" + details.getOverallQueueState());
		break;
		
	      case Micro:
		name = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
	      }
	    }
	    else {
	      name = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
	    }
	    
	    NodeMod mod = details.getWorkingVersion();
	    if((mod != null) && mod.isFrozen()) 
	      name = (name + "-Frozen-Normal");
	    else 
	      name = (name + "-Normal");
	  }
	  
	  pHeaderLabel.setText(pStatus.toString());
	  pNodeNameField.setText(pStatus.getName());
	}
	else {
	  pHeaderLabel.setText(null);
	  pNodeNameField.setText(null);
	}

	try {
	  pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon(name));
	}
	catch(IOException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Tex, LogMgr.Level.Severe,
	     "Internal Error:\n" + 
	     "  " + ex.getMessage());
	  LogMgr.getInstance().flush();
	  System.exit(1);
	} 
      }
    }

    /* frozen node? */
    {
      pIsFrozen = false;
      if((details != null) && (details.getWorkingVersion() != null)) {
	NodeMod mod = details.getWorkingVersion();
	pIsFrozen = mod.isFrozen();
	pFrozenLabel.setIcon(mod.isLocked() ? sLockedIcon : sFrozenIcon);
      }

      pFrozenLabel.setVisible(pIsFrozen);
      pApplyButton.setVisible(!pIsFrozen);
    }

    /* files */ 
    {
      pFileSeqBox.removeAll();

      pFileArrows     = new TreeMap<String,JFileArrow>();
      pFilePanels     = new TreeMap<FileSeq,TreeMap<Integer,JFilePanel>>();
      pNoveltyComps   = new TreeMap<String,TreeMap<String,ArrayList<JComponent>>>();
      pVersionBoxes   = new TreeMap<String,TreeMap<String,TreeMap<String,JFileCheckBox>>>();

      if((pNovelty != null) && (details != null)) {
	NodeMod mod     = details.getWorkingVersion(); 
	NodeVersion vsn = details.getLatestVersion();

	NodeCommon com = null;
	if(mod != null) 
	  com = mod;
	else if(vsn != null)
	  com = vsn;
	else
	  assert(false);

	/* get the primary and unique secondary file sequences */ 
	FileSeq primary = com.getPrimarySequence();
	TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
	{
	  secondary.addAll(com.getSecondarySequences());

	  TreeSet<FileSeq> unique = new TreeSet<FileSeq>();
	  for(TreeMap<FileSeq,boolean[]> table : pNovelty.values()) 
	    unique.addAll(table.keySet());
	  
	  for(FileSeq ufseq : unique) {
	    boolean found = false;
	    
	    if(ufseq.similarTo(primary)) 
	      found = true;
	    else {	    
	      for(FileSeq fseq : secondary) { 
		if(ufseq.similarTo(fseq)) {
		  found = true;
		  break;
		}
	      }
	    }
	    
	    if(!found) 
	      secondary.add(ufseq);
	  }
	}

	/* add the file sequence UI components */ 
	addFileSeqComponents(primary, secondary.isEmpty());
	if(!secondary.isEmpty()) {
	  FileSeq last = secondary.last();
	  for(FileSeq fseq : secondary) 
	    addFileSeqComponents(fseq, last.equals(fseq));
	}
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sSize, 7));
	
	pFileSeqBox.add(spanel);
      }
    }
      
    pFileSeqBox.revalidate();

    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);
  }


  /**
   * Add the UI components for the given file sequence to the panel.
   */ 
  private void 
  addFileSeqComponents
  (
   FileSeq fseq, 
   boolean isLast
  ) 
  { 
    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    boolean isFrozen = false;
    if(details != null) {
      NodeMod mod = details.getWorkingVersion();
      if((mod != null) && mod.isFrozen()) 
	isFrozen = true; 
    }

    /* collate the row information */ 
    TreeMap<Integer,FileSeq> order = new TreeMap<Integer,FileSeq>();
    TreeMap<FileSeq,Integer> singles = new TreeMap<FileSeq,Integer>();	 
    TreeSet<FileSeq> enabled = new TreeSet<FileSeq>();
    TreeMap<FileSeq,FileState>  fstates = new TreeMap<FileSeq,FileState>();
    TreeMap<FileSeq,QueueState> qstates = new TreeMap<FileSeq,QueueState>();
    TreeMap<FileSeq,Boolean[]> novel = new TreeMap<FileSeq,Boolean[]>();
    {
      if(details != null) {
	{	  
	  FileState[]  fs = details.getFileState(fseq);
	  QueueState[] qs = details.getQueueState();
	  if((fs != null) && (qs != null)) {
	    assert(fs.length == fseq.numFrames());
	    assert(qs.length == fseq.numFrames());
	    
	    int wk;
	    for(wk=0; wk<fs.length; wk++) {
	      FileSeq sfseq = new FileSeq(fseq, wk);
	      singles.put(sfseq, wk);
	      
	      fstates.put(sfseq, fs[wk]);
	      qstates.put(sfseq, qs[wk]);

	      if(fs[wk] != FileState.CheckedIn) 
		enabled.add(sfseq);
	    }
	  }
	}
	
	{
	  ArrayList<VersionID> vids = new ArrayList<VersionID>(pNovelty.keySet());
	  Collections.reverse(vids);
	  
	  int idx = 0;
	  for(VersionID vid : vids) {
	    TreeMap<FileSeq,boolean[]> table = pNovelty.get(vid);
	    for(FileSeq nfseq : table.keySet()) {
	      if(fseq.similarTo(nfseq)) {
		boolean[] flags = table.get(nfseq);
		
		int wk;
		for(wk=0; wk<flags.length; wk++) {
		  FileSeq sfseq = new FileSeq(nfseq, wk);
		  if(!singles.containsKey(sfseq)) 
		    singles.put(sfseq, null);

		  Boolean[] rflags = novel.get(sfseq);
		  if(rflags == null) {
		    rflags = new Boolean[pNovelty.size()];
		    novel.put(sfseq, rflags);
		  }
		  
		  rflags[idx] = new Boolean(flags[wk]);
		}

		break;
	      }
	    }

	    idx++;
	  }
	}
      }

      for(FileSeq sfseq : singles.keySet()) {
	int frame = -1;
	if(sfseq.hasFrameNumbers()) 
	  frame = sfseq.getFrameRange().getStart();
	
	order.put(frame, sfseq);
      }
    }
      
    /* the file sequence component */ 
    {
      Box left = new Box(BoxLayout.X_AXIS);
      {
	left.add(Box.createRigidArea(new Dimension(3, 0)));
	
	/* file state/name labels */ 
	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  vbox.setAlignmentY(0.0f);
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    Box lbox = new Box(BoxLayout.X_AXIS);

	    lbox.add(Box.createRigidArea(new Dimension(0, 27))); 

	    {
	      JFileSeqLabel label = new JFileSeqLabel(fseq);
	      
	      if((details != null) && (details.getWorkingVersion() != null) && 
  	       details.getWorkingVersion().getSequences().contains(fseq))
		label.addMouseListener(this);
	      
	      lbox.add(label);
	    }
	    
	    vbox.add(lbox);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    TreeMap<Integer,JFilePanel> panels = new TreeMap<Integer,JFilePanel>();
	    pFilePanels.put(fseq, panels); 
	    
	    for(FileSeq sfseq : order.values()) {
	      Integer idx = singles.get(sfseq);

	      FileState fstate  = fstates.get(sfseq);
	      QueueState qstate = qstates.get(sfseq);
		
	      boolean isActive = enabled.contains(sfseq);

	      JFilePanel fpanel = 
		new JFilePanel(sfseq.toString(), fseq, idx, 
			       fstate, qstate, isFrozen, this);

	      if(idx != null) 
		panels.put(idx, fpanel); 
	      
	      vbox.add(fpanel);
	      vbox.add(Box.createRigidArea(new Dimension(0, 1)));
	    }
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 18)));
	  
	  left.add(vbox);
	}

	left.add(Box.createRigidArea(new Dimension(3, 0)));
      }

      /* file version novelty table */ 
      Box right = new Box(BoxLayout.X_AXIS);
      {
	right.add(Box.createRigidArea(new Dimension(5, 0)));

	/* apply file arrows */ 
	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 35)));
	  
	  for(FileSeq sfseq : order.values()) {
	    boolean hasNovel = false;
	    {
	      Boolean[] flags = novel.get(sfseq);
	      if(flags != null) {
		int wk;
		for(wk=0; wk<flags.length; wk++) {
		  if((flags[wk] != null) && flags[wk]) {
		    hasNovel = true;
		    break;
		  }
		}
	      }
	    }

	    boolean hasWorking = enabled.contains(sfseq);

	    JFileArrow arrow = new JFileArrow(hasNovel && hasWorking && !isFrozen);
	    pFileArrows.put(sfseq.toString(), arrow);

	    vbox.add(arrow);
	  }
	  
	  vbox.add(Box.createVerticalGlue());

	  right.add(vbox);
	}
	  
	right.add(Box.createRigidArea(new Dimension(5, 0)));

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
		
		ArrayList<VersionID> vids = new ArrayList<VersionID>(pNovelty.keySet());
		Collections.reverse(vids);
		  
		VersionID bvid = null;
		if(details.getBaseVersion() != null) 
		  bvid = details.getBaseVersion().getVersionID();
		  
		for(VersionID vid : vids) {
		  JButton btn = new JButton("v" + vid);
		  btn.setName("TableHeaderButton");
		    
		  boolean isOffline = pOffline.contains(vid);

		  if((bvid != null) && bvid.equals(vid)) 
		    btn.setForeground(Color.cyan);
		  else if(isOffline) 
		    btn.setForeground(new Color(0.75f, 0.75f, 0.75f));
		    
		  if(!isOffline) {
		    btn.addActionListener(this);
		    btn.setActionCommand("version-pressed:" + fseq + ":" + vid);
		  }
		    
		  btn.setFocusable(false);
		    
		  Dimension size = new Dimension(70, 23);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
		    
		  hbox.add(btn);
		}
		  
		Dimension size = new Dimension(70*pNovelty.size(), 23); 
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
	    JFileSeqPanel panel = 
	      new JFileSeqPanel(this, fseq, order.values(), 
				enabled, novel, pNovelty.size());
	      
	    {
	      JScrollPane scroll = new JScrollPane(panel);
		
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
	      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		
	      Dimension size = new Dimension(70, 21);
	      scroll.setMinimumSize(size);
		
	      {
		AdjustLinkage linkage = 
		  new AdjustLinkage(scroll.getViewport(), headerViewport);
		scroll.getHorizontalScrollBar().addAdjustmentListener(linkage);
	      }
		
	      vbox.add(scroll);
	    }
	  }

	  right.add(vbox);
	}
	  
	right.add(Box.createRigidArea(new Dimension(0, 3)));
      }
     
      
      JHorzSplitPanel split = new JHorzSplitPanel(left, right);

      split.setResizeWeight(0.0);

      Dimension size = split.getPreferredSize();
      split.setMinimumSize(new Dimension(200, size.height));
      split.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));

      pFileSeqBox.add(split);
    }

    if(!isLast) {
      JPanel spanel = new JPanel();
      spanel.setName("Spacer");
      
      spanel.setMinimumSize(new Dimension(sSize, 7));
      spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
      spanel.setPreferredSize(new Dimension(sSize, 7));
      
      pFileSeqBox.add(spanel);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node menu.
   */ 
  public void 
  updateNodeMenu() 
  {
    boolean privileged = 
      (PackageInfo.sUser.equals(pAuthor) || pPrivilegeDetails.isQueueManaged(pAuthor));

    pQueueJobsItem.setEnabled(privileged);
    pQueueJobsSpecialItem.setEnabled(privileged);
    pPauseJobsItem.setEnabled(privileged);
    pResumeJobsItem.setEnabled(privileged);
    pPreemptJobsItem.setEnabled(privileged);
    pKillJobsItem.setEnabled(privileged);
    pRemoveFilesItem.setEnabled(privileged);  

    updateEditorMenus();
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    pEditorMenuToolset = null;
    pComparatorMenuToolset = null;
  }

  /**
   * Update the editor plugin menus.
   */ 
  private void 
  updateEditorMenus()
  {
    String toolset = null;
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	if(details.getWorkingVersion() != null) 
	  toolset = details.getWorkingVersion().getToolset();
	else if(details.getLatestVersion() != null) 
	  toolset = details.getLatestVersion().getToolset();
      }
    }
      
    if((toolset != null) && !toolset.equals(pEditorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) 
	master.rebuildEditorMenu(toolset, pEditWithMenus[wk], this);
      
      pEditorMenuToolset = toolset;
    }
  }

  /**
   * Update the comparator plugin menus.
   */ 
  private void 
  updateComparatorMenus()
  {
    String toolset = null;
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	if(details.getWorkingVersion() != null) 
	  toolset = details.getWorkingVersion().getToolset();
	else if(details.getLatestVersion() != null) 
	  toolset = details.getLatestVersion().getToolset();
      }
    }

    if((toolset != null) && !toolset.equals(pComparatorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildComparatorMenu(toolset, pCompareWithMenu, this);
      
      pComparatorMenuToolset = toolset;
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
       
    updateMenuToolTip
      (pApplyItem, prefs.getApplyChanges(),
       "Apply the changes to the working version.");

    int wk;
    for(wk=0; wk<pEditItems.length; wk++) {
      updateMenuToolTip
	(pEditItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection.");

      updateMenuToolTip
	(pEditWithDefaultItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection using the default" + 
	 "editor for the file type.");
    }

    updateMenuToolTip
      (pQueueJobsItem, prefs.getQueueJobs(), 
       "Submit jobs to the queue for the current primary selection.");
    updateMenuToolTip
      (pQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Submit jobs to the queue for the current primary selection with special job " + 
       "requirements.");
    updateMenuToolTip
      (pPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pPreemptJobsItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected nodes.");

    updateMenuToolTip
      (pRemoveFilesItem, prefs.getRemoveFiles(), 
       "Remove all the primary/secondary files associated with the selected nodes.");
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Clear the current file selection.
   */ 
  public void
  clearSelection()
  {
    pSelected.clear();

    for(FileSeq fseq : pFilePanels.keySet()) {
      for(JFilePanel fpanel : pFilePanels.get(fseq).values()) 
	fpanel.setSelected(false);
    }
  }

  /** 
   * Select all files of the working file sequences.
   */ 
  public void 
  selectAll() 
  {
    NodeDetails details = pStatus.getDetails();
    if(details == null) 
      return;

    NodeMod mod = details.getWorkingVersion();
    if(mod == null) 
      return;

    pSelected.clear();

    {
      FileSeq fseq = mod.getPrimarySequence();
      if(fseq.isSingle()) 
	pSelected.add(0);
      else {
	int wk;
	for(wk=0; wk<fseq.numFrames(); wk++) 
	  pSelected.add(wk);
      }      
    }

    for(FileSeq fseq : pFilePanels.keySet()) {
      for(JFilePanel fpanel : pFilePanels.get(fseq).values()) 
	fpanel.setSelected(true);
    }    
  }

  /** 
   * Toggle the selection of the given file index.
   */ 
  public void 
  toggleSelect
  (
   Integer idx
  ) 
  {
    if(idx == null) 
      return;

    boolean selected;
    if(pSelected.contains(idx)) {
      pSelected.remove(idx);
      selected = false;
    }
    else {
      pSelected.add(idx);
      selected = true;
    }

    for(FileSeq fseq : pFilePanels.keySet()) {
      JFilePanel fpanel = pFilePanels.get(fseq).get(idx);
      fpanel.setSelected(selected);
    }    
  }

  /** 
   * Add all file indices between the given file index and the nearest selected index
   * to the selection.
   */ 
  public void 
  rangeSelect
  (
   Integer idx
  ) 
  {
    if(idx == null) 
      return;
    
    int start = 0;
    if(!pSelected.isEmpty()) {
      SortedSet<Integer> head = pSelected.subSet(0, idx);
      if(!head.isEmpty()) 
	start = head.last();
    }

    int wk;
    for(wk=start+1; wk<=idx; wk++) {
      pSelected.add(wk);
      
      for(FileSeq fseq : pFilePanels.keySet()) {
	JFilePanel fpanel = pFilePanels.get(fseq).get(wk);
	fpanel.setSelected(true);
      }    
    }
  }
  
  /** 
   * Add the given file index to the selection. 
   */ 
  public void 
  addSelect
  (
   Integer idx
  ) 
  {
    if(idx == null) 
      return;
    
    pSelected.add(idx);

    for(FileSeq fseq : pFilePanels.keySet()) {
      JFilePanel fpanel = pFilePanels.get(fseq).get(idx);
      fpanel.setSelected(true);
    }    
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
  mousePressed
  (
   MouseEvent e
  )
  {
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local mouse events */ 
    int mods = e.getModifiersEx();
    Object source = e.getSource();
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(source instanceof JLabel) {
	JLabel label = (JLabel) source;
	Container parent = label.getParent();
	if(parent instanceof JFilePanel) {
	  JFilePanel fpanel = (JFilePanel) parent;	

	  Integer idx = fpanel.getFileIdx();
	  if(idx != null) {
	    int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	    
	    int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.BUTTON3_DOWN_MASK | 
			MouseEvent.SHIFT_DOWN_MASK |
			MouseEvent.ALT_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	  
	  
	    int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
			MouseEvent.SHIFT_DOWN_MASK);
	  
	    int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.BUTTON3_DOWN_MASK | 
			MouseEvent.SHIFT_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	
	  
	    int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	  
	    int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.BUTTON3_DOWN_MASK | 
			MouseEvent.SHIFT_DOWN_MASK |
			MouseEvent.ALT_DOWN_MASK);
	  
	    /* BUTTON1: replace selection */ 
	    if((mods & (on1 | off1)) == on1) {
	      clearSelection();
	      addSelect(idx);
	    }
	  
	    /* BUTTON1+SHIFT: select a range */ 
	    else if((mods & (on2 | off2)) == on2) {
	      rangeSelect(idx);
	    }
	  
	    /* BUTTON1+CTRL: toggle the selection */ 
	    else if((mods & (on3 | off3)) == on3) {
	      toggleSelect(idx);
	    }
	  }
	}
      }
      break;
	
    case MouseEvent.BUTTON3:
    {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON3: popup menu */ 
	if((mods & (on1 | off1)) == on1) {

	  pTargetFileSeq   = null;
	  pTargetVersionID = null;

	  boolean hasWorking = false;
	  boolean checkedInHeader = false;

	  if(source instanceof JFileBar) {
	    JFileBar bar = (JFileBar) source;

	    pTargetFileSeq   = bar.getFileSeq();
	    pTargetVersionID = bar.getVersionID();
	    hasWorking       = bar.hasWorking();
	  }
	  else if(source instanceof JFileCheckBox) {
	    JFileCheckBox check = (JFileCheckBox) source;
	    pTargetFileSeq      = check.getFileSeq();
	    pTargetVersionID    = check.getVersionID();
	    hasWorking          = check.hasWorking();
	  }
	  else if(source instanceof JFileSeqLabel) {
	    JFileSeqLabel label = (JFileSeqLabel) source;
	    pTargetFileSeq = label.getFileSeq();
	    selectAll(); 
	  }
 	  else if(source == pHeaderIcon) {
 	    if(pStatus == null) 
 	      return; 

 	    NodeDetails details = pStatus.getDetails();
 	    if(details == null) 
 	      return;

 	    NodeMod mod = details.getWorkingVersion();
 	    NodeVersion latest = details.getLatestVersion();
 	    if(mod != null) {
	      pTargetFileSeq = mod.getPrimarySequence();
	      clearSelection();
	      selectAll();
 	    }
 	    else if(latest != null) {
 	      pTargetFileSeq   = latest.getPrimarySequence();
 	      pTargetVersionID = latest.getVersionID();
 	      checkedInHeader  = true;
 	    }
 	    else {
 	      return;
 	    }
 	  }
	  else if(source instanceof JLabel) {
	    JLabel label = (JLabel) source;
	    Container parent = label.getParent();
	    if(parent instanceof JFilePanel) {
	      JFilePanel fpanel = (JFilePanel) parent;
	      pTargetFileSeq = fpanel.getFileSeq();
	      addSelect(fpanel.getFileIdx());
	    }
	    else {
	      return;
	    }
	  }
	  else {
	    return;
	  }

	  if((pTargetVersionID != null) && !checkedInHeader) {
	    updateNodeMenu();
	    updateComparatorMenus();
	    pCompareWithMenu.setEnabled(hasWorking);
	    pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	  else if(pIsFrozen || checkedInHeader) {
	    updateNodeMenu();
	    pFrozenPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	  else {
	    updateNodeMenu();
	    pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	}
      }
      break;
    }
  }

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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getApplyChanges() != null) &&
       prefs.getApplyChanges().wasPressed(e) && 
       pApplyButton.isEnabled())
      doApply();

    else if((prefs.getEdit() != null) &&
	    prefs.getEdit().wasPressed(e))
      doEditWith(null, false);
    else if((prefs.getEditWithDefault() != null) &&
	    prefs.getEditWithDefault().wasPressed(e))
      doEditWith(null, true);
    
    else if((prefs.getQueueJobs() != null) &&
	    prefs.getQueueJobs().wasPressed(e))
      doQueueJobs();
    else if((prefs.getQueueJobsSpecial() != null) &&
	    prefs.getQueueJobsSpecial().wasPressed(e))
      doQueueJobsSpecial();
    else if((prefs.getPauseJobs() != null) &&
	    prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
    else if((prefs.getResumeJobs() != null) &&
	    prefs.getResumeJobs().wasPressed(e))
      doResumeJobs();
    else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
      doKillJobs();
    
    else if((prefs.getRemoveFiles() != null) &&
	    prefs.getRemoveFiles().wasPressed(e))
      doRemoveFiles();
    
    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
      }
    }
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
    else if(cmd.startsWith("file-check:")) {
      String comps[] = cmd.split(":");
      doFileChecked((JFileCheckBox) e.getSource(), comps[1], comps[2]);      
    }
    else if(cmd.startsWith("version-pressed:")) {
      String comps[] = cmd.split(":");
      doVersionPressed(comps[1], comps[2]);
    }
    else if(cmd.equals("edit"))
      doEditWith(null, false);
    else if(cmd.equals("edit-with-default"))
      doEditWith(null, true);
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10), false); 
    else if(cmd.startsWith("compare-with:"))
      doCompareWith(cmd.substring(13)); 

    else if(cmd.equals("queue-jobs"))
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special"))
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("preempt-jobs"))
      doPreemptJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("remove-files"))
      doRemoveFiles();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Replace working files with the selected checked-in files.
   */ 
  private void 
  doApply()
  {
    if(pIsFrozen) 
      return;

    TreeMap<String,VersionID> files = new TreeMap<String,VersionID>();
    for(TreeMap<String,ArrayList<JComponent>> table : pNoveltyComps.values()) {
      for(ArrayList<JComponent> clist : table.values()) {
	for(JComponent comp : clist) {
	  if(comp instanceof JFileCheckBox) {
	    JFileCheckBox check = (JFileCheckBox) comp;
	    if(check.isSelected()) 
	      files.put(check.getFileSeq().getFile(0).toString(), check.getVersionID());
	  }
	}
      }
    }

    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);
	  
    RevertTask task = new RevertTask(files);
    task.start();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the row of file novelty components to reflect a change 
   * in selection.
   */ 
  private void 
  doFileChecked
  (
   JFileCheckBox check, 
   String sname, 
   String fname
  ) 
  {
    boolean selected = check.isSelected();

    ArrayList<JComponent> comps = pNoveltyComps.get(sname).get(fname);

    /* deselect the entire row */ 
    int idx = -1;
    int wk;
    for(wk=0; wk<comps.size(); wk++) {
      JComponent comp = comps.get(wk);
      if(comp instanceof JFileCheckBox) {
	JFileCheckBox cb = (JFileCheckBox) comp;
	cb.setSelected(false);
	if(cb == check) 
	  idx = wk;
      }
      else if(comp instanceof JFileBar) {
	JFileBar bar = (JFileBar) comp;
	bar.setSelected(false);
      }
    }
    assert(idx >= 0);

    /* reselect the checkbox and attached file bar labels */ 
    if(selected) {
      check.setSelected(true);

      for(wk=idx-1; wk>=0; wk--) {
	JComponent comp = comps.get(wk);
	if(comp instanceof JFileBar) {
	  JFileBar bar = (JFileBar) comp;
	  bar.setSelected(true);
	}
	else {
	  break;
	}
      }
    }

    /* update the file arrow selection */ 
    JFileArrow arrow = pFileArrows.get(fname);
    arrow.setSelected(selected);

    pApplyButton.setEnabled(true);
    pApplyItem.setEnabled(true);
  }
  
  /**
   * Select/Deselect all files in the given revision.
   */ 
  private void 
  doVersionPressed
  (
   String sname, 
   String vname
  ) 
  {
    Boolean selected = null;
    TreeMap<String,JFileCheckBox> table = pVersionBoxes.get(sname).get(vname);
    if(table != null) {
      for(String fname : table.keySet()) {
	JFileCheckBox check = table.get(fname);	

	if(selected == null) 
	  selected = !check.isSelected();
	check.setSelected(selected); 
	
	doFileChecked(check, sname, fname);
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the target file with the given editor.
   * 
   * @param editor
   *   The name of the editor plugin or <CODE>null</CODE> for the node's editor.
   * 
   * @param useDefault
   *   Whether to use the default editor for the file suffix instead of the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor, 
   boolean useDefault
  ) 
  {
    if(pTargetFileSeq == null) 
      return;
    
    String ename   = null;
    VersionID evid = null;
    String evendor = null;
    if(editor != null) {
      String parts[] = editor.split(":");
      assert(parts.length == 3);
      
      ename   = parts[0];
      evid    = new VersionID(parts[1]);
      evendor = parts[2];
    }

    if(pTargetVersionID != null) {
      EditTask task = new EditTask(ename, evid, evendor, 
				   pTargetFileSeq, pTargetVersionID, useDefault);
      task.start();
    }
    else if(!pSelected.isEmpty()) {
      ArrayList<Integer[]> ranges = new ArrayList<Integer[]>();
      {
	Integer last = null;
	Integer range[] = new Integer[2];
	for(Integer idx : pSelected) {
	  if(range[0] == null) {
	    range[0] = idx;
	  }
	  else if(range[1] == null) {
	    if((idx - last) > 1) {
	      range[1] = last;
	      ranges.add(range);
	      
	      range = new Integer[2];
	      range[0] = idx;
	      last = idx;
	    }
	  }
	  
	  last = idx;
	}
	
	if(range[0] != null) {
	  range[1] = last;
	  ranges.add(range);
	}
      }

      for(Integer[] range : ranges) {
	FileSeq fseq = new FileSeq(pTargetFileSeq, range[0], range[1]);
	EditTask task = new EditTask(ename, evid, evendor, 
				     fseq, null, useDefault);
	task.start();
      }
      
      clearSelection();
    }
  }

   

  /*----------------------------------------------------------------------------------------*/

  /**
   * Compare the target checked-in file with the corresponding working file using the given
   * comparator.
   */ 
  private void 
  doCompareWith
  (
   String comparator
  ) 
  {
    String parts[] = comparator.split(":");
    assert(parts.length == 3);
    
    String cname   = parts[0];
    VersionID cvid = new VersionID(parts[1]);
    String cvendor = parts[2];

    if((pTargetFileSeq != null) && (pTargetVersionID != null)) {
      CompareTask task = new CompareTask(cname, cvid, cvendor, 
					 pTargetFileSeq, pTargetVersionID);
      task.start();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit jobs to the queue for the node and all nodes upstream of it.
   */ 
  private void 
  doQueueJobs() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      QueueJobsTask task = new QueueJobsTask(pSelected);
      task.start();
    }

    clearSelection();
  }

  /**
   * Submit jobs to the queue for the node and all nodes upstream of it with special
   * job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      JQueueJobsDialog diag = UIMaster.getInstance().showQueueJobsDialog();
      if(diag.wasConfirmed()) {
	Integer batchSize = null;
	if(diag.overrideBatchSize()) 
	  batchSize = diag.getBatchSize();
	
	Integer priority = null;
	if(diag.overridePriority()) 
	  priority = diag.getPriority();
	
	Integer interval = null;
	if(diag.overrideRampUp()) 
	  interval = diag.getRampUp();
	
	TreeSet<String> keys = null;
	if(diag.overrideSelectionKeys()) 
	  keys = diag.getSelectionKeys();
	
	QueueJobsTask task = 
	  new QueueJobsTask(pSelected, batchSize, priority, interval, keys);
	task.start();
      }
    }

    clearSelection();
  }

  /**
   * Pause waiting jobs associated with the node.
   */ 
  private void 
  doPauseJobs() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      TreeSet<Long> paused = new TreeSet<Long>();
      {
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  Long[] jobIDs   = details.getJobIDs();
	  QueueState[] qs = details.getQueueState();
	  assert(jobIDs.length == qs.length);
	  
	  for(Integer idx : pSelected) {
	    switch(qs[idx]) {
	    case Queued:
	      assert(jobIDs[idx] != null);
	      paused.add(jobIDs[idx]);
	    }
	  }
	}
      }

      if(!paused.isEmpty()) {
	PauseJobsTask task = new PauseJobsTask(paused);
	task.start();
      }
    }

    clearSelection();
  }

  /**
   * Resume execution of paused jobs associated with the node.
   */ 
  private void 
  doResumeJobs() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      TreeSet<Long> resumed = new TreeSet<Long>();
      {
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  Long[] jobIDs   = details.getJobIDs();
	  QueueState[] qs = details.getQueueState();
	  assert(jobIDs.length == qs.length);
	  
	  for(Integer idx : pSelected) {
	    switch(qs[idx]) {
	    case Paused:
	      assert(jobIDs[idx] != null);
	      resumed.add(jobIDs[idx]);
	    }
	  }
	}
      }

      if(!resumed.isEmpty()) {
	ResumeJobsTask task = new ResumeJobsTask(resumed);
	task.start();
      }
    }

    clearSelection();
  }

  /**
   * Preempt jobs associated with the selected node.
   */ 
  private void 
  doPreemptJobs() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      TreeSet<Long> dead = new TreeSet<Long>();
      {
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  Long[] jobIDs   = details.getJobIDs();
	  QueueState[] qs = details.getQueueState();
	  assert(jobIDs.length == qs.length);
	  
	  for(Integer idx : pSelected) {
	    switch(qs[idx]) {
	    case Queued:
	    case Paused:
	    case Running:
	      assert(jobIDs[idx] != null);
	      dead.add(jobIDs[idx]);
	    }
	  }
	}
      }

      if(!dead.isEmpty()) {
	PreemptJobsTask task = new PreemptJobsTask(dead);
	task.start();
      }
    }

    clearSelection();    
  }

  /**
   * Kill jobs associated with the selected node.
   */ 
  private void 
  doKillJobs() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      TreeSet<Long> dead = new TreeSet<Long>();
      {
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  Long[] jobIDs   = details.getJobIDs();
	  QueueState[] qs = details.getQueueState();
	  assert(jobIDs.length == qs.length);
	  
	  for(Integer idx : pSelected) {
	    switch(qs[idx]) {
	    case Queued:
	    case Paused:
	    case Running:
	      assert(jobIDs[idx] != null);
	      dead.add(jobIDs[idx]);
	    }
	  }
	}
      }

      if(!dead.isEmpty()) {
	KillJobsTask task = new KillJobsTask(dead);
	task.start();
      }
    }

    clearSelection();    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove primary/secondary files associated with the node.
   */ 
  private void 
  doRemoveFiles() 
  {
    if(!pIsFrozen && !pSelected.isEmpty()) {
      boolean confirmed = false;
      {
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  NodeMod work = details.getWorkingVersion();
	  if(work != null) {
	    if(work.isActionEnabled()) {
	      confirmed = true;
	    }
	    else {
	      JConfirmDialog confirm = 
		new JConfirmDialog("Remove from Node without enabled Actions?");
	      confirm.setVisible(true);
	      confirmed = confirm.wasConfirmed(); 
	    }
	  }
	}
      }
    
      if(confirmed) {
	RemoveFilesTask task = new RemoveFilesTask(pSelected);
	task.start();
      }
    }
    
    clearSelection();    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A label which represents a file sequence.
   */ 
  private 
  class JFileSeqLabel
    extends JLabel
  {
    public 
    JFileSeqLabel
    (
     FileSeq fseq
    ) 
    {
      super(fseq.toString());

      pFileSeq = fseq;

      setName("TextFieldLabel");
      
      setHorizontalAlignment(JLabel.CENTER);
      setAlignmentX(0.5f);
      
      Dimension size = new Dimension(164, 27);
      setMinimumSize(size);
      setMaximumSize(new Dimension(Integer.MAX_VALUE, 27));
    }

    public FileSeq
    getFileSeq() 
    {
      return pFileSeq;
    }

    private static final long serialVersionUID = 1001698705744120743L;

    private FileSeq  pFileSeq;
  }

  /**
   * A component representing a single member file of a file sequence, its current 
   * file state, queue state and selection status.
   */ 
  private 
  class JFilePanel
    extends JPanel
  {
    /**
     * Construct a 
     */ 
    public 
    JFilePanel
    (
     String text,
     FileSeq fseq, 
     Integer idx,
     FileState fstate, 
     QueueState qstate, 
     boolean isFrozen, 
     JNodeFilesPanel parent
    ) 
    {
      super();

      /* initialize fields */ 
      {
	pFileSeq = fseq;
	pFileIdx = idx; 
	
	pTexPrefix = "Blank-";
	if((fstate != null) && (qstate != null)) {
	  pTexPrefix = (fstate + "-" + qstate + (isFrozen ? "-Frozen-" : "-"));
	  pIsSelectable = (fstate != FileState.CheckedIn);
	}

	pIsModified = ((fstate != null) && (fstate != FileState.Identical));
      }

      /* panel components */ 
      {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	
	{
	  JLabel label = new JLabel();
	  pStateLabel = label;
	  
	  if(pIsSelectable) 
	    label.addMouseListener(parent);

	  add(label); 
	}
	
	add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel(text);
	  pNameLabel = label;

	  label.setName("TextFieldLabel");
	  label.setHorizontalAlignment(JLabel.CENTER);
	  
	  Dimension size = new Dimension(140, 19);
	  label.setMinimumSize(size);
	  label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

	  if(pIsSelectable) 
	    label.addMouseListener(parent);

	  add(label); 
	}
      }

      setSelected(false);
    }

    public FileSeq
    getFileSeq() 
    {
      return pFileSeq;
    }

    public Integer
    getFileIdx()
    {
      return pFileIdx; 
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
      if(pIsSelectable) 
	pIsSelected = tf;
      
      TextureMgr mgr = TextureMgr.getInstance();
      try {
	String suffix = pIsSelected ? "Selected" : "Normal"; 
	pStateLabel.setIcon(mgr.getIcon21(pTexPrefix + suffix));
      }	
      catch(IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Tex, LogMgr.Level.Severe,
	   "Internal Error:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	  System.exit(1);
      }

      if(!pIsSelectable) 
	pNameLabel.setForeground(new Color(0.8f, 0.8f, 0.8f));
      else if(pIsSelected) 
	pNameLabel.setForeground(Color.yellow);
      else if(pIsModified) 
	pNameLabel.setForeground(Color.cyan);
      else 
	pNameLabel.setForeground(Color.white);
    }

    private static final long serialVersionUID = -7771122583765848072L;

    private FileSeq  pFileSeq;
    private Integer  pFileIdx;
    private String   pTexPrefix;
    private boolean  pIsModified;
    private boolean  pIsSelected;
    private boolean  pIsSelectable; 
    
    private JLabel   pStateLabel;  
    private JLabel   pNameLabel;
  }

  /**
   * A label used to represent to horizontal bars for checked-in versions which are not novel.
   */ 
  private 
  class JFileBar
    extends JLabel
  {
    /**
     * Construct a new file bar.
     * 
     * @param fseq
     *   The parent file sequence.
     * 
     * @param vid
     *   The revision number of the parent checked-in node.
     * 
     * @param hasWorking
     *   Whether any corresponding working files exist.
     */ 
    public 
    JFileBar
    (
     FileSeq fseq,
     VersionID vid, 
     boolean hasWorking,
     boolean isExtended
    ) 
    {
      super();

      pFileSeq    = fseq;
      pVersionID  = vid;
      pHasWorking = hasWorking;

      pIsExtended = isExtended;
      setSelected(false);

      Dimension size = new Dimension(70, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public FileSeq
    getFileSeq() 
    {
      return pFileSeq;
    }

    public VersionID
    getVersionID() 
    {
      return pVersionID;
    }

    public boolean
    hasWorking() 
    {
      return pHasWorking;
    }

    public boolean
    isExtended()
    {
      return pIsExtended;
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
	setIcon(pIsExtended ? sFileBarExtendIconSelected : sFileBarIconSelected);
      else
	setIcon(pIsExtended ? sFileBarExtendIcon : sFileBarIcon);
    }

    private static final long serialVersionUID = 7847925394838326022L;

    private FileSeq    pFileSeq;
    private VersionID  pVersionID;
    private boolean    pHasWorking;
    private boolean    pIsExtended;
    private boolean    pIsSelected; 
  }

  /**
   * A label used to indicate files which will be replaced by the apply operation.
   */ 
  private 
  class JFileArrow
    extends JLabel
  {
    /**
     * Construct a new file arrow.
     */ 
    public 
    JFileArrow
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

    private static final long serialVersionUID = -6869518287565623649L;

    private boolean  pIsSelected; 
    private boolean  pIsActive;
  }

  /**
   * A JCheckBox with an attached single file sequence. 
   */ 
  private 
  class JFileCheckBox
    extends JCheckBox
  {
    public 
    JFileCheckBox
    (
     FileSeq fseq, 
     VersionID vid, 
     boolean hasWorking
    ) 
    {
      super();

      pFileSeq   = fseq;
      pVersionID = vid;
      pHasWorking = hasWorking;

      setName("FileCheck");
      setSelected(false);
      
      Dimension size = new Dimension(12, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public FileSeq
    getFileSeq() 
    {
      return pFileSeq;
    }

    public VersionID
    getVersionID() 
    {
      return pVersionID;
    }

    public boolean
    hasWorking() 
    {
      return pHasWorking;
    }


    private static final long serialVersionUID = 2303900581105425334L;

    private FileSeq    pFileSeq;
    private VersionID  pVersionID;
    private boolean    pHasWorking;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A panel which displays the current state and revision history of a file sequence.  
   */ 
  private 
  class JFileSeqPanel 
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
     * @param fseq
     *   The file sequence.
     * 
     * @param singles
     *   The single file sequences for all files to display.
     * 
     * @param enabled
     *   The names of the files which have defined states.
     * 
     * @param flags
     *   The per-version (newest to oldest) file novelty flags indexed by filename.
     * 
     * @param numVersions
     *   The number of revisions to display.
     */ 
    public 
    JFileSeqPanel 
    (
     JNodeFilesPanel parent,
     FileSeq fseq, 
     Collection<FileSeq> singles,
     TreeSet<FileSeq> enabled, 
     TreeMap<FileSeq,Boolean[]> flags, 
     int numVersions
    ) 
    {
      super();
      
      TreeMap<String,ArrayList<JComponent>> nameComps = 
	new TreeMap<String,ArrayList<JComponent>>();
      pNoveltyComps.put(fseq.toString(), nameComps);

      TreeMap<String,TreeMap<String,JFileCheckBox>> versionBoxes = 
	new TreeMap<String,TreeMap<String,JFileCheckBox>>();
      pVersionBoxes.put(fseq.toString(), versionBoxes);
      
      ArrayList<VersionID> vids = new ArrayList<VersionID>(pNovelty.keySet());
      Collections.reverse(vids);

      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	add(Box.createRigidArea(new Dimension(0, 1)));
	
	int fk=0;
	for(FileSeq sfseq : singles) {
	  String fname = sfseq.getFile(0).toString();

	  Boolean[] novel = flags.get(sfseq);
	  if(novel == null) {
	    add(Box.createRigidArea(new Dimension(70*numVersions, 19)));
	  }
	  else {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    hbox.add(Box.createRigidArea(new Dimension(27, 0)));
	    
	    int wk;
	    for(wk=0; wk<novel.length; wk++) {
	      VersionID vid = vids.get(wk);

	      if(novel[wk] == null) {
		int width = 70;
		if((wk > 0) && (novel[wk-1] != null) && (novel[wk-1])) 
		  width += 2; 

		hbox.add(Box.createRigidArea(new Dimension(width, 0)));
	      }
	      else {
		ArrayList<JComponent> nlist = nameComps.get(fname);
		if(nlist == null) {
		  nlist = new ArrayList<JComponent>();
		  nameComps.put(fname, nlist);
		}

		TreeMap<String,JFileCheckBox> vboxes = versionBoxes.get(vid.toString());
		if(vboxes == null) {
		  vboxes = new TreeMap<String,JFileCheckBox>();
		  versionBoxes.put(vid.toString(), vboxes);
		}

		boolean isOffline = pOffline.contains(vid);
		boolean isEnabled = enabled.contains(sfseq) && !isOffline;

		if(novel[wk]) {
		  if((wk > 0) && (novel[wk-1] != null) && novel[wk-1])
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));		  
		  
		  {
		    JFileCheckBox check = new JFileCheckBox(sfseq, vid, isEnabled);
		    
		    if(isEnabled && !pIsFrozen) {
		      check.addActionListener(parent);
		      check.setActionCommand("file-check:" + fseq + ":" + fname);
		      
		      nlist.add(check);
		      vboxes.put(fname, check);
		      
		      int vk;
		      for(vk=wk-1; vk>=0; vk--) {
			if((novel[vk] != null) && !novel[vk]) {
			  VersionID pvid = vids.get(vk);
			  
			  TreeMap<String,JFileCheckBox> pvboxes = 
			    versionBoxes.get(pvid.toString());
			  if(pvboxes == null) {
			    pvboxes = new TreeMap<String,JFileCheckBox>();
			    versionBoxes.put(pvid.toString(), pvboxes);
			  }
			  
			  pvboxes.put(fname, check);
			}
			else {
			  break;
			}
		      }
		    }
		    else {
		      check.setEnabled(false);
		    }
		    
		    if(!isOffline) 
		      check.addMouseListener(parent);

		    hbox.add(check);
		  }
		  
		  hbox.add(Box.createRigidArea(new Dimension(56, 0)));
		}
		else {
		  boolean isExtended = false; 
		  if((wk > 0) && (novel[wk-1] != null)) {
		    if(!novel[wk-1])
		      isExtended = true;
		    else 
		      hbox.add(Box.createRigidArea(new Dimension(2, 0)));		 
		  }
		  
		  {
		    JFileBar bar = new JFileBar(sfseq, vid, isEnabled, isExtended);
		    if(isEnabled) 
		      nlist.add(bar);
		    
		    if(!isOffline)
		      bar.addMouseListener(parent);

		    hbox.add(bar);	
		  }
		}
	      }
	    }
	    
	    hbox.add(Box.createHorizontalGlue());
	    
	    add(hbox);
	  }
	  
	  if(fk < (singles.size()-1)) 
	    add(Box.createRigidArea(new Dimension(0, 3)));
	  
	  fk++;
	}
	
	add(Box.createRigidArea(new Dimension(0, 1)));
	
	Dimension size = new Dimension(70*numVersions, 20 + 22*(singles.size()-1));
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

    private static final long serialVersionUID = 5432831706042986281L;


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
   * Edit/View the target file.
   */ 
  private
  class EditTask
    extends Thread
  {
    public 
    EditTask
    (
     String ename, 
     VersionID evid,
     String evendor, 
     FileSeq fseq, 
     VersionID vid, 
     boolean useDefault
    ) 
    {
      super("JNodeFilesPanel:EditTask");

      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor; 
      pFileSeq       = fseq; 
      pVersionID     = vid; 
      pUseDefault    = useDefault;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      SubProcessLight proc = null;
      {
	try {
	  String name = pStatus.getName();
	  MasterMgrClient client = master.getMasterMgrClient();

	  NodeCommon com = null;
	  {
	    NodeMod mod = pStatus.getDetails().getWorkingVersion();
	    if(mod != null) 
	      com = mod;
	    else 
	      com = client.getCheckedInVersion(name, pVersionID);	    
	  }

	  /* create an editor plugin instance */ 
	  BaseEditor editor = null;
	  {
	    if(pEditorName != null) {
	      PluginMgrClient pclient = PluginMgrClient.getInstance();
	      editor = pclient.newEditor(pEditorName, pEditorVersion, pEditorVendor);
	    }
	    else if(pUseDefault) {
	      FilePattern fpat = com.getPrimarySequence().getFilePattern();
	      String suffix = fpat.getSuffix();
	      if(suffix != null) 
		editor = client.getEditorForSuffix(suffix);
	    }
	    
	    if(editor == null) 
	      editor = com.getEditor();
	    
	    if(editor == null) 
	      throw new PipelineException
		("No editor was specified for node (" + com.getName() + ")!");
	  }

	  /* lookup the toolset environment */ 
	  TreeMap<String,String> env = null;
	  {
	    String tname = com.getToolset();
	    if(tname == null) 
	      throw new PipelineException
		("No toolset was specified for node (" + name + ")!");
	      
	    /* passes pAuthor so that WORKING will correspond to the current view */ 
	    env = client.getToolsetEnvironment(pAuthor, pView, tname, PackageInfo.sOsType);
	  }

	  /* working directory */ 
	  File dir = null;
	  if(pVersionID != null) {
	    Path path = new Path(PackageInfo.sRepoPath, name + "/" + pVersionID);
	    dir = path.toFile();
	  }
	  else {
	    Path path = new Path(PackageInfo.sWorkPath, pAuthor + "/" + pView + name);
	    dir = path.getParentPath().toFile();
	  }

	  /* start the editor */ 
	  proc = editor.launch(new FileSeq(dir.getPath(), pFileSeq), env, dir);	   
	}
	catch(Exception ex) {
	  master.showErrorDialog(ex);
	  return;
	}
      }

      /* wait for the editor to exit */ 
      if(proc != null) {
	try {
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    master.showSubprocessFailureDialog("Editor Failure:", proc);
	}
	catch(InterruptedException ex) {
	  master.showErrorDialog(ex);
	}
      }
    }

    private String     pEditorName;
    private VersionID  pEditorVersion; 
    private String     pEditorVendor; 
    private FileSeq    pFileSeq; 
    private VersionID  pVersionID; 
    private boolean    pUseDefault; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Compare the target checked-in file with the corresponding working file using the given
   * comparator.
   */ 
  private
  class CompareTask
    extends Thread
  {
    public 
    CompareTask
    (
     String cname,   
     VersionID cvid,
     String cvendor, 
     FileSeq fseq, 
     VersionID vid
    ) 
    {
      super("JNodeFilesPanel:CompareTask");

      pComparatorName    = cname;
      pComparatorVersion = cvid; 
      pComparatorVendor  = cvendor; 
      pFileSeq           = fseq; 
      pVersionID         = vid; 
    }

    public void 
    run() 
    {
      SubProcessLight proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Comparator...")) {
	  try {
	    String name = pStatus.getName();
	    MasterMgrClient client = master.getMasterMgrClient();

	    NodeCommon com = null;
	    {
	      NodeMod mod = pStatus.getDetails().getWorkingVersion();
	      if(mod != null) 
		com = mod;
	      else 
		com = client.getCheckedInVersion(name, pVersionID);	    
	    }

	    /* create an comparator plugin instance */ 
	    PluginMgrClient pclient = PluginMgrClient.getInstance();
	    BaseComparator comparator = 
	      pclient.newComparator(pComparatorName, pComparatorVersion, pComparatorVendor);

	    /* the checked-in file */  
	    File fileB = null;
	    {
	      Path path = new Path(PackageInfo.sRepoPath, 
				   name + "/" + pVersionID + "/" + pFileSeq.toString());
	      fileB = path.toFile();	      
	    }

	    /* the working file */ 
	    File fileA = null;
	    {
	      Path path = new Path(PackageInfo.sWorkPath, 
				   pAuthor + "/" + pView + pStatus.getName());
	      Path wpath = new Path(path.getParentPath(), pFileSeq.toString());
	      fileA = wpath.toFile();
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = com.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + name + ")!");
	      
	      /* passes pAuthor so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment(pAuthor, pView, tname, PackageInfo.sOsType);
	    }
	    
	    /* start the comparator */ 
	    proc = comparator.launch(fileA, fileB, env, PackageInfo.sTempPath.toFile());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}

	/* wait for the comparator to exit */ 
	if(proc != null) {
	  try {
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      master.showSubprocessFailureDialog("Comparator Failure:", proc);
	  }
	  catch(InterruptedException ex) {
	    master.showErrorDialog(ex);
	  }
	}
      }
    }

    private String     pComparatorName;
    private VersionID  pComparatorVersion; 
    private String     pComparatorVendor; 
    private FileSeq    pFileSeq; 
    private VersionID  pVersionID; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Replace working area files with the given repository versions.
   */ 
  private
  class RevertTask
    extends Thread
  {
    public 
    RevertTask
    (
     TreeMap<String,VersionID> files
    ) 
    {
      super("JNodeFilesPanel:RevertTask");

      pFiles = files;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Reverting Files...")) {
	try {
	  master.getMasterMgrClient().revertFiles(pAuthor, pView, pStatus.getName(), pFiles);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeMap<String,VersionID>  pFiles;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Queue jobs to the queue for the given file sequence index.
   */ 
  private
  class QueueJobsTask
    extends Thread
  {
    public 
    QueueJobsTask
    (
     TreeSet<Integer> indices
    ) 
    {
      this(indices, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeSet<Integer> indices, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      super("JNodeFilesPanel:QueueJobsTask");

      pIndices       = new TreeSet<Integer>(indices);
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pSelectionKeys = selectionKeys;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Submitting Jobs to the Queue...")) {
	try {
	  master.getMasterMgrClient().submitJobs(pAuthor, pView, pStatus.getName(), pIndices, 
						 pBatchSize, pPriority, pRampUp, 
						 pSelectionKeys);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Integer> pIndices; 
    private Integer          pBatchSize;
    private Integer          pPriority;
    private Integer          pRampUp; 
    private TreeSet<String>  pSelectionKeys;
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends Thread
  {
    public 
    PauseJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JNodeFilesPanel:PauseJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Pausing Jobs...")) {
	try {
	  master.getQueueMgrClient().pauseJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends Thread
  {
    public 
    ResumeJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JNodeFilesPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Resuming Paused Jobs...")) {
	try {
	  master.getQueueMgrClient().resumeJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Preempt the given jobs.
   */ 
  private
  class PreemptJobsTask
    extends Thread
  {
    public 
    PreemptJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JNodeFilesPanel:PreemptJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Preempting Jobs...")) {
	try {
	  master.getQueueMgrClient().preemptJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends Thread
  {
    public 
    KillJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JNodeFilesPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Killing Jobs...")) {
	try {
	  master.getQueueMgrClient().killJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  private
  class RemoveFilesTask
    extends Thread
  {
    public 
    RemoveFilesTask
    (
     TreeSet<Integer> indices
    ) 
    {
      super("JNodeViewerPanel:RemoveFilesTask");

      pIndices = new TreeSet<Integer>(indices);
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Removing Files: " + pStatus.getName())) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient();
	  client.removeFiles(pAuthor, pView, pStatus.getName(), pIndices);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}
	
	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private TreeSet<Integer> pIndices; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;

  private static final int  sSize = 484;


  private static final Icon sFileBarIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIcon.png"));

  private static final Icon sFileBarExtendIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIcon.png"));

  private static final Icon sFileBarIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIconSelected.png"));

  private static final Icon sFileBarExtendIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIconSelected.png"));

  private static final Icon sFrozenIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FrozenIcon.png"));

  private static final Icon sLockedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedIcon.png"));

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
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The per-file novelty flags.
   */ 
  private TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>  pNovelty;

  /**
   * The revision numbers of the offline checked-in versions.
   */ 
  private TreeSet<VersionID>  pOffline; 

  /**
   * The toolset used to build the editor menu.
   */ 
  private String  pEditorMenuToolset;

  /**
   * The toolset used to build the comparator menu.
   */ 
  private String  pComparatorMenuToolset;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The popup menus.
   */ 
  private JPopupMenu  pWorkingPopup; 
  private JPopupMenu  pFrozenPopup; 
  private JPopupMenu  pCheckedInPopup; 

  /**
   * The popup menus items.
   */ 
  private JMenuItem  pApplyItem;
  private JMenuItem  pQueueJobsItem;
  private JMenuItem  pQueueJobsSpecialItem;
  private JMenuItem  pPauseJobsItem;
  private JMenuItem  pResumeJobsItem;
  private JMenuItem  pPreemptJobsItem;
  private JMenuItem  pKillJobsItem;
  private JMenuItem  pRemoveFilesItem;

  
  /**
   * The edit with submenus.
   */ 
  private JMenuItem[]  pEditItems;
  private JMenuItem[]  pEditWithDefaultItems;
  private JMenu[]      pEditWithMenus; 

  /**
   * The compare with submenu.
   */ 
  private JMenu  pCompareWithMenu;


  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The file sequence which is the target of the menu operation.
   */ 
  private FileSeq  pTargetFileSeq; 
  
  /**
   * The revision number of the checked-in version which is the target of the menu operation
   * or <CODE>null</CODE> if the menu operation is targeting a working file sequence.
   */ 
  private VersionID  pTargetVersionID;

  /**
   * All currently selected indices of the working file sequences.
   */
  private TreeSet<Integer>  pSelected; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel  pHeaderIcon;
  private JLabel  pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;

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
   * The file sequences container.
   */ 
  private Box  pFileSeqBox;


  /**
   * The selectable file panels indexed by file sequence and file sequence index. 
   */ 
  private TreeMap<FileSeq,TreeMap<Integer,JFilePanel>>  pFilePanels;  


  /**
   * The file update arrows indexed by file name.
   */
  private TreeMap<String,JFileArrow>  pFileArrows; 

  /**
   * The file novelty UI components indexed by file sequence (String) and file name.
   */ 
  private TreeMap<String,TreeMap<String,ArrayList<JComponent>>>  pNoveltyComps;

  /**
   * The file novelty check boxes indexed by file sequence (String), revision number (String)
   * and file name.
   */ 
  private TreeMap<String,TreeMap<String,TreeMap<String,JFileCheckBox>>>  pVersionBoxes;

}
