// $Id: JArchiveDialog.java,v 1.18 2010/01/08 20:42:25 jesse Exp $

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

      JButton btns[] = super.initUI("Archive Tool:", body, null, null, extra, "Close", null);

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
    DoubleMap<String,VersionID,Long> data = pArchiveTableModel.getData();
    for(String name : selected.keySet()) {
      for(VersionID vid : selected.get(name)) 
        data.put(name, vid, null);
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
    DoubleMap<String,VersionID,Long> data = null; 
    MappedSet<String,VersionID> versions  = null;
    {
      data = pArchiveTableModel.getData();

      versions = new MappedSet<String,VersionID>();
      for(String name : data.keySet()) {
        for(VersionID vid : data.keySet(name)) 
          versions.put(name, vid);
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
    DoubleMap<String,VersionID,Long> data = pArchiveTableModel.getData();
    MappedSet<String,VersionID> versions = new MappedSet<String,VersionID>();
    for(String name : data.keySet()) {
      for(VersionID vid : data.keySet(name)) 
        versions.put(name, vid);
    }

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
      long opID = master.beginDialogOp("Searching for Candidate Versions...");
      long monitorID = client.addMonitor(new DialogOpMonitor(opID));
      try {
        info = client.archiveQuery(pPattern, pMaxArchives);
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.endDialogOp(opID, "Archive Search Complete.");
        client.removeMonitor(monitorID); 
        master.releaseMasterMgrClient(client);
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
     DoubleMap<String,VersionID,Long> data,
     MappedSet<String,VersionID> versions
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
      long monitorID = -1L;
      try {
        DoubleMap<String,VersionID,Long> data = null;
        long opID = master.beginDialogOp("Calculating File Sizes...");
        monitorID = client.addMonitor(new DialogOpMonitor(opID));
        try {
          data = client.getArchivedSizes(pVersions);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);
        }
        finally {
          master.endDialogOp(opID, "File Sizes Calculated.");
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
        client.removeMonitor(monitorID); 
        master.releaseMasterMgrClient(client);
      }
    }

    private DoubleMap<String,VersionID,Long> pData;
    private MappedSet<String,VersionID>      pVersions;
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
     DoubleMap<String,VersionID,Long> data
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
    
    private DoubleMap<String,VersionID,Long> pData;
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
     MappedSet<String,VersionID> versions, 
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
        DoubleMap<String,VersionID,Long> versionSizes = null;
        {
          long opID = master.beginDialogOp("Assigning Versions to Archives...");
          long monitorID = client.addMonitor(new DialogOpMonitor(opID));
          try {
            versionSizes = client.getArchivedSizes(pVersions);
          }
          catch(PipelineException ex) {
            showErrorDialog(ex);
          }
          finally {
            master.endDialogOp(opID, "Versions Assigned.");
            client.removeMonitor(monitorID); 
          }
        }
  	
        /* assign the maximum number of versions to each archive volume without 
  	     exceeding its capacity */ 
        TreeMap<Integer,MappedSet<String,VersionID>> archives = 
          new TreeMap<Integer,MappedSet<String,VersionID>>();
        if(versionSizes != null) {
          long capacity = pArchiver.getCapacity();
          int idx = 0;
          long total = 0L;
          boolean done = false;
          DoubleMap<String,VersionID,Long> skippedVersionSizes = 
            new DoubleMap<String,VersionID,Long>();
          while(!done) {
            for(String name : versionSizes.keySet()) {
              for(VersionID vid : versionSizes.keySet(name)) {
                Long size = versionSizes.get(name, vid);                
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

                  skippedVersionSizes.put(name, vid, size);
                }
                
                /* the version fits, add it to this volume */ 
                else {
                  MappedSet<String,VersionID> versions = archives.get(idx); 
                  if(versions == null) {
                    versions = new MappedSet<String,VersionID>();
                    archives.put(idx, versions);
                  }

                  versions.put(name, vid);
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
              skippedVersionSizes = new DoubleMap<String,VersionID,Long>();
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
            long opID = master.beginDialogOp();
            long monitorID = client.addMonitor(new DialogOpMonitor(opID));
            int lastIdx = 0;
            try {
              for(Integer idx : archives.keySet()) {
                master.updateDialogOp
                  (opID, "Archiving Volume (" + (idx+1) + " of " + archives.size() + ")...");
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
              master.endDialogOp(opID, "Archived.");
              client.removeMonitor(monitorID); 
            }
              
            RemoveAllTask task = new RemoveAllTask();
            SwingUtilities.invokeLater(task);      
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

    private MappedSet<String,VersionID>  pVersions;

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
     TreeMap<Integer,MappedSet<String,VersionID>> archives, 
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

    private TreeMap<Integer,MappedSet<String,VersionID>>  pArchives; 

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
     TreeMap<Integer,MappedSet<String,VersionID>> archives, 
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
        MappedSet<String,VersionID> versions = pArchives.get(pIndex);
        String archiveName = null;
        String msg = ("Archiving Volume (" + (pIndex+1) + " of " + pArchives.size() + ")...");
        long opID = master.beginDialogOp(msg); 
        long monitorID = client.addMonitor(new DialogOpMonitor(opID));
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
          master.endDialogOp(opID, "Archived.");
          client.removeMonitor(monitorID); 
        }

        SwingUtilities.invokeLater(new RemoveTask(versions));
  
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

    private TreeMap<Integer,MappedSet<String,VersionID>>  pArchives; 

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
      MappedSet<String,VersionID> versions
    ) 
    {
      super("JArchiveDialog:RemoveTask");
      pVersions = versions;
    }

    @Override
    public void 
    run() 
    {
      DoubleMap<String,VersionID,Long> data = pArchiveTableModel.getData();
      for(String name : pVersions.keySet()) {
        for(VersionID vid : pVersions.get(name)) 
          data.remove(name, vid);
      }

      pArchiveTableModel.setData(data);
    }

    private MappedSet<String,VersionID> pVersions; 
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
