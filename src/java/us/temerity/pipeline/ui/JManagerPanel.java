// $Id: JManagerPanel.java,v 1.1 2004/04/26 23:20:10 jim Exp $

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
    if(getParent() == null)
      setName("RootPanel");

    setContents(child); 

    /* panel layout popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
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
	sub = new JMenu("Panel Layout");
	pPopup.add(sub); 
	
	item = new JMenuItem("Horizontal Split");
	item.setActionCommand("horzontal-split");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Vertical Split");
	item.setActionCommand("vertical-split");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();
	
	item = new JMenuItem("Add Tabs");
	item.setActionCommand("add-tabs");
	item.addActionListener(this);
	sub.add(item);  
      }

      pPopup.addSeparator();
      pPopup.addSeparator();
	
      item = new JMenuItem("Close Panel");
      item.setActionCommand("close-panel");
      item.addActionListener(this);
      pPopup.add(item);  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the panel popup menu. <P> 
   * 
   * @param event
   *   The mouse event causing the menu to be shown.
   */ 
  public void 
  showPopup
  (
   MouseEvent event
  ) 
  {
  }


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
    if(getComponentCount() == 1) {
      Component old = getComponent(0);
      old.removeMouseListener(this);
    }

    add(child, 0);
    validate();

    addMouseListener(this);
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
      int on  = (MouseEvent.BUTTON3_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);
      
      int off = (MouseEvent.BUTTON1_DOWN_MASK | 
		 MouseEvent.BUTTON2_DOWN_MASK | 
		 MouseEvent.SHIFT_DOWN_MASK |
		 MouseEvent.ALT_DOWN_MASK);

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
    /* dispatch event */ 
    if(e.getActionCommand().equals("horzontal-split"))
      doHorizontalSplit();

    // ...

    System.out.print("Action: " + e.getActionCommand() + "\n");

  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Split the panel into left and right sub-panels.
   */ 
  private void 
  doHorizontalSplit()
  {
    assert(getComponentCount() == 1);
    


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
