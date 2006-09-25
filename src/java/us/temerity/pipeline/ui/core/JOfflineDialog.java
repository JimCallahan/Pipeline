// $Id: JOfflineDialog.java,v 1.6 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   O F F L I N E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for selecting the checked-in versions to offline.
 */ 
public 
class JOfflineDialog
  extends JTopLevelDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JOfflineDialog() 
  {
    super("Offline Tool");

    pPrivilegeDetails = new PrivilegeDetails();

    /* create dialog body components */ 
    {
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("ButtonDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	cpanel.add(UIFactory.createPanelLabel("Candidate Versions:"));

	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  OfflineCandidateTableModel model = new OfflineCandidateTableModel();
	  pCandidateTableModel = model;
	  
	  JTablePanel tpanel = new JTablePanel(model);
	  pCandidateTablePanel = tpanel;
	  
	  cpanel.add(tpanel);
	}

	cpanel.add(Box.createRigidArea(new Dimension(0, 5)));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createHorizontalGlue());
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = new JButton("Search...");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("candidate-search");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Search for new candidate checked-in versions to offline."));

	    hbox.add(btn);
	  }

	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
			      
	  {
	    JButton btn = new JButton("Clear");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("clear-candidate");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Clear the displayed candidate checked-in versions."));

	    hbox.add(btn);	  
	  }

	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  hbox.add(Box.createHorizontalGlue());

	  cpanel.add(hbox);
	}
      }

      JPanel apanel = new JPanel();
      {
	apanel.setName("ButtonDialogPanel");
	apanel.setLayout(new BoxLayout(apanel, BoxLayout.Y_AXIS));
	  
	{
	  Box box = new Box(BoxLayout.X_AXIS);
	  
	  box.add(Box.createRigidArea(new Dimension(4, 0)));
	  
	  {    
	    JLabel label = new JLabel("Versions to Offline:");
	    label.setName("PanelLabel");
	    box.add(label);
	  }	    
	  
	  box.add(Box.createHorizontalGlue());
	  
	  {    
	    JLabel label = new JLabel("Total Size: ???");
	    pOfflineSizeLabel = label;
	    label.setName("PanelLabel");
	    box.add(label);
	  }
	  
	  box.add(Box.createRigidArea(new Dimension(23, 0)));
	  
	  apanel.add(box);
	}
	
	apanel.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  NodeVersionSizeTableModel model = new NodeVersionSizeTableModel(1220);
	  pOfflineTableModel = model;
	  
	  JTablePanel tpanel = new JTablePanel(model);
	  pOfflineTablePanel = tpanel;
	  
	  apanel.add(tpanel);
	}	
	
	apanel.add(Box.createRigidArea(new Dimension(0, 5)));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  hbox.add(Box.createHorizontalGlue());
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = new JButton("Add");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("add-offline");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	     ("Add the selected candidate versions to the list of versions to be offlined."));

	    hbox.add(btn);
	  }
	    
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = new JButton("Add All");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("add-all-offline");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Add all candidate versions to the list of versions to be offlined."));

	    hbox.add(btn);
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
	  
	  {
	    JButton btn = new JButton("Remove");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("remove-offline");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Remove the selected versions from the list of versions to be offlined."));

	    hbox.add(btn);
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = new JButton("Clear");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("remove-all-offline");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Clear the list of versions to be offlined."));

	    hbox.add(btn);	  
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
	  
	  {
	    JButton btn = new JButton("Calc Sizes");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("calc-offline");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Calculate the amount of disk space that would be freed by offlining " + 
	       "the files associated with the checked-in versions."));

	    hbox.add(btn);
	  }	  
	  
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  hbox.add(Box.createHorizontalGlue());
	  
	  apanel.add(hbox);
	}
      }

      JSplitPane body = new JVertSplitPanel(cpanel, apanel);
      body.setAlignmentX(0.5f);

      String extra[][] = {
	{ "Offline", "offline" }
      };

      JButton btns[] = super.initUI("Offline Tool:", body, null, null, extra, "Close");

      pOfflineButton = btns[0];
      pOfflineButton.setEnabled(false);
      pOfflineButton.setToolTipText(UIFactory.formatToolTip
        ("Delete the files associated with the checked-in versions."));

      updatePanel();
      pack();
    }

    pQueryDialog = new JOfflineQueryDialog(this);
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel state.
   */
  public void 
  updatePanel() 
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      pPrivilegeDetails = client.getPrivilegeDetails();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    updateButtons();
  }

  /**
   * Update the enabled state of the dialog buttons.
   */ 
  private void 
  updateButtons() 
  {
    pOfflineButton.setEnabled
      (pPrivilegeDetails.isMasterAdmin() && !pOfflineTableModel.getData().isEmpty());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(cmd.equals("candidate-search")) 
      doCandidateSearch();
    else if(cmd.equals("clear-candidate")) 
      doClearCandidate();

    else if(cmd.equals("add-offline")) 
      doAddOffline();
    else if(cmd.equals("add-all-offline")) 
      doAddAllOffline();
    else if(cmd.equals("remove-offline")) 
      doRemoveOffline();
    else if(cmd.equals("remove-all-offline")) 
      doRemoveAllOffline();
    else if(cmd.equals("calc-offline")) 
      doCalcOffline();

    else if(cmd.equals("offline"))
      doOffline();

    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Search for candidate checked-in versions.
   */ 
  private void 
  doCandidateSearch() 
  {
    pQueryDialog.setVisible(true);
    if(pQueryDialog.wasConfirmed()) {
      SearchTask task = 
	new SearchTask(pQueryDialog.getPattern(), pQueryDialog.getExcludeLatest(), 
		       pQueryDialog.getMinArchives(), pQueryDialog.getUnusedOnly());
      task.start();
    }
  }
  
  /**
   * Clear the candidate checked-in versions.
   */ 
  private void 
  doClearCandidate() 
  {
    pCandidateTableModel.setOfflineInfo(null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the selected candidate versions to the offline table.
   */ 
  private void 
  doAddOffline() 
  {
    int rows[] = pCandidateTablePanel.getTable().getSelectedRows();
    addOfflineHelper(pCandidateTableModel.getVersions(rows));
  }

  /**
   * Add all candidate versions to the offline table.
   */ 
  private void 
  doAddAllOffline() 
  {
    addOfflineHelper(pCandidateTableModel.getVersions());
  }

  /**
   * Add the given versions to the offline table.
   */
  private void 
  addOfflineHelper
  (
    TreeMap<String,TreeSet<VersionID>> selected
  ) 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = pOfflineTableModel.getData();
    for(String name : selected.keySet()) {
      TreeMap<VersionID,Long> versions = data.get(name);
      if(versions == null) {
	versions = new TreeMap<VersionID,Long>();
	data.put(name, versions);
      }

      for(VersionID vid : selected.get(name)) {
	if(!versions.containsKey(vid))
	  versions.put(vid, null);
      }
    }

    pOfflineTableModel.setData(data);    
    updateButtons();
  }

  /**
   * Remove the selected rows from the offline versions table.
   */ 
  private void 
  doRemoveOffline() 
  {
    int[] rows = pOfflineTablePanel.getTable().getSelectedRows();
    if(rows.length > 0) 
      pOfflineTableModel.setData(pOfflineTableModel.getDataExcept(rows));
    updateButtons();
  }

  /**
   * Remove all versions from the offline table.
   */ 
  private void 
  doRemoveAllOffline() 
  {
    pOfflineTableModel.setData(null);
    updateButtons();
  }

  /**
   * Calculate the total size of the offlined files. 
   */ 
  private void 
  doCalcOffline() 
  {
    TreeMap<String,TreeSet<VersionID>> versions = null;
    {
      TreeMap<String,TreeMap<VersionID,Long>> data = pOfflineTableModel.getData();
      versions = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : data.keySet()) {
	TreeMap<VersionID,Long> vsizes = data.get(name);
	for(VersionID vid : vsizes.keySet()) {
	  TreeSet<VersionID> vids = versions.get(name);
	  if(vids == null) {
	    vids = new TreeSet<VersionID>();
	    versions.put(name, vids);
	  }
	    
	  vids.add(vid);
	}
      }
    }

    CalcOfflineSizesTask task = new CalcOfflineSizesTask(versions);
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Offline the selected versions.
   */ 
  private void 
  doOffline() 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = pOfflineTableModel.getData();
    TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : data.keySet()) 
      versions.put(name, new TreeSet<VersionID>(data.get(name).keySet()));

    if(!versions.isEmpty()) {
      JConfirmDialog confirm = new JConfirmDialog(this, "Are you sure?");
      confirm.setVisible(true);
      if(confirm.wasConfirmed()) {
	OfflineTask task = new OfflineTask(versions); 
	task.start();
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value == null) 
      return "-";
    
    if(value < 1024) {
      return value.toString();
    }
    else if(value < 1048576) {
      double k = ((double) value) / 1024.0;
      return String.format("%1$.1fK", k);
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.1fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.1fG", g);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Peform an offline candidate query.
   */ 
  private
  class SearchTask
    extends Thread
  {
    public 
    SearchTask
    (
     String pattern,
     Integer excludeLatest, 
     Integer minArchives, 
     boolean unusedOnly 
    )     
    {
      super("JOfflineDialog:SearchTask");

      pPattern       = pattern;	        
      pExcludeLatest = excludeLatest;   
      pMinArchives   = minArchives;   
      pUnusedOnly    = unusedOnly; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      ArrayList<OfflineInfo> info = null;
      if(master.beginPanelOp("Searching for Candidate Versions...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient();
	  info = client.offlineQuery(pPattern, pExcludeLatest, pMinArchives, pUnusedOnly);
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(info);
      SwingUtilities.invokeLater(task);
    }

    private String   pPattern;
    private Integer  pExcludeLatest;
    private Integer  pMinArchives;
    private boolean  pUnusedOnly; 
  }

  /** 
   * Update the UI components.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     ArrayList<OfflineInfo> info
    ) 
    {
      super("JOfflineDialog:UpdateTask");
      pInfo = info;
    }

    public void 
    run() 
    {
      pCandidateTableModel.setOfflineInfo(pInfo);
    }
    
    private ArrayList<OfflineInfo> pInfo; 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Calculate the sizes of offline version files.
   */ 
  private
  class CalcOfflineSizesTask
    extends Thread
  {
    public 
    CalcOfflineSizesTask
    (
     TreeMap<String,TreeSet<VersionID>> versions
    )     
    {
      super("JOfflineDialog:CalcOfflineSizesTask");
      
      pVersions = versions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      TreeMap<String,TreeMap<VersionID,Long>> data = null;
      if(master.beginPanelOp("Calculating File Sizes...")) {
	try {
	  data = client.getOfflineSizes(pVersions);
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      /* add versions without sizes */ 
      if(data != null) {
	for(String name : pVersions.keySet()) {
	  TreeSet<VersionID> oversions = pVersions.get(name);
	  TreeMap<VersionID,Long> versions = data.get(name);
	  if(versions == null) {
	    versions = new TreeMap<VersionID,Long>();
	    data.put(name, versions);
	  }
	  
	  for(VersionID vid : oversions) {
	    if(!versions.containsKey(vid)) 
	      versions.put(vid, null);
	  }
	}

	UpdateSizesTask task = new UpdateSizesTask(data);
	SwingUtilities.invokeLater(task);
      }
    }

    private TreeMap<String,TreeSet<VersionID>>  pVersions;
  }

  /** 
   * Update the offline table sizes.
   */ 
  private
  class UpdateSizesTask
    extends Thread
  {
    public 
    UpdateSizesTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data
    ) 
    {
      super("JOfflineDialog:UpdateSizesTask");
      pData = data;
    }

    public void 
    run() 
    {
      pOfflineTableModel.setData(pData);  
      updateButtons();

      long total = 0L;
      for(String name : pData.keySet()) {
	for(Long size : pData.get(name).values()) {
	  if(size != null) 
	    total += size;
	}
      }

      pOfflineSizeLabel.setText("Total Size: " + formatLong(total));
    }
    
    
    private TreeMap<String,TreeMap<VersionID,Long>>  pData; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Offline the versions. 
   */ 
  private
  class OfflineTask
    extends Thread
  {
    public 
    OfflineTask
    (
     TreeMap<String,TreeSet<VersionID>> versions
    )     
    {
      super("JOfflineDialog:OfflineTask");
      
      pVersions = versions; 
    }

    public void 
    run() 
    {  
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      if(master.beginPanelOp("Offlining Checked-In Versions...")) {
	try {
	  client.offline(pVersions); 
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	RemoveAllTask task = new RemoveAllTask();
	SwingUtilities.invokeLater(task);      
      }
    }

    private TreeMap<String,TreeSet<VersionID>>  pVersions;
  }

  /** 
   * Remove all entries from the offline table.
   */ 
  private
  class RemoveAllTask
    extends Thread
  {
    public 
    RemoveAllTask() 
    {
      super("JOfflineDialog:RemoveAllTask");
    }

    public void 
    run() 
    {
      doRemoveAllOffline();
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1776854816064053417L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The candidate version table model.
   */ 
  private OfflineCandidateTableModel  pCandidateTableModel;

  /**
   * The candidate version table.
   */ 
  private JTablePanel  pCandidateTablePanel;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The offline version table model.
   */ 
  private NodeVersionSizeTableModel  pOfflineTableModel;

  /**
   * The offline version table.
   */ 
  private JTablePanel  pOfflineTablePanel;

  /**
   * The label which displays the total size of all files to be offlined.
   */ 
  private JLabel  pOfflineSizeLabel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The offline button.
   */ 
  private JButton  pOfflineButton;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The offline query parameters dialog.
   */
  private JOfflineQueryDialog  pQueryDialog;

}
