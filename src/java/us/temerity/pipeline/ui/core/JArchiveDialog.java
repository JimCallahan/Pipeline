// $Id: JArchiveDialog.java,v 1.2 2005/02/07 14:53:34 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for selecting the checked-in versions to archive.
 */ 
public 
class JArchiveDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JArchiveDialog() 
  {
    super("Archive Tool", false);

    /* create dialog body components */ 
    {
	
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("ButtonDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	cpanel.add(UIFactory.createPanelLabel("Candidate Versions:"));

	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  ArchiveCandidateTableModel model = new ArchiveCandidateTableModel();
	  pCandidateTableModel = model;
	  
	  JTablePanel tpanel =
	    new JTablePanel(model, model.getColumnWidths(), 
			    model.getRenderers(), model.getEditors());
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
	    
	    hbox.add(btn);	  
	  }

	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  hbox.add(Box.createHorizontalGlue());

	  cpanel.add(hbox);
	}
      }

      
      Box pbox = new Box(BoxLayout.X_AXIS);
      {
	{
	  JPanel panel = new JPanel();
	  panel.setName("ButtonDialogPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	  
	  {
	    Box box = new Box(BoxLayout.X_AXIS);

	    box.add(Box.createRigidArea(new Dimension(4, 0)));

	    {    
	      JLabel label = new JLabel("Versions to Archive:");
	      label.setName("PanelLabel");
	      box.add(label);
	    }	    

	    box.add(Box.createHorizontalGlue());
		
	    {    
	      JLabel label = new JLabel("Total Size: ???");
	      pArchiveSizeLabel = label;
	      label.setName("PanelLabel");
	      box.add(label);
	    }

	    box.add(Box.createRigidArea(new Dimension(23, 0)));

	    panel.add(box);
	  }
	  
	  panel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    NodeVersionTableModel model = new NodeVersionTableModel();
	    pArchiveTableModel = model;
	    
	    JTablePanel tpanel =
	      new JTablePanel(model, model.getColumnWidths(), 
			      model.getRenderers(), model.getEditors());
	    pArchiveTablePanel = tpanel;
	    
	    panel.add(tpanel);
	  }	
	  
	  panel.add(Box.createRigidArea(new Dimension(0, 5)));
	
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
	      
	      btn.setActionCommand("add-archive");
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Add All");
	      btn.setName("RaisedButton");
	      
	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));
	      
	      btn.setActionCommand("add-all-archive");
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(20, 0)));

	    {
	      JButton btn = new JButton("Remove");
	      btn.setName("RaisedButton");
	      
	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));
	      
	      btn.setActionCommand("remove-archive");
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(10, 0)));
			      
	    {
	      JButton btn = new JButton("Clear");
	      btn.setName("RaisedButton");
	      
	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));
	      
	      btn.setActionCommand("remove-all-archive");
	      btn.addActionListener(this);
	      
	      hbox.add(btn);	  
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(20, 0)));

	    {
	      JButton btn = new JButton("Calc Sizes");
	      btn.setName("RaisedButton");
	      
	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));
	      
	      btn.setActionCommand("calc-archive");
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    }	  

	    hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	    hbox.add(Box.createHorizontalGlue());

	    panel.add(hbox);
	  }

	  pbox.add(panel);
	}

	{
	  JPanel panel = new JPanel();
	  panel.setName("Spacer");
	  
	  panel.setMinimumSize(new Dimension(7, 100));
	  panel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  panel.setPreferredSize(new Dimension(7, 200));
	  
	  pbox.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("ButtonDialogPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	  
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

	    panel.add(box);
	  }
	  
	  panel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    NodeVersionTableModel model = new NodeVersionTableModel();
	    pOfflineTableModel = model;
	    
	    JTablePanel tpanel =
	      new JTablePanel(model, model.getColumnWidths(), 
			      model.getRenderers(), model.getEditors());
	    pOfflineTablePanel = tpanel;
	    
	    panel.add(tpanel);
	  }		  

	  panel.add(Box.createRigidArea(new Dimension(0, 5)));
	
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
	      
	      hbox.add(btn);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	    hbox.add(Box.createHorizontalGlue());

	    panel.add(hbox);
	  }

	  pbox.add(panel);
	}
      }

      JSplitPane body = new JVertSplitPanel(cpanel, pbox);
      body.setAlignmentX(0.5f);

      String extra[][] = {
	{ "Archive Only...",      "archive" },
	{ "Archive & Offline...", "archive-and-offline" },
	{ "Offline Only",         "offline" }
      };

      JButton btns[] = super.initUI("Archive Tool:", false, body, 
				    null, null, extra, "Close");

      pArchiveOnlyButton    = btns[0];
      pArchiveOfflineButton = btns[1];
      pOfflineOnlyButton    = btns[2];

      pArchiveOnlyButton.setEnabled(false);
      pArchiveOfflineButton.setEnabled(false);
      pOfflineOnlyButton.setEnabled(false);

      updatePanel();
      pack();
    }

    pSearchDialog         = new JArchivalQueryDialog(this);
    pArchiverParamsDialog = new JArchiverParamsDialog(this);
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
    try {
      pIsPrivileged = master.getMasterMgrClient().isPrivileged(false);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  /**
   * Update the enabled state of the dialog buttons.
   */ 
  private void 
  updateButtons() 
  {
    boolean hasArchive = !pArchiveTableModel.getData().isEmpty();
    boolean hasOffline = !pOfflineTableModel.getData().isEmpty();

    pArchiveOnlyButton.setEnabled(hasArchive);
    pArchiveOfflineButton.setEnabled(hasArchive && hasOffline);
    pOfflineOnlyButton.setEnabled(hasOffline);
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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("candidate-search")) 
      doCandidateSearch();
    else if(cmd.equals("clear-candidate")) 
      doClearCandidate();

    else if(cmd.equals("add-archive")) 
      doAddArchive();
    else if(cmd.equals("add-all-archive")) 
      doAddAllArchive();
    else if(cmd.equals("remove-archive")) 
      doRemoveArchive();
    else if(cmd.equals("remove-all-archive")) 
      doRemoveAllArchive();
    else if(cmd.equals("calc-archive")) 
      doCalcArchive();

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

    else if(cmd.equals("archive"))
      doArchive();
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
    pSearchDialog.setVisible(true);
    if(pSearchDialog.wasConfirmed()) {
      SearchTask task = 
	new SearchTask(pSearchDialog.getPattern(), pSearchDialog.getExcludeLatest(),
		       pSearchDialog.getMaxWorking(), pSearchDialog.getMaxArchives());
      task.start();
    }
  }
  
  /**
   * Clear the candidate checked-in versions.
   */ 
  private void 
  doClearCandidate() 
  {
    pCandidateTableModel.setArchivalInfo(null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the selected candidate versions to the archive table.
   */ 
  private void 
  doAddArchive() 
  {
    int rows[] = pCandidateTablePanel.getTable().getSelectedRows();
    addArchiveHelper(pCandidateTableModel.getVersions(rows));
  }

  /**
   * Add all candidate versions to the archive table.
   */ 
  private void 
  doAddAllArchive() 
  {
    addArchiveHelper(pCandidateTableModel.getVersions());
  }

  /**
   * Add the given versions to the archive table.
   */
  private void 
  addArchiveHelper
  (
    TreeMap<String,TreeSet<VersionID>> selected
  ) 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = pArchiveTableModel.getData();
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

    pArchiveTableModel.setData(data);    
    updateButtons();
  }

  /**
   * Remove the selected rows from the archive versions table.
   */ 
  private void 
  doRemoveArchive() 
  {
    int[] rows = pArchiveTablePanel.getTable().getSelectedRows();
    if(rows.length > 0) 
      pArchiveTableModel.setData(pArchiveTableModel.getDataExcept(rows));
    updateButtons();
  }

  /**
   * Remove all versions from the archive table.
   */ 
  private void 
  doRemoveAllArchive() 
  {
    pArchiveTableModel.setData(null);
    updateButtons();
  }

  /**
   * Calculate the total size of the archived files. 
   */ 
  private void 
  doCalcArchive() 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = null; 
    TreeMap<String,TreeSet<VersionID>> versions = null;
    {
      data = pArchiveTableModel.getData();
      
      versions = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : data.keySet()) {
	TreeMap<VersionID,Long> vsizes = data.get(name);
	for(VersionID vid : vsizes.keySet()) {
	  if(vsizes.get(vid) == null) {
	    TreeSet<VersionID> vids = versions.get(name);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      versions.put(name, vids);
	    }
	    
	    vids.add(vid);
	  }
	}
      }
    }

    CalcArchiveSizesTask task = new CalcArchiveSizesTask(data, versions);
    task.start();
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
    TreeMap<String,TreeMap<VersionID,Long>> data = null; 
    TreeMap<String,TreeSet<VersionID>> versions = null;
    {
      data = pOfflineTableModel.getData();
      
      versions = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : data.keySet()) {
	TreeMap<VersionID,Long> vsizes = data.get(name);
	for(VersionID vid : vsizes.keySet()) {
	  if(vsizes.get(vid) == null) {
	    TreeSet<VersionID> vids = versions.get(name);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      versions.put(name, vids);
	    }
	    
	    vids.add(vid);
	  }
	}
      }
    }

    CalcOfflineSizesTask task = new CalcOfflineSizesTask(data, versions);
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Archive the selected versions.
   */ 
  private void 
  doArchive() 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = pArchiveTableModel.getData();
    TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : data.keySet()) 
      versions.put(name, new TreeSet<VersionID>(data.get(name).keySet()));

    if(!versions.isEmpty()) {
      pArchiverParamsDialog.updateArchiver();
      pArchiverParamsDialog.setVisible(true);
      if(pArchiverParamsDialog.wasConfirmed()) {
	BaseArchiver archiver = pArchiverParamsDialog.getArchiver();
	if(archiver != null) {
	  ArchiveTask task = new ArchiveTask(versions, archiver);
	  task.start();
	}
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
   * Peform an archival candidate query.
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
     Integer maxWorking, 
     Integer maxArchives 
    )     
    {
      super("JArchiveDialog:SearchTask");

      pPattern       = pattern;	      
      pExcludeLatest = excludeLatest;   
      pMaxWorking    = maxWorking;      
      pMaxArchives   = maxArchives;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      TreeMap<String,TreeMap<VersionID,ArchivalInfo>> info = null;
      if(master.beginPanelOp("Searching for Candidate Versions...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient();
	  info = client.archivalQuery(pPattern, pExcludeLatest, pMaxWorking, pMaxArchives);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
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
    private Integer  pMaxWorking;
    private Integer  pMaxArchives;
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
     TreeMap<String,TreeMap<VersionID,ArchivalInfo>> info
    ) 
    {
      super("JArchiveDialog:UpdateTask");
      pInfo = info;
    }

    public void 
    run() 
    {
      pCandidateTableModel.setArchivalInfo(pInfo);
    }
    
    private TreeMap<String,TreeMap<VersionID,ArchivalInfo>> pInfo; 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Calculate the sizes of archive version files.
   */ 
  private
  class CalcArchiveSizesTask
    extends Thread
  {
    public 
    CalcArchiveSizesTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data,
     TreeMap<String,TreeSet<VersionID>> versions
    )     
    {
      super("JArchiveDialog:CalcArchiveSizesTask");
      
      pData     = data;
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
	  data = client.getArchivedSizes(pVersions);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      /* merge existing and new sizes */ 
      if(data != null) {
	for(String name : pData.keySet()) {
	  TreeMap<VersionID,Long> oversions = pData.get(name);
	  TreeMap<VersionID,Long> versions  = data.get(name);
	  if(versions == null) {
	    data.put(name, oversions);
	  }
	  else {
	    for(VersionID vid : oversions.keySet()) {
	      if(versions.get(vid) == null) 
		versions.put(vid, oversions.get(vid));
	    }
	  }
	}

	UpdateArchiveTask task = new UpdateArchiveTask(data);
	SwingUtilities.invokeLater(task);
      }
    }

    private TreeMap<String,TreeMap<VersionID,Long>>  pData;
    private TreeMap<String,TreeSet<VersionID>>       pVersions;
  }

  /** 
   * Update the archive table. 
   */ 
  private
  class UpdateArchiveTask
    extends Thread
  {
    public 
    UpdateArchiveTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data
    ) 
    {
      super("JArchiveDialog:UpdateArchiveTask");
      pData = data;
    }

    public void 
    run() 
    {
      pArchiveTableModel.setData(pData);  
      updateButtons();

      long total = 0L;
      for(String name : pData.keySet()) {
	for(Long size : pData.get(name).values()) {
	  if(size != null) 
	    total += size;
	}
      }

      pArchiveSizeLabel.setText("Total Size: " + formatLong(total));
    }
    
    
    private TreeMap<String,TreeMap<VersionID,Long>>  pData; 
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
     TreeMap<String,TreeMap<VersionID,Long>> data,
     TreeMap<String,TreeSet<VersionID>> versions
    )     
    {
      super("JArchiveDialog:CalcOfflineSizesTask");
      
      pData     = data;
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
	  data = client.getOfflinedSizes(pVersions);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      /* merge existing and new sizes */ 
      if(data != null) {
	for(String name : pData.keySet()) {
	  TreeMap<VersionID,Long> oversions = pData.get(name);
	  TreeMap<VersionID,Long> versions  = data.get(name);
	  if(versions == null) {
	    data.put(name, oversions);
	  }
	  else {
	    for(VersionID vid : oversions.keySet()) {
	      if(versions.get(vid) == null) 
		versions.put(vid, oversions.get(vid));
	    }
	  }
	}

	UpdateOfflineTask task = new UpdateOfflineTask(data);
	SwingUtilities.invokeLater(task);
      }
    }

    private TreeMap<String,TreeMap<VersionID,Long>>  pData;
    private TreeMap<String,TreeSet<VersionID>>       pVersions;
  }

  /** 
   * Update the offline table. 
   */ 
  private
  class UpdateOfflineTask
    extends Thread
  {
    public 
    UpdateOfflineTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data
    ) 
    {
      super("JArchiveDialog:UpdateOfflineTask");
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
   * Perform the archive operation.
   */ 
  private
  class ArchiveTask
    extends Thread
  {
    public 
    ArchiveTask
    (
     TreeMap<String,TreeSet<VersionID>> versions, 
     BaseArchiver archiver
    )     
    {
      super("JArchiveDialog:ArchiveTask");
      
      pVersions = versions;
      pArchiver = archiver;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      String aname = null;
      if(master.beginPanelOp("Archiving Nodes...")) {
	try {
	  aname = client.archive(pVersions, pArchiver);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      // display info about the newly created archives here... 
    }

    private TreeMap<String,TreeSet<VersionID>>  pVersions;
    private BaseArchiver                        pArchiver; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5667924780004601771L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The candidate version table model.
   */ 
  private ArchiveCandidateTableModel  pCandidateTableModel;

  /**
   * The candidate version table.
   */ 
  private JTablePanel  pCandidateTablePanel;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive version table model.
   */ 
  private NodeVersionTableModel  pArchiveTableModel;

  /**
   * The archive version table.
   */ 
  private JTablePanel  pArchiveTablePanel;

  /**
   * The label which displays the total size of all files to be archived.
   */ 
  private JLabel  pArchiveSizeLabel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The offline version table model.
   */ 
  private NodeVersionTableModel  pOfflineTableModel;

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
   * The archive only button.
   */ 
  private JButton  pArchiveOnlyButton;

  /**
   * The archive and offline button.
   */ 
  private JButton  pArchiveOfflineButton;

  /**
   * The offline only button.
   */ 
  private JButton  pOfflineOnlyButton;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The archival query parameters dialog.
   */
  private JArchivalQueryDialog  pSearchDialog;

  /**
   * The archiver parameters dialog.
   */ 
  private JArchiverParamsDialog  pArchiverParamsDialog; 
}
