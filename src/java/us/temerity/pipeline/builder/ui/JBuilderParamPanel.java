package us.temerity.pipeline.builder.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.execution.GUIExecution.*;
import us.temerity.pipeline.laf.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M   P A N E L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 *
 */
public 
class JBuilderParamPanel
  extends JTabbedPane
  implements ComponentListener, ActionListener, ChangeListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unchecked")
  public
  JBuilderParamPanel
  (
    BaseUtil builder,
    int pass,
    JBuilderDialog parentDialog
  ) 
    throws PipelineException
  {
    super();
    pParentDialog = parentDialog;
    
    pStorage = new DoubleMap<String, ParamMapping, Component>();
    pMappedStorage = new DoubleMap<String, ParamMapping, Component>();
    pCompToParam = new ListMap<Component, ParamMapping>();
    pViewedPanels = new TreeMap<Integer, Boolean>();
    
    pBuilder = builder;
    AdvancedLayoutGroup layout = builder.getPassLayout(pass);
    SortedMap<String, UtilityParam> params = builder.getParamMap();
    pMappedParams = builder.getMappedParamNames();
    layout = compactLayout(layout);
    
    if (layout.hasEntries()) {
      
      for(int col : layout.getAllColumns()) {
        JPanel finalBox = new JPanel();
        finalBox.setLayout(new BoxLayout(finalBox, BoxLayout.PAGE_AXIS));
        String columnName = layout.getColumnNameUI(col);
        boolean isOpen = layout.isOpen(col);
        
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];

        boolean first = true;
        for(String pname : layout.getEntries(col)) {
          if(pname == null) 
            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          else {
            UtilityParam bparam = params.get(pname);
            if(bparam != null) {
              if(!first) 
        	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
              doParam(bparam, new ParamMapping(pname), tpanel, vpanel, sTSize, sVSize, null, null);
            }
            first = false;
          }   
        } 
        finalBox.add(comps[2]);
        
        for(LayoutGroup group : layout.getSubGroups(col)) {
          Box hbox = new Box(BoxLayout.X_AXIS);
          hbox.addComponentListener(this);
          hbox.add(UIFactory.createSidebar());
          buildSubGroup(params, group, hbox, 1);
          finalBox.add(hbox);
        }
        
        finalBox.add(UIFactory.createFiller(sTSize + sVSize));

        JScrollPane scroll =
          UIFactory.createVertScrollPane(finalBox);
          //makeInternalScrollPane(finalBox, new Dimension(sTSize + sVSize + 50, 600));
        
        this.addTab(null, sTabIcon, scroll, layout.getDescription());
        pViewedPanels.put(col, false);
      }
    }
    if (getTabCount() > 0)
      pViewedPanels.put(1, true);
    
    this.setMinimumSize(new Dimension(sTSize + sVSize+50, 500));
    this.setPreferredSize(new Dimension(sTSize + sVSize+50, 500));
    this.setMaximumSize(new Dimension(sTSize + sVSize+50, Integer.MAX_VALUE));
  }
  
  @SuppressWarnings("unchecked")
  private void
  doParam
  (
    UtilityParam bparam,
    ParamMapping mapping,
    JPanel tpanel,
    JPanel vpanel,
    int tSize,
    int vSize,
    String prefix,
    String actionCommand
  ) 
    throws PipelineException
  {
    if (pMappedParams.contains(mapping)) {
      if (rightSortOfParam(bparam)) {
        Component field = 
          parameterToComponent(bparam, tpanel, vpanel, tSize, vSize, prefix, "mapped");
          field.setEnabled(false);
        pMappedStorage.put(mapping.getParamName(), mapping, field);
      }      
      return;
    }
    if (bparam instanceof ComplexParamAccess) {
      ComplexParamAccess<UtilityParam> cparam = (ComplexParamAccess<UtilityParam>) bparam;
      if (prefix == null)
	prefix = bparam.getNameUI();
      else
	prefix += " " + bparam.getNameUI();
      
      if (cparam.requiresUpdating())
	actionCommand = mapping.getParamName();
      
      boolean first = true;
      for (String entry : cparam.getLayout()) {
	if (entry == null)
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	else {
	  if (!first)
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  UtilityParam param = cparam.getParam(entry);
	  ParamMapping newMapping = new ParamMapping(mapping);
	  newMapping.addKey(entry);
	  doParam(param, newMapping, tpanel, vpanel, tSize, vSize, prefix, actionCommand);
	  first = false;
	}
      }
    }
    else if (rightSortOfParam(bparam)) {
      Component field = 
	parameterToComponent(bparam, tpanel, vpanel, tSize, vSize, prefix, actionCommand);
      pStorage.put(mapping.getParamName(), mapping, field);
      //pSource.put(mapping.getParamName(), mapping, bparam);
      pCompToParam.put(field, mapping);
    }
    else
      throw new PipelineException
	("The parameter named (" + mapping.getParamName() + ") in builder " +
	 "(" + pBuilder.getName() + ") is of a sort that Pipeline does not know " +
	 "how to create a GUI component for.");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G U I    M E T H O D S                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  private void 
  buildSubGroup
  (
    SortedMap<String, UtilityParam> params,
    LayoutGroup group,
    Box sbox,
    int level
  )
    throws PipelineException
  {
    Box dbox = new Box(BoxLayout.Y_AXIS); 
    
    if (!group.getEntries().isEmpty()) {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      boolean first = true;
      for (String pname : group.getEntries()) {
	if (pname == null) 
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	else {
	  UtilityParam bparam = params.get(pname);
	  int tsize = sTSize - 7 * level;
	  
	  if (bparam != null) {
	    if (!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 4);
	    doParam(bparam, new ParamMapping(pname), tpanel, vpanel, tsize, sVSize, null, null);
	  }
	  first = false;
	}   
      }
      dbox.add(comps[2]);
    } 
    
    if (!group.getSubGroups().isEmpty()) {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.addComponentListener(this);

      hbox.add(UIFactory.createSidebar());

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	for(LayoutGroup sgroup : group.getSubGroups()) 
	  buildSubGroup(params, sgroup, vbox, level+1);

	hbox.add(vbox);
      }
      dbox.add(hbox);
    } 
    
    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
      drawer.setIsOpen(group.isOpen());
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean
  rightSortOfParam
  (
    UtilityParam param
  )
  {
    if (!(param instanceof SimpleParamAccess))
      return false;
    if ( (param instanceof BooleanUtilityParam) ||
         (param instanceof StringUtilityParam) ||
         (param instanceof IntegerUtilityParam) ||
         (param instanceof DoubleUtilityParam) ||
         (param instanceof EnumUtilityParam) ||
         (param instanceof OptionalEnumUtilityParam) ||
         (param instanceof PathUtilityParam) ||
         (param instanceof NodePathUtilityParam) ||
         (param instanceof IdentifierUtilityParam) )
      return true;
    return false;
  }
  
  private Component 
  parameterToComponent
  (
    UtilityParam bparam,
    JPanel tpanel,
    JPanel vpanel,
    int tsize,
    int vsize,
    String prefix,
    String actionCommand
  ) 
  {
    if (bparam != null) {
      SimpleParamAccess sparam = (SimpleParamAccess) bparam;
      String displayName = bparam.getNameUI() + ":";
      if (prefix != null)
	displayName = prefix + " " + displayName;
      if(bparam instanceof BooleanUtilityParam) {
	Boolean value = (Boolean) sparam.getValue();
	JBooleanField field = 
	  UIFactory.createTitledBooleanField 
	  (tpanel, displayName, tsize, 
	   vpanel, vsize, 
	   bparam.getDescription());
	field.setValue(value);
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}
	return field;
      }
      else if(bparam instanceof DoubleUtilityParam) {
	Double value = (Double) sparam.getValue();
	JDoubleField field = 
	  UIFactory.createTitledDoubleField 
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;
      }
      else if(bparam instanceof EnumUtilityParam) {
	EnumUtilityParam eparam = (EnumUtilityParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, displayName, tsize, 
	   vpanel, eparam.getValues(), pParentDialog, vsize, 
	   bparam.getDescription());
	
	field.setSelected((String) eparam.getValue());

	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}
	return field;
      }
      else if(bparam instanceof IntegerUtilityParam) {
	Integer value = (Integer) sparam.getValue();
	JIntegerField field = 
	  UIFactory.createTitledIntegerField 
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;
      }
      else if(bparam instanceof StringUtilityParam) {
	String value = (String) sparam.getValue();
	JTextField field = 
	  UIFactory.createTitledEditableTextField 
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;     
      }
      else if(bparam instanceof PathUtilityParam) {
	Path value = (Path) sparam.getValue();
	JPathField field = 
	  UIFactory.createTitledPathField
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field; 
      }
      else if(bparam instanceof NodePathUtilityParam) {
	String value = (String) sparam.getValue();
	JNodeIdentifierField field = 
	  UIFactory.createTitledNodeIdentifierField
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;     
      }
      else if(bparam instanceof IdentifierUtilityParam) {
	String value = (String) sparam.getValue();
	JIdentifierField field = 
	  UIFactory.createTitledIdentifierField
	  (tpanel, displayName, tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;      
      }
      else if(bparam instanceof OptionalEnumUtilityParam) {
	OptionalEnumUtilityParam eparam = (OptionalEnumUtilityParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, displayName, tsize, 
	   vpanel, eparam.getValues(), pParentDialog, vsize, 
	   bparam.getDescription());

	field.setSelected((String) eparam.getValue());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	return field;
      }
      else {
	assert(false) : "Unknown builder parameter type!";
      }
    }
    return null;
  }

  private void 
  updateValueFromParam
  (
    Component comp,
    UtilityParam param
  )
  {
    if (comp instanceof JDoubleField) {
      Double value = (Double) ((SimpleParamAccess) param).getValue();
      ((JDoubleField) comp).removeActionListener(this);
      ((JDoubleField) comp).setValue(value);
      ((JDoubleField) comp).addActionListener(this);
    }
    else if (comp instanceof JIntegerField) {
      Integer value = (Integer) ((SimpleParamAccess) param).getValue();
      ((JIntegerField) comp).removeActionListener(this);
      ((JIntegerField) comp).setValue(value);
      ((JIntegerField) comp).addActionListener(this);
    }
    else if (comp instanceof JTextField) {
      String value = (String) ((SimpleParamAccess) param).getValue();
      ((JTextField) comp).removeActionListener(this);
      ((JTextField) comp).setText(value);
      ((JTextField) comp).addActionListener(this);
    }
    else if (comp instanceof JPathField) {
      Path value = (Path) ((SimpleParamAccess) param).getValue();
      ((JPathField) comp).removeActionListener(this);
      ((JPathField) comp).setPath(value);
      ((JPathField) comp).addActionListener(this);
    }
    else if (comp instanceof JNodeIdentifierField) {
      String value = (String) ((SimpleParamAccess) param).getValue();
      ((JNodeIdentifierField) comp).removeActionListener(this);
      ((JNodeIdentifierField) comp).setText(value);
      ((JNodeIdentifierField) comp).addActionListener(this);
    }
    else if (comp instanceof JIdentifierField) {
      String value = (String) ((SimpleParamAccess) param).getValue();
      ((JIdentifierField) comp).removeActionListener(this);
      ((JIdentifierField) comp).setText(value);
      ((JIdentifierField) comp).addActionListener(this);
    }
  }

  @SuppressWarnings("unchecked")
  public void
  assignValuesToBuilder()
  {
    for (String name : pStorage.keySet()) {
      for (ParamMapping mapping : pStorage.keySet(name)) {
	Component comp = pStorage.get(name, mapping);
	Comparable value = valueFromComponent(comp);
	pBuilder.setParamValue(mapping, value);
      }
    }
  }

  public void
  disableAllComponents()
  {
    for (Component comp : pCompToParam.keySet())
      comp.setEnabled(false);
  }
  
  public int
  numberOfParameters()
  {
    return pCompToParam.size();
  }
  
  public boolean
  allViewed()
  {
    for (Boolean val : pViewedPanels.values()) {
      if (!val )
        return false;
    }
    return true;
  }
  
  /**
   * Build a new layout group by removing all mapped params from the old
   * layout group.
   * 
   * @param oldGroup
   *   The old layout group
   * @return
   *   The new group with all the mapped params removed.
   */
  private AdvancedLayoutGroup
  compactLayout
  (
    AdvancedLayoutGroup oldGroup
  )
  {
    AdvancedLayoutGroup toReturn = new AdvancedLayoutGroup(oldGroup);
    for (ParamMapping mapped : pMappedParams) {
      if (!mapped.hasKeys())
	toReturn.removeEntry(mapped.getParamName());
    }
    return toReturn;
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
  ) 
  {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved
  (
    @SuppressWarnings("unused")
    ComponentEvent e
  ) 
  {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   @SuppressWarnings("unused")
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
  ) 
  {}
  
  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/
  
  @SuppressWarnings("unchecked")
  public void 
  actionPerformed
  (
    ActionEvent e
  )
  {
    String command = e.getActionCommand();
    
    Component source = (Component) e.getSource();
    ParamMapping mapping = pCompToParam.get(source);
    Comparable value = valueFromComponent(source);
    boolean update = pBuilder.setParamValue(mapping, value);
    
    if (update) {
      Map<ParamMapping, Component> comps = pStorage.get(command);
      
      for (ParamMapping map : comps.keySet()) {
	Component comp = comps.get(map);
	UtilityParam param = pBuilder.getParam(map);
	if (comp == source)
	  continue;
	if (comp instanceof JCollectionField) {
	  JCollectionField field = (JCollectionField) comp;
	  String val = null;
	  Collection<String> values = null;
	  if (param instanceof EnumUtilityParam) {
	    val = (String) ((EnumUtilityParam) param).getValue();
	    values = ((EnumUtilityParam) param).getValues();
	  }
	  else if (param instanceof OptionalEnumUtilityParam) {
	    val = (String) ((OptionalEnumUtilityParam) param).getValue();
	    values = ((OptionalEnumUtilityParam) param).getValues();
	  }
	  field.removeActionListener(this);
	  field.setValues(values);
	  field.setSelected(val);
	  field.addActionListener(this);
	} 
	else 	
	  updateValueFromParam(comp, param);
      }
    }
    this.validate();
  }

  /*-- CHANGE LISTENER METHODS -------------------------------------------------------------*/
  
  public void 
  stateChanged
  (
    ChangeEvent e
  )
  {
    int selected = this.getModel().getSelectedIndex();
    if (selected > 0)
      pViewedPanels.put(selected + 1, true);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public static int
  returnWidth()
  {
    return sTSize + sVSize + 50;
  }
  
  @SuppressWarnings("unchecked")
  private static Comparable
  valueFromComponent
  (
    Component field
  )
  {
    if(field instanceof JBooleanField) 
      return ((JBooleanField) field).getValue();
    else if (field instanceof JDoubleField) 
      return ((JDoubleField) field).getValue();
    else if (field instanceof JCollectionField) 
      return ((JCollectionField) field).getSelected();
    else if (field instanceof JIntegerField) 
      return ((JIntegerField) field).getValue();
    else if (field instanceof JTextField) 
      return ((JTextField) field).getText();
    else if (field instanceof JPathField) 
      return ((JPathField) field).getPath();
    else if (field instanceof JNodeIdentifierField) 
      return ((JNodeIdentifierField) field).getText();
    else if (field instanceof JIdentifierField) 
      return ((JIdentifierField) field).getText();
    else
      assert(false) : "Unknown Component Type has been created.  This should be impossible.";
    return null;
  }
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8771701862983719790L;
  private final static int sTSize = 175;
  private final static int sVSize = 175;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The Builder that is having its parameters created in this panel.
   */
  private BaseUtil pBuilder;
  
  /**
   * A map of all the components in a builder pass indexed by the name of the parameters.
   */
  private DoubleMap<String, ParamMapping, Component> pStorage;
  
  /**
   * A map of all the components and the parameters that correspond to them.
   */
  private ListMap<Component, ParamMapping> pCompToParam;
  
  /**
   * The list of Parameters in this builder which are mapped (and thus are not going to be
   * displayed in the panel).
   */
  private Set<ParamMapping> pMappedParams;
  
  private TreeMap<Integer, Boolean> pViewedPanels;
  
  /**
   * A map of all the components in a builder pass indexed by the name of the parameters.
   */
  private DoubleMap<String, ParamMapping, Component> pMappedStorage;
  
  private JBuilderDialog pParentDialog;
  
  private static final Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));

}
