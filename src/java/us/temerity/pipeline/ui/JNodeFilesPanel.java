// $Id: JNodeFilesPanel.java,v 1.4 2004/07/22 00:07:16 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;
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
  implements ActionListener, MouseListener
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
    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;

      pWorkingPopup   = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu();  

      JPopupMenu menus[] = { pWorkingPopup, pCheckedInPopup };
      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem("Edit");
	item.setActionCommand("edit");
	item.addActionListener(this);
	menus[wk].add(item);
	
	{
	  sub = new JMenu("Edit With");
	  menus[wk].add(sub);
	  
	  for(String editor : Plugins.getEditorNames()) {
	    item = new JMenuItem(editor);
	    item.setActionCommand("edit-with:" + editor);
	    item.addActionListener(this);
	    sub.add(item);
	  }
	}
      }
      
      {
	pWorkingPopup.addSeparator();
	
	item = new JMenuItem("Make");
	item.setActionCommand("make");
	item.addActionListener(this);
	item.setEnabled(false);  // FOR NOW...
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Queue");
	item.setActionCommand("queue");
	item.addActionListener(this);
	item.setEnabled(false);  // FOR NOW...
	pWorkingPopup.add(item);
      }
      
      {
	pCheckedInPopup.addSeparator();
	
	{
	  sub = new JMenu("Compare With");
	  pCompareWithMenu = sub;
	  pCheckedInPopup.add(sub);
	  
	  for(String comparator : Plugins.getComparatorNames()) {
	    item = new JMenuItem(comparator);
	    item.setActionCommand("compare-with:" + comparator);
	    item.addActionListener(this);
	    sub.add(item);
	  }
	}
      }	
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

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sSize+22, 120);
      setMinimumSize(size);
      setPreferredSize(size); 
    }

    updateNodeStatus(null, null);
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
      
	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

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
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, novelty);
  }

  /**
   * Update the UI components to reflect the current per-file status.
   * 
   * @param status
   *   The current node status.
   * 
   * @param novelty
   *   The per-file novelty flags.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty
  ) 
  {
    pStatus  = status;
    pNovelty = novelty; 

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
		name = ("NeedsCheckOutMajor-" + details.getOverallQueueState() + "-Normal");
		break;
		
	      case Minor:
		name = ("NeedsCheckOut-" + details.getOverallQueueState() + "-Normal");
		break;
		
	      case Micro:
		name = ("NeedsCheckOutMicro-" + details.getOverallQueueState() + "-Normal");
	      }
	    }
	    else {
	      name = (details.getOverallNodeState() + "-" + 
		      details.getOverallQueueState() + "-Normal");
	    }
	  }
	  
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
    }

    /* files */ 
    {
      pFileSeqBox.removeAll();

      pFileLabels     = new TreeMap<String,TreeMap<String,JFileLabel>>();
      pNameComponents = new TreeMap<String,TreeMap<String,ArrayList<JComponent>>>();
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

    /* collate the row information */ 
    TreeSet<FileSeq> singles = new TreeSet<FileSeq>();	 
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
	      singles.add(sfseq);
	      
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
		  singles.add(sfseq);

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
    }
      
    /* the drawer body */ 
    Box body = null;
    {
      body = new Box(BoxLayout.X_AXIS);
      
      body.add(Box.createRigidArea(new Dimension(3, 0)));
	
      /* file state/name labels */ 
      {
	String path = null;
	{
	  File file = new File(pStatus.getName());
	  path = (PackageInfo.sWorkDir + "/" + 
		  pAuthor + "/" + pView + file.getParent());
	}

	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  JFileLabel label = new JFileLabel(new FileSeq(path, fseq), fseq.toString());
	  label.setName("TextFieldLabel");
	  
	  label.setHorizontalAlignment(JLabel.CENTER);
	  label.setAlignmentX(0.5f);

	  Dimension size = new Dimension(174, 27);
	  label.setMaximumSize(size);
	  label.setMaximumSize(size);
	  label.setPreferredSize(size);
		  
	  if((details != null) && (details.getWorkingVersion() != null) && 
	     details.getWorkingVersion().getSequences().contains(fseq))
	    label.addMouseListener(this);
	  
	  vbox.add(label);
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  TreeMap<String,JFileLabel> labels = new TreeMap<String,JFileLabel>();
	  pFileLabels.put(fseq.toString(), labels);

	  TextureMgr mgr = TextureMgr.getInstance();
	  try {
	    for(FileSeq sfseq : singles) {
	      FileState fstate  = fstates.get(sfseq);
	      QueueState qstate = qstates.get(sfseq);

	      String name = "Blank-Normal";
	      FileSeq pfseq = null;
	      if((fstate != null) && (qstate != null)) {
		name = (fstate + "-" + qstate + "-Normal");

		if(enabled.contains(sfseq))
		  pfseq = new FileSeq(path, sfseq);
	      }
	      
	      String fname = sfseq.getFile(0).toString();

	      ImageIcon icon = mgr.getIcon21(name);
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
	      
		{
		  JLabel label = new JLabel(icon);
		  hbox.add(label); 
		}
	      
		hbox.add(Box.createRigidArea(new Dimension(3, 0)));
		
		{
		  JFileLabel label = new JFileLabel(pfseq, fname);
		  label.setName("TextFieldLabel");

		  if((fstate != null) && (fstate != FileState.Identical))
		    label.setForeground(Color.cyan);

		  label.setHorizontalAlignment(JLabel.CENTER);

		  Dimension size = new Dimension(150, 19);
		  label.setMaximumSize(size);
		  label.setMaximumSize(size);
		  label.setPreferredSize(size);
		  
		  if(pfseq != null) 
		    label.addMouseListener(this);

		  labels.put(fname, label);
		  
		  hbox.add(label);
		}
		
		vbox.add(hbox);
	      }

	      vbox.add(Box.createRigidArea(new Dimension(0, 1)));
	    }
	  }
	  catch(IOException ex) {
	    Logs.tex.severe("Internal Error:\n" + 
			    "  " + ex.getMessage());
	    Logs.flush();
	    System.exit(1);
	  }
	}
	
  	vbox.add(Box.createRigidArea(new Dimension(0, 18)));

 	Dimension size = vbox.getPreferredSize();
 	vbox.setMinimumSize(size);
 	vbox.setMaximumSize(size);
	
	body.add(vbox);
      }

      body.add(Box.createRigidArea(new Dimension(3, 0)));

      /* file version novelty table */ 
      JViewport headerViewport = null;
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);

	vbox.add(Box.createRigidArea(new Dimension(0, 3)));

  	/* column header */ 
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
  		JButton btn = new JButton("v" + vid.toString());
  		btn.setName("TableHeaderButton");

		if((bvid != null) && bvid.equals(vid)) 
		  btn.setForeground(Color.cyan);

		btn.addActionListener(this);
		btn.setActionCommand
		  ("version-pressed:" + fseq.toString() + ":" + vid.toString());
		  
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
	    new JFileSeqPanel(this, fseq, singles, enabled, novel, pNovelty.size());

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

	vbox.add(Box.createRigidArea(new Dimension(0, 3)));

	body.add(vbox);
      }

      body.add(Box.createRigidArea(new Dimension(3, 0)));
    }

    pFileSeqBox.add(body);

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
      doEdit();
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10)); 
    else if(cmd.startsWith("compare-with:"))
      doCompareWith(cmd.substring(13)); 
  }


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
  mouseEntered(MouseEvent e) {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
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
	  Object source = e.getSource();
	  boolean hasWorking = false;
	  if(source instanceof JFileLabel) {
	    JFileLabel label  = (JFileLabel) source;
	    pTargetFileSeq    = label.getFileSeq();
	    pTargetVersionID  = label.getVersionID();
	    hasWorking        = label.hasWorking();
	  }
	  else if(source instanceof JFileCheckBox) {
	    JFileCheckBox check = (JFileCheckBox) source;
	    pTargetFileSeq      = check.getFileSeq();
	    pTargetVersionID    = check.getVersionID();
	    hasWorking          = check.hasWorking();
	  }

	  if(pTargetVersionID != null) {
	    pCompareWithMenu.setEnabled(hasWorking);
	    pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	  else 
	    pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
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



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Replace working files with the selected checked-in files.
   */ 
  private void 
  doApply()
  {
    String dir = (PackageInfo.sRepoDir + pStatus.getName() + "/");

    TreeMap<String,VersionID> files = new TreeMap<String,VersionID>();
    for(TreeMap<String,ArrayList<JComponent>> table : pNameComponents.values()) {
      for(ArrayList<JComponent> clist : table.values()) {
	for(JComponent comp : clist) {
	  if(comp instanceof JFileCheckBox) {
	    JFileCheckBox check = (JFileCheckBox) comp;
	    if(check.isSelected()) {
	      String path = check.getFileSeq().getFile(0).toString();
	      assert(path.startsWith(dir));

	      String parts[] = path.substring(dir.length()).split("/");
	      assert(parts.length == 2);

	      files.put(parts[1], new VersionID(parts[0]));
	    }
	  }
	}
      }
    }

    pApplyButton.setEnabled(false);
	  
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

    ArrayList<JComponent> comps = pNameComponents.get(sname).get(fname);

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
      else if(comp instanceof JFileLabel) {
	JFileLabel label = (JFileLabel) comp;
	if(label.getName().equals("FileBar"))
	  label.setIcon(sFileBarIcon);
	else 
	  label.setIcon(sFileBarExtendIcon);
      }
    }
    assert(idx >= 0);

    /* reselect the checkbox and attached file bar labels */ 
    if(selected) {
      check.setSelected(true);

      for(wk=idx-1; wk>=0; wk--) {
	JComponent comp = comps.get(wk);
	if(comp instanceof JFileLabel) {
	  JFileLabel label = (JFileLabel) comp;
	  if(label.getName().equals("FileBar"))
	    label.setIcon(sFileBarIconSelected);
	  else 
	    label.setIcon(sFileBarExtendIconSelected);
	}
	else {
	  break;
	}
      }
    }

    /* update the row label appearance */ 
    JFileLabel label = pFileLabels.get(sname).get(fname);
    label.setForeground(selected ? Color.yellow : Color.white);

    pApplyButton.setEnabled(true);
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
   * Edit/View the target file with the current editor.
   */ 
  private void 
  doEdit() 
  {
    EditTask task = new EditTask(null);
    task.start();
  }

  /**
   * Edit/View the target file with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    EditTask task = new EditTask(editor);
    task.start();
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
    CompareTask task = new CompareTask(comparator);
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A JLabel with an attached single file sequence and revision number.
   */ 
  private 
  class JFileLabel
    extends JLabel
  {
    public 
    JFileLabel
    (
     FileSeq fseq, 
     VersionID vid, 
     boolean hasWorking
    ) 
    {
      super();
      pFileSeq    = fseq;
      pVersionID  = vid;
      pHasWorking = hasWorking;
    }

    public 
    JFileLabel
    (
     FileSeq fseq,
     String text 
    ) 
    {
      super(text);
      pFileSeq = fseq;
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


    private static final long serialVersionUID = 1637624990297368379L;

    private FileSeq    pFileSeq;
    private VersionID  pVersionID;
    private boolean    pHasWorking;
  }


  /*----------------------------------------------------------------------------------------*/

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
     TreeSet<FileSeq> singles,
     TreeSet<FileSeq> enabled, 
     TreeMap<FileSeq,Boolean[]> flags, 
     int numVersions
    ) 
    {
      super();
      
      TreeMap<String,ArrayList<JComponent>> nameComps = 
	new TreeMap<String,ArrayList<JComponent>>();
      pNameComponents.put(fseq.toString(), nameComps);

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

		String path = (PackageInfo.sRepoDir + pStatus.getName() + "/" + vid);
		FileSeq pfseq = new FileSeq(path, sfseq);
		
		boolean isEnabled = enabled.contains(sfseq);

		if(novel[wk]) {
		  if((wk > 0) && (novel[wk-1] != null) && novel[wk-1])
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));		  
		  
		  {
		    JFileCheckBox check = new JFileCheckBox(pfseq, vid, isEnabled);
		    check.setName("FileCheck");

		    check.setSelected(false);

		    Dimension size = new Dimension(12, 19);
		    check.setMinimumSize(size);
		    check.setMaximumSize(size);
		    check.setPreferredSize(size);
		    
		    if(isEnabled) {
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
		    
		    check.addMouseListener(parent);

		    hbox.add(check);
		  }
		  
		  hbox.add(Box.createRigidArea(new Dimension(56, 0)));
		}
		else {
		  boolean extend = false; 
		  if((wk > 0) && (novel[wk-1] != null)) {
		    if(!novel[wk-1])
		      extend = true;
		    else 
		      hbox.add(Box.createRigidArea(new Dimension(2, 0)));		 
		  }
		  
		  {
		    JFileLabel label = new JFileLabel(pfseq, vid, isEnabled);
		    label.setName(extend ? "FileBarExtend" : "FileBar");
		    label.setIcon(extend ? sFileBarExtendIcon : sFileBarIcon);

		    Dimension size = new Dimension(70, 19);
		    label.setMinimumSize(size);
		    label.setMaximumSize(size);
		    label.setPreferredSize(size);

		    if(isEnabled) 
		      nlist.add(label);
		    
		    label.addMouseListener(parent);

		    hbox.add(label);	
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
   * Links two viewports horizontal position.
   */ 
  private 
  class AdjustLinkage
    implements AdjustmentListener
  {
    public 
    AdjustLinkage
    (
     JViewport source, 
     JViewport target
    ) 
    {
      pSource = source;
      pTarget = target; 
    }

    public void
    adjustmentValueChanged
    (
     AdjustmentEvent e
    )
    { 
      Point spos = pSource.getViewPosition();    
      Point tpos = pTarget.getViewPosition();
	
      if(spos.x != tpos.x) {
	tpos.x = spos.x;
	pTarget.setViewPosition(tpos);
      }
    }    
    
    private JViewport  pSource;
    private JViewport  pTarget;
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
     String ename
    ) 
    {
      super("JNodeFilesPanel:EditTask");

      pEditorName = ename;
    }

    public void 
    run() 
    {
      SubProcess proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Editor...")) {
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();

	    String name = pStatus.getName();

	    NodeDetails details = pStatus.getDetails();
	    assert(details != null);

	    NodeMod mod = details.getWorkingVersion();

	    NodeVersion vsn = null;
	    if(pTargetVersionID != null) 
	      vsn = client.getCheckedInVersion(name, pTargetVersionID);
	    else 
	      vsn = details.getLatestVersion();

	    NodeCommon com = null;
	    if(mod != null) 
	      com = mod;
	    else 
	      com = vsn;

	    /* create an editor plugin instance */ 
	    BaseEditor editor = null;
	    {
	      String ename = pEditorName;
	      if(ename == null) 
		ename = com.getEditor();
	      if(ename == null) 
		throw new PipelineException
		  ("No editor was specified for node (" + name + ")!");
	      
	      editor = Plugins.newEditor(ename);
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = com.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + name + ")!");
	      
	      String view = null;
	      if(mod != null)
		view = pView; 

	      /* passes pAuthor so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment(pAuthor, view, tname);

	      /* override these since the editor will be run as the current user */ 
	      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
	      env.put("USER", PackageInfo.sUser);
	    }
	    
	    /* start the editor */ 
	    proc = editor.launch(pTargetFileSeq, env, PackageInfo.sTempDir);	   
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}

	/* wait for the editor to exit */ 
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

    private String  pEditorName;
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
     String ename
    ) 
    {
      super("JNodeFilesPanel:CompareTask");

      pComparatorName = ename;
    }

    public void 
    run() 
    {
      SubProcess proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Comparator...")) {
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();

	    String name = pStatus.getName();

	    NodeDetails details = pStatus.getDetails();
	    assert(details != null);

	    NodeMod mod = details.getWorkingVersion();

	    NodeVersion vsn = null;
	    if(pTargetVersionID != null) 
	      vsn = client.getCheckedInVersion(name, pTargetVersionID);
	    else 
	      vsn = details.getLatestVersion();

	    NodeCommon com = null;
	    if(mod != null) 
	      com = mod;
	    else 
	      com = vsn;

	    /* create an comparator plugin instance */ 
	    BaseComparator comparator = Plugins.newComparator(pComparatorName);

	    /* the checked-in file */ 
	    File fileB = new File(pTargetFileSeq.toString());

	    /* the working file */ 
	    File fileA = null;
	    {
	      File path = new File(pStatus.getName());
	      fileA = new File(PackageInfo.sWorkDir, 
			       pAuthor + "/" + pView + path.getParent() + "/" + 
			       fileB.getName());
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = com.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + name + ")!");
	      
	      String view = null;
	      if(mod != null)
		view = pView; 

	      /* passes pAuthor so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment(pAuthor, view, tname);

	      /* override these since the comparator will be run as the current user */ 
	      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
	      env.put("USER", PackageInfo.sUser);
	    }
	    
	    /* start the comparator */ 
	    proc = comparator.launch(fileA, fileB, env, PackageInfo.sTempDir);	   
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

    private String  pComparatorName;
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;

  private static final int  sSize = 484;


  private static Icon sFileBarIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIcon.png"));

  private static Icon sFileBarExtendIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIcon.png"));

  private static Icon sFileBarIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIconSelected.png"));

  private static Icon sFileBarExtendIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIconSelected.png"));



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
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu.
   */ 
  private JPopupMenu  pWorkingPopup; 

  /**
   * The checked-in file popup menu.
   */ 
  private JPopupMenu  pCheckedInPopup; 
  
  /**
   * The compare with submenu.
   */ 
  private JMenu  pCompareWithMenu;

  
  /**
   * The file which is the target of the menu operation.
   */ 
  private FileSeq  pTargetFileSeq; 

  /**
   * The revision number of the checked-in version which is the target of the menu operation.
   */ 
  private VersionID  pTargetVersionID;


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
   * The file sequences container.
   */ 
  private Box  pFileSeqBox;

  /**
   * The file name labels indexed by file sequence (String) and file name.
   */ 
  private TreeMap<String,TreeMap<String,JFileLabel>>  pFileLabels;  

  /**
   * The file novelty UI components indexed by file sequence (String) and file name.
   */ 
  private TreeMap<String,TreeMap<String,ArrayList<JComponent>>>  pNameComponents;

  /**
   * The file novelty check boxes indexed by file sequence (String), revision number (String)
   * and file name.
   */ 
  private TreeMap<String,TreeMap<String,TreeMap<String,JFileCheckBox>>>  pVersionBoxes;

}
