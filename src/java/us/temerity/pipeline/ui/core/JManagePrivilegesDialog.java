// $Id: JManagePrivilegesDialog.java,v 1.1 2006/01/15 06:29:25 jim Exp $

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
/*   M A N A G E   P R I V I L E G E S   D I A L O G                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for managing work group memberships and administrative privileges. 
 */ 
public 
class  JManagePrivilegesDialog
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
  JManagePrivilegesDialog() 
  {
    super("Manage Privileges", false);

    pPrivilegeDetails = new PrivilegeDetails();
    pWorkGroups = new WorkGroups();

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      	
      {
	pUsersPopup = new JPopupMenu();

	item = new JMenuItem("Add User");
	pUsersAddItem = item;
	item.setActionCommand("user-add");
	item.addActionListener(this);
	pUsersPopup.add(item);
	
	item = new JMenuItem("Remove User");
	pUsersRemoveItem = item;
	item.setActionCommand("user-remove");
	item.addActionListener(this);
	pUsersPopup.add(item);
      }

      {
	pGroupsPopup = new JPopupMenu();

	item = new JMenuItem("Add Group");
	pGroupsAddItem = item;
	item.setActionCommand("group-add");
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
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

      {
	JPanel panel = new JPanel();
	panel.setName("DialogHeader");	
	  
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	{
	  JLabel label = new JLabel("Manage User Privileges:");
	  label.setName("DialogHeaderLabel");	
	    
	  panel.add(label);	  
	}
	  
	panel.add(Box.createHorizontalGlue());

	{
	  JToggleButton btn = new JToggleButton();		
	  pPrivsButton = btn;
	  btn.setName("PrivsButton");
	  
	  Dimension size = new Dimension(30, 10);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("toggle-privs-columns");
	  btn.addActionListener(this);
	  
	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of the adminstrative privileges columns."));
	  
	  panel.add(btn);
	} 
	
	panel.add(Box.createRigidArea(new Dimension(15, 0)));
	
	{
	  JToggleButton btn = new JToggleButton();		
	  pGroupsButton = btn;
	  btn.setName("GroupsButton");

	  Dimension size = new Dimension(30, 10);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("toggle-groups-columns");
	  btn.addActionListener(this);
	    
	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of the work groups columns."));
	  
	  panel.add(btn);
	} 

	panel.add(Box.createRigidArea(new Dimension(30, 0)));

	body.add(panel);
      }
	
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	   
	{	
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  vbox.setAlignmentX(0.5f);
	    
	  {
	    PrivilegeNamesTableModel model = new PrivilegeNamesTableModel(this);
	    pPrivilegeNamesTableModel = model;
	  
	    JTablePanel tpanel =
	      new JTablePanel(model, 
			      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
			      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	    pPrivilegeNamesTablePanel = tpanel;
	      
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
	  PrivilegesTableModel model = new PrivilegesTableModel(this);
	  pPrivilegesTableModel = model;
	    
	  JTablePanel tpanel = new JTablePanel(model);
	  pPrivilegesTablePanel = tpanel;
	    
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

	/* sychronize the selected rows in the two tables */ 
	{
	  JTable ntable = pPrivilegeNamesTablePanel.getTable();
	  JTable gtable = pPrivilegesTablePanel.getTable();
	    
	  ntable.getSelectionModel().addListSelectionListener
	    (new TableSyncSelector(ntable, gtable));
	    
	  gtable.getSelectionModel().addListSelectionListener
	    (new TableSyncSelector(gtable, ntable));
	}
	  
	body.add(panel);
      }

      String extra[][] = {
	null,
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI(null, false, body, "Confirm", "Apply", extra, "Close");
      
      pUpdateButton = btns[1];
      pUpdateButton.setToolTipText(UIFactory.formatToolTip(
        "Update the privileges and work group memberships."));

      pConfirmButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes and close."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes."));

      updateAll();
      doToggleGroupsColumns();
      updateHostsHeaderButtons();
      pack();
    }

    pUsersNewDialog  = new JNewPipelineUserDialog(this);
    pGroupsNewDialog = new JNewWorkGroupDialog(this);
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
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      pPrivilegeDetails = client.getPrivilegeDetails();
      pWorkGroups = client.getWorkGroups();
      
      TreeMap<String,Privileges> privileges = client.getPrivileges();

      pPrivilegeNamesTableModel.setNames(pWorkGroups.getUsers());
      TreeMap<String,Boolean> modified = 
	pPrivilegesTableModel.setPrivileges(pWorkGroups, privileges, pPrivilegeDetails);
      pPrivilegesTablePanel.refilterColumns(modified); 
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }

    updateUsersMenu();
    updateGroupsMenu();

    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the sort of the user names table to match the privileges table.
   */ 
  public void
  sortNamesTable
  (
   int[] rowToIndex
  )
  {
    if(pPrivilegeNamesTableModel != null) 
      pPrivilegeNamesTableModel.externalSort(rowToIndex);
  }

  /** 
   * Update the sort of the privileges table to match the user names table.
   */ 
  public void
  sortPrivilegesTable
  (
   int[] rowToIndex
  )
  {
    if(pPrivilegesTableModel != null) 
      pPrivilegesTableModel.externalSort(rowToIndex);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the state of the column visiblity buttons.
   */ 
  public void 
  updateHostsHeaderButtons()
  {
    {
      TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("Master Admin");
      cnames.add("Developer");
      cnames.add("Queue Admin");
      cnames.add("Queue Manager");
      cnames.add("Node Manager");

      boolean selected = true;
      for(String cname : cnames) {
	if(!pPrivilegesTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }
      
      pPrivsButton.setSelected(selected);
    }

    {
      boolean selected = true;
      for(String cname : pWorkGroups.getGroups()) {
	if(!pPrivilegesTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }
      
      pGroupsButton.setSelected(selected);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the users menu. 
   */ 
  public void 
  updateUsersMenu() 
  {
    boolean selected = (pPrivilegeNamesTablePanel.getTable().getSelectedRowCount() > 0);
    boolean privileged = pPrivilegeDetails.isMasterAdmin();

    pUsersAddItem.setEnabled(privileged);
    pUsersRemoveItem.setEnabled(privileged && selected); 
  }

  /**
   * Update the work groups menu. 
   */ 
  public void 
  updateGroupsMenu() 
  {
    boolean privileged = pPrivilegeDetails.isMasterAdmin();
    pGroupsAddItem.setEnabled(privileged);
    pGroupsRemoveItem.setEnabled(privileged && (pGroupUnderMouse != null)); 
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
      (pUsersAddItem, prefs.getPrivilegeUsersAdd(),
       "Add a new user.");
    updateMenuToolTip
      (pUsersRemoveItem, prefs.getPrivilegeUsersRemove(),
       "Remove the selected users.");

    updateMenuToolTip
      (pGroupsAddItem, prefs.getWorkGroupsAdd(),
       "Add a new work group.");
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

  /**
   * Used to determine the name of the work group under the given mouse position (if any).
   */ 
  private void 
  setGroupUnderMouse
  (
   Point pos
  ) 
  {
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
    pGroupUnderMouse = null;

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
	  if((e.getComponent() == pPrivilegeNamesTablePanel.getTable()) ||
	     (e.getComponent() == pPrivilegeNamesTablePanel.getTableScroll())) {
	    updateUsersMenu();
	    pUsersPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	  else {
	    int filteredCol = pPrivilegesTablePanel.getTable().columnAtPoint(e.getPoint());
	    if(filteredCol != -1) {
	      int col = pPrivilegesTablePanel.getColumnIndex(filteredCol);
	      if(col >= 5) 
		pGroupUnderMouse = pPrivilegesTableModel.getColumnName(col);
	    }

	    updateGroupsMenu();
	    pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());	 
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
  mouseReleased
  (
   MouseEvent e
  ) 
  {
    pGroupUnderMouse = null;
  }



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
    if((e.getComponent() == pPrivilegeNamesTablePanel.getTable()) ||
       (e.getComponent() == pPrivilegeNamesTablePanel.getTableScroll())) {
      if((prefs.getPrivilegeUsersAdd() != null) &&
	 prefs.getPrivilegeUsersAdd().wasPressed(e))
	doUsersAdd();
      else if((prefs.getPrivilegeUsersRemove() != null) &&
	      prefs.getPrivilegeUsersRemove().wasPressed(e))
	doUsersRemove();
      else 
	unsupported = true;
    }
    else {
      if((prefs.getWorkGroupsAdd() != null) &&
	 prefs.getWorkGroupsAdd().wasPressed(e))
	doGroupsAdd();
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
    if(cmd.equals("toggle-privs-columns"))
      doTogglePrivsColumns();
    else if(cmd.equals("toggle-groups-columns"))
      doToggleGroupsColumns();    

    else if(cmd.equals("user-add")) 
      doUsersAdd();
    else if(cmd.equals("user-remove")) 
      doUsersRemove();

    else if(cmd.equals("group-add")) 
      doGroupsAdd();
    else if(cmd.equals("group-remove")) 
      doGroupsRemove();

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
    JViewport nview = pPrivilegeNamesTablePanel.getTableScroll().getViewport();
    JViewport gview = pPrivilegesTablePanel.getTableScroll().getViewport();
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
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      TreeMap<String,Privileges> privs = pPrivilegesTableModel.getModifiedPrivileges();
      if(!privs.isEmpty()) 
	client.editPrivileges(privs);

      DoubleMap<String,String,Boolean> members = 
	pPrivilegesTableModel.getModifiedWorkGroupMemberships(); 
      if(!members.isEmpty()) {
	for(String uname : members.keySet()) {
	  for(String gname : members.get(uname).keySet()) 
	  pWorkGroups.setMemberOrManager(uname, gname, members.get(uname, gname));
	}
	client.setWorkGroups(pWorkGroups);
      }
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
    
    updateAll();
  }

  /*
   * Update the table with the current license keys.
   */ 
  private void 
  doUpdate() 
  {
    pPrivilegesTablePanel.stopEditing();

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
   * Toggle display of the privileges columns.
   */ 
  public void 
  doTogglePrivsColumns()
  { 
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add("Master Admin");
    cnames.add("Developer");
    cnames.add("Queue Admin");
    cnames.add("Queue Manager");
    cnames.add("Node Manager");
    
    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pPrivilegesTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pPrivilegesTablePanel.setColumnsVisible(cnames, isVisible);
  }

  /**
   * Toggle display of the work group columns.
   */ 
  public void 
  doToggleGroupsColumns()
  { 
    TreeSet<String> cnames = new TreeSet<String>(pWorkGroups.getGroups());

    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pPrivilegesTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pPrivilegesTablePanel.setColumnsVisible(cnames, isVisible);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a new user.
   */ 
  private void 
  doUsersAdd()
  {
    pPrivilegesTablePanel.stopEditing();

    boolean modified = false;
    {
      pUsersNewDialog.setVisible(true);
      
      if(pUsersNewDialog.wasConfirmed()) {
	String uname = pUsersNewDialog.getName();
	if((uname != null) && (uname.length() > 0)) {
	  
	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.getMasterMgrClient();
	  try {
	    pWorkGroups.addUser(uname);
	    client.setWorkGroups(pWorkGroups);
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
  doUsersRemove() 
  {
    pPrivilegeNamesTablePanel.cancelEditing();

    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	int rows[] = pPrivilegeNamesTablePanel.getTable().getSelectedRows();
	int wk;
	for(wk=0; wk<rows.length; wk++) {
	  String uname = pPrivilegeNamesTableModel.getName(rows[wk]);
	  pWorkGroups.removeUser(uname);
	  client.setWorkGroups(pWorkGroups);
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
   * Add a work group to the table.
   */ 
  private void 
  doGroupsAdd()
  {
    pPrivilegeNamesTablePanel.stopEditing();

    boolean modified = false;
    {
      pGroupsNewDialog.setVisible(true);

      if(pGroupsNewDialog.wasConfirmed()) {
	String gname = pGroupsNewDialog.getName();
	if((gname != null) && (gname.length() > 0)) {
	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.getMasterMgrClient();
	  try {
	    pWorkGroups.addGroup(gname);
	    client.setWorkGroups(pWorkGroups);
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
   * Remove the  rows from the selection groups table.
   */ 
  private void 
  doGroupsRemove() 
  {
    pPrivilegeNamesTablePanel.cancelEditing();

    boolean modified = false;
    if(pGroupUnderMouse != null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	pWorkGroups.removeGroup(pGroupUnderMouse);
	client.setWorkGroups(pWorkGroups);
	modified = true;
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }

      pGroupUnderMouse = null;
    }

    if(modified) 
      updateAll();
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

  private static final long serialVersionUID = -929351750945847185L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * 
   */ 
  private WorkGroups  pWorkGroups; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Column display buttons.
   */ 
  private JToggleButton  pPrivsButton;
  private JToggleButton  pGroupsButton;

  /**
   * The dialog update button.
   */ 
  private JButton  pUpdateButton; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The users popup menu.
   */ 
  private JPopupMenu  pUsersPopup;
  
  /**
   * The users popup menu items.
   */ 
  private JMenuItem  pUsersAddItem;
  private JMenuItem  pUsersRemoveItem;


  /**
   * The user names table model.
   */ 
  private PrivilegeNamesTableModel  pPrivilegeNamesTableModel;

  /**
   * The user names table panel.
   */ 
  private JTablePanel  pPrivilegeNamesTablePanel;


  /**
   * The user creation dialog.
   */ 
  private JNewPipelineUserDialog  pUsersNewDialog; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups popup menu.
   */ 
  private JPopupMenu  pGroupsPopup;
  
  /**
   * The work groups popup menu items.
   */ 
  private JMenuItem  pGroupsAddItem;
  private JMenuItem  pGroupsRemoveItem;

  /**
   * The name of the work group under the mouse when the groups menu is active.
   */ 
  private String  pGroupUnderMouse; 


  /**
   * The privileges table model.
   */ 
  private PrivilegesTableModel  pPrivilegesTableModel;

  /**
   * The privileges table panel.
   */ 
  private JTablePanel  pPrivilegesTablePanel;


  /**
   * The work groups creation dialog.
   */ 
  private JNewWorkGroupDialog  pGroupsNewDialog; 

}

