package us.temerity.pipeline.builder.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.HasBuilderParams.ParamMapping;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M   P A N E L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 *
 */
public class JBuilderParamPanel
  extends JPanel
  implements ComponentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unchecked")
  public
  JBuilderParamPanel
  (
    HasBuilderParams builder,
    int pass
  ) 
    throws PipelineException
  {
    super();
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pStorage = new DoubleMap<String, ParamMapping, Component>();
    pCompToParam = new ListMap<Component, ParamMapping>();
    
    pBuilder = builder;
    AdvancedLayoutGroup layout = builder.getPassLayout(pass);
    SortedMap<String, BuilderParam> params = builder.getParamMap();
    pMappedParams = builder.getMappedParamNames();
    
    if (layout.hasEntries()) {
      int numCol = layout.getNumberOfColumns();
      boolean multiColumn = ( numCol > 1) ? true : false; 
      //Box topBox = new Box(BoxLayout.X_AXIS);
      
      for(int col : layout.getAllColumns()) {
        Box finalBox = new Box(BoxLayout.Y_AXIS);
        String columnName = layout.getColumnNameUI(col);
        boolean isOpen = layout.isOpen(col);
        
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];

        JScrollPane scroll = 
          makeInternalScrollPane(finalBox, new Dimension(sTSize + sVSize + 35, 100));

        boolean first = true;
        for(String pname : layout.getEntries(col)) {
          if(pname == null) 
            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          else {
            BuilderParam bparam = params.get(pname);
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

        JDrawer colDraw = new JDrawer(columnName, scroll, isOpen);  
        this.add(colDraw);
        if(multiColumn && col < numCol)
          this.add(Box.createRigidArea(new Dimension(10, 0)));
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void
  doParam
  (
    BuilderParam bparam,
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
    if (pMappedParams.contains(mapping))
      return;
    if (bparam instanceof ComplexParamAccess) {
      ComplexParamAccess<BuilderParam> cparam = (ComplexParamAccess<BuilderParam>) bparam;
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
	  BuilderParam param = cparam.getParam(entry);
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
    SortedMap<String, BuilderParam> params,
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
	  BuilderParam bparam = params.get(pname);
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
    BuilderParam param
  )
  {
    if (!(param instanceof SimpleParamAccess))
      return false;
    if ( (param instanceof BooleanBuilderParam) ||
         (param instanceof StringBuilderParam) ||
         (param instanceof IntegerBuilderParam) ||
         (param instanceof DoubleBuilderParam) ||
         (param instanceof EnumBuilderParam) ||
         (param instanceof OptionalEnumBuilderParam) ||
         (param instanceof PathBuilderParam) ||
         (param instanceof NodePathBuilderParam) ||
         (param instanceof IdentifierBuilderParam) )
      return true;
    return false;
  }
  
  public Component 
  parameterToComponent
  (
    BuilderParam bparam,
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
      if(bparam instanceof BooleanBuilderParam) {
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
      else if(bparam instanceof DoubleBuilderParam) {
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
      else if(bparam instanceof EnumBuilderParam) {
	EnumBuilderParam eparam = (EnumBuilderParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, displayName, tsize, 
	   vpanel, eparam.getValues(), vsize, 
	   bparam.getDescription());
	
	field.setSelected((String) eparam.getValue());

	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}
	return field;
      }
      else if(bparam instanceof IntegerBuilderParam) {
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
      else if(bparam instanceof StringBuilderParam) {
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
      else if(bparam instanceof PathBuilderParam) {
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
      else if(bparam instanceof NodePathBuilderParam) {
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
      else if(bparam instanceof IdentifierBuilderParam) {
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
      else if(bparam instanceof OptionalEnumBuilderParam) {
	OptionalEnumBuilderParam eparam = (OptionalEnumBuilderParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, displayName, tsize, 
	   vpanel, eparam.getValues(), vsize, 
	   bparam.getDescription());
	
	if (actionCommand != null) {
	  field.addActionListener(this);
	  field.setActionCommand(actionCommand);
	}

	field.setSelected((String) eparam.getValue());

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
    BuilderParam param
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
   ComponentEvent e
  )
  {
//    Box box = (Box) e.getComponent();
//    
//    Dimension size = box.getComponent(1).getSize();
//
//    JPanel spacer = (JPanel) box.getComponent(0);
//    spacer.setMaximumSize(new Dimension(7, size.height));
//    spacer.revalidate();
//    spacer.repaint();
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
	BuilderParam param = pBuilder.getParam(map);
	if (comp == source)
	  continue;
	if (comp instanceof JCollectionField) {
	  JCollectionField field = (JCollectionField) comp;
	  String val = null;
	  Collection<String> values = null;
	  if (param instanceof EnumBuilderParam) {
	    val = (String) ((EnumBuilderParam) param).getValue();
	    values = ((EnumBuilderParam) param).getValues();
	  }
	  else if (param instanceof OptionalEnumBuilderParam) {
	    val = (String) ((OptionalEnumBuilderParam) param).getValue();
	    values = ((OptionalEnumBuilderParam) param).getValues();
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


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  private static JScrollPane
  makeInternalScrollPane
  (
    Component content,
    Dimension size
  )
  {
    JScrollPane scroll = new JScrollPane(content);
    
    scroll.setHorizontalScrollBarPolicy
    			(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setVerticalScrollBarPolicy
    			(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    scroll.setMinimumSize(size);
    scroll.setPreferredSize(size);
    scroll.setMaximumSize(size);

    scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
    
    return scroll;
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
  private HasBuilderParams pBuilder;
  
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
}
