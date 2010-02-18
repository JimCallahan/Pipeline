// $Id: JNodeFilesPanel.java,v 1.59 2009/10/07 08:09:50 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.*;
import us.temerity.pipeline.ui.*;

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
  extends JBaseNodeDetailPanel
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
      pFileSeqPanels = new TreeMap<FileSeq,JFileSeqPanel>(); 
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
        pApplyToolTipText = 
          "Replace the working area files with the selected checked-in files.";
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
        JTabbedPane tab = new JTabbedPane(); 
        pFileSeqsTab = tab; 
        add(tab); 
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
  @Override
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
  @Override
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
    PanelGroup<JNodeFilesPanel> panels = UIMaster.getInstance().getNodeFilesPanels();
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
  
  /**
   * Get the primary file sequence of the working version of the node being viewed
   * <CODE>null</CODE> if no working version is being viewed.
   */ 
  public FileSeq
  getPrimarySequence() 
  {
    if(pStatus != null) {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details != null) {
        NodeMod mod = details.getWorkingVersion();
        if(mod != null) 
          return mod.getPrimarySequence();
      }
    }
    
    return null;
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
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  @Override
  public void 
  postPanelOp() 
  {
    for(JFileSeqPanel panel : pFileSeqPanels.values()) 
      panel.setApplyItemEnabled(false);
 
    super.postPanelOp();
  }
  
  /**
   * Register the name of a panel property which has just been modified.
   */ 
  @Override
  public void
  unsavedChange
  (
   String name
  )
  {
    for(JFileSeqPanel panel : pFileSeqPanels.values()) 
      panel.setApplyItemEnabled(true);

    super.unsavedChange(name); 
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
   * @param novelty
   *   The per-file novelty flags.
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
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty, 
   TreeSet<VersionID> offline
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateNodeStatus(status, novelty, offline);
  }


  /*----------------------------------------------------------------------------------------*/

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
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty,
   TreeSet<VersionID> offline
  ) 
  {    
    super.updateNodeStatus(status, false);

    pNovelty = novelty; 
    pOffline = offline;

    NodeDetailsLight details = null;
    if(pStatus != null) 
      details = pStatus.getLightDetails();

    /* files */ 
    {
      pFileSeqsTab.removeAll();
      pFileSeqPanels.clear(); 

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
	addFileSeqPanel(primary);
        for(FileSeq fseq : secondary) 
          addFileSeqPanel(fseq);
      }
    }
      
    pFileSeqsTab.revalidate();
  }


  /**
   * Add the UI components for the given file sequence to the panel.
   */ 
  private void 
  addFileSeqPanel
  (
   FileSeq fseq
  ) 
  { 
    boolean isPresentInWorking = false;
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();
      NodeMod mod = details.getWorkingVersion();
      if(mod != null) {
        if(mod.getPrimarySequence().equals(fseq)) 
          isPresentInWorking = true;
        else {
          for(FileSeq sfseq : mod.getSecondarySequences()) {
            if(sfseq.equals(fseq)) {
              isPresentInWorking = true;
              break;
            }
          }
        }
      }
    }

    /* collate the row information */ 
    ArrayList<VersionID> vids = new ArrayList<VersionID>(); 
    ArrayList<FileSeq> singles = new ArrayList<FileSeq>(); 
    TreeSet<FileSeq> enabled = new TreeSet<FileSeq>();
    TreeMap<FileSeq,FileState>  fstates = new TreeMap<FileSeq,FileState>();
    TreeMap<FileSeq,NativeFileInfo> finfos = new TreeMap<FileSeq,NativeFileInfo>();
    TreeMap<FileSeq,QueueState> qstates = new TreeMap<FileSeq,QueueState>();
    TreeMap<FileSeq,Boolean[]> novel = new TreeMap<FileSeq,Boolean[]>();
    {
      TreeMap<FileSeq,Integer> wsingles = new TreeMap<FileSeq,Integer>();	 
      if((pStatus != null) && pStatus.hasLightDetails()) {
	if(isPresentInWorking) {
          if(pStatus.hasHeavyDetails()) {
            NodeDetailsHeavy details = pStatus.getHeavyDetails();

            FileState[]  fs = details.getFileStates(fseq);
            QueueState[] qs = details.getQueueStates();
            NativeFileInfo[] infos = details.getFileInfos(fseq); 
            if((fs != null) && (qs != null) && (infos != null)) {
              int wk;
              for(wk=0; wk<fs.length; wk++) {
                FileSeq sfseq = new FileSeq(fseq, wk);
                wsingles.put(sfseq, wk);
                
                fstates.put(sfseq, fs[wk]);
                finfos.put(sfseq, infos[wk]); 
                qstates.put(sfseq, qs[wk]);
                
                if(fs[wk] != FileState.CheckedIn) 
                  enabled.add(sfseq);
              }
            }
          }
          else {
            NodeDetailsLight details = pStatus.getLightDetails();

            int wk;
            for(wk=0; wk<fseq.numFrames(); wk++) {
              FileSeq sfseq = new FileSeq(fseq, wk);
              wsingles.put(sfseq, wk);
              
              if(details.getVersionState() == VersionState.CheckedIn) {
                fstates.put(sfseq, FileState.CheckedIn); 
                qstates.put(sfseq, QueueState.Undefined);
              }
              else {
                enabled.add(sfseq);
              }
            }
          }
        }
	
	{
	  vids.addAll(pNovelty.keySet());
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
		  if(!wsingles.containsKey(sfseq)) 
		    wsingles.put(sfseq, null);

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

      TreeMap<Integer,FileSeq> order = new TreeMap<Integer,FileSeq>();
      for(FileSeq sfseq : wsingles.keySet()) {
	int frame = -1;
	if(sfseq.hasFrameNumbers()) 
	  frame = sfseq.getFrameRange().getStart();
	
	order.put(frame, sfseq);
      }

      singles.addAll(order.values());
    }
    
    /* add the panel */ 
    {
      JFileSeqPanel panel = 
        new JFileSeqPanel(this, pManagerPanel, pStatus, pPrivilegeDetails, 
                          fseq, vids, pOffline, singles, fstates, finfos, qstates, 
                          enabled, novel); 

      pFileSeqsTab.addTab(fseq.getFilePattern().toString(), sTabIcon, panel); 
      pFileSeqPanels.put(fseq, panel); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  @Override
  public void 
  clearPluginCache()
  {
    super.clearPluginCache();
    for(JFileSeqPanel panel : pFileSeqPanels.values()) 
      panel.clearPluginCache(); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the menu item tool tips.
   */ 
  @Override
  protected void 
  updateMenuToolTips() 
  {
    super.updateMenuToolTips(); 

    UserPrefs prefs = UserPrefs.getInstance();       
    updateMenuToolTip
      (pApplyItem, prefs.getApplyChanges(),
       "Apply the changes to the working version.");
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

    TreeMap<String,VersionID> files = new TreeMap<String,VersionID>();
    for(JFileSeqPanel panel : pFileSeqPanels.values()) 
      files.putAll(panel.getFilesToRevert());

    RevertTask task = new RevertTask(files);
    task.start();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Reverting Files...")) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  client.revertFiles(pAuthor, pView, pStatus.getName(), pFiles);
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

    private TreeMap<String,VersionID>  pFiles;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;

  private static final int  sSize = 484;


  private static final Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The per-file novelty flags.
   */ 
  private TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>  pNovelty;

  /**
   * The revision numbers of the offline checked-in versions.
   */ 
  private TreeSet<VersionID>  pOffline; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An icon which indicates whether the working version is frozen.
   */
  //private boolean  pIsFrozen; 
  private JLabel   pFrozenLabel;

  /**
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;

  /**
   * The tabbed container of file sequence panels. 
   */ 
  private JTabbedPane  pFileSeqsTab;

  /**
   * The panel for each file sequence.
   */ 
  private TreeMap<FileSeq,JFileSeqPanel>  pFileSeqPanels;

}
