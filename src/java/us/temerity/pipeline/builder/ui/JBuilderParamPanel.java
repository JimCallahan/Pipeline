/**
 * 
 */
package us.temerity.pipeline.builder.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M   P A N E L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * @author jesse
 *
 */
public class JBuilderParamPanel
  extends JPanel
  implements ComponentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  JBuilderParamPanel
  (
    String name,
    SortedMap<String, BuilderParam> params, 
    AdvancedLayoutGroup layout
  ) 
    throws PipelineException
  {
    if (!layout.hasEntries())
      throw new PipelineException("An empty layout was passed by a builder with the given name " +
      		"(" + name + ")."); 
    
    pSource = params;
    pStorage = new TreeMap<String, Component>();
    
    int numCol = layout.getNumberOfColumns();
    boolean multiColumn = ( numCol > 1) ? true : false; 

    Box topBox = new Box(BoxLayout.X_AXIS);
    topBox.add(UIFactory.createSidebar());
    if (multiColumn)
      topBox.add(Box.createRigidArea(new Dimension(10, 0)));
    
    int currentColumn = 1;
    for(int col : layout.getAllColumns()) {
      JScrollPane scroll;
      
      Box finalBox = new Box(BoxLayout.Y_AXIS);
      
      finalBox.add(UIFactory.createPanelLabel(layout.getColumnNameUI(col)  + ":"));
      finalBox.add(Box.createRigidArea(new Dimension(0,3)));
      
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      {
        scroll = new JScrollPane(finalBox);
        
        scroll.setHorizontalScrollBarPolicy
        			(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy
        			(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        Dimension size = new Dimension(sTSize + sVSize + 52, 500);
        scroll.setMinimumSize(size);
        scroll.setPreferredSize(size);
  
        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
      boolean first = true;
      for(String pname : layout.getEntries(col)) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  BuilderParam bparam = params.get(pname);
	  if(bparam != null) {
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    Component field = parameterToComponent(bparam, tpanel, vpanel, sTSize, sVSize);
	    pStorage.put(pname, field);
	  }
	  first = false;
	}  // else : if(pname == null) 
      } // for(String pname : layout.getEntries(col))
      finalBox.add(comps[2]);

      for(LayoutGroup group : layout.getSubGroups(col)) {
	buildSubGroup(params, group, finalBox, 1, pStorage);
      }
      
      finalBox.add(UIFactory.createFiller(sTSize + sVSize));

      topBox.add(scroll);
      if(multiColumn)
	topBox.add(Box.createRigidArea(new Dimension(10, 0)));
      currentColumn++;
    }
    topBox.add(UIFactory.createSidebar());
   this.add(topBox); 
  }
  
  private void 
  buildSubGroup
  (
    SortedMap<String, BuilderParam> params,
    LayoutGroup group,
    Box sbox,
    int level,
    TreeMap<String, Component> storage
  )
    throws PipelineException
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

	  BuilderParam bparam = params.get(pname);
	  if(bparam != null) {
	    Component field = parameterToComponent(bparam, tpanel, vpanel, tsize, sVSize);
	    storage.put(pname, field);
	  }
	  first = false;
	}  // else : if(pname == null) 
      }
      dbox.add(comps[2]);
    } //if(!group.getEntries().isEmpty())
    
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
	  buildSubGroup(params, sgroup, vbox, level+1, storage);

	hbox.add(vbox);
      }
      dbox.add(hbox);
    } //if(!group.getSubGroups().isEmpty())
    
    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      //drawer.addActionListener(new UpdateParamGroupsOpen(group.getName(), drawer));
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
      
