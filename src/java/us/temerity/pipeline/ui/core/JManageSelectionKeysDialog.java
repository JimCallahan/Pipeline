// $Id: JManageSelectionKeysDialog.java,v 1.7 2006/01/02 20:46:53 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   S E L E C T I O N   K E Y S   D I A L O G                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for adding, removing and modifying selection keys, groups and schedules.
 */ 
public 
class JManageSelectionKeysDialog
  extends JBaseDialog
  implements MouseListener, KeyListener, ActionListener, AdjustmentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageSelectionKeysDialog() 
  {
    super("Manage Selection Keys, Groups and Schedules", false);

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      	
      {
	pKeysPopup = new JPopupMenu();

	item = new JMenuItem("Add Key");
	pKeysAddItem = item;
	item.setActionCommand("key-add");
	item.addActionListener(this);
	pKeysPopup.add(item);
	
	item = new JMenuItem("Remove Key");
	pKeysRemoveItem = item;
	item.setActionCommand("key-remove");
	item.addActionListener(this);
	pKeysPopup.add(item);
      }

      {
	pGroupsPopup = new JPopupMenu();

	item = new JMenuItem("Add Group");
	pGroupsAddItem = item;
	item.setActionCommand("group-add");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Clone Group");
	pGroupsCloneItem = item;
	item.setActionCommand("group-clone");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Remove Group");
	pGroupsRemoveItem = item;
	item.setActionCommand("group-remove");
	item.addActionListener(this);
	pGroupsPopup.add(item);
      }

      {
	pSchedulesPopup = new JPopupMenu();

	item = new JMenuItem("Details...");
	pSchedulesDetailsItem = item;
	item.setActionCommand("schedule-details");
	item.addActionListener(this);
	pSchedulesPopup.add(item);
	
	pSchedulesPopup.addSeparator();

	item = new JMenuItem("Add Schedule");
	pSchedulesAddItem = item;
	item.setActionCommand("schedule-add");
	item.addActionListener(this);
	pSchedulesPopup.add(item);
	
	item = new JMenuItem("Remove Schedule");
	pSchedulesRemoveItem = item;
	item.setActionCommand("schedule-remove");
	item.addActionListener(this);
	pSchedulesPopup.add(item);
      }

      updateMenuToolTips(); 
    }

    /* create dialog body components */ 
    { 
      setLayout(new BorderLayout());

      JTabbedPanel tab = new JTabbedPanel();
      pTab = tab;

      /* selection keys panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  //pKeysHeaderPanel = panel;
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
// 	  panel.addMouseListener(new KeyFocuser(panel));
	  
	  {
	    JLabel label = new JLabel("Manage Selection Keys:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  body.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainPanel");
	  panel.setLayout(new BorderLayout());
	   
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
// 	  panel.addMouseListener(new KeyFocuser(panel));

	  {
	    SelectionKeysTableModel model = new SelectionKeysTableModel();
	    pKeysTableModel = model;
	    
	    JTablePanel tpanel = new JTablePanel(model);
	    pKeysTablePanel = tpanel;
	    
	    {
	      JScrollPane scroll = tpanel.getTableScroll();
	      scroll.addMouseListener(this); 
	      scroll.setFocusable(true);
	      scroll.addKeyListener(this);
	      // 		scroll.addMouseListener(new KeyFocuser(scroll));
	    }
	    
	    {
	      JTable table = tpanel.getTable();
	      table.addMouseListener(this); 
	      table.setFocusable(true);
	      table.addKeyListener(this);
	      // 		table.addMouseListener(new KeyFocuser(table));
	    }

	    panel.add(tpanel);
	  }

	  body.add(panel);
	}

	tab.addTab(body);
      }

      /* selection groups panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  //pGroupsHeaderPanel = panel;
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
// 	  panel.addMouseListener(new KeyFocuser(panel));
	  
	  {
	    JLabel label = new JLabel("Manage Selection Groups:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  body.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	   
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
// 	  panel.addMouseListener(new KeyFocuser(panel));

	  {	
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.setAlignmentX(0.5f);
	    
	    {
	      SelectionGroupNamesTableModel model = new SelectionGroupNamesTableModel(this);
	      pGroupNamesTableModel = model;
	  
	      JTablePanel tpanel =
		new JTablePanel(model, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	      pGroupNamesTablePanel = tpanel;
	      
	      {
		JScrollPane scroll = tpanel.getTableScroll();
		scroll.addMouseListener(this); 
		scroll.setFocusable(true);
		scroll.addKeyListener(this);
// 		scroll.addMouseListener(new KeyFocuser(scroll));
	      }
	      
	      {
		JTable table = tpanel.getTable();
		table.addMouseListener(this); 
		table.setFocusable(true);
		table.addKeyListener(this);
// 		table.addMouseListener(new KeyFocuser(table));
	      }
	      
	      vbox.add(tpanel);
	    }
	    
	    vbox.add(Box.createRigidArea(new Dimension(0, 14)));
	    
	    vbox.setMinimumSize(new Dimension(186, 30));
	    vbox.setMaximumSize(new Dimension(186, Integer.MAX_VALUE));
	    
	    panel.add(vbox);
	  }
	  
	  panel.add(Box.createRigidArea(new Dimension(2, 0)));
	  
	  {	    
	    SelectionGroupsTableModel model = new SelectionGroupsTableModel(this);
	    pGroupsTableModel = model;
	    
	    JTablePanel tpanel = new JTablePanel(model);
	    pGroupsTablePanel = tpanel;
	    
	    {
	      JScrollPane scroll = tpanel.getTableScroll();
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	      
	      scroll.getVerticalScrollBar().addAdjustmentListener(this);
	      
	      scroll.addMouseListener(this); 
	      scroll.setFocusable(true);
	      scroll.addKeyListener(this);
// 	      scroll.addMouseListener(new KeyFocuser(scroll));
	    }
	    
	    {
	      JTable table = tpanel.getTable();
	      table.addMouseListener(this); 
	      table.setFocusable(true);
	      table.addKeyListener(this);
// 	      table.addMouseListener(new KeyFocuser(table));
	    }
	    
	    panel.add(tpanel);
	  }

	  /* sychronize the selected rows in the two tables */ 
	  {
	    JTable ntable = pGroupNamesTablePanel.getTable();
	    JTable gtable = pGroupsTablePanel.getTable();
	    
	    ntable.getSelectionModel().addListSelectionListener
	      (new TableSyncSelector(ntable, gtable));
	    
	    gtable.getSelectionModel().addListSelectionListener
	      (new TableSyncSelector(gtable, ntable));
	  }
	  
	  body.add(panel);
	}

	tab.addTab(body);
      }

      /* selection schedule panel */ 
      {

	  // ...

      }

      String extra[][] = {
	null,
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI(null, false, tab, "Confirm", "Apply", extra, "Close");
      
      pUpdateButton = btns[1];
      pUpdateButton.setToolTipText(UIFactory.formatToolTip(
        "Update the selection keys, groups and schedules."));

      pConfirmButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes to selection biases and schedule details and close."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes to selection biases and schedule details."));

      updateAll();
      pack();
    }


    pKeysCreateDialog      = new JCreateSelectionKeyDialog(this);
    pGroupsCreateDialog    = new JCreateSelectionGroupDialog(this);
    pSchedulesCreateDialog = new JCreateSelectionScheduleDialog(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current selection keys and update the UI components.
   */ 
  public void 
  updateAll() 
  { 
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      pIsPrivileged = client.isPrivileged(false);

      ArrayList<SelectionKey> keys = client.getSelectionKeys();
      TreeMap<String,SelectionGroup> groups = client.getSelectionGroups();
      TreeMap<String,SelectionSchedule> schedules = client.getSelectionSchedules();
      
      TreeMap<String,String> keyDesc = new TreeMap<String,String>();
      for(SelectionKey key : keys) 
	keyDesc.put(key.getName(), key.getDescription());
      
      /* update selection keys */ 
      pKeysTableModel.setSelectionKeys(keys);

      /* update selection groups */ 
      pGroupNamesTableModel.setNames(groups.keySet());
      TreeSet<String> obsolete = 
	pGroupsTableModel.setSelectionGroups(groups, keyDesc, pIsPrivileged);
      pGroupsTablePanel.refilterColumns(obsolete);

      pGroupsTablePanel.doShowAllColumns();  // ???

      /* update selection schedules */ 
      // ...
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }

    updateKeysMenu();
    updateGroupsMenu();
    updateSchedulesMenu();

    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
  }

  /**
   * Update job server tab in all job browser panels.
   */ 
  private void 
  updateJobBrowsers() 
  {
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    if(master.beginPanelOp("Updating...")) {
      try {
	for(JQueueJobBrowserPanel panel : master.getQueueJobBrowserPanels().getPanels()) {
	  
	  TreeMap<Long,QueueJobGroup> groups = client.getJobGroups(); 
	  TreeMap<Long,JobStatus> jobStatus = 
	    client.getJobStatus(new TreeSet<Long>(groups.keySet()));
	  TreeMap<Long,QueueJobInfo> jobInfo = client.getRunningJobInfo();
	  TreeMap<String,QueueHost> hosts = client.getHosts(); 
	  TreeSet<String> selectionGroups = client.getSelectionGroupNames();
	  TreeSet<String> selectionSchedules = client.getSelectionScheduleNames();

	  panel.updateJobs(groups, jobStatus, jobInfo, 
			   hosts, selectionGroups, selectionSchedules); 
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      finally {
	master.endPanelOp("Done.");
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the sort of the selection group names table to match the selection groups table.
   */ 
  public void
  sortNamesTable
  (
   int[] rowToIndex
  )
  {
    if(pGroupNamesTableModel != null) 
      pGroupNamesTableModel.externalSort(rowToIndex);
  }

  /** 
   * Update the sort of the selection groups table to match the selection group names table.
   */ 
  public void
  sortGroupsTable
  (
   int[] rowToIndex
  )
  {
    if(pGroupsTableModel != null) 
      pGroupsTableModel.externalSort(rowToIndex);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection keys menu.
   */ 
  public void 
  updateKeysMenu() 
  {
    boolean selected = (pKeysTablePanel.getTable().getSelectedRowCount() > 0);
    pKeysAddItem.setEnabled(pIsPrivileged); 
    pKeysRemoveItem.setEnabled(pIsPrivileged && selected); 
  }

  /**
   * Update the selection groups menu.
   */ 
  public void 
  updateGroupsMenu() 
  {
    int numSelected = pGroupsTablePanel.getTable().getSelectedRowCount();
    pGroupsAddItem.setEnabled(pIsPrivileged); 
    pGroupsCloneItem.setEnabled(pIsPrivileged && (numSelected == 1)); 
    pGroupsRemoveItem.setEnabled(pIsPrivileged && (numSelected > 0)); 
  }

  /**
   * Update the selection schedules menu.
   */ 
  public void 
  updateSchedulesMenu() 
  {
//     boolean selected = (pSchedulesTablePanel.getTable().getSelectedRowCount() > 0);
//     pSchedulesDetailsItem.setEnabled(selected); 
//     pSchedulesAddItem.setEnabled(pIsPrivileged); 
//     pSchedulesRemoveItem.setEnabled(pIsPrivileged && selected); 
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
      (pKeysAddItem, prefs.getSelectionKeysAdd(),
       "Add a new selection key.");
    updateMenuToolTip
      (pKeysRemoveItem, prefs.getSelectionKeysRemove(),
       "Remove the selected selection keys.");

    updateMenuToolTip
      (pGroupsAddItem, prefs.getSelectionGroupsAdd(),
       "Add a new selection group.");
    updateMenuToolTip
      (pGroupsCloneItem, prefs.getSelectionGroupsClone(),
       "Add a new selection group which is a copy of the selected group.");
    updateMenuToolTip
      (pGroupsRemoveItem, prefs.getSelectionGroupsRemove(),
       "Remove the selected selection groups.");
    
    updateMenuToolTip
      (pSchedulesDetailsItem, prefs.getSelectionSchedulesDetails(),
       "Show the selection schedule details.");
    updateMenuToolTip
      (pSchedulesAddItem, prefs.getSelectionSchedulesAdd(),
       "Add a new selection schedule.");
    updateMenuToolTip
      (pSchedulesRemoveItem, prefs.getSelectionSchedulesRemove(),
       "Remove the selected selection schedules.");
  }

  /**
   * Update the tool tip for the given menu item.
   */   
  private void 
  updateMenuToolTip
  (
   JMenuItem item, 
   HotKey key,
   String desc
  ) 
  {
    String text = null;
    if(UserPrefs.getInstance().getShowMenuToolTips()) {
      if(desc != null) {
	if(key != null) 
	  text = (desc + " <P>Hot Key = " + key);
	else 
	  text = desc;
      }
      else {
	text = ("Hot Key = " + key);
      }
    }
    
    if(text != null) 
      item.setToolTipText(UIFactory.formatToolTip(text));
    else 
      item.setToolTipText(null);
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

	/* BUTTON3: popup menus */ 
	if((mods & (on1 | off1)) == on1) {
	  switch(pTab.getSelectedIndex()) {      
	  case 0:
	    updateKeysMenu();
	    pKeysPopup.show(e.getComponent(), e.getX(), e.getY());
	    break;
	    
	  case 1:
	    updateGroupsMenu();
	    pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());
	    break;
	    
	  case 2:
	    updateSchedulesMenu();
	    pSchedulesPopup.show(e.getComponent(), e.getX(), e.getY());      
	  }
	}
	else {
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
    boolean unsupported = false;
    UserPrefs prefs = UserPrefs.getInstance();
    switch(pTab.getSelectedIndex()) {      
    case 0:
      if((prefs.getSelectionKeysAdd() != null) &&
	 prefs.getSelectionKeysAdd().wasPressed(e))
	doKeysAdd();
      else if((prefs.getSelectionKeysRemove() != null) &&
	      prefs.getSelectionKeysRemove().wasPressed(e))
	doKeysRemove();
      else 
	unsupported = true;
      break;
      
    case 1:
      if((prefs.getSelectionGroupsAdd() != null) &&
	 prefs.getSelectionGroupsAdd().wasPressed(e))
	doGroupsAdd();
      else if((prefs.getSelectionGroupsClone() != null) &&
	 prefs.getSelectionGroupsClone().wasPressed(e))
	doGroupsClone();
      else if((prefs.getSelectionGroupsRemove() != null) &&
	      prefs.getSelectionGroupsRemove().wasPressed(e))
	doGroupsRemove();
      else 
	unsupported = true;
      break;
      
    case 2:
      if((prefs.getSelectionSchedulesDetails() != null) &&
	 prefs.getSelectionSchedulesDetails().wasPressed(e))
	doSchedulesDetails();
      else if((prefs.getSelectionSchedulesAdd() != null) &&
	      prefs.getSelectionSchedulesAdd().wasPressed(e))
	doSchedulesAdd();
      else if((prefs.getSelectionSchedulesRemove() != null) &&
	      prefs.getSelectionSchedulesRemove().wasPressed(e))
	doSchedulesRemove();
      else 
	unsupported = true;
    }

    if(unsupported) {
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
    if(cmd.equals("key-add")) 
      doKeysAdd();
    else if(cmd.equals("key-remove")) 
      doKeysRemove();

    else if(cmd.equals("group-add")) 
      doGroupsAdd();
    else if(cmd.equals("group-clone")) 
      doGroupsClone();
    else if(cmd.equals("group-remove")) 
      doGroupsRemove();

    else if(cmd.equals("schedule-details")) 
      doSchedulesDetails();
    else if(cmd.equals("schedule-add")) 
      doSchedulesAdd();
    else if(cmd.equals("schedule-remove")) 
      doSchedulesRemove();

    else if(cmd.equals("update")) 
      doUpdate();
    else 
      super.actionPerformed(e);
  }


  /*-- ADJUSTMENT LISTENER METHODS ---------------------------------------------------------*/

  /**
   * Invoked when the value of the adjustable has changed.
   */ 
  public void
  adjustmentValueChanged
  (
   AdjustmentEvent e
  )
  { 
    JViewport nview = pGroupNamesTablePanel.getTableScroll().getViewport();
    JViewport gview = pGroupsTablePanel.getTableScroll().getViewport();
    if((nview != null) && (gview != null)) {
      Point npos = nview.getViewPosition();    
      Point gpos = gview.getViewPosition();    
      
      if(npos.y != gpos.y) {
 	npos.y = gpos.y;
 	nview.setViewPosition(npos);
      }
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm() 
  {
    doApply();
    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  public void 
  doApply()
  {
    ArrayList<SelectionGroup> groups = pGroupsTableModel.getModifiedGroups();
    if(groups != null) {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.getQueueMgrClient();
      try {
	client.editSelectionGroups(groups);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }

      updateAll();
    }
  }

  /*
   * Update the table with the current license keys.
   */ 
  private void 
  doUpdate() 
  {
    pKeysTablePanel.stopEditing();
    pGroupsTablePanel.stopEditing();

    updateAll();
  }

  /**
   * Enable the Confirm/Apply buttons in response to an edit.
   */ 
  public void 
  doEdited() 
  {
    pConfirmButton.setEnabled(true);
    pApplyButton.setEnabled(true);
  }
 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a filename suffix to the table.
   */ 
  private void 
  doKeysAdd()
  {
    pKeysTablePanel.stopEditing();

    boolean modified = false;
    {
      pKeysCreateDialog.setVisible(true);
      
      if(pKeysCreateDialog.wasConfirmed()) {
	String kname = pKeysCreateDialog.getKeyName();
	String desc  = pKeysCreateDialog.getDescription();
	if((kname != null) && (kname.length() > 0) &&
	   (desc != null) && (desc.length() > 0)) {
	  
	  UIMaster master = UIMaster.getInstance();
	  QueueMgrClient client = master.getQueueMgrClient();
	  try {
	    client.addSelectionKey(new SelectionKey(kname, desc));
	    modified = true;
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}
      }
    }

    if(modified) 
      updateAll();    
  }
  
  /**
   * Remove the selected rows.
   */ 
  private void 
  doKeysRemove() 
  {
    pKeysTablePanel.cancelEditing();

    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.getQueueMgrClient();
      try {
	int rows[] = pKeysTablePanel.getTable().getSelectedRows();
	int wk;
	for(wk=0; wk<rows.length; wk++) {
	  String kname = pKeysTableModel.getName(rows[wk]);
	  client.removeSelectionKey(kname);
	  modified = true;
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }
    
    if(modified) 
      updateAll();     
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a selection group to the table.
   */ 
  private void 
  doGroupsAdd()
  {
    pGroupsTablePanel.stopEditing();

    boolean modified = false;
    {
      pGroupsCreateDialog.setVisible(true);

      if(pGroupsCreateDialog.wasConfirmed()) {
	String gname = pGroupsCreateDialog.getName();
	if((gname != null) && (gname.length() > 0)) {
	  UIMaster master = UIMaster.getInstance();
	  QueueMgrClient client = master.getQueueMgrClient();
	  try {
	    client.addSelectionGroup(gname);
	    modified = true;
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}
      }
    }

    if(modified) {
      updateAll();
      updateJobBrowsers();
    }
  }
  
  /**
   * Add a selection group to the table which is a copy of the currently selected group.
   */ 
  private void 
  doGroupsClone()
  {
    pGroupsTablePanel.stopEditing();

    boolean modified = false;
    {
      pGroupsCreateDialog.setVisible(true);

      SelectionGroup group = null;
      {
	int rows[] = pGroupsTablePanel.getTable().getSelectedRows();
	int wk;
	for(wk=0; wk<rows.length; wk++) {
	  group = pGroupsTableModel.getGroup(rows[wk]);
	  if(group != null) 
	    break;
	}
      }
      
      if(group != null) {
	if(pGroupsCreateDialog.wasConfirmed()) {
	  String gname = pGroupsCreateDialog.getName();
	  if((gname != null) && (gname.length() > 0)) {
	    UIMaster master = UIMaster.getInstance();
	    QueueMgrClient client = master.getQueueMgrClient();
	    try {
	      client.addSelectionGroup(gname);
	      client.editSelectionGroup(new SelectionGroup(gname, group));
	      modified = true;
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	    }
	  }
	}
      }
    }
      
    if(modified) {
      updateAll();
      updateJobBrowsers();
    }
  }
  
  /**
   * Remove the selected rows from the selection groups table.
   */ 
  private void 
  doGroupsRemove() 
  {
    pGroupsTablePanel.cancelEditing();

    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.getQueueMgrClient();
      try {
	TreeSet<String> gnames = new TreeSet<String>();
	{
	  int rows[] = pGroupsTablePanel.getTable().getSelectedRows();
	  int wk;
	  for(wk=0; wk<rows.length; wk++) 
	    gnames.add(pGroupsTableModel.getGroupName(rows[wk]));
	}
	
	if(!gnames.isEmpty()) {
	  client.removeSelectionGroups(gnames);
	  modified = true; 
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    if(modified) {
      updateAll();
      updateJobBrowsers();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Show the schedule details dialog.
   */ 
  private void 
  doSchedulesDetails()
  {

  }
  
  /**
   * Add a selection schedule to the table.
   */ 
  private void 
  doSchedulesAdd()
  {
    boolean modified = false;

    // ...

    if(modified) {
      updateAll();
      updateJobBrowsers();
    }
  }
  
  /**
   * Remove the selected rows from the selection schedules table.
   */ 
  private void 
  doSchedulesRemove() 
  {
    boolean modified = false;

    // ...

    if(modified) {
      updateAll();
      updateJobBrowsers();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class TableSyncSelector
    implements ListSelectionListener
  {
    public 
    TableSyncSelector
    (
     JTable source,
     JTable target
    ) 
    {
      pSourceTable = source;
      pTargetTable = target;
    }

    /**
     * Called whenever the value of the selection changes.
     */ 
    public void 	
    valueChanged
    (
     ListSelectionEvent e
    )
    {
      if(e.getValueIsAdjusting()) 
	return;
      
      DefaultListSelectionModel smodel = 
	(DefaultListSelectionModel) pTargetTable.getSelectionModel();

      ListSelectionListener[] listeners = smodel.getListSelectionListeners();
      {
	int wk;
	for(wk=0; wk<listeners.length; wk++) 
	  smodel.removeListSelectionListener(listeners[wk]);
      }

      smodel.clearSelection();

      {
	int rows[] = pSourceTable.getSelectedRows();
	int wk; 
	for(wk=0; wk<rows.length; wk++) 
	  smodel.addSelectionInterval(rows[wk], rows[wk]);	  
      }
      
      {
	int wk;
	for(wk=0; wk<listeners.length; wk++) 
	  smodel.addListSelectionListener(listeners[wk]);
      }

      pTargetTable.repaint();
    }

    private JTable pSourceTable;
    private JTable pTargetTable;
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5170821838437538479L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The tabbed panel.
   */ 
  private JTabbedPanel  pTab;

  /**
   * The dialog update button.
   */ 
  private JButton  pUpdateButton; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection keys popup menu.
   */ 
  private JPopupMenu  pKeysPopup;
  
  /**
   * The selection keys popup menu items.
   */ 
  private JMenuItem  pKeysAddItem;
  private JMenuItem  pKeysRemoveItem;


  /**
   * The selection keys table model.
   */ 
  private SelectionKeysTableModel  pKeysTableModel;

  /**
   * The selection keys table panel.
   */ 
  private JTablePanel  pKeysTablePanel;


  /**
   * The new key creation dialog.
   */ 
  private JCreateSelectionKeyDialog  pKeysCreateDialog; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection groups popup menu.
   */ 
  private JPopupMenu  pGroupsPopup;
  
  /**
   * The selection groups popup menu items.
   */ 
  private JMenuItem  pGroupsAddItem;
  private JMenuItem  pGroupsCloneItem;
  private JMenuItem  pGroupsRemoveItem;


  /**
   * The selection group names table model.
   */ 
  private SelectionGroupNamesTableModel  pGroupNamesTableModel;

  /**
   * The selection group names table panel.
   */ 
  private JTablePanel  pGroupNamesTablePanel;


  /**
   * The selection groups table model.
   */ 
  private SelectionGroupsTableModel  pGroupsTableModel;

  /**
   * The selection groups table panel.
   */ 
  private JTablePanel  pGroupsTablePanel;


  /**
   * The new selection group creation dialog.
   */ 
  private JCreateSelectionGroupDialog pGroupsCreateDialog; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection schedules popup menu.
   */ 
  private JPopupMenu  pSchedulesPopup;
  
  /**
   * The selection schedules popup menu items.
   */ 
  private JMenuItem  pSchedulesDetailsItem;
  private JMenuItem  pSchedulesAddItem;
  private JMenuItem  pSchedulesRemoveItem;


  // ... 

  /**
   * The new selection group creation dialog.
   */ 
  private JCreateSelectionScheduleDialog pSchedulesCreateDialog; 

}

