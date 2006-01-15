// $Id: JRestoreDialog.java,v 1.8 2006/01/15 06:29:26 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for selecting the checked-in versions to restore.
 */ 
public 
class JRestoreDialog
  extends JBaseDialog
  implements ActionListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRestoreDialog() 
  {
    super("Restore Tool", false);

    /* initialize fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();

      pArchiveVolumes = new TreeMap<String,ArchiveVolume>();
      pUpdateLock = new Object();
    }

    /* create dialog body components */ 
    {
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("ButtonDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	cpanel.add(UIFactory.createPanelLabel("Restore Requests:"));

	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  RestoreRequestTableModel model = new RestoreRequestTableModel();
	  pRequestTableModel = model;
	  
	  JTablePanel tpanel = new JTablePanel(model);
	  pRequestTablePanel = tpanel;
	  
	  cpanel.add(tpanel);
	}
	
	cpanel.add(Box.createRigidArea(new Dimension(0, 5)));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createHorizontalGlue());
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = new JButton("Update");
	    btn.setName("RaisedButton");
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("update-requests");
	    btn.addActionListener(this);

	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Update the displayed checked-in version restore requests."));

	    hbox.add(btn);
	  }

	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
			      
	  {
	    JButton btn = new JButton("Deny Request");
	    pDenyButton = btn;
	    btn.setName("RaisedButton");
	    btn.setEnabled(false);
	    
	    Dimension size = btn.getPreferredSize();
	    btn.setMinimumSize(new Dimension(108, 31));
	    btn.setMaximumSize(new Dimension(size.width, 31));
	    
	    btn.setActionCommand("deny-requests");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Deny the request to restore the selected checkd-in versions."));

	    hbox.add(btn);	  
	  }

	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  hbox.add(Box.createHorizontalGlue());

	  cpanel.add(hbox);
	}
      }

      JPanel hpanel = new JPanel();
      {
	hpanel.setName("ButtonDialogPanel");
	hpanel.setLayout(new BoxLayout(hpanel, BoxLayout.X_AXIS));
	
	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  {
	    Box box = new Box(BoxLayout.X_AXIS);
	    
	    box.add(Box.createRigidArea(new Dimension(4, 0)));

	    {    
	      JLabel label = new JLabel("Versions to Restore:");
	      label.setName("PanelLabel");
	      box.add(label);
	    }	    

	    box.add(Box.createHorizontalGlue());

	    {    
	      JLabel label = new JLabel("Total Size: ???");
	      pRestoreSizeLabel = label;
	      label.setName("PanelLabel");
	      box.add(label);
	    }

	    box.add(Box.createRigidArea(new Dimension(23, 0)));

	    vbox.add(box);
	  }

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  {
	    NodeVersionSizeTableModel model = new NodeVersionSizeTableModel(520);
	    pRestoreTableModel = model;

	    JTablePanel tpanel = new JTablePanel(model);
	    pRestoreTablePanel = tpanel;

	    vbox.add(tpanel);
	  }	

	  vbox.add(Box.createRigidArea(new Dimension(0, 5)));

	  {
	    Box box = new Box(BoxLayout.X_AXIS);

	    box.add(Box.createHorizontalGlue());
	    box.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Add");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("add-restore");
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Add the selected candidate versions to the list of " + 
		 "versions to be restored."));

	      box.add(btn);
	    }

	    box.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Add All");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("add-all-restore");
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Add all candidate versions to the list of versions to be restored."));

	      box.add(btn);
	    }

	    box.add(Box.createRigidArea(new Dimension(20, 0)));

	    {
	      JButton btn = new JButton("Remove");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("remove-restore");
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Remove the selected versions from the list of versions to be restored."));

	      box.add(btn);
	    }

	    box.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Clear");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("remove-all-restore");
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Clear the list of versions to be restored."));

	      box.add(btn);	  
	    }

	    box.add(Box.createRigidArea(new Dimension(10, 0)));
	    box.add(Box.createHorizontalGlue());

	    vbox.add(box);
	  }

	  hpanel.add(vbox);
	}

	hpanel.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  {
	    Box box = new Box(BoxLayout.X_AXIS);

	    box.add(Box.createRigidArea(new Dimension(4, 0)));

	    {    
	      JLabel label = new JLabel("Archive Volumes to Restore:");
	      label.setName("PanelLabel");
	      box.add(label);
	    }	    

	    box.add(Box.createHorizontalGlue());

	    {    
	      JLabel label = new JLabel("Restores: ???");
	      pRestoreCountLabel = label;
	      label.setName("PanelLabel") ;
	      box.add(label);
	    }

	    box.add(Box.createRigidArea(new Dimension(23, 0)));

	    vbox.add(box);
	  }

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  {
	    ArchiveVolumeTableModel model = new ArchiveVolumeTableModel();
	    pArchiveTableModel = model;

	    JTablePanel tpanel = new JTablePanel(model);
	    pArchiveTablePanel = tpanel;

	    tpanel.getTable().getSelectionModel().addListSelectionListener(this);

	    vbox.add(tpanel);
	  }	
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 5)));

	  {
	    Box box = new Box(BoxLayout.X_AXIS);

	    box.add(Box.createHorizontalGlue());
	    box.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Use Volume");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("use-volume"); 
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Add the selected archive volumes to set which will be used to " + 
		 "restore checked-in versions."));

	      box.add(btn);
	    }

	    box.add(Box.createRigidArea(new Dimension(10, 0)));

	    {
	      JButton btn = new JButton("Ignore Volume");
	      btn.setName("RaisedButton");

	      Dimension size = btn.getPreferredSize();
	      btn.setMinimumSize(new Dimension(108, 31));
	      btn.setMaximumSize(new Dimension(size.width, 31));

	      btn.setActionCommand("ignore-volume");
	      btn.addActionListener(this);

	      btn.setToolTipText(UIFactory.formatToolTip
		("Remove the selected archive volumes to set which will be used to " + 
		 "restore checked-in versions."));

	      box.add(btn);
	    }

	    box.add(Box.createRigidArea(new Dimension(10, 0)));
	    box.add(Box.createHorizontalGlue());

	    vbox.add(box);
	  }

	  hpanel.add(vbox);
	}
      }


      JSplitPane body = new JVertSplitPanel(cpanel, hpanel);
      body.setAlignmentX(0.5f);
      
      String extra[][] = {
	{ "Restore...", "restore" },
      };

      JButton btns[] = 
	super.initUI("Restore Tool:", false, body, null, null, extra, "Close");

      pRestoreButton = btns[0];
      pRestoreButton.setEnabled(false);

      doUpdate();
      pack();
    }

    pRestoreParamsDialog = new JRestoreParamsDialog(this);
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
      master.showErrorDialog(ex);
    }

    updateButtons();
  }

  /**
   * Update the enabled state of the dialog buttons.
   */ 
  private void 
  updateButtons() 
  {
    int chosen = pArchiveTableModel.getChosenCount();
    pRestoreButton.setEnabled(pPrivilegeDetails.isQueueAdmin() && (chosen > 0));
    pDenyButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
  }

  /**
   * Update the restore counter.
   */ 
  private void 
  updateRestoreCounter() 
  {
    int chosen = pArchiveTableModel.getChosenCount();
    int total = pRestoreTableModel.getRowCount();
    pRestoreCountLabel.setText("Restores: " + chosen + "/" + total); 

    updatePanel();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(isVisible) 
      doUpdate();

    super.setVisible(isVisible);
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

    /* get the selected archive volumes */ 
    ArrayList<ArchiveVolume> volumes = null;
    {
      int rows[] = pArchiveTablePanel.getTable().getSelectedRows();
      volumes = pArchiveTableModel.getArchiveVolumes(rows);
    }
    
    /* select any checked-in versions contained in the volumes */ 
    {
      JTable table = pRestoreTablePanel.getTable();
      table.clearSelection();
    
      int row;
      for(row=0; row<pRestoreTableModel.getRowCount(); row++) {
	for(ArchiveVolume vol : volumes) {
	  String name = pRestoreTableModel.getName(row);
	  VersionID vid = pRestoreTableModel.getVersionID(row);
	  if((name != null) && (vid != null) && vol.contains(name, vid)) 
	    table.addRowSelectionInterval(row, row);
	}
      }
    }
  }


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
    if(cmd.equals("restore")) 
      doRestore();
    else if(cmd.equals("deny-requests")) 
      doDeny();
    else if(cmd.equals("update-requests")) 
      doUpdate();

    else if(cmd.equals("add-restore")) 
      doAddRestore();
    else if(cmd.equals("add-all-restore")) 
      doAddAllRestore();
    else if(cmd.equals("remove-restore")) 
      doRemoveRestore();
    else if(cmd.equals("remove-all-restore")) 
      doRemoveAllRestore();

    else if(cmd.equals("use-volume")) 
      doUseVolume();
    else if(cmd.equals("ignore-volume")) 
      doIgnoreVolume();

    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Restore the selected versions.
   */ 
  private void 
  doRestore() 
  {
    TreeMap<String,TreeMap<String,TreeSet<VersionID>>> rversions = 
      pArchiveTableModel.getRestoreVersions();

    ArrayList<String> anames = new ArrayList<String>(rversions.keySet());

    RestoreParamsTask task = new RestoreParamsTask(0, anames, rversions);
    SwingUtilities.invokeLater(task);     
  }

  /**
   * Deny the restore request for the selected versions.
   */ 
  private void 
  doDeny() 
  {
    int rows[] = pRequestTablePanel.getTable().getSelectedRows();
    TreeMap<String,TreeSet<VersionID>> selected = pRequestTableModel.getVersions(rows);
    
    DenyTask task = new DenyTask(selected);
    task.start();
  }

  /**
   * Update the restore requests.
   */ 
  private void 
  doUpdate() 
  {
    GetRequestsTask task = new GetRequestsTask();
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the selected candidate versions to the archive table.
   */ 
  private void 
  doAddRestore() 
  {
    int rows[] = pRequestTablePanel.getTable().getSelectedRows();
    addRestoreHelper(pRequestTableModel.getVersions(rows));
  }

  /**
   * Add all candidate versions to the archive table.
   */ 
  private void 
  doAddAllRestore() 
  {
    addRestoreHelper(pRequestTableModel.getVersions());
  }

  /**
   * Add the given versions to the archive table.
   */
  private void 
  addRestoreHelper
  (
    TreeMap<String,TreeSet<VersionID>> selected
  ) 
  {
    TreeMap<String,TreeMap<VersionID,Long>> data = pRestoreTableModel.getData();
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

    ArchivesTask task = new ArchivesTask(data);
    task.start();
  }

  /**
   * Remove the selected rows from the archive versions table.
   */ 
  private void 
  doRemoveRestore()
  {
    int[] rows = pRestoreTablePanel.getTable().getSelectedRows();
    if(rows.length == 0) 
      return;

    TreeMap<String,TreeMap<VersionID,Long>> data = pRestoreTableModel.getDataExcept(rows); 
      
    ArchivesTask task = new ArchivesTask(data);
    task.start();
  }

  /**
   * Remove all versions from the archive table.
   */ 
  private void 
  doRemoveAllRestore() 
  {
    pRestoreTableModel.setData(null);
    pRestoreSizeLabel.setText("Total Size: ???");

    pArchiveTableModel.setData(null, null);
    pRestoreCountLabel.setText("Restores: ???");

    updateButtons();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the selected archive volumes to set which will be used to restore 
   * checked-in versions.
   */ 
  private void 
  doUseVolume()
  {
    int rows[] = pArchiveTablePanel.getTable().getSelectedRows();
    if(rows.length == 0)
      return;

    TreeSet<String> chosen = pArchiveTableModel.getChosenArchives();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      String aname = pArchiveTableModel.getName(rows[wk]);
      chosen.add(aname);
    }
    pArchiveTableModel.setChosenArchives(chosen);

    updateRestoreCounter();
  }
  
  /**
   * Remove the selected archive volumes to set which will be used to restore 
   * checked-in versions.
   */ 
  private void 
  doIgnoreVolume()
  {
    int rows[] = pArchiveTablePanel.getTable().getSelectedRows();
    if(rows.length == 0)
      return;

    TreeSet<String> chosen = pArchiveTableModel.getChosenArchives();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      String aname = pArchiveTableModel.getName(rows[wk]);
      chosen.remove(aname);
    }
    pArchiveTableModel.setChosenArchives(chosen);

    updateRestoreCounter();
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
   * Get the the restore requests.
   */ 
  private
  class GetRequestsTask
    extends Thread
  {
    public 
    GetRequestsTask() 
    {
      super("JRestoreDialog:GetRequestsTask");
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      TreeMap<String,TreeMap<VersionID,RestoreRequest>> reqs = null;
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Loading Restore Requests...")) {
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();
	    reqs = client.getRestoreRequests();
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }

      UpdateRequestsTask task = new UpdateRequestsTask(reqs);
      SwingUtilities.invokeLater(task);
    }
  }

  /** 
   * Update the restore requests table. 
   */ 
  private
  class UpdateRequestsTask
    extends Thread
  {
    public 
    UpdateRequestsTask
    (
     TreeMap<String,TreeMap<VersionID,RestoreRequest>> reqs
    ) 
    {
      super("JRestoreDialog:UpdateRequestsTask");
      pRequests = reqs;
    }

    public void 
    run() 
    {
      pRequestTableModel.setRestoreRequests(pRequests);
      updatePanel();
    }
    
    private TreeMap<String,TreeMap<VersionID,RestoreRequest>>  pRequests;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Deny the restore of the selected versions.
   */ 
  private
  class DenyTask
    extends Thread
  {
    public 
    DenyTask
    (
     TreeMap<String,TreeSet<VersionID>> versions
    ) 
    {
      super("JRestoreDialog:DenyTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Denying Requests...")) {
	  try {
	    client.denyRestore(pVersions);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }

      GetRequestsTask task = new GetRequestsTask();
      task.start();
    }

    private TreeMap<String,TreeSet<VersionID>>  pVersions; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the archive volumes table. 
   */ 
  private
  class ArchivesTask
    extends Thread
  {
    public 
    ArchivesTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data
    ) 
    {
      super("JRestoreDialog:ArchivesTask");
      pData = data;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();

      TreeMap<String,TreeMap<VersionID,TreeSet<String>>> contains = null;
      ArrayList<ArchiveVolume> volumes = null;
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Update Archive Volumes...")) {
	  try {
	    TreeMap<String,TreeSet<VersionID>> versions = 
	      new TreeMap<String,TreeSet<VersionID>>();
	    for(String name : pData.keySet()) 
	      versions.put(name, new TreeSet<VersionID>(pData.get(name).keySet()));
	    
	    contains = client.getArchivesContaining(versions);
	    TreeSet<String> anames = new TreeSet<String>();
	    for(String name : contains.keySet()) {
	      for(VersionID vid : contains.get(name).keySet())
		anames.addAll(contains.get(name).get(vid));
	    }
	    
	    volumes = new ArrayList<ArchiveVolume>();
	    for(String aname : anames) {
	      ArchiveVolume vol = pArchiveVolumes.get(aname);
	      if(vol == null) {
		master.updatePanelOp("Loading Archive Volume: " + aname);
		vol = client.getArchive(aname);
		pArchiveVolumes.put(aname, vol);
	      }
	      
	      volumes.add(vol);
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
	
      UpdateArchivesTask task = new UpdateArchivesTask(pData, contains, volumes);
      SwingUtilities.invokeLater(task);
    }

    private TreeMap<String,TreeMap<VersionID,Long>>  pData; 
  }

  /** 
   * Update the restore versions and archive volumes tables. 
   */ 
  private
  class UpdateArchivesTask
    extends Thread
  {
    public 
    UpdateArchivesTask
    (
     TreeMap<String,TreeMap<VersionID,Long>> data,
     TreeMap<String,TreeMap<VersionID,TreeSet<String>>> contains, 
     Collection<ArchiveVolume> volumes                              
    ) 
    {
      super("JRestoreDialog:UpdateArchivesTask");

      pData     = data; 
      pContains = contains;
      pVolumes  = volumes; 
    }

    public void 
    run() 
    {
      {
	long total = 0L;
	for(String name : pData.keySet()) {
	  TreeMap<VersionID,Long> vsizes = pData.get(name);
	  for(VersionID vid : vsizes.keySet()) {
	    Long size = vsizes.get(vid);
	    if(size == null) {
	      String aname = pContains.get(name).get(vid).first();
	      ArchiveVolume vol = pArchiveVolumes.get(aname);
	      size = vol.getSize(name, vid);
	      vsizes.put(vid, size);
	    }
	    
	    total += size;
	  }
	}

	pRestoreTableModel.setData(pData);
	pRestoreSizeLabel.setText("Total Size: " + formatLong(total));
      }

      {
	TreeSet<String> chosen = pArchiveTableModel.getChosenArchives();
	pArchiveTableModel.setData(pContains, pVolumes);
	pArchiveTableModel.setChosenArchives(chosen);
	
	updateRestoreCounter();
      }
    }
    
    private TreeMap<String,TreeMap<VersionID,Long>>             pData; 
    private TreeMap<String,TreeMap<VersionID,TreeSet<String>>>  pContains;   
    private Collection<ArchiveVolume>                           pVolumes;
  }


  /*----------------------------------------------------------------------------------------*/

  private 
  class RestoreParamsTask
    extends Thread
  {
    public 
    RestoreParamsTask
    (
     int idx, 
     ArrayList<String> names,
     TreeMap<String,TreeMap<String,TreeSet<VersionID>>> versions 
    )
    {
      super("JRestoreDialog:RestoreParamsTask");

      pIndex    = idx; 
      pNames    = names;
      pVersions = versions; 
    }

    public void 
    run() 
    {  
      String aname = pNames.get(pIndex);
      pRestoreParamsDialog.updateArchiveVolume(pArchiveVolumes.get(aname));
      pRestoreParamsDialog.setVisible(true);
      if(pRestoreParamsDialog.wasConfirmed()) {
	BaseArchiver archiver = pRestoreParamsDialog.getArchiver();
	if(archiver.isManual()) {
	  JConfirmDialog diag = 
	    new JConfirmDialog("Are you ready to read (" + aname + ")?");
	  diag.setVisible(true);
	  
	  if(!diag.wasConfirmed()) {
	    StringBuffer buf = new StringBuffer();
	    buf.append("Restore operation aborted early without restoring:\n");
	      
	    int wk;
	    for(wk=pIndex; wk<pNames.size(); wk++) 
	      buf.append("  " + pNames.get(wk) + "\n");	
	    
	    UIMaster.getInstance().showErrorDialog
	      ("Warning:", buf.toString());
	    return;
	  }	      
	}
	
	String toolset = pRestoreParamsDialog.getToolset();

	RestoreTask task = new RestoreTask(pIndex, pNames, pVersions, archiver, toolset);
	task.start();
      }
      else {
	StringBuffer buf = new StringBuffer();
	buf.append("Restore operation aborted early without restoring:\n");
	
	int wk;
	for(wk=pIndex; wk<pNames.size(); wk++) 
	  buf.append("  " + pNames.get(wk) + "\n");	
	
	UIMaster.getInstance().showErrorDialog
	  ("Warning:", buf.toString());
      }
    }

    private int                                                 pIndex; 
    private ArrayList<String>                                   pNames; 
    private TreeMap<String,TreeMap<String,TreeSet<VersionID>>>  pVersions;
  }
    
  private 
  class RestoreTask
    extends Thread
  {
    public 
    RestoreTask
    (
     int idx, 
     ArrayList<String> names,
     TreeMap<String,TreeMap<String,TreeSet<VersionID>>> versions,
     BaseArchiver archiver, 
     String toolset
    )
    {
      super("JRestoreDialog:RestoreTask");

      pIndex    = idx; 
      pNames    = names;
      pVersions = versions; 
      pArchiver = archiver;
      pToolset  = toolset;
    }
    

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();

      String aname = pNames.get(pIndex);
      synchronized(pUpdateLock) {
 	if(master.beginPanelOp("Restoring from: " + aname)) { 
 	  TreeMap<String,TreeSet<VersionID>> versions = pVersions.get(aname);
	  
 	  try {
 	    client.restore(aname, versions, pArchiver, pToolset);
 	  }
 	  catch(PipelineException ex) {
 	    StringBuffer buf = new StringBuffer();
 	    buf.append(ex.getMessage() + "\n\n" + 
 		       "Restore operation aborted early without restoring:\n");
	    
 	    int wk;
 	    for(wk=pIndex; wk<pNames.size(); wk++) 
 	      buf.append("  " + pNames.get(wk) + "\n");	    
	    
 	    UIMaster.getInstance().showErrorDialog
 	      ("Error:", buf.toString());
 	    return;
 	  }
 	  finally {
 	    master.endPanelOp("Done.");
 	  }
	  
 	  RemoveTask task = new RemoveTask(versions);
 	  SwingUtilities.invokeLater(task);      
 	}
       }

       int next = pIndex+1;
       if(next < pNames.size()) {
 	RestoreParamsTask task = new RestoreParamsTask(next, pNames, pVersions); 
 	SwingUtilities.invokeLater(task);     
       }
       else {
 	GetRequestsTask task = new GetRequestsTask();
 	task.start();
       }
    }

    private int                                                 pIndex; 
    private ArrayList<String>                                   pNames; 
    private TreeMap<String,TreeMap<String,TreeSet<VersionID>>>  pVersions;
    private BaseArchiver                                        pArchiver; 
    private String                                              pToolset; 
  }


  /** 
   * Remove the given entries from the restore table.
   */ 
  private
  class RemoveTask
    extends Thread
  {
    public 
    RemoveTask
    (
     TreeMap<String,TreeSet<VersionID>> versions
    ) 
    {
      super("JRestoreDialog:RemoveTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      TreeMap<String,TreeMap<VersionID,Long>> data = pRestoreTableModel.getData();

      for(String name : pVersions.keySet()) {
	TreeMap<VersionID,Long> vsizes = data.get(name);
	if(vsizes != null) {
	  for(VersionID vid : pVersions.get(name)) 
	    vsizes.remove(vid);

	  if(vsizes.isEmpty()) 
	    data.remove(name);
	}
      }

      ArchivesTask task = new ArchivesTask(data);
      task.start();
    }

    private TreeMap<String,TreeSet<VersionID>> pVersions; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 398897653863238367L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 
  
  /**
   * A cache of archive volumes indexed by volume name.
   */ 
  private TreeMap<String,ArchiveVolume>  pArchiveVolumes; 

  /**
   * A lock used to synchronize communication with the plmaster(1).
   */ 
  private Object  pUpdateLock; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The restore requests table model.
   */
  private RestoreRequestTableModel  pRequestTableModel; 
 
  /**
   *  The restore requests table.
   */
  private JTablePanel  pRequestTablePanel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The restore version table model.
   */
  private NodeVersionSizeTableModel  pRestoreTableModel; 
 
  /**
   *  The restore version table.
   */
  private JTablePanel  pRestoreTablePanel; 

  /**
   * The label which displays the total size of all files to be restored.
   */ 
  private JLabel  pRestoreSizeLabel; 
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive volume table model.
   */
  private ArchiveVolumeTableModel  pArchiveTableModel; 
 
  /**
   *  The archive volume table.
   */
  private JTablePanel  pArchiveTablePanel; 

  /**
   * The label which displays the number of checked-in versions selected for restore 
   * based on the current archive selection.
   */ 
  private JLabel  pRestoreCountLabel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The operation buttons.
   */ 
  private JButton  pRestoreButton; 
  private JButton  pDenyButton; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The archiver parameters dialog.
   */ 
  private JRestoreParamsDialog  pRestoreParamsDialog; 

}
