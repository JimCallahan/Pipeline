// $Id: JArchiveDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
	cpanel.setName("MainDialogPanel");
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
      }

      
      Box hbox = new Box(BoxLayout.X_AXIS);
      {
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainDialogPanel");
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
	  
	  hbox.add(panel);
	}

	{
	  JPanel panel = new JPanel();
	  panel.setName("Spacer");
	  
	  panel.setMinimumSize(new Dimension(7, 100));
	  panel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  panel.setPreferredSize(new Dimension(7, 200));
	  
	  hbox.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainDialogPanel");
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

	  hbox.add(panel);
	}
      }

      JSplitPane body = new JVertSplitPanel(cpanel, hbox);
      body.setAlignmentX(0.5f);

      String extra[][] = {
	null,
	{ "Search...", "search" },
	{ "Clear", "clear" },
	null, 
	{ "Archive", "archive" },
	{ "Offline", "offline" },
	{ "Remove", "remove" },
	null, 
	{ "Calc Sizes", "calc-sizes" }
      };

      JButton btns[] = super.initUI("Archive Tool:", false, body, 
				    null, "Apply", extra, "Close");

      updatePanel();
      pack();
    }

    pSearchDialog = new JArchivalQueryDialog(this);
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
    if(cmd.equals("search")) 
      doSearch();
    else if(cmd.equals("clear")) 
      doClear();
     else if(cmd.equals("archive")) 
      doArchive();
    else if(cmd.equals("offline")) 
      doOffline();
    else if(cmd.equals("remove")) 
      doRemove();   
    else if(cmd.equals("calc-sizes")) 
      doCalcSizes();   
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Archive/Offline the specified versions.
   */ 
  public void 
  doApply() 
  {
    
    
    // ...

  }

  /**
   * Search for candidate checked-in versions.
   */ 
  private void 
  doSearch() 
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
  doClear() 
  {
    pCandidateTableModel.setArchivalInfo(null);
  }
  
  /**
   * Add the selected candidate versions to the version to archive table.
   */ 
  private void 
  doArchive() 
  {
    int rows[] = pCandidateTablePanel.getTable().getSelectedRows();
    TreeMap<String,TreeSet<VersionID>> selected = 
      pCandidateTableModel.getVersions(rows);
    
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
  }

  /**
   * Add the selected candidate versions to the version to offline table.
   */ 
  private void 
  doOffline() 
  {
    int rows[] = pCandidateTablePanel.getTable().getSelectedRows();
    TreeMap<String,TreeSet<VersionID>> selected = 
      pCandidateTableModel.getOfflineVersions(rows);
    
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
  }

  /**
   * Remove the selected rows from the versions to archive/offline tables.
   */ 
  private void 
  doRemove() 
  {
    {
      int[] rows = pArchiveTablePanel.getTable().getSelectedRows();
      if(rows.length > 0) 
	pArchiveTableModel.setData(pArchiveTableModel.getDataExcept(rows));
    }

    {
      int[] rows = pOfflineTablePanel.getTable().getSelectedRows();
      if(rows.length > 0) 
	pOfflineTableModel.setData(pOfflineTableModel.getDataExcept(rows));
    }
  }

  /**
   * Calculate the total size of the archived and offlined files. 
   */ 
  private void 
  doCalcSizes() 
  {
    TreeMap<String,TreeMap<VersionID,Long>> adata = null; 
    TreeMap<String,TreeSet<VersionID>> aversions = null;
    {
      adata = pArchiveTableModel.getData();
      
      aversions = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : adata.keySet()) {
	TreeMap<VersionID,Long> vsizes = adata.get(name);
	for(VersionID vid : vsizes.keySet()) {
	  if(vsizes.get(vid) == null) {
	    TreeSet<VersionID> vids = aversions.get(name);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      aversions.put(name, vids);
	    }
	    
	    vids.add(vid);
	  }
	}
      }
    }

    CalcSizesTask task = new CalcSizesTask(adata, aversions);
    task.start();
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
   * Calculate the sizes of the to be archived/offlined files.
   */ 
  private
  class CalcSizesTask
    extends Thread
  {
    public 
    CalcSizesTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> adata,
     TreeMap<String,TreeSet<VersionID>> aversions
    )     
    {
      super("JArchiveDialog:CalcSizesTask");
      
      pArchiveData     = adata;
      pArchiveVersions = aversions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      TreeMap<String,TreeMap<VersionID,Long>> adata = null;
//       TreeMap<String,TreeMap<VersionID,Long>> osizes = null;
      if(master.beginPanelOp()) {
	try {
	  master.updatePanelOp("Calculating Archive File Sizes...");
	  adata = client.getArchivedSizes(pArchiveVersions);

// 	  master.updatePanelOp("Calculating Offline File Sizes...");
// 	  osizes = client.getOfflinedSizes(pOfflineVersions);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      /* merge existing and new sizes */ 
      if(adata != null) {
	for(String name : pArchiveData.keySet()) {
	  TreeMap<VersionID,Long> oversions = pArchiveData.get(name);
	  TreeMap<VersionID,Long> versions  = adata.get(name);
	  if(versions == null) {
	    adata.put(name, oversions);
	  }
	  else {
	    for(VersionID vid : oversions.keySet()) {
	      if(versions.get(vid) == null) 
		versions.put(vid, oversions.get(vid));
	    }
	  }
	}

	UpdateArchiveTask task = new UpdateArchiveTask(adata);
	SwingUtilities.invokeLater(task);
      }

      // ...

    }

    private TreeMap<String,TreeMap<VersionID,Long>>  pArchiveData;
    private TreeMap<String,TreeSet<VersionID>>       pArchiveVersions;
  }

  
  /** 
   * Update the arhchive table. 
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

      long total = 0L;
      for(String name : pData.keySet()) {
	for(Long size : pData.get(name).values()) {
	  if(size != null) 
	    total += size;
	}
      }

      pArchiveSizeLabel.setText("Total Size: " + formatLong(total));
    }
    
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
    
    private TreeMap<String,TreeMap<VersionID,Long>>  pData; 
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
   * The archival query parameters dialog.
   */
  private JArchivalQueryDialog  pSearchDialog;
}