//      Boolean isOpen = pParamGroupsOpen.get(group.getName());
//      if(isOpen == null) {
//	isOpen = group.isOpen();
//	pParamGroupsOpen.put(group.getName(), isOpen);
//      }
      //drawer.setIsOpen(isOpen);
      drawer.setIsOpen(false);
    }
  }
  
  public static Component 
  parameterToComponent
  (
    BuilderParam bparam,
    JPanel tpanel,
    JPanel vpanel,
    int tsize,
    int vsize
  ) 
    throws PipelineException
  {
    if (bparam != null) {
      SimpleParamAccess sparam = (SimpleParamAccess) bparam;
      if(bparam instanceof BooleanBuilderParam) {
	Boolean value = (Boolean) sparam.getValue();
	JBooleanField field = 
	  UIFactory.createTitledBooleanField 
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, vsize, 
	   bparam.getDescription());
	field.setValue(value);
	return field;
      }
      else if(bparam instanceof DoubleBuilderParam) {
	Double value = (Double) sparam.getValue();
	JDoubleField field = 
	  UIFactory.createTitledDoubleField 
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;
      }
      else if(bparam instanceof EnumBuilderParam) {
	EnumBuilderParam eparam = (EnumBuilderParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, eparam.getValues(), vsize, 
	   bparam.getDescription());

	field.setSelected((String) eparam.getValue());

	return field;
      }
      else if(bparam instanceof IntegerBuilderParam) {
	Integer value = (Integer) sparam.getValue();
	JIntegerField field = 
	  UIFactory.createTitledIntegerField 
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;
      }
      else if(bparam instanceof StringBuilderParam) {
	String value = (String) sparam.getValue();
	JTextField field = 
	  UIFactory.createTitledEditableTextField 
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;     
      }
      else if(bparam instanceof PathBuilderParam) {
	Path value = (Path) sparam.getValue();
	JPathField field = 
	  UIFactory.createTitledPathField
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field; 
      }
      else if(bparam instanceof NodePathBuilderParam) {
	String value = (String) sparam.getValue();
	JNodeIdentifierField field = 
	  UIFactory.createTitledNodeIdentifierField
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;     
      }
      else if(bparam instanceof IdentifierBuilderParam) {
	String value = (String) sparam.getValue();
	JIdentifierField field = 
	  UIFactory.createTitledIdentifierField
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;      
      }
      else if(bparam instanceof OptionalEnumBuilderParam) {
	OptionalEnumBuilderParam eparam = (OptionalEnumBuilderParam) bparam;

	JCollectionField field = 
	  UIFactory.createTitledCollectionField
	  (tpanel, bparam.getNameUI() + ":", tsize, 
	   vpanel, eparam.getValues(), vsize, 
	   bparam.getDescription());

	field.setSelected((String) eparam.getValue());

	return field;
      }
      else if(bparam instanceof MayaContextBuilderParam) {
	MayaContext value = (MayaContext) sparam.getValue();
	JMayaContextField field = 
	  UIFactory.createTitledMayaContextField
	  (tpanel, bparam.getNameUI(), tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;	      
      }
      else if(bparam instanceof UtilContextBuilderParam) {
	UtilContext value = (UtilContext) sparam.getValue();
	JUtilContextField field = 
	  UIFactory.createTitledUtilContextField
	  (tpanel, bparam.getNameUI(), tsize, 
	   vpanel, value, vsize, 
	   bparam.getDescription());

	return field;	      
      }
      else if(bparam instanceof ListBuilderParam) {
	ListBuilderParam mparam = (ListBuilderParam) bparam;
	Set<String> values = mparam.getValues();
	Set<String> selectedValues = mparam.getSelectedValues();
	ArrayList<String> layout = mparam.getLayout();
	TreeMap<String, String> tooltips = new TreeMap<String, String>();
	JMultiEnumField field = 
	  UIFactory.createTitledMultiEnumField
	  (tpanel, bparam.getNameUI(), tsize,
	   vpanel, selectedValues, values, vsize,
	   layout, tooltips);
	
	return field;
      }
      else {
	assert(false) : "Unknown builder parameter type!";
      }
    }
    return null;
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
  
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8771701862983719790L;
  private final static int sTSize = 150;
  private final static int sVSize = 200;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A map of all the components in a builder pass indexed by the name of the parameters.
   */
  private TreeMap<String, Component> pStorage;
  
  /**
   * A map of the all the builder params in a builder pass 
   * indexed by the name of the parameters.
   */
  private SortedMap<String, BuilderParam> pSource;
}
