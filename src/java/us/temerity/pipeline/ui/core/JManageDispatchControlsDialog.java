// $Id: JManageDispatchControlsDialog.java,v 1.5 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import com.sun.org.apache.bcel.internal.generic.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   D I S P A T C H   C O N T R O L S   D I A L O G                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for adding, removing and modifying dispatch criteria.
 */ 
public 
class JManageDispatchControlsDialog
  extends JTopLevelDialog
  implements MouseListener, KeyListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageDispatchControlsDialog() 
  {
    super("Manage Dispatch Controls");
    
    /* initialize fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();
    }
    
    /* initialize the popup menus */ 
    {
      JMenuItem item;
        
      {
        pControlsPopup = new JPopupMenu();

        item = new JMenuItem("Add Control");
        item.setActionCommand("control-add");
        item.addActionListener(this);
        pControlsAddItem = item;
        pControlsPopup.add(item);
        
        item = new JMenuItem("Clone Control");
        item.setActionCommand("control-clone");
        item.addActionListener(this);
        pControlsCloneItem = item;
        pControlsPopup.add(item);
        
        item = new JMenuItem("Remove Control");
        item.setActionCommand("control-remove");
        item.addActionListener(this);
        pControlsRemoveItem = item;
        pControlsPopup.add(item);
      }
      
      {
        pCriteriaPopup = new JPopupMenu();
        
        item = new JMenuItem("Move First");
        item.setActionCommand("to-top");
        item.addActionListener(this);
        pCriteriaTopItem = item;
        pCriteriaPopup.add(item);
        
        item = new JMenuItem("Move Sooner");
        item.setActionCommand("move-up");
        item.addActionListener(this);
        pCriteriaUpItem = item;
        pCriteriaPopup.add(item);
        
        pCriteriaPopup.addSeparator();
        
        item = new JMenuItem("Move Later");
        item.setActionCommand("move-down");
        item.addActionListener(this);
        pCriteriaDownItem = item;
        pCriteriaPopup.add(item);
        
        item = new JMenuItem("Move Last");
        item.setActionCommand("to-bottom");
        item.addActionListener(this);
        pCriteriaBottomItem = item;
        pCriteriaPopup.add(item);
      }
    }
    
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

      {
        JPanel panel = new JPanel();
        panel.setName("DialogHeader");        

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        {
          JLabel label = new JLabel("Manage Dispatch Controls:");
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
          DispatchControlTableModel model = new DispatchControlTableModel(this);
          pControlsTableModel = model;

          JTablePanel tpanel = new JTablePanel(model);
          pControlsTablePanel = tpanel;

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
      
      String extra[][] = {
        null,
        { "Update", "update"},
      };

      JButton btns[] = super.initUI(null, body, "Confirm", "Apply", extra, "Close", null);
      
      pUpdateButton = btns[1];
      pUpdateButton.setToolTipText(UIFactory.formatToolTip(
        "Update the selection keys, groups and schedules."));
      
      pConfirmButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes and close."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip(
       "Apply the changes."));

      updateAll();
      pack();
    }
    
    pControlsNewDialog   = new JNewDispatchControlDialog(this);
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
    QueueMgrClient qclient = master.acquireQueueMgrClient();
    MasterMgrClient mclient = master.acquireMasterMgrClient();
    try {
      pPrivilegeDetails = mclient.getPrivilegeDetails();
      
      TreeMap<String, DispatchControl> controls = new TreeMap<String, DispatchControl>();
      
      for (Entry<String, DispatchControl> entry : qclient.getDispatchControls().entrySet())
        controls.put(entry.getKey(), entry.getValue());
      
      pControlsTableModel.setDispatchControls(controls, pPrivilegeDetails);
      
      pConfirmButton.setEnabled(false);
      pApplyButton.setEnabled(false);
      
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(mclient);
      master.releaseQueueMgrClient(qclient);
    }
    
    updateControlMenu();
    updateCriteriaMenu();
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection keys menu.
   */ 
  public void 
  updateControlMenu() 
  {
    boolean selected = (pControlsTablePanel.getTable().getSelectedRowCount() > 0);
    pControlsAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pControlsCloneItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected);
    pControlsRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
  }
  
  /**
   * Update the selection keys menu.
   */ 
  public void 
  updateCriteriaMenu() 
  {
    pCriteriaTopItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pCriteriaBottomItem.setEnabled(pPrivilegeDetails.isQueueAdmin());
    pCriteriaUpItem.setEnabled(pPrivilegeDetails.isQueueAdmin());
    pCriteriaDownItem.setEnabled(pPrivilegeDetails.isQueueAdmin());
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
      (showTooltips, pControlsAddItem, prefs.getDispatchControlAdd(),
       "Add a new dispatch control.");
    updateMenuToolTip
      (showTooltips, pControlsRemoveItem, prefs.getDispatchControlsRemove(),
       "Remove the selected dispatch controls.");
    updateMenuToolTip
      (showTooltips, pControlsCloneItem, prefs.getDispatchControlClone(),
       "Add a new dispatch control which is a copy of the selected control.");
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
        int on1  = (InputEvent.BUTTON3_DOWN_MASK);
  
        int off1 = (InputEvent.BUTTON1_DOWN_MASK | 
                    InputEvent.BUTTON2_DOWN_MASK | 
                    InputEvent.SHIFT_DOWN_MASK |
                    InputEvent.ALT_DOWN_MASK |
                    InputEvent.CTRL_DOWN_MASK);
  
        /* BUTTON3: popup menus */ 
        if((mods & (on1 | off1)) == on1) {
          int col = pControlsTablePanel.getTable().columnAtPoint(e.getPoint());
          if (col == 0) {
            updateControlMenu();
            pControlsPopup.show(e.getComponent(), e.getX(), e.getY());
          }
          else if (col > 0) {
            updateCriteriaMenu();
            pCriteriaPopup.show(e.getComponent(), e.getX(), e.getY());
            Point p = e.getPoint();
            JTable t = pControlsTablePanel.getTable();
            pTableColumn = t.columnAtPoint(p);
            pTableRow = t.rowAtPoint(p);
          }
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
    
    if(cmd.equals("control-add"))
      doControlAdd();
    else if(cmd.equals("control-remove")) 
      doControlsRemove();
    else if (cmd.equals("control-clone"))
      doControlClone();

    else if(cmd.equals("move-up")) 
      doMoveUp();
    else if(cmd.equals("move-down")) 
      doMoveDown();
    
    else if(cmd.equals("to-top")) 
      doMakeTop();
    else if(cmd.equals("to-bottom")) 
      doMakeBottom();
    
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
    QueueMgrClient client = master.acquireQueueMgrClient();
    try {
      ArrayList<DispatchControl> controls = pControlsTableModel.getModifiedControls();
      if (controls != null) 
        client.editDispatchControls(controls);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseQueueMgrClient(client);
    }

    updateAll();
  }
  
  /*
   * Update the table with the current dispatch control.
   */ 
  private void 
  doUpdate() 
  {
    pControlsTablePanel.stopEditing();

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
   * Add a dispatch control to the table.
   */ 
  private void 
  doControlAdd()
  {
    pControlsTablePanel.stopEditing();

    boolean modified = false;
    {
      pControlsNewDialog.setVisible(true);
      
      if (pControlsNewDialog.wasConfirmed()) {
        String kname = pControlsNewDialog.getName();
        if((kname != null) && (kname.length() > 0)) {
          
          UIMaster master = UIMaster.getInstance();
          QueueMgrClient client = master.acquireQueueMgrClient();
          try {
            client.addDispatchControl(kname);
            modified = true;
          }
          catch(PipelineException ex) {
            showErrorDialog(ex);
          }
          finally {
            master.releaseQueueMgrClient(client);
          }
        }
      }
    }

    if(modified) 
      updateAll();    
  }

  /**
   * Clone an existing dispatch control.
   */ 
  private void 
  doControlClone()
  {
    pControlsTablePanel.stopEditing();

    boolean modified = false;
    DispatchControl control = null;
    {
      int rows[] = pControlsTablePanel.getTable().getSelectedRows();
      int wk;
      for(wk=0; wk<rows.length; wk++) {
        control = pControlsTableModel.getControl(rows[wk]);
        if(control != null) 
          break;
      }
    }
    
    if(control != null) {
      pControlsNewDialog.setVisible(true);
      if(pControlsNewDialog.wasConfirmed()) {
        String cname = pControlsNewDialog.getName();
        if((cname != null) && (cname.length() > 0)) {
          UIMaster master = UIMaster.getInstance();
          QueueMgrClient client = master.acquireQueueMgrClient();
          try {
            client.addDispatchControl(cname);
            client.editDispatchControl(new DispatchControl
              (cname, new LinkedHashSet<DispatchCriteria>(control.getCriteria())));
            modified = true;
          }
          catch(PipelineException ex) {
            showErrorDialog(ex);
          }
          finally {
            master.releaseQueueMgrClient(client);
          }
        }
      }
    }

    if(modified) 
      updateAll();    
  }
  
  /**
   * Remove the selected rows from the dispatch control table.
   */ 
  private void 
  doControlsRemove() 
  {
    pControlsTablePanel.cancelEditing();

    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.acquireQueueMgrClient();
      try {
        TreeSet<String> cnames = new TreeSet<String>();
        {
          int rows[] = pControlsTablePanel.getTable().getSelectedRows();
          int wk;
          for(wk=0; wk<rows.length; wk++) 
            cnames.add(pControlsTableModel.getControlName(rows[wk]));
        }
        
        if(!cnames.isEmpty()) {
          client.removeDispatchControls(cnames);
          modified = true; 
        }
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.releaseQueueMgrClient(client);
      }
    }

    if(modified) {
      updateAll();
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Move the clicked on criteria up one level in the control.
   */
  public void
  doMoveUp()
  {
    pControlsTableModel.moveUp(pTableColumn, pTableRow);
  }
  
  /**
   * Move the clicked on criteria down one level in the control.
   */
  public void
  doMoveDown()
  {
    pControlsTableModel.moveDown(pTableColumn, pTableRow);
  }
  
  /**
   * Move the clicked on criteria to the top in the control.
   */
  public void
  doMakeTop()
  {
    pControlsTableModel.makeTop(pTableColumn, pTableRow);
  }
  
  /**
   * Move the clicked on criteria to the bottom in the control.
   */
  public void
  doMakeBottom()
  {
    pControlsTableModel.makeBottom(pTableColumn, pTableRow);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -536891732648537025L;
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * Cache of valid dispatch control names. 
   */ 
  private TreeSet<String>  pControlNames;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The dialog update button.
   */ 
  private JButton  pUpdateButton; 

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The dispatch controls popup menu.
   */ 
  private JPopupMenu  pControlsPopup;

  /**
   * The dispatch controls popup menu items.
   */ 
  private JMenuItem  pControlsAddItem;
  private JMenuItem  pControlsRemoveItem;
  private JMenuItem  pControlsCloneItem;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The dispatch criteria popup menu.
   */ 
  private JPopupMenu  pCriteriaPopup;
  
  /**
   * The dispatch criteria popup menu items.
   */ 
  private JMenuItem  pCriteriaTopItem;
  private JMenuItem  pCriteriaUpItem;
  private JMenuItem  pCriteriaDownItem;
  private JMenuItem  pCriteriaBottomItem;
  
  /*----------------------------------------------------------------------------------------*/
  /**
   * The dispatch controls table model.
   */ 
  private DispatchControlTableModel  pControlsTableModel;

  /**
   * The dispatch controls table panel.
   */ 
  private JTablePanel  pControlsTablePanel;

  /**
   * The new dispatch control creation dialog.
   */ 
  private JNewDispatchControlDialog pControlsNewDialog;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The column that was clicked on to launch the criteria menu. 
   */
  private int pTableColumn;
  
  /**
   * The row that was clicked on to launch the criteria menu.
   */
  private int pTableRow;

}
