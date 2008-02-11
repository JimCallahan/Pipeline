// $Id: JBuilderIDSelectionField.java,v 1.1 2008/02/11 03:16:25 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I D   S E L E C T I O N   F I E L D                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which displays the name of a BuilderCollection plugin and provides a popup menu 
 * used to change the plugin and builder within the collection plugin selection. <P>
 */
public 
class JBuilderIDSelectionField
  extends JPanel
  implements MouseListener, PopupMenuListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   * 
   * @param layout
   *   The builder collection plugin menu layout.
   * 
   * @param builderLayouts
   *   The layout of builders within a specific builder collection 
   *   indexed by builder collection vendors, names, revision numbers.
   * 
   * @param plugins
   *   The legal builder collection plugin vendors, names, revision numbers and
   *   supported operating systems.
   */ 
  public 
  JBuilderIDSelectionField
  (
   PluginMenuLayout layout, 
   TripleMap<String,String,VersionID,LayoutGroup> builderLayouts,
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins
  )
  {
    super();  
    setName("CollectionField");

    setAlignmentY(0.5f);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    addMouseListener(this);

    {
      add(Box.createRigidArea(new Dimension(18, 0)));
      add(Box.createHorizontalGlue());

      {
	JLabel label = new JLabel("-");
	pLabel = label;

	label.setName("CollectionValueTextField");
	label.setHorizontalAlignment(JLabel.CENTER);
	label.addMouseListener(this);

	add(label);
      }
    
      add(Box.createHorizontalGlue());
      add(Box.createRigidArea(new Dimension(2, 0)));
	
      {
	JLabel label = new JLabel(sEnabledIcon);
	pIconLabel = label;
	add(label);
      }

      add(Box.createRigidArea(new Dimension(4, 0)));
    }

    pListenerList  = new EventListenerList();
    pActionCommand = "value-changed";

    pPopup = new JPopupMenu(); 
    pPopup.addPopupMenuListener(this);

    updatePlugins(layout, builderLayouts, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the allowable builder collection plugins, builders and menu layouts.
   * 
   * @param layout
   *   The builder collection plugin menu layout.
   * 
   * @param builderLayouts
   *   The layout of builders within a specific builder collection plugin. 
   * 
   * @param plugins
   *   The legal builder collection plugin vendors, names, revision numbers and
   *   supported operating systems.
   */ 
  public void 
  updatePlugins
  (
   PluginMenuLayout layout, 
   TripleMap<String,String,VersionID,LayoutGroup> builderLayouts,
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins
  ) 
  {
    if(layout == null) 
      throw new IllegalArgumentException
	("The builder collection plugin menu layout cannot be (null)!");

    if(builderLayouts == null) 
      throw new IllegalArgumentException
	("The builder layouts cannot be (null)!");

    if(plugins == null) 
      throw new IllegalArgumentException
	("The set of legal plugin vendors/names/versions/OSs cannot be (null)!");
    pPlugins = plugins;

    pPopup.removeAll();

    {
      JMenuItem item = new JMenuItem("-");
      item.setActionCommand("-:-:-:-");
      item.addActionListener(this);
      pPopup.add(item);
    }

    for(PluginMenuLayout pml : layout)
      pPopup.add(buildPluginMenu(pml, builderLayouts, plugins));
  }

  /**
   * Set the selected builder.
   */ 
  public void
  setBuilderID
  (
   BuilderID builderID
  ) 
  {
    pBuilderID = builderID; 
    if(pBuilderID != null) {
      pLabel.setText(pBuilderID.getName());
      pPluginSupports = 
        pPlugins.get(pBuilderID.getVendor(), pBuilderID.getName(), pBuilderID.getVersionID());
    }
    else {
      pLabel.setText("-"); 
      pPluginSupports = null;
    }

    if(pVersionField != null) {
      if(pBuilderID != null) 
        pVersionField.setText(pBuilderID.getVersionID().toString()); 
      else 
        pVersionField.setText("-");
    }

    if(pVendorField != null) {
      if(pBuilderID != null) 
        pVendorField.setText(pBuilderID.getVendor()); 
      else 
        pVendorField.setText("-");
    }
 
    if(pOsSupportField != null) {
      if(pBuilderID != null) 
        pOsSupportField.setSupports(pPluginSupports); 
      else 
        pOsSupportField.setSupports(null); 
    }

    if(pBuilderNameField != null) {
      if(pBuilderID != null) 
        pBuilderNameField.setText(pBuilderID.getBuilderName()); 
      else 
        pBuilderNameField.setText("-");
    }

    fireActionPerformed(); 
  }

  /**
   * Get the unique builder identity. 
   * 
   * @return 
   *   The builder ID or <CODE>null</CODE> if undefined.
   */ 
  public BuilderID
  getBuilderID() 
  {
    return pBuilderID;
  }

  /**
   * Get the supported operating system types of the selected plugin.
   * 
   * @return 
   *   The vendor name or <CODE>null</CODE> if undefined.
   */ 
  public SortedSet<OsType>
  getPluginSupports() 
  {
    TreeSet<OsType> supports = pPluginSupports; 
    if(supports == null) {
      supports = 
        pPlugins.get(pBuilderID.getVendor(), pBuilderID.getName(), pBuilderID.getVersionID());
      if(supports == null)
	return null;
    }

    return Collections.unmodifiableSortedSet(supports); 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a non-editable JTextField to represent the version of this builder collection.<P> 
   * 
   * This text field will be automatically updated to reflect any changes to the underlying
   * BuilderID.
   * 
   * @param width
   *   The initial width of the field.
   */ 
   public JTextField
   createVersionField
   (
    int width
   ) 
   {
     if(pVersionField == null) 
       pVersionField = UIFactory.createTextField("-", width, JLabel.CENTER);
     return pVersionField;
   }

  /**
   * Create a non-editable JTextField to represent the vendor of this builder collection.<P> 
   * 
   * This text field will be automatically updated to reflect any changes to the underlying
   * BuilderID.
   * 
   * @param width
   *   The initial width of the field.
   */ 
  public JTextField
  createVendorField 
  (
   int width
  ) 
  {
    if(pVendorField == null) 
      pVendorField = UIFactory.createTextField("-", width, JLabel.CENTER);
    return pVendorField;
  }

  /**
   * Create a non-editable JTextField to represent the operating systems supported by the
   * builder collection.<P> 
   * 
   * This text field will be automatically updated to reflect any changes to the underlying
   * BuilderID.
   * 
   * @param width
   *   The initial width of the field.
   */ 
  public JOsSupportField
  createOsSupportField
  (
   int width
  )  
  {
    if(pOsSupportField == null) 
      pOsSupportField = UIFactory.createOsSupportField(width);
    return pOsSupportField;
  }

  /**
   * Create a non-editable JTextField to represent the name of the selected builder within 
   * this builder collection.<P> 
   * 
   * This text field will be automatically updated to reflect any changes to the underlying
   * BuilderID.
   * 
   * @param width
   *   The initial width of the field.
   */ 
  public JTextField
  createBuilderNameField 
  (
   int width
  ) 
  {
    if(pBuilderNameField == null) 
      pBuilderNameField = UIFactory.createTextField("-", width, JLabel.CENTER);
    return pBuilderNameField;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds the specified action listener to receive action events from this field.
   */ 
  public void
  addActionListener
  (
   ActionListener l
  )
  {
    pListenerList.add(ActionListener.class, l);
  }

  /**
   * Removes the specified action listener so that it no longer receives action events
   * from this field.
   */ 
  public void 	
  removeActionListener
  (
   ActionListener l
  )
  {
    pListenerList.remove(ActionListener.class, l);
  }
          
  /**
   * Sets the command string used for action events.
   */ 
  public void 	
  setActionCommand
  (
   String command
  )
  {
    pActionCommand = command; 
  }

  /**
   * Notifies all listeners that have registered interest for notification of action events.
   */
  protected void 
  fireActionPerformed() 
  {
    ActionEvent event = null;

    Object[] listeners = pListenerList.getListenerList();
    int i;
    for(i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i]==ActionListener.class) {
	if(event == null) 
	  event = new ActionEvent(this, pEventID++, pActionCommand);

	((ActionListener)listeners[i+1]).actionPerformed(event);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets whether or not this component is enabled.
   */ 
  public void 
  setEnabled
  (
   boolean enabled
  )
  {
    if(enabled && !isEnabled()) {
      addMouseListener(this);
      pLabel.addMouseListener(this);
      pIconLabel.setIcon(sEnabledIcon);
    }
    else if(!enabled && isEnabled()) {
      removeMouseListener(this);
      pLabel.removeMouseListener(this);
      pIconLabel.setIcon(sDisabledIcon);
    }

    super.setEnabled(enabled);
  }

  /**
   * Sets the foreground color of this component.
   */ 
  public void 
  setForeground
  (
   Color fg
  )
  {
    pFieldForegroundColor = fg;
    if(pLabel != null) 
      pLabel.setForeground(fg);
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
    pLabel.setForeground(pFieldForegroundColor);
    
    {
      String parts[] = e.getActionCommand().split(":");
      if(!parts[0].equals("-") && !parts[1].equals("-") && 
         !parts[2].equals("-") && !parts[3].equals("-")) 
        setBuilderID(new BuilderID(parts[0], new VersionID(parts[1]), parts[2], parts[3]));
      else 
        setBuilderID(null);
    }
  }



  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {} 

  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {}

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  ) 
  {
    pPopup.show(e.getComponent(), e.getX(), e.getY());
  } 

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}
  

  /*-- POPUP MENU LISTNER METHODS ----------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled. 
   */ 
  public void 
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  { 
    pLabel.setForeground(pFieldForegroundColor);
  }
   
  /**
   * This method is called before the popup menu becomes invisible. 
   */ 
  public void
  popupMenuWillBecomeInvisible(PopupMenuEvent e) {} 
  
  /**
   * This method is called before the popup menu becomes visible. 
   */ 
  public void 	
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively update the plugin portion of the menu. 
   */ 
  private JMenuItem
  buildPluginMenu
  (
   PluginMenuLayout layout, 
   TripleMap<String,String,VersionID,LayoutGroup> builderLayouts,
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      Set<VersionID> vids = plugins.keySet(layout.getVendor(), layout.getName());

      LayoutGroup group = 
        builderLayouts.get(layout.getVendor(), layout.getName(), layout.getVersionID());

      if((vids != null) && vids.contains(layout.getVersionID()) && (group != null)) {
        PluginID pluginID = 
          new PluginID(layout.getName(), layout.getVersionID(), layout.getVendor());
        item = buildPluginMenu(layout.getTitle(), group, pluginID); 
      }
      else {
        JMenu sub = new JMenu(layout.getTitle()); 
        sub.setEnabled(false);
        item = sub;
      }
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(buildPluginMenu(pml, builderLayouts, plugins));
      item = sub;
    }

    return item;
  }
  
  /**
   * Recursively update the builder portion of the plugin menu. 
   */ 
  private JMenuItem
  buildPluginMenu
  ( 
   String title, 
   LayoutGroup group, 
   PluginID pluginID
  ) 
  {
    JMenu sub = new JMenu(((title != null) ? title : group.getNameUI()));
    sub.setToolTipText(UIFactory.formatToolTip(group.getDescription()));

    for(LayoutGroup sgroup : group.getSubGroups()) 
      sub.add(buildPluginMenu(null, sgroup, pluginID));

    for(String entry : group.getEntries()) {
      if(entry != null) {
        JMenuItem item = new JMenuItem(entry);
        item.setActionCommand
          (pluginID.getName() + ":" + pluginID.getVersionID() + ":" + 
           pluginID.getVendor() + ":" + entry);
        item.addActionListener(this);
        sub.add(item); 
      }
      else {
        sub.addSeparator();
      }
    }
    
    return sub;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6103451504229016282L;


  private static final Icon sEnabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIcon.png"));

  private static final Icon sDisabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIconDisabled.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique builder identity or <CODE>null</CODE> if unspecified.
   */ 
  private BuilderID  pBuilderID; 
  
  /**
   * The cached supported operating systems of the current plugin.  If <CODE>null</CODE>, 
   * lookup the supported OSs from pPlugins.
   */ 
  private TreeSet<OsType>  pPluginSupports;

  /**
   * The vender, name, revision numbers and supported operating system types 
   * of all legal plugins.
   */ 
  private TripleMap<String,String,VersionID,TreeSet<OsType>>  pPlugins; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The popup menu.
   */ 
  private JPopupMenu  pPopup; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The value. 
   */ 
  private JLabel  pLabel;

  /**
   * The icon.
   */ 
  private JLabel  pIconLabel; 

  /**
   * The foreground color of the text field.
   */ 
  private Color  pFieldForegroundColor;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The action listeners registered to this object.
   */ 
  private EventListenerList pListenerList;

  /**
   * The command string passed to generated action events. 
   */ 
  private String  pActionCommand; 

  /**
   * The unique event ID.
   */ 
  private int pEventID; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Read-only display fields for the builder ID components.
   */ 
  private JTextField       pVersionField; 
  private JTextField       pVendorField; 
  private JOsSupportField  pOsSupportField; 
  private JTextField       pBuilderNameField; 

}
