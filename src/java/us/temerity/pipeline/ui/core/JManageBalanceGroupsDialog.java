// $Id: JManageBalanceGroupsDialog.java,v 1.3 2009/12/11 23:27:01 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.core.BalanceGroupTableModel.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   B A L A N C E   G R O U P S   D I A L O G                                */
/*------------------------------------------------------------------------------------------*/

public 
class JManageBalanceGroupsDialog
  extends JTopLevelDialog 
  implements ActionListener, ListSelectionListener, MouseListener, FocusListener 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageBalanceGroupsDialog() 
  {
    super("Manage Balance Groups");

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    
    /* initialize fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();
      pWorkGroups = new WorkGroups();
      
      pGroupsNewDialog = new JNewIdentifierDialog
        (this, "New Balance Group", "New Group Name:", null, "Add");
      
      pListDialog = 
        new JBooleanListDialog(this, "Balance Group Add", "Add To Balance Group:");
      
      pBalanceGroups = new TreeMap<String, UserBalanceGroup>();
      
      pCurrentBalanceGroupSize = new IntegerOpMap<String>();
      
      pCurrentUsage = new DoubleMap<String, String, Double>();
      
      pEditedGroups = new TreeSet<String>();
    }
    
    /* initialize the popup menus */ 
    {
      JMenuItem item;
        
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
    }
    
    {
      JMenuItem item;
      
      {
        pTablePopup = new JPopupMenu();
        
        item = new JMenuItem("Add");
        pTableAddItem = item;
        item.setActionCommand("table-add");
        item.addActionListener(this);
        pTablePopup.add(item);
        
        item = new JMenuItem("Remove");
        pTableRemoveItem = item;
        item.setActionCommand("table-remove");
        item.addActionListener(this);
        pTablePopup.add(item);
      }
    }
    
    JPanel parent = new JPanel();
    parent.setLayout(new BoxLayout(parent, BoxLayout.PAGE_AXIS));
    
    {
      Box upper = Box.createHorizontalBox();
      {
        JPanel left = new JPanel();
        left.setName("MainPanel");
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        
        {
          JList lst = new JList(new DefaultListModel());

          lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          lst.setCellRenderer(new JListCellRenderer());
          
          lst.addListSelectionListener(this);
          lst.addMouseListener(this);
          
          JScrollPane scroll = 
            UIFactory.createScrollPane
            (lst, 
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
             ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
             new Dimension(150, 150), new Dimension(sLWidth, sLHeight + 12), null);
          
          left.add(scroll);
          
          pGroupList = lst;
        }
        
        upper.add(left);
      }
      upper.add(UIFactory.createSidebar());
      {
        JPanel right= new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
        right.add(Box.createRigidArea(new Dimension(0, 5)));
        {
          Box defaults = Box.createHorizontalBox();
          defaults.add(Box.createRigidArea(new Dimension(199, 0)));
          {
            Box body = new Box(BoxLayout.X_AXIS);

            String toolTip = 
              "The bias to be applied to all users without an explicitly specified " +
              "bias."; 
            
            Component header = createHeader("Default Bias:", toolTip, 94);
            body.add(header);
            body.add(Box.createHorizontalStrut(8));
            
            pDefaultBiasField = UIFactory.createIntegerField(0, sVSize, JTextField.CENTER);
            pDefaultBiasField.setToolTipText(toolTip);
            pDefaultBiasField.addFocusListener(this);
            pDefaultBiasField.setActionCommand("default-share-field");
            pDefaultBiasField.addActionListener(this);
            
            pDefaultBiasField.setMaximumSize(pDefaultBiasField.getPreferredSize());
            
            body.add(pDefaultBiasField);
            
            Box tempBox = Box.createVerticalBox();
            tempBox.add(body);
            defaults.add(tempBox);
          }
          defaults.add(Box.createRigidArea(new Dimension(67, 0)));
          {
            Box body = new Box(BoxLayout.X_AXIS);
            
            String toolTip = 
              "The default max share usage that will be applied to all users without " +
              "an explicitly specified max share usage."; 

            Component header = createHeader("Default Max Share:", toolTip, 140);
            body.add(header);
            body.add(Box.createHorizontalStrut(8));
            
            pDefaultMaxField = UIFactory.createPercentField(0d, 2, sVSize, JTextField.CENTER);
            pDefaultMaxField.setMaximumSize(pDefaultMaxField.getPreferredSize());
            pDefaultMaxField.addFocusListener(this);
            pDefaultMaxField.setActionCommand("default-max-field");
            pDefaultMaxField.addActionListener(this);
            
            body.add(pDefaultMaxField);
            
            Box tempBox = Box.createVerticalBox();
            tempBox.add(body);
            defaults.add(tempBox);
          }
          defaults.add(Box.createHorizontalStrut(6));
          defaults.add(Box.createHorizontalGlue());
          defaults.add(Box.createRigidArea(new Dimension(5,0)));
          
          right.add(defaults);
        }
        
        right.add(Box.createVerticalStrut(6));
        right.add(UIFactory.createPanelBreak());
        right.add(Box.createVerticalStrut(6));
        
        {
          Box main = Box.createHorizontalBox();
          main.add(Box.createRigidArea(new Dimension(5,0)));
          {
            Box userBox = Box.createVerticalBox();
            {
              JPanel headerBox = createHeader("Users:", null);
              headerBox.add(Box.createHorizontalGlue());
              userBox.add(headerBox);
            }
            userBox.add(Box.createVerticalStrut(6));
            pUserTableModel = new BalanceGroupTableModel(TableType.USER, this);
            pUserTablePanel = new JTablePanel(pUserTableModel);
            
            {
              JScrollPane scroll = pUserTablePanel.getTableScroll();
              scroll.addMouseListener(this); 
              scroll.setFocusable(true);
              pUserScroll = scroll;
            }
            
            {
              JTable table = pUserTablePanel.getTable();
              table.addMouseListener(this); 
              table.setFocusable(true);
              pUserTable = pUserTablePanel.getTable();
            }
            
            userBox.add(pUserTablePanel);
            
            Dimension dim = userBox.getPreferredSize();
            userBox.setPreferredSize(new Dimension(dim.width, sLHeight));
           
            main.add(userBox);
          }
          
          main.add(Box.createHorizontalStrut(4));
          
          {
            Box groupBox = Box.createVerticalBox();
            {
              JPanel headerBox = createHeader("Groups:", null);
              headerBox.add(Box.createHorizontalGlue());
              groupBox.add(headerBox);
            }
            groupBox.add(Box.createVerticalStrut(6));
            pGroupTableModel = new BalanceGroupTableModel(TableType.GROUP, this);
            pGroupTablePanel = new JTablePanel(pGroupTableModel);
            
            {
              JScrollPane scroll = pGroupTablePanel.getTableScroll();
              scroll.addMouseListener(this); 
              scroll.setFocusable(true);
              pGroupScroll = scroll;
            }
            
            {
              JTable table = pGroupTablePanel.getTable();
              table.addMouseListener(this); 
              table.setFocusable(true);
//              table.addKeyListener(this);
              pGroupTable = pGroupTablePanel.getTable();
            }
            
            groupBox.add(pGroupTablePanel);
           
            main.add(groupBox);
          }
          
          main.add(Box.createHorizontalStrut(4));
          
          {
            Box calcBox = Box.createVerticalBox();
            {
              JPanel headerBox = createHeader("Calculated Shares:", null);
              headerBox.add(Box.createHorizontalGlue());
              calcBox.add(headerBox);
            }
            calcBox.add(Box.createVerticalStrut(6));
            pCalcTableModel = new BalanceGroupCalcTableModel(this);
            JTablePanel table = new JTablePanel(pCalcTableModel);
            calcBox.add(table);
            
            main.add(calcBox);
          }
          main.add(Box.createRigidArea(new Dimension(5,0)));
          right.add(main);
        }
        right.add(Box.createRigidArea(new Dimension(0,5)));
        upper.add(right);
      }
      parent.add(upper);
    }
    parent.add(UIFactory.createPanelBreak());
    {
      JBalanceGroupHistPanel bottom = new JBalanceGroupHistPanel();
      
      Dimension size = new Dimension(600, 200);
      bottom.setMinimumSize(size);
      bottom.setPreferredSize(size);
      bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
      
      parent.add(bottom);
      
      pHistogramPanel = bottom;
    }
    
    
    String extra[][] = {
      null,
      { "Update", "update"},
    };

    JButton btns[] = super.initUI("Manage Balance Groups:", parent, "Confirm", "Apply", 
                                  extra, "Close");
    
    pUpdateButton = btns[1];

    pUpdateButton.setToolTipText(UIFactory.formatToolTip("Update the balance groups."));
    pConfirmButton.setToolTipText(UIFactory.formatToolTip("Apply the changes and close."));
    pApplyButton.setToolTipText(UIFactory.formatToolTip("Apply the changes."));
  }

  /*----------------------------------------------------------------------------------------*/

  private JPanel
  createHeader
  (
    String header,
    Integer width
  )
  {
    return createHeader(header, null, width);
  }

  private JPanel 
  createHeader
  (
    String header,
    String tooltip,
    Integer width
  )
  {
    JPanel headerBox = new JPanel();
    headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.LINE_AXIS));
    JLabel label = new JLabel(header);
    label.setName("PanelLabel");
    label.setToolTipText(tooltip);
    label.setVerticalAlignment(SwingConstants.BOTTOM);
    headerBox.add(label);
    if (width != null ) {
      Dimension dim = label.getPreferredSize();
      Dimension newDim = new Dimension(width, dim.height);
      label.setMinimumSize(newDim);
      label.setMaximumSize(newDim);
      label.setPreferredSize(newDim);
    }
    
    return headerBox;
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
    
    String selected = (String) pGroupList.getSelectedValue();
    try {
      pPrivilegeDetails = mclient.getPrivilegeDetails();
      pWorkGroups = mclient.getWorkGroups();

      pBalanceGroups = qclient.getBalanceGroups();
      
      pCurrentUsage = qclient.getBalanceGroupUsage();
      
      pCurrentBalanceGroupSize.clear();
      TreeMap<String, QueueHostInfo> hosts = qclient.getHosts();
      for (QueueHostInfo info : hosts.values()) {
        if (info.getStatus() == QueueHostStatus.Enabled) {
          String bgName = info.getBalanceGroup();
          int slots = info.getJobSlots();
          pCurrentBalanceGroupSize.apply(bgName, slots);
        }
      }
      selected = updateList(selected);  
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(mclient);
      master.releaseQueueMgrClient(qclient);
    }

    updateBalanceGroup(selected);
    
    updateGroupsMenu();

    pEditedGroups.clear();
    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
  }


  /**
   * Update the balance group menu.
   */ 
  public void 
  updateGroupsMenu() 
  {
    boolean selected = (pGroupList.getSelectedValue() != null);
    pGroupsAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pGroupsCloneItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
    pGroupsRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
  }
  
  public void
  updateTablesMenu()
  {
    boolean selected = false;
    if (pTableType == TableType.USER)
      selected = (pUserTable.getSelectedColumnCount() != 0 );
    else 
      selected = (pGroupTable.getSelectedColumnCount() != 0 );
    
    pTableAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pTableRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected);
  }
  
  public void
  updateBalanceGroup
  (
    String groupName  
  )
  {
    if (groupName != null) {
      
      UserBalanceGroup bgroup = pBalanceGroups.get(groupName);
      
      if (bgroup == null) {
        UIMaster master = UIMaster.getInstance();
        QueueMgrClient qclient = master.acquireQueueMgrClient();

        try {
          bgroup = qclient.getBalanceGroup(groupName);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);
        }
        finally {
          master.releaseQueueMgrClient(qclient);
        }
      }

      Map<String, Integer> userShares = bgroup.getUserBiases();
      Map<String, Double> userMax = bgroup.getUserMaxShare();

      Map<String, Integer> groupShares = bgroup.getGroupBiases();
      Map<String, Double> groupMax = bgroup.getGroupMaxShare();

      Map<String, Double> calcShares = bgroup.getCalculatedFairShares(pWorkGroups);
      Map<String, Double> calcMax = bgroup.getCalculatedMaxShares(pWorkGroups);
      
      Map<String, Double> usage = pCurrentUsage.get(groupName);
      if (usage == null)
        usage = new TreeMap<String, Double>();
      
      Map<String, Integer> maxSlots = calcMaxSlots(groupName, calcMax);

      pUserTableModel.setBalanceGroupData(userShares, userMax, pPrivilegeDetails);
      pGroupTableModel.setBalanceGroupData(groupShares, groupMax, pPrivilegeDetails);
      pCalcTableModel.setCalculatedData
        (calcShares, usage, calcMax, maxSlots);
      pDefaultBiasField.setValue(bgroup.getDefaultBias());
      pDefaultMaxField.setValue(bgroup.getDefaultMaxShare());
      
      updateHistograms(calcShares, usage);
    }
  }
  
  public void
  updateFromTable()
  {
    Map<String, Integer> userShares = pUserTableModel.getBiases();
    Map<String, Double> userMax = pUserTableModel.getMaxShares();
    
    Map<String, Integer> groupShares = pGroupTableModel.getBiases();
    Map<String, Double> groupMax = pGroupTableModel.getMaxShares();
    
    UserBalanceGroup group = pBalanceGroups.get(pSelectedGroup); 
    
    group.setUserBiases(userShares);
    group.setUserMaxShares(userMax);
    group.setGroupBiases(groupShares);
    group.setGroupMaxShares(groupMax);
    group.setDefaultBias(pDefaultBiasField.getValue());
    group.setDefaultMaxShare(pDefaultMaxField.getValue());

    Map<String, Double> calcShares = group.getCalculatedFairShares(pWorkGroups);
    Map<String, Double> calcMax = group.getCalculatedMaxShares(pWorkGroups);
    Map<String, Double> usage = pCurrentUsage.get(pSelectedGroup);
    if (usage == null)
      usage = new TreeMap<String, Double>();
    Map<String, Integer> maxSlots = calcMaxSlots(pSelectedGroup, calcMax);


    pCalcTableModel.setCalculatedData
      (calcShares, usage, calcMax, maxSlots);
    
    updateHistograms(calcShares, usage);
  }
  
  private Map<String, Integer> 
  calcMaxSlots
  (
    String selectedGroup,
    Map<String, Double> calcMax
  )
  {
    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();
    
    Integer totalSlots = pCurrentBalanceGroupSize.get(selectedGroup);
    
    for (Entry<String, Double> entry : calcMax.entrySet()) {
      String user = entry.getKey();
      if (totalSlots == null || totalSlots == 0) {
        toReturn.put(user, 0);
      }
      else {
        Double share = entry.getValue();
        int numSlots = (int) Math.ceil(share * (double) totalSlots );
        if (numSlots == totalSlots && numSlots > 1 && share != 1d) 
          numSlots--;
        toReturn.put(user, numSlots);
      }
    }
    return toReturn;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  private void
  updateHistograms
  (
    Map<String, Double> userShares,
    Map<String, Double> userUse
  )
  {
    int maxSlices = 8;
    
    Histogram userShareHist = null;
    Histogram userUseHist = null;
    Histogram usersByShareHist = null;
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      
      MappedSet<Double, String> byValue = new MappedSet<Double, String>(userShares);
      
      TreeSet<String> includedUsers = new TreeSet<String>();
      { 
        Entry<Double, TreeSet<String>> entry = byValue.lastEntry();
        while (includedUsers.size() < maxSlices) {
          if (entry == null)
            break;
          TreeSet<String> value = entry.getValue();
          if (value != null) {
            for (String v : value) {
              includedUsers.add(v);
              if(includedUsers.size() >= maxSlices)
                break;
            }
          }
          entry = byValue.lowerEntry(entry.getKey());
        }
      }

      int otherValue = 0;
      TreeMap<String, Integer> includedShares = new TreeMap<String, Integer>();
      for (Entry<String, Double> entry : userShares.entrySet()) {
        String user = entry.getKey();
        int share = (int) Math.round(entry.getValue() * 100);
        if (includedUsers.contains(user)) {
          ranges.add(new HistogramRange(user));
          includedShares.put(user, share);
        }
        else {
          otherValue += share;
        }
      }
      ranges.add(new HistogramRange("[[Other]]"));
      
      HistogramSpec spec = new HistogramSpec("FairShares", ranges);
      userShareHist = new Histogram(spec);
      for (Entry<String, Integer> entry : includedShares.entrySet())
        userShareHist.catagorize(entry.getKey(), entry.getValue());
      userShareHist.catagorize("[[Other]]", otherValue);
    }
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      
      MappedSet<Double, String> byValue = new MappedSet<Double, String>(userUse);
      
      TreeSet<String> includedUsers = new TreeSet<String>();
      { 
        Entry<Double, TreeSet<String>> entry = byValue.lastEntry();
        while (includedUsers.size() < maxSlices) {
          if (entry == null)
            break;
          TreeSet<String> value = entry.getValue();
          if (value != null) {
            for (String v : value) {
              includedUsers.add(v);
              if(includedUsers.size() >= maxSlices)
                break;
            }
          }
          entry = byValue.lowerEntry(entry.getKey());
        }
      }

      int otherValue = 0;
      TreeMap<String, Integer> includedShares = new TreeMap<String, Integer>();
      for (Entry<String, Double> entry : userUse.entrySet()) {
        String user = entry.getKey();
        int share = (int) Math.round(entry.getValue() * 100);
        if (includedUsers.contains(user)) {
          ranges.add(new HistogramRange(user));
          includedShares.put(user, share);
        }
        else {
          otherValue += share;
        }
      }
      ranges.add(new HistogramRange("[[Other]]"));
      
      HistogramSpec spec = new HistogramSpec("ActualShares", ranges);
      userUseHist = new Histogram(spec);
      for (Entry<String, Integer> entry : includedShares.entrySet())
        userUseHist.catagorize(entry.getKey(), entry.getValue());
      userUseHist.catagorize("[[Other]]", otherValue);
    }
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      
      TreeSet<Double> allShares = new TreeSet<Double>(userShares.values());
      Double low = round2(allShares.first() * 100);
      Double high = round2(allShares.last() * 100);
      
      if (!low.equals(high)) {
        double lerp = round2((high - low) / maxSlices);
        double start = low;
        double end = round2(low + lerp);
        
        for (int i = 0; i < maxSlices-1; i++) {
          ranges.add(new HistogramRange(start, end));
          start = end;
          end = round2(lerp + end);
        }
        ranges.add(new HistogramRange(start, high + .5));
      }
      else
        ranges.add(new HistogramRange(low));
        
      HistogramSpec spec = new HistogramSpec("Users ByShare", ranges);
      usersByShareHist = new Histogram(spec);
      for (Entry<String, Double> entry : userShares.entrySet()) {
        usersByShareHist.catagorize(round2(entry.getValue() * 100));
      }
    }
    pHistogramPanel.updateHistograms(userShareHist, userUseHist, usersByShareHist);
    pHistogramPanel.frameAll();
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
  }
  
  /**
   * Update the balance group list after balance groups are added or removed.
   * 
   * @param selected
   *   The name of the group to be selected after it is updated
   *   
   * @return
   *   The name of the group selected or <code>null</code> if there is no group selected.
   */
  private String 
  updateList
  (
    String selected
  )
  {
    pGroupList.removeListSelectionListener(this);
    {
      DefaultListModel model = (DefaultListModel) pGroupList.getModel();
      model.clear();
  
      Set<String> groupNames = pBalanceGroups.keySet();
      
      for(String name : groupNames) 
        model.addElement(name);
  
      if (selected != null && groupNames.contains(selected))
        pGroupList.setSelectedValue(selected, true);
      else
        selected = null;
    }
    pGroupList.addListSelectionListener(this);
    return selected;
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
    pSelectedGroup = (String) pGroupList.getSelectedValue();

    updateBalanceGroup(pSelectedGroup);
  }
  
  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  public void 
  mouseClicked(MouseEvent e) {}
   
  public void 
  mouseEntered(MouseEvent e) {} 
  
  public void 
  mouseExited(MouseEvent e) {} 
  
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
        if ((mods & (on1 | off1)) == on1) {
          Component comp = e.getComponent();
          if (comp == pGroupList) {
            updateGroupsMenu();
            pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());      
          }
          else if (comp == pUserScroll || comp == pUserTable) {
            pTableType = TableType.USER;
            updateTablesMenu();
            pTablePopup.show(e.getComponent(), e.getX(), e.getY());
          }
          else if (comp == pGroupScroll || comp == pGroupTable) {
            pTableType = TableType.GROUP;
            updateTablesMenu();
            pTablePopup.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      }
    }
  }
  
  public void 
  mouseReleased(MouseEvent e) {}
  
  /*-- FOCUS LISTENER METHODS --------------------------------------------------------------*/
  
  @Override
  public void 
  focusGained(FocusEvent e) {}


  @Override
  public void 
  focusLost
  (
    FocusEvent e
  )
  {
    if (e.getComponent().equals(pDefaultBiasField)) {
      doDefaultShareChanged();
    }
    else if (e.getComponent().equals(pDefaultMaxField)) {
      doDefaultMaxChanged();
    }
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
    if(cmd.equals("group-add"))
      doGroupAdd();
    else if(cmd.equals("group-clone")) 
      doGroupsClone();
    else if(cmd.equals("group-remove")) 
      doGroupRemove();
    else if(cmd.equals("table-add" )) {
      if (pTableType == TableType.GROUP)
        doGroupTableAdd();
      else if (pTableType == TableType.USER)
        doUserTableAdd();
    }
    else if(cmd.equals("table-remove")) {
      if (pTableType == TableType.GROUP)
        doGroupTableRemove();
      else if (pTableType == TableType.USER)
        doUserTableRemove();
    }
    else if (cmd.equals("default-share-field"))
      doDefaultShareChanged();
    else if (cmd.equals("default-max-field"))
      doDefaultMaxChanged();
    else if (cmd.equals("update"))
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
    if (!pEditedGroups.isEmpty() ) {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.acquireQueueMgrClient();
      try {
        ArrayList<UserBalanceGroup> edited = new ArrayList<UserBalanceGroup>();
        for (String name : pEditedGroups) {
          edited.add(pBalanceGroups.get(name));
        }

        client.editBalanceGroups(edited);
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.releaseQueueMgrClient(client);
      }
      updateAll();
    }
  }
  
  /**
   * Update the table with the current selection keys.
   */ 
  private void 
  doUpdate() 
  {
    if (pEditedGroups.size() > 0) {
      String message = 
        "The following balance groups " + pEditedGroups + " have been modified.  All " +
        "existing changes will be lost if this dialog is updated.  Do you wish to continue " +
        "with the update? ";
      JConfirmDialog dialog = new JConfirmDialog(this, "Really Update?", message);
      dialog.setVisible(true);
      if (!dialog.wasConfirmed())
        return;
    }
    
    pUserTablePanel.stopEditing();
    pGroupTablePanel.stopEditing();
    
    updateAll();
  }
  
  @Override
  public void 
  doCancel()
  {
    if (pEditedGroups.size() > 0) {
      String message = 
        "The following balance groups " + pEditedGroups + " have been modified.  All " +
        "existing changes will be lost if this dialog is closed.  Do you wish to continue?";
      JConfirmDialog dialog = new JConfirmDialog(this, "Really Update?", message);
      dialog.setVisible(true);
      if (!dialog.wasConfirmed())
        return;
    }
    
    super.doCancel();
  }
  
  /**
   * Enable the Confirm/Apply buttons in response to an edit.
   */ 
  public void 
  doEdited() 
  {
    pEditedGroups.add(pSelectedGroup);
    pConfirmButton.setEnabled(true);
    pApplyButton.setEnabled(true);
  }
  
  /**
   * Add a balance group to the table.
   */ 
  private void 
  doGroupAdd()
  {
    boolean modified = false;
    {
      pGroupsNewDialog.setVisible(true);

      if(pGroupsNewDialog.wasConfirmed()) {
        String gname = pGroupsNewDialog.getName();
        if((gname != null) && (gname.length() > 0)) {
          UIMaster master = UIMaster.getInstance();
          QueueMgrClient client = master.acquireQueueMgrClient();
          try {
            client.addBalanceGroup(gname);
            UserBalanceGroup group = client.getBalanceGroup(gname);
            pBalanceGroups.put(gname, group);
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

    if(modified) {
      updateList((String) pGroupList.getSelectedValue());
    }
  }
  
  /**
   * Remove the selected rows from the balance groups table.
   */ 
  private void 
  doGroupRemove() 
  {
    boolean modified = false;
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.acquireQueueMgrClient();
      try {
          String selected = (String) pGroupList.getSelectedValue();
          
          if (selected != null) {
            client.removeBalanceGroup(selected);
            pBalanceGroups.remove(selected);
            pEditedGroups.remove(selected);
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
      updateList((String) pGroupList.getSelectedValue());
    }
  }
  
  /**
   * Add a balance group to the table which is a copy of the currently selected balance group.
   */ 
  private void 
  doGroupsClone()
  {
    boolean modified = false;
    
    if (pSelectedGroup != null) {
      pGroupsNewDialog.setVisible(true);
      if(pGroupsNewDialog.wasConfirmed()) {
        String gname = pGroupsNewDialog.getName();
        if((gname != null) && (gname.length() > 0)) {
          UIMaster master = UIMaster.getInstance();
          QueueMgrClient client = master.acquireQueueMgrClient();
          try {
            client.addBalanceGroup(gname);
            client.editBalanceGroup(new UserBalanceGroup
              (gname, pBalanceGroups.get(pSelectedGroup)));
            UserBalanceGroup group = client.getBalanceGroup(gname);
            pBalanceGroups.put(gname, group);
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
    
    if(modified) {
      updateList((String) pGroupList.getSelectedValue());
    }
  }
  
  private void
  doGroupTableAdd()
  {
    pGroupTablePanel.stopEditing();
    
    if (pSelectedGroup != null) {
      List<String> assigned = pGroupTableModel.getNames();
      TreeSet<String> existing = new TreeSet<String>(pWorkGroups.getGroups());
      existing.removeAll(assigned);
      pListDialog.setFields(existing);
      pListDialog.setVisible(true);
      if (pListDialog.wasConfirmed()) {
        TreeSet<String> toAdd = pListDialog.getSelected();
        pGroupTableModel.addEntries(toAdd, pDefaultBiasField.getValue(), 
                                    pDefaultMaxField.getValue());
        updateFromTable();
      }
    }
  }
  
  private void
  doUserTableAdd()
  {
    pUserTablePanel.stopEditing();
    
    if (pSelectedGroup != null) {
      List<String> assigned = pUserTableModel.getNames();
      TreeSet<String> existing = new TreeSet<String>(pWorkGroups.getUsers());
      existing.removeAll(assigned);
      pListDialog.setFields(existing);
      pListDialog.setVisible(true);
      if (pListDialog.wasConfirmed()) {
        TreeSet<String> toAdd = pListDialog.getSelected();
        pUserTableModel.addEntries(toAdd, pDefaultBiasField.getValue(), 
                                   pDefaultMaxField.getValue());
        updateFromTable();
      }
    }
  }
  
  private void
  doGroupTableRemove()
  {
    pGroupTablePanel.stopEditing();
    
    if (pSelectedGroup != null) {
      int[] selected = pGroupTable.getSelectedRows();
      if (selected.length > 0) {
        pGroupTableModel.removeEntries(selected);
      }
      updateFromTable();
    }
  }
  
  private void
  doUserTableRemove()
  {
    pUserTablePanel.stopEditing();
    
    if (pSelectedGroup != null) {
      int[] selected = pUserTable.getSelectedRows();
      if (selected.length > 0) {
        pUserTableModel.removeEntries(selected);
      }
      updateFromTable();
    }
  }
  
  /**
   * Update the current balance group from the default share field.
   */
  private void
  doDefaultShareChanged()
  {
    if (pSelectedGroup != null) {
      UserBalanceGroup bgroup = pBalanceGroups.get(pSelectedGroup);
      Integer share = pDefaultBiasField.getValue();
      int current = bgroup.getDefaultBias();
      if (share == null)
        pDefaultBiasField.setValue(current);
      else {
        share = clampInt(share);
        if (current != share) {
          pDefaultBiasField.setValue(share);
          doEdited();
          updateFromTable();
        }
      }
    }
  }
  
  /**
   * Update the current balance group from the max share field.
   */
  private void
  doDefaultMaxChanged()
  {
    if (pSelectedGroup != null) {
      UserBalanceGroup bgroup = pBalanceGroups.get(pSelectedGroup);
      Double max = pDefaultMaxField.getValue();
      double current = bgroup.getDefaultMaxShare();
      if (max == null)
        pDefaultMaxField.setValue(current);
      else {
        max = clampDouble(max);
        pDefaultMaxField.setValue(max);
        if (current != max) {
          doEdited();
          updateFromTable();
        } 
      }
    }
  }
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Clamp a double value to the appropriate value for a max share.
   */
  public static Double
  clampDouble
  (
    Double d  
  )
  {
    if (d < 0d)
      return 0d;
    if (d > 1d)
      return 1d;
    return d;
  }
  
  /**
   * Clamp an int value to the appropriate value for a user share.
   */
  public static Integer
  clampInt
  (
    Integer i  
  )
  {
    if (i < 0)
      return 0;
    return i;
  }
  
  public static double 
  round2
  (
    double num
  ) 
  {
    double result = num * 100;
    result = Math.round(result);
    result = result / 100;
    return result;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1413231582842832549L;
  
  protected static final int sLWidth  = 240;
  protected static final int sLHeight = 400;
  
  protected static final int sTSize  = 120;
  protected static final int sVSize  = 80;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The details of what users exists and what groups they belong to.
   */
  private WorkGroups pWorkGroups;
  
  /**
   * What sort of table was just clicked on.
   */
  private TableType pTableType;
  
  /*----------------------------------------------------------------------------------------*/

  private TreeMap<String, UserBalanceGroup> pBalanceGroups;
  
  private String pSelectedGroup;
  
  private TreeSet<String> pEditedGroups;
  
  private IntegerOpMap<String> pCurrentBalanceGroupSize;
  
  private DoubleMap<String, String, Double> pCurrentUsage;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The dialog update button.
   */ 
  private JButton  pUpdateButton; 

  /*----------------------------------------------------------------------------------------*/

  /**
   * List containing the names of the groups.
   */
  private JList pGroupList;
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The balance groups popup menu.
   */ 
  private JPopupMenu pGroupsPopup;
  
  /**
   * The balance groups popup menu items.
   */ 
  private JMenuItem  pGroupsAddItem;
  private JMenuItem  pGroupsCloneItem;
  private JMenuItem  pGroupsRemoveItem;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The popup menu for editing the tables.
   */
  private JPopupMenu pTablePopup;
  
  /**
   * The table popup menu items.
   */
  private JMenuItem  pTableAddItem;
  private JMenuItem  pTableRemoveItem;
  
  /*----------------------------------------------------------------------------------------*/
  
  private JIntegerField pDefaultBiasField;
  
  private JPercentField pDefaultMaxField;
  
  /*----------------------------------------------------------------------------------------*/
  
  private JScrollPane pUserScroll;
  private JTable pUserTable;
  private BalanceGroupTableModel pUserTableModel;
  private JTablePanel pUserTablePanel;
  
  private JScrollPane pGroupScroll;
  private JTable pGroupTable;
  private BalanceGroupTableModel pGroupTableModel;
  private JTablePanel pGroupTablePanel;
  
  private BalanceGroupCalcTableModel pCalcTableModel;
  
  /*----------------------------------------------------------------------------------------*/
  
  private JBalanceGroupHistPanel pHistogramPanel;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Dialog for getting the name of the new balance group. 
   */
  private JNewIdentifierDialog pGroupsNewDialog;
  
  /**
   * Dialog for adding users and groups to the balance group.
   */
  private JBooleanListDialog pListDialog;
  
}
