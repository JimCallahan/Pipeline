// $Id: JBaseNodeDetailPanel.java,v 1.4 2009/10/07 08:09:50 jim Exp $

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
/*   B A S E   N O D E   D E T A I L   P A N E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for all panels which display detailed node status information. <P>
 * 
 * Provides the panel header, edit/view and basic queue related menus.
 */ 
public  
class JBaseNodeDetailPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  protected 
  JBaseNodeDetailPanel() 
  {
    super();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  protected
  JBaseNodeDetailPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the panel header user interface components.
   * 
   * @param hasApply
   *   Whether to add an Apply button.
   */ 
  protected JPanel
  initHeader
  (
   boolean hasApply
  )
  {
    /* create the header components */ 
    JPanel panel = new JPanel();
    {
      panel.setName("DialogHeader");	
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      {
        pHeaderIcon = new JLabel();
        pHeaderIcon.addMouseListener(this); 
        panel.add(pHeaderIcon);	  
      }
      
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      
      {
        JLabel label = new JLabel("X");
        pHeaderLabel = label;
        
        label.setName("DialogHeaderLabel");
        
        /* Specify a minimum and maximum size for the label, so that long header strings 
           will be displayed with trailing "..." */
        {
          int height = label.getPreferredSize().height;
          
          label.setMinimumSize(new Dimension(1, height));
          label.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        }
        
        panel.add(label);	  
      }
      
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      panel.add(Box.createHorizontalGlue());

      {
        JLabel label = new JLabel(sFrozenIcon);
        pFrozenLabel = label;
        label.setVisible(false); 

        panel.add(label);	  
      }

      if(hasApply) {
        JButton btn = new JButton();		
        pApplyButton = btn;
        btn.setName("ApplyHeaderButton");
        btn.setEnabled(false);
	
        Dimension size = new Dimension(30, 30);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setPreferredSize(size);
	
        btn.setActionCommand("apply");
        btn.addActionListener(this);
	
        btn.setToolTipText(UIFactory.formatToolTip(pUnApplyToolTipText)); 
        
        panel.add(btn);
      } 
    }

    return panel;
  }

  /**
   * Initialize the basic editor and queue related popup menus. 
   * 
   * @param hasApply
   *   Whether to add an Apply menu item.
   * 
   * @param hasAddAnnotation
   *   Whether to add an AddAnnotation menu item.
   */ 
  protected void 
  initBasicMenus
  (
   boolean hasApply, 
   boolean hasAddAnnotation
  ) 
  {
    JMenuItem item;
    JMenu sub;
      
    pWorkingPopup   = new JPopupMenu();  
    pCheckedInPopup = new JPopupMenu(); 

    pEditItems            = new JMenuItem[2];
    pEditWithDefaultItems = new JMenuItem[2];
    pEditWithMenus        = new JMenu[2];

    if(hasApply) {
      item = new JMenuItem("Apply Changes");
      pApplyItem = item;
      item.setActionCommand("apply");
      item.addActionListener(this);
      pWorkingPopup.add(item);
    }

    if(hasAddAnnotation) {
      item = new JMenuItem("Add Annotation...");
      pAddAnnotationItem = item;
      item.setActionCommand("add-annotation");
      item.addActionListener(this);
      pWorkingPopup.add(item);
    }

    if(hasApply || hasAddAnnotation) 
      pWorkingPopup.addSeparator();

    JPopupMenu menus[] = { pWorkingPopup, pCheckedInPopup };
    int wk;
    for(wk=0; wk<menus.length; wk++) {
      item = new JMenuItem((wk == 1) ? "View" : "Edit");
      pEditItems[wk] = item;
      item.setActionCommand("edit");
      item.addActionListener(this);
      menus[wk].add(item);
	
      pEditWithMenus[wk] = new JMenu((wk == 1) ? "View With" : "Edit With");
      menus[wk].add(pEditWithMenus[wk]);

      item = new JMenuItem((wk == 1) ? "View With Default" : "Edit With Default");
      pEditWithDefaultItems[wk] = item;
      item.setActionCommand("edit-with-default");
      item.addActionListener(this);
      menus[wk].add(item);
    }
      
    item = new JMenuItem("Edit As Owner");
    pEditAsOwnerItem = item;
    item.setActionCommand("edit-as-owner");
    item.addActionListener(this);
    menus[0].add(item);

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
  }

  /**
   * Initialize the full node name user interface components.
   * 
   * @param panel
   *   The parent panel which will contain the name components.
   * 
   * @param extra
   *   Extra components to add to the horizontal box containg the node name field or 
   *   <CODE>null</CODE> to ignore.
   */ 
  protected void
  initNameField
  (
   JPanel panel, 
   Collection<Component> extra
  )
  {
    Box hbox = new Box(BoxLayout.X_AXIS);
	
    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
    {
      pNodeNameField = UIFactory.createTextField(null, 100, JLabel.LEFT);
      hbox.add(pNodeNameField);
    }
    
    if(extra != null) {
      for(Component comp : extra) 
        hbox.add(comp);	
    }

    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
    panel.add(hbox);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update all panels which share the current update channel.
   */ 
  public void 
  updatePanels() 
  {}

  /**
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  @Override
  public void 
  postPanelOp() 
  {
    if(pApplyButton != null) {
      pApplyButton.setEnabled(false);
      pApplyButton.setToolTipText(UIFactory.formatToolTip(pUnApplyToolTipText));
    }

    if(pApplyItem != null) 
      pApplyItem.setEnabled(false);

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
    if(pApplyButton != null) {
      pApplyButton.setEnabled(true);
      pApplyButton.setToolTipText(UIFactory.formatToolTip(pApplyToolTipText)); 
    }

    if(pApplyItem != null) 
      pApplyItem.setEnabled(true);

    super.unsavedChange(name); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current node status.
   * 
   * @param status
   *   The current node status.
   * 
   * @param applyAlwaysVisible
   *   Whether the header apply button should always be visible regardles of frozen status.
   */
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status, 
   boolean applyAlwaysVisible
  ) 
  {
    updatePrivileges();

    pStatus = status;

    /* header */ 
    {
      String iname = "Blank-Normal"; 
      if(pStatus != null) {	
        if(pStatus.hasHeavyDetails()) {
          NodeDetailsHeavy details = pStatus.getHeavyDetails(); 
          switch(details.getOverallNodeState()) {
          case NeedsCheckOut: 
            {
              VersionID wvid = details.getWorkingVersion().getWorkingID();
              VersionID lvid = details.getLatestVersion().getVersionID();
              switch(wvid.compareLevel(lvid)) {
              case Major:
                iname = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
                break;
                
              case Minor:
                iname = ("NeedsCheckOut-" + details.getOverallQueueState());
                break;
                
              case Micro:
                iname = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
              }
            }
            break; 
            
          case Missing: 
            iname = ("Missing" + (details.isAllMissing() ? "" : "Some") + "-" + 
                     details.getOverallQueueState());
            break;

          default:
            iname = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
          }
          
          NodeMod mod = details.getWorkingVersion();
          if((mod != null) && mod.isFrozen()) 
            iname = (iname + "-Frozen-Normal");
          else 
            iname = (iname + "-Normal");
        }
        else if(pStatus.hasLightDetails()) {
          NodeDetailsLight details = pStatus.getLightDetails();
          switch(details.getVersionState()) {
          case CheckedIn:
            iname = "CheckedIn-Undefined-Normal"; 
            break;
            
          default:
            iname = "Lightweight-Normal";
          }
        }

        pHeaderLabel.setText(pStatus.toString());
	pHeaderLabel.setToolTipText(UIFactory.formatToolTip(pStatus.toString()));

        pNodeNameField.setText(pStatus.getName());
      }
      else {
        pHeaderLabel.setText(null);
	pHeaderLabel.setToolTipText(null);

        pNodeNameField.setText(null);
      }

      try {
        pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon32(iname));
      }
      catch(PipelineException ex) {
        pHeaderIcon.setIcon(null); 
        UIMaster.getInstance().showErrorDialog(ex);
      }
    }

    /* frozen node?  locked node? */
    {
      pIsFrozen = false;
      pIsLocked = false;
      
      if(pStatus != null) {
        NodeDetailsLight details = pStatus.getLightDetails();
        if((details != null) && (details.getWorkingVersion() != null)) {
          NodeMod mod = details.getWorkingVersion();
          pIsFrozen = mod.isFrozen();
          pIsLocked = mod.isLocked();
          pFrozenLabel.setIcon(pIsLocked ? sLockedIcon : sFrozenIcon);
          pFrozenLabel.setToolTipText(UIFactory.formatToolTip
            (pIsLocked ? "The Node is Locked." : "The Node is Frozen.")); 
        }
      }
      
      pFrozenLabel.setVisible(pIsFrozen);
      if(pApplyButton != null) 
        pApplyButton.setVisible(applyAlwaysVisible || !pIsFrozen);
    }
    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node menu.
   */ 
  public void 
  updateNodeMenu() 
  {
    boolean queuePrivileged = 
      (PackageInfo.sUser.equals(pAuthor) || pPrivilegeDetails.isQueueManaged(pAuthor));

    boolean nodePrivileged = 
      (PackageInfo.sUser.equals(pAuthor) || pPrivilegeDetails.isNodeManaged(pAuthor));

    pQueueJobsItem.setEnabled(queuePrivileged);
    pQueueJobsSpecialItem.setEnabled(queuePrivileged);

    pVouchItem.setEnabled(queuePrivileged);

    pPauseJobsItem.setEnabled(queuePrivileged);
    pResumeJobsItem.setEnabled(queuePrivileged);
    pPreemptJobsItem.setEnabled(queuePrivileged);
    pKillJobsItem.setEnabled(queuePrivileged);

    pRemoveFilesItem.setEnabled(nodePrivileged);  

    updateEditorMenus();
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  @Override
  public void 
  clearPluginCache()
  {
    pEditorMenuToolset = null;
  }

  /**
   * Update the editor plugin menus.
   */ 
  protected void 
  updateEditorMenus()
  {
    String toolset = null;
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details.getWorkingVersion() != null) 
        toolset = details.getWorkingVersion().getToolset();
      else if(details.getLatestVersion() != null) 
        toolset = details.getLatestVersion().getToolset();
    }

    if((toolset != null) && !toolset.equals(pEditorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) 
	master.rebuildEditorMenu(pGroupID, toolset, pEditWithMenus[wk], this);
      
      pEditorMenuToolset = toolset;
    }

    pEditAsOwnerItem.setEnabled(pPrivilegeDetails.isNodeManaged(pAuthor) && 
                                !PackageInfo.sUser.equals(pAuthor) && 
                                (PackageInfo.sOsType != OsType.Windows));
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  @Override
  public void 
  updateUserPrefs() 
  {
    TextureMgr.getInstance().rebuildIcons();
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  protected void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
       
    if(pApplyItem != null) 
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
      (pEditAsOwnerItem, prefs.getEditAsOwner(), 
       "Edit primary file sequences of the current primary selection with the permissions " +
       "of the owner of the node.");

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

  /**
   * Helper for looking up the job IDs for the panel's current node which match a given 
   * QueueState (heavyweight status) and/or the node IDs (lightweight status).
   */ 
  protected void
  lookupNodeJobsWithState
  (
   TreeSet<NodeID> nodes,
   TreeSet<Long> jobs, 
   QueueState state
  ) 
  {
    if(pStatus != null) {
      if(pStatus.hasHeavyDetails()) {
        NodeDetailsHeavy details = pStatus.getHeavyDetails();
        
        Long[] jobIDs   = details.getJobIDs();
        QueueState[] qs = details.getQueueStates();
        assert(jobIDs.length == qs.length);
        
        int wk;
        for(wk=0; wk<jobIDs.length; wk++) {
          if(qs[wk] == state) {
            assert(jobIDs[wk] != null);
            jobs.add(jobIDs[wk]);
          }
        }
      }
      else if(pStatus.hasLightDetails()) {
        nodes.add(pStatus.getNodeID());
      }
    }
  }

  /**
   * Helper for looking up the job IDs for panel's current node which have a pending 
   * QueueState (heavyweight status) and/or the node IDs (lightweight status).
   */ 
  protected void
  lookupNodeJobsPending
  (
   TreeSet<NodeID> nodes,
   TreeSet<Long> jobs
  ) 
  {
    if(pStatus != null) {
      if(pStatus.hasHeavyDetails()) {
        NodeDetailsHeavy details = pStatus.getHeavyDetails();
        
        Long[] jobIDs   = details.getJobIDs();
        QueueState[] qs = details.getQueueStates();
        assert(jobIDs.length == qs.length);
        
        int wk;
        for(wk=0; wk<jobIDs.length; wk++) {
          switch(qs[wk]) {
          case Queued:
          case Paused:
          case Running:
            assert(jobIDs[wk] != null);
            jobs.add(jobIDs[wk]);
          }
        }
      }
      else if(pStatus.hasLightDetails()) {
        nodes.add(pStatus.getNodeID());
      }
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
    handleHeaderMouseEvent(e);
  }

  /**
   * Handle header icon related mouse events.
   * 
   * @return 
   *   Whether the event was handled.
   */ 
  public boolean
  handleHeaderMouseEvent
  (
   MouseEvent e 
  ) 
  {
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return true;

    /* local mouse events */ 
    if(e.getSource() == pHeaderIcon) {
      if((pStatus != null) && pStatus.hasLightDetails()) {
        NodeDetailsLight details = pStatus.getLightDetails();
        NodeMod work = details.getWorkingVersion();
        NodeVersion latest = details.getLatestVersion();
        if((work != null) && !pIsFrozen) {
          updateNodeMenu();
          pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
          return true;
        }
        else if(latest != null) {
          updateNodeMenu();
          pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
          return true;
        }
      }
    }

    return false;
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
    if(!handleHeaderKeyEvent(e)) {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	if(UIFactory.getBeepPreference())
	  Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  /**
   * Handle header icon related keyboard events.
   * 
   * @return 
   *   Whether the event was handled.
   */ 
  public boolean
  handleHeaderKeyEvent
  (
   KeyEvent e 
  ) 
  {
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return true;

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getApplyChanges() != null) &&
       prefs.getApplyChanges().wasPressed(e) && 
       (pApplyButton != null) && 
       pApplyButton.isEnabled()) {
      doApply();
      return true;
    }
    else if((prefs.getEdit() != null) &&
       prefs.getEdit().wasPressed(e)) {
      doEdit();
      return true;
    }
    else if((prefs.getEditWithDefault() != null) &&
	    prefs.getEditWithDefault().wasPressed(e)) {
      doEditWithDefault();
      return true;
    }
    else if((prefs.getEditAsOwner() != null) &&
	    prefs.getEditAsOwner().wasPressed(e)) {
      doEditAsOwner();
      return true;
    }
    else if((prefs.getQueueJobs() != null) &&
	    prefs.getQueueJobs().wasPressed(e)) {
      doQueueJobs();
      return true;
    }
    else if((prefs.getQueueJobsSpecial() != null) &&
	    prefs.getQueueJobsSpecial().wasPressed(e)) {
      doQueueJobsSpecial();
      return true;
    }
    else if((prefs.getVouch() != null) &&
            prefs.getVouch().wasPressed(e)) {
      doVouch();
      return true;
    }
    else if((prefs.getPauseJobs() != null) &&
	    prefs.getPauseJobs().wasPressed(e)) {
	doPauseJobs();
      return true;
    }
    else if((prefs.getResumeJobs() != null) &&
	    prefs.getResumeJobs().wasPressed(e)) {
      doResumeJobs();
      return true;
    }
    else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e)) {
      doKillJobs();
      return true;
    }
    else if((prefs.getRemoveFiles() != null) &&
	    prefs.getRemoveFiles().wasPressed(e)) {
      doRemoveFiles();
      return true;
    }

    return false; 
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
  @Override
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("apply")) 
      doApply();
    else if(cmd.equals("edit"))
      doEdit();
    else if(cmd.equals("edit-with-default"))
      doEditWithDefault();
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10)); 
    else if(cmd.equals("edit-as-owner"))
      doEditAsOwner();  

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
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("remove-files"))
      doRemoveFiles();        
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the current node with the editor specified by the node version.
   */ 
  private void 
  doEdit() 
  {
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();

      NodeCommon com = details.getWorkingVersion();
      if(com == null) 
        com = details.getLatestVersion();
      
      if(com != null) 
        runEditTask(com, null); 
    }
  }

  /**
   * Create and start a task to Edit/View the current node with the editor specified 
   * by the node version.
   */ 
  public void 
  runEditTask
  (
   NodeCommon com, 
   TreeSet<FileSeq> selectSeqs
  ) 
  {
    EditTask task = new EditTask(com, selectSeqs);
    task.start();
  }

  /**   
   * Edit/View the primary selected node using the default editor for the file type.
   */ 
  private void 
  doEditWithDefault() 
  {
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();

      NodeCommon com = details.getWorkingVersion();
      if(com == null) 
        com = details.getLatestVersion();
      
      if(com != null)
        runEditWithDefaultTask(com, null);
    }
  }

  /**
   * Create and start a task to Edit/View the primary selected node using the default 
   * editor for the file type.
   */ 
  public void 
  runEditWithDefaultTask
  (
   NodeCommon com, 
   TreeSet<FileSeq> selectSeqs
  ) 
  {
    EditTask task = new EditTask(com, selectSeqs, true, false);
    task.start();
  }

  /**
   * Edit/View the current node with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    String parts[] = editor.split(":");
    assert(parts.length == 3);
    
    String ename   = parts[0];
    VersionID evid = new VersionID(parts[1]);
    String evendor = parts[2];

    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();

      NodeCommon com = details.getWorkingVersion();
      if(com == null) 
        com = details.getLatestVersion();
      
      if(com != null) 
        runEditWithTask(com, null, ename, evid, evendor);
    }
  }

  /**
   * Create and start a task to Edit/View the current node with the given editor.
   */ 
  public void 
  runEditWithTask
  (
   NodeCommon com, 
   TreeSet<FileSeq> selectSeqs,
   String ename,
   VersionID evid,
   String evendor
  ) 
  {
    EditTask task = new EditTask(com, selectSeqs, ename, evid, evendor);
    task.start();
  }

  /**
   * Edit/View the current node with the permissions of the owner of the node.
   */ 
  private void 
  doEditAsOwner() 
  {
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();

      boolean isWorking = true;
      NodeCommon com = details.getWorkingVersion();
      if(com == null) {
        com = details.getLatestVersion();
        isWorking = false;
      }

      if(com != null) 
        runEditAsOwnerTask(com, null, isWorking);
    }
  }

  /**
   * Create and start a task to Edit/View the current node with the permissions 
   * of the owner of the node.
   */ 
  public void 
  runEditAsOwnerTask
  (
   NodeCommon com, 
   TreeSet<FileSeq> selectSeqs,
   boolean substitute
  ) 
  {
    EditTask task = new EditTask(com, selectSeqs, false, substitute);
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it.
   */ 
  private void 
  doQueueJobs() 
  {
    if(pIsFrozen) 
      return;

    if((pStatus != null) && pStatus.hasLightDetails()) {
      QueueJobsTask task = new QueueJobsTask(pStatus.getName());
      task.start();
    }
  }

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it
   * with special job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details != null) {
        NodeMod mod = details.getWorkingVersion();
        if(mod != null) {
          TreeMap<String,FrameRange> wholeRanges = new TreeMap<String,FrameRange>();
          wholeRanges.put(mod.getName(), mod.getPrimarySequence().getFrameRange()); 

          MappedSet<String,Integer> targetIndices = new MappedSet<String,Integer>();
          targetIndices.put(mod.getName(), (TreeSet<Integer>) null); 

          JQueueJobsDialog diag = 
            UIMaster.getInstance().showQueueJobsDialog(wholeRanges, targetIndices);
          if(diag.wasConfirmed()) {
            MappedSet<String,Integer> indices = null;
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
              new QueueJobsTask(pStatus.getName(), 
                                (indices == null ? null : indices.get(pStatus.getName())), 
                                batchSize, priority, interval,
                                maxLoad, minMemory, minDisk,
                                selectionKeys, licenseKeys, hardwareKeys);
            task.start();
          }
        }
      }
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
      runVouchTask(pStatus.getName());
  }

  /**
   * Create and start a task to Vouch for the files associated with the given named node.
   */ 
  public void 
  runVouchTask
  (
   String name
  ) 
  {
    VouchTask task = new VouchTask(name);
    task.start();
  }
  
  /**
   * Pause all waiting jobs associated with the current node.
   */ 
  private void 
  doPauseJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupNodeJobsWithState(nodeIDs, jobIDs, QueueState.Queued);

    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      runPauseJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Create and start a task to Pause all waiting jobs associated with the given 
   * nodes or jobs.
   */ 
  public void 
  runPauseJobsTask
  (
   TreeSet<NodeID> nodeIDs,
   TreeSet<Long> jobIDs
  ) 
  {
    PauseJobsTask task = new PauseJobsTask(nodeIDs, jobIDs);
    task.start();
  }

  /**
   * Resume execution of all paused jobs associated with the current node.
   */ 
  private void 
  doResumeJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupNodeJobsWithState(nodeIDs, jobIDs, QueueState.Paused);

    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty())
      runResumeJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Create and start a task to Resume all paused jobs associated with the given 
   * nodes or jobs.
   */ 
  public void 
  runResumeJobsTask
  (
   TreeSet<NodeID> nodeIDs,
   TreeSet<Long> jobIDs
  ) 
  {
    ResumeJobsTask task = new ResumeJobsTask(nodeIDs, jobIDs);
    task.start();
  }

  /**
   * Preempt all jobs associated with the current node.
   */ 
  private void 
  doPreemptJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupNodeJobsPending(nodeIDs, jobIDs); 

    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty())
      runPreemptJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Create and start a task to Preempt all running jobs associated with the given 
   * nodes or jobs.
   */ 
  public void 
  runPreemptJobsTask
  (
   TreeSet<NodeID> nodeIDs,
   TreeSet<Long> jobIDs
  ) 
  {
    PreemptJobsTask task = new PreemptJobsTask(nodeIDs, jobIDs);
    task.start();
  }

  /**
   * Kill all jobs associated with the current node.
   */ 
  private void 
  doKillJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeSet<Long> jobIDs    = new TreeSet<Long>();
    lookupNodeJobsPending(nodeIDs, jobIDs); 

    if(!nodeIDs.isEmpty() || !jobIDs.isEmpty()) 
      runKillJobsTask(nodeIDs, jobIDs);
  }

  /**
   * Create and start a task to Kill all running jobs associated with the given 
   * nodes or jobs.
   */ 
  public void 
  runKillJobsTask
  (
   TreeSet<NodeID> nodeIDs,
   TreeSet<Long> jobIDs
  ) 
  {
    KillJobsTask task = new KillJobsTask(nodeIDs, jobIDs);
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all primary/secondary files associated with the current node.
   */ 
  private void 
  doRemoveFiles() 
  {
    if(pIsFrozen) 
      return;

    if((pStatus != null) && pStatus.hasLightDetails()) {
      RemoveFilesTask task = new RemoveFilesTask(pStatus.getName());
      task.start();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Edit/View the primary file sequence of the given node version.
   */ 
  private
  class EditTask
    extends UIMaster.EditTask
  {
    public 
    EditTask
    (
     NodeCommon com, 
     TreeSet<FileSeq> selectSeqs
    ) 
    {
      UIMaster.getInstance().super
        (pGroupID, com, selectSeqs, false, pAuthor, pView, false);
      setName("JBaseNodeDetailPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com,
     TreeSet<FileSeq> selectSeqs,
     boolean useDefault, 
     boolean substitute 
    ) 
    {
      UIMaster.getInstance().super
        (pGroupID, com, selectSeqs, useDefault, pAuthor, pView, substitute);
      setName("JBaseNodeDetailPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     TreeSet<FileSeq> selectSeqs,
     String ename, 
     VersionID evid, 
     String evendor
    ) 
    {
      UIMaster.getInstance().super
	(pGroupID, com, selectSeqs, ename, evid, evendor, pAuthor, pView, false);
      setName("JBaseNodeDetailPanel:EditTask");
    }
  }


  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Queue jobs to the queue for the given node.
   */ 
  private
  class QueueJobsTask
    extends UIMaster.QueueJobsTask
  {
    public 
    QueueJobsTask
    (
     String name
    ) 
    {
      this(name, null, null, null, null, null, null, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     String name, 
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
      UIMaster.getInstance().super(pGroupID, name, pAuthor, pView, 
				   indices, batchSize, priority, rampUp, 
				   maxLoad, minMemory, minDisk,
				   selectionKeys, licenseKeys, hardwareKeys);
      setName("JBaseNodeDetailPanel:QueueJobsTask");
    }

    protected void
    postOp() 
    {
      updatePanels();
    }
  }

  /** 
   * Vouch for the working area files associated with the given nodes.
   */ 
  private
  class VouchTask
    extends UIMaster.VouchTask
  {
    public 
    VouchTask
    (
     String name
    ) 
    {
      UIMaster.getInstance().super(pGroupID, name, pAuthor, pView);
      setName("JBaseNodeDetailPanel:VouchTask");
    }
    
    protected void
    postOp() 
    {
      updatePanels(); 
    }    
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends UIMaster.PauseJobsTask
  {
    public 
    PauseJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JBaseNodeDetailPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updatePanels(); 
    }
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends UIMaster.ResumeJobsTask
  {
    public 
    ResumeJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JBaseNodeDetailPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updatePanels(); 
    }
  }

  /** 
   * Preempt the given jobs.
   */ 
  private
  class PreemptJobsTask
    extends UIMaster.PreemptJobsTask
  {
    public 
    PreemptJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JBaseNodeDetailPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updatePanels(); 
    }
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends UIMaster.KillJobsTask
  {
    public 
    KillJobsTask
    (
     TreeSet<NodeID> nodeIDs, 
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super("JBaseNodeDetailPanel", 
                                   pGroupID, nodeIDs, jobIDs, pAuthor, pView);
    }

    protected void
    postOp() 
    {
      updatePanels(); 
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  private
  class RemoveFilesTask
    extends UIMaster.RemoveFilesTask
  {
    public 
    RemoveFilesTask
    (
     String name
    ) 
    {
      UIMaster.getInstance().super(pGroupID, name, pAuthor, pView);
      setName("JBaseNodeDetailPanel:RemoveFilesTask");
    }
    
    protected void
    postOp() 
    {
      updatePanels();
    }    
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1027548213455199580L;

  private static final Icon sFrozenIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FrozenIcon.png"));

  private static final Icon sLockedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  protected NodeStatus  pStatus;

  /**
   * Whether the current version is in a Frozen state.
   */ 
  protected boolean  pIsFrozen; 

  /**
   * Whether the working version is locked.
   */
  protected boolean  pIsLocked;

  /**
   * An icon which indicates whether the working version is frozen.
   */
  protected JLabel  pFrozenLabel;

  /**  
   * The button used to apply changes to the working version of the node.
   */ 
  protected JButton  pApplyButton;
  
  /**
   * Messages to use for tooltips on the apply button.
   */ 
  protected String  pApplyToolTipText;
  protected String  pUnApplyToolTipText;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  protected JLabel  pHeaderIcon;
  protected JLabel  pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  protected JTextField  pNodeNameField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu.
   */ 
  protected JPopupMenu  pWorkingPopup; 
  
  /**
   * The working file popup menu items.
   */ 
  protected JMenuItem  pApplyItem;
  protected JMenuItem  pAddAnnotationItem;
  protected JMenuItem  pQueueJobsItem;
  protected JMenuItem  pQueueJobsSpecialItem;
  protected JMenuItem  pVouchItem;
  protected JMenuItem  pPauseJobsItem;
  protected JMenuItem  pResumeJobsItem;
  protected JMenuItem  pPreemptJobsItem;
  protected JMenuItem  pKillJobsItem;
  protected JMenuItem  pRemoveFilesItem;  

  /**
   * The checked-in file popup menu.
   */ 
  protected JPopupMenu  pCheckedInPopup; 

  /**
   * The edit with submenus.
   */ 
  protected JMenuItem[]  pEditItems;
  protected JMenuItem[]  pEditWithDefaultItems;
  protected JMenu[]      pEditWithMenus; 
  protected JMenuItem    pEditAsOwnerItem; 

  /**
   * The toolset used to build the editor menu.
   */ 
  protected String  pEditorMenuToolset;

}
