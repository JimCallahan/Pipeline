// $Id: JQueueJobDetailsPanel.java,v 1.11 2004/11/21 18:39:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

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
	  JTextField field = UIMaster.createTextField(null, 100, JLabel.LEFT);
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
	    pJobStateField = UIMaster.createTitledTextField
	      (tpanel, "Job State:", sTSize, 
	       vpanel, "-", sVSize, 
	       "The current execution state of the job.");
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	    /* time waiting */ 
	    pWaitingField = UIMaster.createTitledTextField
	      (tpanel, "Time Waiting:", sTSize, 
	       vpanel, "-", sVSize, 
	       "How long the job has been waiting to execute.");

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* time running */ 
	    pRunningField = UIMaster.createTitledTextField
	      (tpanel, "Time Running:", sTSize, 
	       vpanel, "-", sVSize, 
	       "How long the job has been running so far.");

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	    
	    /* submitted stamp */ 
	    pSubmittedField = UIMaster.createTitledTextField
	      (tpanel, "Submitted:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job was submitted for execution.");
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* started stamp */ 
	    pStartedField = UIMaster.createTitledTextField
	      (tpanel, "Started:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job began execution.");
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* completed stamp */ 
	    pCompletedField = UIMaster.createTitledTextField
	      (tpanel, "Completed:", sTSize, 
	       vpanel, "-", sVSize, 
	       "When the job completed execution.");	    
	  }
	  
	  JDrawer drawer = new JDrawer("Summary:", (JComponent) comps[2], true);
	  drawer.setToolTipText(UIMaster.formatToolTip("Summary of current job status."));
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
		JLabel label = UIMaster.createFixedLabel
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

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* hostname */ 
	    { 
	      pHostnameField =UIMaster.createTitledTextField
		(tpanel, "Hostname:", sTSize, 
		 vpanel, null, sVSize, 
		 "The fully resolved name of the host where the job was executed.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* exit code */ 
	    { 
	      pExitCodeField = UIMaster.createTitledTextField
		(tpanel, "Exit Code:", sTSize, 
		 vpanel, null, sVSize, 
		 "The job process exit code.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* logs */ 
	    {
	      {
		JLabel label = UIMaster.createFixedLabel
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

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	      
	    /* user time */ 
	    {
	      pUserTimeField = UIMaster.createTitledTextField
		(tpanel, "User Time:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The user space execution time of the job process.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* system time */ 
	    {
	      pSystemTimeField = UIMaster.createTitledTextField
		(tpanel, "System Time:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The kernel space execution time of the job process.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	    /* resident memory */ 
	    {
	      pResidentField = UIMaster.createTitledTextField
		(tpanel, "Resident Memory:", sTSize, 
		 vpanel, "-", sVSize, 
		 "The maximum amount of resident memory used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* virtual memory */ 
	    {
	      pVirtualField = UIMaster.createTitledTextField
		(tpanel, "Virtual Memory:", sTSize, 
		 vpanel, "-", sVSize,  
		 "The maximum amount of virtual memory used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* swapped memory */ 
	    {
	      pSwappedField = UIMaster.createTitledTextField
		(tpanel, "Swapped Memory:", sTSize, 
		 vpanel, "-", sVSize,  
		 "The maximum amount of swap space used by the job process and any " +
		 "child processes.");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* page faults */ 
	    {
	      pPageFaultsField = UIMaster.createTitledTextField
		(tpanel, "Page Faults:", sTSize, 
		 vpanel, null, sVSize, 
		 "The number of major memory page faults by the job process.");
	      pPageFaultsField.setEditable(false);
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Process Details:", (JComponent) comps[2], false);
	  drawer.setToolTipText(UIMaster.formatToolTip("Job process execution details."));
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
		pPriorityField = UIMaster.createTitledIntegerField
		  (tpanel, "Priority:", sTSize, 
		   vpanel, null, sVSize, 
		   "The relative priority of this job.");
		pPriorityField.setEditable(false);
	      }
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	      
	      /* maximum load */ 
	      { 
		pMaxLoadField = UIMaster.createTitledFloatField
		  (tpanel, "Maximum Load:", sTSize, 
		   vpanel, null, sVSize, 
		   "The maxmimum system load allowed on an eligable host.");
		pMaxLoadField.setEditable(false);
	      }
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum memory */ 
	      { 
		pMinMemoryField = UIMaster.createTitledByteSizeField
		  (tpanel, "Minimum Memory:", sTSize, 
		   vpanel, null, sVSize, 
		   "The minimum amount of free memory required on an eligable host.");
		pMinMemoryField.setEditable(false);
	      }

	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum disk */ 
	      { 
		pMinDiskField = UIMaster.createTitledByteSizeField
		  (tpanel, "Minimum Disk:", sTSize, 
		   vpanel, null, sVSize, 
		   "The minimum amount of free temporary local disk space required " +
		   "on an eligable host.");
		pMinDiskField.setEditable(false);
	      }
	    }
	    
	    jrbox.add(comps[2]);
	  }
	    
	  {
	    Box kbox = new Box(BoxLayout.X_AXIS);
	    pKeysBox = kbox;
	  
	    kbox.addComponentListener(this);
	  
	    {
	      JPanel spanel = new JPanel();
	      spanel.setName("Spacer");
	      
	      spanel.setMinimumSize(new Dimension(7, 0));
	      spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	      spanel.setPreferredSize(new Dimension(7, 0));
	      
	      kbox.add(spanel);
	    }
	
	    {
	      Box dbox = new Box(BoxLayout.Y_AXIS);

	      /* selection keys */ 
	      {
		JDrawer drawer = new JDrawer("Selection Keys:",  new JPanel(), false);
		drawer.setToolTipText(UIMaster.formatToolTip
		  ("The set of selection keys a server must have in order to be eligable " + 
		   "to run jobs associated with this node."));
		pSelectionDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      /* license keys */ 
	      {
		JDrawer drawer = new JDrawer("License Keys:", new JPanel(), false);
		drawer.setToolTipText(UIMaster.formatToolTip
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
	  drawer.setToolTipText(UIMaster.formatToolTip
	    ("The requirements that a server must meet in order to be eligable " +
	     "to run jobs associated with this node."));
	  pJobReqsDrawer = drawer;
	  vbox.add(drawer);
	}

	/* files panel */ 
	{
	  Box fbox = new Box(BoxLayout.X_AXIS);
	  pFilesBox = fbox;
	  
	  fbox.addComponentListener(this);
	  
	  {
	    JPanel spanel = new JPanel();
	    spanel.setName("Spacer");
	    
	    spanel.setMinimumSize(new Dimension(7, 0));
	    spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	    spanel.setPreferredSize(new Dimension(7, 0));
	    
	    fbox.add(spanel);
	  }
	
	  {
	    Box dbox = new Box(BoxLayout.Y_AXIS);

	    /* target files panel */ 
	    {
	      JDrawer drawer = new JDrawer("Targets:", new JPanel(), true);
	      drawer.setToolTipText(UIMaster.formatToolTip
		("The target files created by the job."));
	      pTargetFilesDrawer = drawer;
	      dbox.add(drawer);
	    }
	    
	    /* source files panel */ 
	    {
	      JDrawer drawer = new JDrawer("Sources:", new JPanel(), true);
	      drawer.setToolTipText(UIMaster.formatToolTip
		("The source files used by the job."));
	      pSourceFilesDrawer = drawer;
	      dbox.add(drawer);
	    }

	    fbox.add(dbox);
	  }

	  JDrawer drawer = new JDrawer("File Sequences:", fbox, false);
	  drawer.setToolTipText(UIMaster.formatToolTip
	    ("The file sequences associated with the job."));
	  pFilesDrawer = drawer;
	  vbox.add(drawer);
	}

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(sTSize+sVSize+30, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(sTSize+sVSize+30, 7));
	  
	  vbox.add(spanel);
	}

	vbox.add(Box.createVerticalGlue());

	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  add(scroll);
	}
      }

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateJob(null, null);
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
    Component comps[] = UIMaster.createTitledPanels();

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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components to reflect the current job group. 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   */
  public synchronized void 
  updateJob
  (
   String author, 
   String view, 
   QueueJob job,
   QueueJobInfo info
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateJob(job, info); 
  }

  /**
   * Update the UI components to reflect the current job. 
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   */
  public synchronized void 
  updateJob
  (
   QueueJob job,
   QueueJobInfo info
  ) 
  {
    pJob     = job;
    pJobInfo = info;

    ActionAgenda agenda = null;
    if(pJob != null) 
      agenda = pJob.getActionAgenda();

    QueueJobResults results = null;
    if(pJobInfo != null)
      results = pJobInfo.getResults();

    /* header */ 
    if((pJob != null) && (pJobInfo != null)) {
      pHeaderLabel.setText(" Job " + pJob.getJobID() + ":  " + agenda.getPrimaryTarget());
      pHeaderIcon.setIcon(sIcons[pJobInfo.getState().ordinal()]);

      pNodeNameField.setText(agenda.getNodeID().getName());
    }
    else {
      pHeaderLabel.setText(null);
      pHeaderIcon.setIcon(sUndefinedIcon);

      pNodeNameField.setText(null);
    }

    /* summary panel */ 
    {
      {
	String text = "-";
	if(pJobInfo != null)
	  text = pJobInfo.getState().toString();
	pJobStateField.setText(text);
      }

      Date submitted = null;
      if(pJobInfo != null) 
	submitted = pJobInfo.getSubmittedStamp();

      Date started = null;
      if(pJobInfo != null) 
	started = pJobInfo.getStartedStamp();

      Date completed = null;
      if(pJobInfo != null) 
	completed = pJobInfo.getCompletedStamp();

      {
	String text = "-";
	if(submitted != null) {
	  if(started != null)
	    text = Dates.formatInterval(started.getTime() - submitted.getTime());
	  else if(completed != null)
	    text = Dates.formatInterval(completed.getTime() - submitted.getTime());
	}
	
	pWaitingField.setText(text);
      }

      {
	String text = "-";
	if((started != null) && (completed != null))
	  text = Dates.formatInterval(completed.getTime() - started.getTime());
	
	pRunningField.setText(text);
      }

      {
	String text = "-";
	if(submitted != null) 
	  text = Dates.format(submitted);

	pSubmittedField.setText(text);
      }

      {
	String text = "-";
	if(started != null) 
	  text = Dates.format(started);

	pStartedField.setText(text);
      }

      {
	String text = "-";
	if(completed != null) 
	  text = Dates.format(completed);

	pCompletedField.setText(text);
      }

    }

    /* process details panel */ 
    {
      pExecDetailsButton.setEnabled(pJob != null);

      {
	String text = "-";
	if((pJobInfo != null) && (pJobInfo.getHostname() != null)) 
	  text = pJobInfo.getHostname();

	pHostnameField.setText(text);
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
	if(pJobInfo != null) {
	  switch(pJobInfo.getState()) {
	  case Running:
	  case Finished:
	  case Failed:
	    enabled = true;
	  }
	}
	
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

    /* job requirements panel */ 
    {
      if(pJob != null) {
	JobReqs jreqs = pJob.getJobRequirements();
	
	pPriorityField.setValue(jreqs.getPriority());
	pMaxLoadField.setValue(jreqs.getMaxLoad());
	pMinMemoryField.setValue(jreqs.getMinMemory());
	pMinDiskField.setValue(jreqs.getMinDisk());
      }
      else {
	pPriorityField.setValue(null);
	pMaxLoadField.setValue(null);
	pMinMemoryField.setValue(null);
	pMinDiskField.setValue(null);
      }

      UIMaster master = UIMaster.getInstance();

      /* selection keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if(pJob != null) {
	  try {
	    for(SelectionKey key : master.getQueueMgrClient().getSelectionKeys())
	      keys.put(key.getName(), key.getDescription());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
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
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getSelectionKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIMaster.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize, keys.get(kname));
	  }
	}

	pSelectionDrawer.setContents((JComponent) comps[2]);
      }

      /* license keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if(pJob != null) {
	  try {
	    for(LicenseKey key : master.getQueueMgrClient().getLicenseKeys())
	      keys.put(key.getName(), key.getDescription());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
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
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getLicenseKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIMaster.createTitledTextField(tpanel, kname + ":", sTSize-7, 
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

	    UIMaster.createTitledTextField
	      (tpanel, "Primary Target:", sTSize-7, 
	       vpanel, text, sVSize, 
	       "The primary files created by this job.");
	  }

	  if(agenda != null) {
	    SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets();
	    if(!fseqs.isEmpty()) {
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 9);
	      for(FileSeq fseq : fseqs) {
		UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
		UIMaster.createTitledTextField
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

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");

	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));

	  hbox.add(spanel);
	}

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  for(String sname : agenda.getSourceNames()) {
	    Component comps[] = createCommonPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];

	      {
		String text = agenda.getPrimarySource(sname).toString();
		UIMaster.createTitledTextField
		  (tpanel, "Primary Source:", sTSize-14, 
		   vpanel, text, sVSize, 
		   "The primary files associated with the node used by this job.");
	      }

	      SortedSet<FileSeq> fseqs = agenda.getSecondarySources(sname);
	      if(!fseqs.isEmpty()) {
		UIMaster.addVerticalSpacer(tpanel, vpanel, 9);
		for(FileSeq fseq : fseqs) {
		  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
		  UIMaster.createTitledTextField
		    (tpanel, "Secondary Source:", sTSize-14, 
		     vpanel, fseq.toString(), sVSize, 
		     "The secondary files associated with the node used by this job.");
		}
	      }
	    }

	    JDrawer drawer = new JDrawer(sname, (JComponent) comps[2], true);
	    drawer.setToolTipText(UIMaster.formatToolTip("The source node name."));
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
      double s = secs - ((double) (m * 60.0));

      return String.format("%1$dm %2$.1fs", m, s);
    }
    else {
      int h    = (int) Math.floor(secs / 3600.0);
      int m    = (int) Math.floor((secs - ((double) (h * 3600.0))) / 60.0);
      double s = secs - ((double) (h*3600.0 + m*60.0));
     
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
      if((pExecDetailsDialog == null) || !pExecDetailsDialog.isVisible())
	pExecDetailsDialog = new JExecDetailsDialog();

      pExecDetailsDialog.updateContents(pHeaderLabel.getText(), pJob, pJobInfo);
      pExecDetailsDialog.setVisible(true);
    }
  }

  /**
   * Show the STDOUT dialog.
   */ 
  public void 
  doShowOutput()
  {
    if((pJob != null) && (pJobInfo != null) && (pJobInfo.getHostname() != null)) {
      if((pStdOutDialog == null) || !pStdOutDialog.isVisible())
	pStdOutDialog = new JMonitorJobStdOutDialog(pJob, pJobInfo);
      
      pStdOutDialog.setVisible(true);
    }
  }

  /**
   * Show the STDERR dialog.
   */ 
  public void 
  doShowErrors()
  {
    if((pJob != null) && (pJobInfo != null) && (pJobInfo.getHostname() != null)) {
      if((pStdErrDialog == null) || !pStdErrDialog.isVisible())
	pStdErrDialog = new JMonitorJobStdErrDialog(pJob, pJobInfo);
      
      pStdErrDialog.setVisible(true);
    }
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
     encoder.encode("LicenseDrawerOpen",     pLicenseDrawer.isOpen());

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
       Boolean open = (Boolean) decoder.decode("LicenseDrawerOpen");
       if(open != null) 
 	pLicenseDrawer.setIsOpen(open);
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

  /**
   * The JobState header icons.
   */ 
  private static final Icon[] sIcons = {
    new ImageIcon(LookAndFeelLoader.class.getResource("QueuedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("PausedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("AbortedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("RunningIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("FinishedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("FailedIcon.png"))
  };

  private static final Icon sUndefinedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("UndefinedIcon.png")); 



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
   * The drawer containing the job requirements components.
   */ 
  private JDrawer  pJobReqsDrawer;

  
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
