// $Id: JArchiveParamsDialog.java,v 1.4 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   P A R A M S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to select the Archiver plugin and its parameters to use when archiving nodes.
 */ 
public 
class JArchiveParamsDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JArchiveParamsDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Archive", true);

    /* initialize fields */ 
    {
      pArchiverParamComponents = new TreeMap<String,Component>();
    }

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  
      
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	  
	{
	  JIdentifierField field = 
	    UIFactory.createTitledIdentifierField
	    (tpanel, "Volume Prefix:", sTSize, 
	     vpanel, "Archive", sVSize, 
	     "The prefix to prepend to the names of created archive volumes.");
	  pPrefixField = field;
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  JByteSizeField field = 
	    UIFactory.createTitledByteSizeField
	    (tpanel, "Minimum Size:", sTSize, 
	     vpanel, 1073741824L, sVSize, 
	     "The mimimum archive volume size (in bytes).");
	  pMinSizeField = field;
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");

	  JCollectionField field = 
	    UIFactory.createTitledCollectionField
	    (tpanel, "Toolset:", sTSize, 
	     vpanel, values, sVSize, 
	     "The name of the shell environment under which the Archiver plugin is run.");
	  pToolsetField = field;

	  field.setActionCommand("toolset-changed");
	  field.addActionListener(this);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  JLabel label = 
	    UIFactory.createFixedLabel
	    ("Archiver:", sTSize, JLabel.RIGHT, 
	     "The name of the Archiver plugin used to archive/restore node versions.");

	  tpanel.add(label);

	  JPluginSelectionField field = 
	    UIMaster.getInstance().createArchiverSelectionField(sVSize);
	  pArchiverField = field;
	  
	  field.setActionCommand("archiver-changed");
	  field.addActionListener(this);
	  
	  vpanel.add(field);
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	{
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Version:", sTSize, 
	     vpanel, "-", sVSize, 
	     "The revision number of the Archiver plugin.");
	  pArchiverVersionField = field;
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Vendor:", sTSize, 
	     vpanel, "-", sVSize, 
	     "The name of the Archiver plugin vendor.");
	  pArchiverVendorField = field;
	}
	  
	body.add(comps[2]);
      }

      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	JDrawer drawer = new JDrawer("Archiver Parameters:", (JComponent) comps[2], true);
	drawer.setToolTipText(UIFactory.formatToolTip("Archiver plugin parameters."));
	pArchiverParamsDrawer = drawer;
	body.add(drawer);
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	
	body.add(spanel);
      }

      JScrollPane scroll = new JScrollPane(body);
      {
	scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
	Dimension size = new Dimension(sTSize+sVSize+52, 300);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }

      super.initUI("Create Archive Volumes:", true, scroll, "Archive", null, null, "Cancel");
      pack();
    }  

    updateArchiver();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the prefix to prepend to the names of created archive volumes.
   */ 
  public String
  getPrefix()
  {
    return pPrefixField.getText();
  }

  /**
   * The The mimimum archive volume size (in bytes).
   */ 
  public Long
  getMinSize() 
  {
    return pMinSizeField.getValue();
  }

  /**
   * Get the name of the toolset environment under which the archiver plugin is run.
   */ 
  public String
  getToolset()
  {
    return pToolset;
  }

  /**
   * Get the archiver plugin instance to use to archive the nodes.
   */
  public BaseArchiver
  getArchiver() 
  {
    if(pArchiver != null) {
      for(ArchiverParam aparam : pArchiver.getParams()) {
	Component comp = pArchiverParamComponents.get(aparam.getName()); 
	Comparable value = null;
	if(aparam instanceof BooleanArchiverParam) {
	  JBooleanField field = (JBooleanField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof ByteSizeArchiverParam) {
	  JByteSizeField field = (JByteSizeField) comp;
	  value = field.getValue();	  
	}
	else if(aparam instanceof DirectoryArchiverParam) {
	  JPathField field = (JPathField) comp;
	  value = field.getText();
	}
	else if(aparam instanceof DoubleArchiverParam) {
	  JDoubleField field = (JDoubleField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof EnumArchiverParam) {
	  JCollectionField field = (JCollectionField) comp;
	  EnumArchiverParam eparam = (EnumArchiverParam) aparam;
	  value = eparam.getValueOfIndex(field.getSelectedIndex());
	}
	else if(aparam instanceof IntegerArchiverParam) {
	  JIntegerField field = (JIntegerField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof StringArchiverParam) {
	  JTextField field = (JTextField) comp;
	  value = field.getText();	  
	}
	else {
	  assert(false) : "Unknown archiver parameter type!";
	}

	pArchiver.setParamValue(aparam.getName(), value);
      }
    }

    return pArchiver;
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components.
   */
  private void 
  updateArchiver() 
  { 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();

    pToolsetField.removeActionListener(this);
    {
      TreeSet<String> toolsets = new TreeSet<String>();
      try {
	if(pToolset == null) 
	  pToolset = client.getDefaultToolsetName();
	
	toolsets.addAll(client.getActiveToolsetNames());
	if((pToolset != null) && !toolsets.contains(pToolset))
	  toolsets.add(pToolset);
      }
      catch(PipelineException ex) {
      }

      if(toolsets.isEmpty())
	toolsets.add("-");
	  
      LinkedList<String> vlist = new LinkedList<String>(toolsets);
      Collections.reverse(vlist);	 
      pToolsetField.setValues(vlist);
    
      if(pToolset != null) 
	pToolsetField.setSelected(pToolset);
      else 
	pToolsetField.setSelected("-");
    }
    pToolsetField.addActionListener(this);
    
    master.updateArchiverPluginField(pToolset, pArchiverField);

    updateArchiverFields();
    updateArchiverParams();

    updateButton();
  }

  /**
   * Update the archiver name, version and vendor fields.
   */ 
  private void 
  updateArchiverFields()
  {
    pArchiverField.removeActionListener(this);
    {
      pArchiverField.setPlugin(pArchiver);
      if(pArchiver != null) {
	pArchiverVersionField.setText("v" + pArchiver.getVersionID());
	pArchiverVendorField.setText(pArchiver.getVendor());
      }
      else {
	pArchiverVersionField.setText("-");
	pArchiverVendorField.setText("-");
      }
    }
    pArchiverField.addActionListener(this);
  }
  
  /**
   * Update the UI components associated archiver parameters.
   */ 
  private void 
  updateArchiverParams() 
  {
    Component comps[] = UIFactory.createTitledPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];
    
    boolean first = true;
    if((pArchiver != null) && pArchiver.hasParams()) {
      for(String pname : pArchiver.getLayout()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  if(!first) 
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  ArchiverParam aparam = pArchiver.getParam(pname);
	  if(aparam != null) {
	    if(aparam instanceof BooleanArchiverParam) {
	      Boolean value = (Boolean) aparam.getValue();
	      JBooleanField field = 
		UIFactory.createTitledBooleanField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, sVSize, 
		 aparam.getDescription());
	      field.setValue(value);

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof ByteSizeArchiverParam) {
	      Long value = (Long) aparam.getValue();
	      JByteSizeField field = 
		UIFactory.createTitledByteSizeField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof DirectoryArchiverParam) {
	      String value = (String) aparam.getValue();
	      JPathField field = 
		UIFactory.createTitledPathField
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      // add file browser dialog button here...

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof DoubleArchiverParam) {
	      Double value = (Double) aparam.getValue();
	      JDoubleField field = 
		UIFactory.createTitledDoubleField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof EnumArchiverParam) {
	      EnumArchiverParam eparam = (EnumArchiverParam) aparam;
	      
	      JCollectionField field = 
		UIFactory.createTitledCollectionField
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, eparam.getValues(), sVSize, 
		 aparam.getDescription());
	      
	      field.setSelected((String) eparam.getValue());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof IntegerArchiverParam) {
	      Integer value = (Integer) aparam.getValue();
	      JIntegerField field = 
		UIFactory.createTitledIntegerField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof StringArchiverParam) {
	      String value = (String) aparam.getValue();
	      JTextField field = 
		UIFactory.createTitledEditableTextField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);	      
	    }
	    else {
	      assert(false) : "Unknown archiver parameter type!";
	    }
	  }
	}
	
	first = false;
      }
    }
    else {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }

    UIFactory.addVerticalGlue(tpanel, vpanel);

    pArchiverParamsDrawer.setContents((JComponent) comps[2]);
    pArchiverParamsDrawer.revalidate();
    pArchiverParamsDrawer.repaint();
  }

  /**
   * Update the enable status of the archive button.
   */ 
  private void 
  updateButton()
  {
    pConfirmButton.setEnabled((pToolset != null) && (pArchiver != null));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(cmd.equals("archiver-changed")) 
      doArchiverChanged();
    else if(cmd.equals("toolset-changed")) 
      doToolsetChanged();
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the archiver fields after a change of value.
   */ 
  private void 
  doArchiverChanged()
  {
    BaseArchiver oarchiver = getArchiver();

    String aname = pArchiverField.getPluginName();
    if(aname == null) {
      pArchiver = null;
    }
    else {
      VersionID avid = pArchiverField.getPluginVersionID();
      String avendor = pArchiverField.getPluginVendor();

      if((oarchiver == null) || 
	 !oarchiver.getName().equals(aname) ||
	 !oarchiver.getVersionID().equals(avid) ||
	 !oarchiver.getVendor().equals(avendor)) {
	try {
	  pArchiver = PluginMgrClient.getInstance().newArchiver(aname, avid, avendor);
	  if(oarchiver != null)
	    pArchiver.setParamValues(oarchiver);
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	  pArchiver = null;	    
	}
      }
    }
    
    updateArchiverFields();
    updateArchiverParams();
    updateButton();
  }

  /**
   * Update the archiver plugins available in the current toolset.
   */ 
  private void 
  doToolsetChanged()
  {
    String toolset = pToolsetField.getSelected();
    if(toolset.equals("-")) 
      pToolset = null;
    else
      pToolset = toolset;

    UIMaster.getInstance().updateArchiverPluginField(pToolset, pArchiverField);    

    updateButton();
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
    updateArchiver();
    super.setVisible(isVisible);
  }
    



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -236439332236952251L;
  
  private static final int sTSize = 180;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset environment.
   */ 
  private String  pToolset; 

  /**
   * The current archiver instance.
   */ 
  private BaseArchiver  pArchiver; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The prefix to prepend to the names of created archive volumes.
   */ 
  private JIdentifierField  pPrefixField; 

  /**
   * The The mimimum archive volume size (in bytes).
   */ 
  private JByteSizeField  pMinSizeField; 


  /**
   * The name of the toolset environment. 
   */ 
  private JCollectionField  pToolsetField; 

  /**
   * The name of the archiver plugin. 
   */ 
  private JPluginSelectionField  pArchiverField; 

  /**
   * The revision number of the archiver plugin.
   */ 
  private JTextField  pArchiverVersionField;

  /**
   * The name of the archiver plugin vendor. 
   */ 
  private JTextField  pArchiverVendorField; 


  /**
   * The drawer containing archver parameter components.
   */ 
  private JDrawer pArchiverParamsDrawer; 

  /**
   * The archiver parameter components indexed by parameter name.
   */ 
  private TreeMap<String,Component>  pArchiverParamComponents; 

}
