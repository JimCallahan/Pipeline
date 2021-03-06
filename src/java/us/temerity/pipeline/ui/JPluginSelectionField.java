// $Id: JPluginSelectionField.java,v 1.4 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   S E L E C T I O N   F I E L D                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which displays the name of a Pipeline plugin and provides a popup menu used to 
 * change the plugin selection. <P>
 */
public 
class JPluginSelectionField
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
   *   The plugin menu layout.
   * 
   * @param plugins
   *   The legal plugin vendors, names, revision numbers and supported operating systems.
   */ 
  public 
  JPluginSelectionField
  (
   PluginMenuLayout layout, 
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

    updatePlugins(layout, plugins);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the table of allowable plugins names and revision numbers and the layout of the 
   * selection popup menu.
   * 
   * @param layout
   *   The plugin menu layout.
   * 
   * @param plugins
   *   The legal plugin vendors, names, revision numbers and supported operating systems.
   */ 
  public void 
  updatePlugins
  (
   PluginMenuLayout layout, 
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins
  ) 
  {
    if(layout == null) 
      throw new IllegalArgumentException
	("The plugin menu layout cannot be (null)!");

    if(plugins == null) 
      throw new IllegalArgumentException
	("The set of legal plugin vendors/names/versions/OSs cannot be (null)!");
    pPlugins = plugins;

    pPopup.removeAll();

    {
      JMenuItem item = new JMenuItem("-");
      item.setActionCommand("-:-:-");
      item.addActionListener(this);
      pPopup.add(item);
    }

    for(PluginMenuLayout pml : layout)
      pPopup.add(buildPluginMenu(pml, plugins));
  }

  /**
   * Set the selected plugin name and revision number.
   */ 
  public void
  setPlugin
  (
   BasePlugin plugin
  ) 
  {
    if(plugin != null) {
      setPlugin(plugin.getName(), plugin.getVersionID(), plugin.getVendor());
      pPluginSupports = new TreeSet<OsType>(plugin.getSupports());
    }
    else {
      setPlugin(null, null, null);
    }
  }

  /**
   * Set the selected plugin name and revision number.
   */ 
  public void
  setPlugin
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
  {
    pPluginName = name; 
    if(pPluginName != null) {
      pPluginVersionID = vid; 
      pPluginVendor    = vendor; 
    }
    else {
      pPluginVersionID = null;
      pPluginVendor    = null;
    }
    pPluginSupports = null;

    pLabel.setText((pPluginName != null) ? pPluginName : "-");

    fireActionPerformed(); 
  }

  /**
   * Get the name of the selected plugin.
   * 
   * @return 
   *   The plugin name or <CODE>null</CODE> if undefined.
   */ 
  public String
  getPluginName() 
  {
    return pPluginName;
  }

  /**
   * Get the revision number of the selected plugin.
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> if undefined.
   */ 
  public VersionID
  getPluginVersionID() 
  {
    return pPluginVersionID; 
  }

  /**
   * Get the name of the selected plugin vendor. 
   * 
   * @return 
   *   The vendor name or <CODE>null</CODE> if undefined.
   */ 
  public String
  getPluginVendor()
  {
    return pPluginVendor; 
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
      supports = pPlugins.get(pPluginVendor, pPluginName, pPluginVersionID);
      if(supports == null)
	return null;
    }

    return Collections.unmodifiableSortedSet(supports); 
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
      
      String name = null;
      if(!parts[0].equals("-")) 
	name = parts[0];

      VersionID vid = null;
      if(!parts[1].equals("-")) 
	vid = new VersionID(parts[1]);

      String vendor = null;
      if(!parts[2].equals("-")) 
	vendor = parts[2];
      
      setPlugin(name, vid, vendor);
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
   * Recursively update a plugin menu.
   */ 
  private JMenuItem
  buildPluginMenu
  (
   PluginMenuLayout layout, 
   TripleMap<String,String,VersionID,TreeSet<OsType>> plugins
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      item = new JMenuItem(layout.getTitle());
      item.setActionCommand
	(layout.getName() + ":" + layout.getVersionID() + ":" + layout.getVendor());
      item.addActionListener(this);
   
      Set<VersionID> vids = plugins.keySet(layout.getVendor(), layout.getName());
      item.setEnabled((vids != null) && vids.contains(layout.getVersionID()));
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(buildPluginMenu(pml, plugins));
      item = sub;
    }

    return item;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2880910560766850035L;


  private static final Icon sEnabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIcon.png"));

  private static final Icon sDisabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIconDisabled.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the selected plugin or <CODE>null</CODE> if unspecified.
   */ 
  private String  pPluginName; 
  
  /**
   * The revision number of the selected plugin or <CODE>null</CODE> if unspecified.
   */ 
  private VersionID  pPluginVersionID; 

  /**
   * The name of the vendor of the selected plugin or <CODE>null</CODE> if unspecified.
   */ 
  private String  pPluginVendor; 

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

}
