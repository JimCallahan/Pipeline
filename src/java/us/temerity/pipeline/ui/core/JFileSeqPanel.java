// $Id: JFileSeqPanel.java,v 1.2 2009/10/07 08:09:50 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*; 

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E Q   P A N E L                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JFileSeqPanel
  extends JPanel
  implements MouseListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new file sequence panel.
   * 
   * @param parent
   *   The parent node files panel.
   * 
   * @param mpanel
   *   The parent manager panel.
   *
   * @param status
   *   The current node status.
   * 
   * @param privilegeDetails
   *   The details of the administrative privileges granted to the current user. 
   * 
   * @param fseq
   *   The full working file sequence. 
   * 
   * @param vids
   *   The revision numbers (newest to oldest) of the versions to display. 
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   * 
   * @param singles
   *   The single file sequences for all files to display.
   * 
   * @param fstates
   *   The file states of the working files.
   * 
   * @param finfos
   *   The per-file status information of the working files.
   * 
   * @param qstates
   *   The queue states of the working files.
   * 
   * @param enabled
   *   The single file sequences which have defined states.
   * 
   * @param novel
   *   The per-version (newest to oldest) file novelty flags indexed by filename.
   * 
   * @param isListLayout
   *   Whether to layout this panel in list format (true) or tabbed panel layout (false). 
   */ 
  public
  JFileSeqPanel
  (
   JNodeFilesPanel parent, 
   JManagerPanel mpanel,  
   NodeStatus status, 
   PrivilegeDetails privilegeDetails, 
   FileSeq fseq, 
   ArrayList<VersionID> vids,
   SortedSet<VersionID> offline, 
   ArrayList<FileSeq> singles,
   TreeMap<FileSeq,FileState> fstates, 
   TreeMap<FileSeq,NativeFileInfo> finfos, 
   TreeMap<FileSeq,QueueState> qstates, 
   SortedSet<FileSeq> enabled, 
   SortedMap<FileSeq,Boolean[]> novel,
   boolean isListLayout
  )
  {
    super();

    /* initialize fields */ 
    {
      pParent           = parent;
      pManagerPanel     = mpanel;
      pStatus           = status; 
      pPrivilegeDetails = privilegeDetails; 

      NodeDetailsLight details = null;
      if(pStatus != null) 
        details = pStatus.getLightDetails();

      pIsReadOnly   = pParent.isLocked(); 
      pIsFrozen     = false;
      pHasWorking   = false;
      pHasCheckedIn = false;
      if(details != null) {
	NodeMod mod = details.getWorkingVersion();
        if(mod != null) {
          pHasWorking = true;
          if(mod.isFrozen()) {
            pIsReadOnly = true;
            pIsFrozen = true;
          }
        }
      }

      pHasCheckedIn = !vids.isEmpty();

      pTargetSeqs = new TreeMap<FileSeq,Integer>();
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;

      pWorkingPopup   = new JPopupMenu();  
      pReadOnlyPopup    = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu();  

      {
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
	item.setActionCommand("apply");
	item.addActionListener(this);
        item.setEnabled(false); 
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();
      }

      pEditItems            = new JMenuItem[3];
      pEditWithDefaultItems = new JMenuItem[3];
      pEditWithMenus        = new JMenu[3];

      JPopupMenu menus[] = { pWorkingPopup, pReadOnlyPopup, pCheckedInPopup };
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
      
      item = new JMenuItem("Edit As Owner");
      pEditAsOwnerItem = item;
      item.setActionCommand("edit-as-owner");
      item.addActionListener(this);
      pWorkingPopup.add(item);

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
	
	pWorkingPopup.addSeparator();

        item = new JMenuItem("Vouch"); 
        pVouchItem = item;
        item.setActionCommand("vouch");
        item.addActionListener(this);
        pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();

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

	item = new JMenuItem("Preempt/Pause Jobs");
	pPreemptAndPauseJobsItem = item;
	item.setActionCommand("preempt-pause-jobs");
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

      //updateMenuToolTips();
    }

    /* initialize panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	

      if(isListLayout) 
        add(UIFactory.createPanelBreak()); 

      add(Box.createRigidArea(new Dimension(0, 4)));
      
      /* file sequence header */ 
      {
        Box hbox = new Box(BoxLayout.X_AXIS);
        
        hbox.add(Box.createRigidArea(new Dimension(4, 0)));
        
        {
          VersionID vid = null;
          if(!pHasWorking && !vids.isEmpty())
            vid = vids.get(0);
          
          JFileSeqLabel label = new JFileSeqLabel(fseq, vid); 
          pHeaderLabel = label;
          
          label.addMouseListener(this); 
          label.setFocusable(true);
          label.addKeyListener(this);
          label.addMouseListener(new KeyFocuser(label));
          
          hbox.add(label);
        }
        
        hbox.add(Box.createRigidArea(new Dimension(4, 0)));
        
        add(hbox); 
      }
    
      add(Box.createRigidArea(new Dimension(0, 4)));
    
      /* per-file tables */ 
      {
        JPanel panel = new JPanel();
        panel.setName("MainPanel");
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
      
        {
          pTableModel = 
            new FileSeqTableModel(this, fseq, pIsFrozen, pIsReadOnly, 
                                  vids, offline, singles, enabled, fstates, finfos,
                                  qstates, novel); 

          int vpolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS; 
          if(isListLayout) 
            vpolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER; 

          
          Dimension minSize  = null;
          Dimension prefSize = null;
          Dimension maxSize  = null;
          if(isListLayout) {
            int h = 22*singles.size() + 22;
            minSize  = new Dimension(0, h); 
            prefSize = new Dimension(484, h); 
            maxSize  = new Dimension(Integer.MAX_VALUE, h); 
          }

          JTablePanel tpanel =
            new JTablePanel(pTableModel, 
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, vpolicy, 
                            minSize, prefSize, maxSize); 
                            
          pTablePanel = tpanel;
          
          {
            JTable table = tpanel.getTable();
            table.setIntercellSpacing(new Dimension(0, 3));

            table.addMouseListener(this); 
            table.setFocusable(true);
            table.addKeyListener(this);
            table.addMouseListener(new KeyFocuser(table));
          }
        
          panel.add(tpanel);
        }

        add(panel); 
      }

      pTableModel.sort();
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names and revision numbers of the file selected for reversion. 
   */ 
  public TreeMap<String,VersionID> 
  getFilesToRevert()
  {
    return pTableModel.getFilesToRevert();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the menus.
   * 
   * @param perFile
   *   Whether individual files are selected. 
   */ 
  public void 
  updateMenus
  (
   boolean perFile
  ) 
  {
    String author = pParent.getAuthor(); 
    int groupID   = pParent.getGroupID(); 

    boolean queuePrivileged = 
      (PackageInfo.sUser.equals(author) || pPrivilegeDetails.isQueueManaged(author));

    boolean nodePrivileged = 
      (PackageInfo.sUser.equals(author) || pPrivilegeDetails.isNodeManaged(author));

    boolean hasHeavy = ((pStatus != null) && pStatus.hasHeavyDetails()); 
    boolean hasLight = ((pStatus != null) && pStatus.hasLightDetails()); 
    boolean queueOps = queuePrivileged && (hasHeavy || (!perFile && hasLight)); 

    pQueueJobsItem.setEnabled(queuePrivileged);
    pQueueJobsSpecialItem.setEnabled(queuePrivileged);

    pVouchItem.setEnabled(queuePrivileged);

    pPauseJobsItem.setEnabled(queueOps); 
    pResumeJobsItem.setEnabled(queueOps); 
    pPreemptJobsItem.setEnabled(queueOps); 
    pPreemptAndPauseJobsItem.setEnabled(queueOps); 
    pKillJobsItem.setEnabled(queueOps); 

    pRemoveFilesItem.setEnabled(nodePrivileged);  

    String toolset = null;
    if(hasLight) {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details.getWorkingVersion() != null) 
        toolset = details.getWorkingVersion().getToolset();
      else if(details.getLatestVersion() != null) 
        toolset = details.getLatestVersion().getToolset();
    }
      
    if((toolset != null) && !toolset.equals(pMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      for(JMenu menu : pEditWithMenus) 
        master.rebuildEditorMenu(groupID, toolset, menu, this);
      master.rebuildComparatorMenu(groupID, toolset, pCompareWithMenu, this);  
      pMenuToolset = toolset;
    }

    for(JMenu menu : pEditWithMenus) 
      menu.setEnabled(hasLight);       
    pCompareWithMenu.setEnabled(hasLight);

    pEditAsOwnerItem.setEnabled(pPrivilegeDetails.isNodeManaged(author) && 
                                !PackageInfo.sUser.equals(author) && 
                                (PackageInfo.sOsType != OsType.Windows));
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    pMenuToolset = null;
  }

  
  /**
   * Set whether the Apply menu item should be enabled.
   */ 
  public void 
  setApplyItemEnabled
  (
   boolean tf
  ) 
  {
    if(pApplyItem != null) 
      pApplyItem.setEnabled(tf);
  }

  /**
   * Register the name of a panel property which has just been modified.
   */ 
  public void
  unsavedChange
  (
   String name
  )
  {
    pParent.unsavedChange(name); 
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
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local popups */ 
    int mods = e.getModifiersEx();
    Object source = e.getSource();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	/* BUTTON3: popup menus */ 
	if((mods & (on1 | off1)) == on1) {
          pTargetAll = false;
          pTargetSeqs.clear(); 
          pTargetVersionID = null;

          if(source instanceof JFileSeqLabel) {
            JFileSeqLabel slabel = (JFileSeqLabel) source; 
            pTargetAll = true;
            pTargetSeqs.put(slabel.getFileSeq(), null);
            VersionID vid = slabel.getVersionID(); 
            if(vid != null) 
              pTargetVersionID = vid;

            JPopupMenu menu = null;
            if(pIsReadOnly) 
              menu = pReadOnlyPopup; 
            else if(pHasWorking) 
              menu = pWorkingPopup; 
            else if(pHasCheckedIn) 
              menu = pCheckedInPopup; 
            
            if(menu != null) {
              updateMenus(false);
              menu.show(e.getComponent(), e.getX(), e.getY());
            }
          }
          else if(source instanceof JTable) {
            JTable table = pTablePanel.getTable();

            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if((row != -1) && (col != -1)) {
              FileSeq fseq = pTableModel.getFileSeq(row);
              Integer idx  = pTableModel.getFileIndex(row);
              if(col < 4) {
                pTargetSeqs.put(fseq, idx); 
                for(int srow : table.getSelectedRows()) {
                  if(pTableModel.isEnabled(srow)) {
                    pTargetSeqs.put(pTableModel.getFileSeq(srow), 
                                    pTableModel.getFileIndex(srow));
                  }
                }
              }
              else {
                Boolean novel = pTableModel.getNovelty(row, col);
                if(novel == null)
                  return;
                
                pTargetSeqs.put(fseq, idx); 
                pTargetVersionID = pTableModel.getNoveltyColumnVersion(col);
              }
              
              JPopupMenu menu = null;
              if(pTableModel.isEnabled(row) || (col > 1)) {
                if(pIsReadOnly) 
                  menu = pReadOnlyPopup;
                else if(pTargetVersionID == null) 
                  menu = pWorkingPopup;
                else 
                  menu = pCheckedInPopup;
              }
              
              if(menu != null) {
                updateMenus(true);
                menu.show(e.getComponent(), e.getX(), e.getY());
              }
            }
          }
	}
	else {
	  if(UIFactory.getBeepPreference())
	    Toolkit.getDefaultToolkit().beep();
	}
      }
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

    //...

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
    if(pTargetSeqs.isEmpty()) 
      return;

    String cmd = e.getActionCommand();
    if(cmd.equals("edit"))  
      doEditWith(null, false, false);
    else if(cmd.equals("edit-with-default"))
      doEditWith(null, true, false);
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10), false, false); 
    else if(cmd.equals("edit-as-owner"))
      doEditWith(null, false, true);  
    else if(cmd.startsWith("compare-with:"))
      doCompareWith(cmd.substring(13)); 

    else if(cmd.equals("queue-jobs"))
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special"))
      doQueueJobsSpecial();
    
    else if(cmd.equals("vouch"))
      doVouch();

    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("preempt-jobs"))
      doPreemptJobs();
    else if(cmd.equals("preempt-pause-jobs"))
      doPreemptAndPauseJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("remove-files"))
      doRemoveFiles();    
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the target file sequences with the given editor.
   * 
   * @param editor
   *   The name of the editor plugin or <CODE>null</CODE> for the node's editor.
   * 
   * @param useDefault
   *   Whether to use the default editor for the file suffix instead of the given editor.
   * 
   * @param substitute
   *   Whether to run the process as the user owning the node.
   */ 
  private void 
  doEditWith
  (
   String editor, 
   boolean useDefault, 
   boolean substitute
  ) 
  {
    TreeSet<FileSeq> targets = FileSeq.collateSeqs(pTargetSeqs.keySet());
    if(targets.isEmpty()) 
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

    NodeCommon com = null;
    boolean subst = substitute;
    VersionID vid = pTargetVersionID; 
    if(vid != null) {
      subst = false;

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        com = client.getCheckedInVersion(pStatus.getName(), vid);
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
        return;
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }
    else {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details == null) 
        return;

      com = details.getWorkingVersion();
      if(com == null) {
        com = details.getLatestVersion();
        subst = false;
      }
    }
    
    if(useDefault) 
      pParent.runEditWithDefaultTask(com, targets);
    else if(subst) 
      pParent.runEditAsOwnerTask(com, targets, true); 
    else if((ename != null) && (evid != null) && (evendor != null))
      pParent.runEditWithTask(com, targets, ename, evid, evendor);
    else
      pParent.runEditTask(com, targets);
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

    Map.Entry<FileSeq,Integer> entry = pTargetSeqs.firstEntry();
    VersionID vid = pTargetVersionID;
    if((entry != null) && (vid != null)) {
      FileSeq fseq = entry.getKey();
      if(fseq.isSingle()) {
        String fname = fseq.getPath(0).toString();
        CompareTask task = new CompareTask(cname, cvid, cvendor, fname, vid); 
        task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit jobs to the queue for the node and all nodes upstream of it.
   */ 
  private void 
  doQueueJobs() 
  {
    if(pIsFrozen) 
      return; 
    
    QueueJobsTask task = new QueueJobsTask(lookupTargetIndices());
    task.start();
  }

  /**
   * Submit jobs to the queue for the node and all nodes upstream of it with special
   * job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    if(pIsFrozen) 
      return; 

    FileSeq fseq = pParent.getPrimarySequence(); 
    if(fseq == null) 
      return;

    TreeMap<String,FrameRange> wholeRanges = new TreeMap<String,FrameRange>();
    wholeRanges.put(pStatus.getName(), fseq.getFrameRange());

    MappedSet<String,Integer> targetIndices = new MappedSet<String,Integer>();
    targetIndices.put(pStatus.getName(), lookupTargetIndices());

    JQueueJobsDialog diag = 
      UIMaster.getInstance().showQueueJobsDialog(wholeRanges, targetIndices);
    if(diag.wasConfirmed()) {
      MappedSet<String,Integer> indices = targetIndices;
      if(diag.overrideTargetIndices()) 
        indices = diag.getTargetIndices();

      Integer batchSize = null;
      if(diag.overrideBatchSize()) 
        batchSize = diag.getBatchSize();
      
      Integer priority = null;
      if(diag.overridePriority()) 
        priority = diag.getPriority();
      
      Integer interval = null;
      if(diag.overrideRampUp()) 
        interval = diag.getRampUp();
      
      Float maxLoad = null;
      if(diag.overrideMaxLoad())
        maxLoad = diag.getMaxLoad();
      
      Long minMemory = null;
      if(diag.overrideMinMemory())
        minMemory = diag.getMinMemory();
      
      Long minDisk= null;
      if(diag.overrideMinDisk())
        minDisk = diag.getMinDisk();
      
      TreeSet<String> selectionKeys = null;
      if(diag.overrideSelectionKeys()) 
        selectionKeys = diag.getSelectionKeys();
      
      TreeSet<String> licenseKeys = null;
      if(diag.overrideLicenseKeys()) 
        licenseKeys = diag.getLicenseKeys();
      
      TreeSet<String> hardwareKeys = null;
      if(diag.overrideHardwareKeys()) 
        hardwareKeys = diag.getHardwareKeys();
      
      QueueJobsTask task = 
        new QueueJobsTask(indices.get(pStatus.getName()), 
                          batchSize, priority, interval, maxLoad, minMemory, minDisk,
                          selectionKeys, licenseKeys, hardwareKeys);
      task.start();
    }
  }

  /**
   * Vouch for the files associated with the current node.
   */ 
  private void 
  doVouch() 
  {
    if(pIsFrozen) 
      return;

    if((pStatus != null) && pStatus.hasLightDetails())
      pParent.runVouchTask(pStatus.getName());
  }

  /**
   * Pause all waiting jobs associated with the current node.
   */ 
  private void 
  doPauseJobs() 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupTargetJobs(nodeIDs, jobIDs);

    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      pParent.runPauseJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Resume execution of all paused jobs associated with the current node.
   */ 
  private void 
  doResumeJobs() 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupTargetJobs(nodeIDs, jobIDs);
    
    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      pParent.runResumeJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Preempt all jobs associated with the current node.
   */ 
  private void 
  doPreemptJobs() 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupTargetJobs(nodeIDs, jobIDs);
      
    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      pParent.runPreemptJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Preempt and Pause all jobs associated with the current node.
   */ 
  private void 
  doPreemptAndPauseJobs() 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupTargetJobs(nodeIDs, jobIDs);
      
    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      pParent.runPreemptAndPauseJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Kill all jobs associated with the current node.
   */ 
  private void 
  doKillJobs() 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupTargetJobs(nodeIDs, jobIDs);
      
    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      pParent.runKillJobsTask(nodeIDs, jobIDs);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove primary/secondary files associated with the node.
   */ 
  private void 
  doRemoveFiles() 
  {
    if(!pIsFrozen && !pTargetSeqs.isEmpty()) {
      boolean confirmed = false;
      if(pStatus.hasLightDetails()) {
        NodeMod work = pStatus.getLightDetails().getWorkingVersion();
        if(work != null) {
          if(work.isActionEnabled()) {
            confirmed = true;
          }
          else {
            JConfirmDialog confirm = 
              new JConfirmDialog(pParent.getTopFrame(), 
                                 "Remove from Node without enabled Actions?");
            confirm.setVisible(true);
            confirmed = confirm.wasConfirmed(); 
          }
        }
      }
      
      if(confirmed) {
        TreeSet<Integer> indices = lookupTargetIndices(); 
        if(!indices.isEmpty()) {
          RemoveFilesTask task = new RemoveFilesTask(indices); 
          task.start();
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper for looking up the job IDs for selected file indices or 
   * the nodeID of the current node if all are selected.
   */ 
  private void
  lookupTargetJobs
  (
   TreeSet<NodeID> nodeIDs, 
   TreeSet<Long> jobIDs
  ) 
  {   
    if((pStatus == null) || pIsFrozen) 
      return; 

    if(pTargetAll) {
      nodeIDs.add(pStatus.getNodeID());
    }
    else if(!pTargetSeqs.isEmpty() && pStatus.hasHeavyDetails()) {
      NodeDetailsHeavy details = pStatus.getHeavyDetails();
      Long[] jobs = details.getJobIDs();
      
      for(Integer idx : pTargetSeqs.values()) {
        if(idx != null) {
          Long jid = jobs[idx];
          if(jid != null) 
            jobIDs.add(jid);
        }
      }
    }
  }

  /**
   * Return the indices into the working file sequence of the selected files.
   */
  private TreeSet<Integer> 
  lookupTargetIndices() 
  {
    TreeSet<Integer> indices = null; 
    if(!pTargetAll) {
      indices = new TreeSet<Integer>();
      for(Integer idx : pTargetSeqs.values()) { 
        if(idx != null) 
          indices.add(idx);
      }
    }

    return indices;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A label which represents an entire file sequence.
   */ 
  private 
  class JFileSeqLabel
    extends JLabel
  {
    public 
    JFileSeqLabel
    (
     FileSeq fseq, 
     VersionID vid
    ) 
    {
      super(fseq.toString());
      setName("TextFieldLabelLarge");
      
      setHorizontalAlignment(JLabel.CENTER);
      setAlignmentX(0.5f);
      
      Dimension size = new Dimension(164, 31);
      setMinimumSize(size);
      setPreferredSize(size);
      setMaximumSize(new Dimension(Integer.MAX_VALUE, 31));

      pFileSeq   = fseq;
      pVersionID = vid; 
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

    private static final long serialVersionUID = 7052441878694774937L;

    private FileSeq   pFileSeq;
    private VersionID pVersionID;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Makes the given component have the keyboard focuse when the mouse is over it.
   */ 
  private 
  class KeyFocuser
    extends MouseAdapter
  {
    KeyFocuser
    (
     Component comp
    ) 
    {
      pComp = comp;
    }

    /**
     * Invoked when the mouse enters a component. 
     */ 
    @Override
    public void 
    mouseEntered
    (
     MouseEvent e
    ) 
    {
      pComp.requestFocusInWindow();
    }

    /**
     * Invoked when the mouse exits a component. 
     */ 
    @Override
    public void 
    mouseExited
    (
     MouseEvent e
    ) 
    {
      KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    }

    private Component  pComp;
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
     String fname, 
     VersionID vid
    ) 
    {
      super("JNodeFilesPanel:CompareTask");

      pComparatorName    = cname;
      pComparatorVersion = cvid; 
      pComparatorVendor  = cvendor; 
      pFileName          = fname; 
      pVersionID         = vid; 
    }

    @Override
    public void 
    run() 
    {
      SubProcessLight proc = null;
      {
	UIMaster master = UIMaster.getInstance();
        boolean ignoreExitCode = false;
	if(master.beginPanelOp(pParent.getGroupID(), "Launching Node Comparator...")) {
	  MasterMgrClient client = master.acquireMasterMgrClient();
	  try {
	    String name = pStatus.getName();

	    NodeCommon com = null;
	    {
	      NodeMod mod = pStatus.getLightDetails().getWorkingVersion();
	      if(mod != null) 
		com = mod;
	      else 
		com = client.getCheckedInVersion(name, pVersionID);	    
	    }

	    /* create an comparator plugin instance */ 
	    PluginMgrClient pclient = PluginMgrClient.getInstance();
	    BaseComparator comparator = 
	      pclient.newComparator(pComparatorName, pComparatorVersion, pComparatorVendor);
            ignoreExitCode = comparator.ignoreExitCode();

	    /* the checked-in file */  
	    File fileB = null;
	    {
	      Path path = new Path(PackageInfo.sRepoPath, 
				   name + "/" + pVersionID + "/" + pFileName); 
	      fileB = path.toFile();	      
	    }

	    /* the working file */ 
	    File fileA = null;
	    {
	      Path path = 
                new Path(PackageInfo.sWorkPath, 
                         pParent.getAuthor() + "/" + pParent.getView() + pStatus.getName());
	      Path wpath = new Path(path.getParentPath(), pFileName);
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
	      env = client.getToolsetEnvironment(pParent.getAuthor(), pParent.getView(), 
                                                 tname, PackageInfo.sOsType);
	    }
	    
	    /* start the comparator */ 
	    proc = comparator.launch(fileA, fileB, env, PackageInfo.sTempPath.toFile());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  catch(LinkageError ex) {
	    master.showErrorDialog(ex);
            return;
	  }
	  finally {
	    master.releaseMasterMgrClient(client);
	    master.endPanelOp(pParent.getGroupID(), "Done.");
	  }
	}

	/* wait for the comparator to exit */ 
	if(proc != null) {
	  try {
	    proc.join();
	    if(!proc.wasSuccessful() && !ignoreExitCode) 
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
    private String     pFileName; 
    private VersionID  pVersionID; 
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
      this(indices, null, null, null, null, null, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeSet<Integer> indices, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp,
     Float maxLoad,              
     Long minMemory,              
     Long minDisk,  
     TreeSet<String> selectionKeys, 
     TreeSet<String> licenseKeys,
     TreeSet<String> hardwareKeys
    ) 
    {
      super("JNodeFilesPanel:QueueJobsTask");

      pIndices       = indices;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pMaxLoad       = maxLoad;
      pMinMemory     = minMemory;
      pMinDisk       = minDisk;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys;
      pHardwareKeys  = hardwareKeys;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pParent.getGroupID(), "Submitting Jobs to the Queue...")) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  LinkedList<QueueJobGroup> groups = 
	    client.submitJobs(pParent.getAuthor(), pParent.getView(), 
                              pStatus.getName(), pIndices, 
                              pBatchSize, pPriority, pRampUp, 
                              pMaxLoad, pMinMemory, pMinDisk,
                              pSelectionKeys, pLicenseKeys, pHardwareKeys);
	  master.monitorJobGroups(groups);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp(pParent.getGroupID(), "Done.");
	}

	pParent.updatePanels();
      }
    }

    private TreeSet<Integer> pIndices; 
    private Integer          pBatchSize;
    private Integer          pPriority;
    private Integer          pRampUp; 
    private Float            pMaxLoad;        
    private Long             pMinMemory;              
    private Long             pMinDisk;
    private TreeSet<String>  pSelectionKeys;
    private TreeSet<String>  pLicenseKeys;
    private TreeSet<String>  pHardwareKeys;
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
      super("JNodeFilesPanel:RemoveFilesTask");
      pIndices = new TreeSet<Integer>(indices);
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pParent.getGroupID(), "Removing Files: " + pStatus.getName())) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  client.removeFiles(pParent.getAuthor(), pParent.getView(), 
                             pStatus.getName(), pIndices);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp(pParent.getGroupID(), "Done.");
	}
	
	pParent.updatePanels();
      }
    }

    private TreeSet<Integer> pIndices; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1890041542212781243L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent node files panel.
   */ 
  private JNodeFilesPanel pParent; 

  /**
   * The parent manager panel.
   */ 
  private JManagerPanel  pManagerPanel; 

  /**
   * Whether there are working and checked-in versions.
   */ 
  private boolean pHasWorking; 
  private boolean pHasCheckedIn; 

  /**
   * Whether the working version is in a Frozen state.
   */ 
  private boolean pIsFrozen;

  /**
   * Whether the working version is editable given its current state and the users 
   * level of privileges.
   */ 
  private boolean pIsReadOnly; 

  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The label for the entire file sequence.
   */ 
  private JFileSeqLabel pHeaderLabel;

  /**
   * The popup menus.
   */ 
  private JPopupMenu  pWorkingPopup; 
  private JPopupMenu  pReadOnlyPopup; 
  private JPopupMenu  pCheckedInPopup; 

  /**
   * The popup menus items.
   */ 
  private JMenuItem  pApplyItem;
  private JMenuItem  pQueueJobsItem;
  private JMenuItem  pQueueJobsSpecialItem;
  private JMenuItem  pVouchItem;
  private JMenuItem  pPauseJobsItem;
  private JMenuItem  pResumeJobsItem;
  private JMenuItem  pPreemptJobsItem;
  private JMenuItem  pPreemptAndPauseJobsItem;
  private JMenuItem  pKillJobsItem;
  private JMenuItem  pRemoveFilesItem;

  
  /**
   * The edit with submenus.
   */ 
  private JMenuItem[]  pEditItems;
  private JMenuItem[]  pEditWithDefaultItems;
  private JMenu[]      pEditWithMenus; 
  private JMenuItem    pEditAsOwnerItem; 

  /**
   * The compare with submenu.
   */ 
  private JMenu  pCompareWithMenu;

  /**
   * The toolset used to build the editor and comparator menus.
   */ 
  private String  pMenuToolset;


  /**
   * Whether all working files are the target of the menu action.
   */ 
  private boolean pTargetAll; 

  /**
   * The file sequences and corresponding index into the working file sequence of the 
   * selected single file sequences which are the target of the menu action. <P> 
   */ 
  private TreeMap<FileSeq,Integer>  pTargetSeqs;
                                               
  /**
   * The revision number of a checked-in file sequence which is the target of the 
   * menu action.
   */ 
  private VersionID  pTargetVersionID;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-file table model and panel.
   */
  private FileSeqTableModel  pTableModel; 
  private JTablePanel        pTablePanel; 

}
