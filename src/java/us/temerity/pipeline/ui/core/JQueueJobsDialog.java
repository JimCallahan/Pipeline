// $Id: JQueueJobsDialog.java,v 1.6 2007/10/11 18:52:07 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B S   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The job submission parameters dialog.
 */ 
public 
class JQueueJobsDialog
  extends JFullDialog
  implements ComponentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JQueueJobsDialog
  (
   Frame owner,
   String title,
   boolean batching
  )  
  {
    super(owner, title);
    
    pBatching = batching;

    /* initialize fields */ 
    {
      pSelectionKeyFields = new TreeMap<String,JBooleanField>();
      pLicenseKeyFields   = new TreeMap<String,JBooleanField>();
    }

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      pTopBox = vbox;

      /* job requirements */ 
      {
	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  if (pBatching) {
	    {
	      JBooleanField field = 
		UIFactory.createTitledBooleanField(tpanel, "Override Batch Size:", sTSize,
						  vpanel, sVSize);
	      pOverrideBatchSizeField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("batch-size-changed");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pBatchSizeField = 
	      UIFactory.createTitledIntegerField(tpanel, "Batch Size:", sTSize,
						vpanel, null, sVSize);
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	  }
	  
	  {
	    {
	      JBooleanField field = 
		UIFactory.createTitledBooleanField(tpanel, "Override Priority:", sTSize,
						  vpanel, sVSize);
	      pOverridePriorityField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("priority-changed");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pPriorityField = 
	      UIFactory.createTitledIntegerField(tpanel, "Priority:", sTSize,
						vpanel, null, sVSize);
	  }

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  {
	    {
	      JBooleanField field = 
		UIFactory.createTitledBooleanField(tpanel, "Override Ramp Up:", sTSize,
						  vpanel, sVSize);
	      pOverrideRampUpField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("ramp-up-changed");
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pRampUpField = 
	      UIFactory.createTitledIntegerField(tpanel, "Ramp Up Interval:", sTSize,
						vpanel, null, sVSize);
	  }
	}
	
	JDrawer drawer = 
	  new JDrawer("Job Requirements:", (JComponent) comps[2], true);
	vbox.add(drawer);
      }
      
      /* Machine Stats*/
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	{
	  pOverrideMaxLoadField = 
	    UIFactory.createTitledBooleanField
	      (tpanel, "Override Max Load:", sTSize, vpanel, sVSize);
	  pOverrideMaxLoadField.addActionListener(this);
	  pOverrideMaxLoadField.setActionCommand("load-changed");

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pMaxLoadField = 
	    UIFactory.createTitledFloatField
	      (tpanel, "Max Load:", sTSize, vpanel, null, sVSize);
	}
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	{
	  pOverrideMinMemoryField = 
	    UIFactory.createTitledBooleanField
	      (tpanel, "Override Min Memory:", sTSize, vpanel, sVSize);
	  pOverrideMinMemoryField.addActionListener(this);
	  pOverrideMinMemoryField.setActionCommand("memory-changed");

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pMinMemoryField = 
	    UIFactory.createTitledByteSizeField
	      (tpanel, "Min Memory:", sTSize, vpanel, null, sVSize);
	}
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	{
	  pOverrideMinDiskField = 
	    UIFactory.createTitledBooleanField
	      (tpanel, "Override Min Disk:", sTSize, vpanel, sVSize);
	  pOverrideMinDiskField.addActionListener(this);
	  pOverrideMinDiskField.setActionCommand("disk-changed");

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pMinDiskField = 
	    UIFactory.createTitledByteSizeField
	      (tpanel, "Min Disk:", sTSize, vpanel, null, sVSize);
	}
	JDrawer drawer = 
	  new JDrawer("Machine Stats:", (JComponent) comps[2], true);
	vbox.add(drawer);
      }
      
      /* selection keys */ 
      {
	Box box = new Box(BoxLayout.Y_AXIS);

	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  tpanel.setName("TopTitlePanel");
	  JPanel vpanel = (JPanel) comps[1];
	  vpanel.setName("TopValuePanel");
	  
	  {
	    JBooleanField field = 
	      UIFactory.createTitledBooleanField(tpanel, "Override Selection Keys:", sTSize,
						vpanel, sVSize);
	    pOverrideSelectionKeysField = field;

	    field.addActionListener(this);
	    field.setActionCommand("selection-keys-changed");
	  }
	
	  box.add(comps[2]);
	}
	  
	pSelectionKeysBox = new Box(BoxLayout.Y_AXIS);
	box.add(pSelectionKeysBox);
	  
	JDrawer drawer = new JDrawer("Selection Keys:", box, false);
	vbox.add(drawer);
      }

      /* license keys */ 
      {
	Box box = new Box(BoxLayout.Y_AXIS);

	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  tpanel.setName("TopTitlePanel");
	  JPanel vpanel = (JPanel) comps[1];
	  vpanel.setName("TopValuePanel");
	  
	  {
	    JBooleanField field = 
	      UIFactory.createTitledBooleanField(tpanel, "Override License Keys:", sTSize,
						vpanel, sVSize);
	    pOverrideLicenseKeysField = field;

	    field.addActionListener(this);
	    field.setActionCommand("license-keys-changed");
	  }
	
	  box.add(comps[2]);
	}
	  
	pLicenseKeysBox = new Box(BoxLayout.Y_AXIS);
	box.add(pLicenseKeysBox);
	  
	JDrawer drawer = new JDrawer("License Keys:", box, false);
	vbox.add(drawer);
      }

      /* initialize the selection/license keys */ 
      doApply();

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize+30, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize+30, 7));
	
	vbox.add(spanel);
      }
      
      vbox.add(Box.createVerticalGlue());


      JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	
      super.initUI(title + ":", scroll, "Submit", "Reset", null, "Cancel");
      pack();
    }
  }
  
  public JQueueJobsDialog
  (
    Frame parent
  )
  {
    this(parent, DEFAULT_NAME, true); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Display the current selection/license keys.
   */ 
  public void 
  updateKeys() 
  {
    /* lookup latestselection/license keys */ 
    TreeSet<String> sknames = new TreeSet<String>();
    TreeSet<String> lknames = new TreeSet<String>();
    {
      QueueMgrClient qclient = UIMaster.getInstance().getQueueMgrClient();
      try {
	sknames.addAll(qclient.getSelectionKeyNames());
	lknames.addAll(qclient.getLicenseKeyNames());
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }
      
    /* selection keys panel */ 
    {
      boolean override = pOverrideSelectionKeysField.getValue();

      TreeSet<String> selected = new TreeSet<String>();
      for(String kname : pSelectionKeyFields.keySet()) {
	JBooleanField field = pSelectionKeyFields.get(kname);
	if(field != null) {
	  Boolean value = field.getValue(); 
	  if((value != null) && value) 
	    selected.add(kname);
	}
      }

      pSelectionKeysBox.removeAll();
      pSelectionKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");

      if(sknames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true;
	for(String kname : sknames) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, first ? 6 : 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", sTSize,
					       vpanel, sVSize);

	  field.setEnabled(override);
	  field.setValue(override && selected.contains(kname));

	  pSelectionKeyFields.put(kname, field);
	}
      }

      pSelectionKeysBox.add(comps[2]);
    }
      
    /* license keys panel */ 
    {
      boolean override = pOverrideLicenseKeysField.getValue();

      TreeSet<String> selected = new TreeSet<String>();
      for(String kname : pLicenseKeyFields.keySet()) {
	JBooleanField field = pLicenseKeyFields.get(kname);
	if(field != null) {
	  Boolean value = field.getValue(); 
	  if((value != null) && value) 
	    selected.add(kname);
	}
      }

      pLicenseKeysBox.removeAll();
      pLicenseKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");

      if(lknames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true;
	for(String kname : lknames) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, first ? 6 : 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", sTSize,
					       vpanel, sVSize);

	  field.setEnabled(override);
	  field.setValue(override && selected.contains(kname));

	  pLicenseKeyFields.put(kname, field);
	}
      }

      pLicenseKeysBox.add(comps[2]);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to override the batch size.
   */ 
  public boolean 
  overrideBatchSize() 
  {
    return pOverrideBatchSizeField.getValue();
  }
  
  /**
   * The overridden batch size.
   */ 
  public Integer 
  getBatchSize()
  {
    return pBatchSizeField.getValue();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the job priority.
   */ 
  public boolean 
  overridePriority() 
  {
    return pOverridePriorityField.getValue();
  }
  
  /**
   * The overridden priority
   */ 
  public Integer 
  getPriority()
  {
    return pPriorityField.getValue();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the ramp-up interval.
   */ 
  public boolean 
  overrideRampUp() 
  {
    return pOverrideRampUpField.getValue();
  }
  
  /**
   * The overridden ramp-up interval.
   */ 
  public Integer
  getRampUp()
  {
    return pRampUpField.getValue();
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the min memory interval.
   */ 
  public boolean 
  overrideMinMemory() 
  {
    return pOverrideMinMemoryField.getValue();
  }
  
  /**
   * The overridden min memory interval.
   */ 
  public Long
  getMinMemory()
  {
    return pMinMemoryField.getValue();
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the min disk interval.
   */ 
  public boolean 
  overrideMinDisk() 
  {
    return pOverrideMinDiskField.getValue();
  }
  
  /**
   * The overridden min disk interval.
   */ 
  public Long
  getMinDisk()
  {
    return pMinDiskField.getValue();
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the max load interval.
   */ 
  public boolean 
  overrideMaxLoad() 
  {
    return pOverrideMaxLoadField.getValue();
  }
  
  /**
   * The overridden max load interval.
   */ 
  public Float
  getMaxLoad()
  {
    return pMaxLoadField.getValue();
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to override the selection keys. 
   */ 
  public boolean 
  overrideSelectionKeys() 
  {
    return pOverrideSelectionKeysField.getValue();
  }

  /**
   * The names of the overriden selection keys. 
   */ 
  public TreeSet<String> 
  getSelectionKeys() 
  {
    TreeSet<String> keys = new TreeSet<String>();
    for(String kname : pSelectionKeyFields.keySet()) {
      JBooleanField field = pSelectionKeyFields.get(kname);
      if((field != null) && (field.getValue() != null) && field.getValue())
	keys.add(kname);
    }

    return keys;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to override the license keys. 
   */ 
  public boolean 
  overrideLicenseKeys() 
  {
    return pOverrideLicenseKeysField.getValue();
  }

  /**
   * The names of the overriden license keys. 
   */ 
  public TreeSet<String> 
  getLicenseKeys() 
  {
    TreeSet<String> keys = new TreeSet<String>();
    for(String kname : pLicenseKeyFields.keySet()) {
      JBooleanField field = pLicenseKeyFields.get(kname);
      if((field != null) && (field.getValue() != null) && field.getValue())
	keys.add(kname);
    }

    return keys;
  }
  
  public void
  disableBatchSize()
  {
    pBatchSizeField.setEnabled(false);
    pOverrideBatchSizeField.setEnabled(false);
  }
  
  public void
  enableBatchSize()
  {
    pBatchSizeField.setEnabled(true);
    pOverrideBatchSizeField.setEnabled(true);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden
  (
    @SuppressWarnings("unused")
    ComponentEvent e
  ) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved  
  (
    @SuppressWarnings("unused")
    ComponentEvent e
  ) {} 

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
  componentShown
  (
    @SuppressWarnings("unused")
    ComponentEvent e
  ) {} 



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
    if(cmd.equals("batch-size-changed")) 
      doBatchSizeChanged();
    else if(cmd.equals("priority-changed")) 
      doPriorityChanged();
    else if(cmd.equals("ramp-up-changed")) 
      doRampUpChanged();
    else if(cmd.equals("selection-keys-changed")) 
      doSelectionKeysChanged();
    else if(cmd.equals("license-keys-changed")) 
      doLicenseKeysChanged();
    else if (cmd.equals("disk-changed"))
      doMinDiskChanged();
    else if (cmd.equals("memory-changed"))
      doMinMemoryChanged();
    else if (cmd.equals("load-changed"))
      doMaxLoadChanged();
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The value of the override batch size field has changed.
   */ 
  public void 
  doBatchSizeChanged()
  {
    if(pOverrideBatchSizeField.getValue()) {
      pBatchSizeField.setEnabled(true);
      pBatchSizeField.setValue(0);
    }
    else {
      pBatchSizeField.setEnabled(false);
      pBatchSizeField.setValue(null);
    }
  }
  
  /**
   * The value of the override priority field has changed.
   */ 
  public void 
  doPriorityChanged()
  {
    if(pOverridePriorityField.getValue()) {
      pPriorityField.setEnabled(true);
      pPriorityField.setValue(JobReqs.defaultJobReqs().getPriority());
    }
    else {
      pPriorityField.setEnabled(false);
      pPriorityField.setValue(null);
    }
  }

  /**
   * The value of the override ramp-up interval field has changed.
   */ 
  public void 
  doRampUpChanged()
  {
    if(pOverrideRampUpField.getValue()) {
      pRampUpField.setEnabled(true);
      pRampUpField.setValue(JobReqs.defaultJobReqs().getRampUp());
    }
    else {
      pRampUpField.setEnabled(false);
      pRampUpField.setValue(null);
    }
  }

  /**
   * The value of the override min memory field has changed.
   */
  public void 
  doMinMemoryChanged()
  {
    if(pOverrideMinMemoryField.getValue()) {
      pMinMemoryField.setEnabled(true);
      pMinMemoryField.setValue(JobReqs.defaultJobReqs().getMinMemory());
    }
    else {
      pMinMemoryField.setEnabled(false);
      pMinMemoryField.setValue(null);
    }
  }
  
  /**
   * The value of the override min disk field has changed.
   */
  public void 
  doMinDiskChanged()
  {
    if(pOverrideMinDiskField.getValue()) {
      pMinDiskField.setEnabled(true);
      pMinDiskField.setValue(JobReqs.defaultJobReqs().getMinDisk());
    }
    else {
      pMinDiskField.setEnabled(false);
      pMinDiskField.setValue(null);
    }
  }
  
  /**
   * The value of the override min disk field has changed.
   */
  public void 
  doMaxLoadChanged()
  {
    if(pOverrideMaxLoadField.getValue()) {
      pMaxLoadField.setEnabled(true);
      pMaxLoadField.setValue(JobReqs.defaultJobReqs().getMaxLoad());
    }
    else {
      pMaxLoadField.setEnabled(false);
      pMaxLoadField.setValue(null);
    }
  }

  /**
   * The value of the override selection keys field has changed.
   */ 
  public void 
  doSelectionKeysChanged()
  {
    if(pOverrideSelectionKeysField.getValue()) {
      for(String kname : pSelectionKeyFields.keySet()) {
	JBooleanField field = pSelectionKeyFields.get(kname);
	if(field != null) {
	  field.setEnabled(true);
	  field.setValue(false);
	}
      }
    }
    else {
      for(String kname : pSelectionKeyFields.keySet()) {
	JBooleanField field = pSelectionKeyFields.get(kname);
	if(field != null) {
	  field.setEnabled(false);
	  field.setValue(null);
	}
      }
    }
  }

  /**
   * The value of the override license keys field has changed.
   */ 
  public void 
  doLicenseKeysChanged()
  {
    if(pOverrideLicenseKeysField.getValue()) {
      for(String kname : pLicenseKeyFields.keySet()) {
	JBooleanField field = pLicenseKeyFields.get(kname);
	if(field != null) {
	  field.setEnabled(true);
	  field.setValue(false);
	}
      }
    }
    else {
      for(String kname : pLicenseKeyFields.keySet()) {
	JBooleanField field = pLicenseKeyFields.get(kname);
	if(field != null) {
	  field.setEnabled(false);
	  field.setValue(null);
	}
      }
    }
  }

  /**
   * Reset the fields to default values. 
   */ 
  public void 
  doApply()
  { 
    /* job requirements panel */ 
    {
      pOverrideBatchSizeField.setValue(false);
      pBatchSizeField.setEnabled(false);
      pBatchSizeField.setValue(null);
     
      pOverridePriorityField.setValue(false);
      pPriorityField.setEnabled(false);
      pPriorityField.setValue(null);
    }

    /* lookup latestselection/license keys */ 
    TreeSet<String> sknames = new TreeSet<String>();
    TreeSet<String> lknames = new TreeSet<String>();
    {
      QueueMgrClient qclient = UIMaster.getInstance().getQueueMgrClient();
      try {
	sknames.addAll(qclient.getSelectionKeyNames());
	lknames.addAll(qclient.getLicenseKeyNames());
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }
      
    /* selection keys panel */ 
    {
      pOverrideSelectionKeysField.setValue(false);

      pSelectionKeysBox.removeAll();
      pSelectionKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");

      if(sknames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true;
	for(String kname : sknames) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, first ? 6 : 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", sTSize,
					       vpanel, sVSize);
	  field.setEnabled(false);
	  field.setValue(null);

	  pSelectionKeyFields.put(kname, field);
	}
      }

      pSelectionKeysBox.add(comps[2]);
    }
      
    /* license keys panel */ 
    {
      pOverrideLicenseKeysField.setValue(false);

      pLicenseKeysBox.removeAll();
      pLicenseKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");

      if(lknames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true;
	for(String kname : lknames) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, first ? 6 : 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", sTSize,
					       vpanel, sVSize);
	  field.setEnabled(false);
	  field.setValue(null);

	  pLicenseKeyFields.put(kname, field);
	}
      }

      pLicenseKeysBox.add(comps[2]);
    }
      
    pTopBox.revalidate();
    pTopBox.repaint();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8678401089074297592L;
  
  private static final String DEFAULT_NAME = "Queue Jobs Special";
  
  private static final int sTSize = 150;
  private static final int sVSize = 250;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top level container.
   */ 
  private Box  pTopBox; 

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to override the batch size.
   */ 
  private JBooleanField  pOverrideBatchSizeField;
  
  /**
   * The overridden batch size.
   */ 
  private JIntegerField  pBatchSizeField;
  

  /**
   * Whether to override the priority.
   */ 
  private JBooleanField  pOverridePriorityField;
  
  /**
   * The overridden priority.
   */ 
  private JIntegerField  pPriorityField;
  
  /**
   * Whether to override the ramp-up interval.
   */ 
  private JBooleanField  pOverrideRampUpField;
  
  /**
   * The overridden ramp-up interval.
   */ 
  private JIntegerField  pRampUpField;
  
  /**
   * Whether to override the Min Disk requirement.
   */
  private JBooleanField  pOverrideMinDiskField;
  
  /**
   * The overridden Min Disk value.
   */
  private JByteSizeField pMinDiskField;
  
  /**
   * Whether to override the Min Memory requirement.
   */
  private JBooleanField  pOverrideMinMemoryField;
  
  /**
   * The overridden Min Memory value.
   */
  private JByteSizeField pMinMemoryField;
  
  /**
   * Whether to override the Max Load requirement.
   */
  private JBooleanField pOverrideMaxLoadField;

  /**
   * The overridden Max Load value.
   */
  private JFloatField   pMaxLoadField;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the selection keys.
   */ 
  private JBooleanField  pOverrideSelectionKeysField;

  /**
   * The selection keys container.
   */ 
  private Box  pSelectionKeysBox;

  /**
   * Whether to export each selection key indexed by selection key name.
   */ 
  private TreeMap<String,JBooleanField>  pSelectionKeyFields;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to override the license keys.
   */ 
  private JBooleanField  pOverrideLicenseKeysField;

  /**
   * The license keys container.
   */ 
  private Box  pLicenseKeysBox;

  /**
   * Whether to export each license key indexed by license key name.
   */ 
  private TreeMap<String,JBooleanField>  pLicenseKeyFields;
  
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pBatching;
  
}
