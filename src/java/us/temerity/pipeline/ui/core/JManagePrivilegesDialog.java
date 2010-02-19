// $Id: JManagePrivilegesDialog.java,v 1.12 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   P R I V I L E G E S   D I A L O G                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for managing work group memberships and administrative privileges. 
 */ 
public 
class  JManagePrivilegesDialog
  extends JTopLevelDialog
  implements MouseListener, KeyListener, ActionListener
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
    super("Manage Privileges and Work Groups");

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
      setLayout(new BorderLayout());

      JTabbedPanel tab = new JTabbedPanel();
      pTab = tab;

      /* privileges panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  panel.setName("TabbedDialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Privileges:");
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
	    PrivilegesTableModel model = new PrivilegesTableModel(this);
            pPrivilegesTableModel = model;
	    
            JTablePanel tpanel = new JTablePanel(model);
            pPrivilegesTablePanel = tpanel;
	    
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

	tab.addTab("Privileges", body);
      }

      /* work groups panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  panel.setName("TabbedDialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Work Groups:");
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
 	    WorkGroupsTableModel model = new WorkGroupsTableModel(this);
            pWorkGroupsTableModel = model;
	    
            JTablePanel tpanel = new JTablePanel(model);
            pWorkGroupsTablePanel = tpanel;
	    
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

	tab.addTab("Work Groups", body);
      }

      String extra[][] = {
	null,
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI(null, tab, "Confirm", "Apply", extra, "Close", null);
      
      pUpdateButton = btns[1];
      pUpdateButton.setToolTipText(UIFactory.formatToolTip(
        "Update the privileges and work group memberships."));

      pConfirmButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes and close."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes."));

      updateAll();
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
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      TreeMap<String,Privileges> privileges = client.getPrivileges();
      pPrivilegeDetails = client.getPrivilegeDetails();
      pPrivilegesTableModel.setPrivileges(pWorkGroups, privileges, pPrivilegeDetails);

      pWorkGroups = client.getWorkGroups();
      pWorkGroupsTableModel.setWorkGroups(pWorkGroups, pPrivilegeDetails);      
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(client);
    }

    updateUsersMenu();
    updateGroupsMenu();

    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the users menu. 
   */ 
  public void 
  updateUsersMenu() 
  {
    boolean selected = false;
    switch(pTab.getSelectedIndex()) { 
    case 0:
      selected = (pPrivilegesTablePanel.getTable().getSelectedRowCount() > 0);   
      break;
  
    case 1:
      selected = (pWorkGroupsTablePanel.getTable().getSelectedRowCount() > 0);
    }
      
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
    boolean showTooltips = prefs.getShowMenuToolTips();

    updateMenuToolTip
      (showTooltips, pUsersAddItem, prefs.getPrivilegeUsersAdd(),
       "Add a new user.");
    updateMenuToolTip
      (showTooltips, pUsersRemoveItem, prefs.getPrivilegeUsersRemove(),
       "Remove the selected users.");
    updateMenuToolTip
      (showTooltips, pGroupsAddItem, prefs.getWorkGroupsAdd(),
       "Add a new work group.");
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
        pGroupUnderMouse = null;
        
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	/* BUTTON3: popup menus */ 
	if((mods & (on1 | off1)) == on1) {
          pGroupUnderMouse = null;
	  switch(pTab.getSelectedIndex()) {      
	  case 0:
            {
              int col = pPrivilegesTablePanel.getTable().columnAtPoint(e.getPoint());
              switch(col) {
              case 0:
                updateUsersMenu();
                pUsersPopup.show(e.getComponent(), e.getX(), e.getY());
              }
            }
            break;

          case 1:
            {
              int col = pWorkGroupsTablePanel.getTable().columnAtPoint(e.getPoint());
              switch(col) {
              case 0:
                updateUsersMenu();
                pUsersPopup.show(e.getComponent(), e.getX(), e.getY());
                break;

              default:
                if(col != -1) 
                  pGroupUnderMouse = pWorkGroupsTableModel.getColumnName(col);
                updateGroupsMenu();
                pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());
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
  //   boolean unsupported = false;
//     UserPrefs prefs = UserPrefs.getInstance();
//     if((e.getComponent() == pPrivilegeNamesTablePanel.getTable()) ||
//        (e.getComponent() == pPrivilegeNamesTablePanel.getTableScroll())) {
//       if((prefs.getPrivilegeUsersAdd() != null) &&
// 	 prefs.getPrivilegeUsersAdd().wasPressed(e))
// 	doUsersAdd();
//       else if((prefs.getPrivilegeUsersRemove() != null) &&
// 	      prefs.getPrivilegeUsersRemove().wasPressed(e))
// 	doUsersRemove();
//       else 
// 	unsupported = true;
//     }
//     else {
//       if((prefs.getWorkGroupsAdd() != null) &&
// 	 prefs.getWorkGroupsAdd().wasPressed(e))
// 	doGroupsAdd();
//       else 
// 	unsupported = true;      
//     }
    
//     if(unsupported) {
//       switch(e.getKeyCode()) {
//       case KeyEvent.VK_SHIFT:
//       case KeyEvent.VK_ALT:
//       case KeyEvent.VK_CONTROL:
// 	break;
      
//       default:
// 	if(UIFactory.getBeepPreference())
// 	  Toolkit.getDefaultToolkit().beep();
//       }
//     }
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
    if(cmd.equals("user-add")) 
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
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      TreeMap<String,Privileges> privs = pPrivilegesTableModel.getModifiedPrivileges();
      if(!privs.isEmpty()) {
	client.editPrivileges(privs);
	master.invalidateAllCachedPrivilegeDetails();
      }

      DoubleMap<String,String,Boolean> members = 
	pWorkGroupsTableModel.getModifiedWorkGroupMemberships(); 
      if(!members.isEmpty()) {
	for(String uname : members.keySet()) {
	  for(String gname : members.get(uname).keySet()) 
	  pWorkGroups.setMemberOrManager(uname, gname, members.get(uname, gname));
	}
	client.setWorkGroups(pWorkGroups);
	master.invalidateAllCachedWorkGroups();
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(client);
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
   * Add a new user.
   */ 
  private void 
  doUsersAdd()
  { 
    pPrivilegesTablePanel.stopEditing();
    pWorkGroupsTablePanel.stopEditing();

    boolean modified = false;
    {
      pUsersNewDialog.setVisible(true);
      
      if(pUsersNewDialog.wasConfirmed()) {
 	String uname = pUsersNewDialog.getName();
 	if((uname != null) && (uname.length() > 0)) {
          
 	  UIMaster master = UIMaster.getInstance();
 	  MasterMgrClient client = master.acquireMasterMgrClient();
 	  try {
 	    pWorkGroups.addUser(uname);
 	    client.setWorkGroups(pWorkGroups);
 	    master.invalidateAllCachedWorkGroups();
 	    modified = true;
 	  }
 	  catch(PipelineException ex) {
 	    showErrorDialog(ex);
 	  }
 	  finally {
 	    master.releaseMasterMgrClient(client);
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
    pPrivilegesTablePanel.cancelEditing();
    pWorkGroupsTablePanel.cancelEditing();

    ArrayList<String> selected = new ArrayList<String>();
    switch(pTab.getSelectedIndex()) {      
    case 0:
      for(int row : pPrivilegesTablePanel.getTable().getSelectedRows())
        selected.add(pPrivilegesTableModel.getUserName(row));
      break;

    case 1:
      for(int row : pWorkGroupsTablePanel.getTable().getSelectedRows())
        selected.add(pWorkGroupsTableModel.getUserName(row));
    }
      
    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        for(String uname : selected) {
 	  pWorkGroups.removeUser(uname);
 	  client.setWorkGroups(pWorkGroups);
 	  master.invalidateAllCachedWorkGroups();
 	  modified = true;
 	}
      }
      catch(PipelineException ex) {
 	showErrorDialog(ex);
      }
      finally {
        master.releaseMasterMgrClient(client);
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
    pPrivilegesTablePanel.stopEditing();
    pWorkGroupsTablePanel.stopEditing();
    
    boolean modified = false;
    {
      pGroupsNewDialog.setVisible(true);
      
      if(pGroupsNewDialog.wasConfirmed()) {
 	String gname = pGroupsNewDialog.getName();
 	if((gname != null) && (gname.length() > 0)) {
 	  UIMaster master = UIMaster.getInstance();
 	  MasterMgrClient client = master.acquireMasterMgrClient();
 	  try {
 	    pWorkGroups.addGroup(gname);
 	    client.setWorkGroups(pWorkGroups);
 	    master.invalidateAllCachedWorkGroups();
 	    modified = true;
 	  }
 	  catch(PipelineException ex) {
 	    showErrorDialog(ex);
 	  }
 	  finally {
 	    master.releaseMasterMgrClient(client);
 	  }
 	}
      }
    }
    
    if(modified) 
      updateAll();
  }
  
  /**
   * Remove rows from the selection groups table.
   */ 
  private void 
  doGroupsRemove() 
  {
    pPrivilegesTablePanel.cancelEditing();
    pWorkGroupsTablePanel.cancelEditing();

    boolean modified = false;
    if(pGroupUnderMouse != null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pWorkGroups.removeGroup(pGroupUnderMouse);
        client.setWorkGroups(pWorkGroups);
        master.invalidateAllCachedWorkGroups();
        modified = true;
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
      
      pGroupUnderMouse = null;
    }
    
    if(modified) 
      updateAll();
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
   * The tabbed panel.
   */ 
  private JTabbedPanel  pTab;

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
   * The user creation dialog.
   */ 
  private JNewPipelineUserDialog  pUsersNewDialog; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The privileges table model.
   */ 
  private PrivilegesTableModel  pPrivilegesTableModel;

  /**
   * The privileges table panel.
   */ 
  private JTablePanel  pPrivilegesTablePanel;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups table model.
   */ 
  private WorkGroupsTableModel  pWorkGroupsTableModel;

  /**
   * The work groups table panel.
   */ 
  private JTablePanel  pWorkGroupsTablePanel;

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
  private String pGroupUnderMouse;

  /**
   * The work groups creation dialog.
   */ 
  private JNewWorkGroupDialog  pGroupsNewDialog; 


}

