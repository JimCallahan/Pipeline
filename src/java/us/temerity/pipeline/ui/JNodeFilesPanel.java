// $Id: JNodeFilesPanel.java,v 1.1 2004/07/14 21:03:49 jim Exp $

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
  implements ActionListener
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
    }

    /* files */ 
    {
      pFileSeqBox.removeAll();

      pVersionBoxes   = new TreeMap<String,TreeMap<String,TreeMap<String,JCheckBox>>>();
      pNameComponents = new TreeMap<String,TreeMap<String,ArrayList<JComponent>>>();

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

	/* add the primary file sequence */ 
	FileSeq primary = com.getPrimarySequence();
	addFileSeqComponents(primary, true);

	ArrayList<FileSeq> secondary = 
	  new ArrayList<FileSeq>(com.getSecondarySequences());

	/* add the secondary sequences of the working/latest version */ 
	for(FileSeq fseq : secondary) 
	  addFileSeqComponents(fseq, true);
	
	/* add the secondary sequences from previous versions which are not similar
	   to the secondary sequences of the working/latest version */ 
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
	  
	  if(!found) {
	    addFileSeqComponents(ufseq, false);
	    secondary.add(ufseq);
	  }
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
   boolean hasStates
  ) 
  { 
    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    /* collate the row information */ 
    TreeSet<String> allFiles = new TreeSet<String>();
    TreeMap<String,FileState>  fstates = new TreeMap<String,FileState>();
    TreeMap<String,QueueState> qstates = new TreeMap<String,QueueState>();
    TreeMap<String,Boolean[]> novel = new TreeMap<String,Boolean[]>();
    {
      if(details != null) {
	{
	  ArrayList<File> files = fseq.getFiles();
	  
	  FileState[]  fs = details.getFileState(fseq);
	  QueueState[] qs = details.getQueueState();
	  if((fs != null) && (qs != null)) {
	    assert(fs.length == files.size());
	    assert(qs.length == files.size());
	    
	    int wk;
	    for(wk=0; wk<fs.length; wk++) {
	      String fname = files.get(wk).toString();
	      assert(fname != null);
	      
	      fstates.put(fname, fs[wk]);
	      qstates.put(fname, qs[wk]);
	    }
	    
	    allFiles.addAll(fstates.keySet());
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
		ArrayList<File> files = nfseq.getFiles();
		assert(flags.length == files.size());
		
		int wk;
		for(wk=0; wk<flags.length; wk++) {
		  String fname = files.get(wk).toString();
		  assert(fname != null);
		  
		  Boolean[] rflags = novel.get(fname);
		  if(rflags == null) {
		    rflags = new Boolean[pNovelty.size()];
		    novel.put(fname, rflags);
		  }
		  
		  rflags[idx] = new Boolean(flags[wk]);
		}
		
		break;
	      }
	    }

	    idx++;
	  }
	  
	  allFiles.addAll(novel.keySet());
	}
      }
    }
      
    /* the drawer body */ 
    Box body = null;
    {
      body = new Box(BoxLayout.X_AXIS);
      
      body.add(Box.createRigidArea(new Dimension(3, 0)));
	
      /* file state/name fields */ 
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 35)));
	
	{
	  Box box = new Box(BoxLayout.Y_AXIS);

	  TextureMgr mgr = TextureMgr.getInstance();
	  try {
	    for(String fname : allFiles) {
	      FileState fstate  = fstates.get(fname);
	      QueueState qstate = qstates.get(fname);
	      
	      String name = "Blank-Normal";
	      if((fstate != null) && (qstate != null)) 
		name = (fstate + "-" + qstate + "-Normal");
	      
	      ImageIcon icon = mgr.getIcon21(name);
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
	      
		{
		  JLabel label = new JLabel(icon);
		  hbox.add(label); 
		}
	      
		hbox.add(Box.createRigidArea(new Dimension(3, 0)));
		
		{
		  JTextField field = new JTextField(fname);
		  field.setName("TextField");

		  if((fstate != null) && (fstate != FileState.Identical))
		    field.setForeground(Color.cyan);

		  field.setHorizontalAlignment(JLabel.CENTER);
		  field.setEditable(false);

		  Dimension size = new Dimension(150, 19);
		  field.setMaximumSize(size);
		  field.setMaximumSize(size);
		  field.setPreferredSize(size);

		  hbox.add(field);
		}
		
		box.add(hbox);
	      }

	      box.add(Box.createRigidArea(new Dimension(0, 1)));
	    }
	  }
	  catch(IOException ex) {
	    Logs.tex.severe("Internal Error:\n" + 
			    "  " + ex.getMessage());
	    Logs.flush();
	    System.exit(1);
	  }

	  Dimension size = box.getPreferredSize();
	  box.setMinimumSize(size);
	  box.setMaximumSize(size);
	 
	  vbox.add(box);
	}
	
  	vbox.add(Box.createRigidArea(new Dimension(0, 18)));

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
	    new JFileSeqPanel(this, fseq, allFiles, novel, pNovelty.size());

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

    JDrawer drawer = new JDrawer(fseq.toString(), body, true);
    pFileSeqBox.add(drawer);
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
    
    System.out.print("ACTION = " + cmd + "\n");

     if(cmd.equals("apply")) 
       doApply();
     else if(cmd.startsWith("file-check:")) {
       String comps[] = cmd.split(":");
       doFileChecked((JCheckBox) e.getSource(), comps[1], comps[2]);      
     }
     else if(cmd.startsWith("version-pressed:")) {
       String comps[] = cmd.split(":");
       doVersionPressed(comps[1], comps[2]);
     }
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
    

  }

  /**
   * 
   */ 
  private void 
  doFileChecked
  (
   JCheckBox check, 
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
      if(comp instanceof JCheckBox) {
	JCheckBox cb = (JCheckBox) comp;
	cb.setSelected(false);
	if(cb == check) 
	  idx = wk;
      }
      else if(comp instanceof JLabel) {
	JLabel label = (JLabel) comp;
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
	if(comp instanceof JLabel) {
	  JLabel label = (JLabel) comp;
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
  }
  
  /**
   * 
   */ 
  private void 
  doVersionPressed
  (
   String sname, 
   String vname
  ) 
  {
    Boolean selected = null;
    TreeMap<String,JCheckBox> table = pVersionBoxes.get(sname).get(vname);
    if(table != null) {
      for(String fname : table.keySet()) {
	JCheckBox check = table.get(fname);	

	if(selected == null) 
	  selected = !check.isSelected();
	check.setSelected(selected); 
	
	doFileChecked(check, sname, fname);
      }
    }
  }

  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
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
     * @param files
     *   The names of all of the files to display.
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
     TreeSet<String> files,
     TreeMap<String,Boolean[]> flags, 
     int numVersions
    ) 
    {
      super();
      
      TreeMap<String,ArrayList<JComponent>> nameComps = 
	new TreeMap<String,ArrayList<JComponent>>();
      pNameComponents.put(fseq.toString(), nameComps);

      TreeMap<String,TreeMap<String,JCheckBox>> versionBoxes = 
	new TreeMap<String,TreeMap<String,JCheckBox>>();
      pVersionBoxes.put(fseq.toString(), versionBoxes);
      
      ArrayList<VersionID> vids = new ArrayList<VersionID>(pNovelty.keySet());
      Collections.reverse(vids);

      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	add(Box.createRigidArea(new Dimension(0, 1)));
	
	int fk=0;
	for(String fname : files) {
	  Boolean[] novel = flags.get(fname);
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
		hbox.add(Box.createRigidArea(new Dimension(70, 0)));
	      }
	      else {
		ArrayList<JComponent> nlist = nameComps.get(fname);
		if(nlist == null) {
		  nlist = new ArrayList<JComponent>();
		  nameComps.put(fname, nlist);
		}

		TreeMap<String,JCheckBox> vboxes = versionBoxes.get(vid.toString());
		if(vboxes == null) {
		  vboxes = new TreeMap<String,JCheckBox>();
		  versionBoxes.put(vid.toString(), vboxes);
		}

		if(novel[wk]) {
		  if((wk > 0) && (novel[wk-1] != null) && novel[wk-1])
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));		  
		  
		  {
		    JCheckBox check = new JCheckBox();
		    check.setName("FileCheck");

		    check.setSelected(false);

		    Dimension size = new Dimension(12, 19);
		    check.setMinimumSize(size);
		    check.setMaximumSize(size);
		    check.setPreferredSize(size);
		  
		    check.addActionListener(parent);
		    check.setActionCommand("file-check:" + fseq + ":" + fname);

		    nlist.add(check);
		    vboxes.put(fname, check);

		    int vk;
		    for(vk=wk-1; vk>=0; vk--) {
		      if((novel[vk] != null) && !novel[vk]) {
			VersionID pvid = vids.get(vk);

			TreeMap<String,JCheckBox> pvboxes = versionBoxes.get(pvid.toString());
			if(pvboxes == null) {
			  pvboxes = new TreeMap<String,JCheckBox>();
			  versionBoxes.put(pvid.toString(), pvboxes);
			}
			
			pvboxes.put(fname, check);
		      }
		      else {
			break;
		      }
		    }
  
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
		    JLabel label = new JLabel();
		    label.setName(extend ? "FileBarExtend" : "FileBar");
		    label.setIcon(extend ? sFileBarExtendIcon : sFileBarIcon);

		    Dimension size = new Dimension(70, 19);
		    label.setMinimumSize(size);
		    label.setMaximumSize(size);
		    label.setPreferredSize(size);
		    
		    nlist.add(label);

		    hbox.add(label);	
		  }
		}
	      }
	    }
	    
	    hbox.add(Box.createHorizontalGlue());
	    
	    add(hbox);
	  }
	  
	  if(fk < (files.size()-1)) 
	    add(Box.createRigidArea(new Dimension(0, 3)));
	  
	  fk++;
	}
	
	add(Box.createRigidArea(new Dimension(0, 1)));
	
	Dimension size = new Dimension(70*numVersions, 20 + 22*(files.size()-1));
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
   * The file novelty UI components indexed by file sequence (String) and file name.
   */ 
  private TreeMap<String,TreeMap<String,ArrayList<JComponent>>>  pNameComponents;

  /**
   * The file novelty check boxes indexed by file sequence (String), revision number (String)
   * and file name.
   */ 
  private TreeMap<String,TreeMap<String,TreeMap<String,JCheckBox>>>  pVersionBoxes;

}
