// $Id: JQueueJobsDialog.java,v 1.4 2004/12/07 04:55:17 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B S   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The job submission parameters dialog.
 */ 
public 
class JQueueJobsDialog
  extends JBaseDialog
  implements ComponentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JQueueJobsDialog() 
  {
    super("Queue Jobs Special", true);

    /* initialize fields */ 
    {
      pSelectionKeyFields = new TreeMap<String,JBooleanField>();
    }

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      pTopBox = vbox;

      /* job requirements */ 
      {
	Component comps[] = UIMaster.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  {
	    {
	      JBooleanField field = 
		UIMaster.createTitledBooleanField(tpanel, "Override Batch Size:", sTSize,
						  vpanel, sVSize);
	      pOverrideBatchSizeField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("batch-size-changed");
	    }
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pBatchSizeField = 
	      UIMaster.createTitledIntegerField(tpanel, "Batch Size:", sTSize,
						vpanel, null, sVSize);
	  }
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  {
	    {
	      JBooleanField field = 
		UIMaster.createTitledBooleanField(tpanel, "Override Priority:", sTSize,
						  vpanel, sVSize);
	      pOverridePriorityField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("priority-changed");
	    }
	    
	    pOverridePriorityField.addActionListener(this);
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pPriorityField = 
	      UIMaster.createTitledIntegerField(tpanel, "Priority:", sTSize,
						vpanel, null, sVSize);
	  }

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  {
	    {
	      JBooleanField field = 
		UIMaster.createTitledBooleanField(tpanel, "Override Ramp Up:", sTSize,
						  vpanel, sVSize);
	      pOverrideRampUpField = field;
	      
	      field.addActionListener(this);
	      field.setActionCommand("ramp-up-changed");
	    }
	    
	    pOverrideRampUpField.addActionListener(this);
	    
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    pRampUpField = 
	      UIMaster.createTitledIntegerField(tpanel, "Ramp Up Interval:", sTSize,
						vpanel, null, sVSize);
	  }
	}
	
	JDrawer drawer = 
	  new JDrawer("Job Requirements:", (JComponent) comps[2], true);
	vbox.add(drawer);
      }
      
      /* selection keys */ 
      {
	Box box = new Box(BoxLayout.Y_AXIS);

	Component comps[] = UIMaster.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  tpanel.setName("TopTitlePanel");
	  JPanel vpanel = (JPanel) comps[1];
	  vpanel.setName("TopValuePanel");
	  
	  {
	    JBooleanField field = 
	      UIMaster.createTitledBooleanField(tpanel, "Override Selection Keys:", sTSize,
						vpanel, sVSize);
	    pOverrideSelectionKeysField = field;

	    field.addActionListener(this);
	    field.setActionCommand("selection-keys-changed");
	  }
	
	  box.add(comps[2]);
	}
	  
	pSelectionKeysBox = new Box(BoxLayout.Y_AXIS);
	box.add(pSelectionKeysBox);
	  
	JDrawer drawer = new JDrawer("Selection Keys:", box, true);
	vbox.add(drawer);
      }

      /* initialize the selection keys */ 
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


      JScrollPane scroll = new JScrollPane(vbox);
      {	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
	
      super.initUI("Queue Jobs Special:", true, scroll, "Submit", "Reset", null, "Cancel");
    }
    
    setSize(new Dimension(419, 342));
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S                                                                            */
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
    if(cmd.equals("batch-size-changed")) 
      doBatchSizeChanged();
    else if(cmd.equals("priority-changed")) 
      doPriorityChanged();
    else if(cmd.equals("ramp-up-changed")) 
      doRampUpChanged();
    else if(cmd.equals("selection-keys-changed")) 
      doSelectionKeysChanged();
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

    /* selection keys panel */ 
    {
      pOverrideSelectionKeysField.setValue(false);

      TreeSet<String> knames = new TreeSet<String>();
      {
	UIMaster master = UIMaster.getInstance();
	try {
	  knames.addAll(master.getQueueMgrClient().getSelectionKeyNames());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
      
      pSelectionKeysBox.removeAll();
      pSelectionKeyFields.clear();

      Component comps[] = UIMaster.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");

      if(knames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true;
	for(String kname : knames) {
	  UIMaster.addVerticalSpacer(tpanel, vpanel, first ? 6 : 3);
	  first = false;

	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, kname + ":", sTSize,
					      vpanel, sVSize);
	  field.setEnabled(false);
	  field.setValue(null);

	  pSelectionKeyFields.put(kname, field);
	}
      }

      pSelectionKeysBox.add(comps[2]);
    }
      
    pTopBox.revalidate();
    pTopBox.repaint();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8678401089074297592L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 200;


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
   * The overriden batch size.
   */ 
  private JIntegerField  pBatchSizeField;
  

  /**
   * Whether to override the priority.
   */ 
  private JBooleanField  pOverridePriorityField;
  
  /**
   * The overriden priority.
   */ 
  private JIntegerField  pPriorityField;
  

  /**
   * Whether to override the ramp-up interval.
   */ 
  private JBooleanField  pOverrideRampUpField;
  
  /**
   * The overriden ramp-up interval.
   */ 
  private JIntegerField  pRampUpField;

  
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
  
  
}
