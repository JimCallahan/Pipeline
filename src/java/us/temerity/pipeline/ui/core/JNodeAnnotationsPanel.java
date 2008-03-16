// $Id: JNodeAnnotationsPanel.java,v 1.14 2008/03/16 13:02:34 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   A N N O T A T I O N   P A N E L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the annotations associated with a node. <P> 
 */ 
public  
class JNodeAnnotationsPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ComponentListener, ActionListener, DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeAnnotationsPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeAnnotationsPanel
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
      pAnnotationsPanels = new TreeMap<String,JAnnotationPanel>();
      pDeadAnnotations = new TreeSet<String>();
      pDocToAnnotation = new ListMap<Document, String>();
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      
      pWorkingPopup   = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu(); 

      pEditItems            = new JMenuItem[2];
      pEditWithDefaultItems = new JMenuItem[2];
      pEditWithMenus        = new JMenu[2];

      {
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
	item.setActionCommand("apply");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      }

      JPopupMenu menus[] = { pWorkingPopup, pCheckedInPopup };
      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem("Add Annotation...");
	pAddAnnotationItem = item;
	item.setActionCommand("add-annotation");
	item.addActionListener(this);
	menus[wk].add(item);

	menus[wk].addSeparator();

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
            ("Apply changes node annotation changes."));

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
        pAnnotationsBox = vbox;

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sVSize+58, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null);
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
    return "Node Annotations";
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

    PanelGroup<JNodeAnnotationsPanel> panels = master.getNodeAnnotationsPanels();

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
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeAnnotationsPanel> panels = 
      UIMaster.getInstance().getNodeAnnotationsPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return !pPrivilegeDetails.isAnnotator();
  }

  /**
   * Set the author and view.
   */ 
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
  private void 
  updatePanels() 
  {
    PanelUpdater pu = new PanelUpdater(this);
    pu.execute();
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
   */
  public synchronized void 
  applyPanelUpdates
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
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  postPanelOp() 
  {
    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);

    super.postPanelOp();
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
    pApplyButton.setEnabled(true);
    pApplyItem.setEnabled(true);

    super.unsavedChange(name); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   */
  private synchronized void 
  updateNodeStatus
  (
   NodeStatus status
  ) 
  {
    updatePrivileges();

    pStatus = status;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    TreeMap<String,BaseAnnotation> annotations = null;
    if(details != null) 
      annotations = details.getAnnotations(); 

    /* header */ 
    {
      {
	String name = "Blank-Normal";
	if(pStatus != null) {	
          if(details != null) {
            if(details.isLightweight()) {
              switch(details.getVersionState()) {
              case CheckedIn:
                name = "CheckedIn-Undefined-Normal"; 
                break;
                
              default:
                name = "Lightweight-Normal";
              }
            }
            else {  
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
          }
          
          pHeaderLabel.setText(pStatus.toString());
          pNodeNameField.setText(pStatus.getName());
        }
        else {
          pHeaderLabel.setText(null);
          pNodeNameField.setText(null);
        }
        
	try {
	  pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon32(name));
	}
	catch(PipelineException ex) {
          pHeaderIcon.setIcon(null); 
	  UIMaster.getInstance().showErrorDialog(ex);
        }
      }
    }

    /* frozen node? */
    {
      pIsFrozen = false;
      if((details != null) && (details.getWorkingVersion() != null))
	pIsFrozen = details.getWorkingVersion().isFrozen();
    }

    /* annotations */ 
    {
      pAnnotationsBox.removeAll();
      pAnnotationsPanels.clear(); 

      if(annotations != null) {
        for(String aname: annotations.keySet()) {
          BaseAnnotation annot = annotations.get(aname);

          JAnnotationPanel panel = new JAnnotationPanel(this, aname, annot);
          pAnnotationsBox.add(panel);

          pAnnotationsPanels.put(aname, panel);
        }
      }
      
      pAnnotationsBox.add(UIFactory.createFiller(sTSize+sVSize+30));
    }
      
    pAnnotationsBox.revalidate();
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

    pAddAnnotationItem.setEnabled( pPrivilegeDetails.isAnnotator());

    pQueueJobsItem.setEnabled(queuePrivileged);
    pQueueJobsSpecialItem.setEnabled(queuePrivileged);

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
  public void 
  clearPluginCache()
  {
    pEditorMenuToolset = null;
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
  public void 
  updateUserPrefs() 
  {
    TextureMgr.getInstance().rebuildIcons();

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
       "Apply the changes to the node annotations.");

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
    if(e.getSource() == pHeaderIcon) {
      if(pStatus == null) 
	return; 

      NodeDetails details = pStatus.getDetails();
      if(details == null) 
	return;

      NodeMod work = details.getWorkingVersion();
      NodeVersion latest = details.getLatestVersion();
      if((work != null) && !pIsFrozen) {
	updateNodeMenu();
	pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
      }
      else if(latest != null) {
	updateNodeMenu();
	pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
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

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getApplyChanges() != null) &&
       prefs.getApplyChanges().wasPressed(e) && 
       pApplyButton.isEnabled())
      doApply();

    else if((prefs.getEdit() != null) &&
       prefs.getEdit().wasPressed(e))
      doEdit();
    else if((prefs.getEditWithDefault() != null) &&
	    prefs.getEditWithDefault().wasPressed(e))
      doEditWithDefault();
    else if((prefs.getEditAsOwner() != null) &&
	    prefs.getEditAsOwner().wasPressed(e))
      doEditAsOwner();
    
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
    if(cmd.equals("apply")) 
      doApply();

    else if(cmd.equals("add-annotation"))
      doAddAnnotation();
    else if(cmd.startsWith("annotation-changed:"))
      doAnnotationChanged(cmd.substring(19));
    else if(cmd.startsWith("remove-annotation:"))
      doRemoveAnnotation(cmd.substring(18));
    else if(cmd.startsWith("rename-annotation:"))
      doRenameAnnotation(cmd.substring(18));

    else if(cmd.startsWith("param-changed:"))
      doAnnotationParamChanged(cmd.substring(14));

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
  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  public void 
  changedUpdate
  (
    DocumentEvent e
  )
  {}

  public void 
  insertUpdate
  (
    DocumentEvent e
  )
  {
    Document doc = e.getDocument();
    doAnnotationParamChanged(doc);
  }

  public void 
  removeUpdate
  (
    DocumentEvent e
  )
  {
    Document doc = e.getDocument();
    doAnnotationParamChanged(doc);
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply any changes to the annotations based on the current settings if the 
   * UI components.
   */ 
  public void 
  doApply()
  {
    super.doApply();

    TreeSet<String> dead = new TreeSet<String>(pDeadAnnotations); 
    pDeadAnnotations.clear();

    TreeMap<String,BaseAnnotation> modified = new TreeMap<String,BaseAnnotation>();
    for(String aname : pAnnotationsPanels.keySet()) {
      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel.isModified()) {
        modified.put(panel.getName(), panel.getAnnotation());
        if(!aname.equals(panel.getName()))
          dead.add(aname); 
      }
    }
    
    if(!modified.isEmpty() || !dead.isEmpty()) {
      ApplyTask task = new ApplyTask(pStatus.getName(), modified, dead);
      task.start();
    }
  }

  /**
   * Add a new annotation to the node.
   */ 
  public void 
  doAddAnnotation()
  {
    JNewIdentifierDialog diag = 
      new JNewIdentifierDialog(getTopFrame(), "New Annotation", "New Annotation Name:", 
                               null, "Add");
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      String aname = diag.getName();
      if((aname != null) && (aname.length() > 0)) {
        if(pAnnotationsPanels.get(aname) != null) {
          UIMaster.getInstance().showErrorDialog
            ("Error:", 
             "The new annotation name (" + aname + ") is already being used by an " +
             "existing annotation!");
          return;
        }

        JAnnotationPanel panel = new JAnnotationPanel(this, aname); 
        pAnnotationsPanels.put(aname, panel); 

        pDeadAnnotations.remove(aname);
        unsavedChange("Annotation Added: " + aname);

        pAnnotationsBox.remove(pAnnotationsBox.getComponentCount()-1);
        pAnnotationsBox.add(panel);
        pAnnotationsBox.add(UIFactory.createFiller(sTSize+sVSize+30));
        pAnnotationsBox.revalidate();        
      }
    }
  }

  /**
   * Update the appearance of the annotation fields after a change of value.
   */ 
  public void 
  doAnnotationChanged
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) 
      panel.doAnnotationChanged();
  }

  /**
   * Remove the given annotation panel.
   */ 
  public void 
  doRemoveAnnotation
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) {
      pAnnotationsBox.remove(panel);
      pAnnotationsBox.revalidate();   

      pDeadAnnotations.add(aname);     
      unsavedChange("Annotation Removed: " + aname);
    }
  }
  
  /**
   * Rename the given annotation panel.
   */ 
  public void 
  doRenameAnnotation
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(getTopFrame(), "Rename Annotation", "New Annotation Name:", 
				 aname, "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
        String nname = diag.getName();
        if((nname != null) && (nname.length() > 0) && !nname.equals(aname)) {
          panel.renameAnnotation(nname);
          pDeadAnnotations.remove(nname);
          unsavedChange("Annotation Renamed from: " + aname + " to " + nname);
        }
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   String args
  ) 
  {
    String parts[] = args.split(":");
    if((parts.length == 2) && (parts[0].length() > 0) && (parts[1].length() > 0)) {
      String name  = parts[0];
      String aname = parts[1];

      JAnnotationPanel panel = pAnnotationsPanels.get(name);
      if(panel != null) 
        panel.annotationParamChanged(name, aname); 
    }
  }
  
  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   Document doc
  ) 
  {
    String name = pDocToAnnotation.get(doc);
    JAnnotationPanel panel = pAnnotationsPanels.get(name);
    if(panel != null) 
      panel.annotationParamChanged(name, doc); 
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the current node with the editor specified by the node version.
   */ 
  private void 
  doEdit() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com);
	  task.start();
	}
      }
    }
  }

  /**   
   * Edit/View the primary selected node using the default editor for the file type.
   */ 
  private void 
  doEditWithDefault() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, true, false);
	  task.start();
	}
      }
    }
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

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, ename, evid, evendor);
	  task.start();
	}
      }
    }
  }

  /**
   * Edit/View the current node with the permissions of the owner of the node.
   */ 
  private void 
  doEditAsOwner() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	boolean isWorking = true;
	NodeCommon com = details.getWorkingVersion();
	if(com == null) {
	  com = details.getLatestVersion();
	  isWorking = false;
	}

	if(com != null) {
	  EditTask task = new EditTask(com, false, isWorking);
	  task.start();
	}
      }
    }
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

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	QueueJobsTask task = new QueueJobsTask(pStatus.getName());
	task.start();
      }
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
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
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
	    new QueueJobsTask(pStatus.getName(), batchSize, priority, interval, 
	      		      maxLoad, minMemory, minDisk,
			      selectionKeys, licenseKeys, hardwareKeys);
	  task.start();
	}
      }
    }
  }

  /**
   * Pause all waiting jobs associated with the current node.
   */ 
  private void 
  doPauseJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> pausedNodes = new TreeSet<NodeID>();
    TreeSet<Long> pausedJobs    = new TreeSet<Long>();

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          pausedNodes.add(pStatus.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);
          
          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
              assert(jobIDs[wk] != null);
              pausedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!pausedNodes.isEmpty() || !pausedJobs.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(pausedNodes, pausedJobs);
      task.start();
    }
  }

  /**
   * Resume execution of all paused jobs associated with the current node.
   */ 
  private void 
  doResumeJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> resumedNodes = new TreeSet<NodeID>();
    TreeSet<Long> resumedJobs    = new TreeSet<Long>();

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          resumedNodes.add(pStatus.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);
          
          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Paused:
              assert(jobIDs[wk] != null);
              resumedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }

    if(!resumedNodes.isEmpty() || !resumedJobs.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumedNodes, resumedJobs);
      task.start();
    }
  }

  /**
   * Preempt all jobs associated with the current node.
   */ 
  private void 
  doPreemptJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> preemptedNodes = new TreeSet<NodeID>();
    TreeSet<Long> preemptedJobs    = new TreeSet<Long>();

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          preemptedNodes.add(pStatus.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);
          
          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
            case Paused:
            case Running:
              assert(jobIDs[wk] != null);
              preemptedJobs.add(jobIDs[wk]);
            }
          }
        }
      }
    }
      
    if(!preemptedNodes.isEmpty() || !preemptedJobs.isEmpty()) {
      PreemptJobsTask task = new PreemptJobsTask(preemptedNodes, preemptedJobs);
      task.start();
    }
  }

  /**
   * Kill all jobs associated with the current node.
   */ 
  private void 
  doKillJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<NodeID> killedNodes = new TreeSet<NodeID>();
    TreeSet<Long> killedJobs    = new TreeSet<Long>();

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          killedNodes.add(pStatus.getNodeID());
        }
        else {
          Long[] jobIDs   = details.getJobIDs();
          QueueState[] qs = details.getQueueState();
          assert(jobIDs.length == qs.length);
          
          int wk;
          for(wk=0; wk<jobIDs.length; wk++) {
            switch(qs[wk]) {
            case Queued:
            case Paused:
            case Running:
              assert(jobIDs[wk] != null);
              killedJobs.add(jobIDs[wk]);              
            }
          }
        }
      }
    }

    if(!killedNodes.isEmpty() || !killedJobs.isEmpty()) {
      KillJobsTask task = new KillJobsTask(killedNodes, killedJobs);
      task.start();
    }
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

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	RemoveFilesTask task = new RemoveFilesTask(pStatus.getName());
	task.start();
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A component representing a node annotation plugin instance.
   */ 
  private 
  class JAnnotationPanel
    extends JPanel
  {
    /**
     * Construct a new annotation panel.
     */ 
    public 
    JAnnotationPanel
    (
     JNodeAnnotationsPanel parent, 
     String name
    ) 
    {
      this(parent, name, null);
    }

    /**
     * Construct a new annotation panel.
     */ 
    public 
    JAnnotationPanel
    (
     JNodeAnnotationsPanel parent, 
     String name,
     BaseAnnotation annot
    ) 
    {
      super();

      /* initialize fields */ 
      {
	pName = name; 
	pAnnotation = annot; 
        pParent = parent; 
        pParamComponents = new TreeMap<String,Component>();
        pDocToParamName = new ListMap<Document, String>();
      }

      /* panel components */ 
      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
        Box vbox = new Box(BoxLayout.Y_AXIS);
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          if(pPrivilegeDetails.isAnnotator()) {
            tpanel.add(Box.createRigidArea(new Dimension(0, 19)));
            
            Box hbox = new Box(BoxLayout.X_AXIS);
            
            {
              JButton btn = new JButton("Rename...");
              btn.setName("ValuePanelButton");
              btn.setRolloverEnabled(false);
              btn.setFocusable(false);
              
              Dimension size = new Dimension(sTSize/2-2, 19);
              btn.setMinimumSize(size);
              btn.setPreferredSize(size);
              btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
              
              btn.setActionCommand("rename-annotation:" + pName);
              btn.addActionListener(pParent);
              
              hbox.add(btn);
            }
            
            hbox.add(Box.createRigidArea(new Dimension(4, 0)));
            
            {
              JButton btn = new JButton("Remove...");
              btn.setName("ValuePanelButton");
              btn.setRolloverEnabled(false);
              btn.setFocusable(false);
              
              Dimension size = new Dimension(sTSize/2-2, 19);
              btn.setMinimumSize(size);
              btn.setPreferredSize(size);
              btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
              
              btn.setActionCommand("remove-annotation:" + pName);
              btn.addActionListener(pParent);
              
              hbox.add(btn);
            }
            
            vpanel.add(hbox);
          
            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          }
	
          {
            JLabel label = 
              UIFactory.createFixedLabel
              ("Annotation:", sTSize, JLabel.RIGHT, 
               "The name of the Annotation plugin.");
            
            tpanel.add(label);

            JPluginSelectionField field =  
              UIMaster.getInstance().createAnnotationSelectionField(sVSize);
            pAnnotationField = field;
            
            field.setActionCommand("annotation-changed:" + pName);
            field.addActionListener(pParent);

            field.setEnabled(pPrivilegeDetails.isAnnotator());
            
            vpanel.add(field);
          }
	  
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
          {
            JTextField field = 
              UIFactory.createTitledTextField
              (tpanel, "Version:", sTSize, 
               vpanel, "-", sVSize, 
               "The revision number of the Annotation plugin.");
            pVersionField = field;
          }
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          {
            JTextField field = 
              UIFactory.createTitledTextField
              (tpanel, "Vendor:", sTSize, 
               vpanel, "-", sVSize, 
               "The name of the Annotation plugin vendor.");
            pVendorField = field;
          }
          
          vbox.add(comps[2]);
        }
        
        {
          Box hbox = new Box(BoxLayout.X_AXIS);
          hbox.addComponentListener(pParent);
          
          hbox.add(UIFactory.createSidebar());
          
          {
            Component comps[] = UIFactory.createTitledPanels();
            JPanel tpanel = (JPanel) comps[0];
            JPanel vpanel = (JPanel) comps[1];

            tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
            vpanel.add(Box.createHorizontalGlue());

            UIFactory.addVerticalGlue(tpanel, vpanel);

            JDrawer drawer = 
              new JDrawer("Annotation Parameters:", (JComponent) comps[2], true);
            drawer.setToolTipText(UIFactory.formatToolTip("Annotation plugin parameters."));
            pParamsDrawer = drawer;

            hbox.add(drawer);
          }  
          
          vbox.add(hbox);
        }

        JDrawer drawer = new JDrawer("Annotation: " + pName, vbox, true);
        drawer.setToolTipText(UIFactory.formatToolTip("Node Annotation."));
        pTopDrawer = drawer;
        add(drawer);
      }
      
      updateAnnotation();
    }

    /**
     * Whether the annotation has been modified since the panel was created.
     */ 
    public boolean 
    isModified() 
    {
      return pIsModified; 
    }

    /**
     * The name of the annotation plugin instance.
     */ 
    public String
    getName() 
    {
      return pName;
    }

    /**
     * Get the updated annotation plugin instance.
     */
    public BaseAnnotation
    getAnnotation() 
    {
      if(pAnnotation != null) {
        for(AnnotationParam aparam : pAnnotation.getParams()) {
          if(!pAnnotation.isParamConstant(aparam.getName())) {
            Component comp = pParamComponents.get(aparam.getName()); 
            Comparable value = null;
            if(aparam instanceof BooleanAnnotationParam) {
              JBooleanField field = (JBooleanField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof DoubleAnnotationParam) {
              JDoubleField field = (JDoubleField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof EnumAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
              value = eparam.getValueOfIndex(field.getSelectedIndex());
            }
            else if(aparam instanceof IntegerAnnotationParam) {
              JIntegerField field = (JIntegerField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof TextAreaAnnotationParam) {
              JTextArea field = (JTextArea) comp;
              value = field.getText();	  
            }
            else if(aparam instanceof StringAnnotationParam) {
              JTextField field = (JTextField) comp;
              value = field.getText();	  
            }
            else if(aparam instanceof PathAnnotationParam) {
              JPathField field = (JPathField) comp;
              value = field.getPath();	  
            }
            else if(aparam instanceof ToolsetAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              String toolset = field.getSelected();
              if(toolset.equals("-") || (toolset.length() == 0))
                value = null;
              else 
                value = toolset;
            }
            else if(aparam instanceof WorkGroupAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              String ugname = field.getSelected(); 
              if(ugname.equals("-") || (ugname.length() == 0))
                value = null;
              else if(ugname.startsWith("[") && ugname.endsWith("]"))
                value = ugname.substring(1, ugname.length()-1);
              else 
                value = ugname;
            }
            else if(aparam instanceof BuilderIDAnnotationParam) {
              JBuilderIDSelectionField field = (JBuilderIDSelectionField) comp;
              value = field.getBuilderID();
            }
            else {
              assert(false) : "Unknown annotation parameter type!";
            }

            pAnnotation.setParamValue(aparam.getName(), value);
          }
        }
      }
      
      return pAnnotation;
    }

    /**
     * Rename the annotation.
     */ 
    public void 
    renameAnnotation
    (
     String aname
    ) 
    {
      pName = aname; 
      pAnnotationField.setActionCommand("annotation-changed:" + pName);
      pTopDrawer.setTitle("Annotation: " + pName);
      pIsModified = true;
    }

    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    annotationParamChanged
    (
     String name, 
     String aname
    ) 
    {
      unsavedChange("Parameter Changed: " + name + " (" + aname + ")"); 
      pIsModified = true;
    }
    
    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    annotationParamChanged
    (
     String name,
     Document doc
    ) 
    {
      String aname = pDocToParamName.get(doc);
      unsavedChange("Parameter Changed: " + name + " (" + aname + ")"); 
      pIsModified = true;
    }

    /**
     * Update the UI components.
     */
    private void 
    updateAnnotation() 
    {
      UIMaster.getInstance().updateAnnotationPluginField(pAnnotationField); 

      updateAnnotationFields();
      updateAnnotationParams();
    }

    /**
     * Update the annotation name, version and vendor fields.
     */ 
    private void 
    updateAnnotationFields()
    {
      pAnnotationField.removeActionListener(pParent);
      {
        pAnnotationField.setPlugin(pAnnotation);
        if(pAnnotation != null) {
          pVersionField.setText("v" + pAnnotation.getVersionID());
          pVendorField.setText(pAnnotation.getVendor());
        }
        else {
          pVersionField.setText("-");
          pVendorField.setText("-");
        }
      }
      pAnnotationField.addActionListener(pParent);
    }
  
    /**
     * Update the UI components associated annotation parameters.
     */ 
    private void 
    updateAnnotationParams() 
    {
      /* lookup common server info... */ 
      TreeSet<String> toolsets = null; 
      Set<String> workUsers  = null;
      Set<String> workGroups = null;
      if(pAnnotation != null) {
        UIMaster master = UIMaster.getInstance();
        MasterMgrClient mclient = master.getMasterMgrClient();

        boolean needsToolsets = false;
        boolean needsWorkGroups = false;
        for(AnnotationParam aparam : pAnnotation.getParams()) {
          if(aparam instanceof ToolsetAnnotationParam) 
            needsToolsets = true;
          else if(aparam instanceof WorkGroupAnnotationParam) 
            needsWorkGroups = true;
        }

        if(needsToolsets) {
          toolsets = new TreeSet<String>();
          toolsets.add("-");
          try {
            toolsets.addAll(mclient.getActiveToolsetNames());
          }
          catch(PipelineException ex) {
          }
        }

        if(needsWorkGroups) {
          try {
            WorkGroups wgroups = mclient.getWorkGroups();
            workGroups = wgroups.getGroups();
            workUsers  = wgroups.getUsers();
          }
          catch(PipelineException ex) {
            workGroups = new TreeSet<String>(); 
            workUsers  = new TreeSet<String>(); 
          }
        }
      }

      pParamComponents.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      boolean first = true;
      if(pAnnotation != null) {
        for(String pname : pAnnotation.getLayout()) {
          if(pname == null) {
            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          }
          else {
            if(!first) 
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
            
            AnnotationParam aparam = pAnnotation.getParam(pname);

            boolean paramEnabled = 
              (!pAnnotation.isParamConstant(pname) && 
               (pPrivilegeDetails.isAnnotator() ||
                pAnnotation.isParamModifiable(pname, PackageInfo.sUser, pPrivilegeDetails))); 

            if(aparam != null) {
              if(aparam instanceof BooleanAnnotationParam) {
                Boolean value = (Boolean) aparam.getValue();
                JBooleanField field = 
                  UIFactory.createTitledBooleanField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, sVSize, 
                   aparam.getDescription());
                field.setValue(value);

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);
                
                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof DoubleAnnotationParam) {
                Double value = (Double) aparam.getValue();
                JDoubleField field = 
                  UIFactory.createTitledDoubleField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof EnumAnnotationParam) {
                EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
	      
                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, eparam.getValues(), sVSize, 
                   aparam.getDescription());
	      
                field.setSelected((String) eparam.getValue());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof IntegerAnnotationParam) {
                Integer value = (Integer) aparam.getValue();
                JIntegerField field = 
                  UIFactory.createTitledIntegerField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof TextAreaAnnotationParam) {
        	TextAreaAnnotationParam bparam = (TextAreaAnnotationParam) aparam; 
                String value = (String) aparam.getValue();
                JTextArea field = 
                  UIFactory.createTitledEditableTextArea
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, bparam.getRows(), true,
                   aparam.getDescription());
                
                Document doc = field.getDocument();
                doc.addDocumentListener(pParent);
                pDocToParamName.put(doc, pname);
                pDocToAnnotation.put(doc, pName);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof StringAnnotationParam) {
                String value = (String) aparam.getValue();
                JTextField field = 
                  UIFactory.createTitledEditableTextField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof PathAnnotationParam) {
                Path value = (Path) aparam.getValue();
                JPathField field = 
                  UIFactory.createTitledPathField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof ToolsetAnnotationParam) {
                String value = (String) aparam.getValue();

                TreeSet<String> values = new TreeSet<String>(toolsets);
                if((value != null) && !values.contains(value))
                  values.add(value); 

                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, values, sVSize, 
                   aparam.getDescription());

                if(value != null) 
                  field.setSelected(value);
                else 
                  field.setSelected("-");

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof WorkGroupAnnotationParam) {
                WorkGroupAnnotationParam wparam = (WorkGroupAnnotationParam) aparam;
                String value = (String) aparam.getValue();

                TreeSet<String> values = new TreeSet<String>();
                values.add("-");
                if(wparam.allowsGroups()) {
                  for(String gname : workGroups) 
                    values.add("[" + gname + "]"); 
                }
                if(wparam.allowsUsers()) 
                  values.addAll(workUsers);
                
                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, values, sVSize, 
                   aparam.getDescription());
                
                if(value == null) 
                  field.setSelected("-");
                else {                  
                  if(wparam.allowsGroups() && workGroups.contains(value))
                    field.setSelected("[" + value + "]");
                  else if(wparam.allowsUsers() && workUsers.contains(value))
                    field.setSelected(value);
                  else 
                    field.setSelected("-");
                }

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	                      
              }
              else if(aparam instanceof BuilderIDAnnotationParam) {
                BuilderID value = (BuilderID) aparam.getValue();
                JBuilderIDSelectionField field = 
                  UIMaster.getInstance().createTitledBuilderIDSelectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());
                   
                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	    
              }
              else {
                assert(false) : 
                  ("Unknown annotation parameter type (" + aparam.getName() + ")!");
              }
            }
          }
	
          first = false;
        }
      }
      else {
        tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
        vpanel.add(Box.createHorizontalGlue());
      }

      UIFactory.addVerticalGlue(tpanel, vpanel);

      pParamsDrawer.setContents((JComponent) comps[2]);
      pParamsDrawer.revalidate();
      pParamsDrawer.repaint();
    }    

    /**
     * Update the appearance of the annotation fields after a change of value.
     */ 
    private void 
    doAnnotationChanged()
    {
      BaseAnnotation oannot = getAnnotation();

      String aname = pAnnotationField.getPluginName();
      if(aname == null) {
        pAnnotation = null;
      }
      else {
        VersionID avid = pAnnotationField.getPluginVersionID();
        String avendor = pAnnotationField.getPluginVendor();
        
        if((oannot == null) || 
           !oannot.getName().equals(aname) ||
           !oannot.getVersionID().equals(avid) ||
           !oannot.getVendor().equals(avendor)) {
          try {
            pAnnotation = PluginMgrClient.getInstance().newAnnotation(aname, avid, avendor);
            if(oannot != null)
              pAnnotation.setParamValues(oannot);
            unsavedChange("Annotation Changed: " + aname);
          }
          catch(PipelineException ex) {
            UIMaster.getInstance().showErrorDialog(ex);
            pAnnotation = null;	    
          }
        }
      }
      
      updateAnnotationFields();
      updateAnnotationParams();

      pIsModified = true;
    }

    

    private static final long serialVersionUID = -391904734639424578L;

    private String          pName; 
    private BaseAnnotation  pAnnotation; 
    private boolean         pIsModified; 

    private JNodeAnnotationsPanel  pParent; 
    private JDrawer                pTopDrawer;
 
    private JPluginSelectionField  pAnnotationField;
    private JTextField             pVersionField; 
    private JTextField             pVendorField; 

    private JDrawer                    pParamsDrawer; 
    private TreeMap<String,Component>  pParamComponents; 
    
    private ListMap<Document, String> pDocToParamName;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Apply the annotations changes. 
   */ 
  private
  class ApplyTask
    extends Thread
  {
    public 
    ApplyTask
    (
     String name, 
     TreeMap<String,BaseAnnotation> modified, 
     TreeSet<String> dead
    ) 
    {
      super("JNodeAnnotationsPanel:ApplyTask");

      pNodeName = name; 
      pModified = modified;
      pDead = dead;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Applying Annotation Changes...")) {
	try {
          MasterMgrClient mclient = master.getMasterMgrClient();

          for(String aname : pDead) 
            mclient.removeAnnotation(pNodeName, aname); 

          for(String aname : pModified.keySet()) {
            BaseAnnotation annot = pModified.get(aname); 
            mclient.addAnnotation(pNodeName, aname, annot);
          }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private String                          pNodeName; 
    private TreeMap<String,BaseAnnotation>  pModified; 
    private TreeSet<String>                 pDead; 
  }

 
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
     NodeCommon com
    ) 
    {
      UIMaster.getInstance().super(pGroupID, com, false, pAuthor, pView, false);
      setName("JNodeAnnotationsPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com,
     boolean useDefault, 
     boolean substitute 
    ) 
    {
      UIMaster.getInstance().super(pGroupID, com, useDefault, pAuthor, pView, substitute);
      setName("JNodeAnnotationsPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid, 
     String evendor
    ) 
    {
      UIMaster.getInstance().super
	(pGroupID, com, ename, evid, evendor, pAuthor, pView, false);
      setName("JNodeAnnotationsPanel:EditTask");
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
      this(name, null, null, null, null, null, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     String name, 
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
				   batchSize, priority, rampUp, 
				   maxLoad, minMemory, minDisk,
				   selectionKeys, licenseKeys, hardwareKeys);
      setName("JNodeAnnotationsPanel:QueueJobsTask");
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
      UIMaster.getInstance().super("JNodeAnnotationsPanel", 
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
      UIMaster.getInstance().super("JNodeAnnotationsPanel", 
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
      UIMaster.getInstance().super("JNodeAnnotationsPanel", 
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
      UIMaster.getInstance().super("JNodeAnnotationsPanel", 
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
      setName("JNodeAnnotationsPanel:RemoveFilesTask");
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
  
  private static final long serialVersionUID = -1091065151346911428L;
  
  private static final int  sTSize = 150;
  private static final int  sVSize = 150;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The toolset used to build the editor menu.
   */ 
  private String  pEditorMenuToolset;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu.
   */ 
  private JPopupMenu  pWorkingPopup; 
  
  /**
   * The working file popup menu items.
   */ 
  private JMenuItem  pApplyItem;
  private JMenuItem  pAddAnnotationItem;
  private JMenuItem  pQueueJobsItem;
  private JMenuItem  pQueueJobsSpecialItem;
  private JMenuItem  pPauseJobsItem;
  private JMenuItem  pResumeJobsItem;
  private JMenuItem  pPreemptJobsItem;
  private JMenuItem  pKillJobsItem;
  private JMenuItem  pRemoveFilesItem;  

  /**
   * The checked-in file popup menu.
   */ 
  private JPopupMenu  pCheckedInPopup; 

  /**
   * The edit with submenus.
   */ 
  private JMenuItem[]  pEditItems;
  private JMenuItem[]  pEditWithDefaultItems;
  private JMenu[]      pEditWithMenus; 
  private JMenuItem    pEditAsOwnerItem; 


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

  /**  
   * The button used to apply changes to the node annotations. 
   */ 
  private JButton  pApplyButton;

  /**
   * The top-level annotations box.
   */ 
  private Box pAnnotationsBox;

  /**
   * The annotation UI components indexed by annotation name.
   */
  private TreeMap<String,JAnnotationPanel>  pAnnotationsPanels; 

  /**
   * The names of obsolete annotations. 
   */ 
  private TreeSet<String>  pDeadAnnotations;

  /**
   * The scroll panel containing the messages.
   */ 
  private JScrollPane  pScroll;
  
  /**
   * The annotation names indexed by the TextArea parameter documents.
   */ 
  private ListMap<Document, String> pDocToAnnotation;
}
