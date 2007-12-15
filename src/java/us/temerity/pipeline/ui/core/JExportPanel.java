// $Id: JExportPanel.java,v 1.6 2007/12/15 07:29:10 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   E X P O R T   P A N E L                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Export node parameters panel.                                                  
 */ 
public 
class JExportPanel
  extends JPanel
  implements ActionListener, ComponentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JExportPanel
  (
   int channel, 
   String title, 
   int tsize, 
   int vsize 
  )
  {
    super();

    /* initialize fields */ 
    {
      pChannel = channel;

      pTSize = tsize; 
      pVSize = vsize; 

      pActionParamFields  = new TreeMap<String,JBooleanField>();
      pSelectionKeyFields = new TreeMap<String,JBooleanField>();
      pLicenseKeyFields   = new TreeMap<String,JBooleanField>();
      pHardwareKeyFields   = new TreeMap<String,JBooleanField>();
      pSourceFields       = new TreeMap<String,JBooleanField>();
      pAnnotationFields   = new TreeMap<String, JBooleanField>();
    }

    /* create panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      /* export all */ 
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	  
	JBooleanField field = 
	  UIFactory.createTitledBooleanField(tpanel, title, pTSize, 
					     vpanel, pVSize);
	pExportAllField = field;
	
	field.addActionListener(this);
	field.setActionCommand("export-all-changed");

	add(comps[2]);
      }

      /* properties panel */ 
      {
	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  pToolsetField = 
	    UIFactory.createTitledBooleanField(tpanel, "Toolset:", pTSize, 
					       vpanel, pVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  pEditorField = 
	    UIFactory.createTitledBooleanField(tpanel, "Editor:", pTSize, 
					       vpanel, pVSize);
	}
	  
	JDrawer drawer = new JDrawer("Properties:", (JComponent) comps[2], false);
	add(drawer);
      }
      
      /* actions panel */ 
      {
	Box abox = new Box(BoxLayout.Y_AXIS);
	pActionBox = abox;
	
	{
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  tpanel.setName("TopTitlePanel");
	  JPanel vpanel = (JPanel) comps[1];
	  vpanel.setName("TopValuePanel");

	  {	  
	    JBooleanField field = 
	      UIFactory.createTitledBooleanField(tpanel, "Action:", pTSize, 
						vpanel, pVSize);
	    pActionField = field;

	    field.addActionListener(this);
	    field.setActionCommand("action-changed");
	  }
	      
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pActionEnabledField =
	    UIFactory.createTitledBooleanField(tpanel, "Enabled:", pTSize, 
					      vpanel, pVSize);
				
	  UIFactory.addVerticalGlue(tpanel, vpanel);
	  
	  abox.add(comps[2]);
	}	 
	  
	{
	  Box apbox = new Box(BoxLayout.Y_AXIS);
	  pActionParamsBox = apbox;
	  
	  abox.add(apbox);
	}
	  
	{
	  Box jrbox = new Box(BoxLayout.X_AXIS);
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
	      Component comps[] = UIFactory.createTitledPanels();
	      {
		JPanel tpanel = (JPanel) comps[0];
		JPanel vpanel = (JPanel) comps[1];
		
		pOverflowPolicyField = 
		  UIFactory.createTitledBooleanField(tpanel, "Overflow Policy:", pTSize-7, 
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
		
		pExecutionMethodField = 
		  UIFactory.createTitledBooleanField(tpanel, "Execution Method:", pTSize-7,
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		pBatchSizeField = 
		  UIFactory.createTitledBooleanField(tpanel, "Batch Size:", pTSize-7,
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
		
		pPriorityField = 
		  UIFactory.createTitledBooleanField(tpanel, "Priority:", pTSize-7,
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
		
		pRampUpField = 
		  UIFactory.createTitledBooleanField(tpanel, "Ramp Up Interval:", pTSize-7,
		                                    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		pMaxLoadField = 
		  UIFactory.createTitledBooleanField(tpanel, "Maximum Load:", pTSize-7,
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		pMinMemoryField = 
		  UIFactory.createTitledBooleanField(tpanel, "Minimum Memory:", pTSize-7,
						    vpanel, pVSize);
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		pMinDiskField = 
		    UIFactory.createTitledBooleanField(tpanel, "Minimum Disk:", pTSize-7,
						      vpanel, pVSize);
	      }
	      
	      JDrawer drawer = 
		new JDrawer("Job Requirements:", (JComponent) comps[2], true);
	      dbox.add(drawer);
	    }
	      
	    /* selection keys */ 
	    {
	      Box box = new Box(BoxLayout.Y_AXIS);
	      pSelectionKeysBox = box;
	      
	      JDrawer drawer = new JDrawer("Selection Keys:", box, true);
	      dbox.add(drawer);
	    }
	    
	    /* license keys */ 
	    {
	      Box box = new Box(BoxLayout.Y_AXIS);
	      pLicenseKeysBox = box;
	      
	      JDrawer drawer = new JDrawer("License Keys:", box, true);
	      dbox.add(drawer);
	    }
	    
	    /* hardware keys */ 
            {
              Box box = new Box(BoxLayout.Y_AXIS);
              pHardwareKeysBox = box;
              
              JDrawer drawer = new JDrawer("Hardware Keys:", box, true);
              dbox.add(drawer);
            }
	    
	    jrbox.add(dbox);
	  }
	  
	  abox.add(jrbox);
	}
  
	JDrawer drawer = new JDrawer("Regeneration Action:", abox, false);
	add(drawer);
      }

      /* sources panel */ 
      {
	JDrawer drawer = new JDrawer("Sources:", new JPanel(), false);
	pSourcesDrawer = drawer;
	add(drawer);
      }
      
      /* annotation panel */
      {
	JDrawer drawer = new JDrawer("Annotations:", new JPanel(), false);
	pAnnotationDrawer = drawer;
	add(drawer);
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
  
  public boolean
  exportRampUpInterval()
  {
    return pRampUpField.getValue();
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
  
  /**
   * The names of the exported hardware keys. 
   */ 
  public TreeSet<String> 
  exportedHardwareKeys() 
  {
    TreeSet<String> exported = new TreeSet<String>();
    for(String kname : pHardwareKeyFields.keySet()) {
      JBooleanField field = pHardwareKeyFields.get(kname);
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
  
  /**
   * Whether to export the node annotation to the given source node.
   */ 
  public boolean
  exportAnnotation
  (
   String sname
  ) 
  {
    JBooleanField field = pAnnotationFields.get(sname);
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
    pExportAllField.setValue(true);

    /* properties panel */ 
    {
      pToolsetField.setValue(true);
      pEditorField.setValue(true);
    }

    /* actions panel */ 
    {
      pActionField.setValue(true);

      pActionParamsBox.removeAll();
      
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      tpanel.setName("BottomTitlePanel");
      JPanel vpanel = (JPanel) comps[1];
      vpanel.setName("BottomValuePanel");
      
      pActionParamFields.clear();
      pActionSourceParamsField = null;
      BaseAction action = mod.getAction();
      if((action == null) || (!action.hasSingleParams() && !action.supportsSourceParams())) {
	tpanel.add(Box.createRigidArea(new Dimension(pTSize, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	UIFactory.addVerticalSpacer(tpanel, vpanel, 9);
	
	for(ActionParam param : action.getSingleParams()) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, param.getNameUI() + ":", pTSize,
					      vpanel, pVSize);

	  pActionParamFields.put(param.getName(), field);
	}

	pActionSourceParamsField = null;
	if(action.supportsSourceParams()) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  pActionSourceParamsField = 
	    UIFactory.createTitledBooleanField(tpanel, "Source Parameters:", pTSize,
					      vpanel, pVSize);
	}
      }
      pActionParamsBox.add(comps[2]);	

      doActionChanged();
    }

    /* job requirements panel */ 
    {
      pOverflowPolicyField.setValue(true);
      pExecutionMethodField.setValue(true);
      pBatchSizeField.setValue(true);
      pPriorityField.setValue(true);
      pRampUpField.setValue(true);
      pMaxLoadField.setValue(true);
      pMinMemoryField.setValue(true);
      pMinDiskField.setValue(true);
    }

    /* license keys panel */ 
    {
      TreeSet<String> knames = new TreeSet<String>();
      {
	UIMaster master = UIMaster.getInstance();
	try {
	  knames.addAll(master.getQueueMgrClient(pChannel).getLicenseKeyNames(true));
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
      
      pLicenseKeysBox.removeAll();
      pLicenseKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      if(knames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(pTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true; 
	for(String kname : knames) {
	  if(!first) 
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", pTSize-7,
					      vpanel, pVSize);
	  field.setValue(true);

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
	  knames.addAll(master.getQueueMgrClient(pChannel).getSelectionKeyNames(true));
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
      
      pSelectionKeysBox.removeAll();
      pSelectionKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      if(knames.isEmpty()) {
	tpanel.add(Box.createRigidArea(new Dimension(pTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      else {
	boolean first = true; 
	for(String kname : knames) {
	  if(!first) 
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  first = false;

	  JBooleanField field = 
	    UIFactory.createTitledBooleanField(tpanel, kname + ":", pTSize-7,
					      vpanel, pVSize);
	  field.setValue(true);

	  pSelectionKeyFields.put(kname, field);
	}
      }

      pSelectionKeysBox.add(comps[2]);
    }
    
    /* hardware keys panel */ 
    {
      TreeSet<String> knames = new TreeSet<String>();
      {
        UIMaster master = UIMaster.getInstance();
        try {
          knames.addAll(master.getQueueMgrClient(pChannel).getHardwareKeyNames(true));
        }
        catch(PipelineException ex) {
          master.showErrorDialog(ex);
        }
      }
      
      pHardwareKeysBox.removeAll();
      pHardwareKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      if(knames.isEmpty()) {
        tpanel.add(Box.createRigidArea(new Dimension(pTSize-7, 0)));
        vpanel.add(Box.createHorizontalGlue());
      }
      else {
        boolean first = true; 
        for(String kname : knames) {
          if(!first) 
            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          first = false;

          JBooleanField field = 
            UIFactory.createTitledBooleanField(tpanel, kname + ":", pTSize-7,
                                              vpanel, pVSize);
          field.setValue(true);

          pHardwareKeyFields.put(kname, field);
        }
      }

      pHardwareKeysBox.add(comps[2]);
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
	    Component comps[] = UIFactory.createTitledPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      JBooleanField field = 
		UIFactory.createTitledBooleanField(tpanel, "Export Link:", pTSize-14, 
						  vpanel, pVSize);
	      field.setValue(true);

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
	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  tpanel.add(Box.createRigidArea(new Dimension(pTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	
	pSourcesDrawer.setContents((JComponent) comps[2]);
      }
    }
    
    /* annotation panels */ 
    {
      pAnnotationFields.clear();
      UIMaster master = UIMaster.getInstance();
      TreeMap<String, BaseAnnotation> annots = new TreeMap<String, BaseAnnotation>();
      try {
        annots = master.getMasterMgrClient(pChannel).getAnnotations(mod.getName());
      }
      catch (PipelineException ex) {
	master.showErrorDialog(ex);
      }
      
      if(!annots.isEmpty()) {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.addComponentListener(this);
        hbox.add(UIFactory.createSidebar());
	
	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  for(String sname : annots.keySet()) {
	    Component comps[] = UIFactory.createTitledPanels();
	    {
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      JBooleanField field = 
		UIFactory.createTitledBooleanField(tpanel, "Export Annotation:", pTSize-14, 
						  vpanel, pVSize);
	      field.setValue(true);

	      pAnnotationFields.put(sname, field);
	    }
	  
	    JDrawer drawer = new JDrawer(sname, (JComponent) comps[2], true);
	    vbox.add(drawer);
	  }
	  
	  hbox.add(vbox);
	}
      
	pAnnotationDrawer.setContents(hbox);
      }   
      else {
	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  tpanel.add(Box.createRigidArea(new Dimension(pTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	
	pAnnotationDrawer.setContents((JComponent) comps[2]);
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
    else if(cmd.equals("export-all-changed")) 
      doExportAllChanged();
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


  /**
   * The export all field has been toggled.
   */ 
  private void 
  doExportAllChanged()
  {
    boolean exportAll = pExportAllField.getValue();
    
    pToolsetField.setValue(exportAll);
    pEditorField.setValue(exportAll);

    pActionField.setValue(exportAll);
    pOverflowPolicyField.setValue(exportAll);

    pExecutionMethodField.setValue(exportAll);
    pBatchSizeField.setValue(exportAll);
    pRampUpField.setValue(exportAll);
    pPriorityField.setValue(exportAll);
    pMaxLoadField.setValue(exportAll);
    pMinMemoryField.setValue(exportAll);
    pMinDiskField.setValue(exportAll);

    for(JBooleanField field : pSelectionKeyFields.values()) 
      field.setValue(exportAll);

    for(JBooleanField field : pLicenseKeyFields.values()) 
      field.setValue(exportAll);
    
    for(JBooleanField field : pHardwareKeyFields.values()) 
      field.setValue(exportAll);
    
    for(JBooleanField field : pSourceFields.values()) 
      field.setValue(exportAll);
    
    for (JBooleanField field : pAnnotationFields.values())
      field.setValue(exportAll);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5029125355685904735L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The index of the update channel.
   */ 
  private int  pChannel; 

  /**
   * Title and value panel widths.
   */ 
  private int  pTSize; 
  private int  pVSize; 

  /**
   * The export all field.
   */ 
  private JBooleanField   pExportAllField; 


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
   * Whether to export the ramp up interval
   */
  private JBooleanField  pRampUpField;
  
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
   * The hardware keys container.
   */ 
  private Box  pHardwareKeysBox;

  /**
   * Whether to export each hardware key indexed by hardware key name.
   */ 
  private TreeMap<String,JBooleanField>  pHardwareKeyFields;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The drawer containing the source links. 
   */ 
  private JDrawer  pSourcesDrawer; 

  /**
   * Whether to export each source link indexed by the name of the upstream node.
   */ 
  private TreeMap<String,JBooleanField>  pSourceFields;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The drawer containing all the annotations.
   */
  private JDrawer  pAnnotationDrawer;

  /**
   * Where to export each annotation indexed by the name of the annotation.
   */
  private TreeMap<String, JBooleanField> pAnnotationFields;

  
}
