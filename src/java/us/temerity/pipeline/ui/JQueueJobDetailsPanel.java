// $Id: JQueueJobDetailsPanel.java,v 1.1 2004/08/31 08:21:07 jim Exp $

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
  implements ComponentListener
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
	    {
	      pJobStateField = 
		UIMaster.createTitledTextField(tpanel, "Job State:", sTSize, 
					       vpanel, "-", sVSize);
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	    
	    /* submitted stamp */ 
	    {
	      pSubmittedField = 
		UIMaster.createTitledTextField(tpanel, "Submitted:", sTSize, 
					       vpanel, "-", sVSize);
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* started stamp */ 
	    {
	      pStartedField = 
		UIMaster.createTitledTextField(tpanel, "Started:", sTSize, 
					       vpanel, "-", sVSize);
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* completed stamp */ 
	    {
	      pCompletedField = 
		UIMaster.createTitledTextField(tpanel, "Completed:", sTSize, 
					       vpanel, "-", sVSize);
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Summary:", (JComponent) comps[2], true);
	  pSummaryDrawer = drawer;
	  vbox.add(drawer);
	}

	{ 
	  Box jrbox = new Box(BoxLayout.Y_AXIS);
	  
	  /* job requirements */ 
	  {
	    Component comps[] = createCommonPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      /* priority */ 
	      {
		pPriorityField = 
		  UIMaster.createTitledIntegerField(tpanel, "Priority:", sTSize, 
						    vpanel, null, sVSize);
	      }
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	      
	      /* maximum load */ 
	      { 
		pMaxLoadField =
		  UIMaster.createTitledFloatField(tpanel, "Maximum Load:", sTSize, 
						  vpanel, null, sVSize);
	      }
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum memory */ 
	      { 
		pMinMemoryField = 
		  UIMaster.createTitledByteSizeField(tpanel, "Minimum Memory:", sTSize, 
						     vpanel, null, sVSize);
	      }

	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      /* minimum disk */ 
	      { 
		pMinDiskField = 
		  UIMaster.createTitledByteSizeField(tpanel, "Minimum Disk:", sTSize, 
						     vpanel, null, sVSize);
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
	      pKeysSpacer = spanel;
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
		pSelectionDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      /* license keys */ 
	      {
		JDrawer drawer = new JDrawer("License Keys:", new JPanel(), false);
		pLicenseDrawer = drawer;
		dbox.add(drawer);
	      }
	      
	      kbox.add(dbox);
	    }

	    jrbox.add(kbox);
	  }

	  JDrawer drawer = new JDrawer("Job Requirements:", jrbox, true);
	  pJobReqsDrawer = drawer;
	  vbox.add(drawer);
	}

	
	// ...


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

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	  add(scroll);
	}
      }

//       Dimension size = new Dimension(sSize+22, 120);
//       setMinimumSize(size);
//       setPreferredSize(size); 
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
    Component comps[] = new Component[3];
    
    Box body = new Box(BoxLayout.X_AXIS);
    comps[2] = body;
    {
      {
	JPanel panel = new JPanel();
	comps[0] = panel;
	
	panel.setName("TitlePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	body.add(panel);
      }
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

    /* header */ 
    if((pJob != null) && (pJobInfo != null)) {
      ActionAgenda agenda = pJob.getActionAgenda();

      pHeaderLabel.setText(" Job " + pJob.getJobID() + ":  " + agenda.getPrimaryTarget());
      pHeaderLabel.setIcon(sIcons[pJobInfo.getState().ordinal()]);

      pNodeNameField.setText(agenda.getNodeID().getName());
    }
    else {
      pHeaderLabel.setText(null);
      pHeaderLabel.setIcon(sUndefinedIcon);

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

      {
	String text = "-";
	if(pJobInfo != null) {
	  Date stamp = pJobInfo.getSubmittedStamp();
	  if(stamp != null) 
	    text = Dates.format(stamp);
	}

	pSubmittedField.setText(text);
      }

      {
	String text = "-";
	if(pJobInfo != null) {
	  Date stamp = pJobInfo.getStartedStamp();
	  if(stamp != null) 
	    text = Dates.format(stamp);
	}

	pStartedField.setText(text);
      }

      {
	String text = "-";
	if(pJobInfo != null) {
	  Date stamp = pJobInfo.getCompletedStamp();
	  if(stamp != null) 
	    text = Dates.format(stamp);
	}

	pCompletedField.setText(text);
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
	TreeSet<String> knames = new TreeSet<String>();
	if(pJob != null) {
	  try {
	    knames.addAll(master.getQueueMgrClient().getSelectionKeyNames());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(knames.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  JobReqs jreqs = pJob.getJobRequirements();

	  boolean first = true; 
	  for(String kname : knames) {
	
	    if(!first) 
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getSelectionKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIMaster.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize);
	  }
	}

	pSelectionDrawer.setContents((JComponent) comps[2]);
      }

      /* license keys */ 
      {
	TreeSet<String> knames = new TreeSet<String>();
	if(pJob != null) {
	  try {
	    knames.addAll(master.getQueueMgrClient().getLicenseKeyNames());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(knames.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  JobReqs jreqs = pJob.getJobRequirements();

	  boolean first = true; 
	  for(String kname : knames) {
	
	    if(!first) 
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    String value = jreqs.getLicenseKeys().contains(kname) ? "YES" : "no";

	    JTextField field = 
	      UIMaster.createTitledTextField(tpanel, kname + ":", sTSize-7, 
					     vpanel, value, sVSize);
	  }
	}

	pLicenseDrawer.setContents((JComponent) comps[2]);
      }
      
      pKeysBox.revalidate();
      pKeysBox.repaint();
    }
    


    // ...


  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    Box box = (Box) pKeysSpacer.getParent();
    Dimension size = box.getComponent(1).getSize();

    pKeysSpacer.setMaximumSize(new Dimension(7, size.height));
    pKeysSpacer.revalidate();
    pKeysSpacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

//   public void 
//   toGlue
//   ( 
//    GlueEncoder encoder   
//   ) 
//     throws GlueException
//   {
//     super.toGlue(encoder);
  
//     encoder.encode("WorkingAreaDrawerOpen",  pWorkingAreaDrawer.isOpen());
//     encoder.encode("TimeStampsDrawerOpen",   pTimeStampsDrawer.isOpen());
//   }

//   public void 
//   fromGlue
//   (
//    GlueDecoder decoder 
//   ) 
//     throws GlueException
//   {
//     {
//       Boolean open = (Boolean) decoder.decode("WorkingAreaDrawerOpen");
//       if(open != null) 
// 	pWorkingAreaDrawer.setIsOpen(open);
//     }

//     {
//       Boolean open = (Boolean) decoder.decode("TimeStampsDrawerOpen");
//       if(open != null) 
// 	pTimeStampsDrawer.setIsOpen(open);
//     }

//     super.fromGlue(decoder);
//   }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8770292887124076524L;
  
  private static final int  sTSize = 120;
  private static final int  sVSize = 180;
  

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
   * The job requirements spacer.
   */ 
  private JPanel  pKeysSpacer;

  /**
   * The drawer containing the selection key components.
   */ 
  private JDrawer  pSelectionDrawer;

  /**
   * The drawer containing the licence key components.
   */ 
  private JDrawer  pLicenseDrawer;

}
