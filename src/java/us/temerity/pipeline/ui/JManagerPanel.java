// $Id: JManagerPanel.java,v 1.4 2004/04/28 23:23:51 jim Exp $

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
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
  JManagerPanel()
  {
    super();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   

    /* panel layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();    
      
      item = new JMenuItem("Add Left");
      item.setActionCommand("add-left");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Add Right");
      item.setActionCommand("add-right");
      item.addActionListener(this);
      pPopup.add(item);  
	  
      pPopup.addSeparator();
      
      item = new JMenuItem("Add Above");
      item.setActionCommand("add-above");
      item.addActionListener(this);
      pPopup.add(item);  
	  
      item = new JMenuItem("Add Below");
      item.setActionCommand("add-below");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();
      
      item = new JMenuItem("Add Tab");
      item.setActionCommand("add-tab");
      item.addActionListener(this);
      pPopup.add(item);  
    }

    /* panel title bar */ 
    {
      JPanel panel = new JPanel();
      pTitlePanel = panel;

      panel.setName("PanelBar");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

      panel.setMinimumSize(new Dimension(200, 29));
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      panel.setPreferredSize(new Dimension(200, 29));

    //   {
// 	JButton btn = new JButton();
// 	pMenuButton = btn;

// 	btn.setName("PanelMenuButton");

// 	Dimension size = new Dimension(14, 19);
// 	btn.setMinimumSize(size);
// 	btn.setMaximumSize(size);
// 	btn.setPreferredSize(size);
	
// 	btn.setActionCommand("show-panel-menu");
//         btn.addActionListener(this);

// 	panel.add(btn);
//       }

      {
	JMenuAnchor anchor = new JMenuAnchor(pPopup);
	panel.add(anchor);
      }

      panel.add(Box.createHorizontalGlue());

      {
	JComboBox combo = new JComboBox();
	pSelectorCombo = combo;

	combo.setRenderer(new JComboBoxCellRenderer());

	Dimension size = new Dimension(155, 19);
	combo.setMinimumSize(size);
	combo.setMaximumSize(size);
	combo.setPreferredSize(size);

	combo.setActionCommand("select-panel-type");
        combo.addActionListener(this);

	combo.addItem("Node Browser");
	combo.addItem("Node Viewer");
	combo.addItem("Node Properties");
	combo.addItem("Node Links");
	combo.addItem("Node Files");
	combo.addItem("Node History");
	combo.addItem("Queue Manager");
	combo.addItem("Job Details");
	combo.addItem("Task Timeline");
	combo.addItem("Task Details");
	combo.addItem("- None -");
	
	panel.add(combo);
      }

      panel.add(Box.createHorizontalGlue());

      {
	JButton btn = new JButton();
	btn.setName("CloseButton");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	btn.setActionCommand("close-panel");
        btn.addActionListener(this);

	panel.add(btn);
      }
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

    if(!(child instanceof JSplitPanel)) {
      add(pTitlePanel);
    }
    
    // .. set pSelectorCombo
    
    add(child);
    
    validate();
    repaint();
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
    int idx = getComponentCount()-1;
    if(idx == -1) 
      return null;
    
    Component old = getComponent(idx);
    removeAll();
    return old;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTNER METHODS --------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    System.out.print("Action: " + e.getActionCommand() + "\n");

    /* dispatch event */ 
    if(e.getActionCommand().equals("show-panel-menu")) {
      Component comp = (Component) e.getSource();
      pPopup.show(comp, 0, 0);
    }
    else if(e.getActionCommand().equals("select-panel-type")) 
      System.out.print("Select Panel: " + pSelectorCombo.getSelectedItem() + "\n");
    else if(e.getActionCommand().equals("add-left"))
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

      left = new JManagerPanel();
      left.setContents(panel);
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(removeContents());
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
      left = new JManagerPanel();
      left.setContents(removeContents());
    }

    JManagerPanel right = null;
    {    
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      right = new JManagerPanel();
      right.setContents(panel);
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

      above = new JManagerPanel();
      above.setContents(panel);
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(removeContents());
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
      above = new JManagerPanel();
      above.setContents(removeContents());
    }

    JManagerPanel below = null;
    {    
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      below = new JManagerPanel();
      below.setContents(panel);
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

      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(panel);

      tabbed.addTab("Empty", mgr);
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
   * The title bar. 
   */ 
  private JPanel  pTitlePanel;

  /**
   * The panel menu button.
   */ 
  private JButton  pMenuButton;

  /**
   * The combo box used to select the panel type.
   */ 
  private JComboBox  pSelectorCombo;

  /**
   * The panel layout popup menu.
   */ 
  private JPopupMenu  pPopup; 
}
