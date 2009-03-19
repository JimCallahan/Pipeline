// $Id: JArchiveDialog.java,v 1.14 2009/03/19 21:55:59 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for selecting the checked-in versions to archive.
 */ 
public 
class JArchiveDialog
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
  JArchiveDialog() 
  {
    super("Archive Tool");

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
	  ArchiveCandidateTableModel model = new ArchiveCandidateTableModel();
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
	    JButton btn = 
              UIFactory.createDialogButton
                ("Search...", "candidate-search", this, 
                 "Search for new candidate checked-in versions to archive.");
            
	    hbox.add(btn);
	  }

	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
			      
	  {
	    JButton btn = 
              UIFactory.createDialogButton
                ("Clear", "clear-candidate", this, 
                 "Clear the displayed candidate checked-in versions.");

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
	  
	  apanel.add(box);
	}
	
	apanel.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  NodeVersionSizeTableModel model = new NodeVersionSizeTableModel(880);
	  pArchiveTableModel = model;
	  
	  JTablePanel tpanel = new JTablePanel(model);
	  pArchiveTablePanel = tpanel;
	  
	  apanel.add(tpanel);
	}	
	
	apanel.add(Box.createRigidArea(new Dimension(0, 5)));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  hbox.add(Box.createHorizontalGlue());
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = 
              UIFactory.createDialogButton
                ("Add", "add-archive", this, 
                 "Add the selected candidate versions to the list of versions to be " + 
                 "archived.");

	    hbox.add(btn);
	  }
	    
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn =
              UIFactory.createDialogButton
                ("Add All", "add-all-archive", this, 
                 "Add all candidate versions to the list of versions to be archived.");

	    hbox.add(btn);
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
	  
	  {
	    JButton btn = 
              UIFactory.createDialogButton
                ("Remove", "remove-archive", this, 
                 "Remove the selected versions from the list of versions to be archived.");

	    hbox.add(btn);
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	  {
	    JButton btn = 
              UIFactory.createDialogButton
                ("Clear", "remove-all-archive", this, 
                 "Clear the list of versions to be archived.");

	    hbox.add(btn);	  
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(20, 0)));
	  
	  {
	    JButton btn =
              UIFactory.createDialogButton
                ("Calc Sizes", "calc-archive", this, 
                 "Calculate the amount of disk space needed to archive the files " +
                 "associated with the checked-in versions.");

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
	{ "Archive...", "archive" }
      };

      JButton btns[] = super.initUI("Archive Tool:", body, null, null, extra, "Close");

      pArchiveButton = btns[0];
      pArchiveButton.setEnabled(false);

      updatePanel();
      pack();
    }

    pQueryDialog         = new JArchiveQueryDialog(this);
    pArchiveParamsDialog = new JArchiveParamsDialog(this);
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
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      pPrivilegeDetails = client.getPrivilegeDetails();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
     master.releaseMasterMgrClient(client); 
    }

    updateButtons();
  }

  /**
   * Update the enabled state of the dialog buttons.
   */ 
  private void 
  updateButtons() 
  {
    pArchiveButton.setEnabled
      (pPrivilegeDetails.isMasterAdmin() && !pArchiveTableModel.getData().isEmpty());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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

    else if(cmd.equals("archive"))
      doArchive();

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
	new SearchTask(pQueryDialog.getPattern(), pQueryDialog.getMaxArchives());
      task.start();
    }
  }
  
  /**
   * Clear the candidate checked-in versions.
   */ 
  private void 
  doClearCandidate() 
  {
    pCandidateTableModel.setArchiveInfo(null);
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
      pArchiveParamsDialog.setVisible(true);
      if(pArchiveParamsDialog.wasConfirmed()) {
	String prefix = pArchiveParamsDialog.getPrefix();
	if((prefix == null) || (prefix.length() == 0)) 
	  prefix = "Archive";

	Long minSize = pArchiveParamsDialog.getMinSize();
	if(minSize == null)
	  minSize = 0L;

	String toolset = pArchiveParamsDialog.getToolset();
	BaseArchiver archiver = pArchiveParamsDialog.getArchiver();

	if((toolset != null) && (archiver != null)) {
	  AssignVersionsToArchivesTask task = 
	    new AssignVersionsToArchivesTask
	      (this, prefix, minSize, versions, toolset, archiver);
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
     Integer maxArchives 
    )     
    {
      super("JArchiveDialog:SearchTask");

      pPattern       = pattern;	        
      pMaxArchives   = maxArchives;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      ArrayList<ArchiveInfo> info = null;
      
      MasterMgrClient client = master.acquireMasterMgrClient();
      if(master.beginPanelOp("Searching for Candidate Versions...")) {
	try {
	  info = client.archiveQuery(pPattern, pMaxArchives);
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(info);
      SwingUtilities.invokeLater(task);
    }

    private String   pPattern;
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
     ArrayList<ArchiveInfo> info
    ) 
    {
      super("JArchiveDialog:UpdateTask");
      pInfo = info;
    }

    @Override
    public void 
    run() 
    {
      pCandidateTableModel.setArchiveInfo(pInfo);
    }
    
    private ArrayList<ArchiveInfo> pInfo; 
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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        TreeMap<String,TreeMap<VersionID,Long>> data = null;
        if(master.beginPanelOp("Calculating File Sizes...")) {
          try {
            data = client.getArchivedSizes(pVersions);
          }
          catch(PipelineException ex) {
            showErrorDialog(ex);
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

          UpdateSizesTask task = new UpdateSizesTask(data);
          SwingUtilities.invokeLater(task);
        }
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    private TreeMap<String,TreeMap<VersionID,Long>>  pData;
    private TreeMap<String,TreeSet<VersionID>>       pVersions;
  }

  /** 
   * Update the archive table. 
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
      super("JArchiveDialog:UpdateSizesTask");
      pData = data;
    }

    @Override
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
   * Calculate the sizes of files to archive and assign versions to archive volumes.
   */ 
  private
  class AssignVersionsToArchivesTask
    extends Thread
  {
    public 
    AssignVersionsToArchivesTask
    (
     JArchiveDialog parent, 
     String prefix,
     long minSize, 
     TreeMap<String,TreeSet<VersionID>> versions, 
     String toolset, 
     BaseArchiver archiver
    )     
    {
      super("JArchiveDialog:AssignVersionsToArchivesTask");
      
      pParent   = parent;
      pPrefix   = prefix; 
      pMinSize  = minSize; 
      pVersions = versions;
      pToolset  = toolset; 
      pArchiver = archiver;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        TreeMap<String,TreeMap<VersionID,Long>> versionSizes = null;
        if(master.beginPanelOp("Assigning Versions to Archives...")) {
          try {
      	    versionSizes = client.getArchivedSizes(pVersions);
      	  }
          catch(PipelineException ex) {
            showErrorDialog(ex);
          }
          finally {
            master.endPanelOp("Done.");
          }
        }
  	
        /* assign the maximum number of versions to each archive volume without 
  	 exceeding its capacity */ 
        TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>> archives = 
  	new TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>>(); 
        if(versionSizes != null) {
  	long capacity = pArchiver.getCapacity();
  	int idx = 0;
  	long total = 0L;
  	boolean done = false;
  	TreeMap<String,TreeMap<VersionID,Long>> skippedVersionSizes = 
  	  new TreeMap<String,TreeMap<VersionID,Long>>();
  	while(!done) {
  	  for(String name : versionSizes.keySet()) {
  	    TreeMap<VersionID,Long> sizes = versionSizes.get(name);
  	    for(VersionID vid : sizes.keySet()) {
  	      Long size = sizes.get(vid);
  	      
  	      if((total+size) >= capacity) {
  		/* the version is too big to fit by itself in a volume */ 
  		if(total == 0L) {
  		  showErrorDialog
  		    ("Error:", 
  		     "The version (" + vid + ") of node (" + name + ") was larger than " + 
  		     "the capacity of an entire archive volume!  The capacity of the " + 
  		     "archive volume must be increased to at least " + 
  		     "(" + formatLong(size) + ") in order to archive this version.");
  		  return;
  		}
  		
  		/* the version is too big for this volume, skip it for now... */ 
  		TreeMap<VersionID,Long> skippedSizes = skippedVersionSizes.get(name);
  		if(skippedSizes == null) {
  		  skippedSizes = new TreeMap<VersionID,Long>();
  		  skippedVersionSizes.put(name, skippedSizes);
  		}
  		skippedSizes.put(vid, size);
  	      }
  
  	      /* the version fits, add it to this volume */ 
  	      else {
  		TreeMap<String,TreeSet<VersionID>> versions = archives.get(idx);
  		if(versions == null) {
  		  versions = new TreeMap<String,TreeSet<VersionID>>();
  		  archives.put(idx, versions);
  		}
  		
  		TreeSet<VersionID> vids = versions.get(name);
  		if(vids == null) {
  		  vids = new TreeSet<VersionID>();
  		  versions.put(name, vids);
  		}
  		
  		vids.add(vid);
  		total += size;
  	      }
  	    }
  	  }
  	  
  	  /* some versions wouldn't fit in the current volume, 
  	       create a new volume and try again... */ 
  	  if(!skippedVersionSizes.isEmpty()) {
  	    idx++;
  	    total = 0L;
  	    versionSizes = skippedVersionSizes;
  	    skippedVersionSizes = new TreeMap<String,TreeMap<VersionID,Long>>();
  	  }
  	  else {
  	    if(total < pMinSize) {
  	      if(idx == 0) {
  		showErrorDialog
  		  ("Error:", 
  		   "The total size (" + formatLong(total) + ") of all versions selected " + 
  		   "for archiving was less than the minimum archive volume size " + 
  		   "(" + formatLong(pMinSize) + ")!  Either select enough versions to " + 
  		   "meet this minimum size or specify a smaller minimum size to create " + 
  		   "an archive volume.");
  		return;
  	      }
  	      else {
  		archives.remove(idx);
  	      }
  	    }
  
  	    break;
  	  }
  	}
        }
  
        /* perform the archive operations */ 
        if(!archives.isEmpty()) {
          if(pArchiver.isManual()) {
      	    ManualArchiveConfirmTask task = 
      	      new ManualArchiveConfirmTask
      	        (pParent, null, 0, pPrefix, archives, pToolset, pArchiver);
      	    SwingUtilities.invokeLater(task);
          }
          else {  
            if(master.beginPanelOp()) {
              int lastIdx = 0;
              try {
                for(Integer idx : archives.keySet()) {
                  master.updatePanelOp
                    ("Archiving Volume (" + (idx+1) + " of " + archives.size() + ")...");
                  lastIdx = idx;
                  client.archive(pPrefix, archives.get(idx), pArchiver, pToolset);
                }
              }
              catch(PipelineException ex) {
                showErrorDialog
                  ("Error:", 
      		   ex.getMessage() + "\n\n" + 
      		   "Archive operation aborted early without creating " + 
      		   "(" + (archives.size()-lastIdx) + " of " + archives.size() + ") archive " +
      		   "volumes!");
                return;
      	    }
      	    finally {
      	      master.endPanelOp("Done.");
      	    }
      
      	    RemoveAllTask task = new RemoveAllTask();
      	    SwingUtilities.invokeLater(task);      
      	  } //if(master.beginPanelOp()) {
      	} //else
        } //if(!archives.isEmpty()) {
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    private JArchiveDialog  pParent; 
    private String          pPrefix;
    private long            pMinSize; 

    private TreeMap<String,TreeSet<VersionID>>  pVersions;

    private String          pToolset; 
    private BaseArchiver    pArchiver; 
  }

  /** 
   * Ask the user if they are ready to write the archive.
   */ 
  private
  class ManualArchiveConfirmTask
    extends Thread
  {
    public 
    ManualArchiveConfirmTask
    (
     JArchiveDialog parent, 
     String lastArchiveName, 
     int idx, 
     String prefix,
     TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>> archives, 
     String toolset, 
     BaseArchiver archiver
    )     
    {
      super("JArchiveDialog:ManualArchiveConfirmTask");

      pParent = parent;
      
      pLastArchiveName = lastArchiveName; 

      pIndex    = idx; 
      pPrefix   = prefix; 
      pArchives = archives; 
      pToolset  = toolset; 
      pArchiver = archiver;
    }

    @Override
    public void 
    run() 
    {
      if(pLastArchiveName != null) {
	JMessageDialog diag = 
	  new JMessageDialog(pParent, "Created: " + pLastArchiveName);
	diag.setVisible(true);
      }

      if(pArchives.get(pIndex) == null) 
	return;

      JConfirmDialog diag = 
	new JConfirmDialog(pParent, "Are you ready to write the next archive volume " + 
			   "(" + (pIndex+1) + " of " + pArchives.size() + ")?");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	ManualArchiveTask task = 
	  new ManualArchiveTask(pParent, pIndex, pPrefix, pArchives, pToolset, pArchiver);
	task.start();
      }
      else {
	showErrorDialog
	  ("Warning:", 
	   "Archive operation aborted early without creating " + 
	   "(" + (pArchives.size()-pIndex) + " of " + pArchives.size() + ") archive " +
	   "volumes!");
      }      
    }

    private JArchiveDialog  pParent; 
    private String          pLastArchiveName; 
    private int             pIndex; 
    private String          pPrefix;

    private TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>>  pArchives; 

    private String          pToolset;
    private BaseArchiver    pArchiver; 
  }

  /** 
   * Write one archive volume and then query the user again before writing the next volume.
   */ 
  private
  class ManualArchiveTask
    extends Thread
  {
    public 
    ManualArchiveTask
    (
     JArchiveDialog parent, 
     int idx, 
     String prefix,
     TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>> archives, 
     String toolset, 
     BaseArchiver archiver
    )     
    {
      super("JArchiveDialog:ManualArchiveTask");
      
      pParent   = parent;
      pIndex    = idx; 
      pPrefix   = prefix; 
      pArchives = archives; 
      pToolset  = toolset; 
      pArchiver = archiver;
    }

    @Override
    public void 
    run() 
    {  
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        String msg = ("Archiving Volume (" + (pIndex+1) + " of " + pArchives.size() + ")...");
        String archiveName = null;
        if(master.beginPanelOp(msg)) {
          TreeMap<String,TreeSet<VersionID>> versions = pArchives.get(pIndex);

          try {
            archiveName = client.archive(pPrefix, versions, pArchiver, pToolset);
          }
          catch(PipelineException ex) {
            showErrorDialog
              ("Error:", 
               ex.getMessage() + "\n\n" + 
               "Archive operation aborted early without creating " + 
               "(" + (pArchives.size()-pIndex) + " of " + pArchives.size() + ") archive " +
               "volumes!");
            return;
          }
          finally {
            master.endPanelOp("Done.");
          }

          RemoveTask task = new RemoveTask(versions);
          SwingUtilities.invokeLater(task);      
        }
  
        ManualArchiveConfirmTask task = 
  	new ManualArchiveConfirmTask
  	  (pParent, archiveName, pIndex+1, pPrefix, pArchives, pToolset, pArchiver);
        SwingUtilities.invokeLater(task); 
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    private JArchiveDialog  pParent; 
    private int             pIndex; 
    private String          pPrefix;

    private TreeMap<Integer,TreeMap<String,TreeSet<VersionID>>>  pArchives; 

    private String          pToolset; 
    private BaseArchiver    pArchiver; 
  }

  /** 
   * Remove the given entries from the archive table.
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
      super("JArchiveDialog:RemoveTask");
      pVersions = versions;
    }

    @Override
    public void 
    run() 
    {
      TreeMap<String,TreeMap<VersionID,Long>> data = pArchiveTableModel.getData();

      for(String name : pVersions.keySet()) {
	TreeMap<VersionID,Long> vsizes = data.get(name);
	if(vsizes != null) {
	  for(VersionID vid : pVersions.get(name)) 
	    vsizes.remove(vid);

	  if(vsizes.isEmpty()) 
	    data.remove(name);
	}
      }

      pArchiveTableModel.setData(data);
    }

    private TreeMap<String,TreeSet<VersionID>> pVersions; 
  }

  /** 
   * Remove all entries from the archive table.
   */ 
  private
  class RemoveAllTask
    extends Thread
  {
    public 
    RemoveAllTask() 
    {
      super("JArchiveDialog:RemoveAllTask");
    }

    @Override
    public void 
    run() 
    {
      doRemoveAllArchive();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5667924780004601771L;

  

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
  private ArchiveCandidateTableModel  pCandidateTableModel;

  /**
   * The candidate version table.
   */ 
  private JTablePanel  pCandidateTablePanel;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive version table model.
   */ 
  private NodeVersionSizeTableModel  pArchiveTableModel;

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
   * The archive button.
   */ 
  private JButton  pArchiveButton;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The archival query parameters dialog.
   */
  private JArchiveQueryDialog  pQueryDialog;

  /**
   * The archiver parameters dialog.
   */ 
  private JArchiveParamsDialog  pArchiveParamsDialog; 
}
