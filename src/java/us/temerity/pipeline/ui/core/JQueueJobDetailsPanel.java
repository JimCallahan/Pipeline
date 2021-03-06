// $Id: JQueueJobDetailsPanel.java,v 1.22 2009/12/19 21:14:28 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   D E T A I L S   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the details of a job. <P> 
 */ 
public  
class JQueueJobDetailsPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ComponentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JQueueJobDetailsPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobDetailsPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    pLinkActionParamNodeNames = new ArrayList<String>();
    pLinkActionParamValues = new ArrayList<String>();
    
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = new JPanel();	

	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	{
	  JLabel label = new JLabel();
	  pHeaderIcon = label;
	  
	  panel.add(label);	  
	}
	
	panel.add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());
      
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full target node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  field.setFocusable(true);
	  field.addKeyListener(this);
	  field.addMouseListener(this); 

	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	add(hbox);
      }
	
      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);

	/* summary panel */ 
	{
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* job state */ 
	    pJobStateField = UIFactory.createTitledTextField
	      (tpanel, "Job State:", sTSize, 
	       vpanel, "-", sVSize, 
	       "The current execution state of the job.");
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* time waiting */ 
	    pWaitingField = UIFactory.createTitledTextField
	      (tpanel, "Time Waiting:", sTSize, 
	       vpanel, "-", sVSize, 
	       "How long the job has been waiting to execute.");

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* time running */ 
	    pRunningField = UIFactory.createTitledTextField
	      (tpanel, "Time Running:", sTSize, 
	       vpanel, "-", sVSize, 
	       "How long the job has been running so far.");

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	    
	    /* submitted stamp */ 
	    pSubmittedField = UIFactory.createTitledTextField
	      (tpanel, "Submitted:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job was submitted for execution.");
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* started stamp */ 
	    pStartedField = UIFactory.createTitledTextField
	      (tpanel, "Started:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job began execution.");
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* completed stamp */ 
	    pCompletedField = UIFactory.createTitledTextField
	      (tpanel, "Completed:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job completed execution.");	    
	  }
	  
	  JDrawer drawer = new JDrawer("Summary:", (JComponent) comps[2], true);
	  drawer.setToolTipText(UIFactory.formatToolTip("Summary of current job status."));
	  pSummaryDrawer = drawer;
	  vbox.add(drawer);
	}

	/* process details */ 
	{ 
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* execution details */ 
	    {
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Execution Details:", sTSize, JLabel.RIGHT, 
		   "The job process working directory, command-line and environment.");
					    
		tpanel.add(label);
	      }
		
	      {
		JButton btn = new JButton("Show...");
		pExecDetailsButton = btn;

		btn.setAlignmentX(0.5f);
		
		btn.setName("ValuePanelButton");
		btn.setRolloverEnabled(false);
		btn.setFocusable(false);
		
		btn.addActionListener(this);
		btn.setActionCommand("show-exec-details");
		
		Dimension size = new Dimension(sVSize, 19);
		btn.setMinimumSize(size);
		btn.setPreferredSize(size);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
		
		vpanel.add(btn);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* hostname */ 
	    { 
	      pHostnameField = UIFactory.createTitledTextField
		(tpanel, "Hostname:", sTSize, 
		 vpanel, null, sVSize, 
		 "The fully resolved name of the host where the job was executed.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* operating system type */ 
	    { 
	      pOsTypeField = UIFactory.createTitledTextField
		(tpanel, "Operating System:", sTSize, 
		 vpanel, null, sVSize, 
		 "The operating system type of the host where the job was executed.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* exit code */ 
	    { 
	      pExitCodeField = UIFactory.createTitledTextField
		(tpanel, "Exit Code:", sTSize, 
		 vpanel, null, sVSize, 
		 "The job process exit code.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* logs */ 
	    {
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Logs:", sTSize, JLabel.RIGHT, 
		   "The job process STDOUT and STDERR output.");
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JButton btn = new JButton("Output...");
		  pOutputButton = btn;
		  
		  btn.setName("ValuePanelButton");
		  btn.setRolloverEnabled(false);
		  btn.setFocusable(false);
		  
		  btn.addActionListener(this);
		  btn.setActionCommand("show-output");
		  
		  Dimension size = new Dimension(sHSize, 19);
		  btn.setMinimumSize(size);
		  btn.setPreferredSize(size);
		  btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
		  
		  hbox.add(btn);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(6, 0)));
		
		{
		  JButton btn = new JButton("Errors...");
		  pErrorButton = btn;
		  
		  btn.setName("ValuePanelButton");
		  btn.setRolloverEnabled(false);
		  btn.setFocusable(false);
		  
		  btn.addActionListener(this);
		  btn.setActionCommand("show-error");
		  
		  Dimension size = new Dimension(sHSize, 19);
		  btn.setMinimumSize(size);
		  btn.setPreferredSize(size);
		  btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
		  
		  hbox.add(btn);
		}
		
		vpanel.add(hbox);
	      }		  
	    }	    

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	      
	    /* user time */ 
	    {
	      pUserTimeField = UIFactory.createTitledTextField
		(tpanel, "User Time:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The user space execution time of the job process.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* system time */ 
	    {
	      pSystemTimeField = UIFactory.createTitledTextField
		(tpanel, "System Time:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The kernel space execution time of the job process.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* resident memory */ 
	    {
	      pResidentField = UIFactory.createTitledTextField
		(tpanel, "Resident Memory:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The maximum amount of resident memory used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* virtual memory */ 
	    {
	      pVirtualField = UIFactory.createTitledTextField
		(tpanel, "Virtual Memory:", sTSize, 
		 vpanel, "-", sVSize,  
		 "The maximum amount of virtual memory used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* swapped memory */ 
	    {
	      pSwappedField = UIFactory.createTitledTextField
		(tpanel, "Swapped Memory:", sTSize, 
		 vpanel, "-", sVSize,  
		 "The maximum amount of swap space used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* page faults */ 
	    {
	      pPageFaultsField = UIFactory.createTitledTextField
		(tpanel, "Page Faults:", sTSize, 
		 vpanel, null, sVSize, 
		 "The number of major memory page faults by the job process.");
	      pPageFaultsField.setEditable(false);
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Process Details:", (JComponent) comps[2], false);
	  drawer.setToolTipText(UIFactory.formatToolTip("Job process execution details."));
	  pProcessDrawer = drawer;
	  vbox.add(drawer);
	} 
	
	/* job requirements */ 
	{ 
	  Box jrbox = new Box(BoxLayout.Y_AXIS);
	  
	  {
	    Component comps[] = createCommonPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      /* priority */ 
	      {
		pPriorityField = UIFactory.createTitledIntegerField
		  (tpanel, "Priority:", sTSize, 
		   vpanel, null, sVSize, 
		   "The relative priority of this job.");
		pPriorityField.setEditable(false);
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	      
	      /* ramp-up interval */ 
	      {
		pRampUpField = UIFactory.createTitledIntegerField
		  (tpanel, "Ramp Up Interval:", sTSize, 
		   vpanel, null, sVSize, 
		   "The time interval (in seconds) to wait before scheduling " + 
		   "new jobs to the server running the job.");
		pRampUpField.setEditable(false);
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* maximum load */ 
	      { 
		pMaxLoadField = UIFactory.createTitledFloatField
		  (tpanel, "Maximum Load:", sTSize, 
		   vpanel, null, sVSize, 
		   "The maximum system load allowed on an eligable host.");
		pMaxLoadField.setEditable(false);
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum memory */ 
	      { 
		pMinMemoryField = UIFactory.createTitledByteSizeField
		  (tpanel, "Minimum Memory:", sTSize, 
		   vpanel, null, sVSize, 
		   "The minimum amount of free memory required on an eligible host.");
		pMinMemoryField.setEditable(false);
	      }

	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum disk */ 
	      { 
		pMinDiskField = UIFactory.createTitledByteSizeField
		  (tpanel, "Minimum Disk:", sTSize, 
		   vpanel, null, sVSize, 
		   "The minimum amount of free temporary local disk space required " +
		   "on an eligible host.");
		pMinDiskField.setEditable(false);
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	      
	      /* key state */
	      {
                pKeyStateField = UIFactory.createTitledTextField
                  (tpanel, "Key State:", sTSize, 
                   vpanel, null, sVSize, 
                   "Whether key choosers need to be run to update the keys on this job.");
	        
	      }
	    }
	    
	    jrbox.add(comps[2]);
	  }
	    
	  {
	    Box kbox = new Box(BoxLayout.X_AXIS);
	    pKeysBox = kbox;
	  
	    kbox.addComponentListener(this);
            kbox.add(UIFactory.createSidebar());
	
	    {
	      Box dbox = new Box(BoxLayout.Y_AXIS);
	      
	      /* selection keys */ 
	      {
		JDrawer drawer = new JDrawer("Selection Keys:",  new JPanel(), false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of selection keys a server must have in order to be eligable " + 
		   "to run jobs associated with this node."));
		pSelectionDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      /* hardware keys */ 
	      {
		JDrawer drawer = new JDrawer("Hardware Keys:",  new JPanel(), false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of hardware keys a server must have in order to be eligable " + 
		   "to run jobs associated with this node."));
		pHardwareDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      /* license keys */ 
	      {
		JDrawer drawer = new JDrawer("License Keys:", new JPanel(), false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of license keys which are required in order to run jobs " + 
		   "associated with this node."));
		pLicenseDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      kbox.add(dbox);
	    }

	    jrbox.add(kbox);
	  }

	  JDrawer drawer = new JDrawer("Job Requirements:", jrbox, false);
	  drawer.setToolTipText(UIFactory.formatToolTip
	    ("The requirements that a server must meet in order to be eligable " +
	     "to run jobs associated with this node."));
	  pJobReqsDrawer = drawer;
	  vbox.add(drawer);
	}
	
	/* action information */
	{
	  Box actbox = new Box(BoxLayout.Y_AXIS);
	  pActionBox = actbox;
	  
	  {
	    Component comps[] = createCommonPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      /* Action Name */
	      {
		pActionNameField = UIFactory.createTitledTextField
		  (tpanel, "Action:", sTSize, 
		   vpanel, "-", sVSize, 
		   "The name of the Action plugin used to regenerate the job's files.");
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* Action Version */
	      {
		pActionVersionField = UIFactory.createTitledTextField
		  (tpanel, "Version:", sTSize, 
		   vpanel, "-", sVSize, 
		   "The revision number of the Action plugin.");
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* Action Vendor */
	      {
		pActionVendorField = UIFactory.createTitledTextField
		  (tpanel, "Vendor:", sTSize, 
		   vpanel, "-", sVSize, 
		   "The name of the vendor of the Action plugin.");
	      }
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* Action OS Support */
	      {
		pActionOsSupportField = UIFactory.createTitledOsSupportField
		  (tpanel, "OS Support:", sTSize, 
		   vpanel, sVSize, 
		   "The operating system types supported by the Action plugin.");
	      }
	    }
	    actbox.add(comps[2]);
	  }
	  {
	    Box apbox = new Box(BoxLayout.Y_AXIS);
	    pActionParamsBox = apbox;

	    actbox.add(apbox);
	  }
	  
	  JDrawer drawer = new JDrawer("Action Information:", actbox, false);
	  drawer.setToolTipText(UIFactory.formatToolTip
	    ("Information about the Action that is associated with the job."));
	  pActionDrawer = drawer;
	  vbox.add(drawer);
	}

	/* files panel */ 
	{
	  Box fbox = new Box(BoxLayout.X_AXIS);
	  pFilesBox = fbox;
	  
	  fbox.addComponentListener(this);
	  
	  fbox.add(UIFactory.createSidebar());
	  
	  {
	    Box dbox = new Box(BoxLayout.Y_AXIS);

	    /* target files panel */ 
	    {
	      JDrawer drawer = new JDrawer("Targets:", new JPanel(), true);
	      drawer.setToolTipText(UIFactory.formatToolTip
		("The target files created by the job."));
	      pTargetFilesDrawer = drawer;
	      dbox.add(drawer);
	    }
	    
	    /* source files panel */ 
	    {
	      JDrawer drawer = new JDrawer("Sources:", new JPanel(), true);
	      drawer.setToolTipText(UIFactory.formatToolTip
		("The source files used by the job."));
	      pSourceFilesDrawer = drawer;
	      dbox.add(drawer);
	    }

	    fbox.add(dbox);
	  }

	  JDrawer drawer = new JDrawer("File Sequences:", fbox, false);
	  drawer.setToolTipText(UIFactory.formatToolTip
	    ("The file sequences associated with the job."));
	  pFilesDrawer = drawer;
	  vbox.add(drawer);
	}

        vbox.add(UIFactory.createFiller(sTSize+sVSize+30));
	vbox.add(Box.createVerticalGlue());

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  add(scroll);
	}
      }

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateJob(null, null, null, null, null, null);
  }

  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  private Component[]
  createCommonPanels()
  {
    Component comps[] = UIFactory.createTitledPanels();

    {
      JPanel panel = (JPanel) comps[0];
      panel.setFocusable(true);
      panel.addKeyListener(this);
      panel.addMouseListener(this); 
    }

    return comps;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Job Details";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JQueueJobDetailsPanel> panels = master.getQueueJobDetailsPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JQueueJobDetailsPanel> panels =
      UIMaster.getInstance().getQueueJobDetailsPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isQueueManaged(pAuthor));
  }
  
  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    updatePanels();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels() 
  {
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   * 
   * @param licenseKeys
   *   The current license keys.
   * 
   * @param selectionKeys
   *   The current selection keys.
   *   
   * @param hardwareKeys
   *   The current hardware keys.
   *   
   * @param chooserUpdateTime
   *   The key chooser update time that is used to determine if the job's Key State is Stale.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   QueueJob job,
   QueueJobInfo info, 
   ArrayList<LicenseKey> licenseKeys, 
   ArrayList<SelectionKey> selectionKeys,
   ArrayList<HardwareKey> hardwareKeys,
   Long chooserUpdateTime
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateJob(job, info, licenseKeys, selectionKeys, hardwareKeys, chooserUpdateTime);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current job. 
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   * 
   * @param licenseKeys
   *   The current license keys.
   * 
   * @param selectionKeys
   *   The current selection keys.
   *   
   * @param hardwareKeys
   *   The current hardware keys.
   *   
   * @param chooserUpdateTime
   *    The key chooser update time that is used to determine if the job's Key State is Stale.
   */
  private synchronized void 
  updateJob
  (
   QueueJob job,
   QueueJobInfo info, 
   ArrayList<LicenseKey> licenseKeys, 
   ArrayList<SelectionKey> selectionKeys,
   ArrayList<HardwareKey> hardwareKeys,
   Long chooserUpdateTime
  ) 
  {
    updatePrivileges();

    UserPrefs prefs = UserPrefs.getInstance();

    pJob           = job;
    pJobInfo       = info;
    pLicenseKeys   = licenseKeys; 
    pSelectionKeys = selectionKeys;
    pHardwareKeys  = hardwareKeys;

    ActionAgenda agenda = null;
    if(pJob != null) 
      agenda = pJob.getActionAgenda();

    QueueJobResults results = null;
    if(pJobInfo != null)
      results = pJobInfo.getResults();

    /* header */ 
    {
      String tname = "Job-Undefined-Normal";
      if((pJob != null) && (pJobInfo != null)) {
        tname = ("Job-" + pJobInfo.getState() + "-Normal");
        pHeaderLabel.setText(" Job " + pJob.getJobID() + ":  " + agenda.getPrimaryTarget());
        pNodeNameField.setText(agenda.getNodeID().getName());
      }
      else {
        pHeaderLabel.setText(null);
        pNodeNameField.setText(null);
      }
      
      try {
	pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon64(tname));
      }
      catch(PipelineException ex) {
        pHeaderIcon.setIcon(null); 
        UIMaster.getInstance().showErrorDialog(ex);
      }
    }

    /* summary panel */ 
    {
      {
	String text = "-";
	if(pJobInfo != null)
	  text = pJobInfo.getState().toString();
	pJobStateField.setText(text);
      }

      Long submitted = null;
      if(pJobInfo != null) 
	submitted = pJobInfo.getSubmittedStamp();

      Long started = null;
      if(pJobInfo != null) 
	started = pJobInfo.getStartedStamp();

      Long completed = null;
      if(pJobInfo != null) 
	completed = pJobInfo.getCompletedStamp();

      {
	String text = "-";
	if(submitted != null) {
	  if(started != null)
	    text = TimeStamps.formatInterval(started - submitted);
	  else if(completed != null)
	    text = TimeStamps.formatInterval(completed - submitted);
	  else 
	    text = TimeStamps.formatInterval(TimeStamps.now() - submitted);
	}
	
	pWaitingField.setText(text);
      }

      {
	String text = "-";
	if(started != null) {
	  if(completed != null)
	    text = TimeStamps.formatInterval(completed - started);
	  else 
	    text = TimeStamps.formatInterval(TimeStamps.now() - started);
	}
	
	pRunningField.setText(text);
      }

      {
	String text = "-";
	if(submitted != null) 
	  text = TimeStamps.format(submitted);

	pSubmittedField.setText(text);
      }

      {
	String text = "-";
	if(started != null) 
	  text = TimeStamps.format(started);

	pStartedField.setText(text);
      }

      {
	String text = "-";
	if(completed != null) 
	  text = TimeStamps.format(completed);

	pCompletedField.setText(text);
      }

    }

    /* process details panel */ 
    {
      {
	String text = "-";
	if((pJobInfo != null) && (pJobInfo.getHostname() != null)) {
          if(prefs.getShowFullHostnames()) 
            text = pJobInfo.getHostname();
          else 
            text = pJobInfo.getShortHostname();
        }

	pHostnameField.setText(text);
      }

      {
	String text = "-";
	if((pJobInfo != null) && (pJobInfo.getOsType() != null)) 
	  text = pJobInfo.getOsType().toString();

	pOsTypeField.setText(text);
      }

      {
	String text = "-";
	if(results != null) {
	  Integer code = results.getExitCode();
	  if(code != null) 
	    text = ((code == 0) ? "Success" : "Failure [" + code + "]");
	}

	pExitCodeField.setText(text);
      }
      
      {
	boolean enabled = false;
	if((pJob != null) && (pJobInfo != null)) {
	  switch(pJobInfo.getState()) {
	  case Running:
	  case Finished:
	  case Failed:
	    enabled = true;
	  }
	}

	pExecDetailsButton.setEnabled(enabled);
	pOutputButton.setEnabled(enabled);
	pErrorButton.setEnabled(enabled);
      }

      {
	Double secs = null;
	if(results != null)
	  secs = results.getUserTime();
	
	pUserTimeField.setText(formatInterval(secs));
      }

      {
	Double secs = null;
	if(results != null)
	  secs = results.getSystemTime();
	
	pSystemTimeField.setText(formatInterval(secs));
      }

      {
	Long size = null;
	if(results != null)
	  size = results.getResidentSize();
	
	pResidentField.setText(formatLong(size));
      }

      {
	Long size = null;
	if(results != null)
	  size = results.getVirtualSize();
	
	pVirtualField.setText(formatLong(size));
      }

      {
	Long size = null;
	if(results != null)
	  size = results.getSwappedSize();
	
	pSwappedField.setText(formatLong(size));
      }

      {
	Long faults = null;
	if(results != null)
	  faults = results.getPageFaults();
	
	pPageFaultsField.setText(formatLong(faults));
      }
    }
    
    /* action panel */
    
    if (pJob != null) {
      BaseAction action = pJob.getAction();
      assert(action != null);

      pActionNameField.setText(action.getName());
      pActionVersionField.setText("v" + action.getVersionID());
      pActionVendorField.setText(action.getVendor());
      pActionOsSupportField.setSupports(action.getSupports());
      
      updateActionParams(action);
    }
    else {
      pActionNameField.setText(null);
      pActionVersionField.setText(null);
      pActionVendorField.setText(null);
      pActionOsSupportField.setSupports(null);
      
      updateActionParams(null);
    }

    /* job requirements panel */ 
    {
      if(pJob != null) {
	JobReqs jreqs = pJob.getJobRequirements();
	
	pPriorityField.setValue(jreqs.getPriority());
	pRampUpField.setValue(jreqs.getRampUp());
	pMaxLoadField.setValue(jreqs.getMaxLoad());
	pMinMemoryField.setValue(jreqs.getMinMemory());
	pMinDiskField.setValue(jreqs.getMinDisk());
	
	if (pJob.doKeysNeedUpdate(chooserUpdateTime)) 
	  pKeyStateField.setText("Stale");
	else
	  pKeyStateField.setText("Finished");
      }
      else {
	pPriorityField.setValue(null);
	pRampUpField.setValue(null);
	pMaxLoadField.setValue(null);
	pMinMemoryField.setValue(null);
	pMinDiskField.setValue(null);
	
	pKeyStateField.setText(null);
      }

      /* selection keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if((pJob != null) && (pSelectionKeys != null)) {
	  for(SelectionKey key : pSelectionKeys)
	    keys.put(key.getName(), key.getDescription());
	}

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(keys.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  JobReqs jreqs = pJob.getJobRequirements();

	  boolean first = true; 
	  for(String kname : keys.keySet()) {
	
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getSelectionKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIFactory.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize, keys.get(kname));
	  }
	}

	pSelectionDrawer.setContents((JComponent) comps[2]);
      }
      
      /* hardware keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if((pJob != null) && (pHardwareKeys != null)) {
	  for(HardwareKey key : pHardwareKeys)
	    keys.put(key.getName(), key.getDescription());
	}

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(keys.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  JobReqs jreqs = pJob.getJobRequirements();

	  boolean first = true; 
	  for(String kname : keys.keySet()) {
	
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getHardwareKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIFactory.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize, keys.get(kname));
	  }
	}

	pHardwareDrawer.setContents((JComponent) comps[2]);
      }

      /* license keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if((pJob != null) && (pLicenseKeys != null)) {
	  for(LicenseKey key : pLicenseKeys)
	    keys.put(key.getName(), key.getDescription());
	}

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(keys.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  JobReqs jreqs = pJob.getJobRequirements();

	  boolean first = true; 
	  for(String kname : keys.keySet()) {
	
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getLicenseKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIFactory.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize, keys.get(kname));
	  }
	}

	pLicenseDrawer.setContents((JComponent) comps[2]);
      }
      
      pKeysBox.revalidate();
      pKeysBox.repaint();
    }
    
    /* files panel */ 
    {
      /* target files panel */ 
      {
	Component comps[] = createCommonPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  {
	    String text = "-";
	    if(agenda != null) 
	      text = agenda.getPrimaryTarget().toString();

	    UIFactory.createTitledTextField
	      (tpanel, "Primary Target:", sTSize-7, 
	       vpanel, text, sVSize, 
	       "The primary files created by this job.");
	  }

	  if(agenda != null) {
	    SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets();
	    if(!fseqs.isEmpty()) {
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 9);
	      for(FileSeq fseq : fseqs) {
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		UIFactory.createTitledTextField
		  (tpanel, "Secondary Target:", sTSize-7, 
		   vpanel, fseq.toString(), sVSize, 
		   "The secondary files created by this job.");
	      }
	    }
	  }	
	}

	pTargetFilesDrawer.setContents((JComponent) comps[2]);
      }

      /* source files panel */ 
      if((agenda != null) && (!agenda.getSourceNames().isEmpty())) {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.addComponentListener(this);
        hbox.add(UIFactory.createSidebar());

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  for(String sname : agenda.getSourceNames()) {
	    Component comps[] = createCommonPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];

	      {
		String text = agenda.getPrimarySource(sname).toString();
		UIFactory.createTitledTextField
		  (tpanel, "Primary Source:", sTSize-14, 
		   vpanel, text, sVSize, 
		   "The primary files associated with the node used by this job.");
	      }

	      SortedSet<FileSeq> fseqs = agenda.getSecondarySources(sname);
	      if(!fseqs.isEmpty()) {
		UIFactory.addVerticalSpacer(tpanel, vpanel, 9);
		for(FileSeq fseq : fseqs) {
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  UIFactory.createTitledTextField
		    (tpanel, "Secondary Source:", sTSize-14, 
		     vpanel, fseq.toString(), sVSize, 
		     "The secondary files associated with the node used by this job.");
		}
	      }
	    }

	    JDrawer drawer = new JDrawer(sname, (JComponent) comps[2], true);
	    drawer.setToolTipText(UIFactory.formatToolTip("The source node name."));
	    vbox.add(drawer);
	  }

	  hbox.add(vbox);
	}

	pSourceFilesDrawer.setContents(hbox);
      }
      else {
	Component comps[] = createCommonPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}

	pSourceFilesDrawer.setContents((JComponent) comps[2]);
      }

      pFilesBox.revalidate();
      pFilesBox.repaint();
    }

    if((pJob == null) || ((pLastJobID != null) && (pJob.getJobID() != pLastJobID))) {
      pExecDetailsDialog = null;
      pStdOutDialog      = null;
      pStdErrDialog      = null;
    }

    pLastJobID = null;
    if(pJob != null) 
      pLastJobID = pJob.getJobID();
  }

  
  /**
   * Generates a formatted string representation of a time in seconds.
   */ 
  private String
  formatInterval
  (
   Double secs
  )
  {
    if(secs == null) 
      return "-";

    if(secs < 60.0) {
      return String.format("%1$.1fs", secs);
    }
    else if(secs < 3600.0) {
      int m    = (int) Math.floor(secs / 60.0);
      double s = secs - (m * 60.0);

      return String.format("%1$dm %2$.1fs", m, s);
    }
    else {
      int h    = (int) Math.floor(secs / 3600.0);
      int m    = (int) Math.floor((secs - (h * 3600.0)) / 60.0);
      double s = secs - (h*3600.0 + m*60.0);
     
      return String.format("%1$dh %2$dm %3$.1fs", h, m, s); 
    }
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
  
  /**
   * Update the UI components associated with the job's action's parameters.
   */ 
  private void 
  updateActionParams
  (
    BaseAction action
  ) 
  {
    pActionParamsBox.removeAll();

    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    tpanel.setName("BottomTitlePanel");
    JPanel vpanel = (JPanel) comps[1];
    vpanel.setName("BottomValuePanel");

    /* per-source params */ 
    if((action != null) && action.supportsSourceParams()) {
      ActionAgenda agenda = pJob.getActionAgenda();
      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

      pViewSourceParamsDialog = null;
      
      pSourceParamComponents = new Component[2];
      
      {
	JLabel label = UIFactory.createFixedLabel
	  ("Source Parameters:", sTSize, JLabel.RIGHT, 
	   "The Action plugin parameters associated with each source node file sequence.");
	pSourceParamComponents[0] = label;
	
	tpanel.add(label);
      }
      
      { 
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	if(action.supportsSourceParams()) {
	  JButton btn = new JButton("View...");
	  pSourceParamComponents[1] = btn;
		
	  btn.setName("ValuePanelButton");
	  btn.setRolloverEnabled(false);
	  btn.setFocusable(false);
	  
	  Dimension size = new Dimension(sVSize, 19);
	  btn.setMinimumSize(size);
	  btn.setPreferredSize(size);
	  btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	  
	  btn.addActionListener(this);
	  btn.setActionCommand("view-source-params");
	    
	  hbox.add(btn);
	  
	  {
	    String title = agenda.getPrimaryTarget().toString();
	    
	    ArrayList<String> snames  = new ArrayList<String>();
	    ArrayList<String> stitles = new ArrayList<String>();
	    ArrayList<FileSeq> sfseqs = new ArrayList<FileSeq>();

	    for(String sname : agenda.getSourceNames()) {

	      FileSeq primary = agenda.getPrimarySource(sname);
	      String stitle = primary.toString();

	      snames.add(sname);
	      stitles.add(stitle);
	      sfseqs.add(null);

	      for(FileSeq fseq : agenda.getSecondarySources(sname)) {
		snames.add(sname);
		stitles.add(stitle);
		sfseqs.add(fseq);
	      }
	    }
	    
	    pViewSourceParamsDialog = 
	      new JSourceParamsDialog
	        (getTopFrame(), false, title, snames, stitles, sfseqs, action);
	  }
	}
	else {
	  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
	  pSourceParamComponents[1] = field;
	  
	  hbox.add(field);
	}
	
	vpanel.add(hbox);
      }	
    }
    else {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }
    pActionParamsBox.add(comps[2]);

    /* single valued parameters */ 
    if((action != null) && action.hasSingleParams()) {
      ActionAgenda agenda = pJob.getActionAgenda();
      pLinkActionParamValues.clear();
      pLinkActionParamValues.add("-");
      for(String sname : agenda.getSourceNames()) 
	pLinkActionParamValues.add(agenda.getPrimarySource(sname).toString());
      
      pLinkActionParamNodeNames.clear();
      pLinkActionParamNodeNames.add(null);
      pLinkActionParamNodeNames.addAll(agenda.getSourceNames());

      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

        hbox.add(UIFactory.createSidebar());
      
	updateSingleActionParams(action, action.getSingleLayout(), hbox, 1);
	
	pActionParamsBox.add(hbox);
      }
    }

    pActionBox.revalidate();
    pActionBox.repaint();
  }

  /**
   * Recursively create drawers containing the working and checked-in single valued 
   * action parameters.
   */ 
  private void 
  updateSingleActionParams
  (
   BaseAction action, 
   LayoutGroup group, 
   Box sbox, 
   int level
  ) 
  {
    Box dbox = new Box(BoxLayout.Y_AXIS);    
    {
      Component comps[] = createCommonPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      for(String pname : group.getEntries()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  /* single valued parameter */ 
	  ActionParam aparam = action.getSingleParam(pname);
	  if(aparam != null) {
	    if(aparam instanceof Color3dActionParam) {
	      Color3d value = (Color3d) aparam.getValue();
	      JColorField field = UIFactory.createTitledColorField
	        (getTopFrame(), tpanel,aparam.getNameUI() + ":" ,  sTSize-7*level, 
		 vpanel, value, sVSize);

	      field.setEnabled(false); 
	    }
	    else if(aparam instanceof Tuple2iActionParam) {
	      createLabel(aparam, level, tpanel);
	      Tuple2i value = (Tuple2i) aparam.getValue();
	      JTuple2iField field = new JTuple2iField(); 
	      field.setValue(value);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);

	      field.setEnabled(false); 

	      vpanel.add(field);
	    }
	    else if(aparam instanceof Tuple3iActionParam) {
	      createLabel(aparam, level, tpanel);
	      Tuple3i value = (Tuple3i) aparam.getValue();
	      JTuple3iField field = new JTuple3iField(); 
	      field.setValue(value);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);

	      field.setEnabled(false); 

	      vpanel.add(field);
	    }
	    else if(aparam instanceof Tuple2dActionParam) {
	      createLabel(aparam, level, tpanel);
	      Tuple2d value = (Tuple2d) aparam.getValue();
	      JTuple2dField field = new JTuple2dField(); 
	      field.setValue(value);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);

	      field.setEnabled(false); 

	      vpanel.add(field);
	    }
	    else if(aparam instanceof Tuple3dActionParam) {
	      createLabel(aparam, level, tpanel);
	      Tuple3d value = (Tuple3d) aparam.getValue();
	      JTuple3dField field = new JTuple3dField(); 
	      field.setValue(value);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);

	      field.setEnabled(false); 

	      vpanel.add(field);
	    }
	    else if(aparam instanceof Tuple4dActionParam) {
	      createLabel(aparam, level, tpanel);
	      Tuple4d value = (Tuple4d) aparam.getValue();
	      JTuple4dField field = new JTuple4dField(); 
	      field.setValue(value);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);

	      field.setEnabled(false); 

	      vpanel.add(field);
	    }
	    else {
	      String text = "-";
	      {
		if(aparam instanceof LinkActionParam) {
		  String source = (String) aparam.getValue();
		  int idx = pLinkActionParamNodeNames.indexOf(source);
		  if(idx != -1) 
		    text = pLinkActionParamValues.get(idx);
		}
		else if(aparam instanceof BooleanActionParam) {
		  Boolean value = (Boolean) aparam.getValue();
		  if(value != null) 
		    text = (value ? "YES" : "no");
		  else 
		    text = "-";
		}
		else {
		  Comparable value = aparam.getValue();
		  if(value != null)
		    text = value.toString();
		}
	      }

	      JTextField field = UIFactory.createTitledTextField
	        (tpanel,aparam.getNameUI() + ":" ,  sTSize-7*level, 
		 vpanel, text, sVSize);
	    }
	  }
	}
      }
      dbox.add(comps[2]);
    }
    
    if(!group.getSubGroups().isEmpty())  {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.addComponentListener(this);

      hbox.add(UIFactory.createSidebar());

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	for(LayoutGroup sgroup : group.getSubGroups()) 
	  updateSingleActionParams(action, sgroup, vbox, level+1);

	hbox.add(vbox);
      }
      dbox.add(hbox);
    }
    
    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
    }
  }
  
  private JLabel
  createLabel
  (
    ActionParam aparam,
    int level,
    JPanel tpanel
  )
  {
    JLabel toReturn = UIFactory.createFixedLabel
	(aparam.getNameUI() + ":", sTSize-7*level, JLabel.RIGHT, 
	 aparam.getDescription());
    tpanel.add(toReturn);
    return toReturn;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    TextureMgr.getInstance().rebuildIcons();
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
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    requestFocusInWindow();
  }

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    pManagerPanel.handleManagerMouseEvent(e);
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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getShowExecDetails() != null) &&
       prefs.getShowExecDetails().wasPressed(e)) 
      doShowExecDetails();
    else if((prefs.getShowJobOutput() != null) &&
       prefs.getShowJobOutput().wasPressed(e)) 
      doShowOutput();
    else if((prefs.getShowJobErrors() != null) &&
       prefs.getShowJobErrors().wasPressed(e)) 
      doShowErrors();
    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	if(UIFactory.getBeepPreference())
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


  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden(ComponentEvent e) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved(ComponentEvent e) {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   ComponentEvent e
  )
  {
    Box box = (Box) e.getComponent();
    
    Dimension size = box.getComponent(1).getSize();

    JPanel spacer = (JPanel) box.getComponent(0);
    spacer.setMaximumSize(new Dimension(7, size.height));
    spacer.revalidate();
    spacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}


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
    if(cmd.equals("show-exec-details")) 
      doShowExecDetails();
    else if(cmd.equals("show-output")) 
      doShowOutput();
    else if(cmd.equals("show-error")) 
      doShowErrors();
    else if(cmd.equals("view-source-params"))
      doViewSourceParams();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the execution details dialog.
   */ 
  public void 
  doShowExecDetails() 
  {
    if((pJob != null) && (pJobInfo != null)) {
      GetExecDetailsTask task = 
        new GetExecDetailsTask(pHeaderLabel.getText(), pJob, pJobInfo); 
      task.start();
    }
  }

  /**
   * Show the STDOUT dialog.
   */ 
  public void 
  doShowOutput()
  {
    if((pJob != null) && (pJobInfo != null)) {
      switch(pJobInfo.getState()) {
      case Running:
      case Finished:
      case Failed:
        if((pStdOutDialog == null) || !pStdOutDialog.isVisible())
          pStdOutDialog = new JMonitorJobStdOutDialog(pJob, pJobInfo);
        pStdOutDialog.setVisible(true);
      }
    }
  }

  /**
   * Show the STDERR dialog.
   */ 
  public void 
  doShowErrors()
  {
    if((pJob != null) && (pJobInfo != null)) {
      switch(pJobInfo.getState()) {
      case Running:
      case Finished:
      case Failed:
        if((pStdErrDialog == null) || !pStdErrDialog.isVisible())
          pStdErrDialog = new JMonitorJobStdErrDialog(pJob, pJobInfo);
        pStdErrDialog.setVisible(true);
      }
    }
  }
  
  /**
   * Show a dialog for viewing the checked-in per-source parameters.
   */ 
  private void 
  doViewSourceParams() 
  {
    pViewSourceParamsDialog.setVisible(true);
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the execution details for the job from the job manager where its running.
   */ 
  private
  class GetExecDetailsTask
    extends Thread
  {
    public 
    GetExecDetailsTask
    (
     String header,
     QueueJob job,
     QueueJobInfo info
    ) 
    {
      super("JQueueJobDetailsPanel:GetExecDetailsTask"); 

      pDetailsHeader = header;
      pDetailsJob    = job; 
      pDetailsInfo   = info; 
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance(); 
      SubProcessExecDetails details = null;
      if(pDetailsInfo != null) {
        String hostname = pDetailsInfo.getHostname();
        switch(pDetailsInfo.getState()) {
        case Running:
        case Finished:
        case Failed:
          {
            JobMgrClient jclient = null; 
            try {
              jclient = new JobMgrClient(hostname);
              details = jclient.getExecDetails(pDetailsJob.getJobID());
            }
            catch(PipelineException ex) { 
              if(ex.getCause() instanceof IOException) {
                master.showErrorDialog
                  ("Communication Error:", 
                   "Unable to contact the Job Manager on (" + hostname + ") where the " + 
                   "job (" + pDetailsJob.getJobID() + ") was originally executed to " + 
                   "retrieve the execution details!\n" + 
                   "\n" +
                   "The most likely cause is that the Job Manager is not currently " + 
                   "running.  If running, it may also become unresponsive due to the " + 
                   "host running the Job Manager experiencing extremely high system " + 
                   "load or heavy virtual memory swapping.  The cause might also be some " + 
                   "more fundamental networking failure or misconfiguration.  Please " + 
                   "contact your IT staff to correct this problem."); 
              }
              else {
                UIMaster.getInstance().showErrorDialog(ex);
              }
            }
            finally {
              if(jclient != null) 
                jclient.disconnect();
            }
          }
          break;
            
        case Limbo:
          master.showErrorDialog
            ("Error:", 
             "No execution details are available for job " + 
             "(" + pDetailsJob.getJobID() + ") while it is in a Limbo state.  Enable the " + 
             "Job Manager running on (" + hostname + ") in order to resolve the state of " + 
             "the job."); 
          break;
          
        default:
          master.showErrorDialog
            ("Error:", 
             "No execution details are available for job " + 
             "(" + pDetailsJob.getJobID() + ") while it is in a " + 
             pDetailsInfo.getState() + " state."); 
        }
      }
      else {
        master.showErrorDialog
          ("Internal Error:", 
           "No execution details can be retrieved without a specific job!"); 
      }

      if(details != null) {
        UpdateExecDetailsTask task = 
          new UpdateExecDetailsTask(pDetailsHeader, pDetailsJob, pDetailsInfo, details);
        SwingUtilities.invokeLater(task);   
      }
    }

    private String        pDetailsHeader;
    private QueueJob      pDetailsJob;
    private QueueJobInfo  pDetailsInfo;
  }

  /** 
   * Update the execution details dialog and show it.
   */ 
  private
  class UpdateExecDetailsTask
    extends Thread
  {
    public 
    UpdateExecDetailsTask
    (
     String header,
     QueueJob job,
     QueueJobInfo info,
     SubProcessExecDetails details
    ) 
    {
      super("JQueueJobDetailsPanel:UpdateExecDetailsTask"); 

      pDetailsHeader = header;
      pDetailsJob    = job; 
      pDetailsInfo   = info; 
      pDetails       = details;
    }

    @Override
    public void 
    run() 
    {
      if((pExecDetailsDialog == null) || !pExecDetailsDialog.isVisible())
	pExecDetailsDialog = new JExecDetailsDialog();

      pExecDetailsDialog.updateContents(pDetailsHeader, pDetailsJob, pDetailsInfo, pDetails);
      pExecDetailsDialog.setVisible(true);
    }

    private String                pDetailsHeader;
    private QueueJob              pDetailsJob;
    private QueueJobInfo          pDetailsInfo;
    private SubProcessExecDetails pDetails;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    encoder.encode("SummaryDrawerOpen",     pSummaryDrawer.isOpen());
    
    encoder.encode("ProcessDrawerOpen",     pProcessDrawer.isOpen());
    
    encoder.encode("JobReqsDrawerOpen",     pJobReqsDrawer.isOpen());
    encoder.encode("SelectionDrawerOpen",   pSelectionDrawer.isOpen());
    encoder.encode("ActionDrawerOpen",      pActionDrawer.isOpen());
    encoder.encode("LicenseDrawerOpen",     pLicenseDrawer.isOpen());
    encoder.encode("HardwareDrawerOpen",    pHardwareDrawer.isOpen());
    
    encoder.encode("FilesDrawerOpen",       pFilesDrawer.isOpen());
    encoder.encode("TargetFilesDrawerOpen", pTargetFilesDrawer.isOpen());
    encoder.encode("SourceFilesDrawerOpen", pSourceFilesDrawer.isOpen());
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    {
      Boolean open = (Boolean) decoder.decode("SummaryDrawerOpen");
      if(open != null) 
	pSummaryDrawer.setIsOpen(open);
    }
    
    
    {
      Boolean open = (Boolean) decoder.decode("ProcessDrawerOpen");
      if(open != null) 
 	pProcessDrawer.setIsOpen(open);
    }
    
    
    {
      Boolean open = (Boolean) decoder.decode("JobReqsDrawerOpen");
      if(open != null) 
 	pJobReqsDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("SelectionDrawerOpen");
      if(open != null) 
 	pSelectionDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("ActionDrawerOpen");
      if(open != null) 
 	pActionDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("LicenseDrawerOpen");
      if(open != null) 
 	pLicenseDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("HardwareDrawerOpen");
      if(open != null) 
 	pHardwareDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("FilesDrawerOpen");
      if(open != null) 
 	pFilesDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("TargetFilesDrawerOpen");
      if(open != null) 
 	pTargetFilesDrawer.setIsOpen(open);
    }
    
    {
      Boolean open = (Boolean) decoder.decode("SourceFilesDrawerOpen");
      if(open != null) 
 	pSourceFilesDrawer.setIsOpen(open);
    }
    
    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8770292887124076524L;
  
  private static final int  sTSize = 180;
  private static final int  sVSize = 240;
  private static final int  sHSize = 117;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current job.
   */ 
  private QueueJob  pJob; 

  /** 
   * The job status information.
   */ 
  private QueueJobInfo  pJobInfo; 

  /**
   * The current license keys.
   */
  private ArrayList<LicenseKey>  pLicenseKeys; 

  /**
   * The current selection keys.
   */
  private ArrayList<SelectionKey>  pSelectionKeys; 
  
  /**
   * The current selection keys.
   */
  private ArrayList<HardwareKey>  pHardwareKeys; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel pHeaderIcon;
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved target node name field.
   */ 
  private JTextField pNodeNameField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The job state field.
   */ 
  private JTextField pJobStateField; 
  
  
  /**
   * The waiting stamp field.
   */ 
  private JTextField pWaitingField; 
  
  /**
   * The running stamp field.
   */ 
  private JTextField pRunningField; 
  

  /**
   * The submitted stamp field.
   */ 
  private JTextField pSubmittedField; 
  
  /**
   * The started stamp field.
   */ 
  private JTextField pStartedField; 
  
  /**
   * The completed stamp field.
   */ 
  private JTextField pCompletedField; 
  

  /**
   * The drawer containing the job summary.
   */ 
  private JDrawer  pSummaryDrawer;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The execution details dialog button.
   */ 
  private JButton pExecDetailsButton;

  /**
   * The hostname field.
   */ 
  private JTextField pHostnameField; 
  
  /**
   * The operating system type field.
   */ 
  private JTextField pOsTypeField; 
  
  /**
   * The exit code field.
   */ 
  private JTextField pExitCodeField; 

  /**
   * The STDOUT/STDERR dialog buttons.
   */ 
  private JButton  pOutputButton; 
  private JButton  pErrorButton; 

  /**
   * The drawer containing the process details.
   */ 
  private JDrawer  pProcessDrawer;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user time field.
   */ 
  private JTextField  pUserTimeField;

  /**
   * The system time field.
   */ 
  private JTextField  pSystemTimeField;

  /**
   * The resident memory size. 
   */ 
  private JTextField  pResidentField;
  
  /**
   * The virtual memory size. 
   */ 
  private JTextField  pVirtualField;
  
  /**
   * The swapped memory size. 
   */ 
  private JTextField  pSwappedField;
  
  /**
   * The page faults field.
   */ 
  private JTextField  pPageFaultsField;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The priority field.
   */ 
  private JIntegerField pPriorityField;

  /**
   * The ramp-up interval field.
   */ 
  private JIntegerField pRampUpField;

  /**
   * The maximum load field.
   */ 
  private JFloatField pMaxLoadField;

  /**
   * The minimum load field.
   */ 
  private JByteSizeField pMinMemoryField;

  /**
   * The minimum load field.
   */ 
  private JByteSizeField pMinDiskField;

  /**
   * The key state field.
   */
  private JTextField  pKeyStateField;

  /**
   * The drawer containing the job requirements components.
   */ 
  private JDrawer  pJobReqsDrawer;

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The field for the name of the Action.
   */
  private JTextField pActionNameField;
  
  /**
   * The field for the version of the Action.
   */
  private JTextField pActionVersionField;
  
  /**
   * The field for the vendor of the Action.
   */
  private JTextField pActionVendorField;
  
  /**
   * The field for the supported OS's of the Action.
   */
  private JOsSupportField pActionOsSupportField;
  
  /**
   * The action parameters container.
   */ 
  private Box  pActionParamsBox;
  
  /**
   * The action container.
   */ 
  private Box  pActionBox;
  
  /**
   * The UI components related to per-source action parameters.
   */ 
  private Component pSourceParamComponents[]; 
  
  /**
   * The dialog used to view checked-in per-source parameters.
   */ 
  private JSourceParamsDialog  pViewSourceParamsDialog;

  /**
   * The drawer containing the action components.
   */ 
  private JDrawer  pActionDrawer;
  
  /**
   * The JCollectionField values and corresponding fully resolved names of the 
   * upstream nodes used by LinkActionParam fields.
   */ 
  private ArrayList<String>  pLinkActionParamValues;
  private ArrayList<String>  pLinkActionParamNodeNames;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The license/selection keys container.
   */ 
  private Box  pKeysBox;

  /**
   * The drawer containing the selection key components.
   */ 
  private JDrawer  pSelectionDrawer;

  /**
   * The drawer containing the licence key components.
   */ 
  private JDrawer  pLicenseDrawer;
  
  /**
   * The drawer containing the licence key components.
   */ 
  private JDrawer  pHardwareDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The file drawers container.
   */ 
  private Box  pFilesBox;

  /**
   * The drawer containing the target file sequences.
   */ 
  private JDrawer  pTargetFilesDrawer; 

  /**
   * The drawer containing the source file sequences.
   */ 
  private JDrawer  pSourceFilesDrawer; 

  /**
   * The drawer containing the target/source files drawers.
   */ 
  private JDrawer  pFilesDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The execution details dialog.
   */ 
  private JExecDetailsDialog  pExecDetailsDialog; 

  /**
   * The STDOUT dialog.
   */ 
  private JMonitorJobStdOutDialog  pStdOutDialog; 

  /**
   * The STDERR dialog.
   */ 
  private JMonitorJobStdErrDialog  pStdErrDialog; 


  /**
   * The ID of the last job displayed by the panel.
   */ 
  private Long  pLastJobID; 

}
