// $Id: JExportDialog.java,v 1.2 2004/10/29 14:03:52 jim Exp $

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
/*   E X P O R T   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Export node parameters dialog.                                                  
 */ 
public 
class JExportDialog
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
  JExportDialog() 
  {
    super("Export", true);

    /* initialize fields */ 
    {
      pActionParamFields  = new TreeMap<String,JBooleanField>();
      pSelectionKeyFields = new TreeMap<String,JBooleanField>();
      pLicenseKeyFields   = new TreeMap<String,JBooleanField>();
      pSourceFields       = new TreeMap<String,JBooleanField>();
    }


    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      /* properties panel */ 
      {
	Component comps[] = UIMaster.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  pToolsetField = 
	    UIMaster.createTitledBooleanField(tpanel, "Toolset:", sTSize, 
					      vpanel, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  pEditorField = 
	    UIMaster.createTitledBooleanField(tpanel, "Editor:", sTSize, 
					      vpanel, sVSize);
	}
	  
	JDrawer drawer = new JDrawer("Properties:", (JComponent) comps[2], true);
	vbox.add(drawer);
      }
      
      /* actions panel */ 
      {
	Box abox = new Box(BoxLayout.Y_AXIS);
	pActionBox = abox;
	
	{
	  Component comps[] = UIMaster.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  tpanel.setName("TopTitlePanel");
	  JPanel vpanel = (JPanel) comps[1];
	  vpanel.setName("TopValuePanel");

	  {	  
	    JBooleanField field = 
	      UIMaster.createTitledBooleanField(tpanel, "Action:", sTSize, 
						vpanel, sVSize);
	    pActionField = field;

	    field.addActionListener(this);
	    field.setActionCommand("action-changed");
	  }
	      
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pActionEnabledField =
	    UIMaster.createTitledBooleanField(tpanel, "Enabled:", sTSize, 
					      vpanel, sVSize);
				
	  UIMaster.addVerticalGlue(tpanel, vpanel);
	  
	  abox.add(comps[2]);
	}	 
	  
	{
	  Box apbox = new Box(BoxLayout.Y_AXIS);
	  pActionParamsBox = apbox;
	  
	  abox.add(apbox);
	}
	  
	{
	  Box jrbox = new Box(BoxLayout.X_AXIS);
	  pJobReqsBox = jrbox;
	  
	  jrbox.addComponentListener(this);
	  
	  {
	    JPanel spanel = new JPanel();
	    pJobReqsSpacer = spanel;
	    spanel.setName("Spacer");
	    
	    spanel.setMinimumSize(new Dimension(7, 0));
	    spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	    spanel.setPreferredSize(new Dimension(7, 0));
	    
	    jrbox.add(spanel);
	  }
	    
	  { 
	    Box dbox = new Box(BoxLayout.Y_AXIS);
	      
	    /* job requirements */ 
	    {
	      Component comps[] = UIMaster.createTitledPanels();
	      {
		JPanel tpanel = (JPanel) comps[0];
		JPanel vpanel = (JPanel) comps[1];
		
		pOverflowPolicyField = 
		  UIMaster.createTitledBooleanField(tpanel, "Overflow Policy:", sTSize-7, 
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
		
		pExecutionMethodField = 
		  UIMaster.createTitledBooleanField(tpanel, "Execution Method:", sTSize-7,
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
		
		pBatchSizeField = 
		  UIMaster.createTitledBooleanField(tpanel, "Batch Size:", sTSize-7,
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
		
		pPriorityField = 
		  UIMaster.createTitledBooleanField(tpanel, "Priority:", sTSize-7,
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
		
		pMaxLoadField = 
		  UIMaster.createTitledBooleanField(tpanel, "Maximum Load:", sTSize-7,
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
		
		pMinMemoryField = 
		  UIMaster.createTitledBooleanField(tpanel, "Minimum Memory:", sTSize-7,
						    vpanel, sVSize);
		
		UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
		  
		pMinDiskField = 
		    UIMaster.createTitledBooleanField(tpanel, "Minimum Disk:", sTSize-7,
						      vpanel, sVSize);
	      }
	      
	      JDrawer drawer = 
		new JDrawer("Job Requirements:", (JComponent) comps[2], false);
	      dbox.add(drawer);
	    }
	      
	    /* selection keys */ 
	    {
	      Box box = new Box(BoxLayout.Y_AXIS);
	      pSelectionKeysBox = box;
	      
	      JDrawer drawer = new JDrawer("Selection Keys:", box, false);
	      dbox.add(drawer);
	    }
	    
	    /* license keys */ 
	    {
	      Box box = new Box(BoxLayout.Y_AXIS);
	      pLicenseKeysBox = box;
	      
	      JDrawer drawer = new JDrawer("License Keys:", box, false);
	      dbox.add(drawer);
	    }
	    
	    jrbox.add(dbox);
	  }
	  
	  abox.add(jrbox);
	}
  
	JDrawer drawer = new JDrawer("Regeneration Action:", abox, true);
	vbox.add(drawer);
      }

      /* sources panel */ 
      {
	JDrawer drawer = new JDrawer("Sources:", new JPanel(), false);
	pSourcesDrawer = drawer;
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
	
	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
	
	super.initUI("X", true, scroll, "Export", null, null, "Cancel");
      }
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to export the toolset parameter.
   */ 
  public boolean 
  exportToolset() 
  {
    return pToolsetField.getValue();
  }
    
  /**
   * Whether to export the editor parameter.
   */ 
  public boolean 
  exportEditor() 
  {
    return pEditorField.getValue();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to export the regeneration action.
   */ 
  public boolean 
  exportAction() 
  {
    return pActionField.getValue();
  }
  
  /**
   * Whether to export whether the regeneration action is enabled.
   */ 
  public boolean 
  exportActionEnabled() 
  {
    if((pActionEnabledField != null) && (pActionEnabledField.getValue() != null))
      return pActionEnabledField.getValue();
    return false;
  }

  /**
   * Whether to export the single valued regeneration action parameter with the given name.
   * 
   * @param pname
   *   The name of the single valued action parameter.
   */ 
  public boolean 
  exportActionSingleParam
  (
   String pname
  ) 
  {
    JBooleanField field = pActionParamFields.get(pname);
    if((field != null) && (field.getValue() != null))
      return field.getValue();
    return false;
  }
  
  /**
   * Whether to export per-source regeneration action parameters.
   */ 
  public boolean 
  exportActionSourceParams() 
  {
    if((pActionSourceParamsField != null) && (pActionSourceParamsField.getValue() != null))
      return pActionSourceParamsField.getValue();
    return false;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to export the overflow policy.
   */ 
  public boolean 
  exportOverflowPolicy() 
  {
    return pOverflowPolicyField.getValue();
  }
  
  /**
   * Whether to export the execution method.
   */ 
  public boolean 
  exportExecutionMethod() 
  {
    return pExecutionMethodField.getValue();
  }
  
  /**
   * Whether to export the batch size.
   */ 
  public boolean 
  exportBatchSize() 
  {
    return pBatchSizeField.getValue();
  }
  
  /**
   * Whether to export the job priority.
   */ 
  public boolean 
  exportPriority() 
  {
    return pPriorityField.getValue();
  }
  
  /**
   * Whether to export the max system load.
   */ 
  public boolean 
  exportMaxLoad() 
  {
    return pMaxLoadField.getValue();
  }
  
  /**
   * Whether to export the minimum free memory.
   */ 
  public boolean 
  exportMinMemory() 
  {
    return pMinMemoryField.getValue();
  }
  
  /**
   * Whether to export the minimum free disk space.
   */ 
  public boolean 
  exportMinDisk() 
  {
    return pMinDiskField.getValue();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The names of the exported selection keys. 
   */ 
  public TreeSet<String> 
  exportedSelectionKeys() 
  {
    TreeSet<String> exported = new TreeSet<String>();
    for(String kname : pSelectionKeyFields.keySet()) {
      JBooleanField field = pSelectionKeyFields.get(kname);
      if((field != null) && (field.getValue() != null) && field.getValue())
	exported.add(kname);
    }

    return exported;
  }

  /**
   * The names of the exported license keys. 
   */ 
  public TreeSet<String> 
  exportedLicenseKeys() 
  {
    TreeSet<String> exported = new TreeSet<String>();
    for(String kname : pLicenseKeyFields.keySet()) {
      JBooleanField field = pLicenseKeyFields.get(kname);
      if((field != null) && (field.getValue() != null) && field.getValue())
	exported.add(kname);
    }

    return exported;
  }




  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to export the upstream link information to the given source node.
   */ 
  public boolean
  exportSource
  (
   String sname
  ) 
  {
    JBooleanField field = pSourceFields.get(sname);
    if((field != null) && (field.getValue() != null))
      return field.getValue();
    return false;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection fields.
   */ 
  public void 
  updateNode
  (
   NodeMod mod
  )
  { 
    pHeaderLabel.setText("Export:  " + mod);

    /* properties panel */ 
    {
      pToolsetField.setValue(false);
      pEditorField.setValue(false);
    }

    /* actions panel */ 
    {
      pActionField.setValue(false);

      pActionParamsBox.removeAll();
      
      Component comps[] = UIMaster.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");
      
      pActionParamFields.clear();
      pActionSourceParamsField = null;
      BaseAction action = mod.getAction();
      if((action == null) || (!action.hasSingleParams() && !action.supportsSourceParams())) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	UIMaster.addVerticalSpacer(tpanel, vpanel, 9);
	
	for(BaseActionParam param : action.getSingleParams()) {
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, param.getNameUI() + ":", sTSize,
					      vpanel, sVSize);

	  pActionParamFields.put(param.getName(), field);
	}

	pActionSourceParamsField = null;
	if(action.supportsSourceParams()) {
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  pActionSourceParamsField = 
	    UIMaster.createTitledBooleanField(tpanel, "Source Parameters:", sTSize,
					      vpanel, sVSize);
	}
      }
      pActionParamsBox.add(comps[2]);	

      doActionChanged();
    }

    /* job requirements panel */ 
    {
      pOverflowPolicyField.setValue(false);
      pExecutionMethodField.setValue(false);
      pBatchSizeField.setValue(false);
      pPriorityField.setValue(false);
      pMaxLoadField.setValue(false);
      pMinMemoryField.setValue(false);
      pMinDiskField.setValue(false);
    }

    /* license keys panel */ 
    {
      TreeSet<String> knames = new TreeSet<String>();
      {
	UIMaster master = UIMaster.getInstance();
	try {
	  knames.addAll(master.getQueueMgrClient().getLicenseKeyNames());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
      
      pLicenseKeysBox.removeAll();
      pLicenseKeyFields.clear();

      Component comps[] = UIMaster.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      if(knames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true; 
	for(String kname : knames) {
	  if(!first) 
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  first = false;

	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, kname + ":", sTSize-7,
					      vpanel, sVSize);

	  pLicenseKeyFields.put(kname, field);
	}
      }

      pLicenseKeysBox.add(comps[2]);
    }

    /* selection keys panel */ 
    {
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
      JPanel vpanel = (JPanel) comps[1];

      if(knames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true; 
	for(String kname : knames) {
	  if(!first) 
	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  first = false;

	  JBooleanField field = 
	    UIMaster.createTitledBooleanField(tpanel, kname + ":", sTSize-7,
					      vpanel, sVSize);

	  pSelectionKeyFields.put(kname, field);
	}
      }

      pSelectionKeysBox.add(comps[2]);
    }
      
    pActionBox.revalidate();
    pActionBox.repaint();
    
    /* source panels */ 
    {
      pSourceFields.clear();
      
      if(mod.hasSources()) {
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
	  
	  for(String sname : mod.getSourceNames()) {
	    Component comps[] = UIMaster.createTitledPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      JBooleanField field = 
		UIMaster.createTitledBooleanField(tpanel, "Export Link:", sTSize-14, 
						  vpanel, sVSize);

	      pSourceFields.put(sname, field);
	    }
	  
	    JDrawer drawer = new JDrawer(sname, (JComponent) comps[2], true);
	    vbox.add(drawer);
	  }
	  
	  hbox.add(vbox);
	}
      
	pSourcesDrawer.setContents(hbox);
      }   
      else {
	Component comps[] = UIMaster.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	
	pSourcesDrawer.setContents((JComponent) comps[2]);
      }
    } 
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
    if(cmd.equals("action-changed")) 
      doActionChanged();
    else 
      super.actionPerformed(e);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The action field has been toggled.
   */ 
  private void 
  doActionChanged()
  {
    boolean hasAction = pActionField.getValue();

    pActionEnabledField.setEnabled(hasAction);
    pActionEnabledField.setValue(hasAction ? true : null);

    for(JBooleanField field : pActionParamFields.values()) {
      field.setEnabled(hasAction);
      field.setValue(hasAction ? true : null);
    }

    if(pActionSourceParamsField != null) {
      pActionSourceParamsField.setEnabled(hasAction);
      pActionSourceParamsField.setValue(hasAction ? true : null);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4818946288161690402L;
  
  private static final int sTSize = 120;
  private static final int sVSize = 180;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to export the toolset property.
   */ 
  private JBooleanField  pToolsetField; 

  /**
   * Whether to export the editor property.
   */ 
  private JBooleanField  pEditorField; 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level container of the actions drawer.
   */ 
  private Box  pActionBox;


  /**
   * Whether to export the regeneration action.
   */ 
  private JBooleanField  pActionField; 

  /**
   * Whether to export whether the action is enabled.
   */ 
  private JBooleanField  pActionEnabledField; 
  
  
  /**
   * The action parameters container.
   */ 
  private Box  pActionParamsBox;

  /**
   * Whether to export each single action parameter indexed by parameter name.
   */ 
  private TreeMap<String,JBooleanField>  pActionParamFields;

  /**
   * Whether to export whether the per-source action parameters.
   */ 
  private JBooleanField  pActionSourceParamsField; 
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The job requirements container.
   */ 
  private Box  pJobReqsBox;

  /**
   * The job requirements spacer.
   */ 
  private JPanel  pJobReqsSpacer;

  
  /**
   * Whether to export the overflow policy.
   */ 
  private JBooleanField  pOverflowPolicyField;
  
  /**
   * Whether to export the execution method.
   */ 
  private JBooleanField  pExecutionMethodField;
  
  /**
   * Whether to export the batch size.
   */ 
  private JBooleanField  pBatchSizeField;
  
  /**
   * Whether to export the job priority.
   */ 
  private JBooleanField  pPriorityField;
  
  /**
   * Whether to export the maximum system load.
   */ 
  private JBooleanField  pMaxLoadField;
  
  /**
   * Whether to export the minimum free memory.
   */ 
  private JBooleanField  pMinMemoryField;
  
  /**
   * Whether to export the minimum free disk space.
   */ 
  private JBooleanField  pMinDiskField;
  

  /*----------------------------------------------------------------------------------------*/

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
   * The license keys container.
   */ 
  private Box  pLicenseKeysBox;

  /**
   * Whether to export each license key indexed by license key name.
   */ 
  private TreeMap<String,JBooleanField>  pLicenseKeyFields;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The drawer containing the source links. 
   */ 
  private JDrawer  pSourcesDrawer; 

  /**
   * Whether to export each source link indexed by the name of the upstream node.
   */ 
  private TreeMap<String,JBooleanField>  pSourceFields;
  
}
