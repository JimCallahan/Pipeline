// $Id: JManagerPanel.java,v 1.3 2004/04/28 00:43:23 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   S T E M   P A N E L                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A manager of the high-level panel components which make up the main frame. <P> 
 * 
 * The contents of the panel may be changed by the user to one of a number of predefined 
 * Pipeline panel types.  The panel may be divided into two or more child 
 * <CODE>JManagerPanel</CODE>s using either a {@link JSplitPane JSplitPane} or a 
 * {@link JTabbedPane JTabbedPane}.  Unless this panel is the root panel of the main frame,
 * the user may also destroy the panel possibly causing the parent <CODE>JSplitPane</CODE> 
 * or <CODE>JTabedPane</CODE> to be destroyed as well.
 */ 
public 
class JManagerPanel
  extends JPanel
  implements MouseListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct the root manager panel. <P> 
   * 
   * @param child
   *   The iniitial child component.
   */
  JManagerPanel
  (
   Component child
  )
  {
    super(new BorderLayout());
    setContents(child); 

    /* panel layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();    
      
      item = new JMenuItem("Node Browser");
      item.setActionCommand("node-browser");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Node Viewer");
      item.setActionCommand("node-viewer");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem("Node Properties");
      item.setActionCommand("node-properties");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem("Node Links");
      item.setActionCommand("node-links");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Node Files");
      item.setActionCommand("node-files");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Node History");
      item.setActionCommand("node-history");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem("Queue Manager");
      item.setActionCommand("queue-manager");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Job Details");
      item.setActionCommand("job-details");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();
      
      item = new JMenuItem("Task Timeline");
      item.setActionCommand("task-timeline");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Task Details");
      item.setActionCommand("task-details");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      {	
	JMenu layout = new JMenu("Panel Layout");
	pPopup.add(layout); 
	
	{	
	  JMenu horz = new JMenu("Horizontal Split");
	  layout.add(horz); 
	  
	  item = new JMenuItem("Add Left");
	  item.setActionCommand("add-left");
	  item.addActionListener(this);
	  horz.add(item);  
	
	  item = new JMenuItem("Add Right");
	  item.setActionCommand("add-right");
	  item.addActionListener(this);
	  horz.add(item);  
	}

	{	
	  JMenu vert = new JMenu("Vertical Split");
	  layout.add(vert); 
	  
	  item = new JMenuItem("Add Above");
	  item.setActionCommand("add-above");
	  item.addActionListener(this);
	  vert.add(item);  
	
	  item = new JMenuItem("Add Below");
	  item.setActionCommand("add-below");
	  item.addActionListener(this);
	  vert.add(item);  
	}
	
	layout.addSeparator();
	
	item = new JMenuItem("Add Tab");
	item.setActionCommand("add-tab");
	item.addActionListener(this);
	layout.add(item);  
      }

      pPopup.addSeparator();
	
      item = new JMenuItem("Close Panel");
      item.setActionCommand("close-panel");
      item.addActionListener(this);
      pPopup.add(item);  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set sole child component of the panel.
   * 
   * @param child
   *   The new child component.
   */ 
  public void 
  setContents
  (
   Component child
  ) 
  { 
    if(child == null)
      return;

    removeContents();

    add(child, 0);
    validate();

    addMouseListener(this);
  }
    
  /**
   * Remove the sole child component of the panel.
   * 
   * @return 
   *   The removed child or <CODE>null</CODE> if there was no child.
   */ 
  public Component
  removeContents() 
  { 
    if(getComponentCount() == 1) {
      Component old = getComponent(0);
      old.removeMouseListener(this);
      remove(0);
      
      return old;
    }

    assert(getComponentCount() == 0);
    return null;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTNER METHODS ---------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked
  (
   MouseEvent e
  )
  {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  )
  {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  )
  {}
  
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    if(e.getButton() == MouseEvent.BUTTON3) {
      int on  = (MouseEvent.BUTTON3_DOWN_MASK);
      
      int off = (MouseEvent.BUTTON1_DOWN_MASK | 
		 MouseEvent.BUTTON2_DOWN_MASK | 
		 MouseEvent.SHIFT_DOWN_MASK |
		 MouseEvent.ALT_DOWN_MASK |
		 MouseEvent.CTRL_DOWN_MASK);

      if((mods & (on | off)) == on) 
	pPopup.show(e.getComponent(), e.getX(), e.getY());
    }
  }
  
  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  )
  {}



  /*-- ACTION LISTNER METHODS --------------------------------------------------------------*/

  /* Invoked when an action occurs. */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    System.out.print("Action: " + e.getActionCommand() + "\n");

    /* dispatch event */ 
    if(e.getActionCommand().equals("add-left"))
      doAddLeft();
    else if(e.getActionCommand().equals("add-right"))
      doAddRight();
    else if(e.getActionCommand().equals("add-above"))
      doAddAbove();
    else if(e.getActionCommand().equals("add-below"))
      doAddBelow();
    else if(e.getActionCommand().equals("add-tab"))
      doAddTab();

    // ...

    else if(e.getActionCommand().equals("close-panel"))
      doClosePanel();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Split the panel horizontally adding a new empty panel on the left.
   */ 
  private void 
  doAddLeft()
  {
    JManagerPanel left = null;
    {
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      left = new JManagerPanel(panel);
    }

    JManagerPanel right = null;
    {    
      Component old = removeContents();
      right = new JManagerPanel(old);
    }

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
  }

  /**
   * Split the panel horizontally adding a new empty panel on the right.
   */ 
  private void 
  doAddRight()
  {
    JManagerPanel left = null;
    {
      Component old = removeContents();
      left = new JManagerPanel(old);
    }

    JManagerPanel right = null;
    {    
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      right = new JManagerPanel(panel);
    }

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
  }

  /**
   * Split the panel vertically adding a new empty panel above.
   */ 
  private void 
  doAddAbove()
  {
    JManagerPanel above = null;
    {
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      above = new JManagerPanel(panel);
    }

    JManagerPanel below = null;
    {    
      Component old = removeContents();
      below = new JManagerPanel(old);
    }

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
  }

  /**
   * Split the panel vertically adding a new empty panel below.
   */ 
  private void 
  doAddBelow()
  {
    JManagerPanel above = null;
    {
      Component old = removeContents();
      above  = new JManagerPanel(old);
    }

    JManagerPanel below = null;
    {    
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      below = new JManagerPanel(panel);
    }

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
  }

  /**
   * Add a new empty tab to the child tabbed pane. <P> 
   * 
   * If the current child isn't already a tabbed pane, a new tabbed pane is created and
   * the current child is moved to the first tab of the new tabbed pane.
   */ 
  private void 
  doAddTab()
  {

    JTabbedPane tabbed = null;
    {
      Component child = getComponent(0);
      if(child instanceof JTabbedPane) {
	tabbed = (JTabbedPane) child;
      }
      else {
	tabbed = new JTabbedPane();
	tabbed.addTab("Previous", removeContents());

	setContents(tabbed);
      }
    }
    assert(tabbed != null);

    {    
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      tabbed.addTab("Empty", new JManagerPanel(panel));
    }
  }

  // ...


  /**
   * 
   */ 
  private void 
  doClosePanel()
  {
    Container parent = getParent();


    if(parent == null) {
      System.out.print("Parent: (null)\n");
      return;
    }
    System.out.print("Parent: " + parent.getClass().getName() + "\n");

    /* replace the parent split pane with the other child */ 
    if(parent instanceof JSplitPanel) {
      JSplitPanel split = (JSplitPanel) parent;

      Component live = null;
      switch(split.getOrientation()) {
      case JSplitPane.HORIZONTAL_SPLIT:
	if(split.getLeftComponent() == this) 
	  live = split.getRightComponent();
	else if(split.getRightComponent() == this) 
	  live = split.getLeftComponent();
	else 
	  assert(false);
	break;

      case JSplitPane.VERTICAL_SPLIT:
	if(split.getTopComponent() == this) 
	  live = split.getBottomComponent();
	else if(split.getBottomComponent() == this) 
	  live = split.getTopComponent();
	else 
	  assert(false);	
	break;

      default:
	assert(false);
      }
      assert(live != null);
      
      split.removeAll();
      
      JManagerPanel liveMgr = (JManagerPanel) live;
      JManagerPanel grandpa = (JManagerPanel) split.getParent();
      grandpa.setContents(liveMgr.removeContents());
    }
    else {
      System.out.print("Unknown...\n");      
    }

    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -3122417485809218152L;



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel layout popup menu.
   */ 
  private JPopupMenu  pPopup; 
}
