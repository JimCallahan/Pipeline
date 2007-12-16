// $Id: JManageHardwareKeysDialog.java,v 1.2 2007/12/16 06:32:49 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   H A R D W A R E   K E Y S   D I A L O G                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for adding, removing and modifying hardware keys and groups.
 */ 
public 
class JManageHardwareKeysDialog
  extends JTopLevelDialog
  implements ListSelectionListener, MouseListener, KeyListener, ActionListener, 
             AdjustmentListener, ChangeListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageHardwareKeysDialog() 
  {
    super("Manage Hardware Keys and Groups");

    pPrivilegeDetails = new PrivilegeDetails();

    pGroupNames = new TreeSet<String>(); 

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
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Hardware Keys:");
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

	  {
	    BaseKeysTableModel model = new BaseKeysTableModel();
	    pKeysTableModel = model;
	    
	    JTablePanel tpanel = new JTablePanel(model);
	    pKeysTablePanel = tpanel;
	    
	    {
	      JScrollPane scroll = tpanel.getTableScroll();
	      scroll.addMouseListener(this); 
	      scroll.setFocusable(true);
	      scroll.addKeyListener(this);
	    }
	    
	    {
	      JTable table = tpanel.getTable();
	      table.addMouseListener(this); 
	      table.setFocusable(true);
	      table.addKeyListener(this);
	    }

	    panel.add(tpanel);
	  }

	  body.add(panel);
	}

	tab.addTab(body);
      }

      /* hardware groups panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Hardware Groups:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  panel.add(Box.createRigidArea(new Dimension(30, 0)));

	  body.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	   
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);

	  {	
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.setAlignmentX(0.5f);
	    
	    {
	      HardwareGroupNamesTableModel model = new HardwareGroupNamesTableModel(this);
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
	      }
	      
	      {
		JTable table = tpanel.getTable();
		table.addMouseListener(this); 
		table.setFocusable(true);
		table.addKeyListener(this);
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
	    HardwareGroupsTableModel model = new HardwareGroupsTableModel(this);
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
	    }
	    
	    {
	      JTable table = tpanel.getTable();
	      table.addMouseListener(this); 
	      table.setFocusable(true);
	      table.addKeyListener(this);
	    }
	    
	    panel.add(tpanel);
	  }

	  /* Synchronize the selected rows in the two tables */ 
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

      String extra[][] = {
	null,
	{ "Update", "update"},
	{ "Edit", "edit" },
      };

      JButton btns[] = super.initUI(null, tab, "Confirm", "Apply", extra, "Close");
      
      pUpdateButton = btns[1];
      pUpdateButton.setToolTipText(UIFactory.formatToolTip(
        "Update the hardware keys and groups."));
      
      pEditButton = btns[2];
      pEditButton.setToolTipText(UIFactory.formatToolTip(
        "Edit the selected hardware key."));

      pConfirmButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes and close."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes."));
      
      pKeyDetailsDialog = new JKeyChooserConfigDialog(this, "Hardware");

      updateAll();
      pack();
    }

    /* This needs to be run after the edit button is created. */
    pTab.addChangeListener(this);
    
    pKeysCreateDialog   = new JCreateHardwareKeyDialog(this);
    pGroupsNewDialog    = new JNewHardwareGroupDialog(this);
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
      pPrivilegeDetails = master.getMasterMgrClient().getPrivilegeDetails();

      ArrayList<BaseKey> keys = new ArrayList<BaseKey>();
      for (HardwareKey key : client.getHardwareKeys() )
        keys.add(key);
      TreeMap<String,HardwareGroup> groups = client.getHardwareGroups();

      pGroupNames.clear();
      pGroupNames.addAll(groups.keySet());

      TreeMap<String,String> keyDesc = new TreeMap<String,String>();
      for(BaseKey key : keys) 
	keyDesc.put(key.getName(), key.getDescription());
      
      /* update hardware keys */ 
      pKeysTableModel.setKeys(keys);
      
      /* update hardware groups */ 
      pGroupNamesTableModel.setNames(pGroupNames);
      TreeMap<String,Boolean> modified = 
	pGroupsTableModel.setHardwareGroups(groups, keyDesc, pPrivilegeDetails);
      pGroupsTablePanel.refilterColumns(modified);

    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    updateKeysMenu();
    updateGroupsMenu();

    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
  }

  /**
   * Update job server tab in all job browser panels.
   */ 
  private void 
  updateJobBrowsers() 
  {
    // FIX THIS!!!

//     UIMaster master = UIMaster.getInstance();
//     QueueMgrClient client = master.getQueueMgrClient();
//     if(master.beginPanelOp("Updating...")) {
//       try {
// 	TreeMap<Long,QueueJobGroup> groups = client.getJobGroups(); 
// 	TreeMap<Long,JobStatus> jobStatus = 
// 	  client.getJobStatus(new TreeSet<Long>(groups.keySet()));
// 	TreeMap<Long,QueueJobInfo> jobInfo = client.getRunningJobInfo();
// 	TreeMap<String,QueueHostInfo> hosts = client.getHosts(); 
// 	TreeSet<String> selectionGroups = client.getSelectionGroupNames();
// 	TreeSet<String> selectionSchedules = client.getSelectionScheduleNames();

// 	for(JQueueJobBrowserPanel panel : master.getQueueJobBrowserPanels().getPanels()) 
// 	  panel.updateJobs(groups, jobStatus, jobInfo, 
// 			   hosts, selectionGroups, selectionSchedules); 
//       }
//       catch(PipelineException ex) {
// 	showErrorDialog(ex);
//       }
//       finally {
// 	master.endPanelOp("Done.");
//       }
//     }
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
    pKeysAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pKeysRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
  }

  /**
   * Update the selection groups menu.
   */ 
  public void 
  updateGroupsMenu() 
  {
    int numSelected = pGroupsTablePanel.getTable().getSelectedRowCount();
    pGroupsAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pGroupsCloneItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && (numSelected == 1)); 
    pGroupsRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && (numSelected > 0)); 
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

  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

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
	int on1  = (InputEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (InputEvent.BUTTON1_DOWN_MASK | 
		    InputEvent.BUTTON2_DOWN_MASK | 
		    InputEvent.SHIFT_DOWN_MASK |
		    InputEvent.ALT_DOWN_MASK |
		    InputEvent.CTRL_DOWN_MASK);

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
      if((prefs.getHardwareKeysAdd() != null) &&
	 prefs.getHardwareKeysAdd().wasPressed(e))
	doKeysAdd();
      else if((prefs.getHardwareKeysRemove() != null) &&
	      prefs.getHardwareKeysRemove().wasPressed(e))
	doKeysRemove();
      else 
	unsupported = true;
      break;
      
    case 1:
      if((prefs.getHardwareGroupsAdd() != null) &&
	 prefs.getHardwareGroupsAdd().wasPressed(e))
	doGroupsAdd();
      else if((prefs.getHardwareGroupsClone() != null) &&
	 prefs.getHardwareGroupsClone().wasPressed(e))
	doGroupsClone();
      else if((prefs.getHardwareGroupsRemove() != null) &&
	      prefs.getHardwareGroupsRemove().wasPressed(e))
	doGroupsRemove();
      else 
	unsupported = true;
      break;
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
  
  
  /*-- CHANGE LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the tab panel changes
   */
  public void stateChanged
  (
    ChangeEvent e
  )
  {
    int i = pTab.getSelectedIndex();
    pEditButton.setEnabled((i == 0));
  } 


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

    else if(cmd.equals("update")) 
      doUpdate();
    else if(cmd.equals("edit")) 
      doEdit();
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
  @Override
  public void 
  doConfirm() 
  {
    doApply();
    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  @Override
  public void 
  doApply()
  {
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      ArrayList<HardwareGroup> groups = pGroupsTableModel.getModifiedGroups();
      if(groups != null) 
	client.editHardwareGroups(groups);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    
    updateAll();
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
 
  private void
  doEdit()
  {
    UIMaster master = UIMaster.getInstance();
    int row = pKeysTablePanel.getTable().getSelectedRow();
    if(row == -1) 
      return;

    BaseKeyChooser oplugin = pKeysTableModel.getKeyChooser(row);
    pKeyDetailsDialog.setKeyChooser(oplugin);
    pKeyDetailsDialog.setVisible(true);
    
    if(pKeyDetailsDialog.wasConfirmed()) {
      BaseKeyChooser plugin = pKeyDetailsDialog.getKeyChooser();
      BaseKey key = pKeysTableModel.getKey(row);
      if(key != null) {
        try {
          QueueMgrClient qclient = master.getQueueMgrClient();
          HardwareKey newKey = 
            new HardwareKey(key.getName(), key.getDescription(), plugin); 
          qclient.addHardwareKey(newKey);
          
          ArrayList<BaseKey> newKeys = new ArrayList<BaseKey>();
          for (HardwareKey k : qclient.getHardwareKeys())
            newKeys.add(k);
          pKeysTableModel.setKeys(newKeys);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);
        }
      }
    }
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
	    client.addHardwareKey(new HardwareKey(kname, desc));
	    modified = true;
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
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
	  client.removeHardwareKey(kname);
	  modified = true;
	}
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
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
      pGroupsNewDialog.setVisible(true);

      if(pGroupsNewDialog.wasConfirmed()) {
	String gname = pGroupsNewDialog.getName();
	if((gname != null) && (gname.length() > 0)) {
	  UIMaster master = UIMaster.getInstance();
	  QueueMgrClient client = master.getQueueMgrClient();
	  try {
	    client.addHardwareGroup(gname);
	    modified = true;
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
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
      HardwareGroup group = null;
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
	pGroupsNewDialog.setVisible(true);
	if(pGroupsNewDialog.wasConfirmed()) {
	  String gname = pGroupsNewDialog.getName();
	  if((gname != null) && (gname.length() > 0)) {
	    UIMaster master = UIMaster.getInstance();
	    QueueMgrClient client = master.getQueueMgrClient();
	    try {
	      client.addHardwareGroup(gname);
	      client.editHardwareGroup(new HardwareGroup(gname, group));
	      modified = true;
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	  client.removeHardwareGroups(gnames);
	  modified = true; 
	}
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }

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
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * Cache of valid selection group names. 
   */ 
  private TreeSet<String>  pGroupNames; 

  /*----------------------------------------------------------------------------------------*/

  /** 
   * The tabbed panel.
   */ 
  private JTabbedPanel  pTab;

  /**
   * The dialog update button.
   */ 
  private JButton  pUpdateButton; 
  
  /**
   * The dialog edit button.
   */
  private JButton  pEditButton; 


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
  private BaseKeysTableModel  pKeysTableModel;

  /**
   * The selection keys table panel.
   */ 
  private JTablePanel  pKeysTablePanel;


  /**
   * The new key creation dialog.
   */ 
  private JCreateHardwareKeyDialog  pKeysCreateDialog; 


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
  private HardwareGroupNamesTableModel  pGroupNamesTableModel;

  /**
   * The selection group names table panel.
   */ 
  private JTablePanel  pGroupNamesTablePanel;

  /**
   * The add/edit configuration dialog.
   */ 
  private JKeyChooserConfigDialog  pKeyDetailsDialog; 

  /**
   * The selection groups table model.
   */ 
  private HardwareGroupsTableModel  pGroupsTableModel;

  /**
   * The selection groups table panel.
   */ 
  private JTablePanel  pGroupsTablePanel;


  /**
   * The new selection group creation dialog.
   */ 
  private JNewHardwareGroupDialog pGroupsNewDialog;

}

