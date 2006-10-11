// $Id: JBaseExtensionConfigDialog.java,v 1.1 2006/10/11 22:45:41 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T E N S I O N   C O N F I G   D I A L O G                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of dialogs which create/modify server extension configurations.
 */ 
public abstract
class JBaseExtensionConfigDialog
  extends JFullDialog
  implements ComponentListener, DocumentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JBaseExtensionConfigDialog
  (
   Frame owner, 
   String title
  )
  {
    super(owner, title + " Extension Configuration");
    
    /* initialize fields */ 
    {
      pParamComponents = new TreeMap<String,Component>();
      pParamGroupsOpen = new TreeMap<String,Boolean>();
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
	    (tpanel, "Configuration Name:", sTSize, 
	     vpanel, null, sVSize);
	  pNameField = field;

	  field.getDocument().addDocumentListener(this);
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
	    (title + " Extension:", sTSize, JLabel.RIGHT, 
	     "The name of the " + title + " Extension plugin.");

	  tpanel.add(label);

	  JPluginSelectionField field = createExtPluginField(); 
	  pExtensionField = field;
	  
	  field.setActionCommand("extension-changed");
	  field.addActionListener(this);
	  
	  vpanel.add(field);
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	{
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Version:", sTSize, 
	     vpanel, "-", sVSize, 
	     "The revision number of the " + title + " Extension plugin.");
	  pVersionField = field;
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Vendor:", sTSize, 
	     vpanel, "-", sVSize, 
	     "The name of the " + title + " Extension plugin vendor.");
	  pVendorField = field;
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	body.add(comps[2]);
      }

      {
	Box pbox = new Box(BoxLayout.Y_AXIS);
	pParamsBox = pbox;
	
	body.add(pbox);
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
	scroll.getVerticalScrollBar().setUnitIncrement(23);
      }
      
      super.initUI(title + " Extension Configuration:", scroll, 
		   "Confirm", null, null, "Cancel");
      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the extension configuration.
   */ 
  protected void
  setExtensionConfig
  (
   BaseExtensionConfig config
  ) 
  {
    pNameField.getDocument().removeDocumentListener(this);
    if(config != null) {
      pNameField.setText(config.getName());

      pToolset   = config.getToolset();
      pIsEnabled = config.isEnabled();
    }
    else {
      pNameField.setText(null);
 
      pToolset   = null;
      pIsEnabled = false;
    }
    pNameField.getDocument().addDocumentListener(this);
	
    updateExtension();		
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the configuration name.
   */ 
  protected String
  getConfigName() 
  {
    String cname = pNameField.getText();
    if((cname != null) && (cname.length() > 0))
      return cname;
    return null;
  }

  /**
   * Get the extension plugin instance.
   */
  protected BaseExt
  getExtension()
  {
    if(pExtension != null) {
      for(ExtensionParam aparam : pExtension.getParams()) {
	Component comp = pParamComponents.get(aparam.getName()); 
	Comparable value = null;
	if(aparam instanceof BooleanExtensionParam) {
	  JBooleanField field = (JBooleanField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof ByteSizeExtensionParam) {
	  JByteSizeField field = (JByteSizeField) comp;
	  value = field.getValue();	  
	}
	else if(aparam instanceof DoubleExtensionParam) {
	  JDoubleField field = (JDoubleField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof EnumExtensionParam) {
	  JCollectionField field = (JCollectionField) comp;
	  EnumExtensionParam eparam = (EnumExtensionParam) aparam;
	  value = eparam.getValueOfIndex(field.getSelectedIndex());
	}
	else if(aparam instanceof IntegerExtensionParam) {
	  JIntegerField field = (JIntegerField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof StringExtensionParam) {
	  JTextField field = (JTextField) comp;
	  value = field.getText();	  
	}
	else {
	  assert(false) : "Unknown extension parameter type!";
	}

	pExtension.setParamValue(aparam.getName(), value);
      }
    }

    return pExtension;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  protected abstract JPluginSelectionField
  createExtPluginField();
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  protected abstract void 
  updateExtPluginField();


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components.
   */
  private void 
  updateExtension() 
  { 
    MasterMgrClient client = UIMaster.getInstance().getMasterMgrClient();

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
    
    updateExtPluginField();
    updateExtensionFields();
    updateExtensionParams();

    updateButton();
  }

  /**
   * Update the extension name, version and vendor fields.
   */ 
  private void 
  updateExtensionFields()
  {
    pExtensionField.removeActionListener(this);
    {
      pExtensionField.setPlugin(pExtension);
      if(pExtension != null) {
	pVersionField.setText("v" + pExtension.getVersionID());
	pVendorField.setText(pExtension.getVendor());
      }
      else {
	pVersionField.setText("-");
	pVendorField.setText("-");
      }
    }
    pExtensionField.addActionListener(this);
  }
  
  /**
   * Update the UI components associated archiver parameters.
   */ 
  private void 
  updateExtensionParams() 
  {
    pParamsBox.removeAll();
    pParamComponents.clear();

    {
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

      if((pExtension != null) && pExtension.hasParams()) {
	updateParamsHelper(pExtension.getLayout(), hbox, 1);
      }
      else {
	Box dbox = new Box(BoxLayout.Y_AXIS);  
	hbox.add(new JDrawer("Extension Parameters:", dbox, false));
      }

      pParamsBox.add(hbox);
    }

    pParamsBox.revalidate();
    pParamsBox.repaint();    
  }

  
  /**
   * Recursively create drawers containing the extension parameters.
   */ 
  private void 
  updateParamsHelper
  (
   LayoutGroup group, 
   Box sbox, 
   int level
  ) 
  {
    Box dbox = new Box(BoxLayout.Y_AXIS);    
    if(!group.getEntries().isEmpty()) {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      boolean first = true;
      for(String pname : group.getEntries()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  if(!first) 
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 4);

	  int tsize = sTSize-7*level;

	  ExtensionParam aparam = pExtension.getParam(pname);
	  if(aparam != null) {
	    if(aparam instanceof BooleanExtensionParam) {
	      Boolean value = (Boolean) aparam.getValue();
	      JBooleanField field = 
		UIFactory.createTitledBooleanField 
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, sVSize, 
		 aparam.getDescription());
	      field.setValue(value);

	      pParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof ByteSizeExtensionParam) {
	      Long value = (Long) aparam.getValue();
	      JByteSizeField field = 
		UIFactory.createTitledByteSizeField 
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof DoubleExtensionParam) {
	      Double value = (Double) aparam.getValue();
	      JDoubleField field = 
		UIFactory.createTitledDoubleField 
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof EnumExtensionParam) {
	      EnumExtensionParam eparam = (EnumExtensionParam) aparam;
	      
	      JCollectionField field = 
		UIFactory.createTitledCollectionField
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, eparam.getValues(), sVSize, 
		 aparam.getDescription());
	      
	      field.setSelected((String) eparam.getValue());

	      pParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof IntegerExtensionParam) {
	      Integer value = (Integer) aparam.getValue();
	      JIntegerField field = 
		UIFactory.createTitledIntegerField 
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof StringExtensionParam) {
	      String value = (String) aparam.getValue();
	      JTextField field = 
		UIFactory.createTitledEditableTextField 
		(tpanel, aparam.getNameUI() + ":", tsize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pParamComponents.put(pname, field);	      
	    }
	    else {
	      assert(false) : "Unknown extension parameter type!";
	    }
	  }
	}
	
	first = false;
      }

      dbox.add(comps[2]);
    }

    if(!group.getSubGroups().isEmpty())  {
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
	for(LayoutGroup sgroup : group.getSubGroups()) 
	  updateParamsHelper(sgroup, vbox, level+1);

	hbox.add(vbox);
      }

      dbox.add(hbox);
    }

    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      drawer.addActionListener(new UpdateParamGroupsOpen(group.getName(), drawer));
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
      
      Boolean isOpen = pParamGroupsOpen.get(group.getName());
      if(isOpen == null) {
	isOpen = group.isOpen();
	pParamGroupsOpen.put(group.getName(), isOpen);
      }
      drawer.setIsOpen(isOpen);
    }
  }

  /**
   * Update the enable status of the archive button.
   */ 
  private void 
  updateButton()
  {
    String cname = getConfigName();
    BaseExt ext = getExtension();
    pConfirmButton.setEnabled((cname != null) && (pToolset != null) && (ext != null));
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

  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
  public void 
  changedUpdate(DocumentEvent e) {}

  /**
   * Gives notification that there was an insert into the document.
   */
  public void
  insertUpdate
  (
   DocumentEvent e
  )
  {
    updateButton(); 
  }
  
  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void 
  removeUpdate
  (
   DocumentEvent e
  )
  {
    updateButton(); 
  }


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
    if(cmd.equals("extension-changed")) 
      doExtensionChanged();
    else if(cmd.equals("toolset-changed")) 
      doToolsetChanged();
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a new instance of the given extension plugin.
   */ 
  protected abstract BaseExt
  newExtPlugin
  (
   String ename, 
   VersionID evid, 
   String vendor
  )
    throws PipelineException;


  /**
   * Update the appearance of the extension fields after a change of value.
   */ 
  private void 
  doExtensionChanged()
  {
    BaseExt oext = getExtension();
    
    String ename = pExtensionField.getPluginName();
    if(ename == null) {
      pExtension = null;
      
      pParamComponents.clear();
      pParamGroupsOpen.clear();
    }
    else {
      VersionID evid = pExtensionField.getPluginVersionID();
      String evendor = pExtensionField.getPluginVendor();
      if((oext == null) || !oext.getName().equals(ename) ||
	 (evid == null) || !evid.equals(oext.getVersionID()) ||
	 (evendor == null) || !evendor.equals(oext.getVendor())) {
	try {
	  pExtension = newExtPlugin(ename, evid, evendor);
	  if(oext != null) 
	    pExtension.setParamValues(oext);
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);

	  pExtension = null;	    

	  pExtensionField.removeActionListener(this);
	  pExtensionField.setPlugin(null);
	  pExtensionField.addActionListener(this);
	}
      
	pParamComponents.clear();
	pParamGroupsOpen.clear();
      }
    }
    
    updateExtensionFields();
    updateExtensionParams();

    updateButton();
  }

  /**
   * Update the extension plugins available in the current toolset.
   */ 
  private void 
  doToolsetChanged()
  {
    String toolset = pToolsetField.getSelected();
    if(toolset.equals("-")) 
      pToolset = null;
    else
      pToolset = toolset;

    updateExtPluginField(); 

    updateButton();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the table of whether each parameter group is open when the drawer is opened
   * or closed.
   */ 
  private
  class UpdateParamGroupsOpen
    implements ActionListener
  {
    public 
    UpdateParamGroupsOpen
    (
     String name, 
     JDrawer drawer
    ) 
    {
      pName   = name;
      pDrawer = drawer;
    }

    /** 
     * Invoked when an action occurs. 
     */ 
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      pParamGroupsOpen.put(pName, pDrawer.isOpen());
    }
    
    private String   pName;
    private JDrawer  pDrawer;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = -4103423556794887982L;

  protected static final int sTSize = 180;
  protected static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset environment.
   */ 
  protected String  pToolset; 

  /**
   * Whether the extension is currently enabled. 
   */ 
  protected boolean pIsEnabled; 

  /**
   * The current extension instance.
   */ 
  protected BaseExt  pExtension; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the extension configuration.
   */ 
  private JIdentifierField  pNameField; 


  /**
   * The name of the toolset environment. 
   */ 
  private JCollectionField  pToolsetField; 


  /**
   * The name of the extension plugin. 
   */ 
  protected JPluginSelectionField  pExtensionField; 

  /**
   * The revision number of the extension plugin.
   */ 
  private JTextField  pVersionField;

  /**
   * The name of the extension plugin vendor. 
   */ 
  private JTextField  pVendorField; 


  /**
   * The box containing extension parameter components.
   */ 
  private Box  pParamsBox;

  /**
   * The extension parameter components indexed by parameter name.
   */ 
  private TreeMap<String,Component>  pParamComponents; 

  /**
   * Whether the drawers containing the extention parameter components are
   * open indexed by parameter group name.
   */ 
  private TreeMap<String,Boolean>  pParamGroupsOpen; 

}
